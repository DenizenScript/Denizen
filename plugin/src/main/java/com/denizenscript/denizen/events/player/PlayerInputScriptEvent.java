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
    // <context.is_backward> returns an ElementTag(Boolean) that signifies whether the player moves backward
    // <context.is_forward> returns an ElementTag(Boolean) that signifies whether the player moves forward
    // <context.is_left> returns an ElementTag(Boolean) that signifies whether the player moves left
    // <context.is_right> returns an ElementTag(Boolean) that signifies whether the player moves right
    // <context.is_jump> returns an ElementTag(Boolean) that signifies whether the player jumps
    // <context.is_sneak> returns an ElementTag(Boolean) that signifies whether the player sneaks
    // <context.is_sprint> returns an ElementTag(Boolean) that signifies whether the player sprints
    //
    // @Player Always.
    //
    // -->

    public PlayerInputScriptEvent() {
        registerCouldMatcher("player input");
    }

    public PlayerInputEvent event;
    public Player player;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player);
    }

    public ObjectTag getContext(String name) {
        Input i = event.getInput();
        return switch (name) {
            case "is_backward" -> new ElementTag(i.isBackward());
            case "is_forward" -> new ElementTag(i.isForward());
            case "is_left" -> new ElementTag(i.isLeft());
            case "is_right" -> new ElementTag(i.isRight());
            case "is_jump" -> new ElementTag(i.isJump());
            case "is_sneak" -> new ElementTag(i.isSneak());
            case "is_sprint" -> new ElementTag(i.isSprint());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerInputEvent(PlayerInputEvent event) {
        this.event = event;
        player = event.getPlayer();
        fire(event);
    }
}
