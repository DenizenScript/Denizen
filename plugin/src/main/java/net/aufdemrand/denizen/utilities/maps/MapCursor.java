package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.aH;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapCursor extends MapObject {

    protected String directionTag;
    protected String typeTag;
    protected Map<UUID, org.bukkit.map.MapCursor> cursors = new HashMap<UUID, org.bukkit.map.MapCursor>();

    public MapCursor(String xTag, String yTag, String visibilityTag, boolean debug, String directionTag, String typeTag) {
        super(xTag, yTag, visibilityTag, debug);
        this.directionTag = directionTag;
        this.typeTag = typeTag;
    }

    public byte getDirection(dPlayer player) {
        return yawToDirection(aH.getDoubleFrom(tag(directionTag, player)));
    }

    public org.bukkit.map.MapCursor.Type getType(dPlayer player) {
        return org.bukkit.map.MapCursor.Type.valueOf(tag(typeTag, player).toUpperCase());
    }

    private byte yawToDirection(double yaw) {
        return (byte) (Math.floor((yaw / 22.5) + 0.5) % 16);
    }

    @Override
    public void update(dPlayer player, UUID uuid) {
        super.update(player, uuid);
        if (cursors.containsKey(uuid)) {
            org.bukkit.map.MapCursor cursor = cursors.get(uuid);
            cursor.setX((byte) getX(player, uuid));
            cursor.setY((byte) getY(player, uuid));
            cursor.setVisible(isVisibleTo(player, uuid));
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
    public void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid) {
        org.bukkit.map.MapCursor cursor = new org.bukkit.map.MapCursor((byte) getX(player, uuid),
                (byte) getY(player, uuid), getDirection(player), getType(player).getValue(),
                isVisibleTo(player, uuid));
        mapCanvas.getCursors().addCursor(cursor);
        cursors.put(uuid, cursor);
    }

}
