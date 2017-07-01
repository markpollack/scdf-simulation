
package hello;

import javax.jms.ConnectionFactory;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import io.opentracing.Tracer;
import io.opentracing.contrib.jms.spring.TracingJmsTemplate;
import io.opentracing.contrib.jms.spring.TracingMessageConverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@SpringBootApplication
@EnableJms
public class Application {

	public static MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

	public static void main(String[] args) {
		// Launch the application
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

		JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

		// Send a message with a POJO - the template reuse the message converter
		System.out.println("Sending an email message.");
		jmsTemplate.convertAndSend("destination1", new Email("info@example.com", "Hello"));
	}

	@Bean
	public io.opentracing.Tracer jaegerTracer() {
		return new Configuration("scdf-simulation",
				new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
				new Configuration.ReporterConfiguration())
						.getTracer();
	}

	@Bean
	public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, io.opentracing.Tracer tracer,
			MessageConverter tracingJmsMessageConverter) {
		JmsTemplate jmsTemplate = new TracingJmsTemplate(connectionFactory, tracer);
		jmsTemplate.setMessageConverter(tracingJmsMessageConverter);
		jmsTemplate.setPubSubDomain(true);
		jmsTemplate.setDefaultDestinationName("destination1");
		return jmsTemplate;
	}

	@Bean
	public MessageConverter tracingJmsMessageConverter(Tracer tracer) {
		return new TracingMessageConverter(jacksonJmsMessageConverter(), tracer);
	}

}
