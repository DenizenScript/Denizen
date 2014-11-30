package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.TagManager;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class DenizenMapRenderer extends MapRenderer {

    private final List<MapText> textList = new ArrayList<MapText>();
    private final List<MapImage> imageList = new ArrayList<MapImage>();
    private final List<MapRenderer> oldRenderers;

    public DenizenMapRenderer(List<MapRenderer> oldRenderers) {
        super(true);
        this.oldRenderers = oldRenderers;
    }

    public void addText(int x, int y, String text) {
        textList.add(new MapText(x, y, text));
    }

    public void addImage(int x, int y, String file, boolean resize) {
        if (file.toLowerCase().endsWith(".gif"))
            imageList.add(new MapAnimatedImage(x, y, file, resize));
        else
            imageList.add(new MapImage(x, y, file, resize));
    }

    public List<MapRenderer> getOldRenderers() {
        return oldRenderers;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        dPlayer pl = dPlayer.mirrorBukkitPlayer(player);
        for (MapImage image : imageList) {
            // Use custom function to draw image to allow transparency
            this.drawImage(image.getX(), image.getY(), image.getImage(), mapCanvas);
        }
        for (MapText text : textList) {
            mapCanvas.drawText(text.getX(), text.getY(),
                    MinecraftFont.Font, TagManager.tag(pl, pl.getSelectedNPC(), text.getText()));
        }
    }

    private void drawImage(int x, int y, Image image, MapCanvas canvas) {
        byte[] bytes = MapPalette.imageToBytes(image);
        for (int x2 = 0; x2 < image.getWidth(null); ++x2) {
            for (int y2 = 0; y2 < image.getHeight(null); ++y2) {
                byte p = bytes[y2 * image.getWidth(null) + x2];
                if (p != MapPalette.TRANSPARENT)
                    canvas.setPixel(x + x2, y + y2, p);
            }
        }
    }

}
