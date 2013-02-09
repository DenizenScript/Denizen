package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatTrigger extends AbstractTrigger implements Listener {

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void chatTrigger(AsyncPlayerChatEvent event) {

        // Check if there is an NPC within range of a player to chat to.
        dNPC npc = Utilities.getClosestNPC(event.getPlayer().getLocation(), 2);

        // No NPC? Nothing else to do here.
        if (npc == null) return;

        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return.
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return;
        if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) return;

        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled. Should the Player chat only when looking at the NPC? This may
        // reduce accidental chats with NPCs.
        if (Settings.CheckLineOfSightWhenChatting())
            if (!npc.getEntity().hasLineOfSight(event.getPlayer())) return;
        // if (Settings.ChatOnlyWhenLookingAtNPC())
        // TODO: Add davidcernats util method for checking POV

        // If engaged or not cool, calls On Unavailable, if cool, calls On Chat
        // If available (not engaged, and cool) sets cool down and returns true.
        if (!npc.getTriggerTrait().trigger(this, event.getPlayer())) {
            if (Settings.ChatGloballyIfNotInteractable()) {
                dB.echoDebug (ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE
                        + "The NPC is currently cooling down or engaged.");
                return;
            } else {
                event.setCancelled(true);
                // DenizenPlayer.chat(event.getPlayer(), closestNPC, event.getMessage());
            }
        }

        // Denizen should be good to interact with. Let's get the script.
        InteractScriptContainer script = npc.getInteractScript(event.getPlayer(), this.getClass());

        // Parse the script and match Triggers.. if found, cancel the text! The
        // parser will take care of everything else.
        //         Pattern	triggerPattern = Pattern.compile("\\/([^/]*)\\/");

        String matchedId = null; //getMatchId(npc, event.getPlayer(), script, event.getMessage());

        // If there was a match, the id of the match should have been returned.
        if (matchedId != null)
            parse(npc, event.getPlayer(), script, matchedId);

        else {

            if (!Settings.ChatGloballyIfFailedChatTriggers ()) {
                event.setCancelled(true);
                // DenizenPlayer.chat(event.getPlayer(), closestNPC, event.getMessage());
                return;
            }

            // No matching chat triggers, and the config.yml says we should just
            // ignore the interaction...
        }
    }

    private Boolean isKeywordRegex (String keyWord) {
        return keyWord.toUpperCase().startsWith("REGEX:");
    }


}