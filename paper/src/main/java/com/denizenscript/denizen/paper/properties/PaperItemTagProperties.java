package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class PaperItemTagProperties implements Property {
    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static PaperItemTagProperties getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        return new PaperItemTagProperties((ItemTag) item);
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    private PaperItemTagProperties(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "PaperItemTagProperties";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ItemTag.rarity>
        // @returns ElementTag
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the rarity of an item, as "common", "uncommon", "rare", or "epic".
        // -->
        PropertyParser.registerTag(PaperItemTagProperties.class, ElementTag.class, "rarity", (attribute, item) -> {
            return new ElementTag(item.item.getItemStack().getRarity());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {
    }
}
