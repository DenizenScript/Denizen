package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;

public class EntityProfession implements Property {


    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) {
            return false;
        }
        return ((dEntity) entity).getBukkitEntityType() == EntityType.VILLAGER
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_9_R2)
                && ((dEntity) entity).getBukkitEntityType() == EntityType.ZOMBIE)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                && ((dEntity) entity).getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER);
    }

    public static EntityProfession getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityProfession((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
            "profession"
    };

    public static final String[] handledMechs = new String[] {
            "profession"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityProfession(dEntity entity) {
        professional = entity;
    }

    dEntity professional;

    private Villager.Profession getProfession() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                && professional.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER) {
            return ((ZombieVillager) professional.getBukkitEntity()).getVillagerProfession();
        }
        else if (professional.getBukkitEntityType() == EntityType.ZOMBIE) {
            return ((Zombie) professional.getBukkitEntity()).getVillagerProfession();
        }
        return ((Villager) professional.getBukkitEntity()).getProfession();
    }

    public void setProfession(Villager.Profession profession) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                && professional.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER) {
            ((ZombieVillager) professional.getBukkitEntity()).setVillagerProfession(profession);
        }
        else if (professional.getBukkitEntityType() == EntityType.ZOMBIE) {
            ((Zombie) professional.getBukkitEntity()).setVillagerProfession(profession);
        }
        else {
            ((Villager) professional.getBukkitEntity()).setProfession(profession);
        }
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (professional.getBukkitEntityType() == EntityType.ZOMBIE
                && !((Zombie) professional.getBukkitEntity()).isVillager()) {
            return null;
        }
        return CoreUtilities.toLowerCase(getProfession().name());
    }

    @Override
    public String getPropertyId() {
        return "profession";
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
        // @attribute <e@entity.profession>
        // @returns Element
        // @mechanism dEntity.profession
        // @group properties
        // @description
        // If the entity can have professions, returns the entity's profession.
        // Currently, only Villager-type and infected zombie entities can have professions.
        // Possible professions: BLACKSMITH, BUTCHER, FARMER, LIBRARIAN, PRIEST. (Or HUSK for zombies!)
        // -->
        if (attribute.startsWith("profession")) {
            return new Element(CoreUtilities.toLowerCase(getProfession().name()))
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name profession
        // @input Element
        // @description
        // Changes the entity's profession.
        // Currently, only Villager-type entities can have professions.
        // Acceptable professions: BLACKSMITH, BUTCHER, FARMER, LIBRARIAN, PRIEST. (Or HUSK for zombies!)
        // @tags
        // <e@entity.profession>
        // -->

        if (mechanism.matches("profession") && mechanism.requireEnum(false, Villager.Profession.values())) {
            setProfession(Villager.Profession.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
