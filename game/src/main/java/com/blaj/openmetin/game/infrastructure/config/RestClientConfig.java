package com.blaj.openmetin.game.infrastructure.config;

import com.blaj.openmetin.game.infrastructure.properties.RestClientProperties;
import com.blaj.openmetin.game.infrastructure.properties.TrustStoreProperties;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  private final RestClientProperties restClientProperties;
  private final TrustStoreProperties trustStoreProperties;
  private final ResourceLoader resourceLoader;

  @Bean
  @Qualifier("authenticationRestClient")
  public RestClient authenticationRestClient()
      throws KeyStoreException,
          IOException,
          CertificateException,
          NoSuchAlgorithmException,
          KeyManagementException {
    var truststore = KeyStore.getInstance("JKS");
    var resource = resourceLoader.getResource(trustStoreProperties.path());

    try (var truststoreStream = resource.getInputStream()) {
      truststore.load(truststoreStream, trustStoreProperties.password().toCharArray());
    }

    var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(truststore);

    var sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

    var httpClient = HttpClient.newBuilder().sslContext(sslContext).build();

    return RestClient.builder()
        .requestFactory(new JdkClientHttpRequestFactory(httpClient))
        .baseUrl(restClientProperties.authenticationUrl())
        .requestInterceptor(
            (request, body, execution) -> {
              request
                  .getHeaders()
                  .setBasicAuth(
                      restClientProperties.authenticationBasicAuthUsername(),
                      restClientProperties.authenticationBasicAuthPassword());

              return execution.execute(request, body);
            })
        .build();
  }
}
