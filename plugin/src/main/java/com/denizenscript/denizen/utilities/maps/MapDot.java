package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.map.MapCanvas;
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
    public void render(MapView mapView, MapCanvas mapCanvas, PlayerTag player, UUID uuid) {
        try {
            int baseX = getX(player);
            int baseY = getY(player);
            int radius = (int) Double.parseDouble(tag(radiusTag, player));
            ColorTag color = ColorTag.valueOf(tag(colorTag, player), getTagContext(player));
            byte colorId = MapImage.matchColor(color.getAWTColor());
            int max = radius == 0 ? 1 : radius;
            for (int x = -radius; x < max; x++) {
                int finalX = baseX + x;
                if (finalX < 0 || finalX > 127) {
                    continue;
                }
                for (int y = -radius; y < max; y++) {
                    int finalY = baseY + y;
                    if (finalY < 0 || finalY > 127) {
                        continue;
                    }
                    if (((x + 0.5) * (x + 0.5)) + ((y + 0.5) * (y + 0.5)) <= (max * max)) {
                        mapCanvas.setPixel(finalX, finalY, colorId);
                    }
                }
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }
}
