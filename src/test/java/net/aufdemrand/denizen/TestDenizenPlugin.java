package net.aufdemrand.denizen;

import static org.hamcrest.Matchers.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the basic plugin functionality
 * @author grog
 */
@RunWith(JMock.class)
public class TestDenizenPlugin {
	Mockery context = new JUnit4Mockery();
	
	/**
	 * Test the plugin handing no arguments passed with an invocation of "/denizen"
	 */
	@Test
	public void testNoArgCommand() {
		Denizen denizen = new Denizen();
		final CommandSender sender = context.mock(CommandSender.class);
		
		context.checking(new Expectations() {{
			oneOf(sender).sendMessage(with(aNonNull(String.class)));
		}});
		
		denizen.onCommand(sender, null, null, new String[0]);
	}

	/**
	 * Test the plugin handing no arguments passed with an invocation of "/denizen"
	 */
	@Test
	public void testVersion() {
		Denizen denizen = new Denizen();
		final Player sender = context.mock(Player.class);
		
		context.checking(new Expectations() {{
			oneOf(sender).sendMessage(with(allOf(aNonNull(String.class), startsWith(ChatColor.GREEN +"Denizen version: "))));
		}});
		
		denizen.onCommand(sender, null, null, new String[]{"version"});
	}
}