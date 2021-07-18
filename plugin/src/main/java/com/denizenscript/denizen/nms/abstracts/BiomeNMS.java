package com.denizenscript.denizen.nms.abstracts;

import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public abstract class BiomeNMS {

    public String name;

    public World world;

    public BiomeNMS(World world, String name) {
        this.world = world;
        this.name = CoreUtilities.toLowerCase(name);
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
        List<EntityType> entityTypes = new ArrayList<>();
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

    public enum DownfallType {
        RAIN, SNOW, NONE
    }

    protected abstract boolean getDoesRain();

    protected abstract boolean getDoesSnow();

    public abstract void setTo(Block block);
}
