package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author Mark Pollack
 */
public class EmailMessageHandler {

	private JmsTemplate jmsTemplate;

	public EmailMessageHandler(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void handleMessage(Email email) {
		System.out.println("Received <" + email + ">");

		jmsTemplate.convertAndSend("destination2",
				new Email("info@example.com", "Hello-transformed"));
	}
}
