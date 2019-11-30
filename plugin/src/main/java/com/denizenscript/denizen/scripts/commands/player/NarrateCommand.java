package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
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
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

public class NarrateCommand extends AbstractCommand {

    // <--[command]
    // @Name Narrate
    // @Syntax narrate [<text>] (targets:<player>|...) (format:<script>) (per_player)
    // @Required 1
    // @Short Shows some text to the player.
    // @Group player
    //
    // @Description
    // Prints some text into the target's chat area. If no target is specified it will default to the attached player or the console.
    //
    // Accepts the 'format:<script>' argument, which will reformat the text according to the specified format script.
    //
    // Optionally use 'per_player' with a list of player targets, to have the tags in the text input be reparsed for each and every player.
    // So, for example, "- narrate 'hello <player.name>' targets:<server.list_online_players>"
    // would normally say "hello bob" to every player (every player sees the exact same name in the text, ie bob sees "hello bob", steve also sees "hello bob", etc)
    // but if you use "per_player", each player online would see their own name (so bob sees "hello bob", steve sees "hello steve", etc).
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to narrate text to the player.
    // - narrate "Hello World!"
    //
    // @Usage
    // Use to narrate text to a list of players.
    // - narrate "Hello there." targets:p@bob|p@steve|p@john
    //
    // @Usage
    // Use to narrate text to a unique message to every player on the server.
    // - narrate "Hello <player.name>, your secret code is <util.random.duuid>." targets:<server.list_online_players> per_player
    // -->

    @Override
    public void onEnable() {
        setParseArgs(false);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        if (scriptEntry.getArguments().size() > 4) { // TODO: Use this more often!
            throw new InvalidArgumentsException("Too many arguments! Did you forget a 'quote'?");
        }
        for (Argument arg : ArgumentHelper.interpret(scriptEntry.getOriginalArguments())) {
            if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format", "f")) {
                String formatStr = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    Debug.echoError("Could not find format script matching '" + formatStr + "'");
                }
                scriptEntry.addObject("format", new ScriptTag(format));
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")) {
                scriptEntry.addObject("targets", ListTag.getListFor(TagManager.tagObject(arg.getValue(), new BukkitTagContext(scriptEntry, false))).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("per_player")
                    && arg.matches("per_player")) {
                scriptEntry.addObject("per_player", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new ElementTag(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("targets")) {
            scriptEntry.addObject("targets",
                    (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry)) : null));
        }
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Missing any text!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        String text = scriptEntry.getElement("text").asString();
        ScriptTag formatObj = scriptEntry.getObjectTag("format");
        ElementTag perPlayerObj = scriptEntry.getElement("per_player");

        boolean perPlayer = perPlayerObj != null && perPlayerObj.asBoolean();
        BukkitTagContext context = new BukkitTagContext(scriptEntry, false);
        if (!perPlayer || targets == null) {
            text = TagManager.tag(text, context);
        }

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("Narrating", text)
                            + ArgumentHelper.debugList("Targets", targets)
                            + (formatObj != null ? formatObj.debug() : "")
                            + (perPlayerObj != null ? perPlayerObj.debug() : ""));
        }

        FormatScriptContainer format = formatObj == null ? null : (FormatScriptContainer) formatObj.getContainer();
        if (targets == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(format != null ? format.getFormattedText(text, scriptEntry) : text);
            return;
        }

        for (PlayerTag player : targets) {
            if (player != null && player.isOnline()) {
                String personalText = text;
                if (perPlayer) {
                    personalText = TagManager.tag(personalText, context);
                }
                player.getPlayerEntity().spigot().sendMessage(FormattedTextHelper.parse(format != null ? format.getFormattedText(personalText, scriptEntry) : personalText));
            }
            else {
                Debug.echoError("Narrated to non-existent or offline player!");
            }
        }
    }
}
