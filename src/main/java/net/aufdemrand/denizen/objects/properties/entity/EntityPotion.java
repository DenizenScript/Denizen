package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class EntityPotion implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if the entity is a SPLASH_POTION, the EntityType alias for ThrownPotion
        return ((dEntity) entity).getEntityType() == EntityType.SPLASH_POTION;
    }

    public static EntityPotion getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityPotion((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    dEntity potion;

    private EntityPotion(dEntity entity) {
        potion = entity;
    }

    private ThrownPotion getPotion() {
        if (potion == null) return null;
        return (ThrownPotion) potion.getBukkitEntity();
    }

    public void setPotion(ItemStack item) {
        if (potion != null)
            ((ThrownPotion) potion.getBukkitEntity()).setItem(item);
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (potion == null) return null;
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
        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.potion.effects>
        // @returns dList
        // @description
        // Returns a list of effects on the splash potion.
        // -->
        if (attribute.startsWith("potion.effects")) {
            dList effects = new dList();
            for (PotionEffect effect : getPotion().getEffects())
                effects.add(effect.getType().getName());
            return effects.getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.potion.amplifiers>
        // @returns dList
        // @description
        // Returns a list of effects on the splash potion, showing only the amplifier.
        // -->
        if (attribute.startsWith("potion.amplifiers")) {
            dList amplifiers = new dList();
            for (PotionEffect effect : getPotion().getEffects())
                amplifiers.add(String.valueOf(effect.getAmplifier()));
            return amplifiers.getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.potion.amplifier[<name>]>
        // @returns Element(Number)
        // @description
        // Returns the amplifier of a specified effect on the splash potion.
        // -->
        if (attribute.startsWith("potion.amplifier")
                && attribute.hasContext(2)) {
            for (PotionEffect effect : getPotion().getEffects()) {
                if (effect.getType().getName().equalsIgnoreCase(attribute.getContext(2)))
                    return new Element(effect.getAmplifier())
                            .getAttribute(attribute.fulfill(2));
            }
            return new Element(0).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.potion.durations>
        // @returns dList
        // @description
        // Returns a list of effects on the splash potion, showing only the duration.
        // -->
        if (attribute.startsWith("potion.durations")) {
            dList amplifiers = new dList();
            for (PotionEffect effect : getPotion().getEffects())
                amplifiers.add(String.valueOf(effect.getAmplifier()));
            return amplifiers.getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.potion.duration[<name>]>
        // @returns Element(Number)
        // @description
        // Returns the duration of a specified effect on the splash potion.
        // -->
        if (attribute.startsWith("potion.duration")
                && attribute.hasContext(2)) {
            for (PotionEffect effect : getPotion().getEffects()) {
                if (effect.getType().getName().equalsIgnoreCase(attribute.getContext(2)))
                    return Duration.valueOf(effect.getDuration() + "t")
                            .getAttribute(attribute.fulfill(2));
            }
            return new Element(0).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.potion.item>
        // @returns dItem
        // @description
        // Returns the dItem of the splash potion.
        // -->
        if (attribute.startsWith("potion.item")) {
            return new dItem(getPotion().getItem()).getAttribute(attribute.fulfill(2));
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
        // <e@entity.potion.item>
        // -->
        if (mechanism.matches("potion") && mechanism.requireObject(dItem.class)) {
            setPotion(mechanism.getValue().asType(dItem.class).getItemStack());
        }

    }

}
