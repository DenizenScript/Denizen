package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerClicksBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player clicks block
    // player (<click type>) clicks (<material>) (with <item>) (using hand/off_hand/either_hand) (in <area>)
    // player (<click type>) clicks block (with <item>) (using hand/off_hand/either_hand) (in <area>)
    //
    // @Cancellable true
    //
    // @Regex ^on player (((([^\s]+ )?clicks [^\s]+( with [^\s]+)?( in [^\s]+)?)))( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Triggers when a player clicks on a block or in the air.
    //
    // @Context
    // <context.item> returns the dItem the player is clicking with.
    // <context.location> returns the dLocation the player is clicking on.
    // <context.relative> returns a dLocation of the air block in front of the clicked block.
    // <context.click_type> returns an Element of the click type.
    // <context.hand> returns an Element of the used hand.
    //
    // -->

    public PlayerClicksBlockScriptEvent() {
        instance = this;
    }

    PlayerClicksBlockScriptEvent instance;
    PlayerInteractEvent event;
    dItem item;
    dLocation location;
    Element click_type;
    Element hand;
    dLocation relative;

    private boolean couldMatchIn(String lower) {
        int index = CoreUtilities.split(lower, ' ').indexOf("in");
        if (index == -1) {
            return true;
        }

        String in = CoreUtilities.getXthArg(index + 1, lower);
        if (dInventory.matches(in) || in.equalsIgnoreCase("inventory")) {
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
            dB.echoError("Invalid USING hand in " + getName() + " for '" + path.event + "' in " + path.container.getName());
            return false;
        }
        if (!using.equals("either_hand") && !using.equalsIgnoreCase(hand.identify())) {
            return false;
        }
        return true;
    }

    public boolean nonSwitchWithCheck(ScriptPath path, dItem held) {
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
        if (with != null) {
            if (with.equals("item")) {
                return true;
            }
            dItem it = dItem.valueOf(with, false);
            if (it == null) {
                dB.echoError("Invalid WITH item in " + getName() + " for '" + path.event + "' in " + path.container.getName());
                return false;
            }
            if (held == null || !tryItem(held, with)) {
                return false;
            }
        }
        return true;
    }

    private static final List<String> matchHelpList = Arrays.asList("at", "entity", "npc", "player", "vehicle", "projectile", "hanging");

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return (lower.startsWith("player clicks")
                || lower.startsWith("player left clicks")
                || (lower.startsWith("player right clicks")
                && !matchHelpList.contains(CoreUtilities.getXthArg(3, lower))
                && !dEntity.matches(CoreUtilities.getXthArg(3, lower))))
                && couldMatchIn(lower);  // Avoid matching "clicks in inventory"
    }

    private static final List<String> withHelpList = Arrays.asList("with", "using", "in");

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        int index = CoreUtilities.split(lower, ' ').indexOf("clicks") + 1;

        if (index == 3
                && !click_type.identify().startsWith(CoreUtilities.getXthArg(1, lower).toUpperCase())) {
            return false;
        }

        dMaterial material = event.hasBlock() ? new dMaterial(event.getClickedBlock()) : null;
        String mat = CoreUtilities.getXthArg(index, lower);
        if (mat.length() > 0
                && !withHelpList.contains(mat)
                && !tryMaterial(material, mat)) {
            return false;
        }

        if (!nonSwitchWithCheck(path, new dItem(event.getItem()))) {
            return false;
        }

        if (!runUsingCheck(path)) {
            return false;
        }

        if (location != null ? !runInCheck(path, location)
                : !runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerClicksBlock";
    }

    public boolean wasCancellationAltered;

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        wasCancellationAltered = true;
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.getPlayerFrom(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
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
            return relative;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void playerClicksBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        hand = new Element(NMSHandler.getVersion().isAtLeast(NMSVersion.v1_9_R2) ? event.getHand().name() : "HAND");
        item = new dItem(event.getItem());
        location = event.hasBlock() ? new dLocation(event.getClickedBlock().getLocation()) : null;
        relative = event.hasBlock() ? new dLocation(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation()) : null;
        click_type = new Element(event.getAction().name());
        cancelled = event.isCancelled() && event.useItemInHand() == Event.Result.DENY; // Spigot is dumb!
        this.event = event;
        wasCancellationAltered = false;
        fire();
        if (wasCancellationAltered) { // Workaround for Spigot=Dumb!
            event.setCancelled(cancelled);
        }
    }
}
