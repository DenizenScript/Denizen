package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
        List<CustomNBT.AttributeReturn> nbtKeys = CustomNBT.getAttributes(itemStack);
        ListTag list = new ListTag();
        if (nbtKeys != null) {
            for (CustomNBT.AttributeReturn atr : nbtKeys) {
                list.add(EscapeTagUtil.escape(atr.attr) + "/" + EscapeTagUtil.escape(atr.slot) + "/" + atr.op + "/" + atr.amt);
            }
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
            itemStack = CustomNBT.clearNBT(itemStack, CustomNBT.KEY_ATTRIBUTES);
            for (String string : list) {
                String[] split = string.split("/");
                if (split.length != 4) {
                    mechanism.echoError("Invalid nbt_attributes input: must have 4 values per attribute.");
                    continue;
                }
                String attribute = EscapeTagUtil.unEscape(split[0]);
                String slot = EscapeTagUtil.unEscape(split[1]);
                int op = new ElementTag(split[2]).asInt();
                double amt = new ElementTag(split[3]).asDouble();
                itemStack = CustomNBT.addAttribute(itemStack, attribute, slot, op, amt);
            }
            item.setItemStack(itemStack);
        }
    }
}
