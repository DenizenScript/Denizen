package net.aufdemrand.denizen.scripts.commands.core;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class BookCommand extends AbstractCommand {
	
	private enum BookType { GIVE, DROP, EQUIP }
	
	@Override
	public void onEnable() {
		//nothing to do here
	}
	
	//BOOK GIVE|DROP|EQUIP PLAYER: NAME:book_script
	
	BookType TYPE;
	
	Player player;
	String scriptName;
	ItemStack book;

	String author = null;
	String title = null;
	List<String> pages = null;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
		TYPE = BookType.GIVE;
		player = scriptEntry.getPlayer();
		scriptName = null;
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesArg("DROP", arg)) {
				TYPE = BookType.valueOf(arg.toUpperCase());
			} else if (aH.matchesScript(arg)) {
				scriptName = aH.getStringFrom(arg);
				dB.echoDebug("... script name set to " + scriptName);
			} else dB.echoDebug("... arg doesnt work D: " + arg);
		}
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		
		book = createBook(scriptName);
		if (book != null) {
			player.getInventory().setItemInHand(book);
		} else dB.echoDebug("...fail to create book. Does your book script exist?");
		
	}
	
	private ItemStack createBook(String scriptName) {
		
		ItemStack book = new ItemStack(387);
		BookMeta bookInfo = (BookMeta) book.getItemMeta();
		
		if (scriptName == null) return null;
		
		//if script TYPE:BOOK
		if (denizen.getScripts().contains(scriptName.toUpperCase() + ".TYPE") && 
				denizen.getScripts().getString(scriptName.toUpperCase() + ".TYPE").equalsIgnoreCase("BOOK")) {
			
			if (denizen.getScripts().getString(scriptName.toUpperCase() + ".TITLE") != null){
				title = denizen.getScripts().getString(scriptName.toUpperCase() + ".TITLE");
				bookInfo.setTitle(title);
				dB.echoDebug("...book title set to " + title);
			} else dB.echoDebug("...no title specified");
			
			if (denizen.getScripts().getString(scriptName.toUpperCase() + ".AUTHOR") != null){
				author = denizen.getScripts().getString(scriptName.toUpperCase() + ".AUTHOR");
				bookInfo.setAuthor(author);
				dB.echoDebug("...book author set to " + author);
			} else dB.echoDebug("...no author specified");
			
			if (denizen.getScripts().getString(scriptName.toUpperCase() + ".TEXT") != null){
				pages = denizen.getScripts().getStringList(scriptName.toUpperCase() + ".TEXT");
				for (String thePage : pages){
					bookInfo.addPage(thePage);
					dB.echoDebug("...book page added");
				}
			} else dB.echoDebug("...no pages specified");
			
			return book;
			
		} else return null;
		
	}

}
