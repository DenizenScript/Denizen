package com.denizenscript.denizen.nms.abstracts;

import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
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

    public String getName() {
        return name;
    }

    public DownfallType getDownfallType() {
        if (!hasDownfall()) {
            return DownfallType.NONE;
        }
        return getBaseTemperature() > 0.15f ? DownfallType.RAIN : DownfallType.SNOW;
    }

    public DownfallType getDownfallTypeAt(Location location) {
        throw new UnsupportedOperationException();
    }

    public abstract float getHumidity();

    public abstract float getBaseTemperature();

    public float getTemperatureAt(Location location) {
        throw new UnsupportedOperationException();
    }

    public boolean hasDownfall() {
        throw new UnsupportedOperationException();
    }

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

    public abstract void setBaseTemperature(float temperature);

    public void setPrecipitation(DownfallType type) {
        throw new UnsupportedOperationException();
    }

    public void setHasDownfall(boolean hasDownfall) {
        throw new UnsupportedOperationException();
    }

    public enum DownfallType {
        RAIN, SNOW, NONE
    }

    public abstract void setFoliageColor(int color);

    public int getFogColor() {
        throw new UnsupportedOperationException();
    }

    public void setFogColor(int color) {
        throw new UnsupportedOperationException();
    }

    public int getWaterFogColor() {
        throw new UnsupportedOperationException();
    }

    public void setWaterFogColor(int color) {
        throw new UnsupportedOperationException();
    }

    public abstract void setTo(Block block);

    public ColorTag getColor(int x, int y) {
        ColorTag topLeft = new ColorTag(26, 191, 0);
        ColorTag topRight = new ColorTag(28, 164, 73);
        ColorTag bottomLeft = new ColorTag(174, 164, 42);
        ColorTag bottomRight = new ColorTag(96, 161, 123);
        float normalizedX = x / 255.0f;
        float normalizedY = y / 255.0f;
        ColorTag lu = scaleColor(topLeft, (1 - normalizedX) * (1 - normalizedY));
        ColorTag ru = scaleColor(topRight, normalizedX * (1 - normalizedY));
        ColorTag ld = scaleColor(bottomLeft, (1 - normalizedX) * normalizedY);
        ColorTag rd = scaleColor(bottomRight, normalizedX * normalizedY);
        int r = lu.red + ld.red + rd.red + ru.red;
        int g = lu.green + ld.green + rd.green + ru.green;
        int b = lu.blue + ld.blue + rd.blue + ru.blue;
        return new ColorTag(r, g, b);
    }

    public float clampColor(float n) {
        if (n < 0.0f) {
            return 0.0f;
        }
        else if (n > 1.0f) {
            return 1.0f;
        }
        return n;
    }

    private ColorTag scaleColor(ColorTag color, float scale) {
        float r = color.red * scale;
        float g = color.green * scale;
        float b = color.blue * scale;
        return new ColorTag((int) r, (int) g, (int) b);
    }
}
