package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.*;

public class DenizenMapRenderer extends MapRenderer {

    private final List<MapObject> mapObjects = new ArrayList<MapObject>();
    private final List<MapRenderer> oldMapRenderers;
    private final boolean autoUpdate;

    public DenizenMapRenderer(List<MapRenderer> oldMapRenderers, boolean autoUpdate) {
        super(true);
        this.oldMapRenderers = oldMapRenderers;
        this.autoUpdate = autoUpdate;
    }

    public void addObject(MapObject object) {
        mapObjects.add(object);
    }

    public List<MapRenderer> getOldRenderers() {
        return oldMapRenderers;
    }

    public Map<String, Object> getSaveData() {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> objects = new HashMap<String, Object>();
        for (int i = 0; i < mapObjects.size(); i++) {
            Map<String, Object> objectData = mapObjects.get(i).getSaveData();
            objects.put(String.valueOf(i), objectData);
        }
        data.put("objects", objects);
        data.put("auto update", autoUpdate);
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        UUID uuid = player.getUniqueId();
        dPlayer p = dPlayer.mirrorBukkitPlayer(player);
        for (MapObject object : mapObjects) {
            if (autoUpdate)
                object.update(p, uuid);
            if (object.isVisibleTo(p, uuid))
                object.render(mapView, mapCanvas, p, uuid);
        }
    }

}
