package com.denizenscript.denizen.utilities.implementation;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.tags.TagContext;

public class BukkitScriptEntryData extends ScriptEntryData {
    private PlayerTag player;
    private NPCTag npc;

    public BukkitScriptEntryData(PlayerTag player, NPCTag npc) {
        this.player = player;
        this.npc = npc;
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
        return new BukkitTagContext(player, npc, false, scriptEntry,
                scriptEntry != null ? scriptEntry.shouldDebug() : true,
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
