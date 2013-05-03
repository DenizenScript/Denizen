package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_5_R3.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dEntity implements dScriptArgument {

    public static Map<String, dEntity> entities = new HashMap<String, dEntity>();

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static dEntity getSavedEntity(String id) {
        if (entities.containsKey(id.toUpperCase()))
            return entities.get(id.toUpperCase());
        else return null;
    }

    public static void saveEntity(dEntity entity) {
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
     * Gets a dEntity Object from a string form.</br>
     * </br>
     * n@13 will return NPC 13</br>
     * e@5884 will return the entity with the entityid of 5884</br>
     * p@aufdemrand will return the player object of aufdemrand</br>
     * </br>
     * Note that the NPCs, Entities, and Players must be spawned,
     * one coincidentally Players must be logged in.</br>
     *
     *
     * @param string  the string or dScript argument String
     * @return  a dEntity, or null
     *
     */
    @ObjectFetcher("e")
    public static dEntity valueOf(String string) {

        // Make sure string matches what this interpreter can accept.
        final Pattern matchesEntityPtrn =
                Pattern.compile("(?:.+?:|)((n@|e@|p@|)(.+))",
                        Pattern.CASE_INSENSITIVE);

        Matcher m = matchesEntityPtrn.matcher(string);

        if (m.matches()) {
            String entityGroup = m.group(1);
            String entityGroupUpper = entityGroup.toUpperCase();

            if (entityGroupUpper.startsWith("N@")) {
                LivingEntity returnable = CitizensAPI.getNPCRegistry()
                        .getById(Integer.valueOf(m.group(3))).getBukkitEntity();

                if (returnable != null) return new dEntity(returnable);
                else dB.echoError("Invalid NPC! '" + entityGroup + "' could not be found. Has it been despawned or killed?");
            }

            else if (entityGroupUpper.startsWith("P@")) {
                LivingEntity returnable = aH.getPlayerFrom(m.group(4));

                if (returnable != null) new dEntity(returnable);
                else dB.echoError("Invalid Player! '" + entityGroup + "' could not be found. Has the player logged off?");
            }

            // Assume entity
            else {
                if (aH.matchesInteger(m.group(3))) {
                    int entityID = Integer.valueOf(m.group(3));
                    Entity entity = null;

                    for (World world : Bukkit.getWorlds()) {
                        entity = ((CraftWorld) world).getHandle().getEntity(entityID);
                        if (entity != null) break;
                    }
                    if (entity != null) return new dEntity((LivingEntity) entity.getBukkitEntity());
                }
                // Got this far? Invalid entity.
                dB.echoError("Invalid entity! '" + entityGroup + "' could not be found. Has it been despawned or killed?");
            }
        }

        return null;
    }

    private String id = null;
    private String prefix = "Entity";

    private LivingEntity entity;

    public dEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public dEntity(String id, LivingEntity entity) {
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

    public dEntity setId(String id) {
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

        if (entity == null) {
            dB.echoDebug("dEntity has returned null.");
            return "null";
        }

        if (attribute.startsWith("name"))
            return new Element(entity.getCustomName()).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new dLocation(entity.getTargetBlock(null, range).getLocation())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("location.standing_on"))
            return new dLocation(entity.getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("location"))
            return new dLocation(entity.getLocation())
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
            return new dPlayer(entity.getKiller())
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
