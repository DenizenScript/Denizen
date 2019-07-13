package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.objects.dTrade;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenTradesCommand extends AbstractCommand {

    // <--[command]
    // @Name OpenTrades
    // @Syntax opentrades [<entity>/<trade>|...] (title:<title>) (players:<player>|...)
    // @Required 1
    // @Short Opens the specified villager entity's trading inventory or a list of trades.
    // @Group player
    //
    // @Description
    // Forces a player to open a villager's trading inventory or a virtual trading inventory.
    // If an entity is specified, only one player can be specified.
    // Otherwise, if a list of trades is specified, more than one player can be specified.
    // If the title is not specified, no title will be applied to the virtual trading inventory.
    // If no player is specified, by default the attached player will be forced to trade.
    //
    // @Tags
    // <p@player.selected_trade_index>
    // <e@entity.is_trading>
    // <e@entity.trades>
    // <e@entity.trading_with>
    //
    // @Usage
    // Use to open an unusable trade.
    // - opentrades trade@trade
    //
    // @Usage
    // Use to open a list of trades with an optional title.
    // - opentrades trade@trade[result=i@stone;inputs=li@i@stone;max_uses=9999]|trade@trade[result=i@barrier] "title:Useless Trades"
    //
    // @Usage
    // Use to force a player to trade with a villager.
    // - opentrades <def[villager_entity]>
    // -->

    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("trades")
                    && !scriptEntry.hasObject("entity")
                    && arg.matchesArgumentList(dTrade.class)) {
                scriptEntry.addObject("trades", arg.asType(dList.class).filter(dTrade.class, scriptEntry));
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
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("trades") && !scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a villager entity or a list of trades for the player(s) to trade with!");
        }

        scriptEntry.defaultObject("title", new Element(""))
                .defaultObject("players", Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));

    }

    public void execute(ScriptEntry scriptEntry) {

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
