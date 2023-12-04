package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;

public class PaperItemExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.rarity>
        // @returns ElementTag
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the rarity of an item, as "common", "uncommon", "rare", or "epic".
        // -->
        ItemTag.tagProcessor.registerTag(ElementTag.class, "rarity", (attribute, item) -> {
            return new ElementTag(item.getItemStack().getRarity());
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {

            // <--[mechanism]
            // @object EntityTag
            // @name damage_item
            // @input MapTag
            // @Plugin Paper
            // @description
            // Damages the ItemTag based on the given entity for the given amount.
            // This runs all vanilla logic associated with damaging an item, like gamemode and enchantment checks, events, stat changes, and advancement triggers.
            // It does not notify the client to play the break animation.
            //
            // @example
            // # Damages your precious boots! :(
            // - inventory adjust damage_item:[entity=<player>;amount=45] slot:37
            // -->
            ItemTag.tagProcessor.registerMechanism("damage_item", false, MapTag.class, (object, mechanism, map) -> {
                EntityTag entity = map.getElement("entity").asType(EntityTag.class, mechanism.context);
                ElementTag amount = map.getElement("amount");
                if (entity == null || !entity.isLivingEntity()) {
                    mechanism.echoError("Specify a valid entity.");
                    return;
                }
                if (amount == null || !amount.isInt()) {
                    mechanism.echoError("Specify a valid amount to damage this item for.");
                    return;
                }
                entity.getLivingEntity().damageItemStack(object.getItemStack(), amount.asInt());
            });
        }
    }
}
