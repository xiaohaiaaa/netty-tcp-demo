package org.example.client.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class KeyStringRedisSerializer implements RedisSerializer<String> {

    private final Charset charset;

    private final String cachePrefix;

    public KeyStringRedisSerializer(String cachePrefix) {
        this(StandardCharsets.UTF_8, cachePrefix);
    }

    public KeyStringRedisSerializer(Charset charset, String cachePrefix) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
        this.cachePrefix = cachePrefix + ":";
    }

    @Override
    public String deserialize(@Nullable byte[] bytes) {
        return bytes == null ? null : new String(bytes, this.charset).replaceFirst(cachePrefix, "");
    }

    @Override
    public byte[] serialize(@Nullable String string) {
        return string == null ? null : (cachePrefix + string).getBytes(this.charset);
    }

    @Override
    public Class<?> getTargetType() {
        return String.class;
    }

}