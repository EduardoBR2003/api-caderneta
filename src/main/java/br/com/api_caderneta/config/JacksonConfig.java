package br.com.api_caderneta.config;

import br.com.api_caderneta.util.Fuso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

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
        m.addSerializer(LocalDateTime.class, new ValueSerializer<>() {
            @Override
            public void serialize(LocalDateTime v, JsonGenerator g, SerializationContext c) {
                g.writeString(v.atOffset(ZoneOffset.UTC).toInstant().toString());
            }
        });
        m.addDeserializer(LocalDateTime.class, new ValueDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext c) {
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
