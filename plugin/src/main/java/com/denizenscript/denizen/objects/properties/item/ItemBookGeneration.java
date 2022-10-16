package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

public class ItemBookGeneration implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag && ((ItemTag) item).getBukkitMaterial() == Material.WRITTEN_BOOK;
    }

    public static ItemBookGeneration getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemBookGeneration((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "book_generation"
    };

    public static final String[] handledMechs = new String[] {
            "book_generation"
    };

    private ItemBookGeneration(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.book_generation>
        // @returns ListTag
        // @mechanism ItemTag.book_generation
        // @group properties
        // @description
        // Returns the generation of the book (if any), as ORIGINAL, COPY_OF_ORIGINAL, COPY_OF_COPY, or TATTERED.
        // -->
        if (attribute.startsWith("book_generation")) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            if (!meta.hasGeneration()) {
                return null;
            }
            return new ElementTag(meta.getGeneration()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (!meta.hasGeneration()) {
            return null;
        }
        return meta.getGeneration().name();
    }

    @Override
    public String getPropertyId() {
        return "book_generation";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name book_generation
        // @input ListTag
        // @description
        // Sets the generation of the book (if any), as ORIGINAL, COPY_OF_ORIGINAL, COPY_OF_COPY, or TATTERED.
        // @tags
        // <ItemTag.book_generation>
        // -->
        if (mechanism.matches("book_generation") && mechanism.requireEnum(BookMeta.Generation.class)) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.setGeneration(BookMeta.Generation.valueOf(mechanism.getValue().asString().toUpperCase()));
            item.setItemMeta(meta);
        }

    }
}
