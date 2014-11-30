package net.aufdemrand.denizen.utilities.maps;

import java.awt.*;
import java.awt.image.ImageObserver;

public class MapAnimatedImage extends MapImage implements ImageObserver {

    public MapAnimatedImage(int x, int y, String file, boolean resize) {
        super(x, y, file, resize);
        imageIcon.setImageObserver(this);
    }

    @Override
    public boolean imageUpdate(Image gif, int infoflags, int x, int y, int width, int height) {
        this.setImage(gif);
        return true;
    }

}
