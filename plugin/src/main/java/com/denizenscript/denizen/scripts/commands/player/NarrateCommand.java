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
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NarrateCommand extends AbstractCommand {

    public NarrateCommand() {
        setName("narrate");
        setSyntax("narrate [<text>] (targets:<player>|...) (format:<script>) (per_player) (from:<uuid>)");
        setRequiredArguments(1, 5);
        setParseArgs(false);
        isProcedural = true;
    }

    // <--[command]
    // @Name Narrate
    // @Syntax narrate [<text>] (targets:<player>|...) (format:<script>) (per_player) (from:<uuid>)
    // @Required 1
    // @Maximum 5
    // @Short Shows some text to the player.
    // @Group player
    //
    // @Description
    // Prints some text into the target's chat area. If no target is specified it will default to the attached player or the console.
    //
    // Accepts the 'format:<script>' argument, which will reformat the text according to the specified format script. See <@link language Format Script Containers>.
    //
    // Optionally use 'per_player' with a list of player targets, to have the tags in the text input be reparsed for each and every player.
    // So, for example, "- narrate 'hello <player.name>' targets:<server.online_players>"
    // would normally say "hello bob" to every player (every player sees the exact same name in the text, ie bob sees "hello bob", steve also sees "hello bob", etc)
    // but if you use "per_player", each player online would see their own name (so bob sees "hello bob", steve sees "hello steve", etc).
    //
    // Optionally, specify 'from:<uuid>' to indicate that message came from a specific UUID (used for things like the vanilla client social interaction block option).
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
    // - narrate "Hello there." targets:<[player]>|<[someplayer]>|<[thatplayer]>
    //
    // @Usage
    // Use to narrate text to a unique message to every player on the server.
    // - narrate "Hello <player.name>, your secret code is <util.random.duuid>." targets:<server.online_players> per_player
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
                    return;
                }
                scriptEntry.addObject("format", new ScriptTag(format));
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")) {
                scriptEntry.addObject("targets", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("from")
                    && arg.matchesPrefix("from")) {
                scriptEntry.addObject("from", TagManager.tagObject(arg.getValue(), scriptEntry.getContext()));
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
        if (!scriptEntry.hasObject("targets")) {
            scriptEntry.addObject("targets", (Utilities.entryHasPlayer(scriptEntry) ? Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)) : null));
        }
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Missing any text!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        if (scriptEntry.getResidingQueue().procedural) {
            Debug.echoError("'Narrate' should not be used in a procedure script. Consider the 'debug' command instead.");
        }
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        String text = scriptEntry.getElement("text").asString();
        ScriptTag formatObj = scriptEntry.getObjectTag("format");
        ElementTag perPlayerObj = scriptEntry.getElement("per_player");
        ElementTag from = scriptEntry.getElement("from");
        boolean perPlayer = perPlayerObj != null && perPlayerObj.asBoolean();
        BukkitTagContext context = (BukkitTagContext) scriptEntry.getContext();
        if (!perPlayer || targets == null) {
            text = TagManager.tag(text, context);
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Narrating", text), db("Targets", targets), formatObj, perPlayerObj, from);
        }
        UUID fromId = null;
        if (from != null) {
            if (from.asString().startsWith("p@")) {
                fromId = UUID.fromString(from.asString().substring("p@".length()));
            }
            else {
                fromId = UUID.fromString(from.asString());
            }
        }
        FormatScriptContainer format = formatObj == null ? null : (FormatScriptContainer) formatObj.getContainer();
        if (targets == null) {
            Bukkit.getServer().getConsoleSender().spigot().sendMessage(FormattedTextHelper.parse(format != null ? format.getFormattedText(text, scriptEntry) : text, ChatColor.WHITE));
            return;
        }
        for (PlayerTag player : targets) {
            if (player != null) {
                if (!player.isOnline()) {
                    Debug.echoDebug(scriptEntry, "Player is offline, can't narrate to them. Skipping.");
                    continue;
                }
                String personalText = text;
                if (perPlayer) {
                    context.player = player;
                    personalText = TagManager.tag(personalText, context);
                }
                BaseComponent[] component = FormattedTextHelper.parse(format != null ? format.getFormattedText(personalText, scriptEntry) : personalText, ChatColor.WHITE);
                if (fromId == null) {
                    player.getPlayerEntity().spigot().sendMessage(component);
                }
                else {
                    player.getPlayerEntity().spigot().sendMessage(ChatMessageType.CHAT, fromId, component);
                }
            }
            else {
                Debug.echoError("Narrated to non-existent player!?");
            }
        }
    }
}
