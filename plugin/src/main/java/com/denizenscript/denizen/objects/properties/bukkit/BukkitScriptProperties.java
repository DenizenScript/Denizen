package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.scripts.commands.core.CooldownCommand;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

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

    public static final String[] handledTags = new String[] {
            "cooled_down", "cooldown", "step"
    };

    public static final String[] handledMechs = new String[] {
    }; // None

    private BukkitScriptProperties(ScriptTag script) {
        this.script = script;
    }

    ScriptTag script;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ScriptTag.cooled_down[<player>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the script is currently cooled down for the player. Any global
        // cooldown present on the script will also be taken into account. Not specifying a player will result in
        // using the attached player available in the script entry. Not having a valid player will result in 'null'.
        // -->
        if (attribute.startsWith("cooled_down")) {
            PlayerTag player = (attribute.hasContext(1) ? PlayerTag.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            if (player != null && player.isValid()) {
                return new ElementTag(CooldownCommand.checkCooldown(player, script.getContainer().getName()))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <ScriptTag.cooldown[<player>]>
        // @returns DurationTag
        // @description
        // Returns the time left for the player to cooldown for the script.
        // -->
        if (attribute.startsWith("cooldown")) {
            PlayerTag player = (attribute.hasContext(1) ? PlayerTag.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            return CooldownCommand.getCooldownDuration(player, script.getName())
                    .getAttribute(attribute.fulfill(1));

        }


        // <--[tag]
        // @attribute <ScriptTag.step[<player>]>
        // @returns ElementTag
        // @description
        // Returns the name of a script step that the player is currently on.
        // Must be an INTERACT script.
        // -->
        if (attribute.startsWith("step")) {
            PlayerTag player = (attribute.hasContext(1) ? PlayerTag.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());

            if (player != null && player.isValid()) {
                return new ElementTag(InteractScriptHelper.getCurrentStep(player, script.getContainer().getName()))
                        .getAttribute(attribute.fulfill(1));
            }
        }
        return null;
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
