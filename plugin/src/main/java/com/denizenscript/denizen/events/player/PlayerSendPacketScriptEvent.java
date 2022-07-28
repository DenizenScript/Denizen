package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Player;

public class PlayerSendPacketScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player sends packet
    //
    // @Group Player
    //
    // @Triggers when a player sends a packet to the server.
    //
    // @Cancellable true
    //
    // @Switch class:<classname-matcher> to only process the event when the packet class name matches a given classname matcher.
    //
    // @Context
    // <context.class> returns an ElementTag of the packet's class name. Note that these are spigot-mapped names, not Mojang-mapped.
    //
    // @Player Always.
    //
    // -->

    public PlayerSendPacketScriptEvent() {
        instance = this;
        registerCouldMatcher("player sends packet");
        registerSwitches("class");
    }

    public static PlayerSendPacketScriptEvent instance;

    public ElementTag className;
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "class", className.asString())) {
            return false;
        }
        return super.matches(path);
    }

    public static boolean enabled;

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    @Override
    public String getName() {
        return "PlayerSendsPacket";
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "class": return className;
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    public static boolean fireFor(Player player, Object packet) {
        instance.player = new PlayerTag(player);
        instance.className = new ElementTag(packet.getClass().getSimpleName());
        return instance.fire().cancelled;
    }
}
