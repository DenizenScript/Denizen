package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemSkullskin implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof SkullMeta;
    }

    public static ItemSkullskin getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSkullskin((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[]{
            "skin", "has_skin"
    };

    public static final String[] handledMechs = new String[] {
            "skull_skin"
    };



    private ItemSkullskin(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.skin>
        // @returns Element
        // @mechanism dItem.skull_skin
        // @group properties
        // @description
        // Returns the UUID of the player whose skin a skull item uses.
        // Note: Item must be a 'skull_item' with a skin.
        // -->
        // <--[tag]
        // @attribute <i@item.skin.full>
        // @returns Element|Element
        // @mechanism dItem.skull_skin
        // @group properties
        // @description
        // Returns the UUID of the player whose skin a skull item uses, along
        // with the permanently cached texture property.
        // Note: Item must be a 'skull_item' with a skin.
        // -->

        if (attribute.startsWith("skin")) {
            String skin = getPropertyString();
            if (skin != null) {
                attribute = attribute.fulfill(1);
                if (attribute.startsWith("full")) {
                    return new Element(skin).getAttribute(attribute.fulfill(1));
                }
                return new Element(CoreUtilities.split(skin, '|').get(0)).getAttribute(attribute);
            }
            else {
                dB.echoError("This skull item does not have a skin set!");
            }
        }

        // <--[tag]
        // @attribute <i@item.has_skin>
        // @returns Element(Boolean)
        // @mechanism dItem.skull_skin
        // @group properties
        // @description
        // Returns whether the item has a custom skin set.
        // (Only for human 'skull_item's)
        // -->
        if (attribute.startsWith("has_skin")) {
            return new Element(getPropertyString() != null)
                    .getAttribute(attribute.fulfill(1));
        }


        return null;
    }

    public boolean isCorrectDurability() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) || item.getItemStack().getDurability() == 3;
    }

    @Override
    public String getPropertyString() {
        if (isCorrectDurability()) {
            PlayerProfile playerProfile = NMSHandler.getInstance().getItemHelper().getSkullSkin(item.getItemStack());
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
        // @object dItem
        // @name skull_skin
        // @input Element(|Element(|Element))
        // @description
        // Sets the player skin on a skull_item.
        // The first Element is a UUID.
        // Optionally, use the second Element for the skin texture cache.
        // Optionally, use the third Element for a player name.
        // @tags
        // <i@item.skin>
        // <i@item.skin.full>
        // <i@item.has_skin>
        // -->
        if (mechanism.matches("skull_skin")) {
            if (!isCorrectDurability()) {
                item.getItemStack().setDurability((short) 3);
            }
            dList list = mechanism.getValue().asType(dList.class);
            String idString = list.get(0);
            String texture = null;
            if (list.size() > 1) {
                texture = list.get(1);
            }
            PlayerProfile profile;
            if (idString.contains("-")) {
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
            item.setItemStack(NMSHandler.getInstance().getItemHelper().setSkullSkin(item.getItemStack(), profile));
        }
    }
}
