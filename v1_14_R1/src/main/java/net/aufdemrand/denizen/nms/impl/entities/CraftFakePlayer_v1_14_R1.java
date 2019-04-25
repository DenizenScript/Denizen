package net.aufdemrand.denizen.nms.impl.entities;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.FakePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

public class CraftFakePlayer_v1_14_R1 extends CraftPlayer implements FakePlayer {

    private final CraftServer server;
    public String fullName;

    public CraftFakePlayer_v1_14_R1(CraftServer server, EntityFakePlayer_v1_14_R1 entity) {
        super(server, entity);
        this.server = server;
        setMetadata("NPC", new FixedMetadataValue(NMSHandler.getJavaPlugin(), true));
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

    @Override
    public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
        return null;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
        return null;
    }
}
