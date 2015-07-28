package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashMap;

public class EntityTargetsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity targets (<entity>) (because <cause>) (in <area>)
    // <entity> targets (<entity>) (because <cause>) (in <area>)
    //
    // @Regex ^on [^\s]+ targets( [^\s]+)?( because [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity interacts with a block (EG an arrow hits a button)
    //
    // @Triggers when an entity targets a new entity.
    //
    // @Context
    // <context.entity> returns the targeting entity.
    // <context.reason> returns the reason the entity changed targets.
    // <context.target> returns the targeted entity.
    // <context.cuboids> DEPRECATED.
    //
    // @Determine
    // dEntity to make the entity target a different entity instead.
    //
    // -->

    public EntityTargetsScriptEvent() {
        instance = this;
    }

    public static EntityTargetsScriptEvent instance;
    public dEntity entity;
    public Element reason;
    public dEntity target;
    public dList cuboids;
    private dLocation location;
    public EntityTargetEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("targets");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!entity.matchesEntity(CoreUtilities.getXthArg(0, lower))) {
            return false;
        }
        String victim = CoreUtilities.getXthArg(2, lower);
        if (!victim.equals("in") && !victim.equals("because") && target != null && victim.length() > 0) {
            if (!target.matchesEntity(victim)) {
                return false;
            }
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        Integer pos = lower.indexOf(" because ") + 9;
        if (pos > 9) {
            Integer end = lower.indexOf(" ", pos) < 0 ? lower.length() : lower.indexOf(" ", pos);
            if (!lower.substring(pos, end).equals(reason.toString().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "EntityTargets";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityTargetEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (dEntity.matches(determination)) {
            target = dEntity.valueOf(determination);
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("cuboids")) {
            return cuboids;
        }
        else if ((name.equals("target")) && (target != null)) {
            return target;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTargets(EntityTargetEvent event) {
        entity = new dEntity(event.getEntity());
        reason = new Element(event.getReason().toString());
        target = event.getTarget() != null ? new dEntity(event.getTarget()) : null;
        location = new dLocation(event.getEntity().getLocation());
        cuboids = new dList();
        for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
