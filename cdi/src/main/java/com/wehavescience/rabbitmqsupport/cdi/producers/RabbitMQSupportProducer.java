package com.wehavescience.rabbitmqsupport.cdi.producers;

import com.wehavescience.rabbitmqsupport.cdi.RabbitMQConsumerSupportCDI;
import com.wehavescience.rabbitmqsupport.cdi.RabbitMQProducerSupportCDI;
import com.wehavescience.rabbitmqsupport.cdi.annotations.Consumers;
import com.wehavescience.rabbitmqsupport.cdi.annotations.RabbitMQContext;
import com.wehavescience.rabbitmqsupport.configurations.RabbitMQConfiguration;
import com.wehavescience.rabbitmqsupport.consumer.annotations.RabbitMQConsumer;
import org.reflections.Reflections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Gabriel Francisco  <gabfssilva@gmail.com>
 */
public class RabbitMQSupportProducer {
    @Produces
    @Consumers
    public List<Object> consumerList(BeanManager beanManager, InjectionPoint injectionPoint) throws IllegalAccessException, InstantiationException {
        Reflections reflections = new Reflections(injectionPoint.getAnnotated().getAnnotation(Consumers.class).packagePrefixScan());
        Set<Class<? extends Object>> subTypesOf = (Set) reflections.getTypesAnnotatedWith(RabbitMQConsumer.class);
        List<Object> consumers = new LinkedList<Object>();

        for (Class<? extends Object> clazz : subTypesOf) {
            Set<Bean<?>> beans = beanManager.getBeans(clazz);
            Bean<?> bean = beans.iterator().next();
            CreationalContext<? extends Object> ctx = (CreationalContext<? extends Object>) beanManager.createCreationalContext(bean);
            Object consumer = (Object) beanManager.getReference(bean, clazz, ctx);
            consumers.add(consumer);
        }

        return consumers;
    }

    @Produces
    @RabbitMQContext
    public RabbitMQConsumerSupportCDI rabbitMQConsumerSupportCDI(InjectionPoint injectionPoint, @Consumers List<Object> listeners) throws IOException {
        RabbitMQContext rabbitMQContext = injectionPoint.getAnnotated().getAnnotation(RabbitMQContext.class);
        RabbitMQConfiguration configuration = new RabbitMQConfiguration(rabbitMQContext.username(), rabbitMQContext.password(), rabbitMQContext.virtualhost(), rabbitMQContext.urls());
        return new RabbitMQConsumerSupportCDI(configuration, listeners);
    }

    @Produces
    @RabbitMQContext
    public RabbitMQProducerSupportCDI rabbitMQProducerSupportCDI(InjectionPoint injectionPoint) throws IOException {
        RabbitMQContext rabbitMQContext = injectionPoint.getAnnotated().getAnnotation(RabbitMQContext.class);
        RabbitMQConfiguration configuration = new RabbitMQConfiguration(rabbitMQContext.username(), rabbitMQContext.password(), rabbitMQContext.virtualhost(), rabbitMQContext.urls());
        return new RabbitMQProducerSupportCDI(configuration);
    }
}
