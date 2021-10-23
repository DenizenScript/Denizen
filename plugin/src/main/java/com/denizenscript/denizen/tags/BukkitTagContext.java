package com.denizenscript.denizen.tags;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.TagContext;

public class BukkitTagContext extends TagContext {
    public PlayerTag player;
    public NPCTag npc;

    public BukkitTagContext(BukkitTagContext copyFrom) {
        this(copyFrom.player, copyFrom.npc, copyFrom.entry, copyFrom.debug, copyFrom.script);
    }

    public BukkitTagContext(EntityTag entity, ScriptTag script) {
        super(script == null || script.getContainer().shouldDebug(), null, script);
        this.player = entity != null && entity.isPlayer() ? entity.getDenizenPlayer() : null;
        this.npc = entity != null && entity.isNPC() ? entity.getDenizenNPC() : null;
    }

    public BukkitTagContext(PlayerTag player, NPCTag npc, ScriptTag script) {
        super(script == null || script.getContainer().shouldDebug(), null, script);
        this.player = player;
        this.npc = npc;
    }

    public BukkitTagContext(PlayerTag player, NPCTag npc, ScriptEntry entry, boolean debug, ScriptTag script) {
        super(debug, entry, script);
        this.player = player;
        this.npc = npc;
    }

    public BukkitTagContext(ScriptEntry entry) {
        super(entry);
        if (entry != null) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) entry.entryData;
            player = data.getPlayer();
            npc = data.getNPC();
        }
    }

    public BukkitTagContext(ScriptContainer container) {
        super(container == null || container.shouldDebug(), null, container == null ? null : new ScriptTag(container));
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        BukkitScriptEntryData bsed = new BukkitScriptEntryData(player, npc);
        bsed.scriptEntry = entry;
        return bsed;
    }

    @Override
    public String toString() {
        return "Context{player=" + player + ",npc=" + npc + ",entry=" + entry + ",debug=" + debug + ",script=" + script + "}";
    }
}
