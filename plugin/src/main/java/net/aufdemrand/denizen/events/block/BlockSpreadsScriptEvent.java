package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

public class BlockSpreadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block spreads
    // <material> spreads
    //
    // @Regex ^on [^\s]+ spreads$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block spreads based on world conditions, EG, when fire spreads, or when mushrooms spread.
    //
    // @Context
    // <context.source_location> returns the dLocation of the block that spread.
    // <context.location> returns the dLocation of the new block.
    // <context.material> returns the dMaterial of the block that spread.
    //
    // -->

    public BlockSpreadsScriptEvent() {
        instance = this;
    }

    public static BlockSpreadsScriptEvent instance;
    public dLocation location;
    public dLocation source;
    public dMaterial material;
    public BlockSpreadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("spreads") && !lower.startsWith("liquid");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;

        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(0, lower);
        return tryMaterial(material, mat);

    }

    @Override
    public String getName() {
        return "BlockSpreads";
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
        else if (name.equals("source_location")) {
            return source;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockSpreads(BlockSpreadEvent event) {
        source = new dLocation(event.getBlock().getLocation());
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getSource());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
