package net.aufdemrand.denizen.utilities.maps;

import org.bukkit.map.MapPalette;

import javax.swing.ImageIcon;
import java.awt.Image;

public class MapImage extends MapObject {

    protected ImageIcon imageIcon;
    protected Image image;
    protected boolean resize;

    public MapImage(int x, int y, String file, boolean resize) {
        super(x, y);
        this.imageIcon = new ImageIcon(file);
        this.setImage(imageIcon.getImage());
        this.resize = resize;
    }

    public Image getImage() {
        return resize ? MapPalette.resizeImage(image) : image;
    }

    protected void setImage(Image image) {
        this.image = image;
    }

}
