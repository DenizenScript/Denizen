package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
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
    //
    // @Determine
    // ItemTag to change the item being consumed.
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
        if (name.equals("item")) {
            return item;
        }
        if (name.equals("hand")) {
            return new ElementTag(event.getHand());
        }
        return super.getContext(name);
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
