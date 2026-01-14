package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.tags.ParseableTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;

import java.util.List;

public class TitleCommand extends AbstractCommand {

    public TitleCommand() {
        setName("title");
        setSyntax("title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...) (per_player)");
        setRequiredArguments(1, 7);
        addRemappedPrefixes("targets", "target");
        isProcedural = false;
        autoCompile();
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

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("title") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String strTitle,
                                   @ArgName("subtitle") @ArgPrefixed @ArgUnparsed @ArgDefaultNull String strSubTitle,
                                   @ArgName("fade_in") @ArgPrefixed @ArgDefaultText("1s") DurationTag fadeIn,
                                   @ArgName("stay") @ArgPrefixed @ArgDefaultText("3s") DurationTag stay,
                                   @ArgName("fade_out") @ArgPrefixed @ArgDefaultText("1s") DurationTag fadeOut,
                                   @ArgName("targets") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> players,
                                   @ArgName("per_player") boolean perPlayer) {
        if (strTitle == null && strSubTitle == null) {
            Debug.echoError("Must have a title or subtitle!");
            return;
        }
        if (players == null) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                Debug.echoError("Must specify target(s).");
                return;
            }
            players = List.of(Utilities.getEntryPlayer(scriptEntry));
        }
        BukkitTagContext context = perPlayer ? new BukkitTagContext((BukkitTagContext) scriptEntry.getContext()) : (BukkitTagContext) scriptEntry.getContext();
        ParseableTag parseableTitle = TagManager.parseTextToTag(strTitle, context);
        ParseableTag parseableSubTitle = TagManager.parseTextToTag(strSubTitle, context);
        String parsedTitle = perPlayer ? null : parse(parseableTitle, context);
        String parsedSubTitle = perPlayer ? null : parse(parseableSubTitle, context);
        for (PlayerTag player : players) {
            if (!player.isOnline()) {
                Debug.echoDebug(scriptEntry, "Player is offline, can't send title to them. Skipping.");
                continue;
            }
            if (perPlayer) {
                context.player = player;
                parsedTitle = parse(parseableTitle, context);
                parsedSubTitle = parse(parseableSubTitle, context);
            }
            NMSHandler.packetHelper.showTitle(player.getPlayerEntity(), parsedTitle, parsedSubTitle, fadeIn.getTicksAsInt(), stay.getTicksAsInt(), fadeOut.getTicksAsInt());
        }
    }

    public static String parse(ParseableTag tag, BukkitTagContext context) {
        return tag == null ? "" : tag.parse(context).toString();
    }
}
