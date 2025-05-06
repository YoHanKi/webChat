package com.api.config;

import com.api.domain.chat.model.ChatMessage;
import com.api.domain.chat.redis.service.RedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

import java.time.Duration;

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

    @Bean
    public RedisTemplate<String, String> allowanceRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        StringRedisSerializer stringSer = new StringRedisSerializer();
        template.setKeySerializer(stringSer);          // key
        template.setValueSerializer(stringSer);       // value
        template.setHashKeySerializer(stringSer);      // hash key
        template.setHashValueSerializer(stringSer);    // hash value

        template.setEnableTransactionSupport(false);
        template.afterPropertiesSet();
        return template;
    }

    // Lua script: currentCapacity < maxCapacity 일 때만 증가시키고, 아니면 -1 반환
    private static final String LUA_SCRIPT =
            // currentCapacity, maxCapacity을 숫자(또는 기본 0)로 파싱
            "local curr = tonumber(redis.call('HGET', KEYS[1], 'currentCapacity') or '0')\n"
                    + "local max  = tonumber(redis.call('HGET', KEYS[1], 'maxCapacity')     or '0')\n"
                    // ARGV[1]이 없거나 잘못된 값이면 0
                    + "local inc  = tonumber(ARGV[1]) or 0\n"
                    + "if curr < max then\n"
                    + "  local after = curr + inc\n"
                    // 항상 순수 숫자 문자열로 덮어쓰기
                    + "  redis.call('HSET', KEYS[1], 'currentCapacity', tostring(after))\n"
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

    /**
     * 문자열 키·값 연산용 템플릿.
     * Set, Stream, String 값 INCR/DECR 등에 사용합니다.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    /**
     * Redis Streams 컨슈머를 사용하려면 StreamMessageListenerContainer 빈 추가.
     */
    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, ChatMessage>> streamListenerContainer(RedisConnectionFactory cf) {
        StreamMessageListenerContainerOptions<String, ObjectRecord<String, ChatMessage>> opts =
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .targetType(ChatMessage.class) // ObjectRecord<String, ChatMessage>
                        .build();

        // 3) 컨테이너 생성
        return StreamMessageListenerContainer.create(cf, opts);
    }

    /**
     * 순수 String 키·값 연산용 RedisTemplate.
     * WebSocket 시그널링, 간단 Pub/Sub, Stream 중계 등에 사용합니다.
     */
    @Bean
    public RedisTemplate<String, String> redisStreamingTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);

        StringRedisSerializer serializer = new StringRedisSerializer();
        // 문자열 그대로 저장/조회
        tpl.setKeySerializer(serializer);
        tpl.setValueSerializer(serializer);
        tpl.setHashKeySerializer(serializer);
        tpl.setHashValueSerializer(serializer);

        tpl.setEnableTransactionSupport(false);
        tpl.afterPropertiesSet();
        return tpl;
    }
}