package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_5_R2.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dEntity implements dScriptArgument {

    /////////////////////
    //   STATIC METHODS
    /////////////////

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

    public static void saveEntityAs(dEntity entity, String id) {
        if (entity == null) return;
        entities.put(id.toUpperCase(), entity);
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

    public static String isSavedEntity(dEntity entity) {
        for (Map.Entry<String, dEntity> i : entities.entrySet())
            if (i.getValue() == entity) return i.getKey();
        return null;
    }

    /**
     * Gets a dEntity Object from a string form.</br>
     * </br>
     * n@13 will return the entity object of NPC 13</br>
     * e@5884 will return the entity with the entityid of 5884</br>
     * p@aufdemrand will return the entity object of aufdemrand</br>
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
        if (string == null) return null;

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.
        final Pattern entity_by_id =
                Pattern.compile("((n@|e@|p@)(.+))",
                        Pattern.CASE_INSENSITIVE);

        Matcher m;

        m = entity_by_id.matcher(string);

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
                LivingEntity returnable = aH.getPlayerFrom(m.group(3));

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

                else if (isSavedEntity(m.group(3)))
                    return getSavedEntity(m.group(3));

                // Got this far? Invalid entity.
                dB.echoError("Invalid entity! '" + entityGroup + "' could not be found. Has it been despawned or killed?");
            }
        }

        ////////
        // Match EntityType

        string = string.replace("e@", "");

        for (EntityType type : EntityType.values()) {
            if (type.name().equalsIgnoreCase(string))
                return new dEntity(type);
        }

        if (ScriptRegistry.containsScript(m.group(1), ItemScriptContainer.class)) {
            // Get item from script
            return ScriptRegistry.getScriptContainerAs(m.group(1), EntityScriptContainer.class).getEntityFrom();
        }

        return null;
    }


    /////////////////////
    //   INSTANCE METHODS
    /////////////////

    private LivingEntity entity;
    private EntityType entity_type;

    public dEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public dEntity(EntityType entityType) {
        this.entity = null;
        this.entity_type = entityType;
    }

    public void spawnAt(Location location) {
        entity = (LivingEntity) location.getWorld().spawnEntity(location, entity_type);
    }

    public LivingEntity getBukkitEntity() {
        return entity;
    }

    public boolean isAlive() {
        return entity != null;
    }

    public dEntity rememberAs(String id) {
        dEntity.saveEntityAs(this, id);
        return this;
    }



    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = "Entity";

    public String getType() {
        return "entity";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dEntity setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
    }

    @Override
    public String identify() {
        if (getBukkitEntity() != null) {
            if (CitizensAPI.getNPCRegistry().isNPC(getBukkitEntity()))
                return "n@" + CitizensAPI.getNPCRegistry().getNPC(getBukkitEntity()).getId();
            else if (getBukkitEntity() instanceof Player)
                return "p@" + ((Player) getBukkitEntity()).getName();
        }

        else if (isUnique())
            return "e@" + isSavedEntity(this);

        else if (isAlive())
            return "e@" + getBukkitEntity().getEntityId();

        else if (entity_type != null)
            return "e@" + entity_type.name();

        return "null";
    }

    @Override
    public boolean isUnique() {
        if (isSavedEntity(this) != null) return true;
        else return false;
    }

    @Override
    public String toString() {
        return "e@" + entity.getEntityId();
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

        if (attribute.startsWith("type")) {
            if (NBTItem.hasCustomNBT(getBukkitEntity(), "denizen-script-id"))
                return new Script(NBTItem.getCustomNBT(getBukkitEntity(), "denizen-script-id"))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(getBukkitEntity().getType().name())
                        .getAttribute(attribute.fulfill(1));
        }

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

        if (attribute.startsWith("type")) {
            return new Element(getType())
                    .getAttribute(attribute.fulfill(1));
        }

        return identify();
    }

}
