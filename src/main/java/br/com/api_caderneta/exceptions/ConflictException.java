package br.com.api_caderneta.exceptions;

/** Conflito de negócio (login em uso, conta já ativada, exclusão incompatível...). Responde 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
