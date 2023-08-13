package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.SneakingTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
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
    // If the entity is NPC, adds the SneakingTrait to apply the sneak setting persistent.
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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matches("fake")
                    && !scriptEntry.hasObject("fake")
                    && !scriptEntry.hasObject("stopfake")) {
                scriptEntry.addObject("fake", new ElementTag(true));
            }
            else if (arg.matches("stopfake")
                    && !scriptEntry.hasObject("fake")
                    && !scriptEntry.hasObject("stopfake")) {
                scriptEntry.addObject("stopfake", new ElementTag(true));
            }
            else if ((arg.matches("start") || arg.matches("stop"))
                    && !scriptEntry.hasObject("mode")) {
                scriptEntry.addObject("mode", arg.asElement());
            }
            else if (arg.matchesPrefix("for")
                    && arg.matchesArgumentList(PlayerTag.class)
                    && !scriptEntry.hasObject("for_players")) {
                scriptEntry.addObject("for_players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (arg.matchesArgumentList(EntityTag.class)
                    && !scriptEntry.hasObject("entities")) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Missing entities argument.");
        }
        scriptEntry.defaultObject("mode", new ElementTag("start"));
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

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag fake = scriptEntry.getElement("fake");
        ElementTag stopfake = scriptEntry.getElement("stopfake");
        ElementTag mode = scriptEntry.getElement("mode");
        List<PlayerTag> forPlayers = (List<PlayerTag>) scriptEntry.getObject("for_players");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), mode, db("entities", entities), db("for_players", forPlayers), fake, stopfake);
        }
        boolean shouldSneak = mode.asString().equalsIgnoreCase("start");
        boolean shouldFake = fake != null && fake.asBoolean();
        boolean shouldStopFake = stopfake != null && stopfake.asBoolean();
        for (EntityTag entity : entities) {
            if (shouldFake || shouldStopFake) {
                if (forPlayers == null) {
                    updateFakeSneak(entity.getUUID(), null, shouldSneak, shouldFake);
                    for (Player player : NMSHandler.entityHelper.getPlayersThatSee(entity.getBukkitEntity())) {
                        NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player, entity.getBukkitEntity());
                    }
                }
                else {
                    for (PlayerTag player : forPlayers) {
                        updateFakeSneak(entity.getUUID(), player.getUUID(), shouldSneak, shouldFake);
                        NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), entity.getBukkitEntity());
                    }
                }
            }
            else if (entity.isCitizensNPC()) {
                SneakingTrait trait = entity.getDenizenNPC().getCitizen().getOrAddTrait(SneakingTrait.class);
                if (shouldSneak) {
                    trait.sneak();
                }
                else {
                    trait.stand();
                }
            }
            else if (entity.isSpawned()) {
                NMSHandler.entityHelper.setSneaking(entity.getBukkitEntity(), shouldSneak);
            }
            else {
                Debug.echoError("Cannot make unspawned entity sneak.");
            }
        }
    }
}
