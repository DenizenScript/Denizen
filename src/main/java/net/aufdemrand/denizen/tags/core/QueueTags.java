package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueTags implements Listener {

    public QueueTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }


    //////////
    //  ReplaceableTagEvent handler
    ////////

    @EventHandler
    public void queueTag(ReplaceableTagEvent event) {

        if (!event.matches("queue, q")) return;

        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry()).fulfill(1);

        // Handle <queue[id]. ...> tags

        if (event.hasNameContext()) {
            if (ScriptQueue._queueExists(event.getNameContext()))
                event.setReplaced(Element.NULL.getAttribute(attribute.fulfill(1)));
            else
                event.setReplaced(ScriptQueue._getExistingQueue(event.getNameContext())
                        .getAttribute(attribute.fulfill(1)));
            return;
        }


        // Otherwise, try to use queue in a static manner.

        // <--[tag]
        // @attribute <queue.exists[<queue_id>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the specified queue exists.
        // -->
        if (attribute.startsWith("exists")
                && attribute.hasContext(1)) {
            event.setReplaced(new Element(ScriptQueue._queueExists(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1)));
            return;
        }

        // <--[tag]
        // @attribute <queue.stats>
        // @returns Element
        // @description
        // Returns stats for all queues during this server session
        // -->
        if (attribute.startsWith("stats")) {
            event.setReplaced(new Element(ScriptQueue._getStats())
                    .getAttribute(attribute.fulfill(1)));
            return;
        }


        // Else,
        // Use current queue

        event.setReplaced(event.getScriptEntry().getResidingQueue()
                .getAttribute(attribute));
    }


}


