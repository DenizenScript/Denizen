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
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class PlayerKickedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player kicked (for flying)
    //
    // @Group Player
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
    // "MESSAGE:<ElementTag>" to change the kick message.
    // "REASON:<ElementTag>" to change the kick reason.
    // "FLY_COOLDOWN:<DurationTag>" to cancel the automatic fly kick and set its next cooldown.
    //
    // @Player Always.
    //
    // -->

    public PlayerKickedScriptEvent() {
        registerCouldMatcher("player kicked (for flying)");
    }

    public PlayerTag player;
    public PlayerKickEvent event;

    public boolean isFlying() {
        return NMSHandler.playerHelper.getFlyKickCooldown(player.getPlayerEntity()) == 0;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(3).equals("flying")) {
            return isFlying();
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.startsWith("message:")) {
                event.setLeaveMessage(determination.substring("message:".length()));
                return true;
            }
            else if (lower.startsWith("reason:")) {
                event.setReason(determination.substring("reason:".length()));
                return true;
            }
            else if (lower.startsWith("fly_cooldown:")) {
                DurationTag duration = DurationTag.valueOf(determination.substring("fly_cooldown:".length()), getTagContext(path));
                if (duration != null) {
                    NMSHandler.playerHelper.setFlyKickCooldown(player.getPlayerEntity(), (int) duration.getTicks());
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
        switch (name) {
            case "message":
                return new ElementTag(event.getLeaveMessage());
            case "reason":
                return new ElementTag(event.getReason());
            case "flying":
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
        this.event = event;
        fire(event);
    }
}
