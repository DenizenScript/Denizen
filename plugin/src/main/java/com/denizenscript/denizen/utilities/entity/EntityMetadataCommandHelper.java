package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public record EntityMetadataCommandHelper(Predicate<Entity> getter, BiConsumer<EntityTag, Boolean> setter, Map<UUID, Map<UUID, Boolean>> packetOverrides) {

    public EntityMetadataCommandHelper(Predicate<Entity> getter, BiConsumer<EntityTag, Boolean> setter) {
        this(getter, setter, new HashMap<>());
    }

    public void setForPlayers(List<PlayerTag> players, EntityTag target, Predicate<PlayerTag> stateSupplier) {
        if (target == null || target.getUUID() == null || players == null) {
            return;
        }
        NetworkInterceptHelper.enable();
        boolean wasEntityAdded = !packetOverrides.containsKey(target.getUUID());
        Map<UUID, Boolean> playerMap = packetOverrides.computeIfAbsent(target.getUUID(), k -> new HashMap<>());
        for (PlayerTag player : players) {
            boolean state = stateSupplier.test(player);
            Boolean oldState = playerMap.put(player.getUUID(), state);
            if ((wasEntityAdded || oldState == null || oldState != state) && player.isOnline()) {
                NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), target.getBukkitEntity());
            }
        }
    }

    public Boolean getState(Entity entity, UUID player, boolean fakeOnly) {
        if (entity == null) {
            return null;
        }
        if (player != null) {
            Map<UUID, Boolean> playerMap = packetOverrides.get(entity.getUniqueId());
            if (playerMap != null && playerMap.containsKey(player)) {
                return playerMap.get(player);
            }
        }
        if (fakeOnly) {
            return null;
        }
        return getter.test(entity);
    }

    public boolean noOverrides() {
        return packetOverrides.isEmpty();
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
                    Map<UUID, Boolean> playerMap = packetOverrides.get(target.getUUID());
                    if (playerMap == null) {
                        return;
                    }
                    Set<UUID> playersToUpdate = new HashSet<>();
                    if (forPlayers == null) {
                        playersToUpdate.addAll(playerMap.keySet());
                        packetOverrides.remove(target.getUUID());
                    }
                    else {
                        for (PlayerTag player : forPlayers) {
                            if (playerMap.remove(player.getUUID()) != null) {
                                playersToUpdate.add(player.getUUID());
                            }
                        }
                        if (playerMap.isEmpty()) {
                            packetOverrides.remove(target.getUUID());
                        }
                    }
                    if (playersToUpdate.isEmpty()) {
                        return;
                    }
                    for (Player player : NMSHandler.entityHelper.getPlayersThatSee(target.getBukkitEntity())) {
                        if (playersToUpdate.contains(player.getUniqueId())) {
                            NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player, target.getBukkitEntity());
                        }
                    }
                    if (playersToUpdate.contains(target.getUUID())) {
                        NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(target.as(Player.class), target.getBukkitEntity());
                    }
                }
            }
        }
    }
}
