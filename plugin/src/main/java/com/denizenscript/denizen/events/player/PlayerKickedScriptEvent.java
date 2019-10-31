package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player kicked (for flying)
    //
    // @Regex ^on player kicked( for flying)?$
    //
    // @Cancellable true
    //
    // @Triggers when a player is kicked from the server.
    //
    // @Context
    // <context.message> returns an ElementTag of the kick message sent to all players.
    // <context.reason> returns an ElementTag of the kick reason.
    // <context.flying> returns whether the player is being automatically kicked for flying.
    //
    // @Determine
    // "MESSAGE:" + ElementTag to change the kick message.
    // "REASON:" + ElementTag to change the kick reason.
    // "FLY_COOLDOWN:" + DurationTag to cancel the automatic fly kick and set its next cooldown.
    //
    // @Player Always.
    //
    // -->

    public PlayerKickedScriptEvent() {
        instance = this;
    }

    public static PlayerKickedScriptEvent instance;
    public PlayerTag player;
    public ElementTag message;
    public ElementTag reason;
    public PlayerKickEvent event;

    public boolean isFlying() {
        return NMSHandler.getPlayerHelper().getFlyKickCooldown(player.getPlayerEntity()) == 0;
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player kicked");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(3).equals("flying")) {
            return isFlying();
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerKicked";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("message:")) {
                message = new ElementTag(lower.substring("message:".length()));
                return true;
            }
            else if (lower.startsWith("reason:")) {
                reason = new ElementTag(lower.substring("reason:".length()));
                return true;
            }
            else if (lower.startsWith("fly_cooldown:")) {
                DurationTag duration = DurationTag.valueOf(lower.substring("fly_cooldown:".length()));
                if (duration != null) {
                    NMSHandler.getPlayerHelper().setFlyKickCooldown(player.getPlayerEntity(), (int) duration.getTicks());
                    cancelled = true;
                    return true;
                }
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("message")) {
            return message;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("flying")) {
            return new ElementTag(isFlying());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        player = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        message = new ElementTag(event.getLeaveMessage());
        reason = new ElementTag(event.getReason());
        this.event = event;
        fire(event);
        event.setLeaveMessage(message.asString());
        event.setReason(reason.asString());
    }
}
