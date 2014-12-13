package net.aufdemrand.denizen.tags;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizencore.tags.TagContext;

public class BukkitTagContext extends TagContext {
    public final dPlayer player;
    public final dNPC npc;
    public final ScriptEntry entry;
    public final dScript script;
    public BukkitTagContext(dPlayer player, dNPC npc, boolean instant, ScriptEntry entry, boolean debug, dScript script) {
        super(instant, debug);
        this.player = player;
        this.npc = npc;
        this.entry = entry;
        this.script = script;
    }

    public BukkitTagContext(ScriptEntry entry, boolean instant) {
        super(instant, entry != null ? entry.shouldDebug(): true);
        this.entry = entry;
        player = entry != null ? ((BukkitScriptEntryData)entry.entryData).getPlayer(): null;
        npc = entry != null ? ((BukkitScriptEntryData)entry.entryData).getNPC(): null;
        script = entry != null ? entry.getScript(): null;
    }
}
