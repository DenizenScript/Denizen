package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MapText extends MapObject {

    protected String textTag;
    protected Map<UUID, String> playerTexts = new HashMap<>();

    public MapText(String xTag, String yTag, String visibilityTag, boolean debug, String textTag) {
        super(xTag, yTag, visibilityTag, debug);
        this.textTag = textTag;
    }

    @Override
    public void update(dPlayer player, UUID uuid) {
        super.update(player, uuid);
        playerTexts.put(uuid, tag(textTag, player));
    }

    public String getText(dPlayer player) {
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
        return data;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, dPlayer player, UUID uuid) {
        if (!playerTexts.containsKey(uuid)) {
            playerTexts.put(uuid, tag(textTag, player));
        }
        mapCanvas.drawText(getX(player, uuid), getY(player, uuid), MinecraftFont.Font, getText(player));
    }

}
