package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.minecraft.server.v1_9_R2.EntityArrow;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Vehicle;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Set;

public class CraftFakeArrow extends CraftArrow implements DenizenCustomEntity, Vehicle {
    public CraftFakeArrow(CraftServer craftServer, EntityArrow entityArrow) {
        super(craftServer, entityArrow);
    }

    /*@Override
    public void setShooter (LivingEntity livingEntity) {
    }*/

    @Override
    public void remove() {
        if (getPassenger() != null) {
            return;
        }
        super.remove();
    }

    @CreateEntity
    public static Arrow createArrow(Location location, ArrayList<Mechanism> mechanisms) {
        CraftWorld world = (CraftWorld) location.getWorld();
        EntityArrow arrow = new FakeArrowEntity(world, location);
        return (Arrow) arrow.getBukkitEntity();
    }

    @Override
    public String getName() {
        return "FakeArrow";
    }

    @Override
    public void sendMessage(String message) {
        dB.log("Message sent to FakeArrow: " + message);
    }

    @Override
    public void sendMessage(String[] messages) {
        dB.log("Messages sent to FakeArrow: " + messages);
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean op) {
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return false;
    }

    @Override
    public boolean hasPermission(String name) {
        return false;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return false;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return false;
    }

    @Override
    public void recalculatePermissions() {
    }

    public void removeAttachment(PermissionAttachment attachment) {
    }

    @Override
    public String getEntityTypeName() {
        return "FAKE_ARROW";
    }
}
