package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class NoteCommand extends AbstractCommand {

    public NoteCommand() {
        setName("note");
        setSyntax("note [<object>/remove] [as:<name>]");
        setRequiredArguments(2, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Note
    // @Syntax note [<object>/remove] [as:<name>]
    // @Required 2
    // @Maximum 2
    // @Short Adds or removes a named note of an object to the server.
    // @Synonyms Notable
    // @Group core
    // @Guide https://guide.denizenscript.com/guides/advanced/notables.html
    //
    // @Description
    // Add or remove a 'note' to the server, persistently naming an object that can be referenced in events or scripts.
    // Only works for object types that are 'notable'.
    // Noted objects are "permanent" versions of other ObjectTags. (See: <@link language ObjectTags>)
    // Noted objects keep their properties when added.
    //
    // Notable object types: CuboidTag, EllipsoidTag, PolygonTag, LocationTag, InventoryTag
    //
    // @Tags
    // <server.notes[<type>]>
    // <CuboidTag.note_name>
    // <EllipsoidTag.note_name>
    // <PolygonTag.note_name>
    // <InventoryTag.note_name>
    // <LocationTag.note_name>
    //
    // @Usage
    // Use to note a cuboid.
    // - note <[some_cuboid]> as:mycuboid
    //
    // @Usage
    // Use to remove a noted cuboid.
    // - note remove as:mycuboid
    //
    // @Usage
    // Use to note a location.
    // - note <context.location> as:mylocation
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (arg.matchesPrefix("as", "i", "id")
                    && !scriptEntry.hasObject("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (arg.matches("remove")
                    && !scriptEntry.hasObject("object")
                    && !scriptEntry.hasObject("remove")) {
                scriptEntry.addObject("remove", new ElementTag(true));
            }
            else if (ObjectFetcher.canFetch(arg.getValue().split("@")[0])
                    && !scriptEntry.hasObject("object")
                    && !scriptEntry.hasObject("remove")) {
                scriptEntry.addObject("object", arg.object);
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an id");
        }
        if (!scriptEntry.hasObject("object") && !scriptEntry.hasObject("remove")) {
            throw new InvalidArgumentsException("Must specify an object to note.");
        }
        if (!scriptEntry.hasObject("remove")) {
            scriptEntry.addObject("remove", new ElementTag(false));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ObjectTag object = scriptEntry.getObjectTag("object");
        ElementTag id = scriptEntry.getElement("id");
        ElementTag remove = scriptEntry.getElement("remove");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), object, id, remove);
        }
        if (remove.asBoolean()) {
            Notable note = NoteManager.getSavedObject(id.asString());
            if (note != null) {
                note.forget();
                Debug.echoDebug(scriptEntry, "Note '" + id.asString() + "' removed");
            }
            else {
                Debug.echoDebug(scriptEntry, id.asString() + " is not saved");
            }
            return;
        }
        if (object instanceof ElementTag) {
            String stringified = object.toString();
            object = ObjectFetcher.pickObjectFor(stringified, scriptEntry.context);
            if (object == null) {
                Debug.echoError("Failed to read the object '" + stringified + "' into a real object value.");
                return;
            }
        }
        if (!(object instanceof Notable)) {
            Debug.echoError("Object '" + object + "' has type '" + object.getObjectType() + "' which is not a notable object type.");
            return;
        }
        try {
            ((Notable) object).makeUnique(id.asString());
        }
        catch (Throwable ex) {
            Debug.echoError("Something went wrong converting that object!");
            Debug.echoError(ex);
        }
    }
}
