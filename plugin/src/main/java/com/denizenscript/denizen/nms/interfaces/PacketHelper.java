package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.scripts.commands.entity.TeleportCommand;
import com.denizenscript.denizen.utilities.maps.MapImage;
import com.denizenscript.denizencore.objects.core.ColorTag;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PacketHelper {

    void setFakeAbsorption(Player player, float value);

    default void resetWorldBorder(Player player) { // TODO: once minimum version is 1.18 or higher, remove from NMS
        player.setWorldBorder(null);
    }

    default void setWorldBorder(Player player, Location center, double size, double currSize, long time, int warningDistance, int warningTime) { // TODO: once minimum version is 1.18 or higher, remove from NMS
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(center);
        if (time > 0) {
            border.setSize(currSize);
            border.setSize(size, time / 1000);
        }
        else {
            border.setSize(size);
        }
        border.setWarningDistance(warningDistance);
        border.setWarningTime(warningTime);
        player.setWorldBorder(border);
    }

    void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly);

    void setFieldOfView(Player player, float fov);

    void respawn(Player player);

    void setVision(Player player, EntityType entityType);

    default void showDemoScreen(Player player) { // TODO: once minimum version is 1.18 or higher, remove from NMS
        player.showDemoScreen();
    }

    void showBlockAction(Player player, Location location, int action, int state);

    default void showBlockCrack(Player player, int id, Location location, int progress) {
        float progressFloat = 0;
        if (progress >= 0 && progress <= 9) {
            // Spigot treats 0 as -1, so replace 0 with 0.1 which will then get floored
            progressFloat = Math.max(progress, 0.1f) / 9f;
        }
        player.sendBlockDamage(location, progressFloat, id);
    }

    default void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag) { // TODO: once minimum version is 1.20, remove in favor of Player#sendBlockUpdate
        throw new UnsupportedOperationException();
    }

    default void showBannerUpdate(Player player, Location location, List<Pattern> patterns) { // TODO: once minimum version is 1.20, remove from NMS
        Banner banner = (Banner) location.getBlock().getState();
        banner.setPatterns(patterns);
        player.sendBlockUpdate(location, banner);
    }

    void showTabListHeaderFooter(Player player, String header, String footer);

    void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    default void showEquipment(Player player, LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) { // TODO: once minimum version is 1.18 or higher, remove from NMS
        player.sendEquipmentChange(entity, equipmentSlot, itemStack);
    }

    default void resetEquipment(Player player, LivingEntity entity) { // TODO: once minimum version is 1.19 or higher, remove from NMS
        EntityEquipment equipment = entity.getEquipment();
        Map<EquipmentSlot, ItemStack> equipmentMap = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipmentMap.put(slot, equipment.getItem(slot));
        }
        player.sendEquipmentChange(entity, equipmentMap);
    }

    default void showHealth(Player player, float health, int food, float saturation) { // TODO: once minimum version is 1.20, remove from NMS
        player.sendHealthUpdate(health, food, saturation);
    }

    void showMobHealth(Player player, LivingEntity mob, double health, double maxHealth);

    default void resetHealth(Player player) { // TODO: once minimum version is 1.20, remove from NMS
        player.sendHealthUpdate(player.getHealth(), player.getFoodLevel(), player.getSaturation());
    }

    void showSignEditor(Player player, Location location); // TODO: once minimum version is 1.18 or higher, change to "showFakeSignEditor" and remove location param

    void forceSpectate(Player player, Entity entity);

    void setNetworkManagerFor(Player player);

    default void enableNetworkManager() {
        // Pre-1.18 do nothing
    }

    void sendRename(Player player, Entity entity, String name, boolean listMode);

    void generateNoCollideTeam(Player player, UUID noCollide);

    void removeNoCollideTeam(Player player, UUID noCollide);

    void sendEntityMetadataFlagsUpdate(Player player, Entity entity);

    void sendEntityEffect(Player player, Entity entity, EntityEffect effect);

    int getPacketStats(Player player, boolean sent);

    default void setMapData(MapCanvas canvas, byte[] bytes, int x, int y, MapImage image) {
        int width = image.width, height = image.height;
        for (int x2 = 0; x2 < width; ++x2) {
            for (int y2 = 0; y2 < height; ++y2) {
                byte p = bytes[y2 * width + x2];
                if (p != MapPalette.TRANSPARENT) {
                    canvas.setPixel(x + x2, y + y2, p);
                }
            }
        }
    }

    void showDebugTestMarker(Player player, Location location, ColorTag color, String name, int time);

    void clearDebugTestMarker(Player player);

    void sendBrand(Player player, String brand);

    default void sendCollectItemEntity(Player player, Entity taker, Entity item, int amount) {
        throw new UnsupportedOperationException();
    }

    default void sendRelativePositionPacket(Player player, double x, double y, double z, float yaw, float pitch, List<TeleportCommand.Relative> relativeMovement) {
        throw new UnsupportedOperationException();
    }

    default void sendRelativeLookPacket(Player player, float yaw, float pitch) {
        throw new UnsupportedOperationException();
    }

    default void sendEntityDataPacket(List<Player> players, Entity entity, List<Object> data) {
        throw new UnsupportedOperationException();
    }
}
