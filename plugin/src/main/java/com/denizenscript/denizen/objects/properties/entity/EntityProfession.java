package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

public class EntityProfession implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) entity).getBukkitEntityType() == EntityType.VILLAGER
                || ((EntityTag) entity).getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER;
    }

    public static EntityProfession getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityProfession((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "profession"
    };

    public static final String[] handledMechs = new String[] {
            "profession"
    };

    private EntityProfession(EntityTag entity) {
        professional = entity;
    }

    EntityTag professional;

    private Villager.Profession getProfession() {
        if (professional.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER) {
            return ((ZombieVillager) professional.getBukkitEntity()).getVillagerProfession();
        }
        return ((Villager) professional.getBukkitEntity()).getProfession();
    }

    public void setProfession(Villager.Profession profession) {
        if (professional.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER) {
            ((ZombieVillager) professional.getBukkitEntity()).setVillagerProfession(profession);
        }
        else {
            ((Villager) professional.getBukkitEntity()).setProfession(profession);
        }
    }

    @Override
    public String getPropertyString() {
        return CoreUtilities.toLowerCase(getProfession().name());
    }

    @Override
    public String getPropertyId() {
        return "profession";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.profession>
        // @returns ElementTag
        // @mechanism EntityTag.profession
        // @group properties
        // @description
        // If the entity can have professions, returns the entity's profession.
        // Currently, only Villager-type and infected zombie entities can have professions.
        // For the list of possible professions, refer to <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Profession.html>
        // -->
        if (attribute.startsWith("profession")) {
            return new ElementTag(getProfession())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name profession
        // @input ElementTag
        // @description
        // Changes the entity's profession.
        // Currently, only Villager-type entities can have professions.
        // For the list of possible professions, refer to <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Profession.html>
        // @tags
        // <EntityTag.profession>
        // -->
        if (mechanism.matches("profession") && mechanism.requireEnum(Villager.Profession.class)) {
            setProfession(Villager.Profession.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
