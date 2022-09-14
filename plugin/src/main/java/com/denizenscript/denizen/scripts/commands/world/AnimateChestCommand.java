package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AnimateChestCommand extends AbstractCommand {

    public AnimateChestCommand() {
        setName("animatechest");
        setSyntax("animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name AnimateChest
    // @Syntax animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)
    // @Required 1
    // @Maximum 4
    // @Short Makes a chest appear to open or close.
    // @Group world
    //
    // @Description
    // This command animates a chest at a specified location in the world opening or closing.
    // By default, the chest will animate opening.
    // Optionally, specify whether to play a sound with the animation. By default this will play.
    // Optionally, specify a player or list of players that the animation should be visible to.
    // By default, only the linked player can see the animation.
    //
    // Note that this uses a generic 'block action' packet internally,
    // which means other block types may also react to this command.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to animate a chest opening, which only the linked player will see.
    // - animatechest <context.location>
    //
    // @Usage
    // Use to then animate a chest closing, which only the linked player will see.
    // - animatechest <context.location> close
    //
    // @Usage
    // Use to animate a chest opening with no sound, which only the linked player will see.
    // - animatechest <context.location> sound:false
    //
    // @Usage
    // Use to animate a chest opening that only a single specific player will see.
    // - animatechest <context.location> sound:false <[someplayer]>
    //
    // @Usage
    // Use to animate a chest opening that only a list of specific players will see.
    // - animatechest <context.location> sound:false <[someplayer]>|<[player]>|<[thatplayer]>
    // -->

    enum ChestAction {OPEN, CLOSE}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(ChestAction.class)) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("sound")
                    && arg.matchesPrefix("sound")
                    && arg.matchesBoolean()) {
                scriptEntry.addObject("sound", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
        if (!scriptEntry.hasObject("action")) {
            scriptEntry.addObject("action", new ElementTag("OPEN"));
        }
        if (!scriptEntry.hasObject("sound")) {
            scriptEntry.addObject("sound", new ElementTag(true));
        }
        if (!scriptEntry.hasObject("players")) {
            if (Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("players", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
            else // TODO: Perhaps instead add all players in sight range?
            {
                throw new InvalidArgumentsException("Missing 'players' argument!");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag action = scriptEntry.getElement("action");
        ElementTag sound = scriptEntry.getElement("sound");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location,  db("block type", location.getBlock().getType().name()), action, sound, db("players", players));
        }
        switch (ChestAction.valueOf(action.asString().toUpperCase())) {
            case OPEN:
                for (PlayerTag player : players) {
                    Player ent = player.getPlayerEntity();
                    if (sound.asBoolean()) {
                        player.getPlayerEntity().playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1, 1);
                    }
                    NMSHandler.packetHelper.showBlockAction(ent, location, 1, 1);
                }
                break;
            case CLOSE:
                for (PlayerTag player : players) {
                    Player ent = player.getPlayerEntity();
                    if (sound.asBoolean()) {
                        player.getPlayerEntity().playSound(location, Sound.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 1, 1);
                    }
                    NMSHandler.packetHelper.showBlockAction(ent, location, 1, 0);
                }
                break;
        }
    }
}
