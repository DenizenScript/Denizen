package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

public class DisguiseCommand extends AbstractCommand {

    public DisguiseCommand() {
        setName("disguise");
        setSyntax("disguise [<entity>] [cancel/as:<type>] (global/players:<player>|...)");
        setRequiredArguments(2, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name disguise
    // @Syntax disguise [<entity>] [cancel/as:<type>] (global/players:<player>|...)
    // @Required 2
    // @Maximum 4
    // @Short Makes the player see an entity as though it were a different type of entity.
    // @Group player
    //
    // @Description
    // Makes the player see an entity as though it were a different type of entity.
    //
    // The entity won't actually change on the server.
    // The entity will still visibly behave the same as the real entity type does.
    //
    // Be warned that the replacement is imperfect, and visual or internal-client errors may arise from using this command.
    //
    // If you disguise a player to themself, they will see a slightly-lagging-behind copy of the disguise entity.
    //
    // The disguise will last until a server restart, or the cancel option is used.
    //
    // Optionally, specify a list of players to show or cancel the entity to.
    // If unspecified, will default to the linked player.
    // Or, specify 'global' to make the disguise or cancel apply for all players. If using global, use "players:<player>" to show to the self-player.
    //
    // To remove a disguise, use the 'cancel' argument.
    //
    // @Tags
    // <PlayerTag.disguise_to_self[(<player>)]>
    // <EntityTag.is_disguised[(<player>)]>
    // <EntityTag.disguised_type[(<player>)]>
    // <EntityTag.disguise_to_others[(<player>)]>
    //
    // @Usage
    // Use to show a turn the NPC into a creeper for the linked player.
    // - disguise <npc> as:creeper
    //
    // @Usage
    // Use to show a turn the NPC into a red sheep for the linked player.
    // - disguise <npc> as:sheep[color=red]
    //
    // -->

    @Override
    public void addCustomTabCompletions(String arg, Consumer<String> addOne) {
        if (arg.startsWith("as:")) {
            for (EntityType entity : EntityType.values()) {
                addOne.accept("as:" + entity.name());
            }
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("to", "players")) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("global")
                    && arg.matches("global")) {
                scriptEntry.addObject("global", new ElementTag(true));
            }
            else if (arg.matchesPrefix("as")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("as", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("entity", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("players") && Utilities.entryHasPlayer(scriptEntry)) {
            if (scriptEntry.hasObject("global")) {
                scriptEntry.defaultObject("players", new ArrayList<>());
            }
            else {
                scriptEntry.defaultObject("players", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
            }
        }
        if (!scriptEntry.hasObject("as") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a valid type to disguise as!");
        }
        if (!scriptEntry.hasObject("players") && !scriptEntry.hasObject("global")) {
            throw new InvalidArgumentsException("Must have a valid player attached, or 'global' set!");
        }
        if (!scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a valid entity!");
        }
    }

    public static class TrackedDisguise implements Listener {

        public EntityTag entity;

        public EntityTag as;

        public FakeEntity fakeToSelf;

        public FakeEntity toOthers;

        public boolean shouldFake;

        public boolean isActive;

        public TrackedDisguise(EntityTag entity, EntityTag as) {
            this.entity = entity;
            this.as = as;
        }

        public void removeFor(PlayerTag player) {
            if (player.getUUID().equals(entity.getUUID())) {
                if (fakeToSelf != null) {
                    stopFake(player);
                }
                if (shouldFake) {
                    HandlerList.unregisterAll(this);
                    shouldFake = false;
                }
            }
            else if (player.isOnline()) {
                NMSHandler.getPlayerHelper().deTrackEntity(player.getPlayerEntity(), entity.getBukkitEntity());
            }
        }

        public void moveFakeNow(Location position) {
            NMSHandler.getEntityHelper().snapPositionTo(fakeToSelf.entity.getBukkitEntity(), position.toVector());
            NMSHandler.getEntityHelper().look(fakeToSelf.entity.getBukkitEntity(), position.getYaw(), position.getPitch());
        }

        public void startFake(PlayerTag player) {
            if (fakeToSelf != null) {
                stopFake(player);
            }
            if (!shouldFake) {
                shouldFake = true;
                Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
            }
            if (!player.isOnline()) {
                return;
            }
            fakeToSelf = FakeEntity.showFakeEntityTo(Collections.singletonList(player), as, player.getLocation(), null);
            NMSHandler.getPacketHelper().generateNoCollideTeam(player.getPlayerEntity(), fakeToSelf.entity.getUUID());
            NMSHandler.getPacketHelper().sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), player.getPlayerEntity());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (fakeToSelf == null || !fakeToSelf.entity.isFakeValid || !player.isOnline()) {
                        stopFake(player);
                        cancel();
                        return;
                    }
                    moveFakeNow(player.getLocation());
                }
            }.runTaskTimer(Denizen.getInstance(), 1, 1);
        }

        public void stopFake(PlayerTag player) {
            if (fakeToSelf == null) {
                return;
            }
            if (player.isOnline()) {
                NMSHandler.getPacketHelper().removeNoCollideTeam(player.getPlayerEntity(), fakeToSelf.entity.getUUID());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline()) {
                            NMSHandler.getPacketHelper().sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), player.getPlayerEntity());
                        }
                    }
                }.runTaskLater(Denizen.getInstance(), 2);
            }
            fakeToSelf.cancelEntity();
            fakeToSelf = null;
        }

        public void sendTo(List<PlayerTag> players) {
            PlayerTag remove = null;
            for (PlayerTag player : players) {
                if (player.getUUID().equals(entity.getUUID())) {
                    remove = player;
                    startFake(player);
                }
                else {
                    NMSHandler.getPlayerHelper().sendEntityDestroy(player.getPlayerEntity(), entity.getBukkitEntity());
                }
            }
            if (remove != null) {
                if (players.size() == 1) {
                    return;
                }
                players.remove(remove);
            }
            if (players.isEmpty()) {
                return;
            }
            if (toOthers == null) {
                toOthers = NMSHandler.getPlayerHelper().sendEntitySpawn(players, as.getEntityType(), entity.getLocation(), as.getWaitingMechanisms(), entity.getBukkitEntity().getEntityId(), entity.getUUID(), false);
                toOthers.overrideUUID = UUID.randomUUID();
                toOthers.entity.uuid = toOthers.overrideUUID;
                FakeEntity.idsToEntities.put(toOthers.overrideUUID, toOthers);
            }
            else {
                for (PlayerTag player : players) {
                    toOthers.triggerSpawnPacket.accept(player);
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onJoin(PlayerJoinEvent event) {
            if (!shouldFake || !event.getPlayer().getUniqueId().equals(entity.getUUID())) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().isOnline() || !isActive) {
                        return;
                    }
                    startFake(new PlayerTag(event.getPlayer()));
                }
            }.runTaskLater(Denizen.getInstance(), 2);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onTeleport(PlayerTeleportEvent event) {
            if (fakeToSelf == null || !event.getPlayer().getUniqueId().equals(entity.getUUID())) {
                return;
            }
            stopFake(new PlayerTag(event.getPlayer()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getPlayer().isOnline() || !isActive) {
                        return;
                    }
                    startFake(new PlayerTag(event.getPlayer()));
                }
            }.runTaskLater(Denizen.getInstance(), 2);
        }


        @EventHandler(priority = EventPriority.MONITOR)
        public void onMove(PlayerMoveEvent event) {
            if (fakeToSelf == null || !event.getPlayer().getUniqueId().equals(entity.getUUID())) {
                return;
            }
            if (event.getTo() == null) {
                return;
            }
            moveFakeNow(event.getTo());
            if (fakeToSelf.triggerUpdatePacket != null) {
                fakeToSelf.triggerUpdatePacket.run();
            }
        }
    }

    public static HashMap<UUID, HashMap<UUID, TrackedDisguise>> disguises = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        NetworkInterceptHelper.enable();
        EntityTag entity = scriptEntry.getObjectTag("entity");
        EntityTag as = scriptEntry.getObjectTag("as");
        ElementTag cancel = scriptEntry.getElement("cancel");
        ElementTag global = scriptEntry.getElement("global");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entity.debug()
                    + (cancel != null ? cancel.debug() : as.debug())
                    + (global != null ? global.debug() : "")
                    + ArgumentHelper.debugList("players", players));
        }
        boolean isGlobal = global != null && global.asBoolean();
        HashMap<UUID, TrackedDisguise> playerMap = disguises.get(entity.getUUID());
        if (playerMap != null) {
            if (isGlobal) {
                for (Map.Entry<UUID, TrackedDisguise> entry : playerMap.entrySet()) {
                    if (entry.getKey() == null) {
                        for (Player player : entity.getWorld().getPlayers()) {
                            if (entry.getValue().toOthers != null) {
                                FakeEntity.idsToEntities.remove(entry.getValue().toOthers.overrideUUID);
                            }
                            entry.getValue().removeFor(new PlayerTag(player));
                        }
                    }
                    else {
                        PlayerTag player = new PlayerTag(entry.getKey());
                        entry.getValue().removeFor(player);
                    }
                    entry.getValue().isActive = false;
                }
                disguises.remove(entity.getUUID());
            }
            else {
                for (PlayerTag player : players) {
                    TrackedDisguise disguise = playerMap.remove(player.getUUID());
                    if (disguise != null) {
                        disguise.isActive = false;
                        disguise.removeFor(player);
                        if (disguise.toOthers != null) {
                            FakeEntity.idsToEntities.remove(disguise.toOthers.overrideUUID);
                        }
                        if (playerMap.isEmpty()) {
                            disguises.remove(entity.getUUID());
                        }
                    }
                }
            }
        }
        if (cancel == null || !cancel.asBoolean()) {
            TrackedDisguise disguise = new TrackedDisguise(entity, as);
            disguise.as.entity = NMSHandler.getPlayerHelper().sendEntitySpawn(new ArrayList<>(), as.getEntityType(), entity.getLocation(), as.getWaitingMechanisms(), -1, null, false).entity.getBukkitEntity();
            if (isGlobal) {
                playerMap = disguises.computeIfAbsent(entity.getUUID(), k -> new HashMap<>());
                playerMap.put(null, disguise);
                disguise.isActive = true;
                ArrayList<PlayerTag> playerSet = players == null ? new ArrayList<>() : new ArrayList<>(players);
                for (Player player : entity.getWorld().getPlayers()) {
                    if (!playerSet.contains(new PlayerTag(player)) && !player.getUniqueId().equals(entity.getUUID())) {
                        playerSet.add(new PlayerTag(player));
                    }
                }
                disguise.sendTo(playerSet);
            }
            else {
                if (players != null) {
                    for (PlayerTag player : players) {
                        playerMap = disguises.computeIfAbsent(entity.getUUID(), k -> new HashMap<>());
                        playerMap.put(player.getUUID(), disguise);
                        disguise.isActive = true;
                    }
                }
                disguise.sendTo(players);
            }
        }
    }
}
