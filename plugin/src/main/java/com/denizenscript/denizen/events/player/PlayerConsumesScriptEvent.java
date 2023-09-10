package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player consumes item
    // player consumes <item>
    //
    // @Regex ^on player consumes [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player consumes (eats/drinks) an item (like food or potions).
    //
    // @Context
    // <context.item> returns the ItemTag.
    // <context.hand> returns an ElementTag of the hand being used to consume the item. Can be either HAND or OFF_HAND. Requires a 1.19+ server.
    //
    // @Determine
    // ItemTag to change the item being consumed. Use with caution, if the player is eating a stack of items, this will replace the entire stack.
    //
    // @Player Always.
    //
    // -->

    public PlayerConsumesScriptEvent() {
    }


    public ItemTag item;
    public PlayerItemConsumeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player consumes")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ItemTag.matches(determination)) {
            BukkitTagContext context = new BukkitTagContext(EntityTag.getPlayerFrom(event.getPlayer()), null, new ScriptTag(path.container));
            ItemTag newitem = ItemTag.valueOf(determination, context);
            if (newitem != null) {
                event.setItem(newitem.getItemStack());
                return true;
            }
            else {
                Debug.echoError("Invalid event 'item' check [" + getName() + "] ('determine item ????'): '" + determination + "' for " + path.container.getName());
            }

        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? EntityTag.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> item;
            case "hand" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.getHand()) : null;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerConsumes(PlayerItemConsumeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItem());
        this.event = event;
        fire(event);
    }
}
