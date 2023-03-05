package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizencore.utilities.debugging.Debug;
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
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CreateCommand extends AbstractCommand {

    public CreateCommand() {
        setName("create");
        setSyntax("create [<entity>] [<name>] (<location>) (traits:<trait>|...) (registry:<name>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Create
    // @Syntax create [<entity>] [<name>] (<location>) (traits:<trait>|...) (registry:<name>)
    // @Required 1
    // @Maximum 5
    // @Plugin Citizens
    // @Short Creates a new NPC, and optionally spawns it at a location.
    // @Group npc
    //
    // @Description
    // Creates an npc which the entity type specified, or specify an existing npc to create a copy.
    // If no location is specified the npc is created despawned.
    // Use the 'save:<savename>' argument to return the npc for later use in a script.
    //
    // Optionally specify a list of traits to immediately apply when creating the NPC.
    //
    // Optionally specify a custom registry to create the NPC into. (Most users, leave this option off).
    // Will generate a new registry if needed.
    //
    // @Tags
    // <server.npcs>
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
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(EntityType.values());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
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
            else if (!scriptEntry.hasObject("registry")
                    && arg.matchesPrefix("registry")) {
                scriptEntry.addObject("registry", arg.asElement());
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
        ElementTag name = scriptEntry.getElement("name");
        EntityTag type = scriptEntry.getObjectTag("entity_type");
        LocationTag loc = scriptEntry.getObjectTag("spawn_location");
        ListTag traits = scriptEntry.getObjectTag("traits");
        ElementTag registry = scriptEntry.getElement("registry");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), name, type, loc, traits, registry);
        }
        NPCTag created;
        if (!type.isGeneric() && type.isCitizensNPC()) {
            created = new NPCTag(type.getDenizenNPC().getCitizen().clone());
            created.getCitizen().setName(name.asString());
        }
        else {
            NPCRegistry actualRegistry = CitizensAPI.getNPCRegistry();
            if (registry != null) {
                actualRegistry = NPCTag.getRegistryByName(registry.asString());
                if (actualRegistry == null) {
                    actualRegistry = CitizensAPI.createNamedNPCRegistry(registry.asString(), new MemoryNPCDataStore());
                }
            }
            created = new NPCTag(actualRegistry.createNPC(type.getBukkitEntityType(), name.asString()));
        }
        // Add the created NPC into the script entry so it can be utilized if need be.
        scriptEntry.saveObject("created_npc", created);
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
                    Debug.echoError(scriptEntry, "Could not add trait to NPC: " + trait_name);
                }
            }
        }
        for (Mechanism mechanism : type.getWaitingMechanisms()) {
            created.safeAdjust(mechanism);
        }
    }
}
