package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public interface ItemHelper {

    String getInternalNameFromMaterial(Material material);

    Material getMaterialFromInternalName(String internalName);

    String getJsonString(ItemStack itemStack);

    PlayerProfile getSkullSkin(ItemStack itemStack);

    ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    CompoundTag getNbtData(ItemStack itemStack);

    ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon);
}
