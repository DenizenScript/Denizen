package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.events.entity.EntityDespawnScriptEvent;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.properties.entity.EntityBoundingBox;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.world.DenizenWorldAccess;
import net.aufdemrand.denizencore.objects.Element;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class EntityScriptHelper implements Listener {

    static HashMap<UUID, String> entities = new HashMap<UUID, String>();

    public EntityScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        dEntity.rememberEntity(entity);
        EntityDespawnScriptEvent.instance.entity = new dEntity(entity);
        EntityDespawnScriptEvent.instance.cause = new Element("DEATH");
        EntityDespawnScriptEvent.instance.cancelled = false;
        EntityDespawnScriptEvent.instance.fire();
        dEntity.forgetEntity(entity);
        unlinkEntity(event.getEntity());
    }

    public static void linkWorld(World world) {
        NMSHandler.getInstance().getWorldHelper().setWorldAccess(world, new DenizenWorldAccess());
    }

    public static void unlinkWorld(World world) {
        NMSHandler.getInstance().getWorldHelper().removeWorldAccess(world);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        linkWorld(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        if (event.isCancelled()) {
            return;
        }
        unlinkWorld(event.getWorld());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event instanceof Cancellable &&((Cancellable) event).isCancelled()) {
            return;
        }
        // TODO: This doesn't work. Awaiting Entity Despawn Event PR's for Bukkit:
        // Bukkit: https://github.com/Bukkit/Bukkit/pull/1070
        // CraftBukkit: https://github.com/Bukkit/CraftBukkit/pull/1386
        for (Entity ent : event.getChunk().getEntities()) {
            if (!(ent instanceof LivingEntity) || ((LivingEntity) ent).getRemoveWhenFarAway()) {
                dEntity.rememberEntity(ent);
                EntityDespawnScriptEvent.instance.entity = new dEntity(ent);
                EntityDespawnScriptEvent.instance.cause = new Element("CHUNK_UNLOAD");
                EntityDespawnScriptEvent.instance.cancelled = false;
                EntityDespawnScriptEvent.instance.fire();
                dEntity.forgetEntity(ent);
                unlinkEntity(ent);
            }
        }
    }

    public static void reloadEntities() {
        entities.clear();
        ConfigurationSection entity_scripts = DenizenAPI.getCurrentInstance()
                .getEntities().getConfigurationSection("entities.scripts");
        if (entity_scripts == null) {
            return;
        }
        for (String Path : entity_scripts.getKeys(false)) {
            UUID id = UUID.fromString(Path);
            String scriptname = entity_scripts.getString(Path + ".scriptname");
            entities.put(id, scriptname);
        }
    }

    public static void saveEntities() {
        FileConfiguration entityScripts = DenizenAPI.getCurrentInstance().getEntities();
        entityScripts.set("entities.scripts", null);
        for (Map.Entry<UUID, String> entry : entities.entrySet()) {
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
        if (ent == null) {
            return null;
        }
        return getEntityScript(ent.getUniqueId());
    }

    /**
     * Returns the name of the entity script that defined the entity by this UUID, or null if none.
     */
    public static String getEntityScript(UUID entID) {
        if (entID == null) {
            return null;
        }
        return entities.get(entID);
    }

    /**
     * Marks the entity as having been created by a specified script.
     */
    public static void setEntityScript(Entity ent, String script) {
        if (ent == null || ent.getUniqueId() == null || script == null) {
            return;
        }
        entities.put(ent.getUniqueId(), script);
    }

    /**
     * Removes the entity from the list of scripted entities.
     */
    public static void unlinkEntity(final Entity ent) {
        if (ent == null || ent.getUniqueId() == null) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        entities.remove(ent.getUniqueId());
                        FlagManager.clearEntityFlags(new dEntity(ent));
                        EntityBoundingBox.remove(ent.getUniqueId());
                    }
                }, 5);
    }
}
