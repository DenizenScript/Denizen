package com.denizenscript.denizen.utilities.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.List;

public class VoidGenerator extends ChunkGenerator {

    final boolean setVoidBiome;

    public VoidGenerator(boolean setVoidBiome) {
        this.setVoidBiome = setVoidBiome;
    }

    public static class VoidBiomeProvider extends BiomeProvider {

        public static final List<Biome> VOID_BIOME_LIST = List.of(Biome.THE_VOID);

        @Override
        public Biome getBiome(WorldInfo worldInfo, int i, int i1, int i2) {
            return Biome.THE_VOID;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return VOID_BIOME_LIST;
        }
    }

    public static final VoidBiomeProvider VOID_BIOME_PROVIDER = new VoidBiomeProvider();

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return setVoidBiome ? VOID_BIOME_PROVIDER : null;
    }
}
