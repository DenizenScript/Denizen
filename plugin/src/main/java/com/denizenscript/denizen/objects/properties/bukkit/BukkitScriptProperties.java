package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.scripts.commands.core.CooldownCommand;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class BukkitScriptProperties implements Property {

    public static boolean describes(ObjectTag script) {
        return script instanceof ScriptTag;
    }

    public static BukkitScriptProperties getFrom(ObjectTag script) {
        if (!describes(script)) {
            return null;
        }
        else {
            return new BukkitScriptProperties((ScriptTag) script);
        }
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    private BukkitScriptProperties(ScriptTag script) {
        this.script = script;
    }

    ScriptTag script;

    public static void registerTags() {

        // <--[tag]
        // @attribute <ScriptTag.cooled_down[(<player>)]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the script is currently cooled down for the player. Any global
        // cooldown present on the script will also be taken into account. Not specifying a player will result in
        // using the attached player available in the script entry. Not having a valid player will result in 'null'.
        // -->
        PropertyParser.registerTag(BukkitScriptProperties.class, ElementTag.class, "cooled_down", (attribute, script) -> {
            PlayerTag player = (attribute.hasParam() ? attribute.paramAsType(PlayerTag.class)
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            if (player != null && player.isValid()) {
                return new ElementTag(CooldownCommand.checkCooldown(player, script.script.getContainer().getName()));
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
        PropertyParser.registerTag(BukkitScriptProperties.class, DurationTag.class, "cooldown", (attribute, script) -> {
            PlayerTag player = (attribute.hasParam() ? attribute.paramAsType(PlayerTag.class)
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            return CooldownCommand.getCooldownDuration(player, script.script.getName());
        });

        // <--[tag]
        // @attribute <ScriptTag.step[(<player>)]>
        // @returns ElementTag
        // @description
        // Returns the name of a script step that the player is currently on.
        // Must be an INTERACT script.
        // -->
        PropertyParser.registerTag(BukkitScriptProperties.class, ElementTag.class, "step", (attribute, script) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer();
            if (player != null && player.isValid()) {
                return new ElementTag(InteractScriptHelper.getCurrentStep(player, script.script.getContainer().getName()));
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
        PropertyParser.registerTag(BukkitScriptProperties.class, TimeTag.class, "step_expiration", (attribute, script) -> {
            PlayerTag player = attribute.hasParam() ? attribute.paramAsType(PlayerTag.class) : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer();
            if (player != null && player.isValid()) {
                return InteractScriptHelper.getStepExpiration(player, script.script.getContainer().getName());
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
        PropertyParser.registerStaticTag(BukkitScriptProperties.class, ElementTag.class, "default_step", (attribute, script) -> {
            String step = ((InteractScriptContainer) script.script.getContainer()).getDefaultStepName();
            return new ElementTag(step);
        });
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "BukkitScriptProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }
}
