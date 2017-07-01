package hello;

import org.springframework.jms.annotation.JmsListener;

//@Component

// TBD:  There is a bug using @JmsListener, the span is destroyed too early.

public class Receiver {

	@JmsListener(destination = "destination1", containerFactory = "myFactory")
	public void receiveMessage(Email email) {
		System.out.println("Tap Received <" + email + ">");
	}

}
