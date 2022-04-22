package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.utilities.maps.MapImage;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.util.List;
import java.util.UUID;

public interface PacketHelper {

    void setFakeAbsorption(Player player, float value);

    void resetWorldBorder(Player player);

    void setWorldBorder(Player player, Location center, double size, double currSize, long time, int warningDistance, int warningTime);

    void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly);

    void setFieldOfView(Player player, float fov);

    void respawn(Player player);

    void setVision(Player player, EntityType entityType);

    default void showDemoScreen(Player player) {
        throw new UnsupportedOperationException();
    }

    void showBlockAction(Player player, Location location, int action, int state);

    void showBlockCrack(Player player, int id, Location location, int progress);

    void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag);

    void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns);

    void showTabListHeaderFooter(Player player, String header, String footer);

    void resetTabListHeaderFooter(Player player);

    void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void showEquipment(Player player, LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack);

    void resetEquipment(Player player, LivingEntity entity);

    void openBook(Player player, EquipmentSlot hand);

    void showHealth(Player player, float health, int food, float saturation);

    default void showMobHealth(Player player, LivingEntity mob, double health, double maxHealth) {
        throw new UnsupportedOperationException();
    }

    void resetHealth(Player player);

    void showExperience(Player player, float experience, int level);

    void resetExperience(Player player);

    boolean showSignEditor(Player player, Location location);

    void forceSpectate(Player player, Entity entity);

    void setNetworkManagerFor(Player player);

    default void enableNetworkManager() {
        // Pre-1.18 do nothing
    }

    default void sendRename(Player player, Entity entity, String name, boolean listMode) {
        throw new UnsupportedOperationException();
    }

    default void generateNoCollideTeam(Player player, UUID noCollide) {
        throw new UnsupportedOperationException();
    }

    default void removeNoCollideTeam(Player player, UUID noCollide) {
        throw new UnsupportedOperationException();
    }

    default void sendEntityMetadataFlagsUpdate(Player player, Entity entity) {
    }

    default void sendEntityEffect(Player player, Entity entity, byte effectId) {
        throw new UnsupportedOperationException();
    }

    default int getPacketStats(Player player, boolean sent) {
        throw new UnsupportedOperationException();
    }

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

    default void showDebugTestMarker(Player player, Location location, ColorTag color, int alpha, String name, int time) {
        throw new UnsupportedOperationException();
    }

    default void clearDebugTestMarker(Player player) {
        throw new UnsupportedOperationException();
    }

    default void sendBrand(Player player, String brand) {
        throw new UnsupportedOperationException();
    }

    default void sendCollectItemEntity(Player player, Entity taker, Entity item, int amount) {
        throw new UnsupportedOperationException();
    }
}
