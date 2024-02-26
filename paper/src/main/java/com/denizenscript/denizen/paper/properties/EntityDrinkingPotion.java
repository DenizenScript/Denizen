package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.entity.Witch;

public class EntityDrinkingPotion implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Witch;
    }

    public static EntityDrinkingPotion getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityDrinkingPotion((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "drinking_potion", "potion_drink_duration"
    };

    public EntityDrinkingPotion(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.drinking_potion>
        // @returns ItemTag
        // @mechanism EntityTag.drinking_potion
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the potion item a witch is drinking, or air if none.
        // -->
        PropertyParser.registerTag(EntityDrinkingPotion.class, ItemTag.class, "drinking_potion", (attribute, object) -> {
            return new ItemTag(object.getWitch().getDrinkingPotion());
        });

        // <--[tag]
        // @attribute <EntityTag.potion_drink_duration>
        // @returns DurationTag
        // @mechanism EntityTag.potion_drink_duration
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the duration remaining until a witch is done drinking a potion.
        // -->
        PropertyParser.registerTag(EntityDrinkingPotion.class, DurationTag.class, "potion_drink_duration", (attribute, object) -> {
            return new DurationTag((long) object.getWitch().getPotionUseTimeLeft());
        });
    }

    public Witch getWitch() {
        return (Witch) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "drinking_potion";
    }


    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name drinking_potion
        // @input ItemTag
        // @Plugin Paper
        // @group properties
        // @description
        // Sets the potion item a witch is drinking.
        // @tags
        // <EntityTag.drinking_potion>
        // -->
        if (mechanism.matches("drinking_potion") && mechanism.requireObject(ItemTag.class)) {
            ItemTag potion = mechanism.valueAsType(ItemTag.class);
            if (potion.getBukkitMaterial() != Material.POTION) {
                mechanism.echoError("Invalid item input '" + potion + "': item must be a potion");
                return;
            }
            getWitch().setDrinkingPotion(potion.getItemStack());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name potion_drink_duration
        // @input DurationTag
        // @Plugin Paper
        // @group properties
        // @description
        // Sets the duration remaining until a witch is done drinking a potion.
        // @tags
        // <EntityTag.potion_drink_duration>
        // -->
        if (mechanism.matches("potion_drink_duration") && mechanism.requireObject(DurationTag.class)) {
            getWitch().setPotionUseTimeLeft(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
