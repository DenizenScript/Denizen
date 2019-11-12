package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
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
    //
    // @Group Block
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a block is destroyed by fire.
    //
    // @Context
    // <context.location> returns the LocationTag the block was burned at.
    // <context.material> returns the MaterialTag of the block that was burned.
    //
    // -->

    public BlockBurnsScriptEvent() {
        instance = this;
    }

    public static BlockBurnsScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public BlockBurnEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("burns");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!runInCheck(path, location)) {
            return false;
        }

        if (!tryMaterial(material, path.eventArgLowerAt(0))) {
            return false;
        }
        return super.matches(path);

    }

    @Override
    public String getName() {
        return "BlockBurns";
    }

    @Override
    public ObjectTag getContext(String name) {
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
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(event.getBlock());
        this.event = event;
        fire(event);
    }
}
