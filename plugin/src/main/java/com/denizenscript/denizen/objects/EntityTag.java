package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import com.denizenscript.denizen.nms.interfaces.FakePlayer;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.npc.traits.MirrorTrait;
import com.denizenscript.denizen.objects.properties.entity.EntityAge;
import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizen.objects.properties.entity.EntityTame;
import com.denizenscript.denizen.scripts.commands.player.DisguiseCommand;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizen.utilities.entity.HideEntitiesHelper;
import com.denizenscript.denizen.utilities.flags.DataPersistenceFlagTracker;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EntityTag implements ObjectTag, Adjustable, EntityFormObject, FlaggableObject, Cloneable {

    // <--[ObjectType]
    // @name EntityTag
    // @prefix e
    // @base ElementTag
    // @implements FlaggableObject, PropertyHolderObject
    // @ExampleTagBase player
    // @ExampleValues <player>,<npc>
    // @ExampleForReturns
    // - kill %VALUE%
    // @ExampleForReturns
    // - heal %VALUE%
    // @ExampleForReturns
    // - remove %VALUE%
    // @format
    // The identity format for entities is a spawned entity's UUID, or an entity type.
    // For example, 'e@abc123' or 'e@zombie'.
    //
    // @description
    // An EntityTag represents a spawned entity, or a generic entity type.
    //
    // Note that players and NPCs are valid EntityTags, but are generally represented by the more specific
    // PlayerTag and NPCTag objects.
    //
    // Note that a spawned entity can be a living entity (a player, NPC, or mob) or a nonliving entity (a painting, item frame, etc).
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the world chunk files as a part of the entity's NBT.
    //
    // @Matchable
    // EntityTag matchers, sometimes identified as "<entity>", "<projectile>", or "<vehicle>":
    // "entity" plaintext: always matches.
    // "player" plaintext: matches any real player (not NPCs).
    // "npc" plaintext: matches any Citizens NPC.
    // "vehicle" plaintext: matches for any vehicle type (minecarts, boats, horses, etc).
    // "fish" plaintext: matches for any fish type (cod, pufferfish, etc).
    // "projectile" plaintext: matches for any projectile type (arrow, trident, fish hook, snowball, etc).
    // "hanging" plaintext: matches for any hanging type (painting, item_frame, etc).
    // "monster" plaintext: matches for any monster type (creepers, zombies, etc).
    // "animal" plaintext: matches for any animal type (pigs, cows, etc).
    // "mob" plaintext: matches for any mob type (creepers, pigs, etc).
    // "living" plaintext: matches for any living type (players, pigs, creepers, etc).
    // "vanilla_tagged:<tag_name>": matches if the given vanilla tag applies to the entity. Allows advanced matchers, for example: "vanilla_tagged:axolotl_*".
    // "entity_flagged:<flag>": a Flag Matchable for EntityTag flags.
    // "player_flagged:<flag>": a Flag Matchable for PlayerTag flags (will never match non-players).
    // "npc_flagged:<flag>": a Flag Matchable for NPCTag flags (will never match non-NPCs).
    // "npc_<type>": matches if the NPC is the given entity type (like "npc_cow" or "npc_mob" or "npc_player").
    // Any entity type name: matches if the entity is of the given type, using advanced matchers.
    //
    // -->

    /////////////////////
    //   STATIC METHODS
    /////////////////

    // List a mechanism here if it can be safely run before spawn.
    public static HashSet<String> earlyValidMechanisms = new HashSet<>(Arrays.asList(
            "max_health", "health_data", "health",
            "visible", "armor_pose", "arms", "base_plate", "is_small", "marker",
            "velocity", "age", "is_using_riptide", "size", "item"
    ));
    // Definitely not valid: "item"

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
        if (!(entity instanceof NPCHolder)) {
            return false;
        }
        NPC npc = ((NPCHolder) entity).getNPC();
        if (npc == null) {
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
            ItemStack its = getLivingEntity().getEquipment().getItemInMainHand();
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

    @Deprecated
    public static EntityTag valueOf(String string) {
        return valueOf(string, null);
    }

    public static boolean allowDespawnedNpcs = false;

    @Fetchable("e")
    public static EntityTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        UUID id = null;
        if (string.startsWith("e@") && !string.startsWith("e@fake")) {
            int slash = string.indexOf('/');
            if (slash != -1) {
                try {
                    id = UUID.fromString(string.substring(2, slash));
                    string = string.substring(slash + 1);
                    Entity entity = getEntityForID(id);
                    if (entity != null) {
                        EntityTag result = new EntityTag(entity);
                        if (string.equalsIgnoreCase(result.getEntityScript())
                                || string.equalsIgnoreCase(result.getBukkitEntityType().name())) {
                            return result;
                        }
                        else if (context == null || context.showErrors()) {
                            Debug.echoError("Invalid EntityTag! ID '" + id + "' is valid, but '" + string + "' does not match its type data.");
                        }
                    }
                }
                catch (Exception ex) {
                    // DO NOTHING
                }
            }
        }
        if (ObjectFetcher.isObjectWithProperties(string)) {
            return ObjectFetcher.getObjectFromWithProperties(EntityTag.class, string, context);
        }
        string = CoreUtilities.toLowerCase(string);
        if (string.startsWith("e@")) {
            if (string.startsWith("e@fake:")) {
                try {
                    UUID entityID = UUID.fromString(string.substring("e@fake:".length()));
                    FakeEntity entity = FakeEntity.idsToEntities.get(entityID);
                    if (entity != null) {
                        return entity.entity;
                    }
                    return null;
                }
                catch (Exception ex) {
                    // DO NOTHING
                }
            }
            string = string.substring("e@".length());
        }
        // Choose a random entity type if "random" is used
        if (string.equals("random")) {
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
        // NPC entity
        if (string.startsWith("n@")) {
            NPCTag npc = NPCTag.valueOf(string, context);
            if (npc != null) {
                if (npc.isSpawned()) {
                    return new EntityTag(npc);
                }
                else {
                    if (!allowDespawnedNpcs && context != null && context.showErrors()) {
                        Debug.echoDebug(context, "NPC '" + string + "' is not spawned, errors may follow!");
                    }
                    return new EntityTag(npc);
                }
            }
            else {
                if (context == null || context.debug || CoreConfiguration.debugOverride) {
                    Debug.echoError("NPC '" + string + "' does not exist!");
                }
            }
        }
        // Player entity
        else if (string.startsWith("p@")) {
            PlayerTag returnable = PlayerTag.valueOf(string, context);
            if (returnable != null && returnable.isOnline()) {
                return new EntityTag(returnable.getPlayerEntity());
            }
            else if (context == null || context.showErrors()) {
                Debug.echoError("Invalid Player! '" + string + "' could not be found. Has the player logged off?");
            }
        }
        if (ScriptRegistry.containsScript(string, EntityScriptContainer.class)) {
            // Construct a new custom unspawned entity from script
            EntityTag entity = ScriptRegistry.getScriptContainerAs(string, EntityScriptContainer.class).getEntityFrom();
            entity.uuid = id;
            return entity;
        }
        List<String> data = CoreUtilities.split(string, ',');
        // Handle custom DenizenEntityTypes
        DenizenEntityType type = DenizenEntityType.getByName(data.get(0));
        if (type != null && type.getBukkitEntityType() != EntityType.UNKNOWN) {
            EntityTag entity = new EntityTag(type, data.size() > 1 ? data.get(1) : null);
            entity.uuid = id;
            return entity;
        }
        try {
            UUID entityID = id != null ? id : UUID.fromString(string);
            Entity entity = getEntityForID(entityID);
            if (entity != null) {
                return new EntityTag(entity);
            }
            return null;
        }
        catch (Exception ex) {
            // DO NOTHING
        }
        if (context == null || context.showErrors()) {
            Debug.log("valueOf EntityTag returning null: " + string);
        }
        return null;
    }

    public static Entity getEntityForID(UUID id) {
        if (rememberedEntities.containsKey(id)) {
            return rememberedEntities.get(id);
        }
        for (World world : Bukkit.getWorlds()) {
            Entity entity = NMSHandler.entityHelper.getEntity(world, id);
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
        arg = CoreUtilities.toUpperCase(arg.replace("e@", ""));
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

    @Override
    public EntityTag duplicate() {
        if (isUnique()) {
            return this;
        }
        try {
            EntityTag copy = (EntityTag) clone();
            if (copy.mechanisms != null) {
                copy.mechanisms = new ArrayList<>(copy.mechanisms);
            }
            return copy;
        }
        catch (CloneNotSupportedException ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        if (isCitizensNPC()) {
            return getDenizenNPC().getFlagTracker();
        }
        else if (isPlayer()) {
            return getDenizenPlayer().getFlagTracker();
        }
        Entity ent = getBukkitEntity();
        if (ent != null) {
            return new DataPersistenceFlagTracker(ent);
        }
        else {
            // TODO: Warning?
            return null;
        }
    }

    @Override
    public String getReasonNotFlaggable() {
        if (!isSpawned() || getBukkitEntity() == null) {
            return "the entity is not spawned";
        }
        return "unknown reason - something went wrong";
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        if (CoreConfiguration.skipAllFlagCleanings) {
            return;
        }
        if (cleanRateProtect + 60000 > DenizenCore.serverTimeMillis) {
            tracker.doTotalClean();
            cleanRateProtect = DenizenCore.serverTimeMillis;
        }
    }

    @Override
    public boolean isTruthy() {
        return isSpawnedOrValidForTag();
    }

    public Entity entity = null;
    public long cleanRateProtect = -60000;
    public DenizenEntityType entity_type = null;
    private String data1 = null;
    private NPCTag npc = null;
    public UUID uuid = null;
    private String entityScript = null;
    public boolean isFake = false;
    public boolean isFakeValid = false;

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
        if (uuid == null && entity != null) {
            uuid = entity.getUniqueId();
        }
        return uuid;
    }

    @Override
    public EntityTag getDenizenEntity() {
        return this;
    }

    public EntityFormObject getDenizenObject() {
        if (entity == null && npc == null) {
            return this;
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
        if (uuid != null && (entity == null || !entity.isValid())) {
            if (!isFake) {
                Entity backup = Bukkit.getEntity(uuid);
                if (backup != null) {
                    entity = backup;
                }
            }
        }
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
        return entity instanceof LivingEntity;
    }

    public boolean isLivingEntityType() {
        if (getBukkitEntity() == null && entity_type != null) {
            return entity_type.getBukkitEntityType().isAlive();
        }
        return entity instanceof LivingEntity;
    }

    public boolean isMonsterType() {
        if (getBukkitEntity() == null && entity_type != null) {
            return Monster.class.isAssignableFrom(entity_type.getBukkitEntityType().getEntityClass());
        }
        return getBukkitEntity() instanceof Monster;
    }

    public boolean isMobType() {
        if (getBukkitEntity() == null && entity_type != null) {
            return Mob.class.isAssignableFrom(entity_type.getBukkitEntityType().getEntityClass());
        }
        return getBukkitEntity() instanceof Mob;
    }

    public boolean isAnimalType() {
        if (getBukkitEntity() == null && entity_type != null) {
            return Animals.class.isAssignableFrom(entity_type.getBukkitEntityType().getEntityClass());
        }
        return getBukkitEntity() instanceof Animals;
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
        if (entity == null) {
            return entity_type.getBukkitEntityType() == EntityType.PLAYER && npc == null;
        }
        return entity instanceof Player && !isNPC();
    }

    public Projectile getProjectile() {
        return (Projectile) entity;
    }

    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    public EntityTag getShooter() {
        if (getBukkitEntity() instanceof TNTPrimed) {
            Entity source = ((TNTPrimed) getBukkitEntity()).getSource();
            if (source != null) {
                return new EntityTag(source);
            }
        }
        else if (isProjectile()) {
            ProjectileSource shooter = getProjectile().getShooter();
            if (shooter instanceof Entity) {
                return new EntityTag((Entity) shooter);
            }
        }
        return null;
    }

    public void setShooter(EntityTag shooter) {
        if (getBukkitEntity() instanceof TNTPrimed) {
            ((TNTPrimed) getBukkitEntity()).setSource(shooter.getBukkitEntity());
        }
        else if (isProjectile() && shooter.isLivingEntity()) {
            getProjectile().setShooter(shooter.getLivingEntity());
        }
    }

    public boolean hasShooter() {
        return getShooter() != null;
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
        return hasInventory() ? isCitizensNPC() ? getDenizenNPC().getDenizenInventory() : InventoryTag.mirrorBukkitInventory(getBukkitInventory()) : null;
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
        String customName = entity == null ? null : entity.getCustomName();
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
        Entity entity = getBukkitEntity();
        if (entity != null) {
            return new LocationTag(entity.getLocation());
        }

        return null;
    }

    public LocationTag getEyeLocation() {
        Entity entity = getBukkitEntity();
        if (entity == null) {
            return null;
        }
        if (isPlayer()) {
            return new LocationTag(getPlayer().getEyeLocation());
        }
        else if (!isGeneric() && isLivingEntity()) {
            return new LocationTag(getLivingEntity().getEyeLocation());
        }
        else if (!isGeneric()) {
            return new LocationTag(entity.getLocation());
        }

        return null;
    }

    public Location getTargetBlockSafe(Set<Material> mats, int range) {
        try {
            NMSHandler.chunkHelper.changeChunkServerThread(getWorld());
            return getLivingEntity().getTargetBlock(mats, range).getLocation();
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(getWorld());
        }
    }

    public Vector getVelocity() {
        Entity entity = getBukkitEntity();
        if (entity == null) {
            return null;
        }
        return entity.getVelocity();
    }

    public void setVelocity(Vector vector) {
        Entity entity = getBukkitEntity();
        if (entity == null) {
            return;
        }
        entity.setVelocity(vector);
    }

    public World getWorld() {
        Entity entity = getBukkitEntity();
        if (entity == null) {
            return null;
        }
        return entity.getWorld();
    }

    public void spawnAt(Location location) {
        spawnAt(location, TeleportCause.PLUGIN, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public void spawnAt(Location location, TeleportCause cause, CreatureSpawnEvent.SpawnReason reason) {
        if (location.getWorld() == null) {
            Debug.echoError("Cannot teleport or spawn entity at location '" + new LocationTag(location) + "' because it is missing a world.");
            return;
        }
        // If the entity is already spawned, teleport it.
        if (isCitizensNPC() || (isUnique() && entity != null)) {
            teleport(location, cause);
            return;
        }
        else if (entity_type == null) {
            Debug.echoError("Cannot spawn a null EntityTag!");
            return;
        }
        if (entity_type.getBukkitEntityType() == EntityType.PLAYER && !entity_type.isCustom()) {
            if (Depends.citizens == null) {
                Debug.echoError("Cannot spawn entity of type PLAYER!");
                return;
            }
            NPCTag npc = new NPCTag(net.citizensnpcs.api.CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, data1));
            npc.getCitizen().spawn(location);
            entity = npc.getEntity();
        }
        else if (entity_type.getBukkitEntityType() == EntityType.FALLING_BLOCK) {
            MaterialTag material = null;
            if (data1 != null && MaterialTag.matches(data1)) {
                material = MaterialTag.valueOf(data1, CoreUtilities.basicContext);
                // If we did not get a block with "RANDOM", or we got
                // air or portals, keep trying
                while (data1.equalsIgnoreCase("RANDOM") &&
                        ((!material.getMaterial().isBlock()) ||
                                material.getMaterial() == Material.AIR ||
                                material.getMaterial() == Material.NETHER_PORTAL ||
                                material.getMaterial() == Material.END_PORTAL)) {
                    material = MaterialTag.valueOf(data1, CoreUtilities.basicContext);
                }
            }
            else {
                for (Mechanism mech : mechanisms) {
                    if (mech.getName().equals("fallingblock_type")) {
                        material = mech.valueAsType(MaterialTag.class);
                        mechanisms.remove(mech);
                        break;
                    }
                }
            }
            // If material is null or not a block, default to SAND
            if (material == null || !material.getMaterial().isBlock() || !material.hasModernData()) {
                material = new MaterialTag(Material.SAND);
            }
            // This is currently the only way to spawn a falling block
            entity = location.getWorld().spawnFallingBlock(location, material.getModernData());
        }
        else if (entity_type.getBukkitEntityType() == EntityType.PAINTING) {
            entity = entity_type.spawnNewEntity(location, mechanisms, entityScript, reason);
            location = location.clone();
            Painting painting = (Painting) entity;
            Art art = null;
            BlockFace face = null;
            try {
                for (Mechanism mech : mechanisms) {
                    if (mech.getName().equals("painting")) {
                        art = Art.valueOf(mech.getValue().asString().toUpperCase());
                    }
                    else if (mech.getName().equals("rotation")) {
                        face = BlockFace.valueOf(mech.getValue().asString().toUpperCase());
                    }
                }
            }
            catch (Exception ex) {
                // ignore
            }
            if (art != null && face != null) { // Paintings are the worst
                if (art.getBlockHeight() % 2 == 0) {
                    location.subtract(0, 1, 0);
                }
                if (art.getBlockWidth() % 2 == 0) {
                    if (face == BlockFace.WEST) {
                        location.subtract(0, 0, 1);
                    }
                    else if (face == BlockFace.SOUTH) {
                        location.subtract(1, 0, 0);
                    }
                }
                painting.teleport(location);
                painting.setFacingDirection(face, true);
                painting.setArt(art, true);
            }
        }
        else {
            entity = entity_type.spawnNewEntity(location, mechanisms, entityScript, reason);
        }
        if (entity == null) {
            if (!new LocationTag(location).isChunkLoaded()) {
                Debug.echoError("Error spawning entity - tried to spawn in an unloaded chunk.");
            }
            else {
                Debug.echoError("Error spawning entity - bad entity type, or blocked by another plugin?");
            }
            return;
        }
        uuid = entity.getUniqueId();
        if (entityScript != null) {
            EntityScriptHelper.setEntityScript(entity, entityScript);
        }
        for (Mechanism mechanism : mechanisms) {
            safeAdjust(new Mechanism(mechanism.getName(), mechanism.value, mechanism.context));
        }
        mechanisms.clear();
    }

    public boolean isSpawnedOrValidForTag() {
        if (isFake) {
            return true;
        }
        if (entity == null) { // Note: this breaks thread-patch for entities that need revalidating
            if (uuid == null) {
                return false;
            }
            return isValid() || rememberedEntities.containsKey(uuid);
        }
        NMSHandler.chunkHelper.changeChunkServerThread(entity.getWorld());
        try {
            return isValid() || rememberedEntities.containsKey(entity.getUniqueId());
        }
        finally {
            NMSHandler.chunkHelper.restoreServerThread(entity.getWorld());
        }
    }

    public boolean isSpawned() {
        return isValid();
    }

    public boolean isValid() {
        Entity entity = getBukkitEntity();
        return entity != null && (entity.isValid() || (isFake && isFakeValid));
    }

    public void remove() {
        entity.remove();
    }

    public void teleport(Location location) {
        teleport(location, TeleportCause.PLUGIN);
    }

    public void teleport(Location location, TeleportCause cause) {
        if (location.getWorld() == null) {
            Debug.echoError("Cannot teleport or spawn entity at location '" + new LocationTag(location) + "' because it is missing a world.");
            return;
        }
        if (isCitizensNPC()) {
            if (getDenizenNPC().getCitizen().isSpawned()) {
                getDenizenNPC().getCitizen().teleport(location, cause);
            }
            else {
                if (getDenizenNPC().getCitizen().spawn(location)) {
                    entity = getDenizenNPC().getCitizen().getEntity();
                    uuid = getDenizenNPC().getCitizen().getEntity().getUniqueId();
                }
                else {
                    if (new LocationTag(location).isChunkLoaded()) {
                        Debug.echoError("Error spawning NPC - tried to spawn in an unloaded chunk.");
                    }
                    else {
                        Debug.echoError("Error spawning NPC - blocked by plugin");
                    }
                }
            }
        }
        else if (isFake) {
            NMSHandler.entityHelper.snapPositionTo(entity, location.toVector());
            NMSHandler.entityHelper.look(entity, location.getYaw(), location.getPitch());
        }
        else {
            getBukkitEntity().teleport(location, cause);
            if (entity.getWorld().equals(location.getWorld())) { // Force the teleport through (for things like mounts)
                NMSHandler.entityHelper.teleport(entity, location);
            }
        }
    }

    /**
     * Make this entity target another living entity, attempting both
     * old entity AI and new entity AI targeting methods
     *
     * @param target The LivingEntity target
     */

    public void target(LivingEntity target) {
        if (!isSpawned()) {
            return;
        }
        if (entity instanceof Creature) {
            NMSHandler.entityHelper.setTarget((Creature) entity, target);
        }
        else if (entity instanceof ShulkerBullet) {
            ((ShulkerBullet) entity).setTarget(target);
        }
        else {
            Debug.echoError(identify() + " is not an entity type that can hold a target!");
        }
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
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
            else if (isFake) {
                return "<LG>e@<Y>FAKE: " + getUUID() + "<GR>(FAKE-" + entity.getType().name() + "/" + entity.getName() + ")";
            }
            else if (isSpawnedOrValidForTag()) {
                return "<LG>e@<Y> " + getUUID() + "<GR>(" + entity.getType().name() + "/" + entity.getName() + ")";
            }
        }
        return identify(this::getWaitingMechanismsDebuggable);
    }

    @Override
    public String savable() {
        if (npc != null) {
            return npc.savable();
        }
        else if (isPlayer()) {
            return getDenizenPlayer().savable();
        }
        else if (isFake) {
            return "e@fake:" + getUUID();
        }
        else {
            return identify();
        }
    }

    @Override
    public String identify() {
        return identify(this::getWaitingMechanismsString);
    }

    public String identify(Supplier<String> mechsHandler) {
        if (npc != null) {
            return npc.identify();
        }
        if (isFake) {
            return "e@fake:" + getUUID();
        }
        if (getUUID() != null) {
            if (isPlayer()) {
                return getDenizenPlayer().identify();
            }
            if (entityScript != null) {
                return "e@" + getUUID() + "/" + entityScript + mechsHandler.get();
            }
            if (entity_type != null) {
                return "e@" + getUUID() + "/" + entity_type.getLowercaseName() + mechsHandler.get();
            }
        }
        if (entityScript != null) {
            return "e@" + entityScript + mechsHandler.get();
        }
        if (entity_type != null) {
            return "e@" + entity_type.getLowercaseName() + mechsHandler.get();
        }
        return "null";
    }

    public String getWaitingMechanismsDebuggable() {
        StringBuilder properties = new StringBuilder();
        for (Mechanism mechanism : mechanisms) {
            properties.append(mechanism.getName()).append(" <LG>=<Y> ").append(mechanism.getValue().asString()).append("<LG>; <Y>");
        }
        if (properties.length() > 0) {
            return "<LG>[<Y>" + properties.substring(0, properties.length() - "; <Y>".length()) + " <LG>]";
        }
        return "";
    }

    public String getWaitingMechanismsString() {
        StringBuilder properties = new StringBuilder();
        for (Mechanism mechanism : mechanisms) {
            properties.append(PropertyParser.escapePropertyKey(mechanism.getName())).append("=").append(PropertyParser.escapePropertyValue(mechanism.getValue().asString())).append(";");
        }
        if (properties.length() > 0) {
            return "[" + properties.substring(0, properties.length() - 1) + "]";
        }
        return "";
    }

    @Override
    public String identifySimple() {
        if (npc != null && npc.isValid()) {
            return "n@" + npc.getId();
        }
        if (isPlayer()) {
            return "p@" + getPlayer().getName();
        }
        if (entityScript != null) {
            return "e@" + entityScript;
        }
        if (entity_type != null) {
            return "e@" + entity_type.getLowercaseName();
        }
        return "null";
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public Object getJavaObject() {
        return entity == null ? getBukkitEntityType() : entity;
    }

    @Override
    public boolean isUnique() {
        return entity != null || uuid != null || isFake || npc != null;
    }

    public LocationTag doLocationTag(Attribute attribute) {
        if (attribute.startsWith("cursor_on", 2)) {
            BukkitImplDeprecations.entityLocationCursorOnTag.warn(attribute.context);
            int range = attribute.getIntContext(2);
            if (range < 1) {
                range = 50;
            }
            Set<Material> set = new HashSet<>();
            set.add(Material.AIR);

            if (attribute.startsWith("ignore", 3) && attribute.hasContext(3)) {
                List<MaterialTag> ignoreList = attribute.contextAsType(3, ListTag.class).filter(MaterialTag.class, attribute.context);
                for (MaterialTag material : ignoreList) {
                    set.add(material.getMaterial());
                }
                attribute.fulfill(1);
            }
            attribute.fulfill(1);
            return new LocationTag(getTargetBlockSafe(set, range));
        }

        if (attribute.startsWith("standing_on", 2)) {
            BukkitImplDeprecations.entityStandingOn.warn(attribute.context);
            attribute.fulfill(1);
            return new LocationTag(getBukkitEntity().getLocation().clone().add(0, -0.5f, 0));
        }
        return new LocationTag(getBukkitEntity().getLocation());
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);
        PropertyParser.registerPropertyTagHandlers(EntityTag.class, tagProcessor);

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
        tagProcessor.registerTag(ElementTag.class, "entity_type", (attribute, object) -> {
            return new ElementTag(object.entity_type.getName());
        });

        // <--[tag]
        // @attribute <EntityTag.translated_name>
        // @returns ElementTag
        // @description
        // Returns the localized name of the entity.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "translated_name", (attribute, object) -> {
            String key = object.getEntityType().getBukkitEntityType().getKey().getKey();
            return new ElementTag(ChatColor.COLOR_CHAR + "[translate=entity.minecraft." + key + "]");
        });

        // <--[tag]
        // @attribute <EntityTag.vanilla_tags>
        // @returns ListTag
        // @description
        // Returns a list of vanilla tags that apply to this entity type. See also <@link url https://minecraft.fandom.com/wiki/Tag>.
        // -->
        tagProcessor.registerTag(ListTag.class, "vanilla_tags", (attribute, object) -> {
            HashSet<String> tags = VanillaTagHelper.tagsByEntity.get(object.getBukkitEntityType());
            if (tags == null) {
                return new ListTag();
            }
            return new ListTag(tags);
        });

        // <--[tag]
        // @attribute <EntityTag.is_spawned>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is spawned.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_spawned", (attribute, object) -> {
            return new ElementTag(object.isSpawned());
        });

        // <--[tag]
        // @attribute <EntityTag.eid>
        // @returns ElementTag(Number)
        // @group data
        // @description
        // Returns the entity's temporary server entity ID.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "eid", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().getEntityId());
        });

        // <--[tag]
        // @attribute <EntityTag.uuid>
        // @returns ElementTag
        // @group data
        // @description
        // Returns the permanent unique ID of the entity.
        // Works with offline players.
        // -->
        tagProcessor.registerTag(ElementTag.class, "uuid", (attribute, object) -> {
            return new ElementTag(object.getUUID().toString());
        });

        // <--[tag]
        // @attribute <EntityTag.script>
        // @returns ScriptTag
        // @group data
        // @description
        // Returns the entity script that spawned this entity, if any.
        // -->
        tagProcessor.registerTag(ScriptTag.class, "script", (attribute, object) -> {
            if (object.entityScript == null) {
                return null;
            }
            return ScriptTag.valueOf(object.entityScript, CoreUtilities.noDebugContext);
        });

        // <--[tag]
        // @attribute <EntityTag.scriptname>
        // @returns ElementTag
        // @deprecated use ".script.name" instead.
        // @group data
        // @description
        // Use ".script.name" instead.
        // -->
        tagProcessor.registerTag(ElementTag.class, "scriptname", (attribute, object) -> {
            BukkitImplDeprecations.hasScriptTags.warn(attribute.context);
            if (object.entityScript == null) {
                return null;
            }
            return new ElementTag(object.entityScript);
        });

        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        registerSpawnedOnlyTag(ObjectTag.class, "custom_id", (attribute, object) -> {
            BukkitImplDeprecations.entityCustomIdTag.warn(attribute.context);
            if (CustomNBT.hasCustomNBT(object.getLivingEntity(), "denizen-script-id")) {
                return ScriptTag.valueOf(CustomNBT.getCustomNBT(object.getLivingEntity(), "denizen-script-id"), CoreUtilities.noDebugContext);
            }
            else {
                return new ElementTag(object.getBukkitEntity().getType().name());
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
        registerSpawnedOnlyTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.getName(), true);
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
        registerSpawnedOnlyTag(ItemTag.class, "saddle", (attribute, object) -> {
            if (object.getLivingEntity() instanceof AbstractHorse) {
                return new ItemTag(((AbstractHorse) object.getLivingEntity()).getInventory().getSaddle());
            }
            else if (object.getLivingEntity() instanceof Steerable) {
                return new ItemTag(((Steerable) object.getLivingEntity()).hasSaddle() ? Material.SADDLE : Material.AIR);
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
        registerSpawnedOnlyTag(ItemTag.class, "horse_armor", (attribute, object) -> {
            if (object.getLivingEntity() instanceof Horse) {
                return new ItemTag(((Horse) object.getLivingEntity()).getInventory().getArmor());
            }
            return null;
        }, "horse_armour");

        // <--[tag]
        // @attribute <EntityTag.has_saddle>
        // @returns ElementTag(Boolean)
        // @group inventory
        // @description
        // If the entity is a pig or horse, returns whether it has a saddle equipped.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "has_saddle", (attribute, object) -> {
            if (object.getLivingEntity() instanceof AbstractHorse) {
                return new ElementTag(((AbstractHorse) object.getLivingEntity()).getInventory().getSaddle().getType() == Material.SADDLE);
            }
            else if (object.getLivingEntity() instanceof Steerable) {
                return new ElementTag(((Steerable) object.getLivingEntity()).hasSaddle());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.is_trading>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the villager entity is trading.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_trading", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Merchant) {
                return new ElementTag(((Merchant) object.getBukkitEntity()).isTrading());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.trading_with>
        // @returns PlayerTag
        // @description
        // Returns the player who is trading with the villager entity, or null if it is not trading.
        // -->
        registerSpawnedOnlyTag(EntityFormObject.class, "trading_with", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Merchant && ((Merchant) object.getBukkitEntity()).getTrader() != null) {
                return new EntityTag(((Merchant) object.getBukkitEntity()).getTrader()).getDenizenObject();
            }
            return null;
        });

        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <EntityTag.trace_framed_map>
        // @returns MapTag
        // @group location
        // @description
        // Returns information at the framed item of a filled map that an entity is currently looking at, if any.
        // The map contains key "x" and "y" as coordinates in the range of 0 to 128. These will automatically correct for rotation, if the framed item is rotated.
        // The map contains "entity" as the EntityTag of the item frame.
        // The map also contains "map" as the ID of the targeted map.
        // Returns null if the entity is not looking at an item_frame holding a filled_map.
        // -->
        registerSpawnedOnlyTag(MapTag.class, "trace_framed_map", (attribute, object) -> {
            return NMSHandler.entityHelper.mapTrace(object.getLivingEntity());
        });

        // <--[tag]
        // @attribute <EntityTag.map_trace>
        // @returns LocationTag
        // @group location
        // @deprecated use EntityTag.trace_framed_map
        // @description
        // Deprecated in favor of <@link tag EntityTag.trace_framed_map>
        // -->
        registerSpawnedOnlyTag(LocationTag.class, "map_trace", (attribute, object) -> {
            BukkitImplDeprecations.entityMapTraceTag.warn(attribute.context);
            MapTag result = NMSHandler.entityHelper.mapTrace(object.getLivingEntity());
            if (result == null) {
                return null;
            }
            return new LocationTag(null, result.getElement("x").asDouble(), result.getElement("y").asDouble());
        });

        // <--[tag]
        // @attribute <EntityTag.can_see[<entity>]>
        // @returns ElementTag(Boolean)
        // @group location
        // @description
        // Returns whether the entity can see the specified other entity (has an uninterrupted line-of-sight).
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "can_see", (attribute, object) -> {
            if (object.isLivingEntity() && attribute.hasParam() && EntityTag.matches(attribute.getParam())) {
                EntityTag toEntity = attribute.paramAsType(EntityTag.class);
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
        registerSpawnedOnlyTag(LocationTag.class, "eye_location", (attribute, object) -> {
            return new LocationTag(object.getEyeLocation());
        });

        // <--[tag]
        // @attribute <EntityTag.eye_height>
        // @returns ElementTag(Number)
        // @group location
        // @description
        // Returns the height of the entity's eyes above its location.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "eye_height", (attribute, object) -> {
            if (object.isLivingEntity()) {
                return new ElementTag(object.getLivingEntity().getEyeHeight());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.cursor_on_solid[(<range>)]>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the solid block the entity is looking at.
        // Optionally, specify a maximum range to find the location from (defaults to 200).
        // Note that this will return null if there is no solid block in range.
        // This only uses solid blocks, ie it ignores passable blocks like tall-grass. Use <@link tag EntityTag.cursor_on> to include passable blocks.
        // Equivalent to <EntityTag.eye_location.ray_trace[return=block]>
        // -->
        registerSpawnedOnlyTag(LocationTag.class, "cursor_on_solid", (attribute, object) -> {
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 200;
            }
            RayTraceResult traced = object.getWorld().rayTraceBlocks(object.getEyeLocation(), object.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true);
            if (traced != null && traced.getHitBlock() != null) {
                return new LocationTag(traced.getHitBlock().getLocation());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.cursor_on[(<range>)]>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the block the entity is looking at.
        // Optionally, specify a maximum range to find the location from (defaults to 200).
        // This uses logic equivalent to <@link tag LocationTag.precise_cursor_on_block[(range)]>.
        // Note that this will return null if there is no block in range.
        // This uses all blocks, ie it includes passable blocks like tall-grass and water. Use <@link tag EntityTag.cursor_on_solid> to exclude passable blocks.
        // Equivalent to <EntityTag.eye_location.ray_trace[return=block;fluids=true;nonsolids=true]>
        // -->
        registerSpawnedOnlyTag(LocationTag.class, "cursor_on", (attribute, object) -> {
            double range = attribute.getDoubleParam();
            if (range <= 0) {
                range = 200;
            }
            RayTraceResult traced = object.getWorld().rayTraceBlocks(object.getEyeLocation(), object.getEyeLocation().getDirection(), range, FluidCollisionMode.ALWAYS, false);
            if (traced != null && traced.getHitBlock() != null) {
                return new LocationTag(traced.getHitBlock().getLocation());
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
        registerSpawnedOnlyTag(LocationTag.class, "location", (attribute, object) -> {
            return object.doLocationTag(attribute);
        });

        // <--[tag]
        // @attribute <EntityTag.standing_on>
        // @returns LocationTag
        // @group location
        // @description
        // Returns the location of the block the entity is standing on top of (if on the ground, returns null if in the air).
        // -->
        registerSpawnedOnlyTag(LocationTag.class, "standing_on", (attribute, object) -> {
            if (!object.getBukkitEntity().isOnGround()) {
                return null;
            }
            Location loc = object.getBukkitEntity().getLocation().clone().subtract(0, 0.05, 0);
            return new LocationTag(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        });

        // <--[tag]
        // @attribute <EntityTag.body_yaw>
        // @returns ElementTag(Decimal)
        // @group location
        // @description
        // Returns the entity's body yaw (separate from head yaw).
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "body_yaw", (attribute, object) -> {
            return new ElementTag(NMSHandler.entityHelper.getBaseYaw(object.getBukkitEntity()));
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
        registerSpawnedOnlyTag(LocationTag.class, "velocity", (attribute, object) -> {
            return new LocationTag(object.getBukkitEntity().getVelocity().toLocation(object.getBukkitEntity().getWorld()));
        });

        // <--[tag]
        // @attribute <EntityTag.world>
        // @returns WorldTag
        // @group location
        // @description
        // Returns the world the entity is in. Works with offline players.
        // -->
        registerSpawnedOnlyTag(WorldTag.class, "world", (attribute, object) -> {
            return new WorldTag(object.getBukkitEntity().getWorld());
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
        registerSpawnedOnlyTag(ElementTag.class, "can_pickup_items", (attribute, object) -> {
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
        registerSpawnedOnlyTag(MaterialTag.class, "fallingblock_material", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FallingBlock)) {
                return null;
            }
            return new MaterialTag(((FallingBlock) object.getBukkitEntity()).getBlockData());
        });

        // <--[tag]
        // @attribute <EntityTag.fall_distance>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.fall_distance
        // @group attributes
        // @description
        // Returns how far the entity has fallen.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "fall_distance", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().getFallDistance());
        });

        // <--[tag]
        // @attribute <EntityTag.fire_time>
        // @returns DurationTag
        // @mechanism EntityTag.fire_time
        // @group attributes
        // @description
        // Returns the duration for which the entity will remain on fire
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "fire_time", (attribute, object) -> {
            return new DurationTag(object.getBukkitEntity().getFireTicks() / 20);
        });

        // <--[tag]
        // @attribute <EntityTag.on_fire>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is currently ablaze or not.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "on_fire", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().getFireTicks() > 0);
        });

        // <--[tag]
        // @attribute <EntityTag.leash_holder>
        // @returns EntityTag
        // @mechanism EntityTag.leash_holder
        // @group attributes
        // @description
        // Returns the leash holder of entity.
        // -->
        registerSpawnedOnlyTag(EntityFormObject.class, "leash_holder", (attribute, object) -> {
            if (object.isLivingEntity() && object.getLivingEntity().isLeashed()) {
                return new EntityTag(object.getLivingEntity().getLeashHolder()).getDenizenObject();
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
        registerSpawnedOnlyTag(ListTag.class, "passengers", (attribute, object) -> {
            ArrayList<EntityTag> passengers = new ArrayList<>();
            for (Entity ent : object.getBukkitEntity().getPassengers()) {
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
        registerSpawnedOnlyTag(EntityFormObject.class, "passenger", (attribute, object) -> {
            if (!object.getBukkitEntity().isEmpty()) {
                return new EntityTag(object.getBukkitEntity().getPassenger()).getDenizenObject();
            }
            return null;
        }, "get_passenger");

        // <--[tag]
        // @attribute <EntityTag.shooter>
        // @returns EntityTag
        // @group attributes
        // @mechanism EntityTag.shooter
        // @synonyms EntityTag.arrow_firer,EntityTag.fishhook_shooter,EntityTag.snowball_thrower
        // @description
        // Returns the projectile's shooter or TNT's priming source, if any.
        // -->
        registerSpawnedOnlyTag(EntityFormObject.class, "shooter", (attribute, object) -> {
            EntityTag shooter = object.getShooter();
            if (shooter == null) {
                return null;
            }
            return shooter.getDenizenObject();
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
        registerSpawnedOnlyTag(EntityFormObject.class, "left_shoulder", (attribute, object) -> {
            if (!(object.getLivingEntity() instanceof HumanEntity)) {
                return null;
            }
            Entity e = ((HumanEntity) object.getLivingEntity()).getShoulderEntityLeft();
            if (e == null) {
                return null;
            }
            return new EntityTag(e).getDenizenObject();
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
        registerSpawnedOnlyTag(EntityFormObject.class, "right_shoulder", (attribute, object) -> {
            if (!(object.getLivingEntity() instanceof HumanEntity)) {
                return null;
            }
            Entity e = ((HumanEntity) object.getLivingEntity()).getShoulderEntityRight();
            if (e == null) {
                return null;
            }
            return new EntityTag(e).getDenizenObject();
        });

        // <--[tag]
        // @attribute <EntityTag.vehicle>
        // @returns EntityTag
        // @group attributes
        // @description
        // If the entity is in a vehicle, returns the vehicle as a EntityTag.
        // -->
        registerSpawnedOnlyTag(EntityFormObject.class, "vehicle", (attribute, object) -> {
            if (object.getBukkitEntity().isInsideVehicle()) {
                return new EntityTag(object.getBukkitEntity().getVehicle()).getDenizenObject();
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
        registerSpawnedOnlyTag(ElementTag.class, "can_breed", (attribute, object) -> {
            if (!(object.getLivingEntity() instanceof Breedable)) {
                return new ElementTag(false);
            }
            return new ElementTag(((Breedable) object.getLivingEntity()).canBreed());
        });

        // <--[tag]
        // @attribute <EntityTag.breeding>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.breed
        // @group attributes
        // @description
        // Returns whether the animal entity is trying to mate with another of its kind.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "breeding", (attribute, object) -> {
            if (!(object.getLivingEntity() instanceof Animals)) {
                return null;
            }
            return new ElementTag(((Animals) object.getLivingEntity()).getLoveModeTicks() > 0);
        }, "is_breeding");

        // <--[tag]
        // @attribute <EntityTag.has_passenger>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.passenger
        // @group attributes
        // @description
        // Returns whether the entity has a passenger.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "has_passenger", (attribute, object) -> {
            return new ElementTag(!object.getBukkitEntity().isEmpty());
        });

        // <--[tag]
        // @attribute <EntityTag.is_empty>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity does not have a passenger.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_empty", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().isEmpty());
        }, "empty");

        // <--[tag]
        // @attribute <EntityTag.is_inside_vehicle>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is inside a vehicle.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_inside_vehicle", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().isInsideVehicle());
        }, "inside_vehicle");

        // <--[tag]
        // @attribute <EntityTag.is_leashed>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is leashed.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_leashed", (attribute, object) -> {
            return new ElementTag(object.isLivingEntity() && object.getLivingEntity().isLeashed());
        }, "leashed");

        // <--[tag]
        // @attribute <EntityTag.is_sheared>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether a sheep is sheared.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_sheared", (attribute, object) -> {
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
        // This can be inaccurate for players.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_on_ground", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().isOnGround());
        }, "on_ground");

        // <--[tag]
        // @attribute <EntityTag.is_persistent>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @mechanism EntityTag.persistent
        // @description
        // Returns whether the mob-entity will not be removed completely when far away from players.
        // This is Bukkit's "getRemoveWhenFarAway" which is Mojang's "isPersistenceRequired".
        // In many cases, <@link tag EntityTag.force_no_persist> may be preferred.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_persistent", (attribute, object) -> {
            return new ElementTag(object.isLivingEntity() && !object.getLivingEntity().getRemoveWhenFarAway());
        }, "persistent");

        // <--[tag]
        // @attribute <EntityTag.force_no_persist>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @mechanism EntityTag.force_no_persist
        // @description
        // Returns 'true' if the entity is forced to not save to file when chunks unload.
        // Returns 'false' if not forced to not-save. May return 'false' even for entities that don't save for other reasons.
        // This is a custom value added in Bukkit to block saving, which is not the same as Mojang's similar option under <@link tag EntityTag.is_persistent>.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "forced_no_persist", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().isPersistent());
        });

        // <--[tag]
        // @attribute <EntityTag.is_collidable>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.collidable
        // @group attributes
        // @description
        // Returns whether the entity is collidable.
        // Returns the persistent collidable value for NPCs.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_collidable", (attribute, object) -> {
            if (object.isCitizensNPC()) {
                return new ElementTag(object.getDenizenNPC().getCitizen().data().get(NPC.COLLIDABLE_METADATA, true));
            }
            return new ElementTag(object.getLivingEntity().isCollidable());
        });

        // <--[tag]
        // @attribute <EntityTag.is_sleeping>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player, NPC, or villager is currently sleeping.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_sleeping", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Player) {
                return new ElementTag(((Player) object.getBukkitEntity()).isSleeping());
            }
            else if (object.getBukkitEntity() instanceof Villager) {
                return new ElementTag(((Villager) object.getBukkitEntity()).isSleeping());
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.killer>
        // @returns PlayerTag
        // @group attributes
        // @description
        // Returns the player that last killed the entity.
        // -->
        registerSpawnedOnlyTag(PlayerTag.class, "killer", (attribute, object) -> {
            return getPlayerFrom(object.getLivingEntity().getKiller());
        });

        registerSpawnedOnlyTag(ObjectTag.class, "last_damage", (attribute, object) -> {
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
                if (object.getBukkitEntity().getLastDamageCause() == null) {
                    return null;
                }
                return new ElementTag(object.getBukkitEntity().getLastDamageCause().getCause().name());
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
        registerSpawnedOnlyTag(ElementTag.class, "absorption_health", (attribute, object) -> {
            return new ElementTag(object.getLivingEntity().getAbsorptionAmount());
        });

        // <--[tag]
        // @attribute <EntityTag.max_oxygen>
        // @returns DurationTag
        // @group attributes
        // @description
        // Returns the maximum duration of oxygen the entity can have.
        // Works with offline players.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "max_oxygen", (attribute, object) -> {
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
        registerSpawnedOnlyTag(DurationTag.class, "oxygen", (attribute, object) -> {
            if (attribute.startsWith("max", 2)) {
                BukkitImplDeprecations.entityMaxOxygenTag.warn(attribute.context);
                attribute.fulfill(1);
                return new DurationTag((long) object.getLivingEntity().getMaximumAir());
            }
            return new DurationTag((long) object.getLivingEntity().getRemainingAir());
        });

        registerSpawnedOnlyTag(ElementTag.class, "remove_when_far", (attribute, object) -> {
            BukkitImplDeprecations.entityRemoveWhenFar.warn(attribute.context);
            return new ElementTag(object.getLivingEntity().getRemoveWhenFarAway());
        });

        // <--[tag]
        // @attribute <EntityTag.target>
        // @returns EntityTag
        // @group attributes
        // @description
        // Returns the target entity of the creature or shulker_bullet, if any.
        // This is the entity that a hostile mob is currently trying to attack.
        // -->
        registerSpawnedOnlyTag(EntityFormObject.class, "target", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Creature) {
                Entity target = ((Creature) object.getLivingEntity()).getTarget();
                if (target != null) {
                    return new EntityTag(target).getDenizenObject();
                }
            }
            else if (object.getBukkitEntity() instanceof ShulkerBullet) {
                Entity target = ((ShulkerBullet) object.getLivingEntity()).getTarget();
                if (target != null) {
                    return new EntityTag(target).getDenizenObject();
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
        registerSpawnedOnlyTag(EntityFormObject.class, "precise_target", (attribute, object) -> {
            int range = attribute.getIntParam();
            if (range < 1) {
                range = 200;
            }
            Predicate<Entity> requirement;
            // <--[tag]
            // @attribute <EntityTag.precise_target[(<range>)].type[<matcher>]>
            // @returns EntityTag
            // @description
            // Returns the entity this entity is looking at, using precise ray trace logic.
            // Optionally, specify a maximum range to find the entity from (defaults to 200).
            // Specify an entity type matcher to only count matches as possible ray trace hits (types not listed will be ignored).
            // -->
            if (attribute.startsWith("type", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                String matcher = attribute.getParam();
                requirement = (e) -> !e.equals(object.getBukkitEntity()) && new EntityTag(e).tryAdvancedMatcher(matcher);
            }
            else {
                requirement = (e) -> !e.equals(object.getBukkitEntity());
            }
            RayTraceResult result = object.getWorld().rayTrace(object.getEyeLocation(), object.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true, 0, requirement);
            if (result != null && result.getHitEntity() != null) {
                return new EntityTag(result.getHitEntity()).getDenizenObject();
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
        registerSpawnedOnlyTag(LocationTag.class, "precise_target_position", (attribute, object) -> {
            int range = attribute.getIntParam();
            if (range < 1) {
                range = 200;
            }
            Predicate<Entity> requirement;
            // <--[tag]
            // @attribute <EntityTag.precise_target_position[(<range>)].type[<matcher>]>
            // @returns LocationTag
            // @description
            // Returns the location this entity is looking at, using precise ray trace (against entities) logic.
            // Optionally, specify a maximum range to find the target from (defaults to 200).
            // Specify an entity type matcher to only count matches as possible ray trace hits (types not listed will be ignored).
            // -->
            if (attribute.startsWith("type", 2) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                String matcher = attribute.getParam();
                requirement = (e) -> !e.equals(object.getBukkitEntity()) && new EntityTag(e).tryAdvancedMatcher(matcher);
            }
            else {
                requirement = (e) -> !e.equals(object.getBukkitEntity());
            }
            RayTraceResult result = object.getWorld().rayTrace(object.getEyeLocation(), object.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true, 0, requirement);
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
        registerSpawnedOnlyTag(DurationTag.class, "time_lived", (attribute, object) -> {
            return new DurationTag(object.getBukkitEntity().getTicksLived() / 20);
        });

        // <--[tag]
        // @attribute <EntityTag.pickup_delay>
        // @returns DurationTag
        // @mechanism EntityTag.pickup_delay
        // @group attributes
        // @description
        // Returns how long before the item-type entity can be picked up by a player.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "pickup_delay", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "is_in_block", (attribute, object) -> {
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
        registerSpawnedOnlyTag(LocationTag.class, "attached_block", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "gliding", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "swimming", (attribute, object) -> {
            return new ElementTag(object.getLivingEntity().isSwimming());
        });

        // <--[tag]
        // @attribute <EntityTag.visual_pose>
        // @returns ElementTag
        // @group attributes
        // @description
        // Returns the name of the entity's current visual pose.
        // See <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Pose.html>
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "visual_pose", (attribute, object) -> {
            return new ElementTag(object.getBukkitEntity().getPose().name());
        });

        // <--[tag]
        // @attribute <EntityTag.glowing>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.glowing
        // @group attributes
        // @description
        // Returns whether this entity is glowing.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "glowing", (attribute, object) -> {
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
        // Returns whether the entity type is a living-type entity (eg a cow or a player or anything else that lives, as specifically opposed to non-living entities like paintings, etc).
        // Not to be confused with the idea of being alive - see <@link tag EntityTag.is_spawned>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_living", (attribute, object) -> {
            return new ElementTag(object.isLivingEntityType());
        });

        // <--[tag]
        // @attribute <EntityTag.is_monster>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity type is a hostile monster.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_monster", (attribute, object) -> {
            return new ElementTag(object.isMonsterType());
        });

        // <--[tag]
        // @attribute <EntityTag.is_mob>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity type is a mob (Not a player or NPC).
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_mob", (attribute, object) -> {
            return new ElementTag(object.isMobType());
        });

        // <--[tag]
        // @attribute <EntityTag.is_npc>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity is a Citizens NPC.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_npc", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "is_player", (attribute, object) -> {
            return new ElementTag(object.isPlayer());
        });

        // <--[tag]
        // @attribute <EntityTag.is_projectile>
        // @returns ElementTag(Boolean)
        // @group data
        // @description
        // Returns whether the entity type is a projectile.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_projectile", (attribute, object) -> {
            if (object.getBukkitEntity() == null && object.entity_type != null) {
                return new ElementTag(Projectile.class.isAssignableFrom(object.entity_type.getBukkitEntityType().getEntityClass()));
            }
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
        registerSpawnedOnlyTag(ElementTag.class, "tameable", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "ageable", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "colorable", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "experience", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "fuse_ticks", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "dragon_phase", (attribute, object) -> {
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
        registerSpawnedOnlyTag(ElementTag.class, "weapon_damage", (attribute, object) -> {
            Entity target = null;
            if (attribute.hasParam()) {
                target = attribute.paramAsType(EntityTag.class).getBukkitEntity();
            }
            return new ElementTag(NMSHandler.entityHelper.getDamageTo(object.getLivingEntity(), target));
        });

        // <--[tag]
        // @attribute <EntityTag.skin_layers>
        // @returns ListTag
        // @mechanism EntityTag.skin_layers
        // @description
        // Returns the skin layers currently visible on a player-type entity.
        // Output is a list of values from the set of:
        // CAPE, HAT, JACKET, LEFT_PANTS, LEFT_SLEEVE, RIGHT_PANTS, or RIGHT_SLEEVE.
        // -->
        registerSpawnedOnlyTag(ListTag.class, "skin_layers", (attribute, object) -> {
            byte flags = NMSHandler.playerHelper.getSkinLayers((Player) object.getBukkitEntity());
            ListTag result = new ListTag();
            for (PlayerHelper.SkinLayer layer : PlayerHelper.SkinLayer.values()) {
                if ((flags & layer.flag) != 0) {
                    result.add(layer.name());
                }
            }
            return result;
        });

        // <--[tag]
        // @attribute <EntityTag.is_disguised[(<player>)]>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the entity is currently disguised, either globally (if no context input given), or to the specified player.
        // Relates to <@link command disguise>.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_disguised", (attribute, object) -> {
            HashMap<UUID, DisguiseCommand.TrackedDisguise> map = DisguiseCommand.disguises.get(object.getUUID());
            if (map == null) {
                return new ElementTag(false);
            }
            if (attribute.hasParam()) {
                PlayerTag player = attribute.paramAsType(PlayerTag.class);
                if (player == null) {
                    attribute.echoError("Invalid player for is_disguised tag.");
                    return null;
                }
                return new ElementTag(map.containsKey(player.getUUID()) || map.containsKey(null));
            }
            else {
                return new ElementTag(map.containsKey(null));
            }
        });

        // <--[tag]
        // @attribute <EntityTag.disguised_type[(<player>)]>
        // @returns EntityTag
        // @group properties
        // @description
        // Returns the entity type the entity is disguised as, either globally (if no context input given), or to the specified player.
        // Relates to <@link command disguise>.
        // -->
        registerSpawnedOnlyTag(EntityTag.class, "disguised_type", (attribute, object) -> {
            HashMap<UUID, DisguiseCommand.TrackedDisguise> map = DisguiseCommand.disguises.get(object.getUUID());
            if (map == null) {
                return null;
            }
            DisguiseCommand.TrackedDisguise disguise;
            if (attribute.hasParam()) {
                PlayerTag player = attribute.paramAsType(PlayerTag.class);
                if (player == null) {
                    attribute.echoError("Invalid player for is_disguised tag.");
                    return null;
                }
                disguise = map.get(player.getUUID());
                if (disguise == null) {
                    disguise = map.get(null);
                }
            }
            else {
                disguise = map.get(null);
            }
            if (disguise == null) {
                return null;
            }
            return disguise.as.duplicate();
        });

        // <--[tag]
        // @attribute <EntityTag.disguise_to_others[(<player>)]>
        // @returns EntityTag
        // @group properties
        // @description
        // Returns the fake entity used to disguise the entity in other's views, either globally (if no context input given), or to the specified player.
        // Relates to <@link command disguise>.
        // -->
        registerSpawnedOnlyTag(EntityTag.class, "disguise_to_others", (attribute, object) -> {
            HashMap<UUID, DisguiseCommand.TrackedDisguise> map = DisguiseCommand.disguises.get(object.getUUID());
            if (map == null) {
                return null;
            }
            DisguiseCommand.TrackedDisguise disguise;
            if (attribute.hasParam()) {
                PlayerTag player = attribute.paramAsType(PlayerTag.class);
                if (player == null) {
                    attribute.echoError("Invalid player for is_disguised tag.");
                    return null;
                }
                disguise = map.get(player.getUUID());
                if (disguise == null) {
                    disguise = map.get(null);
                }
            }
            else {
                disguise = map.get(null);
            }
            if (disguise == null) {
                return null;
            }
            if (disguise.toOthers == null) {
                return null;
            }
            return disguise.toOthers.entity;
        });

        // <--[tag]
        // @attribute <EntityTag.describe>
        // @returns EntityTag
        // @group properties
        // @description
        // Returns the entity's full description, including all properties.
        // -->
        tagProcessor.registerTag(EntityTag.class, "describe", (attribute, object) -> {
            return object.describe(attribute.context);
        });

        // <--[tag]
        // @attribute <EntityTag.has_equipped[<item-matcher>]>
        // @returns ElementTag(Boolean)
        // @group element checking
        // @description
        // Returns whether the entity has any armor equipment item that matches the given item matcher, using the system behind <@link language Advanced Object Matching>.
        // For example, has_equipped[diamond_*] will return true if the entity is wearing at least one piece of diamond armor.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "has_equipped", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            if (!object.isLivingEntity()) {
                return null;
            }
            String matcher = attribute.getParam();
            for (ItemStack item : object.getLivingEntity().getEquipment().getArmorContents()) {
                if (new ItemTag(item).tryAdvancedMatcher(matcher)) {
                    return new ElementTag(true);
                }
            }
            return new ElementTag(false);
        });

        // <--[tag]
        // @attribute <EntityTag.loot_table_id>
        // @returns ElementTag
        // @mechanism EntityTag.loot_table_id
        // @description
        // Returns an element indicating the minecraft key for the loot-table for the entity (if any).
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "loot_table_id", (attribute, object) -> {
            if (object.getBukkitEntity() instanceof Lootable) {
                LootTable table = ((Lootable) object.getBukkitEntity()).getLootTable();
                if (table != null) {
                    return new ElementTag(table.getKey().toString());
                }
            }
            return null;
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_state>
        // @returns ElementTag
        // @description
        // Returns the current state of the fish hook, as any of: UNHOOKED, HOOKED_ENTITY, BOBBING (unhooked means the fishing hook is in the air or on ground).
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "fish_hook_state", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_state is only valid for fish hooks.");
                return null;
            }
            return new ElementTag(((FishHook) object.getBukkitEntity()).getState().name());
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_lure_time>
        // @returns DurationTag
        // @mechanism EntityTag.fish_hook_lure_time
        // @description
        // Returns the remaining time before this fish hook will lure a fish.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "fish_hook_lure_time", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_lure_time is only valid for fish hooks.");
                return null;
            }
            return new DurationTag((long) NMSHandler.fishingHelper.getLureTime((FishHook) object.getBukkitEntity()));
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_min_lure_time>
        // @returns DurationTag
        // @mechanism EntityTag.fish_hook_min_lure_time
        // @description
        // Returns the minimum possible time before this fish hook can lure a fish.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "fish_hook_min_lure_time", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_min_lure_time is only valid for fish hooks.");
                return null;
            }
            return new DurationTag((long) ((FishHook) object.getBukkitEntity()).getMinWaitTime());
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_max_lure_time>
        // @returns DurationTag
        // @mechanism EntityTag.fish_hook_max_lure_time
        // @description
        // Returns the maximum possible time before this fish hook will lure a fish.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "fish_hook_max_lure_time", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_max_lure_time is only valid for fish hooks.");
                return null;
            }
            return new DurationTag((long) ((FishHook) object.getBukkitEntity()).getMaxWaitTime());
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_hooked_entity>
        // @returns EntityTag
        // @mechanism EntityTag.fish_hook_hooked_entity
        // @description
        // Returns the entity this fish hook is attached to.
        // -->
        registerSpawnedOnlyTag(EntityTag.class, "fish_hook_hooked_entity", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_hooked_entity is only valid for fish hooks.");
                return null;
            }
            Entity entity = ((FishHook) object.getBukkitEntity()).getHookedEntity();
            return entity != null ? new EntityTag(entity) : null;
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_apply_lure>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.fish_hook_apply_lure
        // @description
        // Returns whether this fish hook should respect the lure enchantment.
        // Every level of lure enchantment reduces lure time by 5 seconds.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "fish_hook_apply_lure", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_apply_lure is only valid for fish hooks.");
                return null;
            }
            return new ElementTag(((FishHook) object.getBukkitEntity()).getApplyLure());
        });

        // <--[tag]
        // @attribute <EntityTag.fish_hook_in_open_water>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this fish hook is in open water. Fish hooks in open water can catch treasure.
        // See <@link url https://minecraft.fandom.com/wiki/Fishing> for more info.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "fish_hook_in_open_water", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof FishHook)) {
                attribute.echoError("EntityTag.fish_hook_in_open_water is only valid for fish hooks.");
                return null;
            }
            return new ElementTag(((FishHook) object.getBukkitEntity()).isInOpenWater());
        });

        // <--[tag]
        // @attribute <EntityTag.attached_entities[(<player>)]>
        // @returns ListTag(EntityTag)
        // @description
        // Returns the entities attached to this entity by <@link command attach>.
        // Optionally, specify a player. If specified, will return entities attached visible to that player. If not specified, returns entities globally attached.
        // -->
        registerSpawnedOnlyTag(ListTag.class, "attached_entities", (attribute, object) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : null;
            EntityAttachmentHelper.EntityAttachedToMap data = EntityAttachmentHelper.toEntityToData.get(object.getUUID());
            ListTag result = new ListTag();
            if (data == null) {
                return result;
            }
            for (EntityAttachmentHelper.PlayerAttachMap map : data.attachedToMap.values()) {
                if (player == null || map.getAttachment(player.getUUID()) != null) {
                    result.addObject(map.attached);
                }
            }
            return result;
        });

        // <--[tag]
        // @attribute <EntityTag.attached_to[(<player>)]>
        // @returns EntityTag
        // @description
        // Returns the entity that this entity was attached to by <@link command attach>.
        // Optionally, specify a player. If specified, will return entity attachment visible to that player. If not specified, returns any entity global attachment.
        // -->
        registerSpawnedOnlyTag(EntityTag.class, "attached_to", (attribute, object) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : null;
            EntityAttachmentHelper.PlayerAttachMap data = EntityAttachmentHelper.attachedEntityToData.get(object.getUUID());
            if (data == null) {
                return null;
            }
            EntityAttachmentHelper.AttachmentData attached = data.getAttachment(player == null ? null : player.getUUID());
            if (attached == null) {
                return null;
            }
            return attached.to;
        });

        // <--[tag]
        // @attribute <EntityTag.attached_offset[(<player>)]>
        // @returns LocationTag
        // @description
        // Returns the offset of an attachment for this entity to another that was attached by <@link command attach>.
        // Optionally, specify a player. If specified, will return entity attachment visible to that player. If not specified, returns any entity global attachment.
        // -->
        registerSpawnedOnlyTag(LocationTag.class, "attached_offset", (attribute, object) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : null;
            EntityAttachmentHelper.PlayerAttachMap data = EntityAttachmentHelper.attachedEntityToData.get(object.getUUID());
            if (data == null) {
                return null;
            }
            EntityAttachmentHelper.AttachmentData attached = data.getAttachment(player == null ? null : player.getUUID());
            if (attached == null) {
                return null;
            }
            return attached.positionalOffset == null ? null : new LocationTag(attached.positionalOffset);
        });

        // <--[tag]
        // @attribute <EntityTag.attack_cooldown_duration>
        // @returns DurationTag
        // @mechanism EntityTag.attack_cooldown
        // @description
        // Returns the amount of time that passed since the start of the attack cooldown.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "attack_cooldown_duration", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof Player)) {
                attribute.echoError("Only player-type entities can have attack_cooldowns!");
                return null;
            }
            return new DurationTag((long) NMSHandler.playerHelper.ticksPassedDuringCooldown((Player) object.getLivingEntity()));
        });

        // <--[tag]
        // @attribute <EntityTag.attack_cooldown_max_duration>
        // @returns DurationTag
        // @mechanism EntityTag.attack_cooldown
        // @description
        // Returns the maximum amount of time that can pass before the player's main hand has returned
        // to its original place after the cooldown has ended.
        // NOTE: This is slightly inaccurate and may not necessarily match with the actual attack
        // cooldown progress.
        // -->
        registerSpawnedOnlyTag(DurationTag.class, "attack_cooldown_max_duration", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof Player)) {
                attribute.echoError("Only player-type entities can have attack_cooldowns!");
                return null;
            }
            return new DurationTag((long) NMSHandler.playerHelper.getMaxAttackCooldownTicks((Player) object.getLivingEntity()));
        });

        // <--[tag]
        // @attribute <EntityTag.attack_cooldown_percent>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.attack_cooldown_percent
        // @description
        // Returns the progress of the attack cooldown. 0 means that the attack cooldown has just
        // started, while 100 means that the attack cooldown has finished.
        // NOTE: This may not match exactly with the clientside attack cooldown indicator.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "attack_cooldown_percent", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof Player)) {
                attribute.echoError("Only player-type entities can have attack_cooldowns!");
                return null;
            }
            return new ElementTag(((Player) object.getLivingEntity()).getAttackCooldown() * 100);
        });

        // <--[tag]
        // @attribute <EntityTag.is_hand_raised>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.attack_cooldown_percent
        // @description
        // Returns whether the player's hand is currently raised. Valid for players and player-type NPCs.
        // A player's hand is raised when they are blocking with a shield, aiming a crossbow, looking through a spyglass, etc.
        // -->
        registerSpawnedOnlyTag(ElementTag.class, "is_hand_raised", (attribute, object) -> {
            if (!(object.getBukkitEntity() instanceof HumanEntity)) {
                attribute.echoError("Only player-type entities can have is_hand_raised!");
                return null;
            }
            return new ElementTag(((HumanEntity) object.getLivingEntity()).isHandRaised());
        });
    }

    public EntityTag describe(TagContext context) {
        ArrayList<Mechanism> waitingMechs;
        if (isSpawnedOrValidForTag()) {
            waitingMechs = new ArrayList<>();
            for (Map.Entry<StringHolder, ObjectTag> property : PropertyParser.getPropertiesMap(this).map.entrySet()) {
                waitingMechs.add(new Mechanism(property.getKey().str, property.getValue(), context));
            }
        }
        else {
            waitingMechs = new ArrayList<>(getWaitingMechanisms());
        }
        EntityTag entity = new EntityTag(entity_type, waitingMechs);
        entity.entityScript = entityScript;
        return entity;
    }

    public static ObjectTagProcessor<EntityTag> tagProcessor = new ObjectTagProcessor<>();

    public static <R extends ObjectTag> void registerSpawnedOnlyTag(Class<R> returnType, String name, TagRunnable.ObjectInterface<EntityTag, R> runnable, String... variants) {
        tagProcessor.registerTag(returnType, name, (attribute, object) -> {
            if (!object.isSpawnedOrValidForTag()) {
                if (!attribute.hasAlternative()) {
                    Debug.echoError("Entity is not spawned, but tag '" + attribute.getAttributeWithoutParam(1) + "' requires the entity be spawned, for entity: " + object.debuggable());
                }
                return null;
            }
            return runnable.run(attribute, object);
        }, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public ArrayList<Mechanism> mechanisms = new ArrayList<>();

    public ArrayList<Mechanism> getWaitingMechanisms() {
        return mechanisms;
    }

    public void applyProperty(Mechanism mechanism) {
        if (isGeneric()) {
            mechanisms.add(mechanism);
            mechanism.fulfill();
        }
        else {
            mechanism.echoError("Cannot apply properties to an already-spawned entity!");
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
                mechanism.echoError("Cannot adjust not-spawned NPC " + getDenizenNPC());
            }
            else {
                mechanism.echoError("Cannot adjust entity " + this);
            }
            return;
        }

        if (mechanism.matches("attach_to")) {
            BukkitImplDeprecations.attachToMech.warn(mechanism.context);
            if (mechanism.hasValue()) {
                ListTag list = mechanism.valueAsType(ListTag.class);
                Vector offset = null;
                boolean rotateWith = true;
                if (list.size() > 1) {
                    offset = LocationTag.valueOf(list.get(1), mechanism.context).toVector();
                    if (list.size() > 2) {
                        rotateWith = new ElementTag(list.get(2)).asBoolean();
                    }
                }
                EntityAttachmentHelper.forceAttachMove(this, EntityTag.valueOf(list.get(0), mechanism.context), offset, rotateWith);
            }
            else {
                EntityAttachmentHelper.forceAttachMove(this, null, null, false);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name shooter
        // @input EntityTag
        // @synonyms EntityTag.arrow_firer,EntityTag.fishhook_shooter,EntityTag.snowball_thrower
        // @description
        // Sets the projectile's shooter or TNT's priming source.
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
        // @input DurationTag
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
        // <EntityTag.breeding>
        // -->
        if (mechanism.matches("breed") && mechanism.requireBoolean()) {
            ((Animals) getLivingEntity()).setLoveModeTicks(mechanism.getValue().asBoolean() ? 600 : 0);
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
        // @input DurationTag
        // @synonyms EntityTag.age_nbt,EntityTag.time_nbt
        // @description
        // Sets the amount of time this entity has lived for.
        // For entities that automatically despawn such as dropped_items or falling_blocks, it can be useful to set this value to "-2147483648t" (the minimum valid number of ticks) to cause it to persist indefinitely.
        // For falling_block usage, see also <@link mechanism EntityTag.auto_expire>
        // @tags
        // <EntityTag.time_lived>
        // -->
        if (mechanism.matches("time_lived") && mechanism.requireObject(DurationTag.class)) {
            NMSHandler.entityHelper.setTicksLived(entity, mechanism.valueAsType(DurationTag.class).getTicksAsInt());
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
            getLivingEntity().setAbsorptionAmount(mechanism.getValue().asDouble());
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
            BukkitImplDeprecations.entityRemainingAir.warn(mechanism.context);
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
        // @tags
        // <EntityTag.left_shoulder>
        // -->
        if (mechanism.matches("release_left_shoulder") && getLivingEntity() instanceof HumanEntity) {
            Entity bukkitEnt = ((HumanEntity) getLivingEntity()).getShoulderEntityLeft();
            if (bukkitEnt != null) {
                EntityTag ent = new EntityTag(bukkitEnt);
                String escript = ent.getEntityScript();
                ent = EntityTag.valueOf("e@" + (escript != null && escript.length() > 0 ? escript : ent.getEntityType().getLowercaseName())
                        + PropertyParser.getPropertiesString(ent), mechanism.context);
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
        // @tags
        // <EntityTag.right_shoulder>
        // -->
        if (mechanism.matches("release_right_shoulder") && getLivingEntity() instanceof HumanEntity) {
            Entity bukkitEnt = ((HumanEntity) getLivingEntity()).getShoulderEntityRight();
            if (bukkitEnt != null) {
                EntityTag ent = new EntityTag(bukkitEnt);
                String escript = ent.getEntityScript();
                ent = EntityTag.valueOf("e@" + (escript != null && escript.length() > 0 ? escript : ent.getEntityType().getLowercaseName())
                        + PropertyParser.getPropertiesString(ent), mechanism.context);
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
        if (mechanism.matches("left_shoulder") && getLivingEntity() instanceof HumanEntity) {
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
        if (mechanism.matches("right_shoulder") && getLivingEntity() instanceof HumanEntity) {
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
        // @name persistent
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the mob-entity will not be removed completely when far away from players.
        // This is Bukkit's "setRemoveWhenFarAway" which is Mojang's "isPersistenceRequired".
        // In many cases, <@link mechanism EntityTag.force_no_persist> may be preferred.
        // The entity must be a mob-type entity.
        // @tags
        // <EntityTag.is_persistent>
        // -->
        if (mechanism.matches("persistent") && mechanism.requireBoolean()) {
            getLivingEntity().setRemoveWhenFarAway(!mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name force_no_persist
        // @input ElementTag(Boolean)
        // @description
        // Set 'true' to indicate the entity should be forced to not save to file when chunks unload.
        // Set 'false' to not force to not-save. Entities will then either save or not save depending on separate conditions.
        // This is a custom value added in Bukkit to block saving, which is not the same as Mojang's similar option under <@link mechanism EntityTag.persistent>.
        // @tags
        // <EntityTag.force_no_persist>
        // -->
        if (mechanism.matches("force_no_persist") && mechanism.requireBoolean()) {
            getLivingEntity().setPersistent(!mechanism.getValue().asBoolean());
        }

        if (mechanism.matches("remove_when_far_away") && mechanism.requireBoolean()) {
            BukkitImplDeprecations.entityRemoveWhenFar.warn(mechanism.context);
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
        if (mechanism.matches("sheared") && mechanism.requireBoolean() && getBukkitEntity() instanceof Sheep) {
            ((Sheep) getBukkitEntity()).setSheared(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name collidable
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity is collidable.
        // For NPCs, Sets the persistent collidable value.
        // NOTE: To disable collision between two entities, set this mechanism to false on both entities.
        // NOTE: For players, to fully remove collision you need to use <@link command team> and set "option:collision_rule status:never"
        // @tags
        // <EntityTag.is_collidable>
        // -->
        if (mechanism.matches("collidable") && mechanism.requireBoolean()) {
            if (isCitizensNPC()) {
                getDenizenNPC().getCitizen().data().setPersistent(NPC.COLLIDABLE_METADATA, mechanism.getValue().asBoolean());
            }
            else {
                getLivingEntity().setCollidable(mechanism.getValue().asBoolean());
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name no_damage_duration
        // @input DurationTag
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
        // @input DurationTag
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
        // Sets the entity's movement velocity vector.
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
        // Forces an entity to move in the direction of the velocity vector specified.
        // -->
        if (mechanism.matches("move") && mechanism.requireObject(LocationTag.class)) {
            NMSHandler.entityHelper.move(getBukkitEntity(), mechanism.valueAsType(LocationTag.class).toVector());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fake_move
        // @input LocationTag
        // @description
        // Causes an entity to broadcast a fake movement packet in the direction of the velocity vector specified.
        // The vector value must be in the range [-8,8] on each of X, Y, and Z.
        // -->
        if (mechanism.matches("fake_move") && mechanism.requireObject(LocationTag.class)) {
            NMSHandler.entityHelper.fakeMove(getBukkitEntity(), mechanism.valueAsType(LocationTag.class).toVector());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fake_teleport
        // @input LocationTag
        // @description
        // Causes an entity to broadcast a fake teleport packet to the location specified.
        // -->
        if (mechanism.matches("fake_teleport") && mechanism.requireObject(LocationTag.class)) {
            NMSHandler.entityHelper.fakeTeleport(getBukkitEntity(), mechanism.valueAsType(LocationTag.class));
        }

        // <--[mechanism]
        // @object EntityTag
        // @name reset_client_location
        // @input None
        // @description
        // Causes an entity to broadcast a fake teleport packet to its own location, forcibly resetting its location for all players that can see it.
        // -->
        if (mechanism.matches("reset_client_location")) {
            NMSHandler.entityHelper.clientResetLoc(getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name send_update_packets
        // @input None
        // @description
        // Causes an entity to broadcast any pending entity update packets to all players that can see it.
        // -->
        if (mechanism.matches("send_update_packets")) {
            NMSHandler.entityHelper.sendAllUpdatePackets(getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name interact_with
        // @input LocationTag
        // @description
        // Makes a player-type entity interact with a block.
        // -->
        if (mechanism.matches("interact_with") && mechanism.requireObject(LocationTag.class)) {
            LocationTag interactLocation = mechanism.valueAsType(LocationTag.class);
            NMSHandler.entityHelper.forceInteraction(getPlayer(), interactLocation);
        }

        if (mechanism.matches("play_death")) {
            BukkitImplDeprecations.entityPlayDeath.warn(mechanism.context);
            getLivingEntity().playEffect(EntityEffect.DEATH);
        }

        // <--[mechanism]
        // @object EntityTag
        // @name pickup_delay
        // @input DurationTag
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
        // @input ElementTag
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
        // See also <@link mechanism EntityTag.hide_from_players>.
        // To show to only one player, see <@link mechanism PlayerTag.show_entity>.
        // Works with offline players.
        // -->
        if (mechanism.matches("show_to_players")) {
            HideEntitiesHelper.unhideEntity(null, getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name hide_from_players
        // @input None
        // @description
        // Hides the entity from players by default.
        // See also <@link mechanism EntityTag.show_to_players>.
        // To hide for only one player, see <@link mechanism PlayerTag.hide_entity>.
        // Works with offline players.
        // -->
        if (mechanism.matches("hide_from_players")) {
            HideEntitiesHelper.hideEntity(null, getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name skin_layers
        // @input ListTag
        // @description
        // Sets the visible skin layers on a player-type entity (PlayerTag or player-type NPCTag).
        // Input is a list of values from the set of:
        // CAPE, HAT, JACKET, LEFT_PANTS, LEFT_SLEEVE, RIGHT_PANTS, RIGHT_SLEEVE, or "ALL"
        // @tags
        // <EntityTag.skin_layers>
        // -->
        if (mechanism.matches("skin_layers")) {
            int flags = 0;
            for (String str : mechanism.valueAsType(ListTag.class)) {
                String upper = str.toUpperCase();
                if (upper.equals("ALL")) {
                    flags = 0xFF;
                }
                else {
                    PlayerHelper.SkinLayer layer = PlayerHelper.SkinLayer.valueOf(upper);
                    flags |= layer.flag;
                }
            }
            NMSHandler.playerHelper.setSkinLayers((Player) getBukkitEntity(), (byte) flags);
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
                MirrorTrait mirror = npc.getOrAddTrait(MirrorTrait.class);
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
        if (mechanism.matches("swimming") && mechanism.requireBoolean()) {
            getLivingEntity().setSwimming(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name detonate
        // @input None
        // @description
        // If the entity is a firework or a creeper, detonates it.
        // -->
        if (mechanism.matches("detonate")) {
            if (getBukkitEntity() instanceof Firework) {
                ((Firework) getBukkitEntity()).detonate();
            }
            else if (getBukkitEntity() instanceof Creeper) {
                ((Creeper) getBukkitEntity()).explode();
            }
            else {
                Debug.echoError("Cannot detonate entity of type '" + getBukkitEntityType().name() + "'.");
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name ignite
        // @input None
        // @description
        // If the entity is a creeper, ignites it.
        // -->
        if (mechanism.matches("ignite")) {
            if (getBukkitEntity() instanceof Creeper) {
                ((Creeper) getBukkitEntity()).ignite();
            }
            else {
                Debug.echoError("Cannot ignite entity of type '" + getBukkitEntityType().name() + "'.");
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name head_angle
        // @input ElementTag(Decimal)
        // @description
        // Sets the raw head angle of a living entity.
        // This will not rotate the body at all. Most users should prefer <@link command look>.
        // -->
        if (mechanism.matches("head_angle") && mechanism.requireFloat()) {
            NMSHandler.entityHelper.setHeadAngle(getBukkitEntity(), mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name skeleton_arms_raised
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the skeleton entity should raise its arms.
        // -->
        if (mechanism.matches("skeleton_arms_raised") && mechanism.requireBoolean()) {
            EntityAnimation entityAnimation = NMSHandler.animationHelper.getEntityAnimation(mechanism.getValue().asBoolean() ? "SKELETON_START_SWING_ARM" : "SKELETON_STOP_SWING_ARM");
            entityAnimation.play(entity);
        }

        // <--[mechanism]
        // @object EntityTag
        // @name polar_bear_standing
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the polar bear entity should stand up.
        // -->
        if (mechanism.matches("polar_bear_standing") && mechanism.requireBoolean()) {
            EntityAnimation entityAnimation = NMSHandler.animationHelper.getEntityAnimation(mechanism.getValue().asBoolean() ? "POLAR_BEAR_START_STANDING" : "POLAR_BEAR_STOP_STANDING");
            entityAnimation.play(entity);
        }

        // <--[mechanism]
        // @object EntityTag
        // @name ghast_attacking
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the ghast entity should show the attacking face.
        // -->
        if (mechanism.matches("ghast_attacking") && mechanism.requireBoolean()) {
            NMSHandler.entityHelper.setGhastAttacking(getBukkitEntity(), mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name enderman_angry
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the enderman entity should be screaming angrily.
        // -->
        if (mechanism.matches("enderman_angry") && mechanism.requireBoolean()) {
            NMSHandler.entityHelper.setEndermanAngry(getBukkitEntity(), mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name melee_attack
        // @input EntityTag
        // @description
        // Causes this hostile-mob entity to immediately melee-attack the specified target entity once.
        // Works for Hostile Mobs, and Players.
        // Does not work with passive mobs, non-living entities, etc.
        // -->
        if (mechanism.matches("melee_attack") && mechanism.requireObject(EntityTag.class)) {
            getLivingEntity().attack(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name last_hurt_by
        // @input EntityTag
        // @description
        // Tells this mob entity that it was last hurt by the specified entity.
        // Passive mobs will panic and run away when this is set.
        // Angerable mobs will get angry.
        // -->
        if (mechanism.matches("last_hurt_by") && mechanism.requireObject(EntityTag.class)) {
            NMSHandler.entityHelper.setLastHurtBy(getLivingEntity(), mechanism.valueAsType(EntityTag.class).getLivingEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_nibble_time
        // @input DurationTag
        // @description
        // Sets the time until this fish hook is next nibbled. If this value is set zero, biting will be processed instead.
        // if this value is set above zero, when it runs out, a nibble (failed bite) will occur.
        // -->
        if (mechanism.matches("fish_hook_nibble_time") && mechanism.requireObject(DurationTag.class)) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_nibble_time is only valid for FishHook entities.");
                return;
            }
            NMSHandler.fishingHelper.setNibble((FishHook) getBukkitEntity(), mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_bite_time
        // @input DurationTag
        // @description
        // Sets the time until this fish hook is next bit. If this value and also nibble_time are set zero, luring will happen instead.
        // if this value is set above zero, when it runs out, a bite will occur (and a player can reel to catch it, or fail and have nibble set).
        // -->
        if (mechanism.matches("fish_hook_bite_time") && mechanism.requireObject(DurationTag.class)) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_hook_time is only valid for FishHook entities.");
                return;
            }
            NMSHandler.fishingHelper.setHookTime((FishHook) getBukkitEntity(), mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_lure_time
        // @input DurationTag
        // @description
        // Sets the time until this fish hook is next lured. If this value and also bite_time and nibble_time are set zero, the luring value will be reset to a random amount.
        // if this value is set above zero, when it runs out, particles will spawn and bite_time will be set to a random amount.
        // @tags
        // <EntityTag.fish_hook_lure_time>
        // -->
        if (mechanism.matches("fish_hook_lure_time") && mechanism.requireObject(DurationTag.class)) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_lure_time is only valid for FishHook entities.");
                return;
            }
            NMSHandler.fishingHelper.setLureTime((FishHook) getBukkitEntity(), mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_pull
        // @input None
        // @description
        // Pulls the entity this fish hook is attached to towards the caster.
        // -->
        if (mechanism.matches("fish_hook_pull")) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_pull is only valid for FishHook entities.");
                return;
            }
            ((FishHook) getBukkitEntity()).pullHookedEntity();
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_apply_lure
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this fish hook should respect the lure enchantment.
        // Every level of lure enchantment reduces lure time by 5 seconds.
        // @tags
        // <EntityTag.fish_hook_apply_lure>
        // -->
        if (mechanism.matches("fish_hook_apply_lure") && mechanism.requireBoolean()) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_apply_lure is only valid for FishHook entities.");
                return;
            }
            ((FishHook) getBukkitEntity()).setApplyLure(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_hooked_entity
        // @input EntityTag
        // @description
        // Sets the entity this fish hook is attached to.
        // @tags
        // <EntityTag.fish_hook_hooked_entity>
        // -->
        if (mechanism.matches("fish_hook_hooked_entity") && mechanism.requireObject(EntityTag.class)) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_hooked_entity is only valid for FishHook entities.");
                return;
            }
            ((FishHook) getBukkitEntity()).setHookedEntity(mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_min_lure_time
        // @input DurationTag
        // @description
        // Returns the minimum possible time before this fish hook can lure a fish.
        // @tags
        // <EntityTag.fish_hook_min_lure_time>
        // -->
        if (mechanism.matches("fish_hook_min_lure_time") && mechanism.requireObject(DurationTag.class)) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_min_lure_time is only valid for FishHook entities.");
                return;
            }
            ((FishHook) getBukkitEntity()).setMinWaitTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fish_hook_max_lure_time
        // @input DurationTag
        // @description
        // Returns the maximum possible time before this fish hook will lure a fish.
        // @tags
        // <EntityTag.fish_hook_max_lure_time>
        // -->
        if (mechanism.matches("fish_hook_max_lure_time") && mechanism.requireObject(DurationTag.class)) {
            if (!(getBukkitEntity() instanceof FishHook)) {
                mechanism.echoError("fish_hook_max_lure_time is only valid for FishHook entities.");
                return;
            }
            ((FishHook) getBukkitEntity()).setMaxWaitTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
        // <--[mechanism]
        // @object EntityTag
        // @name redo_attack_cooldown
        // @input None
        // @description
        // Forces the player to wait for the full attack cooldown duration for the item in their hand.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <EntityTag.attack_cooldown_duration>
        // <EntityTag.attack_cooldown_max_duration>
        // <EntityTag.attack_cooldown_percent>
        // -->
        if (mechanism.matches("redo_attack_cooldown")) {
            if (!(getLivingEntity() instanceof Player)) {
                mechanism.echoError("Only player-type entities can have attack_cooldowns!");
                return;
            }
            NMSHandler.playerHelper.setAttackCooldown((Player) getLivingEntity(), 0);
        }

        // <--[mechanism]
        // @object EntityTag
        // @name reset_attack_cooldown
        // @input None
        // @description
        // Ends the player's attack cooldown.
        // NOTE: This will do nothing if the player's attack speed attribute is set to 0.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <EntityTag.attack_cooldown_duration>
        // <EntityTag.attack_cooldown_max_duration>
        // <EntityTag.attack_cooldown_percent>
        // -->
        if (mechanism.matches("reset_attack_cooldown")) {
            if (!(getLivingEntity() instanceof Player)) {
                mechanism.echoError("Only player-type entities can have attack_cooldowns!");
                return;
            }
            NMSHandler.playerHelper.setAttackCooldown((Player) getLivingEntity(), Math.round(NMSHandler.playerHelper.getMaxAttackCooldownTicks((Player) getLivingEntity())));
        }

        // <--[mechanism]
        // @object EntityTag
        // @name attack_cooldown_percent
        // @input ElementTag(Decimal)
        // @description
        // Sets the progress of the player's attack cooldown. Takes a decimal from 0 to 1.
        // 0 means the cooldown has just begun, while 1 means the cooldown has been completed.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <EntityTag.attack_cooldown_duration>
        // <EntityTag.attack_cooldown_max_duration>
        // <EntityTag.attack_cooldown_percent>
        // -->
        if (mechanism.matches("attack_cooldown_percent") && mechanism.requireFloat()) {
            if (!(getLivingEntity() instanceof Player)) {
                mechanism.echoError("Only player-type entities can have attack_cooldowns!");
                return;
            }
            float percent = mechanism.getValue().asFloat();
            if (percent >= 0 && percent <= 1) {
                NMSHandler.playerHelper.setAttackCooldown((Player) getLivingEntity(), Math.round(NMSHandler.playerHelper.getMaxAttackCooldownTicks((Player) getLivingEntity()) * mechanism.getValue().asFloat()));
            }
            else {
                Debug.echoError("Invalid percentage! \"" + percent + "\" is not between 0 and 1!");
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name attack_cooldown
        // @input DurationTag
        // @description
        // Sets the player's time since their last attack. If the time is greater than the max duration of their
        // attack cooldown, then the cooldown is considered finished.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <EntityTag.attack_cooldown_duration>
        // <EntityTag.attack_cooldown_max_duration>
        // <EntityTag.attack_cooldown_percent>
        // -->
        if (mechanism.matches("attack_cooldown") && mechanism.requireObject(DurationTag.class)) {
            if (!(getLivingEntity() instanceof Player)) {
                mechanism.echoError("Only player-type entities can have attack_cooldowns!");
                return;
            }
            NMSHandler.playerHelper.setAttackCooldown((Player) getLivingEntity(), mechanism.getValue().asType(DurationTag.class, mechanism.context).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fallingblock_type
        // @input MaterialTag
        // @description
        // Sets the block type of a falling_block entity (only valid while spawning).
        // @tags
        // <EntityTag.fallingblock_material>
        // -->
        if (mechanism.matches("fallingblock_type") && mechanism.requireObject(MaterialTag.class)) {
            if (!(getBukkitEntity() instanceof FallingBlock)) {
                mechanism.echoError("'fallingblock_type' is only valid for Falling Block entities.");
                return;
            }
            NMSHandler.entityHelper.setFallingBlockType((FallingBlock) getBukkitEntity(), mechanism.valueAsType(MaterialTag.class).getModernData());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name tracking_range
        // @input ElementTag(Number)
        // @description
        // Sets the range (in blocks) that an entity can be seen at. This is equivalent to the "entity-tracking-range" value in "Spigot.yml".
        // -->
        if (mechanism.matches("tracking_range") && mechanism.requireInteger()) {
            NMSHandler.entityHelper.setTrackingRange(getBukkitEntity(), mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name fake_pickup
        // @input EntityTag
        // @description
        // Makes it look like this entity (usually a player) has picked up another entity (item, arrow, or XP orb).
        // This technically also works with any entity type.
        // Note that the original entity doesn't actually get picked up, it's still there, just invisible now.
        // -->
        if (mechanism.matches("fake_pickup") && mechanism.requireObject(EntityTag.class)) {
            Entity ent = mechanism.valueAsType(EntityTag.class).getBukkitEntity();
            int amount = 1;
            if (ent instanceof Item) {
                amount = ((Item) ent).getItemStack().getAmount();
            }
            for (Player player : NMSHandler.entityHelper.getPlayersThatSee(getBukkitEntity())) {
                NMSHandler.packetHelper.sendCollectItemEntity(player, getBukkitEntity(), ent, amount);
            }
            if (isPlayer()) {
                NMSHandler.packetHelper.sendCollectItemEntity((Player) getBukkitEntity(), getBukkitEntity(), ent, amount);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name loot_table_id
        // @input ElementTag
        // @description
        // Sets the loot table of a lootable entity.
        // This is the namespaced path of the loot table, provided by a datapack or Minecraft's default data.
        // @tags
        // <EntityTag.loot_table_id>
        // @Example
        // # Sets the nearest zombie's loot table to a phantom's
        // - adjust <player.location.find_entities[zombie].within[5].first> loot_table_id:entities/phantom
        // -->
        if (mechanism.matches("loot_table_id")) {
            if (!(getBukkitEntity() instanceof Lootable)) {
                mechanism.echoError("'loot_table_id' is only valid for lootable entities.");
                return;
            }
            LootTable table = Bukkit.getLootTable(Utilities.parseNamespacedKey(mechanism.getValue().asString()));
            if (table == null) {
                mechanism.echoError("Invalid loot table ID.");
                return;
            }
            ((Lootable) getBukkitEntity()).setLootTable(table);
        }

        tagProcessor.processMechanism(this, mechanism);
    }

    public static HashSet<String> specialEntityMatchables = new HashSet<>(Arrays.asList("entity", "npc", "player", "living", "vehicle", "fish", "projectile", "hanging", "monster", "mob", "animal"));

    public final boolean trySpecialEntityMatcher(String text, boolean isNPC) {
        if (isNPC) {
            return text.equals("entity") || text.equals("npc");
        }
        switch (text) {
            case "entity":
                return true;
            case "npc":
                return isCitizensNPC();
            case "player":
                return isPlayer();
            case "living":
                return isLivingEntityType();
            case "vehicle":
                return getBukkitEntity() instanceof Vehicle;
            case "fish":
                return getBukkitEntity() instanceof Fish;
            case "projectile":
                return getBukkitEntity() instanceof Projectile;
            case "hanging":
                return getBukkitEntity() instanceof Hanging;
            case "monster":
                return isMonsterType();
            case "mob":
                return isMobType();
            case "animal":
                return isAnimalType();
        }
        return false;
    }

    public final boolean tryExactMatcher(String text) {
        if (specialEntityMatchables.contains(text)) {
            return trySpecialEntityMatcher(text, isCitizensNPC());
        }
        if (text.startsWith("npc_") && !text.startsWith("npc_flagged")) {
            String check = text.substring("npc_".length());
            if (specialEntityMatchables.contains(check)) {
                if (check.equals("player")) { // Special case
                    return npc.getEntityType() == EntityType.PLAYER;
                }
                return trySpecialEntityMatcher(check, false);
            }
            return check.equals(CoreUtilities.toLowerCase(npc.getEntityType().name()));
        }
        if (text.contains(":")) {
            if (text.startsWith("entity_flagged:")) {
                return ScriptEvent.coreFlaggedCheck(text.substring("entity_flagged:".length()), getFlagTracker());
            }
            else if (text.startsWith("player_flagged:")) {
                return isPlayer() && ScriptEvent.coreFlaggedCheck(text.substring("player_flagged:".length()), getFlagTracker());
            }
            else if (text.startsWith("npc_flagged:")) {
                return isCitizensNPC() && ScriptEvent.coreFlaggedCheck(text.substring("npc_flagged:".length()), getFlagTracker());
            }
            else if (text.startsWith("vanilla_tagged:")) {
                String tagCheck = text.substring("vanilla_tagged:".length());
                HashSet<String> tags = VanillaTagHelper.tagsByEntity.get(getBukkitEntityType());
                if (tags == null) {
                    return false;
                }
                ScriptEvent.MatchHelper matcher = ScriptEvent.createMatcher(tagCheck);
                for (String tag : tags) {
                    if (matcher.doesMatch(tag)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean advancedMatches(String text) {
        ScriptEvent.MatchHelper matcher = ScriptEvent.createMatcher(text);
        if (isCitizensNPC()) {
            return matcher.doesMatch("npc", this::tryExactMatcher);
        }
        if (getEntityScript() != null && matcher.doesMatch(getEntityScript(), this::tryExactMatcher)) {
            return true;
        }
        if (matcher.doesMatch(getEntityType().getLowercaseName(), this::tryExactMatcher)) {
            return true;
        }
        return false;
    }
}
