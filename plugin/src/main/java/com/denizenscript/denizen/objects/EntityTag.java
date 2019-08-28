package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.properties.entity.EntityAge;
import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizen.objects.properties.entity.EntityTame;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.interfaces.EntityHelper;
import com.denizenscript.denizen.nms.interfaces.FakePlayer;
import com.denizenscript.denizen.npc.traits.MirrorTrait;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityTag implements ObjectTag, Adjustable, EntityFormObject {

    // <--[language]
    // @name EntityTag
    // @group Object System
    // @description
    // A EntityTag represents a spawned entity, or a generic entity type.
    //
    // Note that players and NPCs are valid dEntities, but are generally represented by the more specific
    // PlayerTag and NPCTag objects.
    //
    // For format info, see <@link language e@>
    //
    // -->

    // <--[language]
    // @name e@
    // @group Object Fetcher System
    // @description
    // e@ refers to the 'object identifier' of a EntityTag. The 'e@' is notation for Denizen's Object
    // Fetcher. The constructor for a EntityTag is a spawned entity's UUID, or an entity type.
    // For example, 'e@zombie'.
    //
    // For general info, see <@link language EntityTag>
    //
    // -->

    /////////////////////
    //   STATIC METHODS
    /////////////////

    // List a mechanism here if it can be safely run before spawn.
    public static HashSet<String> earlyValidMechanisms = new HashSet<>(Arrays.asList(
            "max_health", "health_data", "health"
    ));

    private static final Map<UUID, Entity> rememberedEntities = new HashMap<>();

    public static void rememberEntity(Entity entity) {
        if (entity == null) {
            return;
        }
        rememberedEntities.put(entity.getUniqueId(), entity);
    }

    public static void forgetEntity(Entity entity) {
        if (entity == null) {
            return;
        }
        rememberedEntities.remove(entity.getUniqueId());
    }

    public static boolean isNPC(Entity entity) {
        return entity != null && entity.hasMetadata("NPC") && entity.getMetadata("NPC").get(0).asBoolean();
    }

    public static boolean isCitizensNPC(Entity entity) {
        return entity != null && Depends.citizens != null && CitizensAPI.hasImplementation() && CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    public static NPCTag getNPCFrom(Entity entity) {
        if (isCitizensNPC(entity)) {
            return NPCTag.fromEntity(entity);
        }
        else {
            return null;
        }
    }

    public static boolean isPlayer(Entity entity) {
        return entity != null && entity instanceof Player && !isNPC(entity);
    }

    public static PlayerTag getPlayerFrom(Entity entity) {
        if (isPlayer(entity)) {
            return PlayerTag.mirrorBukkitPlayer((Player) entity);
        }
        else {
            return null;
        }
    }

    public ItemTag getItemInHand() {
        if (isLivingEntity() && getLivingEntity().getEquipment() != null) {
            ItemStack its = getLivingEntity().getEquipment().getItemInHand();
            if (its == null) {
                return null;
            }
            return new ItemTag(its.clone());
        }
        return null;
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static EntityTag getEntityFor(ObjectTag object, TagContext context) {
        if (object instanceof EntityTag) {
            return (EntityTag) object;
        }
        else if (object instanceof PlayerTag && ((PlayerTag) object).isOnline()) {
            return new EntityTag(((PlayerTag) object).getPlayerEntity());
        }
        else if (object instanceof NPCTag) {
            return new EntityTag((NPCTag) object);
        }
        else {
            return valueOf(object.toString(), context);
        }
    }

    public static EntityTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("e")
    public static EntityTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }

        Matcher m;

        ///////
        // Handle objects with properties through the object fetcher
        m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(EntityTag.class, string, context);
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

            return new EntityTag(DenizenEntityType.getByName(randomType.name()), "RANDOM");
        }

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.

        m = entity_by_id.matcher(string);

        if (m.matches()) {

            String entityGroup = m.group(1).toUpperCase();

            // NPC entity
            if (entityGroup.matches("N@")) {

                NPCTag npc = NPCTag.valueOf(string);

                if (npc != null) {
                    if (npc.isSpawned()) {
                        return new EntityTag(npc);
                    }
                    else {
                        if (context != null && context.debug) {
                            Debug.echoDebug(context.entry, "NPC '" + string + "' is not spawned, errors may follow!");
                        }
                        return new EntityTag(npc);
                    }
                }
                else {
                    Debug.echoError("NPC '" + string
                            + "' does not exist!");
                }
            }

            // Player entity
            else if (entityGroup.matches("P@")) {
                LivingEntity returnable = PlayerTag.valueOf(m.group(2)).getPlayerEntity();

                if (returnable != null) {
                    return new EntityTag(returnable);
                }
                else if (context == null || context.debug) {
                    Debug.echoError("Invalid Player! '" + m.group(2)
                            + "' could not be found. Has the player logged off?");
                }
            }

            // Assume entity
            else {
                try {
                    UUID entityID = UUID.fromString(m.group(2));
                    Entity entity = getEntityForID(entityID);
                    if (entity != null) {
                        return new EntityTag(entity);
                    }
                    return null;
                }
                catch (Exception ex) {
                    // DO NOTHING
                }

                // else if (isSaved(m.group(2)))
                //     return getSaved(m.group(2));
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

            // Handle custom DenizenEntityTypes
            if (DenizenEntityType.isRegistered(m.group(1))) {
                return new EntityTag(DenizenEntityType.getByName(m.group(1)), data1, data2);
            }
        }

        try {
            UUID entityID = UUID.fromString(string);
            Entity entity = getEntityForID(entityID);
            if (entity != null) {
                return new EntityTag(entity);
            }
            return null;
        }
        catch (Exception ex) {
            // DO NOTHING
        }

        if (context == null || context.debug) {
            Debug.log("valueOf EntityTag returning null: " + string);
        }

        return null;
    }

    public static Entity getEntityForID(UUID id) {
        if (rememberedEntities.containsKey(id)) {
            return rememberedEntities.get(id);
        }
        for (World world : Bukkit.getWorlds()) {
            Entity entity = NMSHandler.getEntityHelper().getEntity(world, id);
            if (entity != null) {
                return entity;
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
        if (m.matches()) {
            return true;
        }

        // No longer picky about e@.. let's remove it from the arg
        arg = arg.replace("e@", "").toUpperCase();

        // Allow 'random'
        if (arg.equals("RANDOM")) {
            return true;
        }

        // Allow any entity script
        if (ScriptRegistry.containsScript(arg, EntityScriptContainer.class)) {
            return true;
        }

        // Use regex to make some matcher groups
        m = entity_with_data.matcher(arg);
        if (m.matches()) {
            // Check first word with a valid entity_type (other groups are datas used in constructors)
            if (DenizenEntityType.isRegistered(m.group(1))) {
                return true;
            }
        }

        // No luck otherwise!
        return false;
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    public EntityTag(Entity entity) {
        if (entity != null) {
            this.entity = entity;
            entityScript = EntityScriptHelper.getEntityScript(entity);
            this.uuid = entity.getUniqueId();
            this.entity_type = DenizenEntityType.getByEntity(entity);
            if (isCitizensNPC(entity)) {
                this.npc = getNPCFrom(entity);
            }
        }
        else {
            Debug.echoError("Entity referenced is null!");
        }
    }

    @Deprecated
    public EntityTag(EntityType entityType) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = DenizenEntityType.getByName(entityType.name());
        }
        else {
            Debug.echoError("Entity_type referenced is null!");
        }
    }

    @Deprecated
    public EntityTag(EntityType entityType, ArrayList<Mechanism> mechanisms) {
        this(entityType);
        this.mechanisms = mechanisms;
    }

    @Deprecated
    public EntityTag(EntityType entityType, String data1) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = DenizenEntityType.getByName(entityType.name());
            this.data1 = data1;
        }
        else {
            Debug.echoError("Entity_type referenced is null!");
        }
    }

    @Deprecated
    public EntityTag(EntityType entityType, String data1, String data2) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = DenizenEntityType.getByName(entityType.name());
            this.data1 = data1;
            this.data2 = data2;
        }
        else {
            Debug.echoError("Entity_type referenced is null!");
        }
    }

    public EntityTag(DenizenEntityType entityType) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
        }
        else {
            Debug.echoError("DenizenEntityType referenced is null!");
        }
    }

    public EntityTag(DenizenEntityType entityType, ArrayList<Mechanism> mechanisms) {
        this(entityType);
        this.mechanisms = mechanisms;
    }

    public EntityTag(DenizenEntityType entityType, String data1) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
            this.data1 = data1;
        }
        else {
            Debug.echoError("DenizenEntityType referenced is null!");
        }
    }

    public EntityTag(DenizenEntityType entityType, String data1, String data2) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
            this.data1 = data1;
            this.data2 = data2;
        }
        else {
            Debug.echoError("DenizenEntityType referenced is null!");
        }
    }

    public EntityTag(NPCTag npc) {
        if (Depends.citizens == null) {
            return;
        }
        if (npc != null) {
            this.npc = npc;

            if (npc.isSpawned()) {
                this.entity = npc.getEntity();
                this.entity_type = DenizenEntityType.getByName(npc.getEntityType().name());
                this.uuid = entity.getUniqueId();
            }
        }
        else {
            Debug.echoError("NPC referenced is null!");
        }

    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private Entity entity = null;
    private DenizenEntityType entity_type = null;
    private String data1 = null;
    private String data2 = null;
    private DespawnedEntity despawned_entity = null;
    private NPCTag npc = null;
    private UUID uuid = null;
    private String entityScript = null;

    public DenizenEntityType getEntityType() {
        return entity_type;
    }

    public EntityType getBukkitEntityType() {
        return entity_type.getBukkitEntityType();
    }

    public void setEntityScript(String entityScript) {
        this.entityScript = entityScript;
    }

    public String getEntityScript() {
        return entityScript;
    }

    /**
     * Returns the unique UUID of this entity
     *
     * @return The UUID
     */

    public UUID getUUID() {
        return uuid;
    }

    public String getSaveName() {
        String baseID = uuid.toString().toUpperCase().replace("-", "");
        return baseID.substring(0, 2) + "." + baseID;
    }

    @Override
    public EntityTag getDenizenEntity() {
        return this;
    }

    /**
     * Get the ObjectTag that most accurately describes this entity,
     * useful for automatically saving dEntities to contexts as
     * NPCTags and PlayerTags
     *
     * @return The ObjectTag
     */

    public EntityFormObject getDenizenObject() {

        if (entity == null && npc == null) {
            return null;
        }

        if (isCitizensNPC()) {
            return getDenizenNPC();
        }
        else if (isPlayer()) {
            return new PlayerTag(getPlayer());
        }
        else {
            return this;
        }
    }

    /**
     * Get the Bukkit entity corresponding to this EntityTag
     *
     * @return the underlying Bukkit entity
     */

    public Entity getBukkitEntity() {
        return entity;
    }

    /**
     * Get the living entity corresponding to this EntityTag
     *
     * @return The living entity
     */

    public LivingEntity getLivingEntity() {
        if (entity instanceof LivingEntity) {
            return (LivingEntity) entity;
        }
        else {
            return null;
        }
    }

    /**
     * Check whether this EntityTag is a living entity
     *
     * @return true or false
     */

    public boolean isLivingEntity() {
        return (entity instanceof LivingEntity);
    }

    public boolean hasInventory() {
        return getBukkitEntity() instanceof InventoryHolder || isCitizensNPC();
    }

    /**
     * Get the NPCTag corresponding to this EntityTag
     *
     * @return The NPCTag
     */

    public NPCTag getDenizenNPC() {
        if (npc != null) {
            return npc;
        }
        else {
            return getNPCFrom(entity);
        }
    }

    /**
     * Check whether this EntityTag is an NPC
     *
     * @return true or false
     */

    public boolean isNPC() {
        return npc != null || isNPC(entity);
    }

    public boolean isCitizensNPC() {
        return npc != null || isCitizensNPC(entity);
    }

    /**
     * Get the Player corresponding to this EntityTag
     *
     * @return The Player
     */

    public Player getPlayer() {
        if (isPlayer()) {
            return (Player) entity;
        }
        else {
            return null;
        }
    }

    /**
     * Get the PlayerTag corresponding to this EntityTag
     *
     * @return The PlayerTag
     */

    public PlayerTag getDenizenPlayer() {
        if (isPlayer()) {
            return new PlayerTag(getPlayer());
        }
        else {
            return null;
        }
    }

    /**
     * Check whether this EntityTag is a Player
     *
     * @return true or false
     */

    public boolean isPlayer() {
        return entity instanceof Player && !isNPC();
    }

    /**
     * Get this EntityTag as a Projectile
     *
     * @return The Projectile
     */

    public Projectile getProjectile() {

        return (Projectile) entity;
    }

    /**
     * Check whether this EntityTag is a Projectile
     *
     * @return true or false
     */

    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    /**
     * Get this Projectile entity's shooter
     *
     * @return A EntityTag of the shooter
     */

    public EntityTag getShooter() {
        if (hasShooter()) {
            return new EntityTag((LivingEntity) getProjectile().getShooter());
        }
        else {
            return null;
        }
    }

    /**
     * Set this Projectile entity's shooter
     */

    public void setShooter(EntityTag shooter) {
        if (isProjectile() && shooter.isLivingEntity()) {
            getProjectile().setShooter(shooter.getLivingEntity());
        }
    }

    /**
     * Check whether this entity has a shooter.
     *
     * @return true or false
     */

    public boolean hasShooter() {
        return isProjectile() && getProjectile().getShooter() != null && getProjectile().getShooter() instanceof LivingEntity;
        // TODO: Handle other shooter source thingy types
    }

    public Inventory getBukkitInventory() {
        if (hasInventory()) {
            if (!isCitizensNPC()) {
                return ((InventoryHolder) getBukkitEntity()).getInventory();
            }
        }
        return null;
    }

    /**
     * Returns this entity's InventoryTag.
     *
     * @return the entity's InventoryTag
     */

    public InventoryTag getInventory() {
        return hasInventory() ? isCitizensNPC() ? getDenizenNPC().getDenizenInventory()
                : InventoryTag.mirrorBukkitInventory(getBukkitInventory()) : null;
    }

    public String getName() {
        if (isCitizensNPC()) {
            return getDenizenNPC().getCitizen().getName();
        }
        if (entity instanceof FakePlayer) {
            return ((FakePlayer) entity).getFullName();
        }
        if (entity instanceof Player) {
            return entity.getName();
        }
        String customName = entity.getCustomName();
        if (customName != null) {
            return customName;
        }
        return entity_type.getName();
    }

    /**
     * Returns this entity's equipment
     *
     * @return the entity's equipment
     */

    public ListTag getEquipment() {
        ItemStack[] equipment = getLivingEntity().getEquipment().getArmorContents();
        ListTag equipmentList = new ListTag();
        for (ItemStack item : equipment) {
            equipmentList.add(new ItemTag(item).identify());
        }
        return equipmentList;
    }

    /**
     * Whether this entity identifies as a generic
     * entity type, for instance "e@cow", instead of
     * a spawned entity
     *
     * @return true or false
     */

    public boolean isGeneric() {
        return !isUnique();
    }

    /**
     * Get the location of this entity
     *
     * @return The Location
     */

    public LocationTag getLocation() {

        if (entity != null) {
            return new LocationTag(entity.getLocation());
        }

        return null;
    }

    /**
     * Get the eye location of this entity
     *
     * @return The location
     */

    public LocationTag getEyeLocation() {

        if (isPlayer()) {
            return new LocationTag(getPlayer().getEyeLocation());
        }
        else if (!isGeneric() && isLivingEntity()) {
            return new LocationTag(getLivingEntity().getEyeLocation());
        }
        else if (!isGeneric()) {
            return new LocationTag(getBukkitEntity().getLocation());
        }

        return null;
    }

    public Location getTargetBlockSafe(Set<Material> mats, int range) {
        try {
            NMSHandler.getChunkHelper().changeChunkServerThread(getWorld());
            return getLivingEntity().getTargetBlock(mats, range).getLocation();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(getWorld());
        }
    }

    /**
     * Gets the velocity of this entity
     *
     * @return The velocity's vector
     */

    public Vector getVelocity() {

        if (!isGeneric()) {
            return entity.getVelocity();
        }
        return null;
    }

    /**
     * Sets the velocity of this entity
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
     * @return The entity's world
     */

    public World getWorld() {

        if (!isGeneric()) {
            return entity.getWorld();
        }
        return null;
    }

    public void spawnAt(Location location) {
        // If the entity is already spawned, teleport it.
        if (isCitizensNPC()) {
            if (getDenizenNPC().getCitizen().isSpawned()) {
                getDenizenNPC().getCitizen().teleport(location, TeleportCause.PLUGIN);
            }
            else {
                getDenizenNPC().getCitizen().spawn(location);
                entity = getDenizenNPC().getCitizen().getEntity();
                uuid = getDenizenNPC().getCitizen().getEntity().getUniqueId();
            }
        }
        else if (entity != null && isUnique()) {
            entity.teleport(location);
            if (entity.getWorld().equals(location.getWorld())) { // Force the teleport through (for things like mounts)
                NMSHandler.getEntityHelper().teleport(entity, location.toVector());
            }
        }
        else {
            if (entity_type != null) {
                if (despawned_entity != null) {
                    // If entity had a custom_script, use the script to rebuild the base entity.
                    if (despawned_entity.custom_script != null) {
                        // TODO: Build entity from custom script
                    }
                    // Else, use the entity_type specified/remembered
                    else {
                        entity = entity_type.spawnNewEntity(location, mechanisms, entityScript);
                    }

                    getLivingEntity().teleport(location);
                    getLivingEntity().getEquipment().setArmorContents(despawned_entity.equipment);
                    getLivingEntity().setHealth(despawned_entity.health);

                    despawned_entity = null;
                }
                else {

                    org.bukkit.entity.Entity ent;

                    if (entity_type.getName().equals("PLAYER")) {
                        if (Depends.citizens == null) {
                            Debug.echoError("Cannot spawn entity of type PLAYER!");
                            return;
                        }
                        else {
                            NPCTag npc = new NPCTag(net.citizensnpcs.api.CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, data1));
                            npc.getCitizen().spawn(location);
                            entity = npc.getEntity();
                            uuid = entity.getUniqueId();
                        }
                    }
                    else if (entity_type.getName().equals("FALLING_BLOCK")) {

                        Material material = null;

                        if (data1 != null && MaterialTag.matches(data1)) {

                            material = MaterialTag.valueOf(data1).getMaterial();

                            // If we did not get a block with "RANDOM", or we got
                            // air or portals, keep trying
                            while (data1.equalsIgnoreCase("RANDOM") &&
                                    ((!material.isBlock()) ||
                                            material == Material.AIR ||
                                            material == MaterialCompat.NETHER_PORTAL ||
                                            material == MaterialCompat.END_PORTAL)) {

                                material = MaterialTag.valueOf(data1).getMaterial();
                            }
                        }

                        // If material is null or not a block, default to SAND
                        if (material == null || (!material.isBlock())) {
                            material = Material.SAND;
                        }

                        byte materialData = 0;

                        // Get special data value from data2 if it is a valid integer
                        if (data2 != null && ArgumentHelper.matchesInteger(data2)) {

                            materialData = (byte) ArgumentHelper.getIntegerFrom(data2);
                        }

                        // This is currently the only way to spawn a falling block
                        ent = location.getWorld().spawnFallingBlock(location, material, materialData);
                        entity = ent;
                        uuid = entity.getUniqueId();
                    }
                    else {

                        ent = entity_type.spawnNewEntity(location, mechanisms, entityScript);
                        entity = ent;
                        if (entity == null) {
                            if (Debug.verbose) {
                                Debug.echoError("Failed to spawn entity of type " + entity_type.getName());
                            }
                            return;
                        }
                        uuid = entity.getUniqueId();
                        if (entityScript != null) {
                            EntityScriptHelper.setEntityScript(entity, entityScript);
                        }
                    }
                }
            }
            else {
                Debug.echoError("Cannot spawn a null EntityTag!");
            }

            if (!isUnique()) {
                Debug.echoError("Error spawning entity - bad entity type, blocked by another plugin, or tried to spawn in an unloaded chunk?");
                return;
            }

            for (Mechanism mechanism : mechanisms) {
                safeAdjust(new Mechanism(new ElementTag(mechanism.getName()), mechanism.getValue(), mechanism.context));
            }
            mechanisms.clear();
        }
    }

    public void despawn() {
        despawned_entity = new DespawnedEntity(this);
        getLivingEntity().remove();
    }

    public void respawn() {
        if (despawned_entity != null) {
            spawnAt(despawned_entity.location);
        }
        else if (entity == null) {
            Debug.echoError("Cannot respawn a null EntityTag!");
        }

    }

    public boolean isSpawned() {
        return entity != null && isValid();
    }

    public boolean isValid() {
        return entity != null && entity.isValid();
    }

    public void remove() {
        EntityScriptHelper.unlinkEntity(entity);
        entity.remove();
    }

    public void teleport(Location location) {
        if (isCitizensNPC()) {
            getDenizenNPC().getCitizen().teleport(location, TeleportCause.PLUGIN);
        }
        else {
            entity.teleport(location);
        }
    }

    /**
     * Make this entity target another living entity, attempting both
     * old entity AI and new entity AI targeting methods
     *
     * @param target The LivingEntity target
     */

    public void target(LivingEntity target) {

        if (!isSpawned() || !(entity instanceof Creature)) {
            Debug.echoError(identify() + " is not a valid creature entity!");
            return;
        }

        NMSHandler.getEntityHelper().setTarget((Creature) entity, target);
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

        public DespawnedEntity(EntityTag entity) {
            if (entity != null) {
                // Save some important info to rebuild the entity
                health = entity.getLivingEntity().getHealth();
                location = entity.getLivingEntity().getLocation();
                equipment = entity.getLivingEntity().getEquipment().getArmorContents();

                if (CustomNBT.hasCustomNBT(entity.getLivingEntity(), "denizen-script-id")) {
                    custom_script = CustomNBT.getCustomNBT(entity.getLivingEntity(), "denizen-script-id");
                }
            }
        }
    }

    public int comparesTo(EntityTag entity) {
        // Never matches a null
        if (entity == null) {
            return 0;
        }

        // If provided is unique, and both are the same unique entity, return 1.
        if (entity.isUnique() && entity.identify().equals(identify())) {
            return 1;
        }

        // If provided isn't unique...
        if (!entity.isUnique()) {
            // Return 1 if this object isn't unique either, but matches
            if (!isUnique() && entity.identify().equals(identify())) {
                return 1;
            }
            // Return 1 if the provided object isn't unique, but whose entity_type
            // matches this object, even if this object is unique.
            if (entity_type == entity.entity_type) {
                return 1;
            }
        }

        return 0;
    }

    public boolean comparedTo(String compare) {
        compare = CoreUtilities.toLowerCase(compare);
        if (compare.equals("entity")) {
            return true;
        }
        else if (compare.equals("player")) {
            return isPlayer();
        }
        else if (compare.equals("npc")) {
            return isCitizensNPC() || isNPC();
        }
        else if (getEntityScript() != null && compare.equals(CoreUtilities.toLowerCase(getEntityScript()))) {
            return true;
        }
        else if (compare.equals(getEntityType().getLowercaseName())) {
            return true;
        }
        return false;
    }


    /////////////////////
    //  ObjectTag Methods
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
    public EntityTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debuggable() {
        if (npc != null) {
            return npc.debuggable();
        }
        if (entity != null) {
            if (isPlayer()) {
                return getDenizenPlayer().debuggable();
            }
            if (isSpawned() || rememberedEntities.containsKey(entity.getUniqueId())) {
                return "e@ " + entity.getUniqueId().toString() + "<GR>(" + entity.getType().name() + "/" + entity.getName() + ")";
            }
        }
        if (entityScript != null) {
            return "e@" + entityScript;
        }
        if (entity_type != null) {
            return identify();
        }
        return "null";
    }

    @Override
    public String identify() {

        // Check if entity is an NPC
        if (npc != null) {
            return "n@" + npc.getId();
        }

        // Check if entity is a Player or is spawned
        if (entity != null) {
            if (isPlayer()) {
                return "p@" + getPlayer().getUniqueId();
            }

            // TODO:
            // Check if entity is a 'notable entity'
            // if (isSaved(this))
            //    return "e@" + getSaved(this);

            else if (isSpawned() || rememberedEntities.containsKey(entity.getUniqueId())) {
                return "e@" + entity.getUniqueId().toString();
            }
        }

        // Try to identify as an entity script
        if (entityScript != null) {
            return "e@" + entityScript;
        }

        // Check if an entity_type is available
        if (entity_type != null) {
            // Build the pseudo-property-string, if any
            StringBuilder properties = new StringBuilder();
            for (Mechanism mechanism : mechanisms) {
                properties.append(mechanism.getName()).append("=").append(mechanism.getValue().asString().replace(';', (char) 0x2011)).append(";");
            }
            String propertyOutput = "";
            if (properties.length() > 0) {
                propertyOutput = "[" + properties.substring(0, properties.length() - 1) + "]";
            }
            return "e@" + entity_type.getLowercaseName() + propertyOutput;
        }

        return "null";
    }


    @Override
    public String identifySimple() {

        // Check if entity is an NPC
        if (npc != null && npc.isValid()) {
            return "n@" + npc.getId();
        }

        if (isPlayer()) {
            return "p@" + getPlayer().getName();
        }

        // Try to identify as an entity script
        if (entityScript != null) {
            return "e@" + entityScript;
        }

        // Check if an entity_type is available
        if (entity_type != null) {
            return "e@" + entity_type.getLowercaseName();
        }

        return "null";
    }


    public String identifyType() {
        if (isCitizensNPC()) {
            return "npc";
        }
        else if (isPlayer()) {
            return "player";
        }
        else {
            return "e@" + entity_type.getName();
        }
    }

    public String identifySimpleType() {
        if (isCitizensNPC()) {
            return "npc";
        }
        else if (isPlayer()) {
            return "player";
        }
        else {
            return entity_type.getLowercaseName();
        }
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public boolean isUnique() {
        return isPlayer() || isCitizensNPC() || isSpawned() || isLivingEntity()
                || (entity != null && rememberedEntities.containsKey(entity.getUniqueId()));  // || isSaved()
    }

    public boolean matchesEntity(String ent) {
        if (ent.equalsIgnoreCase("entity")) {
            return true;
        }
        if (ent.equalsIgnoreCase("npc")) {
            return this.isCitizensNPC();
        }
        if (ent.equalsIgnoreCase("player")) {
            return this.isPlayer();
        }
        if (ent.equalsIgnoreCase("vehicle")) {
            return entity instanceof Vehicle;
        }
        if (ent.equalsIgnoreCase("projectile")) {
            return entity instanceof Projectile;
        }
        if (ent.equalsIgnoreCase("hanging")) {
            return entity instanceof Hanging;
        }
        if (ent.equalsIgnoreCase(getName())) {
            return true;
        }
        if (ent.equalsIgnoreCase(entity_type.getLowercaseName())) {
            return true;
        }
        if (entity != null && getEntityScript() != null) {
            return ent.equalsIgnoreCase(getEntityScript());
        }
        if (uuid != null && uuid.toString().equals(ent)) {
            return true;
        }
        return false;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (entity == null && entity_type == null) {
            if (npc != null) {
                return new ElementTag(identify()).getAttribute(attribute);
            }
            Debug.echoError("dEntity has returned null.");
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Entity' for EntityTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new ElementTag("Entity").getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   UNSPAWNED ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.entity_type>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the type of the entity.
        // -->
        if (attribute.startsWith("entity_type")) {
            return new ElementTag(entity_type.getName()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_spawned>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is spawned.
        // -->
        if (attribute.startsWith("is_spawned")) {
            return new ElementTag(isSpawned())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.eid>
        // @returns ElementTag(Number)
        // @group data
        // @description
        // Returns the entity's temporary server entity ID.
        // -->
        if (attribute.startsWith("eid")) {
            return new ElementTag(entity.getEntityId())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.uuid>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the permanent unique ID of the entity.
        // Works with offline players.
        // -->
        if (attribute.startsWith("uuid")) {
            return new ElementTag(getUUID().toString())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.scriptname>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the name of the entity script that spawned this entity, if any.
        // -->
        if (attribute.startsWith("scriptname")) {
            if (entityScript == null) {
                return null;
            }
            return new ElementTag(entityScript)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the entity has the specified flag, otherwise returns false.
        // -->
        if (attribute.startsWith("has_flag")) {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                return null;
            }
            if (isPlayer() || isCitizensNPC()) {
                Debug.echoError("Reading flag for PLAYER or NPC as if it were an ENTITY!");
                return null;
            }
            return new ElementTag(FlagManager.entityHasFlag(this, flag_name)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.flag[<flag_name>]>
        // @returns Flag ListTag
        // @description
        // Returns the specified flag from the entity.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                return null;
            }
            if (isPlayer() || isCitizensNPC()) {
                Debug.echoError("Reading flag for PLAYER or NPC as if it were an ENTITY!");
                return null;
            }
            if (attribute.getAttribute(2).equalsIgnoreCase("is_expired")
                    || attribute.startsWith("isexpired")) {
                return new ElementTag(!FlagManager.entityHasFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(2));
            }
            if (attribute.getAttribute(2).equalsIgnoreCase("size") && !FlagManager.entityHasFlag(this, flag_name)) {
                return new ElementTag(0).getAttribute(attribute.fulfill(2));
            }
            if (FlagManager.entityHasFlag(this, flag_name)) {
                FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager().getEntityFlag(this, flag_name);
                return new ListTag(flag.toString(), true, flag.values()).getAttribute(attribute.fulfill(1));
            }
            return new ElementTag(identify()).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <EntityTag.list_flags[(regex:)<search>]>
        // @returns ListTag
        // @description
        // Returns a list of an entity's flag names, with an optional search for
        // names containing a certain pattern.
        // -->
        if (attribute.startsWith("list_flags")) {
            ListTag allFlags = new ListTag(DenizenAPI.getCurrentInstance().flagManager().listEntityFlags(this));
            ListTag searchFlags = null;
            if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                searchFlags = new ListTag();
                String search = attribute.getContext(1);
                if (search.startsWith("regex:")) {
                    try {
                        Pattern pattern = Pattern.compile(search.substring(6), Pattern.CASE_INSENSITIVE);
                        for (String flag : allFlags) {
                            if (pattern.matcher(flag).matches()) {
                                searchFlags.add(flag);
                            }
                        }
                    }
                    catch (Exception e) {
                        Debug.echoError(e);
                    }
                }
                else {
                    search = CoreUtilities.toLowerCase(search);
                    for (String flag : allFlags) {
                        if (CoreUtilities.toLowerCase(flag).contains(search)) {
                            searchFlags.add(flag);
                        }
                    }
                }
                DenizenAPI.getCurrentInstance().flagManager().shrinkEntityFlags(this, searchFlags);
            }
            else {
                DenizenAPI.getCurrentInstance().flagManager().shrinkEntityFlags(this, allFlags);
            }
            return searchFlags == null ? allFlags.getAttribute(attribute.fulfill(1))
                    : searchFlags.getAttribute(attribute.fulfill(1));
        }

        if (entity == null) {
            return new ElementTag(identify()).getAttribute(attribute);
        }
        // Only spawned entities past this point!


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.custom_id>
        // @returns ScriptTag/Element
        // @group data
        // @description
        // If the entity has a script ID, returns the ScriptTag of that ID.
        // Otherwise, returns the name of the entity type.
        // -->
        if (attribute.startsWith("custom_id")) {
            if (CustomNBT.hasCustomNBT(getLivingEntity(), "denizen-script-id")) {
                return new ScriptTag(CustomNBT.getCustomNBT(getLivingEntity(), "denizen-script-id"))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return new ElementTag(entity.getType().name())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.name>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the name of the entity.
        // This can be a player name, an NPC name, a custom_name, or the entity type.
        // Works with offline players.
        // -->
        if (attribute.startsWith("name")) {
            return new ElementTag(getName()).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   INVENTORY ATTRIBUTES
        /////////////////


        // <--[tag]
        // @attribute <EntityTag.saddle>
        // @returns ItemTag
        // @group inventory
        // @description
        // If the entity is a horse or pig, returns the saddle as a ItemTag, or air if none.
        // -->
        if (attribute.startsWith("saddle")) {
            if (getLivingEntity().getType() == EntityType.HORSE) {
                return new ItemTag(((Horse) getLivingEntity()).getInventory().getSaddle())
                        .getAttribute(attribute.fulfill(1));
            }
            else if (getLivingEntity().getType() == EntityType.PIG) {
                return new ItemTag(((Pig) getLivingEntity()).hasSaddle() ? Material.SADDLE : Material.AIR)
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.horse_armor>
        // @returns ItemTag
        // @group inventory
        // @description
        // If the entity is a horse, returns the item equipped as the horses armor, or air if none.
        // -->
        if (attribute.startsWith("horse_armor") || attribute.startsWith("horse_armour")) {
            if (getLivingEntity().getType() == EntityType.HORSE) {
                return new ItemTag(((Horse) getLivingEntity()).getInventory().getArmor())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.has_saddle>
        // @returns ElementTag(Boolean)
        // @group inventory
        // @description
        // If the entity s a pig or horse, returns whether it has a saddle equipped.
        // -->
        if (attribute.startsWith("has_saddle")) {
            if (getLivingEntity().getType() == EntityType.HORSE) {
                return new ElementTag(((Horse) getLivingEntity()).getInventory().getSaddle().getType() == Material.SADDLE)
                        .getAttribute(attribute.fulfill(1));
            }
            else if (getLivingEntity().getType() == EntityType.PIG) {
                return new ElementTag(((Pig) getLivingEntity()).hasSaddle())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.item_in_hand>
        // @returns ItemTag
        // @group inventory
        // @description
        // Returns the item the entity is holding, or air if none.
        // -->
        if (attribute.startsWith("item_in_hand") ||
                attribute.startsWith("iteminhand")) {
            return new ItemTag(NMSHandler.getEntityHelper().getItemInHand(getLivingEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.item_in_offhand>
        // @returns ItemTag
        // @group inventory
        // @description
        // Returns the item the entity is holding in their off hand, or air if none.
        // -->
        if (attribute.startsWith("item_in_offhand") ||
                attribute.startsWith("iteminoffhand")) {
            return new ItemTag(NMSHandler.getEntityHelper().getItemInOffHand(getLivingEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_trading>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the villager entity is trading.
        // -->
        if (attribute.startsWith("is_trading")) {
            if (entity instanceof Merchant) {
                return new ElementTag(((Merchant) entity).isTrading()).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.trading_with>
        // @returns PlayerTag
        // @description
        // Returns the player who is trading with the villager entity, or null if it is not trading.
        // -->
        if (attribute.startsWith("trading_with")) {
            if (entity instanceof Merchant
                    && ((Merchant) entity).getTrader() != null) {
                return new EntityTag(((Merchant) entity).getTrader()).getAttribute(attribute.fulfill(1));
            }
        }


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.map_trace>
        // @returns LocationTag
        // @group location
        // @description
        // Returns a 2D location indicating where on the map the entity's looking at.
        // Each coordinate is in the range of 0 to 128.
        // -->
        if (attribute.startsWith("map_trace")) {
            EntityHelper.MapTraceResult mtr = NMSHandler.getEntityHelper().mapTrace(getLivingEntity(), 200);
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
                return new LocationTag(null, Math.round(x), Math.round(y)).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.can_see[<entity>]>
        // @returns ElementTag(Boolean)
        // @group location
        // @description
        // Returns whether the entity can see the specified other entity (has an uninterrupted line-of-sight).
        // -->
        if (attribute.startsWith("can_see")) {
            if (isLivingEntity() && attribute.hasContext(1) && EntityTag.matches(attribute.getContext(1))) {
                EntityTag toEntity = EntityTag.valueOf(attribute.getContext(1));
                if (toEntity != null && toEntity.isSpawned()) {
                    return new ElementTag(getLivingEntity().hasLineOfSight(toEntity.getBukkitEntity())).getAttribute(attribute.fulfill(1));
                }
            }
        }

        // <--[tag]
        // @attribute <EntityTag.eye_location>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the entity's eyes.
        // -->
        if (attribute.startsWith("eye_location")) {
            return new LocationTag(getEyeLocation())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.eye_height>
        // @returns ElementTag(Boolean)
        // @group location
        // @description
        // Returns the height of the entity's eyes above its location.
        // -->
        if (attribute.startsWith("eye_height")) {
            if (isLivingEntity()) {
                return new ElementTag(getLivingEntity().getEyeHeight())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.location.cursor_on[<range>]>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the block the entity is looking at.
        // Optionally, specify a maximum range to find the location from.
        // -->
        // <--[tag]
        // @attribute <EntityTag.location.cursor_on[<range>].ignore[<material>|...]>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the block the entity is looking at, ignoring
        // the specified materials along the way. Note that air is always an
        // ignored material.
        // Optionally, specify a maximum range to find the location from.
        // -->
        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) {
                range = 50;
            }
            Set<Material> set = new HashSet<>();
            set.add(Material.AIR);
            attribute = attribute.fulfill(2);
            if (attribute.startsWith("ignore") && attribute.hasContext(1)) {
                List<MaterialTag> ignoreList = ListTag.valueOf(attribute.getContext(1)).filter(MaterialTag.class, attribute.context);
                for (MaterialTag material : ignoreList) {
                    set.add(material.getMaterial());
                }
                attribute = attribute.fulfill(1);
            }
            return new LocationTag(getTargetBlockSafe(set, range)).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <EntityTag.location.standing_on>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of what the entity is standing on.
        // Works with offline players.
        // -->
        if (attribute.startsWith("location.standing_on")) {
            return new LocationTag(entity.getLocation().clone().add(0, -0.5f, 0))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.location>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the entity.
        // Works with offline players.
        // -->
        if (attribute.startsWith("location")) {
            return new LocationTag(entity.getLocation())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.body_yaw>
        // @returns ElementTag(Decimal)
        // @group location
        // @description
        // Returns the entity's body yaw (separate from head yaw).
        // -->
        if (attribute.startsWith("body_yaw")) {
            return new ElementTag(NMSHandler.getEntityHelper().getBaseYaw(entity))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.velocity>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the movement velocity of the entity.
        // Note: Does not accurately calculate player clientside movement velocity.
        // -->
        if (attribute.startsWith("velocity")) {
            return new LocationTag(entity.getVelocity().toLocation(entity.getWorld()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.world>
        // @returns WorldTag
        // @group location
        // @description
        // Returns the world the entity is in. Works with offline players.
        // -->
        if (attribute.startsWith("world")) {
            return new WorldTag(entity.getWorld())
                    .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.can_pickup_items>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity can pick up items.
        // -->
        if (attribute.startsWith("can_pickup_items")) {
            if (isLivingEntity()) {
                return new ElementTag(getLivingEntity().getCanPickupItems())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.fallingblock_material>
        // @returns MaterialTag
        // @group attributes
        // @description
        // Returns the material of a fallingblock-type entity.
        // -->
        if (attribute.startsWith("fallingblock_material") && entity instanceof FallingBlock) {
            return new MaterialTag(NMSHandler.getEntityHelper().getBlockDataFor((FallingBlock) entity))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.fall_distance>
        // @returns ElementTag(Decimal)
        // @group attributes
        // @description
        // Returns how far the entity has fallen.
        // -->
        if (attribute.startsWith("fall_distance")) {
            return new ElementTag(entity.getFallDistance())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.fire_time>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns the duration for which the entity will remain on fire
        // -->
        if (attribute.startsWith("fire_time")) {
            return new DurationTag(entity.getFireTicks() / 20)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.on_fire>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is currently ablaze or not.
        // -->
        if (attribute.startsWith("on_fire")) {
            return new ElementTag(entity.getFireTicks() > 0).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.leash_holder>
        // @returns EntityTag
        // @group attributes
        // @description
        // Returns the leash holder of entity.
        // -->
        if (attribute.startsWith("leash_holder") || attribute.startsWith("get_leash_holder")) {
            if (isLivingEntity() && getLivingEntity().isLeashed()) {
                return new EntityTag(getLivingEntity().getLeashHolder())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.passengers>
        // @returns ListTag(EntityTag)
        // @group attributes
        // @description
        // Returns a list of the entity's passengers, if any.
        // -->
        if (attribute.startsWith("passengers") || attribute.startsWith("get_passengers")) {
            ArrayList<EntityTag> passengers = new ArrayList<>();
            for (Entity ent : entity.getPassengers()) {
                passengers.add(new EntityTag(ent));
            }
            return new ListTag(passengers).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.passenger>
        // @returns EntityTag
        // @group attributes
        // @description
        // Returns the entity's passenger, if any.
        // -->
        if (attribute.startsWith("passenger") || attribute.startsWith("get_passenger")) {
            if (!entity.isEmpty()) {
                return new EntityTag(entity.getPassenger())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.shooter>
        // @returns EntityTag
        // @group attributes
        // @Mechanism EntityTag.shooter
        // @description
        // Returns the entity's shooter, if any.
        // -->
        if (attribute.startsWith("shooter") ||
                attribute.startsWith("get_shooter")) {
            if (isProjectile() && hasShooter()) {
                return getShooter().getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.left_shoulder>
        // @returns EntityTag
        // @description
        // Returns the entity on the entity's left shoulder.
        // Only applies to player-typed entities.
        // NOTE: The returned entity will not be spawned within the world,
        // so most operations are invalid unless the entity is first spawned in.
        // -->
        if (getLivingEntity() instanceof HumanEntity
                && attribute.startsWith("left_shoulder")) {
            return new EntityTag(((HumanEntity) getLivingEntity()).getShoulderEntityLeft())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.right_shoulder>
        // @returns EntityTag
        // @description
        // Returns the entity on the entity's right shoulder.
        // Only applies to player-typed entities.
        // NOTE: The returned entity will not be spawned within the world,
        // so most operations are invalid unless the entity is first spawned in.
        // -->
        if (getLivingEntity() instanceof HumanEntity
                && attribute.startsWith("right_shoulder")) {
            return new EntityTag(((HumanEntity) getLivingEntity()).getShoulderEntityRight())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.vehicle>
        // @returns EntityTag
        // @group attributes
        // @description
        // If the entity is in a vehicle, returns the vehicle as a EntityTag.
        // -->
        if (attribute.startsWith("vehicle") || attribute.startsWith("get_vehicle")) {
            if (entity.isInsideVehicle()) {
                return new EntityTag(entity.getVehicle())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.can_breed>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the animal entity is capable of mating with another of its kind.
        // -->
        if (attribute.startsWith("can_breed")) {
            return new ElementTag(((Ageable) getLivingEntity()).canBreed())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.breeding>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the animal entity is trying to with another of its kind.
        // -->
        if (attribute.startsWith("breeding") || attribute.startsWith("is_breeding")) {
            return new ElementTag(NMSHandler.getEntityHelper().isBreeding((Animals) getLivingEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.has_passenger>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity has a passenger.
        // -->
        if (attribute.startsWith("has_passenger")) {
            return new ElementTag(!entity.isEmpty())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_empty>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity does not have a passenger.
        // -->
        if (attribute.startsWith("empty") || attribute.startsWith("is_empty")) {
            return new ElementTag(entity.isEmpty())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_inside_vehicle>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is inside a vehicle.
        // -->
        if (attribute.startsWith("inside_vehicle") || attribute.startsWith("is_inside_vehicle")) {
            return new ElementTag(entity.isInsideVehicle())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_leashed>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is leashed.
        // -->
        if (attribute.startsWith("leashed") || attribute.startsWith("is_leashed")) {
            return new ElementTag(isLivingEntity() && getLivingEntity().isLeashed())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_sheared>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether a sheep is sheared.
        // -->
        if (attribute.startsWith("is_sheared") && getBukkitEntity() instanceof Sheep) {
            return new ElementTag(((Sheep) getBukkitEntity()).isSheared())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_on_ground>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is supported by a block.
        // -->
        if (attribute.startsWith("on_ground") || attribute.startsWith("is_on_ground")) {
            return new ElementTag(entity.isOnGround())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_persistent>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity will not be removed completely when far away from players.
        // -->
        if (attribute.startsWith("persistent") || attribute.startsWith("is_persistent")) {
            return new ElementTag(isLivingEntity() && !getLivingEntity().getRemoveWhenFarAway())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_collidable>
        // @returns ElementTag(Boolean)
        // @mechanism collidable
        // @group attributes
        // @description
        // Returns whether the entity is collidable.
        // -->
        if (attribute.startsWith("is_collidable")) {
            return new ElementTag(getLivingEntity().isCollidable())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.killer>
        // @returns PlayerTag
        // @group attributes
        // @description
        // Returns the player that last killed the entity.
        // -->
        if (attribute.startsWith("killer")) {
            return getPlayerFrom(getLivingEntity().getKiller())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.last_damage.amount>
        // @returns ElementTag(Decimal)
        // @group attributes
        // @description
        // Returns the amount of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.amount")) {
            return new ElementTag(getLivingEntity().getLastDamage())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.last_damage.cause>
        // @returns ElementTag
        // @group attributes
        // @description
        // Returns the cause of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.cause")
                && entity.getLastDamageCause() != null) {
            return new ElementTag(entity.getLastDamageCause().getCause().name())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.last_damage.duration>
        // @returns DurationTag
        // @mechanism EntityTag.no_damage_duration
        // @group attributes
        // @description
        // Returns the duration of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.duration")) {
            return new DurationTag((long) getLivingEntity().getNoDamageTicks())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.last_damage.max_duration>
        // @returns DurationTag
        // @mechanism EntityTag.max_no_damage_duration
        // @group attributes
        // @description
        // Returns the maximum duration of the last damage taken by the entity.
        // -->
        if (attribute.startsWith("last_damage.max_duration")) {
            return new DurationTag((long) getLivingEntity().getMaximumNoDamageTicks())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.oxygen.max>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns the maximum duration of oxygen the entity can have.
        // Works with offline players.
        // -->
        if (attribute.startsWith("oxygen.max")) {
            return new DurationTag((long) getLivingEntity().getMaximumAir())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.oxygen>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns the duration of oxygen the entity has left.
        // Works with offline players.
        // -->
        if (attribute.startsWith("oxygen")) {
            return new DurationTag((long) getLivingEntity().getRemainingAir())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.remove_when_far>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity despawns when away from players.
        // -->
        if (attribute.startsWith("remove_when_far")) {
            return new ElementTag(getLivingEntity().getRemoveWhenFarAway())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.target>
        // @returns EntityTag
        // @group attributes
        // @description
        // Returns the target entity of the creature, if any.
        // Note: use <NPCTag.navigator.target_entity> for NPC's.
        // -->
        if (attribute.startsWith("target")) {
            if (getBukkitEntity() instanceof Creature) {
                Entity target = ((Creature) getLivingEntity()).getTarget();
                if (target != null) {
                    return new EntityTag(target).getAttribute(attribute.fulfill(1));
                }
            }
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.time_lived>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns how long the entity has lived.
        // -->
        if (attribute.startsWith("time_lived")) {
            return new DurationTag(entity.getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.pickup_delay>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns how long before the item-type entity can be picked up by a player.
        // -->
        if ((attribute.startsWith("pickup_delay") || attribute.startsWith("pickupdelay"))
                && getBukkitEntity() instanceof Item) {
            return new DurationTag(((Item) getBukkitEntity()).getPickupDelay() * 20).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_in_block>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether or not the arrow/trident entity is in a block.
        // -->
        if (attribute.startsWith("is_in_block")) {
            if (getBukkitEntity() instanceof Arrow) {
                return new ElementTag(((Arrow) getBukkitEntity()).isInBlock()).getAttribute(attribute.fulfill(1));
            }
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.attached_block>
        // @returns LocationTag
        // @group attributes
        // @description
        // Returns the location of the block that the arrow/trident or hanging entity is attached to.
        // -->
        if (attribute.startsWith("attached_block")) {
            if (getBukkitEntity() instanceof Arrow) {
                Block attachedBlock = ((Arrow) getBukkitEntity()).getAttachedBlock();
                if (attachedBlock != null) {
                    return new LocationTag(attachedBlock.getLocation()).getAttribute(attribute.fulfill(1));
                }
            }
            else if (getBukkitEntity() instanceof Hanging) {
                Vector dir = ((Hanging) getBukkitEntity()).getAttachedFace().getDirection();
                return new LocationTag(getLocation().clone().add(dir.multiply(0.5))).getBlockLocation()
                        .getAttribute(attribute.fulfill(1));
            }
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.gliding>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.gliding
        // @group attributes
        // @description
        // Returns whether this entity is gliding.
        // -->
        if (attribute.startsWith("gliding")) {
            return new ElementTag(getLivingEntity().isGliding())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.swimming>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.swimming
        // @group attributes
        // @description
        // Returns whether this entity is swimming.
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13) && attribute.startsWith("swimming")) {
            return new ElementTag(getLivingEntity().isSwimming())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.glowing>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.glowing
        // @group attributes
        // @description
        // Returns whether this entity is glowing.
        // -->
        if (attribute.startsWith("glowing")) {
            return new ElementTag(getBukkitEntity().isGlowing())
                    .getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   TYPE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.is_living>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a living entity.
        // -->
        if (attribute.startsWith("is_living")) {
            return new ElementTag(isLivingEntity())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_monster>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a hostile monster.
        // -->
        if (attribute.startsWith("is_monster")) {
            return new ElementTag(getBukkitEntity() instanceof Monster)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_mob>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a mob (Not a player or NPC).
        // -->
        if (attribute.startsWith("is_mob")) {
            return new ElementTag(!isPlayer() && !isNPC() && isLivingEntity())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_npc>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a Citizens NPC.
        // -->
        if (attribute.startsWith("is_npc")) {
            return new ElementTag(isCitizensNPC())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_player>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a player.
        // Works with offline players.
        // -->
        if (attribute.startsWith("is_player")) {
            return new ElementTag(isPlayer())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.is_projectile>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a projectile.
        // -->
        if (attribute.startsWith("is_projectile")) {
            return new ElementTag(isProjectile())
                    .getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   PROPERTY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.tameable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity is tameable.
        // If this returns true, it will enable access to:
        // <@link mechanism EntityTag.tame>, <@link mechanism EntityTag.owner>,
        // <@link tag EntityTag.is_tamed>, and <@link tag EntityTag.owner>
        // -->
        if (attribute.startsWith("tameable") || attribute.startsWith("is_tameable")) {
            return new ElementTag(EntityTame.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.ageable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity is ageable.
        // If this returns true, it will enable access to:
        // <@link mechanism EntityTag.age>, <@link mechanism EntityTag.age_lock>,
        // <@link tag EntityTag.is_baby>, <@link tag EntityTag.age>,
        // and <@link tag EntityTag.is_age_locked>
        // -->
        if (attribute.startsWith("ageable") || attribute.startsWith("is_ageable")) {
            return new ElementTag(EntityAge.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.colorable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity can be colored.
        // If this returns true, it will enable access to:
        // <@link mechanism EntityTag.color> and <@link tag EntityTag.color>
        // -->
        if (attribute.startsWith("colorable") || attribute.startsWith("is_colorable")) {
            return new ElementTag(EntityColor.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.experience>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the experience value of this experience orb entity.
        // -->
        if (attribute.startsWith("experience") && getBukkitEntity() instanceof ExperienceOrb) {
            return new ElementTag(((ExperienceOrb) getBukkitEntity()).getExperience())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.fuse_ticks>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the number of ticks until the explosion of the primed TNT.
        // -->
        if (attribute.startsWith("fuse_ticks") && getBukkitEntity() instanceof TNTPrimed) {
            return new ElementTag(((TNTPrimed) getBukkitEntity()).getFuseTicks())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.dragon_phase>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the phase an EnderDragon is currently in.
        // Valid phases: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EnderDragon.Phase.html>
        // -->
        if (attribute.startsWith("dragon_phase") && getBukkitEntity() instanceof EnderDragon) {
            return new ElementTag(((EnderDragon) getLivingEntity()).getPhase().name())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.describe>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns the entity's full description, including all properties.
        // -->
        if (attribute.startsWith("describe")) {
            String escript = getEntityScript();
            return new ElementTag("e@" + (escript != null && escript.length() > 0 ? escript : getEntityType().getLowercaseName())
                    + PropertyParser.getPropertiesString(this))
                    .getAttribute(attribute.fulfill(1));
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new ElementTag(identify()).getAttribute(attribute);
    }

    private ArrayList<Mechanism> mechanisms = new ArrayList<>();

    public ArrayList<Mechanism> getWaitingMechanisms() {
        return mechanisms;
    }

    public void applyProperty(Mechanism mechanism) {
        if (isGeneric()) {
            mechanisms.add(mechanism);
            mechanism.fulfill();
        }
        else {
            Debug.echoError("Cannot apply properties to an already-spawned entity!");
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (isGeneric()) {
            mechanisms.add(mechanism);
            mechanism.fulfill();
            return;
        }

        if (getBukkitEntity() == null) {
            if (isCitizensNPC()) {
                Debug.echoError("Cannot adjust not-spawned NPC " + getDenizenNPC());
            }
            else {
                Debug.echoError("Cannot adjust entity " + this);
            }
            return;
        }

        // <--[mechanism]
        // @object EntityTag
        // @name item_in_hand
        // @input ItemTag
        // @description
        // Sets the item in the entity's hand.
        // The entity must be living.
        // @tags
        // <EntityTag.item_in_hand>
        // -->
        if (mechanism.matches("item_in_hand")) {
            NMSHandler.getEntityHelper().setItemInHand(getLivingEntity(), mechanism.valueAsType(ItemTag.class).getItemStack());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name item_in_offhand
        // @input ItemTag
        // @description
        // Sets the item in the entity's offhand.
        // The entity must be living.
        // @tags
        // <EntityTag.item_in_offhand>
        // -->
        if (mechanism.matches("item_in_offhand")) {
            NMSHandler.getEntityHelper().setItemInOffHand(getLivingEntity(), mechanism.valueAsType(ItemTag.class).getItemStack());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name attach_to
        // @input EntityTag(|dLocation(|Element(Boolean)))
        // @description
        // Attaches this entity's client-visible motion to another entity.
        // Optionally, specify an offset vector as well.
        // Optionally specify a boolean indicating whether offset should match the target entity's rotation (defaults to true).
        // Note that because this is client-visible motion, it does not take effect server-side. You may wish to occasionally teleport the entity to its attachment.
        // Tracking may be a bit off with a large (8 blocks is large in this context) offset on a rotating entity.
        // Run with no value to disable attachment.
        // -->
        if (mechanism.matches("attach_to")) {
            if (mechanism.hasValue()) {
                ListTag list = mechanism.valueAsType(ListTag.class);
                Vector offset = null;
                boolean rotateWith = true;
                if (list.size() > 1) {
                    offset = LocationTag.valueOf(list.get(1)).toVector();
                    if (list.size() > 2) {
                        rotateWith = new ElementTag(list.get(2)).asBoolean();
                    }
                }
                NMSHandler.getInstance().forceAttachMove(entity, EntityTag.valueOf(list.get(0)).getBukkitEntity(), offset, rotateWith);
            }
            else {
                NMSHandler.getInstance().forceAttachMove(entity, null, null, false);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name shooter
        // @input EntityTag
        // @description
        // Sets the entity's shooter.
        // The entity must be a projectile.
        // @tags
        // <EntityTag.shooter>
        // -->
        if (mechanism.matches("shooter")) {
            setShooter(mechanism.valueAsType(EntityTag.class));
        }

        // <--[mechanism]
        // @object EntityTag
        // @name can_pickup_items
        // @input Element(Boolean)
        // @description
        // Sets whether the entity can pick up items.
        // The entity must be living.
        // @tags
        // <EntityTag.can_pickup_items>
        // -->
        if (mechanism.matches("can_pickup_items") && mechanism.requireBoolean()) {
            getLivingEntity().setCanPickupItems(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fall_distance
        // @input Element(Decimal)
        // @description
        // Sets the fall distance.
        // @tags
        // <EntityTag.fall_distance>
        // -->
        if (mechanism.matches("fall_distance") && mechanism.requireFloat()) {
            entity.setFallDistance(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fallingblock_drop_item
        // @input Element(Boolean)
        // @description
        // Sets whether the falling block will drop an item if broken.
        // -->
        if (mechanism.matches("fallingblock_drop_item") && mechanism.requireBoolean()
                && entity instanceof FallingBlock) {
            ((FallingBlock) entity).setDropItem(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fallingblock_hurt_entities
        // @input Element(Boolean)
        // @description
        // Sets whether the falling block will hurt entities when it lands.
        // -->
        if (mechanism.matches("fallingblock_hurt_entities") && mechanism.requireBoolean()
                && entity instanceof FallingBlock) {
            ((FallingBlock) entity).setHurtEntities(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fire_time
        // @input Duration
        // @description
        // Sets the entity's current fire time (time before the entity stops being on fire).
        // @tags
        // <EntityTag.fire_time>
        // -->
        if (mechanism.matches("fire_time") && mechanism.requireObject(DurationTag.class)) {
            entity.setFireTicks(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name leash_holder
        // @input EntityTag
        // @description
        // Sets the entity holding this entity by leash.
        // The entity must be living.
        // @tags
        // <EntityTag.leashed>
        // <EntityTag.leash_holder>
        // -->
        if (mechanism.matches("leash_holder") && mechanism.requireObject(EntityTag.class)) {
            getLivingEntity().setLeashHolder(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name can_breed
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is capable of mating with another of its kind.
        // The entity must be living and 'ageable'.
        // @tags
        // <EntityTag.can_breed>
        // -->
        if (mechanism.matches("can_breed") && mechanism.requireBoolean()) {
            ((Ageable) getLivingEntity()).setBreed(true);
        }

        // <--[mechanism]
        // @object EntityTag
        // @name breed
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is trying to mate with another of its kind.
        // The entity must be living and an animal.
        // @tags
        // <EntityTag.can_breed>
        // -->
        if (mechanism.matches("breed") && mechanism.requireBoolean()) {
            NMSHandler.getEntityHelper().setBreeding((Animals) getLivingEntity(), mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name passengers
        // @input ListTag(EntityTag)
        // @description
        // Sets the passengers of this entity.
        // @tags
        // <EntityTag.passengers>
        // <EntityTag.empty>
        // -->
        if (mechanism.matches("passengers")) {
            entity.eject();
            for (EntityTag ent : mechanism.valueAsType(ListTag.class).filter(EntityTag.class, mechanism.context)) {
                if (ent.isSpawned() && comparesTo(ent) != 1) {
                    entity.addPassenger(ent.getBukkitEntity());
                }
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name passenger
        // @input EntityTag
        // @description
        // Sets the passenger of this entity.
        // @tags
        // <EntityTag.passenger>
        // <EntityTag.empty>
        // -->
        if (mechanism.matches("passenger") && mechanism.requireObject(EntityTag.class)) {
            entity.setPassenger(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name time_lived
        // @input Duration
        // @description
        // Sets the amount of time this entity has lived for.
        // @tags
        // <EntityTag.time_lived>
        // -->
        if (mechanism.matches("time_lived") && mechanism.requireObject(DurationTag.class)) {
            entity.setTicksLived(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remaining_air
        // @input Element(Number)
        // @description
        // Sets how much air the entity has remaining before it drowns.
        // The entity must be living.
        // @tags
        // <EntityTag.oxygen>
        // <EntityTag.oxygen.max>
        // -->
        if (mechanism.matches("remaining_air") && mechanism.requireInteger()) {
            getLivingEntity().setRemainingAir(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remove_effects
        // @input None
        // @description
        // Removes all potion effects from the entity.
        // The entity must be living.
        // @tags
        // <EntityTag.has_effect[<effect>]>
        // -->
        if (mechanism.matches("remove_effects")) {
            for (PotionEffect potionEffect : this.getLivingEntity().getActivePotionEffects()) {
                getLivingEntity().removePotionEffect(potionEffect.getType());
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name release_left_shoulder
        // @input None
        // @description
        // Releases the player's left shoulder entity.
        // Only applies to player-typed entities.
        // -->
        if (getLivingEntity() instanceof HumanEntity
                && mechanism.matches("release_left_shoulder")) {
            Entity bukkitEnt = ((HumanEntity) getLivingEntity()).getShoulderEntityLeft();
            if (bukkitEnt != null) {
                EntityTag ent = new EntityTag(bukkitEnt);
                String escript = ent.getEntityScript();
                ent = EntityTag.valueOf("e@" + (escript != null && escript.length() > 0 ? escript : ent.getEntityType().getLowercaseName())
                        + PropertyParser.getPropertiesString(ent));
                ent.spawnAt(getEyeLocation());
                ((HumanEntity) getLivingEntity()).setShoulderEntityLeft(null);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name release_right_shoulder
        // @input None
        // @description
        // Releases the player's right shoulder entity.
        // Only applies to player-typed entities.
        // -->
        if (getLivingEntity() instanceof HumanEntity
                && mechanism.matches("release_right_shoulder")) {
            Entity bukkitEnt = ((HumanEntity) getLivingEntity()).getShoulderEntityRight();
            if (bukkitEnt != null) {
                EntityTag ent = new EntityTag(bukkitEnt);
                String escript = ent.getEntityScript();
                ent = EntityTag.valueOf("e@" + (escript != null && escript.length() > 0 ? escript : ent.getEntityType().getLowercaseName())
                        + PropertyParser.getPropertiesString(ent));
                ent.spawnAt(getEyeLocation());
                ((HumanEntity) getLivingEntity()).setShoulderEntityRight(null);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name left_shoulder
        // @input EntityTag
        // @description
        // Sets the entity's left shoulder entity.
        // Only applies to player-typed entities.
        // Provide no input to remove the shoulder entity.
        // NOTE: This mechanism will remove the current shoulder entity from the world.
        // Also note the client will currently only render parrot entities.
        // @tags
        // <EntityTag.left_shoulder>
        // -->
        if (getLivingEntity() instanceof HumanEntity
                && mechanism.matches("left_shoulder")) {
            if (mechanism.hasValue()) {
                if (mechanism.requireObject(EntityTag.class)) {
                    ((HumanEntity) getLivingEntity()).setShoulderEntityLeft(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
                }
            }
            else {
                ((HumanEntity) getLivingEntity()).setShoulderEntityLeft(null);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name right_shoulder
        // @input EntityTag
        // @description
        // Sets the entity's right shoulder entity.
        // Only applies to player-typed entities.
        // Provide no input to remove the shoulder entity.
        // NOTE: This mechanism will remove the current shoulder entity from the world.
        // Also note the client will currently only render parrot entities.
        // @tags
        // <EntityTag.right_shoulder>
        // -->
        if (getLivingEntity() instanceof HumanEntity
                && mechanism.matches("right_shoulder")) {
            if (mechanism.hasValue()) {
                if (mechanism.requireObject(EntityTag.class)) {
                    ((HumanEntity) getLivingEntity()).setShoulderEntityRight(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
                }
            }
            else {
                ((HumanEntity) getLivingEntity()).setShoulderEntityRight(null);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remove_when_far_away
        // @input Element(Boolean)
        // @description
        // Sets whether the entity should be removed entirely when despawned.
        // The entity must be living.
        // @tags
        // <EntityTag.remove_when_far>
        // -->
        if (mechanism.matches("remove_when_far_away") && mechanism.requireBoolean()) {
            getLivingEntity().setRemoveWhenFarAway(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name sheared
        // @input Element(Boolean)
        // @description
        // Sets whether the sheep is sheared.
        // @tags
        // <EntityTag.is_sheared>
        // -->
        if (mechanism.matches("sheared") && mechanism.requireBoolean()
                && getBukkitEntity() instanceof Sheep) {
            ((Sheep) getBukkitEntity()).setSheared(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name collidable
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is collidable.
        // NOTE: To disable collision between two entities, set this mechanism to false on both entities.
        // @tags
        // <EntityTag.is_collidable>
        // -->
        if (mechanism.matches("collidable")
                && mechanism.requireBoolean()) {
            getLivingEntity().setCollidable(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name no_damage_duration
        // @input Duration
        // @description
        // Sets the duration in which the entity will take no damage.
        // @tags
        // <EntityTag.last_damage.duration>
        // <EntityTag.last_damage.max_duration>
        // -->
        if (mechanism.matches("no_damage_duration") && mechanism.requireObject(DurationTag.class)) {
            getLivingEntity().setNoDamageTicks(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name max_no_damage_duration
        // @input Duration
        // @description
        // Sets the maximum duration in which the entity will take no damage.
        // @tags
        // <EntityTag.last_damage.duration>
        // <EntityTag.last_damage.max_duration>
        // -->
        if (mechanism.matches("max_no_damage_duration") && mechanism.requireObject(DurationTag.class)) {
            getLivingEntity().setMaximumNoDamageTicks(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name velocity
        // @input LocationTag
        // @description
        // Sets the entity's movement velocity.
        // @tags
        // <EntityTag.velocity>
        // -->
        if (mechanism.matches("velocity") && mechanism.requireObject(LocationTag.class)) {
            setVelocity(mechanism.valueAsType(LocationTag.class).toVector());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name move
        // @input LocationTag
        // @description
        // Forces an entity to move in the direction of the velocity specified.
        // -->
        if (mechanism.matches("move") && mechanism.requireObject(LocationTag.class)) {
            NMSHandler.getEntityHelper().move(getBukkitEntity(), mechanism.valueAsType(LocationTag.class).toVector());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name interact_with
        // @input LocationTag
        // @description
        // Makes a player-type entity interact with a block.
        // @tags
        // None
        // -->
        if (mechanism.matches("interact_with") && mechanism.requireObject(LocationTag.class)) {
            LocationTag interactLocation = mechanism.valueAsType(LocationTag.class);
            NMSHandler.getEntityHelper().forceInteraction(getPlayer(), interactLocation);
        }

        // <--[mechanism]
        // @object EntityTag
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

        // <--[mechanism]
        // @object EntityTag
        // @name pickup_delay
        // @input Duration
        // @description
        // Sets the pickup delay of this Item Entity.
        // @tags
        // <EntityTag.pickup_delay>
        // -->
        if ((mechanism.matches("pickup_delay") || mechanism.matches("pickupdelay")) &&
                getBukkitEntity() instanceof Item && mechanism.requireObject(DurationTag.class)) {
            ((Item) getBukkitEntity()).setPickupDelay(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name gliding
        // @input Element(Boolean)
        // @description
        // Sets whether this entity is gliding.
        // @tags
        // <EntityTag.gliding>
        // -->
        if (mechanism.matches("gliding") && mechanism.requireBoolean()) {
            getLivingEntity().setGliding(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name glowing
        // @input Element(Boolean)
        // @description
        // Sets whether this entity is glowing.
        // @tags
        // <EntityTag.glowing>
        // -->
        if (mechanism.matches("glowing") && mechanism.requireBoolean()) {
            getBukkitEntity().setGlowing(mechanism.getValue().asBoolean());
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(getLivingEntity())) {
                CitizensAPI.getNPCRegistry().getNPC(getLivingEntity()).data().setPersistent(NPC.GLOWING_METADATA, mechanism.getValue().asBoolean());
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name dragon_phase
        // @input Element
        // @description
        // Sets an EnderDragon's combat phase.
        // @tags
        // <EntityTag.dragon_phase>
        // -->
        if (mechanism.matches("dragon_phase")) {
            EnderDragon ed = (EnderDragon) getLivingEntity();
            ed.setPhase(EnderDragon.Phase.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object EntityTag
        // @name experience
        // @input Element(Number)
        // @description
        // Sets the experience value of this experience orb entity.
        // @tags
        // <EntityTag.experience>
        // -->
        if (mechanism.matches("experience") && getBukkitEntity() instanceof ExperienceOrb && mechanism.requireInteger()) {
            ((ExperienceOrb) getBukkitEntity()).setExperience(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fuse_ticks
        // @input Element(Number)
        // @description
        // Sets the number of ticks until the TNT blows up after being primed.
        // @tags
        // <EntityTag.fuse_ticks>
        // -->
        if (mechanism.matches("fuse_ticks") && getBukkitEntity() instanceof TNTPrimed && mechanism.requireInteger()) {
            ((TNTPrimed) getBukkitEntity()).setFuseTicks(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name show_to_players
        // @input None
        // @description
        // Marks the entity as visible to players by default (if it was hidden).
        // -->
        if (mechanism.matches("show_to_players")) {
            NMSHandler.getEntityHelper().unhideEntity(null, getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name hide_from_players
        // @input None
        // @description
        // Hides the entity from players by default.
        // -->
        if (mechanism.matches("hide_from_players")) {
            NMSHandler.getEntityHelper().hideEntity(null, getBukkitEntity(), false);
        }

        // <--[mechanism]
        // @object EntityTag
        // @name mirror_player
        // @input Element(Boolean)
        // @description
        // Makes the player-like entity have the same skin as the player looking at it.
        // For NPCs, this will add the Mirror trait.
        // -->
        if (mechanism.matches("mirror_player") && mechanism.requireBoolean()) {
            if (isNPC()) {
                NPC npc = getDenizenNPC().getCitizen();
                if (!npc.hasTrait(MirrorTrait.class)) {
                    npc.addTrait(MirrorTrait.class);
                }
                MirrorTrait mirror = npc.getTrait(MirrorTrait.class);
                if (mechanism.getValue().asBoolean()) {
                    mirror.enableMirror();
                }
                else {
                    mirror.disableMirror();
                }
            }
            else {
                if (mechanism.getValue().asBoolean()) {
                    ProfileEditor.mirrorUUIDs.add(getUUID());
                }
                else {
                    ProfileEditor.mirrorUUIDs.remove(getUUID());
                }
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name swimming
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is swimming.
        // @tags
        // <EntityTag.swimming>
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13) && mechanism.matches("swimming")
                && mechanism.requireBoolean()) {
            getLivingEntity().setSwimming(mechanism.getValue().asBoolean());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
