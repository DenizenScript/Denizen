package net.aufdemrand.denizen.utilities.packets.handler;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dItem;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.StreamSerializer;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtType;

public class InvisibleLoreHandler {
    
    public Denizen denizen;
    public ProtocolManager protocolManager;

    public InvisibleLoreHandler(Denizen denizen) {
        this.denizen = denizen;
        protocolManager = ProtocolLibrary.getProtocolManager();
        registerListeners();
    }

    public void registerListeners() {

        protocolManager.addPacketListener(new PacketAdapter(denizen, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGH, 0x67, 0x68) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                try {
                    
                    switch (packet.getID()) {

                        case 0x67:
                            StructureModifier<ItemStack> sm = packet.getItemModifier();
                            for (int i = 0; i < sm.size(); i++) {
                                encodeLore(sm.read(i));
                            }
                            break;

                        case 0x68:
                            StructureModifier<ItemStack[]> smArray = packet.getItemArrayModifier();
                            for (int i = 0; i < smArray.size(); i++) {
                                encodeLore(smArray.read(i));
                            }
                            break;
                            
                    }
                    
                } catch (FieldAccessException ex) {}
            }

        });


        PacketAdapter.AdapterParameteters params = PacketAdapter.params()
                .plugin(denizen)
                .connectionSide(ConnectionSide.BOTH)
                .listenerPriority(ListenerPriority.HIGH)
                .options(ListenerOptions.INTERCEPT_INPUT_BUFFER)
                .packets(Packets.Client.SET_CREATIVE_SLOT);

        protocolManager.addPacketListener(new PacketAdapter(params) {

            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketID() == Packets.Server.SET_CREATIVE_SLOT) {
                    encodeLore(event.getPacket().getItemModifier().read(0));
                }
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketID() == Packets.Client.SET_CREATIVE_SLOT) {
                    DataInputStream input = event.getNetworkMarker().getInputStream();
                    if (input == null) {
                        return;
                    }
                    try {
                        input.readShort();
                        ItemStack stack = readItemStack(input, new StreamSerializer());
                        stack = decodeLore(stack);
                        event.getPacket().getItemModifier().write(0, stack);

                    } catch (IOException e) {}
                }
            }
        });

    }

    public ItemStack decodeLore(ItemStack stack) {
        if (stack != null) {
            if (!MinecraftReflection.isCraftItemStack(stack)) {
                stack = MinecraftReflection.getBukkitItemStack(stack);
            }
            NbtCompound tag = NbtFactory.asCompound(NbtFactory.fromItemTag(stack));
            if (tag.containsKey("dItem")) {
                ItemMeta meta = stack.getItemMeta();
                LinkedList<String> lore = new LinkedList<String>();
                for (String line: meta.getLore()) {
                    if (!line.startsWith("§0id:")) {
                        lore.add(line);
                    }
                }
                NbtList dataList = tag.getList("dItem");
                dataList.setElementType(NbtType.TAG_STRING);
                Iterator<String> it = dataList.iterator();
                while (it.hasNext()) {
                    lore.add(it.next());
                }
                meta.setLore(lore);
                stack.setItemMeta(meta);
            }
        }
        return stack;
    }


    public ItemStack[] encodeLore(ItemStack[] stacks) {
        for (int i=0;i<stacks.length;i++) {
            if (stacks[i] != null) {
                stacks[i] = encodeLore(stacks[i]);
            }
        }
        return stacks;
    }
    
    public ItemStack encodeLore(ItemStack stack) {
        if (stack != null) {
            if (stack.hasItemMeta() && stack.getItemMeta().hasLore()) {
                dItem item = new dItem(stack);
                if (item.containsLore("§0id:")) {
                    if (!MinecraftReflection.isCraftItemStack(stack)) {
                        stack = MinecraftReflection.getBukkitItemStack(stack);
                    }
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = meta.getLore();
                    List<String> data = new LinkedList<String>();
                    LinkedList<String> newLore = new LinkedList<String>();
                    for (String line: lore) {
                        if (line.startsWith("§0id:")) {
                            data.add(line);
                        } else {
                            newLore.add(line);
                        }
                    }
                    meta.setLore(newLore);
                    stack.setItemMeta(meta);
                    NbtCompound tag = NbtFactory.asCompound(NbtFactory.fromItemTag(stack));
                    tag.put("dItem", NbtFactory.ofList("dItem", data));
                }
            }
            return stack;
        }
        return null;
    }

    private ItemStack readItemStack(DataInputStream input, StreamSerializer serializer) throws IOException {
        ItemStack result = null;
        short type = input.readShort();

        if (type >= 0) {
            byte amount = input.readByte();
            short damage = input.readShort();
            result = new ItemStack(type, amount, damage);
            NbtCompound tag = serializer.deserializeCompound(input);
            if (tag != null) {
                result = MinecraftReflection.getBukkitItemStack(result);
                NbtFactory.setItemTag(result, tag);
            }
        }
        return result;
    }

}
