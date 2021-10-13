package com.denizenscript.denizen.scripts.commands.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class NoteCommand extends AbstractCommand {

    public NoteCommand() {
        setName("note");
        setSyntax("note [<Notable ObjectTag>/remove] [as:<name>]");
        setRequiredArguments(2, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Note
    // @Syntax note [<Notable ObjectTag>/remove] [as:<name>]
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
            if (arg.matchesPrefix("as", "i", "id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (ObjectFetcher.canFetch(arg.getValue().split("@")[0])) {
                scriptEntry.addObject("object", arg.getValue());
            }
            else if (arg.matches("remove")) {
                scriptEntry.addObject("remove", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must specify an id");
        }
        if (!scriptEntry.hasObject("object") && !scriptEntry.hasObject("remove")) {
            throw new InvalidArgumentsException("Must specify a fetchable-object to note.");
        }
        if (!scriptEntry.hasObject("remove")) {
            scriptEntry.addObject("remove", new ElementTag(false));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        String object = (String) scriptEntry.getObject("object");
        ElementTag id = scriptEntry.getElement("id");
        ElementTag remove = scriptEntry.getElement("remove");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("object", object), id, remove);
        }
        if (remove.asBoolean()) {
            Notable note = NotableManager.getSavedObject(id.asString());
            if (note != null) {
                note.forget();
                Debug.echoDebug(scriptEntry, "notable '" + id.asString() + "' removed");
            }
            else {
                Debug.echoDebug(scriptEntry, id.asString() + " is not saved");
            }
            return;
        }
        String object_type = CoreUtilities.toLowerCase(object.split("@")[0]);
        Class object_class = ObjectFetcher.getObjectClass(object_type);
        if (object_class == null) {
            Debug.echoError(scriptEntry.getResidingQueue(), "Invalid object type! Could not fetch '" + object_type + "'!");
            return;
        }
        ObjectTag arg;
        try {
            if (!ObjectFetcher.checkMatch(object_class, object)) {
                Debug.echoError(scriptEntry.getResidingQueue(), "'" + object + "' is an invalid " + object_class.getSimpleName() + ".");
                return;
            }
            arg = ObjectFetcher.getObjectFrom(object_class, object, scriptEntry.getContext());
            if (arg instanceof Notable) {
                ((Notable) arg).makeUnique(id.asString());
            }
        }
        catch (Exception e) {
            Debug.echoError(scriptEntry.getResidingQueue(), "Uh oh! Report this to the Denizen developers! Err: NoteCommandObjectReflection");
            Debug.echoError(scriptEntry.getResidingQueue(), e);
        }
    }
}
