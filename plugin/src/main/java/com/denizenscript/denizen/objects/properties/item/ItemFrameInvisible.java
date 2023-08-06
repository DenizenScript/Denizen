package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.ByteTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemFrameInvisible implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof ItemTag &&
                (((ItemTag) object).getBukkitMaterial() == Material.ITEM_FRAME
                || ((ItemTag) object).getBukkitMaterial() == Material.GLOW_ITEM_FRAME);
    }

    public static ItemFrameInvisible getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        return new ItemFrameInvisible((ItemTag) object);
    }

    public static final String[] handledTags = new String[] {
            "invisible"
    };

    public static final String[] handledMechs = new String[] {
            "invisible"
    };

    public ItemFrameInvisible(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public boolean isInvisible() {
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(item.getItemStack());
        if (compoundTag == null) {
            return false;
        }
        CompoundTag entTag = (CompoundTag) compoundTag.getValue().get("EntityTag");
        if (entTag == null) {
            return false;
        }
        byte b = entTag.getByte("Invisible");
        return b == 1;
    }

    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.invisible>
        // @returns ElementTag(Boolean)
        // @group properties
        // @mechanism ItemTag.invisible
        // @description
        // Returns whether an Item_Frame item will be invisible when placed.
        // -->
        if (attribute.startsWith("invisible")) {
            return new ElementTag(isInvisible()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public String getPropertyString() {
        if (isInvisible()) {
            return "true";
        }
        return null;
    }

    public String getPropertyId() {
        return "invisible";
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name invisible
        // @input ElementTag(Boolean)
        // @description
        // Changes whether an Item_Frame item will be invisible when placed.
        // @tags
        // <ItemTag.invisible>
        // -->
        if (mechanism.matches("invisible") && mechanism.requireBoolean()) {
            CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(item.getItemStack());
            Map<String, Tag> result = new LinkedHashMap<>(compoundTag.getValue());
            CompoundTag entityTag = (CompoundTag) result.get("EntityTag");
            Map<String, Tag> entMap;
            if (entityTag != null) {
                entMap = new LinkedHashMap<>(entityTag.getValue());
            }
            else {
                entMap = new LinkedHashMap<>();
            }
            if (mechanism.getValue().asBoolean()) {
                entMap.put("Invisible", new ByteTag((byte) 1));
            }
            else {
                entMap.remove("Invisible");
            }
            if (entMap.isEmpty()) {
                result.remove("EntityTag");
            }
            else {
                result.put("EntityTag", NMSHandler.instance.createCompoundTag(entMap));
            }
            compoundTag = NMSHandler.instance.createCompoundTag(result);
            item.setItemStack(NMSHandler.itemHelper.setNbtData(item.getItemStack(), compoundTag));
        }
    }
}
