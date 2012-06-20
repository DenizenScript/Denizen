/**
 * 
 */
package net.aufdemrand.denizen;

import org.bukkit.command.CommandSender;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple plugin unit test for validating functionality.
 * 
 * @author grog
 */
@RunWith(JMock.class)
public class TestDenizen {
	Mockery context = new JUnit4Mockery();

	@Test
	public void testNoArgsMessageSent() {
		final CommandSender mockSender = context.mock(CommandSender.class);
		Denizen denizen = new Denizen();
		
		context.checking(new Expectations() {{
		    oneOf (mockSender).sendMessage(with(any(String.class)));
		}});
		
		denizen.onCommand(mockSender, null, null, new String[0]);
	}
}
