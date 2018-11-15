package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.enums.EntityAttribute;
import net.aufdemrand.denizen.nms.util.EntityAttributeModifier;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public interface ItemHelper {

    String getInternalNameFromMaterial(Material material);

    Material getMaterialFromInternalName(String internalName);

    String getJsonString(ItemStack itemStack);

    PlayerProfile getSkullSkin(ItemStack itemStack);

    ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    CompoundTag getNbtData(ItemStack itemStack);

    ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    Map<EntityAttribute, List<EntityAttributeModifier>> getAttributeModifiers(ItemStack itemStack);

    ItemStack setAttributeModifiers(ItemStack itemStack, Map<EntityAttribute, List<EntityAttributeModifier>> modifiers);

    PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon);
}
