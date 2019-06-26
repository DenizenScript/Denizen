package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.PacketHelper;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AnimateChestCommand extends AbstractCommand {

    // <--[command]
    // @Name AnimateChest
    // @Syntax animatechest [<location>] ({open}/close) (sound:{true}/false) (<player>|...)
    // @Required 1
    // @Short Makes a chest appear to open or close.
    // @Group world
    //
    // @Description
    // This command animates a chest in the world to open or close at a specified location.
    // The command by default will open the chest. It accepts a sound argument which specifies whether
    // the open or close sound will play aswell as the animation. The sound plays by default and
    // can be disabled with 'sound:false' It also accepts a player or list of players to animate the chest to,
    // allowing only selected players to see the chest animate as opened or closed.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to animate a chest to open at 15,89,-45 in world: world
    // - animatechest l@15,89,-45,world
    //
    // @Usage
    // To then close the chest at 15,89,-45 in world: world
    // - animatechest l@15,89,-45,world close
    //
    // @Usage
    // Use to animate a chest to open with no sound at 12,12,-64 in world: peter
    // - animatechest l@12,12,-64,peter sound:false
    //
    // @Usage
    // If only a player by the name of Morphan1 should see the chest open
    // - animatechest l@12,12,-64,peter sound:false p@Morphan1
    //
    // @Usage
    // The command also accepts a list of players to view the animation
    // - animatechest l@12,12,-64,peter sound:false p@Morphan1|p@mcmonkey4eva|p@Fortifier42
    // -->

    enum ChestAction {OPEN, CLOSE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(ChestAction.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("sound")
                    && arg.matchesPrefix("sound")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("sound", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        if (!scriptEntry.hasObject("action")) {
            scriptEntry.addObject("action", new Element("OPEN"));
        }

        if (!scriptEntry.hasObject("sound")) {
            scriptEntry.addObject("sound", new Element(true));
        }

        if (!scriptEntry.hasObject("players")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("players", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()));
            }
            else // TODO: Perhaps instead add all players in sight range?
            {
                throw new InvalidArgumentsException("Missing 'players' argument!");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element action = scriptEntry.getElement("action");
        Element sound = scriptEntry.getElement("sound");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), location.debug()
                    + action.debug()
                    + sound.debug()
                    + aH.debugObj("players", players.toString()));

        }

        PacketHelper packetHelper = NMSHandler.getInstance().getPacketHelper();

        switch (ChestAction.valueOf(action.asString().toUpperCase())) {
            case OPEN:
                for (dPlayer player : players) {
                    Player ent = player.getPlayerEntity();
                    if (sound.asBoolean()) {
                        NMSHandler.getInstance().getSoundHelper().playSound(ent, location,
                                Sound.BLOCK_CHEST_OPEN, 1, 1, "BLOCKS");
                    }
                    packetHelper.showBlockAction(ent, location, 1, 1);
                }
                break;

            case CLOSE:
                for (dPlayer player : players) {
                    Player ent = player.getPlayerEntity();
                    if (sound.asBoolean()) {
                        NMSHandler.getInstance().getSoundHelper().playSound(ent, location,
                                Sound.BLOCK_CHEST_CLOSE, 1, 1, "BLOCKS");
                    }
                    packetHelper.showBlockAction(ent, location, 1, 0);
                }
                break;
        }
    }
}
