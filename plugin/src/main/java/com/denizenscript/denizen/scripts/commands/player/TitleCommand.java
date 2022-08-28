package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;

import java.util.Collections;
import java.util.List;

public class TitleCommand extends AbstractCommand {

    public TitleCommand() {
        setName("title");
        setSyntax("title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...) (per_player)");
        setRequiredArguments(1, 7);
        setParseArgs(false);
        isProcedural = false;
    }

    // <--[command]
    // @Name Title
    // @Syntax title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...) (per_player)
    // @Required 1
    // @Maximum 7
    // @Short Displays a title to specified players.
    // @Group player
    //
    // @Description
    // Shows the players a large, noticeable wall of text in the center of the screen.
    // You can also show a "subtitle" below that title.
    // You may add timings for fading in, staying there, and fading out.
    // The defaults for these are: 1 second, 3 seconds, and 1 second, respectively.
    //
    // Optionally use 'per_player' with a list of player targets, to have the tags in the text input be reparsed for each and every player.
    // So, for example, "- title 'title:hello <player.name>' targets:<server.online_players>"
    // would normally say "hello bob" to every player (every player sees the exact same name in the text, ie bob sees "hello bob", steve also sees "hello bob", etc)
    // but if you use "per_player", each player online would see their own name (so bob sees "hello bob", steve sees "hello steve", etc).
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to alert players of impending server restart.
    // - title "title:<red>Server Restarting" "subtitle:<red>In 1 minute!" stay:1m targets:<server.online_players>
    //
    // @Usage
    // Use to inform the player about the area they have just entered.
    // - title "title:<green>Tatooine" "subtitle:<gold>What a desolate place this is."
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : ArgumentHelper.interpret(scriptEntry, scriptEntry.getOriginalArguments())) {
            if (arg.matchesPrefix("title")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (arg.matchesPrefix("subtitle")) {
                scriptEntry.addObject("subtitle", arg.asElement());
            }
            else if (arg.matchesPrefix("fade_in")) {
                String argStr = TagManager.tag(arg.getValue(), scriptEntry.getContext());
                scriptEntry.addObject("fade_in", DurationTag.valueOf(argStr, scriptEntry.context));
            }
            else if (arg.matchesPrefix("stay")) {
                String argStr = TagManager.tag(arg.getValue(), scriptEntry.getContext());
                scriptEntry.addObject("stay", DurationTag.valueOf(argStr, scriptEntry.context));
            }
            else if (arg.matchesPrefix("fade_out")) {
                String argStr = TagManager.tag(arg.getValue(), scriptEntry.getContext());
                scriptEntry.addObject("fade_out", DurationTag.valueOf(argStr, scriptEntry.context));
            }
            else if (arg.matchesPrefix("targets", "target")) {
                scriptEntry.addObject("targets", ListTag.getListFor(TagManager.tagObject(arg.getValue(), scriptEntry.getContext()), scriptEntry.getContext()).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("per_player")
                    && arg.matches("per_player")) {
                scriptEntry.addObject("per_player", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("title") && !scriptEntry.hasObject("subtitle")) {
            throw new InvalidArgumentsException("Must have a title or subtitle!");
        }
        scriptEntry.defaultObject("fade_in", new DurationTag(1)).defaultObject("stay", new DurationTag(3))
                .defaultObject("fade_out", new DurationTag(1))
                .defaultObject("targets", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)))
            .defaultObject("subtitle", new ElementTag("")).defaultObject("title", new ElementTag(""));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        String title = scriptEntry.getElement("title").asString();
        String subtitle = scriptEntry.getElement("subtitle").asString();
        DurationTag fade_in = scriptEntry.getObjectTag("fade_in");
        DurationTag stay = scriptEntry.getObjectTag("stay");
        DurationTag fade_out = scriptEntry.getObjectTag("fade_out");
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        ElementTag perPlayerObj = scriptEntry.getElement("per_player");
        boolean perPlayer = perPlayerObj != null && perPlayerObj.asBoolean();
        BukkitTagContext context = (BukkitTagContext) scriptEntry.getContext();
        if (!perPlayer) {
            title = TagManager.tag(title, context);
            subtitle = TagManager.tag(subtitle, context);
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("title", title), db("subtitle", subtitle), fade_in, stay, fade_out, db("targets", targets), perPlayerObj);
        }
        for (PlayerTag player : targets) {
            if (player != null) {
                if (!player.isOnline()) {
                    Debug.echoDebug(scriptEntry, "Player is offline, can't send title to them. Skipping.");
                    continue;
                }
                String personalTitle = title;
                String personalSubtitle = subtitle;
                if (perPlayer) {
                    context.player = player;
                    personalTitle = TagManager.tag(personalTitle, context);
                    personalSubtitle = TagManager.tag(personalSubtitle, context);
                }
                NMSHandler.packetHelper.showTitle(player.getPlayerEntity(), personalTitle, personalSubtitle, fade_in.getTicksAsInt(), stay.getTicksAsInt(), fade_out.getTicksAsInt());
            }
            else {
                Debug.echoError("Sent title to non-existent player!?");
            }
        }
    }
}
