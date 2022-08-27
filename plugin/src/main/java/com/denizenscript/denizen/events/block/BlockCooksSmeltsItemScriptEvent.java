package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;

public class BlockCooksSmeltsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> cooks|smelts <item> (into <item>)
    //
    // @Cancellable true
    //
    // @Group Block
    //
    // @Location true
    //
    // @Triggers when an item is smelted/cooked by a block.
    //
    // @Context
    // <context.location> returns the LocationTag of the block smelting/cooking.
    // <context.source_item> returns the ItemTag that is being smelted/cooked.
    // <context.result_item> returns the ItemTag that is the result of the smelting/cooking.
    //
    // @Determine
    // ItemTag to set the item that is the result of the smelting/cooking.
    //
    // -->

    public BlockCooksSmeltsItemScriptEvent() {
        registerCouldMatcher("<block> cooks|smelts <item> (into <item>)");
    }

    public ItemTag source_item;
    public ItemTag result_item;
    public LocationTag location;
    public BlockCookEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!location.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!source_item.tryAdvancedMatcher(path.eventArgLowerAt(2))) {
            return false;
        }
        if (path.eventArgLowerAt(3).equals("into")) {
            if (!result_item.tryAdvancedMatcher(path.eventArgLowerAt(4))) {
                return false;
            }
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj.canBeType(ItemTag.class)) {
            result_item = determinationObj.asType(ItemTag.class, getTagContext(path));
            event.setResult(result_item.getItemStack());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "source_item": return source_item;
            case "result_item": return result_item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockCooks(BlockCookEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        source_item = new ItemTag(event.getSource());
        result_item = new ItemTag(event.getResult());
        this.event = event;
        fire(event);
    }
}
