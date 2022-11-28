package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.TriggerTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class TriggerCommand extends AbstractCommand {

    public TriggerCommand() {
        setName("trigger");
        setSyntax("trigger [name:<trigger>] (state:{toggle}/true/false) (cooldown:<duration>) (radius:<#>)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Trigger
    // @Syntax trigger [name:<trigger>] (state:{toggle}/true/false) (cooldown:<duration>) (radius:<#>)
    // @Required 1
    // @Maximum 4
    // @Plugin Citizens
    // @Short Enables or disables a trigger.
    // @Group npc
    // @Guide https://guide.denizenscript.com/guides/npcs/interact-scripts.html
    //
    // @Description
    // This command enables or disables an interact script trigger for the linked NPC.
    // This is generally meant to be used within the 'on assignment' action in an assignment script.
    // This might also be useful on timed activations or other special events (such as an NPC that "goes to bed" at the end of the day,
    // you might disable the proximity trigger that would otherwise normally show a greeting message).
    //
    // The "name" argument is required, and can have any supported trigger name.
    // The 4 triggers available by default are chat, click, damage, and proximity.
    // For more details of the available trigger types, refer to <@link language Interact Script Triggers>.
    //
    // The "state" argument can be 'true' (to enable it), 'false' (to disable it),
    // or unspecified to toggle it (that is, enable if it's currently off, or disable if it's currently on).
    //
    // You can specify the "cooldown" argument to set how long the trigger must wait
    // after any firing before it can be fired again.
    //
    // You can specify the "radius" argument to set how far away a player can be when activating it.
    // Note that the way this applies varies from trigger to trigger.
    // For the "chat" trigger, a large radius can be easily accidentally triggered by unrelated chatter.
    // For the "proximity" trigger, the radius argument should almost always be specified, as you generally want to control this with care.
    // For the "click" and "damage" trigger, the radius argument will be ignored.
    //
    // @Tags
    // <NPCTag.has_trigger[<trigger>]>
    //
    // @Usage
    // Use to enable the click trigger.
    // - trigger name:click state:true
    //
    // @Usage
    // Use to enable the chat trigger with a 10-second cooldown and a radius of 5 blocks.
    // - trigger name:chat state:true cooldown:10s radius:5
    //
    // @Usage
    // Use to disable the proximity trigger.
    // - trigger name:proximity state:false
    // -->

    private enum Toggle {TOGGLE, TRUE, FALSE}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add("name:click", "name:chat", "name:damage", "name:proximity");
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cooldown")
                    && arg.matchesPrefix("cooldown", "c")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("cooldown", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius", "r")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("radius", arg.asElement());
            }
            else if (!scriptEntry.hasObject("toggle")
                    && arg.matchesEnum(Toggle.class)) {
                scriptEntry.addObject("toggle", arg.asElement());
            }
            else if (!scriptEntry.hasObject("npc")
                    && arg.matchesArgumentType(NPCTag.class)) {
                scriptEntry.addObject("npc", arg.asType(NPCTag.class));
            }
            else if (!scriptEntry.hasObject("trigger")) {
                scriptEntry.addObject("trigger", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("trigger")) {
            throw new InvalidArgumentsException("Missing name argument!");
        }
        if (!scriptEntry.hasObject("toggle")) {
            scriptEntry.addObject("toggle", new ElementTag("TOGGLE"));
        }
        if (!Utilities.entryHasNPC(scriptEntry) && !scriptEntry.hasObject("npc")) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag toggle = scriptEntry.getElement("toggle");
        ElementTag trigger = scriptEntry.getElement("trigger");
        ElementTag radius = scriptEntry.getElement("radius");
        DurationTag cooldown = scriptEntry.getObjectTag("cooldown");
        NPCTag npc = scriptEntry.hasObject("npc") ? (NPCTag) scriptEntry.getObject("npc") : Utilities.getEntryNPC(scriptEntry);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), trigger, toggle, radius, cooldown, npc);
        }
        // Add trigger trait
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) {
            npc.getCitizen().addTrait(TriggerTrait.class);
        }
        TriggerTrait trait = npc.getCitizen().getOrAddTrait(TriggerTrait.class);
        if (!trait.triggerNameIsValid(trigger.asString())) {
            throw new InvalidArgumentsRuntimeException("Invalid trigger name '" + trigger + "' - are you sure you spelled it right?");
        }
        switch (Toggle.valueOf(toggle.asString().toUpperCase())) {
            case TOGGLE:
                trait.toggleTrigger(trigger.asString());
                break;
            case TRUE:
                trait.toggleTrigger(trigger.asString(), true);
                break;
            case FALSE:
                trait.toggleTrigger(trigger.asString(), false);
                break;
        }
        if (radius != null) {
            trait.setLocalRadius(trigger.asString(), radius.asInt());
        }
        if (cooldown != null) {
            trait.setLocalCooldown(trigger.asString(), cooldown.getSeconds());
        }
    }
}
