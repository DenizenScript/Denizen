package com.denizenscript.denizen.paper.commands;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.util.List;

public class PostEffectCommand extends AbstractCommand {

    public PostEffectCommand() {
        setName("posteffect");
        setSyntax("posteffect [add/remove/set/clear] (effects:<effect>|...) (targets:<player>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Posteffect
    // @Syntax posteffect [add/remove/set/clear] (effects:<effect>|...) (targets:<player>|...)
    // @Required 1
    // @Maximum 3
    // @Short Controls the posteffects on a player.
    // @Group paper
    // @Plugin Paper
    //
    // @Description
    // Post-processing effects are not traditional potion effects, but are used to add a layer of effect to a player's view.
    // These posteffects can be layered, allowing for a combination of different views.
    //
    // A list of default effects can be found at <@link url https://minecraft.wiki/w/Shader#Post-processing_effects>.
    //
    // Posteffects being added, removed, or set should be in "Namespace:Key" format.
    // If no Namespace is specified, it will be assumed to be 'MINECRAFT:'.
    // Setting the posteffects will remove all other posteffects from the player.
    //
    // These effects can also be cleared, with no additional input required.
    //
    // If no targets are specified, the linked player will be used.
    //
    // @Tags
    // <PlayerTag.post_effects>
    //
    // @Usage
    // Use to add the "blur" posteffect to the linked player.
    // - posteffect add effects:minecraft:blur
    //
    // @Usage
    // Use to remove the default Minecraft "entity_outline" posteffect and a custom "scary" posteffect from the linked player.
    // - posteffect remove effects:entity_outline|my_resource_pack:scary
    //
    // @Usage
    // Use to clear the posteffects on a list of players.
    // - posteffect clear targets:<[players]>
    // -->

    public enum Action { ADD, REMOVE, SET, CLEAR }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") @ArgLinear Action action,
                                   @ArgName("effects") @ArgPrefixed @ArgDefaultNull @ArgSubType(ElementTag.class) List<ElementTag> effects,
                                   @ArgName("targets") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> targets) {
        if (action != Action.CLEAR && effects == null) {
            throw new InvalidArgumentsRuntimeException("Missing 'effects' argument!");
        }
        if (targets == null) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsRuntimeException("Must specify a valid player Target!");
            }
            targets = List.of(Utilities.getEntryPlayer(scriptEntry));
        }
        Player player;
        switch (action) {
            case ADD -> {
                for (PlayerTag playerTag : targets) {
                    player = playerTag.getPlayerEntity();
                    for (ElementTag effect : effects) {
                        player.postEffects().add(Key.key(effect.asString(), ':'));
                    }
                }
            }
            case REMOVE -> {
                for (PlayerTag playerTag : targets) {
                    player = playerTag.getPlayerEntity();
                    for (ElementTag effect : effects) {
                        player.postEffects().remove(Key.key(effect.asString(), ':'));
                    }
                }
            }
            case SET -> {
                for (PlayerTag playerTag : targets) {
                    player = playerTag.getPlayerEntity();
                    player.postEffects().clear();
                    for (ElementTag effect : effects) {
                        player.postEffects().add(Key.key(effect.asString(), ':'));
                    }
                }
            }
            case CLEAR -> {
                for (PlayerTag playerTag : targets) {
                    playerTag.getPlayerEntity().postEffects().clear();
                }
            }
        }
    }
}
