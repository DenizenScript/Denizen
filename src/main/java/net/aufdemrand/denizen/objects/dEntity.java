package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_6_R2.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
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
                        net.minecraft.server.v1_6_R2.Entity nmsEntity = ((CraftWorld) world).getHandle().getEntity(entityID);

                        // Make sure the nmsEntity is valid, to prevent
                        // unpleasant errors

                        if (nmsEntity != null) {
                            entity = nmsEntity.getBukkitEntity();
                        }
                        else {
                            return null;
                        }

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

                data1 = m.group(2);
            }

            if (m.group(3) != null) {

                data2 = m.group(3);
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

    public EntityType getEntityType() {
        return entity_type;
    }

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

    /**
     * Get the NPC corresponding to this entity
     *
     * @return  The NPC
     */

    public NPC getNPC() {

        return CitizensAPI.getNPCRegistry().getNPC(getBukkitEntity());
    }

    /**
     * Whether this entity is an NPC
     *
     * @return  true or false
     */

    public boolean isNPC() {
        if (CitizensAPI.getNPCRegistry().isNPC(getBukkitEntity()))
            return true;
        return false;
    }

    /**
     * Whether this entity identifies as a generic
     * entity type instead of a spawned entity
     *
     * @return  true or false
     */

    public boolean isGeneric() {
        if (identify().matches("e@\\D+"))
            return true;
        return false;
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

                    if (entity_type.name().matches("PLAYER")) {
                    	
                    	NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, data1);
                    	npc.spawn(location);
                    }
                    else if (entity_type.name().matches("FALLING_BLOCK")) {

                        Material material = null;

                        if (data1 != null && dMaterial.matches(data1)) {

                            material = dMaterial.valueOf(data1).getMaterial();

                            // If we did not get a block with "RANDOM", or we got
                            // air or portals, keep trying
                            while (data1.equalsIgnoreCase("RANDOM") &&
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

                                // Allow setting of blocks held by endermen
                                else if (ent instanceof Enderman && dMaterial.matches(data1)) {
                                    ((Enderman) entity).setCarriedMaterial(dMaterial.valueOf(data1).getMaterialData());
                                }

                                // Allow setting of horse variants and colors
                                else if (ent instanceof Horse) {
                                    setSubtype("org.bukkit.entity.Horse", "org.bukkit.entity.Horse$Variant", "setVariant", data1);

                                    if (data2 != null) {
                                        setSubtype("org.bukkit.entity.Horse", "org.bukkit.entity.Horse$Color", "setColor", data2);
                                    }
                                }

                                // Allow setting of ocelot types
                                else if (ent instanceof Ocelot) {
                                    setSubtype("org.bukkit.entity.Ocelot", "org.bukkit.entity.Ocelot$Type", "setCatType", data1);
                                }

                                // Allow setting of sheep colors
                                else if (ent instanceof Sheep) {
                                    setSubtype("org.bukkit.entity.Sheep", "org.bukkit.DyeColor", "setColor", data1);
                                }

                                // Allow setting of skeleton types and their weapons
                                else if (ent instanceof Skeleton) {
                                    setSubtype("org.bukkit.entity.Skeleton", "org.bukkit.entity.Skeleton$SkeletonType", "setSkeletonType", data1);

                                    // Give skeletons bows by default, unless data2 specifies
                                    // a different weapon
                                    if (dItem.matches(data2) == false) {
                                        data2 = "bow";
                                    }

                                    ((Skeleton) entity).getEquipment()
                                            .setItemInHand(dItem.valueOf(data2).getItemStack());
                                }
                                // Allow setting of slime sizes
                                else if (ent instanceof Slime && aH.matchesInteger(data1)) {
                                    ((Slime) entity).setSize(aH.getIntegerFrom(data1));
                                }

                                // Allow setting of villager professions
                                else if (ent instanceof Villager) {
                                    setSubtype("org.bukkit.entity.Villager", "org.bukkit.entity.Villager$Profession", "setProfession", data1);
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

    public void remove() {
        entity.remove();
    }

    public dEntity rememberAs(String id) {
        dEntity.saveAs(this, id);
        return this;
    }

    public void teleport(Location location) {
    	if (isNPC())
    		getNPC().teleport(location, TeleportCause.PLUGIN);
    	else
    		this.getBukkitEntity().teleport(location);
    }

    /**
     * Make this entity target another living entity, attempting both
     * old entity AI and new entity AI targeting methods
     *
     * @param target  The LivingEntity target
     */

    public void target(LivingEntity target) {

    	// If the target is not null, cast it to an NMS EntityLiving
    	// as well for one of the two methods below
    	EntityLiving nmsTarget = target != null ? ((CraftLivingEntity) target).getHandle()
    									: null;
    	
    	((CraftCreature) entity).getHandle().
			setGoalTarget(nmsTarget);

        ((CraftCreature) entity).getHandle().
                setGoalTarget(((CraftLivingEntity) target).getHandle());

        ((CraftCreature) entity).setTarget(target);
    }

    /**
     * Set the subtype of this entity by using the chosen method and Enum from
     * this Bukkit entity's class and:
     * 1) using a random subtype if value is "RANDOM"
     * 2) looping through the entity's subtypes until one matches the value string
     *
     * Example: setSubtype("org.bukkit.entity.Ocelot", "org.bukkit.entity.Ocelot$Type", "setCatType", "SIAMESE_CAT");
     *
     * @param entityName  The name of the entity's class.
     * @param typeName  The name of the entity class' Enum with subtypes.
     * @param method  The name of the method used to set the subtype of this entity.
     * @param value  The value of the subtype.
     */

    public void setSubtype (String entityName, String typeName, String method, String value)
            throws Exception {

        Class<?> entityClass = Class.forName(entityName);
        Class<?> typeClass = Class.forName(typeName);
        Object[] types = typeClass.getEnumConstants();

        if (value.equalsIgnoreCase("RANDOM")) {

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

    public int comparesTo(dEntity entity) {
        // If provided is unique, and both are the same unique entity, return 1.
        if (entity.isUnique() && entity.identify().equals(identify())) return 1;

        // If provided isn't unique...
        if (!entity.isUnique()) {
            // Return 1 if this object isn't unique either, but matches
            if (!isUnique() && entity.identify().equals(identify()))
                return 1;
            // Return 1 if the provided object isn't unique, but whose entity_type
            // matches this object, even if this object is unique.
            if (entity_type == entity.entity_type) return 1;
        }

        return 0;
    }

    @Override
    public String identify() {

        // Check if entity is a Player or NPC
        if (getBukkitEntity() != null) {
            if (isNPC())
                return "n@" + getNPC().getId();
            else if (getBukkitEntity() instanceof Player)
                return "p@" + ((Player) getBukkitEntity()).getName();
        }

        // Check if entity is a 'saved entity'
        if (isSaved(this))
            return "e@" + getSaved(this);


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
        if (isNPC()) return true;
        if (isSaved(this)) return true;
        return isSpawned();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (entity == null) {
            dB.echoDebug("dEntity has returned null.");
            return "null";
        }
        
        // <--
        // <entity> -> dEntity
        // Returns the dEntity of the entity.
        // -->
        
        // <--
        // <entity.get_vehicle> -> dEntity
        // If the entity is in a vehicle, returns the vehicle as a
        // dEntity. Else, returns null.
        // -->
        if (attribute.startsWith("get_vehicle")) {
            if (getBukkitEntity().isInsideVehicle())
                return new dEntity(getBukkitEntity().getVehicle())
                        .getAttribute(attribute.fulfill(1));
            else return "null";
        }

        // <--
        // <entity.custom_name> -> Element
        // If the entity has a custom name, returns the name as an
        // Element. Else, returns null.
        // -->
        if (attribute.startsWith("custom_name")) {
            if (getLivingEntity().getCustomName() == null) return "null";
            return new Element(getLivingEntity().getCustomName()).getAttribute(attribute.fulfill(2));
        }

        // <--
        // <entity.name> -> Element
        // Returns the name of the entity.
        // -->
        if (attribute.startsWith("name")) {
            if (isNPC())
                return new Element(getNPC().getName())
                        .getAttribute(attribute.fulfill(1));
            if (entity instanceof Player)
                return new Element(((Player) entity).getName())
                        .getAttribute(attribute.fulfill(1));
            return new Element(entity.getType().getName())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.entity_type> -> Element
        // Returns the type of the entity.
        // -->
        if (attribute.startsWith("entity_type")) {
            return new Element(entity_type.toString()).getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.custom_id> -> dScript/Element
        // If the entity has a script ID, returns the dScript of that
        // ID. Else, returns the name of the entity type.
        // -->
        if (attribute.startsWith("custom_id")) {
            if (CustomNBT.hasCustomNBT(getLivingEntity(), "denizen-script-id"))
                return new dScript(CustomNBT.getCustomNBT(getLivingEntity(), "denizen-script-id"))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(getBukkitEntity().getType().name())
                        .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.location.cursor_on> -> dLocation
        // Returns the dLocation of where the entity is looking.
        // -->
        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new dLocation(getLivingEntity().getTargetBlock(null, range).getLocation())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--
        // <entity.location.standing_on> -> dLocation
        // Returns the dLocation of what the entity is standing on.
        // -->
        if (attribute.startsWith("location.standing_on"))
            return new dLocation(entity.getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <entity.location> -> dLocation
        // Returns the dLocation of the entity.
        // -->
        if (attribute.startsWith("location")) {

            if (entity instanceof Player) {
                // Important for player yaw and direction!
                //
                // A player's true yaw is always 90 less than the one given by
                // Bukkit's location.getYaw(), so correct it here

                dLocation location = new dLocation(entity.getLocation());
                location.setYaw(location.getYaw() - 90);
                return location.getAttribute(attribute.fulfill(1));
            }
            else {
                return new dLocation(entity.getLocation())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--
        // <entity.health.formatted> -> Element
        // Returns a 'formatted' value of the player's current health level.
        // May be 'dying', 'seriously wounded', 'injured', 'scraped', or 'healthy'.
        // -->
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

        // <--
        // <entity.health.percentage> -> Element(Number)
        // Returns the entity's current health as a percentage.
        // -->
        if (attribute.startsWith("health.percentage")) {
            double maxHealth = getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            return new Element(String.valueOf(((float) getLivingEntity().getHealth() / maxHealth) * 100))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--
        // <entity.health.max> -> Element(Number)
        // Returns the maximum health of the entity.
        // -->
        if (attribute.startsWith("health.max"))
            return new Element(String.valueOf(getLivingEntity().getMaxHealth()))
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <entity.health> -> Element(Number)
        // Returns the current health of the entity.
        // -->
        if (attribute.startsWith("health"))
            return new Element(String.valueOf(getLivingEntity().getHealth()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.is_inside_vehicle> -> Element(Boolean)
        // Returns true if the entity is inside a vehicle. Else, returns false.
        // -->
        if (attribute.startsWith("is_inside_vehicle"))
            return new Element(String.valueOf(entity.isInsideVehicle()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.killer> -> dPlayer
        // Returns the player that last killed the entity.
        // -->
        if (attribute.startsWith("killer"))
            return new dPlayer(getLivingEntity().getKiller())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.last_damage.cause> -> Element
        // Returns the cause of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.cause"))
            return new Element(String.valueOf(entity.getLastDamageCause().getCause().toString()))
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <entity.last_damage.amount> -> Element(Number)
        // Returns the amount of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.amount"))
            return new Element(String.valueOf(getLivingEntity().getLastDamage()))
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <entity.last_damage.duration> -> Duration
        // Returns the duration of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.duration"))
            return new Duration((long) getLivingEntity().getNoDamageTicks())
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <entity.time_lived> -> Duration
        // Returns how long the entity has lived.
        // -->
        if (attribute.startsWith("time_lived"))
            return new Duration(entity.getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.can_pickup_items> -> Element(Boolean)
        // Returns true if the entity can pick up items. Else, returns false.
        // -->
        if (attribute.startsWith("can_pickup_items"))
            return new Element(String.valueOf(getLivingEntity().getCanPickupItems()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.eid> -> Element(Number)
        // Returns the entity's Bukkit entity ID
        // -->
        if (attribute.startsWith("eid"))
            return new Element(String.valueOf(entity.getEntityId()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.fall_distance> -> Element(Number)
        // Returns how far the entity has fallen.
        // -->
        if (attribute.startsWith("fall_distance"))
            return new Element(String.valueOf(entity.getFallDistance()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.uuid> -> Element(Number)
        // Returns a unique ID for the entity.
        // -->
        if (attribute.startsWith("uuid"))
            return new Element(String.valueOf(entity.getUniqueId().toString()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.has_effect[<effect>]> -> Element(Boolean)
        // Returns true if the entity has an effect. If no effect is
        // specified, returns true if the entity has any effect. Else,
        // returns false.
        // -->
        if (attribute.startsWith("has_effect")) {
        	Boolean returnElement = false;
            if (attribute.hasContext(1))
            	for (org.bukkit.potion.PotionEffect effect : getLivingEntity().getActivePotionEffects())
            		if (effect.getType().equals(org.bukkit.potion.PotionType.valueOf(attribute.getContext(1))))
            			returnElement = true;
            else if (!getLivingEntity().getActivePotionEffects().isEmpty()) returnElement = true;
            return new Element(returnElement).getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.equipment> -> dInventory
        // Returns the dInventory of the entity.
        // -->
        if (attribute.startsWith("equipment")) {
            return new dInventory(getLivingEntity()).getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.world> -> dWorld
        // Returns the world the entity is in.
        // -->
        if (attribute.startsWith("world")) {
            return new dWorld(entity.getWorld())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.prefix> -> Element
        // Returns the prefix of the entity.
        // -->
        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <entity.debug.log> -> Element(Boolean)
        // Debugs the entity in the log and returns true.
        // -->
        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--
        // <entity.debug.no_color> -> Element
        // Returns the entity's debug with no color.
        // -->
        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--
        // <entity.debug> -> Element
        // Returns the entity's debug.
        // -->
        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <entity.type> -> Element
        // Returns the entity's type.
        // -->
        if (attribute.startsWith("type")) {
            return new Element(getType())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
