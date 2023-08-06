package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.DataPersistenceHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.events.entity.EntityDespawnScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import java.util.HashMap;

public class EntityScriptHelper implements Listener {

    public static HashMap<String, EntityScriptContainer> scripts = new HashMap<>();

    public EntityScriptHelper() {
        Denizen.getInstance().getServer().getPluginManager()
                .registerEvents(this, Denizen.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityTag.rememberEntity(entity);
        EntityDespawnScriptEvent.instance.entity = new EntityTag(entity);
        EntityDespawnScriptEvent.instance.cause = new ElementTag("DEATH");
        EntityDespawnScriptEvent.instance.fire();
        EntityTag.forgetEntity(entity);
    }

    @EventHandler
    public void onChunkUnload(EntitiesUnloadEvent event) {
        for (Entity ent : event.getEntities()) {
            if (!(ent instanceof LivingEntity) || ((LivingEntity) ent).getRemoveWhenFarAway()) {
                EntityTag.rememberEntity(ent);
                EntityDespawnScriptEvent.instance.entity = new EntityTag(ent);
                EntityDespawnScriptEvent.instance.cause = new ElementTag("CHUNK_UNLOAD");
                EntityDespawnScriptEvent.instance.fire();
                EntityTag.forgetEntity(ent);
            }
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
        if (!DataPersistenceHelper.hasDenizenKey(ent, "entity_script")) {
            return null;
        }
        ObjectTag scriptObject = DataPersistenceHelper.getDenizenKey(ent, "entity_script");
        if (!(scriptObject instanceof ScriptTag)) {
            return null;
        }
        return ((ScriptTag) scriptObject).getName();
    }

    /**
     * Marks the entity as having been created by a specified script.
     */
    public static void setEntityScript(Entity ent, String script) {
        if (ent == null || ent.getUniqueId() == null || script == null) {
            return;
        }
        ScriptTag scriptObj = ScriptTag.valueOf(script, CoreUtilities.basicContext);
        if (scriptObj == null) {
            Debug.echoError("Can't set entity script to '" + script + "': not a valid script!");
        }
        DataPersistenceHelper.setDenizenKey(ent, "entity_script", scriptObj);
        return;
    }
}
