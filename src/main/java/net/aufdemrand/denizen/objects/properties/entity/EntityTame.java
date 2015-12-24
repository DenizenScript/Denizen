package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Tameable;

public class EntityTame implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntity() instanceof Tameable;
    }

    public static EntityTame getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityTame((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityTame(dEntity tame) {
        entity = tame;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (((Tameable) entity.getBukkitEntity()).isTamed()) {
            OfflinePlayer owner = (OfflinePlayer) ((Tameable) entity.getBukkitEntity()).getOwner();
            if (owner == null)
                return "true";
            else
                return "true|" + owner.getUniqueId();
        }
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "tame";
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
        // @attribute <e@entity.is_tamed>
        // @returns Element(Boolean)
        // @mechanism dEntity.tame
        // @group properties
        // @description
        // Returns whether the entity has been tamed.
        // -->
        if (attribute.startsWith("is_tamed")) {
            return new Element(((Tameable) entity.getBukkitEntity()).isTamed())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.get_owner>
        // @returns dPlayer
        // @mechanism dEntity.owner
        // @group properties
        // @description
        // Returns the owner of a tamed entity.
        // -->
        if (attribute.startsWith("get_owner")) {
            if (((Tameable) entity.getBukkitEntity()).isTamed())
                return new dPlayer((OfflinePlayer) ((Tameable) entity.getBukkitEntity()).getOwner())
                        .getAttribute(attribute.fulfill(1));
            else
                return null;
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name tame
        // @input Element(Boolean)(|dPlayer)
        // @description
        // Sets whether the entity has been tamed.
        // Also available: <@link mechanism dEntity.owner>
        // @tags
        // <e@entity.is_tamed>
        // <e@entity.is_tameable>
        // -->

        if (mechanism.matches("tame")) {
            dList list = mechanism.getValue().asType(dList.class);
            if (list.size() == 0) {
                dB.echoError("Missing value for 'tame' mechanism!");
                return;
            }
            if (new Element(list.get(0)).isBoolean())
                ((Tameable) entity.getBukkitEntity()).setTamed(mechanism.getValue().asBoolean());
            else
                dB.echoError("Invalid boolean value!");
            if (list.size() > 1 && new Element(list.get(1)).matchesType(dPlayer.class))
                ((Tameable) entity.getBukkitEntity()).setOwner(new Element(list.get(1)).asType(dPlayer.class).getOfflinePlayer());
        }

        // <--[mechanism]
        // @object dEntity
        // @name owner
        // @input dPlayer
        // @description
        // Sets the entity's owner. Use with no input to make it not have an owner.
        // Also available: <@link mechanism dEntity.tame>
        // @tags
        // <e@entity.is_tamed>
        // <e@entity.is_tameable>
        // <e@entity.get_owner>
        // -->

        if (mechanism.matches("owner")) {
            if (mechanism.hasValue() && mechanism.requireObject(dPlayer.class))
                ((Tameable) entity.getBukkitEntity()).setOwner(mechanism.getValue().asType(dPlayer.class).getOfflinePlayer());
            else
                ((Tameable) entity.getBukkitEntity()).setOwner(null);
        }

    }
}

