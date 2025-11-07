package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.VaultDisplayItemEvent;

public class VaultDisplayItemScriptEvent extends BukkitScriptEvent implements Listener {

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
    // ItemTag to set the item being displayed.
    //
    // -->

    public VaultDisplayItemScriptEvent() {
        registerCouldMatcher("vault displays <item>");
        this.<VaultDisplayItemScriptEvent, ItemTag>registerDetermination(null, ItemTag.class, (evt, context, item) -> {
            this.item = item;
            evt.event.setDisplayItem(item.getItemStack());
        });
    }

    public LocationTag location;
    public ItemTag item;
    public VaultDisplayItemEvent event;

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
            case "item" -> item;
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