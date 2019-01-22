package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.npc.traits.InvisibleTrait;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityVisible implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityVisible getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityVisible((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
            "visible"
    };

    public static final String[] handledMechs = new String[] {
            "visible"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityVisible(dEntity ent) {
        entity = ent;
        stand = (ArmorStand) ent.getBukkitEntity();
    }

    dEntity entity;

    ArmorStand stand;

    /////////
    // Property Methods
    ///////

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


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.visible>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the armor stand is visible.
        // -->
        if (attribute.startsWith("visible")) {
            return new Element(stand.isVisible()).getAttribute(attribute.fulfill(1));
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name visible
        // @input Element(Boolean)
        // @description
        // Sets whether the armor stand is visible.
        // @tags
        // <e@entity.visible>
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
