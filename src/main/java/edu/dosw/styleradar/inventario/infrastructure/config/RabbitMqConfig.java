package edu.dosw.styleradar.inventario.infrastructure.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

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

    // Routing keys publicadas por ms-inventario
    public static final String ROUTING_KEY_ACTUALIZADO = "styleradar.inventario.actualizado";
    public static final String ROUTING_KEY_AGOTADO = "styleradar.inventario.stock.agotado";
    public static final String ROUTING_KEY_ULTIMAS_UNIDADES = "styleradar.inventario.stock.ultimas-unidades";

    // Routing keys reales publicadas por ms-pagos (NQS) - Hallazgo H19 (RES-139)
    public static final String ROUTING_KEY_SUSCRIPCION_SUSPENDIDA = "styleradar.pagos.suscripcion.suspendida";
    public static final String ROUTING_KEY_PAGO_CONFIRMADO = "styleradar.pagos.pago.confirmado";
    // NOTA RES-139: Pendiente confirmar con NQS (ms-pagos) si publicará un evento de pago rechazado;
    // se mantiene la cola declarada sin enlaces activos hacia claves no publicadas.
    public static final String ROUTING_KEY_PAGO_RECHAZADO_PENDIENTE = "styleradar.pagos.pago.rechazado";

    @Bean
    public TopicExchange styleradarExchange() {
        return new TopicExchange(EXCHANGE_EVENTS, true, false);
    }

    @Bean
    public DirectExchange styleradarDlx() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    @Bean
    public Queue inventarioDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding bindingDlq(Queue inventarioDlq, DirectExchange styleradarDlx) {
        return BindingBuilder.bind(inventarioDlq).to(styleradarDlx).with(QUEUE_DLQ);
    }

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

    @Bean
    public Binding bindingSuscripcion(Queue suscripcionQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(suscripcionQueue)
                .to(styleradarExchange)
                .with(ROUTING_KEY_SUSCRIPCION_SUSPENDIDA);
    }

    @Bean
    public Binding bindingPagoConfirmado(Queue pagoAprobadoQueue, TopicExchange styleradarExchange) {
        return BindingBuilder.bind(pagoAprobadoQueue)
                .to(styleradarExchange)
                .with(ROUTING_KEY_PAGO_CONFIRMADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        ObjectMapper rabbitMapper = objectMapper.copy();
        rabbitMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper() {
            @Override
            public void fromJavaType(JavaType javaType, MessageProperties properties) {
                // No emitir cabecera __TypeId__
            }

            @Override
            public JavaType toJavaType(MessageProperties properties) {
                Type inferredType = properties.getInferredArgumentType();
                if (inferredType != null) {
                    JavaType inferred = TypeFactory.defaultInstance().constructType(inferredType);
                    if (!inferred.isAbstract() && (!inferred.isInterface()
                            || inferred.getRawClass().getPackageName().startsWith("java.util"))) {
                        return inferred;
                    }
                }
                return TypeFactory.defaultInstance().constructMapType(
                        LinkedHashMap.class, String.class, Object.class);
            }
        };
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.addTrustedPackages("*");

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(rabbitMapper);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitRetryTemplateCustomizer rabbitRetryTemplateCustomizer(
            @Value("${spring.rabbitmq.listener.simple.retry.max-attempts:3}") int maxAttempts) {
        Map<Class<? extends Throwable>, Boolean> nonRetryable = Map.of(
                AmqpRejectAndDontRequeueException.class, false,
                MessageConversionException.class, false,
                org.springframework.messaging.converter.MessageConversionException.class, false,
                MethodArgumentResolutionException.class, false
        );
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, nonRetryable, true, true);
        return (target, retryTemplate) -> {
            if (target == RabbitRetryTemplateCustomizer.Target.LISTENER) {
                retryTemplate.setRetryPolicy(retryPolicy);
            }
        };
    }
}
