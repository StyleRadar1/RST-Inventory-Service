package edu.dosw.styleradar.inventario.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Exchanges Oficiales
    public static final String EXCHANGE_EVENTS = "styleradar.exchange";
    public static final String EXCHANGE_DLX = "styleradar.dlx";

    // Colas con sufijo .q y DLQ
    public static final String QUEUE_DLQ = "styleradar.inventario.dlq";
    public static final String QUEUE_SUSCRIPCION = "styleradar.inventario.suscripcion-suspendida.q";
    public static final String QUEUE_PAGO_APROBADO = "styleradar.inventario.pago-aprobado.q";
    public static final String QUEUE_PAGO_RECHAZADO = "styleradar.inventario.pago-rechazado.q";

    // Routing keys publicadas
    public static final String ROUTING_KEY_ACTUALIZADO = "styleradar.inventario.actualizado";
    public static final String ROUTING_KEY_AGOTADO = "styleradar.inventario.stock.agotado";
    public static final String ROUTING_KEY_ULTIMAS_UNIDADES = "styleradar.inventario.stock.ultimas-unidades";

    // Routing keys consumidas
    public static final String ROUTING_KEY_SUSPENDIDA = "tienda.suscripcion.suspendida";
    public static final String ROUTING_KEY_SUSPENDIDA_ALT = "styleradar.tienda.suscripcion.suspendida";
    public static final String ROUTING_KEY_PAGO_APROBADO = "orden.pago.aprobado";
    public static final String ROUTING_KEY_PAGO_APROBADO_ALT = "styleradar.orden.pago.aprobado";
    public static final String ROUTING_KEY_PAGO_RECHAZADO = "orden.pago.rechazado";
    public static final String ROUTING_KEY_PAGO_RECHAZADO_ALT = "styleradar.orden.pago.rechazado";

    // Exchanges
    @Bean
    public TopicExchange styleradarExchange() {
        return new TopicExchange(EXCHANGE_EVENTS, true, false);
    }

    @Bean
    public TopicExchange styleradarDlx() {
        return new TopicExchange(EXCHANGE_DLX, true, false);
    }

    // Dead Letter Queue
    @Bean
    public Queue inventarioDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding bindingDlq(Queue inventarioDlq, TopicExchange styleradarDlx) {
        return BindingBuilder.bind(inventarioDlq).to(styleradarDlx).with("#");
    }

    // Colas de Negocio con DLX configurado
    @Bean
    public Queue suscripcionQueue() {
        return QueueBuilder.durable(QUEUE_SUSCRIPCION)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue pagoAprobadoQueue() {
        return QueueBuilder.durable(QUEUE_PAGO_APROBADO)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue pagoRechazadoQueue() {
        return QueueBuilder.durable(QUEUE_PAGO_RECHAZADO)
                .deadLetterExchange(EXCHANGE_DLX)
                .deadLetterRoutingKey(QUEUE_DLQ)
                .build();
    }

    // Bindings hacia el exchange principal
    @Bean
    public Binding bindingSuscripcion(Queue suscripcionQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(suscripcionQueue).to(styleradarExchange).with(ROUTING_KEY_SUSPENDIDA);
    }

    @Bean
    public Binding bindingSuscripcionAlt(Queue suscripcionQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(suscripcionQueue).to(styleradarExchange).with(ROUTING_KEY_SUSPENDIDA_ALT);
    }

    @Bean
    public Binding bindingPagoAprobado(Queue pagoAprobadoQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(pagoAprobadoQueue).to(styleradarExchange).with(ROUTING_KEY_PAGO_APROBADO);
    }

    @Bean
    public Binding bindingPagoAprobadoAlt(Queue pagoAprobadoQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(pagoAprobadoQueue).to(styleradarExchange).with(ROUTING_KEY_PAGO_APROBADO_ALT);
    }

    @Bean
    public Binding bindingPagoRechazado(Queue pagoRechazadoQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(pagoRechazadoQueue).to(styleradarExchange).with(ROUTING_KEY_PAGO_RECHAZADO);
    }

    @Bean
    public Binding bindingPagoRechazadoAlt(Queue pagoRechazadoQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(pagoRechazadoQueue).to(styleradarExchange).with(ROUTING_KEY_PAGO_RECHAZADO_ALT);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
