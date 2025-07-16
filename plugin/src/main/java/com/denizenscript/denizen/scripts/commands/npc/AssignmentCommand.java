package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizen.npc.traits.AssignmentTrait;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;

import java.util.List;

public class AssignmentCommand extends AbstractCommand {

    public AssignmentCommand() {
        setName("assignment");
        setSyntax("assignment [set/add/remove/clear] (script:<name>) (to:<npc>|...)");
        setRequiredArguments(1, 3);
        autoCompile();
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
    // - assignment remove script:name_fix_assign
    // -->

    public enum Action { SET, ADD, REMOVE, CLEAR }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(Action.values());
        tab.addScriptsOfType(AssignmentScriptContainer.class);
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") Action action,
                                   @ArgName("linearScript") @ArgLinear @ArgDefaultNull ScriptTag linearScript,
                                   @ArgName("linearTo") @ArgLinear @ArgDefaultNull @ArgSubType(NPCTag.class) List<NPCTag> linearTo,
                                   @ArgName("script") @ArgPrefixed @ArgDefaultNull ScriptTag script,
                                   @ArgName("to") @ArgPrefixed @ArgDefaultNull @ArgSubType(NPCTag.class) List<NPCTag> to) {
        PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
        if (linearScript != null) {
            BukkitImplDeprecations.assignmentOptionalPrefixArgs.warn(scriptEntry);
            script = linearScript;
        }
        if (linearTo != null) {
            BukkitImplDeprecations.assignmentOptionalPrefixArgs.warn(scriptEntry);
            to = linearTo;
        }
        if (to == null) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsRuntimeException("This command requires a linked NPC!");
            }
            to = List.of(Utilities.getEntryNPC(scriptEntry));
        }
        switch (action) {
            case SET -> {
                if (script == null) {
                    throw new InvalidArgumentsRuntimeException("Missing script!");
                }
                if (!(script.getContainer() instanceof AssignmentScriptContainer assignmentScriptContainer)) {
                    throw new InvalidArgumentsRuntimeException("Script specified is not an 'assignment-type' container.");
                }
                for (NPCTag npc : to) {
                    AssignmentTrait assignment = npc.getCitizen().getOrAddTrait(AssignmentTrait.class);
                    assignment.clearAssignments(player);
                    assignment.addAssignmentScript(assignmentScriptContainer, player);
                }
            }
            case ADD -> {
                if (script == null) {
                    throw new InvalidArgumentsRuntimeException("Missing script!");
                }
                if (!(script.getContainer() instanceof AssignmentScriptContainer assignmentScriptContainer)) {
                    throw new InvalidArgumentsRuntimeException("Script specified is not an 'assignment-type' container.");
                }
                for (NPCTag npc : to) {
                    npc.getCitizen().getOrAddTrait(AssignmentTrait.class).addAssignmentScript(assignmentScriptContainer, player);
                }
            }
            case REMOVE -> {
                for (NPCTag npc : to) {
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
                }
            }
            case CLEAR -> {
                for (NPCTag npc : to) {
                    if (npc.getCitizen().hasTrait(AssignmentTrait.class)) {
                        npc.getCitizen().getOrAddTrait(AssignmentTrait.class).clearAssignments(player);
                        npc.getCitizen().removeTrait(AssignmentTrait.class);
                    }
                }
            }
        }
    }
}
