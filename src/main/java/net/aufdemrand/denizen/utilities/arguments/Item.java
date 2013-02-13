package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.ItemScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item extends ItemStack implements dScriptArgument {

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static Item getSavedItem(String id) {
        // TODO: Test to see if this is even possible. Keeping track of ACTUAL item instances may
        // be more difficult than entities.
        return null;
    }

    /**
     * Checks if there is a saved item with this Id.
     *
     * @param id  the Id to check
     * @return  true if it exists, false if not
     */
    public static boolean isSavedItem(String id) {
        // TODO: Test to see if this is even possible. Keeping track of ACTUAL item instances may
        // be more difficult than entities.
        return false;
    }

    /**
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallItems() {
        // TODO: Test to see if this is even possible. Keeping track of ACTUAL item instances may
        // be more difficult than entities.
    }

    /**
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveItems() {
        // TODO: Test to see if this is even possible. Keeping track of ACTUAL item instances may
        // be more difficult than entities.
    }

    // Patterns used in valueOf...
    // TODO: Make prettier.. maybe an array of Patterns isn't even necessary anymore.
    // Maybe seperate them out instead?
    final static Pattern[] getItemPtrn = {
            Pattern.compile("(?:(?:.+?:)|)(\\d+):(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+?):(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+)"),
            Pattern.compile("(?:(?:.+?:)|)itemstack\\.(.+)", Pattern.CASE_INSENSITIVE),
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
            // Don't report error yet, the item may identified in a custom item script.
        }

        // Check custom item script
        m[0] = getItemPtrn[5].matcher(string);
        if (m[0].matches() && ScriptRegistry.containsScript(m[0].group(1), InteractScriptContainer.class)) {
            // Get item from script
            return ScriptRegistry.getScriptContainerAs(m[0].group(1), ItemScriptContainer.class).getItemFrom();
        }

        // No match.
        dB.echoError("Invalid item! Failed to find a matching Item type.");
        return stack;
    }

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

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return null;
    }

    @Override
    public String dScriptArg() {
        return null;
    }

    @Override
    public String dScriptArgValue() {
        return getDefaultPrefix().toLowerCase() + ":" + dScriptArg();
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




}
