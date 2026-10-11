package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.entity.Player;

import java.util.List;

public class PostEffectCommand extends AbstractCommand {

    public PostEffectCommand() {
        setName("posteffect");
        setSyntax("posteffect [{add}/remove/set/clear] (effects:<effect>|...)");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Posteffect
    // @Syntax posteffect [{add}/remove/set/clear] (effects:<effect>|...)
    // @Required 1
    // @Maximum 2
    // @Short Controls the posteffects on a player.
    // @Group player
    //
    // @Description
    // Post-processing effects are not traditional potion effects, but are used to add a layer of effect to a player's view.
    // These posteffects can be layered, allowing for a combination of different views.
    //
    // Posteffects being added, removed, or set should be in "Namespace:Key" format.
    // If no Namespace is specified, it will be assumed to be 'MINECRAFT:'.
    // Setting the posteffects will remove all other posteffects from the player.
    //
    // These effects can also be cleared, with no additional input required.
    //
    // @Tags
    // <PlayerTag.post_effects>
    //
    // @Usage
    // Use to add the "blur" posteffect to a player.
    // - posteffect add effects:minecraft:blur
    //
    // @Usage
    // Use to remove the default Minecraft "entity_outline" posteffect and a custom "scary" posteffect from a player.
    // - posteffect remove effects:entity_outline|my_resource_pack:scary
    //
    // @Usage
    // Use to clear the posteffects on a player.
    // - posteffect clear
    // -->

    public enum Action { ADD, REMOVE, SET, CLEAR }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgLinear @ArgDefaultText("ADD") Action action,
                                   @ArgName("effects") @ArgPrefixed @ArgDefaultNull @ArgSubType(ElementTag.class) List<ElementTag> effects) {
        if (!Utilities.entryHasPlayer(scriptEntry)) {
            throw new InvalidArgumentsRuntimeException("Must specify a valid player Target!");
        }
        if (action != Action.CLEAR && effects == null) {
            throw new InvalidArgumentsRuntimeException("Missing 'effects' argument!");
        }
        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
        switch (action) {
            case ADD -> PaperAPITools.instance.addPostEffects(player, effects);
            case REMOVE -> PaperAPITools.instance.removePostEffects(player, effects);
            case SET -> {
                PaperAPITools.instance.clearPostEffects(player);
                PaperAPITools.instance.addPostEffects(player, effects);
            }
            case CLEAR -> PaperAPITools.instance.clearPostEffects(player);
        }
    }
}
