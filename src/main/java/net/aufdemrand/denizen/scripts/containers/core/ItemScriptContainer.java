package net.aufdemrand.denizen.scripts.containers.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.LeatherColorer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemScriptContainer extends ScriptContainer {

    dNPC npc = null;
    dPlayer player = null;
    public boolean bound = false;

    public ItemScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        ItemScriptHelper.item_scripts.put(getName(), this);
        // Set Recipe
        if (contains("RECIPE")) {
            List<dItem> materials = new ArrayList<dItem>();
            for (String recipeRow : getStringList("RECIPE")) {
                recipeRow = TagManager.tag(player, npc, recipeRow);
                String[] row = recipeRow.split("\\|", 3);
                for (String material : row) {
                    materials.add(materials.size(), dItem.valueOf(material));
                    if (material.contains(":"))
                        materials.get(materials.size()-1).setData(Byte.valueOf(material.split(":")[1]));
                }
            }
            ShapedRecipe recipe = new ShapedRecipe(getItemFrom().getItemStack());
            recipe.shape("abc", "def", "ghi");
            char x = 'a';
            for (dItem material : materials) {
                if (!material.getItemStack().getType().name().equals("AIR"))
                    recipe.setIngredient(x, material.getItemStack().getData());
                x++;
            }
            Bukkit.getServer().addRecipe(recipe);
        }
    }

    public dItem getItemFrom() {
       return getItemFrom(null, null);
    }

    public dItem getItemFrom(dPlayer player, dNPC npc) {
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
            List<String> lore = new ArrayList<String>();

            // Set Id of the first, invisible lore
            lore.add("§0id:" + getName());

            // Set Display Name
            if (contains("DISPLAY NAME")){
                String displayName = TagManager.tag(player, npc, getString("DISPLAY NAME"));
                meta.setDisplayName(displayName);
            }

            // Set if the object is bound to the player
            if (contains("BOUND")) {
                bound = Boolean.valueOf(TagManager.tag(player, npc, getString("BOUND")));
            }

            // Set Lore
            if (contains("LORE")) {

                for (String l : getStringList("LORE")){
                     l = TagManager.tag(player, npc, l);
                     lore.add(l);
                }
            }

            meta.setLore(lore);
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
                        stack.getItemStack().addUnsafeEnchantment(ench, level);
                    } catch (Exception e) {
                        dB.echoError("While constructing '" + getName() + "', there has been a problem. '" + enchantment + "' is an invalid Enchantment!");
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

    public void setPlayer(dPlayer player) {
        this.player = player;
    }

}
