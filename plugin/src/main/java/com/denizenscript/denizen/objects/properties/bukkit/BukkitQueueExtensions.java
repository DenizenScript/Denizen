package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.QueueTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;

public class BukkitQueueExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <QueueTag.npc>
        // @returns NPCTag
        // @mechanism QueueTag.linked_npc
        // @description
        // Returns the NPCTag linked to a queue.
        // -->
        QueueTag.tagProcessor.registerTag(NPCTag.class, "npc", (attribute, object) -> {
            NPCTag npc = null;
            if (object.queue.getLastEntryExecuted() != null) {
                npc = ((BukkitScriptEntryData) object.queue.getLastEntryExecuted().entryData).getNPC();
            }
            else if (object.queue.getEntries().size() > 0) {
                npc = ((BukkitScriptEntryData) object.queue.getEntries().get(0).entryData).getNPC();
            }
            else if (!attribute.hasAlternative()) {
                attribute.echoError("Can't determine a linked NPC.");
            }
            return npc;
        });

        // <--[tag]
        // @attribute <QueueTag.player>
        // @returns PlayerTag
        // @mechanism QueueTag.linked_player
        // @description
        // Returns the PlayerTag linked to a queue.
        // -->
        QueueTag.tagProcessor.registerTag(PlayerTag.class, "player", (attribute, object) -> {
            PlayerTag player = null;
            if (object.queue.getLastEntryExecuted() != null) {
                player = ((BukkitScriptEntryData) object.queue.getLastEntryExecuted().entryData).getPlayer();
            }
            else if (object.queue.getEntries().size() > 0) {
                player = ((BukkitScriptEntryData) object.queue.getEntries().get(0).entryData).getPlayer();
            }
            else {
                attribute.echoError("Can't determine a linked player.");
            }
            return player;
        });

        // <--[mechanism]
        // @object QueueTag
        // @name linked_player
        // @input PlayerTag
        // @description
        // Sets the linked player for the remainder of the queue.
        // @tags
        // <QueueTag.player>
        // -->
        QueueTag.tagProcessor.registerMechanism("linked_player", false, PlayerTag.class, (queue, mechanism, player) -> {
            for (ScriptEntry entry : queue.queue.getEntries()) {
                BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
                data.setPlayer(player);
            }
        });

        // <--[mechanism]
        // @object QueueTag
        // @name linked_npc
        // @input NPCTag
        // @description
        // Sets the linked NPC for the remainder of the queue.
        // @tags
        // <QueueTag.npc>
        // -->
        QueueTag.tagProcessor.registerMechanism("linked_npc", false, NPCTag.class, (queue, mechanism, npc) -> {
            for (ScriptEntry entry : queue.queue.getEntries()) {
                BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
                data.setNPC(npc);
            }
        });
    }
}
