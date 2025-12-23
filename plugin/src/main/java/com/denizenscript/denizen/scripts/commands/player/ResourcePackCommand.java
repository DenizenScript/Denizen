package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ResourcePackCommand extends AbstractCommand {

    public ResourcePackCommand() {
        setName("resourcepack");
        setSyntax("resourcepack ({set}/add/remove) (id:<id>) (url:<url>) (hash:<hash>) (forced) (prompt:<text>) (targets:<player>|...)");
        setRequiredArguments(1, 7);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name ResourcePack
    // @Syntax resourcepack ({set}/add/remove) (id:<id>) (url:<url>) (hash:<hash>) (forced) (prompt:<text>) (targets:<player>|...)
    // @Required 2
    // @Maximum 5
    // @Short Prompts a player to download a server resource pack.
    // @group player
    //
    // @Description
    // Sets the current resource pack by specifying a valid URL to a resource pack.
    //
    // Optionally, you can send the player additional resource packs by using the "add" argument.
    // The "id" argument allows you to overwrite a specific resource pack or remove one with "remove" argument.
    //
    // The player will be prompted to download the pack, with the optional prompt text or a default vanilla message.
    // Once a player says "yes" once, all future packs will be automatically downloaded. If the player selects "no" once, all future packs will automatically be rejected.
    // Players can change the automatic setting from their server list in the main menu.
    //
    // Use "hash:" to specify a 40-character (20 byte) hexadecimal SHA-1 hash value (without '0x') for the resource pack to prevent redownloading cached data.
    // Specifying a hash is required, though you can get away with copy/pasting a fake value if you don't care for the consequences.
    // There are a variety of tools to generate the real hash, such as the `sha1sum` command on Linux, or using the 7-Zip GUI's Checksum option on Windows.
    //
    // Specify "forced" to tell the vanilla client they must accept the pack or quit the server. Hacked clients may still bypass this requirement.
    //
    // "Forced" and "prompt" inputs only work on Paper servers.
    //
    // Optionally specify players to send the pack to. If unspecified, will use the linked player.
    //
    // See also <@link event resource pack status>.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to set a resource pack with a pre-known hash.
    // - resourcepack url:https://example.com/pack.zip hash:0102030405060708090a0b0c0d0e0f1112131415
    //
    // @Usage
    // Use to send multiple resource packs to a player.
    // - resourcepack add id:first_pack url:https://example.com/pack1.zip hash:0102030405060708090a0b0c0d0e0f1112131415
    // - resourcepack add id:second_pack url:https://example.com/pack2.zip hash:0102030405060708090a0b0c0d0e0f1112131415
    //
    // @Usage
    // Use to remove all resource packs from all online players.
    // - resourcepack remove targets:<server.online_players>
    // -->

    public enum Action { SET, ADD, REMOVE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgDefaultText("set") Action action,
                                   @ArgName("id") @ArgPrefixed @ArgDefaultNull String id,
                                   @ArgName("url") @ArgPrefixed @ArgDefaultNull String url,
                                   @ArgName("hash") @ArgPrefixed @ArgDefaultNull String hash,
                                   @ArgName("prompt") @ArgPrefixed @ArgDefaultNull String prompt,
                                   @ArgName("targets") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> targets,
                                   @ArgName("forced") boolean forced) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && (action == Action.ADD || id != null)) {
            throw new UnsupportedOperationException();
        }
        if (targets == null) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsRuntimeException("Must specify an online player!");
            }
            targets = Collections.singletonList(Utilities.getEntryPlayer(scriptEntry));
        }
        if ((action == Action.ADD || action == Action.SET) && (url == null || hash == null)) {
            throw new InvalidArgumentsRuntimeException("Must specify both a resource pack URL and hash!");
        }
        if ((action == Action.ADD || action == Action.SET) && hash.length() != 40) {
            Debug.echoError("Invalid resource_pack hash. Should be 40 characters of hexadecimal data.");
            return;
        }
        switch (action) {
            case SET -> {
                for (PlayerTag player : targets) {
                    if (checkOnline(player)) {
                        PaperAPITools.instance.setResourcePack(player.getPlayerEntity(), url, hash, forced, prompt, id);
                    }
                }
            }
            case ADD -> {
                byte[] hashData = new byte[20];
                for (int i = 0; i < 20; i++) {
                    hashData[i] = (byte) Integer.parseInt(hash.substring(i * 2, i * 2 + 2), 16);
                }
                UUID packUUID = id == null ? UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)) : parseUUID(id);
                for (PlayerTag player : targets) {
                    if (checkOnline(player)) {
                        player.getPlayerEntity().addResourcePack(packUUID, url, hashData, prompt, forced);
                    }
                }
            }
            case REMOVE -> {
                if (id == null) {
                    for (PlayerTag player : targets) {
                        if (checkOnline(player)) {
                            player.getPlayerEntity().removeResourcePacks();
                        }
                    }
                }
                else {
                    UUID packUUID = parseUUID(id);
                    for (PlayerTag player : targets) {
                        if (checkOnline(player)) {
                            player.getPlayerEntity().removeResourcePack(packUUID);
                        }
                    }
                }
            }
        }
    }

    public static boolean checkOnline(PlayerTag player) {
        if (!player.isOnline()) {
            Debug.echoError("Invalid player '" + player.getName() + "' specified: must be online");
            return false;
        }
        return true;
    }

    public static UUID parseUUID(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        }
        catch (IllegalArgumentException ex) {
            uuid = UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
        }
        return uuid;
    }
}
