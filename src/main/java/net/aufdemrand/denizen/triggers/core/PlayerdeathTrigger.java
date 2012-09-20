package net.aufdemrand.denizen.triggers.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.DenizenTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerdeathTrigger extends net.aufdemrand.denizen.triggers.AbstractTrigger  implements Listener {
    
  @EventHandler
  public void onDeath(PlayerDeathEvent event){
    Player player = event.getEntity();

    List<DenizenNPC> list = new ArrayList<DenizenNPC>(plugin.getDenizenNPCRegistry().getDenizens().values());

    ScriptHelper sE = plugin.getScriptEngine().helper;

    for (DenizenNPC thisDenizen:list) {
      if (thisDenizen != null && thisDenizen.isSpawned() && thisDenizen.getLocation().getWorld() != player.getLocation().getWorld()) continue;
    
      try {
        if (thisDenizen.getCitizensEntity().getTrait(DenizenTrait.class).triggerIsEnabled("Playerdeath")) {
          // if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Playerdeath Trigger not enabled for " + thisDenizen.getCitizensEntity().getName());
        continue;
        }
      } catch (Exception e) {
        continue;
      }
            
      String theScriptName = thisDenizen.getInteractScript(player, PlayerdeathTrigger.class);

      if (theScriptName == null) {
        if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "No script found.");
        continue;
      }

      Integer theStep = sE.getCurrentStep(player, theScriptName);

      String sradius = plugin.getScripts().getString(sE.getTriggerPath(theScriptName, theStep, triggerName)) + ".Trigger";
      int radius;

      try {
        radius = Integer.parseInt(sradius);
      } catch (Exception e) {
        if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Invalid radius");
        continue;
      }

      if ( radius == -1 || (thisDenizen.isSpawned() && thisDenizen.getLocation().distance(player.getLocation()) < radius )){
        List<String> theScript = sE.getScript(sE.getTriggerPath(theScriptName, theStep, triggerName ) +  sE.scriptString);
        if(theScript ==null || theScript.isEmpty()){
          if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Commands missing or empty");
          continue;
        }
        sE.queueScriptEntries(player, sE.buildScriptEntries(player, thisDenizen, theScript, theScriptName, theStep), QueueType.TRIGGER);
        
      }


    }    
  }
}