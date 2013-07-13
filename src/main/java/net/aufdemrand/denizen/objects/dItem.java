package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dItem implements dObject {

    final static Pattern itemPattern = Pattern.compile("(?:item:)?(\\w+)[:,]?(\\d+)?", Pattern.CASE_INSENSITIVE);

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
     * @param dPlayer  The dPlayer to be used for player contexts
     *                where applicable.
     * @param dNPC  The dNPC to be used for NPC contexts
     *                where applicable.
     * @return  an Item, or null if incorrectly formatted
     *
     */
    @ObjectFetcher("i")
    public static dItem valueOf(String string, dPlayer player, dNPC npc) {
        if (string == null) return null;
        Matcher m;

        ///////
        // Match @object format for spawned Item entities

        final Pattern item_by_entity_id = Pattern.compile("(i@)(\\d+)");
        m = item_by_entity_id.matcher(string);

        // Check if it's an entity in the world
        if (m.matches())
            for (World world : Bukkit.getWorlds())
                for (Entity entity : world.getEntitiesByClass(Item.class))
                    if (entity.getEntityId() == Integer.valueOf(m.group(2)))
                        return new dItem(((Item) entity).getItemStack());

        ////////
        // Match @object format for saved dItems

        final Pattern item_by_saved = Pattern.compile("(i@)(.+)");
        m = item_by_saved.matcher(string);

        if (m.matches() && isSaved(m.group(2)))
            return getSaved(m.group(2));

        string = string.replace("i@", "");

        ///////
        // Match item and book script custom items

        // Check if it's a valid item/book script
        
        if (ScriptRegistry.containsScript(string, ItemScriptContainer.class))
            // Get item from script
            return ScriptRegistry.getScriptContainerAs(string, ItemScriptContainer.class).getItemFrom(player, npc);
        
        else if (ScriptRegistry.containsScript(string, BookScriptContainer.class))
        	// Get book from script
            return ScriptRegistry.getScriptContainerAs(string, BookScriptContainer.class).getBookFrom(player, npc);

        ///////
        // Match bukkit/minecraft standard items format

        dItem stack = null;
        
        m = itemPattern.matcher(string);
        
        if (m.matches()) {
        	try {
        		String material = m.group(1).toUpperCase();
        		String data = null;
           
        		if (m.groupCount() > 1) {
        			data = m.group(2);
        		}
           
        		if (aH.matchesInteger(material)) {
        			stack = new dItem(Integer.valueOf(material));
        		}
        		else {
        			stack = new dItem(Material.valueOf(material));
        		}
           
        		if (data != null) {
        			stack.setDurability(Short.valueOf(m.group(2)));
        		}
           
        		return stack;
        	}
        	catch (Exception e) {
               // Just a catch, might be an item script...
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

    public void setDurability(short value) {
        if (item != null)
            item.setDurability(value);
    }
    
    // Additional helper methods

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
    	
    		if (isSaved(this))
    			return "i@" + getSaved(this);

    		// If not a saved item, but is a custom item, return the script id
    		else if (CustomNBT.hasCustomNBT(getItemStack(), "denizen-script-id"))
    			return "i@" + CustomNBT.getCustomNBT(getItemStack(), "denizen-script-id");
    	}

        // Else, return the material name and data
        return getItemStack().getType().name().toLowerCase()
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

        if (attribute.startsWith("qty"))
            return new Element(String.valueOf(getItemStack().getAmount()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("id"))
            return new Element(String.valueOf(getItemStack().getTypeId()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("max_stack"))
            return new Element(String.valueOf(getItemStack().getMaxStackSize()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("data"))
            return new Element(String.valueOf(getItemStack().getData().getData()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("durability"))
            return new Element(String.valueOf(getItemStack().getDurability()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("repairable"))
            return new Element(String.valueOf(isRepairable()))
                    .getAttribute(attribute.fulfill(1));

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

        if (attribute.startsWith("material"))
            return new Element(getItemStack().getType().toString())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("display"))
            if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasDisplayName())
                return new Element(getItemStack().getItemMeta().getDisplayName())
                        .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("enchantments")) {

        }

        if (attribute.startsWith("lore")) {
            if (getItemStack().hasItemMeta() && getItemStack().getItemMeta().hasLore())
                return new dList(getItemStack().getItemMeta().getLore()).getAttribute(attribute.fulfill(1));
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

        if (attribute.startsWith("identify"))
            return new Element(identify())
                    .getAttribute(attribute.fulfill(1));


        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
