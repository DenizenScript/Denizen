package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerClicksFakeEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player clicks fake entity
    // player (right/left) clicks fake entity
    //
    // @Regex ^on player ([^\s]+ )?clicks fake entity$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Plugin Paper
    //
    // @Triggers when a player clicks a fake entity, one that is only shown to the player and not tracked by the server.
    //
    // @Context
    // <context.entity> returns the EntityTag of the entity that was clicked. Note that this entity is not being tracked by the server, so many operations may not be possible on it.
    // <context.hand> returns an ElementTag of the hand used to click.
    // <context.click_type> returns an ElementTag of the click type (left/right).
    //
    // @Player Always.
    //
    // -->

    public PlayerClicksFakeEntityScriptEvent() {
        instance = this;
    }

    public static PlayerClicksFakeEntityScriptEvent instance;
    public PlayerUseUnknownEntityEvent event;

    @Override
    public boolean couldMatch(ScriptEvent.ScriptPath path) {
        return path.eventLower.startsWith("player clicks fake")
                || path.eventLower.startsWith("player right clicks fake")
                || path.eventLower.startsWith("player left clicks fake");
    }

    @Override
    public boolean matches(ScriptEvent.ScriptPath path) {
        if (path.eventArgLowerAt(1).equals("left") && !event.isAttack()) {
            return false;
        }
        else if (path.eventArgLowerAt(1).equals("right") && event.isAttack()) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerClicksFakeEntity";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            FakeEntity fakeEntity = FakeEntity.getFakeEntityFor(event.getPlayer().getUniqueId(), event.getEntityId());
            if (fakeEntity != null) {
                return fakeEntity.entity;
            }
        }
        else if (name.equals("hand")) {
            return new ElementTag(event.getHand().name());
        }
        else if (name.equals("click_type")) {
            return new ElementTag(event.isAttack() ? "left" : "right");
        }
        return super.getContext(name);
    }

    @EventHandler
    public void clickFakeEntity(PlayerUseUnknownEntityEvent event) {
        this.event = event;
        fire(event);
    }
}
