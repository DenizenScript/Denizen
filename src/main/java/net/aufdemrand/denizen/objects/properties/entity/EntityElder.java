package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;

public class EntityElder implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.GUARDIAN;
    }

    public static EntityElder getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityElder((dEntity) entity);
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityElder(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    private boolean getElder() {
        return ((Guardian) (entity.getBukkitEntity())).isElder();
    }

    private void setElder(boolean elder) {
        if (entity == null) return;

        ((Guardian) (entity.getBukkitEntity())).setElder(elder);
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!getElder())
            return null;
        else
            return "true";
    }

    @Override
    public String getPropertyId() {
        return "elder";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.elder>
        // @returns Element(Boolean)
        // @mechanism dEntity.elder
        // @group properties
        // @description
        // If the entity is a guardian, returns whether it is elder.
        // -->
        if (attribute.startsWith("elder"))
            return new Element(getElder())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name elder
        // @input Element(Boolean)
        // @description
        // Changes the elder state of a Guardian.
        // @tags
        // <e@entity.elder>
        // -->

        if (mechanism.matches("elder") && mechanism.requireBoolean()) {
            setElder(mechanism.getValue().asBoolean());
        }
    }
}
