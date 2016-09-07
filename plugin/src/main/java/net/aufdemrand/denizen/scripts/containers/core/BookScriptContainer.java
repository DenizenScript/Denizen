package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Book Script Containers
    // @group Script Container System
    // @description
    // Book script containers are similar to item script containers, except they are specifically
    // for the book items. They work with with the dItem object, and can be fetched
    // with the Object Fetcher by using the dItem constructor i@book_script_name
    // Example: - give <player> i@my_book
    //
    // <code>
    // Book Script Name:
    //
    //   type: book
    //
    //   # The 'custom name' can be anything you wish.
    //   title: custom name
    //
    //   # The 'custom name' can be anything you wish.
    //   author: custom name
    //
    //   # Defaults to true. Set to false to spawn a 'book and quill' instead of a 'written book'.
    //   signed: true/false
    //
    //   # Each -line in the text section represents an entire page.
    //   # To create a newline, use the tag <n>. To create a paragraph, use <p>.
    //   text:
    //   - page
    //   - ...
    // </code>
    //
    // -->
    public BookScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
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
            title = TagManager.tag(title, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)));
            bookInfo.setTitle(title);
        }

        if (contains("SIGNED")) {
            if (getString("SIGNED").equalsIgnoreCase("false")) {
                book.getItemStack().setType(Material.BOOK_AND_QUILL);
            }
        }

        if (contains("AUTHOR")) {
            String author = getString("AUTHOR");
            author = TagManager.tag(author, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)));
            bookInfo.setAuthor(author);
        }

        if (contains("TEXT")) {
            List<String> pages = getStringList("TEXT");

            for (String page : pages) {
                page = TagManager.tag(page, new BukkitTagContext(player, npc, false, null, shouldDebug(), new dScript(this)));
                bookInfo.addPage(page);
            }
        }

        book.getItemStack().setItemMeta(bookInfo);
        return book;
    }
}
