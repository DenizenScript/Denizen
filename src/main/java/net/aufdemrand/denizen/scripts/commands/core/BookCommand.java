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
 * 
 * <br><b>dScript Usage:</b><br>
 * <pre>BOOK [SCRIPT:book_script] (GIVE|DROP|EQUIP) (ITEM:ITEMSTACK.name)</pre>
 * 
 * <ol><tt>Arguments: [] - Required</ol></tt>
 * 
 * <ol><tt>['message to announce']</tt><br> 
 *         The message to send to the server. This will be seen by all Players.</ol>
 * 
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - ANNOUNCE 'Today is Christmas!' <br>
 *  - ANNOUNCE "&#60;PLAYER.NAME> has completed '&#60;FLAG.P:currentQuest>'!" <br>
 *  - ANNOUNCE "&#60;GOLD>$$$ &#60;WHITE>- Make some quick cash at our &#60;RED>MINEA-SINO&#60;WHITE>!" 
 * </ol></tt>
 * 
 * 
 * @author Mason Adkins
 */

public class BookCommand extends AbstractCommand implements Listener{

	private enum BookAction { GIVE, DROP, EQUIP, NONE }

	@Override
	public void onEnable() {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	/* BOOK (GIVE|DROP|EQUIP) [SCRIPT:NAME] (ITEM:ITEMSTACK.name) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * (GIVE|DROP|EQUIP) specifies how the player receives the book. GIVE adds book to 
	 * 		the player inventory. DROP drops the book on the ground by the NPC. EQUIP 
	 * 		places the book in the player's hand. (default GIVE)
	 * [SCRIPT:NAME] defines the name of the Book script to use.
	 * (ITEM:ITEMSTACK.name) specifies an itemstack created with the NEW command. 
	 * 
	 * Example Usage:
	 * BOOK DROP SCRIPT:RuleBook
	 * BOOK EQUIP SCRIPT:QuestJournal
	 * BOOK SCRIPT:WelcomeGuide ITEM:ITEMSTACK.guide
	 * 
	 */

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Initialize required fields
		boolean newItem = false; // Used in determining if a 'NEW ItemStack' is being used
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
				dB.echoDebug("...SCRIPT to use '" + scriptName + "'.");

			} else if (aH.matchesLocation(arg)) {
				location = aH.getLocationFrom(arg);
				action = BookAction.DROP;
				dB.echoDebug("...set DROP location: '%s'", aH.getStringFrom(arg));
				
			} else if (aH.matchesItem(arg)) {
				book = aH.getItemFrom(arg);
				if (book.getType() == Material.BOOK || book.getType() == Material.WRITTEN_BOOK) {
					dB.echoDebug("...using existing book '%s'.", arg);
					newItem = true;
				} else {
					dB.echoError("This ItemStack is not a BOOK!");
					book = null;
				}
				
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

		if (action == BookAction.NONE && !newItem) action = BookAction.GIVE;
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
