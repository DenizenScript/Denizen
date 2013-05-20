package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class OfflinePlayerTags implements Listener {

    public OfflinePlayerTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void offlinePlayerTags(ReplaceableTagEvent event) {

        // TODO: Fill in rest of offline-player compatible tags

        // These tags require an offline player.
        if (!event.matches("PLAYER") || !event.hasOfflinePlayer()) return;

        OfflinePlayer p = event.getOfflinePlayer();
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";

        if (type.equals("HAS_PLAYED_BEFORE"))
            event.setReplaced(String.valueOf(p.hasPlayedBefore()));

        else if (type.equals("IS_OP"))
            event.setReplaced(String.valueOf(p.isOp()));

        else if (type.equals("IS_BANNED"))
            event.setReplaced(String.valueOf(p.isBanned()));
        
        else if (type.equals("IS_ONLINE"))
            event.setReplaced(String.valueOf(p.isOnline()));
        
        else if (type.equals("CHAT_HISTORY")) {
            if (event.hasTypeContext()) {
                if (aH.matchesInteger(event.getTypeContext())) {
                    // Check that player has history
                    if (PlayerTags.playerChatHistory.containsKey(p.getName())) {
                        List<String> history = PlayerTags.playerChatHistory.get(p.getName());
                        if (history.size() < aH.getIntegerFrom(event.getTypeContext()))
                            event.setReplaced(history.get(history.size() - 1));
                        else event.setReplaced(history.get(aH.getIntegerFrom(event.getTypeContext()) - 1));
                    }
                }

            } else {
                if (PlayerTags.playerChatHistory.containsKey(p.getName())) {
                    event.setReplaced(PlayerTags.playerChatHistory.get(p.getName()).get(0));
                }
            }

        } else if (type.equals("NAME")) {
            event.setReplaced(p.getName());

        } else if (type.equals("LOCATION")) {
            if (subType.equals("BED_SPAWN"))
            	if (p.getBedSpawnLocation() != null)
            	{
            		event.setReplaced(p.getBedSpawnLocation().getX()
            				+ "," + p.getBedSpawnLocation().getY()
            				+ "," + p.getBedSpawnLocation().getZ()
            				+ "," + p.getBedSpawnLocation().getWorld().getName());
            	}

        } else if (type.equals("MONEY")) {
            if(Depends.economy != null) {
                event.setReplaced(String.valueOf(Depends.economy.getBalance(p.getName())));
                if (subType.equals("ASINT"))
                    event.setReplaced(String.valueOf((int)Depends.economy.getBalance(p.getName())));
                else if (subType.equals("CURRENCY_SINGULAR"))
                    event.setReplaced(Depends.economy.currencyNameSingular());
                else if (subType.equals("CURRENCY_PLURAL"))
                    event.setReplaced(Depends.economy.currencyNamePlural());
            } else {
                dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
            }

        } else if (event.getType().startsWith("XP")) {
            event.setReplaced(String.valueOf(event.getPlayer().getExp() * 100));
        }

    }
}
