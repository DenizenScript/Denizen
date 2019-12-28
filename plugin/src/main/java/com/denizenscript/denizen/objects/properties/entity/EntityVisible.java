package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.npc.traits.InvisibleTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityVisible implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityVisible getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityVisible((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "visible"
    };

    public static final String[] handledMechs = new String[] {
            "visible"
    };

    private EntityVisible(EntityTag ent) {
        entity = ent;
        stand = (ArmorStand) ent.getBukkitEntity();
    }

    EntityTag entity;

    ArmorStand stand;

    @Override
    public String getPropertyString() {
        if (!((ArmorStand) entity.getBukkitEntity()).isVisible()) {
            return "false";
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "visible";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.visible>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.visible
        // @group attributes
        // @description
        // Returns whether the armor stand is visible.
        // -->
        if (attribute.startsWith("visible")) {
            return new ElementTag(stand.isVisible()).getObjectAttribute(attribute.fulfill(1));
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name visible
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the armor stand is visible.
        // @tags
        // <EntityTag.visible>
        // -->
        if (mechanism.matches("visible") && mechanism.requireBoolean()) {
            if (Depends.citizens != null) {
                InvisibleTrait.setInvisible(stand, CitizensAPI.getNPCRegistry().getNPC(stand), !mechanism.getValue().asBoolean());
            }
            else {
                stand.setVisible(mechanism.getValue().asBoolean());
            }
        }
    }
}
