package net.aufdemrand.denizen.nms.impl;

import net.aufdemrand.denizen.nms.abstracts.Sidebar;
import net.aufdemrand.denizen.nms.helpers.PacketHelper_v1_10_R1;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.entity.Player;

public class Sidebar_v1_10_R1 extends Sidebar {

    private static final Scoreboard dummyScoreboard = new Scoreboard();
    private static final IScoreboardCriteria dummyCriteria = new ScoreboardBaseCriteria("dummy");

    private ScoreboardObjective obj1;
    private ScoreboardObjective obj2;

    public Sidebar_v1_10_R1(Player player) {
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
        PacketHelper_v1_10_R1.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj1, 0));
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i];
            if (line == null) {
                break;
            }
            ScoreboardScore score = new ScoreboardScore(dummyScoreboard, this.obj1, line);
            score.setScore(this.scores[i]);
            PacketHelper_v1_10_R1.sendPacket(player, new PacketPlayOutScoreboardScore(score));
        }
        PacketHelper_v1_10_R1.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, this.obj1));
        PacketHelper_v1_10_R1.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
        ScoreboardObjective temp = this.obj2;
        this.obj2 = this.obj1;
        this.obj1 = temp;
    }

    @Override
    public void remove() {
        PacketHelper_v1_10_R1.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
    }
}
