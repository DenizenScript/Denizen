package net.aufdemrand.denizen.npc.traits;

import java.io.File;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.alicebot.ab.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class ChatbotTrait extends Trait {

    public ChatbotTrait() {
        super("chatbot");
    }

    static String path = Bukkit.getPluginManager().getPlugin("Denizen")
            .getDataFolder() + File.separator;

    @Persist("botname")
    private String botname;
    private Bot bot;
    private Chat chatSession;

    @Override
    public void load(DataKey key) throws NPCLoadException {
        setBot(key.getString("botname"));
    }

    public void chatTo(LivingEntity entity, String input) {
        SpeechContext context = new SpeechContext(reponse(input));
        context.addRecipient(entity);
        context.setTalker(getNPC().getBukkitEntity());
        npc.getDefaultSpeechController().speak(context, "chat");
    }

    public String reponse(String request) {
        return chatSession.multisentenceRespond(request);
    }

    public void setBot(String newname) {
        botname = newname;
        bot = new Bot(botname, path);
        chatSession = new Chat(bot);
    }

}
