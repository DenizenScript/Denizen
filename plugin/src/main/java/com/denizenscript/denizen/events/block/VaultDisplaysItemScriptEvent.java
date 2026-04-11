package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.VaultDisplayItemEvent;

public class VaultDisplaysItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vault displays <item>
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a vault block displays an item.
    //
    // @Context
    // <context.location> returns the LocationTag of the vault block.
    // <context.item> returns the ItemTag being displayed.
    //
    // @Determine
    // "ITEM:<ItemTag>" to set the item being displayed.
    //
    // -->

    public VaultDisplaysItemScriptEvent() {
        registerCouldMatcher("vault displays <item>");
        this.<VaultDisplaysItemScriptEvent, ItemTag>registerDetermination("item", ItemTag.class, (evt, context, input) -> {
            evt.event.setDisplayItem(input.getItemStack());
        });
    }

    public LocationTag location;
    public VaultDisplayItemEvent event;
    public ItemTag item;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> new ItemTag(event.getDisplayItem());
            case "location" -> location;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onVaultDisplayItemEvent(VaultDisplayItemEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getDisplayItem());
        this.event = event;
        fire(event);
    }
}
