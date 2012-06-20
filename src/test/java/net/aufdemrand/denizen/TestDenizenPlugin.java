package net.aufdemrand.denizen;

import org.bukkit.command.CommandSender;
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
	
	@Test
	/**
	 * Test the plugin handing no arguments passed with an invocation of "/denizen"
	 */
	public void testNoArgCommand() {
		Denizen denizen = new Denizen();
		final CommandSender sender = context.mock(CommandSender.class);
		
		context.checking(new Expectations() {{
			oneOf(sender).sendMessage(with(aNonNull(String.class)));
		}});
		
		denizen.onCommand(sender, null, null, new String[0]);
	}
}