package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
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
        return item instanceof ItemTag
                && ((ItemTag) item).getBukkitMaterial() == Material.CROSSBOW;
    }

    public static ItemChargedProjectile getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemChargedProjectile((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "charged_projectiles", "is_charged"
    };

    public static final String[] handledMechs = new String[] {
            "charged_projectiles", "add_charged_projectile", "remove_charged_projectiles"
    };

    public ItemChargedProjectile(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.charged_projectiles>
        // @returns ListTag(ItemTag)
        // @mechanism ItemTag.charged_projectiles
        // @group properties
        // @description
        // Returns a list of charged projectile items on this crossbow.
        // -->
        if (attribute.startsWith("charged_projectiles")) {
            return getChargedProjectiles()
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.is_charged>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.charged_projectiles
        // @group properties
        // @description
        // Returns whether this crossbow is charged.
        // -->
        if (attribute.startsWith("is_charged")) {
            return new ElementTag(((CrossbowMeta) item.getItemMeta()).hasChargedProjectiles())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getChargedProjectiles() {
        CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
        ListTag list = new ListTag();
        if (!meta.hasChargedProjectiles()) {
            return list;
        }

        for (ItemStack projectile : meta.getChargedProjectiles()) {
            list.addObject(new ItemTag(projectile));
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
        // @object ItemTag
        // @name charged_projectiles
        // @input ListTag(ItemTag)
        // @description
        // Sets the charged projectile items on this crossbow. Charged projectiles may only be arrows and fireworks.
        // @tags
        // <ItemTag.charged_projectiles>
        // <ItemTag.is_charged>
        // -->
        if (mechanism.matches("charged_projectiles")) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
            meta.setChargedProjectiles(null);
            for (ItemTag projectile : mechanism.valueAsType(ListTag.class).filter(ItemTag.class, mechanism.context)) {
                try {
                    meta.addChargedProjectile(projectile.getItemStack());
                }
                catch (IllegalArgumentException e) {
                    mechanism.echoError("Charged crossbow projectiles may only be arrows or fireworks!");
                }
            }
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name add_charged_projectile
        // @input ItemTag
        // @description
        // Adds a new charged projectile item on this crossbow. Charged projectiles may only be arrows and fireworks.
        // @tags
        // <ItemTag.charged_projectiles>
        // <ItemTag.is_charged>
        // -->
        if (mechanism.matches("add_charged_projectile") && mechanism.requireObject(ItemTag.class)) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
            try {
                meta.addChargedProjectile(mechanism.valueAsType(ItemTag.class).getItemStack());
            }
            catch (IllegalArgumentException e) {
                mechanism.echoError("Charged crossbow projectiles may only be arrows or fireworks!");
            }
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name remove_charged_projectiles
        // @input None
        // @description
        // Removes all charged projectiles from this crossbow.
        // @tags
        // <ItemTag.charged_projectiles>
        // <ItemTag.is_charged>
        // -->
        if (mechanism.matches("remove_charged_projectiles")) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
            meta.setChargedProjectiles(null);
            item.setItemMeta(meta);
        }
    }
}
