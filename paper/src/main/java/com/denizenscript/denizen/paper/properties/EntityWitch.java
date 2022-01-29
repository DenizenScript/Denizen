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
import org.bukkit.inventory.ItemStack;

public class EntityWitch implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Witch;
    }

    public static EntityWitch getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityWitch((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "held_potion", "potion_drink_duration"
    };

    private EntityWitch(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.held_potion>
        // @returns ItemTag
        // @mechanism EntityTag.held_potion
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the potion item a witch is holding, or air if none.
        // -->
        PropertyParser.<EntityWitch, ItemTag>registerTag(ItemTag.class, "held_potion", (attribute, object) -> {
            ItemStack potion = object.getWitch().getDrinkingPotion();
            if (potion == null) {
                return new ItemTag(Material.AIR);
            }
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
        PropertyParser.<EntityWitch, DurationTag>registerTag(DurationTag.class, "potion_drink_duration", (attribute, object) -> {
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
        return "held_potion";
    }


    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name held_potion
        // @input ItemTag
        // @Plugin Paper
        // @description
        // Sets the potion item a witch is holding.
        // @tags
        // <EntityTag.held_potion>
        // -->
        if (mechanism.matches("held_potion") && mechanism.requireObject(ItemTag.class)) {
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