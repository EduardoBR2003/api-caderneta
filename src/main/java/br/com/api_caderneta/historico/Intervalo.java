package br.com.api_caderneta.historico;

import br.com.api_caderneta.exceptions.BusinessException;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Intervalo [inicio, fim) em instantes; as colunas são comparadas em UTC. */
public record Intervalo(Instant inicio, Instant fim) {

    public static Intervalo de(String inicio, String fim) {
        Instant i = parse("inicio", inicio), f = parse("fim", fim);
        if (i != null && f != null && !i.isBefore(f)) {
            throw new BusinessException("O intervalo é inválido: 'inicio' deve ser anterior a 'fim'.");
        }
        return new Intervalo(i, f);
    }

    private static Instant parse(String nome, String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            // '+' de offset chega como espaço quando o cliente não codifica a URL.
            return OffsetDateTime.parse(valor.trim().replace(' ', '+')).toInstant();
        } catch (DateTimeException e) {
            throw new BusinessException("'" + nome + "' deve ser um instante ISO 8601 com offset (ex.: 2026-11-05T00:00:00Z).");
        }
    }

    public LocalDateTime inicioUtc() { return inicio == null ? null : LocalDateTime.ofInstant(inicio, ZoneOffset.UTC); }
    public LocalDateTime fimUtc() { return fim == null ? null : LocalDateTime.ofInstant(fim, ZoneOffset.UTC); }
}
