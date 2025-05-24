package br.com.api_caderneta.exceptions;

import java.util.Date;

public record ExceptionResponse(Date timestamp, String message, String details) {


}
