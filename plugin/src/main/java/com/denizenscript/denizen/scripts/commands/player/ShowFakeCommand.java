package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class ShowFakeCommand extends AbstractCommand {

    // <--[command]
    // @Name ShowFake
    // @Syntax showfake [<material>|.../cancel] [<location>|...] (players:<player>|...) (d:<duration>{10s})
    // @Required 2
    // @Short Makes the player see a block change that didn't actually happen.
    // @Group player
    //
    // @Description
    // Shows a fake block for a player which is not affected on server-side.
    // You can show a block for a player without anyone else can see it.
    //
    // If a player stands on a showfake block which is originally and air block,
    // then the server will treat this as the player is flying/falling.
    //
    // If a player tries to interact with the block (usually by right-clicking or left-click),
    // the server will then update the block for the player with the original block and the
    // effect of showfake is lost.
    //
    // If no duration is specefied, then it assumes the default duration of 10 seconds.
    //
    // @Tags
    // <LocationTag.block.material>
    //
    // @Usage
    // Use to place a fake gold block at where the player is looking
    // - showfake GOLD_BLOCK <player.location.cursor_on> players:<player> duration:1m
    //
    // @Usage
    // Use to place a stone block right on player's face
    // - showfake STONE <player.location.add[0,1,0]> players:<player> duration:5s
    //
    // @Usage
    // Use to place fake lava (shows visual fire if standing in it)
    // - showfake LAVA <player.location> players:<server.list_online_players> duration:5s
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        ListTag locations = new ListTag();
        ListTag entities = new ListTag();
        boolean added_entities = false;

        // Iterate through arguments
        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matchesPrefix("to", "players")) {
                for (String entity : ListTag.valueOf(arg.getValue())) {
                    if (PlayerTag.matches(entity)) {
                        entities.add(entity);
                    }
                }
                added_entities = true; // TODO: handle lists properly
            }
            else if (arg.matchesArgumentList(MaterialTag.class)) {
                scriptEntry.addObject("materials", arg.asType(ListTag.class));
            }
            else if (locations.isEmpty()
                    && arg.matchesArgumentType(ListTag.class)) {
                for (String item : ListTag.valueOf(arg.getValue())) {
                    if (LocationTag.matches(item)) {
                        locations.add(item);
                    }
                }
            }
            else if (locations.isEmpty()
                    && arg.matchesArgumentType(LocationTag.class)) {
                locations.add(arg.getValue());
            }
            else if (arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (entities.isEmpty() && Utilities.entryHasPlayer(scriptEntry)) {
            entities.add(Utilities.getEntryPlayer(scriptEntry).identify());
        }

        if (locations.isEmpty()) {
            throw new InvalidArgumentsException("Must specify at least one valid location!");
        }

        if (!added_entities && (!Utilities.entryHasPlayer(scriptEntry)
                || !Utilities.getEntryPlayer(scriptEntry).isOnline())) {
            throw new InvalidArgumentsException("Must have a valid, online player attached!");
        }

        if (entities.isEmpty() && added_entities) {
            throw new InvalidArgumentsException("Must specify valid targets!");
        }

        if (!scriptEntry.hasObject("materials") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify valid material(s)!");
        }

        scriptEntry.addObject("entities", entities);
        scriptEntry.addObject("locations", locations);

        scriptEntry.defaultObject("duration", new DurationTag(10)).defaultObject("cancel", new ElementTag(false));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        DurationTag duration = scriptEntry.getdObject("duration");
        ListTag material_list = scriptEntry.getdObject("materials");
        ListTag list = scriptEntry.getdObject("locations");
        ListTag players = scriptEntry.getdObject("entities");
        ElementTag cancel = scriptEntry.getElement("cancel");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), (material_list != null ? material_list.debug() : "")
                    + list.debug() + players.debug() + duration.debug() + cancel.debug());

        }


        boolean shouldCancel = cancel.asBoolean();

        List<MaterialTag> mats = null;
        if (!shouldCancel) {
            mats = material_list.filter(MaterialTag.class, scriptEntry);
        }

        int i = 0;
        for (LocationTag loc : list.filter(LocationTag.class, scriptEntry)) {
            if (!shouldCancel) {
                FakeBlock.showFakeBlockTo(players.filter(PlayerTag.class, scriptEntry), loc, mats.get(i % mats.size()), duration);
            }
            else {
                FakeBlock.stopShowingTo(players.filter(PlayerTag.class, scriptEntry), loc);
            }
            i++;
        }
    }
}
