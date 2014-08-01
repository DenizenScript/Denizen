package net.aufdemrand.denizen.scripts.queues.core;


import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;

public class InstantQueue extends ScriptQueue {

    /**
     * Gets an InstantQueue instance.
     *
     * If a queue already exists with the given id, it will return that instance,
     * which may be currently running, unless the type of Queue is not an InstantQueue.
     * If a queue does not exist, a new stopped queue is created instead.
     *
     * IDs are case insensitive.  If having an easy-to-recall ID is not necessary, just
     * pass along null as the id, and it will use ScriptQueue's static method _getNextId()
     * which will return a random UUID.
     *
     * The default speed node will be automatically read from the configuration,
     * and new ScriptQueues may need further information before they
     * can start(), including entries, delays, loops, and possibly context.
     *
     * @param id  unique id of the queue
     * @return  a ScriptQueue
     */
    public static InstantQueue getQueue(String id) {
        // Get id if not specified.
        if (id == null) id = String.valueOf(_getNextId());
        InstantQueue scriptQueue;
        // Does the queue already exist?
        if (_queueExists(id))
            scriptQueue = (InstantQueue) _queues.get(id.toUpperCase());
            // If not, create a new one.
        else {
            scriptQueue = new InstantQueue(id);
        }
        return scriptQueue;
    }


    /////////////////////
    // Private instance fields and constructors
    /////////////////////

    public InstantQueue(String id) {
        super(id);
    }

    @Override
    public void onStart() {
        while (is_started) revolve();
    }

    public void onStop() {
        // Nothing to do here!
    }

    @Override
    protected boolean shouldRevolve() {
        // Instant queues aren't picky!
        return true;
    }


    @Override
    public String getAttribute(Attribute attribute) {

        // Meta defined in TimedQueue
        if (attribute.startsWith("speed")) {
            return new Duration(0).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <q@queue.type>
        // @returns Element
        // @description
        // Returns the type of queue.
        // -->
        if (attribute.startsWith("type")) {

           return new Element("Instant").getAttribute(attribute.fulfill(1));
        }

        return super.getAttribute(attribute);

    }
}
