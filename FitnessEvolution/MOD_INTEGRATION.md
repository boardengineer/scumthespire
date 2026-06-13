# FitnessEvolution — Mod Integration

Everything you need to wire `BattleAiMod` into the GP driver. ~10 minutes.

Branch: **`2-level-GP-Jenetics`** on `Flooflez/scumthespire`.

## 1. Build

From `FitnessEvolution/`:

```bash
mvn test      # expect: Tests run: 36, Failures: 0  →  BUILD SUCCESS
mvn package   # produces target/FitnessEvolution.jar
```

Requires **Java 17+**. `java -version` to check.

## 2. How the handshake works

The jar is **generation-agnostic** — every call does the same thing:

```
┌──────────────────────────────────────────────────────────────────┐
│ 1.  mod writes ipc/ModOutput.txt with K scored expressions       │
│     (K can be anywhere from 1 to 20; fitness required per line)  │
│                                                                  │
│ 2.  java -jar target/FitnessEvolution.jar --run-id=<your-id>     │
│                                                                  │
│ 3.  jar reads ModOutput, produces 20 expressions in              │
│     ipc/JeneticsOutput.txt, increments runs/<your-id>.step       │
│                                                                  │
│ 4.  mod reads JeneticsOutput, runs one battle per expression,    │
│     writes 20 FITNESS/expr pairs back to ModOutput, loops to 2.  │
└──────────────────────────────────────────────────────────────────┘
```

**What the jar does depends on how many expressions the mod provided:**

| K (input count) | Jar's behavior |
|---|---|
| `0` | Error, exit 2 (`"ModOutput.txt is empty"`) |
| `1 ≤ K < 20` | **Warmup**: echo the K expressions + fill with (20 − K) random trees. No evolution this step. Fitness values from ModOutput are ignored. |
| `K = 20` | **Evolve**: one GP step — elitism (top 2) + tournament + crossover + mutation → next generation. |
| `K > 20` | Truncate to top 20 by fitness (warn on stderr), then evolve. |

**Stop cleanly:** write `END` as the first non-blank line of ModOutput.txt and call the jar once more. It logs and exits 0 without advancing the step counter.

**Working directory matters** — launch the jar from `FitnessEvolution/` so relative paths (`ipc/`, `runs/`, `logs/`) resolve correctly.

## 3. File formats

**`ipc/JeneticsOutput.txt`** (we write, you read): 20 lines, one MathExpr each. Operators `+ − * /` only. Variables from `ipc/FeatureBank.txt`. Constants are numeric literals.

**`ipc/ModOutput.txt`** (you write): K FITNESS/expression pairs, **same order** the jar wrote them (when applicable), **or** the literal `END`.

```
FITNESS=12.5
<expr that produced that fitness>
FITNESS=-3.0
<another expr>
...
```

- FITNESS **must be finite**. Map `NaN`/`±Infinity` → `0` on your side.
- Echo the raw expression string as you received it (safest). Our parser accepts MathExpr-parse round-trips too (we canonicalize internally), but verbatim is simplest.
- Blank lines between pairs are ignored.

## 4. Bootstrapping a new run

You have two choices for how to seed the initial population:

**Option A — seed with just your template (or any handful of expressions).**

```bash
cat > ipc/ModOutput.txt <<'EOF'
FITNESS=0
SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING - 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED
EOF
java -jar target/FitnessEvolution.jar --run-id=myrun
```

The jar emits 20 expressions (your 1 + 19 random fill-ins). Run 20 combats, score them, write all 20 pairs back to ModOutput, call again.

**Option B — seed with a bigger set (up to 20) you already have.**

Write K ≤ 20 FITNESS/expr pairs in ModOutput (fitness values ignored during warmup). Jar will fill to 20.

## 5. `--run-id` and reproducibility

Required-ish (defaults to `default` if omitted). Different run IDs have independent step counters — useful for running parallel experiments without interference.

RNG seed for each call is computed as `baseSeed + step` where `step` is the current value in `runs/<id>.step`. So:
- Default: `--seed=42`, step starts at 0, seeds are 42, 43, 44, … on successive calls.
- Override base seed: `--seed=N` on any call.
- Reset a run: delete `runs/<id>.step`. The next call starts from step 0 again.

## 6. Exit codes

| Exit | Meaning | What to do |
|---:|---|---|
| `0` | success | read `ipc/JeneticsOutput.txt` |
| `2` | bad input | read stderr — names the file + line |
| `3` | IO error | check disk / permissions |
| `4` | bug on our side | send me the stderr stack trace |

## 7. Common stderr messages

- **`ModOutput.txt does not exist`** — you called the jar before writing ModOutput.
- **`ModOutput.txt has 0 scored expressions — cannot evolve from empty input`** — ModOutput exists but has no FITNESS/expr pairs (also triggers for all-blank files).
- **`ModOutput.txt has K > 20 entries; truncating to top 20 by fitness`** — informational warning; we proceed with the top 20.
- **`non-finite fitness 'NaN'`** — clamp NaN/Infinity to `0` before writing.
- **`cannot parse expression from ModOutput.txt: <expr>`** — expression isn't valid MathExpr syntax. Check for typos, unbalanced parens.

## 8. Reset / Debug

| Need | Do |
|---|---|
| Start a run over from scratch | delete `runs/<id>.step` |
| See per-call summaries | `cat logs/evolution_log.txt` |
| Smoke-test engine without your mod | `mvn -q exec:java -Dexec.mainClass=fitnessevolution.OfflineMockRun` |
| Full docs | [`README.md`](README.md) |

## 9. Poll mode — `--poll`

Instead of calling the jar once per generation, you can start it as a
**long-running daemon** that watches `ipc/ModOutput.txt` for a `READY`
flag and processes each handshake automatically. This is the
"set-it-and-forget-it" option: your mod just reads/writes the IPC files;
we handle the loop.

### Start

```bash
java -jar target/FitnessEvolution.jar --poll --run-id=<id>
# optional: --poll-interval-ms=100   (default 100 ms)
# optional: --seed=<long>            (default 42)
```

`--run-id` behaves the same as in one-shot mode (step counter in
`runs/<id>.step`, used to derive the per-iteration RNG seed).

### The handshake (READY on last line, symmetric wipe)

Each cycle involves both files; each file has exactly one writer at a
time so no locking is needed:

```
┌─ mod writes ipc/ModOutput.txt ──────────────────────────────┐
│   <FITNESS=... / expr lines>                                 │
│   READY                ← must be the LAST non-blank line     │
└──────────────────────────────────────────────────────────────┘
                          ▼
  we poll (every --poll-interval-ms), see READY on last line
                          ▼
  we read + process (warmup / evolve / truncate per §2)
                          ▼
┌─ we write ipc/JeneticsOutput.txt (atomic rename) ───────────┐
│   <20 MathExpr lines>                                        │
│   READY                ← our last line                       │
└──────────────────────────────────────────────────────────────┘
                          ▼
  we truncate ipc/ModOutput.txt to empty
                          ▼
  mod polls JeneticsOutput, sees READY on last line
                          ▼
  mod reads the 20 expressions, truncates JeneticsOutput to empty,
  runs 20 battles, writes next ipc/ModOutput.txt with fresh data +
  READY on last line ──────── (repeat)
```

### Mod-side responsibilities under `--poll`

1. **Write `READY` as the last non-blank line of `ModOutput.txt`.** Do
   not flush `READY` before the rest of the content. Either
   (a) write data first and `READY` last with a final flush, or (b)
   write to `ModOutput.txt.tmp` and rename — either works because we
   use last-line detection.
2. **Poll `JeneticsOutput.txt`** for `READY` on its last non-blank line.
3. **After reading**, truncate `JeneticsOutput.txt` to empty. This is
   your half of the symmetric wipe — it prevents stale-READY bugs if
   something goes wrong on either side.
4. **Never write to `JeneticsOutput.txt`** other than truncating.
5. **Never read `ModOutput.txt`** after writing it — it's our file to
   read and wipe.

### Stop

- **Foreground** (typical): Ctrl-C. We catch SIGINT, finish the current
  iteration cleanly, and exit 0.
- **Background**: `kill <pid>` (SIGTERM). Same behaviour.
- A second Ctrl-C / SIGKILL forces immediate termination.

### Error handling in the loop

Malformed ModOutput (parse error, non-finite fitness, unparseable
expression, zero FITNESS pairs) is **not fatal** in poll mode:

- The raw content is logged to `logs/evolution_log.txt` + stderr
- `ModOutput.txt` is truncated so the bad READY isn't re-processed in
  a loop
- The step counter does **not** advance
- The daemon keeps polling; next valid READY proceeds normally

IO errors (disk full, permission denied) are fatal: we log and exit 3.

### Notes

- Poll interval of 100 ms adds at most ~100 ms of latency per handshake.
  Battles take seconds, so this is negligible.
- `END` as a line in ModOutput is treated as malformed input in poll
  mode — logged, wiped, ignored. Use Ctrl-C / SIGTERM to stop instead.

## 10. First-run sanity check (before wiring to your mod)

```bash
# Seed with a template
cat > ipc/ModOutput.txt <<'EOF'
FITNESS=0
SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING - 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED
EOF
java -jar target/FitnessEvolution.jar --run-id=smoke    # step 0: warmup

# Fake 20 fitness values as if you had run 20 combats
python3 -c "
lines = open('ipc/JeneticsOutput.txt').read().splitlines()
open('ipc/ModOutput.txt','w').write(
    ''.join(f'FITNESS={-i*0.5}\n{e}\n' for i,e in enumerate(lines)))
"
java -jar target/FitnessEvolution.jar --run-id=smoke    # step 1: evolve

# Stop
echo END > ipc/ModOutput.txt
java -jar target/FitnessEvolution.jar --run-id=smoke    # END, counter unchanged
```

If all three calls return exit 0 and `runs/smoke.step` contains `2`, you're green.

---

Ping me on anything that bites.
