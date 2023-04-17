package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.entity.EntityMetadataCommandHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

import java.util.List;

public class GlowCommand extends AbstractCommand {

    public static final EntityMetadataCommandHelper helper = new EntityMetadataCommandHelper(GlowCommand::isGlowing, GlowCommand::setGlowing);

    public GlowCommand() {
        setName("glow");
        setSyntax("glow (<entity>|...) (state:true/false/toggle/reset) (for:<player>|...)");
        setRequiredArguments(0, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Glow
    // @Syntax glow (<entity>|...) (state:true/false/toggle/reset) (for:<player>|...)
    // @Required 0
    // @Maximum 3
    // @Short Makes the linked player see the chosen entities as glowing.
    // @Group player
    //
    // @Description
    // Makes the linked player see the chosen entities as glowing.
    // BE WARNED, THIS COMMAND IS HIGHLY EXPERIMENTAL AND MAY NOT WORK AS EXPECTED.
    // This command works by globally enabling the glow effect, then whitelisting who is allowed to see it.
    //
    // THIS COMMAND IS UNSTABLE AND IS SUBJECT TO BEING REWRITTEN IN THE NEAR FUTURE.
    //
    // @Tags
    // <EntityTag.glowing>
    //
    // @Usage
    // Use to make the player's target glow.
    // - glow <player.target>
    //
    // @Usage
    // Use to make the player's target not glow.
    // - glow <player.target> false
    // -->


    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        helper.tabComplete(tab);
    }

    public static boolean isGlowing(Entity entity) {
        if (EntityTag.isCitizensNPC(entity)) {
            return NPCTag.fromEntity(entity).getCitizen().data().get(NPC.Metadata.GLOWING);
        }
        return entity.isGlowing();
    }

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
                                   @ArgName("state") @ArgDefaultText("TRUE") EntityMetadataCommandHelper.Action action,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers) {
        helper.execute(scriptEntry, targets, action, forPlayers);
    }
}
