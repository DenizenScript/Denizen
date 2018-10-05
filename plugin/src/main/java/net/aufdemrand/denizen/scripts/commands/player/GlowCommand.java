package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class GlowCommand extends AbstractCommand {

    public static HashMap<Integer, HashSet<UUID>> glowViewers = new HashMap<Integer, HashSet<UUID>>();

    public static void unGlow(LivingEntity e) {
        if (glowViewers.containsKey(e.getEntityId())) {
            glowViewers.remove(e.getEntityId());
            e.setGlowing(false);
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(e)) {
                CitizensAPI.getNPCRegistry().getNPC(e).data().setPersistent(NPC.GLOWING_METADATA, false);
            }
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }
            else if (!scriptEntry.hasObject("glowing")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("glowing", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("glowing", new Element("true"));

        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
            throw new InvalidArgumentsException("Must have a valid player link!");
        }

        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entities to make glow!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final ArrayList<dEntity> entities = (ArrayList<dEntity>) scriptEntry.getObject("entities");
        Element glowing = scriptEntry.getElement("glowing");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugList("entities", entities) + glowing.debug());

        }

        boolean shouldGlow = glowing.asBoolean();

        final UUID puuid = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getOfflinePlayer().getUniqueId();

        if (puuid == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid/non-spawned player link!");
            return;
        }

        for (dEntity ent : entities) {
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(ent.getLivingEntity())) {
                CitizensAPI.getNPCRegistry().getNPC(ent.getLivingEntity()).data().setPersistent(NPC.GLOWING_METADATA, shouldGlow);
            }
            if (shouldGlow) {
                HashSet<UUID> players = glowViewers.get(ent.getLivingEntity().getEntityId());
                if (players == null) {
                    players = new HashSet<UUID>();
                    glowViewers.put(ent.getLivingEntity().getEntityId(), players);
                }
                players.add(puuid);
            }
            else {
                HashSet<UUID> players = glowViewers.get(ent.getLivingEntity().getEntityId());
                if (players != null) {
                    players.remove(puuid);
                    shouldGlow = !players.isEmpty();
                    if (!shouldGlow) {
                        glowViewers.remove(ent.getLivingEntity().getEntityId());
                    }
                }
            }
            ent.getLivingEntity().setGlowing(shouldGlow);
        }
    }
}
