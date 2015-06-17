package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityTargetsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity targets (<entity>) (in <notable cuboid>)
    // entity targets (<entity>) because <cause> (in <notable cuboid>)
    // <entity> targets (<entity>) (in <notable cuboid>)
    // <entity> targets (<entity>) because <cause> (in <notable cuboid>)
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
    // <context.cuboids> returns dList of cuboids event happened in. DEPRECATED.
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
        String lower = CoreUtilities.toLowerCase(s);
        String attacker = CoreUtilities.getXthArg(0, lower);
        String cmd = CoreUtilities.getXthArg(1, lower);
        List<String> types = Arrays.asList("entity", "player", "npc");
        return cmd.equals("targets")
                && (types.contains(attacker) || dEntity.matches(attacker));
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
        Integer pos = lower.indexOf(" in ") + 4;
        if (pos > 4) {
            Integer end = lower.indexOf(" ", pos) < 0 ? lower.length(): lower.indexOf(" ", pos);
            String it = lower.substring(pos, end);
            if (dCuboid.matches(it)) {
                dCuboid cuboid = dCuboid.valueOf(it);
                if (!cuboid.isInsideCuboid(location)) {
                    return false;
                }
            }
            else if (dEllipsoid.matches(it)) {
                dEllipsoid ellipsoid = dEllipsoid.valueOf(it);
                if (!ellipsoid.contains(location)) {
                    return false;
                }
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [" + getName() + "]: '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }
        pos = lower.indexOf(" because ") + 9;
        if (pos > 9) {
            Integer end = lower.indexOf(" ", pos) < 0 ? lower.length(): lower.indexOf(" ", pos);
            String it = lower.substring(pos, end);
            if (!it.equals(reason.toString().toLowerCase())) {
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
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("reason", reason);
        if (target != null) {
            context.put("target", target);
        }
        context.put("cuboids", cuboids);
        return context;
    }

    @EventHandler
    public void onEntityTargets(EntityTargetEvent event) {
        entity = new dEntity(event.getEntity());
        reason = new Element(event.getReason().toString());
        target = event.getTarget() != null ? new dEntity(event.getTarget()): null;
        location = new dLocation(event.getEntity().getLocation());
        cuboids = new dList();
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }

}
