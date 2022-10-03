package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Collections;
import java.util.List;

public class ResourcePackCommand extends AbstractCommand {

    public ResourcePackCommand() {
        setName("resourcepack");
        setSyntax("resourcepack [url:<url>] [hash:<hash>] (forced) (prompt:<text>) (targets:<player>|...)");
        setRequiredArguments(2, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name ResourcePack
    // @Syntax resourcepack [url:<url>] [hash:<hash>] (forced) (prompt:<text>) (targets:<player>|...)
    // @Required 2
    // @Maximum 5
    // @Short Prompts a player to download a server resource pack.
    // @group player
    //
    // @Description
    // Sets the current resource pack by specifying a valid URL to a resource pack.
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
    // Use to send a resource pack with a pre-known hash.
    // - resourcepack url:https://example.com/pack.zip hash:0102030405060708090a0b0c0d0e0f1112131415
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("url")
                    && arg.matchesPrefix("url")) {
                scriptEntry.addObject("url", arg.asElement());
            }
            else if (!scriptEntry.hasObject("hash")
                    && arg.matchesPrefix("hash")) {
                scriptEntry.addObject("hash", arg.asElement());
            }
            else if (!scriptEntry.hasObject("forced")
                    && arg.matches("forced")) {
                scriptEntry.addObject("forced", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("prompt")
                    && arg.matchesPrefix("prompt")) {
                scriptEntry.addObject("prompt", arg.asElement());
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("targets")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("url")) {
            throw new InvalidArgumentsException("Must specify a URL!");
        }
        if (!scriptEntry.hasObject("hash")) {
            throw new InvalidArgumentsException("Must specify a hash!");
        }
        if (!scriptEntry.hasObject("targets")) {
            if (Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline()) {
                scriptEntry.addObject("targets", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
            else {
                throw new InvalidArgumentsException("Must specify an online player!");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<PlayerTag> targets = scriptEntry.getObjectTag("targets");
        ElementTag url = scriptEntry.getElement("url");
        ElementTag hash = scriptEntry.getElement("hash");
        ElementTag prompt = scriptEntry.getElement("prompt");
        ElementTag forced = scriptEntry.getElement("forced");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Targets", targets), url, hash, prompt, forced);
        }
        if (hash.asString().length() != 40) {
            Debug.echoError("Invalid resource_pack hash. Should be 40 characters of hexadecimal data.");
            return;
        }
        for (PlayerTag player : targets) {
            if (!player.isOnline()) {
                Debug.echoDebug(scriptEntry, "Player is offline, can't send resource pack to them. Skipping.");
                continue;
            }
            PaperAPITools.instance.sendResourcePack(player.getPlayerEntity(), url.asString(), hash.asString(), forced != null && forced.asBoolean(), prompt == null ? null : prompt.asString());
        }
    }
}
