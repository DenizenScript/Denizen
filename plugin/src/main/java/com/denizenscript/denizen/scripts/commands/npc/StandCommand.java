package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.npc.traits.SittingTrait;
import com.denizenscript.denizen.npc.traits.SleepingTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SitTrait;
import net.citizensnpcs.trait.SleepTrait;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Villager;

public class StandCommand extends AbstractCommand {

    public StandCommand() {
        setName("stand");
        setSyntax("stand");
        setRequiredArguments(0, 0);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Stand
    // @Syntax stand
    // @Required 0
    // @Maximum 0
    // @Plugin Citizens
    // @Short Causes the NPC to stand up from sitting or sleeping.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC stop sitting or sleeping.
    // To make them sit, see <@link command Sit>.
    // To make them sleep, see <@link command Sleep>.
    //
    // @Tags
    // None
    //
    // @Usage
    // Make the linked NPC stand up.
    // - stand
    //
    // -->

    public static void autoExecute(ScriptEntry scriptEntry) {
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsRuntimeException("This command requires a linked NPC!");
        }
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (!(npc.getEntity() instanceof Player || npc.getEntity() instanceof Sittable || npc.getEntity() instanceof Villager)) {
            Debug.echoError("Entities of type " + npc.getEntityType() + " cannot sit or sleep.");
            return;
        }
        if (npc.getEntity() instanceof Sittable sittable) {
            sittable.setSitting(false);
            return;
        }
        NPC citizen = npc.getCitizen();
        if (citizen.hasTrait(SittingTrait.class)) {
            citizen.getOrAddTrait(SittingTrait.class).stand();
            citizen.removeTrait(SittingTrait.class);
        }
        if (citizen.hasTrait(SitTrait.class)) {
            citizen.getOrAddTrait(SitTrait.class).setSitting(null);
        }
        if (citizen.hasTrait(SleepingTrait.class)) {
            citizen.getOrAddTrait(SleepingTrait.class).wakeUp();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
                citizen.removeTrait(SleepingTrait.class);
            }
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && citizen.hasTrait(SleepTrait.class)) {
            citizen.getOrAddTrait(SleepTrait.class).setSleeping(null);
        }
    }
}
