package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.InvisibleTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class InvisibleCommand extends AbstractCommand {

    public InvisibleCommand() {
        setName("invisible");
        setSyntax("invisible (<entity>) (state:true/false/toggle/reset) (for:<player>|...)");
        setPrefixesHandled("for");
        setRequiredArguments(0, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Invisible
    // @Syntax invisible (<entity>) (state:true/false/toggle/reset) (for:<player>|...)
    // @Required 0
    // @Maximum 3
    // @Short Sets whether an NPC or entity are invisible.
    // @Group entity
    //
    // @Description
    // Makes the specified entity invisible, defaults to the linked player/NPC if one wasn't specified.
    // If an NPC was specified, the 'invisible' trait is applied.
    //
    // Optionally specify 'for:' with a list of players to fake the entity's visibility state for these players.
    // When using the 'toggle' state with the 'for:' argument, the visibility state will be toggled for each player separately.
    // Note that using the 'for:' argument won't apply the 'invisible' trait to NPCs.
    // If unspecified, will be set globally.
    //
    // To reset an entity's fake visibility (for all players) use the 'reset' state.
    //
    // NPCs can't be made invisible if not added to the playerlist (the invisible trait adds the NPC to the playerlist when set).
    // See <@link language invisible trait>
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to make the linked player (or NPC, if there isn't one) invisible.
    // - invisible
    //
    // @Usage
    // Use to make the linked NPC visible if previously invisible, and invisible if not.
    // - invisible <npc> state:toggle
    //
    // @Usage
    // Use to make an entity visible for specific players, without changing the way other players see it.
    // - invisible <[entity]> state:false for:<[player1]>|<[player2]>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("state:", Action.values());
    }

    enum Action {TRUE, FALSE, TOGGLE, RESET}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("state")
                    && arg.matchesEnum(Action.class)) {
                scriptEntry.addObject("state", arg.asElement());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matches("player")
                    && Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("target", Utilities.getEntryPlayer(scriptEntry).getDenizenEntity());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matches("npc")
                    && Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("target", Utilities.getEntryNPC(scriptEntry).getDenizenEntity());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("target", arg.asType(EntityTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("state", new ElementTag("true"));
        scriptEntry.defaultObject("target", Utilities.entryDefaultEntity(scriptEntry, true));
    }

    public static HashMap<UUID, HashMap<UUID, Boolean>> invisibleEntities = new HashMap<>();

    public static void setInvisibleForPlayer(EntityTag target, PlayerTag player, boolean invisible) {
        if (target == null || target.getUUID() == null || player == null) {
            return;
        }
        NetworkInterceptHelper.enable();
        boolean wasModified = !invisibleEntities.containsKey(target.getUUID());
        HashMap<UUID, Boolean> playerMap = invisibleEntities.computeIfAbsent(target.getUUID(), k -> new HashMap<>());
        wasModified = !playerMap.containsKey(player.getUUID()) || playerMap.get(player.getUUID()) != invisible || wasModified;
        playerMap.put(player.getUUID(), invisible);
        if (wasModified) {
            NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player.getPlayerEntity(), target.getBukkitEntity());
        }
    }

    public void setInvisible(EntityTag entity, boolean invisible) {
        if (entity.isCitizensNPC()) {
            entity.getDenizenNPC().getCitizen().getOrAddTrait(InvisibleTrait.class).setInvisible(invisible);
        }
        else if (entity.getBukkitEntity() instanceof ArmorStand) {
            ((ArmorStand) entity.getBukkitEntity()).setVisible(!invisible);
        }
        else if (entity.getBukkitEntity() instanceof ItemFrame) {
            ((ItemFrame) entity.getBukkitEntity()).setVisible(!invisible);
        }
        else if (entity.isLivingEntity() && !entity.isFake) {
            entity.getLivingEntity().setInvisible(invisible);
            if (!invisible) {
                // Remove the invisibility potion effect for compact with old uses (the command used to add it)
                entity.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
        else {
            NMSHandler.entityHelper.setInvisible(entity.getBukkitEntity(), invisible);
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        EntityTag target = scriptEntry.getObjectTag("target");
        if (target == null) {
            Debug.echoError(scriptEntry, "Must specify a valid target.");
            return;
        }
        ElementTag state = scriptEntry.getElement("state");
        List<PlayerTag> forPlayers = scriptEntry.argForPrefixList("for", PlayerTag.class, true);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), target, state, db("for", forPlayers));
        }
        switch (state.asEnum(Action.class)) {
            case TRUE:
            case FALSE:
                if (forPlayers == null) {
                    setInvisible(target, state.asBoolean());
                }
                else {
                    boolean invisible = state.asBoolean();
                    for (PlayerTag player : forPlayers) {
                        setInvisibleForPlayer(target, player, invisible);
                    }
                }
                break;
            case TOGGLE:
                if (forPlayers == null) {
                    setInvisible(target, !isInvisible(target.getBukkitEntity(), null, false));
                }
                else {
                    for (PlayerTag player : forPlayers) {
                        setInvisibleForPlayer(target, player, !isInvisible(target.getBukkitEntity(), player.getUUID(), false));
                    }
                }
                break;
            case RESET:
                HashMap<UUID, Boolean> playerMap = invisibleEntities.get(target.getUUID());
                if (playerMap == null) {
                    return;
                }
                HashSet<UUID> playersToUpdate = new HashSet<>();
                if (forPlayers == null) {
                    playersToUpdate.addAll(playerMap.keySet());
                    invisibleEntities.remove(target.getUUID());
                }
                else {
                    for (PlayerTag player : forPlayers) {
                        playerMap.remove(player.getUUID());
                        playersToUpdate.add(player.getUUID());
                    }
                    if (playerMap.isEmpty()) {
                        invisibleEntities.remove(target.getUUID());
                    }
                }
                if (!playersToUpdate.isEmpty()) {
                    for (Player player : NMSHandler.entityHelper.getPlayersThatSee(target.getBukkitEntity())) {
                        if (playersToUpdate.contains(player.getUniqueId())) {
                            NMSHandler.packetHelper.sendEntityMetadataFlagsUpdate(player, target.getBukkitEntity());
                        }
                    }
                }
                break;
        }
    }

    public static Boolean isInvisible(Entity entity, UUID player, boolean fakeOnly) {
        if (entity == null) {
            return null;
        }
        if (player != null) {
            HashMap<UUID, Boolean> playerMap = invisibleEntities.get(entity.getUniqueId());
            if (playerMap != null && playerMap.containsKey(player)) {
                return playerMap.get(player);
            }
        }
        if (fakeOnly) {
            return null;
        }
        if (EntityTag.isCitizensNPC(entity)) {
            InvisibleTrait invisibleTrait = NPCTag.fromEntity(entity).getCitizen().getTraitNullable(InvisibleTrait.class);
            return invisibleTrait != null && invisibleTrait.isInvisible();
        }
        else if (entity instanceof ArmorStand) {
            return !((ArmorStand) entity).isVisible();
        }
        else if (entity instanceof ItemFrame) {
            return !((ItemFrame) entity).isVisible();
        }
        else if (entity instanceof LivingEntity) {
            // Check for the invisibility potion effect for compact with old uses (the command used to add it)
            return ((LivingEntity) entity).isInvisible() || ((LivingEntity) entity).hasPotionEffect(PotionEffectType.INVISIBILITY);
        }
        else {
            return NMSHandler.entityHelper.isInvisible(entity);
        }
    }

}
