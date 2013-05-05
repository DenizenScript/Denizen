package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;

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
        }
        return scriptEntryList;
    }

    /* 
     * Builds ScriptEntry(ies) of items read from a script 
     */

    public static List<ScriptEntry> buildScriptEntries(List<String> contents, ScriptContainer parent, Player player, dNPC npc) {
        return buildScriptEntries(contents, parent, player, npc, null);
    }
    
    public static List<ScriptEntry> buildScriptEntries(List<String> contents, ScriptContainer parent, Player player, dNPC npc, AbstractTrigger trigger) {
        List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

        if (contents == null || contents.isEmpty()) {
            if (dB.showScriptBuilder)
                dB.echoError("Building script entries... no entries to build!");
            return null;
        }

        if (dB.showScriptBuilder)
            dB.echoDebug("Building script entries:");

        for (String entry : contents) {

            // TODO: REMOVE THIS CHUNK OF CODE -- no longer needed.
            // // ENGAGE NOW functionality engages the NPC at the soonest possible point.
            //
            // if (thisItem.toUpperCase().contains("ENGAGE")
            //        && thisItem.toUpperCase().contains("NOW")) {
            //    plugin.getCommandRegistry().get(EngageCommand.class).setEngaged(npc.getCitizen(), true);
            // }

            String[] scriptEntry = new String[2];

            if (entry.split(" ", 2).length == 1) {
                scriptEntry[0] = entry;
                scriptEntry[1] = null;
            } else {
                scriptEntry = entry.split(" ", 2);
            }

            try {
                /* Build new script commands */
                String[] args = aH.buildArgs(scriptEntry[1]);
                if (dB.showScriptBuilder)
                    dB.echoDebug("Adding '" + scriptEntry[0] + "'  Args: " + Arrays.toString(args));
                scriptCommands.add(new ScriptEntry(scriptEntry[0], args, parent).setPlayer(player).setNPC(npc).setTrigger(trigger));
            } catch (Exception e) {
                if (dB.showStackTraces) e.printStackTrace();
            }
        }

        return scriptCommands;
    }

}
