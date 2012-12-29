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
 * Creates written books from Book scripts..
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

	BookAction action;

	Player player;
	String scriptName;
	ItemStack book;
	Location npcLocation;

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {

		boolean newItem = false;
		action = BookAction.NONE;
		player = scriptEntry.getPlayer();
		scriptName = null;
		npcLocation = null;
		if (scriptEntry.getNPC() != null)
			npcLocation = scriptEntry.getNPC().getLocation();
		book = null;

		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesArg("DROP", arg) || aH.matchesArg("GIVE", arg) || aH.matchesArg("EQUIP", arg)) {
				action = BookAction.valueOf(arg.toUpperCase());

			} else if (aH.matchesScript(arg)) {
				scriptName = aH.getStringFrom(arg);
				dB.echoDebug("... script name set to '" + scriptName + "'.");

			} else if (aH.matchesItem(arg)) {
				book = aH.getItemFrom(arg);
				if (book.getType() == Material.BOOK
						|| book.getType() == Material.WRITTEN_BOOK) {
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
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		writeBook(book, scriptName);

		switch (action) {

		case DROP:
			dropBook();
			break;

		case GIVE:
			giveBook(player);
			break;

		case EQUIP:
			equipBook(player);
			break;

		case NONE:
			break;

		}

		player.updateInventory();
	}

	private ItemStack writeBook(ItemStack book, String scriptName) {

		BookMeta bookInfo = (BookMeta) book.getItemMeta();

		String author = null;
		String title = null;
		List<String> pages = null;

		if (scriptName == null) return null;

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
			return book;

		} else return null;
	}

	private void giveBook(Player player) {
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

	public void equipBook (Player player) {
		ItemStack currItem = player.getItemInHand();
		Inventory inv = player.getInventory();
		int emptySpot = inv.firstEmpty();

		// if they aren't holding anything 
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

	public void dropBook() {
		player.getWorld().dropItem(npcLocation, book);
		dB.echoDebug("... dropped book by NPC");
	}

	@EventHandler
	public void paragraph(ReplaceableTagEvent e) {
		if (!e.matches("P")) return;
		e.setReplaceable("\n \u00A7r \n");
	}
}
