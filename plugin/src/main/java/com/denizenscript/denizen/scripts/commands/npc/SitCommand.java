package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.trait.SitTrait;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;

public class SitCommand extends AbstractCommand {

    public SitCommand() {
        setName("sit");
        setSyntax("sit (<location>)");
        setRequiredArguments(0, 1);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Sit
    // @Syntax sit (<location>)
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Causes the NPC to sit. To make them stand, see <@link command Stand>.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC sit at the specified location.
    // Use <@link command Stand> to make the NPC stand up again.
    //
    // @Tags
    // <NPCTag.is_sitting>
    //
    // @Usage
    // Make the linked NPC sit at the player's cursor location.
    // - sit <player.cursor_on>
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("location") @ArgLinear @ArgDefaultNull LocationTag location) {
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsRuntimeException("This command requires a linked NPC!");
        }
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (npc.getEntity() instanceof Sittable sittable) {
            sittable.setSitting(true);
        }
        else if (npc.getEntity() instanceof Player) {
            SitTrait trait = npc.getCitizen().getOrAddTrait(SitTrait.class);
            if (location != null) {
                trait.setSitting(location);
            }
            else {
                trait.setSitting(npc.getLocation());
            }
        }
        else {
            Debug.echoError("Entities of type " + npc.getEntityType() + " cannot sit.");
        }
    }
}
