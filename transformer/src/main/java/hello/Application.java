
package hello;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import io.opentracing.Tracer;
import io.opentracing.contrib.jms.common.TracingMessageListener;
import io.opentracing.contrib.jms.spring.TracingJmsTemplate;
import io.opentracing.contrib.jms.spring.TracingMessageConverter;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@SpringBootApplication
@EnableJms
public class Application {

    @Bean
    public io.opentracing.Tracer jaegerTracer() {
        return new Configuration("scdf-simulation", new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
                new Configuration.ReporterConfiguration())
                .getTracer();
    }


    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory, io.opentracing.Tracer tracer,
                                   MessageConverter tracingJmsMessageConverter) {
        JmsTemplate jmsTemplate = new TracingJmsTemplate(connectionFactory, tracer);
        jmsTemplate.setMessageConverter(tracingJmsMessageConverter);
        jmsTemplate.setPubSubDomain(true);
        jmsTemplate.setDefaultDestinationName("destination2");
        return jmsTemplate;
    }

    @Bean
    public MessageConverter tracingJmsMessageConverter(Tracer tracer) {
        return new TracingMessageConverter(jacksonJmsMessageConverter(), tracer);
    }

    @Bean
    public MessageListener messageListener(Tracer tracer, JmsTemplate jmsTemplate) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
        EmailMessageHandler messageListener = new EmailMessageHandler(jmsTemplate);
        messageListenerAdapter.setDelegate(messageListener);
        messageListenerAdapter.setMessageConverter(jacksonJmsMessageConverter());
        TracingMessageListener tracingMessageListener = new TracingMessageListener(messageListenerAdapter, tracer);

        return tracingMessageListener;
    }

    @Bean
    public DefaultMessageListenerContainer jmsListenerContainer(ConnectionFactory connectionFactory, MessageListener messageListener) {
        DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
        dmlc.setConnectionFactory(connectionFactory);
        dmlc.setDestination(new ActiveMQTopic("destination1"));
        dmlc.setMessageConverter(jacksonJmsMessageConverter());

//        // To schedule our concurrent listening tasks
//        dmlc.setTaskExecutor(taskExecutor());

        // To perform actual message processing
        dmlc.setMessageListener(messageListener);
        dmlc.setSubscriptionDurable(true);
        dmlc.setClientId("scdf-transformer-client");
        dmlc.setConcurrentConsumers(1);

        // ... more parameters that you might want to inject ...
        return dmlc;
    }

    // TBD:  There is a bug using @JmsListener, the span is destroyed too early.

//    @Bean
//    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
//                                                    DefaultJmsListenerContainerFactoryConfigurer configurer,
//                                                    MessageConverter tracingJmsMessageConverter) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        //factory.setMessageConverter(tracingJmsMessageConverter);
//
//        // This provides all boot's default to this factory, including the message converter
//        configurer.configure(factory, connectionFactory);
//        // You could still override some of Boot's default if necessary.
//        factory.setPubSubDomain(true);
//        factory.setSubscriptionDurable(true);
//        factory.setClientId("scdf-transformer-client");
//        return factory;
//    }

    //@Bean // Serialize message content to json using TextMessage
    public static MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    public static void main(String[] args) {
        // Launch the application
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

    }

}
