package com.denizenscript.denizen.nms.v1_19.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.v1_19.ReflectionMappingsInfo;
import com.denizenscript.denizencore.objects.core.ColorTag;
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
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.EntityType;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BiomeNMSImpl extends BiomeNMS {

    public static final MethodHandle BIOME_CLIMATESETTINGS_CONSTRUCTOR = ReflectionHelper.getConstructor(Biome.class.getDeclaredClasses()[0], boolean.class, float.class, Biome.TemperatureModifier.class, float.class);

    public Holder<Biome> biomeHolder;
    public ServerLevel world;

    public BiomeNMSImpl(ServerLevel world, String name) {
        super(world.getWorld(), name);
        this.world = world;
        biomeHolder = world.registryAccess().registryOrThrow(Registries.BIOME).getHolder(ResourceKey.create(Registries.BIOME, new ResourceLocation(name))).orElse(null);
    }

    @Override
    public DownfallType getDownfallType() { // TODO: 1.19.4: This is no longer valid, downfall is based on height now
        throw new UnsupportedOperationException();
        /*
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
        }*/
    }

    @Override
    public float getHumidity() {
        return biomeHolder.value().climateSettings.downfall();
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

    @Override
    public int getFoliageColor() {
        // Check if the biome already has a default color
        if (biomeHolder.value().getFoliageColor() != 0) {
            return biomeHolder.value().getFoliageColor();
        }

        // Based on net.minecraft.world.level.biome.Biome#getFoliageColorFromTexture()
        float temperature = clampColor(getTemperature());
        float humidity = clampColor(getHumidity());

        // Based on net.minecraft.world.level.FoliageColor#get()
        humidity *= temperature;
        int humidityValue = (int)((1.0f - humidity) * 255.0f);
        int temperatureValue = (int)((1.0f - temperature) * 255.0f);
        int index = temperatureValue << 8 | humidityValue;
        return index >= 65536 ? 4764952 : getColor(index / 256, index % 256).asRGB();
    }

    public Object getClimate() {
        return ReflectionHelper.getFieldValue(Biome.class, ReflectionMappingsInfo.Biome_climateSettings, biomeHolder.value());
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
        setClimate(biomeHolder.value().climateSettings.hasPrecipitation(), getTemperature(), getTemperatureModifier(), humidity);
    }

    @Override
    public void setTemperature(float temperature) {
        setClimate(biomeHolder.value().hasPrecipitation(), temperature, getTemperatureModifier(), getHumidity());
    }

    @Override
    public void setPrecipitation(DownfallType type) { // TODO: 1.19.4: This is no longer valid, downfall is based on height now
        throw new UnsupportedOperationException();
        /*
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
        setClimate(nmsType, getTemperature(), getTemperatureModifier(), getHumidity());*/
    }

    @Override
    public void setFoliageColor(int color) {
        try {
            ReflectionHelper.setFieldValue(BiomeSpecialEffects.class, ReflectionMappingsInfo.BiomeSpecialEffects_foliageColorOverride, biomeHolder.value().getSpecialEffects(), Optional.of(color));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
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
