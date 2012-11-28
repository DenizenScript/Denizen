package net.aufdemrand.denizen.npc.actions;

import java.util.List;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.Debugger.DebugElement;

public class ActionHandler {

    Denizen plugin;
    
    public ActionHandler(Denizen denizen) {
        this.plugin = denizen;
    }
    
    public void doAction(String actionName, DenizenNPC npc, Player player, String assignment) {

        // Fetch script from Actions
        List<String> script = plugin.getScriptEngine().getScriptHelper().getStringListIgnoreCase(assignment + ".actions.on " + actionName);
        if (script.isEmpty()) return;
        
        plugin.getDebugger().echoDebug(DebugElement.Header, "Executing action 'On " + actionName.toUpperCase() + "' for " + npc.toString());
        
        // Build script entries
        List<ScriptEntry> scriptEntries = plugin.getScriptEngine().getScriptBuilder().buildScriptEntries(player, npc, script, null, null);
        
        // Execute scriptEntries
        for (ScriptEntry scriptEntry : scriptEntries)
           plugin.getScriptEngine().getScriptExecuter().execute(scriptEntry);
        
        plugin.getDebugger().echoDebug(DebugElement.Footer);
        
    }
    
}
