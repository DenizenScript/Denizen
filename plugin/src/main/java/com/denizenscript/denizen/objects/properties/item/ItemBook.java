package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBook implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getBukkitMaterial() == Material.WRITTEN_BOOK
                || ((ItemTag) item).getBukkitMaterial() == Material.WRITABLE_BOOK);
    }

    public static ItemBook getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemBook((ItemTag) _item);
        }
    }

    private ItemBook(ItemTag _item) {
        item = _item;
    }

    ItemTag item;


    @Override
    public String getPropertyString() {
        return getBookMap().identify();
    }

    @Override
    public String getPropertyId() {
        return "book";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ItemTag.book_author>
        // @returns ElementTag
        // @mechanism ItemTag.book_author
        // @group properties
        // @description
        // Returns the author of the book.
        // -->
        PropertyParser.registerTag(ItemBook.class, ElementTag.class, "book_author", (attribute, object) -> {
            BookMeta bookMeta = object.getBookMeta();
            return bookMeta.hasAuthor() ? new ElementTag(bookMeta.getAuthor(), true) : null;
        });

        // <--[tag]
        // @attribute <ItemTag.book_title>
        // @returns ElementTag
        // @mechanism ItemTag.book_title
        // @group properties
        // @description
        // Returns the title of the book.
        // -->
        PropertyParser.registerTag(ItemBook.class, ElementTag.class, "book_title", (attribute, object) -> {
            BookMeta bookMeta = object.getBookMeta();
            return bookMeta.hasTitle() ? new ElementTag(bookMeta.getTitle()) : null;
        });

        // <--[tag]
        // @attribute <ItemTag.book_pages>
        // @returns ListTag
        // @mechanism ItemTag.book_pages
        // @group properties
        // @description
        // Returns the plain-text pages of the book as a ListTag.
        // -->
        PropertyParser.registerTag(ItemBook.class, ListTag.class, "book_pages", (attribute, object) -> {
            List<BaseComponent[]> pages = object.getBookMeta().spigot().getPages();
            ListTag pageList = new ListTag(pages.size());
            for (BaseComponent[] page : pages) {
                pageList.addObject(new ElementTag(FormattedTextHelper.stringify(page), true));
            }
            return pageList;
        });

        // <--[tag]
        // @attribute <ItemTag.book_map>
        // @returns MapTag
        // @mechanism ItemTag.book
        // @group properties
        // @description
        // Returns a MapTag of data about the book, with keys "pages" (a ListTag), and when available, "author" and "title".
        // -->
        PropertyParser.registerTag(ItemBook.class, MapTag.class, "book_map", (attribute, object) -> {
            return object.getBookMap();
        });


        PropertyParser.registerTag(ItemBook.class, ObjectTag.class, "book", (attribute, object) -> {
            BukkitImplDeprecations.itemBookTags.warn(attribute.context);
            BookMeta bookMeta = object.getBookMeta();
            if (object.isWrittenBook()) {
                if (attribute.startsWith("author", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(bookMeta.getAuthor());
                }
                if (attribute.startsWith("title", 2)) {
                    attribute.fulfill(1);
                    return new ElementTag(bookMeta.getTitle());
                }
            }
            if (attribute.startsWith("page_count", 2)) {
                attribute.fulfill(1);
                return new ElementTag(bookMeta.getPageCount());
            }
            if ((attribute.startsWith("page", 2) || attribute.startsWith("get_page", 2)) && attribute.hasContext(2)) {
                attribute.fulfill(1);
                return new ElementTag(FormattedTextHelper.stringify(bookMeta.spigot().getPage(attribute.getIntParam())));
            }
            if ((attribute.startsWith("raw_page", 2) || attribute.startsWith("get_raw_page", 2)) && attribute.hasContext(2)) {
                BukkitImplDeprecations.bookItemRawTags.warn(attribute.context);
                attribute.fulfill(1);
                return new ElementTag(ComponentSerializer.toString(bookMeta.spigot().getPage(attribute.getIntParam())));
            }
            if (attribute.startsWith("pages", 2)) {
                attribute.fulfill(1);
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookMeta.spigot().getPages()) {
                    output.add(FormattedTextHelper.stringify(page));
                }
                return output;
            }
            if (attribute.startsWith("raw_pages", 2)) {
                BukkitImplDeprecations.bookItemRawTags.warn(attribute.context);
                attribute.fulfill(1);
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookMeta.spigot().getPages()) {
                    output.add(ComponentSerializer.toString(page));
                }
                return output;
            }
            String output = object.getOutputString();
            if (output == null) {
                output = "null";
            }
            return new ElementTag(output);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name book_pages
        // @input ListTag
        // @description
        // Changes the plain-text pages of a book item.
        // @tags
        // <ItemTag.book_pages>
        // -->
        PropertyParser.registerMechanism(ItemBook.class, ListTag.class, "book_pages", (object, mechanism, input) -> {
            BookMeta bookMeta = object.getBookMeta();
            List<BaseComponent[]> newPages = new ArrayList<>(input.size());
            for (String page : input) {
                newPages.add(FormattedTextHelper.parse(page, ChatColor.BLACK));
            }
            bookMeta.spigot().setPages(newPages);
            object.item.setItemMeta(bookMeta);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name book_author
        // @input ElementTag
        // @description
        // Changes the author of a book item.
        // @tags
        // <ItemTag.book_author>
        // -->
        PropertyParser.registerMechanism(ItemBook.class, ElementTag.class, "book_author", (object, mechanism, input) -> {
            if (!object.isWrittenBook()) {
                mechanism.echoError("Only 'written_book' items can have an author!");
                return;
            }
            BookMeta bookMeta = object.getBookMeta();
            bookMeta.setAuthor(input.asString());
            object.item.setItemMeta(bookMeta);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name book_title
        // @input ElementTag
        // @description
        // Changes the title of a book item.
        // @tags
        // <ItemTag.book_title>
        // -->
        PropertyParser.registerMechanism(ItemBook.class, ElementTag.class, "book_title", (object, mechanism, input) -> {
            if (!object.isWrittenBook()) {
                mechanism.echoError("Only 'written_book' items can have a title!");
                return;
            }
            BookMeta bookMeta = object.getBookMeta();
            bookMeta.setTitle(input.asString());
            object.item.setItemMeta(bookMeta);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name book
        // @input MapTag
        // @description
        // Changes the information on a book item.
        // Can have keys "pages" (a ListTag), "title", and "author", all optional.
        // @tags
        // <ItemTag.is_book>
        // <ItemTag.book_title>
        // <ItemTag.book_author>
        // <ItemTag.book_pages>
        // -->
        PropertyParser.registerMechanism(ItemBook.class, ObjectTag.class, "book", (object, mechanism, input) -> {
            BookMeta bookMeta = object.getBookMeta();
            if (input.canBeType(MapTag.class)) {
                MapTag bookMap = input.asType(MapTag.class, mechanism.context);
                if (bookMap == null) {
                    mechanism.echoError("Invalid book map specified: " + input);
                    return;
                }
                ElementTag author = bookMap.getElement("author");
                ElementTag title = bookMap.getElement("title");
                if (author != null) {
                    if (!object.isWrittenBook()) {
                        mechanism.echoError("Only 'written_book' items can have an author!");
                        return;
                    }
                    bookMeta.setAuthor(author.asString());
                }
                if (title != null) {
                    if (!object.isWrittenBook()) {
                        mechanism.echoError("Only 'written_book' items can have a title!");
                        return;
                    }
                    bookMeta.setTitle(title.asString());
                }
                ListTag pages = bookMap.getObjectAs("pages", ListTag.class, mechanism.context);
                if (pages != null) {
                    List<BaseComponent[]> newPages = new ArrayList<>(pages.size());
                    for (String page : pages) {
                        newPages.add(FormattedTextHelper.parse(page, ChatColor.BLACK));
                    }
                    bookMeta.spigot().setPages(newPages);
                }
                object.item.setItemMeta(bookMeta);
                return;
            }
            ListTag data = input.asType(ListTag.class, mechanism.context);
            if (data.size() < 1) {
                mechanism.echoError("Invalid book input!");
                return;
            }
            if (data.size() < 2) {
                // Nothing to do, but not necessarily invalid.
                return;
            }
            if (data.size() > 4 && data.get(0).equalsIgnoreCase("author")
                    && data.get(2).equalsIgnoreCase("title")) {
                if (!object.isWrittenBook()) {
                    mechanism.echoError("Only 'written_book' items can have a title or author!");
                }
                else {
                    bookMeta.setAuthor(EscapeTagBase.unEscape(data.get(1)));
                    bookMeta.setTitle(EscapeTagBase.unEscape(data.get(3)));
                    for (int i = 0; i < 4; i++) {
                        data.removeObject(0); // No .removeRange?
                    }
                }
            }
            if (data.get(0).equalsIgnoreCase("raw_pages")) {
                List<BaseComponent[]> newPages = new ArrayList<>(data.size());
                for (int i = 1; i < data.size(); i++) {
                    newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(data.get(i))));
                }
                bookMeta.spigot().setPages(newPages);
            }
            else if (data.get(0).equalsIgnoreCase("pages")) {
                List<BaseComponent[]> newPages = new ArrayList<>(data.size());
                for (int i = 1; i < data.size(); i++) {
                    newPages.add(FormattedTextHelper.parse(EscapeTagBase.unEscape(data.get(i)), ChatColor.BLACK));
                }
                bookMeta.spigot().setPages(newPages);
            }
            else {
                mechanism.echoError("Invalid book input!");
            }
            object.item.setItemMeta(bookMeta);
        });

        PropertyParser.registerMechanism(ItemBook.class, ListTag.class, "book_raw_pages", (object, mechanism, input) -> {
            BukkitImplDeprecations.bookItemRawTags.warn(mechanism.context);
            BookMeta bookMeta = object.getBookMeta();
            List<BaseComponent[]> newPages = new ArrayList<>();
            for (String page : input) {
                newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(page)));
            }
            bookMeta.spigot().setPages(newPages);
            object.item.setItemMeta(bookMeta);
        });
    }

    public MapTag getBookMap() {
        MapTag bookMap = new MapTag();
        BookMeta bookMeta = getBookMeta();
        if (bookMeta.hasAuthor()) {
            bookMap.putObject("author", new ElementTag(bookMeta.getAuthor(), true));
        }
        if (bookMeta.hasTitle()) {
            bookMap.putObject("title", new ElementTag(bookMeta.getTitle(), true));
        }
        if (bookMeta.hasPages()) {
            List<BaseComponent[]> pages = bookMeta.spigot().getPages();
            ListTag pageList = new ListTag(pages.size());
            for (BaseComponent[] page : pages) {
                pageList.addObject(new ElementTag(FormattedTextHelper.stringify(page), true));
            }
            bookMap.putObject("pages", pageList);
        }
        return bookMap;
    }

    public boolean isWrittenBook() {
        return item.getBukkitMaterial() == Material.WRITTEN_BOOK;
    }

    public BookMeta getBookMeta() {
        return (BookMeta) item.getItemMeta();
    }

    @Deprecated
    public String getOutputString() {
        StringBuilder output = new StringBuilder(128);
        BookMeta bookMeta = getBookMeta();
        if (isWrittenBook() && bookMeta.hasAuthor() && bookMeta.hasTitle()) {
            output.append("author|").append(EscapeTagBase.escape(bookMeta.getAuthor()))
                    .append("|title|").append(EscapeTagBase.escape(bookMeta.getTitle())).append("|");
        }
        output.append("pages|");
        if (bookMeta.hasPages()) {
            for (BaseComponent[] page : bookMeta.spigot().getPages()) {
                output.append(EscapeTagBase.escape(FormattedTextHelper.stringify(page))).append("|");
            }
        }
        return output.substring(0, output.length() - 1);
    }
}
