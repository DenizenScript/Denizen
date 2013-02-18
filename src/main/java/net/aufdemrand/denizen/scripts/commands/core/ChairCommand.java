package net.aufdemrand.denizen.scripts.commands.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the necessary methods and logic for NPCs to sit on blocks.
 *
 * @author Mason Adkins (Jeebiss)
 */
public class ChairCommand extends AbstractCommand implements Listener {

    private enum ChairAction { SIT, STAND }

    /**
     * Keeps track of sitting NPCs. References NPCID as the key (to avoid a lingering NPC instance
     * of the NPC is removed while sitting, etc.), and the Block that is being used as a Chair.
     */
    public static ConcurrentHashMap<Integer, Block> chairRegistry = new ConcurrentHashMap<Integer, Block>();

    @Override
    public void onEnable() {
        // Register with Bukkit's Event Registry
        denizen.getServer().getPluginManager().registerEvents(this, denizen);

        denizen.getServer().getScheduler().scheduleSyncRepeatingTask(denizen, new Runnable() {
            @Override
            public void run() {
                // Iterate through sitting NPCs
                for (Map.Entry<Integer, Block> entry : chairRegistry.entrySet()) {
                    // Get a valid dNPC object
                    NPC npc = CitizensAPI.getNPCRegistry().getById(entry.getKey());
                    if (npc == null)
                        chairRegistry.remove(entry.getKey());
                    // Check location
                    if (!Utilities.checkLocation(npc.getBukkitEntity(), entry.getValue().getLocation(), 1))
                        makeStand(DenizenAPI.getDenizenNPC(npc));
                    else
                        makeSitAllPlayers(DenizenAPI.getDenizenNPC(npc));

                    dB.echoDebug("task done");
                }
            }

        }, 40L, 100L);
    }


    /**
     * <p>Parses the arguments for the CHAIR command.</p>
     *
     *
     *
     * @param scriptEntry
     * 		The {@link ScriptEntry}, which contains run-time context that may
     * 		be utilized by this Command.
     * @throws InvalidArgumentsException
     */
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initiate required fields
        Block chairBlock = null;
        ChairAction chairAction = ChairAction.SIT;

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesLocation(arg))
                chairBlock = aH.getLocationFrom(arg).getBlock();
                // dB.echoDebug("...sit location set");

            else if (aH.matchesArg("SIT, STAND", arg))
                chairAction = ChairAction.valueOf(aH.getStringFrom(arg).toUpperCase());
                // dB.echoDebug("...npc will " + chairAction.name());

            else throw new InvalidArgumentsException (dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Check for required objects
        if (chairBlock == null && chairAction != ChairAction.STAND)
            throw new InvalidArgumentsException("Must specify a location to sit!");

        // Store objects in scriptEntry for execution
        scriptEntry.addObject("chairBlock", chairBlock)
                .addObject("chairAction", chairAction);
    }


    /**
     * Executes a parsed scriptEntry.
     *
     * @param scriptEntry
     * @throws CommandExecutionException
     */
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Block chairBlock = (Block) scriptEntry.getObject("chairBlock");
        ChairAction chairAction = (ChairAction) scriptEntry.getObject("chairAction");
        dNPC npc = scriptEntry.getNPC();

        switch (chairAction) {
            case SIT:
                if (isSitting(npc)) {
                    dB.echoError("...NPC is already sitting!");
                    return;
                }

                if (isChair(chairBlock)) {
                    dB.echoError("...location is already being sat on!");
                    return;
                }

				/*
				 * Teleport NPC to the chair and
				 * make him sit. Good NPC!				
				 */
                npc.getEntity().teleport(chairBlock.getLocation().add(0.5, 0, 0.5), TeleportCause.PLUGIN);
                makeSitAllPlayers(npc);
                npc.action("sit", scriptEntry.getPlayer());

                chairRegistry.put(npc.getId(), chairBlock);
                // dB.echoDebug("...NPC sits!");
                break;

            case STAND:
                if (!isSitting(npc)) {
                    dB.echoError("...NPC is already standing!");
                    return;
                }

                makeStand(npc);
                npc.action("stand", scriptEntry.getPlayer());
                // dB.echoDebug("...NPC stands!");
                break;
        }

    }


    /*
     * Sends packet via ProtocolLib to all online
     * players so they can see the NPC as sitting.
     */
    public void makeSitAllPlayers(dNPC npc) {
        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(40);
            packet.getSpecificModifier(int.class).write(0, npc.getEntity().getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 4);
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            for (Player player : denizen.getServer().getOnlinePlayers()) {
                if (npc.getEntity().getWorld().equals(player.getWorld())) {
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    } catch (InvocationTargetException e) {
                        dB.echoError("...error sending packet to player: " + player.getName());
                    }
                }
            }

        } catch (Error e) {
            dB.echoError("ProtocolLib required for SIT command!!");
        }
    }


    /*
     * Sends the sit packet to a specific player.
     */
    public void makeSitSpecificPlayer(dNPC npc, Player player) {
        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(40);
            packet.getSpecificModifier(int.class).write(0, npc.getEntity().getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 4);
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            if (npc.getEntity().getWorld().equals(player.getWorld())) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                } catch (Exception e) {
                    dB.echoError("...error sending packet to player: " + player.getName());
                }
            }

        } catch (Error e) {
            dB.echoError("ProtocolLib required for SIT command!!");
        }
    }


    /*
     * Sends packet via ProtocolLib to all online
     * players so they can see the NPC as standing.
     */
    public void makeStand(dNPC npc) {
        try {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(40);
            packet.getSpecificModifier(int.class).write(0, npc.getEntity().getEntityId());
            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 0);
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

            for (Player player : denizen.getServer().getOnlinePlayers()) {
                if (npc.getEntity().getWorld().equals(player.getWorld())) {
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    } catch (InvocationTargetException e) {
                        dB.echoError("...error sending packet to player: " + player.getName());
                    }
                }
            }
        } catch (Error e) {
            dB.echoError("ProtocolLib required for SIT command!!");
        }

        chairRegistry.remove(npc);
    }


    /*
     * Checks if given NPC is registered as sitting.
     */
    public Boolean isSitting(dNPC npc) {
        if (chairRegistry.containsKey(npc.getId())) return true;
        return false;
    }

    /**
     * Checks if given Block is registered as a chair.
     *
     * @return true if the block is currently a chair.
     */
    public Boolean isChair(Block block) {
        if (chairRegistry.containsValue(block)) return true;
        return false;
    }

    /**
     * If the NPC starts to navigate, and
     * he is sitting, he better stand up.
     *
     */
    @EventHandler
    public void onNavigationBeginEvent(NavigationBeginEvent event) {
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        if (isSitting(npc))
            makeStand(npc);
    }

    /**
     * If someone tries to break the poor
     * NPC's chair, we need to stop them!
     *
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isChair(event.getBlock())) {
            event.setCancelled(true);
            dB.echoDebug("..." + event.getPlayer().getName() + " tried to break an NPCs chair!");
        }
    }

    /**
     * Send packets for all currently sitting NPCs
     * to all new players who join.
     *
     */
    @EventHandler
    public void onPlayerLoginEvent(PlayerJoinEvent event) {
        Set<dNPC> npcs = new HashSet<dNPC>();
        for (Integer intgr : chairRegistry.keySet()) {
            npcs.add(DenizenAPI.getDenizenNPC(
                    CitizensAPI.getNPCRegistry().getById(intgr)
            ));
        }

        for (dNPC npc : npcs)
            makeSitSpecificPlayer(npc, event.getPlayer());
        // dB.echoDebug("..." + event.getPlayer().getName() + " joined, sending sit packets.");
    }

}
