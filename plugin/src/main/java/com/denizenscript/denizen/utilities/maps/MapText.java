package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapText extends MapObject {

    protected String textTag, colorTag;
    protected Map<UUID, String> playerTexts = new HashMap<>();

    public MapText(String xTag, String yTag, String visibilityTag, boolean debug, String textTag, String colorTag) {
        super(xTag, yTag, visibilityTag, debug);
        this.textTag = textTag;
        this.colorTag = colorTag;
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
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, PlayerTag player, UUID uuid) {
        try {
            if (!playerTexts.containsKey(uuid)) {
                playerTexts.put(uuid, tag(textTag, player));
            }
            ColorTag color = ColorTag.valueOf(colorTag == null ? "black" : tag(colorTag, player), getTagContext(player));
            byte b = MapImage.matchColor(color.getAWTColor());
            String text = ((char) 167) + Byte.toString(b) + ((char) 59) + getText(player);
            mapCanvas.drawText(getX(player), getY(player), MinecraftFont.Font, text);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
