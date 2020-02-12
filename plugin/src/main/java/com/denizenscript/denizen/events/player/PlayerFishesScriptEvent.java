package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFishesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player fishes (<entity>/<item>) (while <state>)
    //
    // @Regex ^on player fishes( [^\s]+)?( while [^\s]+)?$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a player uses a fishing rod.
    //
    // @Context
    // <context.hook> returns an EntityTag of the hook.
    // <context.state> returns an ElementTag of the fishing state. Valid states: <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/player/PlayerFishEvent.State.html>
    // <context.entity> returns an EntityTag of the entity that got caught.
    // <context.item> returns an ItemTag of the item gotten, if any.
    //
    // @Determine
    // "CAUGHT:" + ItemTag to change the item that was caught (only if an item was already being caught).
    //
    // @Player Always.
    //
    // -->

    public PlayerFishesScriptEvent() {
        instance = this;
    }

    public static PlayerFishesScriptEvent instance;
    public EntityTag hook;
    public ElementTag state;
    public EntityTag entity;
    public ItemTag item;
    public PlayerFishEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player fishes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String fish = path.eventArgLowerAt(2);

        if (!fish.isEmpty() && !fish.equals("in") && !fish.equals("while")) {
            if (entity == null) {
                return false;
            }
            if (!tryEntity(entity, fish)) {
                if (item == null) {
                    return false;
                }
                if (!tryItem(item, fish)) {
                    return false;
                }
            }
        }

        String[] data = path.eventArgsLower;
        for (int index = 2; index < data.length; index++) {
            if (data[index].equals("while") && !data[index + 1].equalsIgnoreCase(state.asString())) {
                return false;
            }
        }

        if (!runInCheck(path, hook.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (!isDefaultDetermination(determinationObj) && determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            if (CoreUtilities.toLowerCase(determination).startsWith("caught:")) {
                item = ItemTag.valueOf(determination.substring("caught:".length()));
                if (entity != null && entity.getBukkitEntityType() == EntityType.DROPPED_ITEM) {
                    ((Item) entity.getBukkitEntity()).setItemStack(item.getItemStack());
                }
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public String getName() {
        return "PlayerFishes";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.isPlayer(event.getPlayer()) ? EntityTag.getPlayerFrom(event.getPlayer()) :
                EntityTag.isPlayer(event.getCaught()) ? EntityTag.getPlayerFrom(event.getCaught()) : null,
                EntityTag.isCitizensNPC(event.getPlayer()) ? EntityTag.getNPCFrom(event.getPlayer()) :
                        EntityTag.isCitizensNPC(event.getCaught()) ? EntityTag.getNPCFrom(event.getCaught()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("hook")) {
            return hook;
        }
        else if (name.equals("entity") && entity != null) {
            return entity.getDenizenObject();
        }
        else if (name.equals("item") && item != null) {
            return item;
        }
        else if (name.equals("state")) {
            return state;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerFishes(PlayerFishEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Entity hookEntity = NMSHandler.getEntityHelper().getFishHook(event);
        EntityTag.rememberEntity(hookEntity);
        hook = new EntityTag(hookEntity);
        state = new ElementTag(event.getState().toString());
        item = null;
        entity = null;
        Entity caughtEntity = event.getCaught();
        if (caughtEntity != null) {
            EntityTag.rememberEntity(caughtEntity);
            entity = new EntityTag(caughtEntity);
            if (caughtEntity instanceof Item) {
                item = new ItemTag(((Item) caughtEntity).getItemStack());
            }
        }
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(hookEntity);
        EntityTag.forgetEntity(caughtEntity);
    }
}
