# ManaMetalMod GTNH NEI Support

Small Forge 1.7.10 addon for GTNH NotEnoughItems. It registers ManaMetalMod's custom ManaCraftingTable GUI as a normal NEI `crafting` overlay so the recipe overlay no longer fails with `mismatch crafting grid`.

## Current status

This repo contains a configurable integration skeleton:

- NEI plugin entrypoint: `NEIManaMetalConfig`
- `API.registerGuiOverlay(...)`
- `API.registerGuiOverlayHandler(...)`
- configurable slot mapping handler for custom containers
- multiple integration profiles in one config file

It does not hard-code ManaMetalMod classes yet, because the exact GUI/container class names and slot mapping need to be confirmed from the ManaMetalMod jar.

By default only `mana_crafting_table` is enabled. `metal_fusion_table` is generated as a disabled template so it can be filled in later without changing code.

## Build inputs

This repo can use local portable tools installed under `.tools/`. In PowerShell:

```text
.\use-local-tools.bat
```

Put Forge/CodeChicken/NEI development jars in `libs/`, for example:

```text
libs/CodeChickenLib-1.7.10-...-dev.jar
libs/CodeChickenCore-1.7.10-...-dev.jar
libs/NotEnoughItems-2.8.97-GTNH-dev.jar
```

Then build with:

```text
.\build-local.bat build
```

The helper skips ForgeGradle 1.2's dead legacy Minecraft S3 download tasks. The local cache must contain the 1.7.10 client/server jars and version JSON; this workspace has already been prepared that way.

If your local GTNH development environment already has a preferred ForgeGradle setup, you can keep this source tree and swap `build.gradle` to that setup.

## Target versions

Current target pack/mod versions:

```text
Minecraft: 1.7.10
Forge: 10.13.4.1614
CodeChickenCore-1.4.12.jar
NotEnoughItems-2.8.97-GTNH.jar
gtnhlib-0.9.64.jar
manametalmod-7.5.2.jar
```

## Runtime config

On first launch, edit:

```text
config/manametalmod_gtnh_nei_support.cfg
```

Important values for the first target:

```text
[mana_crafting_table]
enabled=true
guiClassName=project.studio.manametalmod.client.GuiManaCraftTable
overlayIdent=CraftTable_Crafting
overlayOffsetX=5
overlayOffsetY=0
registerDefaultOverlayAlias=true
useExplicitSlotMapping=true
gridWidth=3
inputStackRelXs <
    45
    45
    45
    66
    75
    84
    105
    105
    105
>
inputStackRelYs <
    13
    31
    49
    49
    31
    49
    49
    31
    13
>
inputSlots <
    0
    1
    2
    3
    4
    5
    6
    7
    8
>
```

For ManaMetalMod 7.5.2, the magic crafting table mapping found from `ContainerManaCraftTable` is:

```text
0 = input, x=50, y=13
1 = input, x=50, y=31
2 = input, x=50, y=49
3 = input, x=71, y=49
4 = input, x=80, y=31
5 = input, x=89, y=49
6 = input, x=110, y=49
7 = input, x=110, y=31
8 = input, x=110, y=13
9 = output, x=80, y=13
10-18 = ManaMetalMod tile inventory slots, x=8..152, y=95
19-45 = player inventory
46-54 = hotbar
```

The NEI recipe handler uses the same shape at x coordinates `45,45,45,66,75,84,105,105,105` and y coordinates `13,31,49,49,31,49,49,31,13`. The `overlayOffsetX=5` and `overlayOffsetY=0` values map those NEI positions to the real GUI slot positions.

`registerDefaultOverlayAlias=true` is important for ManaMetalMod 7.5.2. Its `NEIManaCraftTable` opens recipes with `CraftTable_Crafting`, but it does not override NEI's `getOverlayIdentifier()`, so the actual overlay transfer check asks for NEI's default/null identifier.

If ManaMetalMod uses special mana/catalyst/ghost slots, do not include those in `inputSlots`; only include the nine recipe input slots.

The generated config also contains:

```text
[metal_fusion_table]
enabled=false
guiClassName=manametalmod.client.gui.GuiMetalFusionTable
gridWidth=3
```

When the metal fusion table's real GUI class and slot mapping are known, set `enabled=true` and update `guiClassName`, `gridWidth`, and `inputSlots`.

Additional future containers can be added without code changes by putting category names in:

```text
[general]
extraProfiles <
    another_table
>
```

Then add an `[another_table]` section using the same keys as `mana_crafting_table`.
