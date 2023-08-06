package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Goat;

public class EntityHorns implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Goat;
    }

    public static EntityHorns getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHorns((EntityTag) entity);
        }
    }

    public EntityHorns(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getHornsList().identify();
    }

    @Override
    public String getPropertyId() {
        return "horns";
    }

    public ListTag getHornsList() {
        ListTag result = new ListTag();
        if (getGoat().hasLeftHorn()) {
            result.addObject(new ElementTag("left"));
        }
        if (getGoat().hasRightHorn()) {
            result.addObject(new ElementTag("right"));
        }
        return result;
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.horns>
        // @returns ListTag
        // @mechanism EntityTag.horns
        // @group properties
        // @description
        // Returns a ListTag of a goat's horns. can include "left" and "right", for the left and right horns.
        // -->
        PropertyParser.registerTag(EntityHorns.class, ListTag.class, "horns", (attribute, object) -> {
            return object.getHornsList();
        });

        // <--[mechanism]
        // @object EntityTag
        // @name horns
        // @input ListTag
        // @description
        // Sets a goat's horns. can include "left" and "right", for the left and right horns.
        // @tags
        // <EntityTag.horns>
        // -->
        PropertyParser.registerMechanism(EntityHorns.class, ListTag.class, "horns", (object, mechanism, input) -> {
            boolean left = false, right = false;
            for (String value : input) {
                String low = CoreUtilities.toLowerCase(value);
                if (low.equals("left")) {
                    left = true;
                }
                else if (low.equals("right")) {
                    right = true;
                }
                else {
                    mechanism.echoError("Invalid horn '" + value + "': must be 'left' or 'right'.");
                }
            }
            object.getGoat().setLeftHorn(left);
            object.getGoat().setRightHorn(right);
        });
    }

    public Goat getGoat() {
        return (Goat) entity.getBukkitEntity();
    }
}
