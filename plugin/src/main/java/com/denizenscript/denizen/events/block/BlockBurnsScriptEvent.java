package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

public class BlockBurnsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block burns
    // <block> burns
    //
    // @Regex ^on [^\s]+ burns$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block is destroyed by fire.
    //
    // @Context
    // <context.location> returns the dLocation the block was burned at.
    // <context.material> returns the dMaterial of the block that was burned.
    //
    // -->

    public BlockBurnsScriptEvent() {
        instance = this;
    }

    public static BlockBurnsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public BlockBurnEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("burns");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = path.eventArgLowerAt(0);
        return tryMaterial(material, mat);

    }

    @Override
    public String getName() {
        return "BlockBurns";
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
    public void onBlockBurns(BlockBurnEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        this.event = event;
        fire(event);
    }
}
