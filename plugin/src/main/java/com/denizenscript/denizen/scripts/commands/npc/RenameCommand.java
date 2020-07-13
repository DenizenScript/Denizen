package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityFormObject;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        setName("rename");
        setSyntax("rename [<name>] (t:<entity>|...)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Rename
    // @Syntax rename [<name>] (t:<entity>|...)
    // @Required 1
    // @Maximum 2
    // @Plugin Citizens
    // @Short Renames the linked NPC or list of entities.
    // @Group npc
    //
    // @Description
    // Renames the linked NPC or list of entities.
    // Functions like the '/npc rename' command.
    //
    // Can rename a spawned or unspawned NPC to any name up to 256 characters.
    //
    // Can rename a vanilla entity to any name up to 256 characters, and will automatically make the nameplate visible.
    //
    // Can rename a player to any name up to 16 characters. This will affect only the player's nameplate.
    //
    // @Tags
    // <NPCTag.name>
    // <NPCTag.nickname>
    //
    // @Usage
    // Use to rename the linked NPC.
    // - rename Bob
    //
    // @Usage
    // Use to rename a different NPC.
    // - rename Bob t:<[some_npc]>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("name")) {
                scriptEntry.addObject("name", arg.asElement());
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("t", "target", "targets")) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("name")) {
            throw new InvalidArgumentsException("Must specify a name!");
        }
        if (!scriptEntry.hasObject("targets")) {
            if (Utilities.getEntryNPC(scriptEntry) == null || !Utilities.getEntryNPC(scriptEntry).isValid()) {
                throw new InvalidArgumentsException("Must have an NPC attached, or specify a list of targets to rename!");
            }
            scriptEntry.addObject("targets", new ListTag(Utilities.getEntryNPC(scriptEntry)));
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        ElementTag name = scriptEntry.getElement("name");
        ListTag targets = scriptEntry.getObjectTag("targets");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), name.debug() + targets.debug());
        }
        String nameString = name.asString().length() > 256 ? name.asString().substring(0, 256) : name.asString();
        for (ObjectTag target : targets.objectForms) {
            EntityFormObject entity = target.asType(EntityTag.class, CoreUtilities.noDebugContext);
            if (entity == null) {
                entity = target.asType(NPCTag.class, scriptEntry.context);
            }
            else {
                entity = ((EntityTag) entity).getDenizenObject();
            }
            if (entity instanceof NPCTag) {
                NPC npc = ((NPCTag) entity).getCitizen();
                if (npc.isSpawned()) {
                    Location prev = npc.getEntity().getLocation();
                    npc.despawn(DespawnReason.PENDING_RESPAWN);
                    npc.setName(nameString);
                    npc.spawn(prev);
                }
                else {
                    npc.setName(nameString);
                }
            }
            else if (entity instanceof PlayerTag) {
                String limitedName = nameString.length() > 16 ? nameString.substring(0, 16) : nameString;
                NMSHandler.getInstance().getProfileEditor().setPlayerName(((PlayerTag) entity).getPlayerEntity(), limitedName);
            }
            else {
                Entity bukkitEntity = entity.getDenizenEntity().getBukkitEntity();
                bukkitEntity.setCustomName(nameString);
                bukkitEntity.setCustomNameVisible(true);
            }
        }
    }
}
