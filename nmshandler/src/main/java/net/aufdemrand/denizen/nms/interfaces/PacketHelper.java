package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface PacketHelper {

    void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly);

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

    void resetHealth(Player player);

    void showExperience(Player player, float experience, int level);

    void resetExperience(Player player);

    boolean showSignEditor(Player player, Location location);

    void forceSpectate(Player player, Entity entity);
}
