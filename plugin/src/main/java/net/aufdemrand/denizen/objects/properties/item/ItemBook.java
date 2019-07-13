package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTags;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

public class ItemBook implements Property {

    public static boolean describes(dObject item) {
        Material material = ((dItem) item).getItemStack().getType();
        return (material == Material.WRITTEN_BOOK || material == MaterialCompat.WRITABLE_BOOK);
    }

    public static ItemBook getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemBook((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "book"
    };

    public static final String[] handledMechs = new String[] {
            "book", "book_raw_pages", "book_pages", "book_author", "book_title"
    };


    private ItemBook(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("book")) {
            BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
            attribute = attribute.fulfill(1);

            if (item.getItemStack().getType() == Material.WRITTEN_BOOK) {

                // <--[tag]
                // @attribute <i@item.book.author>
                // @returns Element
                // @mechanism dItem.book
                // @group properties
                // @description
                // Returns the author of the book.
                // -->
                if (attribute.startsWith("author")) {
                    return new Element(bookInfo.getAuthor())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.book.title>
                // @returns Element
                // @mechanism dItem.book
                // @group properties
                // @description
                // Returns the title of the book.
                // -->
                if (attribute.startsWith("title")) {
                    return new Element(bookInfo.getTitle())
                            .getAttribute(attribute.fulfill(1));
                }
            }

            // <--[tag]
            // @attribute <i@item.book.page_count>
            // @returns Element(Number)
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns the number of pages in the book.
            // -->
            if (attribute.startsWith("page_count")) {
                return new Element(bookInfo.getPageCount())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <i@item.book.page[<#>]>
            // @returns Element
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns the page specified from the book as an element.
            // -->
            if ((attribute.startsWith("page") || attribute.startsWith("get_page")) && attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1))) {
                return new Element(bookInfo.getPage(attribute.getIntContext(1)))
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <i@item.book.raw_page[<#>]>
            // @returns Element
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns the page specified from the book as an element containing raw JSON.
            // -->
            if ((attribute.startsWith("raw_page") || attribute.startsWith("get_raw_page")) && attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1))) {
                return new Element(ComponentSerializer.toString(bookInfo.spigot().getPage(attribute.getIntContext(1))))
                        .getAttribute(attribute.fulfill(1));
            }

            // Deprecated in favor of pages.escape_contents
            if (attribute.startsWith("pages.escaped")) {
                StringBuilder output = new StringBuilder();
                for (String page : bookInfo.getPages()) {
                    output.append(EscapeTags.escape(page)).append("|");
                }
                return new dList(output.length() > 0 ?
                        output.substring(0, output.length() - 1) : output.toString())
                        .getAttribute(attribute.fulfill(2));
            }

            // <--[tag]
            // @attribute <i@item.book.pages>
            // @returns dList
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns the plain-text pages of the book as a dList.
            // -->
            if (attribute.startsWith("pages")) {
                return new dList(bookInfo.getPages())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <i@item.book.raw_pages>
            // @returns dList
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns the pages of the book as a dList of raw JSON.
            // -->
            if (attribute.startsWith("raw_pages")) {
                dList output = new dList();
                for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                    output.add(ComponentSerializer.toString(page));
                }
                return output.getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <i@item.book>
            // @returns Element
            // @mechanism dItem.book
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
            return new Element(output)
                    .getAttribute(attribute);
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
            output.append("author|").append(EscapeTags.escape(bookInfo.getAuthor()))
                    .append("|title|").append(EscapeTags.escape(bookInfo.getTitle())).append("|");
        }
        output.append("raw_pages|");
        if (bookInfo.hasPages()) {
            for (BaseComponent[] page : bookInfo.spigot().getPages()) {
                output.append(EscapeTags.escape(ComponentSerializer.toString(page))).append("|");
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
        // @object dItem
        // @name book_raw_pages
        // @input dList
        // @description
        // Changes the raw JSON pages of a book item.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.book.page_count>
        // <i@item.book.raw_page[<#>]>
        // <i@item.book.raw_pages>
        // -->

        if (mechanism.matches("book_raw_pages")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            dList data = mechanism.valueAsType(dList.class);
            ArrayList<BaseComponent[]> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(ComponentSerializer.parse(EscapeTags.unEscape(str)));
            }
            meta.spigot().setPages(newPages);
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object dItem
        // @name book_pages
        // @input dList
        // @description
        // Changes the plain-text pages of a book item.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.book.page_count>
        // <i@item.book.page[<#>]>
        // <i@item.book.pages>
        // -->

        if (mechanism.matches("book_pages")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            dList data = mechanism.valueAsType(dList.class);
            ArrayList<String> newPages = new ArrayList<>();
            for (String str : data) {
                newPages.add(EscapeTags.unEscape(str));
            }
            meta.setPages(newPages);
            item.getItemStack().setItemMeta(meta);
        }

        // <--[mechanism]
        // @object dItem
        // @name book_author
        // @input Element
        // @description
        // Changes the author of a book item.
        // @tags
        // <i@item.book.author>
        // -->

        if (mechanism.matches("book_author")) {
            if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                dB.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
                meta.setAuthor(mechanism.getValue().asString());
                item.getItemStack().setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object dItem
        // @name book_title
        // @input Element
        // @description
        // Changes the title of a book item.
        // @tags
        // <i@item.book.title>
        // -->

        if (mechanism.matches("book_title")) {
            if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                dB.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
            }
            else {
                BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
                meta.setTitle(mechanism.getValue().asString());
                item.getItemStack().setItemMeta(meta);
            }
        }

        // <--[mechanism]
        // @object dItem
        // @name book
        // @input Element
        // @description
        // Changes the information on a book item.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.is_book>
        // <i@item.book.author>
        // <i@item.book.title>
        // <i@item.book.page_count>
        // <i@item.book.page[<#>]>
        // <i@item.book.pages>
        // <i@item.book.raw_page[<#>]>
        // <i@item.book.raw_pages>
        // <i@item.book>
        // -->

        if (mechanism.matches("book")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            dList data = mechanism.valueAsType(dList.class);
            if (data.size() < 2) {
                dB.echoError("Invalid book input!");
            }
            else {
                if (data.size() > 4 && data.get(0).equalsIgnoreCase("author")
                        && data.get(2).equalsIgnoreCase("title")) {
                    if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                        dB.echoError("Only WRITTEN_BOOK (not WRITABLE_BOOK) can have a title or author!");
                    }
                    else {
                        meta.setAuthor(EscapeTags.unEscape(data.get(1)));
                        meta.setTitle(EscapeTags.unEscape(data.get(3)));
                        for (int i = 0; i < 4; i++) {
                            data.remove(0); // No .removeRange?
                        }
                    }
                }
                if (data.get(0).equalsIgnoreCase("raw_pages")) {
                    ArrayList<BaseComponent[]> newPages = new ArrayList<>();
                    for (int i = 1; i < data.size(); i++) {
                        newPages.add(ComponentSerializer.parse(EscapeTags.unEscape(data.get(i))));
                    }
                    meta.spigot().setPages(newPages);
                }
                else if (data.get(0).equalsIgnoreCase("pages")) {
                    ArrayList<String> newPages = new ArrayList<>();
                    for (int i = 1; i < data.size(); i++) {
                        newPages.add(EscapeTags.unEscape(data.get(i)));
                    }
                    meta.setPages(newPages);
                }
                else {
                    dB.echoError("Invalid book input!");
                }
                item.getItemStack().setItemMeta(meta);
            }
        }
    }
}
