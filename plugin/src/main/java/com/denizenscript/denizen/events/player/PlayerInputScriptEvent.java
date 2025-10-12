package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Input;
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
    // @Triggers when a player sends updated keyboard/gamepad movement control input to the server.
    //
    // @Context
    // <context.backward> returns whether the player is providing backwards movement input (normally this means they are pressing S).
    // <context.forward> returns whether the player is providing forwards movement input (normally this means they are pressing W).
    // <context.left> returns whether the player is providing left movement input (normally this means they are pressing A).
    // <context.right> returns whether the player is providing right movement input (normally this means they are pressing D).
    // <context.jump> returns whether the player is providing jump input (normally this means they are pressing SPACEBAR).
    // <context.sneak> returns whether the player is providing sneak input (normally this means they are pressing SHIFT).
    // <context.sprint> returns whether the player is providing sprint input (normally this means they are pressing CONTROL).
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
