package net.aufdemrand.denizen.objects.properties.bukkit;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.tags.Attribute;

public class BukkitQueueProperties implements Property {

    public static boolean describes(dObject script) {
        return script instanceof ScriptQueue;
    }

    public static BukkitQueueProperties getFrom(dObject queue) {
        if (!describes(queue)) {
            return null;
        }
        else {
            return new BukkitQueueProperties((ScriptQueue) queue);
        }
    }


    private BukkitQueueProperties(ScriptQueue queue) {
        this.queue = queue;
    }

    ScriptQueue queue;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <q@queue.npc>
        // @returns dNPC
        // @description
        // Returns the dNPC linked to a queue.
        // -->
        if (attribute.startsWith("npc")) {
            dNPC npc = null;
            if (queue.getLastEntryExecuted() != null) {
                npc = ((BukkitScriptEntryData) queue.getLastEntryExecuted().entryData).getNPC();
            }
            else if (queue.getEntries().size() > 0) {
                npc = ((BukkitScriptEntryData) queue.getEntries().get(0).entryData).getNPC();
            }
            else {
                dB.echoError(queue, "Can't determine a linked NPC.");
            }
            if (npc == null) {
                return null;
            }
            else {
                return npc.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <q@queue.player>
        // @returns dPlayer
        // @description
        // Returns the dPlayer linked to a queue.
        // -->
        if (attribute.startsWith("player")) {
            dPlayer player = null;
            if (queue.getLastEntryExecuted() != null) {
                player = ((BukkitScriptEntryData) queue.getLastEntryExecuted().entryData).getPlayer();
            }
            else if (queue.getEntries().size() > 0) {
                player = ((BukkitScriptEntryData) queue.getEntries().get(0).entryData).getPlayer();
            }
            else {
                dB.echoError(queue, "Can't determine a linked player.");
            }
            if (player == null) {
                return null;
            }
            else {
                return player.getAttribute(attribute.fulfill(1));
            }
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "BukkitQueueProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }
}
