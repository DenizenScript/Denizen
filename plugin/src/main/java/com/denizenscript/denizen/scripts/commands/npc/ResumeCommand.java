package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;

public class ResumeCommand extends AbstractCommand {

    public ResumeCommand() {
        setName("resume");
        setSyntax("resume [waypoints/activity] (<duration>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Resume
    // @Syntax resume [waypoints/activity] (<duration>)
    // @Required 1
    // @Plugin Citizens
    // @Short Resumes an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // @Group npc
    //
    // @Description
    // The resume command resumes an NPC's waypoint navigation or goal activity temporarily or indefinitely.
    // This works along side <@link command pause>.
    // See the documentation of the pause command for more details.
    //
    // @Tags
    // <NPCTag.is_navigating>
    //
    // @Usage
    // Use to pause an NPC's waypoint navigation and then resume it.
    // - pause waypoints
    // - resume waypoints
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") PauseCommand.Type type,
                                   @ArgName("duration") @ArgLinear @ArgDefaultNull DurationTag duration) {
        PauseCommand.executeToggle(scriptEntry, type, duration, false);
    }
}
