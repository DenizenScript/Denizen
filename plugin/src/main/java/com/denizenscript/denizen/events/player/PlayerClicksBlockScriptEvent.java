package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.HashSet;

public class PlayerClicksBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player (right|left) clicks <block>
    //
    // @Cancellable true
    //
    // @Group Player
    //
    // @Warning this event may in some cases double-fire, requiring usage of the 'ratelimit' command (like 'ratelimit <player> 1t') to prevent doubling actions.
    //
    // @Switch with:<item> to only process the event if a specified item was held.
    // @Switch using:hand/off_hand/either_hand to only process the event if the specified hand was used to click.
    // @Switch type:<material> to only run if the block clicked matches the material input.
    //
    // @Location true
    //
    // @Triggers when a player clicks on a block or in the air.
    //
    // @Context
    // <context.item> returns the ItemTag the player is clicking with.
    // <context.location> returns the LocationTag the player is clicking on.
    // <context.relative> returns a LocationTag of the air block in front of the clicked block.
    // <context.click_type> returns an ElementTag of the Spigot API click type <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/Action.html>.
    // <context.hand> returns an ElementTag of the used hand.
    //
    // @Player Always.
    //
    // -->

    public PlayerClicksBlockScriptEvent() {
        registerCouldMatcher("player (right|left) clicks <block>");
        registerSwitches("with", "using", "type");
    }

    public PlayerInteractEvent event;
    public ItemTag item;
    public LocationTag location;
    public ElementTag click_type;
    public ElementTag hand;
    public MaterialTag blockMaterial;

    private boolean runUsingCheck(ScriptPath path) {
        String using = path.switches.get("using");
        if (using == null) {
            int index;
            for (index = 0; index < path.eventArgsLower.length; index++) {
                if (path.eventArgsLower[index].equals("using")) {
                    break;
                }
            }
            if (index >= path.eventArgsLower.length) {
                using = "hand";
            }
            else {
                using = path.eventArgLowerAt(index + 1);
            }
        }
        if (!using.equals("hand") && !using.equals("off_hand") && !using.equals("either_hand")) {
            Debug.echoError("Invalid USING hand in " + getName() + " for '" + path.event + "' in " + path.container.getName());
            return false;
        }
        if (!using.equals("either_hand") && !runGenericCheck(using, hand.identify())) {
            return false;
        }
        return true;
    }

    private static final HashSet<String> matchHelpList = new HashSet<>(Arrays.asList("at", "entity", "npc", "player", "vehicle", "projectile", "hanging", "fake", "item"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        boolean clickFirst = path.eventArgLowerAt(1).equals("clicks");
        if (!clickFirst && !path.eventArgLowerAt(2).equals("clicks")) {
            return false;
        }
        if (!clickFirst && !path.eventArgLowerAt(1).equals("right") && !path.eventArgLowerAt(1).equals("left")) {
            return false;
        }
        String clickedOn = path.eventArgLowerAt(clickFirst ? 2 : 3);
        if (matchHelpList.contains(clickedOn)) {
            return false;
        }
        if (!clickedOn.isEmpty() && !couldMatchBlock(clickedOn)
                && !clickedOn.equals("with") && !clickedOn.equals("in") && !clickedOn.equals("using")) { // Legacy format support
            return false;
        }
        if (!couldMatchLegacyInArea(path.eventLower)) {
            return false;
        }
        if (clickedOn.isEmpty()) {
            Debug.echoError("'on player clicks:' is is not valid, use 'on player clicks block:' (for script '" + path.container.getName() + "').");
        }
        return true;
    }

    private static final HashSet<String> withHelpList = new HashSet<>(Arrays.asList("with", "using", "in"));

    @Override
    public boolean matches(ScriptPath path) {
        int index = path.eventArgLowerAt(1).equals("clicks") ? 1 : 2;
        if (index == 2 && !click_type.identify().startsWith(path.eventArgLowerAt(1).toUpperCase())) {
            return false;
        }
        String mat = path.eventArgLowerAt(index + 1);
        if (mat.length() > 0 && !withHelpList.contains(mat) && !blockMaterial.tryAdvancedMatcher(mat)) {
            return false;
        }
        if (!nonSwitchWithCheck(path, new ItemTag(event.getItem()))) {
            return false;
        }
        if (!runWithCheck(path, new ItemTag(event.getItem()))) {
            return false;
        }
        if (!runUsingCheck(path)) {
            return false;
        }
        if (!runInCheck(path, location != null ? location : event.getPlayer().getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("type", blockMaterial)) {
            return false;
        }
        return super.matches(path);
    }

    public boolean wasCancellationAltered;

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        wasCancellationAltered = true;
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.getPlayerFrom(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item":
                return item;
            case "location":
                return location;
            case "click_type":
                return click_type;
            case "hand":
                return hand;
            case "relative":
                return event.hasBlock() ? new LocationTag(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation()) : null;
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        event.setCancelled(cancelled); // Workaround for Spigot=Dumb!
    }

    @EventHandler
    public void playerClicksBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        blockMaterial = event.hasBlock() ? new MaterialTag(event.getClickedBlock()) : new MaterialTag(Material.AIR);
        hand = new ElementTag(event.getHand());
        item = new ItemTag(event.getItem());
        location = event.hasBlock() ? new LocationTag(event.getClickedBlock().getLocation()) : null;
        click_type = new ElementTag(event.getAction());
        cancelled = event.isCancelled() && event.useItemInHand() == Event.Result.DENY; // Spigot is dumb!
        this.event = event;
        fire(); // Explicitly don't use `fire(event)` due to spigot bork
    }
}
