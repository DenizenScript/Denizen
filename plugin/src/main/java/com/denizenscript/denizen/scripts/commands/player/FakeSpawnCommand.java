package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class FakeSpawnCommand extends AbstractCommand {

    public FakeSpawnCommand() {
        setName("fakespawn");
        setSyntax("fakespawn [<entity>] [<location>] (players:<player>|...) (d:<duration>{10s})");
        setRequiredArguments(2, 4);
    }

    // <--[command]
    // @Name FakeSpawn
    // @Syntax fakespawn [<entity>] [<location>] (players:<player>|...) (d:<duration>{10s})
    // @Required 2
    // @Maximum 4
    // @Short Makes the player see a fake entity spawn that didn't actually happen.
    // @Group player
    //
    // @Description
    // Makes the player see a fake entity spawn that didn't actually happen.
    // This means that the server will not track the entity,
    // and players not included in the command will not see the entity.
    //
    // You must specify a location and an entity to spawn.
    //
    // Optionally, specify a list of players to show the entity to.
    // If unspecified, will default to the linked player.
    //
    // Optionally, specify how long the fake entity should remain for.
    // If unspecified, will default to 10 seconds.
    // After the duration is up, the entity will be removed from the player(s).
    //
    // @Tags
    // <PlayerTag.fake_entities>
    //
    // @Usage
    // Use to show a fake creeper in front of the attached player.
    // - fakespawn creeper <player.forward[5]>
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("to", "players")) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("entity", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("players") && Utilities.entryHasPlayer(scriptEntry)) {
            scriptEntry.defaultObject("players", Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a valid location!");
        }

        if (!scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must have a valid, online player attached!");
        }

        if (!scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a valid entity!");
        }

        scriptEntry.defaultObject("duration", new DurationTag(10));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        EntityTag entity = scriptEntry.getObjectTag("entity");
        LocationTag location = scriptEntry.getObjectTag("location");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        DurationTag duration = scriptEntry.getObjectTag("duration");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entity.debug() + location.debug() + duration.debug()
                    + ArgumentHelper.debugList("players", players));
        }

        FakeEntity.showFakeEntityTo(players, entity, location, duration);
    }
}
