package com.denizenscript.denizen.nms.v1_16.impl;

import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiomeNMSImpl extends BiomeNMS {

    public BiomeBase biomeBase;

    public Biome bukkitBiome;

    public BiomeNMSImpl(World world, Biome biome) {
        super(world, biome.name());
        bukkitBiome = biome;
        this.biomeBase = CraftBlock.biomeToBiomeBase(((CraftWorld) world).getHandle().r().b(IRegistry.ay), biome);
    }

    @Override
    public DownfallType getDownfallType() {
        BiomeBase.Precipitation nmsType = biomeBase.c();
        switch (nmsType) {
            case RAIN:
                return DownfallType.RAIN;
            case SNOW:
                return DownfallType.SNOW;
            case NONE:
                return DownfallType.NONE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public float getHumidity() {
        return biomeBase.getHumidity();
    }

    @Override
    public float getTemperature() {
        return biomeBase.k();
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
        ReflectionHelper.setFieldValue(BiomeBase.class, "j", biomeBase, humidity);
    }

    @Override
    public void setTemperature(float temperature) {
        ReflectionHelper.setFieldValue(BiomeBase.class, "i", biomeBase, temperature);
    }

    private List<EntityType> getSpawnableEntities(EnumCreatureType creatureType) {
        BiomeSettingsMobs mobs = biomeBase.b();
        List<BiomeSettingsMobs.c> typeSettingList = mobs.a(creatureType);
        List<EntityType> entityTypes = new ArrayList<>();
        if (typeSettingList == null) {
            return entityTypes;
        }
        for (BiomeSettingsMobs.c meta : typeSettingList) {
            // TODO: check if this works
            try {
                String n = EntityTypes.getName(meta.c).getKey();
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

    @Override
    public void setTo(Block block) {
        block.setBiome(bukkitBiome);
    }
}
