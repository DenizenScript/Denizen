package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
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
        this.<BlockExplodesScriptEvent, ObjectTag>registerOptionalDetermination(null, ObjectTag.class, (evt, context, value) -> {
            String blocks = value.identify();
            if (blocks.contains(",") || blocks.startsWith("li@")) { // Raw ListTag check due to there not being a prefix for block strength previously
                evt.event.blockList().clear();
                for (LocationTag newBlock : value.asType(ListTag.class, context).filter(LocationTag.class, context)) {
                    if (newBlock.getWorld() != null) {
                        evt.event.blockList().add(newBlock.getBlock());
                    }
                    else {
                        Debug.echoError("Block input of " + newBlock + " does not contain a valid world.");
                    }
                }
                return true;
            }
            else if (value.asElement().isFloat()) {
                BukkitImplDeprecations.blockExplodesStrengthDetermination.warn();
                evt.event.setYield(value.asElement().asFloat());
                return true;
            }
            return false;
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
