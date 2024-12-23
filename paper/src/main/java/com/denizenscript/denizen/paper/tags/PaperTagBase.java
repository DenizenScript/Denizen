package com.denizenscript.denizen.paper.tags;

import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.*;
import org.bukkit.Bukkit;

public class PaperTagBase extends PseudoObjectTagBase<PaperTagBase> {

    public static PaperTagBase instance;

    public PaperTagBase() {
        instance = this;
        TagManager.registerStaticTagBaseHandler(PaperTagBase.class, "paper", (t) -> instance);
    }

    @Override
    public void register() {

        // <--[tag]
        // @attribute <paper.tick_times>
        // @returns ListTag(DurationTag)
        // @Plugin Paper
        // @description
        // Returns a sample of the server's last 5s of tick times as a list of durations.
        // On average, a tick should take 50ms or less for a stable 20tps.
        // -->
        tagProcessor.registerTag(ListTag.class, "tick_times", (attribute, object) -> {
            ListTag list = new ListTag();
            for (long time : Bukkit.getServer().getTickTimes()) {
                list.addObject(new DurationTag(time / 1000000000D));
            }
            return list;
        });
    }
}
