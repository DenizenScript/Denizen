package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizen.objects.properties.item.*;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ImageTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemTag implements ObjectTag, Adjustable, FlaggableObject {

    // <--[ObjectType]
    // @name ItemTag
    // @prefix i
    // @base ElementTag
    // @implements FlaggableObject, PropertyHolderObject
    // @ExampleTagBase player.item_in_hand
    // @ExampleValues <player.item_in_hand>,stick,iron_sword
    // @ExampleForReturns
    // - give %VALUE%
    // @format
    // The identity format for items is the basic material type name, or an item script name. Other data is specified in properties.
    // For example, 'i@stick'.
    //
    // @description
    // An ItemTag represents a holdable item generically.
    //
    // ItemTags are temporary objects, to actually modify an item in an inventory you must add the item into that inventory.
    //
    // ItemTags do NOT remember where they came from. If you read an item from an inventory, changing it
    // does not change the original item in the original inventory. You must set it back in.
    //
    // Find a list of valid materials at:
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html>
    // Note that some materials on that list are exclusively for use with blocks, and cannot be held as items.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the item NBT.
    //
    // @Matchable
    // ItemTag matchers, sometimes identified as "<item>", often seen as "with:<item>":
    // "potion": plaintext: matches if the item is any form of potion item.
    // "script": plaintext: matches if the item is any form of script item.
    // "item_flagged:<flag>": A Flag Matcher for item flags.
    // "item_enchanted:<enchantment>": matches if the item is enchanted with the given enchantment name (excluding enchantment books). Allows advanced matchers.
    // "raw_exact:<item>": matches based on exact raw item data comparison (almost always a bad idea to use).
    // Item property format: will validate that the item material matches and all directly specified properties also match. Any properties not specified won't be checked.
    //                       for example "stick[display=Hi]" will match any 'stick' with a displayname of 'hi', regardless of whether that stick has lore or not, or has enchantments or not, or etc.
    // Item script names: matches if the item is a script item with the given item script name, using advanced matchers.
    // If none of the above are used, uses MaterialTag matchables. Refer to MaterialTag matchable list above.
    // Note that "item" plaintext is always true.
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static ItemTag valueOf(String string, PlayerTag player, NPCTag npc) {
        return valueOf(string, new BukkitTagContext(player, npc, null));
    }

    public static ItemTag valueOf(String string, Debuggable debugMe) {
        return valueOf(string, new BukkitTagContext(null, null, null, debugMe == null || debugMe.shouldDebug(), null));
    }

    public static ItemTag valueOf(String string, boolean debugMe) {
        return valueOf(string, new BukkitTagContext(null, null, null, debugMe, null));
    }

    @Fetchable("i")
    public static ItemTag valueOf(String string, TagContext context) {
        if (string == null || string.equals("")) {
            return null;
        }
        ItemTag stack = null;
        if (ObjectFetcher.isObjectWithProperties(string)) {
            return ObjectFetcher.getObjectFromWithProperties(ItemTag.class, string, context);
        }
        if (string.startsWith("i@")) {
            string = string.substring("i@".length());
        }
        string = CoreUtilities.toLowerCase(string);
        try {
            if (ScriptRegistry.containsScript(string, ItemScriptContainer.class)) {
                ItemScriptContainer isc = ScriptRegistry.getScriptContainer(string);
                // TODO: If a script does not contain tags, get the clean reference here.
                stack = isc.getItemFrom(context);
                if (stack == null && (context == null || context.showErrors())) {
                    Debug.echoError("Item script '" + isc.getName() + "' returned a null item.");
                }
            }
            else if (ScriptRegistry.containsScript(string, BookScriptContainer.class)) {
                BookScriptContainer book = ScriptRegistry.getScriptContainer(string);
                stack = book.getBookFrom(context);
                if (stack == null && (context == null || context.showErrors())) {
                    Debug.echoError("Book script '" + book.getName() + "' returned a null item.");
                }
            }
            if (stack != null) {
                return stack;
            }
        }
        catch (Exception ex) {
            if (CoreConfiguration.debugVerbose) {
                Debug.echoError(ex);
            }
        }
        try {
            MaterialTag mat = MaterialTag.valueOf(string, context);
            if (mat != null) {
                stack = new ItemTag(mat.getMaterial());
            }
            if (stack != null) {
                return stack;
            }
        }
        catch (Exception ex) {
            if (!string.equalsIgnoreCase("none") && (context == null || context.showErrors())) {
                Debug.log("Does not match a valid item ID or material: " + string);
            }
            if (CoreConfiguration.debugVerbose) {
                Debug.echoError(ex);
            }
        }
        if (context == null || context.showErrors()) {
            Debug.log("valueOf ItemTag returning null: " + string);
        }
        return null;
    }

    public static boolean matches(String arg) {
        if (arg == null) {
            return false;
        }
        if (CoreUtilities.toLowerCase(arg).startsWith("i@")) {
            return true;
        }
        if (ScriptRegistry.containsScript(arg, ItemScriptContainer.class)) {
            return true;
        }
        else if (ScriptRegistry.containsScript(arg, BookScriptContainer.class)) {
            return true;
        }
        if (valueOf(arg, CoreUtilities.noDebugContext) != null) {
            return true;
        }
        return false;
    }

    public static ItemTag getItemFor(ObjectTag object, TagContext context) {
        return object instanceof ItemTag ? (ItemTag) object : valueOf(object.toString(), context);
    }

    @Override
    public ObjectTag duplicate() {
        return new ItemTag(item.clone());
    }

    ///////////////
    //   Constructors
    /////////////

    public ItemTag(Material material) {
        this(new ItemStack(material));
    }

    public ItemTag(Material material, int qty) {
        this(new ItemStack(material, qty));
    }

    public ItemTag(MaterialTag material, int qty) {
        this.item = new ItemStack(material.getMaterial(), qty);
    }

    public ItemTag(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            this.item = new ItemStack(Material.AIR, 0);
        }
        else {
            this.item = item.clone();
        }
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    @Override
    public AbstractFlagTracker getFlagTracker() {
        if (flagTrackerCache == null) {
            String value = CustomNBT.getCustomNBT(getItemStack(), "flags", "Denizen");
            if (value == null) {
                return new MapTagFlagTracker();
            }
            flagTrackerCache = new MapTagFlagTracker(value, CoreUtilities.noDebugContext);
        }
        return flagTrackerCache;
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        if (tracker instanceof MapTagFlagTracker && ((MapTagFlagTracker) tracker).map.isEmpty()) {
            setItemStack(CustomNBT.removeCustomNBT(getItemStack(), "flags", "Denizen"));
        }
        else {
            setItemStack(CustomNBT.addCustomNBT(getItemStack(), "flags", tracker.toString(), "Denizen"));
        }
        flagTrackerCache = tracker;
    }

    private ItemStack item;

    public ItemMeta metaCache;

    public AbstractFlagTracker flagTrackerCache;

    public ItemStack getItemStack() {
        return item;
    }

    public ItemMeta getItemMeta() {
        if (metaCache == null) {
            metaCache = item.getItemMeta();
        }
        return metaCache;
    }

    public void setItemMeta(ItemMeta meta) {
        this.metaCache = meta;
        item.setItemMeta(meta);
    }

    public void setItemStack(ItemStack item) {
        this.item = item;
        resetCache();
    }

    public void resetCache() {
        metaCache = null;
        flagTrackerCache = null;
    }

    // Compare item to item.
    // -1 indicates it is not a match
    //  0 indicates it is a perfect match
    //  1 or higher indicates the item being matched against
    //    was probably originally alike, but may have been
    //    modified or enhanced.

    public int comparesTo(ItemTag item) {
        return comparesTo(item.getItemStack());
    }

    public int comparesTo(ItemStack compared_to) {
        if (item == null) {
            return -1;
        }
        int determination = 0;
        ItemStack compared = getItemStack();
        if (compared.getType() != compared_to.getType()) {
            return -1;
        }
        if (compared_to.hasItemMeta()) {
            if (!compared.hasItemMeta()) {
                return -1;
            }
            ItemMeta thisMeta = getItemMeta();
            ItemMeta comparedItemMeta = compared_to.getItemMeta();
            if (comparedItemMeta.hasDisplayName()) {
                if (!thisMeta.hasDisplayName()) {
                    return -1;
                }
                if (CoreUtilities.toLowerCase(comparedItemMeta.getDisplayName()).startsWith(CoreUtilities.toLowerCase(thisMeta.getDisplayName()))) {
                    if (thisMeta.getDisplayName().length() > comparedItemMeta.getDisplayName().length()) {
                        determination++;
                    }
                }
                else {
                    return -1;
                }
            }
            if (comparedItemMeta.hasLore()) {
                if (!thisMeta.hasLore()) {
                    return -1;
                }
                for (String lore : comparedItemMeta.getLore()) {
                    if (!thisMeta.getLore().contains(lore)) {
                        return -1;
                    }
                }
                if (thisMeta.getLore().size() > comparedItemMeta.getLore().size()) {
                    determination++;
                }
            }
            if (!comparedItemMeta.getEnchants().isEmpty()) {
                if (thisMeta.getEnchants().isEmpty()) {
                    return -1;
                }
                for (Map.Entry<Enchantment, Integer> enchant : comparedItemMeta.getEnchants().entrySet()) {
                    if (!thisMeta.getEnchants().containsKey(enchant.getKey()) || thisMeta.getEnchants().get(enchant.getKey()) < enchant.getValue()) {
                        return -1;
                    }
                }
                if (thisMeta.getEnchants().size() > comparedItemMeta.getEnchants().size()) {
                    determination++;
                }
            }
            if (isRepairable()) {
                if (((Damageable) thisMeta).getDamage() < ((Damageable) comparedItemMeta).getDamage()) {
                    determination++;
                }
            }
        }
        return determination;
    }

    public boolean isItemscript() {
        return ItemScriptHelper.isItemscript(item);
    }

    public String getScriptName() {
        return ItemScriptHelper.getItemScriptNameText(item);
    }

    public void setItemScriptName(String name) {
        setItemStack(NMSHandler.itemHelper.addNbtData(getItemStack(), "DenizenItemScript", new StringTag(CoreUtilities.toLowerCase(name))));
    }

    public void setItemScript(ItemScriptContainer script) {
        if (script.contains("NO_ID", String.class) && Boolean.parseBoolean(script.getString("NO_ID"))) {
            return;
        }
        setItemScriptName(script.getName());
    }

    public Material getBukkitMaterial() {
        return getItemStack().getType();
    }

    public MaterialTag getMaterial() {
        return new MaterialTag(getBukkitMaterial());
    }

    public void setAmount(int value) {
        if (item != null) {
            item.setAmount(value);
        }
    }

    public int getAmount() {
        if (item != null) {
            return item.getAmount();
        }
        else {
            return 0;
        }
    }

    public void setDurability(short value) {
        ItemMeta meta = getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(value);
            setItemMeta(meta);
        }
    }

    public boolean isRepairable() {
        return getItemMeta() instanceof Damageable;
    }

    public boolean matchesRawExact(ItemTag item) {
        ItemTag thisItem = this;
        if (thisItem.getItemStack().getAmount() != 1) {
            thisItem = new ItemTag(thisItem.getItemStack().clone());
            thisItem.getItemStack().setAmount(1);
        }
        if (item.getItemStack().getAmount() != 1) {
            item = new ItemTag(item.getItemStack().clone());
            item.getItemStack().setAmount(1);
        }
        return thisItem.identify().equals(item.identify());
    }

    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = "Item";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ItemTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String identify() {
        if (item == null || item.getType() == Material.AIR) {
            return "i@air";
        }
        return "i@" + getMaterial().identifyNoPropertiesNoIdentifier() + PropertyParser.getPropertiesString(this);
    }

    @Override
    public String debuggable() {
        if (item == null || item.getType() == Material.AIR) {
            return "<LG>i@<Y>air";
        }
        return "<LG>i@<Y>" + getMaterial().identifyNoPropertiesNoIdentifier() + PropertyParser.getPropertiesDebuggable(this);
    }

    @Override
    public String identifySimple() {
        if (item == null) {
            return "null";
        }
        if (item.getType() != Material.AIR) {
            if (isItemscript()) {
                return "i@" + getScriptName();
            }
        }
        return "i@" + identifyMaterial().replace("m@", "");
    }

    public String identifyMaterial() {
        return getMaterial().identifySimple();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public Object getJavaObject() {
        return getItemStack();
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return !getBukkitMaterial().isAir();
    }

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);
        PropertyParser.registerPropertyTagHandlers(ItemTag.class, tagProcessor);

        // <--[tag]
        // @attribute <ItemTag.repairable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the item can be repaired.
        // If this returns true, it will enable access to:
        // <@link mechanism ItemTag.durability>,
        // <@link tag ItemTag.max_durability>, and <@link tag ItemTag.durability>.
        // Note that due to odd design choices in Spigot, this is effectively true for all items, even though the durability value of most items is locked at zero.
        // -->
        tagProcessor.registerTag(ElementTag.class, "repairable", (attribute, object) -> {
            return new ElementTag(ItemDurability.describes(object));
        });

        // <--[tag]
        // @attribute <ItemTag.is_book>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the item is considered an editable book.
        // If this returns true, it will enable access to:
        // <@link mechanism ItemTag.book>,
        // <@link tag ItemTag.book_author>, <@link tag ItemTag.book_title>, and <@link tag ItemTag.book_pages>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_book", (attribute, object) -> {
            return new ElementTag(ItemBook.describes(object));
        });

        // <--[tag]
        // @attribute <ItemTag.is_colorable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the item can have a custom color.
        // If this returns true, it will enable access to:
        // <@link mechanism ItemTag.color>, and <@link tag ItemTag.color>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_colorable", (attribute, object) -> {
            return new ElementTag(ItemColor.describes(object));
        });

        tagProcessor.registerTag(ElementTag.class, "is_dyeable", (attribute, object) -> {
            return new ElementTag(ItemColor.describes(object));
        });

        // <--[tag]
        // @attribute <ItemTag.is_firework>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the item is a firework.
        // If this returns true, it will enable access to:
        // <@link mechanism ItemTag.firework>, and <@link tag ItemTag.firework>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_firework", (attribute, object) -> {
            return new ElementTag(ItemFirework.describes(object));
        });

        // <--[tag]
        // @attribute <ItemTag.has_inventory>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the item has an inventory.
        // If this returns true, it will enable access to:
        // <@link mechanism ItemTag.inventory_contents>, and <@link tag ItemTag.inventory_contents>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_inventory", (attribute, object) -> {
            return new ElementTag(ItemInventoryContents.describes(object));
        });

        // <--[tag]
        // @attribute <ItemTag.is_lockable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the item is lockable.
        // If this returns true, it will enable access to:
        // <@link mechanism ItemTag.lock>, and <@link tag ItemTag.lock>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_lockable", (attribute, object) -> {
            return new ElementTag(ItemLock.describes(object));
        });

        // <--[tag]
        // @attribute <ItemTag.material>
        // @returns MaterialTag
        // @mechanism ItemTag.material
        // @group conversion
        // @description
        // Returns the MaterialTag that is the basis of the item.
        // EG, a stone with lore and a display name, etc. will return only "m@stone".
        // -->
        tagProcessor.registerTag(ObjectTag.class, "material", (attribute, object) -> {
            if (attribute.getAttribute(2).equals("formatted")) {
                return object;
            }
            if (object.getItemMeta() instanceof BlockStateMeta) {
                if (object.getBukkitMaterial() == Material.SHIELD) {
                    MaterialTag material = new MaterialTag(Material.SHIELD);
                    material.setModernData(((BlockStateMeta) object.getItemMeta()).getBlockState().getBlockData());
                    return material;
                }
                return new MaterialTag(((BlockStateMeta) object.getItemMeta()).getBlockState());
            }
            return object.getMaterial();
        });

        // <--[tag]
        // @attribute <ItemTag.placed_material>
        // @returns MaterialTag
        // @group conversion
        // @description
        // Returns the MaterialTag that this item would place as a block, if it is a block-like item.
        // For example, the "redstone" item will return a "redstone_wire" block.
        // Returns null if the item doesn't place as a block.
        // -->
        tagProcessor.registerTag(ObjectTag.class, "placed_material", (attribute, object) -> {
            BlockData data = NMSHandler.itemHelper.getPlacedBlock(object.getBukkitMaterial());
            if (data == null) {
                return null;
            }
            return new MaterialTag(data);
        });

        // <--[tag]
        // @attribute <ItemTag.json>
        // @returns ElementTag
        // @group conversion
        // @description
        // Returns the item converted to a raw JSON object with one layer of escaping for network transmission.
        // EG, via /tellraw.
        // Generally, prefer tags like <@link tag ElementTag.on_hover.type> with type 'show_item'.
        // -->
        tagProcessor.registerTag(ElementTag.class, "json", (attribute, object) -> {
            return new ElementTag(NMSHandler.itemHelper.getJsonString(object.item));
        });

        // <--[tag]
        // @attribute <ItemTag.meta_type>
        // @returns ElementTag
        // @group conversion
        // @description
        // Returns the name of the Bukkit item meta type that applies to this item.
        // This is for debugging purposes.
        // -->
        tagProcessor.registerTag(ElementTag.class, "meta_type", (attribute, object) -> {
            return new ElementTag(object.getItemMeta().getClass().getName());
        });

        // <--[tag]
        // @attribute <ItemTag.bukkit_serial>
        // @returns ElementTag
        // @group conversion
        // @description
        // Returns a YAML text section representing the Bukkit serialization of the item, under subkey "item".
        // -->
        tagProcessor.registerTag(ElementTag.class, "bukkit_serial", (attribute, object) -> {
            YamlConfiguration config = new YamlConfiguration();
            config.set("item", object.getItemStack());
            return new ElementTag(config.saveToString());
        });

        // <--[tag]
        // @attribute <ItemTag.simple>
        // @returns ElementTag
        // @group conversion
        // @description
        // Returns a simple reusable item identification for this item, with minimal extra data.
        // -->
        tagProcessor.registerTag(ElementTag.class, "simple", (attribute, object) -> {
            return new ElementTag(object.identifySimple());
        });

        // <--[tag]
        // @attribute <ItemTag.recipe_ids[(<type>)]>
        // @returns ListTag
        // @description
        // If the item is a scripted item, returns a list of all recipe IDs created by the item script.
        // Others, returns a list of all recipe IDs that the server lists as capable of crafting the item.
        // Returns a list in the Namespace:Key format, for example "minecraft:gold_nugget".
        // Optionally, specify a recipe type (CRAFTING, FURNACE, COOKING, BLASTING, SHAPED, SHAPELESS, SMOKING, STONECUTTING, BREWING)
        // to limit to just recipes of that type.
        // Brewing recipes are only supported on Paper, and only custom ones are available.
        // -->
        tagProcessor.registerTag(ListTag.class, "recipe_ids", (attribute, object) -> {
            String type = attribute.hasParam() ? CoreUtilities.toLowerCase(attribute.getParam()) : null;
            ItemScriptContainer container = ItemScriptHelper.getItemScriptContainer(object.getItemStack());
            ListTag list = new ListTag();
            Consumer<NamespacedKey> addRecipe = (recipe) -> {
                if (CoreUtilities.equalsIgnoreCase(recipe.getNamespace(), "denizen")) {
                    if (container != ItemScriptHelper.recipeIdToItemScript.get(recipe.toString())) {
                        return;
                    }
                }
                else if (container != null) {
                    return;
                }
                list.add(recipe.toString());
            };
            if (type == null || !type.equals("brewing")) {
                for (Recipe recipe : Bukkit.getRecipesFor(object.getItemStack())) {
                    if (recipe instanceof Keyed keyedRecipe && Utilities.isRecipeOfType(recipe, type)) {
                        addRecipe.accept(keyedRecipe.getKey());
                    }
                }
            }
            if (Denizen.supportsPaper && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && (type == null || type.equals("brewing"))) {
                for (Map.Entry<NamespacedKey, ItemHelper.BrewingRecipe> entry : NMSHandler.itemHelper.getCustomBrewingRecipes().entrySet()) {
                    ItemStack result = entry.getValue().result();
                    if (object.getBukkitMaterial() == result.getType() && (object.getItemStack().getDurability() == -1 || object.getItemStack().getDurability() == result.getDurability())) {
                        addRecipe.accept(entry.getKey());
                    }
                }
            }
            return list;
        });

        // <--[tag]
        // @attribute <ItemTag.formatted>
        // @returns ElementTag
        // @group formatting
        // @description
        // Returns the formatted material name of the item to be used in a sentence.
        // Correctly uses singular and plural forms of item names, among other things.
        // -->
        tagProcessor.registerTag(ElementTag.class, "formatted", (attribute, object) -> {
            return new ElementTag(object.formattedName());
        });

        // <--[mechanism]
        // @object ItemTag
        // @name material
        // @input MaterialTag
        // @description
        // Changes the item's material to the given material.
        // Only copies the base material type, not any advanced block-data material properties.
        // Note that this may cause some properties of the item to be lost.
        // @tags
        // <ItemTag.material>
        // -->
        tagProcessor.registerMechanism("material", true, MaterialTag.class, (object, mechanism, material) -> {
            object.item.setType(material.getMaterial());
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {

            // <--[tag]
            // @attribute <ItemTag.map_to_image[<player>]>
            // @returns ImageTag
            // @description
            // Returns an image of a filled map item's contents.
            // Must specify a player for the map to render for, as if that player is holding the map.
            // Note that this does not include cursors, as their rendering is entirely client-side.
            // -->
            tagProcessor.registerTag(ImageTag.class, PlayerTag.class, "map_to_image", (attribute, object, input) -> {
                if (!(object.getItemMeta() instanceof MapMeta mapMeta)) {
                    return null;
                }
                MapView mapView = mapMeta.getMapView();
                if (mapView == null) {
                    attribute.echoError("Invalid map item: must have contents.");
                    return null;
                }
                byte[] data = NMSHandler.itemHelper.renderMap(mapView, input.getPlayerEntity());
                BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < 128; x++) {
                    for (int y = 0; y < 128; y++) {
                        image.setRGB(x, y, MapPalette.getColor(data[y * 128 + x]).getRGB());
                    }
                }
                return new ImageTag(image);
            });
        }
    }

    public String formattedName() {
        String id = CoreUtilities.toLowerCase(getMaterial().name()).replace('_', ' ');
        if (id.equals("air")) {
            return "nothing";
        }
        if (id.equals("ice") || id.equals("dirt") || id.endsWith("copper") || id.endsWith("cream")) {
            return id;
        }
        if (getItemStack().getAmount() > 1) {
            if (id.equals("cactus")) {
                return "cacti";
            }
            if (id.endsWith(" off")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith(" on")) {
                id = id.substring(0, id.length() - 3);
            }
            if (id.equals("rotten flesh") || id.equals("cooked fish")
                    || id.equals("raw fish") || id.endsWith("s")) {
                return id;
            }
            if (id.endsWith("y")) {
                return id.substring(0, id.length() - 1) + "ies";  // ex: lily -> lilies
            }
            if (id.endsWith("sh") || id.endsWith("ch")) {
                return id + "es";
            }
            return id + "s"; // iron sword -> iron swords
        }
        else {
            if (id.equals("cactus")) {
                return "a cactus";
            }
            if (id.endsWith("s")) {
                return id;
            }
            if (id.endsWith(" off")) {
                return "a " + id.substring(0, id.length() - 4);
            }
            if (id.endsWith(" on")) {
                return "a " + id.substring(0, id.length() - 3);
            }
            if (id.startsWith("a") || id.startsWith("e") || id.startsWith("i")
                    || id.startsWith("o") || id.startsWith("u")) {
                return "an " + id; // ex: emerald -> an emerald
            }
            return "a " + id; // ex: diamond -> a diamond
        }
    }

    public static ObjectTagProcessor<ItemTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {
        tagProcessor.processMechanism(this, mechanism);
    }

    public static class ItemPropertyMatchHelper {

        public ItemTag properItem;

        public static class PropertyComparison {

            public String compareValue;

            public PropertyParser.PropertyGetter getter;

            public PropertyComparison(String compareValue, PropertyParser.PropertyGetter getter) {
                this.compareValue = compareValue;
                this.getter = getter;
            }
        }

        public List<PropertyComparison> comparisons = new ArrayList<>();

        public final boolean doesMatch(ItemTag item) {
            if (item == null) {
                return false;
            }
            if (item.getBukkitMaterial() != properItem.getBukkitMaterial()) {
                Debug.verboseLog("[ItemPropertyMatchHelper] deny because material mismatch");
                return false;
            }
            for (PropertyComparison comparison : comparisons) {
                Property p = comparison.getter.get(item);
                if (p == null) {
                    Debug.verboseLog("[ItemPropertyMatchHelper] deny because property is null");
                    return false;
                }
                String val = p.getPropertyString();
                if (comparison.compareValue == null) {
                    if (val != null) {
                        Debug.verboseLog("[ItemPropertyMatchHelper] deny because nullity");
                        return false;
                    }
                }
                else {
                    if (val == null || !CoreUtilities.equalsIgnoreCase(comparison.compareValue, val)) {
                        Debug.verboseLog("[ItemPropertyMatchHelper] deny because unequal");
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "item=" + properItem + ", comparisons=" + comparisons.stream().map(c -> c.compareValue).collect(Collectors.joining(", "));
        }
    }

    public static LinkedHashMap<String, ItemPropertyMatchHelper> matchHelperCache = new LinkedHashMap<>();

    public static int MAX_MATCH_HELPER_CACHE = 1024;

    public static ItemPropertyMatchHelper getPropertyMatchHelper(String text) {
        if (CoreConfiguration.debugVerbose) {
            Debug.verboseLog("[ItemPropertyMatchHelper] getting helper for " + text);
        }
        ItemPropertyMatchHelper matchHelper = matchHelperCache.get(text);
        if (matchHelper != null) {
            return matchHelper;
        }
        ItemTag item = valueOf(text, CoreUtilities.noDebugContext);
        if (item == null) {
            Debug.verboseLog("[ItemPropertyMatchHelper] rejecting item because it's null");
            return null;
        }
        matchHelper = new ItemPropertyMatchHelper();
        matchHelper.properItem = item;
        List<String> propertiesGiven = ObjectFetcher.separateProperties(text);
        if (propertiesGiven == null) {
            return matchHelper;
        }
        PropertyParser.ClassPropertiesInfo itemInfo = PropertyParser.propertiesByClass.get(ItemTag.class);
        for (int i = 1; i < propertiesGiven.size(); i++) {
            String property = propertiesGiven.get(i);
            int equalSign = property.indexOf('=');
            if (equalSign == -1) {
                if (CoreConfiguration.debugVerbose) {
                    Debug.verboseLog("[ItemPropertyMatchHelper] rejecting item because " + property + " lacks an equal sign");
                }
                return null;
            }
            String label = ObjectFetcher.unescapeProperty(property.substring(0, equalSign));
            PropertyParser.PropertyGetter getter = itemInfo.propertiesByMechanism.get(label);
            if (getter == null) {
                continue;
            }
            Property realProp = getter.get(item);
            if (realProp == null) {
                continue;
            }
            matchHelper.comparisons.add(new ItemPropertyMatchHelper.PropertyComparison(realProp.getPropertyString(), getter));
        }
        if (matchHelperCache.size() > MAX_MATCH_HELPER_CACHE) {
            String firstMost = matchHelperCache.keySet().iterator().next();
            matchHelperCache.remove(firstMost);
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.verboseLog("[ItemPropertyMatchHelper] stored final result as " + matchHelper);
        }
        matchHelperCache.put(text, matchHelper);
        return matchHelper;
    }

    @Override
    public boolean advancedMatches(String matcher) {
        String matcherLow = CoreUtilities.toLowerCase(matcher);
        if (matcherLow.contains(":")) {
            if (matcherLow.startsWith("item_flagged:")) {
                if (getBukkitMaterial().isAir()) {
                    return false;
                }
                return BukkitScriptEvent.coreFlaggedCheck(matcher.substring("item_flagged:".length()), getFlagTracker());
            }
            else if (matcherLow.startsWith("item_enchanted:")) {
                String enchMatcher = matcher.substring("item_enchanted:".length());
                if (getBukkitMaterial().isAir() || !getItemMeta().hasEnchants()) {
                    return false;
                }
                for (Enchantment enchant : getItemMeta().getEnchants().keySet()) {
                    if (BukkitScriptEvent.runGenericCheck(enchMatcher, enchant.getKey().getKey())) {
                        return true;
                    }
                }
                return false;
            }
            else if (matcherLow.startsWith("raw_exact:")) {
                ItemTag compareItem = ItemTag.valueOf(matcher.substring("raw_exact:".length()), CoreUtilities.errorButNoDebugContext);
                return compareItem != null && compareItem.matchesRawExact(this);
            }
        }
        if (matcherLow.equals("potion") && CoreUtilities.toLowerCase(getBukkitMaterial().name()).contains("potion")) {
            return true;
        }
        boolean isItemScript = isItemscript();
        if (matcherLow.equals("script") && isItemScript) {
            return true;
        }
        if (matcher.contains("[") && matcher.endsWith("]")) {
            ItemPropertyMatchHelper helper = getPropertyMatchHelper(matcher);
            if (helper == null) {
                return false;
            }
            return helper.doesMatch(this);
        }
        if (isItemScript) {
            ScriptEvent.MatchHelper matchHelper = BukkitScriptEvent.createMatcher(matcher);
            if (matchHelper.doesMatch(getScriptName())) {
                return true;
            }
        }
        return MaterialTag.advancedMatchesInternal(getBukkitMaterial(), matcher, !isItemScript);
    }
}
