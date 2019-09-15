package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class ItemBook implements Property {

    public static boolean describes(ObjectTag item) {
        Material material = ((ItemTag) item).getItemStack().getType();
        return (material == Material.WRITTEN_BOOK || material == MaterialCompat.WRITABLE_BOOK);
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
            "book"
    };

    public static final String[] handledMechs = new String[] {
            "book", "book_raw_pages", "book_pages", "book_author", "book_title"
    };


    private ItemBook(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("book")) {
            BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
            attribute = attribute.fulfill(1);

            if (item.getItemStack().getType() == Material.WRITTEN_BOOK) {

                // <--[tag]
                // @attribute <ItemTag.book.author>
                // @returns ElementTag
                // @mechanism ItemTag.book
                // @group properties
                // @description
                // Returns the author of the book.
                // -->
                if (attribute.startsWith("author")) {
                    return new ElementTag(bookInfo.getAuthor())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.book.title>
                // @returns ElementTag
                // @mechanism ItemTag.book
                // @group properties
                // @description
                // Returns the title of the book.
                // -->
                if (attribute.startsWith("title")) {
                    return new ElementTag(bookInfo.getTitle())
                            .getObjectAttribute(attribute.fulfill(1));
                }
            }

            // <--[tag]
            // @attribute <ItemTag.book.page_count>
            // @returns ElementTag(Number)
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns the number of pages in the book.
            // -->
            if (attribute.startsWith("page_count")) {
                return new ElementTag(bookInfo.getPageCount())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <ItemTag.book.page[<#>]>
            // @returns ElementTag
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns the page specified from the book as an element.
            // -->
            if ((attribute.startsWith("page") || attribute.startsWith("get_page")) && attribute.hasContext(1) && ArgumentHelper.matchesInteger(attribute.getContext(1))) {
                return new ElementTag(bookInfo.getPage(attribute.getIntContext(1)))
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <ItemTag.book.raw_page[<#>]>
            // @returns ElementTag
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns the page specified from the book as an element containing raw JSON.
            // -->
            if ((attribute.startsWith("raw_page") || attribute.startsWith("get_raw_page")) && attribute.hasContext(1) && ArgumentHelper.matchesInteger(attribute.getContext(1))) {
                return new ElementTag(ComponentSerializer.toString(bookInfo.spigot().getPage(attribute.getIntContext(1))))
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // Deprecated in favor of pages.escape_contents
            if (attribute.startsWith("pages.escaped")) {
                StringBuilder output = new StringBuilder();
                for (String page : bookInfo.getPages()) {
                    output.append(EscapeTagBase.escape(page)).append("|");
                }
                return new ListTag(output.length() > 0 ?
                        output.substring(0, output.length() - 1) : output.toString())
                        .getObjectAttribute(attribute.fulfill(2));
            }

            // <--[tag]
            // @attribute <ItemTag.book.pages>
            // @returns ListTag
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns the plain-text pages of the book as a ListTag.
            // -->
            if (attribute.startsWith("pages")) {
                return new ListTag(bookInfo.getPages())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <ItemTag.book.raw_pages>
            // @returns ListTag
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns the pages of the book as a ListTag of raw JSON.
            // -->
            if (attribute.startsWith("raw_pages")) {
                ListTag output = new ListTag();
                for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                    output.add(ComponentSerializer.toString(page));
                }
                return output.getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <ItemTag.book>
            // @returns ElementTag
            // @mechanism ItemTag.book
            // @group properties
            // @description
            // Returns full information on the book item, in the format
            // author|AUTHOR|title|TITLE|raw_pages|PAGE_ONE|PAGE_TWO|...
            // or as raw_pages|PAGE_ONE|PAGE_TWO|...
            // Pre-escaped to prevent issues.
            // See <@link language Property Escaping>
            // -->
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
        String output = getOutputString();
        if (output.equals("raw_pages")) {
            return null;
        }
        return output;
    }

    public String getOutputString() {
        StringBuilder output = new StringBuilder();
        BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
        if (item.getItemStack().getType().equals(Material.WRITTEN_BOOK)
                && bookInfo.hasAuthor() && bookInfo.hasTitle()) {
            output.append("author|").append(EscapeTagBase.escape(bookInfo.getAuthor()))
                    .append("|title|").append(EscapeTagBase.escape(bookInfo.getTitle())).append("|");
        }
        output.append("raw_pages|");
        if (bookInfo.hasPages()) {
            for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                output.append(EscapeTagBase.escape(ComponentSerializer.toString(page))).append("|");
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

        // <--[mechanism]
        // @object ItemTag
        // @name book_raw_pages
        // @input ListTag
        // @description
        // Changes the raw JSON pages of a book item.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.book.page_count>
        // <ItemTag.book.raw_page[<#>]>
        // <ItemTag.book.raw_pages>
        // -->

        if (mechanism.matches("book_raw_pages")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            ArrayList<BaseComponent[]> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(str)));
            }
            meta.spigot().setPages(newPages);
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_pages
        // @input ListTag
        // @description
        // Changes the plain-text pages of a book item.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.book.page_count>
        // <ItemTag.book.page[<#>]>
        // <ItemTag.book.pages>
        // -->

        if (mechanism.matches("book_pages")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            ArrayList<String> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(EscapeTagBase.unEscape(str));
            }
            meta.setPages(newPages);
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_author
        // @input Element
        // @description
        // Changes the author of a book item.
        // @tags
        // <ItemTag.book.author>
        // -->

        if (mechanism.matches("book_author")) {
            if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                Debug.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
                meta.setAuthor(mechanism.getValue().asString());
                item.getItemStack().setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book_title
        // @input Element
        // @description
        // Changes the title of a book item.
        // @tags
        // <ItemTag.book.title>
        // -->

        if (mechanism.matches("book_title")) {
            if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                Debug.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
                meta.setTitle(mechanism.getValue().asString());
                item.getItemStack().setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name book
        // @input Element
        // @description
        // Changes the information on a book item.
        // See <@link language Property Escaping>
        // @tags
        // <ItemTag.is_book>
        // <ItemTag.book.author>
        // <ItemTag.book.title>
        // <ItemTag.book.page_count>
        // <ItemTag.book.page[<#>]>
        // <ItemTag.book.pages>
        // <ItemTag.book.raw_page[<#>]>
        // <ItemTag.book.raw_pages>
        // <ItemTag.book>
        // -->

        if (mechanism.matches("book")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            ListTag data = mechanism.valueAsType(ListTag.class);
            if (data.size() < 2) {
                Debug.echoError("Invalid book input!");
            }
            else {
                if (data.size() > 4 && data.get(0).equalsIgnoreCase("author")
                        && data.get(2).equalsIgnoreCase("title")) {
                    if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                        Debug.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
                    }
                    else {
                        meta.setAuthor(EscapeTagBase.unEscape(data.get(1)));
                        meta.setTitle(EscapeTagBase.unEscape(data.get(3)));
                        for (int i = 0; i < 4; i++) {
                            data.remove(0); // No .removeRange?
                        }
                    }
                }
                if (data.get(0).equalsIgnoreCase("raw_pages")) {
                    ArrayList<BaseComponent[]> newPages = new ArrayList<>();
                    for (int i = 1; i < data.size(); i++) {
                        newPages.add(ComponentSerializer.parse(EscapeTagBase.unEscape(data.get(i))));
                    }
                    meta.spigot().setPages(newPages);
                }
                else if (data.get(0).equalsIgnoreCase("pages")) {
                    ArrayList<String> newPages = new ArrayList<>();
                    for (int i = 1; i < data.size(); i++) {
                        newPages.add(EscapeTagBase.unEscape(data.get(i)));
                    }
                    meta.setPages(newPages);
                }
                else {
                    Debug.echoError("Invalid book input!");
                }
                item.getItemStack().setItemMeta(meta);
            }
        }
    }
}
