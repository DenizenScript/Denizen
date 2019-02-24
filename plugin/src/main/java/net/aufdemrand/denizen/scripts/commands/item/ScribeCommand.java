package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ScribeCommand extends AbstractCommand {


    private enum BookAction {GIVE, DROP, EQUIP, NONE}

    @Override
    public void onEnable() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                paragraph(event);
            }
        }, "p", "n");
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesEnum(BookAction.values())
                    && !scriptEntry.hasObject("action")) {
                scriptEntry.addObject("action", BookAction.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
                scriptEntry.addObject("action", BookAction.DROP);
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentType(dItem.class)) {
                scriptEntry.addObject("item", arg.asType(dItem.class));

            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("action", BookAction.GIVE);
        scriptEntry.defaultObject("item", new dItem(Material.WRITTEN_BOOK));

        // Must contain a book script
        if (!scriptEntry.hasObject("script")) {
            throw new InvalidArgumentsException("Missing SCRIPT argument!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Retrieve objects from ScriptEntry
        BookAction action = (BookAction) scriptEntry.getObject("action");
        dItem book = (dItem) scriptEntry.getObject("item");
        dScript script = (dScript) scriptEntry.getObject("script");
        dLocation location = (dLocation) scriptEntry.getObject("location");

        BookScriptContainer bookScript = (BookScriptContainer) script.getContainer();

        book = bookScript.writeBookTo(book, ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer(), ((BukkitScriptEntryData) scriptEntry.entryData).getNPC());

        // Post-write action? Can be NONE.
        switch (action) {
            case DROP:
                dropBook(location, book.getItemStack());
                break;

            case GIVE:
                giveBook(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity(), book.getItemStack());
                // Update player's inventory
                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().updateInventory();
                break;

            case EQUIP:
                equipBook(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity(), book.getItemStack());
                // Update player's inventory
                ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().updateInventory();
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
            dB.log("Player's inventory is full, dropped book.");
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
        emptySpot = inv.firstEmpty();
        dB.log("emptySpot: " + emptySpot);

        if (emptySpot == -1) {
            player.getWorld().dropItem(player.getLocation(), book);
            dB.log("Player's inventory is full, dropped book.");
        }
        // move current held item to empty spot, set item in hand to the book
        else {
            inv.setItem(emptySpot, currItem);
            player.setItemInHand(book);
            dB.log("...added book to player hand, moved original item");
        }
    }

    private void dropBook(Location location, ItemStack book) {
        location.getWorld().dropItem(location, book);
    }

    /**
     * Catches a replaceable tag event for '<P>' and replaces it with
     * a book-friendly 'newline'.
     *
     * @param e ReplaceableTagEvent
     */
    public void paragraph(ReplaceableTagEvent e) {
        // <--[tag]
        // @attribute <p>
        // @returns Element
        // @description
        // Returns a paragraph, for use in books.
        // -->
        if (e.matches("p")) {
            e.setReplaced("\n \u00A7r \n");
            // <--[tag]
            // @attribute <n>
            // @returns Element
            // @description
            // Returns a newline symbol, for use in books.
            // -->
        }
        else if (e.matches("n")) {
            e.setReplaced("\n");
        }
    }
}
