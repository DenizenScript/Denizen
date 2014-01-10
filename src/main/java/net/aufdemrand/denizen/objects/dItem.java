package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.properties.item.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dItem implements dObject, Notable, Adjustable {

    // TODO: Check out this pattern, is this going to/does it conflict with our new properties system?
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

    final public static String itemscriptIdentifier = "§0id:";



    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dItem valueOf(String string) {
        return valueOf(string, null, null);
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
    @Fetchable("i")
    public static dItem valueOf(String string, dPlayer player, dNPC npc) {
        if (string == null) return null;

        Matcher m;
        dItem stack = null;

        ///////
        // Handle objects with properties through the object fetcher
        m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dItem.class, string, player, npc);
        }

        ///////
        // Match @object format for spawned Item entities

        final Pattern item_by_entity_id = Pattern.compile("(i@)(\\d+)\\[?(\\d+)?\\]?");
        m = item_by_entity_id.matcher(string);

        // Check if it's an entity in the world
        if (m.matches()) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntitiesByClass(Item.class)) {
                    if (entity.getEntityId() == Integer.valueOf(m.group(2))) {
                        stack = new dItem(((Item) entity).getItemStack());

                        if (m.group(3) != null) {
                            stack.setAmount(Integer.valueOf(m.group(3)));
                        }

                        return stack;
                    }
                }
            }
        }

        ////////
        // Match @object format for saved dItems

        final Pattern item_by_saved = Pattern.compile("(i@)(.+)\\[?(\\d+)?\\]?");
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
        item = new ItemStack(material);
    }

    public dItem(int itemId) {
        item = new ItemStack(itemId);
    }

    public dItem(Material material, int qty) {
        item = new ItemStack(material, qty);
    }

    public dItem(dMaterial material, int qty) {
        item = new ItemStack(material.getMaterial(), qty, (short)0, material.getData());
    }

    public dItem(int type, int qty) {
        item = new ItemStack(type, qty);
    }

    public dItem(ItemStack item) {
        this.item = item;
    }

    public dItem(Item item) {
        this.item = item.getItemStack();
    }



    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Bukkit itemstack associated

    private ItemStack item = null;

    public ItemStack getItemStack() {
        return item;
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
     * Check whether this item is some form of armor.
     *
     * @return  True if it is, otherwise false.
     */
    public boolean isArmor() {
        int type = item.getTypeId();
        if (type >= 298 && type <= 317) {
            return true;
        }
        return false;
    }

    /**
     * Check whether this item contains the lore specific
     * to item scripts.
     *
     * @return  True if it does, otherwise false
     *
     */
    public boolean isItemscript() {
        return containsLore(itemscriptIdentifier);
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
                return "i@" + getLore(itemscriptIdentifier) + (item.getAmount() == 1 ? "": "[quantity=" + item.getAmount() + "]");
            }
        }

        // Else, return the material name
        return "i@" + identifyMaterial().replace("m@", "") + PropertyParser.getPropertiesString(this);
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
                return "i@" + getLore(itemscriptIdentifier);
            }
        }

        // Else, return the material name
        return "i@" + identifyMaterial().replace("m@", "");
    }


    // Special-case that essentially fetches the material of the items and uses its 'identify()' method
    public String identifyMaterial() {
        return dMaterial.getMaterialFrom(item.getType(), item.getData().getData()).identify();
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
    public Object getSaveObject() {

        StringBuilder sb = new StringBuilder();

        sb.append("i@");
        sb.append(item.getType().name().toUpperCase());

        return sb.toString();
    }


    @Override
    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }


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
        // @description
        // Returns the item ID number of the item.
        // -->
        if (attribute.startsWith("id"))
            return new Element(getItemStack().getTypeId())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.data>
        // @returns Element(Number)
        // @description
        // Returns the data value of the material of the item.
        // -->
        if (attribute.startsWith("data")) {
            return new Element(getItemStack().getData().getData())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.repairable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the item can be repaired.
        // -->
        if (attribute.startsWith("repairable"))
            return new Element(ItemDurability.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.has_lore>
        // @returns Element(Boolean)
        // @description
        // Returns whether the item has lore set on it.
        // -->
        if (attribute.startsWith("has_lore"))
            return new Element(ItemLore.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.is_crop>
        // @returns Element(Boolean)
        // @description
        // Returns whether the item is a growable crop.
        // -->
        if (attribute.startsWith("is_crop"))
            return new Element(ItemPlantgrowth.describes(this))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.has_display>
        // @returns Element(Boolean)
        // @description
        // Returns whether the item has a custom set display name.
        // -->
        if (attribute.startsWith("has_display")) {
            return new Element(ItemDisplayname.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.is_book>
        // @returns Element(Boolean)
        // @description
        // Returns whether the item is considered an editable book.
        // -->
        if (attribute.startsWith("is_book")) {
            return new Element(ItemBook.describes(this))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.material.formatted>
        // @returns Element
        // @description
        // Returns the formatted material name of the item to be used in a sentence.
        // Correctly uses singular and plural forms of item names, among other things.
        // -->
        if (attribute.startsWith("material.formatted")) {

            String id = item.getType().name().toLowerCase().replace('_', ' ');

            if (id.equals("air"))
                return new Element("nothing")
                        .getAttribute(attribute.fulfill(2));

            if (id.equals("ice") || id.equals("dirt"))
                return new Element(id)
                        .getAttribute(attribute.fulfill(2));

            if (getItemStack().getAmount() > 1) {
                if (id.equals("cactus"))
                    return new Element("cactuses")
                            .getAttribute(attribute.fulfill(2));
                if (id.endsWith("y"))
                    return new Element(id.substring(0, id.length() - 1) + "ies")
                            .getAttribute(attribute.fulfill(2));  // ex: lily -> lilies
                if (id.endsWith("s"))
                    return new Element(id)
                            .getAttribute(attribute.fulfill(2));  // ex: shears -> shears
                // else
                return new Element(id + "s")
                        .getAttribute(attribute.fulfill(2)); // iron sword -> iron swords

            }   else {
                if (id.equals("cactus")) return new Element("a cactus").getAttribute(attribute.fulfill(2));
                if (id.endsWith("s")) return new Element(id).getAttribute(attribute.fulfill(2));
                if (id.startsWith("a") || id.startsWith("e") || id.startsWith("i")
                        || id.startsWith("o") || id.startsWith("u"))
                    return new Element("an " + id)
                            .getAttribute(attribute.fulfill(2));// ex: emerald -> an emerald
                // else
                return new Element("a " + id)
                        .getAttribute(attribute.fulfill(2));// ex: diamond -> a diamond
            }
        }

        // <--[tag]
        // @attribute <i@item.material>
        // @returns dMaterial
        // @description
        // Returns the material corresponding to the item.
        // -->
        if (attribute.startsWith("material"))
            return getMaterial().getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.scriptname>
        // @returns Element
        // @description
        // Returns the script name of the item if it was created by an item script.
        // -->
        if (attribute.startsWith("scriptname")) // Note: Update this when the id: is stored differently
            if (isItemscript()) {
                return new Element(getLore(itemscriptIdentifier))
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

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }


    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

        // <--[mechanism]
        // @object dItem
        // @name display_name
        // @input Element
        // @description
        // Changes the items display name.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.display>
        // -->
        if (mechanism.matches("display_name")) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ItemBook.unEscape(value.asString()));
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object dItem
        // @name lore
        // @input dList
        // @description
        // Sets the item's lore.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.lore>
        // -->
        if (mechanism.matches("lore")) {
            ItemMeta meta = item.getItemMeta();
            dList lore = value.asType(dList.class);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ItemBook.unEscape(lore.get(i)));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object dItem
        // @name enchantments
        // @input dList
        // @description
        // Sets the item's enchantments.
        // @tags
        // <i@item.enchantments>
        // <i@item.enchantments.levels>
        // <i@item.enchantments.with_levels>
        // -->
        if (mechanism.matches("enchantments")) {
            for (String enchant: value.asType(dList.class)) {
                if (!enchant.contains(","))
                    dB.echoError("Invalid enchantment format, use name,level|...");
                else {
                    String[] data = enchant.split(",", 2);
                    if (Integer.valueOf(data[1]) == null)
                        dB.echoError("Cannot apply enchantment '" + data[0] +"': '" + data[1] + "' is not a valid integer!");
                    else {
                        try {
                            item.addUnsafeEnchantment(Enchantment.getByName(data[0].toUpperCase()), Integer.valueOf(data[1]));
                        }
                        catch (NullPointerException e) {
                            dB.echoError("Unknown enchantment '" + data[0] + "'");
                        }
                    }
                }
            }
        }

        // <--[mechanism]
        // @object dItem
        // @name quantity
        // @input Element(Number)
        // @description
        // Changes the number of items in this stack.
        // @tags
        // <i@item.qty>
        // <i@item.max_stack>
        // -->
        if (mechanism.matches("quantity") && mechanism.requireInteger()) {
            item.setAmount(value.asInt());
        }

        // <--[mechanism]
        // @object dItem
        // @name durability
        // @input Element(Number)
        // @description
        // Changes the durability of damageable items.
        // @tags
        // <i@item.durability>
        // <i@item.max_durability>
        // <i@item.repairable>
        // -->
        if (mechanism.matches("durability") && mechanism.requireInteger()) {
            if (ItemDurability.describes(this))
                item.setDurability((short)value.asInt());
            else
                dB.echoError("Material '" + getMaterial().identify().replace("m@", "") + "' is not repairable.");
        }

        // <--[mechanism]
        // @object dItem
        // @name skull_skin
        // @input Element
        // @description
        // Changes the durability of damageable items.
        // @tags
        // <i@item.skin>
        // <i@item.has_skin>
        // -->
        if (mechanism.matches("skull_skin")) {
            if (ItemSkullskin.describes(this)) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setOwner(value.asString());
                item.setItemMeta(meta);
            }
            else
                dB.echoError("Material '" + getMaterial().identify().replace("m@", "") + "' cannot hold a skin.");
        }

        // <--[mechanism]
        // @object dItem
        // @name plant_growth
        // @input Element
        // @description
        // Changes the growth level of plant items.
        // See <@link tag i@item.plant_growth> for valid inputs.
        // @tags
        // <i@item.is_crop>
        // <i@item.plant_growth>
        // -->
        if (mechanism.matches("plant_growth")) {
            if (ItemPlantgrowth.describes(this)) {
                Element inputValue = new Element(value.asString().toUpperCase());
                if (item.getData() instanceof Crops && inputValue.matchesEnum(CropState.values()))
                    ((Crops)item.getData()).setState(CropState.valueOf(value.asString().toUpperCase()));
                else if (item.getData() instanceof NetherWarts && inputValue.matchesEnum(NetherWartsState.values()))
                    ((NetherWarts)item.getData()).setState(NetherWartsState.valueOf(value.asString().toUpperCase()));
                else if (item.getData() instanceof CocoaPlant && inputValue.matchesEnum(CocoaPlant.CocoaPlantSize.values()))
                    ((CocoaPlant)item.getData()).setSize(CocoaPlant.CocoaPlantSize.valueOf(value.asString().toUpperCase()));
                else if (mechanism.requireInteger())
                    item.getData().setData((byte) value.asInt());
            }
            else
                dB.echoError("Material '" + getMaterial().identify().replace("m@", "") + "' is not a plant.");
        }

        // <--[mechanism]
        // @object dItem
        // @name book
        // @input Element
        // @description
        // Changes the information on a book item.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.is_book>
        // <i@item.book.author>
        // <i@item.book.title>
        // <i@item.book.page_count>
        // <i@item.book.get_page[<#>]>
        // <i@item.book.pages>
        // <i@item.book>
        // -->
        if (mechanism.matches("book")) {
            if (ItemBook.describes(this)) {
                BookMeta meta = (BookMeta) item.getItemMeta();
                dList data = value.asType(dList.class);
                if (data.size() < 2) {
                    dB.echoError("Invalid book input!");
                }
                else {
                    if (data.size() > 4 && data.get(0).equalsIgnoreCase("author")
                            && data.get(2).equalsIgnoreCase("title")) {
                        if (!item.getType().equals(Material.WRITTEN_BOOK)) {
                            dB.echoError("That type of book cannot have title or author!");
                        }
                        else {
                            meta.setAuthor(ItemBook.unEscape(data.get(1)));
                            meta.setTitle(ItemBook.unEscape(data.get(3)));
                            for (int i = 0; i < 4; i++)
                                data.remove(0); // No .removeRange?
                        }
                    }
                    if (!data.get(0).equalsIgnoreCase("pages")) {
                        dB.echoError("Invalid book input!");
                    }
                    else {
                        ArrayList<String> newPages = new ArrayList<String>();
                        for (int i = 1; i < data.size(); i++) {
                            newPages.add(ItemBook.unEscape(data.get(i)));
                        }
                        meta.setPages(newPages);
                    }
                    item.setItemMeta(meta);
                }
            }
            else
                dB.echoError("Material '" + getMaterial().identify().replace("m@", "") + "' is not a book.");
        }



        if (!mechanism.fulfilled())
            mechanism.reportInvalid();

    }

}
