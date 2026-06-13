# FitnessEvolution

External Java program that evolves Slay the Spire fitness (evaluator)
expressions as [MathExpr](https://jenetics.io/javadoc/jenetics.prog/7.2/io/jenetics/prog/op/MathExpr.html)
trees using [Jenetics 7.2.0](https://jenetics.io/) genetic programming.

Integrates with `BattleAiMod` via a **generation-agnostic one-shot** CLI:
the mod seeds `ipc/ModOutput.txt` with whatever expressions it wants to
evolve, runs our jar once, reads the next population from
`ipc/JeneticsOutput.txt`, runs combats, writes scored results back,
calls the jar again. No long-lived process, no polling, no hidden
gen-0 bootstrap — every call does the same thing.

**Status:** engine + IPC done, awaiting first live run with the mod.

## Requirements

- Java 17+
- Maven 3.8+
- Nothing else — Jenetics pulls in via Maven Central.

## Folder structure

```
FitnessEvolution/
├── README.md                 -- this file
├── MOD_INTEGRATION.md        -- ~10-min quick-start for the mod author
├── pom.xml                   -- Maven build (Java 17, Jenetics 7.2.0, shade → Main)
├── .gitignore
│
├── ipc/                      -- file-based IPC with the mod
│   ├── FeatureBank.txt       -- (TRACKED) ordered feature names; edit to match what the mod reports
│   ├── JeneticsOutput.txt    -- (GITIGNORED, created at runtime) we write: one MathExpr per line
│   └── ModOutput.txt         -- (GITIGNORED, created at runtime) mod writes: FITNESS=<x>/<expr> pairs, or END
│
├── runs/                     -- (gitignored) per-run step counters
│   └── <run-id>.step         -- single integer, advances by 1 each successful call
│
├── logs/                     -- (gitignored, .gitkeep preserves dir)
│   └── evolution_log.txt     -- one human-readable line per invocation
│
├── src/main/java/fitnessevolution/
│   ├── Main.java             -- CLI entry point; dispatches one-shot vs --poll
│   ├── Config.java           -- central paths + population tunables
│   ├── FeatureBankLoader.java-- parse FeatureBank.txt → List<String> / ISeq<Var<Double>>
│   ├── OpSet.java            -- operator set (+,-,*,/) + ephemeral const in [-5,5]
│   ├── MathExprIO.java       -- parseExpression() / canonical serialize()
│   ├── Scored.java           -- record (Genotype, fitness) with expr() helper
│   ├── GPEngine.java         -- GP driver: randomTrees, rehydrate, step (elitism, tournament, mutate, crossover)
│   ├── GPProcessor.java      -- shared K→N processing (warmup, evolve, truncate)
│   ├── JeneticsIO.java       -- read/write IPC, READY detection, atomic writes
│   ├── RunCounter.java       -- per-run step counter (runs/<id>.step)
│   ├── PollLoop.java         -- long-running daemon for --poll mode
│   └── OfflineMockRun.java   -- dev harness (see "Debugging without the mod" below)
│
├── src/test/java/fitnessevolution/
│   └── *Test.java            -- 31 unit + integration tests (run with `mvn test`)
│
└── target/                   -- (gitignored) Maven output + shaded jar
```

### Files you do not see in a fresh clone

Four paths live inside this directory but are **not committed** — they
are created locally on first run (or by your mod) and are gitignored
so they never sync across machines:

| Path | Who creates it | When |
|---|---|---|
| `ipc/JeneticsOutput.txt` | our jar | on every call, overwritten |
| `ipc/ModOutput.txt`      | your mod | before every call |
| `runs/<id>.step`         | our jar | on each successful call, increments |
| `logs/evolution_log.txt` | our jar | appended each successful call |

All paths resolve **relative to the current working directory**. Launch
from `FinalProject/scumthespire/FitnessEvolution/` so everything lands
where the tree above shows.

## Important pieces at a glance

- **`ipc/FeatureBank.txt`** — the feature-name contract. Each line is a
  variable name that may appear in any MathExpr we emit; line order is
  the index the mod passes to `MathExpr.eval(double[] features)`. Edit
  this file to add/remove features exposed to evolution.
- **`src/main/java/fitnessevolution/Main.java`** — the CLI entry point.
  Reads ModOutput, branches on `K = results.size()` vs
  `Config.POPULATION_SIZE`, writes JeneticsOutput, increments step
  counter. See MOD_INTEGRATION.md §2 for the decision table.
- **`src/main/java/fitnessevolution/Config.java`** — all paths and
  run-level tunables (`POPULATION_SIZE`, `MAX_DEPTH`, `DEFAULT_SEED`,
  `DEFAULT_RUN_ID`). Change anything here rather than hunting through
  the rest of the code.
- **`src/main/java/fitnessevolution/GPEngine.java`** — the GP algorithm:
  elitism %, tournament k, crossover/mutation rates, parent-pool size,
  random tree generation.

## Teammate integration

See [`MOD_INTEGRATION.md`](MOD_INTEGRATION.md) for the mod author's
~10-minute run-book. Two modes:

### One-shot (default)
Mod drives the loop by calling the jar once per generation.
1. Mod writes `ipc/ModOutput.txt` with K scored expressions (1 ≤ K ≤ 20).
2. Mod runs `java -jar target/FitnessEvolution.jar --run-id=<id>`.
3. Jar writes 20 expressions to `ipc/JeneticsOutput.txt`.
   - If K < 20 (warmup): echoes the K inputs + fills (20 − K) random trees.
   - If K = 20 (evolve): one GP step → next generation.
   - If K > 20: truncates to top 20 by fitness, then evolves.
4. Mod runs one battle per expression, writes 20 FITNESS/expr pairs back,
   loops to 2.
5. Stop cleanly by writing `END` to ModOutput.txt and calling the jar
   once more.

### Poll (`--poll`)
Long-running daemon. Mod interacts via files only.
1. Start: `java -jar target/FitnessEvolution.jar --poll --run-id=<id>`.
2. Each handshake uses `READY` on the last non-blank line of each file as
   the completion flag. We wipe `ModOutput.txt` after consuming; mod
   wipes `JeneticsOutput.txt` after consuming. Fully symmetric.
3. Stop: Ctrl-C (foreground) or SIGTERM (background). Current iteration
   completes before exit.
4. See [`MOD_INTEGRATION.md` §9](MOD_INTEGRATION.md) for the full
   protocol and the mod-side responsibilities.

## Configuration reference

Everything the GP engine does with default settings:

| | |
|---|---|
| Population size | 20 |
| Max tree depth | 7 |
| Init depth range | 2 – 7 (ramped half-and-half) |
| Operators | `+ − × ÷` (unprotected division) |
| Variables | from `ipc/FeatureBank.txt` (currently 5) |
| Constants | ephemeral, range `[−5, 5]` |
| Selection | tournament k=3 over top 50% |
| Elitism | top 10% (2 of 20) preserved unchanged |
| P(crossover) | 0.7 (SingleNodeCrossover) |
| P(mutation) | 0.1 (subtree replacement) |
| RNG seed | `baseSeed + runs/<id>.step` (deterministic per run) |
| IPC | file-based, one-shot CLI |
| Termination | `END` sentinel in ModOutput.txt |

## Debugging without the mod

`OfflineMockRun` runs the GP engine **entirely in-process** for 10
generations against a synthetic fitness function
(target value 10.0 at features `[1,1,1,1,1]`, fitness `= -|eval - 10|`).
It does not touch `ipc/` or `runs/` — purely a smoke test for the
engine.

```bash
mvn -q exec:java -Dexec.mainClass=fitnessevolution.OfflineMockRun
```

Prints the top-5 individuals per generation and the best-ever
individual at the end. Useful for confirming crossover/mutation are
producing sensible offspring without the mod running.

## Running the tests

```bash
mvn test
```

36 tests, all green. Covers feature-bank parsing, operator set,
MathExpr canonical serialize round-trip, GP engine invariants, run
counter round-trip, JeneticsIO malformed-input handling, end-to-end
`Main` cycles (warmup, evolve, END, truncation, independent run IDs),
and poll-mode daemon lifecycle (warmup + evolve iterations, malformed
recovery, graceful shutdown, READY marker on output).
