package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;

public class EntityInfected implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) {
            return false;
        }
        // Check if a Villager or Zombie -- the only two EntityTypes that can be 'infected'
        return ((dEntity) entity).getBukkitEntityType() == EntityType.ZOMBIE
                || ((dEntity) entity).getBukkitEntityType() == EntityType.VILLAGER;
    }

    public static EntityInfected getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityInfected((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_infected"
    };

    public static final String[] handledMechs = new String[] {
            "is_infected"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityInfected(dEntity item) {
        infected = item;
    }

    dEntity infected;

    public boolean isInfected() {
        return infected.getBukkitEntity() instanceof Zombie
                && (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)
                ? infected.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER
                : ((Zombie) infected.getBukkitEntity()).isVillager());
    }

    public void setInfected(boolean bool) {

        if (bool) {
            if (infected.isCitizensNPC()) {
                NPC infected_npc = infected.getDenizenNPC().getCitizen();
                infected_npc.setBukkitEntityType(EntityType.ZOMBIE_VILLAGER);
            }

            // TODO: Improve upon.
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)) {
                LivingEntity entity = infected.getLivingEntity();
                // Make a new entity
                ZombieVillager infect = (ZombieVillager) entity.getLocation().getWorld().spawnEntity(infected.getLocation(), EntityType.ZOMBIE_VILLAGER);
                // Set health
                infect.setHealth(entity.getHealth());
                // Set equipment
                infect.getEquipment().setArmorContents(entity.getEquipment().getArmorContents());
                // Remove the Villager
                entity.remove();
                // Set the dEntity to the new entity
                infected.setEntity(infect);
            }

            // TODO: Should be bother allowing villager input at all?

            // If it's a Villager, we need to spawn a Zombie instead.
            // This is kind of messy, and can be improved upon.
            // TODO: Improve upon.
            else if (infected.getBukkitEntity() instanceof Villager) {
                Villager villager = (Villager) infected.getBukkitEntity();
                // Make a new entity
                Zombie infect = (Zombie) villager.getLocation().getWorld().spawnEntity(infected.getLocation(), EntityType.ZOMBIE);
                infect.setVillager(true);
                // Set health
                infect.setHealth(villager.getHealth());
                // Set equipment
                infect.getEquipment().setArmorContents(villager.getEquipment().getArmorContents());
                // Remove the Villager
                villager.remove();
                // Set the dEntity to the new entity
                infected.setEntity(infect);
            }

            // Much much easier
            else if (infected.getBukkitEntity() instanceof Zombie) {
                ((Zombie) infected).setVillager(true);
            }
        }


    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (isInfected()) {
            return "true";
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "infected";
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
        // @attribute <e@entity.is_infected>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // If the entity is infectable, returns whether the entity is infected.
        // Currently only Zombie or Villager entities can be infected.
        // -->
        if (attribute.startsWith("is_infected")) {
            return new Element(isInfected())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // <--[mechanism]
        // @object dEntity
        // @name is_infected
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is infected.
        // @tags
        // <e@entity.is_infected>
        // -->
        if (mechanism.matches("is_infected") && mechanism.requireBoolean()) {
            setInfected(mechanism.getValue().asBoolean());
        }
    }
}
