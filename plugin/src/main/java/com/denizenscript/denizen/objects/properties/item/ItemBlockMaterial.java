package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.meta.BlockDataMeta;

public class ItemBlockMaterial implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockDataMeta;
    }

    public static ItemBlockMaterial getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemBlockMaterial((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "block_material"
    };

    public static final String[] handledMechs = new String[] {
            "block_material"
    };

    private ItemBlockMaterial(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.block_material>
        // @returns MaterialTag
        // @mechanism ItemTag.block_material
        // @group properties
        // @description
        // Returns the material for an item with complex-block-data attached.
        // -->
        if (attribute.startsWith("block_material")) {
            BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();
            if (meta.hasBlockData()) {
                return new MaterialTag(new ModernBlockData(meta.getBlockData(item.getItemStack().getType())))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();
        if (meta.hasBlockData()) {
            return new MaterialTag(new ModernBlockData(meta.getBlockData(item.getItemStack().getType()))).identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "block_material";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name block_material
        // @input MaterialTag
        // @description
        // Attaches complex-block-data from a material to an item.
        // @tags
        // <ItemTag.block_material>
        // -->
        if (mechanism.matches("block_material") && mechanism.requireObject(MaterialTag.class)) {
            BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();
            MaterialTag mat = mechanism.valueAsType(MaterialTag.class);
            meta.setBlockData(mat.getModernData().data);
            item.setItemMeta(meta);
        }
    }
}
