package com.denizenscript.denizen.v1_12.impl;

import com.denizenscript.denizen.v1_12.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.entity.Player;

public class SidebarImpl extends Sidebar {

    private static final Scoreboard dummyScoreboard = new Scoreboard();
    private static final IScoreboardCriteria dummyCriteria = new ScoreboardBaseCriteria("dummy");

    private ScoreboardObjective obj1;
    private ScoreboardObjective obj2;

    public SidebarImpl(Player player) {
        super(player);
        this.obj1 = new ScoreboardObjective(dummyScoreboard, "dummy_1", dummyCriteria);
        this.obj2 = new ScoreboardObjective(dummyScoreboard, "dummy_2", dummyCriteria);
        setDisplayName(title);
    }

    @Override
    protected void setDisplayName(String title) {
        if (this.obj1 != null) {
            this.obj1.setDisplayName(title);
            this.obj2.setDisplayName(title);
        }
    }

    @Override
    public void sendUpdate() {
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj1, 0));
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i];
            if (line == null) {
                break;
            }
            ScoreboardScore score = new ScoreboardScore(dummyScoreboard, this.obj1, line);
            score.setScore(this.scores[i]);
            PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardScore(score));
        }
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, this.obj1));
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
        ScoreboardObjective temp = this.obj2;
        this.obj2 = this.obj1;
        this.obj1 = temp;
    }

    @Override
    public void remove() {
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
    }
}
