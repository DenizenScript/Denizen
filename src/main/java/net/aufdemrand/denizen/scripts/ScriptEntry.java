package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.*;


/**
 * ScriptEntry contain information about a single entry from a dScript.
 *
 * @author Jeremy Schroeder
 *
 */
public class ScriptEntry {

    // The name of the command that will be executed
    private String command;

    private long creationTime;

    private boolean instant = false;
    private boolean waitfor = false;
    private boolean done = false;

    private dPlayer player = null;
    private dNPC npc = null;

    private dScript script = null;

    private ScriptQueue queue = null;
    private List<String> args = null;

    private Map<String, Object> objects = new HashMap<String, Object>();

    public ScriptEntry(String command, String[] arguments, ScriptContainer script) throws ScriptEntryCreationException {

        // Must never be null
        if (command == null)
            throw new ScriptEntryCreationException("dCommand 'type' cannot be null!");

        this.command = command.toUpperCase();
        if (script != null)
            this.script = script.getAsScriptArg();

        // Internal, never null.
        this.creationTime = System.currentTimeMillis();

        // Check if this is an 'instant' or 'waitfor' command.
        if (command.startsWith("^")) {
            instant = true;
            this.command = command.substring(1);
        } else if (command.startsWith("~")) {
            waitfor = true;
            this.command = command.substring(1);
        }

        this.args = new ArrayList<String>();
        if (arguments != null)
            this.args = Arrays.asList(arguments);

        // Check for replaceable tags.
        for (String arg : args) {
            if (arg.contains("<") && arg.contains(">")) {
                has_tags = true;
                break;
            }
        }

    }

    public boolean has_tags = false;

    public ScriptEntry addObject(String key, Object object) {
        if (object == null) return this;
        if (object instanceof dObject)
            ((dObject) object).setPrefix(key);
        objects.put(key.toUpperCase(), object);
        return this;
    }

    /**
     * If the scriptEntry lacks the object corresponding to the
     * key, set it to the first non-null argument
     *
     * @param key  The key of the object to check
     * @return  The scriptEntry
     *
     */

    public ScriptEntry defaultObject(String key, Object... objects) throws InvalidArgumentsException {
        if (!this.objects.containsKey(key.toUpperCase()))
            for (Object obj : objects)
                if (obj != null) {
                    this.addObject(key, obj);
                    break;
                }
        // Check if the object has been filled. If not, throw new Invalid Arguments Exception.
        if (!hasObject(key)) throw new InvalidArgumentsException(dB.Messages.ERROR_MISSING_OTHER, key);
        else
            return this;
    }

    public List<String> getArguments() {
        return args;
    }

    public String getCommandName() {
        return command;
    }

    public dNPC getNPC() {
        return npc;
    }

    public boolean hasNPC() {

        return (npc != null);
    }

    public void setFinished(boolean finished) {
        done = finished;
    }

    public dPlayer getPlayer() {
        return player;
    }

    public boolean hasPlayer() {

        return (player != null);
    }

    public Map<String, Object> getObjects() {
        return objects;
    }

    public Object getObject(String key) {
        try {
            return objects.get(key.toUpperCase());
        } catch (Exception e) { return null; }
    }

    public dObject getdObject(String key) {
        try {
            return (dObject) objects.get(key.toUpperCase());
        } catch (Exception e) { return null; }
    }

    public Element getElement(String key) {
        try {
            return (Element) objects.get(key.toUpperCase());
        } catch (Exception e) { return null; }
    }

    public boolean hasObject(String key) {
        return (objects.containsKey(key.toUpperCase())
                && objects.get(key.toUpperCase()) != null);
    }

    public dScript getScript() {
        return script;
    }

    public ScriptQueue getResidingQueue() {
        return queue;
    }

    public boolean isInstant() {
        return instant;
    }

    public boolean shouldWaitFor() {
        return waitfor;
    }

    public boolean isDone() {
        return done;
    }

    public ScriptEntry setArguments(List<String> arguments) {
        args = arguments;
        return this;
    }

    public ScriptEntry setInstant(boolean instant) {
        this.instant = instant;
        return this;
    }

    public ScriptEntry setPlayer(dPlayer player) {
        this.player = player;
        return this;
    }

    public ScriptEntry setNPC(dNPC dNPC) {
        this.npc = dNPC;
        return this;
    }

    public ScriptEntry setScript(String scriptName) {
        this.script = dScript.valueOf(scriptName);
        return this;
    }

    public void setSendingQueue(ScriptQueue scriptQueue) {
        queue = scriptQueue;
    }

    // Keep track of objects which were added by mass
    // so that IF can inject them into new entries.
    // This is ugly, but it will keep from breaking
    // previous versions of Denizen.
    public List<String> tracked_objects = new ArrayList<String>();
    public ScriptEntry trackObject(String key) {
        tracked_objects.add(key.toUpperCase());
        return this;
    }

    public void setCommandName(String commandName) {
        this.command = commandName;
    }
}
