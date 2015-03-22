package net.aufdemrand.denizen.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.craftbukkit.v1_8_R2.command.ColouredConsoleSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Set;

/**
 * Used to send commands and record their output.
 */
public class DenizenCommandSender extends ColouredConsoleSender {

    private ArrayList<String> output = new ArrayList<String>();

    public boolean silent = false;

    public ArrayList<String> getOutput() {
        return output;
    }

    public void clearOutput() {
        output.clear();
    }

    @Override
    public void sendMessage(String s) {
        output.add(s);
        if (!silent)
            Bukkit.getServer().getConsoleSender().sendMessage(s);
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String string: strings) {
            sendMessage(string);
        }
    }

    @Override
    public Server getServer() {
        return Bukkit.getConsoleSender().getServer();
    }

    @Override
    public String getName() {
        return Bukkit.getConsoleSender().getName();
    }

    @Override
    public boolean isConversing() {
        return Bukkit.getConsoleSender().isConversing();
    }

    @Override
    public void acceptConversationInput(String s) {
        Bukkit.getConsoleSender().acceptConversationInput(s);
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return Bukkit.getConsoleSender().beginConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        Bukkit.getConsoleSender().abandonConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {
        Bukkit.getConsoleSender().abandonConversation(conversation, conversationAbandonedEvent);
    }

    @Override
    public void sendRawMessage(String s) {
        // TODO: Maybe handle this?
        Bukkit.getConsoleSender().sendRawMessage(s);
    }

    @Override
    public boolean isPermissionSet(String s) {
        return Bukkit.getConsoleSender().isPermissionSet(s);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return Bukkit.getConsoleSender().isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String s) {
        return Bukkit.getConsoleSender().hasPermission(s);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return Bukkit.getConsoleSender().hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return Bukkit.getConsoleSender().addAttachment(plugin, s, b);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return Bukkit.getConsoleSender().addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return Bukkit.getConsoleSender().addAttachment(plugin, s, b, i);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return Bukkit.getConsoleSender().addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {
        Bukkit.getConsoleSender().removeAttachment(permissionAttachment);
    }

    @Override
    public void recalculatePermissions() {
        Bukkit.getConsoleSender().recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Bukkit.getConsoleSender().getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return Bukkit.getConsoleSender().isOp();
    }

    @Override
    public void setOp(boolean b) {
        Bukkit.getConsoleSender().setOp(b);
    }
}
