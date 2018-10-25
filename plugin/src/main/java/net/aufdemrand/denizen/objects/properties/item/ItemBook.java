package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
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
            // @attribute <i@item.book.get_page[<#>]>
            // @returns Element
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns the page specified from the book as an element.
            // -->
            if (attribute.startsWith("get_page") && aH.matchesInteger(attribute.getContext(1))) {
                return new Element(bookInfo.getPage(attribute.getIntContext(1)))
                        .getAttribute(attribute.fulfill(1));
            }

            // Deprecated in favor of pages.escape_contents
            if (attribute.startsWith("pages.escaped")) {
                StringBuilder output = new StringBuilder();
                for (String page : bookInfo.getPages()) {
                    output.append(EscapeTags.Escape(page)).append("|");
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
            // Returns the pages of the book as a dList.
            // -->
            if (attribute.startsWith("pages")) {
                return new dList(bookInfo.getPages())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <i@item.book>
            // @returns Element
            // @mechanism dItem.book
            // @group properties
            // @description
            // Returns full information on the book item, in the format
            // author|AUTHOR|title|TITLE|pages|PAGE_ONE|PAGE_TWO|...
            // or as pages|PAGE_ONE|PAGE_TWO|...
            // Pre-escaped to prevent issues.
            // See <@link language Property Escaping>
            // -->
            String output = getPropertyString();
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
        StringBuilder output = new StringBuilder();
        BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
        if (item.getItemStack().getType().equals(Material.WRITTEN_BOOK)
                && bookInfo.hasAuthor() && bookInfo.hasTitle()) {
            output.append("author|").append(EscapeTags.Escape(bookInfo.getAuthor()))
                    .append("|title|").append(EscapeTags.Escape(bookInfo.getTitle())).append("|");
        }
        output.append("pages|");
        if (bookInfo.hasPages()) {
            for (String page : bookInfo.getPages()) {
                output.append(EscapeTags.Escape(page)).append("|");
            }
        }
        if (output.length() <= 6) {
            return null;
        }
        else {
            return output.substring(0, output.length() - 1);
        }
    }

    @Override
    public String getPropertyId() {
        return "book";
    }

    @Override
    public void adjust(Mechanism mechanism) {

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
        // <i@item.book.get_page[<#>]>
        // <i@item.book.pages>
        // <i@item.book>
        // -->

        if (mechanism.matches("book")) {
            BookMeta meta = (BookMeta) item.getItemStack().getItemMeta();
            dList data = mechanism.getValue().asType(dList.class);
            if (data.size() < 2) {
                dB.echoError("Invalid book input!");
            }
            else {
                if (data.size() > 4 && data.get(0).equalsIgnoreCase("author")
                        && data.get(2).equalsIgnoreCase("title")) {
                    if (!item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                        dB.echoError("That type of book cannot have title or author!");
                    }
                    else {
                        meta.setAuthor(EscapeTags.unEscape(data.get(1)));
                        meta.setTitle(EscapeTags.unEscape(data.get(3)));
                        for (int i = 0; i < 4; i++) {
                            data.remove(0); // No .removeRange?
                        }
                    }
                }
                if (!data.get(0).equalsIgnoreCase("pages")) {
                    dB.echoError("Invalid book input!");
                }
                else {
                    ArrayList<String> newPages = new ArrayList<String>();
                    for (int i = 1; i < data.size(); i++) {
                        newPages.add(EscapeTags.unEscape(data.get(i)));
                    }
                    meta.setPages(newPages);
                }
                item.getItemStack().setItemMeta(meta);
            }
        }
    }
}
