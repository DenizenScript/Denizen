package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
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
    // @Regex ^on player (((([^\s]+ )?clicks [^\s]+( with [^\s]+)?( in [^\s]+)?)))( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Triggers when a player clicks on a block or in the air.
    // @Context
    // <context.item> returns the dItem the player is clicking with.
    // <context.location> returns the dLocation the player is clicking on.
    // <context.relative> returns a dLocation of the air block in front of the clicked block.
    // <context.click_type> returns an Element of the click type.
    // <context.hand> returns an Element of the used hand.
    //
    // @Determine
    // "CANCELLED" to stop the click from happening.
    // "CANCELLED:FALSE" to uncancel the event. Some plugins may have this cancelled by default.
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
        if (index == -1) return true;

        String in = CoreUtilities.getXthArg(index + 1, lower);
        if (in.equals("notable")
                || dWorld.matches(in)
                || dCuboid.matches(in)
                || dEllipsoid.matches(in)) {
            return true;
        }
        return false;
    }

    private boolean runUsingCheck(ScriptContainer scriptContainer, String s, String lower) {
        int index = CoreUtilities.split(lower, ' ').indexOf("using");
        String using = index >= 0 ? CoreUtilities.getXthArg(index + 1, lower) : "hand";

        if (!using.equals("hand") && !using.equals("off_hand") && !using.equals("either_hand")) {
            dB.echoError("Invalid USING hand in " + getName() + " for '" + s + "' in " + scriptContainer.getName());
            return false;
        }
        if (!using.equals("either_hand") && !using.equalsIgnoreCase(hand.identify())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean runWithCheck(ScriptContainer scriptContainer, String s, String lower, dItem held) {
        int index = CoreUtilities.split(lower, ' ').indexOf("with");
        if (index == -1) return true;

        String with = CoreUtilities.getXthArg(index + 1, lower);
        if (with != null) {
            if (with.equals("item")) {
                return true;
            }
            dItem it = dItem.valueOf(with);
            if (it == null) {
                dB.echoError("Invalid WITH item in " + getName() + " for '" + s + "' in " + scriptContainer.getName());
                return false;
            }
            if (held == null || !tryItem(held, with)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        List<String> list = Arrays.asList("at", "entity", "npc", "player", "vehicle", "projectile", "hanging");
        return (lower.startsWith("player clicks")
                || lower.startsWith("player left clicks")
                || (lower.startsWith("player right clicks")
                    && !list.contains(CoreUtilities.getXthArg(3, lower))
                    && !dEntity.matches(CoreUtilities.getXthArg(3, lower))))
                && couldMatchIn(lower);  // Avoid matching "clicks in inventory"
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        int index = CoreUtilities.split(lower, ' ').indexOf("clicks") + 1;

        if (index == 3
                && !click_type.identify().startsWith(CoreUtilities.getXthArg(1, lower).toUpperCase())) {
            return false;
        }

        dMaterial material = event.hasBlock() ? dMaterial.getMaterialFrom(event.getClickedBlock().getType()) : null;
        String mat = CoreUtilities.getXthArg(index, lower);
        if (mat.length() > 0
                && !Arrays.asList("with", "using", "in").contains(mat)
                && !tryMaterial(material, mat)) {
            return false;
        }

        if (!runWithCheck(scriptContainer, s, lower, new dItem(event.getItem()))) {
            return false;
        }

        if (!runUsingCheck(scriptContainer, s, lower)) {
            return false;
        }

        if (location != null ? !runInCheck(scriptContainer, s, lower, location)
                : !runInCheck(scriptContainer, s, lower, event.getPlayer().getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerClicksBlock";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
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
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
