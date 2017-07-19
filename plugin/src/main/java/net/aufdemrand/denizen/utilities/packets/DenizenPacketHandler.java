package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.events.player.PlayerReceivesMessageScriptEvent;
import net.aufdemrand.denizen.events.player.PlayerSteersEntityScriptEvent;
import net.aufdemrand.denizen.events.player.ResourcePackStatusScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.packets.*;
import net.aufdemrand.denizen.nms.util.TradeOffer;
import net.aufdemrand.denizen.nms.util.jnbt.StringTag;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.player.GlowCommand;
import net.aufdemrand.denizen.scripts.commands.server.ExecuteCommand;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
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
                event.status = new Element(resourcePackStatus.getStatus());
                event.player = dPlayer.mirrorBukkitPlayer(player);
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
                            event.player = dPlayer.mirrorBukkitPlayer(player);
                            event.entity = player.isInsideVehicle() ? new dEntity(player.getVehicle()) : null;
                            event.sideways = new Element(steerVehicle.getLeftwardInput());
                            event.forward = new Element(steerVehicle.getForwardInput());
                            event.jump = new Element(steerVehicle.getJumpInput());
                            event.dismount = new Element(steerVehicle.getDismountInput());
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
                dB.echoError(e);
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
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    int pos = chat.getPosition();
                    if (pos != 2) {
                        event.message = new Element(chat.getMessage());
                        event.rawJson = new Element(chat.getRawJson());
                        event.system = new Element(pos == 1);
                        event.messageModified = false;
                        event.rawJsonModified = false;
                        event.player = dPlayer.mirrorBukkitPlayer(player);
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
            if (Bukkit.isPrimaryThread()) {
                futureTask.run();
            }
            else {
                Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), futureTask);
            }
            try {
                return futureTask.get();
            }
            catch (Exception e) {
                dB.echoError(e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean sendPacket(Player player, PacketOutEntityMetadata entityMetadata) {
        HashSet<UUID> players = GlowCommand.glowViewers.get(entityMetadata.getEntityId());
        // TODO: Check effect type against GLOWING (24)
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
                return NMSHandler.getInstance().getItemHelper().addNbtData(itemStack, "Denizen Item Script", new StringTag(hash));
            }
        }
        return itemStack;
    }
}
