package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.ZombieModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;

public class EntityInfected implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) return false;
        // Check if a Villager or Zombie -- the only two EntityTypes that can be 'infected'
        return ((dEntity) entity).getEntityType() == EntityType.ZOMBIE
                || ((dEntity) entity).getEntityType() == EntityType.VILLAGER;
    }

    public static EntityInfected getFrom(dObject entity) {
        if (!describes(entity)) return null;
        else return new EntityInfected((dEntity)entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityInfected(dEntity item) {
        infected = item;
    }

    dEntity infected;

    public boolean isInfected() {
        if (infected.getBukkitEntity() instanceof Zombie
                && ((Zombie) infected.getBukkitEntity()).isVillager())
            return true;
        else return false;
    }

    public void setInfected(boolean bool) {

        if (bool) {
            if (infected.isNPC()) {
                NPC infected_npc = infected.getNPC();
                infected_npc.setBukkitEntityType(EntityType.ZOMBIE);
                if (!infected_npc.getTrait(ZombieModifier.class).toggleVillager())
                    infected_npc.getTrait(ZombieModifier.class).toggleVillager();
            }

            // If it's a Villager, we need to spawn a Zombie instead.
            // This is kind of messy, and can be improved upon.
            // TODO: Improve upon.
            else if (infected.getBukkitEntity() instanceof Villager) {
                // Make a new entity
                Entity infect = infected.getLocation().getWorld().spawnEntity(infected.getLocation(), EntityType.ZOMBIE);
                ((Zombie) infect).setVillager(true);
                // Set health
                ((Zombie) infect).setHealth(((Villager) infected.getBukkitEntity()).getHealth());
                // Set equipment
                ((Zombie) infect).getEquipment().setArmorContents(((Villager) infected.getBukkitEntity()).getEquipment().getArmorContents());
                // Remove the Villager
                infected.getBukkitEntity().remove();
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
        if (isInfected())
            return "true";
        else return null;
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

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.is_infected>
        // @returns Element(Boolean)
        // @description
        // If the entity is infectable, returns whether the entity is infected.
        // Currently only Zombie or Villager entities can be infected.
        // -->
        if (attribute.startsWith("is_infected"))
            return new Element(isInfected())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

}
