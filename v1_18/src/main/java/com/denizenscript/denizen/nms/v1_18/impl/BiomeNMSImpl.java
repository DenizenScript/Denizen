package com.denizenscript.denizen.nms.v1_18.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_18.ReflectionMappingsInfo;
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
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.EntityType;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiomeNMSImpl extends BiomeNMS {

    public static final MethodHandle BIOMESPECIALEFFECTS_BUILDER_CONSTRUCTOR = ReflectionHelper.getConstructor(BiomeSpecialEffects.Builder.class);

    public Holder<Biome> biomeBase;

    public ServerLevel world;

    public BiomeNMSImpl(ServerLevel world, String name) {
        super(world.getWorld(), name);
        this.world = world;
        biomeBase = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getHolder(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(name))).orElse(null);
    }

    @Override
    public DownfallType getDownfallType() {
        Biome.Precipitation nmsType = biomeBase.value().getPrecipitation();
        return switch (nmsType) {
            case RAIN -> DownfallType.RAIN;
            case SNOW -> DownfallType.SNOW;
            case NONE -> DownfallType.NONE;
            default -> throw new UnsupportedOperationException();
        };
    }

    @Override
    public float getHumidity() {
        return biomeBase.value().getDownfall();
    }

    @Override
    public float getTemperature() {
        return biomeBase.value().getBaseTemperature();
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
        return biomeBase.value().getFoliageColor();
    }

    public Object getClimate() {
        return ReflectionHelper.getFieldValue(net.minecraft.world.level.biome.Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeBase.value());
    }

    @Override
    public void setHumidity(float humidity) {
        Object climate = getClimate();
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_downfall, climate, humidity);
    }

    @Override
    public void setTemperature(float temperature) {
        Object climate = getClimate();
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_temperature, climate, temperature);
    }

    @Override
    public void setPrecipitation(DownfallType type) {
        Biome.Precipitation nmsType = switch (type) {
            case RAIN -> Biome.Precipitation.RAIN;
            case SNOW -> Biome.Precipitation.SNOW;
            case NONE -> Biome.Precipitation.NONE;
            default -> throw new UnsupportedOperationException();
        };
        Object climate = getClimate();
        ReflectionHelper.setFieldValue(climate.getClass(), ReflectionMappingsInfo.Biome_ClimateSettings_precipitation, climate, nmsType);
    }

    @Override
    public void setFoliageColor(int color) {
        try {
            BiomeSpecialEffects.Builder builder = (BiomeSpecialEffects.Builder) BIOMESPECIALEFFECTS_BUILDER_CONSTRUCTOR.invoke();
            // fogColor, waterColor, waterFogColor, and skyColor are needed for the Builder class.
            builder.fogColor(biomeBase.value().getFogColor())
                    .waterColor(biomeBase.value().getWaterColor())
                    .waterFogColor(biomeBase.value().getWaterFogColor())
                    .skyColor(biomeBase.value().getSkyColor())
                    .foliageColorOverride(color);
            BiomeSpecialEffects effects = builder.build();
            ReflectionHelper.setFieldValue(Biome.class, ReflectionMappingsInfo.Biome_specialEffects, biomeBase.value(), effects);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    private List<EntityType> getSpawnableEntities(MobCategory creatureType) {
        MobSpawnSettings mobs = biomeBase.value().getMobSettings();
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
                chunk.setBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2, biomeBase);
                chunk.setUnsaved(true);
            }
        }
    }
}
