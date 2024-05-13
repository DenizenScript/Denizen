package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChangesFramedItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes framed <item>
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player interacts with an item frame by adding, removing, or rotating the item held in it.
    //
    // @Switch frame:<entity> to only process the event if the item frame entity being matches the input.
    // @Switch action:<action> to only process the event if the change matches the input.
    //
    // @Context
    // <context.frame> returns the EntityTag of the item frame.
    // <context.item> returns the ItemTag of the item held in the item frame.
    // <context.action> returns the ElementTag of the action being performed, based on <@link url https://jd.papermc.io/paper/1.20/io/papermc/paper/event/player/PlayerItemFrameChangeEvent.ItemFrameChangeAction.html>
    //
    // @Determine
    // "ITEM:<ItemTag>" to change the item held by the item frame. If there is an item already in the frame, it will be replaced. To remove the item, set it to air.
    //
    // @Player Always.
    // -->

    public PlayerChangesFramedItemScriptEvent() {
        registerCouldMatcher("player changes framed <item>");
        registerSwitches("frame", "action");
        this.<PlayerChangesFramedItemScriptEvent, ItemTag>registerDetermination("item", ItemTag.class, (evt, context, item) -> {
            evt.event.setItemStack(item.getItemStack());
        });
    }

    public ItemTag item;
    public EntityTag frame;
    public ElementTag action;
    public PlayerItemFrameChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(3, item)) {
            return false;
        }
        if (!path.tryObjectSwitch("frame", frame)) {
            return false;
        }
        if (!path.tryObjectSwitch("action", action)) {
            return false;
        }
        if (!runInCheck(path, frame.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "frame" -> frame;
            case "item" -> new ItemTag(event.getItemStack());
            case "action" -> action;
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangesFramedItem(PlayerItemFrameChangeEvent event) {
        item = new ItemTag(event.getItemStack());
        frame = new EntityTag(event.getItemFrame());
        action = new ElementTag(event.getAction());
        this.event = event;
        fire(event);
    }
}
