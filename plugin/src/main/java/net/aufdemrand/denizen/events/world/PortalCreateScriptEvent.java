package net.aufdemrand.denizen.events.world;


import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // portal created (because <reason>) (in <area>)
    //
    // @Regex ^on portal created( because [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a portal is created.
    //
    // @Context
    // <context.world> returns the dWorld the portal was created in.
    // <context.reason> returns an Element of the reason the portal was created. (FIRE or OBC_DESTINATION)
    // <context.blocks> returns a dList of all the blocks that will become portal blocks.
    //
    // -->

    public PortalCreateScriptEvent() {
        instance = this;
    }

    public static PortalCreateScriptEvent instance;
    public dWorld world;
    public Element reason;
    public dList blocks;
    public PortalCreateEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("portal created");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String rCheck = CoreUtilities.getXthArg(2, lower).equals("because") ? CoreUtilities.getXthArg(3, lower) : CoreUtilities.getXthArg(5, lower);
        if (rCheck.length() > 0 && !rCheck.equals(CoreUtilities.toLowerCase(reason.asString()))) {
            return false;
        }
        return runInCheck(path, dLocation.valueOf(blocks.get(0)));
    }

    @Override
    public String getName() {
        return "PortalCreate";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("blocks")) {
            return blocks;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        world = new dWorld(event.getWorld());
        reason = new Element(event.getReason().toString());
        blocks = new dList();
        for (Block block : event.getBlocks()) {
            blocks.add(new dLocation(block.getLocation()).identify());
        }
        this.event = event;
        cancelled = event.isCancelled();
        fire();
        event.setCancelled(cancelled);
    }
}
