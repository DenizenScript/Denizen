package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapCursor extends MapObject {

    protected String directionTag;
    protected String typeTag;
    protected Map<UUID, org.bukkit.map.MapCursor> cursors = new HashMap<>();

    public MapCursor(String xTag, String yTag, String visibilityTag, boolean debug, String directionTag, String typeTag) {
        super(xTag, yTag, visibilityTag, debug);
        this.directionTag = directionTag;
        this.typeTag = typeTag;
    }

    public byte getDirection(PlayerTag player) {
        return yawToDirection(Double.parseDouble(tag(directionTag, player)));
    }

    public org.bukkit.map.MapCursor.Type getType(PlayerTag player) {
        return org.bukkit.map.MapCursor.Type.valueOf(tag(typeTag, player).toUpperCase());
    }

    private byte yawToDirection(double yaw) {
        return (byte) (Math.floor((yaw / 22.5) + 0.5) % 16);
    }

    @Override
    public void update(PlayerTag player, UUID uuid) {
        super.update(player, uuid);
        if (cursors.containsKey(uuid)) {
            org.bukkit.map.MapCursor cursor = cursors.get(uuid);
            cursor.setX((byte) getX(player));
            cursor.setY((byte) getY(player));
            cursor.setVisible(isVisibleTo(player));
            cursor.setDirection(getDirection(player));
            cursor.setType(getType(player));
        }
    }

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = super.getSaveData();
        data.put("type", "CURSOR");
        data.put("direction", directionTag);
        data.put("cursor", typeTag);
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, PlayerTag player, UUID uuid) {
        try {
            int x = (getX(player) - 64) * 2;
            int z = (getY(player) - 64) * 2;
            if (x < -127 || z < -127 || x > 127 || z > 127) {
                if (showPastEdge) {
                    x = Math.max(Math.min(x, 127), -127);
                    z = Math.max(Math.min(z, 127), -127);
                }
                else {
                    return;
                }
            }
            org.bukkit.map.MapCursor cursor = new org.bukkit.map.MapCursor((byte) x, (byte) z, getDirection(player), getType(player).getValue(), isVisibleTo(player));
            mapCanvas.getCursors().addCursor(cursor);
            cursors.put(uuid, cursor);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
