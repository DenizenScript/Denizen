package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.enums.EntityAttribute;
import com.denizenscript.denizen.nms.util.EntityAttributeModifier;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public interface ItemHelper {

    String getInternalNameFromMaterial(Material material);

    Material getMaterialFromInternalName(String internalName);

    String getJsonString(ItemStack itemStack);

    PlayerProfile getSkullSkin(ItemStack itemStack);

    ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    CompoundTag getNbtData(ItemStack itemStack);

    ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag);

    default Map<EntityAttribute, List<EntityAttributeModifier>> getAttributeModifiers(ItemStack itemStack) {
        Map<EntityAttribute, List<EntityAttributeModifier>> modifiers = new HashMap<>();
        List<Tag> modifierList = getNbtData(itemStack).getList("AttributeModifiers");
        for (Tag tag : modifierList) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }
            CompoundTag modifier = (CompoundTag) tag;
            EntityAttribute attribute = EntityAttribute.getByName(modifier.getString("AttributeName"));
            if (attribute == null) {
                continue;
            }
            if (!modifiers.containsKey(attribute)) {
                modifiers.put(attribute, new ArrayList<>());
            }
            UUID uuid = new UUID(modifier.getLong("UUIDMost"), modifier.getLong("UUIDLeast"));
            String name = modifier.getString("Name");
            EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.values()[modifier.getInt("Operation")];
            if (operation == null) {
                continue;
            }
            double amount = modifier.getDouble("Amount");
            modifiers.get(attribute).add(new EntityAttributeModifier(uuid, name, operation, amount));
        }
        return modifiers;
    }

    ItemStack setAttributeModifiers(ItemStack itemStack, Map<EntityAttribute, List<EntityAttributeModifier>> modifiers);

    PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon);
}
