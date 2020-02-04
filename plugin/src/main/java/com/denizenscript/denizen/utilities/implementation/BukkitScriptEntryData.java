package com.denizenscript.denizen.utilities.implementation;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.tags.TagContext;
import org.bukkit.entity.Entity;

public class BukkitScriptEntryData extends ScriptEntryData {
    private PlayerTag player;
    private NPCTag npc;

    public BukkitScriptEntryData(PlayerTag player, NPCTag npc) {
        this.player = player;
        this.npc = npc;
    }

    public BukkitScriptEntryData(EntityTag entity) {
        if (entity == null) {
            return;
        }
        if (entity.isCitizensNPC()) {
            this.npc = entity.getDenizenNPC();
        }
        if (entity.isPlayer()) {
            this.player = entity.getDenizenPlayer();
        }
    }

    public BukkitScriptEntryData(Entity entity) {
        this(entity == null ? null : new EntityTag(entity));
    }

    public PlayerTag getPlayer() {
        return player;
    }

    public NPCTag getNPC() {
        return npc != null && npc.getCitizen() != null ? npc : null;
    }

    public boolean hasNPC() {
        return npc != null && npc.getCitizen() != null;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public void setPlayer(PlayerTag player) {
        this.player = player;
    }

    public void setNPC(NPCTag npc) {
        this.npc = npc;
    }

    @Override
    public void transferDataFrom(ScriptEntryData scriptEntryData) {
        if (scriptEntryData == null) {
            return;
        }
        player = ((BukkitScriptEntryData) scriptEntryData).getPlayer();
        npc = ((BukkitScriptEntryData) scriptEntryData).getNPC();
        scriptEntry = scriptEntryData.scriptEntry;

    }

    @Override
    public TagContext getTagContext() {
        return new BukkitTagContext(player, npc, scriptEntry,
                scriptEntry == null || scriptEntry.shouldDebug(),
                scriptEntry != null ? scriptEntry.getScript() : null);
    }

    @Override
    public String toString() {
        if (npc == null && player == null) {
            return "";
        }
        else if (npc == null) {
            return "player=p@" + player.getName();
        }
        else if (player == null) {
            return "npc=n@" + npc.getId();
        }
        else {
            return "player=p@" + player.getName() + "   npc=n@" + npc.getId();
        }
    }
}
