package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.UUID;

public class TablistCommand extends AbstractCommand {

    public TablistCommand() {
        setName("tablist");
        setSyntax("tablist [add/remove/update] (name:<name>) (display:<display>) (uuid:<uuid>) (skin_blob:<blob>) (latency:<#>) (gamemode:creative/survival/adventure/spectator) (listed:true/false)");
        setRequiredArguments(2, 8);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name TabList
    // @Syntax tablist [add/remove/update] (name:<name>) (display:<display>) (uuid:<uuid>) (skin_blob:<blob>) (latency:<#>) (gamemode:creative/survival/adventure/spectator) (listed:true/false)
    // @Required 2
    // @Maximum 8
    // @Short Modifies values in the player's tablist.
    // @Group player
    //
    // @Description
    // Adds, removes, or updates a player profile entry in the player's tab-list view.
    //
    // Using 'add' will add a new entry to the client's player list.
    // 'name' must be specified.
    // 'display' if unspecified will be the same as the name.
    // 'uuid' if unspecified will be randomly generated.
    // 'skin_blob' if unspecified will be a default Minecraft skin. Skin blob should be in format "texture;signature" (separated by semicolon).
    // 'latency' is a number representing the players ping, if unspecified will be 0. 0 renders as full ping, -1 renders an "X", 500 renders orange (3 bars), 1000 renders red (1 bar).
    // 'gamemode' if unspecified will be creative. 'spectator' renders as faded and is pushed below all non-spectator entries.
    // 'listed' determines whether the entry will show up in the tab list, defaults to 'true'.
    //
    // Using 'remove' will remove an entry from the tab list.
    // 'uuid' must be specified.
    //
    // Using 'update' will update an existing entry in the tab list.
    // 'uuid' must be specified.
    // Only 'display', 'latency', 'gamemode', and 'listed' can be updated.
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
    // Use to update an existing entry
    // - tablist update uuid:<[uuid]> gamemode:spectator latency:200
    //
    // -->


    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("gamemode:", GameMode.values());
    }

    public enum Mode { ADD, REMOVE, UPDATE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("mode") Mode mode,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("name") String name,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("display") String display,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("uuid") String uuid,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("skin_blob") String skinBlob,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("latency") ElementTag latency,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("gamemode") GameMode gamemode,
                                   @ArgDefaultNull @ArgPrefixed @ArgName("listed") ElementTag listed) {
        if (!Utilities.entryHasPlayer(scriptEntry)) {
            Debug.echoError("Must have a linked player!");
            return;
        }
        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
        if (listed != null && !listed.isBoolean()) {
            Debug.echoError("Invalid input '" + listed + "' to 'listed': must be a boolean");
            return;
        }
        UUID id;
        if (uuid != null) {
            try {
                id = UUID.fromString(uuid);
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
            int semicolon = skinBlob.indexOf(';');
            if (semicolon == -1) {
                Debug.echoError("Invalid skinblob '" + skinBlob + "'");
                return;
            }
            texture = skinBlob.substring(0, semicolon);
            signature = skinBlob.substring(semicolon + 1);
        }
        if (latency != null && !latency.isInt()) {
            Debug.echoError("Invalid latency, not a number '" + latency + "'");
            return;
        }
        int latencyNum = latency == null ? 0 : latency.asInt();
        switch (mode) {
            case ADD -> {
                if (name == null) {
                    throw new InvalidArgumentsRuntimeException("'name' wasn't specified but is required for 'add'");
                }
                if ((display == null || display.length() > 0) && !CoreConfiguration.allowRestrictedActions) {
                    Debug.echoError("Cannot use 'tablist add' to add a non-empty display-named entry: 'Allow restricted actions' is disabled in Denizen config.yml.");
                    return;
                }
                if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18)) {
                    NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, EnumSet.of(PlayerHelper.ProfileEditMode.ADD), name, display, id, texture, signature, latencyNum, gamemode == null ? GameMode.CREATIVE : gamemode, false);
                    return;
                }
                EnumSet<PlayerHelper.ProfileEditMode> editModes = EnumSet.of(PlayerHelper.ProfileEditMode.ADD);
                boolean listedBool = listed == null || listed.asBoolean();
                if (listedBool) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_LISTED);
                }
                if (display != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_DISPLAY);
                }
                if (latency != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_LATENCY);
                }
                if (gamemode != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_GAME_MODE);
                }
                NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, editModes, name, display, id, texture, signature, latencyNum, gamemode, listedBool);
            }
            case REMOVE -> {
                if (uuid == null) {
                    throw new InvalidArgumentsRuntimeException("'uuid' wasn't specified but is required for 'remove'");
                }
                NMSHandler.playerHelper.sendPlayerInfoRemovePacket(player, id);
            }
            case UPDATE -> {
                if (uuid == null) {
                    throw new InvalidArgumentsRuntimeException("'uuid' wasn't specified but is required for 'update'");
                }
                if ((display == null || display.length() > 0) && !CoreConfiguration.allowRestrictedActions) {
                    Debug.echoError("Cannot use 'tablist update' to create a non-empty named entry: 'Allow restricted actions' is disabled in Denizen config.yml.");
                    return;
                }
                if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_18)) {
                    if (display != null) {
                        NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, EnumSet.of(PlayerHelper.ProfileEditMode.UPDATE_DISPLAY), name, display, id, texture, signature, latencyNum, gamemode, false);
                    }
                    if (latency != null) {
                        NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, EnumSet.of(PlayerHelper.ProfileEditMode.UPDATE_LATENCY), name, display, id, texture, signature, latencyNum, gamemode, false);
                    }
                    return;
                }
                EnumSet<PlayerHelper.ProfileEditMode> editModes = EnumSet.noneOf(PlayerHelper.ProfileEditMode.class);
                if (display != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_DISPLAY);
                }
                if (latency != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_LATENCY);
                }
                if (gamemode != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_GAME_MODE);
                }
                if (listed != null) {
                    editModes.add(PlayerHelper.ProfileEditMode.UPDATE_LISTED);
                }
                NMSHandler.playerHelper.sendPlayerInfoAddPacket(player, editModes, name, display, id, texture, signature, latencyNum, gamemode, listed == null || listed.asBoolean());
            }
        }
    }
}
