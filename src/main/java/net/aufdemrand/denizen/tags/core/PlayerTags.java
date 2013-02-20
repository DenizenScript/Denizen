package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.depends.MCMMOUtilities;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTags implements Listener {

    public PlayerTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    public static Map<String, List<String>> playerChatHistory = new ConcurrentHashMap<String, List<String>>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void addMessage(AsyncPlayerChatEvent event) {
        List<String> history = new ArrayList<String>();
        if (playerChatHistory.containsKey(event.getPlayer().getName())) {
            history = playerChatHistory.get(event.getPlayer().getName());
        }

        if (history.size() > 10) history.remove(9);
        history.add(0, event.getMessage());

        playerChatHistory.put(event.getPlayer().getName(), history);
    }

    @EventHandler
    public void playerTags(ReplaceableTagEvent event) {

        // These tags require a player.
        if (!event.matches("PLAYER")) return;

        Player p = event.getPlayer();
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext().toUpperCase() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";
        String subTypeContext = event.getSubTypeContext() != null ? event.getSubTypeContext().toUpperCase() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier().toUpperCase() : "";  
        String specifierContext = event.getSpecifierContext() != null ? event.getSpecifierContext().toUpperCase() : "";
        
        if (type.equals("LIST"))
        {
        	StringBuilder players = new StringBuilder();
        	
        	if (subType.equals("ONLINE"))
        	{
        		if (specifier.equals("OPS"))
        		{
        			for (Player player : Bukkit.getOnlinePlayers())
                    {
                        if (player.isOp())
                        {
                            players.append(player.getName());
                            players.append("|");
                        }
                    }
        		}
        		else
                {   
                    for (Player player : Bukkit.getOnlinePlayers())
                    {
                        players.append(player.getName());
                        players.append("|");
                    }
                }
        	}
        	
        	else if (subType.equals("OFFLINE"))
        	{
        		// Bukkit's idea of OfflinePlayers includes online players as well,
        		// so get the list of online players and check against it
        		StringBuilder onlinePlayers = new StringBuilder();
        		
        		for (Player player : Bukkit.getOnlinePlayers())
                {
                    onlinePlayers.append(player.getName());
                    onlinePlayers.append("|");
                }
        		
        		if (specifier.equals("OPS"))
        		{	
        			for (OfflinePlayer player : Bukkit.getOfflinePlayers())
        			{
                        if (player.isOp() && !onlinePlayers.toString().contains(player.getName()))
                        {
                            players.append(player.getName());
                            players.append("|");
                        }
                    }
        		}
        		else
        		{
        			for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                    {        				
        				if (!onlinePlayers.toString().contains(player.getName()))
        				{	
        					players.append(player.getName());
                        	players.append("|");
        				}
                    }
        		}
        			
        	}
        	else // Get both online and offline players
        	{
        		for (OfflinePlayer player : Bukkit.getOfflinePlayers())
                {        				
    				players.append(player.getName());
                    players.append("|");
                }
        	}
        
        	event.setReplaced(players.toString().substring(0, players.length() - 1));
        	return;
        }
        
        if (event.getPlayer() == null) return;

        if (type.equals("CHAT_HISTORY")) {
            if (event.hasTypeContext()) {
                if (aH.matchesInteger(event.getTypeContext())) {
                    // Check that player has history
                    if (playerChatHistory.containsKey(event.getPlayer().getName())) {
                        List<String> history = playerChatHistory.get(event.getPlayer().getName());
                        if (history.size() < aH.getIntegerFrom(event.getTypeContext()))
                            event.setReplaced(history.get(history.size() - 1));
                        else event.setReplaced(history.get(aH.getIntegerFrom(event.getTypeContext()) - 1));
                    }
                }

            } else {
                if (playerChatHistory.containsKey(event.getPlayer().getName())) {
                    event.setReplaced(playerChatHistory.get(event.getPlayer().getName()).get(0));
                }
            }
        }

        else if (type.equals("ITEM_IN_HAND")) {
            if (subType.equals("QTY"))
                event.setReplaced(String.valueOf(p.getItemInHand().getAmount()));
            else if (subType.equals("ID"))
                event.setReplaced(String.valueOf(p.getItemInHand().getTypeId()));
            else if (subType.equals("DURABILITY"))
                event.setReplaced(String.valueOf(p.getItemInHand().getDurability()));
            else if (subType.equals("DATA"))
                event.setReplaced(String.valueOf(p.getItemInHand().getData()));
            else if (subType.equals("MAX_STACK"))
                event.setReplaced(String.valueOf(p.getItemInHand().getMaxStackSize()));
            else if (subType.equals("ENCHANTMENTS"))
                event.setReplaced(NBTItem.getEnchantments(p.getItemInHand()).asDScriptList());
            else if (subType.equals("ENCHANTMENTS_WITH_LEVEL"))
                event.setReplaced(NBTItem.getEnchantments(p.getItemInHand()).asDScriptListWithLevels());
            else if (subType.equals("ENCHANTMENTS_WITH_LEVEL_ONLY"))
                event.setReplaced(NBTItem.getEnchantments(p.getItemInHand()).asDScriptListLevelsOnly());
            else if (subType.equals("LORE"))
                event.setReplaced(NBTItem.getLore(p.getItemInHand()).asDScriptList());
            else if (subType.equals("DISPLAY"))
                // This is the name set when using an anvil
                event.setReplaced(p.getItemInHand().getItemMeta().getDisplayName());
            else if (subType.equals("MATERIAL"))
                if (specifier.equals("FORMATTED"))
                {
                    // Turn "1 iron sword" into "an iron sword"
                    // "2 iron swords" into "iron swords"
                    // "1 emerald" into "an emerald"
                    // etc.
                    String itemName = p.getItemInHand().getType().name().toLowerCase().replace('_', ' ');
                    int itemQty = p.getItemInHand().getAmount();

                    if (itemName.equals("air"))
                    {
                        event.setReplaced("nothing");
                    }
                    else if (itemName.equals("ice") || itemName.equals("dirt"))
                    {
                        event.setReplaced(itemName);
                    }
                    else if (itemQty > 1)
                    {
                        if (itemName.equals("cactus"))
                            event.setReplaced("cactuses");
                        else if (itemName.endsWith("y"))
                            event.setReplaced(itemName.substring(0, itemName.length() - 1) + "ies"); // lily -> lilies
                        else if (itemName.endsWith("s"))
                            event.setReplaced(itemName); // shears -> shears
                        else
                            event.setReplaced(itemName + "s"); // iron sword -> iron swords
                    }
                    else
                    {
                        if (itemName.equals("cactus"))
                            event.setReplaced("a cactus");
                        else if (itemName.endsWith("s"))
                            event.setReplaced(itemName);
                        else if (itemName.startsWith("a") ||
                                itemName.startsWith("e") ||
                                itemName.startsWith("i") ||
                                itemName.startsWith("o") ||
                                itemName.startsWith("u"))
                            event.setReplaced("an " + itemName); // emerald -> an emerald
                        else
                            event.setReplaced("a " + itemName); // diamond -> a diamond
                    }
                }
                else
                    event.setReplaced(p.getItemInHand().getType().name());


        } else if (type.equals("NAME")) {
            event.setReplaced(p.getName());
            if (subType.equals("DISPLAY"))
                event.setReplaced(p.getDisplayName());
            else if (subType.equals("LIST"))
                event.setReplaced(p.getPlayerListName());


        } else if (type.equals("LOCATION")) {
            event.setReplaced(p.getLocation().getBlockX()
                    + "," + p.getLocation().getBlockY()
                    + "," + p.getLocation().getBlockZ()
                    + "," + p.getWorld().getName());
            if (subType.equals("FORMATTED"))
                event.setReplaced("X '" + p.getLocation().getBlockX()
                        + "', Y '" + p.getLocation().getBlockY()
                        + "', Z '" + p.getLocation().getBlockZ()
                        + "', in world '" + p.getWorld().getName() + "'");
            else if (subType.equals("X"))
                event.setReplaced(String.valueOf(p.getLocation().getBlockX()));
            else if (subType.equals("Y"))
                event.setReplaced(String.valueOf(p.getLocation().getBlockY()));
            else if (subType.equals("Z"))
                event.setReplaced(String.valueOf(p.getLocation().getBlockZ()));
            else if (subType.equals("WORLD"))
                event.setReplaced(p.getWorld().getName());
            else if (subType.equals("STANDING_ON"))
                if (specifier.equals("FORMATTED"))
                    event.setReplaced(p.getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().replace('_', ' '));
                else
                    event.setReplaced(p.getLocation().add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaced(p.getWorld().getSpawnLocation().getBlockX()
                        + "," + p.getWorld().getSpawnLocation().getBlockY()
                        + "," + p.getWorld().getSpawnLocation().getBlockZ()
                        + "," + p.getWorld().getName());
            else if (subType.equals("BED_SPAWN"))
                event.setReplaced(p.getBedSpawnLocation().getBlockX()
                        + "," + p.getBedSpawnLocation().getBlockY()
                        + "," + p.getBedSpawnLocation().getBlockZ()
                        + "," + p.getWorld().getName());


        } else if (type.equals("HEALTH")) {
            event.setReplaced(String.valueOf(p.getHealth()));
            if (subType.equals("FORMATTED")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split("\\.").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                if ((float)p.getHealth() / maxHealth < .10)
                    event.setReplaced("dying");
                else if ((float) p.getHealth() / maxHealth < .40)
                    event.setReplaced("seriously wounded");
                else if ((float) p.getHealth() / maxHealth < .75)
                    event.setReplaced("injured");
                else if ((float) p.getHealth() / maxHealth < 1)
                    event.setReplaced("scraped");
                else
                    event.setReplaced("healthy");
            } else if (subType.equals("PERCENTAGE")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split("\\.").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaced(String.valueOf(((float) p.getHealth() / maxHealth) * 100));
            }


        } else if (type.equals("FOOD_LEVEL")) {
            event.setReplaced(String.valueOf(p.getFoodLevel()));
            if (subType.equals("FORMATTED")) {
                int maxFood = 20;
                if (event.getType().split("\\.").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                if ((float)p.getHealth() / maxFood < .10)
                    event.setReplaced("starving");
                else if ((float) p.getFoodLevel() / maxFood < .40)
                    event.setReplaced("famished");
                else if ((float) p.getFoodLevel() / maxFood < .75)
                    event.setReplaced("hungry");
                else if ((float) p.getFoodLevel() / maxFood < 1)
                    event.setReplaced("parched");
                else
                    event.setReplaced("healthy");
            } else if (subType.equals("PERCENTAGE")) {
                int maxFood = 20;
                if (event.getType().split("\\.").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaced(String.valueOf(((float) p.getFoodLevel() / maxFood) * 100));
            }
            
        } else if (type.equals("MONEY")) {
            if(Depends.economy != null) {
                event.setReplaced(String.valueOf(Depends.economy.getBalance(p.getName())));
                if (subType.equals("ASINT"))
                    event.setReplaced(String.valueOf((int)Depends.economy.getBalance(p.getName())));
                else if (subType.equals("CURRENCY"))
                    if (specifier.equals("SINGULAR"))
                        event.setReplaced(Depends.economy.currencyNameSingular());
                    else
                        event.setReplaced(Depends.economy.currencyNamePlural());
            } else {
                dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
            }
            
        } else if (type.equals("PERMISSION")) {
            if(Depends.permissions != null)
            {
                event.setReplaced(String.valueOf(Depends.permissions.has(p, typeContext)));
            }
            else
            {
                dB.echoError("Cannot check permission! No permissions loaded!");
            }
            
        } else if (type.equals("GROUP")) {
            if(Depends.permissions != null)
            {
                event.setReplaced(String.valueOf(Depends.permissions.playerInGroup(p, typeContext)));
            }
            else
            {
                dB.echoError("Cannot check group! No permissions loaded!");
            }    
            
        } else if (type.equals("MCMMO")) {
            if(Depends.mcmmo != null) {
                if (subType.equals("LEVEL")) {
            	    event.setReplaced(String.valueOf(MCMMOUtilities.getPlayerSkillLevel(p.getName(), subTypeContext)));
                }
            } else {
                dB.echoError("mcMMO not loaded! Have you installed the mcMMO plugin?");
            }
            
        } else if (type.equals("GAMEMODE")) {
            event.setReplaced(String.valueOf(p.getGameMode().name()));

        } else if (type.equals("IS_OP")) {
            event.setReplaced(String.valueOf(p.isOp()));

        } else if (type.equals("IS_BANNED")) {
            event.setReplaced(String.valueOf(p.isBanned()));

        } else if (type.equals("IS_ONLINE")) {
            event.setReplaced(String.valueOf(p.isOnline()));
        
        } else if (type.equals("IS_FLYING")) {
            event.setReplaced(String.valueOf(p.isFlying()));
            
        } else if (type.equals("IS_SNEAKING")) {
            event.setReplaced(String.valueOf(p.isSneaking()));

        } else if (type.equals("TIME")) {
            event.setReplaced(String.valueOf(p.getPlayerTime()));
            if (subType.equals("PERIOD"))
                if (p.getPlayerTime() < 13500 || p.getPlayerTime() > 23000)
                    event.setReplaced("day");
                else if (p.getPlayerTime() > 13500)
                    event.setReplaced("night");

        } else if (type.equals("WEATHER")) {
            if (p.getWorld().hasStorm())
                event.setReplaced("storming");
            else
            if (p.getPlayerTime() > 13500)
                event.setReplaced("clear");
            else event.setReplaced("sunny");


        } else if (type.equals("EQUIPMENT")) {
        	if (subType.equals("BOOTS"))
        		event.setReplaced(p.getInventory().getBoots().getType().name());
        	else if (subType.equals("CHESTPLATE"))
        		event.setReplaced(p.getInventory().getChestplate().getType().name());
        	else if (subType.equals("HELMET"))
        		event.setReplaced(p.getInventory().getHelmet().getType().name());
        	else if (subType.equals("LEGGINGS"))
        		event.setReplaced(p.getInventory().getLeggings().getType().name());
        	if (specifier.equals("FORMATTED"))
        		event.setReplaced(event.getReplaced().toLowerCase().replace('_', ' '));


        } else if (type.equals("INVENTORY")) {
        	if (subType.equals("CONTAINS") && (aH.matchesItem("item:" + subTypeContext)))
        	{
        		ItemStack item = aH.getItemFrom("item:" + subTypeContext);
        		
        		if (specifier.equals("QTY") && (aH.matchesQuantity("qty:" + specifierContext)))
        		{
        			int qty = aH.getIntegerFrom(specifierContext);
        			
        			event.setReplaced(String.valueOf(event.getPlayer().getInventory().containsAtLeast(item, qty)));
        		}
        		else
        			event.setReplaced(String.valueOf(event.getPlayer().getInventory().containsAtLeast(item, 1)));
        	}

        } else if (type.equals("XP")) {
            event.setReplaced(String.valueOf(event.getPlayer().getExp() * 100));
            if (subType.equals("TO_NEXT_LEVEL"))
                event.setReplaced(String.valueOf(p.getExpToLevel()));
            else if (subType.equals("TOTAL"))
                event.setReplaced(String.valueOf(p.getTotalExperience()));
            else if (subType.equals("LEVEL"))
                event.setReplaced(String.valueOf(p.getLevel()));

        }

    }

}



//    .replace("<^PLAYER.ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
//    .replace("<^PLAYER.ITEM_IN_HAND.NAME>", itemInHandName)
//    .replace("<^PLAYER.ITEM_IN_HAND.QTY>", itemInHandQty)
//    .replace("<^PLAYER.ITEM_IN_HAND.ID>", itemInHandId)
//    .replace("<^PLAYER.NAME>", thePlayer.getName())
//    .replace("<^PLAYER>", thePlayer.getName())
//    .replace("<^PLAYER.KILLER>", playerKiller)
//    .replace("<^PLAYER.HEALTH>", String.valueOf(thePlayer.getHealth()))
//    .replace("<^PLAYER.HELM>", playerHelm)
//    .replace("<^PLAYER.LEGGINGS>", playerLeggings)
//    .replace("<^PLAYER.BOOTS>", playerBoots)
//    .replace("<^PLAYER.CHESTPLATE>", playerChestplate)
//    .replace("<^PLAYER.WORLD>", thePlayer.getWorld().getName())
//    .replace("<^PLAYER.MONEY>", playerMoney)
//    .replace("<^PLAYER.EXP_TO_NEXT_LEVEL>", String.valueOf(thePlayer.getExpToLevel()))
//    .replace("<^PLAYER.EXP>", String.valueOf(thePlayer.getTotalExperience()))
//    .replace("<^PLAYER.FOOD_LEVEL>", String.valueOf(thePlayer.getFoodLevel()));*/
