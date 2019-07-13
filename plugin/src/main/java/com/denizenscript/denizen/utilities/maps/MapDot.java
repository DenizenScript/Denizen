package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.dColor;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.aH;
import org.bukkit.Color;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import java.util.Map;
import java.util.UUID;

public class MapDot extends MapObject {

    protected String radiusTag;
    protected String colorTag;

    public MapDot(String xTag, String yTag, String visibilityTag, boolean debug, String radiusTag, String colorTag) {
        super(xTag, yTag, visibilityTag, debug);
        this.radiusTag = radiusTag;
        this.colorTag = colorTag;
    }

    @Override
    public Map<String, Object> getSaveData() {
        Map<String, Object> data = super.getSaveData();
        data.put("type", "DOT");
        data.put("radius", radiusTag);
        data.put("color", colorTag);
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid) {
        int baseX = getX(player, uuid);
        int baseY = getY(player, uuid);
        int radius = (int) aH.getDoubleFrom(tag(radiusTag, player));
        Color color = dColor.valueOf(tag(colorTag, player)).getColor();
        for (int x = -radius; x < radius; x++) {
            int finalX = baseX + x;
            if (finalX >= 128) {
                continue;
            }
            for (int y = -radius; y < radius; y++) {
                int finalY = baseY + y;
                if (finalY >= 128) {
                    continue;
                }
                if (((x + 0.5) * (x + 0.5)) + ((y + 0.5) * (y + 0.5)) <= (radius * radius)) {
                    mapCanvas.setPixel(finalX, finalY, MapPalette.matchColor(color.getRed(), color.getGreen(), color.getBlue()));
                }
            }
        }
    }
}
