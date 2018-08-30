package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.BookScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

// <--[event]
// @Events
// player edits book
// player signs book
//
// @Regex ^on player (edits|signs) book$
//
// @Triggers when a player edits or signs a book.
// @Context
// <context.title> returns the name of the book, if any.
// <context.pages> returns the number of pages in the book.
// <context.book> returns the book item being edited.
// <context.signing> returns whether the book is about to be signed.
//
// @Determine
// "NOT_SIGNING" to prevent the book from being signed.
// dScript to set the book information to set it to instead.
//
// -->

public class PlayerEditsBookScriptEvent extends BukkitScriptEvent implements Listener {

    PlayerEditsBookScriptEvent instance;
    PlayerEditBookEvent event;
    Element signing;
    Element title;
    Element pages;
    dItem book;
    dPlayer player;
    BookMeta bookMeta;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player edits book") || lower.startsWith("player signs book");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String action = CoreUtilities.getXthArg(1, lower);
        if (action.equals("edits")) {
            return true;
        }
        if (action.equals("signs") && signing.asBoolean()) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "PlayerEditsBook";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerEditBookEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.toUpperCase().equals("NOT_SIGNING")) {
            signing = Element.FALSE;
        }
        else if (dScript.matches(determination)) {
            dScript script = dScript.valueOf(determination);
            if (script.getContainer() instanceof BookScriptContainer) {
                dItem dBook = ((BookScriptContainer) script.getContainer()).getBookFrom(player, null);
                bookMeta = (BookMeta) dBook.getItemStack().getItemMeta();
                // TODO: 1.13 - better method?
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) ? dBook.getMaterial().getMaterial() == Material.WRITABLE_BOOK
                    : dBook.getMaterial().getMaterial() == Material.valueOf("BOOK_AND_QUILL")) {
                    signing = Element.FALSE;
                }
            }
            else {
                dB.echoError("Script '" + determination + "' is valid, but not of type 'book'!");
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("signing")) {
            return signing;
        }
        if (name.equals("title")) {
            return title;
        }
        else if (name.equals("book")) {
            return book;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEditsBook(PlayerEditBookEvent event) {
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        signing = new Element(event.isSigning());
        bookMeta = event.getNewBookMeta();
        pages = new Element(bookMeta.getPageCount());
        title = event.isSigning() ? new Element(bookMeta.getTitle()) : null;
        book = new dItem(event.getPlayer().getInventory().getItem(event.getSlot()));
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setNewBookMeta(bookMeta);
        event.setSigning(signing.asBoolean());
    }
}
