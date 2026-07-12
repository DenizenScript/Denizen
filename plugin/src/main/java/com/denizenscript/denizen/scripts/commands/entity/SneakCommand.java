package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.SneakingTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SneakTrait;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SneakCommand extends AbstractCommand {

    public SneakCommand() {
        setName("sneak");
        setSyntax("sneak [<entity>|...] ({start}/stop) (fake/stopfake) (for:<player>|...)");
        setRequiredArguments(1, 4);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Sneak
    // @Syntax sneak [<entity>|...] ({start}/stop) (fake/stopfake) (for:<player>|...)
    // @Required 1
    // @Maximum 4
    // @Short Causes the entity to start or stop sneaking.
    // @Synonyms Crouch,Shift
    // @Group entity
    //
    // @Description
    // Causes an entity to start or stop sneaking.
    // If the entity is NPC, adds the SneakTrait to apply the sneak setting persistent.
    //
    // Can optionally use the 'fake' argument to apply a fake sneak using packets, either globally or for specific players.
    // Use 'stopfake' to disable faking of sneak.
    // A fake sneak only affects the name plate, not the entity's pose.
    //
    // Note: using this command on a player will only show to other players. You cannot alter a player in their own view.
    // Note that <@link property EntityTag.is_sneaking> is also available.
    //
    // @Tags
    // <EntityTag.is_sneaking>
    //
    // @Usage
    // Make the linked NPC start sneaking.
    // - sneak <npc>
    //
    // @Usage
    // Make the linked NPC stop sneaking.
    // - sneak <npc> stop
    //
    // -->

    public enum SneakMode { START, STOP }

    public enum SneakFake { FAKE, STOPFAKE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("entities") @ArgLinear @ArgDefaultNull @ArgSubType(EntityTag.class) List<EntityTag> entities,
                                   @ArgName("mode") @ArgLinear @ArgDefaultNull SneakMode mode,
                                   @ArgName("fake") @ArgLinear @ArgDefaultNull SneakFake fake,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> players) {
        if (entities == null) {
            throw new InvalidArgumentsRuntimeException("Missing entities argument.");
        }
        boolean shouldSneak = mode == null || mode.equals(SneakMode.START);
        boolean shouldFake = fake != null && fake.equals(SneakFake.FAKE);
        boolean shouldStopFake = fake != null && fake.equals(SneakFake.STOPFAKE);
        for (EntityTag entity : entities) {
            if (shouldFake || shouldStopFake) {
                if (players == null) {
                    updateFakeSneak(entity.getUUID(), null, shouldSneak, shouldFake);
                    for (Player player : NMSHandler.entityHelper.getPlayersThatSee(entity.getBukkitEntity())) {
                        NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player, entity.getBukkitEntity());
                    }
                }
                else {
                    for (PlayerTag player : players) {
                        updateFakeSneak(entity.getUUID(), player.getUUID(), shouldSneak, shouldFake);
                        NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), entity.getBukkitEntity());
                    }
                }
            }
            else if (entity.isCitizensNPC()) {
                NPC npc = entity.getDenizenNPC().getCitizen();
                if (npc.hasTrait(SneakingTrait.class)) {
                    npc.getOrAddTrait(SneakingTrait.class).stand();
                    npc.removeTrait(SneakingTrait.class);
                }
                SneakTrait trait = npc.getOrAddTrait(SneakTrait.class);
                trait.setSneaking(shouldSneak);
            }
            else if (entity.isSpawned()) {
                NMSHandler.entityHelper.setSneaking(entity.getBukkitEntity(), shouldSneak);
            }
            else {
                Debug.echoError("Cannot make unspawned entity sneak.");
            }
        }
    }

    public static HashMap<UUID, HashMap<UUID, Boolean>> forceSetSneak = new HashMap<>();

    public static void updateFakeSneak(UUID entity, UUID player, boolean shouldSneak, boolean start) {
        NetworkInterceptHelper.enable();
        HashMap<UUID, Boolean> subMap = forceSetSneak.get(entity);
        if (subMap == null) {
            if (!start) {
                return;
            }
            subMap = new HashMap<>();
            forceSetSneak.put(entity, subMap);
        }
        if (start) {
            subMap.put(player, shouldSneak);
        }
        else {
            subMap.remove(player);
            if (subMap.isEmpty()) {
                forceSetSneak.remove(entity);
            }
        }
    }

    public static Boolean shouldSneak(UUID entity, UUID player) {
        HashMap<UUID, Boolean> subMap = forceSetSneak.get(entity);
        if (subMap == null) {
            return null;
        }
        Boolean b = subMap.get(player);
        if (b != null) {
            return b;
        }
        return subMap.get(null);
    }
}
