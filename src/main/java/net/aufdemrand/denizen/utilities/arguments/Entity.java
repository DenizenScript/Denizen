package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity implements dScriptArgument {

    public static Map<String, Entity> entities = new HashMap<String, Entity>();

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static Entity getSavedEntity(String id) {
        if (entities.containsKey(id.toUpperCase()))
                return entities.get(id.toUpperCase());
        else return null;
    }

    public static void saveEntity(Entity entity) {
        if (entity.id == null) return;
        entities.put(entity.id.toUpperCase(), entity);
    }

    /**
     * Checks if there is a saved item with this Id.
     *
     * @param id  the Id to check
     * @return  true if it exists, false if not
     */
    public static boolean isSavedEntity(String id) {
        return entities.containsKey(id.toUpperCase());
    }

    /**
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallEntities() {
        List<Map<?, ?>> entitylist = DenizenAPI.getCurrentInstance().getSaves().getMapList("dScript.Entities");
        entities.clear();
        // TODO: Figure out de-serialization of this.
    }

    /**
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveEntities() {
        // TODO: Figure out serialization
    }

    /**
     * Gets a Item Object from a string form.
     *
     * @param string  the string or dScript argument String
     * @return  an Item, or null if incorrectly formatted
     *
     */
    public static Entity valueOf(String string) {

        // Create entity!

        return null;
    }

    private String id = null;
    private String prefix = "Entity";

    private LivingEntity entity;

    public Entity(LivingEntity entity) {
        this.entity = entity;
    }

    public Entity(String id, LivingEntity entity) {
        this.entity = entity;
        this.id = id;
        saveEntity(this);
    }

    public void identifyAs(String id) {
        if (this.id == null) {
            this.id = id;
            saveEntity(this);
        }
    }

    public LivingEntity getBukkitEntity() {
        return entity;
    }

    public boolean isAlive() {
        return entity != null;
    }

    public Entity setId(String id) {
        this.id = id.toUpperCase();
        return this;
    }

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return null;
    }

    @Override
    public String as_dScriptArg() {
        return null;
    }

    public String dScriptArgValue() {
        return getDefaultPrefix().toLowerCase() + ":" + as_dScriptArg();
    }

    @Override
    public String toString() {
        return entity.getUniqueId().toString();
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public LivingEntity getEntity(String string) {
       return entity;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // Desensitize the attribute for comparison
        String id = this.id.toLowerCase();

        if (attribute.startsWith("name"))
            return new Element(entity.getCustomName()).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new Location(entity.getTargetBlock(null, range).getLocation())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("location.standing_on"))
            return new Location(entity.getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("location"))
            return new Location(entity.getLocation())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("health.formatted")) {
            int maxHealth = entity.getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            if ((float) entity.getHealth() / maxHealth < .10)
                return new Element("dying").getAttribute(attribute.fulfill(2));
            else if ((float) entity.getHealth() / maxHealth < .40)
                return new Element("seriously wounded").getAttribute(attribute.fulfill(2));
            else if ((float) entity.getHealth() / maxHealth < .75)
                return new Element("injured").getAttribute(attribute.fulfill(2));
            else if ((float) entity.getHealth() / maxHealth < 1)
                return new Element("scraped").getAttribute(attribute.fulfill(2));

            else return new Element("healthy").getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.percentage")) {
            int maxHealth = entity.getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            return new Element(String.valueOf(((float) entity.getHealth() / maxHealth) * 100))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health"))
            return new Element(String.valueOf(entity.getHealth()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_inside_vehicle"))
            return new Element(String.valueOf(entity.isInsideVehicle()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("killer"))
            return new Player(entity.getKiller())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage_cause"))
            return new Element(String.valueOf(entity.getLastDamageCause().getCause().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage"))
            return new Element(String.valueOf(entity.getLastDamage()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("time_lived"))
            return new Duration(entity.getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("can_pickup_items"))
            return new Element(String.valueOf(entity.getCanPickupItems()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("entity_id"))
            return new Element(String.valueOf(entity.getEntityId()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("fall_distance"))
            return new Element(String.valueOf(entity.getFallDistance()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("uuid"))
            return new Element(String.valueOf(entity.getUniqueId().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("has_effect")) {
            // Add later
        }

        if (attribute.startsWith("equipment")) {
            // Add later
        }

        if (attribute.startsWith("world")) {
            // Add world dScriptArg
        }

        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        return dScriptArgValue();
    }

}
