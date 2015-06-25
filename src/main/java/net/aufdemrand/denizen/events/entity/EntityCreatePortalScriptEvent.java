package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCreatePortalEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityCreatePortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity creates portal (in <area>)
    // <entity> creates portal (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when an entity creates a portal.
    //
    // @Context
    // <context.entity> returns the dEntity that created the portal.
    // <context.portal_type> returns the type of portal: CUSTOM, ENDER, NETHER.
    //
    // -->

    public EntityCreatePortalScriptEvent() {
        instance = this;
    }
    public static EntityCreatePortalScriptEvent instance;
    public dEntity entity;
    public Element portal_type;
//    public dList blocks;
    public EntityCreatePortalEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String entOne = CoreUtilities.getXthArg(0, lower);
        List<String> types = Arrays.asList("entity", "player", "npc");
        return lower.contains("creates portal")
                && (types.contains(entOne) || dEntity.matches(entOne));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String entName = CoreUtilities.getXthArg(0, lower);
        if (!entity.matchesEntity(entName)){
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityCreatesPortal";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityCreatePortalEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("portal_type", portal_type);
//        context.put("blocks", blocks);
        return context;
    }

    @EventHandler
    public void onEntityCreatesPortal(EntityCreatePortalEvent event) {
        entity = new dEntity(event.getEntity());
        portal_type = new Element(event.getPortalType().toString());
/*
        blocks = new dList();
        for (int i=0; i < event.getBlocks().size(); i++) {
            dLocation tempLoc = new dLocation(event.getBlocks().get(i).getBlock().getLocation());
            blocks.add(tempLoc.identifySimple());
        }
*/
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
