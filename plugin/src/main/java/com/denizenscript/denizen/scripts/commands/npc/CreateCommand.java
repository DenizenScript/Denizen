package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CreateCommand extends AbstractCommand {

    // <--[command]
    // @Name Create
    // @Syntax create [<entity>] [<name>] (<location>) (traits:<trait>|...)
    // @Required 1
    // @Maximum 4
    // @Plugin Citizens
    // @Short Creates a new NPC, and optionally spawns it at a location.
    // @Group npc
    //
    // @Description
    // Creates an npc which the entity type specified, or specify an existing npc to create a copy. If no location
    // is specified the npc is created despawned. Use the 'save:<savename>' argument to return the npc for later
    // use in a script.
    //
    // @Tags
    // <server.list_npcs>
    // <entry[saveName].created_npc> returns the NPC that was created.
    //
    // @Usage
    // Use to create a despawned NPC for later usage.
    // - create player Bob
    //
    // @Usage
    // Use to create an NPC and spawn it immediately.
    // - create spider Joe <player.location>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("entity_type")
                    && arg.matchesArgumentType(EntityTag.class)) {
                // Avoid duplication of objects
                EntityTag ent = arg.asType(EntityTag.class);
                if (!ent.isGeneric() && !ent.isCitizensNPC()) {
                    throw new InvalidArgumentsException("Entity supplied must be generic or a Citizens NPC!");
                }
                scriptEntry.addObject("entity_type", ent);
            }
            else if (!scriptEntry.hasObject("spawn_location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("spawn_location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("name")) {
                scriptEntry.addObject("name", arg.asElement());
            }
            else if (!scriptEntry.hasObject("traits")
                    && arg.matchesPrefix("t", "trait", "traits")) {
                scriptEntry.addObject("traits", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("name")) {
            throw new InvalidArgumentsException("Must specify a name!");
        }
        if (!scriptEntry.hasObject("entity_type")) {
            throw new InvalidArgumentsException("Must specify an entity type!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {

        ElementTag name = (ElementTag) scriptEntry.getObject("name");
        EntityTag type = (EntityTag) scriptEntry.getObject("entity_type");
        LocationTag loc = (LocationTag) scriptEntry.getObject("spawn_location");
        ListTag traits = (ListTag) scriptEntry.getObject("traits");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), name.debug() + type.debug() + (loc != null ? loc.debug() : "")
                    + (traits != null ? traits.debug() : ""));

        }

        NPCTag created;
        if (!type.isGeneric() && type.isCitizensNPC()) {
            created = new NPCTag(type.getDenizenNPC().getCitizen().clone());
            created.getCitizen().setName(name.asString());
        }
        else {
            created = NPCTag.mirrorCitizensNPC(CitizensAPI.getNPCRegistry()
                    .createNPC(type.getBukkitEntityType(), name.asString()));
        }

        // Add the created NPC into the script entry so it can be utilized if need be.
        scriptEntry.addObject("created_npc", created);

        if (created.isSpawned()) {
            if (loc != null) {
                created.getCitizen().teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
            else {
                created.getCitizen().despawn();
            }
        }
        else {
            if (loc != null) {
                created.getCitizen().spawn(loc);
            }
        }
        if (traits != null) {
            for (String trait_name : traits) {
                Trait trait = CitizensAPI.getTraitFactory().getTrait(trait_name);
                if (trait != null) {
                    created.getCitizen().addTrait(trait);
                }
                else {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Could not add trait to NPC: " + trait_name);
                }
            }
        }
        for (Mechanism mechanism : type.getWaitingMechanisms()) {
            created.adjust(mechanism);
        }
    }
}
