package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.dB;
import net.milkbowl.vault.economy.Economy;

import java.util.Arrays;
import java.util.List;

public class MoneyCommand extends AbstractCommand {

    // <--[command]
    // @Name Money
    // @Syntax money [give/take/set] (quantity:<#.#>) (players:<player>|...)
    // @Required 1
    // @Short Manage a player's money.
    // @Group player
    // @Plugin Vault
    //
    // @Description
    // Give money to, take money from, and set the balance of a player.
    // If no quantity is specified it defaults to '1'. You can specify a list of
    // players to give to or take from. If no player(s) are specified defaults to the attached player.
    // NOTE: This requires an economy plugin. May work for offline players depending on economy plugin.
    //
    // @Tags
    // <p@player.money>
    //
    // @Usage
    // Use to give 1 money to the player.
    // - money give
    //
    // @Usage
    // Use to take 10 money from a player.
    // - money take quantity:10 from:p@mcmonkey4eva
    //
    // @Usage
    // Use to give all players on the server 100 money.
    // - money give quantity:100 to:<server.list_players>
    //
    // @Usage
    // Use to set the money of all online players to 250.
    // - money set quantity:250 players:<server.list_online_players>
    // -->

    enum Action {
        GIVE,
        TAKE,
        SET
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        if (Depends.economy == null) {
            dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
            return;
        }

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (!scriptEntry.hasObject("action") && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("quantity") && arg.matchesPrefix("quantity", "qty", "q")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("quantity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players") && arg.matchesPrefix("to", "from", "players", "player") &&
                    arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("quantity", new Element(1));

        if (!scriptEntry.hasObject("players")) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("This command must have a player attached!");
            }
            else {
                scriptEntry.addObject("players",
                        Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));
            }
        }
        else if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a valid action!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        Element action = scriptEntry.getElement("action");
        Element quantity = scriptEntry.getElement("quantity");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugList("Player(s)", players) + action.debug() + quantity.debug());

        }
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
