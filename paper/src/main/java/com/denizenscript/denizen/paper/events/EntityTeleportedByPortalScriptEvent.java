package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityTeleportedByPortalScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> teleported by portal
    //
    // @Switch to:<world> to only process the event if the world the entity is being teleported to matches the specified WorldTag matcher.
    // @Switch portal_type:<type> to only process the event if the portal's type matches the specified portal type.
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers When an entity is about to be teleported by a portal (currently only fires for nether portals).
    //
    // @Context
    // <context.entity> returns an EntityTag of the entity being teleported.
    // <context.target_world> returns a WorldTag of the world the entity is being teleported to.
    // <context.portal_type> returns an ElementTag of the portal's type. Will be one of <@link url https://jd.papermc.io/paper/1.19/org/bukkit/PortalType.html>.
    //
    // @Determine
    // "TARGET_WORLD:" + WorldTag to set the world the entity will be teleported to.
    // "REMOVE_TARGET_WORLD" to remove the target world. Should usually cancel the event instead of using this.
    //
    // -->

    public EntityTeleportedByPortalScriptEvent() {
        registerCouldMatcher("<entity> teleported by portal");
        registerSwitches("to", "portal_type");
    }

    EntityPortalReadyEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("to", new WorldTag(event.getTargetWorld()))) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "portal_type", event.getPortalType().name())) {
            return false;
        }
        if (!path.tryArgObject(0, new EntityTag(event.getEntity()))) {
            return false;
        }
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> new EntityTag(event.getEntity());
            case "target_world" -> event.getTargetWorld() != null ? new WorldTag(event.getTargetWorld()) : null;
            case "portal_type" -> new ElementTag(event.getPortalType());
            default -> super.getContext(name);
        };
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = CoreUtilities.toLowerCase(determinationObj.toString());
            if (determination.startsWith("target_world:")) {
                WorldTag world = WorldTag.valueOf(determination.substring("target_world:".length()), getTagContext(path));
                if (world != null) {
                    event.setTargetWorld(world.getWorld());
                    return true;
                }
            }
            else if (determination.equals("remove_target_world")) {
                event.setTargetWorld(null);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @EventHandler
    public void onEntityPortalReady(EntityPortalReadyEvent event) {
        this.event = event;
        fire(event);
    }
}
