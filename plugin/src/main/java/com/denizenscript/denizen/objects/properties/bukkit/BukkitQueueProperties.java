package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.QueueTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.Attribute;

public class BukkitQueueProperties implements Property {

    public static boolean describes(ObjectTag script) {
        return script instanceof QueueTag;
    }

    public static BukkitQueueProperties getFrom(ObjectTag queue) {
        if (!describes(queue)) {
            return null;
        }
        else {
            return new BukkitQueueProperties((QueueTag) queue);
        }
    }

    public static final String[] handledTags = new String[] {
            "player", "npc"
    };

    public static final String[] handledMechs = new String[] {
            "linked_player", "linked_npc"
    };

    private BukkitQueueProperties(QueueTag queue) {
        this.queue = queue.queue;
    }

    ScriptQueue queue;

    @Override
    public String getAttribute(Attribute attribute) {

        // <--[tag]
        // @attribute <QueueTag.npc>
        // @returns NPCTag
        // @mechanism ScriptQueue.linked_npc
        // @description
        // Returns the NPCTag linked to a queue.
        // -->
        if (attribute.startsWith("npc")) {
            NPCTag npc = null;
            if (queue.getLastEntryExecuted() != null) {
                npc = ((BukkitScriptEntryData) queue.getLastEntryExecuted().entryData).getNPC();
            }
            else if (queue.getEntries().size() > 0) {
                npc = ((BukkitScriptEntryData) queue.getEntries().get(0).entryData).getNPC();
            }
            else {
                Debug.echoError(queue, "Can't determine a linked NPC.");
            }
            if (npc == null) {
                return null;
            }
            else {
                return npc.getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <QueueTag.player>
        // @returns PlayerTag
        // @mechanism ScriptQueue.linked_player
        // @description
        // Returns the PlayerTag linked to a queue.
        // -->
        if (attribute.startsWith("player")) {
            PlayerTag player = null;
            if (queue.getLastEntryExecuted() != null) {
                player = ((BukkitScriptEntryData) queue.getLastEntryExecuted().entryData).getPlayer();
            }
            else if (queue.getEntries().size() > 0) {
                player = ((BukkitScriptEntryData) queue.getEntries().get(0).entryData).getPlayer();
            }
            else {
                Debug.echoError(queue, "Can't determine a linked player.");
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

        // <--[mechanism]
        // @object ScriptQueue
        // @name linked_player
        // @input PlayerTag
        // @description
        // Sets the linked player for the remainder of the queue.
        // @tags
        // <QueueTag.player>
        // -->
        if (mechanism.matches("linked_player") && mechanism.requireObject(PlayerTag.class)) {
            PlayerTag player = mechanism.valueAsType(PlayerTag.class);
            for (ScriptEntry entry : queue.getEntries()) {
                BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
                data.setPlayer(player);
            }
        }

        // <--[mechanism]
        // @object ScriptQueue
        // @name linked_npc
        // @input NPCTag
        // @description
        // Sets the linked NPC for the remainder of the queue.
        // @tags
        // <QueueTag.npc>
        // -->
        if (mechanism.matches("linked_npc") && mechanism.requireObject(NPCTag.class)) {
            NPCTag npc = mechanism.valueAsType(NPCTag.class);
            for (ScriptEntry entry : queue.getEntries()) {
                BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
                data.setNPC(npc);
            }
        }
    }
}
