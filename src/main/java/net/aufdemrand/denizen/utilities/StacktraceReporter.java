package net.aufdemrand.denizen.utilities;

import java.util.*;
import java.util.logging.Level;

import javax.mail.*;
import javax.mail.internet.*;

import net.aufdemrand.denizen.Denizen;

public class StacktraceReporter {

	final String to = "jeremy@ssdbg.com";
	final String from = "errors@denizendev.com";
	final String host = "localhost";
	final Properties properties = System.getProperties();
	final Session session = Session.getDefaultInstance(properties);
	final Denizen plugin;

	public StacktraceReporter(Denizen denizen) {
		this.plugin = denizen;
	}

	public void sendError(final String text) {
		properties.setProperty("174.102.4.21", host);
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() { 
				try{
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(from));
					message.addRecipient(Message.RecipientType.TO,
							new InternetAddress(to));
					message.setSubject("There's been a problem!");
					message.setText(text);
					Transport.send(message);
					plugin.getLogger().log(Level.INFO, "An error report has been automatically sent. To disable, see config.yml.");

				} catch (MessagingException mex) {
					mex.printStackTrace();
				}
			}
		}, 1);
	}

}


