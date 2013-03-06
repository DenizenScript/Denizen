package net.aufdemrand.denizen.scripts.containers.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.debugging.dB;
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

 //   public Item getItemFrom() {
 //       return getItemFrom(null, null);
 //   }

    public Item getItemFrom(Player player, dNPC npc) {
        // Try to use this script to make an item.
        Item stack = null;
        try {
            // Check validity of material
            if (contains("MATERIAL")){
            	String material = TagManager.tag(player, npc, getString("MATERIAL"));
                stack = Item.valueOf(material);
            }

            // Make sure we're working with a valid base ItemStack
            if (stack == null) return null;

            ItemMeta meta = stack.getItemMeta();

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
            	
            stack.setItemMeta(meta);

            // Set Enchantments
            if (contains("ENCHANTMENTS")) {
                for (String enchantment : getStringList("ENCHANTMENTS")) {
                    try {
                        // Build enchantment context
                        int level = 1;
                        if (enchantment.split(":").length > 1) {
                            level = Integer.valueOf(enchantment.split(":")[1]);
                            enchantment = TagManager.tag(player, npc, enchantment.split(":")[0]);
                        }
                        // Add enchantment
                        Enchantment ench = Enchantment.getByName(enchantment.toUpperCase());
                        stack.addEnchantment(ench, level);
                    } catch (Exception e) {
                        // Invalid enchantment information, let's try the next entry
                        continue;
                    }
                }
            }

            // Set Color
            if (contains("COLOR"))
                LeatherColorer.colorArmor(stack, getString("COLOR"));

            // Set Book
            if (contains("BOOK")) {
                BookScriptContainer book = ScriptRegistry
                        .getScriptContainerAs(getString("BOOK"), BookScriptContainer.class);

                stack = book.writeBookTo(stack, player, npc);
            }

            // Set Id of the stack
            stack.setId(getName());

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
