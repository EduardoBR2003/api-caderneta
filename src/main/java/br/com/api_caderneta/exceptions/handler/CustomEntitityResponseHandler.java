package br.com.api_caderneta.exceptions.handler;

import br.com.api_caderneta.exceptions.BusinessException;
import br.com.api_caderneta.exceptions.ConflictException;
import br.com.api_caderneta.exceptions.ExceptionResponse;
import br.com.api_caderneta.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomEntitityResponseHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomEntitityResponseHandler.class);

    private static ResponseEntity<ExceptionResponse> resposta(HttpStatus status, String code, String msg, WebRequest req) {
        return new ResponseEntity<>(new ExceptionResponse(code, msg, new Date(), req.getDescription(false)), status);
    }

    /** Exceções de segurança seguem para os handlers dos filtros (401/403 JSON). */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public final void repassarSeguranca(RuntimeException ex) {
        throw ex;
    }

    @ExceptionHandler(br.com.api_caderneta.auth.LimiteTentativas.Bloqueado.class)
    public final ResponseEntity<ExceptionResponse> handleBloqueado(
            br.com.api_caderneta.auth.LimiteTentativas.Bloqueado ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.segundos()))
                .body(new ExceptionResponse("MUITAS_TENTATIVAS", ex.getMessage(), new Date(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Recurso não encontrado: {} - URL: {}", ex.getMessage(), request.getDescription(false));
        return resposta(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public final ResponseEntity<ExceptionResponse> handleBusiness(BusinessException ex, WebRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "REGRA_DE_NEGOCIO", ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<ExceptionResponse> handleConflict(ConflictException ex, WebRequest request) {
        return resposta(HttpStatus.CONFLICT, "CONFLITO", ex.getMessage(), request);
    }

    /** Chave estrangeira, valor único etc.: o registro está em uso. Nunca expõe o SQL ao cliente. */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public final ResponseEntity<ExceptionResponse> handleIntegridade(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        // A mensagem do banco pode conter o valor duplicado (ex.: CPF): registra só o nome da constraint.
        Throwable c = ex;
        String constraint = null;
        while (c != null && constraint == null) {
            if (c instanceof org.hibernate.exception.ConstraintViolationException h) constraint = h.getConstraintName();
            c = c.getCause();
        }
        logger.warn("Violação de integridade (constraint: {}, causa: {})",
                constraint != null ? constraint : "desconhecida", ex.getMostSpecificCause().getClass().getSimpleName());
        return resposta(HttpStatus.CONFLICT, "CONFLITO",
                "Não foi possível concluir: o registro está vinculado a outros dados (conta de acesso, vendas, "
                        + "dívidas) ou já existe. Desative a conta em vez de excluir, se for o caso.", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public final ResponseEntity<ExceptionResponse> handleStatus(ResponseStatusException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return resposta(status, status.name(), ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(), request);
    }

    /** Nunca devolve detalhes internos ao cliente; o detalhe fica só no log. */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAll(Exception ex, WebRequest request) {
        logger.error("Exceção interna não tratada: {}", ex.getMessage(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Erro interno. Tente novamente mais tarde.", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map((FieldError e) -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(new ExceptionResponse("VALIDACAO", msg, new Date(), request.getDescription(false)),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (body == null) {
            body = new ExceptionResponse("REQUISICAO_INVALIDA", "Requisição inválida.", new Date(),
                    request.getDescription(false));
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}
