package br.com.api_caderneta.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Escreve o erro padrão {"code","message"} fora do MVC (filtros de segurança). */
final class ApiErrors {
    private ApiErrors() {}

    static void write(ObjectMapper mapper, HttpServletResponse res, int status, String code, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.setHeader("Cache-Control", "no-store");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        mapper.writeValue(res.getOutputStream(), body);
    }
}
