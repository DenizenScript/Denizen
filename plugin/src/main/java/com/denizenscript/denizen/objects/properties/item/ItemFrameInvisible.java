package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTagBuilder;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

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
        CompoundTag entityNbt = NMSHandler.itemHelper.getEntityData(item.getItemStack());
        if (entityNbt == null) {
            return false;
        }
        byte b = entityNbt.getByte("Invisible");
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

    @Override
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
            CompoundTag entityNbt = NMSHandler.itemHelper.getEntityData(item.getItemStack());
            boolean invisible = mechanism.getValue().asBoolean();
            if (!invisible && entityNbt == null) {
                return;
            }
            if (invisible) {
                CompoundTagBuilder builder = entityNbt != null ? entityNbt.createBuilder() : CompoundTagBuilder.create();
                entityNbt = builder.putByte("Invisible", (byte) 1).build();
            }
            else {
                entityNbt = entityNbt.createBuilder().remove("Invisible").build();
            }
            item.setItemStack(NMSHandler.itemHelper.setEntityData(item.getItemStack(), entityNbt, item.getBukkitMaterial() == Material.ITEM_FRAME ? EntityType.ITEM_FRAME : EntityType.GLOW_ITEM_FRAME));
        }
    }
}
