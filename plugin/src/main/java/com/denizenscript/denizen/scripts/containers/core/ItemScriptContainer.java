package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.LeatherColorer;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dScript;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    //   # List any mechanisms you want to apply to the item within
    //   mechanisms:
    //     # An example of a mechanism to apply
    //     unbreakable: true
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
    //   # If no_id is specified, only the material/data pair will be validated.
    //   # This might misbehave with some smelting systems, as the Minecraft smelting logic may refuse
    //   # To continue smelting items in some cases when the script validator gets in the way.
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
    public static Map<ItemScriptContainer, List<dItem>> specialrecipesMap = new HashMap<>();
    public static Map<ItemScriptContainer, List<dItem>> shapelessRecipesMap = new HashMap<>();

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

            // Process later so that any item script ingredients can be fulfilled
            ItemScriptHelper.recipes_to_register.put(this, recipeList);

        }

        if (contains("SHAPELESS_RECIPE")) {
            ItemScriptHelper.shapeless_to_register.put(this, getString("SHAPELESS_RECIPE"));
        }

        if (contains("FURNACE_RECIPE")) {
            // Process later so that any item script ingredients can be fulfilled
            ItemScriptHelper.furnace_to_register.put(this, getString("FURNACE_RECIPE"));
        }
    }

    private dItem cleanReference;

    public dItem getCleanReference() {
        if (cleanReference == null) {
            cleanReference = getItemFrom();
        }
        return new dItem(cleanReference.getItemStack().clone());
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

    public static SlowWarning boundWarning = new SlowWarning("Item script 'bound' functionality has never been reliable and should not be used. Consider replicating the concept with world events.");

    public dItem getItemFrom(dPlayer player, dNPC npc) {
        // Try to use this script to make an item.
        dItem stack = null;
        try {
            boolean debug = true;
            if (contains("DEBUG")) {
                debug = Boolean.valueOf(getString("DEBUG"));
            }
            BukkitTagContext context = new BukkitTagContext(player, npc, false, null, debug, new dScript(this));
            // Check validity of material
            if (contains("MATERIAL")) {
                String material = TagManager.tag(getString("MATERIAL"), context);
                if (material.startsWith("m@")) {
                    material = material.substring(2);
                }
                stack = dItem.valueOf(material, this);
            }

            // Make sure we're working with a valid base ItemStack
            if (stack == null) {
                return null;
            }

            // Handle listed mechanisms
            if (contains("MECHANISMS")) {
                YamlConfiguration mechs = getConfigurationSection("MECHANISMS");
                for (StringHolder key : mechs.getKeys(false)) {
                    String val = TagManager.tag(mechs.getString(key.str), context);
                    stack.safeAdjust(new Mechanism(new Element(key.low), new Element(val), context));
                }
            }

            ItemMeta meta = stack.getItemStack().getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            // Set Display Name
            if (contains("DISPLAY NAME")) {
                String displayName = TagManager.tag(getString("DISPLAY NAME"), context);
                meta.setDisplayName(displayName);
            }

            // Set if the object is bound to the player
            if (contains("BOUND")) {
                boundWarning.warn();
                bound = Boolean.valueOf(TagManager.tag(getString("BOUND"), context));
            }

            // Set Lore
            if (contains("LORE")) {

                for (String l : getStringList("LORE")) {
                    l = TagManager.tag(l, context);
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

                    enchantment = TagManager.tag(enchantment, context);
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
                        Debug.echoError("While constructing '" + getName() + "', encountered error: '"
                                + enchantment + "' is an invalid enchantment!");
                    }
                }
            }

            // Set Color
            if (contains("COLOR")) {
                String color = TagManager.tag(getString("COLOR"), context);
                LeatherColorer.colorArmor(stack, color);
            }

            // Set Book
            if (contains("BOOK")) {
                BookScriptContainer book = ScriptRegistry
                        .getScriptContainer(TagManager.tag(getString("BOOK"),
                                context).replace("s@", ""));

                stack = book.writeBookTo(stack, player, npc);
            }

            stack.setItemScript(this);
        }
        catch (Exception e) {
            Debug.echoError("Woah! An exception has been called with this item script!");
            Debug.echoError(e);
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
