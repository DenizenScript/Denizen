package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ItemColor implements Property {

    public static boolean describes(dObject item) {
        // Leather armor and potions
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == Material.LEATHER_BOOTS
                || ((dItem) item).getItemStack().getType() == Material.LEATHER_CHESTPLATE
                || ((dItem) item).getItemStack().getType() == Material.LEATHER_HELMET
                || ((dItem) item).getItemStack().getType() == Material.LEATHER_LEGGINGS
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                    && (((dItem) item).getItemStack().getType() == Material.POTION
                    || ((dItem) item).getItemStack().getType() == Material.SPLASH_POTION
                    || ((dItem) item).getItemStack().getType() == Material.LINGERING_POTION)));
    }

    public static ItemColor getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemColor((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "color", "dye_color"
    };

    public static final String[] handledMechs = new String[] {
            "color", "dye_color", "dye"
    };


    private ItemColor(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.color>
        // @returns dColor
        // @mechanism dItem.color
        // @group properties
        // @description
        // Returns the color of the leather armor item or potion item.
        // -->
        if (attribute.startsWith("color") || attribute.startsWith("dye_color")) {
            Material mat = item.getItemStack().getType();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                    && (mat == Material.POTION
                    || mat == Material.LINGERING_POTION
                    || mat == Material.SPLASH_POTION)) {
                PotionMeta pm = (PotionMeta) item.getItemStack().getItemMeta();
                if (!pm.hasColor()) {
                    return new dColor(Color.WHITE).getAttribute(attribute.fulfill((1)));
                }
                return new dColor(pm.getColor()).getAttribute(attribute.fulfill((1)));
            }
            return new dColor(((LeatherArmorMeta) item.getItemStack().getItemMeta()).getColor()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        Material mat = item.getItemStack().getType();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                && (mat == Material.POTION
                || mat == Material.LINGERING_POTION
                || mat == Material.SPLASH_POTION)) {
            PotionMeta pm = (PotionMeta) item.getItemStack().getItemMeta();
            if (!pm.hasColor()) {
                return null;
            }
            return new dColor(pm.getColor()).identify();
        }
        return new dColor(((LeatherArmorMeta) item.getItemStack().getItemMeta()).getColor()).identify();
    }

    @Override
    public String getPropertyId() {
        return "color";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name color
        // @input dColor
        // @description
        // Sets the leather armor item's dye color or the potion item's color in the format RED,GREEN,BLUE.
        // @tags
        // <i@item.color>
        // <i@item.dye_color>
        // -->
        if ((mechanism.matches("dye") || mechanism.matches("dye_color")
                || mechanism.matches("color")) && (mechanism.requireObject(dColor.class))) {
            dColor color = mechanism.valueAsType(dColor.class);
            Material mat = item.getItemStack().getType();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                    && (mat == Material.POTION
                    || mat == Material.LINGERING_POTION
                    || mat == Material.SPLASH_POTION)) {
                PotionMeta meta = (PotionMeta) item.getItemStack().getItemMeta();
                meta.setColor(color.getColor());
                item.getItemStack().setItemMeta(meta);
                return;
            }
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemStack().getItemMeta();
            meta.setColor(color.getColor());
            item.getItemStack().setItemMeta(meta);
        }

    }
}
