package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.SoundLookup;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PlayerHearsSoundScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player hears sound
    //
    // @Group Player
    //
    // @Triggers when a player receives a sound packet from the server.
    //
    // @Cancellable true
    //
    // @Location true
    //
    // @Context
    // <context.sound_key> returns an ElementTag of the modern Minecraft sound key.
    // <context.sound_name> returns an ElementTag of the sound's Bukkit name.
    // <context.category> returns the name of the category the sound is from.
    // <context.is_custom> returns 'true' if the sound is custom, otherwise false.
    // <context.source_entity> returns the entity this sound came from (if any).
    // <context.location> returns the location the sound will play at.
    // <context.volume> returns the volume level.
    // <context.pitch> returns the pitch.
    //
    // @Player Always.
    //
    // -->

    public PlayerHearsSoundScriptEvent() {
        instance = this;
        registerCouldMatcher("player hears sound");
    }

    public static PlayerHearsSoundScriptEvent instance;
    public static boolean enabled;

    public Player player;
    public String soundName;
    public String category;
    public boolean isCustom;
    public Entity entity;
    public Location location;
    public float volume, pitch;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player);
    }

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
        entity = null;
        player = null;
        location = null;
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "sound_key": return new ElementTag(soundName);
            case "sound_name": return isCustom ? null : new ElementTag(SoundLookup.keyToSound.get(soundName));
            case "category": return new ElementTag(category);
            case "is_custom": return new ElementTag(isCustom);
            case "source_entity": return entity == null ? null : new EntityTag(entity);
            case "location": return new LocationTag(location);
            case "volume": return new ElementTag(volume);
            case "pitch": return new ElementTag(pitch);
        }
        return super.getContext(name);
    }

    public boolean run(Player player, String soundName, String category, boolean isCustom, Entity entity, Location location, float volume, float pitch) {
        this.player = player;
        this.soundName = soundName;
        this.category = category;
        this.isCustom = isCustom;
        this.entity = entity;
        this.location = location == null ? entity.getLocation() : location;
        this.volume = volume;
        this.pitch = pitch;
        ScriptEvent fired = fire();
        return fired.cancelled;
    }
}
