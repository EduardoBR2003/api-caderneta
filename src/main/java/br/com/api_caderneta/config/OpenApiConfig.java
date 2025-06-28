package br.com.api_caderneta.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Caderneta - Gestão de Vendas a Prazo",
                version = "1.0",
                description = "API RESTful para o sistema de gestão de vendas a prazo (fiado), " +
                        "permitindo o controle completo de clientes, dívidas e pagamentos.",
                contact = @Contact(
                        name = "Equipe de Desenvolvimento",
                        email = "contato@suaempresa.com",
                        url = "https://suaempresa.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        )
)
public class OpenApiConfig {

}