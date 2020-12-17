package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DisguiseCommand extends AbstractCommand {

    public DisguiseCommand() {
        setName("disguise");
        setSyntax("disguise [<entity>] [cancel/as:<type>] (players:<player>|...)");
        setRequiredArguments(2, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name disguise
    // @Syntax disguise [<entity>] [cancel/as:<type>] (players:<player>|...)
    // @Required 2
    // @Maximum 3
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
    // This command should not be used to disguise players in their own view.
    //
    // The disguise will last until a server restart, or the cancel option is used.
    //
    // Optionally, specify a list of players to show or cancel the entity to.
    // If unspecified, will default to the linked player.
    //
    // To remove a disguise, use the 'cancel' argument.
    //
    // @Tags
    // None.
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
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("players")
                    && arg.matchesPrefix("to", "players")) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
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
            scriptEntry.defaultObject("players", Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));
        }
        if (!scriptEntry.hasObject("as") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify a valid type to disguise as!");
        }
        if (!scriptEntry.hasObject("players")) {
            throw new InvalidArgumentsException("Must have a valid, online player attached!");
        }
        if (!scriptEntry.hasObject("entity")) {
            throw new InvalidArgumentsException("Must specify a valid entity!");
        }
    }

    public static class TrackedDisguise {

        public EntityTag entity;

        public EntityTag as;

        public HashSet<UUID> players;

        public FakeEntity fake;

        public TrackedDisguise(EntityTag entity, EntityTag as, List<PlayerTag> players) {
            this.entity = entity;
            this.as = as;
            this.players = new HashSet<>();
            for (PlayerTag player : players ) {
                this.players.add(player.getOfflinePlayer().getUniqueId());
            }
        }

        public void sendTo(List<PlayerTag> players) {
            PlayerTag remove = null;
            for (PlayerTag player : players) {
                if (player.getOfflinePlayer().getUniqueId().equals(entity.getUUID())) {
                    remove = player;
                    if (fake == null) {
                        if (player.isOnline()) {
                            fake = FakeEntity.showFakeEntityTo(Collections.singletonList(player), as, player.getLocation(), null);
                            NMSHandler.getPacketHelper().generateNoCollideTeam(player.getPlayerEntity(), fake.entity.getUUID());
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!fake.entity.isFakeValid) {
                                        fake = null;
                                        cancel();
                                        return;
                                    }
                                    NMSHandler.getEntityHelper().move(fake.entity.getBukkitEntity(), player.getLocation().toVector().subtract(fake.entity.getLocation().toVector()));
                                    NMSHandler.getEntityHelper().look(fake.entity.getBukkitEntity(), player.getLocation().getYaw(), player.getLocation().getPitch());
                                }
                            }.runTaskTimer(Denizen.getInstance(), 1, 1);
                        }
                    }
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
            NMSHandler.getPlayerHelper().sendEntitySpawn(players, as.getBukkitEntityType(), entity.getLocation(), as.getWaitingMechanisms(), entity.getBukkitEntity().getEntityId(), entity.getUUID(), false);
        }
    }

    public static HashMap<UUID, HashMap<UUID, TrackedDisguise>> disguises = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        EntityTag entity = scriptEntry.getObjectTag("entity");
        EntityTag as = scriptEntry.getObjectTag("as");
        ElementTag cancel = scriptEntry.getElement("cancel");
        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), entity.debug()
                    + (cancel != null ? cancel.debug() : as.debug())
                    + ArgumentHelper.debugList("players", players));
        }
        if (cancel != null && cancel.asBoolean()) {
            for (PlayerTag player : players) {
                HashMap<UUID, TrackedDisguise> playerMap = disguises.get(entity.getUUID());
                if (playerMap != null) {
                    TrackedDisguise disguise = playerMap.remove(player.getOfflinePlayer().getUniqueId());
                    if (disguise != null) {
                        if (disguise.fake != null && player.getOfflinePlayer().getUniqueId().equals(entity.getUUID())) {
                            NMSHandler.getPacketHelper().removeNoCollideTeam(player.getPlayerEntity(), disguise.fake.entity.getUUID());
                            disguise.fake.cancelEntity();
                            disguise.fake = null;
                        }
                        else if (player.isOnline()) {
                            NMSHandler.getPlayerHelper().deTrackEntity(player.getPlayerEntity(), entity.getBukkitEntity());
                        }
                        if (playerMap.isEmpty()) {
                            disguises.remove(entity.getUUID());
                        }
                    }
                }
            }
        }
        else {
            TrackedDisguise disguise = new TrackedDisguise(entity, as, players);
            for (PlayerTag player : players) {
                HashMap<UUID, TrackedDisguise> playerMap = disguises.get(entity.getUUID());
                if (playerMap == null) {
                    playerMap = new HashMap<>();
                    disguises.put(entity.getUUID(), playerMap);
                }
                playerMap.put(player.getOfflinePlayer().getUniqueId(), disguise);
            }
            disguise.sendTo(players);
        }
    }
}
