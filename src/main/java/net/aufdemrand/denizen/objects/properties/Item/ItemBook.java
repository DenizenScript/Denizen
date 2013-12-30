package net.aufdemrand.denizen.objects.properties.Item;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
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

            if (item.getItemStack().getType() == Material.WRITTEN_BOOK) {

                // <--[tag]
                // @attribute <i@item.book.author>
                // @returns Element
                // @description
                // Returns the author of the book.
                // -->
                if (attribute.startsWith("author"))
                    return new Element(bookInfo.getAuthor())
                            .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <i@item.book.title>
                // @returns Element
                // @description
                // Returns the title of the book.
                // -->
                if (attribute.startsWith("title"))
                    return new Element(bookInfo.getTitle())
                            .getAttribute(attribute.fulfill(2));
            }

                // <--[tag]
                // @attribute <i@item.book.page_count>
                // @returns Element(Number)
                // @description
                // Returns the number of pages in the book.
                // -->
                if (attribute.startsWith("page_count"))
                    return new Element(bookInfo.getPageCount())
                            .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <i@item.book.get_page[<#>]>
                // @returns Element
                // @description
                // Returns the page specified from the book as an element.
                // -->
                if (attribute.startsWith("get_page") && aH.matchesInteger(attribute.getContext(2)))
                    return new Element(bookInfo.getPage(attribute.getIntContext(2)))
                            .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <i@item.book.pages>
                // @returns dList
                // @description
                // Returns the pages of the book as a dList.
                // -->
                if (attribute.startsWith("pages"))
                    return new dList(bookInfo.getPages())
                            .getAttribute(attribute.fulfill(2));

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
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    // <--[language]
    // @name Property Escaping
    // @group Useful Lists
    // @description
    // Some item properties (and corresponding mechanisms) need to escape their
    // text output/input to prevent players using them to cheat the system
    // (EG, if a player set the display name of an item to:
    //      'name;enchantments=damage_all,3', he would get a free enchantment!)
    // This are the escape codes used to prevent that:
    //
    // | = &pipe;
    // < = &lt;
    // > = &gt;
    // newline = &nl;
    // & = &amp;
    // semicolons are just simplified to ‑ (a non-breaking hyphen)
    // Semicolons can also be input as &sc;
    // If you're directly typing this into a script, use <&sc> for semicolons
    // (That means for all of escapes, EG: &pipe<&sc>)
    // -->
    /**
     * A quick function to escape book Strings.
     * This is just to prevent tag reading errors.
     *
     * @param input the unescaped data.
     * @return the escaped data.
     */
    public static String Escape(String input) {
        return input.replace("&", "&amp;").replace("|", "&pipe;")
                .replace(">", "&gt;").replace("<", "&lt;")
                .replace("\n", "&nl;");
    }

    /**
     * A quick function to reverse a book string escaping.
     * This is just to prevent tag reading errors.
     *
     * @param input the escaped data.
     * @return the unescaped data.
     */
    public static String unEscape(String input) {
        return input.replace("&pipe;", "|").replace("&nl;", "\n")
                .replace("&gt;", ">").replace("&lt;", "<")
                .replace("&amp;", "&").replace("&sc;", ";");
    }

    @Override
    public String getPropertyString() {
        StringBuilder output = new StringBuilder();
        BookMeta bookInfo = (BookMeta) item.getItemStack().getItemMeta();
        if (item.getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
            output.append("author|").append(Escape(bookInfo.getAuthor()))
                    .append("|title|").append(Escape(bookInfo.getTitle())).append("|");
        }
        output.append("pages|");
        for (String page: bookInfo.getPages()) {
            output.append(Escape(page)).append("|");
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
