package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // <context.source_location> returns the LocationTag of the block that spread.
    // <context.location> returns the LocationTag of the new block.
    // <context.material> returns the MaterialTag of the block that spread.
    //
    // -->

    public BlockSpreadsScriptEvent() {
        instance = this;
    }

    public static BlockSpreadsScriptEvent instance;
    public LocationTag location;
    public LocationTag source;
    public MaterialTag material;
    public BlockSpreadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("spreads") && !lower.startsWith("liquid");
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
        return "BlockSpreads";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        source = new LocationTag(event.getBlock().getLocation());
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getSource());
        this.event = event;
        fire(event);
    }
}
