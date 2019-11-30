package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.QueueTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;

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

    public static final String[] handledMechs = new String[] {
            "linked_player", "linked_npc"
    };

    private BukkitQueueProperties(QueueTag queue) {
        this.queue = queue.getQueue();
    }

    ScriptQueue queue;

    public static void registerTags() {

        // <--[tag]
        // @attribute <QueueTag.npc>
        // @returns NPCTag
        // @mechanism ScriptQueue.linked_npc
        // @description
        // Returns the NPCTag linked to a queue.
        // -->
        PropertyParser.<BukkitQueueProperties>registerTag("npc", (attribute, object) -> {
            NPCTag npc = null;
            if (object.queue.getLastEntryExecuted() != null) {
                npc = ((BukkitScriptEntryData) object.queue.getLastEntryExecuted().entryData).getNPC();
            }
            else if (object.queue.getEntries().size() > 0) {
                npc = ((BukkitScriptEntryData) object.queue.getEntries().get(0).entryData).getNPC();
            }
            else if (!attribute.hasAlternative()) {
                Debug.echoError(object.queue, "Can't determine a linked NPC.");
            }
            if (npc == null) {
                return null;
            }
            else {
                return npc;
            }
        });

        // <--[tag]
        // @attribute <QueueTag.player>
        // @returns PlayerTag
        // @mechanism ScriptQueue.linked_player
        // @description
        // Returns the PlayerTag linked to a queue.
        // -->
        PropertyParser.<BukkitQueueProperties>registerTag("player", (attribute, object) -> {
            PlayerTag player = null;
            if (object.queue.getLastEntryExecuted() != null) {
                player = ((BukkitScriptEntryData) object.queue.getLastEntryExecuted().entryData).getPlayer();
            }
            else if (object.queue.getEntries().size() > 0) {
                player = ((BukkitScriptEntryData) object.queue.getEntries().get(0).entryData).getPlayer();
            }
            else {
                Debug.echoError(object.queue, "Can't determine a linked player.");
            }
            if (player == null) {
                return null;
            }
            else {
                return player;
            }
        });
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
