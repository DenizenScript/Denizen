package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.ObjectFetcher;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.CoreUtilities;


public class NoteCommand extends AbstractCommand {

    // <--[command]
    // @Name Note
    // @Syntax note [<Notable dObject>/remove] [as:<name>]
    // @Required 2
    // @Short Adds or removes a notable object.
    // @Group core
    //
    // @Description
    // Add or remove a notable object that can be referenced in events or scripts.
    // Notable objects are "permanent" versions of other dObjects. (See: <@link language dObject>)
    // Notable objects keep their properties when added.
    //
    // @Tags
    // <server.list_notables[<type>]>
    // <cu@cuboid.notable_name>
    // <in@inventory.notable_name>
    // <i@item.notable_name>
    // <l@location.notable_name>
    //
    // @Usage
    // Use to add a notable cuboid.
    // - note cu@1,2,3,world|4,5,6,world as:mycuboid
    //
    // @Usage
    // Use to remove a notable cuboid.
    // - note remove as:mycuboid
    //
    // @Usage
    // Use to note a location.
    // - note l@10,5,10,world as:mylocation
    //
    // @Usage
    // Use to note a region with WorldEdit selection.
    // - note <player.selected_region> as:mycuboid
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matchesPrefix("as", "i", "id")) {
                scriptEntry.addObject("id", arg.asElement());
            }
            else if (ObjectFetcher.canFetch(arg.getValue().split("@")[0])) {
                scriptEntry.addObject("object", arg.getValue());
            }
            else if (arg.matches("remove")) {
                scriptEntry.addObject("remove", new Element(true));
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
            scriptEntry.addObject("remove", new Element(false));
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        String object = (String) scriptEntry.getObject("object");
        Element id = scriptEntry.getElement("id");
        Element remove = scriptEntry.getElement("remove");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugObj("object", object) + id.debug() + remove.debug());

        }

        if (remove.asBoolean()) {
            if (NotableManager.isSaved(id.asString())) {
                NotableManager.remove(id.asString());
                dB.echoDebug(scriptEntry, "notable '" + id.asString() + "' removed");
            }
            else {
                dB.echoDebug(scriptEntry, id.asString() + " is not saved");
            }
            return;
        }

        String object_type = CoreUtilities.toLowerCase(object.split("@")[0]);
        Class object_class = ObjectFetcher.getObjectClass(object_type);

        if (object_class == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid object type! Could not fetch '" + object_type + "'!");
            return;
        }

        dObject arg;
        try {

            if (!ObjectFetcher.checkMatch(object_class, object)) {
                dB.echoError(scriptEntry.getResidingQueue(), "'" + object
                        + "' is an invalid " + object_class.getSimpleName() + ".");
                return;
            }

            arg = ObjectFetcher.getObjectFrom(object_class, object);

            if (arg instanceof Notable) {
                ((Notable) arg).makeUnique(id.asString());
            }

        }
        catch (Exception e) {
            dB.echoError(scriptEntry.getResidingQueue(), "Uh oh! Report this to the Denizen developers! Err: NoteCommandObjectReflection");
            dB.echoError(scriptEntry.getResidingQueue(), e);
        }


    }
}
