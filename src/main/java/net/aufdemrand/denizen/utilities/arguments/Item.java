package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.scripts.requirements.core.EnchantedRequirement;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item implements dScriptArgument {

    /////////////////////
    //   STATIC METHODS
    /////////////////


    // Patterns used in valueOf...
    // TODO: Make prettier.. maybe an array of Patterns isn't even necessary anymore.
    //  Seperate them out instead?
    final static Pattern[] getItemPtrn = {
            Pattern.compile("(?:(?:.+?:)|)(\\d+):(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+?):(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+)"),
            Pattern.compile("(?:(?:.+?:)|)item\\.(.+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:(?:.+?:)|)(.+)"),
    };

    /**
     * Gets a Item Object from a string form.
     *
     * @param string  the string or dScript argument String
     * @return  an Item, or null if incorrectly formatted
     *
     */

    public static Item valueOf(String string) {
        return valueOf(string, null, null);
    }

    public static Item valueOf(String string, Player player, dNPC npc) {

        if (string == null) return null;

        Matcher[] m = new Matcher[4];
        Item stack = null;

        // Check if a saved item instance from NEW
        m[0] = getItemPtrn[4].matcher(string);
        if (m[0].matches()) {
            // TODO: Finish NEW command.
        }

        // Check traditional item patterns.
        m[0] = getItemPtrn[0].matcher(string);
        m[1] = getItemPtrn[1].matcher(string);
        m[2] = getItemPtrn[2].matcher(string);
        m[3] = getItemPtrn[3].matcher(string);

        try {
            // Match 'ItemId:Data'
            if (m[0].matches()) {
                stack = new Item(Integer.valueOf(m[0].group(1)));
                stack.setDurability(Short.valueOf(m[0].group(2)));
                return stack;

                // Match 'ItemId'
            } else if (m[1].matches()) {
                stack = new Item(Integer.valueOf(m[1].group(1)));
                return stack;

                // Match 'Material:Data'
            } else if (m[2].matches()) {
                stack = new Item(Material.valueOf(m[2].group(1).toUpperCase()));
                stack.setDurability(Short.valueOf(m[2].group(2)));
                return stack;

                // Match 'Material'
            } else if (m[3].matches()) {
                stack = new Item(Material.valueOf(m[3].group(1).toUpperCase()));
                return stack;
            }

        } catch (Exception e) {
            // Just a catch, might be an item script...
        }

        // Check custom item script
        m[0] = getItemPtrn[5].matcher(string);
        if (m[0].matches() && ScriptRegistry.containsScript(m[0].group(1), ItemScriptContainer.class)) {

            dB.echoDebug("TEST!");

            // Get item from script
            return ScriptRegistry.getScriptContainerAs(m[0].group(1), ItemScriptContainer.class).getItemFrom(player, npc);
        }

        // No match.
        // dB.echoError("Invalid item! Failed to find a matching Item type.");
        return stack;
    }


    /////////////////////
    //   INSTANCE METHODS
    /////////////////


    private ItemStack item;
    private String prefix = "Item";

    public Item(Material material) {
        item = new ItemStack(material);
    }

    public Item(int itemId) {
        item = new ItemStack(itemId);
    }

    public Item(Material material, int qty) {
        item = new ItemStack(material, qty);
    }

    public Item(int type, int qty) {
        item = new ItemStack(type, qty);
    }

    public Item(ItemStack item) {
        this.item = item;
    }

    public int comparesTo(Item item) {
        return comparesTo(item.getItemStack());
    }

    public ItemStack getItemStack() {
        return item;
    }

    public int comparesTo(ItemStack item) {

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

    public void setDurability(short amt) {
        item.setDurability(amt);
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

    public String getId() {

        if (NBTItem.hasCustomNBT(getItemStack(), "denizen-script-id"))
             return NBTItem.getCustomNBT(getItemStack(), "denizen-script-id");
        else
            return getItemStack().getType().name().toLowerCase() + ":" + getItemStack().getData().getData();
    }

    public void setItemStack(ItemStack item) {
        this.item = item;
    }



    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////


    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + getId() + "<G>'  ";
    }

    @Override
    public String as_dScriptArg() {
        return prefix + ":" + getId();
    }

    public String dScriptArgValue() {
        return getDefaultPrefix().toLowerCase() + ":" + as_dScriptArg();
    }

    @Override
    public String toString() {
        // TODO: Serialize itemstack?
        return getId();
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // Desensitize the attribute for comparison
        String id = getId().toLowerCase();

        if (attribute.startsWith("qty"))
            return new Element(String.valueOf(getItemStack().getAmount()))
                .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("id"))
            return new Element(id)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("typeid"))
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

        if (attribute.startsWith("material.formatted")) {

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
            else return new dList("Empty dList", "").getAttribute(attribute.fulfill(1));
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

        return new Element(dScriptArgValue()).getAttribute(attribute.fulfill(1));
    }

}
