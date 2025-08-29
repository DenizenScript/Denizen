package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;

public class PlayerInputScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player input
    //
    // @Group Player
    //
    // @Triggers when a player sends updated input to the server.
    //
    // @Context
    // <context.backward> returns whether the player provided backwards movement input
    // <context.forward> returns whether the player provided forward movement input
    // <context.left> returns whether the player provided left movement input
    // <context.right> returns whether the player provided right movement input
    // <context.jump> returns whether the player provided jump input
    // <context.sneak> returns whether the player provided sneak input
    // <context.sprint> returns whether the player provided sprint input
    //
    // @Player Always.
    //
    // -->

    public PlayerInputScriptEvent() {
        registerCouldMatcher("player input");
    }

    public PlayerInputEvent event;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    public ObjectTag getContext(String name) {
        Input i = event.getInput();
        return switch (name) {
            case "backward" -> new ElementTag(i.isBackward());
            case "forward" -> new ElementTag(i.isForward());
            case "left" -> new ElementTag(i.isLeft());
            case "right" -> new ElementTag(i.isRight());
            case "jump" -> new ElementTag(i.isJump());
            case "sneak" -> new ElementTag(i.isSneak());
            case "sprint" -> new ElementTag(i.isSprint());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerInputEvent(PlayerInputEvent event) {
        this.event = event;
        fire(event);
    }
}
