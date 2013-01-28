package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.LeatherColorer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item extends ItemStack implements dScriptArgument {

    final static Pattern[] getItemPtrn = {
            Pattern.compile("(?:(?:.+?:)|)(\\d+):(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+?):(\\d+)"),
            Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+)"),
            Pattern.compile("(?:(?:.+?:)|)itemstack\\.(.+)", Pattern.CASE_INSENSITIVE)
    };

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static Item getSavedItem(String id) {
        return null;
    }

    /**
     * Checks if there is a saved location with this Id.
     *
     * @param id  the Id to check
     * @return  true if it exists, false if not
     */
    public static boolean isSavedItem(String id) {
        return false;
    }

    /**
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallItems() {

    }

    /**
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveItems() {

    }

    /**
     * Gets a Location Object from a string form of id,x,y,z,world
     * or a dScript argument (location:)x,y,z,world. If including an Id,
     * this location will persist and can be recalled at any time.
     *
     * @param string  the string or dScript argument String
     * @return  a Location, or null if incorrectly formatted
     *
     */
    public static Item valueOf(String string) {

        if (string == null) return null;

        Matcher[] m = new Matcher[4];
        Item stack = null;

        m[0] = getItemPtrn[4].matcher(string);
        if (m[0].matches()) {
            Script itemScript = Script.valueOf(m[0].group(1));
            if (itemScript != null) {

                // Make sure this is an 'ITEM'-type script
                if (!itemScript.getType().equalsIgnoreCase("ITEM")) {
                    dB.echoError("The script specified for use in building" +
                            " an Item is an invalid type! Must be type 'ITEM', " +
                            " found type '%s'.", itemScript.getType().toUpperCase());
                    return null;
                }

                // Check validity of material
                if (itemScript.getContents().contains("MATERIAL"))
                    stack = Item.valueOf(itemScript.getContents().getString("MATERIAL"));

                // Make sure we're working with a valid base ItemStack
                if (stack == null) return null;

                ItemMeta meta = stack.getItemMeta();

                // Set Display Name
                if (itemScript.getContents().contains("DISPLAY NAME"))
                    meta.setDisplayName(itemScript.getContents().getString("DISPLAY NAME"));

                // Set Enchantments
                if (itemScript.getContents().contains("ENCHANTMENTS")) {
                    for (String enchantment : itemScript.getContents().getStringList("ENCHANTMENTS")) {
                        try {
                            // Build enchantment context
                            int level = 1;
                            if (enchantment.split(":").length > 1) {
                                level = Integer.valueOf(enchantment.split(":")[1]);
                                enchantment = enchantment.split(":")[0];
                            }
                            // Add enchantment
                            Enchantment ench = Enchantment.getByName(enchantment.toUpperCase());
                            stack.addEnchantment(ench, level);
                        } catch (Exception e) {
                            // Invalid enchantment information, let's try the next entry
                            continue;
                        }
                    }
                }

                // Set Lore
                if (itemScript.getContents().contains("LORE"))
                    meta.setLore(itemScript.getContents().getStringList("LORE"));

                stack.setItemMeta(meta);

                // Set Color
                if (itemScript.getContents().contains("COLOR"))
                    LeatherColorer.colorArmor(stack, itemScript.getContents().getString("COLOR"));

                stack.setId(itemScript.getName());
                return stack;
            }

            return null;
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
                return new Item(Integer.valueOf(m[1].group(1)));

                // Match 'Material:Data'
            } else if (m[2].matches()) {
                stack = new Item(Material.valueOf(m[2].group(1).toUpperCase()));
                stack.setDurability(Short.valueOf(m[2].group(2)));
                return stack;

                // Match 'Material'
            } else if (m[3].matches()) {
                return new Item(Material.valueOf(m[3].group(1).toUpperCase()));
            }

        } catch (Exception e) {
            dB.echoError("Invalid item! Failed to find a matching Bukkit ItemStack.");
            if (dB.showStackTraces) e.printStackTrace();
        }

        return stack;
    }

    private String id;
    private String prefix = "Item";

    public Item(Material material) {
        super(material);
    }

    public Item(int itemId) {
        super(itemId);
    }

    public Item(Material material, int qty) {
        super(material, qty);
    }

    public Item(int type, int qty) {
        super(type, qty);
    }

    protected void setId(String id) {
        this.id = id;
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
