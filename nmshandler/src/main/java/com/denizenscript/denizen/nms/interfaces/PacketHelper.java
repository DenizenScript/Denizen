package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface PacketHelper {

    void setFakeAbsorption(Player player, float value);

    void resetWorldBorder(Player player);

    void setWorldBorder(Player player, Location center, double size, double currSize, long time, int warningDistance, int warningTime);

    void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly);

    void setFieldOfView(Player player, float fov);

    void respawn(Player player);

    void setVision(Player player, EntityType entityType);

    void showDemoScreen(Player player);

    void showBlockAction(Player player, Location location, int action, int state);

    void showBlockCrack(Player player, int id, Location location, int progress);

    void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag);

    void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns);

    void showTabListHeaderFooter(Player player, String header, String footer);

    void resetTabListHeaderFooter(Player player);

    void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void sendActionBarMessage(Player player, String message);

    void showEquipment(Player player, LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack);

    void resetEquipment(Player player, LivingEntity entity);

    void openBook(Player player, EquipmentSlot hand);

    void showHealth(Player player, float health, int food, float saturation);

    void resetHealth(Player player);

    void showExperience(Player player, float experience, int level);

    void resetExperience(Player player);

    boolean showSignEditor(Player player, Location location);

    void forceSpectate(Player player, Entity entity);
}
