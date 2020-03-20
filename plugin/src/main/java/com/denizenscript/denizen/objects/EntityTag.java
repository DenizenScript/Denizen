package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.properties.entity.EntityAge;
import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizen.objects.properties.entity.EntityTame;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.blocks.MaterialCompat;
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
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.Deprecations;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Pattern;

public class EntityTag implements ObjectTag, Adjustable, EntityFormObject {

    // <--[language]
    // @name EntityTag Objects
    // @group Object System
    // @description
    // An EntityTag represents a spawned entity, or a generic entity type.
    //
    // Note that players and NPCs are valid EntityTags, but are generally represented by the more specific
    // PlayerTag and NPCTag objects.
    //
    // Note that a spawned entity can be a living entity (a player, NPC, or mob) or a nonliving entity (a painting, item frame, etc).
    //
    // These use the object notation "e@".
    // The identity format for entities is a spawned entity's UUID, or an entity type.
    // For example, 'e@abc123' or 'e@zombie'.
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
        if (entity == null) {
            return false;
        }
        if (Depends.citizens == null) {
            return false;
        }
        if (!CitizensAPI.hasImplementation()) {
            return false;
        }
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
        if (npc == null) {
            return false;
        }
        if (npc.getOwningRegistry() != CitizensAPI.getNPCRegistry()) {
            return false;
        }
        return true;
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
        return entity instanceof Player && !isNPC(entity);
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
        if (ObjectFetcher.isObjectWithProperties(string)) {
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

            return new EntityTag(randomType, "RANDOM");
        }
        if (string.startsWith("n@") || string.startsWith("e@") || string.startsWith("p@")) {
            // NPC entity
            if (string.startsWith("n@")) {
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
            else if (string.startsWith("p@")) {
                LivingEntity returnable = PlayerTag.valueOf(string).getPlayerEntity();
                if (returnable != null) {
                    return new EntityTag(returnable);
                }
                else if (context == null || context.debug) {
                    Debug.echoError("Invalid Player! '" + string + "' could not be found. Has the player logged off?");
                }
            }
            // Assume entity
            else {
                if (string.startsWith("e@")) {
                    string = string.substring("e@".length());
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

                // else if (isSaved(m.group(2)))
                //     return getSaved(m.group(2));
            }
        }
        if (ScriptRegistry.containsScript(string, EntityScriptContainer.class)) {
            // Construct a new custom unspawned entity from script
            return ScriptRegistry.getScriptContainerAs(string, EntityScriptContainer.class).getEntityFrom();
        }
        List<String> data = CoreUtilities.split(string, ',');
        // Handle custom DenizenEntityTypes
        if (DenizenEntityType.isRegistered(data.get(0))) {
            return new EntityTag(DenizenEntityType.getByName(data.get(0)), data.size() > 1 ? data.get(1) : null, data.size() > 2 ? data.get(2) : null);
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

    public static boolean matches(String arg) {

        // Accept anything that starts with a valid entity object identifier.
        if (arg.startsWith("n@") || arg.startsWith("e@") || arg.startsWith("p@")) {
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

        // Check first word with a valid entity_type (other groups are datas used in constructors)
        if (DenizenEntityType.isRegistered(CoreUtilities.split(arg, ',').get(0))) {
            return true;
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

    public EntityTag(EntityType entityType) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = DenizenEntityType.getByName(entityType.name());
        }
        else {
            Debug.echoError("Entity_type referenced is null!");
        }
    }

    public EntityTag(EntityType entityType, ArrayList<Mechanism> mechanisms) {
        this(entityType);
        this.mechanisms = mechanisms;
    }

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

    public Entity getBukkitEntity() {
        return entity;
    }

    public LivingEntity getLivingEntity() {
        if (entity instanceof LivingEntity) {
            return (LivingEntity) entity;
        }
        else {
            return null;
        }
    }

    public boolean isLivingEntity() {
        return (entity instanceof LivingEntity);
    }

    public boolean hasInventory() {
        return getBukkitEntity() instanceof InventoryHolder || isCitizensNPC();
    }

    public NPCTag getDenizenNPC() {
        if (npc != null) {
            return npc;
        }
        else {
            return getNPCFrom(entity);
        }
    }

    public boolean isNPC() {
        return npc != null || isNPC(entity);
    }

    public boolean isCitizensNPC() {
        return npc != null || isCitizensNPC(entity);
    }

    public Player getPlayer() {
        if (isPlayer()) {
            return (Player) entity;
        }
        else {
            return null;
        }
    }

    public PlayerTag getDenizenPlayer() {
        if (isPlayer()) {
            return new PlayerTag(getPlayer());
        }
        else {
            return null;
        }
    }

    public boolean isPlayer() {
        return entity instanceof Player && !isNPC();
    }

    public Projectile getProjectile() {

        return (Projectile) entity;
    }

    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    public EntityTag getShooter() {
        if (hasShooter()) {
            return new EntityTag((LivingEntity) getProjectile().getShooter());
        }
        else {
            return null;
        }
    }

    public void setShooter(EntityTag shooter) {
        if (isProjectile() && shooter.isLivingEntity()) {
            getProjectile().setShooter(shooter.getLivingEntity());
        }
    }

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

    public ListTag getEquipment() {
        ItemStack[] equipment = getLivingEntity().getEquipment().getArmorContents();
        ListTag equipmentList = new ListTag();
        for (ItemStack item : equipment) {
            equipmentList.addObject(new ItemTag(item));
        }
        return equipmentList;
    }

    public boolean isGeneric() {
        return !isUnique();
    }

    @Override
    public LocationTag getLocation() {

        if (entity != null) {
            return new LocationTag(entity.getLocation());
        }

        return null;
    }

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

    public Vector getVelocity() {

        if (!isGeneric()) {
            return entity.getVelocity();
        }
        return null;
    }

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
                        else {
                            // <--[mechanism]
                            // @object EntityTag
                            // @name fallingblock_type
                            // @input MaterialTag
                            // @description
                            // Sets the block type of a falling_block entity (only valid while spawning).
                            // @tags
                            // <EntityTag.fallingblock_material>
                            // -->
                            for (Mechanism mech : mechanisms) {
                                if (mech.getName().equalsIgnoreCase("fallingblock_type")) {
                                    material = mech.valueAsType(MaterialTag.class).getMaterial();
                                    mechanisms.remove(mech);
                                    break;
                                }
                            }
                        }

                        // If material is null or not a block, default to SAND
                        if (material == null || (!material.isBlock())) {
                            material = Material.SAND;
                        }

                        byte materialData = 0;

                        // Get special data value from data2 if it is a valid integer
                        if (data2 != null && ArgumentHelper.matchesInteger(data2)) {

                            materialData = (byte) Integer.parseInt(data2);
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

    public boolean isSpawnedOrValidForTag() {
        return entity != null && (isValidForTag() || rememberedEntities.containsKey(entity.getUniqueId()));
    }

    public boolean isSpawned() {
        return entity != null && entity.isValid();
    }

    public boolean isValid() {
        return entity != null && entity.isValid();
    }

    public boolean isValidForTag() {
        NMSHandler.getChunkHelper().changeChunkServerThread(entity.getWorld());
        try {
            return entity.isValid();
        }
        finally {
            NMSHandler.getChunkHelper().restoreServerThread(entity.getWorld());
        }
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
            if (isSpawnedOrValidForTag()) {
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

            else if (isSpawnedOrValidForTag()) {
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
                properties.append(mechanism.getName()).append("=").append(EscapeTagBase.escape(mechanism.getValue().asString())).append(";");
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

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Entity' for EntityTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", (attribute, object) -> {
            return new ElementTag("Entity");
        });

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
        registerTag("entity_type", (attribute, object) -> {
            return new ElementTag(object.entity_type.getName());
        });

        // <--[tag]
        // @attribute <EntityTag.is_spawned>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is spawned.
        // -->
        registerTag("is_spawned", (attribute, object) -> {
            return new ElementTag(object.isSpawned());
        });

        // <--[tag]
        // @attribute <EntityTag.eid>
        // @returns ElementTag(Number)
        // @group data
        // @description
        // Returns the entity's temporary server entity ID.
        // -->
        registerTag("eid", (attribute, object) -> {
            return new ElementTag(object.entity.getEntityId());
        });

        // <--[tag]
        // @attribute <EntityTag.uuid>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the permanent unique ID of the entity.
        // Works with offline players.
        // -->
        registerTag("uuid", (attribute, object) -> {
            return new ElementTag(object.getUUID().toString());
        });

        // <--[tag]
        // @attribute <EntityTag.script>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the entity script that spawned this entity, if any.
        // -->
        registerTag("script", (attribute, object) -> {
            if (object.entityScript == null) {
                return null;
            }
            ScriptTag tag = new ScriptTag(object.entityScript);
            if (tag.isValid()) {
                return tag;
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.scriptname>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the name of the entity script that spawned this entity, if any.
        // Generally prefer <@link tag entity.script>, but this tag may be useful if an entity script was renamed after this entity was spawned.
        // -->
        registerTag("scriptname", (attribute, object) -> {
            if (object.entityScript == null) {
                return null;
            }
            return new ElementTag(object.entityScript);
        });

        // <--[tag]
        // @attribute <EntityTag.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the entity has the specified flag, otherwise returns false.
        // -->
        registerSpawnedOnlyTag("has_flag", (attribute, object) -> {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                return null;
            }
            if (object.isPlayer() || object.isCitizensNPC()) {
                Debug.echoError("Reading flag for PLAYER or NPC as if it were an ENTITY!");
                return null;
            }
            return new ElementTag(FlagManager.entityHasFlag(object, flag_name));
        });

        // <--[tag]
        // @attribute <EntityTag.flag[<flag_name>]>
        // @returns Flag ListTag
        // @description
        // Returns the specified flag from the entity.
        // -->
        registerSpawnedOnlyTag("flag", (attribute, object) -> {
            String flag_name;
            if (attribute.hasContext(1)) {
                flag_name = attribute.getContext(1);
            }
            else {
                return null;
            }
            if (object.isPlayer() || object.isCitizensNPC()) {
                Debug.echoError("Reading flag for PLAYER or NPC as if it were an ENTITY!");
                return null;
            }
            // <--[tag]
            // @attribute <EntityTag.flag[<flag_name>].is_expired>
            // @returns ElementTag(Boolean)
            // @description
            // returns true if the flag is expired or does not exist, false if it is not yet expired or has no expiration.
            // -->
            if (attribute.startsWith("is_expired", 2) || attribute.startsWith("isexpired", 2)) {
                attribute.fulfill(1);
                return new ElementTag(!FlagManager.entityHasFlag(object, flag_name));
            }
            if (attribute.startsWith("size", 2) && !FlagManager.entityHasFlag(object, flag_name)) {
                attribute.fulfill(1);
                return new ElementTag(0);
            }
            if (FlagManager.entityHasFlag(object, flag_name)) {
                FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager().getEntityFlag(object, flag_name);

                // <--[tag]
                // @attribute <EntityTag.flag[<flag_name>].expiration>
                // @returns DurationTag
                // @description
                // Returns a DurationTag of the time remaining on the flag, if it has an expiration.
                // -->
                if (attribute.startsWith("expiration", 2)) {
                    attribute.fulfill(1);
                    return flag.expiration();
                }
                return new ListTag(flag.toString(), true, flag.values());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.list_flags[(regex:)<search>]>
        // @returns ListTag
        // @description
        // Returns a list of an entity's flag names, with an optional search for
        // names containing a certain pattern.
        // -->
        registerSpawnedOnlyTag("list_flags", (attribute, object) -> {
            ListTag allFlags = new ListTag(DenizenAPI.getCurrentInstance().flagManager().listEntityFlags(object));
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
                DenizenAPI.getCurrentInstance().flagManager().shrinkEntityFlags(object, searchFlags);
            }
            else {
                DenizenAPI.getCurrentInstance().flagManager().shrinkEntityFlags(object, allFlags);
            }
            return searchFlags == null ? allFlags
                    : searchFlags;
        });

        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        registerSpawnedOnlyTag("custom_id", (attribute, object) -> {
            Deprecations.entityCustomIdTag.warn(attribute.context);
            if (CustomNBT.hasCustomNBT(object.getLivingEntity(), "denizen-script-id")) {
                return new ScriptTag(CustomNBT.getCustomNBT(object.getLivingEntity(), "denizen-script-id"));
            }
            else {
                return new ElementTag(object.entity.getType().name());
            }
        });

        // <--[tag]
        // @attribute <EntityTag.name>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the name of the entity.
        // This can be a player name, an NPC name, a custom_name, or the entity type.
        // Works with offline players.
        // -->
        registerSpawnedOnlyTag("name", (attribute, object) -> {
            return new ElementTag(object.getName());
        });

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
        registerSpawnedOnlyTag("saddle", (attribute, object) -> {
            if (object.getLivingEntity().getType() == EntityType.HORSE) {
                return new ItemTag(((Horse) object.getLivingEntity()).getInventory().getSaddle());
            }
            else if (object.getLivingEntity().getType() == EntityType.PIG) {
                return new ItemTag(((Pig) object.getLivingEntity()).hasSaddle() ? Material.SADDLE : Material.AIR);
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.horse_armor>
        // @returns ItemTag
        // @group inventory
        // @description
        // If the entity is a horse, returns the item equipped as the horses armor, or air if none.
        // -->
        registerTag("horse_armor", (attribute, object) -> {
            if (object.getBukkitEntityType() == EntityType.HORSE) {
                return new ItemTag(((Horse) object.getLivingEntity()).getInventory().getArmor());
            }
            return null;
        }, "horse_armour");

        // <--[tag]
        // @attribute <EntityTag.has_saddle>
        // @returns ElementTag(Boolean)
        // @group inventory
        // @description
        // If the entity s a pig or horse, returns whether it has a saddle equipped.
        // -->
        registerSpawnedOnlyTag("has_saddle", (attribute, object) -> {
            if (object.getBukkitEntityType() == EntityType.HORSE) {
                return new ElementTag(((Horse) object.getLivingEntity()).getInventory().getSaddle().getType() == Material.SADDLE);
            }
            else if (object.getBukkitEntityType() == EntityType.PIG) {
                return new ElementTag(((Pig) object.getLivingEntity()).hasSaddle());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.item_in_hand>
        // @returns ItemTag
        // @mechanism EntityTag.item_in_hand
        // @group inventory
        // @description
        // Returns the item the entity is holding, or air if none.
        // -->
        registerSpawnedOnlyTag("item_in_hand", (attribute, object) -> {
            return new ItemTag(object.getLivingEntity().getEquipment().getItemInMainHand());
        }, "iteminhand");

        // <--[tag]
        // @attribute <EntityTag.item_in_offhand>
        // @returns ItemTag
        // @mechanism EntityTag.item_in_offhand
        // @group inventory
        // @description
        // Returns the item the entity is holding in their off hand, or air if none.
        // -->
        registerSpawnedOnlyTag("item_in_offhand", (attribute, object) -> {
            return new ItemTag(object.getLivingEntity().getEquipment().getItemInOffHand());
        }, "iteminoffhand");

        // <--[tag]
        // @attribute <EntityTag.is_trading>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the villager entity is trading.
        // -->
        registerSpawnedOnlyTag("is_trading", (attribute, object) -> {
            if (object.entity instanceof Merchant) {
                return new ElementTag(((Merchant) object.entity).isTrading());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.trading_with>
        // @returns PlayerTag
        // @description
        // Returns the player who is trading with the villager entity, or null if it is not trading.
        // -->
        registerSpawnedOnlyTag("trading_with", (attribute, object) -> {
            if (object.entity instanceof Merchant
                    && ((Merchant) object.entity).getTrader() != null) {
                return new EntityTag(((Merchant) object.entity).getTrader());
            }
            return null;
        });

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
        registerSpawnedOnlyTag("map_trace", (attribute, object) -> {
            EntityHelper.MapTraceResult mtr = NMSHandler.getEntityHelper().mapTrace(object.getLivingEntity(), 200);
            if (mtr != null) {
                double x = 0;
                double y;
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
                return new LocationTag(null, Math.round(x), Math.round(y));
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.can_see[<entity>]>
        // @returns ElementTag(Boolean)
        // @group location
        // @description
        // Returns whether the entity can see the specified other entity (has an uninterrupted line-of-sight).
        // -->
        registerSpawnedOnlyTag("can_see", (attribute, object) -> {
            if (object.isLivingEntity() && attribute.hasContext(1) && EntityTag.matches(attribute.getContext(1))) {
                EntityTag toEntity = EntityTag.valueOf(attribute.getContext(1));
                if (toEntity != null && toEntity.isSpawnedOrValidForTag()) {
                    return new ElementTag(object.getLivingEntity().hasLineOfSight(toEntity.getBukkitEntity()));
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.eye_location>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the entity's eyes.
        // -->
        registerSpawnedOnlyTag("eye_location", (attribute, object) -> {
            return new LocationTag(object.getEyeLocation());
        });

        // <--[tag]
        // @attribute <EntityTag.eye_height>
        // @returns ElementTag(Boolean)
        // @group location
        // @description
        // Returns the height of the entity's eyes above its location.
        // -->
        registerSpawnedOnlyTag("eye_height", (attribute, object) -> {
            if (object.isLivingEntity()) {
                return new ElementTag(object.getLivingEntity().getEyeHeight());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.cursor_on[(<range>)]>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the block the entity is looking at.
        // Optionally, specify a maximum range to find the location from.
        // This uses logic equivalent to <@link tag LocationTag.precise_cursor_on_block[(range)]>.
        // Note that this will return null if there is no solid block in range.
        // -->
        registerSpawnedOnlyTag("cursor_on", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            // TODO: after 1.12 support is dropped, World#rayTraceBlocks should be used.
            Location location = NMSHandler.getEntityHelper().rayTraceBlock(object.getEyeLocation(), object.getEyeLocation().getDirection(), range);
            if (location != null) {
                return new LocationTag(location).getBlockLocation();
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.location>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the entity.
        // For living entities, this is at the center of their feet.
        // For eye location, use <@link tag EntityTag.eye_location>
        // Works with offline players.
        // -->
        registerSpawnedOnlyTag("location", (attribute, object) -> {
            if (attribute.startsWith("cursor_on", 2)) {
                Deprecations.entityLocationCursorOnTag.warn(attribute.context);
                int range = attribute.getIntContext(2);
                if (range < 1) {
                    range = 50;
                }
                Set<Material> set = new HashSet<>();
                set.add(Material.AIR);

                if (attribute.startsWith("ignore", 3) && attribute.hasContext(3)) {
                    List<MaterialTag> ignoreList = ListTag.valueOf(attribute.getContext(3), attribute.context).filter(MaterialTag.class, attribute.context);
                    for (MaterialTag material : ignoreList) {
                        set.add(material.getMaterial());
                    }
                    attribute.fulfill(1);
                }
                attribute.fulfill(1);
                return new LocationTag(object.getTargetBlockSafe(set, range));
            }

            // <--[tag]
            // @attribute <EntityTag.location.standing_on>
            // @returns LocationTag
            // @group location
            // @description
            // Returns the location of what the entity is standing on.
            // Works with offline players.
            // -->
            if (attribute.startsWith("standing_on", 2)) {
                attribute.fulfill(1);
                return new LocationTag(object.entity.getLocation().clone().add(0, -0.5f, 0));
            }
            return new LocationTag(object.entity.getLocation());
        });

        // <--[tag]
        // @attribute <EntityTag.body_yaw>
        // @returns ElementTag(Decimal)
        // @group location
        // @description
        // Returns the entity's body yaw (separate from head yaw).
        // -->
        registerSpawnedOnlyTag("body_yaw", (attribute, object) -> {
            return new ElementTag(NMSHandler.getEntityHelper().getBaseYaw(object.entity));
        });

        // <--[tag]
        // @attribute <EntityTag.velocity>
        // @returns LocationTag
        // @group location
        // @mechanism EntityTag.velocity
        // @description
        // Returns the movement velocity of the entity.
        // Note: Does not accurately calculate player clientside movement velocity.
        // -->
        registerSpawnedOnlyTag("velocity", (attribute, object) -> {
            return new LocationTag(object.entity.getVelocity().toLocation(object.entity.getWorld()));
        });

        // <--[tag]
        // @attribute <EntityTag.world>
        // @returns WorldTag
        // @group location
        // @description
        // Returns the world the entity is in. Works with offline players.
        // -->
        registerSpawnedOnlyTag("world", (attribute, object) -> {
            return new WorldTag(object.entity.getWorld());
        });

        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.can_pickup_items>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.can_pickup_items
        // @group attributes
        // @description
        // Returns whether the entity can pick up items.
        // -->
        registerSpawnedOnlyTag("can_pickup_items", (attribute, object) -> {
            if (object.isLivingEntity()) {
                return new ElementTag(object.getLivingEntity().getCanPickupItems());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.fallingblock_material>
        // @returns MaterialTag
        // @mechanism EntityTag.fallingblock_type
        // @group attributes
        // @description
        // Returns the material of a fallingblock-type entity.
        // -->
        registerSpawnedOnlyTag("fallingblock_material", (attribute, object) -> {
            if (!(object.entity instanceof FallingBlock)) {
                return null;
            }
            return new MaterialTag(NMSHandler.getEntityHelper().getBlockDataFor((FallingBlock) object.entity));
        });

        // <--[tag]
        // @attribute <EntityTag.fall_distance>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.fall_distance
        // @group attributes
        // @description
        // Returns how far the entity has fallen.
        // -->
        registerSpawnedOnlyTag("fall_distance", (attribute, object) -> {
            return new ElementTag(object.entity.getFallDistance());
        });

        // <--[tag]
        // @attribute <EntityTag.fire_time>
        // @returns DurationTag
        // @mechanism EntityTag.fire_time
        // @group attributes
        // @description
        // Returns the duration for which the entity will remain on fire
        // -->
        registerSpawnedOnlyTag("fire_time", (attribute, object) -> {
            return new DurationTag(object.entity.getFireTicks() / 20);
        });

        // <--[tag]
        // @attribute <EntityTag.on_fire>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is currently ablaze or not.
        // -->
        registerSpawnedOnlyTag("on_fire", (attribute, object) -> {
            return new ElementTag(object.entity.getFireTicks() > 0);
        });

        // <--[tag]
        // @attribute <EntityTag.leash_holder>
        // @returns EntityTag
        // @mechanism EntityTag.leash_holder
        // @group attributes
        // @description
        // Returns the leash holder of entity.
        // -->
        registerSpawnedOnlyTag("leash_holder", (attribute, object) -> {
            if (object.isLivingEntity() && object.getLivingEntity().isLeashed()) {
                return new EntityTag(object.getLivingEntity().getLeashHolder());
            }
            return null;
        }, "get_leash_holder");

        // <--[tag]
        // @attribute <EntityTag.passengers>
        // @returns ListTag(EntityTag)
        // @mechanism EntityTag.passengers
        // @group attributes
        // @description
        // Returns a list of the entity's passengers, if any.
        // -->
        registerSpawnedOnlyTag("passengers", (attribute, object) -> {
            ArrayList<EntityTag> passengers = new ArrayList<>();
            for (Entity ent : object.entity.getPassengers()) {
                passengers.add(new EntityTag(ent));
            }
            return new ListTag(passengers);
        }, "get_passengers");

        // <--[tag]
        // @attribute <EntityTag.passenger>
        // @returns EntityTag
        // @mechanism EntityTag.passenger
        // @group attributes
        // @description
        // Returns the entity's passenger, if any.
        // -->
        registerSpawnedOnlyTag("passenger", (attribute, object) -> {
            if (!object.entity.isEmpty()) {
                return new EntityTag(object.entity.getPassenger());
            }
            return null;
        }, "get_passenger");

        // <--[tag]
        // @attribute <EntityTag.shooter>
        // @returns EntityTag
        // @group attributes
        // @Mechanism EntityTag.shooter
        // @description
        // Returns the entity's shooter, if any.
        // -->
        registerSpawnedOnlyTag("shooter", (attribute, object) -> {
            return object.getShooter().getDenizenObject();
        }, "get_shooter");

        // <--[tag]
        // @attribute <EntityTag.left_shoulder>
        // @returns EntityTag
        // @mechanism EntityTag.left_shoulder
        // @description
        // Returns the entity on the entity's left shoulder.
        // Only applies to player-typed entities.
        // NOTE: The returned entity will not be spawned within the world,
        // so most operations are invalid unless the entity is first spawned in.
        // -->
        registerSpawnedOnlyTag("left_shoulder", (attribute, object) -> {
            if (!(object.getLivingEntity() instanceof HumanEntity)) {
                return null;
            }
            return new EntityTag(((HumanEntity) object.getLivingEntity()).getShoulderEntityLeft());
        });

        // <--[tag]
        // @attribute <EntityTag.right_shoulder>
        // @returns EntityTag
        // @mechanism EntityTag.right_shoulder
        // @description
        // Returns the entity on the entity's right shoulder.
        // Only applies to player-typed entities.
        // NOTE: The returned entity will not be spawned within the world,
        // so most operations are invalid unless the entity is first spawned in.
        // -->
        registerSpawnedOnlyTag("right_shoulder", (attribute, object) -> {
            if (!(object.getLivingEntity() instanceof HumanEntity)) {
                return null;
            }
            return new EntityTag(((HumanEntity) object.getLivingEntity()).getShoulderEntityRight());
        });

        // <--[tag]
        // @attribute <EntityTag.vehicle>
        // @returns EntityTag
        // @group attributes
        // @description
        // If the entity is in a vehicle, returns the vehicle as a EntityTag.
        // -->
        registerSpawnedOnlyTag("vehicle", (attribute, object) -> {
            if (object.entity.isInsideVehicle()) {
                return new EntityTag(object.entity.getVehicle());
            }
            return null;
        }, "get_vehicle");

        // <--[tag]
        // @attribute <EntityTag.can_breed>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.can_breed
        // @group attributes
        // @description
        // Returns whether the animal entity is capable of mating with another of its kind.
        // -->
        registerSpawnedOnlyTag("can_breed", (attribute, object) -> {
            return new ElementTag(((Ageable) object.getLivingEntity()).canBreed());
        });

        // <--[tag]
        // @attribute <EntityTag.breeding>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the animal entity is trying to with another of its kind.
        // -->
        registerSpawnedOnlyTag("breeding", (attribute, object) -> {
            return new ElementTag(NMSHandler.getEntityHelper().isBreeding((Animals) object.getLivingEntity()));
        }, "is_breeding");

        // <--[tag]
        // @attribute <EntityTag.has_passenger>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.passenger
        // @group attributes
        // @description
        // Returns whether the entity has a passenger.
        // -->
        registerSpawnedOnlyTag("has_passenger", (attribute, object) -> {
            return new ElementTag(!object.entity.isEmpty());
        });

        // <--[tag]
        // @attribute <EntityTag.is_empty>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity does not have a passenger.
        // -->
        registerSpawnedOnlyTag("is_empty", (attribute, object) -> {
            return new ElementTag(object.entity.isEmpty());
        }, "empty");

        // <--[tag]
        // @attribute <EntityTag.is_inside_vehicle>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is inside a vehicle.
        // -->
        registerSpawnedOnlyTag("is_inside_vehicle", (attribute, object) -> {
            return new ElementTag(object.entity.isInsideVehicle());
        }, "inside_vehicle");

        // <--[tag]
        // @attribute <EntityTag.is_leashed>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is leashed.
        // -->
        registerSpawnedOnlyTag("is_leashed", (attribute, object) -> {
            return new ElementTag(object.isLivingEntity() && object.getLivingEntity().isLeashed());
        }, "leashed");

        // <--[tag]
        // @attribute <EntityTag.is_sheared>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether a sheep is sheared.
        // -->
        registerSpawnedOnlyTag("is_sheared", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof Sheep)) {
                return null;
            }
            return new ElementTag(((Sheep) object.getBukkitEntity()).isSheared());
        });

        // <--[tag]
        // @attribute <EntityTag.is_on_ground>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is supported by a block.
        // -->
        registerSpawnedOnlyTag("is_on_ground", (attribute, object) -> {
            return new ElementTag(object.entity.isOnGround());
        }, "on_ground");

        // <--[tag]
        // @attribute <EntityTag.is_persistent>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity will not be removed completely when far away from players.
        // -->
        registerSpawnedOnlyTag("is_persistent", (attribute, object) -> {
            return new ElementTag(object.isLivingEntity() && !object.getLivingEntity().getRemoveWhenFarAway());
        }, "persistent");

        // <--[tag]
        // @attribute <EntityTag.is_collidable>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.collidable
        // @group attributes
        // @description
        // Returns whether the entity is collidable.
        // -->
        registerSpawnedOnlyTag("is_collidable", (attribute, object) -> {
            return new ElementTag(object.getLivingEntity().isCollidable());
        });

        // <--[tag]
        // @attribute <EntityTag.killer>
        // @returns PlayerTag
        // @group attributes
        // @description
        // Returns the player that last killed the entity.
        // -->
        registerSpawnedOnlyTag("killer", (attribute, object) -> {
            return getPlayerFrom(object.getLivingEntity().getKiller());
        });

        registerSpawnedOnlyTag("last_damage", (attribute, object) -> {
            // <--[tag]
            // @attribute <EntityTag.last_damage.amount>
            // @returns ElementTag(Decimal)
            // @group attributes
            // @description
            // Returns the amount of the last damage taken by the entity.
            // -->
            if (attribute.startsWith("amount", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getLivingEntity().getLastDamage());
            }
            // <--[tag]
            // @attribute <EntityTag.last_damage.cause>
            // @returns ElementTag
            // @group attributes
            // @description
            // Returns the cause of the last damage taken by the entity.
            // -->
            if (attribute.startsWith("cause", 2)) {
                attribute.fulfill(1);
                if (object.entity.getLastDamageCause() == null) {
                    return null;
                }
                return new ElementTag(object.entity.getLastDamageCause().getCause().name());
            }
            // <--[tag]
            // @attribute <EntityTag.last_damage.duration>
            // @returns DurationTag
            // @mechanism EntityTag.no_damage_duration
            // @group attributes
            // @description
            // Returns the duration of the last damage taken by the entity.
            // -->
            if (attribute.startsWith("duration", 2)) {
                attribute.fulfill(1);
                return new DurationTag((long) object.getLivingEntity().getNoDamageTicks());
            }
            // <--[tag]
            // @attribute <EntityTag.last_damage.max_duration>
            // @returns DurationTag
            // @mechanism EntityTag.max_no_damage_duration
            // @group attributes
            // @description
            // Returns the maximum duration of the last damage taken by the entity.
            // -->
            if (attribute.startsWith("max_duration", 2)) {
                attribute.fulfill(1);
                return new DurationTag((long) object.getLivingEntity().getMaximumNoDamageTicks());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.absorption_health>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.absorption_health
        // @description
        // Returns the living entity's absorption health.
        // -->
        registerTag("absorption_health", (attribute, object) -> {
            return new ElementTag(NMSHandler.getEntityHelper().getAbsorption(object.getLivingEntity()));
        });

        // <--[tag]
        // @attribute <EntityTag.max_oxygen>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns the maximum duration of oxygen the entity can have.
        // Works with offline players.
        // -->
        registerSpawnedOnlyTag("max_oxygen", (attribute, object) -> {
            return new DurationTag((long) object.getLivingEntity().getMaximumAir());
        });

        // <--[tag]
        // @attribute <EntityTag.oxygen>
        // @returns DurationTag
        // @mechanism EntityTag.oxygen
        // @group attributes
        // @description
        // Returns the duration of oxygen the entity has left.
        // Works with offline players.
        // -->
        registerSpawnedOnlyTag("oxygen", (attribute, object) -> {
            if (attribute.startsWith("max", 2)) {
                Deprecations.entityMaxOxygenTag.warn(attribute.context);
                attribute.fulfill(1);
                return new DurationTag((long) object.getLivingEntity().getMaximumAir());
            }
            return new DurationTag((long) object.getLivingEntity().getRemainingAir());
        });

        // <--[tag]
        // @attribute <EntityTag.remove_when_far>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity despawns when away from players.
        // -->
        registerSpawnedOnlyTag("remove_when_far", (attribute, object) -> {
            return new ElementTag(object.getLivingEntity().getRemoveWhenFarAway());
        });

        // <--[tag]
        // @attribute <EntityTag.target>
        // @returns EntityTag
        // @group attributes
        // @description
        // Returns the target entity of the creature, if any.
        // Note: use <NPCTag.navigator.target_entity> for NPC's.
        // -->
        registerSpawnedOnlyTag("target", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Creature) {
                Entity target = ((Creature) object.getLivingEntity()).getTarget();
                if (target != null) {
                    return new EntityTag(target);
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.precise_target[(<range>)]>
        // @returns EntityTag
        // @description
        // Returns the entity this entity is looking at, using precise ray trace logic.
        // Optionally, specify a maximum range to find the entity from (defaults to 200).
        // -->
        registerTag("precise_target", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            RayTraceResult result = object.getWorld().rayTrace(object.getEyeLocation(), object.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true, 0, (e) -> !e.equals(object.getBukkitEntity()));
            if (result != null && result.getHitEntity() != null) {
                return new EntityTag(result.getHitEntity());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.precise_target_position[(<range>)]>
        // @returns LocationTag
        // @description
        // Returns the location this entity is looking at, using precise ray trace (against entities) logic.
        // Optionally, specify a maximum range to find the target from (defaults to 200).
        // -->
        registerTag("precise_target_position", (attribute, object) -> {
            int range = attribute.getIntContext(1);
            if (range < 1) {
                range = 200;
            }
            RayTraceResult result = object.getWorld().rayTrace(object.getEyeLocation(), object.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true, 0, (e) -> !e.equals(object.getBukkitEntity()));
            if (result != null) {
                return new LocationTag(object.getWorld(), result.getHitPosition());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.time_lived>
        // @returns DurationTag
        // @mechanism EntityTag.time_lived
        // @group attributes
        // @description
        // Returns how long the entity has lived.
        // -->
        registerSpawnedOnlyTag("time_lived", (attribute, object) -> {
            return new DurationTag(object.entity.getTicksLived() / 20);
        });

        // <--[tag]
        // @attribute <EntityTag.pickup_delay>
        // @returns DurationTag
        // @mechanism EntityTag.pickup_delay
        // @group attributes
        // @description
        // Returns how long before the item-type entity can be picked up by a player.
        // -->
        registerSpawnedOnlyTag("pickup_delay", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof Item)) {
                return null;
            }
            return new DurationTag(((Item) object.getBukkitEntity()).getPickupDelay() * 20);
        }, "pickupdelay");

        // <--[tag]
        // @attribute <EntityTag.is_in_block>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether or not the arrow/trident entity is in a block.
        // -->
        registerSpawnedOnlyTag("is_in_block", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Arrow) {
                return new ElementTag(((Arrow) object.getBukkitEntity()).isInBlock());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.attached_block>
        // @returns LocationTag
        // @group attributes
        // @description
        // Returns the location of the block that the arrow/trident or hanging entity is attached to.
        // -->
        registerSpawnedOnlyTag("attached_block", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Arrow) {
                Block attachedBlock = ((Arrow) object.getBukkitEntity()).getAttachedBlock();
                if (attachedBlock != null) {
                    return new LocationTag(attachedBlock.getLocation());
                }
            }
            else if (object.getBukkitEntity() instanceof Hanging) {
                Vector dir = ((Hanging) object.getBukkitEntity()).getAttachedFace().getDirection();
                return new LocationTag(object.getLocation().clone().add(dir.multiply(0.5))).getBlockLocation();
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.gliding>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.gliding
        // @group attributes
        // @description
        // Returns whether this entity is gliding.
        // -->
        registerSpawnedOnlyTag("gliding", (attribute, object) -> {
            return new ElementTag(object.getLivingEntity().isGliding());
        });

        // <--[tag]
        // @attribute <EntityTag.swimming>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.swimming
        // @group attributes
        // @description
        // Returns whether this entity is swimming.
        // -->
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            registerSpawnedOnlyTag("swimming", (attribute, object) -> {
                return new ElementTag(object.getLivingEntity().isSwimming());

            });
        }

        // <--[tag]
        // @attribute <EntityTag.glowing>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.glowing
        // @group attributes
        // @description
        // Returns whether this entity is glowing.
        // -->
        registerSpawnedOnlyTag("glowing", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().isGlowing());
        });

        /////////////////////
        //   TYPE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.is_living>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a living-type entity (eg a cow or a player or anything else that lives, as specifically opposed to non-living entities like paintings, etc).
        // Not to be confused with the idea of being alive - see <@link tag EntityTag.is_spawned>.
        // This tag is valid for entity type objects.
        // -->
        registerTag("is_living", (attribute, object) -> {
            if (object.entity == null && object.entity_type != null) {
                return new ElementTag(object.entity_type.getBukkitEntityType().isAlive());
            }
            return new ElementTag(object.isLivingEntity());
        });

        // <--[tag]
        // @attribute <EntityTag.is_monster>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a hostile monster.
        // -->
        registerSpawnedOnlyTag("is_monster", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity() instanceof Monster);
        });

        // <--[tag]
        // @attribute <EntityTag.is_mob>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a mob (Not a player or NPC).
        // -->
        registerSpawnedOnlyTag("is_mob", (attribute, object) -> {
            return new ElementTag(!object.isPlayer() && !object.isNPC() && object.isLivingEntity());
        });

        // <--[tag]
        // @attribute <EntityTag.is_npc>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a Citizens NPC.
        // -->
        registerSpawnedOnlyTag("is_npc", (attribute, object) -> {
            return new ElementTag(object.isCitizensNPC());
        });

        // <--[tag]
        // @attribute <EntityTag.is_player>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a player.
        // Works with offline players.
        // -->
        registerSpawnedOnlyTag("is_player", (attribute, object) -> {
            return new ElementTag(object.isPlayer());
        });

        // <--[tag]
        // @attribute <EntityTag.is_projectile>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a projectile.
        // -->
        registerSpawnedOnlyTag("is_projectile", (attribute, object) -> {
            return new ElementTag(object.isProjectile());
        });

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
        registerSpawnedOnlyTag("tameable", (attribute, object) -> {
            return new ElementTag(EntityTame.describes(object));
        }, "is_tameable");

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
        registerSpawnedOnlyTag("ageable", (attribute, object) -> {
            return new ElementTag(EntityAge.describes(object));
        }, "is_ageable");

        // <--[tag]
        // @attribute <EntityTag.colorable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity can be colored.
        // If this returns true, it will enable access to:
        // <@link mechanism EntityTag.color> and <@link tag EntityTag.color>
        // -->
        registerSpawnedOnlyTag("colorable", (attribute, object) -> {
            return new ElementTag(EntityColor.describes(object));
        }, "is_colorable");

        // <--[tag]
        // @attribute <EntityTag.experience>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.experience
        // @group properties
        // @description
        // Returns the experience value of this experience orb entity.
        // -->
        registerSpawnedOnlyTag("experience", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof ExperienceOrb)) {
                return null;
            }
            return new ElementTag(((ExperienceOrb) object.getBukkitEntity()).getExperience());
        });

        // <--[tag]
        // @attribute <EntityTag.fuse_ticks>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.fuse_ticks
        // @group properties
        // @description
        // Returns the number of ticks until the explosion of the primed TNT.
        // -->
        registerSpawnedOnlyTag("fuse_ticks", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof TNTPrimed)) {
                return null;
            }
            return new ElementTag(((TNTPrimed) object.getBukkitEntity()).getFuseTicks());
        });

        // <--[tag]
        // @attribute <EntityTag.dragon_phase>
        // @returns ElementTag
        // @mechanism EntityTag.dragon_phase
        // @group properties
        // @description
        // Returns the phase an EnderDragon is currently in.
        // Valid phases: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EnderDragon.Phase.html>
        // -->
        registerSpawnedOnlyTag("dragon_phase", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof EnderDragon)) {
                return null;
            }
            return new ElementTag(((EnderDragon) object.getLivingEntity()).getPhase().name());
        });

        // <--[tag]
        // @attribute <EntityTag.weapon_damage[(<entity>)]>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the amount of damage the entity will do based on its held item.
        // Optionally, specify a target entity to test how much damage will be done to that specific target
        // (modified based on enchantments and that entity's armor/status/etc).
        // Note that the result will not always be completely exact, as it doesn't take into account some specific factors
        // (eg sweeping vs single-hit, etc).
        // -->
        registerSpawnedOnlyTag("weapon_damage", (attribute, object) -> {
            Entity target = null;
            if (attribute.hasContext(1)) {
                target = valueOf(attribute.getContext(1)).getBukkitEntity();
            }
            return new ElementTag(NMSHandler.getEntityHelper().getDamageTo(object.getLivingEntity(), target));
        });

        // <--[tag]
        // @attribute <EntityTag.describe>
        // @returns ElementTag
        // @group properties
        // @description
        // Returns the entity's full description, including all properties.
        // -->
        registerTag("describe", (attribute, object) -> {
            String escript = object.getEntityScript();
            return new ElementTag("e@" + (escript != null && escript.length() > 0 ? escript : object.getEntityType().getLowercaseName())
                    + PropertyParser.getPropertiesString(object));
        });
    }

    public static ObjectTagProcessor<EntityTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerSpawnedOnlyTag(String name, TagRunnable.ObjectInterface<EntityTag> runnable, String... variants) {
        TagRunnable.ObjectInterface<EntityTag> newRunnable = (attribute, object) -> {
            if (!object.isSpawnedOrValidForTag()) {
                if (!attribute.hasAlternative()) {
                    com.denizenscript.denizen.utilities.debugging.Debug.echoError("Entity is not spawned, but tag '" + attribute.getAttributeWithoutContext(1) + "' requires the entity be spawned, for entity: " + object.debuggable());
                }
                return null;
            }
            return runnable.run(attribute, object);
        };
        registerTag(name, newRunnable, variants);
    }

    public static void registerTag(String name, TagRunnable.ObjectInterface<EntityTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
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
            getLivingEntity().getEquipment().setItemInMainHand(mechanism.valueAsType(ItemTag.class).getItemStack());
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
            getLivingEntity().getEquipment().setItemInOffHand(mechanism.valueAsType(ItemTag.class).getItemStack());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name attach_to
        // @input EntityTag(|dLocation(|ElementTag(Boolean)))
        // @description
        // Attaches this entity's client-visible motion to another entity.
        // Optionally, specify an offset vector as well.
        // Optionally specify a boolean indicating whether offset should match the target entity's rotation (defaults to true).
        // Note that because this is client-visible motion, it does not take effect server-side. You may wish to occasionally teleport the entity to its attachment.
        // Note that if a player is involved as either input entity, that player will not see the attachment - only other players will.
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Decimal)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // <EntityTag.is_leashed>
        // <EntityTag.leash_holder>
        // -->
        if (mechanism.matches("leash_holder") && mechanism.requireObject(EntityTag.class)) {
            getLivingEntity().setLeashHolder(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name can_breed
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // <EntityTag.is_empty>
        // -->
        if (mechanism.matches("passengers")) {
            entity.eject();
            for (EntityTag ent : mechanism.valueAsType(ListTag.class).filter(EntityTag.class, mechanism.context)) {
                if (comparesTo(ent) == 1) {
                    continue;
                }
                if (!ent.isSpawned()) {
                    ent.spawnAt(getLocation());
                }
                if (ent.isSpawned()) {
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
        // <EntityTag.is_empty>
        // -->
        if (mechanism.matches("passenger") && mechanism.requireObject(EntityTag.class)) {
            EntityTag ent = mechanism.valueAsType(EntityTag.class);
            if (!ent.isSpawned()) {
                ent.spawnAt(getLocation());
            }
            entity.eject();
            if (ent.isSpawned()) {
                entity.addPassenger(ent.getBukkitEntity());
            }
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
        // @name absorption_health
        // @input ElementTag(Decimal)
        // @description
        // Sets the living entity's absorption health.
        // @tags
        // <EntityTag.absorption_health>
        // -->
        if (mechanism.matches("absorption_health") && mechanism.requireFloat()) {
            NMSHandler.getEntityHelper().setAbsorption(getLivingEntity(), mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name oxygen
        // @input DurationTag
        // @description
        // Sets how much air the entity has remaining before it drowns.
        // The entity must be living.
        // @tags
        // <EntityTag.oxygen>
        // <EntityTag.max_oxygen>
        // -->
        if (mechanism.matches("oxygen") && mechanism.requireObject(DurationTag.class)) {
            getLivingEntity().setRemainingAir(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        if (mechanism.matches("remaining_air") && mechanism.requireInteger()) {
            Deprecations.entityRemainingAir.warn(mechanism.context);
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Number)
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
        // @input ElementTag(Number)
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
        // @input ElementTag(Boolean)
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
        // @input ElementTag(Boolean)
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
