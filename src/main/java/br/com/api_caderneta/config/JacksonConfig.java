package br.com.api_caderneta.config;

import br.com.api_caderneta.util.Fuso;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Padroniza instantes na API: os {@code LocalDateTime} (gravados em UTC) saem com "Z", então o navegador
 * converte para o horário local do usuário. Na entrada, aceita offset explícito; sem offset, a data/hora é
 * entendida no fuso do negócio (é o que o formulário de edição envia).
 */
@Configuration
public class JacksonConfig {

    @Bean
    SimpleModule instantesEmUtc() {
        var m = new SimpleModule("instantes-utc");
        m.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime v, JsonGenerator g, SerializerProvider p) throws IOException {
                g.writeString(v.atOffset(ZoneOffset.UTC).toInstant().toString());
            }
        });
        m.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext c) throws IOException {
                String s = p.getValueAsString();
                if (s == null || s.isBlank()) return null;
                try {
                    return OffsetDateTime.parse(s).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
                } catch (DateTimeParseException semOffset) {
                    try {
                        return LocalDateTime.parse(s).atZone(Fuso.NEGOCIO)
                                .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                    } catch (DateTimeParseException e) {
                        return (LocalDateTime) c.handleWeirdStringValue(LocalDateTime.class, s,
                                "data/hora inválida (use ISO 8601)");
                    }
                }
            }
        });
        return m;
    }
}
