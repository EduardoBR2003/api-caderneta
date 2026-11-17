package br.com.api_caderneta.util;
import jakarta.validation.*;
import java.lang.annotation.*;
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelefoneValido.Validador.class)
public @interface TelefoneValido {
 String message() default "Informe um telefone válido com DDD.";
 Class<?>[] groups() default {};
 Class<? extends Payload>[] payload() default {};
 class Validador implements ConstraintValidator<TelefoneValido,String> {
  public boolean isValid(String v, ConstraintValidatorContext ctx) {
   return v == null || v.isBlank() || v.matches("[0-9()+.\\s-]+") && v.replaceAll("\\D", "").matches("[1-9]{2}(?:9[0-9]{8}|[2-5][0-9]{7})");
  }
 }
}
