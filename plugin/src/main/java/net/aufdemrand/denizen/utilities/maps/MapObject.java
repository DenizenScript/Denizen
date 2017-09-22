package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class MapObject {

    protected String xTag;
    protected String yTag;
    protected String visibilityTag = "true";
    protected Map<UUID, Integer> currentX = new HashMap<UUID, Integer>();
    protected Map<UUID, Integer> currentY = new HashMap<UUID, Integer>();
    protected Map<UUID, Boolean> currentVisibility = new HashMap<UUID, Boolean>();
    protected boolean debug;

    public MapView lastMap;

    public boolean worldCoordinates = false;

    public MapObject(String xTag, String yTag, String visibilityTag, boolean debug) {
        this.xTag = xTag;
        this.yTag = yTag;
        this.visibilityTag = visibilityTag;
        this.debug = debug;
    }

    public void update(dPlayer player, UUID uuid) {
        currentX.put(uuid, getX(player, uuid));
        currentY.put(uuid, getY(player, uuid));
        currentVisibility.put(uuid, aH.getBooleanFrom(tag(visibilityTag, player)));
    }

    public int getX(dPlayer player, UUID uuid) {
        //if (!currentX.containsKey(uuid)) {
            int x = (int) aH.getDoubleFrom(tag(xTag, player));
            currentX.put(uuid, x);
        //}
        int tx = x;
        if (worldCoordinates && lastMap != null) {
            float f = (float) (tx - lastMap.getCenterX()) / (1 << (lastMap.getScale().getValue()));
            byte bx = (byte) ((int) ((f * 2.0F) + 0.5D));
            return bx;
        }
        return tx;
    }

    public int getY(dPlayer player, UUID uuid) {
        //if (!currentY.containsKey(uuid)) {
            int y = (int) aH.getDoubleFrom(tag(yTag, player));
            currentY.put(uuid, y);
        //}
        int ty = y;
        if (worldCoordinates && lastMap != null) {
            float f1 = (float) (ty - lastMap.getCenterZ()) / (1 << (lastMap.getScale().getValue()));
            byte by = (byte) ((int) ((f1 * 2.0F) + 0.5D));
            return by;
        }
        return ty;
    }

    public boolean isVisibleTo(dPlayer player, UUID uuid) {
        if (!currentVisibility.containsKey(uuid)) {
            currentVisibility.put(uuid, tag(visibilityTag, player).equalsIgnoreCase("true"));
        }
        return currentVisibility.get(uuid);
    }

    protected String tag(String arg, dPlayer player) {
        // Short, reusable TagManager call
        return TagManager.tag(arg, new BukkitTagContext(player, player.getSelectedNPC(), false, null, debug, null));
    }

    public Map<String, Object> getSaveData() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("x", xTag);
        data.put("y", yTag);
        data.put("visibility", visibilityTag);
        data.put("debug", debug ? "true" : "false");
        data.put("world_coordinates", worldCoordinates ? "true" : "false");
        return data;
    }

    public abstract void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid);

}
