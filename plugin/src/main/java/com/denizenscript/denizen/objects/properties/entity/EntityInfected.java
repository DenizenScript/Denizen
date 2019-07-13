package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;

public class EntityInfected implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        // Check if a Villager or Zombie -- the only two EntityTypes that can be 'infected'
        return ((EntityTag) entity).getBukkitEntityType() == EntityType.ZOMBIE
                || ((EntityTag) entity).getBukkitEntityType() == EntityType.VILLAGER;
    }

    public static EntityInfected getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityInfected((EntityTag) entity);
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

    private EntityInfected(EntityTag item) {
        infected = item;
    }

    EntityTag infected;

    public boolean isInfected() {
        return infected.getBukkitEntity() instanceof Zombie
                && infected.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER;
    }

    public void setInfected(boolean bool) {

        if (bool) {
            if (infected.isCitizensNPC()) {
                NPC infected_npc = infected.getDenizenNPC().getCitizen();
                infected_npc.setBukkitEntityType(EntityType.ZOMBIE_VILLAGER);
            }
            LivingEntity entity = infected.getLivingEntity();
            // Make a new entity
            ZombieVillager infect = (ZombieVillager) entity.getLocation().getWorld().spawnEntity(infected.getLocation(), EntityType.ZOMBIE_VILLAGER);
            // Set health
            infect.setHealth(entity.getHealth());
            // Set equipment
            infect.getEquipment().setArmorContents(entity.getEquipment().getArmorContents());
            // Remove the Villager
            entity.remove();
            // Set the EntityTag to the new entity
            infected.setEntity(infect);
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
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("is_infected")) {
            Debug.echoError("Different infection types are represented by different entity types. Please remove usage of the 'is_infected' tag.");
            return new ElementTag(isInfected())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("is_infected") && mechanism.requireBoolean()) {
            Debug.echoError("Different infection types are represented by different entity types. Please remove usage of the 'is_infected' mechanism.");
            setInfected(mechanism.getValue().asBoolean());
        }
    }
}
