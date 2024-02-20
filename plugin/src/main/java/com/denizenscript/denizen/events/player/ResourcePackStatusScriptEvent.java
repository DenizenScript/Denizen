package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;

public class ResourcePackStatusScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // resource pack status
    //
    // @Group Player
    //
    // @Triggers when a player accepts, denies, successfully loads, or fails to download a resource pack.
    //
    // @Switch status:<status> to only process the event when a specific status is returned. Same status names as returned by 'context.status'.
    //
    // @Context
    // <context.status> returns an ElementTag of the status. Can be: SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED.
    //
    // @Player Always.
    //
    // -->

    public ResourcePackStatusScriptEvent() {
        instance = this;
        registerCouldMatcher("resource pack status");
        registerSwitches("status");
    }

    public static ResourcePackStatusScriptEvent instance;

    public ElementTag status;
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "status", status.asString())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        super.init();
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("status")) {
            return status;
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }
}
