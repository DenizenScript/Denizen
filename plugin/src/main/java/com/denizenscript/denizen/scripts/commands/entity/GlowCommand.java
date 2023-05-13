package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.entity.EntityMetadataCommandHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

import java.util.List;

public class GlowCommand extends AbstractCommand {

    public static final EntityMetadataCommandHelper helper = new EntityMetadataCommandHelper(Entity::isGlowing, GlowCommand::setGlowing);

    public GlowCommand() {
        setName("glow");
        setSyntax("glow (<entity>|...) ({true}/false/toggle/reset) (for:<player>|...)");
        setRequiredArguments(0, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Glow
    // @Syntax glow (<entity>|...) ({true}/false/toggle/reset) (for:<player>|...)
    // @Required 0
    // @Maximum 3
    // @Short Sets whether an NPC or entity is glowing.
    // @Group entity
    //
    // @Description
    // Sets whether the specified entities glow, defaults to the linked player/NPC if none were specified.
    //
    // Optionally specify 'for:' with a list of players to fake the entities' glow state for these players.
    // When using 'toggle' with the 'for:' argument, the glow state will be toggled for each player separately.
    // If unspecified, will be set globally.
    // 'for:' players remain tracked even when offline/reconnecting, but are forgotten after server restart.
    //
    // When not using 'for:', the glow is global / on the real entity, which will persist in that entity's data until changed.
    //
    // To reset an entity's fake glow use the 'reset' state.
    // A reset is global by default, use the 'for:' argument to reset specific players.
    //
    // @Tags
    // <EntityTag.glowing>
    //
    // @Usage
    // Use to make the linked player (or NPC, if there isn't one) glow.
    // - glow
    //
    // @Usage
    // Use to toggle whether the linked NPC is glowing.
    // - glow <npc> toggle
    //
    // @Usage
    // Use to make an entity glow for specific players, without changing the way other players see it.
    // - glow <[entity]> for:<[player1]>|<[player2]>
    //
    // @Usage
    // Use to reset an entity's fake glow state for the linked player.
    // - glow <[entity]> reset for:<player>
    // -->

    public static void setGlowing(EntityTag entity, boolean glowing) {
        if (entity.isCitizensNPC()) {
            entity.getDenizenNPC().getCitizen().data().setPersistent(NPC.Metadata.GLOWING, glowing);
        }
        else {
            entity.getBukkitEntity().setGlowing(glowing);
        }
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("target") @ArgLinear @ArgDefaultNull @ArgSubType(EntityTag.class) List<EntityTag> targets,
                                   @ArgName("state") @ArgDefaultText("true") EntityMetadataCommandHelper.Action action,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers) {
        helper.execute(scriptEntry, targets, action, forPlayers);
    }
}
