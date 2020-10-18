package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.properties.item.*;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemTag implements ObjectTag, Notable, Adjustable {

    // <--[language]
    // @name ItemTag Objects
    // @group Object System
    // @description
    // An ItemTag represents a holdable item generically.
    //
    // ItemTags are temporary objects, to actually modify an item in an inventory you must add the item into that inventory.
    //
    // ItemTags do NOT remember where they came from. If you read an item from an inventory, changing it
    // does not change the original item in the original inventory. You must set it back in.
    //
    // These use the object notation "i@".
    // The identity format for items is the basic material type name, or an item script name. Other data is specified in properties.
    // For example, 'i@stick'.
    //
    // Find a list of valid materials at:
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html>
    // Note that some materials on that list are exclusively for use with blocks, and cannot be held as items.
    //
    // -->

    final public static String itemscriptIdentifier = "§0id:";

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Deprecated
    public static ItemTag valueOf(String string) {
        return valueOf(string, null);
    }

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
        Notable noted = NotableManager.getSavedObject(string);
        if (noted instanceof ItemTag) {
            Deprecations.notableItems.warn();
            return (ItemTag) noted;
        }
        if (string.startsWith("i@")) {
            string = string.substring("i@".length());
        }
        string = CoreUtilities.toLowerCase(string);
        try {
            if (ScriptRegistry.containsScript(string, ItemScriptContainer.class)) {
                ItemScriptContainer isc = ScriptRegistry.getScriptContainerAs(string, ItemScriptContainer.class);
                // TODO: If a script does not contain tags, get the clean reference here.
                stack = isc.getItemFrom(context);
                if (stack == null && (context == null || context.showErrors())) {
                    Debug.echoError("Item script '" + isc.getName() + "' returned a null item.");
                }
            }
            else if (ScriptRegistry.containsScript(string, BookScriptContainer.class)) {
                BookScriptContainer book = ScriptRegistry.getScriptContainerAs(string, BookScriptContainer.class);
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
            if (Debug.verbose) {
                Debug.echoError(ex);
            }
        }
        try {
            MaterialTag mat = MaterialTag.valueOf(string.toUpperCase(), context);
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
            if (Debug.verbose) {
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

        // All ObjectTags should 'match' if there is a proper
        // ObjectFetcher identifier
        if (CoreUtilities.toLowerCase(arg).startsWith("i@")) {
            return true;
        }

        // Try a quick and simple item/book script match
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
            this.item = item;
        }
    }

    public ItemTag(Item item) {
        this(item.getItemStack());
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private ItemStack item;

    public ItemMeta meta;

    public ItemStack getItemStack() {
        return item;
    }

    public ItemMeta getItemMeta() {
        if (meta == null) {
            meta = item.getItemMeta();
        }
        return meta;
    }

    public void setItemMeta(ItemMeta meta) {
        this.meta = meta;
        item.setItemMeta(meta);
    }

    public void setItemStack(ItemStack item) {
        this.item = item;
        meta = null;
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
                if (comparedItemMeta.getDisplayName().toUpperCase().startsWith(thisMeta.getDisplayName().toUpperCase())) {
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

    /**
     * Check whether this item contains the lore specific
     * to item scripts.
     *
     * @return True if it does, otherwise false
     */
    public boolean isItemscript() {
        return ItemScriptHelper.isItemscript(this);
    }

    public String getScriptName() {
        ItemScriptContainer cont = ItemScriptHelper.getItemScriptContainer(this);
        if (cont != null) {
            return cont.getName();
        }
        else {
            return null;
        }
    }

    public void setItemScript(ItemScriptContainer script) {
        if (script.contains("NO_ID") && Boolean.valueOf(script.getString("NO_ID"))) {
            return;
        }
        if (Settings.packetInterception()) {
            setItemStack(NMSHandler.getItemHelper().addNbtData(getItemStack(), "Denizen Item Script", new StringTag(script.getHashID())));
        }
        else {
            List<String> lore = meta.hasLore() ? NMSHandler.getItemHelper().getLore(this) : new ArrayList<>();
            lore.add(0, script.getHashID());
            NMSHandler.getItemHelper().setLore(this, lore);
        }
    }

    public MaterialTag getMaterial() {
        return new MaterialTag(getItemStack().getType());
    }

    public String getMaterialName() {
        return CoreUtilities.toLowerCase(getItemStack().getType().name());
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

    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = getObjectType();

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
    public String getObjectType() {
        return "Item";
    }

    @Override
    public String identify() {
        if (item == null || item.getType() == Material.AIR) {
            return "i@air";
        }
        // If saved item, return that
        if (isUnique()) {
            Deprecations.notableItems.warn();
            return "i@" + NotableManager.getSavedId(this) + PropertyParser.getPropertiesString(this);
        }
        return "i@" + getMaterial().identifyNoPropertiesNoIdentifier().replace("m@", "") + PropertyParser.getPropertiesString(this);
    }

    @Override
    public String identifySimple() {
        if (item == null) {
            return "null";
        }

        if (item.getType() != Material.AIR) {

            // If saved item, return that
            if (isUnique()) {
                Deprecations.notableItems.warn();
                return "i@" + NotableManager.getSavedId(this);
            }

            // If not a saved item, but is a custom item, return the script id
            else if (isItemscript()) {
                return "i@" + getScriptName();
            }
        }

        // Else, return the material name
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
    public boolean isUnique() {
        if (NotableManager.isSaved(this)) {
            Deprecations.notableItems.warn();
            return true;
        }
        return false;
    }

    @Override
    @Note("Items")
    public String getSaveObject() {
        return identify();
    }

    @Override
    public void makeUnique(String id) {
        Deprecations.notableItems.warn();
        NotableManager.saveAs(this, id);
    }

    @Override
    public void forget() {
        Deprecations.notableItems.warn();
        NotableManager.remove(this);
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ItemTag.with[<mechanism>=<value>;...]>
        // @returns ItemTag
        // @group properties
        // @description
        // Returns a copy of the item with mechanism adjustments applied.
        // -->
        registerTag("with", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                Debug.echoError("ItemTag.with[...] tag must have an input mechanism list.");
            }
            ItemTag item = new ItemTag(object.getItemStack().clone());
            List<String> properties = ObjectFetcher.separateProperties("[" + attribute.getContext(1) + "]");
            for (int i = 1; i < properties.size(); i++) {
                List<String> data = CoreUtilities.split(properties.get(i), '=', 2);
                if (data.size() != 2) {
                    Debug.echoError("Invalid property string '" + properties.get(i) + "'!");
                }
                else {
                    item.safeApplyProperty(new Mechanism(new ElementTag(data.get(0)), new ElementTag(data.get(1)), attribute.context));
                }
            }
            return item;
        });

        // <--[tag]
        // @attribute <ItemTag.property_map>
        // @returns MapTag
        // @group properties
        // @description
        // Returns the item's property map.
        // -->
        registerTag("property_map", (attribute, object) -> {
            return PropertyParser.getPropertiesMap(object);
        });

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
        registerTag("repairable", (attribute, object) -> {
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
        registerTag("is_book", (attribute, object) -> {
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
        registerTag("is_colorable", (attribute, object) -> {
            return new ElementTag(ItemColor.describes(object));
        });

        registerTag("is_dyeable", (attribute, object) -> {
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
        registerTag("is_firework", (attribute, object) -> {
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
        registerTag("has_inventory", (attribute, object) -> {
            return new ElementTag(ItemInventory.describes(object));
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
        registerTag("is_lockable", (attribute, object) -> {
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
        registerTag("material", (attribute, object) -> {
            if (attribute.getAttribute(2).equals("formatted")) {
                return object;
            }
            if (object.getItemMeta() instanceof BlockStateMeta) {
                if (object.getItemStack().getType() == Material.SHIELD) {
                    MaterialTag material = new MaterialTag(Material.SHIELD);
                    material.setModernData(new ModernBlockData(((BlockStateMeta) object.getItemMeta()).getBlockState()));
                    return material;
                }
                return new MaterialTag(new ModernBlockData(((BlockStateMeta) object.getItemMeta()).getBlockState()));
            }
            return object.getMaterial();
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
        registerTag("json", (attribute, object) -> {
            return new ElementTag(NMSHandler.getItemHelper().getJsonString(object.item));
        });

        // <--[tag]
        // @attribute <ItemTag.bukkit_serial>
        // @returns ElementTag
        // @group conversion
        // @description
        // Returns a YAML text section representing the Bukkit serialization of the item, under subkey "item".
        // -->
        registerTag("bukkit_serial", (attribute, object) -> {
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
        registerTag("simple", (attribute, object) -> {
            return new ElementTag(object.identifySimple());
        });

        // <--[tag]
        // @attribute <ItemTag.recipe_ids[(<type>)]>
        // @returns ListTag
        // @description
        // If the item is a scripted item, returns a list of all recipe IDs created by the item script.
        // Others, returns a list of all recipe IDs that the server lists as capable of crafting the item.
        // Returns a list in the Namespace:Key format, for example "minecraft:gold_nugget".
        // Optionally, specify a recipe type (CRAFTING, FURNACE, COOKING, BLASTING, SHAPED, SHAPELESS, SMOKING, STONECUTTING)
        // to limit to just recipes of that type.
        // -->
        registerTag("recipe_ids", (attribute, object) -> {
            String type = attribute.hasContext(1) ? CoreUtilities.toLowerCase(attribute.getContext(1)) : null;
            ItemScriptContainer container = object.isItemscript() ? ItemScriptHelper.getItemScriptContainer(object) : null;
            ListTag list = new ListTag();
            for (Recipe recipe : Bukkit.getRecipesFor(object.getItemStack())) {
                if (!Utilities.isRecipeOfType(recipe, type)) {
                    continue;
                }
                if (recipe instanceof Keyed) {
                    NamespacedKey key = ((Keyed) recipe).getKey();
                    if (key.getNamespace().equalsIgnoreCase("denizen")) {
                        if (container != ItemScriptHelper.recipeIdToItemScript.get(key.toString())) {
                            continue;
                        }
                    }
                    else if (container != null) {
                        continue;
                    }
                    list.add(key.toString());
                }
            }
            return list;
        });

        registerTag("notable_name", (attribute, object) -> {
            Deprecations.notableItems.warn();
            String notname = NotableManager.getSavedId(object);
            if (notname == null) {
                return null;
            }
            return new ElementTag(notname);
        });

        // <--[tag]
        // @attribute <ItemTag.formatted>
        // @returns ElementTag
        // @group formatting
        // @description
        // Returns the formatted material name of the item to be used in a sentence.
        // Correctly uses singular and plural forms of item names, among other things.
        // -->
        registerTag("formatted", (attribute, object) -> {
            return new ElementTag(object.formattedName());
        });
    }

    public String formattedName() {
        String id = CoreUtilities.toLowerCase(getMaterial().realName()).replace('_', ' ');
        if (id.equals("air")) {
            return "nothing";
        }
        if (id.equals("ice") || id.equals("dirt")) {
            return id;
        }
        if (getItemStack().getAmount() > 1) {
            if (id.equals("cactus")) {
                return "cactuses";
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

    public static void registerTag(String name, TagRunnable.ObjectInterface<ItemTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    public void applyProperty(Mechanism mechanism) {
        if (NotableManager.isExactSavedObject(this)) {
            Deprecations.notableItems.warn();
            Debug.echoError("Cannot apply properties to noted objects.");
            return;
        }
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

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
        if (mechanism.matches("material") && mechanism.requireObject(MaterialTag.class)) {
            item.setType(mechanism.valueAsType(MaterialTag.class).getMaterial());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
