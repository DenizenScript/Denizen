package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.EscapeTags;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

public class ItemBook implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (
                ((dItem) item).getItemStack().getType().equals(Material.WRITTEN_BOOK)
                || ((dItem) item).getItemStack().getType().equals(Material.BOOK_AND_QUILL)
                );
    }

    public static ItemBook getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemBook((dItem)_item);
    }


    private ItemBook(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        if (attribute.startsWith("book")) {
            BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
            attribute = attribute.fulfill(1);

            if (item.getItemStack().getType() == Material.WRITTEN_BOOK) {

                // <--[tag]
                // @attribute <i@item.book.author>
                // @returns Element
                // @description
                // Returns the author of the book.
                // -->
                if (attribute.startsWith("author"))
                    return new Element(bookInfo.getAuthor())
                            .getAttribute(attribute.fulfill(1));

                // <--[tag]
                // @attribute <i@item.book.title>
                // @returns Element
                // @description
                // Returns the title of the book.
                // -->
                if (attribute.startsWith("title"))
                    return new Element(bookInfo.getTitle())
                            .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <i@item.book.page_count>
            // @returns Element(Number)
            // @description
            // Returns the number of pages in the book.
            // -->
            if (attribute.startsWith("page_count"))
                return new Element(bookInfo.getPageCount())
                        .getAttribute(attribute.fulfill(1));

            // <--[tag]
            // @attribute <i@item.book.get_page[<#>]>
            // @returns Element
            // @description
            // Returns the page specified from the book as an element.
            // -->
            if (attribute.startsWith("get_page") && aH.matchesInteger(attribute.getContext(1)))
                return new Element(bookInfo.getPage(attribute.getIntContext(1)))
                    .getAttribute(attribute.fulfill(1));

            // <--[tag]
            // @attribute <i@item.book.pages>
            // @returns dList
            // @description
            // Returns the pages of the book as a dList.
            // -->
            if (attribute.startsWith("pages"))
                return new dList(bookInfo.getPages())
                        .getAttribute(attribute.fulfill(1));

            // <--[tag]
            // @attribute <i@item.book>
            // @returns Element
            // @description
            // Returns full information on the book item, in the format
            // author|AUTHOR|title|TITLE|pages|PAGE_ONE|PAGE_TWO|...
            // or as pages|PAGE_ONE|PAGE_TWO|...
            // -->
            String output = getPropertyString();
            if (output == null)
                output = "null";
            return new Element(output)
                    .getAttribute(attribute);
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        StringBuilder output = new StringBuilder();
        BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
        if (item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
            output.append("author|").append(EscapeTags.Escape(bookInfo.getAuthor()))
                    .append("|title|").append(EscapeTags.Escape(bookInfo.getTitle())).append("|");
        }
        output.append("pages|");
        for (String page: bookInfo.getPages()) {
            output.append(EscapeTags.Escape(page)).append("|");
        }
        if (output.length() == 6)
            return null;
        else
            return output.substring(0, output.length() - 1);
    }

    @Override
    public String getPropertyId() {
        return "book";
    }
}
