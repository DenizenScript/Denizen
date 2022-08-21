package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerReceivesTablistUpdateScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player receives tablist update
    //
    // @Switch mode:add/update/remove to only trigger if the tablist update is the specified mode.
    //
    // @Group Player
    //
    // @Triggers when a player receives a tablist update.
    //
    // @Location true
    // @Cancellable true
    //
    // @Context
    // <context.mode> returns the update mode: 'add', 'remove', 'update_gamemode', 'update_latency', or 'update_display'.
    // <context.uuid> returns the packet's associated UUID.
    // <context.name> returns the packet's associated name (if any).
    // <context.display> returns the packet's associated display name (if any).
    // <context.latency> returns the packet's associated latency (if any).
    // <context.gamemode> returns the packet's associated gamemode (if any).
    // <context.skin_blob> returns the packet's associated skin blob (if any).
    //
    // @Determine
    // "LATENCY:" + ElementTag(Number) to change the latency.
    // "NAME:" + ElementTag to change the name.
    // "DISPLAY:" + ElementTag to change the display name. 'name', 'display' and 'cancelled' determinations require 'Allow restricted actions' in Denizen/config.yml
    // "GAMEMODE:" + ElementTag to change the gamemode.
    // "SKIN_BLOB:" + ElementTag to change the skin blob.
    //
    // @Player Always.
    //
    // -->

    public PlayerReceivesTablistUpdateScriptEvent() {
        instance = this;
        registerCouldMatcher("player receives tablist update");
        registerSwitches("mode");
    }

    public static PlayerReceivesTablistUpdateScriptEvent instance;

    public static class TabPacketData {

        public TabPacketData(String mode, UUID id, String name, String display, String gamemode, String texture, String signature, int latency) {
            this.mode = mode;
            this.id = id;
            this.name = name;
            this.display = display;
            this.gamemode = gamemode;
            this.texture = texture;
            this.signature = signature;
            this.latency = latency;
        }

        public UUID id;

        public String mode, name, display, gamemode, texture, signature;

        public int latency;

        public boolean cancelled = false;

        public boolean modified = false;
    }

    public Player player;

    public TabPacketData data;

    public static boolean enabled = false;

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    @Override
    public void cancellationChanged() {
        if (cancelled && !CoreConfiguration.allowRestrictedActions) {
            Debug.echoError("Cannot use 'receives tablist update' event to cancel a tablist packet: 'Allow restricted actions' is disabled in Denizen config.yml.");
            return;
        }
        data.cancelled = cancelled;
        data.modified = true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "mode", data.mode)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String determinationLow = CoreUtilities.toLowerCase(determination);
        if (determinationLow.contains(":")) {
            if (determinationLow.startsWith("latency:")) {
                data.modified = true;
                data.latency = Integer.parseInt(determination.substring("latency:".length()));
                return true;
            }
            else if (determinationLow.startsWith("name:")) {
                if (!CoreConfiguration.allowRestrictedActions) {
                    Debug.echoError("Cannot use 'receives tablist update' event to edit a display name: 'Allow restricted actions' is disabled in Denizen config.yml.");
                    return true;
                }
                data.modified = true;
                data.name = determination.substring("name:".length());
                return true;
            }
            else if (determinationLow.startsWith("display:")) {
                if (!CoreConfiguration.allowRestrictedActions) {
                    Debug.echoError("Cannot use 'receives tablist update' event to edit a display name: 'Allow restricted actions' is disabled in Denizen config.yml.");
                    return true;
                }
                data.modified = true;
                data.name = determination.substring("display:".length());
                return true;
            }
            else if (determinationLow.startsWith("gamemode:")) {
                data.modified = true;
                data.gamemode = determination.substring("gamemode:".length());
                return true;
            }
            else if (determinationLow.startsWith("skin_blob:")) {
                String blob = determination.substring("skin_blob:".length());
                int semicolon = blob.indexOf(';');
                if (semicolon == -1) {
                    Debug.echoError("Invalid skin blob!");
                    return true;
                }
                data.modified = true;
                data.texture = blob.substring(0, semicolon);
                data.signature = blob.substring(semicolon + 1);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "name": return new ElementTag(data.name);
            case "uuid": return new ElementTag(data.id.toString());
            case "mode": return new ElementTag(data.mode);
            case "display": return new ElementTag(data.display);
            case "gamemode": return new ElementTag(data.gamemode);
            case "skin_blob": return new ElementTag(data.texture + ";" + data.signature);
            case "latency": return new ElementTag(data.latency);
        }
        return super.getContext(name);
    }

    public static void fire(Player player, TabPacketData data) {
        instance.player = player;
        instance.data = data;
        instance.fire();
    }
}
