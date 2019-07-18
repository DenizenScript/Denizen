package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
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
    // <PlayerTag.statistic[<statistic>]>
    // <PlayerTag.statistic[<statistic>].qualifier[<material>/<entity>]>
    //
    // @Usage
    // TODO: Document Command Details
    // -->

    private enum Action {ADD, TAKE, SET}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_players = false;

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (arg.matchesPrefix("players")
                    && !scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class));
                specified_players = true;
            }
            else if (!scriptEntry.hasObject("statistic")
                    && arg.matchesEnum(Statistic.values())) {
                scriptEntry.addObject("statistic", arg.asElement());
            }
            else if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (arg.matchesPrefix("qualifier", "q")
                    && !scriptEntry.hasObject("material")
                    && !scriptEntry.hasObject("entity")) {
                if (arg.matchesArgumentType(MaterialTag.class)) {
                    scriptEntry.addObject("material", arg.asType(MaterialTag.class));
                }
                else if (arg.matchesArgumentType(EntityTag.class)) {
                    scriptEntry.addObject("entity", arg.asType(EntityTag.class));
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
            scriptEntry.addObject("amount", new ElementTag(1));
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
            scriptEntry.addObject("players", new ListTag(Utilities.getEntryPlayer(scriptEntry).identify()));
        }

        if (!scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must specify valid players!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag action = scriptEntry.getElement("action");
        ListTag players = scriptEntry.getObjectTag("players");
        ElementTag statistic = scriptEntry.getElement("statistic");
        ElementTag amount = scriptEntry.getElement("amount");
        MaterialTag material = scriptEntry.getObjectTag("material");
        EntityTag entity = scriptEntry.getObjectTag("entity");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), action.debug() + statistic.debug() + amount.debug() + players.debug()
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
                        for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                            player.incrementStatistic(stat, mat, amt);
                        }
                        break;
                    case TAKE:
                        for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                            player.decrementStatistic(stat, mat, amt);
                        }
                        break;
                    case SET:
                        for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                            player.setStatistic(stat, mat, amt);
                        }
                        break;
                }
                break;

            case ENTITY:
                EntityType ent = entity.getBukkitEntityType();
                switch (act) {
                    case ADD:
                        for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                            player.incrementStatistic(stat, ent, amt);
                        }
                        break;
                    case TAKE:
                        for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                            player.decrementStatistic(stat, ent, amt);
                        }
                        break;
                    case SET:
                        for (PlayerTag player : players.filter(PlayerTag.class, scriptEntry)) {
                            player.setStatistic(stat, ent, amt);
                        }
                        break;
                }
                break;

        }

    }
}
