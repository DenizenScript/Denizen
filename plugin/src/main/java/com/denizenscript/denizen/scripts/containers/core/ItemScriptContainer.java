package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EnchantmentTag;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.LeatherColorer;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Material;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Item Script Containers
    // @group Script Container System
    // @description
    // Item script containers are an easy way to pre-define custom items for use within scripts. Item
    // scripts work with the ItemTag object, and can be fetched with the Object Fetcher by using the
    // ItemTag constructor ItemTag_script_name. Example: - drop <player.location> super_dooper_diamond
    //
    // The following is the format for the container. Except for the 'material' key (and the dScript
    // required 'type' key), all other keys are optional.
    //
    // <code>
    // # The name of the item script is the same name that you can use to construct a new
    // # ItemTag based on this item script. For example, an item script named 'sword_of_swiftness'
    // # can be referred to as simply 'sword_of_swiftness'.
    // Item_Script_Name:
    //
    //     type: item
    //
    //     # Must be a valid ItemTag. See 'ItemTag' for more information.
    //     # | All item scripts MUST have this key!
    //     material: base_material
    //
    //     # List any mechanisms you want to apply to the item within
    //     # | Some item scripts should have this key!
    //     mechanisms:
    //       # An example of a mechanism to apply
    //       unbreakable: true
    //       # Other common example
    //       custom_model_data: 5
    //       # This demonstrates how to add a custom attribute modifier.
    //       attribute_modifiers:
    //           # One subkey for each attribute you want to modify.
    //           # Valid attribute names are listed at https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html
    //           generic_armor:
    //               # Each attribute can have a list of modifiers, using numbered keys. They will be applied in numeric order, low to high.
    //               1:
    //                   # Each modifier requires keys 'operation' and 'amount', and can optionally have keys 'name', 'slot', and 'id'.
    //                   # Operations can be: ADD_NUMBER, ADD_SCALAR, and MULTIPLY_SCALAR_1
    //                   operation: add_number
    //                   amount: 5
    //                   # Slots can be: HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, ANY
    //                   slot: head
    //                   # ID is a UUID used to uniquely identify modifiers. If unspecified the ID will be randomly generated.
    //                   # Items with modifiers that lack IDs cannot be stacked due to the random generation.
    //                   id: 10000000-1000-1000-1000-100000000000
    //
    //     # The 'custom name' can be anything you wish. Use color tags to make colored custom names.
    //     # | Some item scripts should have this key!
    //     display name: custom name
    //
    //     # Lore lines can make items extra unique. This is a list, so multiple entries will result in multiple lores.
    //     # If using a replaceable tag, they are filled in when the item script is given/created/dropped/etc.
    //     # | Some item scripts should have this key!
    //     lore:
    //     - item
    //     - ...
    //
    //     # If you want an item to be damaged on creation, you can change its durability.
    //     # | Most item scripts should exclude this key!
    //     durability: 12
    //
    //     # Each line must specify a valid enchantment name.
    //     # | Some item scripts should have this key!
    //     enchantments:
    //     - enchantment_name:level
    //     - ...
    //
    //     # Set this to 'true' to allow the item script item to be used in material-based recipe (eg most vanilla recipes).
    //     # Defaults to false if unspecified.
    //     # | Most item scripts should exclude this key!
    //     allow in material recipes: false
    //
    //     # You can specify flags to be added to the item.
    //     flags:
    //       # Each line within the flags section should be a flag name as a key, and the flag value as the value.
    //       # You can use lists or maps here the way you would expect them to work.
    //       my_flag: my value
    //
    //     # You can optionally add crafting recipes for your item script.
    //     # Note that recipes won't show in the recipe book when you add a new item script, until you either reconnect or use the "resend_recipes" mechanism.
    //     # | Most item scripts should exclude this key, unless you're specifically building craftable items.
    //     recipes:
    //         1:
    //             # The type can be: shaped, shapeless, stonecutting, furnace, blast, smoker, campfire, or smithing.
    //             # | All recipes must include this key!
    //             type: shaped
    //             # The recipe can optionally have a custom internal recipe ID (for recipe books).
    //             # If not specified, will be of the form "<type>_<script.name>_<id>" where ID is the recipe list index (starting at 1, counting up).
    //             # IDs will always have the namespace "denizen". So, for the below, the full ID is "denizen:my_custom_item_id"
    //             # Note that most users should not set a custom ID. If you choose to set a custom one, be careful to avoid duplicates or invalid text.
    //             # Note that the internal rules for Recipe IDs are very strict (limited to "a-z", "0-9", "/", ".", "_", or "-").
    //             # | Most recipes should exclude this key.
    //             recipe_id: my_custom_item_id
    //             # You can optionally add a group as well. If unspecified, the item will have no group.
    //             # Groups are used to merge together similar recipes in the recipe book (in particular, multiple recipes for one item).
    //             # | Most recipes should exclude this key.
    //             group: my_custom_group
    //             # You can optionally specify the quantity to output. The default is 1 (or whatever the item script's quantity is).
    //             # | Only some recipes should have this key.
    //             output_quantity: 4
    //             # You must specify the input for the recipe. The below is a sample of a 3x3 shaped recipe. Other recipe types have a different format.
    //             # You are allowed to have non-3x3 shapes (can be any value 1-3 x 1-3, so for example 1x3, 2x1, and 2x2 are fine).
    //             # For an empty slot, use "air".
    //             # By default, items require an exact match. For a material-based match, use the format "material:MaterialNameHere" like "material:stick".
    //             # To make multiple different items match for any slot, just separate them with slashes, like "stick/stone".
    //             # To match multiple materials, use "material:a/b/c".
    //             # You can also make a dynamic matcher using '*', like "material:*_log" to match any log block,
    //             # or 'test_*' to match any item script that has name starting with 'test_'.
    //             # Note that to require multiple of an item as an input, the only option is to use multiple slots.
    //             # A single slot cannot require a quantity of items, as that is not part of the minecraft recipe system.
    //             # | All recipes must include this key!
    //             input:
    //             - ItemTag|ItemTag|ItemTag
    //             - ItemTag|ItemTag|ItemTag
    //             - ItemTag|ItemTag|ItemTag
    //        # You can add as many as you want.
    //        2:
    //             # Sample of the format for a 2x2 recipe
    //             type: shaped
    //             input:
    //             - ItemTag|ItemTag
    //             - ItemTag|ItemTag
    //        3:
    //            # Shapeless recipes take a list of input items.
    //            type: shapeless
    //            # Optionally specify the shapeless category for shapeless recipes, as "building", "redstone", "equipment", or "misc". Defaults to "misc" if unspecified.
    //            # | Only some recipes should have this key.
    //            category: misc
    //            input: ItemTag|...
    //        4:
    //            # Stonecutting recipes take exactly one input item.
    //            type: stonecutting
    //            input: ItemTag
    //        5:
    //            # Furnace, blast, smoker, and campfire recipes take one input and have additional options.
    //            type: furnace
    //            # Optionally specify the cook time as a duration (default 2s).
    //            # | Only some recipes should have this key.
    //            cook_time: 1s
    //            # Optionally specify the cooking category for cooking recipes, as "food", "blocks", or "misc". Defaults to "misc" if unspecified.
    //            # | Only some recipes should have this key.
    //            category: misc
    //            # Optionally specify experience reward amount (default 0).
    //            # | Only some recipes should have this key.
    //            experience: 5
    //            input: ItemTag
    //        6:
    //            # Smithing recipes take one base item and one upgrade item.
    //            # In versions 1.20 and up, smithing recipes take one template item, one base item, and one upgrade item.
    //            type: smithing
    //            template: ItemTag
    //            base: ItemTag
    //            # Optionally, choose what values to retain, as a simple pipe-separated list of parts to retain.
    //            # If unspecified, no values will be retained.
    //            # Parts can be: 'display', 'enchantments'
    //            retain: display|enchantments
    //            upgrade: ItemTag
    //
    //        7:
    //            # Brewing recipes take one base item and one ingredient item.
    //            # | Brewing recipes are only available on Paper versions 1.18 and up.
    //            # | Brewing recipes also have a special input option on 1.20 and above: "matcher:<item matcher>", to allow advanced matchers on the input/ingredient items.
    //            type: brewing
    //            input: ItemTag
    //            ingredient: ItemTag
    //
    //     # Set to true to not store the scriptID on the item, treating it as an item dropped by any other plugin.
    //     # NOTE: THIS IS NOT RECOMMENDED UNLESS YOU HAVE A SPECIFIC REASON TO USE IT.
    //     # | Most item scripts should exclude this key!
    //     no_id: true/false
    //
    //     # If your material is a 'written_book', you can specify a book script to automatically scribe your item
    //     # upon creation. See 'book script containers' for more information.
    //     # | Most item scripts should exclude this key, though there are certain rare cases it may be useful to.
    //     book: book_script_name
    // </code>
    //
    // -->

    String hash = "";

    public boolean allowInMaterialRecipes;

    public ItemScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        canRunScripts = false;
        allowInMaterialRecipes = getString("allow in material recipes", "false").equalsIgnoreCase("true");
        ItemScriptHelper.item_scripts.put(CoreUtilities.toLowerCase(getName()), this);
        ItemScriptHelper.item_scripts_by_hash_id.put(ItemScriptHelper.createItemScriptID(this), this);
    }

    private ItemTag cleanReference;

    public ItemTag getCleanReference() {
        if (cleanReference == null) {
            cleanReference = getItemFrom();
            if (cleanReference == null) {
                Debug.echoError("Item script '" + getName() + "' is invalid and unable to construct an ItemTag.");
                return null;
            }
        }
        return new ItemTag(cleanReference.getItemStack().clone());
    }

    public void setHashID(String HashID) {
        hash = HashID;
    }

    public ItemTag getItemFrom() {
        return getItemFrom(null);
    }

    boolean isProcessing = false;

    public ItemTag getItemFrom(TagContext context) {
        if (isProcessing) {
            Debug.echoError("Item script contains (or chains to) a reference to itself. Cannot process.");
            return null;
        }
        if (context == null) {
            context = new BukkitTagContext(null, null, new ScriptTag(this));
        }
        else {
            context = new BukkitTagContext((BukkitTagContext) context);
            context.script = new ScriptTag(this);
        }
        // Try to use this script to make an item.
        ItemTag stack;
        isProcessing = true;
        try {
            if (!contains("material", String.class)) {
                Debug.echoError("Item script '" + getName() + "' does not contain a material. Script cannot function.");
                return null;
            }
            // Check validity of material
            String material = TagManager.tag(getString("material"), context);
            if (material.startsWith("m@")) {
                material = material.substring(2);
            }
            stack = ItemTag.valueOf(material, this);
            // Make sure we're working with a valid base ItemStack
            if (stack == null) {
                Debug.echoError("Item script '" + getName() + "' contains an invalid or incorrect material '" + material + "' (did you spell the material name wrong?). Script cannot function.");
                return null;
            }
            // Handle listed mechanisms
            if (contains("mechanisms", Map.class)) {
                YamlConfiguration mechs = getConfigurationSection("mechanisms");
                for (StringHolder key : mechs.getKeys(false)) {
                    ObjectTag obj = CoreUtilities.objectToTagForm(mechs.get(key.low), context, true, true);
                    stack.safeAdjust(new Mechanism(key.low, obj, context));
                }
            }
            // Set Display Name
            if (contains("display name", String.class)) {
                String displayName = TagManager.tag(getString("display name"), context);
                NMSHandler.itemHelper.setDisplayName(stack, displayName);
            }
            // Set if the object is bound to the player
            if (contains("bound", String.class)) {
                BukkitImplDeprecations.boundWarning.warn(context);
            }
            // Set Lore
            if (contains("lore", List.class)) {
                List<String> lore = NMSHandler.itemHelper.getLore(stack);
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                for (String line : getStringList("lore")) {
                    line = TagManager.tag(line, context);
                    lore.add(line);
                }
                CoreUtilities.fixNewLinesToListSeparation(lore);
                NMSHandler.itemHelper.setLore(stack, lore);
            }
            // Set Durability
            if (contains("durability", String.class)) {
                short durability = Short.parseShort(getString("durability"));
                stack.setDurability(durability);
            }
            // Set Enchantments
            if (contains("enchantments", List.class)) {
                for (String enchantment : getStringList("enchantments")) {
                    enchantment = TagManager.tag(enchantment, context);
                    try {
                        // Build enchantment context
                        int level = 1;
                        int colon = enchantment.lastIndexOf(':');
                        if (colon == -1) {
                            Debug.echoError("Item script '" + getName() + "' has enchantment '" + enchantment + "' without a level.");
                        }
                        else {
                            level = Integer.parseInt(enchantment.substring(colon + 1).replace(" ", ""));
                            enchantment = enchantment.substring(0, colon).replace(" ", "");
                        }
                        // Add enchantment
                        EnchantmentTag ench = EnchantmentTag.valueOf(enchantment, context);
                        if (ench == null) {
                            Debug.echoError("Item script '" + getName() + "' specifies enchantment '" + enchantment + "' which is invalid.");
                            continue;
                        }
                        if (stack.getBukkitMaterial() == Material.ENCHANTED_BOOK) {
                            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
                            meta.addStoredEnchant(ench.enchantment, level, true);
                            stack.setItemMeta(meta);
                        }
                        else {
                            stack.getItemStack().addUnsafeEnchantment(ench.enchantment, level);
                            stack.resetCache();
                        }
                    }
                    catch (Exception ex) {
                        Debug.echoError("While constructing item script '" + getName() + "', encountered error while applying enchantment '" + enchantment + "':");
                        Debug.echoError(ex);
                    }
                }
            }
            // Set Color
            if (contains("color", String.class)) {
                BukkitImplDeprecations.itemScriptColor.warn(context);
                String color = TagManager.tag(getString("color"), context);
                LeatherColorer.colorArmor(stack, color);
            }
            // Set Book
            if (contains("book", String.class)) {
                BookScriptContainer book = ScriptRegistry.getScriptContainer(TagManager.tag(getString("book"), context).replace("s@", ""));
                stack = book.writeBookTo(stack, context);
            }
            if (contains("flags", Map.class)) {
                YamlConfiguration flagSection = getConfigurationSection("flags");
                AbstractFlagTracker tracker = stack.getFlagTracker();
                for (StringHolder key : flagSection.getKeys(false)) {
                    tracker.setFlag(key.str, CoreUtilities.objectToTagForm(flagSection.get(key.str), context, true, true), null);
                }
                stack.reapplyTracker(tracker);
            }
            stack.setItemScript(this);
        }
        catch (Exception e) {
            Debug.echoError("Woah! An exception has been called with this item script!");
            Debug.echoError(e);
            stack = null;
        }
        finally {
            isProcessing = false;
        }

        return stack;
    }
}
