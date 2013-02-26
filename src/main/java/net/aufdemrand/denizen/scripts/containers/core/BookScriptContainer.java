package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookScriptContainer extends ScriptContainer {

    public BookScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public Item getBookFrom() {
        Item stack = new Item(Material.BOOK);
        return writeBookTo(stack);
    }

    public Item writeBookTo(Item book) {
        // Get current ItemMeta from the book
        BookMeta bookInfo = (BookMeta) book.getItemMeta();
        
        

        if (contains("TITLE")) {
        	String title = getString("TITLE");
        	title = DenizenAPI.getCurrentInstance().tagManager()
                    .tag(null, null, title, false);
            bookInfo.setTitle(title);
        }
        
        if (contains("AUTHOR")) {
        	String author = getString("AUTHOR");
        	author = DenizenAPI.getCurrentInstance().tagManager()
                     .tag(null, null, author, false);
            bookInfo.setAuthor(author);
        }

        if (contains("TEXT")) {
            List<String> pages = getStringList("TEXT");

            for (String page : pages) {
            	page = DenizenAPI.getCurrentInstance().tagManager()
                       .tag(null, null, page, false);
                bookInfo.addPage(page);
            }
        }

        book.setItemMeta(bookInfo);
        return book;
    }

}