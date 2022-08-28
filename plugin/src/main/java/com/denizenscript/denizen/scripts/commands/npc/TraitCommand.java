package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;

import java.util.Collections;
import java.util.List;

public class TraitCommand extends AbstractCommand {

    public TraitCommand() {
        setName("trait");
        setSyntax("trait (state:true/false/{toggle}) [<trait>] (to:<npc>|...)");
        setRequiredArguments(1, 3);
        isProcedural = false;
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

    private enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        for (TraitInfo trait : CitizensAPI.getTraitFactory().getRegisteredTraits()) {
            tab.add(trait.getTraitName());
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("state")
                    && arg.matchesPrefix("state", "s")
                    && arg.matchesEnum(Toggle.class)) {
                scriptEntry.addObject("state", new ElementTag(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("trait")) {
                scriptEntry.addObject("trait", new ElementTag(arg.getValue()));
            }
            else if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(NPCTag.class)) {
                scriptEntry.addObject("npcs", arg.asType(ListTag.class).filter(NPCTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("trait")) {
            throw new InvalidArgumentsException("Missing trait argument!");
        }
        if (!scriptEntry.hasObject("npcs")) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsException("This command requires a linked NPC!");
            }
            scriptEntry.addObject("npcs", Collections.singletonList(Utilities.getEntryNPC(scriptEntry)));
        }
        scriptEntry.defaultObject("state", new ElementTag("TOGGLE"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag toggle = scriptEntry.getElement("state");
        ElementTag traitName = scriptEntry.getElement("trait");
        List<NPCTag> npcs = (List<NPCTag>) scriptEntry.getObject("npcs");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), traitName, toggle, db("npc", npcs));
        }
        Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(traitName.asString());
        if (trait == null) {
            Debug.echoError(scriptEntry, "Trait not found: " + traitName.asString());
            return;
        }
        for (NPCTag npcTag : npcs) {
            NPC npc = npcTag.getCitizen();
            switch (Toggle.valueOf(toggle.asString())) {
                case TRUE:
                case ON:
                    if (npc.hasTrait(trait)) {
                        Debug.echoError(scriptEntry, "NPC already has trait '" + traitName.asString() + "'");
                    }
                    else {
                        npc.addTrait(trait);
                    }
                    break;
                case FALSE:
                case OFF:
                    if (!npc.hasTrait(trait)) {
                        Debug.echoError(scriptEntry, "NPC does not have trait '" + traitName.asString() + "'");
                    }
                    else {
                        npc.removeTrait(trait);
                    }
                    break;
                case TOGGLE:
                    if (npc.hasTrait(trait)) {
                        npc.removeTrait(trait);
                    }
                    else {
                        npc.addTrait(trait);
                    }
                    break;
            }
        }
    }
}
