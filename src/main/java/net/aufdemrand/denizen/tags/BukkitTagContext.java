package net.aufdemrand.denizen.tags;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.tags.TagContext;

public class BukkitTagContext extends TagContext {
    public final dPlayer player;
    public final dNPC npc;

    public BukkitTagContext(dPlayer player, dNPC npc, boolean instant, ScriptEntry entry, boolean debug, dScript script) {
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
}
