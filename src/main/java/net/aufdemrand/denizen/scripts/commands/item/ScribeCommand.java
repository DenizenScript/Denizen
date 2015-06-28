package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ScribeCommand extends AbstractCommand implements Listener {

    // <--[example]
    // @Title Book scripts and the scribe command example
    // @Description
    // Use the following example to learn the basics on book scripts, tags,
    // and the scribe command.

    // @Code
    // # +-----------------------------------
    // # | Book Meta Tester
    // # |
    // # | Use /gettestbook, then /testbook to check if you are holding the book.
    //
    // Book Meta Tester:
    //   type: world
    //
    //   events:
    //     on gettestbook command:
    //     # Use the scribe command to create a book, and drop it on the ground.
    //     - scribe drop 'script:Example book script' location:<player.location>
    //     - narrate 'Hey look, a book on the ground! Pick it up and use /testbook.'
    //     # Let bukkit know we handled the 'gettestbook' command.
    //     - determine fulfilled
    //
    //     on testbook command:
    //     - define item player.item_in_hand
    //     - if '<pr:example book script checker>' {
    //         - narrate 'This is the book!'
    //         - narrate "This is the title<&co> <%item%.book.title>"
    //         - narrate "It has <%item%.book.page_count> pages."
    //         - narrate "It was written by <%item%.book.author>!"
    //       } else narrate 'The item in your hand is not the test book.'
    //
    //     - determine fulfilled
    //
    //
    // Example book script:
    //   type: book
    //
    //   title: This is an example title.
    //   author: aufdemrand
    //   text:
    //   - page 1 is pretty short.
    //   - The next page is kind of short, but longer than page 1.
    //   - The final page is <N>split<N>Across<N>Several lines.
    //
    //
    // # We'll use this in the if statement to test if the book has the title
    // # we are looking for.
    // # Want to use something like this in interact script requirements?
    // # Just add an entry like so: - valueof '<pr:Example book script checker>'
    //
    // Example book script checker:
    //   type: procedure
    //
    //   script:
    //   - if <player.item_in_hand.book.title> == 'This is an example title.'
    //     determine true
    //     else determine false

    // -->


    private enum BookAction {GIVE, DROP, EQUIP, NONE}

    @Override
    public void onEnable() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
        TagManager.registerTagEvents(this);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesEnum(BookAction.values())
                    && !scriptEntry.hasObject("action"))
                scriptEntry.addObject("action", BookAction.valueOf(arg.getValue().toUpperCase()));

            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
                scriptEntry.addObject("action", BookAction.DROP);
            }

            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentType(dItem.class)) {
                scriptEntry.addObject("item", arg.asType(dItem.class));

            }
            else arg.reportUnhandled();
        }

        scriptEntry.defaultObject("action", BookAction.GIVE);
        scriptEntry.defaultObject("item", new dItem(Material.WRITTEN_BOOK));

        // Must contain a book script
        if (!scriptEntry.hasObject("script"))
            throw new InvalidArgumentsException("Missing SCRIPT argument!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

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
        if (emptySpot != -1)
            player.getInventory().addItem(book);
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
    @TagManager.TagEvents
    public void paragraph(ReplaceableTagEvent e) {
        // <--[tag]
        // @attribute <P>
        // @returns Element
        // @description
        // Returns a paragraph, for use in books.
        // -->
        if (e.matches("P")) {
            e.setReplaced("\n \u00A7r \n");
            // <--[tag]
            // @attribute <N>
            // @returns Element
            // @description
            // Returns a newline symbol, for use in books.
            // -->
        }
        else if (e.matches("N")) {
            e.setReplaced("\n");
        }
    }
}
