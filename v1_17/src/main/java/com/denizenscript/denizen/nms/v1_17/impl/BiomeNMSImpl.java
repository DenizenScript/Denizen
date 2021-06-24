package com.denizenscript.denizen.nms.v1_17.impl;

import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_17.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.core.Registry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiomeNMSImpl extends BiomeNMS {

    public net.minecraft.world.level.biome.Biome biomeBase;

    public BiomeNMSImpl(Biome biome) {
        super(biome);
        World world = Bukkit.getWorlds().get(0); // TODO: Biomes can now be world-unique as of 1.16.2
        this.biomeBase = CraftBlock.biomeToBiomeBase(((CraftWorld) world).getHandle().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), biome);
    }

    @Override
    public float getHumidity() {
        return biomeBase.getDownfall();
    }

    @Override
    public float getTemperature() {
        return biomeBase.getBaseTemperature();
    }

    @Override
    public List<EntityType> getAmbientEntities() {
        return getSpawnableEntities(MobCategory.AMBIENT);
    }

    @Override
    public List<EntityType> getCreatureEntities() {
        return getSpawnableEntities(MobCategory.CREATURE);
    }

    @Override
    public List<EntityType> getMonsterEntities() {
        return getSpawnableEntities(MobCategory.MONSTER);
    }

    @Override
    public List<EntityType> getWaterEntities() {
        return getSpawnableEntities(MobCategory.WATER_CREATURE);
    }

    @Override
    public void setHumidity(float humidity) {
        Object climate = ReflectionHelper.getFieldValue(net.minecraft.world.level.biome.Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeBase);
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_downfall, climate, humidity);
    }

    @Override
    public void setTemperature(float temperature) {
        Object climate = ReflectionHelper.getFieldValue(net.minecraft.world.level.biome.Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeBase);
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_temperature, climate, temperature);
    }

    @Override
    protected boolean getDoesRain() {
        return biomeBase.getPrecipitation() == net.minecraft.world.level.biome.Biome.Precipitation.RAIN;
    }

    @Override
    protected boolean getDoesSnow() {
        return biomeBase.getPrecipitation() == net.minecraft.world.level.biome.Biome.Precipitation.SNOW;
    }

    private List<EntityType> getSpawnableEntities(MobCategory creatureType) {
        MobSpawnSettings mobs = biomeBase.getMobSettings();
        WeightedRandomList<MobSpawnSettings.SpawnerData> typeSettingList = mobs.getMobs(creatureType);
        List<EntityType> entityTypes = new ArrayList<>();
        if (typeSettingList == null) {
            return entityTypes;
        }
        for (MobSpawnSettings.SpawnerData meta : typeSettingList.unwrap()) {
            try {
                String n = net.minecraft.world.entity.EntityType.getKey(meta.type).getPath();
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
