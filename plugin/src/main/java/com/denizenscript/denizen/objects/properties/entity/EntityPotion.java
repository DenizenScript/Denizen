package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;

public class EntityPotion implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        // Check if the entity is a SPLASH_POTION, the EntityType alias for ThrownPotion
        return ((EntityTag) entity).getBukkitEntityType() == EntityType.SPLASH_POTION;
    }

    public static EntityPotion getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPotion((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "potion"
    };

    public static final String[] handledMechs = new String[] {
            "potion"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    EntityTag potion;

    private EntityPotion(EntityTag entity) {
        potion = entity;
    }

    private ThrownPotion getPotion() {
        if (potion == null) {
            return null;
        }
        return (ThrownPotion) potion.getBukkitEntity();
    }

    public void setPotion(ItemStack item) {
        if (potion != null) {
            ((ThrownPotion) potion.getBukkitEntity()).setItem(item);
        }
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (potion == null) {
            return null;
        }
        return new ItemTag(getPotion().getItem()).identify();
    }

    @Override
    public String getPropertyId() {
        return "potion";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.potion>
        // @returns ItemTag
        // @mechanism EntityTag.potion
        // @group properties
        // @description
        // Returns the ItemTag of the splash potion.
        // -->
        if (attribute.startsWith("potion")) {
            return new ItemTag(getPotion().getItem()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name potion
        // @input ItemTag
        // @description
        // Sets the splash potion's ItemStack (must be a potion), thereby changing the effects.
        // @tags
        // <EntityTag.potion>
        // -->
        if (mechanism.matches("potion") && mechanism.requireObject(ItemTag.class)) {
            setPotion(mechanism.valueAsType(ItemTag.class).getItemStack());
        }

    }
}
