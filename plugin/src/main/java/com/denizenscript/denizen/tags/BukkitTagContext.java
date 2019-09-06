package com.denizenscript.denizen.tags;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.tags.TagContext;

public class BukkitTagContext extends TagContext {
    public final PlayerTag player;
    public final NPCTag npc;

    public BukkitTagContext(PlayerTag player, NPCTag npc, ScriptTag script) {
        super(false, script.getContainer().shouldDebug(), null, script);
        this.player = player;
        this.npc = npc;
    }

    public BukkitTagContext(PlayerTag player, NPCTag npc, boolean instant, ScriptEntry entry, boolean debug, ScriptTag script) {
        super(instant, debug, entry, script);
        this.player = player;
        this.npc = npc;
    }

    public BukkitTagContext(ScriptEntry entry, boolean instant) {
        super(instant, entry != null ? entry.shouldDebug() : true, entry, entry != null ? entry.getScript() : null);
        player = entry != null ? ((BukkitScriptEntryData) entry.entryData).getPlayer() : null;
        npc = entry != null ? ((BukkitScriptEntryData) entry.entryData).getNPC() : null;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        BukkitScriptEntryData bsed = new BukkitScriptEntryData(player, npc);
        bsed.scriptEntry = entry;
        return bsed;
    }

    @Override
    public String toString() {
        return "Context{player=" + player + ",npc=" + npc + ",instant=" + instant + ",entry=" + entry + ",debug=" + debug + ",script=" + script + "}";
    }
}
