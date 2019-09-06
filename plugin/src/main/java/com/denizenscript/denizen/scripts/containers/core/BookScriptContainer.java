package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class BookScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Book Script Containers
    // @group Script Container System
    // @description
    // Book script containers are similar to item script containers, except they are specifically
    // for the book items. They work with with the ItemTag object, and can be fetched
    // with the Object Fetcher by using the ItemTag constructor book_script_name
    // Example: - give <player> my_book
    //
    // <code>
    // Book_Script_Name:
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

    public ItemTag getBookFrom() {
        return getBookFrom(null, null);
    }

    public ItemTag getBookFrom(PlayerTag player, NPCTag npc) {
        ItemTag stack = new ItemTag(Material.WRITTEN_BOOK);
        return writeBookTo(stack, player, npc);
    }

    public ItemTag writeBookTo(ItemTag book, PlayerTag player, NPCTag npc) {
        // Get current ItemMeta from the book
        BookMeta bookInfo = (BookMeta) book.getItemStack().getItemMeta();

        if (contains("TITLE")) {
            String title = getString("TITLE");
            title = TagManager.tag(title, new BukkitTagContext(player, npc, new ScriptTag(this)));
            bookInfo.setTitle(title);
        }

        if (contains("SIGNED")) {
            if (getString("SIGNED").equalsIgnoreCase("false")) {
                book.getItemStack().setType(MaterialCompat.WRITABLE_BOOK);
            }
        }

        if (contains("AUTHOR")) {
            String author = getString("AUTHOR");
            author = TagManager.tag(author, new BukkitTagContext(player, npc, new ScriptTag(this)));
            bookInfo.setAuthor(author);
        }

        if (contains("TEXT")) {
            List<String> pages = getStringList("TEXT");

            for (String page : pages) {
                page = TagManager.tag(page, new BukkitTagContext(player, npc, new ScriptTag(this)));
                bookInfo.addPage(page);
            }
        }

        book.getItemStack().setItemMeta(bookInfo);
        return book;
    }
}
