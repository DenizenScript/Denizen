package net.aufdemrand.denizen.npc.traits;

import java.io.File;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.alicebot.ab.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class ChatbotTrait extends Trait {

    // <--[language]
    // @name ChatBot Trait
    // @group NPC Traits
    // @description
    // A fully functional NPC AI Chat bot.
    //
    // Requires <@link url http://ci.citizensnpcs.co/job/AliceBot/> (ALICE Bot)
    // In your /plugins/Denizen/lib folder.
    //
    // -->
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

    public void chatTo(Entity entity, String input) {
        SpeechContext context = new SpeechContext(response(input));
        context.addRecipient(entity);
        context.setTalker(getNPC().getEntity());
        npc.getDefaultSpeechController().speak(context, "chat");
    }

    public String response(String request) {
        return chatSession.multisentenceRespond(request);
    }

    public void setBot(String newname) {
        botname = newname;
        bot = new Bot(botname, path);
        chatSession = new Chat(bot);
    }
}
