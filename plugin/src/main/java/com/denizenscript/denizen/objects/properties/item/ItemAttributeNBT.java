package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class ItemAttributeNBT implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static ItemAttributeNBT getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemAttributeNBT((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "nbt_attributes"
    };

    public static final String[] handledMechs = new String[] {
            "nbt_attributes"
    };

    public ItemAttributeNBT(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("nbt_attributes")) {
            BukkitImplDeprecations.legacyAttributeProperties.warn(attribute.context);
            return getList().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getList() {
        ItemStack itemStack = item.getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        ListTag list = new ListTag();
        if (meta == null || !meta.hasAttributeModifiers()) {
            return list;
        }
        for (Map.Entry<org.bukkit.attribute.Attribute, AttributeModifier> entry : meta.getAttributeModifiers().entries()) {
            AttributeModifier modifier = entry.getValue();
            String slotName = toLegacyName(modifier.getSlot() != null ? modifier.getSlot() : EquipmentSlot.HAND);
            list.add(EscapeTagUtil.escape(entry.getKey().getKey().getKey()) + "/" + EscapeTagUtil.escape(slotName) + "/" + modifier.getOperation().ordinal() + "/" + modifier.getAmount());
        }
        return list;
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "nbt_attributes";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("nbt_attributes")) {
            BukkitImplDeprecations.legacyAttributeProperties.warn(mechanism.context);
            if (item.getMaterial().getMaterial() == Material.AIR) {
                mechanism.echoError("Cannot apply NBT to AIR!");
                return;
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            ItemStack itemStack = item.getItemStack();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasAttributeModifiers()) {
                meta.getAttributeModifiers().keySet().forEach(meta::removeAttributeModifier);
            }
            for (String string : list) {
                String[] split = string.split("/");
                if (split.length != 4) {
                    mechanism.echoError("Invalid nbt_attributes input: must have 4 values per attribute.");
                    continue;
                }
                String attribute = fixAttributeName1_16(EscapeTagUtil.unEscape(split[0]));
                String slot = EscapeTagUtil.unEscape(split[1]);
                int op = new ElementTag(split[2]).asInt();
                double amt = new ElementTag(split[3]).asDouble();
                long uuidhelp = uuidChoice(itemStack);
                int attribsSize = meta.hasAttributeModifiers() ? meta.getAttributeModifiers().values().size() : 0;
                UUID fullUuid = new UUID(uuidhelp + 88512 + attribsSize, uuidhelp * 2 + 1250025L + attribsSize);
                meta.addAttributeModifier(Registry.ATTRIBUTE.get(NamespacedKey.minecraft(attribute)), new AttributeModifier(fullUuid, attribute, amt, AttributeModifier.Operation.values()[op], fromLegacyName(slot)));
            }
            itemStack.setItemMeta(meta);
            item.setItemStack(itemStack);
        }
    }

    public static final AsciiMatcher uppercaseMatcher = new AsciiMatcher(AsciiMatcher.LETTERS_UPPER);

    public static final HashMap<String, String> attributeNameUpdates = new HashMap<>();

    static {
        attributeNameUpdates.put("generic.maxHealth", "generic.max_health");
        attributeNameUpdates.put("generic.followRange", "generic.follow_range");
        attributeNameUpdates.put("generic.knockbackResistance", "generic.knockback_resistance");
        attributeNameUpdates.put("generic.movementSpeed", "generic.movement_speed");
        attributeNameUpdates.put("generic.flyingSpeed", "generic.flying_speed");
        attributeNameUpdates.put("generic.attackDamage", "generic.attack_damage");
        attributeNameUpdates.put("generic.attackKnockback", "generic.attack_knockback");
        attributeNameUpdates.put("generic.attackSpeed", "generic.attack_speed");
        attributeNameUpdates.put("generic.armorToughness", "generic.armor_toughness");
    }

    public static String fixAttributeName1_16(String input) {
        if (!uppercaseMatcher.containsAnyMatch(input)) {
            return input;
        }
        String replacement = attributeNameUpdates.get(input);
        if (replacement != null) {
            return replacement;
        }
        return CoreUtilities.toLowerCase(input);
    }

    public static long uuidChoice(ItemStack its) {
        String mat = CoreUtilities.toLowerCase(its.getType().name());
        if (mat.contains("boots")) {
            return 1000;
        }
        else if (mat.contains("legging")) {
            return 100000;
        }
        else if (mat.contains("helmet")) {
            return 10000000;
        }
        else if (mat.contains("chestp")) {
            return 1000000000;
        }
        else {
            return 1;
        }
    }

    private static EquipmentSlot fromLegacyName(String name) {
        return switch (name) {
            case "mainhand" -> EquipmentSlot.HAND;
            case "offhand" -> EquipmentSlot.OFF_HAND;
            default -> {
                EquipmentSlot slot = ElementTag.asEnum(EquipmentSlot.class, name);
                yield slot != null ? slot : EquipmentSlot.HAND;
            }
        };
    }

    private static String toLegacyName(EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> "mainhand";
            case OFF_HAND -> "offhand";
            default -> CoreUtilities.toLowerCase(slot.name());
        };
    }
}
