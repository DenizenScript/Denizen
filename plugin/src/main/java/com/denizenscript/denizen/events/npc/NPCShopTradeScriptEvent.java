package com.denizenscript.denizen.events.npc;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.citizensnpcs.trait.ShopTrait;
import net.citizensnpcs.trait.shop.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class NPCShopTradeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player trades with npc
    //
    // @Switch shop:<shop> to only process the event if the transaction was within a specific shop.
    //
    // @Group NPC
    //
    // @Location true
    //
    // @Triggers when a player trades in a Citizens shop.
    //
    // @Context
    // <context.cost> return a MapTag of the types of costs and what their values were in the purchase.
    // <context.result> return a MapTag of the types of results and what their values were in the purchase.
    // <context.shop> returns the name of the shop the purchase was made in.
    //
    // @Player Always.
    //
    // @NPC when the shop is accessed through an NPC.
    //
    // -->

    public NPCShopTradeScriptEvent() {
        registerCouldMatcher("player trades with npc");
        registerSwitches("shop");
    }

    public PlayerTag player;
    public NPCTag npc;
    public ShopTrait.NPCShop shop;
    public ShopTrait.NPCShopPurchaseEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("shop", new ElementTag(shop.getName()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, npc);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "cost" -> actionListToMap(event.getItem().getCost());
            case "result" -> actionListToMap(event.getItem().getResult());
            case "shop" -> new ElementTag(shop.getName(), true);
            default -> super.getContext(name);
        };
    }

    private MapTag actionListToMap(List<NPCShopAction> actions) {
        MapTag result = new MapTag();
        for (NPCShopAction action : actions) {
            if (action instanceof CommandAction commandAction) {
                result.putObject("commands", new ListTag(commandAction.commands, true));
            }
            else if (action instanceof ConditionAction conditionAction) {
                result.putObject("condition", new ElementTag(conditionAction.describe(), true));
            }
            else if (action instanceof ExperienceAction experienceAction) {
                result.putObject("experience", new ElementTag(experienceAction.exp));
            }
            else if (action instanceof ItemAction itemAction) {
                result.putObject("items", new ListTag(itemAction.items, ItemTag::new));
            }
            else if (action instanceof MoneyAction moneyAction) {
                result.putObject("money", new ElementTag(moneyAction.money));
            }
            else if (action instanceof OpenShopAction openShopAction) {
                result.putObject("open_shop", new ElementTag(openShopAction.shopName, true));
            }
            else if (action instanceof PermissionAction permissionAction) {
                result.putObject("permissions", new ListTag(permissionAction.permissions, true));
            }
        }
        return result;
    }

    @EventHandler
    public void onNPCShopPurchase(ShopTrait.NPCShopPurchaseEvent event) {
        this.event = event;
        shop = event.getShop();
        player = PlayerTag.mirrorBukkitPlayer(event.getPlayer());
        npc = shop.getNPC().isPresent() ? new NPCTag(shop.getNPC().get()) : null;
        fire(event);
    }
}
