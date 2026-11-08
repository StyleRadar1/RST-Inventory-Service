package edu.dosw.styleradar.inventario.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_EVENTS = "styleradar.events";
    public static final String QUEUE_SUSCRIPCION = "q.inventario.suscripcion";
    public static final String QUEUE_PAGO_APROBADO = "q.inventario.pago-aprobado";
    public static final String QUEUE_PAGO_RECHAZADO = "q.inventario.pago-rechazado";

    public static final String ROUTING_KEY_SUSPENDIDA = "tienda.suscripcion.suspendida";
    public static final String ROUTING_KEY_PAGO_APROBADO = "orden.pago.aprobado";
    public static final String ROUTING_KEY_PAGO_RECHAZADO = "orden.pago.rechazado";

    @Bean
    public TopicExchange styleradarEventsExchange() {
        return new TopicExchange(EXCHANGE_EVENTS, true, false);
    }

    @Bean
    public Queue suscripcionQueue() {
        return QueueBuilder.durable(QUEUE_SUSCRIPCION).build();
    }

    @Bean
    public Queue pagoAprobadoQueue() {
        return QueueBuilder.durable(QUEUE_PAGO_APROBADO).build();
    }

    @Bean
    public Queue pagoRechazadoQueue() {
        return QueueBuilder.durable(QUEUE_PAGO_RECHAZADO).build();
    }

    @Bean
    public Binding bindingSuscripcion(Queue suscripcionQueue, TopicExchange styleradarEventsExchange) {
        return BindingBuilder.bind(suscripcionQueue).to(styleradarEventsExchange).with(ROUTING_KEY_SUSPENDIDA);
    }

    @Bean
    public Binding bindingPagoAprobado(Queue pagoAprobadoQueue, TopicExchange styleradarEventsExchange) {
        return BindingBuilder.bind(pagoAprobadoQueue).to(styleradarEventsExchange).with(ROUTING_KEY_PAGO_APROBADO);
    }

    @Bean
    public Binding bindingPagoRechazado(Queue pagoRechazadoQueue, TopicExchange styleradarEventsExchange) {
        return BindingBuilder.bind(pagoRechazadoQueue).to(styleradarEventsExchange).with(ROUTING_KEY_PAGO_RECHAZADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
