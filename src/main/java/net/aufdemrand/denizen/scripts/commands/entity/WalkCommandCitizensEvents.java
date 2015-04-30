package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.event.NavigationEvent;
import net.citizensnpcs.api.ai.flocking.*;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class WalkCommandCitizensEvents implements Listener {

    public static Flocker generateNewFlocker(NPC npc, double radius) {
        NPCFlock flock = new RadiusNPCFlock(radius);
        return new Flocker(npc, flock, new SeparationBehavior(Flocker.LOW_INFLUENCE),
                new CohesionBehavior(Flocker.LOW_INFLUENCE), new AlignmentBehavior(Flocker.HIGH_INFLUENCE));
    }

    @EventHandler
    public void finish(NavigationCompleteEvent e) {

        if (WalkCommand.held.isEmpty()) return;

        checkHeld(e);

    }

    @EventHandler
    public void cancel(NavigationCancelEvent e) {

        if (WalkCommand.held.isEmpty()) return;

        checkHeld(e);

    }


    public void checkHeld(NavigationEvent e) {
        if (e.getNPC() == null)
            return;

        // Check each held entry -- the scriptExecuter is waiting on
        // the entry to be marked 'waited for'.
        for (int i = 0; i < WalkCommand.held.size(); i++) {
            ScriptEntry entry = WalkCommand.held.get(i);

            // Get all NPCs associated with the entry. They must all
            // finish navigation before the entry can be let go
            List<dNPC> tally = (List<dNPC>) entry.getObject("tally");
            // If the NPC is the NPC from the event, take it from the list.
            tally.remove(dNPC.mirrorCitizensNPC(e.getNPC()));

            // Check if tally is empty.
            if (tally.isEmpty()) {
                entry.setFinished(true);
                WalkCommand.held.remove(i);
                i--;
            }
        }
    }
}
