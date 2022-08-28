package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TablistCommand extends AbstractCommand {

    public TablistCommand() {
        setName("tablist");
        setSyntax("tablist [add/remove/update] (name:<name>) (display:<display>) (uuid:<uuid>) (skin_blob:<blob>) (latency:<#>) (gamemode:creative/survival/adventure/spectator)");
        setRequiredArguments(2, 7);
        isProcedural = false;
        setPrefixesHandled("name", "display", "uuid", "skin_blob", "latency", "gamemode");
    }

    // <--[command]
    // @Name TabList
    // @Syntax tablist [add/remove/update] (name:<name>) (display:<display>) (uuid:<uuid>) (skin_blob:<blob>) (latency:<#>) (gamemode:creative/survival/adventure/spectator)
    // @Required 2
    // @Maximum 7
    // @Short Modifies values in the player's tablist.
    // @Group player
    //
    // @Description
    // Adds, removes, or updates a player profile entry in the player's tab-list view.
    //
    // Using 'add' will add a new entry to the tab list.
    // 'name' must be specified.
    // 'display' if unspecified will be the same as the name.
    // 'uuid' if unspecified will be randomly generated.
    // 'skin_blob' if unspecified will be default (steve). Skin blob should be in format "texture;signature" (separated by semicolon).
    // 'latency' is a number representing the players ping, if unspecified will be 0. 0 renders as full ping, -1 renders an "X", 500 renders orange (3 bars), 1000 renders red (1 bar).
    // 'gamemode' if unspecified will be creative. 'spectator' renders as faded and is pushed below all non-spectator entries.
    //
    // Using 'remove' will remove an entry from the tab list.
    // 'uuid' must be specified.
    //
    // Using 'update' will update an existing entry in the tab list.
    // 'uuid' must be specified.
    // Only values that can be updated are 'display' or 'latency'
    //
    // Usage of display names that are not empty requires enabling Denizen/config.yml option "Allow restricted actions".
    // Using this tool to add entries that look like real players (but aren't) is forbidden.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to add a new empty entry to the player's tab list to fill space.
    // - tablist add name:<empty> display:<empty> gamemode:spectator
    //
    // @Usage
    // Use to add a custom tab completion in the in-game chat, by adding an empty entry to the bottom of the tab list.
    // - tablist add name:my_tab_complete display:<empty> gamemode:spectator
    //
    // -->

    public enum Mode { ADD, REMOVE, UPDATE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("mode")
                    && arg.matchesEnum(Mode.class)) {
                scriptEntry.addObject("mode", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("mode")) {
            throw new InvalidArgumentsException("Missing add/remove/update argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        Mode mode = scriptEntry.getElement("mode").asEnum(Mode.class);
        ElementTag name = scriptEntry.argForPrefixAsElement("name", null);
        ElementTag display = scriptEntry.argForPrefixAsElement("display", null);
        ElementTag uuid = scriptEntry.argForPrefixAsElement("uuid", null);
        ElementTag skinBlob = scriptEntry.argForPrefixAsElement("skin_blob", null);
        ElementTag latency = scriptEntry.argForPrefixAsElement("latency", null);
        ElementTag gamemode = scriptEntry.argForPrefixAsElement("gamemode", "creative");
        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), mode, name, display, uuid, gamemode, latency, skinBlob);
        }
        UUID id = null;
        if (uuid != null) {
            try {
                id = UUID.fromString(uuid.asString());
            }
            catch (IllegalArgumentException ex) {
                Debug.echoError("Invalid UUID '" + uuid + "'");
                return;
            }
        }
        else {
            id = UUID.randomUUID();
        }
        String texture = null, signature = null;
        if (skinBlob != null) {
            int semicolon = skinBlob.asString().indexOf(';');
            if (semicolon == -1) {
                Debug.echoError("Invalid skinblob '" + skinBlob + "'");
                return;
            }
            texture = skinBlob.asString().substring(0, semicolon);
            signature = skinBlob.asString().substring(semicolon + 1);
        }
        if (latency != null && !latency.isInt()) {
            Debug.echoError("Invalid latency, not a number '" + latency + "'");
            return;
        }
        int latencyNum = latency == null ? 0 : latency.asInt();
        if (!gamemode.matchesEnum(GameMode.class)) {
            Debug.echoError("Invalid gamemode '" + gamemode + "'");
            return;
        }
        GameMode gameModeBukkit = gamemode.asEnum(GameMode.class);
        switch (mode) {
            case ADD:
                if (name == null) {
                    throw new InvalidArgumentsRuntimeException("'name' wasn't specified but is required for 'add'");
                }
                if ((display == null || display.asString().length() > 0) && !CoreConfiguration.allowRestrictedActions) {
                    Debug.echoError("Cannot use 'tablist add' to add a non-empty display-named entry: 'Allow restricted actions' is disabled in Denizen config.yml.");
                    return;
                }
                NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, PlayerHelper.ProfileEditMode.ADD, name.asString(), CoreUtilities.stringifyNullPass(display), id, texture, signature, latencyNum, gameModeBukkit);
                break;
            case REMOVE:
                if (uuid == null) {
                    throw new InvalidArgumentsRuntimeException("'uuid' wasn't specified but is required for 'remove'");
                }
                NMSHandler.playerHelper.sendPlayerRemovePacket(player, id);
                break;
            case UPDATE:
                if (uuid == null) {
                    throw new InvalidArgumentsRuntimeException("'uuid' wasn't specified but is required for 'update'");
                }
                if ((display == null || display.asString().length() > 0) && !CoreConfiguration.allowRestrictedActions) {
                    Debug.echoError("Cannot use 'tablist update' to create a non-empty named entry: 'Allow restricted actions' is disabled in Denizen config.yml.");
                    return;
                }
                if (display != null) {
                    NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, PlayerHelper.ProfileEditMode.UPDATE_DISPLAY, CoreUtilities.stringifyNullPass(name), CoreUtilities.stringifyNullPass(display), id, texture, signature, latencyNum, gameModeBukkit);
                }
                if (latency != null) {
                    NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, PlayerHelper.ProfileEditMode.UPDATE_LATENCY, CoreUtilities.stringifyNullPass(name), CoreUtilities.stringifyNullPass(display), id, texture, signature, latencyNum, gameModeBukkit);
                }
                break;
        }
    }
}
