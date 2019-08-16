package com.denizenscript.denizen.v1_12.impl;

import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiomeNMS_v1_12_R1 extends BiomeNMS {

    private final BiomeBase biomeBase;

    public BiomeNMS_v1_12_R1(Biome biome) {
        super(biome);
        this.biomeBase = CraftBlock.biomeToBiomeBase(biome);
    }

    @Override
    public float getHumidity() {
        return biomeBase.getHumidity();
    }

    @Override
    public float getTemperature() {
        return biomeBase.getTemperature();
    }

    @Override
    public List<EntityType> getAmbientEntities() {
        return getSpawnableEntities(EnumCreatureType.AMBIENT);
    }

    @Override
    public List<EntityType> getCreatureEntities() {
        return getSpawnableEntities(EnumCreatureType.CREATURE);
    }

    @Override
    public List<EntityType> getMonsterEntities() {
        return getSpawnableEntities(EnumCreatureType.MONSTER);
    }

    @Override
    public List<EntityType> getWaterEntities() {
        return getSpawnableEntities(EnumCreatureType.WATER_CREATURE);
    }

    @Override
    public void setHumidity(float humidity) {
        ReflectionHelper.setFieldValue(BiomeBase.class, "C", biomeBase, humidity);
    }

    @Override
    public void setTemperature(float temperature) {
        ReflectionHelper.setFieldValue(BiomeBase.class, "B", biomeBase, temperature);
    }

    @Override
    public void changeBlockBiome(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        if (world.isLoaded(new BlockPosition(x, 0, z))) {
            Chunk chunk = world.getChunkAtWorldCoords(new BlockPosition(x, 0, z));

            if (chunk != null) {
                byte[] biomevals = chunk.getBiomeIndex();
                biomevals[((z & 0xF) << 4) | (x & 0xF)] = (byte) BiomeBase.a(biomeBase);
            }
        }
    }

    @Override
    protected boolean getDoesRain() {
        Boolean rains = ReflectionHelper.getFieldValue(BiomeBase.class, "F", biomeBase);
        if (rains != null) {
            return rains;
        }
        return false;
    }

    @Override
    protected boolean getDoesSnow() {
        Boolean rains = ReflectionHelper.getFieldValue(BiomeBase.class, "E", biomeBase);
        if (rains != null) {
            return rains;
        }
        return false;
    }

    private List<EntityType> getSpawnableEntities(EnumCreatureType creatureType) {
        List<EntityType> entityTypes = new ArrayList<>();
        for (BiomeBase.BiomeMeta meta : biomeBase.getMobs(creatureType)) {
            // TODO: check if this works
            try {
                String n = EntityTypes.getName(meta.b).getKey();
                EntityType et = EntityType.fromName(n);
                if (et == null) {
                    et = EntityType.valueOf(n.toUpperCase(Locale.ENGLISH));
                }
                entityTypes.add(et);
            }
            catch (Throwable e) {
                // Ignore the error. Likely from invalid entity type name output.
            }
        }
        return entityTypes;
    }
}
