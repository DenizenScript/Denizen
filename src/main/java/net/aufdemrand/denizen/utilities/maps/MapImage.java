package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapImage extends MapObject {

    protected Image image;
    protected ImageIcon imageIcon;
    protected int width = 0;
    protected int height = 0;
    protected String fileTag;
    protected String actualFile = null;

    public MapImage(String xTag, String yTag, String visibilityTag, boolean debug, String fileTag, int width, int height) {
        super(xTag, yTag, visibilityTag, debug);
        this.fileTag = fileTag;
        if (width > 0 || height > 0) {
            this.width = width > 0 ? width : 0;
            this.height = height > 0 ? height : 0;
        }
    }

    protected void setImage(Image image) {
        this.image = image;
    }

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = super.getSaveData();
        data.put("type", "IMAGE");
        data.put("width", width);
        data.put("height", height);
        data.put("image", fileTag);
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid) {
        if (actualFile == null) {
            String file = fileTag;
            actualFile = DenizenMapManager.getActualFile(file);
            imageIcon = new ImageIcon(actualFile);
            image = imageIcon.getImage();
            if (width == 0) {
                width = image.getWidth(null);
            }
            if (height == 0) {
                height = image.getHeight(null);
            }
        }
        // Use custom functions to draw image to allow transparency and reduce lag intensely
        byte[] bytes = imageToBytes(image, width, height);
        int x = getX(player, uuid);
        int y = getY(player, uuid);
        for (int x2 = 0; x2 < width; ++x2) {
            for (int y2 = 0; y2 < height; ++y2) {
                byte p = bytes[y2 * width + x2];
                if (p != MapPalette.TRANSPARENT)
                    mapCanvas.setPixel(x + x2, y + y2, p);
            }
        }
    }

    private static Color[] bukkitColors = null;
    // Since color conversions will never change, remember them instead of using a bunch of math every single time
    private final static Map<Integer, Byte> colorCache = new HashMap<Integer, Byte>();

    private static byte[] imageToBytes(Image image, int width, int height) {
        if (bukkitColors == null) {
            try {
                Field field = MapPalette.class.getDeclaredField("colors");
                field.setAccessible(true);
                bukkitColors = (Color[]) field.get(null);
            } catch (Exception e) {
                dB.echoError(e);
            }
        }
        BufferedImage temp = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        int[] pixels = new int[width * height];
        temp.getRGB(0, 0, width, height, pixels, 0, width);
        byte[] result = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            if (colorCache.containsKey(pixel)) {
                result[i] = colorCache.get(pixel);
            }
            else {
                byte color = matchColor(new Color(pixel));
                colorCache.put(pixel, color);
                result[i] = color;
            }
        }
        return result;
    }

    private static byte matchColor(Color color) {
        if (color.getAlpha() < 128) return 0;
        int index = 0;
        double best = -1;
        for (int i = 4; i < bukkitColors.length; i++) {
            double distance = getDistance(color, bukkitColors[i]);
            if (distance < best || best == -1) {
                best = distance;
                index = i;
            }
        }
        return (byte) (index < 128 ? index : -129 + (index - 127));
    }

    private static double getDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }

}
