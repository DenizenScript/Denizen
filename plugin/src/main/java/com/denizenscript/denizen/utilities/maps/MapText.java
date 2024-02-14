package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapText extends MapObject {

    protected String textTag, colorTag, fontTag, sizeTag, styleTag;
    protected Map<UUID, String> playerTexts = new HashMap<>();

    public MapText(String xTag, String yTag, String visibilityTag, boolean debug, String textTag, String colorTag, String fontTag, String sizeTag, String styleTag) {
        super(xTag, yTag, visibilityTag, debug);
        this.textTag = textTag;
        this.colorTag = colorTag;
        this.fontTag = fontTag;
        this.sizeTag = sizeTag;
        this.styleTag = styleTag;
    }

    @Override
    public void update(PlayerTag player, UUID uuid) {
        super.update(player, uuid);
        playerTexts.put(uuid, tag(textTag, player));
    }

    public String getText(PlayerTag player) {
        return playerTexts.get(player.getPlayerEntity().getUniqueId());
    }

    public void setText(String textTag) {
        this.textTag = textTag;
    }

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = super.getSaveData();
        data.put("type", "TEXT");
        data.put("text", textTag);
        data.put("color", colorTag);
        data.put("font", fontTag);
        data.put("size", sizeTag);
        data.put("style", styleTag);
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, PlayerTag player, UUID uuid) {
        try {
            if (!playerTexts.containsKey(uuid)) {
                playerTexts.put(uuid, tag(textTag, player));
            }
            ColorTag color = ColorTag.valueOf(colorTag == null ? "black" : tag(colorTag, player), getTagContext(player));
            if (fontTag == null) {
                byte b = MapImage.matchColor(color.getAWTColor());
                String text = ((char) 167) + Byte.toString(b) + ((char) 59) + getText(player);
                mapCanvas.drawText(getX(player), getY(player), MinecraftFont.Font, text);
                return;
            }
            int style = Font.PLAIN;
            if (styleTag != null) {
                TagContext context = getTagContext(player);
                ListTag styles = TagManager.tagObject(styleTag, context).asType(ListTag.class, context);
                for (String styleStr : styles) {
                    String styleLower = CoreUtilities.toLowerCase(styleStr);
                    switch (styleLower) {
                        case "bold" -> style |= Font.BOLD;
                        case "italic" -> style |= Font.ITALIC;
                    }
                }
            }
            int size = sizeTag != null ? TagManager.tagObject(sizeTag, getTagContext(player)).asElement().asInt() : 10;
            BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setFont(new Font(tag(fontTag, player), style, size));
            graphics.setColor(color.getAWTColor());
            FontMetrics metrics = graphics.getFontMetrics();
            int y = getY(player) + metrics.getAscent() - metrics.getDescent() - metrics.getLeading();
            graphics.drawString(getText(player), getX(player), y);
            graphics.dispose();
            mapCanvas.drawImage(0, 0, image);

        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
