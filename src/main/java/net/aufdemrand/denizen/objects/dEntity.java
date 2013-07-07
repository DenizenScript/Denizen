package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftAnimals;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dEntity implements dObject {

    /////////////////////
    //   PATTERNS
    /////////////////

    final static Pattern entity_by_id =
            Pattern.compile("(n@|e@|p@)(.+)",
                    Pattern.CASE_INSENSITIVE);

    final static Pattern entity_with_data =
            Pattern.compile("(\\w+),?(\\w+)?,?(\\w+)?",
                    Pattern.CASE_INSENSITIVE);


    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static Map<String, dEntity> uniqueObjects = new HashMap<String, dEntity>();

    public static boolean isSaved(String id) {
        return uniqueObjects.containsKey(id.toUpperCase());
    }

    public static boolean isSaved(dEntity entity) {
        return uniqueObjects.containsValue(entity);
    }

    public static dEntity getSaved(String id) {
        if (uniqueObjects.containsKey(id.toUpperCase()))
            return uniqueObjects.get(id.toUpperCase());
        else return null;
    }

    public static String getSaved(dEntity entity) {
        for (Map.Entry<String, dEntity> i : uniqueObjects.entrySet())
            if (i.getValue() == entity) return i.getKey();
        return null;
    }

    public static void saveAs(dEntity entity, String id) {
        if (entity == null) return;
        uniqueObjects.put(id.toUpperCase(), entity);
    }

    public static void remove(String id) {
        uniqueObjects.remove(id.toUpperCase());
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////

    /**
     * Gets a dEntity Object from a string form. </br>
     * </br>
     * Unique dEntities: </br>
     * n@13 will return the entity object of NPC 13 </br>
     * e@5884 will return the entity object for the entity with the entityid of 5884 </br>
     * e@jimmys_pet will return the saved entity object for the id 'jimmys pet' </br>
     * p@aufdemrand will return the entity object for aufdemrand </br>
     * </br>
     * New dEntities: </br>
     * zombie will return an unspawned Zombie dEntity </br>
     * super_creeper will return an unspawned custom 'Super_Creeper' dEntity </br>
     *
     * @param string  the string or dScript argument String
     * @return  a dEntity, or null
     */
    @ObjectFetcher("e")
    public static dEntity valueOf(String string) {
        if (string == null) return null;

        // Choose a random entity type if "RANDOM" is used
        if (string.equalsIgnoreCase("RANDOM")) {

            EntityType randomType = null;

            // When selecting a random entity type, ignore invalid or inappropriate ones
            while (randomType == null ||
                    randomType.name().matches("^(COMPLEX_PART|DROPPED_ITEM|ENDER_CRYSTAL|ENDER_DRAGON|FISHING_HOOK|ITEM_FRAME|LIGHTNING|PAINTING|PLAYER|UNKNOWN|WEATHER|WITHER|WITHER_SKULL)$") == true) {

                randomType = EntityType.values()[Utilities.getRandom().nextInt(EntityType.values().length)];
            }

            return new dEntity(randomType, "RANDOM");
        }

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.


        Matcher m;
        m = entity_by_id.matcher(string);

        if (m.matches()) {

            String entityGroup = m.group(1).toUpperCase();

            // NPC entity
            if (entityGroup.matches("N@")) {
                NPC returnable = CitizensAPI.getNPCRegistry()
                        .getById(Integer.valueOf(m.group(2)));

                if (returnable != null) return new dEntity(returnable.getBukkitEntity());
                else dB.echoError("Invalid NPC! '" + entityGroup
                        + "' could not be found. Has it been despawned or killed?");
            }

            // Player entity
            else if (entityGroup.matches("P@")) {
                LivingEntity returnable = aH.getPlayerFrom(m.group(2)).getPlayerEntity();

                if (returnable != null) return new dEntity(returnable);
                else dB.echoError("Invalid Player! '" + entityGroup
                        + "' could not be found. Has the player logged off?");
            }

            // Assume entity
            else {
                if (aH.matchesInteger(m.group(2))) {
                    int entityID = Integer.valueOf(m.group(2));
                    Entity entity = null;

                    for (World world : Bukkit.getWorlds()) {
                        entity = ((CraftWorld) world).getHandle().getEntity(entityID).getBukkitEntity();
                        if (entity != null) break;
                    }
                    if (entity != null) return new dEntity(entity);
                }

                else if (isSaved(m.group(2)))
                    return getSaved(m.group(2));
            }
        }

        string = string.replace("e@", "");

        ////////
        // Match Custom Entity

        if (ScriptRegistry.containsScript(string, EntityScriptContainer.class)) {
            // Construct a new custom unspawned entity from script
            return ScriptRegistry.getScriptContainerAs(m.group(0), EntityScriptContainer.class).getEntityFrom();
        }

        ////////
        // Match Entity_Type

        m = entity_with_data.matcher(string);

        if (m.matches()) {

            String data1 = null;
            String data2 = null;

            if (m.group(2) != null) {

                data1 = m.group(2).toUpperCase();
            }

            if (m.group(3) != null) {

                data2 = m.group(3).toUpperCase();
            }

            for (EntityType type : EntityType.values()) {
                if (type.name().equalsIgnoreCase(m.group(1)))
                    // Construct a new 'vanilla' unspawned dEntity                	
                    return new dEntity(type, data1, data2);
            }
        }

        dB.log("valueOf dEntity returning null: " + string);

        return null;
    }


    public static boolean matches(String arg) {

        Matcher m;
        m = entity_by_id.matcher(arg);
        if (m.matches()) return true;

        arg = arg.replace("e@", "");

        if (arg.equalsIgnoreCase("RANDOM"))
            return true;

        if (ScriptRegistry.containsScript(arg, EntityScriptContainer.class))
            return true;

        m = entity_with_data.matcher(arg);

        if (m.matches()) {

            for (EntityType type : EntityType.values())
                if (type.name().equalsIgnoreCase(m.group(1))) return true;
        }

        return false;
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    public dEntity(Entity entity) {
        if (entity != null) {
            this.entity = entity;
            this.entity_type = entity.getType();
        } else dB.echoError("Entity referenced is null!");
    }

    public dEntity(EntityType entityType) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
        } else dB.echoError("Entity_type referenced is null!");
    }

    public dEntity(EntityType entityType, String data1) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
            this.data1 = data1;
        } else dB.echoError("Entity_type referenced is null!");
    }

    public dEntity(EntityType entityType, String data1, String data2) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
            this.data1 = data1;
            this.data2 = data2;
        } else dB.echoError("Entity_type referenced is null!");
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////


    private Entity entity = null;
    private EntityType entity_type = null;
    private String data1 = null;
    private String data2 = null;
    private DespawnedEntity despawned_entity = null;

    public Entity getBukkitEntity() {
        return entity;
    }

    public LivingEntity getLivingEntity() {
        if (entity instanceof LivingEntity)
            return (LivingEntity) entity;
        else return null;
    }

    public boolean isLivingEntity() {
        return (entity instanceof LivingEntity);
    }

    public void spawnAt(Location location) {
        // If the entity is already spawned, teleport it.
        if (entity != null && isUnique()) entity.teleport(location);

        else {
            if (entity_type != null) {
                if (despawned_entity != null) {
                    // If entity had a custom_script, use the script to rebuild the base entity.
                    if (despawned_entity.custom_script != null)
                    { } // TODO: Build entity from custom script
                    // Else, use the entity_type specified/remembered
                    else entity = location.getWorld().spawnEntity(location, entity_type);

                    getLivingEntity().teleport(location);
                    getLivingEntity().getEquipment().setArmorContents(despawned_entity.equipment);
                    getLivingEntity().setHealth(despawned_entity.health);

                    despawned_entity = null;
                }

                else {

                    org.bukkit.entity.Entity ent = null;

                    if (entity_type.name().matches("FALLING_BLOCK")) {

                        Material material = null;

                        if (data1 != null && dMaterial.matches(data1)) {

                            material = dMaterial.valueOf(data1).getMaterial();

                            // If we did not get a block with "RANDOM", or we got
                            // air or portals, keep trying
                            while (data1.equals("RANDOM") &&
                                    (material.isBlock() == false ||
                                            material == Material.AIR ||
                                            material == Material.PORTAL ||
                                            material == Material.ENDER_PORTAL)) {

                                material = dMaterial.valueOf(data1).getMaterial();
                            }
                        }

                        // If material is null or not a block, default to SAND
                        if (material == null || material.isBlock() == false) {

                            material = Material.SAND;
                        }

                        byte materialData = 0;

                        // Get special data value from data2 if it is a valid integer
                        if (data2 != null && aH.matchesInteger(data2)) {

                            materialData = (byte) aH.getIntegerFrom(data2);
                        }

                        // This is currently the only way to spawn a falling block
                        ent = location.getWorld().spawnFallingBlock(location, material, materialData);
                        entity = ent;
                    }

                    else {

                        ent = location.getWorld().spawnEntity(location, entity_type);
                        entity = ent;

                        // If there is some special subtype data associated with this dEntity,
                        // use the setSubtype method to set it in a clean, object-oriented
                        // way that uses reflection
                        //
                        // Otherwise, just use entity-specific methods manually
                        if (data1 != null) {

                            try {

                                // Allow creepers to be powered
                                if (ent instanceof Creeper && data1.equalsIgnoreCase("POWERED")) {
                                    ((Creeper) entity).setPowered(true);
                                }
                                else if (ent instanceof Enderman && dMaterial.matches(data1)) {
                                    ((Enderman) entity).setCarriedMaterial(dMaterial.valueOf(data1).getMaterialData());
                                }
                                else if (ent instanceof Ocelot) {
                                    setSubtype(Ocelot.class, "Type", "setCatType", data1);
                                }
                                else if (ent instanceof Skeleton) {
                                    setSubtype(Skeleton.class, "SkeletonType", "setSkeletonType", data1);
                                }
                                else if (ent instanceof Slime && aH.matchesInteger(data1)) {
                                    ((Slime) entity).setSize(aH.getIntegerFrom(data1));
                                }
                                else if (ent instanceof Villager) {
                                    setSubtype(Villager.class, "Profession", "setProfession", data1);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            else dB.echoError("Cannot spawn a null dEntity!");
        }
    }

    public void despawn() {
        despawned_entity = new DespawnedEntity(this);
        getLivingEntity().remove();
    }

    public void respawn() {
        if (despawned_entity != null)
            spawnAt(despawned_entity.location);
        else if (entity != null)
            dB.echoDebug("Entity " + identify() + " is already spawned!");
        else
            dB.echoError("Cannot respawn a null dEntity!");

    }

    public boolean isSpawned() {
        return entity != null;
    }

    public dEntity rememberAs(String id) {
        dEntity.saveAs(this, id);
        return this;
    }

    /**
     * Set the subtype of this entity by using the chosen method and Enum from
     * this Bukkit entity's class and:
     * 1) using a random subtype if value is "RANDOM"
     * 2) looping through the entity's subtypes until one matches the value string
     *
     * Example: setSubtype(Ocelot.class, "Type", "setCatType", "SIAMESE_CAT");
     *
     * @param entityClass  The Bukkit entity class of the entity.
     * @param typeName  The name of the entity class' Enum with subtypes.
     * @param method  The name of the method used to set the subtype of this entity.
     * @param value  The value of the subtype.
     */

    public void setSubtype (Class<? extends Entity> entityClass, String typeName, String method, String value)
            throws Exception {

        Class<?> typeClass = Class.forName(entityClass.getName() + "$" + typeName);
        Object[] types = typeClass.getEnumConstants();

        if (value.matches("RANDOM")) {

            entityClass.getMethod(method, typeClass).invoke(entity, types[Utilities.getRandom().nextInt(types.length)]);
        }
        else {
            for (Object type : types) {

                if (type.toString().equalsIgnoreCase(value)) {

                    entityClass.getMethod(method, typeClass).invoke(entity, type);
                    break;
                }
            }
        }
    }


    // Used to store some information about a livingEntity while it's despawned
    private class DespawnedEntity {

        Double health = null;
        Location location = null;
        ItemStack[] equipment = null;
        String custom_script = null;

        public DespawnedEntity(dEntity entity) {
            if (entity != null) {
                // Save some important info to rebuild the entity
                health = entity.getLivingEntity().getHealth();
                location = entity.getLivingEntity().getLocation();
                equipment = entity.getLivingEntity().getEquipment().getArmorContents();

                if (CustomNBT.hasCustomNBT(entity.getLivingEntity(), "denizen-script-id"))
                    custom_script = CustomNBT.getCustomNBT(entity.getLivingEntity(), "denizen-script-id");
            }
        }
    }



    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = "Entity";

    @Override
    public String getType() {
        return "Entity";
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

        // Check if entity is a Player or NPC
        if (getBukkitEntity() != null) {
            if (CitizensAPI.getNPCRegistry().isNPC(getBukkitEntity()))
                return "n@" + CitizensAPI.getNPCRegistry().getNPC(getBukkitEntity()).getId();
            else if (getBukkitEntity() instanceof Player)
                return "p@" + ((Player) getBukkitEntity()).getName();
        }

        // Check if entity is a 'saved entity'
        else if (isUnique())
            return "e@" + getSaved(this);

            // Check if entity is spawned, therefore having a bukkit entityId
        else if (isSpawned())
            return "e@" + getBukkitEntity().getEntityId();

            // Check if an entity_type is available
        else if (entity_type != null)
            return "e@" + entity_type.name();

        return "null";
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public boolean isUnique() {
        if (entity instanceof Player) return true;
        if (CitizensAPI.getNPCRegistry().isNPC(entity)) return true;
        return isSaved(this);
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (entity == null) {
            dB.echoDebug("dEntity has returned null.");
            return "null";
        }

        if (attribute.startsWith("get_vehicle")) {
            if (getBukkitEntity().isInsideVehicle())
                return new dEntity(getBukkitEntity().getVehicle())
                        .getAttribute(attribute.fulfill(1));
            else return "null";
        }

        if (attribute.startsWith("custom_name")) {
            if (getLivingEntity().getCustomName() == null) return "null";
            return new Element(getLivingEntity().getCustomName()).getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("name")) {
            if (CitizensAPI.getNPCRegistry().isNPC(entity))
                return new Element(CitizensAPI.getNPCRegistry().getNPC(entity).getName())
                        .getAttribute(attribute.fulfill(1));
            if (entity instanceof Player)
                return new Element(((Player) entity).getName())
                        .getAttribute(attribute.fulfill(1));
            return new Element(entity.getType().getName())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("entity_type"))      {
           // TODO: Fix this.. seems to be a bug? Horse will not return correct entityType
           if (entity instanceof CraftAnimals
                   && !(entity instanceof Pig))
                return new Element("HORSE").getAttribute(attribute.fulfill(1));
            return new Element(entity_type.toString()).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("custom_id")) {
            if (CustomNBT.hasCustomNBT(getLivingEntity(), "denizen-script-id"))
                return new dScript(CustomNBT.getCustomNBT(getLivingEntity(), "denizen-script-id"))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(getBukkitEntity().getType().name())
                        .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new dLocation(getLivingEntity().getTargetBlock(null, range).getLocation())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("location.standing_on"))
            return new dLocation(entity.getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("location"))
            return new dLocation(entity.getLocation())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("health.formatted")) {
            double maxHealth = getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            if ((float) getLivingEntity().getHealth() / maxHealth < .10)
                return new Element("dying").getAttribute(attribute.fulfill(2));
            else if ((float) getLivingEntity().getHealth() / maxHealth < .40)
                return new Element("seriously wounded").getAttribute(attribute.fulfill(2));
            else if ((float) getLivingEntity().getHealth() / maxHealth < .75)
                return new Element("injured").getAttribute(attribute.fulfill(2));
            else if ((float) getLivingEntity().getHealth() / maxHealth < 1)
                return new Element("scraped").getAttribute(attribute.fulfill(2));

            else return new Element("healthy").getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.percentage")) {
            double maxHealth = getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            return new Element(String.valueOf(((float) getLivingEntity().getHealth() / maxHealth) * 100))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.max"))
            return new Element(String.valueOf(getLivingEntity().getMaxHealth()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("health"))
            return new Element(String.valueOf(getLivingEntity().getHealth()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_inside_vehicle"))
            return new Element(String.valueOf(entity.isInsideVehicle()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("killer"))
            return new dPlayer(getLivingEntity().getKiller())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage.cause"))
            return new Element(String.valueOf(entity.getLastDamageCause().getCause().toString()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("last_damage.amount"))
            return new Element(String.valueOf(getLivingEntity().getLastDamage()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("last_damage.duration"))
            return new Duration((long) getLivingEntity().getNoDamageTicks())
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("time_lived"))
            return new Duration(entity.getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("can_pickup_items"))
            return new Element(String.valueOf(getLivingEntity().getCanPickupItems()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("id"))
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
            return new dWorld(entity.getWorld())
                    .getAttribute(attribute.fulfill(1));
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

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
