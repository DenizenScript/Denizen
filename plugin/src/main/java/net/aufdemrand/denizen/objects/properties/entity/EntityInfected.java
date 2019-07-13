package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
            // Set the dEntity to the new entity
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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("is_infected")) {
            dB.echoError("Different infection types are represented by different entity types. Please remove usage of the 'is_infected' tag.");
            return new Element(isInfected())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("is_infected") && mechanism.requireBoolean()) {
            dB.echoError("Different infection types are represented by different entity types. Please remove usage of the 'is_infected' mechanism.");
            setInfected(mechanism.getValue().asBoolean());
        }
    }
}
