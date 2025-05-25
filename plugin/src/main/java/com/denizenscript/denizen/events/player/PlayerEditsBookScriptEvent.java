package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class PlayerEditsBookScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player edits book
    // player signs book
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Triggers when a player edits or signs a book.
    //
    // @Context
    // <context.title> returns the name of the book, if any.
    // <context.pages> returns the number of pages in the book.
    // <context.book> returns the book item being edited, containing the new page contents.
    // <context.old_book> returns the book item being edited, containing the old page contents.
    // <context.signing> returns whether the book is about to be signed.
    //
    // @Determine
    // "NOT_SIGNING" to prevent the book from being signed.
    // ScriptTag to set the book information to set it to instead.
    //
    // @Player Always.
    //
    // -->

    public PlayerEditsBookScriptEvent() {
        registerCouldMatcher("player edits book");
        registerCouldMatcher("player signs book");
        this.<PlayerEditsBookScriptEvent>registerTextDetermination("not_signing", (evt) -> {
            evt.event.setSigning(false);
        });
        this.<PlayerEditsBookScriptEvent, ScriptTag>registerOptionalDetermination(null, ScriptTag.class, (evt, context, value) -> {
            if (value.getContainer() instanceof BookScriptContainer script) {
                ItemTag dBook = script.getBookFrom(context);
                BookMeta bookMeta = (BookMeta) dBook.getItemMeta();
                if (dBook.getBukkitMaterial() == Material.WRITABLE_BOOK) {
                    evt.event.setSigning(false);
                }
                evt.event.setNewBookMeta(bookMeta);
                return true;
            }
            else {
                Debug.echoError("Script '" + value + "' is valid, but not of type 'book'!");
                return false;
            }
        });
    }

    public PlayerEditBookEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String action = path.eventArgLowerAt(1);
        if (!(action.equals("edits") && !event.isSigning()) && !(action.equals("signs") && event.isSigning())) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "signing" -> new ElementTag(event.isSigning());
            case "title" -> event.isSigning() ? new ElementTag(event.getNewBookMeta().getTitle(), true) : null;
            case "pages" -> new ElementTag(event.getNewBookMeta().getPageCount());
            case "book" ->  {
                ItemStack book = new ItemStack(event.isSigning() ? Material.WRITTEN_BOOK : Material.WRITABLE_BOOK);
                book.setItemMeta(event.getNewBookMeta());
                yield new ItemTag(book);
            }
            case "old_book" -> {
                ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
                book.setItemMeta(event.getPreviousBookMeta());
                yield new ItemTag(book);
            }
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerEditsBook(PlayerEditBookEvent event) {
        this.event = event;
        fire(event);
    }
}
