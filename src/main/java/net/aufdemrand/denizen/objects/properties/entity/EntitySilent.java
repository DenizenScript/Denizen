package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class EntitySilent implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity;
    }

    public static EntitySilent getFrom(dObject entity) {
        if (!describes(entity)) return null;
        else return new EntitySilent((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySilent(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(isSilent(entity.getBukkitEntity()));
    }

    @Override
    public String getPropertyId() {
        return "silent";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.silent>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is silent. (Plays no sounds)
        // -->
        if (attribute.startsWith("silent"))
            return new Element(isSilent(entity.getBukkitEntity()))
                    .getAttribute(attribute.fulfill(1));


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name silent
        // @input Element(Boolean)
        // @description
        // Sets whether this entity is silent. (Plays no sounds)
        // @tags
        // <e@entity.silent>
        // -->
        if (mechanism.matches("silent") && mechanism.requireBoolean()) {
            setSilent(entity.getBukkitEntity(), mechanism.getValue().asBoolean());
        }
    }

    private static boolean isSilent(Entity entity) {
        return ((CraftEntity) entity).getHandle().R();
    }

    private static void setSilent(Entity entity, boolean silent) {
        ((CraftEntity) entity).getHandle().b(silent);
    }
}
