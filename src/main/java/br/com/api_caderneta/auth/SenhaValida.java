package br.com.api_caderneta.auth;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** O BCrypt limita bytes UTF-8, não a quantidade de caracteres da senha. */
@Target({FIELD, PARAMETER, RECORD_COMPONENT, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = SenhaValida.Validador.class)
public @interface SenhaValida {
  String message() default "deve ter pelo menos 8 caracteres e no máximo 72 bytes em UTF-8";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  class Validador implements ConstraintValidator<SenhaValida, String> {
    @Override
    public boolean isValid(String senha, ConstraintValidatorContext contexto) {
      return PoliticaSenha.aceita(senha);
    }
  }
}
