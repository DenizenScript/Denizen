package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockFadesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block fades
    // <block> fades
    //
    // @Regex ^on [^\s]+ fades$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block fades, melts or disappears based on world conditions.
    //
    // @Context
    // <context.location> returns the dLocation the block faded at.
    // <context.material> returns the dMaterial of the block that faded.
    //
    // -->

    public BlockFadesScriptEvent() {
        instance = this;
    }

    public static BlockFadesScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public BlockFadeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("fades");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = path.eventArgLowerAt(0);
        return tryMaterial(material, mat);
    }

    @Override
    public String getName() {
        return "BlockFades";
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
    public void onBlockFades(BlockFadeEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        this.event = event;
        fire(event);
    }
}
