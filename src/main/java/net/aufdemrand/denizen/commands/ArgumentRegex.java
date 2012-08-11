package net.aufdemrand.denizen.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentRegex {

	final public Pattern durationArgument = Pattern.compile("(?:DURATION|duration|Duration)(:)(\\d+)");
	public boolean matchesDuration(String regex) {
	    Matcher m = durationArgument.matcher(regex);
	    return m.matches();
	}

	final public Pattern integerArgument = Pattern.compile("\\d+");
	public boolean matchesInteger(String regex) {
	    Matcher m = integerArgument.matcher(regex);
	    return m.matches();
	}

	final public Pattern scriptArgument = Pattern.compile("(?:SCRIPT|script|Script)(:)(\\w+)");
	public boolean matchesScript(String regex) {
	    Matcher m = scriptArgument.matcher(regex);
	    return m.matches();
	}
}
