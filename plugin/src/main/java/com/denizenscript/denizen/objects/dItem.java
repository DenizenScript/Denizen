package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.properties.item.*;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.blocks.OldMaterialsHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.abstracts.ModernBlockData;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dItem implements dObject, Notable, Adjustable {

    // <--[language]
    // @name dItem
    // @group Object System
    // @description
    // A dItem represents a holdable item generically.
    //
    // dItems are temporary objects, to actually modify an item in an inventory you must add the item into that inventory.
    //
    // dItems do NOT remember where they came from. If you read an item from an inventory, changing it
    // does not change the original item in the original inventory. You must set it back in.
    //
    // For format info, see <@link language i@>
    //
    // -->

    // <--[language]
    // @name i@
    // @group Object Fetcher System
    // @description
    // i@ refers to the 'object identifier' of a dItem. The 'i@' is notation for Denizen's Object
    // Fetcher. The constructor for a dItem is the basic material type name, or an item script name. Other data is specified in properties.
    // For example, 'i@stick'.
    //
    // Find a list of valid materials at:
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html>
    // Note that some materials on that list are exclusively for use with blocks, and cannot be held as items.
    //
    // For general info, see <@link language dItem>
    //
    // -->

    final static Pattern ITEM_PATTERN =
            Pattern.compile("(?:item:)?([\\w ]+)[:,]?(\\d+)?\\[?(\\d+)?\\]?", // TODO: Wot.
                    Pattern.CASE_INSENSITIVE);

    final static Pattern item_by_saved = Pattern.compile("(i@)?(.+)\\[?(\\d+)?\\]?"); // TODO: Wot.

    final public static String itemscriptIdentifier = "§0id:";

    //////////////////
    //    OBJECT FETCHER
    ////////////////


    public static dItem valueOf(String string) {
        return valueOf(string, null, null);
    }

    @Fetchable("i")
    public static dItem valueOf(String string, TagContext context) {
        if (context == null) {
            return valueOf(string, null, null);
        }
        else {
            nope = !context.debug;
            dItem tmp = valueOf(string, ((BukkitTagContext) context).player, ((BukkitTagContext) context).npc);
            nope = false;
            return tmp;
        }
    }

    public static dItem valueOf(String string, Debuggable debugMe) {
        nope = debugMe != null && !debugMe.shouldDebug();
        dItem tmp = valueOf(string, null, null);
        nope = false;
        return tmp;
    }

    public static dItem valueOf(String string, boolean debugMe) {
        nope = !debugMe;
        dItem tmp = valueOf(string, null, null);
        nope = false;
        return tmp;
    }

    /**
     * Gets a Item Object from a string form.
     *
     * @param string The string or dScript argument String
     * @param player The dPlayer to be used for player contexts
     *               where applicable.
     * @param npc    The dNPC to be used for NPC contexts
     *               where applicable.
     * @return an Item, or null if incorrectly formatted
     */
    public static dItem valueOf(String string, dPlayer player, dNPC npc) {
        if (string == null || string.equals("")) {
            return null;
        }

        Matcher m;
        dItem stack = null;

        ///////
        // Handle objects with properties through the object fetcher
        m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dItem.class, string, new BukkitTagContext(player, npc, false, null, !nope, null));
        }

        ////////
        // Match @object format for saved dItems

        m = item_by_saved.matcher(string);

        if (m.matches() && NotableManager.isSaved(m.group(2)) && NotableManager.isType(m.group(2), dItem.class)) {
            stack = (dItem) NotableManager.getSavedObject(m.group(2));

            if (m.group(3) != null) {
                stack.setAmount(Integer.valueOf(m.group(3)));
            }

            return stack;
        }

        string = string.replace("i@", "");

        m = ITEM_PATTERN.matcher(string);

        if (m.matches()) {

            try {

                ///////
                // Match item and book script custom items

                if (ScriptRegistry.containsScript(m.group(1), ItemScriptContainer.class)) {
                    ItemScriptContainer isc = ScriptRegistry.getScriptContainerAs(m.group(1), ItemScriptContainer.class);
                    // TODO: If a script does not contain tags, get the clean reference here.
                    stack = isc.getItemFrom(player, npc);
                }
                else if (ScriptRegistry.containsScript(m.group(1), BookScriptContainer.class)) {
                    // Get book from script
                    stack = ScriptRegistry.getScriptContainerAs
                            (m.group(1), BookScriptContainer.class).getBookFrom(player, npc);
                }

                if (stack != null) {

                    if (m.group(3) != null) {
                        stack.setAmount(Integer.valueOf(m.group(3)));
                    }
                    return stack;
                }
            }
            catch (Exception e) {
                // Just a catch, might be a regular item...
            }


            ///////
            // Match Bukkit/Minecraft standard items format

            try {
                String material = m.group(1).toUpperCase();

                if (ArgumentHelper.matchesInteger(material)) {
                    if (!nope) {
                        Debug.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release. For item input of '" + string + "'.");
                    }
                    stack = new dItem(Integer.valueOf(material));
                }
                else {
                    dMaterial mat = dMaterial.valueOf(material);
                    stack = new dItem(mat.getMaterial());
                    if (mat.hasData() && NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1)) {
                        stack.setDurability(mat.getData());
                    }
                }

                if (m.group(2) != null) {
                    stack.setDurability(Short.valueOf(m.group(2)));
                }
                if (m.group(3) != null) {
                    stack.setAmount(Integer.valueOf(m.group(3)));
                }

                return stack;
            }
            catch (Exception e) {
                if (!string.equalsIgnoreCase("none") && !nope) {
                    Debug.log("Does not match a valid item ID or material: " + string);
                }
            }
        }

        if (!nope) {
            Debug.log("valueOf dItem returning null: " + string);
        }

        // No match! Return null.
        return null;
    }

    // :( boolean for technicality, can be fixed
    // by making matches() method better.
    public static boolean nope = false;


    public static boolean matches(String arg) {

        if (arg == null) {
            return false;
        }

        // All dObjects should 'match' if there is a proper
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

        // TODO: Make this better. Probably creating some unnecessary
        // objects by doing this :(
        nope = true;
        if (valueOf(arg, null, null) != null) {
            nope = false;
            return true;
        }
        nope = false;
        return false;
    }

    ///////////////
    //   Constructors
    /////////////

    public dItem(Material material) {
        this(new ItemStack(material));
    }

    @Deprecated
    public dItem(int itemId) {
        this(MaterialCompat.updateItem(itemId));
    }

    private static ItemStack fixQty(ItemStack item, int qty) {
        item.setAmount(qty);
        return item;
    }

    @Deprecated
    public dItem(int itemId, int qty) {
        this(fixQty(MaterialCompat.updateItem(itemId), qty));
    }

    public dItem(Material material, int qty) {
        this(new ItemStack(material, qty));
    }

    public dItem(dMaterial material, int qty) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            this.item = new ItemStack(material.getMaterial(), qty);
        }
        else {
            this.item = new ItemStack(material.getMaterial(), qty, (short) 0, material.getData());
        }
    }

    public dItem(MaterialData data) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) && item.getType().isLegacy()) {
            this.item = new ItemStack(Bukkit.getUnsafe().fromLegacy(data));
        }
        else {
            this.item = data.toItemStack();
        }
    }

    public dItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            this.item = new ItemStack(Material.AIR, 0);
        }
        else {
            this.item = item;
        }
    }

    public dItem(Item item) {
        this(item.getItemStack());
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Bukkit itemstack associated

    private ItemStack item = null;

    public ItemStack getItemStack() {
        return item;
    }

    public void setItemStack(ItemStack item) {
        this.item = item;
    }

    // Compare item to item.
    // -1 indicates it is not a match
    //  0 indicates it is a perfect match
    //  1 or higher indicates the item being matched against
    //    was probably originally alike, but may have been
    //    modified or enhanced.

    public int comparesTo(dItem item) {
        return comparesTo(item.getItemStack());
    }

    public int comparesTo(ItemStack compared_to) {
        if (item == null) {
            return -1;
        }

        int determination = 0;
        ItemStack compared = getItemStack();

        // Will return -1 if these are not the same
        // Material IDs
        if (compared.getType().getId() != compared_to.getType().getId()) {
            return -1;
        }

        // If compared_to has item meta, and compared does not, return -1
        if (compared_to.hasItemMeta()) {
            if (!compared.hasItemMeta()) {
                return -1;
            }

            // If compared_to has a display name, and compared does not, return -1
            if (compared_to.getItemMeta().hasDisplayName()) {
                if (!compared.getItemMeta().hasDisplayName()) {
                    return -1;
                }

                // If compared_to's display name does not at least start with compared's item name,
                // return -1.
                if (compared_to.getItemMeta().getDisplayName().toUpperCase()
                        .startsWith(compared.getItemMeta().getDisplayName().toUpperCase())) {

                    // If the compared item has a longer display name than compared_to,
                    // it is similar, but modified. Perhaps 'engraved' or something?
                    if (compared.getItemMeta().getDisplayName().length() >
                            compared_to.getItemMeta().getDisplayName().length()) {
                        determination++;
                    }
                }
                else {
                    return -1;
                }
            }

            // If compared_to has lore, and compared does not, return -1
            if (compared_to.getItemMeta().hasLore()) {
                if (!compared.getItemMeta().hasLore()) {
                    return -1;
                }

                // If compared doesn't have a piece of lore contained in compared_to, return -1
                for (String lore : compared_to.getItemMeta().getLore()) {
                    if (!compared.getItemMeta().getLore().contains(lore)) {
                        return -1;
                    }
                }

                // If the compared item has more lore than compared to, it is similar, but modified.
                // Still qualifies for a match, but it seems the item may be a 'better' item, so increase
                // the determination.
                if (compared.getItemMeta().getLore().size() > compared_to.getItemMeta().getLore().size()) {
                    determination++;
                }
            }

            if (!compared_to.getItemMeta().getEnchants().isEmpty()) {
                if (compared.getItemMeta().getEnchants().isEmpty()) {
                    return -1;
                }

                for (Map.Entry<Enchantment, Integer> enchant : compared_to.getItemMeta().getEnchants().entrySet()) {
                    if (!compared.getItemMeta().getEnchants().containsKey(enchant.getKey())
                            || compared.getItemMeta().getEnchants().get(enchant.getKey()) < enchant.getValue()) {
                        return -1;
                    }
                }

                if (compared.getItemMeta().getEnchants().size() > compared_to.getItemMeta().getEnchants().size()) {
                    determination++;
                }
            }
        }

        if (isRepairable()) {
            if (compared.getDurability() < compared_to.getDurability()) {
                determination++;
            }
        }
        else
            // Check data
            if (getItemStack().getData().getData() != item.getData().getData()) {
                return -1;
            }

        return determination;
    }

    // Additional helper methods

    public void setStackSize(int size) {
        getItemStack().setAmount(size);
    }

    /**
     * Check whether this item contains a lore that starts
     * with a certain prefix.
     *
     * @param prefix The prefix
     * @return True if it does, otherwise false
     */
    public boolean containsLore(String prefix) {

        if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasLore()) {
            for (String itemLore : getItemStack().getItemMeta().getLore()) {
                if (itemLore.startsWith(prefix)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the lore from this item that starts with a
     * certain prefix.
     *
     * @param prefix The prefix
     * @return String  The lore
     */
    public String getLore(String prefix) {
        for (String itemLore : getItemStack().getItemMeta().getLore()) {
            if (itemLore.startsWith(prefix)) {
                return itemLore.substring(prefix.length());
            }
        }

        return "";
    }

    /**
     * Check whether this item contains the lore specific
     * to item scripts.
     *
     * @return True if it does, otherwise false
     */
    public boolean isItemscript() {
        return ItemScriptHelper.isItemscript(item);
    }

    public String getScriptName() {
        ItemScriptContainer cont = ItemScriptHelper.getItemScriptContainer(item);
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
            setItemStack(NMSHandler.getInstance().getItemHelper().addNbtData(getItemStack(), "Denizen Item Script", new StringTag(script.getHashID())));
        }
        else {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(0, script.getHashID());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    public dMaterial getMaterial() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            return new dMaterial(getItemStack().getType());
        }
        return OldMaterialsHelper.getMaterialFrom(getItemStack().getType(), getItemStack().getData().getData());
    }

    public String getMaterialName() {
        return CoreUtilities.toLowerCase(getItemStack().getType().name());
    }

    public void setAmount(int value) {
        if (item != null) {
            item.setAmount(value);
        }
    }

    public int getMaxStackSize() {
        return item.getMaxStackSize();
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
        if (item != null) {
            item.setDurability(value);
        }
    }

    public void setData(byte value) {
        if (item != null) {
            item.getData().setData(value);
        }
    }

    public boolean isRepairable() {
        return item.getType().getMaxDurability() > 0;
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
    public dItem setPrefix(String prefix) {
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
            return "i@" + NotableManager.getSavedId(this) + PropertyParser.getPropertiesString(this);
        }

        // Else, return the material name
        else if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1) && (item.getDurability() >= 16 || item.getDurability() < 0) && item.getType() != Material.AIR) {
            return "i@" + getMaterial().realName() + "," + item.getDurability() + PropertyParser.getPropertiesString(this);
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

    public String identifyMaterialNoIdentifier() {
        return getMaterial().identifySimpleNoIdentifier();
    }

    public String getFullString() {
        return "i@" + getMaterial().realName() + "," + item.getDurability() + PropertyParser.getPropertiesString(this);
    }


    @Override
    public String toString() {
        return identify();
    }


    @Override
    public boolean isUnique() {
        return NotableManager.isSaved(this);
    }


    @Override
    @Note("Items")
    public String getSaveObject() {
        return getFullString();
    }


    @Override
    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }


    @Override
    public void forget() {
        NotableManager.remove(this);
    }

    public static void registerTags() {

        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Debug.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release.");
                return new Element(((dItem) object).getItemStack().getType().getId())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        registerTag("data", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                Debug.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release.");
                return new Element(((dItem) object).getItemStack().getData().getData())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.with[<mechanism>=<value>;...]>
        // @returns dItem
        // @group properties
        // @description
        // Returns a copy of the item with mechanism adjustments applied.
        // -->
        registerTag("with", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (!attribute.hasContext(1)) {
                    Debug.echoError("i@item.with[...] tag must have an input mechanism list.");
                }
                dItem item = new dItem(((dItem) object).getItemStack().clone());
                List<String> properties = ObjectFetcher.separateProperties("[" + attribute.getContext(1) + "]");
                for (int i = 1; i < properties.size(); i++) {
                    List<String> data = CoreUtilities.split(properties.get(i), '=', 2);
                    if (data.size() != 2) {
                        Debug.echoError("Invalid property string '" + properties.get(i) + "'!");
                    }
                    else {
                        item.safeApplyProperty(new Mechanism(new Element(data.get(0)), new Element((data.get(1)).replace('‑', ';')), attribute.context));
                    }
                }
                return item.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.repairable>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the item can be repaired.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.durability>, <@link tag i@item.max_durability>,
        // and <@link tag i@item.durability>
        // -->
        registerTag("repairable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemDurability.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.is_crop>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the item is a growable crop.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.plant_growth> and <@link tag i@item.plant_growth>
        // -->
        registerTag("is_crop", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemPlantgrowth.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.is_book>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the item is considered an editable book.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.book>, <@link tag i@item.book>,
        // <@link tag i@item.book.author>, <@link tag i@item.book.title>,
        // <@link tag i@item.book.page_count>, <@link tag i@item.book.page[<#>]>,
        // and <@link tag i@item.book.pages>
        // -->
        registerTag("is_book", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemBook.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.is_colorable>
        // @returns Element(Boolean)
        // @group properties
        // Returns whether the item can have a custom color.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.color>, and <@link tag i@item.color>
        // -->
        registerTag("is_colorable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemColor.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        registerTag("is_dyeable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemColor.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.is_firework>
        // @returns Element(Boolean)
        // @group properties
        // Returns whether the item is a firework.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.firework>, and <@link tag i@item.firework>
        // -->
        registerTag("is_firework", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemFirework.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.has_inventory>
        // @returns Element(Boolean)
        // @group properties
        // Returns whether the item has an inventory.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.inventory>, and <@link tag i@item.inventory>
        // -->
        registerTag("has_inventory", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemInventory.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.is_lockable>
        // @returns Element(Boolean)
        // @group properties
        // Returns whether the item is lockable.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.lock>, and <@link tag i@item.lock>
        // -->
        registerTag("is_lockable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(ItemLock.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.material>
        // @returns dMaterial
        // @group conversion
        // @description
        // Returns the dMaterial that is the basis of the item.
        // EG, a stone with lore and a display name, etc. will return only "m@stone".
        // -->
        registerTag("material", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dItem item = (dItem) object;
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) &&
                        item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof BlockStateMeta) {
                    return new dMaterial(new ModernBlockData(((BlockStateMeta) item.getItemStack().getItemMeta()).getBlockState()))
                            .getAttribute(attribute.fulfill(1));
                }
                return item.getMaterial().getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.json>
        // @returns Element
        // @group conversion
        // @description
        // Returns the item converted to a raw JSON object with one layer of escaping for network transmission.
        // EG, via /tellraw.
        // EXAMPLE USAGE: execute as_server 'tellraw <player.name>
        // {"text":"","extra":[{"text":"This is the item in your hand ","color":"white"},
        // {"text":"Item","color":"white","hoverEvent":{"action":"show_item","value":"{<player.item_in_hand.json>}"}}]}'
        // -->
        registerTag("json", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(NMSHandler.getInstance().getItemHelper().getJsonString(((dItem) object).item))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.bukkit_serial>
        // @returns Element
        // @group conversion
        // @description
        // Returns a YAML text section representing the Bukkit serialization of the item, under subkey "item".
        // -->
        registerTag("bukkit_serial", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                YamlConfiguration config = new YamlConfiguration();
                config.set("item", ((dItem) object).getItemStack());
                return new Element(config.saveToString()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.full>
        // @returns Element
        // @group conversion
        // @description
        // Returns a full reusable item identification for this item, with extra, generally useless data.
        // Irrelevant on modern (1.13+) servers.
        // -->
        registerTag("full", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dItem) object).getFullString()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.simple>
        // @returns Element
        // @group conversion
        // @description
        // Returns a simple reusable item identification for this item, with minimal extra data.
        // -->
        registerTag("simple", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dItem) object).identifySimple()).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.notable_name>
        // @returns Element
        // @description
        // Gets the name of a Notable dItem. If the item isn't noted,
        // this is null.
        // -->
        registerTag("notable_name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                String notname = NotableManager.getSavedId((dItem) object);
                if (notname == null) {
                    return null;
                }
                return new Element(notname).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <i@item.type>
        // @returns Element
        // @description
        // Always returns 'Item' for dItem objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Item").getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    /////////////////
    // ATTRIBUTES
    /////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                com.denizenscript.denizencore.utilities.debugging.Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        //
        // TODO: Replace the next 2 with something saner in the tag section
        //

        // <--[tag]
        // @attribute <i@item.formatted>
        // @returns Element
        // @group formatting
        // @description
        // Returns the formatted material name of the item to be used in a sentence.
        // Correctly uses singular and plural forms of item names, among other things.
        // -->
        if (attribute.startsWith("material.formatted")) {
            attribute.fulfill(1);
        }

        if (attribute.startsWith("formatted")) {
            String id = CoreUtilities.toLowerCase(getMaterial().realName()).replace('_', ' ');

            if (id.equals("air")) {
                return new Element("nothing")
                        .getAttribute(attribute.fulfill(1));
            }

            if (id.equals("ice") || id.equals("dirt")) {
                return new Element(id)
                        .getAttribute(attribute.fulfill(1));
            }

            if (getItemStack().getAmount() > 1) {
                if (id.equals("cactus")) {
                    return new Element("cactuses")
                            .getAttribute(attribute.fulfill(1));
                }

                if (id.endsWith(" off")) {
                    id = id.substring(0, id.length() - 4);
                }
                if (id.endsWith(" on")) {
                    id = id.substring(0, id.length() - 3);
                }

                if (id.equals("rotten flesh") || id.equals("cooked fish")
                        || id.equals("raw fish") || id.endsWith("s")) {
                    return new Element(id)
                            .getAttribute(attribute.fulfill(1));
                }
                if (id.endsWith("y")) {
                    return new Element(id.substring(0, id.length() - 1) + "ies")
                            .getAttribute(attribute.fulfill(1));  // ex: lily -> lilies
                }
                if (id.endsWith("sh") || id.endsWith("ch")) {
                    return new Element(id + "es")
                            .getAttribute(attribute.fulfill(1));
                }
                // else
                return new Element(id + "s")
                        .getAttribute(attribute.fulfill(1)); // iron sword -> iron swords

            }
            else {
                if (id.equals("cactus")) {
                    return new Element("a cactus").getAttribute(attribute.fulfill(1));
                }
                if (id.endsWith("s")) {
                    return new Element(id).getAttribute(attribute.fulfill(1));
                }

                if (id.endsWith(" off")) {
                    return new Element("a " + id.substring(0, id.length() - 4))
                            .getAttribute(attribute.fulfill(1));
                }
                if (id.endsWith(" on")) {
                    return new Element("a " + id.substring(0, id.length() - 3))
                            .getAttribute(attribute.fulfill(1));
                }

                if (id.startsWith("a") || id.startsWith("e") || id.startsWith("i")
                        || id.startsWith("o") || id.startsWith("u")) {
                    return new Element("an " + id)
                            .getAttribute(attribute.fulfill(1));// ex: emerald -> an emerald
                }
                // else
                return new Element("a " + id)
                        .getAttribute(attribute.fulfill(1));// ex: diamond -> a diamond
            }
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }


    public void applyProperty(Mechanism mechanism) {
        if (NotableManager.isExactSavedObject(this)) {
            Debug.echoError("Cannot apply properties to noted objects.");
            return;
        }
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
