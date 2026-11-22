package com.gepapant.enersense.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

    @Bean
    public MqttPahoClientFactory mqttClientFactory(
        @Value("${enersense.mqtt.broker}")
        String broker
    ) {
        MqttConnectOptions options =
            new MqttConnectOptions();

        options.setServerURIs(
            new String[] { broker }
        );

        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        DefaultMqttPahoClientFactory factory =
            new DefaultMqttPahoClientFactory();

        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttInbound(
        MqttPahoClientFactory clientFactory,
        MessageChannel mqttInputChannel,

        @Value("${enersense.mqtt.client-id}")
        String clientId,

        @Value("${enersense.mqtt.topic}")
        String topic
    ) {
        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(
                clientId,
                clientFactory,
                topic
            );

        adapter.setCompletionTimeout(5000);

        adapter.setConverter(
            new DefaultPahoMessageConverter()
        );

        adapter.setQos(1);

        adapter.setOutputChannel(
            mqttInputChannel
        );

        return adapter;
    }
}