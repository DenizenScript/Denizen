package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // player clicks block
    // player (right/left) clicks (<material>) (with <item>) (using hand/off_hand/either_hand)
    // player (right/left) clicks block (with <item>) (using hand/off_hand/either_hand)
    //
    // @Cancellable true
    //
    // @Regex ^on player (((([^\s]+ )?clicks [^\s]+( with [^\s]+)?( in [^\s]+)?)))$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
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
        instance = this;
    }

    PlayerClicksBlockScriptEvent instance;
    PlayerInteractEvent event;
    ItemTag item;
    LocationTag location;
    ElementTag click_type;
    ElementTag hand;
    MaterialTag blockMaterial;

    private boolean couldMatchIn(String lower) {
        int index = CoreUtilities.split(lower, ' ').indexOf("in");
        if (index == -1) {
            return true;
        }

        String in = CoreUtilities.getXthArg(index + 1, lower);
        if (InventoryTag.matches(in) || in.equalsIgnoreCase("inventory") || isRegexMatchable(in)) {
            return false;
        }
        if (in.equalsIgnoreCase("notable")) {
            String next = CoreUtilities.getXthArg(index + 2, lower);
            if (!next.equalsIgnoreCase("cuboid") && !next.equalsIgnoreCase("ellipsoid")) {
                return false;
            }
        }
        return true;
    }

    private boolean runUsingCheck(ScriptPath path) {
        int index;
        for (index = 0; index < path.eventArgsLower.length; index++) {
            if (path.eventArgsLower[index].equals("using")) {
                break;
            }
        }
        String using;
        if (index >= path.eventArgsLower.length) {
            using = "hand";
        }
        else {
            using = path.eventArgLowerAt(index + 1);
        }

        if (!using.equals("hand") && !using.equals("off_hand") && !using.equals("either_hand")) {
            Debug.echoError("Invalid USING hand in " + getName() + " for '" + path.event + "' in " + path.container.getName());
            return false;
        }
        if (!using.equals("either_hand") && !using.equalsIgnoreCase(hand.identify())) {
            return false;
        }
        return true;
    }

    public boolean nonSwitchWithCheck(ScriptPath path, ItemTag held) {
        int index;
        for (index = 0; index < path.eventArgsLower.length; index++) {
            if (path.eventArgsLower[index].equals("with")) {
                break;
            }
        }
        if (index >= path.eventArgsLower.length) {
            // No 'with ...' specified
            return true;
        }

        String with = path.eventArgLowerAt(index + 1);
        if (with != null && !tryItem(held, with)) {
            return false;
        }
        return true;
    }

    private static final HashSet<String> matchHelpList = new HashSet<>(Arrays.asList("at", "entity", "npc", "player", "vehicle", "projectile", "hanging"));

    @Override
    public boolean couldMatch(ScriptPath path) {
        return (path.eventLower.startsWith("player clicks")
                || path.eventLower.startsWith("player left clicks")
                || (path.eventLower.startsWith("player right clicks")
                && !matchHelpList.contains(path.eventArgLowerAt(3))
                && !EntityTag.matches(path.eventArgLowerAt(3))))
                && couldMatchIn(path.eventLower);  // Avoid matching "clicks in inventory"
    }

    private static final HashSet<String> withHelpList = new HashSet<>(Arrays.asList("with", "using", "in"));

    @Override
    public boolean matches(ScriptPath path) {
        int index = path.eventArgLowerAt(1).equals("clicks") ? 1 : 2;

        if (index == 2
                && !click_type.identify().startsWith(path.eventArgLowerAt(1).toUpperCase())) {
            return false;
        }

        String mat = path.eventArgLowerAt(index + 1);
        if (mat.length() > 0
                && !withHelpList.contains(mat)
                && !tryMaterial(blockMaterial, mat)) {
            return false;
        }

        if (!nonSwitchWithCheck(path, new ItemTag(event.getItem()))) {
            return false;
        }

        if (!runUsingCheck(path)) {
            return false;
        }

        if (location != null ? !runInCheck(path, location)
                : !runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerClicksBlock";
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
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("click_type")) {
            return click_type;
        }
        else if (name.equals("hand")) {
            return hand;
        }
        else if (name.equals("relative")) {
            return event.hasBlock() ? new LocationTag(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation()) : null;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerClicksBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        blockMaterial = event.hasBlock() ? new MaterialTag(event.getClickedBlock()) : new MaterialTag(Material.AIR);
        hand = new ElementTag(event.getHand().name());
        item = new ItemTag(event.getItem());
        location = event.hasBlock() ? new LocationTag(event.getClickedBlock().getLocation()) : null;
        click_type = new ElementTag(event.getAction().name());
        cancelled = event.isCancelled() && event.useItemInHand() == Event.Result.DENY; // Spigot is dumb!
        this.event = event;
        wasCancellationAltered = false;
        fire(); // Explicitly don't use `fire(event)` due to spigot bork
        if (wasCancellationAltered) { // Workaround for Spigot=Dumb!
            event.setCancelled(cancelled);
        }
    }
}
