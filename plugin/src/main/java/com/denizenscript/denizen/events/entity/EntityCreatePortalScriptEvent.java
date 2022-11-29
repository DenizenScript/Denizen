package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;

public class EntityCreatePortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> creates portal
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity creates a portal. Generally, prefer <@link event portal created> instead of this.
    //
    // @Context
    // <context.entity> returns the EntityTag that created the portal.
    // <context.portal_type> returns the type of portal: CUSTOM, ENDER, or NETHER.
    // <context.blocks> returns a list of block locations where the portal is being created.
    //
    // @Player if the entity that created the portal is a player.
    //
    // -->

    public EntityCreatePortalScriptEvent() {
        registerCouldMatcher("<entity> creates portal");
    }

    public EntityTag entity;
    public EntityCreatePortalEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "portal_type":
                return new ElementTag(event.getPortalType().toString());
            case "blocks":
                ListTag blocks = new ListTag();
                for (BlockState block : event.getBlocks()) {
                    blocks.add(new LocationTag(block.getBlock().getLocation()).identifySimple());
                }
                return blocks;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityCreatesPortal(EntityCreatePortalEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
