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

    public abstract DownfallType getDownfallType();

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

    public abstract int getFoliageColor();

    public abstract void setHumidity(float humidity);

    public abstract void setTemperature(float temperature);

    public void setPrecipitation(DownfallType type) {
        throw new UnsupportedOperationException();
    }

    public enum DownfallType {
        RAIN, SNOW, NONE
    }

    public abstract void setFoliageColor(int color);

    public abstract void setTo(Block block);
}
