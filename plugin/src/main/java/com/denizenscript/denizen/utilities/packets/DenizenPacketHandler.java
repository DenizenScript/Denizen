package com.denizenscript.denizen.utilities.packets;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.player.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.packets.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.player.GlowCommand;
import com.denizenscript.denizen.scripts.commands.server.ExecuteCommand;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class DenizenPacketHandler {

    public static DenizenPacketHandler instance;

    public static HashSet<UUID> forceNoclip = new HashSet<>();

    public void receivePacket(final Player player, final PacketInResourcePackStatus resourcePackStatus) {
        if (!ResourcePackStatusScriptEvent.instance.enabled) {
            return;
        }
        Bukkit.getScheduler().runTask(Denizen.getInstance(), () -> {
            ResourcePackStatusScriptEvent event = ResourcePackStatusScriptEvent.instance;
            event.status = new ElementTag(resourcePackStatus.getStatus());
            event.player = PlayerTag.mirrorBukkitPlayer(player);
            event.fire();
        });
    }

    public boolean receivePacket(final Player player, final PacketInSteerVehicle steerVehicle, Runnable allow) {
        if (PlayerSteersEntityScriptEvent.instance.enabled) {
            Runnable process = () -> {
                if (!player.isInsideVehicle()) {
                    return;
                }
                PlayerSteersEntityScriptEvent event = PlayerSteersEntityScriptEvent.instance;
                event.player = PlayerTag.mirrorBukkitPlayer(player);
                event.entity = new EntityTag(player.getVehicle());
                event.sideways = new ElementTag(steerVehicle.getLeftwardInput());
                event.forward = new ElementTag(steerVehicle.getForwardInput());
                event.jump = new ElementTag(steerVehicle.getJumpInput());
                event.dismount = new ElementTag(steerVehicle.getDismountInput());
                event.cancelled = false;
                event.modifyCancellation = (c) -> event.cancelled = c;
                event.fire();
                if (!event.cancelled) {
                    allow.run();
                }
            };
            if (Bukkit.isPrimaryThread()) {
                process.run();
            }
            else {
                Bukkit.getScheduler().runTask(Denizen.getInstance(), process);
            }
        }
        return false;
    }

    public static HashSet<Material> raisableItems = new HashSet<>();

    static {
        raisableItems.add(Material.SHIELD);
        raisableItems.add(Material.CROSSBOW);
        raisableItems.add(Material.BOW);
        raisableItems.add(Material.TRIDENT);
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17)) {
            raisableItems.add(Material.valueOf("SPYGLASS"));
        }
    }

    public static boolean isHoldingRaisable(Player player) {
        return raisableItems.contains(player.getEquipment().getItemInMainHand().getType())
            || raisableItems.contains(player.getEquipment().getItemInOffHand().getType());
    }

    public void receivePlacePacket(final Player player) {
        if (!PlayerHoldsItemEvent.instance.enabled) {
            return;
        }
        if (isHoldingRaisable(player)) {
            Bukkit.getScheduler().runTask(Denizen.getInstance(), () -> {
                PlayerHoldsItemEvent.signalDidRaise(player);
            });
        }
    }

    public void receiveDigPacket(final Player player) {
        if (!PlayerHoldsItemEvent.instance.enabled) {
            return;
        }
        if (isHoldingRaisable(player)) {
            Bukkit.getScheduler().runTask(Denizen.getInstance(), () -> {
                PlayerHoldsItemEvent.signalDidLower(player);
            });
        }
    }

    public boolean shouldInterceptChatPacket() {
        return !ExecuteCommand.silencedPlayers.isEmpty()
                || PlayerReceivesMessageScriptEvent.instance.loaded
                || PlayerReceivesActionbarScriptEvent.instance.loaded;
    }

    public PlayerReceivesMessageScriptEvent sendPacket(final Player player, final PacketOutChat chat) {
        if (!chat.isActionbar() && ExecuteCommand.silencedPlayers.contains(player.getUniqueId())) {
            PlayerReceivesMessageScriptEvent event = (PlayerReceivesMessageScriptEvent) PlayerReceivesMessageScriptEvent.instance.clone();
            event.cancelled = true;
            return event;
        }
        if (chat.getMessage() == null) {
            return null;
        }
        final PlayerReceivesMessageScriptEvent event = chat.isActionbar() ? PlayerReceivesActionbarScriptEvent.instance : PlayerReceivesMessageScriptEvent.instance;
        if (event.loaded) {
            Callable<PlayerReceivesMessageScriptEvent> eventCall = () -> {
                event.reset();
                event.message = new ElementTag(chat.getMessage());
                event.rawJson = new ElementTag(chat.getRawJson());
                event.system = new ElementTag(chat.isSystem());
                event.player = PlayerTag.mirrorBukkitPlayer(player);
                return event.triggerNow();
            };
            try {
                if (DenizenCore.isMainThread()) {
                    return eventCall.call();
                }
                else {
                    FutureTask<PlayerReceivesMessageScriptEvent> futureTask = new FutureTask<>(eventCall);
                    Bukkit.getScheduler().runTask(Denizen.getInstance(), futureTask);
                    return futureTask.get();
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
                return null;
            }
        }
        return null;
    }

    public boolean shouldInterceptMetadata() {
        return !GlowCommand.glowViewers.isEmpty();
    }

    public boolean sendPacket(Player player, PacketOutEntityMetadata entityMetadata) {
        HashSet<UUID> players = GlowCommand.glowViewers.get(entityMetadata.getEntityId());
        return players != null && entityMetadata.checkForGlow() && !players.contains(player.getUniqueId());
    }
}
