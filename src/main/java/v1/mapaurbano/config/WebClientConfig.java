package v1.mapaurbano.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;


@Configuration
public class WebClientConfig {

    @Value("${geoapi.url}")
    private String url;

    @Value("$geoapi.keyapi")
    private String keyApi;

    @Bean
    public ConnectionProvider connectionProvider(){
        return ConnectionProvider.builder("geoapi-pool")
                .maxConnections(30)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .build();
    }

    @Bean
    public WebClient webClient(ConnectionProvider provider){
         HttpClient httpClient = HttpClient.create(provider)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                 .responseTimeout(Duration.ofSeconds(5));


        return WebClient.builder()
                .baseUrl(url)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
