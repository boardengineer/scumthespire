# FitnessEvolution — Poll Mode Setup

How to run our GP driver as a long-running daemon. Pull the latest branch,
build, start the daemon, and leave it running while your mod handles the
file I/O.

Branch: **`2-level-GP-Jenetics`** on `Flooflez/scumthespire`.

---

## 1. Pull and build

From the repo root:

```bash
git pull upstream 2-level-GP-Jenetics    # or whatever you track Flooflez as
cd FitnessEvolution
mvn test                                  # expect: Tests run: 36, BUILD SUCCESS
mvn package                               # produces target/FitnessEvolution.jar
```

Requires Java 17+. Check with `java -version`.

---

## 2. Start the daemon

```bash
java -jar target/FitnessEvolution.jar --poll --run-id=<your-run-id>
```

Defaults you can override:

```bash
# All flags shown at their default values
java -jar target/FitnessEvolution.jar \
    --poll \
    --run-id=default \
    --seed=42 \
    --poll-interval-ms=100
```

- `--run-id=<id>` — isolates step counters (`runs/<id>.step`) for parallel runs. Use any string; a timestamp works well.
- `--seed=<long>` — base RNG seed. Effective per-step seed = `baseSeed + step`.
- `--poll-interval-ms=<int>` — how often to check `ipc/ModOutput.txt`.

Leave it running. Ctrl-C to stop (foreground). If you backgrounded it,
`kill <pid>` (SIGTERM) does the same. The daemon finishes the current
iteration cleanly before exiting. A second Ctrl-C force-kills.

---

## 3. IMPORTANT: READY flag is on the LAST line, not the first

This changed since our initial discussion. Both sides now put `READY` as the
**last non-blank line** of their file.

Why: it removes the need for atomic writes on your side — you can write
data line-by-line as long as `READY` is the final line you flush. Until
`READY` is on the last line, the reader waits. No race conditions.

---

## 4. Handshake cycle (per generation)

Your mod does this on every generation:

### Step A — Write `ipc/ModOutput.txt`

```
FITNESS=<double>
<expr 1>
FITNESS=<double>
<expr 2>
...
FITNESS=<double>
<expr 20>
READY
```

Notes:
- Exactly 20 `FITNESS=<d>` / `<expr>` pairs, in the same order you read them.
- `FITNESS` must be finite. Map `NaN` / `±Infinity` to `0` on your side.
- `READY` on the last non-blank line.

### Step B — Poll `ipc/JeneticsOutput.txt`

Wait until its last non-blank line is `READY`.

### Step C — Read and consume

Read the 20 MathExpr lines above `READY`. These are the next generation.

### Step D — Truncate `ipc/JeneticsOutput.txt` to empty

**This is your half of the symmetric wipe. Don't skip it** — otherwise on
your next read you'd see stale `READY`.

### Step E — Run 20 battles

One battle per expression. Each expression becomes the battle's evaluator.
Score the outcome via your outer fitness formula
(`playerHealth·1 − healthLost·5 − turnCount·2`).

### Step F — Loop to Step A

On our side, between your writes, we:

1. See `READY` on last line of your `ModOutput.txt`
2. Read the 20 pairs, run one GP step
3. Write 20 new expressions + `READY` (last line) to `JeneticsOutput.txt`
4. Truncate `ModOutput.txt` to empty (our half of the symmetric wipe)
5. Increment `runs/<id>.step`

Each file has exactly one writer at a time, so no locking is needed.

---

## 5. Bootstrapping the first run

For the very first call, you don't need 20 scored pairs. Write 1–20
scored expressions (fitness values ignored during warmup) + `READY`:

```
FITNESS=0
SUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED - MONSTERS_REMAINING - 0.1*SUM_MONSTER_HEALTH + POWERS_PLAYED
READY
```

We fill the remaining (20 − K) slots with random trees and write 20 back.
Score all 20 in battles next round → normal evolution from there.

---

## 6. Error handling

If we can't parse what you sent (malformed fitness line, unparseable
expression, non-finite fitness), we:

1. Log the raw content to `logs/evolution_log.txt` + stderr
2. Truncate `ModOutput.txt` (so the bad `READY` isn't reprocessed)
3. Keep polling

The step counter does **not** advance for bad inputs. Your daemon stays
alive — just fix your next write and we'll pick up.

IO errors (disk full, permission denied) are fatal: we log and exit 3.

---

## 7. Exit codes

| Exit | Meaning | What to do |
|---:|---|---|
| `0` | normal shutdown via Ctrl-C/SIGTERM | — |
| `2` | malformed CLI args / startup config | read stderr |
| `3` | IO error (filesystem failure) | check disk / permissions |
| `4` | internal bug | send me the stderr stack trace |

---

## 8. File layout at runtime

Everything resolves relative to the directory you launch the jar from.
Launch from `FitnessEvolution/` so paths match below:

```
FitnessEvolution/
├── ipc/
│   ├── FeatureBank.txt      (tracked, edit to add/remove features)
│   ├── ModOutput.txt        (YOU write)
│   └── JeneticsOutput.txt   (WE write)
├── runs/
│   └── <run-id>.step        (tiny file, one integer; delete to reset the run)
└── logs/
    └── evolution_log.txt    (one line per successful iteration; tail to monitor)
```

To reset a run from generation 0, stop the daemon and `rm runs/<id>.step`.

---

## 9. Quick sanity check before wiring to your mod

Verify the daemon works end-to-end on your machine in 30 seconds:

```bash
# Terminal 1 — start the daemon
cd FitnessEvolution
rm -rf runs logs/evolution_log.txt ipc/ModOutput.txt ipc/JeneticsOutput.txt 2>/dev/null
java -jar target/FitnessEvolution.jar --poll --run-id=smoke

# Terminal 2 — simulate your mod
cd FitnessEvolution

# Bootstrap with just a template
printf "FITNESS=0\nSUM_DAMAGE_DEALT - 2.0*DAMAGE_RECEIVED\nREADY\n" > ipc/ModOutput.txt

# Wait until daemon writes JeneticsOutput with READY on last line
while [ "$(tail -1 ipc/JeneticsOutput.txt 2>/dev/null)" != "READY" ]; do sleep 0.1; done
echo "Got 20 expressions (warmup complete):"
wc -l < ipc/JeneticsOutput.txt        # should be 21 (20 expressions + READY)
cat runs/smoke.step                    # should be 1

# Simulate: consume JeneticsOutput, write 20 fake scored pairs
python3 <<'PY'
lines = open('ipc/JeneticsOutput.txt').read().splitlines()
exprs = [l for l in lines if l and l != 'READY']
open('ipc/JeneticsOutput.txt','w').write('')   # your mod's wipe
with open('ipc/ModOutput.txt','w') as f:
    for i, e in enumerate(exprs):
        f.write(f'FITNESS={-i*0.5}\n{e}\n')
    f.write('READY\n')
PY

while [ "$(tail -1 ipc/JeneticsOutput.txt 2>/dev/null)" != "READY" ]; do sleep 0.1; done
echo "Evolved to generation 1"
cat runs/smoke.step                    # should be 2

# Back in Terminal 1: Ctrl-C to stop
```

If step counter ends at 2 and `ipc/JeneticsOutput.txt` has 21 lines on
each round, it's working.

---

## 10. Full docs

Detailed protocol spec, additional edge cases, and one-shot-mode
reference: `FitnessEvolution/MOD_INTEGRATION.md` in the repo.

---

Ping me on Discord or the group chat if anything bites. 🚀
