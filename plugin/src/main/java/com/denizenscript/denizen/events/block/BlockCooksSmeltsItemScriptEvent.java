package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;

public class BlockCooksSmeltsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> smelts <item> (into <item>)
    // <block> cooks <item> (into <item>)
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
        instance = this;
        registerCouldMatcher("<block> cooks|smelts <item> (into <item>)");
    }

    public static BlockCooksSmeltsItemScriptEvent instance;
    public ItemTag source_item;
    public ItemTag result_item;
    public Block block;
    public BlockCookEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!tryMaterial(block.getType(), path.eventArgLowerAt(0))) {
            return false;
        }
        if (!tryItem(source_item, path.eventArgLowerAt(2))) {
            return false;
        }
        if (path.eventArgLowerAt(3).equals("into")) {
            if (!tryItem(result_item, path.eventArgLowerAt(4))) {
                return false;
            }
        }
        if (!runInCheck(path, block.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "BlockCooksSmelts";
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
            case "location": return new LocationTag(block.getLocation());
            case "source_item": return source_item;
            case "result_item": return result_item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockCooks(BlockCookEvent event) {
        block = event.getBlock();
        source_item = new ItemTag(event.getSource());
        result_item = new ItemTag(event.getResult());
        this.event = event;
        fire(event);
    }
}
