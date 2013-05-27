package net.aufdemrand.denizen.scripts.containers.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizen.utilities.nbt.LeatherColorer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemScriptContainer extends ScriptContainer {
	
	dNPC npc = null;
	Player player = null;
	
    public ItemScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

   public dItem getItemFrom() {
       return getItemFrom(null, null);
   }

    public dItem getItemFrom(Player player, dNPC npc) {
        // Try to use this script to make an item.
        dItem stack = null;
        try {
            // Check validity of material
            if (contains("MATERIAL")){
            	String material = TagManager.tag(player, npc, getString("MATERIAL"));
                stack = dItem.valueOf(material);
            }

            // Make sure we're working with a valid base ItemStack
            if (stack == null) return null;

            ItemMeta meta = stack.getItemStack().getItemMeta();

            // Set Display Name
            if (contains("DISPLAY NAME")){
            	String displayName = TagManager.tag(player, npc, getString("DISPLAY NAME"));
            	meta.setDisplayName(displayName);
            }

            // Set Lore
            if (contains("LORE")) {
            	List<String> taggedLore = new ArrayList<String>();
            	for (String l : getStringList("LORE")){
            		 l = TagManager.tag(player, npc, l);
            		 taggedLore.add(l);
            	}
                meta.setLore(taggedLore);
            }
            	
            stack.getItemStack().setItemMeta(meta);

            // Set Enchantments
            if (contains("ENCHANTMENTS")) {
                for (String enchantment : getStringList("ENCHANTMENTS")) {
                    
                	enchantment = TagManager.tag(player, npc, enchantment);
                	try {
                        // Build enchantment context
                        int level = 1;
                        if (enchantment.split(":").length > 1) {
                            level = Integer.valueOf(enchantment.split(":")[1]);
                            enchantment = enchantment.split(":")[0];
                        }
                        // Add enchantment
                        Enchantment ench = Enchantment.getByName(enchantment.toUpperCase());
                        stack.getItemStack().addEnchantment(ench, level);
                    } catch (Exception e) {
                        dB.echoError("While constructing '" + getName() + "', there has been a problem. '" + enchantment + "' is an invalid Enchantment!");
                        continue;
                    }
                }
            }

            // Set Color
            if (contains("COLOR"))
            {
            	String color = TagManager.tag(player, npc, getString("COLOR"));
                LeatherColorer.colorArmor(stack, color);
            }
                
            // Set Book
            if (contains("BOOK")) {
                BookScriptContainer book = ScriptRegistry
                        .getScriptContainerAs(getString("BOOK"), BookScriptContainer.class);

                stack = book.writeBookTo(stack, player, npc);
            }

            // Set Id of the stack
            stack.setItemStack(CustomNBT.addCustomNBT(stack.getItemStack(), "denizen-script-id", getName()));

        } catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this item script!");
            if (!dB.showStackTraces)
                dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
            else e.printStackTrace();
            stack = null;
        }

        return stack;
    }
    
    public void setNPC(dNPC npc) {
    	this.npc = npc;
    }
    
    public void setPlayer(Player player) {
    	this.player = player;
    }

}
