package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AssignmentCommand extends AbstractCommand {

    public AssignmentCommand() {
        setName("assignment");
        setSyntax("assignment [set/remove] (script:<name>) (to:<npc>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Assignment
    // @Syntax assignment [set/remove] (script:<name>) (to:<npc>|...)
    // @Required 1
    // @Maximum 3
    // @Plugin Citizens
    // @Short Changes an NPC's assignment.
    // @Group npc
    // @Guide https://guide.denizenscript.com/guides/npcs/assignment-scripts.html
    //
    // @Description
    // Changes an NPC's assignment as though you used the '/npc assignment' command.
    //
    // Uses the script: argument, which accepts an assignment-type script.
    //
    // Optionally, specify a list of NPCs to apply the trait to. If unspecified, the linked NPC will be used.
    //
    // @Tags
    // <NPCTag.script>
    // <server.npcs_assigned[<assignment_script>]>
    //
    // @Usage
    // Use to assign an npc with an assignment script named 'Bob_the_Builder'.
    // - assignment set script:Bob_the_Builder
    //
    // @Usage
    // Use to give a different NPC an assignment.
    // - assignment set script:Bob_the_Builder npc:<[some_npc]>
    //
    // @Usage
    // Use to remove an npc's assignment.
    // - assignment remove
    // -->

    private enum Action {SET, REMOVE}

    @Override
    public void addCustomTabCompletions(String arg, Consumer<String> addOne) {
        for (ScriptContainer script : ScriptRegistry.scriptContainers.values()) {
            if (script instanceof AssignmentScriptContainer) {
                addOne.accept(script.getName());
            }
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesEnum(Action.values())
                    && !scriptEntry.hasObject("action")) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }
            else if (arg.matchesArgumentType(ScriptTag.class)
                    && !scriptEntry.hasObject("script")) {
                // Check the type of script.. it must be an assignment-type container
                if (arg.asType(ScriptTag.class) != null
                        && arg.asType(ScriptTag.class).getContainer() instanceof AssignmentScriptContainer) {
                    scriptEntry.addObject("script", arg.asType(ScriptTag.class));
                }
                else {
                    throw new InvalidArgumentsException("Script specified is not an 'assignment-type' container.");
                }
            }
            else if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(NPCTag.class)) {
                scriptEntry.addObject("npcs", arg.asType(ListTag.class).filter(NPCTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("npcs")) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsException("This command requires a linked NPC!");
            }
            scriptEntry.addObject("npcs", Collections.singletonList(Utilities.getEntryNPC(scriptEntry)));
        }
        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify an action!");
        }
        if (scriptEntry.getObject("action").equals(Action.SET) && !scriptEntry.hasObject("script")) {
            throw new InvalidArgumentsException("Script specified was missing or invalid.");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ScriptTag script = scriptEntry.getObjectTag("script");
        Action action = (Action) scriptEntry.getObject("action");
        List<NPCTag> npcs = (List<NPCTag>) scriptEntry.getObject("npcs");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("action", action), script, db("npc", npcs));
        }
        for (NPCTag npc : npcs) {
            if (action.equals(Action.SET)) {
                npc.getCitizen().getOrAddTrait(AssignmentTrait.class).setAssignment(script.getName(), Utilities.getEntryPlayer(scriptEntry));
            }
            else if (action.equals(Action.REMOVE)) {
                npc.getCitizen().getOrAddTrait(AssignmentTrait.class).removeAssignment(Utilities.getEntryPlayer(scriptEntry));
                npc.getCitizen().removeTrait(AssignmentTrait.class);
            }
        }
    }
}
