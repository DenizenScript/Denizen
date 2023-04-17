package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record EntityMetadataCommandHelper(Function<Entity, Boolean> getter, BiConsumer<EntityTag, Boolean> setter, Map<UUID, Map<UUID, Boolean>> perPlayerMap) {

    public EntityMetadataCommandHelper(Function<Entity, Boolean> getter, BiConsumer<EntityTag, Boolean> setter) {
        this(getter, setter, new HashMap<>());
    }

    public void setForPlayers(List<PlayerTag> players, EntityTag target, Function<PlayerTag, Boolean> stateSupplier) {
        if (target == null || target.getUUID() == null || players == null) {
            return;
        }
        NetworkInterceptHelper.enable();
        boolean wasEntityAdded = !perPlayerMap.containsKey(target.getUUID());
        Map<UUID, Boolean> playerMap = perPlayerMap.computeIfAbsent(target.getUUID(), k -> new HashMap<>());
        for (PlayerTag player : players) {
            boolean state = stateSupplier.apply(player);
            boolean wasModified = wasEntityAdded || !playerMap.containsKey(player.getUUID()) || playerMap.get(player.getUUID()) != state;
            playerMap.put(player.getUUID(), state);
            if (wasModified && player.isOnline()) {
                NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), target.getBukkitEntity());
            }
        }
    }

    public Boolean getState(Entity entity, UUID player, boolean fakeOnly) {
        if (entity == null) {
            return null;
        }
        if (player != null) {
            Map<UUID, Boolean> playerMap = perPlayerMap.get(entity.getUniqueId());
            if (playerMap != null && playerMap.containsKey(player)) {
                return playerMap.get(player);
            }
        }
        if (fakeOnly) {
            return null;
        }
        return getter.apply(entity);
    }

    public void tabComplete(AbstractCommand.TabCompletionsBuilder builder) {
        builder.addWithPrefix("state:", Action.values());
    }

    public boolean noOverrides() {
        return perPlayerMap.isEmpty();
    }

    public enum Action {TRUE, FALSE, TOGGLE, RESET}

    public void execute(ScriptEntry scriptEntry, List<EntityTag> targets, Action action, List<PlayerTag> forPlayers) {
        if (targets == null) {
            targets = Utilities.entryDefaultEntityList(scriptEntry, true);
            if (targets == null) {
                throw new InvalidArgumentsRuntimeException("Must specify valid targets.");
            }
        }
        switch (action) {
            case TRUE, FALSE -> {
                boolean state = action == Action.TRUE;
                if (forPlayers == null) {
                    for (EntityTag target : targets) {
                        setter.accept(target, state);
                    }
                }
                else {
                    for (EntityTag target : targets) {
                        setForPlayers(forPlayers, target, player -> state);
                    }
                }
            }
            case TOGGLE -> {
                if (forPlayers == null) {
                    for (EntityTag target : targets) {
                        setter.accept(target, !getState(target.getBukkitEntity(), null, false));
                    }
                }
                else {
                    for (EntityTag target : targets) {
                        setForPlayers(forPlayers, target, player -> !getState(target.getBukkitEntity(), player.getUUID(), false));
                    }
                }
            }
            case RESET -> {
                for (EntityTag target : targets) {
                    Map<UUID, Boolean> playerMap = perPlayerMap.get(target.getUUID());
                    if (playerMap == null) {
                        return;
                    }
                    Set<UUID> playersToUpdate = new HashSet<>();
                    if (forPlayers == null) {
                        playersToUpdate.addAll(playerMap.keySet());
                        perPlayerMap.remove(target.getUUID());
                    }
                    else {
                        for (PlayerTag player : forPlayers) {
                            playerMap.remove(player.getUUID());
                            playersToUpdate.add(player.getUUID());
                        }
                        if (playerMap.isEmpty()) {
                            perPlayerMap.remove(target.getUUID());
                        }
                    }
                    if (!playersToUpdate.isEmpty()) {
                        for (Player player : NMSHandler.entityHelper.getPlayersThatSee(target.getBukkitEntity())) {
                            if (playersToUpdate.contains(player.getUniqueId())) {
                                NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player, target.getBukkitEntity());
                            }
                        }
                    }
                }
            }
        }
    }
}
