package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class MapObject {

    protected String xTag;
    protected String yTag;
    protected String visibilityTag;
    protected Map<UUID, Boolean> currentVisibility = new HashMap<>();
    protected boolean debug;

    public MapView lastMap;

    public boolean worldCoordinates = false;

    public MapObject(String xTag, String yTag, String visibilityTag, boolean debug) {
        this.xTag = xTag;
        this.yTag = yTag;
        this.visibilityTag = visibilityTag;
        this.debug = debug;
    }

    public void update(PlayerTag player, UUID uuid) {
        currentVisibility.put(uuid, tag(visibilityTag, player).equalsIgnoreCase("true"));
    }

    public int getX(PlayerTag player) {
        int x = (int) Double.parseDouble(tag(xTag, player));
        if (worldCoordinates && lastMap != null) {
            float f = (float) (x - lastMap.getCenterX()) / (2 << (lastMap.getScale().getValue()));
            return ((int) ((f * 2.0F) + 0.5D));
        }
        return x;
    }

    public int getY(PlayerTag player) {
        int y = (int) Double.parseDouble(tag(yTag, player));
        if (worldCoordinates && lastMap != null) {
            float f1 = (float) (y - lastMap.getCenterZ()) / (2 << (lastMap.getScale().getValue()));
            return ((int) ((f1 * 2.0F) + 0.5D));
        }
        return y;
    }

    public boolean isVisibleTo(PlayerTag player) {
        if (!currentVisibility.containsKey(player.getUUID())) {
            currentVisibility.put(player.getUUID(), tag(visibilityTag, player).equalsIgnoreCase("true"));
        }
        return currentVisibility.get(player.getUUID());
    }

    public TagContext getTagContext(PlayerTag player) {
        return new BukkitTagContext(player, player.getSelectedNPC(), null, debug, null);
    }

    protected String tag(String arg, PlayerTag player) {
        // Short, reusable TagManager call
        return TagManager.tag(arg, getTagContext(player));
    }

    public Map<String, Object> getSaveData() {
        Map<String, Object> data = new HashMap<>();
        data.put("x", xTag);
        data.put("y", yTag);
        data.put("visibility", visibilityTag);
        data.put("debug", debug ? "true" : "false");
        data.put("world_coordinates", worldCoordinates ? "true" : "false");
        return data;
    }

    public abstract void render(MapView mapView, MapCanvas mapCanvas, PlayerTag player, UUID uuid);

}
