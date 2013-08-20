package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.objects.dItem;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookScriptContainer extends ScriptContainer {
    
    public BookScriptContainer(ConfigurationSection configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
    }
    
    public dItem getBookFrom() {
        return getBookFrom(null, null);
    }

    public dItem getBookFrom(dPlayer player, dNPC npc) {
        dItem stack = new dItem(Material.WRITTEN_BOOK);
        return writeBookTo(stack, player, npc);
    }

    public dItem writeBookTo(dItem book, dPlayer player, dNPC npc) {
        // Get current ItemMeta from the book
        BookMeta bookInfo = (BookMeta) book.getItemStack().getItemMeta();
        
        if (contains("TITLE")) {
            String title = getString("TITLE");
            title = TagManager.tag(player, npc, title, false);
            bookInfo.setTitle(title);
        }
        
        if (contains("AUTHOR")) {
            String author = getString("AUTHOR");
            author = TagManager.tag(player, npc, author, false);
            bookInfo.setAuthor(author);
        }

        if (contains("TEXT")) {
            List<String> pages = getStringList("TEXT");

            for (String page : pages) {
                page = TagManager.tag(player, npc, page, false);
                bookInfo.addPage(page);
            }
        }

        book.getItemStack().setItemMeta(bookInfo);
        return book;
    }

}