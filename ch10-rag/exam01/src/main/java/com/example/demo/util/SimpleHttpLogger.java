package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

@Configuration
public class SimpleHttpLogger implements ClientHttpRequestInterceptor {
  //@Bean
  public RestClient.Builder restClientBuilder (RestClientBuilderConfigurer restClientBuilderConfigurer) {
    RestClient.Builder builder = RestClient.builder().requestInterceptor(new SimpleHttpLogger());
    return restClientBuilderConfigurer.configure(builder);
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    logRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    CachedClientHttpResponseWrapper responseWrapper = new CachedClientHttpResponseWrapper(response);
    logResponse(responseWrapper);
    return responseWrapper;
  }

  static void logRequest (HttpRequest request, byte[] bytes) throws UnsupportedEncodingException {
    System.out.println("\n[요청 HTTP 시작]>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println(request.getMethod() + " " + request.getURI());
    System.out.println(request.getHeaders().toString());
    System.out.println("");
    System.out.println(new String(bytes, "UTF-8").replace("\\r\\n", System.lineSeparator()));
    System.out.println("[요청 HTTP 맨끝]>>>>>>>>>>>>>>>>>>>>>>>\n");
  }

  static void logResponse (CachedClientHttpResponseWrapper response) throws IOException {
    System.out.println("\n[응답 HTTP 시작]<<<<<<<<<<<<<<<<<<<<<<<");
    System.out.println(response.getStatusCode());
    System.out.println(response.getHeaders().toString());
    System.out.println("");
    System.out.println(new String(response.cachedBody, "UTF-8").substring(0, 500));
    System.out.println("[응답 HTTP 맨끝]<<<<<<<<<<<<<<<<<<<<<<<\n");
  }

  static class CachedClientHttpResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse originalResponse;
    final byte[] cachedBody;
  
    public CachedClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
      this.originalResponse = response;
      InputStream responseBodyStream = response.getBody();
      this.cachedBody = responseBodyStream.readAllBytes();
    }
  
    @Override
    public InputStream getBody() {
      return new ByteArrayInputStream(cachedBody);
    }
  
    @Override
    public HttpStatusCode getStatusCode() throws IOException {
      return originalResponse.getStatusCode();
    }
  
    @Override
    public int getRawStatusCode() throws IOException {
      return originalResponse.getStatusCode().value();
    }
  
    @Override
    public String getStatusText() throws IOException {
      return originalResponse.getStatusText();
    }
  
    @Override
    public void close() {
      originalResponse.close();
    }
  
    @Override
    public HttpHeaders getHeaders() {
      return originalResponse.getHeaders();
    }
  }
}
