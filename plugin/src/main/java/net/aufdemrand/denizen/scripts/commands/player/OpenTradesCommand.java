package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenTradesCommand extends AbstractCommand {

    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("trades")
                    && !scriptEntry.hasObject("entity")
                    && arg.matchesArgumentList(dTrade.class)) {
                scriptEntry.addObject("trades", arg.asType(dList.class).filter(dTrade.class));
            }
            else if (!scriptEntry.hasObject("trades")
                    && !scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(dEntity.class)) {
                scriptEntry.addObject("entity", arg.asType(dEntity.class));
            }
            else if (arg.matchesPrefix("title")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (arg.matchesPrefix("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("trades") && !scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a villager entity or a list of trades for the player(s) to trade with!");
        }

        scriptEntry.defaultObject("title", new Element(""))
                .defaultObject("players", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()));

    }

    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String title = scriptEntry.getElement("title").asString();
        dEntity entity = scriptEntry.getdObject("entity");
        List<dTrade> trades = (List<dTrade>) scriptEntry.getObject("trades");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    (entity != null ? aH.debugObj("entity", entity) : "")
                            + (trades != null ? aH.debugList("trades", trades) : "")
                            + (title.isEmpty() ? aH.debugObj("title", title) : "")
                            + aH.debugList("players", players));

        }

        if (entity != null) {
            if (players.size() > 1) {
                dB.echoError("No more than one player can access the same entity!");
                return;
            }
            if (entity.getBukkitEntity() instanceof Merchant) {
                dPlayer player = players.get(0);
                if (player.isValid() && player.isOnline()) {
                    player.getPlayerEntity().openMerchant((Merchant) entity.getBukkitEntity(), true);
                }
                else {
                    dB.echoError("Tried to make a nonexistent or offline player trade with a villager entity!");
                }
                return;
            }
            dB.echoError("The specified entity isn't a merchant!");
            return;
        }

        List<MerchantRecipe> recipes = new ArrayList<>();
        for (dTrade trade : trades) {
            recipes.add(trade.getRecipe());
        }

        for (dPlayer player : players) {
            if (player.isValid() && player.isOnline()) {
                Merchant merchant = Bukkit.createMerchant(title);
                merchant.setRecipes(recipes);
                player.getPlayerEntity().openMerchant(merchant, true);
            }
            else {
                dB.echoError("Tried to make a nonexistent or offline player view a virtual trading inventory!");
            }
        }
    }
}
