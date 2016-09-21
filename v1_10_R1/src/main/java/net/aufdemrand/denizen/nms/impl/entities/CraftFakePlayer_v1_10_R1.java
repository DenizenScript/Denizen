package net.aufdemrand.denizen.nms.impl.entities;

import net.aufdemrand.denizen.nms.interfaces.FakePlayer;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CraftFakePlayer_v1_10_R1 extends CraftPlayer implements FakePlayer {

    private final CraftServer server;
    public String fullName;

    public CraftFakePlayer_v1_10_R1(CraftServer server, EntityFakePlayer_v1_10_R1 entity, JavaPlugin plugin) {
        super(server, entity);
        this.server = server;
        setMetadata("NPC", new FixedMetadataValue(plugin, true));
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        this.server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return this.server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return this.server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        this.server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public String getEntityTypeName() {
        return "FAKE_PLAYER";
    }

    @Override
    public String getFullName() {
        return fullName;
    }
}
