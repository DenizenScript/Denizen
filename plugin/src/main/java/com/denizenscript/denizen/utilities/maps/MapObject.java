package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class MapObject {

    protected String xTag;
    protected String yTag;
    protected String visibilityTag = "true";
    protected Map<UUID, Integer> currentX = new HashMap<>();
    protected Map<UUID, Integer> currentY = new HashMap<>();
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
        currentX.put(uuid, getX(player, uuid));
        currentY.put(uuid, getY(player, uuid));
        currentVisibility.put(uuid, tag(visibilityTag, player).equalsIgnoreCase("true"));
    }

    public int getX(PlayerTag player, UUID uuid) {
        //if (!currentX.containsKey(uuid)) {
        int x = (int) Double.parseDouble(tag(xTag, player));
        currentX.put(uuid, x);
        //}
        if (worldCoordinates && lastMap != null) {
            float f = (float) (x - lastMap.getCenterX()) / (1 << (lastMap.getScale().getValue()));
            int bx = ((int) ((f * 2.0F) + 0.5D));
            return (bx < -127 ? -127 : (bx > 127 ? 127 : bx));
        }
        return x;
    }

    public int getY(PlayerTag player, UUID uuid) {
        //if (!currentY.containsKey(uuid)) {
        int y = (int) Double.parseDouble(tag(yTag, player));
        currentY.put(uuid, y);
        //}
        if (worldCoordinates && lastMap != null) {
            float f1 = (float) (y - lastMap.getCenterZ()) / (1 << (lastMap.getScale().getValue()));
            int by = ((int) ((f1 * 2.0F) + 0.5D));
            return (by < -127 ? -127 : (by > 127 ? 127 : by));
        }
        return y;
    }

    public boolean isVisibleTo(PlayerTag player, UUID uuid) {
        if (!currentVisibility.containsKey(uuid)) {
            currentVisibility.put(uuid, tag(visibilityTag, player).equalsIgnoreCase("true"));
        }
        return currentVisibility.get(uuid);
    }

    protected String tag(String arg, PlayerTag player) {
        // Short, reusable TagManager call
        return TagManager.tag(arg, new BukkitTagContext(player, player.getSelectedNPC(), false, null, debug, null));
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
