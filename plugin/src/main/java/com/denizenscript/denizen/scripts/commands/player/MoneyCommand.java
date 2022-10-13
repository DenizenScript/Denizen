package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.milkbowl.vault.economy.Economy;

import java.util.Collections;
import java.util.List;

public class MoneyCommand extends AbstractCommand {

    public MoneyCommand() {
        setName("money");
        setSyntax("money [give/take/set] (quantity:<#.#>) (players:<player>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Money
    // @Syntax money [give/take/set] (quantity:<#.#>) (players:<player>|...)
    // @Required 1
    // @Maximum 3
    // @Short Manage a player's money.
    // @Group player
    // @Plugin Vault
    //
    // @Description
    // Give money to, take money from, and set the balance of a player.
    // If no quantity is specified it defaults to '1'.
    // You can specify a list of players to give to or take from. If no player(s) are specified, defaults to the attached player.
    // NOTE: This requires an economy plugin or script, and Vault. May work for offline players depending on economy plugin.
    //
    // @Tags
    // <PlayerTag.money>
    //
    // @Usage
    // Use to give 1 money to the player.
    // - money give
    //
    // @Usage
    // Use to take 10 money from a player.
    // - money take quantity:10 players:<[player]>
    //
    // @Usage
    // Use to give all players on the server 100 money.
    // - money give quantity:100 players:<server.players>
    //
    // @Usage
    // Use to set the money of all online players to 250.
    // - money set quantity:250 players:<server.online_players>
    // -->

    enum Action {
        GIVE,
        TAKE,
        SET
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        if (Depends.economy == null) {
            Debug.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
            return;
        }
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.class)) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("quantity")
                    && arg.matchesPrefix("quantity", "qty", "q")
                    && arg.matchesFloat()) {
                if (arg.matchesPrefix("q", "qty")) {
                    BukkitImplDeprecations.qtyTags.warn(scriptEntry);
                }
                scriptEntry.addObject("quantity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("to", "from", "players", "player") &&
                    arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("quantity", new ElementTag(1));
        if (!scriptEntry.hasObject("players")) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("This command must have a player attached!");
            }
            else {
                scriptEntry.addObject("players",
                        Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
        }
        else if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a valid action!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        ElementTag quantity = scriptEntry.getElement("quantity");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Player(s)", players), action, quantity);
        }
        Economy eco = Depends.economy;
        double amt = quantity.asDouble();
        switch (Action.valueOf(action.asString().toUpperCase())) {
            case GIVE:
                for (PlayerTag player : players) {
                    eco.depositPlayer(player.getOfflinePlayer(), amt);
                }
                break;
            case TAKE:
                for (PlayerTag player : players) {
                    eco.withdrawPlayer(player.getOfflinePlayer(), amt);
                }
                break;
            case SET:
                for (PlayerTag player : players) {
                    double balance = eco.getBalance(player.getOfflinePlayer());
                    if (amt > balance) {
                        eco.depositPlayer(player.getOfflinePlayer(), amt - balance);
                    }
                    else {
                        eco.withdrawPlayer(player.getOfflinePlayer(), balance - amt);
                    }
                }
                break;
        }
    }
}
