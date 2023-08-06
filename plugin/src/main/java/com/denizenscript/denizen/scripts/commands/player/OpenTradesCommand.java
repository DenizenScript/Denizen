package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenTradesCommand extends AbstractCommand {

    public OpenTradesCommand() {
        setName("opentrades");
        setSyntax("opentrades [<entity>/<trade>|...] (title:<title>) (players:<player>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name OpenTrades
    // @Syntax opentrades [<entity>/<trade>|...] (title:<title>) (players:<player>|...)
    // @Required 1
    // @Maximum 3
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
    // <PlayerTag.selected_trade_index>
    // <EntityTag.is_trading>
    // <EntityTag.trades>
    // <EntityTag.trading_with>
    //
    // @Usage
    // Use to open an unusable trade.
    // - opentrades trade
    //
    // @Usage
    // Use to open a list of trades with an optional title.
    // - opentrades trade[result=stone;inputs=stone;max_uses=9999]|trade[result=barrier] "title:Useless Trades"
    //
    // @Usage
    // Use to force a player to trade with a villager.
    // - opentrades <[villager_entity]>
    // -->

    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("trades")
                    && !scriptEntry.hasObject("entity")
                    && arg.matchesArgumentList(TradeTag.class)) {
                scriptEntry.addObject("trades", arg.asType(ListTag.class).filter(TradeTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("trades")
                    && !scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("entity", arg.asType(EntityTag.class));
            }
            else if (arg.matchesPrefix("title")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (arg.matchesPrefix("players")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("trades") && !scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a villager entity or a list of trades for the player(s) to trade with!");
        }
        scriptEntry.defaultObject("title", new ElementTag(""))
                .defaultObject("players", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
    }

    public void execute(ScriptEntry scriptEntry) {
        ElementTag title = scriptEntry.getElement("title");
        EntityTag entity = scriptEntry.getObjectTag("entity");
        List<TradeTag> trades = (List<TradeTag>) scriptEntry.getObject("trades");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entity, db("trades", trades), title, db("players", players));
        }
        if (entity != null) {
            if (players.size() > 1) {
                Debug.echoError("No more than one player can access the same entity!");
                return;
            }
            if (entity.getBukkitEntity() instanceof Merchant) {
                PlayerTag player = players.get(0);
                if (player.isValid() && player.isOnline()) {
                    player.getPlayerEntity().openMerchant((Merchant) entity.getBukkitEntity(), true);
                }
                else {
                    Debug.echoError("Tried to make a nonexistent or offline player trade with a villager entity!");
                }
                return;
            }
            Debug.echoError("The specified entity isn't a merchant!");
            return;
        }
        List<MerchantRecipe> recipes = new ArrayList<>();
        for (TradeTag trade : trades) {
            recipes.add(trade.getRecipe());
        }
        for (PlayerTag player : players) {
            if (player.isValid() && player.isOnline()) {
                Merchant merchant = PaperAPITools.instance.createMerchant(title.asString());
                merchant.setRecipes(recipes);
                player.getPlayerEntity().openMerchant(merchant, true);
            }
            else {
                Debug.echoError("Tried to make a nonexistent or offline player view a virtual trading inventory!");
            }
        }
    }
}
