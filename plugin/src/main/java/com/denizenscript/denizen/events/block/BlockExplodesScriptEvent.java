package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.List;

public class BlockExplodesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> explodes
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a block explodes (like a bed in the nether. For TNT, refer to the "entity explodes" event instead). For a block being destroyed by an explosion, refer to the "block destroyed by explosion" event instead.
    //
    // @Context
    // <context.block> returns the location of the exploding block.
    // <context.blocks> returns a ListTag of blocks that blew up.
    // <context.strength> returns an ElementTag(Decimal) of the strength of the explosion.
    //
    // @Determine
    // ListTag(LocationTag) to set a new lists of blocks that are to be affected by the explosion.
    // "STRENGTH:<ElementTag(Decimal)>" to change the strength of the explosion.
    //
    // -->

    public BlockExplodesScriptEvent() {
        registerCouldMatcher("<block> explodes");
        this.<BlockExplodesScriptEvent, ListTag>registerOptionalDetermination(null, ListTag.class, (evt, context, value) -> {
            evt.event.blockList().clear();
            boolean valid = false;
            for (LocationTag newBlock : value.filter(LocationTag.class, context)) {
                evt.event.blockList().add(newBlock.getBlock());
                valid = true;
            }
            if (!valid) {
                Debug.echoError("No blocks in the provided list were valid.");
                return false;
            }
            return true;
        });
        this.<BlockExplodesScriptEvent, ElementTag>registerOptionalDetermination("strength", ElementTag.class, (evt, context, value) -> {
            if (value.isFloat()) {
                evt.event.setYield(value.asFloat());
                return true;
            }
            return false;
        });
    }

    public BlockExplodeEvent event;
    public List<Block> blocks;
    public LocationTag location;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, location)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "block" -> location;
            case "blocks" -> new ListTag(this.blocks, block -> new LocationTag(block.getLocation()));
            case "strength" -> new ElementTag(event.getYield());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBlockExplodes(BlockExplodeEvent event) {
        this.blocks = event.blockList();
        this.event = event;
        location = new LocationTag(event.getBlock().getLocation());
        fire(event);
    }
}
