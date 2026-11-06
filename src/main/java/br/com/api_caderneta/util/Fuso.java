package br.com.api_caderneta.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Tempo do negócio. Instantes ({@code LocalDateTime}) são gravados em UTC; o que depende do "dia" do
 * comerciante (emissão, vencimento, pagamento, vencida) usa o calendário de Brasília. Sem isso, entre
 * 21h e 24h o servidor já estaria "no dia seguinte".
 */
public final class Fuso {
    public static final ZoneId NEGOCIO = ZoneId.of("America/Sao_Paulo");

    private Fuso() {}

    public static LocalDate hoje() { return LocalDate.now(NEGOCIO); }

    /** Instante atual como data/hora UTC (formato de armazenamento). */
    public static LocalDateTime agoraUtc() { return LocalDateTime.now(ZoneOffset.UTC); }

    /** Meia-noite do dia no fuso do negócio, convertida para UTC (limite inclusivo de relatórios). */
    public static LocalDateTime inicioDoDiaUtc(LocalDate dia) {
        return dia.atStartOfDay(NEGOCIO).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
