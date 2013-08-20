package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dItem implements dObject {

    // An item pattern with the following groups:
    //
    // 1) An optional item: prefix.
    // 2) Word characters (letters and digits) and
    //    spaces that specify the name or ID of the item
    // 3) Digits that specify the special data value
    //    of the item
    // 4) Digits between [] brackets that specify the
    //    quantity of the item
    
    final static Pattern itemPattern =
            Pattern.compile("(?:item:)?([\\w ]+)[:,]?(\\d+)?\\[?(\\d+)?\\]?",
                    Pattern.CASE_INSENSITIVE);

    /////////////////////
    //  STATIC METHODS
    /////////////////

    public static Map<String, dItem> uniqueObjects = new HashMap<String, dItem>();

    public static boolean isSaved(String id) {
        return uniqueObjects.containsKey(id.toUpperCase());
    }

    public static boolean isSaved(dItem item) {
        return uniqueObjects.containsValue(item);
    }

    public static dItem getSaved(String id) {
        if (uniqueObjects.containsKey(id.toUpperCase()))
            return uniqueObjects.get(id.toUpperCase());
        else return null;
    }

    public static String getSaved(dItem item) {
        for (Map.Entry<String, dItem> i : uniqueObjects.entrySet())
            if (i.getValue() == item) return i.getKey();
        return null;
    }

    public static void saveAs(dItem item, String id) {
        if (item == null) return;
        uniqueObjects.put(id.toUpperCase(), item);
    }

    public static void remove(String id) {
        uniqueObjects.remove(id.toUpperCase());
    }


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
    @ObjectFetcher("i")
    public static dItem valueOf(String string, dPlayer player, dNPC npc) {
        if (string == null) return null;
        
        Matcher m;
        dItem stack = null;

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

        if (m.matches() && isSaved(m.group(2))) {
            stack = getSaved(m.group(2));
            
            if (m.group(3) != null) {
                stack.setAmount(Integer.valueOf(m.group(3)));
            }
            
            return stack;
        }

        string = string.replace("i@", "");
        
        m = itemPattern.matcher(string);
        
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
                    stack = new dItem(Material.valueOf(material));
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
                if (!string.equalsIgnoreCase("none"))
                dB.log("Does not match a valid item ID or material: " + string);
            }
        }
        
        if (!nope) dB.log("valueOf dItem returning null: " + string);

        // No match! Return null.
        return null;
    }

    public static boolean nope = false;

    public static boolean matches(String arg) {
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

    public dItem(int type, int qty) {
        item = new ItemStack(type, qty);
    }

    public dItem(ItemStack item) {
        this.item = item;
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
     * Check whether this item contains the lore specific
     * to item scripts.
     *
     * @return  True if it does, otherwise false
     *
     */
    public boolean isItemscript() {
        
        return containsLore("§0id:");
    }
    
    public String getMaterial() {
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
        switch (getItemStack().getType()) {
            case BOW:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SPADE:
            case DIAMOND_SWORD:
            case FISHING_ROD:
            case GOLD_AXE:
            case GOLD_HOE:
            case GOLD_PICKAXE:
            case GOLD_SPADE:
            case GOLD_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SPADE:
            case IRON_SWORD:
            case SHEARS:
            case WOOD_AXE:
            case WOOD_HOE:
            case WOOD_PICKAXE:
            case WOOD_SPADE:
            case WOOD_SWORD:
                return true;

            default:
                return getItemStack().getType().getId() >= Material.LEATHER_HELMET.getId()
                        && getItemStack().getType().getId() <= Material.GOLD_BOOTS.getId();
        }
    }

    public void setItemStack(ItemStack item) {
        this.item = item;
    }

    public dItem rememberAs(String id) {
        dItem.saveAs(this, id);
        return this;
    }


    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = "Item";

    @Override
    public String getType() {
        return "Item";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
    }

    @Override
    public String identify() {
        // If saved item, return that
        if (getItemStack() == null) return null;
        
        if (getItemStack().getTypeId() != 0) {
            
            if (isSaved(this)) {
                return "i@" + getSaved(this);
            }

            // If not a saved item, but is a custom item, return the script id
            else if (isItemscript()) {
                return "i@" + getLore("§0id:");
            }
        }

        // Else, return the material name and data
        return "i@" + getItemStack().getType().name().toLowerCase()
                    + (getItemStack().getData().getData() != 0 ? ":" + getItemStack().getData().getData() : "");
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public boolean isUnique() {
        return isSaved(this);
    }

    @Override
    public dItem setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // <--
        // <i@item.qty> -> Element(Number)
        // Returns the number of items in the dItem's itemstack.
        // -->
        if (attribute.startsWith("qty"))
            return new Element(String.valueOf(getItemStack().getAmount()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.identify> -> Element
        // Returns the dItem identification for the item.
        // -->
        if (attribute.startsWith("identify")) {
            return new Element(identify())
                    .getAttribute(attribute.fulfill(1));
        }
        
        // <--
        // <i@item.id> -> Element(Number)
        // Returns the item ID number of the dItem.
        // -->
        if (attribute.startsWith("id"))
            return new Element(String.valueOf(getItemStack().getTypeId()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.max_stack> -> Element(Number)
        // Returns the max number of this item possible in a single stack.
        // -->
        if (attribute.startsWith("max_stack"))
            return new Element(String.valueOf(getItemStack().getMaxStackSize()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.data> -> Element(Number)
        // Returns the data value of the material of the dItem.
        // -->
        if (attribute.startsWith("data"))
            return new Element(String.valueOf(getItemStack().getData().getData()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.durability> -> Element(Number)
        // Returns the durability of the dItem.
        // -->
        if (attribute.startsWith("durability"))
            return new Element(String.valueOf(getItemStack().getDurability()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.repairable> -> Element(Boolean)
        // Returns true if the dItem can be repaired. Otherwise, returns false.
        // -->
        if (attribute.startsWith("repairable"))
            return new Element(String.valueOf(isRepairable()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.material.formatted> -> Element
        // Returns the formatted material name of the dItem.
        // -->
        if (attribute.startsWith("material.formatted")) {

            String id = item.getType().name().toLowerCase();

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

        // <--
        // <i@item.material> -> Element
        // Returns the material name of the dItem.
        // -->
        if (attribute.startsWith("material"))
            return new Element(getItemStack().getType().toString())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.display> -> Element
        // Returns the display name of the dItem.
        // -->
        if (attribute.startsWith("display"))
            if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasDisplayName())
                return new Element(getItemStack().getItemMeta().getDisplayName())
                        .getAttribute(attribute.fulfill(1));

        // <--
        // <i@item.enchantments> -> dList
        // Returns a list of enchantment names on the dItem.
        // -->
        if (attribute.startsWith("enchantments")) {
            if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasEnchants()) {
                List<String> enchants = new ArrayList<String>();
                for (Enchantment enchantment : getItemStack().getEnchantments().keySet())
                    enchants.add(enchantment.getName());
                return new dList(enchants)
                        .getAttribute(attribute.fulfill(1));
            }
        }

        if (attribute.startsWith("book")) {
            if (getItemStack().getType() == Material.WRITTEN_BOOK) {
                attribute.fulfill(1);
                BookMeta bookInfo = (BookMeta) getItemStack().getItemMeta();

                // <--
                // <i@item.book.author> -> Element
                // Returns the author of the book.
                // -->
                if (attribute.startsWith("author"))
                    return new Element(bookInfo.getAuthor())
                            .getAttribute(attribute.fulfill(1));

                // <--
                // <i@item.book.title> -> Element
                // Returns the title of the book.
                // -->
                if (attribute.startsWith("title"))
                    return new Element(bookInfo.getTitle())
                            .getAttribute(attribute.fulfill(1));

                // <--
                // <i@item.book.page_count> -> Element(Number)
                // Returns the number of pages in the book.
                // -->
                if (attribute.startsWith("page_count"))
                    return new Element(bookInfo.getPageCount())
                            .getAttribute(attribute.fulfill(1));

                // <--
                // <i@item.book.get_page[<#>]> -> Element
                // Returns the page specified from the book.
                // -->
                if (attribute.startsWith("get_page") && aH.matchesInteger(attribute.getContext(1)))
                    return new Element(bookInfo.getPage(attribute.getIntContext(1)))
                            .getAttribute(attribute.fulfill(1));

                // <--
                // <i@item.book.pages> -> dList
                // Returns the pages of the book as a dList.
                // -->
                if (attribute.startsWith("pages"))
                    return new dList(bookInfo.getPages())
                            .getAttribute(attribute.fulfill(1));

            } else dB.echoError("Item referenced is not a written book!");
        }

        // <--
        // <i@item.scriptname> -> Element
        // Returns the script name of the dItem.
        // -->
        if (attribute.startsWith("scriptname")) // Note: Update this when the id: is stored less stupidly!
            if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasLore()) {
                List<String> loreList = new ArrayList<String>();
                for (String itemLore : getItemStack().getItemMeta().getLore())
                    if (itemLore.startsWith("§0id:"))
                        return new Element(itemLore.substring(5)).getAttribute(attribute.fulfill(1));
            }

        // <--
        // <i@item.lore> -> dList
        // Returns lore as a dList except for the "invisible" 
        // lore that holds the item script ID.
        // -->
        if (attribute.startsWith("lore")) {
            if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasLore()) {
                
                List<String> loreList = new ArrayList<String>();
                
                for (String itemLore : getItemStack().getItemMeta().getLore()) {
                    if (!itemLore.startsWith("§0id:")) {
                        loreList.add(itemLore);
                    }
                }
                return new dList(loreList).getAttribute(attribute.fulfill(1));
            }
            else return new dList("").getAttribute(attribute.fulfill(1));
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


        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
