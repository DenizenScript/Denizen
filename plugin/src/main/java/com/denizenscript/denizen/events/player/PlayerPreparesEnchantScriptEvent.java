package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class PlayerPreparesEnchantScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player prepares item enchant
    // player prepares <item> enchant
    //
    // @Regex ^on player prepares [^\s]+ enchant$
    //
    // @Group Player
    //
    // @Triggers when a player prepares to enchant an item.
    //
    // @Context
    // <context.location> returns the LocationTag of the enchanting block.
    // <context.item> returns the ItemTag to be enchanted.
    // <context.bonus> returns an ElementTag(Number) of the enchanting bonus available (number of bookshelves).
    // <context.offers> returns a ListTag of the available enchanting offers, each as a MapTag with keys 'cost', 'enchantment_type', and 'level'.
    //
    // @Determine
    // "OFFERS:<ListTag>" of MapTags to set the offers available. Cannot be a different size list than the size of context.offers.
    //
    // @Player Always.
    //
    // -->

    public PlayerPreparesEnchantScriptEvent() {
    }

    public PrepareItemEnchantEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player prepares") || !path.eventArgLowerAt(3).equals("enchant")) {
            return false;
        }
        if (!couldMatchItem(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, new ItemTag(event.getItem()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determineLow = CoreUtilities.toLowerCase(determinationObj.toString());
            if (determineLow.startsWith("offers:")) {
                ListTag offers = ListTag.valueOf(determineLow.substring("offers:".length()), getTagContext(path));
                if (offers.size() != event.getOffers().length) {
                    Debug.echoError("Offer list size incorrect.");
                    return false;
                }
                for (int i = 0; i < offers.size(); i++) {
                    MapTag map = MapTag.getMapFor(offers.getObject(i), getTagContext(path));
                    event.getOffers()[i].setCost(map.getElement("cost").asInt());
                    EnchantmentTag enchantment = map.getObjectAs("enchantment_type", EnchantmentTag.class, getTagContext(path));
                    if (enchantment == null) {
                        enchantment = map.getObjectAs("enchantment", EnchantmentTag.class, getTagContext(path));
                    }
                    event.getOffers()[i].setEnchantment(enchantment.enchantment);
                    event.getOffers()[i].setEnchantmentLevel(map.getElement("level").asInt());
                }
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getEnchanter());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item":
                return new ItemTag(event.getItem());
            case "location":
                return new LocationTag(event.getEnchantBlock().getLocation());
            case "bonus":
                return new ElementTag(event.getEnchantmentBonus());
            case "offers":
                ListTag output = new ListTag();
                for (EnchantmentOffer offer : event.getOffers()) {
                    MapTag map = new MapTag();
                    map.putObject("cost", new ElementTag(offer.getCost()));
                    map.putObject("enchantment", new ElementTag(offer.getEnchantment().getKey().getKey()));
                    map.putObject("enchantment_type", new EnchantmentTag(offer.getEnchantment()));
                    map.putObject("level", new ElementTag(offer.getEnchantmentLevel()));
                    output.addObject(map);
                }
                return output;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEnchantItem(PrepareItemEnchantEvent event) {
        if (event.getInventory().getViewers().isEmpty()) {
            return;
        }
        this.event = event;
        fire(event);
    }
}
