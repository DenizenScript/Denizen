package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.utilities.command.exceptions.CommandException;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;

public class RequirementsProcessor implements CommandAnnotationProcessor {

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return Requirements.class;
    }

    @Override
    public void process(CommandSender sender, CommandContext context, Annotation instance, Object[] methodArgs)
            throws CommandException {
        // TODO: add Denizen-based requirements/remove this system
    }
}
