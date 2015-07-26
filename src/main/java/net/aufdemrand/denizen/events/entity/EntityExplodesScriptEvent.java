package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashMap;

public class EntityExplodesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity explodes (in <area>)
    // <entity> explodes (in <area>)
    //
    // @Regex ^on [^\s]+ explodes( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity explodes.
    //
    // @Context
    // <context.blocks> returns a dList of blocks that the entity blew up.
    // <context.entity> returns the dEntity that exploded.
    // <context.location> returns the dLocation the entity blew up at.
    // <context.strength> returns an Element(Decimal) of the strength of the explosion.
    //
    // @Determine
    // dList(dLocation) to set a new lists of blocks that are to be affected by the explosion.
    // Element(Decimal) to change the strength of the explosion.
    //
    // -->

    public EntityExplodesScriptEvent() {
        instance = this;
    }

    public static EntityExplodesScriptEvent instance;
    public dEntity entity;
    public dList blocks;
    public dLocation location;
    public Float strength;
    private Boolean blockSet;
    public EntityExplodeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("explodes");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String target = CoreUtilities.getXthArg(0, lower);
        return entity.matchesEntity(target) && runInCheck(scriptContainer, s, lower, entity.getLocation());
    }

    @Override
    public String getName() {
        return "EntityExplodes";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityExplodeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesDouble(determination)) {
            strength = aH.getFloatFrom(determination);
            return true;
        }
        if (dList.matches(determination)) {
            blocks = new dList();
            blockSet = true;
            for (String loc : dList.valueOf(determination)) {
                dLocation location = dLocation.valueOf(loc);
                if (location == null) {
                    dB.echoError("Invalid location '" + loc + "' check [" + getName() + "]: '  for " + container.getName());
                }
                else {
                    blocks.add(location.identifySimple());
                }
            }
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("location", location);
        context.put("blocks", blocks);
        context.put("strength", new Element(strength));
        return context;
    }

    @EventHandler
    public void onEntityExplodes(EntityExplodeEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(event.getLocation());
        strength = event.getYield();
        blocks = new dList();
        blockSet = false;
        for (Block block : event.blockList()) {
            blocks.add(new dLocation(block.getLocation()).identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        if (blockSet) {
            event.blockList().clear();
            if (blocks.size() > 0) {
                event.blockList().clear();
                for (String loc : blocks) {
                    dLocation location = dLocation.valueOf(loc);
                    event.blockList().add(location.getWorld().getBlockAt(location));
                }
            }
        }
        event.setYield(strength);
    }
}
