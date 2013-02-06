package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.arguments.Item;
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

        if (contains("TITLE"))
            bookInfo.setTitle(getString("TITLE"));

        if (contains("AUTHOR"))
            bookInfo.setAuthor(getString("AUTHOR"));

        if (contains("TEXT")) {
            List<String> pages = getStringList("TEXT");
            for (String page : pages)
                bookInfo.addPage(page);
        }

        book.setItemMeta(bookInfo);
        return book;
    }

}