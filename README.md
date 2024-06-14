Here are many utility classes used for updating protocols and exporting data.
All generated files are typically located in the `build` or `resource` directory.

# Update PNX guide:
1. we first need to update the protocol library, which simply requires copying and pasting from Cloudbrust to PNX.
2. Then we need to update https://github.com/CloudburstMC/BlockStateUpdater to PNX's `cn\nukkit\level\updater\block`
3. update https://github.com/df-mc/worldupgrader/tree/master/itemupgrader/schemas to PNX's `cn\nukkit\level\updater\item`
4. update resource use `DownloadResource.java` and other,For recipes, you need to use
   either https://github.com/AllayMC/Allay/blob/master/Allay-Data/src/main/java/org/allaymc/data/RecipeExportUtil.java
   or https://github.com/JukeboxMC/data-extractor.
5. Remove deprecated block states and block property from the PNX code and rename block classes if there are changes in
   block names.
   this is a breaking change that occurs almost every version until Minecraft's bedrock version no longer modifies block
   states.
6. The api related to the addon may also change. If there are errors, you need to load the addon in the vanilla BDS and
   observe the corresponding packets sent through proxypass.
   These packets are usually `ItemComponentPacket` for custom item, `StartGamePacket#blockProperties` for custom block ,
   and `AvailableEntityIdentifiersPacket` for custom entity.

## BdsLangExport
The language files related to commands used for exporting PNX from BDS.  
After the export is completed, you still need to manually copy the language files into the PNX.
#### usage:
Download BDS from https://www.minecraft.net/en-us/download/server/bedrock ,update the path to your BDS folder
```java
cn/powernukkitx/codegen/BdsLangExport.java:21
static final String TARGET = "C:\\Users\\admin\\Downloads\\bedrock-server-1.20.51.01\\resource_packs\\vanilla\\texts";
```
then run main method

## BiomeGen
Download the biome files from AllayMC and generate the corresponding `BiomeID.class` for PNX. This file does not need to
be updated frequently because biomes are not updated often.
#### usage:
Run main method

## BlockAttributeFileCutter
Download the `block_attributes` files from AllayMC and generate the corresponding `block_color.json` for PNX.
The `block_attributes` from AllayMC may not be updated promptly, as this depends on LeviLamina's update speed. However,
it's not critical because this file is not the most important; it only affects the color filling of the map and doesn't
need to be updated as frequently.
#### usage:
Run main method

## BlockIDGen
generate the corresponding `BlockID.class` and new Block class for PNX.  
you must be run it after `DownloadResource.java`.
#### usage:
Run main method

## BlockPropertiesGen
unzip the `block_attributes.nbt`
#### usage:
Run main method

## ConstGen
Convert the string exported from the protocol typeMap into constants in PNX format.
#### usage:
Change the string.
then run main method

## DownloadResource
Updating the necessary resources requires waiting for `CloudBurst/Data` to update, but they usually update very quickly.
#### usage:
Run main method

## EntityGen
generate the corresponding `EntityID.class` for PNX.  
you must be run it after `DownloadResource.java`.
#### usage:
Run main method

## ItemGen
generate the corresponding `ItemID.class` and some new Item class for PNX.  
you must be run it after `DownloadResource.java`.
#### usage:
Run main method

## PickBlockClass
Select class files that match the pattern from the batch-generated block classes
#### usage:
Run main method

## PropertyTypeDataDumper
Used to export the `block_property_types.json` file. This file is a requisite for run `BlockIDGen`.
#### usage:
Use `DownloadResouce` to update `block_palette.nbt`
Run main method

## Recipe
refer https://github.com/CoolLoong/data-extractor

## RuntimeBlockStateGen
Dump the block palette to a txt
#### usage:
Run main method