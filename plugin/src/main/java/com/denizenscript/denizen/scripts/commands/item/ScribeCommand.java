package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ScribeCommand extends AbstractCommand {

    // <--[command]
    // @Name Scribe
    // @Syntax scribe [<script>] (<item>/give/equip/{drop <location>})
    // @Required 1
    // @Short Writes information to a book from a book-type script or a book item.
    // @Group item
    //
    // @Description
    // Create a book item from a book-type script or book item.
    // This can then be directly given to a player, or dropped at a specific location
    // Read more about book-scripts here: <@link language book script containers>
    //
    // @Tags
    // <ItemTag.book_author>
    // <ItemTag.book_title>
    // <ItemTag.book_pages>
    //
    // @Usage
    // Gives the book "Cosmos Book" to the player
    // - scribe "Cosmos_Book" give
    //
    // @Usage
    // Drops the "Cosmos Book" at the players location
    // - scribe "Cosmos_Book" drop <player.location>
    //
    // @Usage
    // Puts the "Cosmos Book" in the players hand
    // - scribe "Cosmos_Book" equip
    // -->

    private enum BookAction {GIVE, DROP, EQUIP, NONE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (arg.matchesEnum(BookAction.values())
                    && !scriptEntry.hasObject("action")) {
                scriptEntry.addObject("action", BookAction.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(ScriptTag.class)) {
                scriptEntry.addObject("script", arg.asType(ScriptTag.class));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
                scriptEntry.addObject("action", BookAction.DROP);
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentType(ItemTag.class)) {
                scriptEntry.addObject("item", arg.asType(ItemTag.class));

            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("action", BookAction.GIVE);
        scriptEntry.defaultObject("item", new ItemTag(Material.WRITTEN_BOOK));

        // Must contain a book script
        if (!scriptEntry.hasObject("script")) {
            throw new InvalidArgumentsException("Missing SCRIPT argument!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Retrieve objects from ScriptEntry
        BookAction action = (BookAction) scriptEntry.getObject("action");
        ItemTag book = (ItemTag) scriptEntry.getObject("item");
        ScriptTag script = (ScriptTag) scriptEntry.getObject("script");
        LocationTag location = (LocationTag) scriptEntry.getObject("location");

        BookScriptContainer bookScript = (BookScriptContainer) script.getContainer();

        book = bookScript.writeBookTo(book, (BukkitTagContext) scriptEntry.entryData.getTagContext());

        // Post-write action? Can be NONE.
        switch (action) {
            case DROP:
                dropBook(location, book.getItemStack());
                break;

            case GIVE:
                giveBook(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity(), book.getItemStack());
                // Update player's inventory
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().updateInventory();
                break;

            case EQUIP:
                equipBook(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity(), book.getItemStack());
                // Update player's inventory
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().updateInventory();
                break;

            case NONE:
                break;
        }

    }

    private void giveBook(Player player, ItemStack book) {
        Inventory inv = player.getInventory();
        int emptySpot = inv.firstEmpty();
        if (emptySpot != -1) {
            player.getInventory().addItem(book.clone());
        }
        else {
            player.getWorld().dropItem(player.getLocation(), book);
            Debug.log("Player's inventory is full, dropped book.");
        }
    }

    private void equipBook(Player player, ItemStack book) {
        ItemStack currItem = player.getItemInHand();
        Inventory inv = player.getInventory();
        int emptySpot = inv.firstEmpty();

        // if player isn't holding anything
        if (currItem == null || currItem.getType() == Material.AIR) {
            player.setItemInHand(book);
            return;
        }
        // drop it if inventory has no empty slots
        Debug.log("emptySpot: " + emptySpot);

        if (emptySpot == -1) {
            player.getWorld().dropItem(player.getLocation(), book);
            Debug.log("Player's inventory is full, dropped book.");
        }
        // move current held item to empty spot, set item in hand to the book
        else {
            NMSHandler.getItemHelper().setInventoryItem(inv, currItem, emptySpot);
            inv.setItem(emptySpot, currItem);
            player.setItemInHand(book);
            Debug.log("...added book to player hand, moved original item");
        }
    }

    private void dropBook(Location location, ItemStack book) {
        location.getWorld().dropItem(location, book);
    }
}
