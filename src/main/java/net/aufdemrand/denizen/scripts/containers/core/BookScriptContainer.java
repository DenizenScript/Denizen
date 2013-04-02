package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.dItem;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookScriptContainer extends ScriptContainer {
	
    public BookScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }

    public dItem getBookFrom(Player player, dNPC npc) {
        dItem stack = new dItem(Material.BOOK);
        return writeBookTo(stack, player, npc);
    }

    public dItem writeBookTo(dItem book, Player player, dNPC npc) {
        // Get current ItemMeta from the book
        BookMeta bookInfo = (BookMeta) book.getItemStack().getItemMeta();
        
        

        if (contains("TITLE")) {
        	String title = getString("TITLE");
        	title = DenizenAPI.getCurrentInstance().tagManager()
                    .tag(player, npc, title, false);
            bookInfo.setTitle(title);
        }
        
        if (contains("AUTHOR")) {
        	String author = getString("AUTHOR");
        	author = DenizenAPI.getCurrentInstance().tagManager()
                     .tag(player, npc, author, false);
            bookInfo.setAuthor(author);
        }

        if (contains("TEXT")) {
            List<String> pages = getStringList("TEXT");

            for (String page : pages) {
            	page = DenizenAPI.getCurrentInstance().tagManager()
                       .tag(player, npc, page, false);
                bookInfo.addPage(page);
            }
        }

        book.getItemStack().setItemMeta(bookInfo);
        return book;
    }

}