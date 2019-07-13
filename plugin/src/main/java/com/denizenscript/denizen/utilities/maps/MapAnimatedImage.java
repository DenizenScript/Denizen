package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.dPlayer;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.UUID;

public class MapAnimatedImage extends MapImage {

    protected AnimationObserver observer = null;

    public MapAnimatedImage(String xTag, String yTag, String visibilityTag, boolean debug, String fileTag, int width, int height) {
        super(xTag, yTag, visibilityTag, debug, fileTag, width, height, false);
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid) {
        super.render(mapView, mapCanvas, player, uuid);
        if (observer == null) {
            observer = new AnimationObserver(this);
            imageIcon.setImageObserver(observer);
        }
    }

    public class AnimationObserver implements ImageObserver {

        private final MapAnimatedImage image;

        AnimationObserver(MapAnimatedImage image) {
            this.image = image;
        }

        @Override
        public boolean imageUpdate(Image gif, int infoflags, int x, int y, int width, int height) {
            image.setImage(gif);
            return true;
        }

    }

}
