package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;

import java.util.Collections;
import java.util.List;

public class AssignmentCommand extends AbstractCommand {

    public AssignmentCommand() {
        setName("assignment");
        setSyntax("assignment [set/add/remove/clear] (script:<name>) (to:<npc>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Assignment
    // @Syntax assignment [set/add/remove/clear] (script:<name>) (to:<npc>|...)
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
    // 'Set' is equivalent to 'clear' + 'add'.
    //
    // @Tags
    // <NPCTag.script>
    // <server.npcs_assigned[<assignment_script>]>
    //
    // @Usage
    // Use to assign an npc with exactly one assignment script named 'Bob_the_Builder'.
    // - assignment set script:Bob_the_Builder
    //
    // @Usage
    // Use to give a different NPC an assignment.
    // - assignment set script:Bob_the_Builder npc:<[some_npc]>
    //
    // @Usage
    // Use to clear an npc's assignments.
    // - assignment clear
    //
    // @Usage
    // Use to add an extra assignment to the NPC.
    // - assignment add script:name_fix_assign
    //
    // @Usage
    // Use to remove an extra assignment from the NPC.
    // - assignment add script:name_fix_assign
    // -->

    private enum Action {SET, ADD, REMOVE, CLEAR}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add("set", "add", "remove", "clear");
        tab.addScriptsOfType(AssignmentScriptContainer.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesEnum(Action.class)
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
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ScriptTag script = scriptEntry.getObjectTag("script");
        Action action = (Action) scriptEntry.getObject("action");
        List<NPCTag> npcs = (List<NPCTag>) scriptEntry.getObject("npcs");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("action", action), script, db("npc", npcs));
        }
        PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
        for (NPCTag npc : npcs) {
            switch (action) {
                case SET: {
                    if (script == null) {
                        throw new InvalidArgumentsRuntimeException("Missing script!");
                    }
                    AssignmentTrait assignment = npc.getCitizen().getOrAddTrait(AssignmentTrait.class);
                    assignment.clearAssignments(player);
                    assignment.addAssignmentScript((AssignmentScriptContainer) script.getContainer(), player);
                    break;
                }
                case ADD:
                    if (script == null) {
                        throw new InvalidArgumentsRuntimeException("Missing script!");
                    }
                    npc.getCitizen().getOrAddTrait(AssignmentTrait.class).addAssignmentScript((AssignmentScriptContainer) script.getContainer(), player);
                    break;
                case REMOVE:
                    if (script == null) {
                        BukkitImplDeprecations.assignmentRemove.warn(scriptEntry);
                        if (npc.getCitizen().hasTrait(AssignmentTrait.class)) {
                            npc.getCitizen().getOrAddTrait(AssignmentTrait.class).clearAssignments(player);
                            npc.getCitizen().removeTrait(AssignmentTrait.class);
                        }
                    }
                    else {
                        if (npc.getCitizen().hasTrait(AssignmentTrait.class)) {
                            AssignmentTrait trait = npc.getCitizen().getOrAddTrait(AssignmentTrait.class);
                            trait.removeAssignmentScript(script.getName(), player);
                            trait.checkAutoRemove();
                        }
                    }
                    break;
                case CLEAR:
                    if (npc.getCitizen().hasTrait(AssignmentTrait.class)) {
                        npc.getCitizen().getOrAddTrait(AssignmentTrait.class).clearAssignments(player);
                        npc.getCitizen().removeTrait(AssignmentTrait.class);
                    }
                    break;
            }
        }
    }
}
