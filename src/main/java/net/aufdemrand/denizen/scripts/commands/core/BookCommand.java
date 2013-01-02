package net.aufdemrand.denizen.scripts.commands.core;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * <p>Creates a Written Book from a dScript 'Book-type Script' and a Book ItemStack.</p>
 * 
 * <b>dScript Usage:</b><br>
 * <pre>BOOK [SCRIPT:book_script] (GIVE|{DROP}|EQUIP) (LOCATION:x,y,z,world) (ITEM:ITEMSTACK.name)</pre>
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
 * <p>Note: BookCommand also implements a replaceable tag for &#60;P>, which creates a new
 * paragraph in a written book's text.</p>
 *          
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - BOOK DROP SCRIPT:Cosmos<br>
 *  - BOOK EQUIP 'SCRIPT:Spellbook of Haste'<br>
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
 *  - ^ANIMATE TARGET:NPC ANIMATION:ARM_SWING <br>
 *  - ^NEW ITEMSTACK ITEM:book ID:&#60;PLAYER.NAME>s_enchanted_spellbook<br>
 *  - ^BOOK SCRIPT:silk_touch_description ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook<br>
 *  - ^ENCHANT ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook ENCHANTMENT:SILKTOUCH<br>
 *  - ^LORE ADD ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook 'A spell of Silk-touch, level 1'<br>
 *  - DROP ITEM:ITEMSTACK.&#60;PLAYER.NAME>s_enchanted_spellbook<br>
 *  - NARRATE '&#60;NPC.NAME> drops an old book.' <br>
 * </ol></tt>
 * 
 * @author Mason Adkins
 */

public class BookCommand extends AbstractCommand implements Listener{

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
		
		ItemStack book = null;
		Player player = scriptEntry.getPlayer();
		String scriptName = null;
		Location location = null;
		
		if (scriptEntry.getNPC() != null)
			location = scriptEntry.getNPC().getLocation();
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesArg("DROP, GIVE, EQUIP", arg)) {
				action = BookAction.valueOf(arg.toUpperCase());

			} else if (aH.matchesScript(arg)) {
				scriptName = aH.getStringFrom(arg);
				dB.echoDebug("...set SCRIPT to use '%s'", scriptName);

			} else if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
				action = BookAction.DROP;
				dB.echoDebug("...set DROP location: '%s'", aH.getStringFrom(arg));
				
			} else if (aH.matchesItem(arg)) {
				book = aH.getItemFrom(arg);
				if (book.getType() == Material.BOOK || book.getType() == Material.WRITTEN_BOOK) {
					dB.echoDebug("...using existing book '%s'.", arg);
					savedItem = true;
				} else {
					dB.echoError("This ItemStack is not a BOOK!");
					book = null;
				}
				
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

		if (action == BookAction.NONE && !savedItem) action = BookAction.GIVE;
		if (scriptName == null) throw new InvalidArgumentsException("Missing SCRIPT argument!");
		if (book == null) book = new ItemStack(387);
		
		// Save objects to ScriptEntry for usage in execute
		scriptEntry.addObject("action", action);
		scriptEntry.addObject("book", book);
		scriptEntry.addObject("script", scriptName);
		scriptEntry.addObject("player", player);
		scriptEntry.addObject("location", location);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		// Retrieve objects from ScriptEntry
		BookAction action = (BookAction) scriptEntry.getObject("action");
		ItemStack book = (ItemStack) scriptEntry.getObject("book");
		String scriptName = (String) scriptEntry.getObject("script");
		Player player = (Player) scriptEntry.getObject("player");
		Location location = (Location) scriptEntry.getObject("location");
		
		// Write to the book item using the script specified
		if (!writeBook(book, scriptName))
			throw new CommandExecutionException("Invalid BOOK script!");

		// Post-write action? Can be NONE.
		switch (action) {
		case DROP:
			dropBook(location, book);
			break;

		case GIVE:
			giveBook(player, book);
			break;

		case EQUIP:
			equipBook(player, book);
			break;

		case NONE:
			break;
		}

		// Update player's inventory
		player.updateInventory();
	}

	private boolean writeBook(ItemStack book, String scriptName) {
		// Get current ItemMeta from the book
		BookMeta bookInfo = (BookMeta) book.getItemMeta();

		String author = null;
		String title = null;
		List<String> pages = null;

		if (scriptName == null) return false;

		//if script TYPE:BOOK
		if (denizen.getScripts().contains(scriptName.toUpperCase() + ".TYPE") && 
				denizen.getScripts().getString(scriptName.toUpperCase() + ".TYPE").equalsIgnoreCase("BOOK")) {

			if (denizen.getScripts().getString(scriptName.toUpperCase() + ".TITLE") != null){
				title = denizen.getScripts().getString(scriptName.toUpperCase() + ".TITLE");
				bookInfo.setTitle(title);
				dB.echoDebug("...book title set to '" + title + "'.");
			} else dB.echoDebug("...no title specified.");

			if (denizen.getScripts().getString(scriptName.toUpperCase() + ".AUTHOR") != null){
				author = denizen.getScripts().getString(scriptName.toUpperCase() + ".AUTHOR");
				bookInfo.setAuthor(author);
				dB.echoDebug("...book author set to '" + author + "'.");
			} else dB.echoDebug("...no author specified.");

			if (denizen.getScripts().getString(scriptName.toUpperCase() + ".TEXT") != null){
				pages = denizen.getScripts().getStringList(scriptName.toUpperCase() + ".TEXT");
				for (String thePage : pages){
					bookInfo.addPage(thePage);
					dB.echoDebug("...book page added.");
				}
			} else dB.echoDebug("...no text specified.");

			book.setItemMeta(bookInfo);
			return true;

		} else return false;
	}

	private void giveBook(Player player, ItemStack book) {
		Inventory inv = player.getInventory();
		int emptySpot = inv.firstEmpty();
		if (emptySpot != -1) {
			player.getInventory().addItem(book);
			dB.echoDebug("... added book to player inventory");
		} else {
			player.getWorld().dropItem(player.getLocation(), book);
			dB.echoDebug("... player inventtory full, dropped book");
		}
	}

	private void equipBook (Player player, ItemStack book) {
		ItemStack currItem = player.getItemInHand();
		Inventory inv = player.getInventory();
		int emptySpot = inv.firstEmpty();

		// if player isn't holding anything 
		if (currItem == null || currItem.getType() == Material.AIR) {
			player.setItemInHand(book);
			dB.echoDebug("... added book to player hand");
			return;
		}
		// drop it if inventory has no empty slots
		emptySpot = inv.firstEmpty();
		dB.echoDebug("emptySpot: " + emptySpot);

		if (emptySpot == -1) {
			player.getWorld().dropItem(player.getLocation(), book);
			dB.echoDebug("... dropped book, player inventory full");
		}
		// move current held item to empty spot, set item in hand to the book
		else {
			inv.setItem(emptySpot, currItem);
			player.setItemInHand(book);
			dB.echoDebug("... added book to player hand, moved original item");
		}
	}

	private void dropBook(Location location, ItemStack book) {
		location.getWorld().dropItem(location, book);
		dB.echoDebug("... dropped book by NPC");
	}

	/**
	 * Catches a replaceable tag event for '<P>' and replaces it with
	 * a book-friendly 'newline'.
	 * 
	 * @param e ReplaceableTagEvent
	 */
	@EventHandler
	public void paragraph(ReplaceableTagEvent e) {
		if (!e.matches("P")) return;
		e.setReplaceable("\n \u00A7r \n");
	}
}
