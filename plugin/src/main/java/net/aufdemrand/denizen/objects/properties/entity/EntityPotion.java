package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;

public class EntityPotion implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) {
            return false;
        }
        // Check if the entity is a SPLASH_POTION, the EntityType alias for ThrownPotion
        return ((dEntity) entity).getBukkitEntityType() == EntityType.SPLASH_POTION;
    }

    public static EntityPotion getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPotion((dEntity) entity);
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

    dEntity potion;

    private EntityPotion(dEntity entity) {
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
        return new dItem(getPotion().getItem()).identify();
    }

    @Override
    public String getPropertyId() {
        return "potion";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.potion>
        // @returns dItem
        // @mechanism dEntity.potion
        // @group properties
        // @description
        // Returns the dItem of the splash potion.
        // -->
        if (attribute.startsWith("potion")) {
            return new dItem(getPotion().getItem()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name potion
        // @input dItem
        // @description
        // Sets the splash potion's ItemStack (must be a potion), thereby changing the effects.
        // @tags
        // <e@entity.potion>
        // -->
        if (mechanism.matches("potion") && mechanism.requireObject(dItem.class)) {
            setPotion(mechanism.valueAsType(dItem.class).getItemStack());
        }

    }
}
