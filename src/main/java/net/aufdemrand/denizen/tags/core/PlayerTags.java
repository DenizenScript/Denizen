package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.Depends;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
        if (!event.matches("PLAYER") || event.getPlayer() == null) return;

        Player p = event.getPlayer();
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier().toUpperCase() : "";

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
            		// "1 shears" into "a pair of shears"
            		// etc.
            		String itemName = p.getItemInHand().getType().name().toLowerCase().replace('_', ' ');
            		int itemQty = p.getItemInHand().getAmount();
            		
            		if (itemName == "air")
                		event.setReplaced("nothing");
            		else if (itemName == "ice" || itemName == "dirt")
            			event.setReplaced(itemName);
            		else if (itemQty > 1)
            		{
            			if (itemName == "cactus")
            				event.setReplaced("cactuses");
            			else if (itemName.endsWith("y"))
            				// lily -> lilies
            				event.setReplaced(itemName.substring(0, itemName.length() - 1) + "ies");
            			else if (itemName.endsWith("s"))
            				// shears -> shears
            				event.setReplaced(itemName);
            			else
            				// iron sword -> iron swords
            				event.setReplaced(itemName + "s");
            		}
            		else
            		{
            			if (itemName == "cactus")
            				event.setReplaced("a cactus");
            			else if (itemName.endsWith("s"))
            				event.setReplaced(itemName);
            			else if (itemName.startsWith("a") ||
            					 itemName.startsWith("e") ||
            					 itemName.startsWith("i") ||
            					 itemName.startsWith("o") ||
            					 itemName.startsWith("u"))
            				// emerald -> an emerald
            				event.setReplaced("an " + itemName);
            			else
            				// diamond -> a diamond
            				event.setReplaced("a " + itemName);
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
                	else if (specifier.equals("PLURAL"))
                		event.setReplaced(Depends.economy.currencyNamePlural());
            } else {
                dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
            }

        } else if (type.equals("IS_OP")) {
            event.setReplaced(String.valueOf(p.isOp()));

        } else if (type.equals("IS_BANNED")) {
            event.setReplaced(String.valueOf(p.isBanned()));

        } else if (type.equals("IS_ONLINE")) {
            event.setReplaced(String.valueOf(p.isOnline()));
            
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
            
        } else if (event.getType().startsWith("EQUIPMENT")) {
            event.setReplaced(String.valueOf(event.getNPC().getEntity().getHealth()));


        } else if (event.getType().startsWith("INVENTORY")) {
            event.setReplaced(String.valueOf(event.getNPC().getEntity().getHealth()));

        } else if (event.getType().startsWith("XP")) {
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
