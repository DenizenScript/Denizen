package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import net.minecraft.server.v1_4_5.NBTTagCompound;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlayerTags implements Listener {

    public PlayerTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void playerTags(ReplaceableTagEvent event) {

        if (!event.matches("PLAYER") || event.getPlayer() == null) return;

        Player p = event.getPlayer();
        String type = event.getType().toUpperCase();
        String subType = "";

        if (event.getType().split("\\.").length > 1) {
            type = event.getType().split("\\.")[0].toUpperCase();
            subType = event.getType().split("\\.")[1].toUpperCase();
        }

        if (type.equals("ITEM_IN_HAND")) {
            if (subType.equals("QTY"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getAmount()));
            else if (subType.equals("ID"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getTypeId()));
            else if (subType.equals("DURABILITY"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getDurability()));
            else if (subType.equals("DATA"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getData()));
            else if (subType.equals("MAX_STACK"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getMaxStackSize()));
            else if (subType.equals("ENCHANTMENTS"))
            	event.setReplaceable(NBTItem.getEnchantments(p.getItemInHand()).asDScriptList());
            else if (subType.equals("ENCHANTMENTS_WITH_LEVEL"))
                event.setReplaceable(NBTItem.getEnchantments(p.getItemInHand()).asDScriptListWithLevels());
            else if (subType.equals("ENCHANTMENTS_WITH_LEVEL_ONLY"))
                event.setReplaceable(NBTItem.getEnchantments(p.getItemInHand()).asDScriptListLevelsOnly());
            else if (subType.equals("LORE")) 
                event.setReplaceable(NBTItem.getLore(p.getItemInHand()).asDScriptList());
            else if (subType.equals("DISPLAY"))
                event.setReplaceable(NBTItem.getName(p.getItemInHand()));
            else if (subType.equals("MATERIAL"))
                event.setReplaceable(p.getItemInHand().getType().name());
            return;

            
        } else if (type.equals("NAME")) { 
            event.setReplaceable(p.getName());
            if (subType.equals("DISPLAY"))
                event.setReplaceable(p.getDisplayName());
            else if (subType.equals("LIST"))
                event.setReplaceable(p.getPlayerListName());
            return;

            
        } else if (type.equals("LOCATION")) {
            event.setReplaceable(p.getLocation().getBlockX() 
                    + "," + p.getLocation().getBlockY()
                    + "," + p.getLocation().getBlockZ()
                    + "," + p.getWorld().getName());
            if (subType.equals("FORMATTED")) 
                event.setReplaceable("X '" + p.getLocation().getBlockX() 
                        + "', Y '" + p.getLocation().getBlockY()
                        + "', Z '" + p.getLocation().getBlockZ()
                        + "', in world '" + p.getWorld().getName() + "'");
            else if (subType.equals("STANDING_ON"))
                event.setReplaceable(p.getLocation().add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaceable(p.getWorld().getSpawnLocation().getBlockX() 
                        + "," + p.getWorld().getSpawnLocation().getBlockY()
                        + "," + p.getWorld().getSpawnLocation().getBlockZ()
                        + "," + p.getWorld().getName());
            else if (subType.equals("BED_SPAWN"))
                event.setReplaceable(p.getBedSpawnLocation().getBlockX() 
                        + "," + p.getBedSpawnLocation().getBlockY()
                        + "," + p.getBedSpawnLocation().getBlockZ()
                        + "," + p.getWorld().getName());
            else if (subType.equals("WORLD"))
                event.setReplaceable(p.getWorld().getName());
            return;

            
        } else if (type.equals("HEALTH")) {
            event.setReplaceable(String.valueOf(p.getHealth()));
            if (subType.equals("FORMATTED")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split("\\.").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                if ((float)p.getHealth() / maxHealth < .10)
                    event.setReplaceable("dying");
                else if ((float) p.getHealth() / maxHealth < .40)
                    event.setReplaceable("seriously wounded");
                else if ((float) p.getHealth() / maxHealth < .75)
                    event.setReplaceable("injured");
                else if ((float) p.getHealth() / maxHealth < 1)
                    event.setReplaceable("scraped");
                else 
                    event.setReplaceable("healthy");
            } else if (subType.equals("PERCENTAGE")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split("\\.").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaceable(String.valueOf(((float) p.getHealth() / maxHealth) * 100));
            }

            
        } else if (type.equals("FOOD_LEVEL")) {
            event.setReplaceable(String.valueOf(p.getFoodLevel()));
            if (subType.equals("FORMATTED")) {
                int maxFood = 20;
                if (event.getType().split("\\.").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                if ((float)p.getHealth() / maxFood < .10)
                    event.setReplaceable("starving");
                else if ((float) p.getFoodLevel() / maxFood < .40)
                    event.setReplaceable("famished");
                else if ((float) p.getFoodLevel() / maxFood < .75)
                    event.setReplaceable("hungry");
                else if ((float) p.getFoodLevel() / maxFood < 1)
                    event.setReplaceable("parched");
                else 
                    event.setReplaceable("healthy");
            } else if (subType.equals("PERCENTAGE")) {
                int maxFood = 20;
                if (event.getType().split("\\.").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaceable(String.valueOf(((float) p.getFoodLevel() / maxFood) * 100));
            }


        } else if (event.getType().startsWith("EQUIPMENT")) {
            event.setReplaceable(String.valueOf(event.getNPC().getEntity().getHealth()));


        } else if (event.getType().startsWith("INVENTORY")) {
            event.setReplaceable(String.valueOf(event.getNPC().getEntity().getHealth()));


        } else if (event.getType().startsWith("XP")) {
            event.setReplaceable(String.valueOf(event.getPlayer().getExp() * 100));
            if (subType.equals("TO_NEXT_LEVEL"))
                event.setReplaceable(String.valueOf(p.getExpToLevel()));
            else if (subType.equals("TOTAL"))
                event.setReplaceable(String.valueOf(p.getTotalExperience()));
            else if (subType.equals("LEVEL"))
                event.setReplaceable(String.valueOf(p.getLevel()));
            return;

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

