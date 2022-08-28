package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ScribeCommand extends AbstractCommand {

    public ScribeCommand() {
        setName("scribe");
        setSyntax("(Deprecated)");
        setRequiredArguments(0, -1);
        isProcedural = false;
    }

    private enum BookAction {GIVE, DROP, EQUIP, NONE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry) {

            if (arg.matchesEnum(BookAction.class)
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

        BukkitImplDeprecations.scribeCommand.warn(scriptEntry);

        // Retrieve objects from ScriptEntry
        BookAction action = (BookAction) scriptEntry.getObject("action");
        ItemTag book = scriptEntry.getObjectTag("item");
        ScriptTag script = scriptEntry.getObjectTag("script");
        LocationTag location = scriptEntry.getObjectTag("location");

        BookScriptContainer bookScript = (BookScriptContainer) script.getContainer();

        book = bookScript.writeBookTo(book, scriptEntry.getContext());

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
        ItemStack currItem = player.getEquipment().getItemInMainHand();
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
            NMSHandler.itemHelper.setInventoryItem(inv, currItem, emptySpot);
            inv.setItem(emptySpot, currItem);
            player.setItemInHand(book);
            Debug.log("...added book to player hand, moved original item");
        }
    }

    private void dropBook(Location location, ItemStack book) {
        location.getWorld().dropItem(location, book);
    }
}
