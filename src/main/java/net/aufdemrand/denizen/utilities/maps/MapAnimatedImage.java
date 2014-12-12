package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.UUID;

public class MapAnimatedImage extends MapImage {

    //private final Map<UUID, AnimationObserver> observers = new HashMap<UUID, AnimationObserver>();

    public MapAnimatedImage(String xTag, String yTag, String visibilityTag, boolean debug, String fileTag, int width, int height) {
        super(xTag, yTag, visibilityTag, debug, fileTag, width, height);
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid) {
        super.render(mapView, mapCanvas, player, uuid);
        //if (!observers.containsKey(uuid)) {
            //AnimationObserver observer = new AnimationObserver(uuid, this);
            //imageIcons.get(uuid).setImageObserver(observer);
            AnimationObserver observer = new AnimationObserver(this);
            imageIcon.setImageObserver(observer);
            //observers.put(uuid, observer);
        //}
    }

    public class AnimationObserver implements ImageObserver {

        //private final UUID player;
        private final MapAnimatedImage image;

        //AnimationObserver(UUID player, MapAnimatedImage image) {
        //    this.player = player;
        //    this.image = image;
        //}

        AnimationObserver(MapAnimatedImage image) {
            this.image = image;
        }

        @Override
        public boolean imageUpdate(Image gif, int infoflags, int x, int y, int width, int height) {
            //image.setImage(player, gif);
            image.setImage(gif);
            return true;
        }

    }

}
