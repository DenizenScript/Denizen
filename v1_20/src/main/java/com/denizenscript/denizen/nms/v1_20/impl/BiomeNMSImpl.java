package com.denizenscript.denizen.nms.v1_20.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftLocation;
import org.bukkit.entity.EntityType;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BiomeNMSImpl extends BiomeNMS {

    public static final MethodHandle BIOME_CLIMATESETTINGS_CONSTRUCTOR = ReflectionHelper.getConstructor(Biome.ClimateSettings.class, boolean.class, float.class, Biome.TemperatureModifier.class, float.class);

    public Holder<Biome> biomeHolder;
    public ServerLevel world;

    public BiomeNMSImpl(ServerLevel world, String name) {
        super(world.getWorld(), name);
        this.world = world;
        biomeHolder = world.registryAccess().registryOrThrow(Registries.BIOME).getHolder(ResourceKey.create(Registries.BIOME, new ResourceLocation(name))).orElse(null);
    }

    @Override
    public DownfallType getDownfallTypeAt(Location location) {
        Biome.Precipitation precipitation = biomeHolder.value().getPrecipitationAt(CraftLocation.toBlockPosition(location));
        return switch (precipitation) {
            case RAIN -> DownfallType.RAIN;
            case SNOW -> DownfallType.SNOW;
            case NONE -> DownfallType.NONE;
        };
    }

    @Override
    public float getHumidity() {
        return biomeHolder.value().climateSettings.downfall();
    }

    @Override
    public float getBaseTemperature() {
        return biomeHolder.value().getBaseTemperature();
    }

    @Override
    public float getTemperatureAt(Location location) {
        return biomeHolder.value().getTemperature(CraftLocation.toBlockPosition(location));
    }

    @Override
    public boolean hasDownfall() {
        return biomeHolder.value().hasPrecipitation();
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
        if (biomeHolder.value().getFoliageColor() != 0) {
            return biomeHolder.value().getFoliageColor();
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

    public void setClimate(boolean hasPrecipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
        try {
            Object newClimate = BIOME_CLIMATESETTINGS_CONSTRUCTOR.invoke(hasPrecipitation, temperature, temperatureModifier, downfall);
            ReflectionHelper.setFieldValue(Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeHolder.value(), newClimate);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void setHumidity(float humidity) {
        setClimate(hasDownfall(), getBaseTemperature(), getTemperatureModifier(), humidity);
    }

    @Override
    public void setBaseTemperature(float baseTemperature) {
        setClimate(hasDownfall(), baseTemperature, getTemperatureModifier(), getHumidity());
    }

    @Override
    public void setHasDownfall(boolean hasDownfall) {
        setClimate(hasDownfall, getBaseTemperature(), getTemperatureModifier(), getHumidity());
    }

    @Override
    public void setFoliageColor(int color) {
        ReflectionHelper.setFieldValue(BiomeSpecialEffects.class, ReflectionMappingsInfo.BiomeSpecialEffects_foliageColorOverride, biomeHolder.value().getSpecialEffects(), Optional.of(color));
    }

    @Override
    public int getFogColor() {
        return biomeHolder.value().getFogColor();
    }

    @Override
    public void setFogColor(int color) {
        ReflectionHelper.setFieldValue(BiomeSpecialEffects.class, ReflectionMappingsInfo.BiomeSpecialEffects_fogColor, biomeHolder.value().getSpecialEffects(), color);
    }

    @Override
    public int getWaterFogColor() {
        return biomeHolder.value().getWaterFogColor();
    }

    @Override
    public void setWaterFogColor(int color) {
        ReflectionHelper.setFieldValue(BiomeSpecialEffects.class, ReflectionMappingsInfo.BiomeSpecialEffects_waterFogColor, biomeHolder.value().getSpecialEffects(), color);
    }

    private List<EntityType> getSpawnableEntities(MobCategory creatureType) {
        MobSpawnSettings mobs = biomeHolder.value().getMobSettings();
        WeightedRandomList<MobSpawnSettings.SpawnerData> typeSettingList = mobs.getMobs(creatureType);
        List<EntityType> entityTypes = new ArrayList<>();
        if (typeSettingList == null) {
            return entityTypes;
        }
        for (MobSpawnSettings.SpawnerData meta : typeSettingList.unwrap()) {
            entityTypes.add(CraftEntityType.minecraftToBukkit(meta.type));
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
        return biomeHolder.value().climateSettings.temperatureModifier();
    }
}
