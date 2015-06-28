package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiomeNMS {

    private final BiomeBase biomeBase;
    private final String name;

    public BiomeNMS(Biome biome) {
        this.biomeBase = CraftBlock.biomeToBiomeBase(biome);
        this.name = CoreUtilities.toLowerCase(biome.name());
    }

    public DownfallType getDownfallType() {
        if (getDoesSnow())
            return DownfallType.SNOW;
        else if (getDoesRain())
            return DownfallType.RAIN;
        else
            return DownfallType.NONE;
    }

    public String getName() {
        return name;
    }

    public float getHumidity() {
        return biomeBase.humidity;
    }

    public float getTemperature() {
        return biomeBase.temperature;
    }

    private List<EntityType> getSpawnableEntities(EnumCreatureType creatureType) {
        List<EntityType> entityTypes = new ArrayList<EntityType>();
        for (BiomeBase.BiomeMeta meta : (List<BiomeBase.BiomeMeta>) biomeBase.getMobs(creatureType)) {
            entityTypes.add(EntityType.fromId(ENTITY_CLASS_ID_MAP.get(meta.b)));
        }
        return entityTypes;
    }

    public List<EntityType> getAllEntities() {
        List<EntityType> entityTypes = new ArrayList<EntityType>();
        entityTypes.addAll(getAmbientEntities());
        entityTypes.addAll(getCreatureEntities());
        entityTypes.addAll(getMonsterEntities());
        entityTypes.addAll(getWaterEntities());
        return entityTypes;
    }

    public List<EntityType> getAmbientEntities() {
        return getSpawnableEntities(EnumCreatureType.AMBIENT);
    }

    public List<EntityType> getCreatureEntities() {
        return getSpawnableEntities(EnumCreatureType.CREATURE);
    }

    public List<EntityType> getMonsterEntities() {
        return getSpawnableEntities(EnumCreatureType.MONSTER);
    }

    public List<EntityType> getWaterEntities() {
        return getSpawnableEntities(EnumCreatureType.WATER_CREATURE);
    }

    // TODO: figure out how to make this actually work
    public void setDownfallType(DownfallType type) {
        switch (type) {
            case RAIN:
                setDoesRain(true);
                setDoesSnow(false);
                break;
            case SNOW:
                setDoesRain(false);
                setDoesSnow(true);
                break;
            case NONE:
                setDoesRain(false);
                setDoesSnow(false);
                break;
        }
    }

    public void setHumidity(float humidity) {
        biomeBase.humidity = humidity;
    }

    public void setTemperature(float temperature) {
        biomeBase.temperature = temperature;
    }

    public void changeBlockBiome(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        World world = ((CraftWorld) location.getWorld()).getHandle();
        if (world.isLoaded(new BlockPosition(x, 0, z))) {
            Chunk chunk = world.getChunkAtWorldCoords(new BlockPosition(x, 0, z));

            if (chunk != null) {
                byte[] biomevals = chunk.getBiomeIndex();
                biomevals[((z & 0xF) << 4) | (x & 0xF)] = (byte) biomeBase.id;
            }
        }
    }

    public enum DownfallType {
        RAIN, SNOW, NONE
    }

    private boolean getDoesRain() {
        try {
            return DOES_RAIN.getBoolean(biomeBase);
        }
        catch (Exception e) {
            dB.echoError(e);
            return false;
        }
    }

    private boolean getDoesSnow() {
        try {
            return DOES_SNOW.getBoolean(biomeBase);
        }
        catch (Exception e) {
            dB.echoError(e);
            return false;
        }
    }

    private void setDoesRain(boolean doesRain) {
        try {
            DOES_RAIN.set(biomeBase, doesRain);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    private void setDoesSnow(boolean doesSnow) {
        try {
            DOES_SNOW.set(biomeBase, doesSnow);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    private static final Map<Class<? extends Entity>, Integer> ENTITY_CLASS_ID_MAP;
    private static final Field DOES_RAIN;
    private static final Field DOES_SNOW;

    static {
        Map<Class<? extends Entity>, Integer> map = null;
        Field rains = null;
        Field snows = null;
        try {
            Field field = EntityTypes.class.getDeclaredField("f");
            field.setAccessible(true);
            map = (Map<Class<? extends Entity>, Integer>) field.get(null);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        try {
            rains = BiomeBase.class.getDeclaredField("ay");
            rains.setAccessible(true);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        try {
            snows = BiomeBase.class.getDeclaredField("ax");
            snows.setAccessible(true);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        ENTITY_CLASS_ID_MAP = map;
        DOES_RAIN = rains;
        DOES_SNOW = snows;
    }
}
