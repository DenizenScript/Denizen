package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.LeatherColorer;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
    // # ItemTag based on this item script. For example, an item script named 'sword of swiftness'
    // # can be referred to as 'sword of swiftness'.
    // Item Script Name:
    //
    //   type: item
    //
    //   # Must be a valid ItemTag (EG red_wool or potion,8226) See 'ItemTag' for more information.
    //   material: base_material
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
    //   # You can optionally add crafting recipes for your item script.
    //   recipes:
    //       1:
    //           # The type can be: shaped, shapeless, stonecutting, furnace, blast, smoker, or campfire.
    //           type: shaped
    //           # The recipe can optionally have a custom internal recipe ID (for recipe books).
    //           # If not specified, will be of the form "<type>_<script.name>_<id>" where ID is the recipe list index (starting at 1, counting up).
    //           # IDs will always have the namespace "denizen". So, for the below, the full ID is "denizen:my_custom_item_id"
    //           # Note that most users should not set a custom ID. If you choose to set a custom one, be careful to avoid duplicates or invalid text.
    //           recipe_id: my_custom_item_id
    //           # You can optional add a group as well. The default is "custom".
    //           group: custom
    //           # You need to specify the input for the recipe. The below is a sample of a 3x3 shaped recipe. Other recipe types have a different format.
    //           # You are allowed to have non-3x3 shapes (can be any value 1-3 x 1-3, so for example 1x3, 2x1, and 2x2 are fine).
    //           # For an empty slot, use "air".
    //           input:
    //           - ItemTag|ItemTag|ItemTag
    //           - ItemTag|ItemTag|ItemTag
    //           - ItemTag|ItemTag|ItemTag
    //      # You can add as many as you want.
    //      2:
    //           # Sample of the format for a 2x2 recipe
    //           type: shaped
    //           input:
    //           - ItemTag|ItemTag
    //           - ItemTag|ItemTag
    //      3:
    //          # Shapeless recipes take a list of input items.
    //          type: shapeless
    //          input: ItemTag|...
    //      4:
    //          # Stonecutting recipes take exactly one input item.
    //          type: stonecutting
    //          input: ItemTag
    //      5:
    //          # Furnace, blast, smoker, and campfire recipes take one input and have additional options.
    //          type: furnace
    //          # Optionally specify the cook time as a duration (default 2s).
    //          cook_time: 1s
    //          # Optionally specify experience reward amount (default 0).
    //          experience: 5
    //          input: ItemTag
    //
    //   # Set to true to not store the scriptID on the item, treating it as an item dropped by any other plugin.
    //   # NOTE: THIS IS NOT RECOMMENDED UNLESS YOU HAVE A SPECIFIC REASON TO USE IT.
    //   no_id: true/false
    //
    //   # For colorable items, such as leather armor, you can specify a valid ColorTag to specify the item's appearance.
    //   # See 'ColorTag' for more information.
    //   color: ColorTag
    //
    //   # If your material is a 'm@written_book', you can specify a book script to automatically scribe your item
    //   # upon creation. See 'book script containers' for more information.
    //   book: book_script_name
    // </code>
    //
    // -->

    public NPCTag npc = null;
    public PlayerTag player = null;
    public boolean bound = false;
    String hash = "";

    public ItemScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);

        ItemScriptHelper.item_scripts.put(getName(), this);
        ItemScriptHelper.item_scripts_by_hash_id.put(ItemScriptHelper.createItemScriptID(this), this);
    }

    private ItemTag cleanReference;

    public ItemTag getCleanReference() {
        if (cleanReference == null) {
            cleanReference = getItemFrom();
        }
        return new ItemTag(cleanReference.getItemStack().clone());
    }

    public String getHashID() {
        return hash;
    }

    public void setHashID(String HashID) {
        hash = HashID;
    }

    public ItemTag getItemFrom() {
        return getItemFrom(null, null);
    }

    boolean isProcessing = false;

    public ItemTag getItemFrom(PlayerTag player, NPCTag npc) {
        if (isProcessing) {
            Debug.echoError("Item script contains (or chains to) a reference to itself. Cannot process.");
            return null;
        }
        // Try to use this script to make an item.
        ItemTag stack = null;
        isProcessing = true;
        try {
            BukkitTagContext context = new BukkitTagContext(player, npc, new ScriptTag(this));
            // Check validity of material
            if (contains("MATERIAL")) {
                String material = TagManager.tag(getString("MATERIAL"), context);
                if (material.startsWith("m@")) {
                    material = material.substring(2);
                }
                stack = ItemTag.valueOf(material, this);
            }

            // Make sure we're working with a valid base ItemStack
            if (stack == null) {
                return null;
            }

            // Handle listed mechanisms
            if (contains("MECHANISMS")) {
                YamlConfiguration mechs = getConfigurationSection("MECHANISMS");
                for (StringHolder key : mechs.getKeys(false)) {
                    String val;
                    if (mechs.isList(key.str)) {
                        ListTag list = new ListTag();
                        for (String listVal : mechs.getStringList(key.str)) {
                            list.add(ScriptBuilder.stripLinePrefix(TagManager.tag(listVal, context)));
                        };
                        val = list.identify();
                    }
                    else {
                        val = TagManager.tag(mechs.getString(key.str), context);
                    }
                    stack.safeAdjust(new Mechanism(new ElementTag(key.low), new ElementTag(val), context));
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
                Deprecations.boundWarning.warn();
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
        finally {
            isProcessing = false;
        }

        return stack;
    }

    public void setNPC(NPCTag npc) {
        this.npc = npc;
    }

    public void setPlayer(PlayerTag player) {
        this.player = player;
    }
}
