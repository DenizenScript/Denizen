package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBook implements Property {

    public static boolean describes(ObjectTag item) {
        Material material = ((ItemTag) item).getBukkitMaterial();
        return (material == Material.WRITTEN_BOOK || material == Material.WRITABLE_BOOK);
    }

    public static ItemBook getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemBook((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "book", "book_author", "book_title", "book_pages", "book_map"
    };

    public static final String[] handledMechs = new String[] {
            "book", "book_raw_pages", "book_pages", "book_author", "book_title"
    };

    private ItemBook(ItemTag _item) {
        item = _item;
    }

    public BookMeta getBookInfo() {
        return (BookMeta) item.getItemMeta();
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.book_author>
        // @returns ElementTag
        // @mechanism ItemTag.book_author
        // @group properties
        // @description
        // Returns the author of the book.
        // -->
        if (attribute.startsWith("book_author") && item.getBukkitMaterial() == Material.WRITTEN_BOOK) {
            return new ElementTag(getBookInfo().getAuthor(), true)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.book_title>
        // @returns ElementTag
        // @mechanism ItemTag.book_title
        // @group properties
        // @description
        // Returns the title of the book.
        // -->
        if (attribute.startsWith("book_title") && item.getBukkitMaterial() == Material.WRITTEN_BOOK) {
            return new ElementTag(getBookInfo().getTitle(), true)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.book_pages>
        // @returns ListTag
        // @mechanism ItemTag.book_pages
        // @group properties
        // @description
        // Returns the plain-text pages of the book as a ListTag.
        // -->
        if (attribute.startsWith("book_pages")) {
            ListTag output = new ListTag();
            for (BaseComponent[] page : getBookInfo().spigot().getPages()) {
                output.addObject(new ElementTag(FormattedTextHelper.stringify(page, ChatColor.BLACK), true));
            }
            return output.getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.book_map>
        // @returns MapTag
        // @mechanism ItemTag.book
        // @group properties
        // @description
        // Returns a MapTag of data about the book, with keys "pages" (a ListTag), and when available, "author" and "title".
        // -->
        if (attribute.startsWith("book_map")) {
            return getBookMap().getObjectAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("book")) {
            BukkitImplDeprecations.itemBookTags.warn(attribute.context);
            BookMeta bookInfo = (BookMeta) item.getItemMeta();
            attribute = attribute.fulfill(1);

            if (item.getBukkitMaterial() == Material.WRITTEN_BOOK) {
                if (attribute.startsWith("author")) {
                    return new ElementTag(bookInfo.getAuthor())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                if (attribute.startsWith("title")) {
                    return new ElementTag(bookInfo.getTitle())
                            .getObjectAttribute(attribute.fulfill(1));
                }
            }
            if (attribute.startsWith("page_count")) {
                return new ElementTag(bookInfo.getPageCount())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            if ((attribute.startsWith("page") || attribute.startsWith("get_page")) && attribute.hasParam()) {
                return new ElementTag(FormattedTextHelper.stringify(bookInfo.spigot().getPage(attribute.getIntParam()), ChatColor.BLACK))
                        .getObjectAttribute(attribute.fulfill(1));
            }
            if ((attribute.startsWith("raw_page") || attribute.startsWith("get_raw_page")) && attribute.hasParam()) {
                BukkitImplDeprecations.bookItemRawTags.warn(attribute.context);
                return new ElementTag(ComponentSerializer.toString(bookInfo.spigot().getPage(attribute.getIntParam())))
                        .getObjectAttribute(attribute.fulfill(1));
            }
            if (attribute.startsWith("pages")) {
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                    output.add(FormattedTextHelper.stringify(page, ChatColor.BLACK));
                }
                return output.getObjectAttribute(attribute.fulfill(1));
            }
            if (attribute.startsWith("raw_pages")) {
                BukkitImplDeprecations.bookItemRawTags.warn(attribute.context);
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                    output.add(ComponentSerializer.toString(page));
                }
                return output.getObjectAttribute(attribute.fulfill(1));
            }
            String output = getOutputString();
            if (output == null) {
                output = "null";
            }
            return new ElementTag(output)
                    .getObjectAttribute(attribute);
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        MapTag map = getBookMap();
        return map.toString();
    }

    public MapTag getBookMap() {
        MapTag outMap = new MapTag();
        BookMeta bookInfo = (BookMeta) item.getItemMeta();
        if (item.getBukkitMaterial().equals(Material.WRITTEN_BOOK) && bookInfo.hasAuthor() && bookInfo.hasTitle()) {
            outMap.putObject("author", new ElementTag(bookInfo.getAuthor(), true));
            outMap.putObject("title", new ElementTag(bookInfo.getTitle(), true));
        }
        if (bookInfo.hasPages()) {
            List<BaseComponent[]> pages = bookInfo.spigot().getPages();
            ListTag pageList = new ListTag(pages.size());
            for (BaseComponent[] page : pages) {
                pageList.addObject(new ElementTag(FormattedTextHelper.stringify(page, ChatColor.BLACK), true));
            }
            outMap.putObject("pages", pageList);
        }
        return outMap;
    }

    @Deprecated
    public String getOutputString() {
        StringBuilder output = new StringBuilder(128);
        BookMeta bookInfo = (BookMeta) item.getItemMeta();
        if (item.getBukkitMaterial().equals(Material.WRITTEN_BOOK) && bookInfo.hasAuthor() && bookInfo.hasTitle()) {
            output.append("author|").append(EscapeTagBase.escape(bookInfo.getAuthor()))
                    .append("|title|").append(EscapeTagBase.escape(bookInfo.getTitle())).append("|");
        }
        output.append("pages|");
        if (bookInfo.hasPages()) {
            for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                output.append(EscapeTagBase.escape(FormattedTextHelper.stringify(page, ChatColor.BLACK))).append("|");
            }
        }
        return output.substring(0, output.length() - 1);
    }

    @Override
    public String getPropertyId() {
        return "book";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("book_raw_pages")) {
            BukkitImplDeprecations.bookItemRawTags.warn(mechanism.context);
            BookMeta meta = (BookMeta) item.getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            ArrayList<BaseComponent[]> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(str)));
            }
            meta.spigot().setPages(newPages);
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_pages
        // @input ListTag
        // @description
        // Changes the plain-text pages of a book item.
        // @tags
        // <ItemTag.book_pages>
        // -->
        if (mechanism.matches("book_pages")) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            ArrayList<BaseComponent[]> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(FormattedTextHelper.parse(str, ChatColor.BLACK));
            }
            meta.spigot().setPages(newPages);
            item.setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_author
        // @input ElementTag
        // @description
        // Changes the author of a book item.
        // @tags
        // <ItemTag.book_author>
        // -->
        if (mechanism.matches("book_author")) {
            if (!item.getBukkitMaterial().equals(Material.WRITTEN_BOOK)) {
                mechanism.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemMeta();
                meta.setAuthor(mechanism.getValue().asString());
                item.setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_title
        // @input ElementTag
        // @description
        // Changes the title of a book item.
        // @tags
        // <ItemTag.book_title>
        // -->
        if (mechanism.matches("book_title")) {
            if (!item.getBukkitMaterial().equals(Material.WRITTEN_BOOK)) {
                mechanism.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemMeta();
                meta.setTitle(mechanism.getValue().asString());
                item.setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book
        // @input MapTag
        // @description
        // Changes the information on a book item.
        // Should have keys "pages" (a ListTag), and optionally "title" and "author".
        // @tags
        // <ItemTag.is_book>
        // <ItemTag.book_title>
        // <ItemTag.book_author>
        // <ItemTag.book_pages>
        // -->
        if (mechanism.matches("book")) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            if (mechanism.getValue().asString().startsWith("map@")) {
                MapTag mapData = mechanism.valueAsType(MapTag.class);
                if (mapData == null) {
                    mechanism.echoError("Book input is an invalid map?");
                    return;
                }
                ElementTag author = mapData.getElement("author");
                ElementTag title = mapData.getElement("title");
                if (author != null && title != null) {
                    if (!item.getBukkitMaterial().equals(Material.WRITTEN_BOOK)) {
                        mechanism.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
                    }
                    else {
                        meta.setAuthor(author.toString());
                        meta.setTitle(title.toString());
                    }
                }
                ListTag pages = mapData.getObjectAs("pages", ListTag.class, mechanism.context);
                if (pages != null) {
                    ArrayList<BaseComponent[]> newPages = new ArrayList<>(pages.size());
                    for (int i = 0; i < pages.size(); i++) {
                        newPages.add(FormattedTextHelper.parse(pages.get(i), ChatColor.BLACK));
                    }
                    meta.spigot().setPages(newPages);
                }
                item.setItemMeta(meta);
                return;
            }
            ListTag data = mechanism.valueAsType(ListTag.class);
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
                if (!item.getBukkitMaterial().equals(Material.WRITTEN_BOOK)) {
                    mechanism.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
                }
                else {
                    meta.setAuthor(EscapeTagBase.unEscape(data.get(1)));
                    meta.setTitle(EscapeTagBase.unEscape(data.get(3)));
                    for (int i = 0; i < 4; i++) {
                        data.removeObject(0); // No .removeRange?
                    }
                }
            }
            if (data.get(0).equalsIgnoreCase("raw_pages")) {
                ArrayList<BaseComponent[]> newPages = new ArrayList<>(data.size());
                for (int i = 1; i < data.size(); i++) {
                    newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(data.get(i))));
                }
                meta.spigot().setPages(newPages);
            }
            else if (data.get(0).equalsIgnoreCase("pages")) {
                ArrayList<BaseComponent[]> newPages = new ArrayList<>(data.size());
                for (int i = 1; i < data.size(); i++) {
                    newPages.add(FormattedTextHelper.parse(EscapeTagBase.unEscape(data.get(i)), ChatColor.BLACK));
                }
                meta.spigot().setPages(newPages);
            }
            else {
                mechanism.echoError("Invalid book input!");
            }
            item.setItemMeta(meta);
        }
    }
}
