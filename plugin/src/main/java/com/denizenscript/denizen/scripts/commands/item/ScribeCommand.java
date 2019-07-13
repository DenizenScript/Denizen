package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dScript;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ScribeCommand extends AbstractCommand {

    /*
     * <b>dScript Usage:</b><br>
     * <pre>Scribe [SCRIPT:book_script] (GIVE|{DROP}|EQUIP) (LOCATION:x,y,z,world) (ITEM:ITEMSTACK.name)</pre>
     *
     * <ol><tt>Arguments: [] - Required () - Optional  {} - Default</ol></tt>
     *
     * <ol><tt>[SCRIPT:book_script]</tt><br>
     *         The name of the 'Book Script'. See below for format.</ol>
     *
     * <ol><tt>[GIVE|{DROP}|EQUIP]</tt><br>
     *         What to do with the book after it is written. If not specified, it will default
     *         to dropping the book near the NPC. Note: When using BOOK with an 'ITEMSTACK.name',
     *         no default action is set allowing other commands to modify the book.</ol>
     *
     * <ol><tt>(LOCATION:x,y,z,world)</tt><br>
     *         When using DROP, a location may be specified. Default location, if unspecified,
     *         is the attached NPC.</ol>
     *
     * <ol><tt>(ITEM:ITEMSTACK.name)</tt><br>
     *         Allows the use of a specific BOOK created with a 'saved ITEMSTACK' from the NEW
     *         command. If not specified, a new book will be used.</ol>
     *
     *
     * <br><b>Sample Book Script:</b><br>
     * <ol><pre>
     * "Cosmos Book":<br>
     *   Type: Book<br>
     *   Title: Cosmos, a Personal Voyage<br>
     *   Author: Carl Sagan<br>
     *   Text:<br>
     *   - Every one of us is, in the cosmic perspective, precious. If a human disagrees with<br>
     *     you, let him live. In a hundred billion galaxies, you will not find another<br>
     *   - The nitrogen in our DNA, the calcium in our teeth, the iron in our blood, the <br>
     *     carbon in our apple pies were made in the interiors of collapsing stars. We are <br>
     *     made of starstuff.<br>
     * </pre></ol>
     *
     * <p>Note: ScribeCommand also implements a replaceable tag for &#60;P>, which creates a new
     * paragraph in a written book's text.</p>
     *
     * <br><b>Example Usage:</b><br>
     * <ol><tt>
     *  - SCRIBE SCRIPT:Cosmos DROP<br>
     *  - SCRIBE ITEM:ITEMSTACK.ImportantBook 'SCRIPT:Spellbook of Haste'<br>
     * </ol></tt>
     *
     * <br><b>Extended Usage:</b><br>
     * <ol><tt>
     *  Script: <br>
     *  - ENGAGE NOW DURATION:10 <br>
     *  - LOOKCLOSE TOGGLE:TRUE DURATION:10 <br>
     *  - CHAT 'Use this book with care, as it is very powerful and could cause great harm<br>
     *    if put into the wrong hands!' <br>
     *  - WAIT 2 <br>
     *  - ^ANIMATE ANIMATION:ARM_SWING <br>
     *  - ^NEW ITEMSTACK ITEM:book ID:&#60;PLAYER.NAME>s_enchanted_spellbook<br>
     *  - ^SCRIBE ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook SCRIPT:silk_touch_description <br>
     *  - ^ENCHANT ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook ENCHANTMENT:SILKTOUCH<br>
     *  - ^LORE ADD ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook 'A spell of Silk-touch, level 1'<br>
     *  - DROP ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook<br>
     *  - NARRATE '&#60;NPC.NAME> drops an old book.' <br>
     * </ol></tt>
     *
     */
    // TODO: Combine the above outdated information with the new meta tags below

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
    // <i@item.book.author>
    // <i@item.book.title>
    // <i@item.book.page_count>
    // <i@item.book.page[<#>]>
    // <i@item.book.pages>
    //
    //
    // @Usage
    // Gives the book "Cosmos Book" to the player
    // - scribe "Cosmos Book" give
    //
    // @Usage
    // Drops the "Cosmos Book" at the players location
    // - scribe "Cosmos Book" drop <player.location>
    //
    // @Usage
    // Puts the "Cosmos Book" in the players hand
    // - scribe "Cosmos Book" equip
    // -->

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

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

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

        book = bookScript.writeBookTo(book, Utilities.getEntryPlayer(scriptEntry), Utilities.getEntryNPC(scriptEntry));

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
