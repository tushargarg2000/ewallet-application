import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;


import java.util.Properties;

@Configuration
public class Config {




    @Bean
    LettuceConnectionFactory getConnectionFactory(){

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;

    }


    @Bean
    RedisTemplate getRedisTemplate(){

        RedisTemplate<String,User> redisTemplate = new RedisTemplate<>();

        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();

        redisTemplate.setKeySerializer(stringRedisSerializer);

        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();

        redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);

        redisTemplate.setConnectionFactory(getConnectionFactory());

        return redisTemplate;
    }

    @Bean
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }

    //Properties

    @Bean
    Properties kafkaProps(){

        Properties properties = new Properties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");

        return properties;
    }

    //ProducerFactory
    @Bean
    ProducerFactory<String,String> getProducerFactory(){

        return new DefaultKafkaProducerFactory<>();
    }

    //KafkaTemplate
    @Bean
    KafkaTemplate<String,String> getKafkaTemplate(){
        return new KafkaTemplate<>(getProducerFactory());
    }



}
