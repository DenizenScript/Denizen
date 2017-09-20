package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.*;

public class DenizenMapRenderer extends MapRenderer {

    private final List<MapObject> mapObjects = new ArrayList<MapObject>();
    private final List<MapRenderer> oldMapRenderers;
    private final boolean autoUpdate;

    private boolean active;

    public DenizenMapRenderer(List<MapRenderer> oldMapRenderers, boolean autoUpdate) {
        super(true);
        this.oldMapRenderers = oldMapRenderers;
        this.autoUpdate = autoUpdate;
        this.active = true;
    }

    public void addObject(MapObject object) {
        if (active) {
            mapObjects.add(object);
        }
        else {
            throw new IllegalStateException("DenizenMapRenderer is not active");
        }
    }

    public List<MapRenderer> getOldRenderers() {
        return oldMapRenderers;
    }

    public void deactivate() {
        if (!active) {
            throw new IllegalStateException("Already deactivated");
        }
        this.active = false;
        mapObjects.clear();
        oldMapRenderers.clear();
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, Object> getSaveData() {
        if (active) {
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
        throw new IllegalStateException("DenizenMapRenderer is not active");
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (!DenizenAPI.getCurrentInstance().isEnabled()) {
            // Special case for shutdown borko
            return;
        }
        if (active) {
            try {
                UUID uuid = player.getUniqueId();
                dPlayer p = dPlayer.mirrorBukkitPlayer(player);
                for (MapObject object : mapObjects) {
                    if (autoUpdate) {
                        object.update(p, uuid);
                    }
                    if (object.isVisibleTo(p, uuid)) {
                        object.render(mapView, mapCanvas, p, uuid);
                    }
                }
            }
            catch (Exception e) {
                dB.echoError(e);
                mapView.removeRenderer(this);
            }
        }
        else {
            mapView.removeRenderer(this);
        }
    }

}
