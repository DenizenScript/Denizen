package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.DataPersistenceHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.utilities.world.DenizenWorldAccess;
import com.denizenscript.denizen.events.entity.EntityDespawnScriptEvent;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.properties.entity.EntityBoundingBox;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dScript;
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

    static HashMap<UUID, String> entities = new HashMap<>();

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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return;
        }
        NMSHandler.getInstance().getWorldHelper().setWorldAccess(world, new DenizenWorldAccess());
    }

    public static void unlinkWorld(World world) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return;
        }
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return;
        }
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return;
        }
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            if (!DataPersistenceHelper.hasDenizenKey(ent, "entity_script")) {
                return null;
            }
            dScript script = (dScript) DataPersistenceHelper.getDenizenKey(ent, "entity_script");
            if (script == null) {
                return null;
            }
            return script.getName();
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return getEntityScript(Bukkit.getEntity(entID));
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            dScript scriptObj = dScript.valueOf(script);
            if (scriptObj == null) {
                dB.echoError("Can't set entity script to '" + script + "': not a valid script!");
            }
            DataPersistenceHelper.setDenizenKey(ent, "entity_script", scriptObj);
            return;
        }
        entities.put(ent.getUniqueId(), script);
    }

    /**
     * Removes the entity from the list of scripted entities.
     */
    public static void unlinkEntity(final Entity ent) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14_R1)) {
            return;
        }
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
