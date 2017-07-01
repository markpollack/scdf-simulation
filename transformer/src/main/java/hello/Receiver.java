package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//@Component
// TBD:  There is a bug using @JmsListener, the span is destroyed too early.
public class Receiver {

    private static List<Class<?>> SUPPORTED_PROPERTY_TYPES = Arrays.asList(new Class<?>[] {
            Boolean.class, Byte.class, Double.class, Float.class, Integer.class, Long.class, Short.class, String.class });


    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = "destination1", containerFactory = "myFactory")
    public void receiveMessage(Email email, final @Header Map<String, Object> headers) {
        System.out.println("Received <" + email + ">");

        jmsTemplate.convertAndSend("destination2",
                new Email("info@example.com", "Hello-transformed"));
//                new MessagePostProcessor() {
//                    @Override
//                    public Message postProcessMessage(Message jmsMessage) throws JMSException {
//                        for (Map.Entry<String, Object> entry : headers.entrySet()) {
//                            String headerName = entry.getKey();
//                            if (StringUtils.hasText(headerName) && !headerName.startsWith(JmsHeaders.PREFIX)
//                                    && jmsMessage.getObjectProperty(headerName) == null) {
//                                Object value = entry.getValue();
//                                if (value != null) {
//                                    if (SUPPORTED_PROPERTY_TYPES.contains(value.getClass())) {
//                                        jmsMessage.setObjectProperty(headerName, value);
//                                    }
//                                }
//                            }
//                        }
//                        return jmsMessage;
//                    }
//                });
    }

}
