package net.aufdemrand.denizen.objects.properties.bukkit;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class BukkitScriptProperties implements Property {

    public static boolean describes(dObject script) {
        return script instanceof dScript;
    }

    public static BukkitScriptProperties getFrom(dObject script) {
        if (!describes(script)) return null;
        else return new BukkitScriptProperties((dScript) script);
    }


    private BukkitScriptProperties(dScript script) {
        this.script = script;
    }

    dScript script;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <s@script.cooled_down[<player>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the script is currently cooled down for the player. Any global
        // cooldown present on the script will also be taken into account. Not specifying a player will result in
        // using the attached player available in the script entry. Not having a valid player will result in 'null'.
        // -->
        if (attribute.startsWith("cooled_down")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            if (player != null && player.isValid())
                return new Element(CooldownCommand.checkCooldown(player, script.getContainer().getName()))
                        .getAttribute(attribute.fulfill(1));
            else return "null";
        }

        // <--[tag]
        // @attribute <s@script.requirements[<player>].check[<path>]>
        // @returns Element
        // @description
        // Returns whether the player specified (defaults to current) has the requirement.
        // Must be an INTERACT script.
        // -->
        if (attribute.startsWith("requirements.check")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            if (attribute.hasContext(2))
                return new Element(((InteractScriptContainer) script.getContainer()).checkRequirements(player,
                        ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC(),
                        attribute.getContext(2)))
                        .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <s@script.cooldown[<player>]>
        // @returns Duration
        // @description
        // Returns the time left for the player to cooldown for the script.
        // -->
        if (attribute.startsWith("cooldown")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());
            return CooldownCommand.getCooldownDuration(player, script.getName())
                    .getAttribute(attribute.fulfill(1));

        }


        // <--[tag]
        // @attribute <s@script.step[<player>]>
        // @returns Element
        // @description
        // Returns the name of a script step that the player is currently on.
        // Must be an INTERACT script.
        // -->
        if (attribute.startsWith("step")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer());

            if (player != null && player.isValid())
                return new Element(InteractScriptHelper.getCurrentStep(player, script.getContainer().getName()))
                        .getAttribute(attribute.fulfill(1));
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
