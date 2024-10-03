package com.denizenscript.denizen.nms.v1_17.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_17.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BiomeNMSImpl extends BiomeNMS {

    public net.minecraft.world.level.biome.Biome biomeBase;

    public ServerLevel world;

    public BiomeNMSImpl(ServerLevel world, NamespacedKey key) {
        super(world.getWorld(), key);
        this.world = world;
        biomeBase = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(CraftNamespacedKey.toMinecraft(key));
    }

    @Override
    public DownfallType getDownfallType() {
        Biome.Precipitation nmsType = biomeBase.getPrecipitation();
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
        return biomeBase.getDownfall();
    }

    @Override
    public float getBaseTemperature() {
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
    public int getFoliageColor() {
        // Check if the biome already has a default color
        if (biomeBase.getFoliageColor() != 0) {
            return biomeBase.getFoliageColor();
        }
        // Based on net.minecraft.world.level.biome.Biome#getFoliageColorFromTexture()
        float temperature = clampColor(getBaseTemperature());
        float humidity = clampColor(getHumidity());
        // Based on net.minecraft.world.level.FoliageColor#get()
        humidity *= temperature;
        int humidityValue = (int)((1.0f - humidity) * 255.0f);
        int temperatureValue = (int)((1.0f - temperature) * 255.0f);
        int index = temperatureValue << 8 | humidityValue;
        return index >= 65536 ? 4764952 : getColor(index / 256, index % 256).asRGB();
    }

    public Object getClimate() {
        return ReflectionHelper.getFieldValue(net.minecraft.world.level.biome.Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeBase);
    }

    @Override
    public void setHumidity(float humidity) {
        Object climate = getClimate();
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_downfall, climate, humidity);
    }

    @Override
    public void setBaseTemperature(float temperature) {
        Object climate = getClimate();
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_temperature, climate, temperature);
    }

    @Override
    public void setPrecipitation(DownfallType type) {
        Biome.Precipitation nmsType;
        switch (type) {
            case NONE:
                nmsType = Biome.Precipitation.NONE;
                break;
            case RAIN:
                nmsType = Biome.Precipitation.RAIN;
                break;
            case SNOW:
                nmsType = Biome.Precipitation.SNOW;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        Object climate = getClimate();
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_precipitation, climate, nmsType);
    }

    @Override
    public void setFoliageColor(int color) {
        try {
            ReflectionHelper.setFieldValue(BiomeSpecialEffects.class, ReflectionMappingsInfo.BiomeSpecialEffects_foliageColorOverride, biomeBase.getSpecialEffects(), Optional.of(color));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
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

    @Override
    public void setTo(Block block) {
        if (((CraftWorld) block.getWorld()).getHandle() != this.world) {
            NMSHandler.instance.getBiomeNMS(block.getWorld(), getKey()).setTo(block);
            return;
        }
        // Based on CraftWorld source
        BlockPos pos = new BlockPos(block.getX(), 0, block.getZ());
        if (world.hasChunkAt(pos)) {
            LevelChunk chunk = world.getChunkAt(pos);
            if (chunk != null) {
                chunk.getBiomes().setBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2, biomeBase);
                chunk.markUnsaved();
            }
        }
    }
}
