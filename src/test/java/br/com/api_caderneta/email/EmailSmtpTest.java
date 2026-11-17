package br.com.api_caderneta.email;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;

class EmailSmtpTest {
  @Test
  void adaptadorEntregaMensagemEmServidorSmtpLocal() throws Exception {
    try (var server = new ServerSocket(0); var executor = Executors.newSingleThreadExecutor()) {
      var recebido = executor.submit(() -> {
        try (var socket = server.accept()) {
          socket.setSoTimeout(10000);
          var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          var out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
          out.print("220 localhost teste\r\n"); out.flush();
          var dados = new StringBuilder(); boolean corpo = false;
          for (String linha; (linha = in.readLine()) != null;) {
            if (corpo) {
              if (linha.equals(".")) { corpo = false; out.print("250 recebido\r\n"); }
              else dados.append(linha).append('\n');
            } else if (linha.startsWith("EHLO")) out.print("250 localhost\r\n");
            else if (linha.equals("DATA")) { corpo = true; out.print("354 envie\r\n"); }
            else if (linha.equals("QUIT")) { out.print("221 fim\r\n"); out.flush(); break; }
            else out.print("250 ok\r\n");
            out.flush();
          }
          return dados.toString();
        }
      });
      new EmailSmtp("127.0.0.1", server.getLocalPort(), "", "", "caderneta@example.test", false)
          .enviar("loja@example.test", "Confirmacao de teste", "Link: http://localhost/confirmar-email#loja=1&token=teste");
      assertThat(recebido.get(10, TimeUnit.SECONDS)).contains("To: loja@example.test", "confirmar-email#loja=1&token=teste");
    }
  }
}
