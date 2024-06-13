package com.denizenscript.denizen.nms.v1_20.impl;

import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.helpers.PacketHelperImpl;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SidebarImpl extends Sidebar {

    // TODO: 1.20.3: the scoreboard system saw significant changes, there is likely a better way to do this now

    public static Scoreboard dummyScoreboard = new Scoreboard();
    public static ObjectiveCriteria dummyCriteria;

    static {
        try {
            Constructor<ObjectiveCriteria> constructor = ObjectiveCriteria.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            dummyCriteria = constructor.newInstance("dummy");
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public Objective obj1;
    public Objective obj2;

    public SidebarImpl(Player player) {
        super(player);
        Component chatComponentTitle = Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE));
        this.obj1 = new Objective(dummyScoreboard, "dummy_1", dummyCriteria, chatComponentTitle, ObjectiveCriteria.RenderType.INTEGER, false, StyledFormat.SIDEBAR_DEFAULT);
        this.obj2 = new Objective(dummyScoreboard, "dummy_2", dummyCriteria, chatComponentTitle, ObjectiveCriteria.RenderType.INTEGER, false, StyledFormat.SIDEBAR_DEFAULT);
    }

    @Override
    protected void setDisplayName(String title) {
        if (this.obj1 != null) {
            Component chatComponentTitle = Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE));
            this.obj1.setDisplayName(chatComponentTitle);
            this.obj2.setDisplayName(chatComponentTitle);
        }
    }

    public List<PlayerTeam> generatedTeams = new ArrayList<>();

    @Override
    public void sendUpdate() {
        List<PlayerTeam> oldTeams = generatedTeams;
        generatedTeams = new ArrayList<>();
        PacketHelperImpl.send(player, new ClientboundSetObjectivePacket(this.obj1, 0));
        String[] ids = getIds();
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i];
            if (line == null) {
                break;
            }
            String lineId = ids[i];
            PlayerTeam team = new PlayerTeam(dummyScoreboard, lineId);
            team.getPlayers().add(lineId);
            team.setPlayerPrefix(Handler.componentToNMS(FormattedTextHelper.parse(line, ChatColor.WHITE)));
            generatedTeams.add(team);
            PacketHelperImpl.send(player, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
            PacketHelperImpl.send(player, new ClientboundSetScorePacket(lineId, obj1.getName(), this.scores[i], Optional.empty(), Optional.of(StyledFormat.SIDEBAR_DEFAULT)));
        }
        PacketHelperImpl.send(player, new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, this.obj1));
        PacketHelperImpl.send(player, new ClientboundSetObjectivePacket(this.obj2, 1));
        Objective temp = this.obj2;
        this.obj2 = this.obj1;
        this.obj1 = temp;
        for (PlayerTeam team : oldTeams) {
            PacketHelperImpl.send(player, ClientboundSetPlayerTeamPacket.createRemovePacket(team));
        }
    }

    @Override
    public void remove() {
        for (PlayerTeam team : generatedTeams) {
            PacketHelperImpl.send(player, ClientboundSetPlayerTeamPacket.createRemovePacket(team));
        }
        generatedTeams.clear();
        PacketHelperImpl.send(player, new ClientboundSetObjectivePacket(this.obj2, 1));
    }
}
