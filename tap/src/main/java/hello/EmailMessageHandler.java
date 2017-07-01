package hello;

/**
 * @author Mark Pollack
 */
public class EmailMessageHandler {

	public EmailMessageHandler() {

	}

	public void handleMessage(Email email) {
		try {
			Thread.sleep(50);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Tap Received <" + email + ">");
	}
}
