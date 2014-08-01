package net.aufdemrand.denizen.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class ScriptBuilder {
    /**
     * Adds an object to a list of ScriptEntries. Can later be retrieved from the ScriptEntry
     * by using getObject(String key)
     *
     * @param scriptEntryList the list of ScriptEntries
     * @param key the key (name) of the object being added
     * @param obj the object
     *
     * @return the List of ScriptEntries, with the object added in each member
     *
     */
    public static List<ScriptEntry> addObjectToEntries(List<ScriptEntry> scriptEntryList, String key, Object obj) {
        for (ScriptEntry entry : scriptEntryList) {
            entry.addObject(key, obj);
            entry.trackObject(key);
        }
        return scriptEntryList;
    }

    /*
     * Builds ScriptEntry(ies) of items read from a script
     */

    public static List<ScriptEntry> buildScriptEntries(List<String> contents, ScriptContainer parent, dPlayer player, dNPC npc) {
        List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

        if (contents == null || contents.isEmpty()) {
            if (dB.showScriptBuilder)
                dB.echoError("Building script entries... no entries to build!");
            return null;
        }

        if (dB.showScriptBuilder)
            dB.echoDebug(parent, "Building script entries:");

        for (String entry : contents) {


            String[] scriptEntry = new String[2];
            String[] splitEntry = entry.split(" ", 2);

            if (splitEntry.length == 1) {
                scriptEntry[0] = entry;
                scriptEntry[1] = null;
            } else {
                scriptEntry = splitEntry;
            }

            try {
                /* Build new script commands */
                String[] args = aH.buildArgs(scriptEntry[1]);
                if (dB.showScriptBuilder)
                    dB.echoDebug(parent, "Adding '" + scriptEntry[0] + "'  Args: " + Arrays.toString(args));
                ScriptEntry newEntry = new ScriptEntry(scriptEntry[0], args, parent).setPlayer(player).setNPC(npc);
                scriptCommands.add(newEntry);
            } catch (Exception e) {
                dB.echoError(e);
            }
        }

        return scriptCommands;
    }
}
