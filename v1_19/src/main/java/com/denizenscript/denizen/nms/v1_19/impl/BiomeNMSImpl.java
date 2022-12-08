package com.denizenscript.denizen.nms.v1_19.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.entity.EntityType;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiomeNMSImpl extends BiomeNMS {

    public static final MethodHandle BIOME_CLIMATESETTINGS_CONSTRUCTOR = ReflectionHelper.getConstructor(Biome.class.getDeclaredClasses()[0], Biome.Precipitation.class, float.class, Biome.TemperatureModifier.class, float.class);

    public Holder<Biome> biomeHolder;
    public ServerLevel world;

    public BiomeNMSImpl(ServerLevel world, String name) {
        super(world.getWorld(), name);
        this.world = world;
        biomeHolder = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(name))).orElse(null);
    }

    @Override
    public DownfallType getDownfallType() {
        Biome.Precipitation nmsType = biomeHolder.value().getPrecipitation();
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
        return biomeHolder.value().getDownfall();
    }

    @Override
    public float getTemperature() {
        return biomeHolder.value().getBaseTemperature();
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

    public Object getClimate() {
        return ReflectionHelper.getFieldValue(Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeHolder.value());
    }

    public void setClimate(Biome.Precipitation precipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
        try {
            Object newClimate = BIOME_CLIMATESETTINGS_CONSTRUCTOR.invoke(precipitation, temperature, temperatureModifier, downfall);
            ReflectionHelper.setFieldValue(Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeHolder.value(), newClimate);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void setHumidity(float humidity) {
        setClimate(biomeHolder.value().getPrecipitation(), getTemperature(), getTemperatureModifier(), humidity);
    }

    @Override
    public void setTemperature(float temperature) {
        setClimate(biomeHolder.value().getPrecipitation(), temperature, getTemperatureModifier(), getHumidity());
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
        setClimate(nmsType, getTemperature(), getTemperatureModifier(), getHumidity());
    }

    private List<EntityType> getSpawnableEntities(MobCategory creatureType) {
        MobSpawnSettings mobs = biomeHolder.value().getMobSettings();
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
            NMSHandler.instance.getBiomeNMS(block.getWorld(), getName()).setTo(block);
            return;
        }
        // Based on CraftWorld source
        BlockPos pos = new BlockPos(block.getX(), 0, block.getZ());
        if (world.hasChunkAt(pos)) {
            LevelChunk chunk = world.getChunkAt(pos);
            if (chunk != null) {
                chunk.setBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2, biomeHolder);
                chunk.setUnsaved(true);
            }
        }
    }

    public Biome.TemperatureModifier getTemperatureModifier() {
        Object climate = getClimate();
        return ReflectionHelper.getFieldValue(climate.getClass(), ReflectionMappingsInfo.BiomeClimateSettings_temperatureModifier, climate);
    }
}
