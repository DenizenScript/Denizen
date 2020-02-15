package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemSkullskin implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemStack().getItemMeta() instanceof SkullMeta;
    }

    public static ItemSkullskin getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSkullskin((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "skin", "has_skin"
    };

    public static final String[] handledMechs = new String[] {
            "skull_skin"
    };

    private ItemSkullskin(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.skin>
        // @returns ElementTag
        // @mechanism ItemTag.skull_skin
        // @group properties
        // @description
        // Returns the UUID of the player whose skin a skull item uses.
        // Note: Item must be a 'skull_item' with a skin.
        // In format: UUID|Texture|Name.
        // -->
        if (attribute.startsWith("skin")) {
            String skin = getPropertyString();
            if (skin != null) {
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <ItemTag.skin.full>
                // @returns ElementTag|Element
                // @mechanism ItemTag.skull_skin
                // @group properties
                // @description
                // Returns the UUID of the player whose skin a skull item uses, along
                // with the permanently cached texture property.
                // Note: Item must be a 'skull_item' with a skin.
                // -->
                if (attribute.startsWith("full")) {
                    return new ElementTag(skin).getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(CoreUtilities.split(skin, '|').get(0)).getObjectAttribute(attribute);
            }
            else {
                Debug.echoError("This skull item does not have a skin set!");
            }
        }

        // <--[tag]
        // @attribute <ItemTag.has_skin>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.skull_skin
        // @group properties
        // @description
        // Returns whether the item has a custom skin set.
        // (Only for human 'skull_item's)
        // -->
        if (attribute.startsWith("has_skin")) {
            return new ElementTag(getPropertyString() != null)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public boolean isCorrectDurability() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13) || item.getItemStack().getDurability() == 3;
    }

    @Override
    public String getPropertyString() {
        if (isCorrectDurability()) {
            PlayerProfile playerProfile = NMSHandler.getItemHelper().getSkullSkin(item.getItemStack());
            if (playerProfile != null) {
                String name = playerProfile.getName();
                UUID uuid = playerProfile.getUniqueId();
                return (uuid != null ? uuid : name)
                        + (playerProfile.hasTexture() ? "|" + playerProfile.getTexture() +
                        (uuid != null && name != null ? "|" + name : "") : "");
            }
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "skull_skin";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name skull_skin
        // @input ElementTag(|ElementTag(|ElementTag))
        // @description
        // Sets the player skin on a skull_item.
        // The first ElementTag is a UUID.
        // Optionally, use the second ElementTag for the skin texture cache.
        // Optionally, use the third ElementTag for a player name.
        // @tags
        // <ItemTag.skin>
        // <ItemTag.skin.full>
        // <ItemTag.has_skin>
        // -->
        if (mechanism.matches("skull_skin")) {
            if (!isCorrectDurability()) {
                item.getItemStack().setDurability((short) 3);
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            String idString = list.get(0);
            String texture = null;
            if (list.size() > 1) {
                texture = list.get(1);
            }
            PlayerProfile profile;
            if (CoreUtilities.contains(idString, '-')) {
                UUID uuid = UUID.fromString(idString);
                String name = null;
                if (list.size() > 2) {
                    name = list.get(2);
                }
                profile = new PlayerProfile(name, uuid, texture);
            }
            else {
                profile = new PlayerProfile(idString, null, texture);
            }
            profile = NMSHandler.getInstance().fillPlayerProfile(profile);
            if (texture != null) { // Ensure we didn't get overwritten
                profile.setTexture(texture);
            }
            item.setItemStack(NMSHandler.getItemHelper().setSkullSkin(item.getItemStack(), profile));
        }
    }
}
