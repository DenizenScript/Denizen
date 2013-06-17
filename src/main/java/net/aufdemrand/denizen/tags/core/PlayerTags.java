package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.scripts.commands.core.FailCommand;
import net.aufdemrand.denizen.scripts.commands.core.FinishCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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

    public static Map<String, List<String>> playerChatHistory = new ConcurrentHashMap<String, List<String>>(8, 0.9f, 2);

    @EventHandler(priority = EventPriority.MONITOR)
    public void addMessage(AsyncPlayerChatEvent event) {
        List<String> history = playerChatHistory.get(event.getPlayer().getName());
        if (history == null) {
            history = new ArrayList<String>();
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
        String type = event.getType() != null ? event.getType() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext() : "";
        String subType = event.getSubType() != null ? event.getSubType() : "";
        String subTypeContext = event.getSubTypeContext() != null ? event.getSubTypeContext() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier() : "";
        String specifierContext = event.getSpecifierContext() != null ? event.getSpecifierContext() : "";

        if (type.equalsIgnoreCase("LIST"))
        {
            StringBuilder players = new StringBuilder();

            if (subType.equalsIgnoreCase("ONLINE"))
            {
                if (specifier.equalsIgnoreCase("OPS"))
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

            else if (subType.equalsIgnoreCase("OFFLINE"))
            {
                // Bukkit's idea of OfflinePlayers includes online players as well,
                // so get the list of online players and check against it
                StringBuilder onlinePlayers = new StringBuilder();

                for (Player player : Bukkit.getOnlinePlayers())
                {
                    onlinePlayers.append(player.getName());
                    onlinePlayers.append("|");
                }

                if (specifier.equalsIgnoreCase("OPS"))
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

        if (type.equalsIgnoreCase("CHAT_HISTORY")) {
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

        else if (type.equalsIgnoreCase("CLOSEST"))
        {
            int range = 100;

            if (aH.matchesInteger(typeContext))
                range = aH.getIntegerFrom(typeContext);

            if (subType.equalsIgnoreCase("NPC"))
            {
                if (specifier.equalsIgnoreCase("NAME"))
                    event.setReplaced(String.valueOf(Utilities.getClosestNPC(p.getLocation(), range).getName()));
                else
                    event.setReplaced(String.valueOf(Utilities.getClosestNPC(p.getLocation(), range).getId()));
            }
        }

        else if (type.equalsIgnoreCase("ITEM_IN_HAND")) {
            if (p.getItemInHand() != null) {
	        	if (subType.equalsIgnoreCase("QTY"))
	                event.setReplaced(String.valueOf(p.getItemInHand().getAmount()));
	            else if (subType.equalsIgnoreCase("ID"))
	                event.setReplaced(String.valueOf(p.getItemInHand().getTypeId()));
	            else if (subType.equalsIgnoreCase("DURABILITY"))
	                event.setReplaced(String.valueOf(p.getItemInHand().getDurability()));
	            else if (subType.equalsIgnoreCase("DATA"))
	                event.setReplaced(String.valueOf(p.getItemInHand().getData()));
	            else if (subType.equalsIgnoreCase("MAX_STACK"))
	                event.setReplaced(String.valueOf(p.getItemInHand().getMaxStackSize()));
	            else if (subType.equalsIgnoreCase("OWNER")) {
	                if (NBTItem.hasCustomNBT(p.getItemInHand(), "owner"))
	                    event.setReplaced(NBTItem.getCustomNBT(p.getItemInHand(), "owner"));
	            }
	            else if (subType.equalsIgnoreCase("ENCHANTMENTS"))
	            {
	                String enchantments = null;
	
	                if (specifier.equalsIgnoreCase("LEVELS"))
	                    enchantments = NBTItem.getEnchantments(p.getItemInHand()).asDScriptListWithLevels();
	                else if (specifier.equalsIgnoreCase("LEVELS_ONLY"))
	                    enchantments = NBTItem.getEnchantments(p.getItemInHand()).asDScriptListLevelsOnly();
	                else
	                    enchantments = NBTItem.getEnchantments(p.getItemInHand()).asDScriptList();
	
	                if (enchantments != null && enchantments.length() > 0)
	                    event.setReplaced(enchantments);
	            }
	            else if (subType.equalsIgnoreCase("LORE")) {
	                if (p.getItemInHand().hasItemMeta()) {
	                    if (p.getItemInHand().getItemMeta().hasLore()) {
	                        event.setReplaced(new dList(p.getItemInHand().getItemMeta().getLore()).dScriptArgValue());
	                    }
	                }
	            }
	            else if (subType.equalsIgnoreCase("DISPLAY"))
	                // This is the name set when using an anvil
	                event.setReplaced(p.getItemInHand().getItemMeta().getDisplayName());
	            else if (subType.equalsIgnoreCase("MATERIAL"))
	                if (specifier.equalsIgnoreCase("FORMATTED"))
	                {
	                    // Turn "1 iron sword" into "an iron sword"
	                    // "2 iron swords" into "iron swords"
	                    // "1 emerald" into "an emerald"
	                    // etc.
	                    String itemName = p.getItemInHand().getType().name().toLowerCase().replace('_', ' ');
	                    int itemQty = p.getItemInHand().getAmount();
	
	                    if (itemName.equalsIgnoreCase("air"))
	                    {
	                        event.setReplaced("nothing");
	                    }
	                    else if (itemName.equalsIgnoreCase("ice") || itemName.equalsIgnoreCase("dirt"))
	                    {
	                        event.setReplaced(itemName);
	                    }
	                    else if (itemQty > 1)
	                    {
	                        if (itemName.equalsIgnoreCase("cactus"))
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
	                        if (itemName.equalsIgnoreCase("cactus"))
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
	                } else event.setReplaced(p.getItemInHand().getType().name());
            }

        } else if (type.equalsIgnoreCase("NAME")) {
            event.setReplaced(p.getName());
            if (subType.equalsIgnoreCase("DISPLAY"))
                event.setReplaced(p.getDisplayName());
            else if (subType.equalsIgnoreCase("LIST"))
                event.setReplaced(p.getPlayerListName());


        } else if (type.equalsIgnoreCase("LOCATION")) {
            event.setReplaced(p.getLocation().getX()
                    + "," + p.getLocation().getY()
                    + "," + p.getLocation().getZ()
                    + "," + p.getWorld().getName());
            if (subType.equalsIgnoreCase("BLOCK"))
                event.setReplaced(p.getLocation().getBlockX()
                        + "," + p.getLocation().getBlockY()
                        + "," + p.getLocation().getBlockZ()
                        + "," + p.getWorld().getName());
            else if (subType.equalsIgnoreCase("FORMATTED"))
                event.setReplaced("X '" + p.getLocation().getX()
                        + "', Y '" + p.getLocation().getY()
                        + "', Z '" + p.getLocation().getZ()
                        + "', in world '" + p.getWorld().getName() + "'");
            else if (subType.equalsIgnoreCase("X"))
                event.setReplaced(String.valueOf(p.getLocation().getX()));
            else if (subType.equalsIgnoreCase("Y"))
                event.setReplaced(String.valueOf(p.getLocation().getY()));
            else if (subType.equalsIgnoreCase("Z"))
                event.setReplaced(String.valueOf(p.getLocation().getZ()));
            else if (subType.equalsIgnoreCase("WORLD"))
                event.setReplaced(p.getWorld().getName());
            else if (subType.equalsIgnoreCase("CURSOR_ON")) {
                int range = 50;
                if (aH.matchesInteger(subTypeContext))
                    range = aH.getIntegerFrom(subTypeContext);
                event.setReplaced(new dLocation(p.getTargetBlock(null, range).getLocation()).dScriptArgValue());
            }
            else if (subType.equalsIgnoreCase("STANDING_ON"))
                if (specifier.equalsIgnoreCase("FORMATTED"))
                    event.setReplaced(p.getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().replace('_', ' '));
                else
                    event.setReplaced(p.getLocation().add(0, -1, 0).getBlock().getType().name());
            else if (subType.equalsIgnoreCase("WORLD_SPAWN"))
                event.setReplaced(p.getWorld().getSpawnLocation().getX()
                        + "," + p.getWorld().getSpawnLocation().getY()
                        + "," + p.getWorld().getSpawnLocation().getZ()
                        + "," + p.getWorld().getName());
            else if (subType.equalsIgnoreCase("BED_SPAWN") && p.getBedSpawnLocation() != null)
                event.setReplaced(p.getBedSpawnLocation().getX()
                        + "," + p.getBedSpawnLocation().getY()
                        + "," + p.getBedSpawnLocation().getZ()
                        + "," + p.getWorld().getName());


        } else if (type.equalsIgnoreCase("HEALTH")) {
            event.setReplaced(String.valueOf(p.getHealth()));
            if (subType.equalsIgnoreCase("FORMATTED")) {
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
            } else if (subType.equalsIgnoreCase("PERCENTAGE")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split("\\.").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaced(String.valueOf(((float) p.getHealth() / maxHealth) * 100));
            }


        } else if (type.equalsIgnoreCase("FOOD_LEVEL")) {
            event.setReplaced(String.valueOf(p.getFoodLevel()));
            if (subType.equalsIgnoreCase("FORMATTED")) {
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
            } else if (subType.equalsIgnoreCase("PERCENTAGE")) {
                int maxFood = 20;
                if (event.getType().split("\\.").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaced(String.valueOf(((float) p.getFoodLevel() / maxFood) * 100));
            }

        } else if (type.equalsIgnoreCase("MONEY")) {
            if(Depends.economy != null) {
                event.setReplaced(String.valueOf(Depends.economy.getBalance(p.getName())));
                if (subType.equalsIgnoreCase("ASINT"))
                    event.setReplaced(String.valueOf((int)Depends.economy.getBalance(p.getName())));
                else if (subType.equalsIgnoreCase("CURRENCY"))
                    if (specifier.equalsIgnoreCase("SINGULAR"))
                        event.setReplaced(Depends.economy.currencyNameSingular());
                    else
                        event.setReplaced(Depends.economy.currencyNamePlural());
            } else {
                dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
            }

        } else if (type.equalsIgnoreCase("PERMISSION")) {
            if(Depends.permissions != null)
            {
                if (subType.equalsIgnoreCase("GLOBAL"))
                    event.setReplaced(String.valueOf(Depends.permissions.has((World) null, p.getName(), typeContext)));
                else if (subType.equalsIgnoreCase("WORLD"))
                    event.setReplaced(String.valueOf(Depends.permissions.has(subTypeContext, p.getName(), typeContext)));
                else
                    event.setReplaced(String.valueOf(Depends.permissions.has(p, typeContext)));
            }
            else
            {
                dB.echoError("Cannot check permission! No permissions loaded!");
            }

        } else if (type.equalsIgnoreCase("GROUP")) {
            if(Depends.permissions != null)
            {
                if (subType.equalsIgnoreCase("GLOBAL"))
                    event.setReplaced(String.valueOf(Depends.permissions.playerInGroup((World) null, p.getName(), typeContext)));
                else if (subType.equalsIgnoreCase("WORLD"))
                    event.setReplaced(String.valueOf(Depends.permissions.playerInGroup(subTypeContext, p.getName(), typeContext)));
                else
                    event.setReplaced(String.valueOf(Depends.permissions.playerInGroup(p, typeContext)));
            }
            else
            {
                dB.echoError("Cannot check group! No permissions loaded!");
            }

        } else if (type.equalsIgnoreCase("GAMEMODE")) {
            if (subType.equalsIgnoreCase("ID"))
                event.setReplaced(String.valueOf(p.getGameMode().getValue()));
            else
                event.setReplaced(String.valueOf(p.getGameMode().name()));

        } else if (type.equalsIgnoreCase("IS_OP")) {
            event.setReplaced(String.valueOf(p.isOp()));

        } else if (type.equalsIgnoreCase("IS_BANNED")) {
            event.setReplaced(String.valueOf(p.isBanned()));

        } else if (type.equalsIgnoreCase("IS_ONLINE")) {
            event.setReplaced(String.valueOf(p.isOnline()));

        } else if (type.equalsIgnoreCase("IS_FLYING")) {
            event.setReplaced(String.valueOf(p.isFlying()));

        } else if (type.equalsIgnoreCase("IS_SNEAKING")) {
            event.setReplaced(String.valueOf(p.isSneaking()));

        } else if (type.equalsIgnoreCase("TIME")) {
            event.setReplaced(String.valueOf(p.getPlayerTime()));
            if (subType.equalsIgnoreCase("PERIOD"))
            {
                if (p.getPlayerTime() >= 23000)
                    event.setReplaced("dawn");
                else if (p.getPlayerTime() >= 13500)
                    event.setReplaced("night");
                else if (p.getPlayerTime() >= 12500)
                    event.setReplaced("dusk");
                else
                    event.setReplaced("day");
            }

        } else if (type.equalsIgnoreCase("WEATHER")) {
            if (p.getWorld().hasStorm())
                event.setReplaced("storming");
            else
            if (p.getPlayerTime() > 13500)
                event.setReplaced("clear");
            else event.setReplaced("sunny");


        } else if (type.equalsIgnoreCase("EQUIPMENT")) {
            if (subType.equalsIgnoreCase("BOOTS") && p.getInventory().getBoots() != null) {
            	if (specifier.equalsIgnoreCase("DISPLAYNAME")) {
            		event.setReplaced(p.getInventory().getBoots().getItemMeta().getDisplayName());
            	} else event.setReplaced(p.getInventory().getBoots().getType().name());
            	
            } else if (subType.equalsIgnoreCase("CHESTPLATE") && p.getInventory().getChestplate() != null) {
            	if (specifier.equalsIgnoreCase("DISPLAYNAME")) {
            		event.setReplaced(p.getInventory().getChestplate().getItemMeta().getDisplayName());
            	} else event.setReplaced(p.getInventory().getChestplate().getType().name());
            	
            } else if (subType.equalsIgnoreCase("HELMET") && p.getInventory().getHelmet() != null) {
            	if (specifier.equalsIgnoreCase("DISPLAYNAME")) {
            		event.setReplaced(p.getInventory().getHelmet().getItemMeta().getDisplayName());
            	} else event.setReplaced(p.getInventory().getHelmet().getType().name());
            	
            } else if (subType.equalsIgnoreCase("LEGGINGS") && p.getInventory().getLeggings() != null) {
            	if (specifier.equalsIgnoreCase("DISPLAYNAME")) {
            		event.setReplaced(p.getInventory().getLeggings().getItemMeta().getDisplayName());
            	} else event.setReplaced(p.getInventory().getLeggings().getType().name());
            	
            } else
                event.setReplaced("NOTHING");
            
            if (specifier.equalsIgnoreCase("FORMATTED"))
                event.setReplaced(event.getReplaced().toLowerCase().replace('_', ' '));

        } else if (type.equalsIgnoreCase("SCRIPT")) {

            if (aH.matchesScript("script:" + typeContext))
            {
                int times = 0;

                if (subType.equalsIgnoreCase("FINISHED"))
                {
                    times = FinishCommand.getScriptCompletes(p.getName(), aH.getStringFrom(typeContext).toUpperCase());
                }
                else if (subType.equalsIgnoreCase("FAILED"))
                {
                    times = FailCommand.getScriptFails(p.getName(), aH.getStringFrom(typeContext).toUpperCase());
                }

                if (times > 0)
                    event.setReplaced("true");
                else
                    event.setReplaced("false");
            }


        } else if (type.equalsIgnoreCase("INVENTORY")) {
            if (subType.equalsIgnoreCase("CONTAINS"))
            {
                if (specifier.equalsIgnoreCase("DISPLAY"))
                {
                    // Check if an item with this display name (set on an anvil)
                    // exists in this player's inventory

                    for (ItemStack itemstack : event.getPlayer().getInventory().getContents())
                    {
                        if (itemstack != null && itemstack.getItemMeta().getDisplayName() != null)
                        {
                            if (itemstack.getItemMeta().getDisplayName().equalsIgnoreCase(specifierContext))
                            {
                                event.setReplaced("true");
                                return;
                            }
                        }
                    }

                    for (ItemStack itemstack : event.getPlayer().getInventory().getArmorContents())
                    {
                        if (itemstack.getType().name() != "AIR" && itemstack.getItemMeta().getDisplayName() != null)
                        {
                            if (itemstack.getItemMeta().getDisplayName().equalsIgnoreCase(specifierContext))
                            {
                                event.setReplaced("true");
                                return;
                            }
                        }
                    }

                    event.setReplaced("false");
                }
                else if (aH.matchesItem("item:" + subTypeContext))
                {
                    ItemStack item = aH.getItemFrom("item:" + subTypeContext).getItemStack();

                    if (specifier.equalsIgnoreCase("QTY") && (aH.matchesQuantity("qty:" + specifierContext)))
                    {
                        int qty = aH.getIntegerFrom(specifierContext);

                        event.setReplaced(String.valueOf(event.getPlayer().getInventory().containsAtLeast(item, qty)));
                    }
                    else
                        event.setReplaced(String.valueOf(event.getPlayer().getInventory().containsAtLeast(item, 1)));
                }
            }
            else if (subType.equalsIgnoreCase("QTY"))
            {
                int qty = 0;

                if (aH.matchesItem("item:" + subTypeContext))
                {
                    ItemStack item = new ItemStack(aH.getItemFrom("item:" + subTypeContext).getItemStack());

                    qty = Utilities.countItems(item, event.getPlayer().getInventory());

                    for (ItemStack itemstack : event.getPlayer().getInventory().getArmorContents())
                    {
                        // If ItemStacks are empty here, they are AIR
                        if (itemstack.getType().name() != "AIR")
                        {
                            if (itemstack.isSimilar(item))
                                qty = qty + itemstack.getAmount();
                        }
                    }
                }
                else // Add up the quantities of all itemstacks
                {
                    qty = Utilities.countItems(event.getPlayer().getInventory());

                    for (ItemStack itemstack : event.getPlayer().getInventory().getArmorContents())
                    {
                        // If ItemStacks are empty here, they are AIR
                        if (itemstack.getType().name() != "AIR")
                            qty = qty + itemstack.getAmount();
                    }
                }

                event.setReplaced(String.valueOf(qty));
            }
            else if (subType.equalsIgnoreCase("STACKS"))
            {
                int qty = 0;

                for (ItemStack itemstack : event.getPlayer().getInventory().getContents())
                {
                    // If ItemStacks are empty here, they are null
                    if (itemstack != null)
                        qty++;
                }

                for (ItemStack itemstack : event.getPlayer().getInventory().getArmorContents())
                {
                    // If ItemStacks are empty here, they are AIR
                    if (itemstack.getType().name() != "AIR")
                        qty++;
                }

                event.setReplaced(String.valueOf(qty));
            }


        } else if (type.equalsIgnoreCase("XP")) {
            event.setReplaced(String.valueOf(event.getPlayer().getExp() * 100));
            if (subType.equalsIgnoreCase("TO_NEXT_LEVEL"))
                event.setReplaced(String.valueOf(p.getExpToLevel()));
            else if (subType.equalsIgnoreCase("TOTAL"))
                event.setReplaced(String.valueOf(p.getTotalExperience()));
            else if (subType.equalsIgnoreCase("LEVEL"))
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
