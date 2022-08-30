package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.scripts.commands.core.CooldownCommand;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.core.TimeTag;

public class BukkitScriptExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <ScriptTag.cooled_down[(<player>)]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the script is currently cooled down for the player. Any global
        // cooldown present on the script will also be taken into account. Not specifying a player will result in
        // using the attached player available in the script entry. Not having a valid player will result in 'null'.
        // -->
        ScriptTag.tagProcessor.registerTag(ElementTag.class, "cooled_down", (attribute, script) -> {
            PlayerTag player = (attribute.hasParam() ? attribute.paramAsType(PlayerTag.class)
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            if (player != null && player.isValid()) {
                return new ElementTag(CooldownCommand.checkCooldown(player, script.getContainer().getName()));
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <ScriptTag.cooldown[(<player>)]>
        // @returns DurationTag
        // @description
        // Returns the time left for the player to cooldown for the script.
        // -->
        ScriptTag.tagProcessor.registerTag(DurationTag.class, "cooldown", (attribute, script) -> {
            PlayerTag player = (attribute.hasParam() ? attribute.paramAsType(PlayerTag.class)
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            return CooldownCommand.getCooldownDuration(player, script.getName());
        });

        // <--[tag]
        // @attribute <ScriptTag.step[(<player>)]>
        // @returns ElementTag
        // @description
        // Returns the name of a script step that the player is currently on.
        // Must be an INTERACT script.
        // -->
        ScriptTag.tagProcessor.registerTag(ElementTag.class, "step", (attribute, script) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer();
            if (player != null && player.isValid()) {
                return new ElementTag(InteractScriptHelper.getCurrentStep(player, script.getContainer().getName()));
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <ScriptTag.step_expiration[(<player>)]>
        // @returns TimeTag
        // @description
        // Returns the time that an interact script step expires at, if applied by <@link command zap> with a duration limit.
        // -->
        ScriptTag.tagProcessor.registerTag(TimeTag.class, "step_expiration", (attribute, script) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer();
            if (player != null && player.isValid()) {
                return InteractScriptHelper.getStepExpiration(player, script.getContainer().getName());
            }
            else {
                return null;
            }
        });

        // <--[tag]
        // @attribute <ScriptTag.default_step>
        // @returns ElementTag
        // @description
        // Returns the name of the default step of an interact script.
        // -->
        ScriptTag.tagProcessor.registerStaticTag(ElementTag.class, "default_step", (attribute, script) -> {
            String step = ((InteractScriptContainer) script.getContainer()).getDefaultStepName();
            return new ElementTag(step);
        });
    }
}
