package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class TitleCommand extends AbstractCommand {

    // <--[command]
    // @Name Title
    // @Syntax title (title:<text>) (subtitle:<text>) (fade_in:<duration>/{1s}) (stay:<duration>/{3s}) (fade_out:<duration>/{1s}) (targets:<player>|...)
    // @Required 1
    // @Short Displays a title to specified players.
    // @Group player
    //
    // @Description
    // Shows the players a large, noticeable wall of text in the center of the screen.
    // You can also show a "subtitle" below that title.
    // You may add timings for fading in, staying there, and fading out.
    // The defaults for these are: 1 second, 3 seconds, and 1 second, respectively.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to alert players of impending server restart.
    // - title "title:<red>Server Restarting" "subtitle:<red>In 1 minute!" stay:1m targets:<server.list_online_players>
    //
    // @Usage
    // Use to inform the player about the area they have just entered.
    // - title "title:<green>Tatooine" "subtitle:<gold>What a desolate place this is."
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matchesPrefix("title")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (arg.matchesPrefix("subtitle")) {
                scriptEntry.addObject("subtitle", arg.asElement());
            }
            else if (arg.matchesPrefix("fade_in")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("fade_in", arg.asType(DurationTag.class));
            }
            else if (arg.matchesPrefix("stay")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("stay", arg.asType(DurationTag.class));
            }
            else if (arg.matchesPrefix("fade_out")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("fade_out", arg.asType(DurationTag.class));
            }
            else if (arg.matchesPrefix("targets", "target")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(dPlayer.class, scriptEntry));
            }

        }

        if (!scriptEntry.hasObject("title") && !scriptEntry.hasObject("subtitle")) {
            throw new InvalidArgumentsException("Must have a title or subtitle!");
        }

        scriptEntry.defaultObject("fade_in", new DurationTag(1)).defaultObject("stay", new DurationTag(3))
                .defaultObject("fade_out", new DurationTag(1))
                .defaultObject("targets", Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag title = scriptEntry.getElement("title");
        ElementTag subtitle = scriptEntry.getElement("subtitle");
        DurationTag fade_in = scriptEntry.getdObject("fade_in");
        DurationTag stay = scriptEntry.getdObject("stay");
        DurationTag fade_out = scriptEntry.getdObject("fade_out");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(),
                    (title != null ? title.debug() : "") +
                            (subtitle != null ? subtitle.debug() : "") +
                            fade_in.debug() +
                            stay.debug() +
                            fade_out.debug() +
                            ArgumentHelper.debugObj("targets", targets));

        }

        for (dPlayer player : targets) {
            if (player.isValid() && player.isOnline()) {
                NMSHandler.getInstance().getPacketHelper().showTitle(player.getPlayerEntity(),
                        title != null ? title.asString() : "",
                        subtitle != null ? subtitle.asString() : "",
                        fade_in.getTicksAsInt(),
                        stay.getTicksAsInt(),
                        fade_out.getTicksAsInt());
            }
        }

    }

}
