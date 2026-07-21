# Immersive Technology — CraftTweaker Support

This mod exposes every multiblock's recipes to CraftTweaker so modpack authors can add and remove recipes without touching Java. This document covers every supported machine, its exact script syntax, and what each parameter means.

## Before you start

A few conventions apply across every machine on this page:

- **Time** is always in **ticks** (20 ticks = 1 second). A `time` of `200` is a 10-second process.
- **Energy** is the RF cost for the *entire* process, not per tick.
- **Fluids** use CraftTweaker's standard `<liquid:...>` bracket syntax, e.g. `<liquid:water>`.
- **Items** use the standard `<item:...>` bracket syntax, and can carry an amount (`<item:minecraft:iron_ingot> * 4`).
- Parameters marked **optional** can be omitted from the script entirely, or passed as `null` for a fluid/item slot you don't want to use.
- Recipes are added and removed through CraftTweaker's normal action queue, so they show up correctly with `/ct reload` and in a dry run — nothing here bypasses CraftTweaker's own logging.
- Scripts go in your usual `scripts/` folder, one `import mods.immersivetechnology.<Machine>;` per machine you're editing, same as any other CraftTweaker addon.

---

## Quick reference

| Machine | Inputs | Outputs | Extra params |
|---|---|---|---|
| [Boiler](#boiler) | 1 fluid | 1 fluid | time |
| [Boiler Fuel](#boiler) | 1 fluid | — | time, heat |
| [Distiller](#distiller) | 1 fluid | 1 fluid + 1 item | energy, time, chance |
| [Solar Tower](#solar-tower) | 1 fluid | 1 fluid | time |
| [Steam Turbine](#steam-turbine) | 1 fluid | 1 fluid | time |
| [Gas Turbine](#gas-turbine) | 1 fluid | 1 fluid | time |
| [High Pressure Steam Turbine](#high-pressure-steam-turbine) | 1 fluid | 1 fluid | time |
| [Heat Exchanger](#heat-exchanger) | 2 fluids | 1–2 fluids | energy, time |
| [Electrolytic Crucible Battery](#electrolytic-crucible-battery) | 1 fluid | 1–3 fluids + 1 item | energy, time |
| [Melting Crucible / Solar Melter](#melting-crucible--solar-melter) | 1 item/ingredient | 1 fluid | time |
| [Radiator](#radiator) | 1 fluid | 1 fluid | time |
| [Cooling Tower](#cooling-tower) | 2 fluids | 1–3 fluids | time |
| [Pressurized Fluid](#pressurized-fluid) | *(not a recipe — see below)* | | |

---

## Boiler

Two independent recipe pools: **recipes** (fluid → fluid, the boiler's main job) and **fuel** (what it burns to run).

```zenscript
import mods.immersivetechnology.Boiler;

// Recipe: turn one fluid into another
Boiler.addRecipe(<liquid:steam> * 1000, <liquid:water> * 1000, 200);
// (outputFluid, inputFluid, time)

Boiler.removeRecipe(<liquid:water>);
// removes whichever recipe consumes this input fluid

// Fuel: what the boiler can burn, and how much heat it gives
Boiler.addFuel(<liquid:diesel> * 100, 200, 5.0);
// (inputFluid, time, heat)

Boiler.removeFuel(<liquid:diesel>);
```

`heat` is a floating-point value — higher means the fuel burns hotter (and generally faster) per tick. There's no fixed scale to match against; compare it to existing fuels in the mod's own recipe JSONs if you want a sense of proportion.

---

## Distiller

Fluid in, fluid **and** item out, with a chance the item output actually appears.

```zenscript
import mods.immersivetechnology.Distiller;

Distiller.addRecipe(<liquid:lava> * 1000, <liquid:water> * 1000, <item:minecraft:obsidian>, 10000, 20, 0.01);
// (outputFluid, inputFluid, outputItem, energy, time, chance)

// Shorthand with sensible defaults (energy 10000, time 20, chance 0.01)
Distiller.addRecipe(<liquid:lava> * 1000, <liquid:water> * 1000, <item:minecraft:obsidian>);

Distiller.removeRecipe(<liquid:water>);
```

`chance` is a value from `0.0` to `1.0` — the probability, per completed process, that `outputItem` is actually produced. The fluid output always happens; the item output is the gamble.

`removeRecipe` technically also accepts an optional item argument (`Distiller.removeRecipe(<liquid:water>, <item:...>)`), but as currently implemented it doesn't narrow the match — it removes by input fluid alone regardless of what you pass there. If you have multiple Distiller recipes sharing the same input fluid, removing one removes all of them.

---

## Solar Tower

Simple fluid → fluid. The recipe itself has no energy parameter.

```zenscript
import mods.immersivetechnology.SolarTower;

SolarTower.addRecipe(<liquid:steam> * 1000, <liquid:water> * 1000, 200);
// (outputFluid, inputFluid, time)

SolarTower.removeRecipe(<liquid:water>);
```

---

## Steam Turbine

This is a **fuel** list, not a two-way recipe — it defines what fluids the turbine can consume to generate power.

```zenscript
import mods.immersivetechnology.SteamTurbine;

SteamTurbine.addFuel(<liquid:steam> * 1000, <liquid:steam> * 1000, 200);
// (outputFluid, inputFluid, time) — outputFluid is what's left over after generating power (often the same fluid, condensed)

SteamTurbine.removeFuel(<liquid:steam>);
```

---

## Gas Turbine

Same shape as the Steam Turbine — a fuel list.

```zenscript
import mods.immersivetechnology.GasTurbine;

GasTurbine.addFuel(<liquid:air> * 1000, <liquid:methane> * 1000, 200);
// (outputFluid, inputFluid, time)

GasTurbine.removeFuel(<liquid:methane>);
```

---

## High Pressure Steam Turbine

Same shape again.

```zenscript
import mods.immersivetechnology.HighPressureSteamTurbine;

HighPressureSteamTurbine.addFuel(<liquid:steam> * 1000, <liquid:highpressuresteam> * 1000, 200);
// (outputFluid, inputFluid, time)

HighPressureSteamTurbine.removeFuel(<liquid:highpressuresteam>);
```

---

## Heat Exchanger

Two fluids in, up to two fluids out.

```zenscript
import mods.immersivetechnology.HeatExchanger;

HeatExchanger.addRecipe(<liquid:steam> * 1000, <liquid:hotwater> * 1000, <liquid:water> * 1000, <liquid:water> * 1000, 10000, 200);
// (outputFluid0, outputFluid1, inputFluid0, inputFluid1, energy, time)

// outputFluid1 can be null if there's only one output
HeatExchanger.addRecipe(<liquid:steam> * 1000, null, <liquid:water> * 1000, <liquid:water> * 1000, 10000, 200);

// Remove by both inputs (exact match)
HeatExchanger.removeRecipe(<liquid:water>, <liquid:water>);

// Remove by first input only — this removes every recipe using that input, regardless of the second input
HeatExchanger.removeRecipe(<liquid:water>);
```

`inputFluid0` and `inputFluid1` are both **required** — this machine always needs two inputs. `outputFluid0` is required; `outputFluid1` is optional.

---

## Electrolytic Crucible Battery

One fluid in, up to three fluids and an item out.

```zenscript
import mods.immersivetechnology.ElectrolyticCrucibleBattery;

ElectrolyticCrucibleBattery.addRecipe(<liquid:hydrogen> * 1000, <liquid:oxygen> * 500, null, <item:minecraft:iron_ingot>, <liquid:water> * 1000, 10000, 200);
// (outputFluid0, outputFluid1, outputFluid2, outputItem, inputFluid0, energy, time)

ElectrolyticCrucibleBattery.removeRecipe(<liquid:water>);
```

Only `outputFluid0` and `inputFluid0` are required — `outputFluid1`, `outputFluid2`, and `outputItem` can all be `null`/omitted if you don't need them.

---

## Melting Crucible / Solar Melter

**These two machines share one recipe pool.** Adding a Melting Crucible recipe makes it available to the Solar Melter too, and vice versa — there's no separate registration for Solar Melter.

The input accepts anything CraftTweaker treats as an ingredient — a specific item, an ore dictionary entry, or an amount-stacked ingredient — not just a fixed item stack.

```zenscript
import mods.immersivetechnology.MeltingCrucible;

MeltingCrucible.addRecipe(<liquid:iron> * 1000, <ore:ingotIron>, 200);
// (outputFluid, inputItem, time) — inputItem accepts item stacks, ore dict entries, or amount * ingredient

MeltingCrucible.addRecipe(<liquid:iron> * 1000, <item:minecraft:iron_ingot> * 2, 200);

MeltingCrucible.removeRecipe(<item:minecraft:iron_ingot>);
```

---

## Radiator

Fluid → fluid, no energy cost.

```zenscript
import mods.immersivetechnology.Radiator;

Radiator.addRecipe(<liquid:water> * 1000, <liquid:steam> * 1000, 200);
// (outputFluid, inputFluid, time)

Radiator.removeRecipe(<liquid:steam>);
```

---

## Cooling Tower

Two fluids in, up to three fluids out.

```zenscript
import mods.immersivetechnology.CoolingTower;

CoolingTower.addRecipe(<liquid:water> * 1000, null, null, <liquid:steam> * 1000, <liquid:water> * 100, 200);
// (outputFluid0, outputFluid1, outputFluid2, inputFluid0, inputFluid1, time)

// Remove by both inputs (exact match)
CoolingTower.removeRecipe(<liquid:steam>, <liquid:water>);

// Remove by first input only — removes every recipe using that input, regardless of the second
CoolingTower.removeRecipe(<liquid:steam>);
```

`inputFluid0` and `inputFluid1` are both required. Only `outputFluid0` is required — `outputFluid1` and `outputFluid2` are optional.

---

## Pressurized Fluid

This isn't a recipe — it's a flag that marks a fluid as always pressurized by default, which gives it the faster pressurized transfer rate through this mod's fluid pipes instead of the normal rate. Steam, flue gas, exhaust steam, and high pressure steam are flagged this way out of the box.

```zenscript
import mods.immersivetechnology.PressurizedFluid;

PressurizedFluid.add(<liquid:my_custom_gas>);
PressurizedFluid.remove(<liquid:steam>);
```

Calling `add` on a fluid that's already flagged, or `remove` on one that isn't, is a safe no-op.

---

## A note on removing recipes

For every machine above, removing a recipe removes it everywhere at once — from the machine's actual crafting logic, and from JEI. You won't end up with a recipe that's gone from JEI but still secretly works, or vice versa.
