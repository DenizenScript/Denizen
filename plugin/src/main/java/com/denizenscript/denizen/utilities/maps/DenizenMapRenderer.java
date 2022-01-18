package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.*;

public class DenizenMapRenderer extends MapRenderer {

    public List<MapObject> mapObjects = new ArrayList<>();

    private List<MapRenderer> oldMapRenderers;

    public boolean autoUpdate;

    public boolean displayOriginal = true;

    private boolean active;

    public boolean hasChanged = true;

    public DenizenMapRenderer(List<MapRenderer> oldMapRenderers, boolean autoUpdate, boolean contextual) {
        super(contextual);
        this.oldMapRenderers = oldMapRenderers;
        if (oldMapRenderers.size() == 1 && oldMapRenderers.get(0) instanceof DenizenMapRenderer) {
            this.oldMapRenderers = ((DenizenMapRenderer) oldMapRenderers.get(0)).oldMapRenderers;
        }
        this.autoUpdate = autoUpdate;
        this.active = true;
    }

    public void addObject(MapObject object) {
        if (!active) {
            throw new IllegalStateException("DenizenMapRenderer is not active");
        }
        mapObjects.add(object);
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
        if (!active) {
            throw new IllegalStateException("DenizenMapRenderer is not active");
        }
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> objects = new HashMap<>();
        for (int i = 0; i < mapObjects.size(); i++) {
            Map<String, Object> objectData = mapObjects.get(i).getSaveData();
            objects.put(String.valueOf(i), objectData);
        }
        data.put("objects", objects);
        data.put("contextual", isContextual());
        data.put("auto update", autoUpdate);
        data.put("original", displayOriginal);
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (!Denizen.getInstance().isEnabled()) {
            // Special case for shutdown borko
            return;
        }
        if (!active) {
            mapView.removeRenderer(this);
            return;
        }
        if (!autoUpdate && !hasChanged && !isContextual()) {
            return;
        }
        try {
            while (mapCanvas.getCursors().size() > 0) {
                mapCanvas.getCursors().removeCursor(mapCanvas.getCursors().getCursor(0));
            }
            if (displayOriginal) {
                for (MapRenderer oldR : oldMapRenderers) {
                    oldR.render(mapView, mapCanvas, player);
                }
            }
            UUID uuid = player.getUniqueId();
            PlayerTag p = PlayerTag.mirrorBukkitPlayer(player);
            for (MapObject object : mapObjects) {
                if (autoUpdate) {
                    object.lastMap = mapView;
                    object.update(p, uuid);
                }
                if (object.isVisibleTo(p)) {
                    object.render(mapView, mapCanvas, p, uuid);
                }
            }
            hasChanged = false;
        }
        catch (Exception e) {
            Debug.echoError(e);
            mapView.removeRenderer(this);
        }
    }
}
