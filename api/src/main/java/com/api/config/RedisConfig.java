package com.api.config;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.service.RedisSubscriber;
import com.api.domain.room.model.RoomAllowance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("chat");
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter, ChannelTopic topic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisTemplate<String, ChatMessage> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, ChatMessage> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessage.class));
        return template;
    }

    /**
     * RoomAllowance RedisTemplate을 위한 RedisConnectionFactory를 설정합니다.
     */
    @Bean
    public RedisTemplate<String, RoomAllowance> allowanceRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, RoomAllowance> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(RoomAllowance.class));
        template.setEnableTransactionSupport(true); // 트랜잭션 지원 활성화
        template.afterPropertiesSet(); // 초기화

        return template;
    }

    /**
     * String 기반 Redis 연산(카운터, 간단한 값 등)을 위해 StringRedisTemplate 빈을 추가합니다.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // Lua script: currentCapacity < maxCapacity 일 때만 증가시키고, 아니면 -1 반환
    private static final String LUA_SCRIPT =
            "local curr = redis.call('HGET', KEYS[1], 'currentCapacity')\n"
                    + "local max  = redis.call('HGET', KEYS[1], 'maxCapacity')\n"
                    + "curr = tonumber(curr) or 0\n"
                    + "max  = tonumber(max)  or 0\n"
                    + "if curr < max then\n"
                    + "  local after = redis.call('HINCRBY', KEYS[1], 'currentCapacity', ARGV[1])\n"
                    + "  return after\n"
                    + "else\n"
                    + "  return -1\n"
                    + "end";

    /**
     * RedisScript<Long> 빈 등록: Lua 스크립트 실행용
     */
    @Bean
    public RedisScript<Long> roomAllowanceScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_SCRIPT);
        script.setResultType(Long.class);
        return script;
    }
}