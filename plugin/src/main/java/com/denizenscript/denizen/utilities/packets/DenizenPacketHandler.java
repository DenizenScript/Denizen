package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.DenizenCoreImplementation;
import com.denizenscript.denizen.nms.interfaces.packets.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.player.PlayerReceivesMessageScriptEvent;
import com.denizenscript.denizen.events.player.PlayerSteersEntityScriptEvent;
import com.denizenscript.denizen.events.player.ResourcePackStatusScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.TradeOffer;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.player.GlowCommand;
import com.denizenscript.denizen.scripts.commands.server.ExecuteCommand;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class DenizenPacketHandler implements PacketHandler {

    @Override
    public void receivePacket(final Player player, final PacketInResourcePackStatus resourcePackStatus) {
        Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {
                ResourcePackStatusScriptEvent event = ResourcePackStatusScriptEvent.instance;
                // TODO: get hash on server?... last sent hash? event.hash = new Element(hash);
                event.status = new ElementTag(resourcePackStatus.getStatus());
                event.player = PlayerTag.mirrorBukkitPlayer(player);
                event.fire();
            }
        });
    }

    @Override
    public boolean receivePacket(final Player player, final PacketInSteerVehicle steerVehicle) {
        if (PlayerSteersEntityScriptEvent.instance.enabled) {
            Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(),
                    new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            PlayerSteersEntityScriptEvent event = PlayerSteersEntityScriptEvent.instance;
                            event.player = PlayerTag.mirrorBukkitPlayer(player);
                            event.entity = player.isInsideVehicle() ? new EntityTag(player.getVehicle()) : null;
                            event.sideways = new ElementTag(steerVehicle.getLeftwardInput());
                            event.forward = new ElementTag(steerVehicle.getForwardInput());
                            event.jump = new ElementTag(steerVehicle.getJumpInput());
                            event.dismount = new ElementTag(steerVehicle.getDismountInput());
                            event.cancelled = false;
                            event.fire();
                            return event.cancelled;
                        }
                    }
            );
            try {
                return future.get();
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
        return false;
    }

    @Override
    public boolean sendPacket(final Player player, final PacketOutChat chat) {
        if (ExecuteCommand.silencedPlayers.contains(player.getUniqueId())) {
            return true;
        }
        final PlayerReceivesMessageScriptEvent event = PlayerReceivesMessageScriptEvent.instance;
        if (event.loaded) {
            FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    int pos = chat.getPosition();
                    if (pos != 2) {
                        event.message = new ElementTag(chat.getMessage());
                        event.rawJson = new ElementTag(chat.getRawJson());
                        event.system = new ElementTag(pos == 1);
                        event.messageModified = false;
                        event.rawJsonModified = false;
                        event.player = PlayerTag.mirrorBukkitPlayer(player);
                        event.cancelled = false;
                        event.fire();
                        if (event.messageModified) {
                            chat.setMessage(event.message.asString());
                        }
                        else if (event.rawJsonModified) {
                            chat.setRawJson(event.rawJson.asString());
                        }
                        return event.cancelled;
                    }
                    return false;
                }
            });
            if (DenizenCoreImplementation.isSafeThread()) {
                futureTask.run();
            }
            else {
                Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), futureTask);
            }
            try {
                return futureTask.get();
            }
            catch (Exception e) {
                Debug.echoError(e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean sendPacket(Player player, PacketOutEntityMetadata entityMetadata) {
        HashSet<UUID> players = GlowCommand.glowViewers.get(entityMetadata.getEntityId());
        return players != null && entityMetadata.checkForGlow() && !players.contains(player.getUniqueId());
    }

    @Override
    public boolean sendPacket(Player player, PacketOutSetSlot setSlot) {
        setSlot.setItemStack(removeItemScriptLore(setSlot.getItemStack()));
        return false;
    }

    @Override
    public boolean sendPacket(Player player, PacketOutWindowItems windowItems) {
        ItemStack[] contents = windowItems.getContents();
        for (int i = 0; i < contents.length; i++) {
            contents[i] = removeItemScriptLore(contents[i]);
        }
        windowItems.setContents(contents);
        return false;
    }

    @Override
    public boolean sendPacket(Player player, PacketOutTradeList tradeList) {
        List<TradeOffer> tradeOffers = tradeList.getTradeOffers();
        for (TradeOffer tradeOffer : tradeOffers) {
            tradeOffer.setFirstCost(removeItemScriptLore(tradeOffer.getFirstCost()));
            tradeOffer.setSecondCost(removeItemScriptLore(tradeOffer.getSecondCost()));
            tradeOffer.setProduct(removeItemScriptLore(tradeOffer.getProduct()));
        }
        tradeList.setTradeOffers(tradeOffers);
        return false;
    }

    private static ItemStack removeItemScriptLore(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = meta.getLore();
            Iterator<String> iter = lore.iterator();
            String hash = null;
            while (iter.hasNext()) {
                String line = iter.next();
                if (line.startsWith(ItemScriptHelper.ItemScriptHashID)) {
                    hash = line;
                    iter.remove();
                    break;
                }
            }
            if (hash != null) {
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
                return NMSHandler.getItemHelper().addNbtData(itemStack, "Denizen Item Script", new StringTag(hash));
            }
        }
        return itemStack;
    }
}
