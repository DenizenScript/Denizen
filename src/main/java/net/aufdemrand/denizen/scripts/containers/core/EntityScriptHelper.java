package net.aufdemrand.denizen.scripts.containers.core;


import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class EntityScriptHelper implements Listener {

    static HashMap<UUID, String> entities = null;

    public EntityScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        unlinkEntity(event.getEntity());
    }

    @EventHandler
    public void onEntityDespawn(ChunkUnloadEvent event) {
        // TODO: This doesn't seem to work + no proper entity despawn event... eck.
        for (Entity ent: event.getChunk().getEntities()) {
            if (!(ent instanceof LivingEntity) || ((LivingEntity)ent).getRemoveWhenFarAway())
                unlinkEntity(ent);
        }
    }

    public static void reloadEntities() {
        if (entities == null)
            entities = new HashMap<UUID, String>();
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
    }
}
