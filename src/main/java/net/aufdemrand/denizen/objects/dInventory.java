package net.aufdemrand.denizen.objects;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import net.aufdemrand.denizen.objects.aH.Argument;
import net.aufdemrand.denizen.objects.aH.PrimitiveType;
import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.notable.Note;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.InventoryScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class dInventory implements dObject, Notable, Adjustable {

    // The maximum number of slots a Bukkit inventory can have
    final static public int maxSlots = 54;

    public static dInventory mirrorBukkitInventory(Inventory inventory) {
        // Iterate through Notable Inventories
        for (dObject inv : NotableManager.getAllType(dInventory.class)) {
            if (((CraftInventory) ((dInventory) inv).inventory).getInventory().equals(((CraftInventory) inventory).getInventory()))
                return (dInventory) inv;
        }

        return new dInventory(inventory);
    }

    /////////////////////
    //   PATTERNS
    /////////////////

    final static Pattern inventory_by_type = Pattern.compile("(in@)(npc|player|entity|location|equipment|generic)\\[(.+?)\\]", Pattern.CASE_INSENSITIVE);
    final static Pattern inventory_by_script = Pattern.compile("(in@)(.+)", Pattern.CASE_INSENSITIVE);

    /////////////////////
    //   NOTABLE METHODS
    /////////////////

    public boolean isUnique() {
        return idType.equals("notable");
    }

    @Note("inventory")
    public String getSaveObject() {
        return idHolder;
    }

    public void makeUnique(String id) {
        idType = "notable";
        idHolder = id;
        NotableManager.saveAs(this, id);
    }

    public void forget() {
        NotableManager.remove(idHolder);
        loadIdentifiers();
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dInventory valueOf(String string) {
        return valueOf(string, null, null);
    }

    /**
     *
     * Gets a dInventory from a string format.
     *
     * @param string
     *          The inventory in string form. (in@player[playerName], in@scriptName, etc.)
     * @return
     *          The dInventory value. If the string is incorrectly formatted or
     *          the specified inventory is invalid, this is null.
     *
     */
    @Fetchable("in")
    public static dInventory valueOf(String string, dPlayer player, dNPC npc) {

        if (string == null) return null;

        ///////
        // Handle objects with properties through the object fetcher
        Matcher m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dInventory.class, string, player, npc);
        }

        // Match in@scriptName for Inventory Scripts, as well as in@notableName
        m = inventory_by_script.matcher(string);

        if (m.matches()) {
            if (ScriptRegistry.containsScript(m.group(2), InventoryScriptContainer.class))
                return ScriptRegistry.getScriptContainerAs(m.group(2), InventoryScriptContainer.class)
                        .getInventoryFrom(player, npc);

            if (NotableManager.isSaved(m.group(2)) && NotableManager.isType(m.group(2), dInventory.class))
                return (dInventory) NotableManager.getSavedObject(m.group(2));

            if (m.group(2).toLowerCase().matches("npc|player|entity|location|equipment|generic"))
                return new dInventory(m.group(2));
        }

        m = inventory_by_type.matcher(string);

        if (m.matches()) {

            // Set the type of the inventory holder
            String type = m.group(2).toLowerCase();
            // Set the name/id/location of the inventory holder
            String holder = m.group(3);

            if (type.equals("generic")) {
                Argument arg = Argument.valueOf(holder);
                if (arg.matchesEnum(InventoryType.values())) {
                    return new dInventory(InventoryType.valueOf(holder.toUpperCase()));
                }
                else if (arg.matchesPrimitive(PrimitiveType.Integer)) {
                    return new dInventory(arg.asElement().asInt());
                }
                else {
                    dB.echoError("That type of inventory does not exist!");
                }
            }
            else if (type.equals("npc")) {
                if (dNPC.matches(holder))
                    return dNPC.valueOf(holder).getDenizenEntity().getInventory();
            }
            else if (type.equals("player")) {
                if (dPlayer.matches(holder))
                    return dPlayer.valueOf(holder).getDenizenEntity().getInventory();
            }
            else if (type.equals("entity")) {
                if (dEntity.matches(holder))
                    return dEntity.valueOf(holder).getInventory();
            }
            else if (type.equals("location")) {
                if (dLocation.matches(holder))
                    return dLocation.valueOf(holder).getInventory();
            }
            else if (type.equals("equipment")) {
                dEntity.valueOf(holder).getEquipment();
            }

            // If the dInventory is invalid, alert the user and return null
            dB.echoError("Value of dInventory returning null. Invalid " +
                    type + " specified: " + holder);
            return null;
        }

        dB.echoError("Value of dInventory returning null. Invalid dInventory specified: " + string);
        return null;
    }

    /**
     * Determine whether a string is a valid inventory.
     *
     * @param arg  the arg string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {

        // Every single dInventory should have the in@ prefix. No exceptions.
        return arg.toLowerCase().startsWith("in@");

    }


    ///////////////
    //   Constructors
    /////////////

    String idType = null;
    String idHolder = null;

    public dInventory(Inventory inventory) {
        this.inventory = inventory;
        loadIdentifiers();
    }

    public dInventory(InventoryHolder holder) {
        inventory = holder.getInventory();
        loadIdentifiers();
    }

    public dInventory(int size, String title) {
        if (size <= 0 || size%9 != 0) {
            dB.echoError("InventorySize must be multiple of 9, and greater than 0.");
            return;
        }
        inventory = Bukkit.getServer().createInventory(null, size, title);
        loadIdentifiers();
    }

    public dInventory(InventoryType type) {
        inventory = Bukkit.getServer().createInventory(null, type);
        loadIdentifiers();
    }

    public dInventory(int size) {
        this(size, "Chest");
    }

    public dInventory(String idType) {
        this.idType = idType.toLowerCase();
    }

    public dInventory(dObject object) {
        if (object instanceof dEntity)
            inventory = ((dEntity) object).getInventory().getInventory();
        else if (object instanceof dLocation)
            inventory = ((dLocation) object).getInventory().getInventory();
        else if (object instanceof Element) {
            Element element = (Element) object;
            if (element.matchesEnum(InventoryType.values()))
                inventory = Bukkit.getServer().createInventory(null,
                        InventoryType.valueOf(((Element) object).asString().toUpperCase()));
            else if (element.isInt()) {
                String title = null;
                if (inventory != null)
                    title = inventory.getTitle();
                inventory = Bukkit.getServer().createInventory(null, element.asInt(),
                        title != null ? title : "Chest");
            }
        }
        if (inventory != null)
            loadIdentifiers();
    }



    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Inventory

    private Inventory inventory = null;

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Changes the inventory to a new inventory, possibly of a different
     * type, size, and with different contents.
     * NOTE: SHOULD ONLY BE USED IN CASES WHERE THERE
     *       ARE NO OTHER OPTIONS.
     *
     * @param inventory  The new inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        loadIdentifiers();
    }

    public void setTitle(String title) {
        if (!idType.equals("generic") || title == null)
            return;
        else if (inventory == null) {
            inventory = Bukkit.getServer().createInventory(null, maxSlots, title);
            loadIdentifiers();
            return;
        }
        ItemStack[] contents = inventory.getContents();
        inventory = Bukkit.createInventory(null, inventory.getSize(), title);
        inventory.setContents(contents);
        loadIdentifiers();
    }

    public void setSize(int size) {
        if (!idType.equals("generic"))
            return;
        else if (size <= 0 || size%9 != 0) {
            dB.echoError("InventorySize must be multiple of 9, and greater than 0.");
            return;
        }
        else if (inventory == null) {
            inventory = Bukkit.getServer().createInventory(null, size, "Chest");
            loadIdentifiers();
            return;
        }
        int oldSize = inventory.getSize();
        ItemStack[] oldContents = inventory.getContents();
        ItemStack[] newContents = new ItemStack[size];
        if (oldSize > size)
            for (int i = 0; i < size; i++)
                newContents[i] = oldContents[i];
        else
            newContents = oldContents;
        String title = inventory.getTitle();
        inventory = Bukkit.getServer().createInventory(null, size,
                (title != null ? title : inventory.getType().getDefaultTitle()));
        inventory.setContents(newContents);
        loadIdentifiers();
    }

    private void loadIdentifiers() {
        InventoryHolder holder = inventory.getHolder();

        if (holder != null) {
            if (holder instanceof Entity && CitizensAPI.getNPCRegistry().isNPC((Entity) holder)) {
                idType = "npc";
                idHolder = "n@" + CitizensAPI.getNPCRegistry().getNPC((Entity) holder).getId();
            }
            else if (holder instanceof Player) {
                idType = "player";
                idHolder = "p@" + ((Player) holder).getName();
            }
            else if (holder instanceof Entity) {
                idType = "entity";
                idHolder = "e@" + ((Entity) holder).getEntityId();
            }
            else {
                idType = "location";
                if (getLocation() != null)
                    idHolder = getLocation().identify();
                else
                    idHolder = "null";
            }
        }
        else {
            idType = "generic";
            idHolder = getInventory().getType().name();
        }
    }

    public void setIdentifiers(String type, String holder) {
        idType = type;
        idHolder = holder;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdHolder() {
        return idHolder;
    }

    /**
     * Return the dLocation of this inventory's
     * holder
     *
     * @return  The holder's dLocation
     *
     */

    public dLocation getLocation() {

        if (inventory != null) {

            InventoryHolder holder = inventory.getHolder();

            if (holder instanceof BlockState) {
                return new dLocation(((BlockState) holder).getLocation());
            }
            else if (holder instanceof Entity) {
                Entity entity = (Entity) holder;
                if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
                    if (entity.getLocation() == null)
                        return new dLocation(CitizensAPI.getNPCRegistry().getNPC(entity).getStoredLocation());
                }
                return new dLocation(((Entity) holder).getLocation());
            }
        }

        return null;
    }

    public ItemStack[] getContents() {
        if (inventory != null) return inventory.getContents();
        else return new ItemStack[0];
    }

    public dInventory getEquipment() {
        if (inventory instanceof PlayerInventory) {
            return new dInventory(InventoryType.CRAFTING)
                .add(((PlayerInventory) inventory).getArmorContents());
        }
        else if (inventory instanceof HorseInventory) {
            return new dInventory(InventoryType.CRAFTING)
                .add(((HorseInventory) inventory).getArmor())
                .add(((HorseInventory) inventory).getSaddle());
        }
        return null;
    }

    public InventoryType getInventoryType() {
        return inventory.getType();
    }

    public int getSize() {
        return inventory.getSize();
    }

    public void remove(ItemStack item) {
        inventory.remove(item);
    }

    public void setContents(ItemStack[] contents) {
        inventory.setContents(contents);
    }

    public void setContents(dList list) {
        int size = 0;
        if (inventory == null) {
            size = Math.round(list.size()/9)*9;
            if (size == 0) size = 9;
            inventory = Bukkit.createInventory(null, size);
            loadIdentifiers();
        }
        else
            size = inventory.getSize();
        ItemStack[] contents = new ItemStack[size];
        int filled = 0;
        for (dItem item : list.filter(dItem.class)) {
            contents[filled] = item.getItemStack();
            filled++;
        }
        final ItemStack air = new ItemStack(Material.AIR);
        while (filled < size) {
            contents[filled] = air;
            filled ++;
        }
        inventory.setContents(contents);
    }

    public boolean update() {
        if (idType.equals("player")) {
            dPlayer.valueOf(idHolder).getPlayerEntity().updateInventory();
            return true;
        }
        return false;
    }

    /////////////////////
    //   INVENTORY MANIPULATION
    /////////////////

    /**
     * Add an array of items to this inventory
     * and return the result
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */

    public dInventory add(ItemStack... items) {
        if (inventory == null || items == null) return this;

        for (ItemStack item : items) {
            if (item != null) inventory.addItem(item);
        }

        return this;
    }

    // TODO: Fix this
    public HashMap<Integer, ItemStack> addWithLeftovers(ItemStack... items) {
        if (inventory == null || items == null) return null;

        return inventory.addItem(items);
    }

    /**
     * Count the number or quantities of stacks that
     * match an item in an inventory.
     *
     * @param item  The item (can be null)
     * @param stacks  Whether stacks should be counted
     *                   instead of item quantities
     * @return  The number of stacks or quantity of items
     *
     */

    public int count(ItemStack item, boolean stacks)
    {
        if (inventory == null) return 0;

        int qty = 0;

        for (ItemStack invStack : inventory)
        {
            // If ItemStacks are empty here, they are null
            if (invStack != null)
            {
                // If item is null, include all items in the
                // inventory

                if (item == null || invStack.isSimilar(item)) {

                    // If stacks is true, only count the number
                    // of stacks
                    //
                    // Otherwise, count the quantities of stacks

                    if (stacks) qty++;
                    else qty = qty + invStack.getAmount();
                }
            }
        }

        return qty;
    }

    /**
     * Keep only the items from a certain array
     * in this inventory, removing all others
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */

    public dInventory keep(ItemStack[] items) {

        if (inventory == null || items == null) return this;

        for (ItemStack invStack : inventory) {

            if (invStack != null) {

                boolean keep = false;

                // See if the item array contains
                // this inventory item
                for (ItemStack item : items) {

                    if (invStack.isSimilar(item)) {

                        keep = true;
                        break;
                    }
                }

                // If the item array did not contain
                // this inventory item, remove it
                // from the inventory
                if (!keep) {

                    this.remove(invStack);
                }
            }
        }

        return this;
    }

    /**
     * Exclude an array of items from this
     * inventory by removing them over and over
     * until they are completely gone
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */

    public dInventory exclude(ItemStack[] items) {

        if (inventory == null || items == null) return this;

        int oldCount = this.count(null, false);
        int newCount = -1;

        while (oldCount != newCount) {

            oldCount = newCount;
            newCount = this.remove(items).count(null, false);
        }

        return this;
    }

    /**
     * Fill an inventory with an array of items by
     * continuing to add the items to it over and
     * over until there is no more room
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */

    public dInventory fill(ItemStack[] items) {

        if (inventory == null || items == null) return this;

        int oldCount = this.count(null, false);
        int newCount = -1;

        while (oldCount != newCount) {

            oldCount = newCount;
            newCount = this.add(items).count(null, false);
        }

        return this;
    }

    /**
     * Remove an array of items from this inventory,
     * and return the result
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */

    public dInventory remove(ItemStack[] items) {

        if (inventory == null || items == null) return this;

        for (ItemStack item : items) {
            if (item != null) inventory.removeItem(item);
        }

        return this;
    }

    /**
     * Remove a book from this inventory, comparing
     * only its title and author with books in the
     * inventory, but ignoring its text, thus having
     * Denizen support for updatable quest journals
     * and their like
     *
     * @param   book  The itemStack of the book
     * @return  The resulting dInventory
     *
     */

    public dInventory removeBook(ItemStack book) {

        if (inventory == null || book == null) return this;

        // We have to manually keep track of the quantity
        // we are removing, because we are not relying on
        // Bukkit methods to find matching itemStacks
        int qty = book.getAmount();

        // Store the book's meta information in a variable
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        for (ItemStack invStack : inventory) {

            if (qty == 0) break;

            if (invStack != null && invStack.getItemMeta() instanceof BookMeta) {

                BookMeta invMeta = (BookMeta) invStack.getItemMeta();

                if (invMeta.getAuthor().equalsIgnoreCase(bookMeta.getAuthor())
                        && invMeta.getTitle().equalsIgnoreCase(bookMeta.getTitle())) {

                    // Make sure we don't remove more books than we
                    // need to
                    if (qty - invStack.getAmount() < 0) {

                        invStack.setAmount((qty - invStack.getAmount()) * -1);
                    }
                    else {

                        inventory.removeItem(invStack);

                        // Update the quantity we still have to remove
                        qty = qty - invStack.getAmount();
                    }
                }
            }
        }

        return this;
    }

    /**
     * Replace another inventory with this one,
     * cropping it if necessary so that it fits.
     *
     * @param destination  The destination inventory
     *
     */

    public void replace(dInventory destination) {

        if (inventory == null || destination == null) return;

        // If the destination is smaller than our current inventory,
        // add as many items as possible

        if (destination.getSize() < this.getSize()) {

            destination.clear();
            destination.add(this.getContents());
        }
        else {

            destination.setContents(this.getContents());
        }
    }

    public void clear() {
        if (inventory != null) inventory.clear();
    }



    ////////////////////////
    //  dObject Methods
    /////////////////////

    private String prefix = getObjectType();

    @Override
    public String getObjectType() {
        return "Inventory";
    }


    @Override
    public String getPrefix() {
        return prefix;
    }


    @Override
    public dInventory setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
    }


    @Override
    public String identify() {
        if (isUnique())
            return "in@" + NotableManager.getSavedId(this);
        else return "in@" + (idType.equals("script") || idType.equals("notable")
                ? idHolder : (idType + PropertyParser.getPropertiesString(this)));
    }


    @Override
    public String identifySimple() {
        if (isUnique())
            return "in@" + NotableManager.getSavedId(this);
        else return "in@" + (idType.equals("script") || idType.equals("notable")
                ? idHolder : (idType + "[" + idHolder + ']'));
    }


    @Override
    public String toString() {
        return identify();
    }



    ////////////////////////
    //  Attributes
    /////////////////////


    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // <--[tag]
        // @attribute <in@inventory.contains.display[(strict:)<element>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified display
        // name. Use 'strict:' in front of the search element to ensure the display
        // name is EXACTLY the search element, otherwise the searching will only
        // check if the search element is contained in the display name.
        // -->
        if (attribute.startsWith("contains.display")) {
            if (attribute.hasContext(2)) {
                int qty = 1;
                int attribs = 2;
                String search_string = attribute.getContext(2);
                boolean strict = false;
                if (search_string.startsWith("strict:")) {
                    strict = true;
                    search_string = search_string.replace("strict:", "");
                }

                // <--[tag]
                // @attribute <in@inventory.contains.display[(strict:)<element>].qty[<#>]>
                // @returns Element(Boolean)
                // @description
                // Returns whether the inventory contains a certain quantity of an item with the
                // specified display name. Use 'strict:' in front of the search element to ensure
                // the display name is EXACTLY the search element, otherwise the searching will only
                // check if the search element is contained in the display name.
                // -->
                if (attribute.getAttribute(3).startsWith("qty") &&
                        attribute.hasContext(3) &&
                        aH.matchesInteger(attribute.getContext(3))) {

                    qty = attribute.getIntContext(3);
                    attribs = 3;
                }

                int found_items = 0;

                if (strict) {
                    for (ItemStack item : getContents()) {
                        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                                item.getItemMeta().getDisplayName().equalsIgnoreCase(search_string))
                            found_items += item.getAmount();
                    }
                } else {
                    for (ItemStack item : getContents()) {
                        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                                item.getItemMeta().getDisplayName().toLowerCase()
                                        .contains(search_string.toLowerCase()))
                            found_items += item.getAmount();
                    }
                }

                return (found_items >= qty ? Element.TRUE.getAttribute(attribute.fulfill(attribs))
                        : Element.FALSE.getAttribute(attribute.fulfill(attribs)));
            }
        }

        // <--[tag]
        // @attribute <in@inventory.contains.material[<material>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified material.
        // -->
        if (attribute.startsWith("contains.material")) {
            if (attribute.hasContext(2)) {
                int qty = 1;
                int attribs = 2;
                dMaterial material = dMaterial.valueOf(attribute.getContext(2));

                // <--[tag]
                // @attribute <in@inventory.contains.material[<material>].qty[<#>]>
                // @returns Element(Boolean)
                // @description
                // Returns whether the inventory contains a certain quantity of an item with the
                // specified material.
                // -->
                if (attribute.getAttribute(3).startsWith("qty") &&
                        attribute.hasContext(3) &&
                        aH.matchesInteger(attribute.getContext(3))) {
                    qty = attribute.getIntContext(3);
                    attribs = 3;
                }

                int found_items = 0;

                for (ItemStack item : getContents()) {
                    if (item != null && item.getType() == material.getMaterial())
                        found_items += item.getAmount();
                }

                return (found_items >= qty ? Element.TRUE.getAttribute(attribute.fulfill(attribs))
                        : Element.FALSE.getAttribute(attribute.fulfill(attribs)));
            }
        }

        // <--[tag]
        // @attribute <in@inventory.contains[<item>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains an item.
        // -->
        if (attribute.startsWith("contains")) {
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1))) {
                int qty = 1;
                int attribs = 1;

                // <--[tag]
                // @attribute <in@inventory.contains[<item>].qty[<#>]>
                // @returns Element(Boolean)
                // @description
                // Returns whether the inventory contains a certain quantity of an item.
                // -->
                if (attribute.getAttribute(2).startsWith("qty") &&
                        attribute.hasContext(2) &&
                        aH.matchesInteger(attribute.getContext(2))) {

                    qty = attribute.getIntContext(2);
                    attribs = 2;
                }

                return new Element(getInventory().containsAtLeast
                        (dItem.valueOf(attribute.getContext(1)).getItemStack(), qty))
                        .getAttribute(attribute.fulfill(attribs));
            }
        }

        // <--[tag]
        // @attribute <in@inventory.id_type>
        // @returns Element
        // @description
        // Returns Denizen's type ID for this inventory. (player, location, etc.)
        // -->
        if (attribute.startsWith("id_type")) {
            return new Element(idType).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.location>
        // @returns dLocation
        // @description
        // Returns the location of this inventory's holder.
        // -->
        if (attribute.startsWith("location")) {
            if (getLocation() != null)
                return getLocation().getAttribute(attribute.fulfill(1));
            else return "null";
        }

        // <--[tag]
        // @attribute <in@inventory.qty[<item>]>
        // @returns Element(Number)
        // @description
        // Returns the combined quantity of itemstacks that match an item if
        // one if specified, or the combined quantity of all itemstacks
        // if one is not.
        // -->
        if (attribute.startsWith("qty"))
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1)))
                return new Element(count
                        (dItem.valueOf(attribute.getContext(1)).getItemStack(), false))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(count(null, false))
                        .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <in@inventory.stacks[<item>]>
        // @returns Element(Number)
        // @description
        // Returns the number of itemstacks that match an item if one is
        // specified, or the number of all itemstacks if one is not.
        // -->
        if (attribute.startsWith("stacks"))
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1)))
                return new Element(count
                        (dItem.valueOf(attribute.getContext(1)).getItemStack(), true))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(count(null, true))
                        .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <in@inventory.type>
        // @returns Element
        // @description
        // Returns the type of the inventory (e.g. "PLAYER", "CRAFTING", "HORSE").
        // -->
        if (attribute.startsWith("type"))
            return new Element(getInventory().getType().name())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <in@inventory.equipment>
        // @returns dInventory(Equipment)
        // @description
        // Returns the equipment of an inventory. If the inventory has no
        // equipment (Generally, if it's not alive), returns null.
        // -->
        if (attribute.startsWith("equipment")) {
            dInventory equipment = getEquipment();
            if (equipment == null)
                return Element.NULL.getAttribute(attribute.fulfill(1));
            else
                return getEquipment().getAttribute(attribute.fulfill(1));
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
        // @object dInventory
        // @name contents
        // @input dList(dItem)
        // @description
        // Sets the contents of the inventory.
        // @tags
        // <in@inventory.list_contents>
        // <in@inventory.list_contents.simple>
        // <in@inventory.list_contents.with_lore[<lore>]>
        // <in@inventory.list_contents.with_lore[<lore>].simple>
        // -->
        if (mechanism.matches("contents")) {
            setContents(value.asType(dList.class));
        }

        // <--[mechanism]
        // @object dInventory
        // @name size
        // @input Element(Number)
        // @description
        // Sets the size of the inventory. (Only works for "generic" chest inventories.)
        // @tags
        // <in@inventory.size>
        // -->
        if (mechanism.matches("size") && mechanism.requireInteger()) {
            setSize(value.asInt());
        }

        // <--[mechanism]
        // @object dInventory
        // @name title
        // @input Element
        // @description
        // Sets the title of the inventory. (Only works for "generic" chest inventories.)
        // @tags
        // <in@inventory.title>
        // -->
        if (mechanism.matches("title") && idType.equals("generic")) {
            setTitle(value.asString());
        }

        // <--[mechanism]
        // @object dInventory
        // @name holder
        // @input dObject
        // @description
        // Changes the holder of the dInventory, therefore completely reconfiguring
        // the inventory to that of the holder.
        // @tags
        // <in@inventory.id_holder>
        // -->
        if (mechanism.matches("holder")) {
            net.aufdemrand.denizen.objects.properties.inventory.InventoryHolder holder =
                    net.aufdemrand.denizen.objects.properties.inventory.InventoryHolder.getFrom(this);
            if (value.matchesType(dEntity.class))
                holder.setHolder(value.asType(dEntity.class));
            else if (value.matchesType(dLocation.class))
                holder.setHolder(value.asType(dLocation.class));
            else if (value.matchesEnum(InventoryType.values()) || value.isInt())
                holder.setHolder(value);
        }


        if (!mechanism.fulfilled())
            mechanism.reportInvalid();

    }

}
