package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface PacketHelper {

    void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly);

    void showBlockAction(Player player, Location location, int action, int state);

    void showBlockCrack(Player player, int id, Location location, int progress);

    void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag);

    void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns);
}
