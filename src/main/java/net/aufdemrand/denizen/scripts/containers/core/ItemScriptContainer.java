package net.aufdemrand.denizen.scripts.containers.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.LeatherColorer;

import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Item Script Containers
    // @group Script Container System
    // @description
    // Item script containers are an easy way to pre-define custom items for use within scripts. Item
    // scripts work with the dItem object, and can be fetched with the Object Fetcher by using the
    // dItem constructor i@item_script_name. Example: - drop <player.location> i@super_dooper_diamond
    //
    // The following is the format for the container. Except for the 'material' key (and the dScript
    // required 'type' key), all other keys are optional.
    //
    // <code>
    // # The name of the item script is the same name that you can use to construct a new
    // # dItem based on this item script. For example, an item script named 'sword of swiftness'
    // # can be referred to as 'i@sword of swiftness'.
    // Item Script Name:
    //
    //   type: item
    //
    //   # Must be a valid dItem (EG i@red_wool or i@potion,8226) See 'dItem' for more information.
    //   material: i@base_material
    //
    //   # The 'custom name' can be anything you wish. Use color tags to make colored custom names.
    //   display name: custom name
    //
    //   # Lore lines can make items extra unique. This is a list, so multiple entries will result in multiple lores.
    //   # If using a replaceable tag, they are filled in when the item script is given/created/dropped/etc.
    //   lore:
    //   - item
    //   - ...
    //
    //   # If you want an item to be damaged on creation, you can change its durability.
    //   durability: 12
    //
    //   # Each line must specify a valid Bukkit enchantment. See 'enchantments' for more information.
    //   enchantments:
    //   - enchantment_name:level
    //   - ...
    //
    //   # You can specify the items required to craft your item. For an empty slot, use i@air.
    //   recipe:
    //   - i@item|i@item|i@item
    //   - i@item|i@item|i@item
    //   - i@item|i@item|i@item
    //
    //   # You can specify a material that can be smelted into your item.
    //   # Note: This can overwrite existing furnace recipes.
    //   # Additional note: This does not support datavalues currently, only specific material types can be used as a recipe.
    //   furnace_recipe: i@item
    //
    //   # You can specify a list of materials that make up a shapeless recipe.
    //   # Note: This can overwrite existing shapeless recipes.
    //   shapeless_recipe: i@item|...
    //
    //   # Set to true to not store the scriptID on the item, treating it as an item dropped by any other plugin.
    //   # NOTE: THIS IS NOT RECOMMENDED UNLESS YOU HAVE A SPECIFIC REASON TO USE IT.
    //   no_id: true/false
    //
    //   # Set to true to not allow players to drop or otherwise lose this item besides by way of script
    //   # or plugin-related means.
    //   bound: true/false
    //
    //   # For colorable items, such as leather armor, you can specify a valid dColor to specify the item's appearance.
    //   # See 'dColor' for more information.
    //   color: co@color
    //
    //   # If your material is a 'm@written_book', you can specify a book script to automatically scribe your item
    //   # upon creation. See 'book script containers' for more information.
    //   book: book_script_name
    // </code>
    //
    // -->

    // A map storing special recipes that use itemscripts as ingredients
    public static Map<dItem, dList> specialrecipesMap = new HashMap<dItem, dList>();
    public static Map<dItem, dList> shapelessRecipesMap = new HashMap<dItem, dList>();

    dNPC npc = null;
    dPlayer player = null;
    public boolean bound = false;
    String hash = "";

    public ItemScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);

        ItemScriptHelper.item_scripts.put(getName(), this);
        ItemScriptHelper.item_scripts_by_hash_id.put(ItemScriptHelper.createItemScriptID(this), this);

        // Set Recipe
        if (contains("RECIPE")) {

            // Get recipe list from item script
            List<String> recipeList = getStringList("RECIPE");

            // Process all tags in list
            for (int n = 0; n < recipeList.size(); n++) {
                recipeList.set(n, TagManager.tag(recipeList.get(n), new BukkitTagContext(player, npc, false, null, dB.shouldDebug(this), new dScript(this))));
            }

            // Store every ingredient in a dList
            dList ingredients = new dList();

            for (String recipeRow : recipeList) {
                String[] elements = recipeRow.split("\\|", 3);

                for (String element : elements) {
                    ingredients.add(element.replaceAll("[iImM]@", ""));
                }
            }

            // Add the recipe to Denizen's item script recipe list so it
            // will be checked manually inside ItemScriptHelper
            specialrecipesMap.put(getItemFrom(), ingredients);

        }

        if (contains("SHAPELESS_RECIPE")) {
            String list = TagManager.tag(getString("SHAPELESS_RECIPE"), new BukkitTagContext(player, npc, false, null, dB.shouldDebug(this), new dScript(this)));
            dList actual_list = dList.valueOf(list);
            shapelessRecipesMap.put(getItemFrom(), actual_list);
        }

        if (contains("FURNACE_RECIPE")) {
            dItem furnace_item = dItem.valueOf(getString("FURNACE_RECIPE"));
            if (furnace_item == null) {
                dB.echoError("Invalid item '" + getString("FURNACE_RECIPE") + "'");
                return;
            }
            FurnaceRecipe recipe = new FurnaceRecipe(getItemFrom().getItemStack(), furnace_item.getMaterial().getMaterial(), furnace_item.getItemStack().getDurability());
            Bukkit.getServer().addRecipe(recipe);
        }
    }

    public String getHashID() {
        return hash;
    }

    public void setHashID(String HashID) {
        hash = HashID;
    }

    public dItem getItemFrom() {
       return getItemFrom(null, null);
    }

    public dItem getItemFrom(dPlayer player, dNPC npc) {
        // Try to use this script to make an item.
        dItem stack = null;
        try {
            boolean debug = true;
            if (contains("DEBUG")) {
                debug = Boolean.valueOf(getString("DEBUG"));
            }
            // Check validity of material
            if (contains("MATERIAL")){
                String material = TagManager.tag(getString("MATERIAL"), new BukkitTagContext(player, npc, false, null, debug, new dScript(this)));
                if (material.startsWith("m@"))
                    material = material.substring(2);
                stack = dItem.valueOf(material);
            }

            // Make sure we're working with a valid base ItemStack
            if (stack == null) return null;

            ItemMeta meta = stack.getItemStack().getItemMeta();
            List<String> lore = new ArrayList<String>();

            // Set Id of the first, invisible lore
            boolean hideLore = false;
            if (contains("NO_ID")) {
                hideLore = Boolean.valueOf(getString("NO_ID"));
            }
            if (!hideLore)
                lore.add(hash);

            // Set Display Name
            if (contains("DISPLAY NAME")){
                String displayName = TagManager.tag(getString("DISPLAY NAME"), new BukkitTagContext(player, npc, false, null, debug, new dScript(this)));
                meta.setDisplayName(displayName);
            }

            // Set if the object is bound to the player
            if (contains("BOUND")) {
                bound = Boolean.valueOf(TagManager.tag(getString("BOUND"), new BukkitTagContext(player, npc, false, null, debug, new dScript(this))));
            }

            // Set Lore
            if (contains("LORE")) {

                for (String l : getStringList("LORE")){
                     l = TagManager.tag(l, new BukkitTagContext(player, npc, false, null, debug, new dScript(this)));
                     lore.add(l);
                }
            }

            meta.setLore(lore);
            stack.getItemStack().setItemMeta(meta);

            // Set Durability
            if (contains("DURABILITY")) {
                short durability = Short.valueOf(getString("DURABILITY"));
                stack.setDurability(durability);
            }

            // Set Enchantments
            if (contains("ENCHANTMENTS")) {
                for (String enchantment : getStringList("ENCHANTMENTS")) {

                    enchantment = TagManager.tag(enchantment, new BukkitTagContext(player, npc, false, null, debug, new dScript(this)));
                    try {
                        // Build enchantment context
                        int level = 1;
                        String[] split = enchantment.split(":");
                        if (split.length > 1) {
                            level = Integer.valueOf(split[1].replace(" ", ""));
                            enchantment = split[0].replace(" ", "");
                        }
                        // Add enchantment
                        Enchantment ench = Enchantment.getByName(enchantment.toUpperCase());
                        stack.getItemStack().addUnsafeEnchantment(ench, level);
                    }
                    catch (Exception e) {
                        dB.echoError("While constructing '" + getName() + "', encountered error: '"
                                + enchantment + "' is an invalid enchantment!");
                    }
                }
            }

            // Set Color
            if (contains("COLOR"))
            {
                String color = TagManager.tag(getString("COLOR"), new BukkitTagContext(player, npc, false, null, debug, new dScript(this)));
                LeatherColorer.colorArmor(stack, color);
            }

            // Set Book
            if (contains("BOOK")) {
                BookScriptContainer book = ScriptRegistry
                        .getScriptContainer(TagManager.tag(getString("BOOK"),
                                new BukkitTagContext(player, npc, false, null, debug,
                                        new dScript(this))).replace("s@", ""));

                stack = book.writeBookTo(stack, player, npc);
            }

        } catch (Exception e) {
            dB.echoError("Woah! An exception has been called with this item script!");
            dB.echoError(e);
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
