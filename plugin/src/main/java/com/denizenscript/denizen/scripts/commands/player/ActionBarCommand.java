package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;

import java.util.Collections;
import java.util.List;

public class ActionBarCommand extends AbstractCommand {

    public ActionBarCommand() {
        setName("actionbar");
        setSyntax("actionbar [<text>] (targets:<player>|...) (format:<script>) (per_player)");
        setRequiredArguments(1, 4);
        setParseArgs(false);
        isProcedural = false;
    }

    // <--[command]
    // @Name ActionBar
    // @Syntax actionbar [<text>] (targets:<player>|...) (format:<script>) (per_player)
    // @Required 1
    // @Maximum 4
    // @Short Sends a message to a player's action bar.
    // @group player
    //
    // @Description
    // Sends a message to the target's action bar area.
    // If no target is specified it will default to the attached player.
    // Accepts the 'format:<name>' argument, which will reformat the text according to the specified format script. See <@link language Format Script Containers>.
    //
    // Optionally use 'per_player' with a list of player targets, to have the tags in the text input be reparsed for each and every player.
    // So, for example, "- actionbar 'hello <player.name>' targets:<server.online_players>"
    // would normally show "hello bob" to every player (every player sees the exact same name in the text, ie bob sees "hello bob", steve also sees "hello bob", etc)
    // but if you use "per_player", each player online would see their own name (so bob sees "hello bob", steve sees "hello steve", etc).
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to send a message to the player's action bar.
    // - actionbar "Hey there <player.name>!"
    //
    // @Usage
    // Use to send a message to a list of players.
    // - actionbar "Hey, welcome to the server!" targets:<[thatplayer]>|<[player]>|<[someplayer]>
    //
    // @Usage
    // Use to send a message to a list of players, with a formatted message.
    // - actionbar "Hey there!" targets:<[thatplayer]>|<[player]> format:ServerChat
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : ArgumentHelper.interpret(scriptEntry, scriptEntry.getOriginalArguments())) {
            if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format", "f")) {
                String formatStr = TagManager.tag(arg.getValue(), scriptEntry.getContext());
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    Debug.echoError("Could not find format script matching '" + formatStr + "'");
                }
                scriptEntry.addObject("format", new ScriptTag(format));
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("targets", "target")) {
                scriptEntry.addObject("targets", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("per_player")
                    && arg.matches("per_player")) {
                scriptEntry.addObject("per_player", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", arg.getRawElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }
        if (!scriptEntry.hasObject("targets") && !Utilities.entryHasPlayer(scriptEntry)) {
            throw new InvalidArgumentsException("Must specify target(s).");
        }
        if (!scriptEntry.hasObject("targets")) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("Must specify valid player Targets!");
            }
            else {
                scriptEntry.addObject("targets", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        String text = scriptEntry.getElement("text").asString();
        ScriptTag formatObj = scriptEntry.getObjectTag("format");
        ElementTag perPlayerObj = scriptEntry.getElement("per_player");
        boolean perPlayer = perPlayerObj != null && perPlayerObj.asBoolean();
        BukkitTagContext context = (BukkitTagContext) scriptEntry.getContext();
        if (!perPlayer) {
            text = TagManager.tag(text, context);
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("message", text), db("targets", targets), formatObj, perPlayerObj);
        }
        FormatScriptContainer format = formatObj == null ? null : (FormatScriptContainer) formatObj.getContainer();
        for (PlayerTag player : targets) {
            if (player != null) {
                if (!player.isOnline()) {
                    Debug.echoDebug(scriptEntry, "Player is offline, can't send actionbar to them. Skipping.");
                    continue;
                }
                String personalText = text;
                if (perPlayer) {
                    context.player = player;
                    personalText = TagManager.tag(personalText, context);
                }
                player.getPlayerEntity().spigot().sendMessage(ChatMessageType.ACTION_BAR, FormattedTextHelper.parse(format != null ? format.getFormattedText(personalText, scriptEntry) : personalText, ChatColor.WHITE));
            }
            else {
                Debug.echoError("Sent actionbar to non-existent player!?");
            }
        }
    }
}
