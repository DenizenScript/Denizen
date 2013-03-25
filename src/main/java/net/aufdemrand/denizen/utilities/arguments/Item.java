package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item extends ItemStack implements dScriptArgument {

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
                return stack.setId(stack.getType().name());

                // Match 'ItemId'
            } else if (m[1].matches()) {
                stack = new Item(Integer.valueOf(m[1].group(1)));
                stack.setId(stack.getType().name());
                return stack;

                // Match 'Material:Data'
            } else if (m[2].matches()) {
                stack = new Item(Material.valueOf(m[2].group(1).toUpperCase()));
                stack.setDurability(Short.valueOf(m[2].group(2)));
                return stack.setId(stack.getType().name());

                // Match 'Material'
            } else if (m[3].matches()) {
                stack = new Item(Material.valueOf(m[3].group(1).toUpperCase()));
                stack.setId(stack.getType().name());
                return stack;
            }

        } catch (Exception e) {
            // Just a catch, might be an item script...
        }

        // Check custom item script
        m[0] = getItemPtrn[5].matcher(string);
        if (m[0].matches() && ScriptRegistry.containsScript(m[0].group(1), ItemScriptContainer.class)) {
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


    private String id;
    private String prefix = "Item";

    public Item(Material material) {
        super(material);
        setId(getType().name());
    }

    public Item(int itemId) {
        super(itemId);
        setId(getType().name());
    }

    public Item(Material material, int qty) {
        super(material, qty);
        setId(getType().name());
    }

    public Item(int type, int qty) {
        super(type, qty);
        setId(getType().name());
    }

    public Item(ItemStack item) {
        super(item);
        setId(getType().name());
    }

    public ItemStack toCraftBukkit() {
        if (!((ItemStack) this instanceof CraftItemStack)) {
            try {
                // Use reflection to grant access to CraftItemStack constructer
                // which is not public
                Constructor<CraftItemStack> con = CraftItemStack.class
                        .getDeclaredConstructor(org.bukkit.inventory.ItemStack.class);
                con.setAccessible(true);
                return con.newInstance((ItemStack) this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Item setId(String id) {
        if (NBTItem.hasCustomNBT(this, "denizen-item-id"))
            this.id = NBTItem.getCustomNBT(this, "denizen-custom-item-id");
        else {
            this.id = id.toUpperCase();
            NBTItem.addCustomNBT(this, "denizen-item-id", id);
        }
        return this;
    }

    public boolean matches(ItemStack item) {
        return matches(new Item(item));
    }

    public boolean matches(Item item) {
        // Check IDs
        if (!id.equalsIgnoreCase(item.id))
            return false;
        return true;
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
        return "<G>" + prefix + "='<Y>" + id + " seconds<G>'  ";
    }

    @Override
    public String as_dScriptArg() {
        return prefix + ":" + id;
    }

    public String dScriptArgValue() {
        return getDefaultPrefix().toLowerCase() + ":" + as_dScriptArg();
    }

    @Override
    public String toString() {
        return serialize().toString();
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
        String id = this.id.toLowerCase();

        if (attribute.startsWith("qty"))
            return new Element(String.valueOf(getAmount()))
                .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("id"))
            return new Element(id)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("typeid"))
            return new Element(String.valueOf(getTypeId()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("max_stack"))
            return new Element(String.valueOf(getMaxStackSize()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("data"))
            return new Element(String.valueOf(getData().getData()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("durability"))
            return new Element(String.valueOf(getDurability()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("material.formatted")) {

            if (id.equals("air"))
                return new Element("nothing")
                        .getAttribute(attribute.fulfill(2));

            if (id.equals("ice") || id.equals("dirt"))
                return new Element(id)
                        .getAttribute(attribute.fulfill(2));

            if (getAmount() > 1) {
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
            return new Element(getType().toString())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("display"))
            if (hasItemMeta() && getItemMeta().hasDisplayName())
                return new Element(getItemMeta().getDisplayName())
                        .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("enchantments")) {

        }

        if (attribute.startsWith("lore")) {
            if (hasItemMeta() && getItemMeta().hasLore())
                return new dList(getItemMeta().getLore()).getAttribute(attribute.fulfill(1));
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
