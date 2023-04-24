package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.InvisibleTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.entity.EntityMetadataCommandHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class InvisibleCommand extends AbstractCommand {

    public static final EntityMetadataCommandHelper helper = new EntityMetadataCommandHelper(InvisibleCommand::isInvisible, InvisibleCommand::setInvisible);

    public InvisibleCommand() {
        setName("invisible");
        setSyntax("invisible (<entity>|...) ({true}/false/toggle/reset) (for:<player>|...)");
        setRequiredArguments(0, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Invisible
    // @Syntax invisible (<entity>|...) ({true}/false/toggle/reset) (for:<player>|...)
    // @Required 0
    // @Maximum 3
    // @Short Sets whether an NPC or entity are invisible.
    // @Group entity
    //
    // @Description
    // Sets whether the specified entities are invisible (equivalent to an invisibility potion), defaults to the linked player/NPC if none were specified.
    // If an NPC was specified, the 'invisible' trait is applied.
    //
    // Optionally specify 'for:' with a list of players to fake the entities' visibility state for these players.
    // When using 'toggle' with the 'for:' argument, the visibility state will be toggled for each player separately.
    // If unspecified, will be set globally.
    // 'for:' players remain tracked even when offline/reconnecting, but are forgotten after server restart.
    // Note that using the 'for:' argument won't apply the 'invisible' trait to NPCs.
    //
    // When not using 'for:', the effect is global / on the real entity, which will persist in that entity's data until changed.
    //
    // To reset an entity's fake visibility use the 'reset' state.
    // A reset is global by default, use the 'for:' argument to reset specific players.
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
    // - invisible <npc> toggle
    //
    // @Usage
    // Use to make an entity visible for specific players, without changing the way other players see it.
    // - invisible <[entity]> false for:<[player1]>|<[player2]>
    //
    // @Usage
    // Use to reset an entity's fake visibility state for the linked player.
    // - invisible <[entity]> reset for:<player>
    // -->

    public static boolean isInvisible(Entity entity) {
        if (EntityTag.isCitizensNPC(entity)) {
            InvisibleTrait invisibleTrait = NPCTag.fromEntity(entity).getCitizen().getTraitNullable(InvisibleTrait.class);
            return invisibleTrait != null && invisibleTrait.isInvisible();
        }
        else if (entity instanceof ArmorStand armorStand) {
            return !armorStand.isVisible();
        }
        else if (entity instanceof ItemFrame itemFrame) {
            return !itemFrame.isVisible();
        }
        else if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.isInvisible() || livingEntity.hasPotionEffect(PotionEffectType.INVISIBILITY);
        }
        else {
            return NMSHandler.entityHelper.isInvisible(entity);
        }
    }

    public static void setInvisible(EntityTag entity, boolean invisible) {
        if (entity.isCitizensNPC()) {
            entity.getDenizenNPC().getCitizen().getOrAddTrait(InvisibleTrait.class).setInvisible(invisible);
        }
        else if (entity.getBukkitEntity() instanceof ArmorStand armorStand) {
            armorStand.setVisible(!invisible);
        }
        else if (entity.getBukkitEntity() instanceof ItemFrame itemFrame) {
            itemFrame.setVisible(!invisible);
        }
        else if (!entity.isFake && entity.getBukkitEntity() instanceof LivingEntity livingEntity) {
            livingEntity.setInvisible(invisible);
            if (!invisible) {
                // Remove the invisibility potion effect for compact with old uses (the command used to add it)
                livingEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
        else {
            NMSHandler.entityHelper.setInvisible(entity.getBukkitEntity(), invisible);
        }
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("target") @ArgLinear @ArgDefaultNull @ArgSubType(EntityTag.class) List<EntityTag> targets,
                                   @ArgName("state") @ArgDefaultText("true") EntityMetadataCommandHelper.Action action,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers) {
        helper.execute(scriptEntry, targets, action, forPlayers);
    }
}
