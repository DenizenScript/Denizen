package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;

public class EntityCreatePortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity creates portal
    // <entity> creates portal
    //
    // @Regex ^on [^\s]+ creates portal$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity creates a portal.
    //
    // @Context
    // <context.entity> returns the dEntity that created the portal.
    // <context.portal_type> returns the type of portal: CUSTOM, ENDER, NETHER.
    //
    // @Player if the entity that created the portal is a player.
    //
    // -->

    public EntityCreatePortalScriptEvent() {
        instance = this;
    }

    public static EntityCreatePortalScriptEvent instance;
    public dEntity entity;
    public ElementTag portal_type;
    public EntityCreatePortalEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).contains("creates portal");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityCreatesPortal";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("portal_type")) {
            return portal_type;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityCreatesPortal(EntityCreatePortalEvent event) {
        entity = new dEntity(event.getEntity());
        portal_type = new ElementTag(event.getPortalType().toString());
        // TODO: Add this back?
/*
        blocks = new ListTag();
        for (int i=0; i < event.getBlocks().size(); i++) {
            dLocation tempLoc = new dLocation(event.getBlocks().get(i).getBlock().getLocation());
            blocks.add(tempLoc.identifySimple());
        }
*/
        this.event = event;
        fire(event);
    }
}
