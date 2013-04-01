package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * <p>Scribes information to a Book from a dScript 'Book-type Script' or a Book ItemStack.</p>
 *
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
 * @author Mason Adkins
 */

public class ScribeCommand extends AbstractCommand implements Listener{

    private enum BookAction { GIVE, DROP, EQUIP, NONE }

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initialize required fields
        boolean savedItem = false; // Used in determining if a 'saved ItemStack' is being used
        BookAction action = BookAction.NONE;

        Item book = null;
        Player player = scriptEntry.getPlayer();
        String scriptName = null;
        Location location = null;
        dNPC npc = scriptEntry.getNPC();

        if (npc != null)
            location = npc.getLocation();

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("DROP, GIVE, EQUIP", arg))
                action = BookAction.valueOf(arg.toUpperCase());

            else if (aH.matchesScript(arg))
                scriptName = aH.getStringFrom(arg);

            else if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                action = BookAction.DROP;
            }

            else if (aH.matchesItem(arg)) {
                book = aH.getItemFrom(arg);
                if (book.getItemStack().getType() == Material.BOOK || book.getItemStack().getType() == Material.WRITTEN_BOOK) {
                    savedItem = true;
                } else {
                    book = null;
                }

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (action == BookAction.NONE && !savedItem) action = BookAction.GIVE;
        if (scriptName == null) throw new InvalidArgumentsException("Missing SCRIPT argument!");
        if (book == null) book = new Item(387);

        // Save objects to ScriptEntry for usage in execute
        scriptEntry.addObject("action", action);
        scriptEntry.addObject("book", book);
        scriptEntry.addObject("script", scriptName);
        scriptEntry.addObject("player", player);
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("npc", npc);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Retrieve objects from ScriptEntry
        BookAction action = (BookAction) scriptEntry.getObject("action");
        Item book = (Item) scriptEntry.getObject("book");
        String scriptName = (String) scriptEntry.getObject("script");
        Player player = (Player) scriptEntry.getObject("player");
        Location location = (Location) scriptEntry.getObject("location");
        dNPC npc = (dNPC) scriptEntry.getObject("npc");

        BookScriptContainer bookScript = ScriptRegistry.getScriptContainerAs(scriptName, BookScriptContainer.class);
        
        book = bookScript.writeBookTo(book, player, npc);

        // Post-write action? Can be NONE.
        switch (action) {
            case DROP:
                dropBook(location, book.getItemStack());
                break;

            case GIVE:
                giveBook(player, book.getItemStack());
                break;

            case EQUIP:
                equipBook(player, book.getItemStack());
                break;

            case NONE:
                break;
        }

        // Update player's inventory
        player.updateInventory();
    }

    private void giveBook(Player player, ItemStack book) {
        Inventory inv = player.getInventory();
        int emptySpot = inv.firstEmpty();
        if (emptySpot != -1)
            player.getInventory().addItem(book);
        else {
            player.getWorld().dropItem(player.getLocation(), book);
            dB.echoDebug("Player's inventory is full, dropped book.");
        }
    }

    private void equipBook (Player player, ItemStack book) {
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
        dB.echoDebug("emptySpot: " + emptySpot);

        if (emptySpot == -1) {
            player.getWorld().dropItem(player.getLocation(), book);
            dB.echoDebug("Player's inventory is full, dropped book.");
        }
        // move current held item to empty spot, set item in hand to the book
        else {
            inv.setItem(emptySpot, currItem);
            player.setItemInHand(book);
            dB.echoDebug("...added book to player hand, moved original item");
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
    @EventHandler
    public void paragraph(ReplaceableTagEvent e) {
        if (e.matches("P")) {
        	e.setReplaced("\n \u00A7r \n");
        	return;
        } else if (e.matches("N")) {
        	e.setReplaced("\n");
        }
    }
}
