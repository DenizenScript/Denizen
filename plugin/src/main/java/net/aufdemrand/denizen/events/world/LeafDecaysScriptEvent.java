package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeafDecaysScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // leaves decay
    // <block> decay
    //
    // @Regex ^on [^\s]+ decay$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when leaves decay.
    //
    // @Context
    // <context.location> returns the dLocation of the leaves.
    // <context.material> returns the dMaterial of the leaves.
    //
    // -->

    public LeafDecaysScriptEvent() {
        instance = this;
    }

    public static LeafDecaysScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public LeavesDecayEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.getXthArg(1, lower).equals("decay");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String mat = path.eventArgLowerAt(0);
        return (mat.equals("leaves") || (tryMaterial(material, mat)))
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "LeafDecays";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLeafDecays(LeavesDecayEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        this.event = event;
        fire(event);
    }
}
