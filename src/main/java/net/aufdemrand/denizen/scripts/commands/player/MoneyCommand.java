package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.milkbowl.vault.economy.Economy;

import java.util.Arrays;
import java.util.List;

public class MoneyCommand extends AbstractCommand {

    enum Action {
        GIVE,
        TAKE,
        SET
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("action") && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("quantity") && arg.matchesPrefix("quantity", "qty", "q")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("quantity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players") && arg.matchesPrefix("to", "from") &&
                    arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("quantity", new Element(1));

        if (!scriptEntry.hasObject("players")) {
            if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                throw new InvalidArgumentsException("This command must have a player attached!");
            }
            else {
                scriptEntry.addObject("players",
                        Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()));
            }
        }
        else if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a valid action!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element action = scriptEntry.getElement("action");
        Element quantity = scriptEntry.getElement("quantity");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");

        dB.report(scriptEntry, getName(), aH.debugList("Player(s)", players) + action.debug() + quantity.debug());
        Economy eco = Depends.economy;
        double amt = quantity.asDouble();
        switch (Action.valueOf(action.asString().toUpperCase())) {
            case GIVE:
                for (dPlayer player : players) {
                    eco.depositPlayer(player.getOfflinePlayer(), amt);
                }
                break;

            case TAKE:
                for (dPlayer player : players) {
                    eco.withdrawPlayer(player.getOfflinePlayer(), amt);
                }
                break;

            case SET:
                for (dPlayer player : players) {
                    double balance = eco.getBalance(player.getOfflinePlayer());
                    if (amt > balance) {
                        eco.depositPlayer(player.getOfflinePlayer(), amt - balance);
                    }
                    else {
                        eco.withdrawPlayer(player.getOfflinePlayer(), balance - amt);
                    }
                }
        }
    }
}
