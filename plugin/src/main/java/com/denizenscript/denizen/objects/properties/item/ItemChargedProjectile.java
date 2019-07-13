package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

public class ItemChargedProjectile implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getType() == Material.CROSSBOW;
    }

    public static ItemChargedProjectile getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemChargedProjectile((dItem) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "charged_projectiles", "is_charged"
    };

    public static final String[] handledMechs = new String[] {
            "charged_projectiles", "add_charged_projectile", "remove_charged_projectiles"
    };

    private ItemChargedProjectile(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.charged_projectiles>
        // @returns ListTag(dItem)
        // @mechanism dItem.charged_projectiles
        // @group properties
        // @description
        // Returns a list of charged projectile items on this crossbow.
        // -->
        if (attribute.startsWith("charged_projectiles")) {
            return getChargedProjectiles()
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.is_charged>
        // @returns ElementTag(Boolean)
        // @mechanism dItem.charged_projectiles
        // @group properties
        // @description
        // Returns whether this crossbow is charged.
        // -->
        if (attribute.startsWith("is_charged")) {
            return new ElementTag(((CrossbowMeta) item.getItemStack().getItemMeta()).hasChargedProjectiles())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getChargedProjectiles() {
        CrossbowMeta meta = (CrossbowMeta) item.getItemStack().getItemMeta();
        ListTag list = new ListTag();
        if (!meta.hasChargedProjectiles()) {
            return list;
        }

        for (ItemStack projectile : meta.getChargedProjectiles()) {
            list.addObject(new dItem(projectile));
        }
        return list;
    }

    @Override
    public String getPropertyString() {
        ListTag projectiles = getChargedProjectiles();
        return projectiles.size() > 0 ? projectiles.identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "charged_projectiles";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name charged_projectiles
        // @input ListTag(dItem)
        // @description
        // Sets the charged projectile items on this crossbow. Charged projectiles may only be arrows and fireworks.
        // @tags
        // <i@item.charged_projectiles>
        // <i@item.is_charged>
        // -->
        if (mechanism.matches("charged_projectiles")) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemStack().getItemMeta();
            meta.setChargedProjectiles(null);
            for (dItem projectile : mechanism.valueAsType(ListTag.class).filter(dItem.class, mechanism.context)) {
                try {
                    meta.addChargedProjectile(projectile.getItemStack());
                }
                catch (IllegalArgumentException e) {
                    Debug.echoError("Charged crossbow projectiles may only be arrows or fireworks!");
                }
            }
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object dItem
        // @name add_charged_projectile
        // @input dItem
        // @description
        // Adds a new charged projectile item on this crossbow. Charged projectiles may only be arrows and fireworks.
        // @tags
        // <i@item.charged_projectiles>
        // <i@item.is_charged>
        // -->
        if (mechanism.matches("add_charged_projectile") && mechanism.requireObject(dItem.class)) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemStack().getItemMeta();
            try {
                meta.addChargedProjectile(mechanism.valueAsType(dItem.class).getItemStack());
            }
            catch (IllegalArgumentException e) {
                Debug.echoError("Charged crossbow projectiles may only be arrows or fireworks!");
            }
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object dItem
        // @name remove_charged_projectiles
        // @input None
        // @description
        // Removes all charged projectiles from this crossbow.
        // @tags
        // <i@item.charged_projectiles>
        // <i@item.is_charged>
        // -->
        if (mechanism.matches("remove_charged_projectiles")) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemStack().getItemMeta();
            meta.setChargedProjectiles(null);
            item.getItemStack().setItemMeta(meta);
        }
    }
}
