package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.npc.traits.SittingTrait;
import com.denizenscript.denizen.npc.traits.SleepingTrait;
import com.denizenscript.denizen.npc.traits.SneakingTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.trait.SitTrait;
import net.citizensnpcs.trait.SleepTrait;
import net.citizensnpcs.trait.SneakTrait;

import java.util.Collections;
import java.util.List;

public class TraitCommand extends AbstractCommand {

    public TraitCommand() {
        setName("trait");
        setSyntax("trait (state:true/false/{toggle}) [<trait>] (to:<npc>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Trait
    // @Syntax trait (state:true/false/{toggle}) [<trait>] (to:<npc>|...)
    // @Required 1
    // @Maximum 3
    // @Plugin Citizens
    // @Short Adds or removes a trait from an NPC.
    // @Group npc
    //
    // @Description
    // This command adds or removes a trait from an NPC.
    //
    // Use "state:true" to add or "state:false" to remove.
    // If neither is specified, the default is "toggle", which means remove if already present or add if not.
    //
    // Note that a redundant instruction, like adding a trait that the NPC already has, will give an error message.
    //
    // The trait input is simply the name of the trait, like "sentinel".
    //
    // Optionally, specify a list of NPCs to apply the trait to. If unspecified, the linked NPC will be used.
    //
    // @Tags
    // <NPCTag.has_trait[<trait>]>
    // <NPCTag.traits>
    // <server.traits>
    //
    // @Usage
    // Use to add the Sentinel trait to the linked NPC.
    // - trait state:true sentinel
    //
    // @Usage
    // Use to toggle the MobProx trait on the linked NPC.
    // - trait mobprox
    //
    // -->

    public enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        for (TraitInfo trait : CitizensAPI.getTraitFactory().getRegisteredTraits()) {
            tab.add(trait.getTraitName());
        }
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("state") @ArgPrefixed @ArgDefaultNull Toggle toggle,
                                   @ArgName("trait") @ArgLinear @ArgDefaultNull String traitName,
                                   @ArgName("to") @ArgPrefixed @ArgDefaultNull @ArgSubType(NPCTag.class) List<NPCTag> npcs) {
        if (traitName == null) {
            throw new InvalidArgumentsRuntimeException("Missing trait argument!");
        }
        Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(traitName);
        if (trait == null) {
            throw new InvalidArgumentsRuntimeException("Trait not found: " + traitName);
        }
        if (trait == SittingTrait.class || trait == SleepingTrait.class || trait == SneakingTrait.class) {
            BukkitImplDeprecations.citizensTraits.warn();
            if (trait == SittingTrait.class) {
                trait = SitTrait.class;
            }
            else if (trait == SleepingTrait.class) {
                trait = SleepTrait.class;
            }
            else {
                trait = SneakTrait.class;
            }
        }
        if (npcs == null) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsRuntimeException("This command requires a linked NPC!");
            }
            npcs = Collections.singletonList(Utilities.getEntryNPC(scriptEntry));
        }
        for (NPCTag npcTag : npcs) {
            NPC npc = npcTag.getCitizen();
            switch (toggle) {
                case TRUE:
                case ON:
                    if (npc.hasTrait(trait)) {
                        Debug.echoError(scriptEntry, "NPC already has trait '" + trait.getName() + "'");
                        break;
                    }
                    npc.addTrait(trait);
                    break;
                case FALSE:
                case OFF:
                    if (!npc.hasTrait(trait)) {
                        Debug.echoError(scriptEntry, "NPC does not have trait '" + trait.getName() + "'");
                    }
                    else {
                        npc.removeTrait(trait);
                    }
                    break;
                default:
                    if (npc.hasTrait(trait)) {
                        npc.removeTrait(trait);
                        break;
                    }
                    npc.addTrait(trait);
                    break;
            }
        }
    }
}
