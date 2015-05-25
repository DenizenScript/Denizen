package net.aufdemrand.denizen.scripts.containers.core;


import net.aufdemrand.denizen.events.scriptevents.EntityDespawnScriptEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.world.DenizenWorldAccess;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class EntityScriptHelper implements Listener {

    static HashMap<UUID, String> entities = new HashMap<UUID, String>();
    private static final Field iWorldAccessList;
    private static final Map<World, DenizenWorldAccess> worlds = new HashMap<World, DenizenWorldAccess>();

    static {
        Field field = null;
        try {
            field = net.minecraft.server.v1_8_R3.World.class.getDeclaredField("u");
            field.setAccessible(true);
        } catch (Exception e) {
            dB.echoError(e);
        }
        iWorldAccessList = field;
    }

    public EntityScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        EntityDespawnScriptEvent.instance.entity = new dEntity(event.getEntity());
        EntityDespawnScriptEvent.instance.cause = new Element("DEATH");
        EntityDespawnScriptEvent.instance.cancelled = false;
        EntityDespawnScriptEvent.instance.fire();
        unlinkEntity(event.getEntity());
    }

    public static void linkWorld(World world) {
        DenizenWorldAccess denizenWorldAccess = new DenizenWorldAccess();
        worlds.put(world, denizenWorldAccess);
        ((CraftWorld) world).getHandle().addIWorldAccess(denizenWorldAccess);
    }

    public static void unlinkWorld(World world) {
        try {
            ((List) iWorldAccessList.get(((CraftWorld) world).getHandle())).remove(worlds.get(world));
            worlds.remove(world);
        } catch (Exception e) {
            dB.echoError(e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        linkWorld(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        unlinkWorld(event.getWorld());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // TODO: This doesn't work. Awaiting Entity Despawn Event PR's for Bukkit:
        // Bukkit: https://github.com/Bukkit/Bukkit/pull/1070
        // CraftBukkit: https://github.com/Bukkit/CraftBukkit/pull/1386
        for (Entity ent: event.getChunk().getEntities()) {
            if (!(ent instanceof LivingEntity) || ((LivingEntity)ent).getRemoveWhenFarAway()) {
                EntityDespawnScriptEvent.instance.entity = new dEntity(ent);
                EntityDespawnScriptEvent.instance.cause = new Element("CHUNK_UNLOAD");
                EntityDespawnScriptEvent.instance.cancelled = false;
                EntityDespawnScriptEvent.instance.fire();
                unlinkEntity(ent);
            }
        }
    }

    public static void reloadEntities() {
        entities.clear();
        ConfigurationSection entity_scripts = DenizenAPI.getCurrentInstance()
                .getEntities().getConfigurationSection("entities.scripts");
        if (entity_scripts == null)
            return;
        for (String Path: entity_scripts.getKeys(false)) {
            UUID id = UUID.fromString(Path);
            String scriptname = entity_scripts.getString(Path + ".scriptname");
            entities.put(id, scriptname);
        }
    }

    public static void saveEntities() {
        FileConfiguration entityScripts = DenizenAPI.getCurrentInstance().getEntities();
        entityScripts.set("entities.scripts", null);
        for (Map.Entry<UUID, String> entry: entities.entrySet()) {
            entityScripts.set("entities.scripts." + entry.getKey() + ".scriptname", entry.getValue());
        }
    }

    /**
     * Indicates whether a specified entity has a custom entity script.
     */
    public static boolean entityHasScript(Entity ent) {
        return getEntityScript(ent) != null;
    }

    /**
     * Returns the name of the entity script that defined this entity, or null if none.
     */
    public static String getEntityScript(Entity ent) {
        if (ent == null)
            return null;
        return getEntityScript(ent.getUniqueId());
    }

    /**
     * Returns the name of the entity script that defined the entity by this UUID, or null if none.
     */
    public static String getEntityScript(UUID entID) {
        if (entID == null)
            return null;
        return entities.get(entID);
    }

    /**
     * Marks the entity as having been created by a specified script.
     */
    public static void setEntityScript(Entity ent, String script) {
        if (ent == null || ent.getUniqueId() == null || script == null)
            return;
        entities.put(ent.getUniqueId(), script);
    }

    /**
     * Removes the entity from the list of scripted entities.
     */
    public static void unlinkEntity(Entity ent) {
        if (ent == null || ent.getUniqueId() == null)
            return;
        entities.remove(ent.getUniqueId());
        FlagManager.clearEntityFlags(new dEntity(ent));
    }
}
