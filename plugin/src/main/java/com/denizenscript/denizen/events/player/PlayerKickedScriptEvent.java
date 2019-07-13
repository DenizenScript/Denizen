package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
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
    // <context.message> returns an Element of the kick message sent to all players.
    // <context.reason> returns an Element of the kick reason.
    // <context.flying> returns whether the player is being automatically kicked for flying.
    //
    // @Determine
    // "MESSAGE:" + Element to change the kick message.
    // "REASON:" + Element to change the kick reason.
    // "FLY_COOLDOWN:" + Duration to cancel the automatic fly kick and set its next cooldown.
    //
    // -->

    public PlayerKickedScriptEvent() {
        instance = this;
    }

    public static PlayerKickedScriptEvent instance;
    public dPlayer player;
    public Element message;
    public Element reason;
    public PlayerKickEvent event;

    public boolean isFlying() {
        return NMSHandler.getInstance().getPlayerHelper().getFlyKickCooldown(player.getPlayerEntity()) == 0;
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("message:")) {
            message = new Element(lower.substring("message:".length()));
            return true;
        }
        else if (lower.startsWith("reason:")) {
            reason = new Element(lower.substring("reason:".length()));
            return true;
        }
        else if (lower.startsWith("fly_cooldown:")) {
            Duration duration = Duration.valueOf(lower.substring("fly_cooldown:".length()));
            if (duration != null) {
                NMSHandler.getInstance().getPlayerHelper().setFlyKickCooldown(player.getPlayerEntity(), (int) duration.getTicks());
                cancelled = true;
                return true;
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("message")) {
            return message;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("flying")) {
            return new Element(isFlying());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerKicked(PlayerKickEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        message = new Element(event.getLeaveMessage());
        reason = new Element(event.getReason());
        this.event = event;
        fire(event);
        event.setLeaveMessage(message.asString());
        event.setReason(reason.asString());
    }
}
