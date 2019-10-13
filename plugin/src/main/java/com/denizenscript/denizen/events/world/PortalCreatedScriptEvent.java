package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreatedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // portal created
    //
    // @Regex ^on portal created$
    //
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a portal is created. Note that several features of this event only work in 1.14+.
    //
    // @Context
    // <context.entity> returns the EntityTag that created the portal.
    // <context.reason> returns the reason a portal was created: FIRE, NETHER_PAIR, or END_PLATFORM.
    // <context.blocks> returns a list of block locations where the portal is being created.
    //
    // @Player if the entity that created the portal is a player.
    //
    // -->

    public PortalCreatedScriptEvent() {
        instance = this;
    }

    public static PortalCreatedScriptEvent instance;
    public EntityTag entity;
    public PortalCreateEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("portal created");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getBlocks().get(0).getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PortalCreated";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData((entity != null && entity.isPlayer()) ? entity.getDenizenPlayer() : null,
                (entity != null && entity.isCitizensNPC()) ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity") && event.getEntity() != null) {
            return new EntityTag(event.getEntity());
        }
        else if (name.equals("reason")) {
            return new ElementTag(event.getReason().name());
        }
        else if (name.equals("blocks")) {
            ListTag blocks = new ListTag();
            for (BlockState block : event.getBlocks()) {
                blocks.add(new LocationTag(block.getBlock().getLocation()).identifySimple());
            }
            return blocks;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityCreatesPortal(PortalCreateEvent event) {
        this.event = event;
        entity = null;
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14) && event.getEntity() != null) {
            entity = new EntityTag(event.getEntity());
        }
        fire(event);
    }
}
