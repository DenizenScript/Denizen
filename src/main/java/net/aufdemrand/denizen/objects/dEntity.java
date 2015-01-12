package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.objects.properties.entity.EntityAge;
import net.aufdemrand.denizen.objects.properties.entity.EntityColor;
import net.aufdemrand.denizen.objects.properties.entity.EntityTame;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptHelper;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dEntity implements dObject, Adjustable {


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
    @Fetchable("e")
    public static dEntity valueOf(String string) {
        if (string == null) return null;

        Matcher m;

        ///////
        // Handle objects with properties through the object fetcher
        m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dEntity.class, string);
        }


        // Choose a random entity type if "RANDOM" is used
        if (string.equalsIgnoreCase("RANDOM")) {

            EntityType randomType = null;

            // When selecting a random entity type, ignore invalid or inappropriate ones
            while (randomType == null ||
                    randomType.name().matches("^(COMPLEX_PART|DROPPED_ITEM|ENDER_CRYSTAL" +
                            "|ENDER_DRAGON|FISHING_HOOK|ITEM_FRAME|LEASH_HITCH|LIGHTNING" +
                            "|PAINTING|PLAYER|UNKNOWN|WEATHER|WITHER|WITHER_SKULL)$")) {

                randomType = EntityType.values()[CoreUtilities.getRandom().nextInt(EntityType.values().length)];
            }

            return new dEntity(randomType, "RANDOM");
        }

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.

        m = entity_by_id.matcher(string);

        if (m.matches()) {

            String entityGroup = m.group(1).toUpperCase();

            // NPC entity
            if (entityGroup.matches("N@")) {

                dNPC npc = dNPC.valueOf(string);

                if (npc != null)
                    return new dEntity(npc);
                else dB.echoError("NPC '" + string
                        + "' does not exist!");
            }

            // Player entity
            else if (entityGroup.matches("P@")) {
                LivingEntity returnable = dPlayer.valueOf(m.group(2)).getPlayerEntity();

                if (returnable != null) return new dEntity(returnable);
                else dB.echoError("Invalid Player! '" + m.group(2)
                        + "' could not be found. Has the player logged off?");
            }

            // Assume entity
            else {
                try {
                    UUID entityID = UUID.fromString(m.group(2));
                    Entity entity = getEntityForID(entityID);
                    if (entity != null) return new dEntity(entity);
                    return null;
                }
                catch (Exception ex) {
                    ex.getCause(); // DO NOTHING
                }

//                else if (isSaved(m.group(2)))
//                    return getSaved(m.group(2));
            }
        }

        string = string.replace("e@", "");

        ////////
        // Match Custom Entity

        if (ScriptRegistry.containsScript(string, EntityScriptContainer.class)) {
            // Construct a new custom unspawned entity from script
            return ScriptRegistry.getScriptContainerAs(string, EntityScriptContainer.class).getEntityFrom();
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

    @Deprecated
    public static Entity getEntityForID(UUID ID) {
        for (World world : Bukkit.getWorlds()) {
            net.minecraft.server.v1_8_R1.Entity nmsEntity = ((CraftWorld) world).getHandle().getEntity(ID);

            // Make sure the nmsEntity is valid, to prevent unpleasant errors
            if (nmsEntity != null) {
                return nmsEntity.getBukkitEntity();
            }
        }
        return null;
    }

    final static Pattern entity_by_id = Pattern.compile("(n@|e@|p@)(.+)",
            Pattern.CASE_INSENSITIVE);

    final static Pattern entity_with_data = Pattern.compile("(\\w+),?(\\w+)?,?(\\w+)?",
            Pattern.CASE_INSENSITIVE);

    public static boolean matches(String arg) {

        // Accept anything that starts with a valid entity object identifier.
        Matcher m;
        m = entity_by_id.matcher(arg);
        if (m.matches()) return true;

        // No longer picky about e@.. let's remove it from the arg
        arg = arg.replace("e@", "").toUpperCase();

        // Allow 'random'
        if (arg.equals("RANDOM"))
            return true;

        // Allow any entity script
        if (ScriptRegistry.containsScript(arg, EntityScriptContainer.class))
            return true;

        // Use regex to make some matcher groups
        m = entity_with_data.matcher(arg);
        if (m.matches()) {
            // Check first word with a valid entity_type (other groups are datas used in constructors)
            for (EntityType type : EntityType.values())
                if (type.name().equals(m.group(1))) return true;
        }

        // No luck otherwise!
        return false;
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    public dEntity(Entity entity) {
        if (entity != null) {
            this.entity = entity;
            EntityScript = EntityScriptHelper.getEntityScript(entity);
            this.uuid = entity.getUniqueId();
            this.entity_type = entity.getType();
            if (Depends.citizens != null && net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity)) {
                this.npc = new dNPC(net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getNPC(entity));
            }
        } else dB.echoError("Entity referenced is null!");
    }

    public dEntity(EntityType entityType) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
        } else dB.echoError("Entity_type referenced is null!");
    }

    public dEntity(EntityType entityType, ArrayList<Mechanism> mechanisms) {
        this(entityType);
        this.mechanisms = mechanisms;
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

    public dEntity(dNPC npc) {
        if (Depends.citizens == null) {
            return;
        }
        if (npc != null) {
            this.npc = npc;

            if (npc.isSpawned()) {
                this.entity = npc.getEntity();
                this.entity_type = npc.getEntity().getType();
                this.uuid = entity.getUniqueId();
            }
        } else dB.echoError("NPC referenced is null!");

    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private Entity entity = null;
    private EntityType entity_type = null;
    private String data1 = null;
    private String data2 = null;
    private DespawnedEntity despawned_entity = null;
    private dNPC npc = null;
    private UUID uuid = null;
    private String EntityScript = null;

    public EntityType getEntityType() {
        return entity_type;
    }

    public void setEntityScript(String EntityScript) {
        this.EntityScript = EntityScript;
    }

    public String getEntityScript() {
        return EntityScript;
    }

    /**
     * Returns the unique UUID of this entity
     *
     * @return  The UUID
     */

    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the dObject that most accurately describes this entity,
     * useful for automatically saving dEntities to contexts as
     * dNPCs and dPlayers
     *
     * @return  The dObject
     */

    public dObject getDenizenObject() {

        if (entity == null) return null;

        if (isNPC()) {
            return getDenizenNPC();
        }
        else if (isPlayer()) return new dPlayer(getPlayer());
        else return this;
    }

    /**
     * Get the Bukkit entity corresponding to this dEntity
     *
     * @return the underlying Bukkit entity
     */

    public Entity getBukkitEntity() {
        return entity;
    }

    /**
     * Get the living entity corresponding to this dEntity
     *
     * @return  The living entity
     */

    public LivingEntity getLivingEntity() {
        if (entity instanceof LivingEntity)
            return (LivingEntity) entity;
        else return null;
    }

    /**
     * Check whether this dEntity is a living entity
     *
     * @return  true or false
     */

    public boolean isLivingEntity() {
        return (entity instanceof LivingEntity);
    }

    public boolean hasInventory() { return getBukkitEntity() instanceof InventoryHolder || isNPC(); }

    /**
     * Get the dNPC corresponding to this dEntity
     *
     * @return  The dNPC
     */

    public dNPC getDenizenNPC() {
        if (Depends.citizens == null)
            return null;
        if (npc != null)
            return npc;
        else if (entity != null && net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity))
            return dNPC.fromEntity(entity);
        else return null;
    }

    /**
     * Check whether this dEntity is an NPC
     *
     * @return  true or false
     */

    public boolean isNPC() {
        if (Depends.citizens == null)
            return false;
        if (npc != null) return true;
        else if (entity != null && net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity)) return true;
        else return false;
    }

    /**
     * Get the Player corresponding to this dEntity
     *
     * @return  The Player
     */

    public Player getPlayer() {

        return (Player) entity;
    }

    /**
     * Get the dPlayer corresponding to this dEntity
     *
     * @return  The dPlayer
     */

    public dPlayer getDenizenPlayer() {

        return new dPlayer(getPlayer());
    }

    /**
     * Check whether this dEntity is a Player
     *
     * @return  true or false
     */

    public boolean isPlayer() {
        return !isNPC() && entity instanceof Player;
    }

    /**
     * Get this dEntity as a Projectile
     *
     * @return  The Projectile
     */

    public Projectile getProjectile() {

        return (Projectile) entity;
    }

    /**
     * Check whether this dEntity is a Projectile
     *
     * @return  true or false
     */

    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    /**
     * Get this Projectile entity's shooter
     *
     * @return  A dEntity of the shooter
     */

    public dEntity getShooter() {
        if (hasShooter())
            return new dEntity((LivingEntity) getProjectile().getShooter());
        else
            return null;
    }

    /**
     * Set this Projectile entity's shooter
     *
     */

    public void setShooter(dEntity shooter) {
        if (isProjectile() && shooter.isLivingEntity())
            getProjectile().setShooter(shooter.getLivingEntity());
    }

    /**
     * Check whether this entity has a shooter.
     *
     * @return  true or false
     */

    public boolean hasShooter() {
        return isProjectile() && getProjectile().getShooter() != null && getProjectile().getShooter() instanceof LivingEntity;
        // TODO: Handle other shooter source thingy types
    }

    public Inventory getBukkitInventory() {
        if (hasInventory()) {
            if (!isNPC())
                return ((InventoryHolder) getBukkitEntity()).getInventory();
        }
        return null;
    }

    /**
     * Returns this entity's dInventory.
     *
     * @return  the entity's dInventory
     */

    public dInventory getInventory() {
        return hasInventory() ? isNPC() ? getDenizenNPC().getDenizenInventory()
                : new dInventory(getBukkitInventory()) : null;
    }

    public String getName() {
        if (isNPC())
            return getDenizenNPC().getCitizen().getName();
        if (entity instanceof Player)
            return ((Player) entity).getName();
        if (isLivingEntity()) {
            String customName = getLivingEntity().getCustomName();
            if (customName != null)
                return customName;
        }
        return entity_type.name();
    }

    /**
     * Returns this entity's equipment
     *
     * @return  the entity's equipment
     */

    public dList getEquipment() {
        return getInventory().getEquipment();
    }

    /**
     * Whether this entity identifies as a generic
     * entity type, for instance "e@cow", instead of
     * a spawned entity
     *
     * @return  true or false
     */

    public boolean isGeneric() {
        return !isUnique();
    }

    /**
     * Get the location of this entity
     *
     * @return  The Location
     */

    public dLocation getLocation() {

        if (isUnique() && entity != null) {
            return new dLocation(entity.getLocation());
        }

        return null;
    }

    /**
     * Get the eye location of this entity
     *
     * @return  The location
     */

    public dLocation getEyeLocation() {

        if (!isGeneric() && isLivingEntity()) {
            return new dLocation(getLivingEntity().getEyeLocation());
        }
        else if (!isGeneric()) {
            return new dLocation(getBukkitEntity().getLocation());
        }

        return null;
    }

    /**
     * Gets the velocity of this entity
     *
     * @return  The velocity's vector
     */

    public Vector getVelocity() {

        if (!isGeneric()) {
            return entity.getVelocity();
        }
        return null;
    }

    /**
     * Sets the velocity of this entity
     *
     */

    public void setVelocity(Vector vector) {

        if (!isGeneric()) {
            if (entity instanceof WitherSkull) {
                ((WitherSkull) entity).setDirection(vector);
            }
            else {
                entity.setVelocity(vector);
            }
        }
    }

    /**
     * Gets the world of this entity
     *
     * @return  The entity's world
     */

    public World getWorld() {

        if (!isGeneric()) {
            return entity.getWorld();
        }
        return null;
    }

    public void spawnAt(Location location) {
        // If the entity is already spawned, teleport it.

        if (isNPC()) {
            if (getDenizenNPC().getCitizen().isSpawned())
                getDenizenNPC().getCitizen().teleport(location, TeleportCause.PLUGIN);
            else {
                getDenizenNPC().getCitizen().spawn(location);
                entity = getDenizenNPC().getCitizen().getEntity();
                uuid = getDenizenNPC().getCitizen().getEntity().getUniqueId();
            }
        }
        else if (entity != null && isUnique()) entity.teleport(location);

        else {
            if (entity_type != null) {
                if (despawned_entity != null) {
                    // If entity had a custom_script, use the script to rebuild the base entity.
                    if (despawned_entity.custom_script != null) {
                        // TODO: Build entity from custom script
                    }
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
                        if (Depends.citizens == null) {
                            dB.echoError("Cannot spawn entity of type PLAYER!");
                            return;
                        }
                        else {
                            dNPC npc = new dNPC(net.citizensnpcs.api.CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, data1));
                            npc.getCitizen().spawn(location);
                            entity = npc.getEntity();
                            uuid = entity.getUniqueId();
                        }
                    }
                    else if (entity_type.name().matches("FALLING_BLOCK")) {

                        Material material = null;

                        if (data1 != null && dMaterial.matches(data1)) {

                            material = dMaterial.valueOf(data1).getMaterial();

                            // If we did not get a block with "RANDOM", or we got
                            // air or portals, keep trying
                            while (data1.equalsIgnoreCase("RANDOM") &&
                                    ((!material.isBlock()) ||
                                            material == Material.AIR ||
                                            material == Material.PORTAL ||
                                            material == Material.ENDER_PORTAL)) {

                                material = dMaterial.valueOf(data1).getMaterial();
                            }
                        }

                        // If material is null or not a block, default to SAND
                        if (material == null || (!material.isBlock())) {

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
                        uuid = entity.getUniqueId();
                    }
                    else {

                        if (entity_type == EntityType.DROPPED_ITEM)
                            ent = location.getWorld().dropItem(location, new ItemStack(Material.STONE));
                        else
                            ent = location.getWorld().spawnEntity(location, entity_type);
                        entity = ent;
                        uuid = entity.getUniqueId();
                        if (EntityScript != null)
                            EntityScriptHelper.setEntityScript(entity, EntityScript);

                        if (entity_type.name().matches("PIG_ZOMBIE")) {

                            // Give pig zombies golden swords by default, unless data2 specifies
                            // a different weapon
                            if (!dItem.matches(data1)) {
                                data1 = "gold_sword";
                            }

                            ((PigZombie) entity).getEquipment()
                                    .setItemInHand(dItem.valueOf(data1).getItemStack());
                        }
                        else if (entity_type.name().matches("SKELETON")) {

                            // Give skeletons bows by default, unless data2 specifies
                            // a different weapon
                            if (!dItem.matches(data2)) {
                                data2 = "bow";
                            }

                            ((Skeleton) entity).getEquipment()
                                    .setItemInHand(dItem.valueOf(data2).getItemStack());
                        }

                        // If there is some special subtype data associated with this dEntity,
                        // use the setSubtype method to set it in a clean, object-oriented
                        // way that uses reflection
                        //
                        // Otherwise, just use entity-specific methods manually
                        if (data1 != null) {

                            // TODO: Discourage usage of + delete the below (Use properties instead!)
                            // TODO: Remove in 1.0
                            try {

                                // Allow creepers to be powered - replaced by EntityPowered
                                if (ent instanceof Creeper && data1.equalsIgnoreCase("POWERED")) {
                                    ((Creeper) entity).setPowered(true);
                                }

                                // Allow setting of blocks held by endermen - replaced by EntityItem
                                else if (ent instanceof Enderman && dMaterial.matches(data1)) {
                                    ((Enderman) entity).setCarriedMaterial(dMaterial.valueOf(data1).getMaterialData());
                                }

                                // Allow setting of horse variants and colors - replaced by EntityColor
                                else if (ent instanceof Horse) {
                                    setSubtype("org.bukkit.entity.Horse", "org.bukkit.entity.Horse$Variant", "setVariant", data1);

                                    if (data2 != null) {
                                        setSubtype("org.bukkit.entity.Horse", "org.bukkit.entity.Horse$Color", "setColor", data2);
                                    }
                                }

                                // Allow setting of ocelot types - replaced by EntityColor
                                else if (ent instanceof Ocelot) {
                                    setSubtype("org.bukkit.entity.Ocelot", "org.bukkit.entity.Ocelot$Type", "setCatType", data1);
                                }

                                // Allow setting of sheep colors - replaced by EntityColor
                                else if (ent instanceof Sheep) {
                                    setSubtype("org.bukkit.entity.Sheep", "org.bukkit.DyeColor", "setColor", data1);
                                }

                                // Allow setting of skeleton types - replaced by EntitySkeleton
                                else if (ent instanceof Skeleton) {
                                    setSubtype("org.bukkit.entity.Skeleton", "org.bukkit.entity.Skeleton$SkeletonType", "setSkeletonType", data1);
                                }
                                // Allow setting of slime sizes - replaced by EntitySize
                                else if (ent instanceof Slime && aH.matchesInteger(data1)) {
                                    ((Slime) entity).setSize(aH.getIntegerFrom(data1));
                                }

                                // Allow setting of villager professions - replaced by EntityProfession
                                else if (ent instanceof Villager) {
                                    setSubtype("org.bukkit.entity.Villager", "org.bukkit.entity.Villager$Profession", "setProfession", data1);
                                }

                            } catch (Exception e) {
                                dB.echoError("Error setting custom entity data.");
                                dB.echoError(e);
                            }
                        }
                    }
                }
            }

            else dB.echoError("Cannot spawn a null dEntity!");

            if (!isUnique()) {
                dB.echoError("Error spawning entity - bad entity type, blocked by another plugin, or tried to spawn in an unloaded chunk?");
                return;
            }

            for (Mechanism mechanism: mechanisms) {
                adjust(mechanism);
            }
            mechanisms.clear();
        }
    }

    public void despawn() {
        despawned_entity = new DespawnedEntity(this);
        getLivingEntity().remove();
    }

    public void respawn() {
        if (despawned_entity != null)
            spawnAt(despawned_entity.location);
        else if (entity == null)
            dB.echoError("Cannot respawn a null dEntity!");

    }

    public boolean isSpawned() {
        return entity != null && isValid();
    }

    public boolean isValid() {
        return entity.isValid();
    }

    public void remove() {
        EntityScriptHelper.unlinkEntity(entity);
        entity.remove();
    }

    public void teleport(Location location) {
        if (isNPC())
            getDenizenNPC().getCitizen().teleport(location, TeleportCause.PLUGIN);
        else
            entity.teleport(location);
    }

    /**
     * Make this entity target another living entity, attempting both
     * old entity AI and new entity AI targeting methods
     *
     * @param target  The LivingEntity target
     */

    public void target(LivingEntity target) {

        if (!isSpawned() || !(entity instanceof Creature)) {
            dB.echoError(identify() + " is not a valid creature entity!");
            return;
        }

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

            entityClass.getMethod(method, typeClass).invoke(entity, types[CoreUtilities.getRandom().nextInt(types.length)]);
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

    public void setEntity(Entity entity) {
        this.entity = entity;
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

    public int comparesTo(dEntity entity) {
        // Never matches a null
        if (entity == null) return 0;

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


    /////////////////////
    //  dObject Methods
    ///////////////////

    private String prefix = "Entity";

    @Override
    public String getObjectType() {
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

        // Check if entity is an NPC
        if (npc != null) {
            return "n@" + npc.getId();
        }

        // Check if entity is a Player or is spawned
        if (entity != null) {
            if (isPlayer())
                return "p@" + getPlayer().getUniqueId();

                // TODO:
                // Check if entity is a 'notable entity'
                // if (isSaved(this))
                //    return "e@" + getSaved(this);

            else if (isSpawned())
                return "e@" + entity.getUniqueId().toString();
        }

        // Try to identify as an entity script
        if (EntityScript != null)
            return "e@" + EntityScript;

        // Check if an entity_type is available
        if (entity_type != null) {
            // Build the pseudo-property-string, if any
            StringBuilder properties = new StringBuilder();
            for (Mechanism mechanism: mechanisms) {
                properties.append(mechanism.getName()).append("=").append(mechanism.getValue().asString().replace(';', (char)0x2011)).append(";");
            }
            String propertyOutput = "";
            if (properties.length() > 0) {
                propertyOutput = "[" + properties.substring(0, properties.length() - 1) + "]";
            }
            return "e@" + entity_type.name() + propertyOutput;
        }

        return "null";
    }


    @Override
    public String identifySimple() {

        // Check if entity is an NPC
        if (npc != null) {
            return "n@" + npc.getId();
        }

        if (isPlayer())
            return "p@" + getPlayer().getName();

        // Try to identify as an entity script
        if (EntityScript != null)
            return "e@" + EntityScript;

        // Check if an entity_type is available
        if (entity_type != null)
            return "e@" + entity_type.name();

        return "null";
    }


    public String identifyType() {
        if (isNPC()) return "npc";
        else if (isPlayer()) return "player";
        else return "e@" + entity_type.name();
    }

    public String identifySimpleType() {
        if (isNPC()) return "npc";
        else if (isPlayer()) return "player";
        else return entity_type.name();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public boolean isUnique() {
        return (isPlayer() || isNPC() || isSpawned());  // || isSaved()
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (entity == null && entity_type == null) {
            if (npc != null) {
                return new Element(identify()).getAttribute(attribute);
            }
            dB.echoError("dEntity has returned null.");
            return null;
        }

        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.debug.log>
        // @returns Element(Boolean)
        // @group debug
        // @description
        // Debugs the entity in the log and returns true.
        // -->
        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.debug.no_color>
        // @returns Element
        // @group debug
        // @description
        // Returns the entity's debug with no color.
        // -->
        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.debug>
        // @returns Element
        // @group debug
        // @description
        // Returns the entity's debug.
        // -->
        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.prefix>
        // @returns Element
        // @group debug
        // @description
        // Returns the prefix of the entity.
        // -->
        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.type>
        // @returns Element
        // @description
        // Always returns 'Entity' for dEntity objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("Entity").getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   UNSPAWNED ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.entity_type>
        // @returns Element
        // @group data
        // @description
        // Returns the type of the entity.
        // -->
        if (attribute.startsWith("entity_type")) {
            return new Element(entity_type.name()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.is_spawned>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns whether the entity is spawned.
        // -->
        if (attribute.startsWith("is_spawned")) {
            return new Element(isSpawned())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.eid>
        // @returns Element(Number)
        // @group data
        // @description
        // Returns the entity's temporary server entity ID.
        // -->
        if (attribute.startsWith("eid"))
            return new Element(entity.getEntityId())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.uuid>
        // @returns Element
        // @group data
        // @description
        // Returns the permanent unique ID of the entity.
        // Works with offline players.
        // -->
        if (attribute.startsWith("uuid"))
            return new Element(getUUID().toString())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.scriptname>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns the name of the entity script that spawned this entity, if any.
        // -->
        if (attribute.startsWith("script")) {
            return new Element(EntityScript == null ? "null": EntityScript)
                    .getAttribute(attribute.fulfill(1));
        }

        if (entity == null) {
            return new Element(identify()).getAttribute(attribute);
        }
        // Only spawned entities past this point!


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.custom_id>
        // @returns dScript/Element
        // @group data
        // @description
        // If the entity has a script ID, returns the dScript of that ID.
        // Otherwise, returns the name of the entity type.
        // -->
        if (attribute.startsWith("custom_id")) {
            if (CustomNBT.hasCustomNBT(getLivingEntity(), "denizen-script-id"))
                return new dScript(CustomNBT.getCustomNBT(getLivingEntity(), "denizen-script-id"))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(entity.getType().name())
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.custom_name>
        // @returns Element
        // @group attributes
        // @description
        // If the entity has a custom name, returns the name as an Element.
        // Otherwise, returns null.
        // -->
        if (attribute.startsWith("custom_name")) {
            if (!isLivingEntity() || getLivingEntity().getCustomName() == null)
                return null;
            return new Element(getLivingEntity().getCustomName()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.custom_name.visible>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns true if the entity's custom name is visible.
        // -->
        if (attribute.startsWith("custom_name.visible")) {
            if (!isLivingEntity())
                return null;
            return new Element(getLivingEntity().isCustomNameVisible())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.name>
        // @returns Element
        // @group data
        // @description
        // Returns the name of the entity.
        // This can be a player name, an NPC name, a custom_name, or the entity type.
        // Works with offline players.
        // -->
        if (attribute.startsWith("name")) {
            return new Element(getName()).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   INVENTORY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.equipment.boots>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as boots, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.boots")) {
            if (getLivingEntity().getEquipment().getBoots() != null) {
                return new dItem(getLivingEntity().getEquipment().getBoots())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.chestplate>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as a chestplate, or null
        // if none.
        // -->
        else if (attribute.startsWith("equipment.chestplate") ||
                attribute.startsWith("equipment.chest")) {
            if (getLivingEntity().getEquipment().getChestplate() != null) {
                return new dItem(getLivingEntity().getEquipment().getChestplate())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.helmet>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as a helmet, or null
        // if none.
        // -->
        else if (attribute.startsWith("equipment.helmet") ||
                attribute.startsWith("equipment.head")) {
            if (getLivingEntity().getEquipment().getHelmet() != null) {
                return new dItem(getLivingEntity().getEquipment().getHelmet())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment.leggings>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is wearing as leggings, or null
        // if none.
        // -->
        else if (attribute.startsWith("equipment.leggings") ||
                attribute.startsWith("equipment.legs")) {
            if (getLivingEntity().getEquipment().getLeggings() != null) {
                return new dItem(getLivingEntity().getEquipment().getLeggings())
                        .getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.equipment>
        // @returns dList
        // @group inventory
        // @description
        // returns a dInventory containing the entity's equipment.
        // -->
        else if (attribute.startsWith("equipment")) {
            return getEquipment().getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.item_in_hand>
        // @returns dItem
        // @group inventory
        // @description
        // returns the item the entity is holding, or i@air
        // if none.
        // -->
        if (attribute.startsWith("item_in_hand") ||
                attribute.startsWith("iteminhand"))
            return new dItem(getLivingEntity().getEquipment().getItemInHand())
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.map_trace>
        // @returns dLocation
        // @group inventory
        // @description
        // returns a 2D location indicating where on the map the entity's looking at.
        // Each coordinate is in the range of 0 to 128.
        // -->
        if (attribute.startsWith("map_trace")) {
            Rotation.MapTraceResult mtr  = Rotation.mapTrace(getLivingEntity(), 200);
            if (mtr != null) {
                double x = 0;
                double y = 0;
                double basex = mtr.hitLocation.getX() - Math.floor(mtr.hitLocation.getX());
                double basey = mtr.hitLocation.getY() - Math.floor(mtr.hitLocation.getY());
                double basez = mtr.hitLocation.getZ() - Math.floor(mtr.hitLocation.getZ());
                if (mtr.angle == BlockFace.NORTH) {
                    x = 128f - (basex * 128f);
                }
                else if (mtr.angle == BlockFace.SOUTH) {
                    x = basex * 128f;
                }
                else if (mtr.angle == BlockFace.WEST) {
                    x = basez * 128f;
                }
                else if (mtr.angle == BlockFace.EAST) {
                    x = 128f - (basez * 128f);
                }
                y = 128f - (basey * 128f);
                return new dLocation(null, Math.round(x), Math.round(y)).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <e@entity.can_see[<entity>]>
        // @returns Element(Boolean)
        // @group location
        // @description
        // Returns whether the entity can see the specified other entity.
        // -->
        if (attribute.startsWith("can_see")) {
            if (attribute.hasContext(1) && dEntity.matches(attribute.getContext(1))) {
                dEntity toEntity = dEntity.valueOf(attribute.getContext(1));
                if (toEntity != null && toEntity.isSpawned())
                    return new Element(getLivingEntity().hasLineOfSight(toEntity.getBukkitEntity())).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <e@entity.eye_location>
        // @returns dLocation
        // @group location
        // @description
        // returns the location of the entity's eyes.
        // -->
        if (attribute.startsWith("eye_location"))
            return new dLocation(getEyeLocation())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.get_eye_height>
        // @returns Element(Boolean)
        // @group location
        // @description
        // Returns the height of the entity's eyes above its location.
        // -->
        if (attribute.startsWith("get_eye_height")) {
            if (isLivingEntity())
                return new Element(getLivingEntity().getEyeHeight())
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element("null")
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.location.cursor_on[<range>]>
        // @returns dLocation
        // @group location
        // @description
        // Returns the location of the block the entity is looking at.
        // Optionally, specify a maximum range to find the location from.
        // -->
        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new dLocation(getLivingEntity().getTargetBlock(null, range).getLocation().clone())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.location.standing_on>
        // @returns dLocation
        // @group location
        // @description
        // Returns the location of what the entity is standing on.
        // Works with offline players.
        // -->
        if (attribute.startsWith("location.standing_on"))
            return new dLocation(entity.getLocation().clone().add(0, -0.5f, 0))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <e@entity.location>
        // @returns dLocation
        // @group location
        // @description
        // Returns the location of the entity.
        // Works with offline players.
        // -->
        if (attribute.startsWith("location")) {
            return new dLocation(entity.getLocation())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.velocity>
        // @returns dLocation
        // @group location
        // @description
        // Returns the movement velocity of the entity.
        // Note: Does not accurately calculate player clientside movement velocity.
        // -->
        if (attribute.startsWith("velocity")) {
            return new dLocation(entity.getVelocity().toLocation(entity.getWorld()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.world>
        // @returns dWorld
        // @group location
        // @description
        // Returns the world the entity is in.
        // -->
        if (attribute.startsWith("world")) {
            return new dWorld(entity.getWorld())
                    .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.can_pickup_items>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity can pick up items.
        // -->
        if (attribute.startsWith("can_pickup_items"))
            return new Element(getLivingEntity().getCanPickupItems())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.fallingblock_material>
        // @returns dMaterial
        // @group attributes
        // @description
        // Returns the material of a fallingblock-type entity.
        // -->
        if (attribute.startsWith("fallingblock_material"))
            return dMaterial.getMaterialFrom(((FallingBlock) entity).getMaterial())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.fall_distance>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns how far the entity has fallen.
        // -->
        if (attribute.startsWith("fall_distance"))
            return new Element(entity.getFallDistance())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.fire_time>
        // @returns Duration
        // @group attributes
        // @description
        // Returns the duration for which the entity will remain on fire
        // -->
        if (attribute.startsWith("fire_time"))
            return new Duration(entity.getFireTicks() / 20)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.get_leash_holder>
        // @returns dEntity
        // @group attributes
        // @description
        // Returns the leash holder of entity.
        // -->
        if (attribute.startsWith("get_leash_holder")) {
            if (isLivingEntity() && getLivingEntity().isLeashed()) {
                return new dEntity(getLivingEntity().getLeashHolder())
                        .getAttribute(attribute.fulfill(1));
            }
            else return new Element("null")
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.get_passenger>
        // @returns dEntity
        // @group attributes
        // @description
        // If the entity has a passenger, returns the passenger as a dEntity.
        // Otherwise, returns null.
        // -->
        if (attribute.startsWith("get_passenger")) {
            if (!entity.isEmpty())
                return new dEntity(entity.getPassenger())
                        .getAttribute(attribute.fulfill(1));
            else return new Element("null")
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.get_shooter>
        // @returns dEntity
        // @group attributes
        // @description
        // If the entity is a projectile with a shooter, gets its shooter
        // Otherwise, returns null.
        // -->
        if (attribute.startsWith("get_shooter") ||
                attribute.startsWith("shooter")) {
            if (isProjectile() && hasShooter())
                return getShooter().getAttribute(attribute.fulfill(1));
            else return new Element("null")
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.get_vehicle>
        // @returns dEntity
        // @group attributes
        // @description
        // If the entity is in a vehicle, returns the vehicle as a dEntity.
        // Otherwise, returns null.
        // -->
        if (attribute.startsWith("get_vehicle")) {
            if (entity.isInsideVehicle())
                return new dEntity(entity.getVehicle())
                        .getAttribute(attribute.fulfill(1));
            else return new Element("null")
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.has_effect[<effect>]>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity has a specified effect.
        // If no effect is specified, returns whether the entity has any effect.
        // -->
        // TODO: add list_effects ?
        if (attribute.startsWith("has_effect")) {
            Boolean returnElement = false;
            if (attribute.hasContext(1)) {
                for (org.bukkit.potion.PotionEffect effect : getLivingEntity().getActivePotionEffects())
                    if (effect.getType().equals(PotionEffectType.getByName(attribute.getContext(1))))
                        returnElement = true;
            }
            else if (!getLivingEntity().getActivePotionEffects().isEmpty()) returnElement = true;
            return new Element(returnElement).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.health.formatted>
        // @returns Element
        // @group attributes
        // @description
        // Returns a formatted value of the player's current health level.
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

        // <--[tag]
        // @attribute <e@entity.health.max>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the maximum health of the entity.
        // -->
        if (attribute.startsWith("health.max"))
            return new Element(getLivingEntity().getMaxHealth())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <e@entity.health.percentage>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the entity's current health as a percentage.
        // -->
        if (attribute.startsWith("health.percentage")) {
            double maxHealth = getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            return new Element((getLivingEntity().getHealth() / maxHealth) * 100)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.health>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the current health of the entity.
        // -->
        if (attribute.startsWith("health"))
            return new Element(getLivingEntity().getHealth())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.can_breed>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the animal entity is capable of mating with another of its kind.
        // -->
        if (attribute.startsWith("can_breed"))
            return new Element(((Ageable)getLivingEntity()).canBreed())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_breeding>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the animal entity is trying to with another of its kind.
        // -->
        if (attribute.startsWith("is_breeding"))
            return new Element(((CraftAnimals)getLivingEntity()).getHandle().ce())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_empty>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity does not have a passenger.
        // -->
        if (attribute.startsWith("is_empty"))
            return new Element(entity.isEmpty())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_inside_vehicle>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is inside a vehicle.
        // -->
        if (attribute.startsWith("is_inside_vehicle"))
            return new Element(entity.isInsideVehicle())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_leashed>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is leashed.
        // -->
        if (attribute.startsWith("is_leashed")) {
            if (isLivingEntity())
                return new Element(getLivingEntity().isLeashed())
                        .getAttribute(attribute.fulfill(1));
            else
                return Element.FALSE
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.is_on_ground>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is supported by a block.
        // -->
        if (attribute.startsWith("is_on_ground"))
            return new Element(entity.isOnGround())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_persistent>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity will not be removed completely when far away from players.
        // -->
        if (attribute.startsWith("is_persistent")) {
            if (isLivingEntity())
                return new Element(!getLivingEntity().getRemoveWhenFarAway())
                        .getAttribute(attribute.fulfill(1));
            else
                return Element.FALSE
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.killer>
        // @returns dPlayer
        // @group attributes
        // @description
        // Returns the player that last killed the entity.
        // -->
        if (attribute.startsWith("killer"))
            return new dPlayer(getLivingEntity().getKiller())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.last_damage.amount>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the amount of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.amount"))
            return new Element(getLivingEntity().getLastDamage())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <e@entity.last_damage.cause>
        // @returns Element
        // @group attributes
        // @description
        // Returns the cause of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.cause")
                && entity.getLastDamageCause() != null)
            return new Element(entity.getLastDamageCause().getCause().name())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <e@entity.last_damage.duration>
        // @returns Duration
        // @group attributes
        // @description
        // Returns the duration of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.duration"))
            return new Duration((long) getLivingEntity().getNoDamageTicks())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <e@entity.oxygen.max>
        // @returns Duration
        // @group attributes
        // @description
        // Returns the maximum duration of oxygen the entity can have.
        // Works with offline players.
        // -->
        if (attribute.startsWith("oxygen.max"))
            return new Duration((long) getLivingEntity().getMaximumAir())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.oxygen>
        // @returns Duration
        // @group attributes
        // @description
        // Returns the duration of oxygen the entity has left.
        // Works with offline players.
        // -->
        if (attribute.startsWith("oxygen"))
            return new Duration((long) getLivingEntity().getRemainingAir())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.remove_when_far>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns if the entity despawns when away from players.
        // -->
        if (attribute.startsWith("remove_when_far"))
            return new Element(getLivingEntity().getRemoveWhenFarAway())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.time_lived>
        // @returns Duration
        // @group attributes
        // @description
        // Returns how long the entity has lived.
        // -->
        if (attribute.startsWith("time_lived"))
            return new Duration(entity.getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   TYPE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.is_living>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a living entity.
        // -->
        if (attribute.startsWith("is_living")) {
            return new Element(isLivingEntity())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.is_mob>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a mob (Not a player or NPC).
        // -->
        if (attribute.startsWith("is_mob")) {
            if (!isPlayer() && !isNPC())
                return Element.TRUE.getAttribute(attribute.fulfill(1));
            else return Element.FALSE.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.is_npc>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns whether the entity is an NPC.
        // -->
        if (attribute.startsWith("is_npc")) {
            return new Element(isNPC())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.is_player>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a player.
        // Works with offline players.
        // -->
        if (attribute.startsWith("is_player")) {
            return new Element(isPlayer())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.is_projectile>
        // @returns Element(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a projectile.
        // -->
        if (attribute.startsWith("is_projectile")) {
            return new Element(isProjectile())
                    .getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   PROPERTY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <e@entity.is_tameable>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the entity is tameable.
        // If this returns true, it will enable access to:
        // <@link mechanism dEntity.tame>, <@link mechanism dEntity.owner>,
        // <@link tag e@entity.is_tamed>, and <@link tag e@entity.get_owner>
        // -->
        if (attribute.startsWith("is_tameable"))
            return new Element(EntityTame.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_ageable>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the entity is ageable.
        // If this returns true, it will enable access to:
        // <@link mechanism dEntity.age>, <@link mechanism dEntity.age_lock>,
        // <@link tag e@entity.is_baby>, <@link tag e@entity.age>,
        // and <@link tag e@entity.is_age_locked>
        // -->
        if (attribute.startsWith("is_ageable"))
            return new Element(EntityAge.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.is_colorable>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the entity can be colored.
        // If this returns true, it will enable access to:
        // <@link mechanism dEntity.color> and <@link tag e@entity.color>
        // -->
        if (attribute.startsWith("is_colorable"))
            return new Element(EntityColor.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <e@entity.describe>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns the entity's full description, including all properties.
        // -->
        if (attribute.startsWith("describe"))
            return new Element("e@" + getEntityType().name().toLowerCase()
                    + PropertyParser.getPropertiesString(this))
                    .getAttribute(attribute.fulfill(1));

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

    private ArrayList<Mechanism> mechanisms = new ArrayList<Mechanism>();

    public ArrayList<Mechanism> getWaitingMechanisms() {
        return mechanisms;
    }

    public void applyProperty(Mechanism mechanism) {
        if (isGeneric()) {
            mechanisms.add(mechanism);
        }
        else {
            dB.echoError("Cannot apply properties to an already-spawned entity!");
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (isGeneric()) {
            mechanisms.add(mechanism);
            return;
        }

        Element value = mechanism.getValue();

        // <--[mechanism]
        // @object dEntity
        // @name can_pickup_items
        // @input Element(Boolean)
        // @description
        // Sets whether the entity can pick up items.
        // The entity must be living.
        // @tags
        // <e@entity.can_pickup_items>
        // -->
        if (mechanism.matches("can_pickup_items") && mechanism.requireBoolean())
            getLivingEntity().setCanPickupItems(value.asBoolean());

        // <--[mechanism]
        // @object dEntity
        // @name custom_name
        // @input Element
        // @description
        // Sets the custom name of the entity.
        // The entity must be living.
        // @tags
        // <e@entity.custom_name>
        // -->
        if (mechanism.matches("custom_name"))
            getLivingEntity().setCustomName(value.asString());

        // <--[mechanism]
        // @object dEntity
        // @name custom_name_visibility
        // @input Element(Boolean)
        // @description
        // Sets whether the custom name is visible.
        // The entity must be living.
        // @tags
        // <e@entity.custom_name.visible>
        // -->
        if (mechanism.matches("custom_name_visibility") && mechanism.requireBoolean())
            getLivingEntity().setCustomNameVisible(value.asBoolean());

        // <--[mechanism]
        // @object dEntity
        // @name fall_distance
        // @input Element(Float)
        // @description
        // Sets the fall distance.
        // @tags
        // <e@entity.fall_distance>
        // -->
        if (mechanism.matches("fall_distance") && mechanism.requireFloat())
            entity.setFallDistance(value.asFloat());

        // <--[mechanism]
        // @object dEntity
        // @name fire_time
        // @input Duration
        // @description
        // Sets the entity's current fire time (time before the entity stops being on fire).
        // @tags
        // <e@entity.fire_time>
        // -->
        if (mechanism.matches("fire_time") && mechanism.requireObject(Duration.class))
            entity.setFireTicks(value.asType(Duration.class).getTicksAsInt());

        // <--[mechanism]
        // @object dEntity
        // @name leash_holder
        // @input dEntity
        // @description
        // Sets the entity holding this entity by leash.
        // The entity must be living.
        // @tags
        // <e@entity.is_leashed>
        // <e@entity.get_leash_holder>
        // -->
        if (mechanism.matches("leash_holder") && mechanism.requireObject(dEntity.class))
            getLivingEntity().setLeashHolder(value.asType(dEntity.class).getBukkitEntity());

        // <--[mechanism]
        // @object dEntity
        // @name max_health
        // @input Number
        // @description
        // Sets the maximum health the entity may have.
        // The entity must be living.
        // @tags
        // <e@entity.health>
        // <e@entity.health.max>
        // -->
        // TODO: Maybe a property?
        if (mechanism.matches("max_health") && mechanism.requireInteger()) {
            if (isNPC()) {
                if (getDenizenNPC().getCitizen().hasTrait(HealthTrait.class))
                    getDenizenNPC().getCitizen().getTrait(HealthTrait.class).setMaxhealth(mechanism.getValue().asInt());
                else
                    dB.echoError("NPC doesn't have health trait!");
            }
            else if (isLivingEntity()) {
                getLivingEntity().setMaxHealth(mechanism.getValue().asDouble());
            }
            else {
                dB.echoError("Entity is not alive!");
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name health
        // @input Number(Decimal)
        // @description
        // Sets the amount of health the entity has.
        // The entity must be living.
        // @tags
        // <e@entity.health>
        // <e@entity.health.max>
        // -->
        // TODO: Maybe a property?
        if (mechanism.matches("health") && mechanism.requireDouble()) {
            if (isLivingEntity()) {
                getLivingEntity().setHealth(mechanism.getValue().asDouble());
            }
            else {
                dB.echoError("Entity is not alive!");
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name can_breed
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is capable of mating with another of its kind.
        // The entity must be living and 'ageable'.
        // @tags
        // <e@entity.can_breed>
        // -->
        if (mechanism.matches("can_breed") && mechanism.requireBoolean())
            ((Ageable)getLivingEntity()).setBreed(true);

        // <--[mechanism]
        // @object dEntity
        // @name breed
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is trying to mate with another of its kind.
        // The entity must be living and an animal.
        // @tags
        // <e@entity.can_breed>
        // -->
        if (mechanism.matches("breed")) {
            dList list = dList.valueOf(value.asString());
            if (list.size() > 1) {
                if (list.get(0).equalsIgnoreCase("true"))
                    ((CraftAnimals)getLivingEntity()).getHandle().a((EntityHuman) null);
                else
                    ((CraftAnimals)getLivingEntity()).getHandle().cq();
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name passenger
        // @input dEntity
        // @description
        // Sets the passenger of this entity.
        // @tags
        // <e@entity.get_passenger>
        // <e@entity.is_empty>
        // -->
        if (mechanism.matches("passenger") && mechanism.requireObject(dEntity.class))
            entity.setPassenger(value.asType(dEntity.class).getBukkitEntity());

        // <--[mechanism]
        // @object dEntity
        // @name time_lived
        // @input Duration
        // @description
        // Sets the amount of time this entity has lived for.
        // @tags
        // <e@entity.time_lived>
        // -->
        if (mechanism.matches("time_lived") && mechanism.requireObject(Duration.class))
            entity.setTicksLived(value.asType(Duration.class).getTicksAsInt());

        // <--[mechanism]
        // @object dEntity
        // @name remaining_air
        // @input Element(Number)
        // @description
        // Sets how much air the entity has remaining before it drowns.
        // The entity must be living.
        // @tags
        // <e@entity.oxygen>
        // <e@entity.oxygen.max>
        // -->
        if (mechanism.matches("remaining_air") && mechanism.requireInteger())
            getLivingEntity().setRemainingAir(value.asInt());

        // <--[mechanism]
        // @object dEntity
        // @name remove_effects
        // @input None
        // @description
        // Removes all potion effects from the entity.
        // The entity must be living.
        // @tags
        // <e@entity.has_effect[<effect>]>
        // -->
        if (mechanism.matches("remove_effects"))
            for (PotionEffect potionEffect : this.getLivingEntity().getActivePotionEffects())
                getLivingEntity().removePotionEffect(potionEffect.getType());

        // <--[mechanism]
        // @object dEntity
        // @name remove_when_far_away
        // @input Element(Boolean)
        // @description
        // Sets whether the entity should be removed entirely when despawned.
        // The entity must be living.
        // @tags
        // <e@entity.remove_when_far>
        // -->
        if (mechanism.matches("remove_when_far_away") && mechanism.requireBoolean())
            getLivingEntity().setRemoveWhenFarAway(value.asBoolean());

        // <--[mechanism]
        // @object dEntity
        // @name velocity
        // @input dLocation
        // @description
        // Sets the entity's movement velocity.
        // @tags
        // <e@entity.velocity>
        // -->
        if (mechanism.matches("velocity") && mechanism.requireObject(dLocation.class)) {
            setVelocity(value.asType(dLocation.class).toVector());
        }

        // <--[mechanism]
        // @object dEntity
        // @name interact_with
        // @input dLocation
        // @description
        // Makes a player-type entity interact with a block.
        // @tags
        // None
        // -->
        if (mechanism.matches("interact_with") && mechanism.requireObject(dLocation.class)) {
            dLocation interactLocation = value.asType(dLocation.class);
            CraftPlayer craftPlayer = (CraftPlayer)getPlayer();
            BlockPosition pos =
                    new BlockPosition(interactLocation.getBlockX(),
                            interactLocation.getBlockY(),
                            interactLocation.getBlockZ());
            Block.getById(interactLocation.getBlock().getType().getId())
                    .interact(((CraftWorld) interactLocation.getWorld()).getHandle(),
                            pos,
                            ((CraftWorld) interactLocation.getWorld()).getHandle().getType(pos),
                            craftPlayer != null ? craftPlayer.getHandle() : null, EnumDirection.NORTH, 0f, 0f, 0f);
        }

        // <--[mechanism]
        // @object dEntity
        // @name play_death
        // @input None
        // @description
        // Animates the entity dying.
        // @tags
        // None
        // -->
        if (mechanism.matches("play_death")) {
            getLivingEntity().playEffect(EntityEffect.DEATH);
        }

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled())
                break;
        }

        if (!mechanism.fulfilled())
            mechanism.reportInvalid();
    }
}
