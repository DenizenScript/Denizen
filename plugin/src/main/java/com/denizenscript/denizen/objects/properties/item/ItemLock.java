package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lockable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemLock implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemStack().getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemStack().getItemMeta()).getBlockState() instanceof Lockable;
    }

    public static ItemLock getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemLock((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "lock", "is_locked"
    };

    public static final String[] handledMechs = new String[] {
            "lock"
    };

    private String getItemLock() {
        return ((Lockable) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).getLock();
    }

    private boolean isLocked() {
        return ((Lockable) ((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()).isLocked();
    }

    private ItemLock(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.lock>
        // @returns ElementTag
        // @mechanism ItemTag.lock
        // @group properties
        // @description
        // Returns the lock password of this item.
        // -->
        if (attribute.startsWith("lock")) {
            return new ElementTag(isLocked() ? getItemLock() : null).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.is_locked>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.lock
        // @group properties
        // @description
        // Returns whether this item has a lock password.
        // -->
        if (attribute.startsWith("is_locked")) {
            return new ElementTag(isLocked()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        return isLocked() ? getItemLock() : null;
    }

    @Override
    public String getPropertyId() {
        return "lock";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name lock
        // @input ElementTag
        // @description
        // Sets the item's lock password.
        // Locked blocks can only be opened while holding an item with the name of the lock.
        // @tags
        // <ItemTag.lock>
        // <ItemTag.is_locked>
        // <ItemTag.is_lockable>
        // -->
        if (mechanism.matches("lock")) {
            ItemStack itemStack = item.getItemStack();
            BlockStateMeta bsm = ((BlockStateMeta) itemStack.getItemMeta());
            Lockable lockable = (Lockable) bsm.getBlockState();

            lockable.setLock(mechanism.hasValue() ? mechanism.getValue().asString() : null);
            bsm.setBlockState((BlockState) lockable);
            itemStack.setItemMeta(bsm);
        }
    }
}
