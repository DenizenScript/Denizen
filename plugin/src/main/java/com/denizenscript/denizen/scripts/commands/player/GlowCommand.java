package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.debugging.Warning;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class GlowCommand extends AbstractCommand {

    public GlowCommand() {
        setName("glow");
        setSyntax("glow [<entity>|...] (<should glow>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // TODO: REWRITE: 'glow [<entity>|...] (glow:{true}/false/reset) (per_player) (players:<player>|...)'
    // Default apply globally, unless 'per_player' set, in which case use a purely network solution and do not override the global state at all.
    // Also note that 'reset' is to reset per_player to view global state.

    // <--[command]
    // @Name Glow
    // @Syntax glow [<entity>|...] (<should glow>)
    // @Required 1
    // @Maximum 2
    // @Short Makes the linked player see the chosen entities as glowing.
    // @Group player
    //
    // @Description
    // Makes the linked player see the chosen entities as glowing.
    // BE WARNED, THIS COMMAND IS HIGHLY EXPERIMENTAL AND MAY NOT WORK AS EXPECTED.
    // This command works by globally enabling the glow effect, then whitelisting who is allowed to see it.
    //
    // THIS COMMAND IS UNSTABLE AND IS SUBJECT TO BEING REWRITTEN IN THE NEAR FUTURE.
    //
    // @Tags
    // <EntityTag.glowing>
    //
    // @Usage
    // Use to make the player's target glow.
    // - glow <player.target>
    //
    // @Usage
    // Use to make the player's target not glow.
    // - glow <player.target> false
    // -->

    public static HashMap<Integer, HashSet<UUID>> glowViewers = new HashMap<>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("glowing")
                    && arg.matchesBoolean()) {
                scriptEntry.addObject("glowing", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("glowing", new ElementTag("true"));
        if (!Utilities.entryHasPlayer(scriptEntry)) {
            throw new InvalidArgumentsException("Must have a valid player link!");
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entities to make glow!");
        }
    }

    public static Warning GLOW_UNSTABLE_WARN = new SlowWarning("The 'glow' command is unstable, glitchy, and experimental. It is subject to a rewrite in the near future. It is recommended that you avoid it for the time being.");

    @Override
    public void execute(ScriptEntry scriptEntry) {
        GLOW_UNSTABLE_WARN.warn(scriptEntry);
        NetworkInterceptHelper.enable();
        final ArrayList<EntityTag> entities = (ArrayList<EntityTag>) scriptEntry.getObject("entities");
        ElementTag glowing = scriptEntry.getElement("glowing");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("entities", entities), glowing);
        }
        boolean shouldGlow = glowing.asBoolean();
        final UUID puuid = Utilities.getEntryPlayer(scriptEntry).getUUID();
        for (EntityTag ent : entities) {
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(ent.getLivingEntity())) {
                CitizensAPI.getNPCRegistry().getNPC(ent.getLivingEntity()).data().setPersistent(NPC.GLOWING_METADATA, shouldGlow);
            }
            if (shouldGlow) {
                HashSet<UUID> players = glowViewers.computeIfAbsent(ent.getLivingEntity().getEntityId(), k -> new HashSet<>());
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
