package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.enums.EntityAttribute;
import net.aufdemrand.denizen.nms.util.EntityAttributeModifier;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface ItemHelper {

    String getVanillaName(ItemStack itemStack);

    String getJsonString(ItemStack itemStack);

    PlayerProfile getSkullSkin(ItemStack itemStack);

    ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    CompoundTag getNbtData(ItemStack itemStack);

    ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    Map<EntityAttribute, List<EntityAttributeModifier>> getAttributeModifiers(ItemStack itemStack);

    ItemStack setAttributeModifiers(ItemStack itemStack, Map<EntityAttribute, List<EntityAttributeModifier>> modifiers);
}
