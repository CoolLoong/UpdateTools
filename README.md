Here are many utility classes used for updating protocols and exporting data.

## 1.BdsLangExport.java

The language files related to commands used for exporting PNX from BDS.  
After the export is completed, you still need to manually copy the language files into the PNX.

### Usage

Download BDS from https://www.minecraft.net/en-us/download/server/bedrock ,update the path to your BDS folder

```java
cn/powernukkitx/codegen/BdsLangExport.java:21
static final String TARGET = "C:\\Users\\admin\\Downloads\\bedrock-server-1.20.51.01\\resource_packs\\vanilla\\texts";
```

then run main method

## 2.BiomeGen.java

Download the biome files from AllayMC and generate the corresponding `BiomeID.class` for PNX. This file does not need to
be updated frequently because biomes are not updated often.

### Usage

Run main method

## 3.BlockAttributeFileCutter.java

Download the `block_attributes` files from AllayMC and generate the corresponding `block_color.json` for PNX.
The `block_attributes` from AllayMC may not be updated promptly, as this depends on LeviLamina's update speed. However,
it's not critical because this file is not the most important; it only affects the color filling of the map and doesn't
need to be updated as frequently.

### Usage

Run main method