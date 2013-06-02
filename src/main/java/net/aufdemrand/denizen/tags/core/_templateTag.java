package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Example replaceable tag class.
 *
 */
public class _templateTag implements Listener {

	public _templateTag(Denizen denizen) {
		// Register this class with bukkit's plugin manager events.
		// Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void constantTags(ReplaceableTagEvent event) {
		// Since this event will be called each time Denizen comes across a
        // replaceable tag, something needs to tell Denizen if this is the
        // appropriate place to fill the tag. This is done by checking the
        // tag 'name'. Using event.matches(tag_name) will ensure that the name
        // of the tag is 'tag_name'. :)
        // ie. <skills.something...> .. the tag name would be 'skills'
        if (!event.matches("skills")) return;

		// Your event may need to fetch some information.
        // Denizen will break down the first 4 parts of the tag to help identify
        // the intent: name, type, subtype, and specifier

        // It also has some helper methods to check if a part is present, and to
        // easily fetch the information.
        // ie. <name.type.subtype.specifier> ... to get the type easily, use:

        String type = event.hasType() ? event.getType() : "";
        // The other parts may be handled the same way.

        // The various parts may also have some 'context', which can be referenced easy.
        // Context to a type, subtype, specifier, etc. is contained in [] brackets.
        // ie. <tag_name.type[context]>
        String type_context = event.hasTypeContext() ? event.getTypeContext() : "";

        //
        // Now, 2 small examples of possible fulfillment of the tag.
        //

        // For this small example, let's process a tag in the format of: <skills.version>
        if (type.equalsIgnoreCase("version")) {

            String version_number = null;

            // It's assumed here that your code will handle getting the appropriate
            // information to be filled in.
            // version_number = Skills.getVersionNumber();

            event.setReplaced(version_number);
            return;
        }

        //
        // Tag possibility 2
        //

        // For this example, let's process a tag in the format of: <skills.for[player_name]>
        // and return a dList dScriptArgument object to fulfill any additional attributes of the tag.
        if (type.equalsIgnoreCase("for")) {
            // Check if type_context is a valid player...
            if (aH.getPlayerFrom(type_context) == null) {
                dB.echoDebug("This tag requires a player! Has this player logged off? Aborting replacement...");
                return;
            }

            // At this point, the player specified is valid, so let's return a list of skills in dList format.

            // Returning the results as another object allows for other attributes on the tag
            // to be filled.Returning a dList object, for example, allows attributes such as
            // .get[#] or .ascslist, but should at the very least return an Element, which
            // will handle such attributes as .asint, .split, .aslocation, etc.

            // Returning a dList will help with things like: <skills.for[player_name].size>
            // which can tell how many items are in the list, all without any additional
            // code to handle each situation. A full list of attributes can be found
            // in Denizen's documentation. First you need to turn the tag into an
            // attribute object.
            Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

            // Now to catch up, 2 attributes have been handled already...
            // Fulfilling 2 attributes, skills and .for, in <skills.for[player].get[1]>
            // will leave the .get[1] to be handled by the dList.
            attribute.fulfill(2);

            List<String> skills = new ArrayList<String>();

         // skills = Skills.getForPlayer(aH.getPlayerFrom(type_context)).list()

            // Use event.setReplaced() to pass the attribute off to the dList object (or any other dScriptArg object).
            // The dList constructor requires a string list and a prefix.
            event.setReplaced(new dList(skills).getAttribute(attribute));
        }

        // Got here? No attributes were handled! Probably should let the dBugger know.
        dB.echoError("Example skills tag '" + event.raw_tag + "' was unable to match an attribute. Replacement has been cancelled...");
        return;

	}
}