package com.denizenscript.denizen.paper.tags;

import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.Bukkit;

public class PaperTagBase {

    public PaperTagBase() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                paperTag(event);
            }
        }, "paper");
    }

    public void paperTag(ReplaceableTagEvent event) {
        if (!event.matches("paper") || event.replaced()) {
            return;
        }

        Attribute attribute = event.getAttributes().fulfill(1);

        // <--[tag]
        // @attribute <paper.tick_times>
        // @returns ListTag
        // @description
        // Returns a sample of the server's last 5s of tick times (in milliseconds).
        // On average, a tick should take 50ms or less for a stable 20tps.
        // -->
        if (attribute.startsWith("tick_times")) {
            ListTag list = new ListTag();
            for (long time : Bukkit.getServer().getTickTimes()) {
                list.add(String.valueOf(time / 1000000D));
            }
            event.setReplacedObject(list.getObjectAttribute(attribute.fulfill(1)));
            return;
        }
    }
}
