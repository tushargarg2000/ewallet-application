package com.example.majorproject;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.Properties;

@Configuration
public class Config {

    @Bean
    Properties kafkaProps(){

        Properties properties = new Properties();
        //Producer properties
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        //Consumer Properties
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"test1234");

        return properties;
    }

    @Bean
    ProducerFactory<String, String> getProducerFactory(){
        return new DefaultKafkaProducerFactory(kafkaProps());
    }


    @Bean
    ConsumerFactory<String,String> getConsumerFactory(){
        return new DefaultKafkaConsumerFactory(kafkaProps());
    }


    //This is only for consumers bcz they have to listen simultaneous...so this property needs to be there
    @Bean
    ConcurrentKafkaListenerContainerFactory<String,String> concurrentKafkaListenerContainerFactory(){

        ConcurrentKafkaListenerContainerFactory concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(getConsumerFactory());
        return concurrentKafkaListenerContainerFactory;
    }

    @Bean
    KafkaTemplate<String, String> getKafkaTemplate(){
        return new KafkaTemplate(getProducerFactory());
    }


    @Bean
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }
}
