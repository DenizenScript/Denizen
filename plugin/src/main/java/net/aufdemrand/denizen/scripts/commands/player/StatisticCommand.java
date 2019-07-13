package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

public class StatisticCommand extends AbstractCommand {

    // <--[command]
    // @Name Statistic
    // @Syntax statistic [<statistic>] [add/take/set] (<#>) (qualifier:<material>/<entity>)
    // @Required 2
    // @Short Changes the specified statistic value for a player.
    // @Group player
    //
    // @Description
    // Changes the specified statistic for the player.
    // For more info on statistics, see https://minecraft.gamepedia.com/Statistics
    // For statistic names, see https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Statistic.html
    //
    //
    // @Tags
    // <p@player.statistic[<statistic>]>
    // <p@player.statistic[<statistic>].qualifier[<material>/<entity>]>
    //
    // @Usage
    // TODO: Document Command Details
    // -->

    private enum Action {ADD, TAKE, SET}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_players = false;

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (arg.matchesPrefix("players")
                    && !scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class));
                specified_players = true;
            }
            else if (!scriptEntry.hasObject("statistic")
                    && arg.matchesEnum(Statistic.values())) {
                scriptEntry.addObject("statistic", arg.asElement());
            }
            else if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (arg.matchesPrefix("qualifier", "q")
                    && !scriptEntry.hasObject("material")
                    && !scriptEntry.hasObject("entity")) {
                if (arg.matchesArgumentType(dMaterial.class)) {
                    scriptEntry.addObject("material", arg.asType(dMaterial.class));
                }
                else if (arg.matchesArgumentType(dEntity.class)) {
                    scriptEntry.addObject("entity", arg.asType(dEntity.class));
                }
            }

        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a valid action!");
        }

        if (!scriptEntry.hasObject("statistic")) {
            throw new InvalidArgumentsException("Must specify a valid Statistic!");
        }

        if (!scriptEntry.hasObject("amount")) {
            scriptEntry.addObject("amount", new Element(1));
        }

        Statistic.Type type = Statistic.valueOf(scriptEntry.getElement("statistic").asString().toUpperCase()).getType();
        if (type != Statistic.Type.UNTYPED) {
            if ((type == Statistic.Type.BLOCK || type == Statistic.Type.ITEM) && !scriptEntry.hasObject("material")) {
                throw new InvalidArgumentsException("Must specify a valid " + type.name() + " MATERIAL!");
            }
            else if (type == Statistic.Type.ENTITY && !scriptEntry.hasObject("entity")) {
                throw new InvalidArgumentsException("Must specify a valid ENTITY!");
            }
        }

        if (!scriptEntry.hasObject("players") && Utilities.entryHasPlayer(scriptEntry) && !specified_players) {
            scriptEntry.addObject("players", new dList(Utilities.getEntryPlayer(scriptEntry).identify()));
        }

        if (!scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must specify valid players!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element action = scriptEntry.getElement("action");
        dList players = scriptEntry.getdObject("players");
        Element statistic = scriptEntry.getElement("statistic");
        Element amount = scriptEntry.getElement("amount");
        dMaterial material = scriptEntry.getdObject("material");
        dEntity entity = scriptEntry.getdObject("entity");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), action.debug() + statistic.debug() + amount.debug() + players.debug()
                    + (material != null ? material.debug() : entity != null ? entity.debug() : ""));

        }

        Action act = Action.valueOf(action.asString().toUpperCase());
        Statistic stat = Statistic.valueOf(statistic.asString().toUpperCase());
        int amt = amount.asInt();
        switch (stat.getType()) {

            case BLOCK:
            case ITEM:
                Material mat = material.getMaterial();
                switch (act) {
                    case ADD:
                        for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                            player.incrementStatistic(stat, mat, amt);
                        }
                        break;
                    case TAKE:
                        for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                            player.decrementStatistic(stat, mat, amt);
                        }
                        break;
                    case SET:
                        for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                            player.setStatistic(stat, mat, amt);
                        }
                        break;
                }
                break;

            case ENTITY:
                EntityType ent = entity.getBukkitEntityType();
                switch (act) {
                    case ADD:
                        for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                            player.incrementStatistic(stat, ent, amt);
                        }
                        break;
                    case TAKE:
                        for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                            player.decrementStatistic(stat, ent, amt);
                        }
                        break;
                    case SET:
                        for (dPlayer player : players.filter(dPlayer.class, scriptEntry)) {
                            player.setStatistic(stat, ent, amt);
                        }
                        break;
                }
                break;

        }

    }
}
