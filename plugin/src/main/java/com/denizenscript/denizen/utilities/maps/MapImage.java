package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

public class MapImage extends MapObject {

    public byte[] cachedImageData = null;
    public Image imageForCache = null;
    public Image image;
    public ImageIcon imageIcon;
    public int width = 0;
    public int height = 0;
    public String fileTag;
    public String actualFile = null;
    public boolean disabled = false;
    public DenizenMapRenderer renderer;

    public MapImage(DenizenMapRenderer renderer, String xTag, String yTag, String visibilityTag, boolean debug, String fileTag, int width, int height) {
        super(xTag, yTag, visibilityTag, debug);
        this.fileTag = fileTag;
        if (width > 0 || height > 0) {
            this.width = width > 0 ? width : 0;
            this.height = height > 0 ? height : 0;
        }
        this.renderer = renderer;
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
    public void render(MapView mapView, MapCanvas mapCanvas, PlayerTag player, UUID uuid) {
        try {
            if (actualFile == null) {
                actualFile = DenizenMapManager.getActualFile(fileTag);
                if (actualFile == null) {
                    disabled = true;
                    return;
                }
                imageIcon = new ImageIcon(actualFile);
                image = imageIcon.getImage();
                image.getSource().addConsumer(new ImageConsumer() {
                    @Override
                    public void setDimensions(int width, int height) {
                    }

                    @Override
                    public void setProperties(Hashtable<?, ?> props) {
                    }

                    @Override
                    public void setColorModel(ColorModel model) {
                    }

                    @Override
                    public void setHints(int hintflags) {
                    }

                    @Override
                    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
                        // When the internal pixels are updated, the cache is no longer current.
                        cachedImageData = null;
                        renderer.hasChanged = true;
                    }

                    @Override
                    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
                    }

                    @Override
                    public void imageComplete(int status) {
                    }
                });
                if (width == 0) {
                    width = image.getWidth(null);
                }
                if (height == 0) {
                    height = image.getHeight(null);
                }
                if (width == -1 || height == -1) {
                    Debug.echoError("Image loading failed (bad width/height) for image " + fileTag);
                    disabled = true;
                    return;
                }
                disabled = false;
            }
            if (disabled) {
                return;
            }
            // Use custom functions to draw image to allow transparency and reduce lag intensely
            byte[] bytes;
            if (cachedImageData == null || image != imageForCache) {
                bytes = imageToBytes(image, width, height);
                if (bytes == null) {
                    Debug.echoError("Image loading failed (bad imageToBytes) for image " + fileTag);
                    disabled = true;
                    return;
                }
                cachedImageData = bytes;
                imageForCache = image;
            }
            else {
                bytes = cachedImageData;
            }
            int x = getX(player);
            int y = getY(player);
            NMSHandler.packetHelper.setMapData(mapCanvas, bytes, x, y, this);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    private static final Color[] bukkitColors;

    static {
        Color[] colors = null;
        try {
            Field field = MapPalette.class.getDeclaredField("colors");
            field.setAccessible(true);
            colors = (Color[]) field.get(null);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        bukkitColors = colors;
    }

    public static byte[] imageToBytes(Image image, int width, int height) {
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = temp.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        int[] pixels = new int[width * height];
        temp.getRGB(0, 0, width, height, pixels, 0, width);
        byte[] result = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = matchColor(new Color(pixels[i], true));
        }
        return result;
    }

    public static HashMap<Color, Byte> colorCache = new HashMap<>(1024);

    public static byte matchColor(Color color) {
        if (color.getAlpha() < 128) {
            return 0;
        }
        Byte result = colorCache.get(color);
        if (result != null) {
            return result;
        }
        int index = 0;
        double best = -1;
        for (int i = 4; i < bukkitColors.length; i++) {
            double distance = getDistance(color, bukkitColors[i]);
            if (distance < best || best == -1) {
                best = distance;
                index = i;
            }
        }
        byte gotten = (byte) (index < 128 ? index : -129 + (index - 127));
        if (colorCache.size() < 1024 * 16) {
            colorCache.put(color, gotten);
        }
        return gotten;
    }

    public static double getDistance(Color c1, Color c2) {
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
