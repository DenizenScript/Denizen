package com.denizenscript.denizen.utilities.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;

public class VoidGenerator1_17 extends ChunkGenerator {

    public static class VoidBiomeProvider extends BiomeProvider {

        public static List<Biome> biomes = Collections.singletonList(Biome.THE_VOID);

        @Override
        public Biome getBiome(WorldInfo worldInfo, int i, int i1, int i2) {
            return Biome.THE_VOID;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return biomes;
        }
    }

    public static VoidBiomeProvider biomeProviderInstance = new VoidBiomeProvider();

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return biomeProviderInstance;
    }
}
