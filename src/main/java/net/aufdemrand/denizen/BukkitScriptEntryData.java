package net.aufdemrand.denizen;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.tags.TagContext;
import net.citizensnpcs.api.CitizensAPI;

public class BukkitScriptEntryData extends ScriptEntryData {
    private dPlayer player;
    private dNPC npc;

    public BukkitScriptEntryData(dPlayer player, dNPC npc) {
        this.player = player;
        this.npc = npc;
    }

    public dPlayer getPlayer() {
        return player;
    }

    public dNPC getNPC() {
        return npc;
    }

    public boolean hasNPC() {
        return npc != null;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public void setPlayer(dPlayer player) {
        if (player != null && player.isOnline() && Depends.citizens != null
                && CitizensAPI.getNPCRegistry().isNPC(player.getPlayerEntity())) {
            dontFixMe = true;
            setNPC(new dNPC(CitizensAPI.getNPCRegistry().getNPC(player.getPlayerEntity())));
        }
        else
            this.player = player;
    }

    private boolean dontFixMe = false;

    public void setNPC(dNPC npc) {
        if (npc == null && dontFixMe) {
            dontFixMe = false;
        }
        this.npc = npc;
    }

    @Override
    public void transferDataFrom(ScriptEntryData scriptEntryData) {
        if (scriptEntryData == null) {
            return;
        }
        player = ((BukkitScriptEntryData)scriptEntryData).getPlayer();
        npc = ((BukkitScriptEntryData)scriptEntryData).getNPC();

    }

    @Override
    public TagContext getTagContext() {
        return new BukkitTagContext(player, npc, false, null, true, null);
    }
}
