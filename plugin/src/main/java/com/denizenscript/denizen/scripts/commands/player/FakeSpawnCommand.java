package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Collections;
import java.util.List;

public class FakeSpawnCommand extends AbstractCommand {

    public FakeSpawnCommand() {
        setName("fakespawn");
        setSyntax("fakespawn [<entity>] [<location>/cancel] (players:<player>|...) (d:<duration>{10s})");
        setRequiredArguments(2, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name FakeSpawn
    // @Syntax fakespawn [<entity>] [<location>/cancel] (players:<player>|...) (d:<duration>{10s})
    // @Required 2
    // @Maximum 4
    // @Short Makes the player see a fake entity spawn that didn't actually happen.
    // @Group player
    //
    // @Description
    // Makes the player see a fake entity spawn that didn't actually happen.
    // This means that the server will not track the entity, and players not included in the command will not see the entity.
    //
    // You must specify an entity to spawn and a location to spawn it at, or to remove a fake entity, specify the fake entity object and 'cancel' instead of a location.
    //
    // Optionally, specify a list of players to show the entity to. If unspecified, will default to the linked player.
    //
    // Optionally, specify how long the fake entity should remain for. If unspecified, will default to 10 seconds.
    // After the duration is up, the entity will be removed from the player(s).
    //
    // @Tags
    // <PlayerTag.fake_entities>
    // <entry[saveName].faked_entity> returns the spawned faked entity.
    //
    // @Usage
    // Use to show a fake creeper in front of the attached player.
    // - fakespawn creeper <player.location.forward[5]>
    //
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteEntityTypes(tab);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
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
            else if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("players") && Utilities.entryHasPlayer(scriptEntry)) {
            scriptEntry.defaultObject("players", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
        }
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("cancel")) {
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
        ElementTag cancel = scriptEntry.getElement("cancel");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entity, cancel, location, duration, db("players", players));
        }
        if (cancel != null && cancel.asBoolean()) {
            if (entity.isFake) {
                FakeEntity fakeEnt = FakeEntity.idsToEntities.get(entity.getUUID());
                if (fakeEnt != null) {
                    fakeEnt.cancelEntity();
                }
                else {
                    Debug.echoDebug(scriptEntry, "Entity '" + entity + "' cannot be cancelled: not listed in fake-entity map.");
                }
            }
        }
        else {
            FakeEntity created = FakeEntity.showFakeEntityTo(players, entity, location, duration);
            scriptEntry.addObject("faked_entity", created.entity);
        }
    }
}
