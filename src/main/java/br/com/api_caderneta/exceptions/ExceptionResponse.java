package br.com.api_caderneta.exceptions;

import java.util.Date;

/** Corpo padrão de erro: "code" e "message" são o contrato; timestamp/details mantêm a compatibilidade. */
public record ExceptionResponse(String code, String message, Date timestamp, String details) {
}
