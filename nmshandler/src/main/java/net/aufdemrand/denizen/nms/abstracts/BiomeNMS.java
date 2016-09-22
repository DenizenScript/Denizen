package net.aufdemrand.denizen.nms.abstracts;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public abstract class BiomeNMS {

    private final String name;

    public BiomeNMS(Biome biome) {
        this.name = biome.name();
    }

    public DownfallType getDownfallType() {
        if (getDoesSnow()) {
            return DownfallType.SNOW;
        }
        else if (getDoesRain()) {
            return DownfallType.RAIN;
        }
        else {
            return DownfallType.NONE;
        }
    }

    public String getName() {
        return name;
    }

    public abstract float getHumidity();

    public abstract float getTemperature();

    public List<EntityType> getAllEntities() {
        List<EntityType> entityTypes = new ArrayList<EntityType>();
        entityTypes.addAll(getAmbientEntities());
        entityTypes.addAll(getCreatureEntities());
        entityTypes.addAll(getMonsterEntities());
        entityTypes.addAll(getWaterEntities());
        return entityTypes;
    }

    public abstract List<EntityType> getAmbientEntities();

    public abstract List<EntityType> getCreatureEntities();

    public abstract List<EntityType> getMonsterEntities();

    public abstract List<EntityType> getWaterEntities();

    public abstract void setHumidity(float humidity);

    public abstract void setTemperature(float temperature);

    public abstract void changeBlockBiome(Location location);

    public enum DownfallType {
        RAIN, SNOW, NONE
    }

    protected abstract boolean getDoesRain();

    protected abstract boolean getDoesSnow();
}
