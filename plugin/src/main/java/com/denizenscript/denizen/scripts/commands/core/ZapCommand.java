package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.core.TimeTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.Listener;

public class ZapCommand extends AbstractCommand implements Listener {

    public ZapCommand() {
        setName("zap");
        setSyntax("zap (<script>) [<step>] (<duration>)");
        setRequiredArguments(0, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Zap
    // @Syntax zap (<script>) [<step>] (<duration>)
    // @Required 0
    // @Maximum 3
    // @Short Changes the current interact script step.
    // @Synonyms Step
    // @Group core
    // @Guide https://guide.denizenscript.com/guides/npcs/interact-scripts.html
    //
    // @Description
    // Changes the current interact script step for the linked player.
    //
    // The step name input should match the name of a step in the interact script.
    // The step name can be '*' to automatically zap to the default step.
    //
    // If used inside an interact script, will default to the current interact script.
    // If used elsewhere, but there is a linked NPC with an assignment and interact, that NPC's interact script will be used.
    // For anywhere else, you must specify the script by name.
    //
    // Optionally specify a duration. When the duration is up, the script will zap back to the step it was previously on.
    // If any zap commands are used during the duration, that duration will be discarded.
    //
    // The command's name was inspired by a command in the language "ZZT-OOP", from a 1991 DOS game enjoyed by the original developer of Denizen.
    //
    // @Tags
    // <ScriptTag.step[<player>]>
    //
    // @Usage
    // Use to change the step to 2.
    // - zap 2
    //
    // @Usage
    // Use to return to the default step.
    // - zap *
    //
    // @Usage
    // Use to change the step to 3 in a script called Interact_Example.
    // - zap 3 Interact_Example
    //
    // @Usage
    // Use to change the step to 1 for the defined player in a script called InteractScript.
    // - zap 1 InteractScript player:<[player]>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addScriptsOfType(InteractScriptContainer.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("script")
                    && !scriptEntry.hasObject("step")
                    && arg.hasPrefix()
                    && arg.getPrefix().matchesArgumentType(ScriptTag.class)) {
                BukkitImplDeprecations.zapPrefix.warn(scriptEntry);
                scriptEntry.addObject("script", arg.getPrefix().asType(ScriptTag.class));
                scriptEntry.addObject("step", arg.asElement());
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(ScriptTag.class)
                    && arg.asType(ScriptTag.class).getContainer() instanceof InteractScriptContainer
                    && arg.limitToOnlyPrefix("script")) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (!scriptEntry.hasObject("step")
                    && arg.limitToOnlyPrefix("step")) {
                scriptEntry.addObject("step", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)
                    && arg.limitToOnlyPrefix("duration")) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
        if (player == null || !player.isValid()) {
            throw new InvalidArgumentsException("Must have player context!");
        }
        if (!scriptEntry.hasObject("script")) {
            ScriptTag script = scriptEntry.getScript();
            if (script != null) {
                if (script.getContainer() instanceof InteractScriptContainer) {
                    scriptEntry.addObject("script", script);
                }
                else if (script.getContainer() instanceof AssignmentScriptContainer) {
                    InteractScriptContainer interact = ((AssignmentScriptContainer) script.getContainer()).interact;
                    if (interact != null) {
                        scriptEntry.addObject("script", new ScriptTag(interact));
                    }
                }
            }
            if (!scriptEntry.hasObject("script")) {
                NPCTag npc = Utilities.getEntryNPC(scriptEntry);
                if (npc != null && npc.getCitizen().hasTrait(AssignmentTrait.class)) {
                    AssignmentTrait trait = npc.getCitizen().getOrAddTrait(AssignmentTrait.class);
                    for (AssignmentScriptContainer container : trait.containerCache) {
                        if (container != null && container.getInteract() != null) {
                            scriptEntry.addObject("script", new ScriptTag(container.getInteract()));
                            break;
                        }
                    }
                }
            }
            if (!scriptEntry.hasObject("script")) {
                throw new InvalidArgumentsException("No script to zap! Must be in an interact script, or have a linked NPC with an associated interact script.");
            }
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final ScriptTag script = scriptEntry.getObjectTag("script");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        ElementTag stepElement = scriptEntry.getElement("step");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), Utilities.getEntryPlayer(scriptEntry), script, stepElement != null ? stepElement : db("step", "++ (inc)"), duration);
        }
        String step = stepElement == null ? null : stepElement.asString();
        String currentStep = InteractScriptHelper.getCurrentStep(Utilities.getEntryPlayer(scriptEntry), script.getName());
        // Special-case for backwards compatibility: ability to use ZAP to count up steps.
        if (step == null) {
            // Okay, no step was identified.. that means we should count up,
            // ie. if currentStep = 1, new step should = 2
            // If the currentStep is a number, increment it. If not, set it
            // to '1' so it can be incremented next time.
            if (ArgumentHelper.matchesInteger(currentStep)) {
                step = String.valueOf(Integer.parseInt(currentStep) + 1);
            }
            else {
                step = "1";
            }
        }
        else if (step.equals("*")) {
            step = ((InteractScriptContainer) script.getContainer()).getDefaultStepName();
        }
        if (step.equalsIgnoreCase(currentStep)) {
            Debug.echoError(scriptEntry, "Zapping to own current step!");
            return;
        }
        TimeTag expiration = null;
        if (duration != null && duration.getSeconds() > 0) {
            expiration = new TimeTag(TimeTag.now().millis() + duration.getMillis());
        }
        Utilities.getEntryPlayer(scriptEntry).getFlagTracker().setFlag("__interact_step." + script.getName(), new ElementTag(step), expiration);
    }
}
