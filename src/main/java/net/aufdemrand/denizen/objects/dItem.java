package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.notable.*;
import net.aufdemrand.denizen.objects.properties.item.*;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.notable.Note;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptHelper;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizencore.tags.TagContext;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dItem implements dObject, Notable, Adjustable {

    // An item pattern with the following groups:
    //
    // 1) An optional item: prefix.
    // 2) Word characters (letters and digits) and
    //    spaces that specify the name or ID of the item
    // 3) Digits that specify the special data value
    //    of the item
    // 4) Digits between [] brackets that specify the
    //    quantity of the item
    final static Pattern ITEM_PATTERN =
            Pattern.compile("(?:item:)?([\\w ]+)[:,]?(\\d+)?\\[?(\\d+)?\\]?",
                    Pattern.CASE_INSENSITIVE);

    final static Pattern item_by_saved = Pattern.compile("(i@)(.+)\\[?(\\d+)?\\]?");

    final public static String itemscriptIdentifier = "§0id:";

    //////////////////
    //    OBJECT FETCHER
    ////////////////


    public static dItem valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("i")
    public static dItem valueOf(String string, TagContext context) {
        if (context == null) {
            return valueOf(string, null, null);
        }
        else {
            return valueOf(string, ((BukkitTagContext) context).player, ((BukkitTagContext) context).npc);
        }
    }

    /**
     * Gets a Item Object from a string form.
     *
     * @param string  The string or dScript argument String
     * @param player  The dPlayer to be used for player contexts
     *                where applicable.
     * @param npc     The dNPC to be used for NPC contexts
     *                where applicable.
     * @return  an Item, or null if incorrectly formatted
     *
     */
    public static dItem valueOf(String string, dPlayer player, dNPC npc) {
        if (string == null) return null;

        Matcher m;
        dItem stack = null;

        ///////
        // Handle objects with properties through the object fetcher
        m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dItem.class,  string, new BukkitTagContext(player, npc, false, null, true, null));
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
                    // Get item from script
                    stack = ScriptRegistry.getScriptContainerAs
                            (m.group(1), ItemScriptContainer.class).getItemFrom(player, npc);
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

                if (aH.matchesInteger(material)) {
                    stack = new dItem(Integer.valueOf(material));
                }
                else {
                    dMaterial mat = dMaterial.valueOf(material);
                    stack = new dItem(mat.getMaterial());
                    if (mat.hasData()) stack.setDurability(mat.getData());
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
                if (!string.equalsIgnoreCase("none") && !nope)
                    dB.log("Does not match a valid item ID or material: " + string);
            }
        }

        if (!nope) dB.log("valueOf dItem returning null: " + string);

        // No match! Return null.
        return null;
    }

    // :( boolean for technicality, can be fixed
    // by making matches() method better.
    public static boolean nope = false;


    public static boolean matches(String arg) {

        if (arg == null) return false;

        // All dObjects should 'match' if there is a proper
        // ObjectFetcher identifier
        if (arg.toLowerCase().startsWith("i@"))
            return true;

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
        if (valueOf(arg) != null) {
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

    public dItem(int itemId) {
        this(new ItemStack(itemId));
    }

    public dItem(Material material, int qty) {
        this(new ItemStack(material, qty));
    }

    public dItem(dMaterial material, int qty) {
        this(new ItemStack(material.getMaterial(), qty, (short)0, material.getData()));
    }

    public dItem(int type, int qty) {
        this(new ItemStack(type, qty));
    }

    public dItem(ItemStack item) {
        if (item == null)
            this.item = new ItemStack(Material.AIR);
        else
            this.item = item;
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
    //  1 indicates the item being matched against
    //    was probably originally alike, but may have been
    //    modified or enhanced.

    public int comparesTo(dItem item) {
        return comparesTo(item.getItemStack());
    }

    public int comparesTo(ItemStack item) {
        if (item == null) return -1;

        int determination = 0;
        ItemStack compared = getItemStack();
        ItemStack compared_to = item;

        // Will return -1 if these are not the same
        // Material IDs
        if (compared.getTypeId() != compared_to.getTypeId()) return -1;

        // If compared_to has item meta, and compared does not, return -1
        if (compared_to.hasItemMeta()) {
            if (!compared.hasItemMeta()) return -1;

            // If compared_to has a display name, and compared does not, return -1
            if (compared_to.getItemMeta().hasDisplayName()) {
                if (!compared.getItemMeta().hasDisplayName()) return -1;

                // If compared_to's display name does not at least start with compared's item name,
                // return -1.
                if (compared_to.getItemMeta().getDisplayName().toUpperCase()
                        .startsWith(compared.getItemMeta().getDisplayName().toUpperCase())) {

                    // If the compared item has a longer display name than compared_to,
                    // it is similar, but modified. Perhaps 'engraved' or something?
                    if (compared.getItemMeta().getDisplayName().length() >
                            compared_to.getItemMeta().getDisplayName().length())
                        determination++;
                }

                else return -1;
            }

            // If compared_to has lore, and compared does not, return -1
            if (compared_to.getItemMeta().hasLore()) {
                if (!compared.getItemMeta().hasLore()) return -1;

                // If compared doesn't have a piece of lore contained in compared_to, return -1
                for (String lore : compared_to.getItemMeta().getLore()) {
                    if (!compared.getItemMeta().getLore().contains(lore)) return -1;
                }

                // If the compared item has more lore than compared to, it is similar, but modified.
                // Still qualifies for a match, but it seems the item may be a 'better' item, so increase
                // the determination.
                if (compared.getItemMeta().getLore().size() > compared_to.getItemMeta().getLore().size())
                    determination++;
            }

            if (!compared_to.getItemMeta().getEnchants().isEmpty()) {
                if (compared.getItemMeta().getEnchants().isEmpty()) return -1;

                for (Map.Entry<Enchantment, Integer> enchant : compared_to.getItemMeta().getEnchants().entrySet()) {
                    if (!compared.getItemMeta().getEnchants().containsKey(enchant.getKey())
                            || compared.getItemMeta().getEnchants().get(enchant.getKey()) < enchant.getValue())
                        return -1;
                }

                if (compared.getItemMeta().getEnchants().size() > compared_to.getItemMeta().getEnchants().size())
                    determination++;
            }
        }

        if (isRepairable()) {
            if (compared.getDurability() < compared_to.getDurability())
                determination++;
        } else
            // Check data
            if (getItemStack().getData().getData() != item.getData().getData()) return -1;

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
     * @param prefix  The prefix
     * @return  True if it does, otherwise false
     *
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
     * @param prefix  The prefix
     * @return  String  The lore
     *
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
     * @return  True if it does, otherwise false
     *
     */
    public boolean isItemscript() {
        return ItemScriptHelper.isItemscript(item);
    }

    public String getScriptName() {
        ItemScriptContainer cont = ItemScriptHelper.getItemScriptContainer(item);
        if (cont != null)
            return cont.getName();
        else
            return null;
    }

    public dMaterial getMaterial() {
        return dMaterial.getMaterialFrom(getItemStack().getType(), getItemStack().getData().getData());
    }

    public String getMaterialName() {
        return getItemStack().getType().name().toLowerCase();
    }

    public void setAmount(int value) {
        if (item != null)
            item.setAmount(value);
    }

    public int getMaxStackSize() {
        return item.getMaxStackSize();
    }

    public int getAmount() {
        if (item != null)
            return item.getAmount();
        else
            return 0;
    }

    public void setDurability(short value) {
        if (item != null)
            item.setDurability(value);
    }

    public void setData(byte value) {
        if (item != null)
            item.getData().setData(value);
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
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
    }

    @Override
    public String identify() {

        if (item == null) return "null";

        if (item.getTypeId() != 0) {

            // If saved item, return that
            if (isUnique()) {
                return "i@" + NotableManager.getSavedId(this) + (item.getAmount() == 1 ? "": "[quantity=" + item.getAmount() + "]");
            }

            // If not a saved item, but is a custom item, return the script id
            else if (isItemscript()) {
                return "i@" + getScriptName() + (item.getAmount() == 1 ? "": "[quantity=" + item.getAmount() + "]");
            }
        }

        // Else, return the material name
        if (item.getDurability() >= 16 || item.getDurability() < 0) {
            return "i@" + getMaterial().realName() + "," + item.getDurability()  + PropertyParser.getPropertiesString(this);
        }
        return "i@" + getMaterial().identify().replace("m@", "") + PropertyParser.getPropertiesString(this);
    }


    @Override
    public String identifySimple() {
        if (item == null) return "null";

        if (item.getTypeId() != 0) {

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


    // Special-case that essentially fetches the material of the items and uses its 'identify()' method
    public String identifyMaterial() {
        return dMaterial.getMaterialFrom(item.getType(), item.getData().getData()).identifySimple();
    }


    // Special-case that essentially fetches the material of the items and uses its 'identify()' method
    public String identifyMaterialNoIdentifier() {
        return dMaterial.getMaterialFrom(item.getType(), item.getData().getData()).identifySimpleNoIdentifier();
    }

    public String identifyNoIdentifier() {

        if (item == null) return "null";

        if (item.getTypeId() != 0) {

            // If saved item, return that
            if (isUnique()) {
                return NotableManager.getSavedId(this) + (item.getAmount() == 1 ? "": "[quantity=" + item.getAmount() + "]");
            }

            // If not a saved item, but is a custom item, return the script id
            else if (isItemscript()) {
                return getScriptName() + (item.getAmount() == 1 ? "": "[quantity=" + item.getAmount() + "]");
            }
        }

        // Else, return the material name
        if (item.getDurability() >= 16 || item.getDurability() < 0) {
            return getMaterial().realName() + "," + item.getDurability()  + PropertyParser.getPropertiesString(this);
        }
        return getMaterial().identifyNoIdentifier() + PropertyParser.getPropertiesString(this);
    }

    public String identifySimpleNoIdentifier() {
        if (item == null) return "null";

        if (item.getTypeId() != 0) {

            // If saved item, return that
            if (isUnique()) {
                return NotableManager.getSavedId(this);
            }

            // If not a saved item, but is a custom item, return the script id
            else if (isItemscript()) {
                return getScriptName();
            }
        }

        // Else, return the material name
        return identifyMaterialNoIdentifier();
    }

    public String getFullString() {
        return "i@" + (isItemscript() ? getScriptName(): getMaterial().realName()) + "," + item.getDurability() + PropertyParser.getPropertiesString(this);
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
    public void makeUnique(String id) { NotableManager.saveAs(this, id); }


    @Override
    public void forget() {
        NotableManager.remove(this);
    }


    /////////////////
    // ATTRIBUTES
    /////////


    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // <--[tag]
        // @attribute <i@item.id>
        // @returns Element(Number)
        // @group deprecated info
        // @description
        // Returns the item ID number of the item.
        // EG, a stone item will return 1.
        // Note that ID numbers are considered deprecated - you should use the names instead!
        // -->
        if (attribute.startsWith("id"))
            return new Element(getItemStack().getTypeId())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.data>
        // @returns Element(Number)
        // @group deprecated info
        // @description
        // Returns the data value of the material of the item.
        // EG, white wool will return 0, while red wool will return 14.
        // Note that data values are considered deprecated - you should use the names instead!
        // -->
        if (attribute.startsWith("data")) {
            return new Element(getItemStack().getData().getData())
                    .getAttribute(attribute.fulfill(1));
        }

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
        if (attribute.startsWith("repairable"))
            return new Element(ItemDurability.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.is_crop>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the item is a growable crop.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.plant_growth> and <@link tag i@item.plant_growth>
        // -->
        if (attribute.startsWith("is_crop"))
            return new Element(ItemPlantgrowth.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.is_book>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the item is considered an editable book.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.book>, <@link tag i@item.book>,
        // <@link tag i@item.book.author>, <@link tag i@item.book.title>,
        // <@link tag i@item.book.page_count>, <@link tag i@item.book.get_page[<#>]>,
        // and <@link tag i@item.book.pages>
        // -->
        if (attribute.startsWith("is_book")) {
            return new Element(ItemBook.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.is_dyeable>
        // @returns Element(Boolean)
        // @group properties
        // Returns whether the item can have a dye.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.dye>, and <@link tag i@item.dye_color>
        // -->
        if (attribute.startsWith("is_dyeable")) {
            return new Element(ItemDye.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.is_firework>
        // @returns Element(Boolean)
        // @group properties
        // Returns whether the item is a firework.
        // If this returns true, it will enable access to:
        // <@link mechanism dItem.firework>, and <@link tag i@item.firework>
        // -->
        if (attribute.startsWith("is_firework")) {
            return new Element(ItemFirework.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

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
            String id = getMaterial().realName().toLowerCase().replace('_', ' ');

            if (id.equals("air"))
                return new Element("nothing")
                        .getAttribute(attribute.fulfill(1));

            if (id.equals("ice") || id.equals("dirt"))
                return new Element(id)
                        .getAttribute(attribute.fulfill(1));

            if (getItemStack().getAmount() > 1) {
                if (id.equals("cactus"))
                    return new Element("cactuses")
                            .getAttribute(attribute.fulfill(1));
                if (id.endsWith("y"))
                    return new Element(id.substring(0, id.length() - 1) + "ies")
                            .getAttribute(attribute.fulfill(1));  // ex: lily -> lilies
                if (id.endsWith("s"))
                    return new Element(id)
                            .getAttribute(attribute.fulfill(1));  // ex: shears -> shears
                // else
                return new Element(id + "s")
                        .getAttribute(attribute.fulfill(1)); // iron sword -> iron swords

            }   else {
                if (id.equals("cactus")) return new Element("a cactus").getAttribute(attribute.fulfill(1));
                if (id.endsWith("s")) return new Element(id).getAttribute(attribute.fulfill(1));
                if (id.startsWith("a") || id.startsWith("e") || id.startsWith("i")
                        || id.startsWith("o") || id.startsWith("u"))
                    return new Element("an " + id)
                            .getAttribute(attribute.fulfill(1));// ex: emerald -> an emerald
                // else
                return new Element("a " + id)
                        .getAttribute(attribute.fulfill(1));// ex: diamond -> a diamond
            }
        }

        // <--[tag]
        // @attribute <i@item.material>
        // @returns dMaterial
        // @group conversion
        // @description
        // Returns the dMaterial that is the basis of the item.
        // EG, a stone with lore and a display name, etc. will return only "m@stone".
        // -->
        if (attribute.startsWith("material"))
            return getMaterial().getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.json>
        // @returns Element
        // @group conversion
        // @description
        // Returns the item converted to a raw JSON object for network transmission.
        // EG, via /tellraw.
        // EXAMPLE USAGE: execute as_server "tellraw <player.name>
        // {'text':'','extra':[{'text':'This is the item in your hand ','color':'white'},
        // {'text':'Item','color':'white','hoverEvent':{'action':'show_item','value':'{<player.item_in_hand.json>}'}}]}"
        // -->
        if (attribute.startsWith("json")) {
            String JSON = CraftItemStack.asNMSCopy(item).C().getChatModifier().toString();
            return new Element(JSON.substring(176, JSON.length() - 186))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.full>
        // @returns Element
        // @group conversion
        // @description
        // Returns a full reusable item identification for this item, with extra, generally useless data.
        // -->
        if (attribute.startsWith("full"))
            return new Element(getFullString()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.simple>
        // @returns Element
        // @group conversion
        // @description
        // Returns a simple reusable item identification for this item, with minimal extra data.
        // -->
        if (attribute.startsWith("simple"))
            return new Element(identifySimple()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.notable_name>
        // @returns Element
        // @description
        // Gets the name of a Notable dItem. If the item isn't noted,
        // this is null.
        // -->
        if (attribute.startsWith("notable_name")) {
            return new Element(NotableManager.getSavedId(this)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.scriptname>
        // @returns Element
        // @group scripts
        // @description
        // Returns the script name of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("scriptname"))
            if (isItemscript()) {
                return new Element(getScriptName())
                        .getAttribute(attribute.fulfill(1));
            }

        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.type>
        // @returns Element
        // @description
        // Always returns 'Item' for dItem objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("Item").getAttribute(attribute.fulfill(1));
        }
        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }


    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled())
                break;
        }

        if (!mechanism.fulfilled())
            mechanism.reportInvalid();

    }
}
