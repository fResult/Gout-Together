package dev.fResult.goutTogether.common.configs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
  @Bean
  public BufferedImageHttpMessageConverter bufferedImageHttpMessageConverter() {
    return new BufferedImageHttpMessageConverter();
  }

  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    final var imageConverter = bufferedImageHttpMessageConverter();

    builder.configureMessageConvertersList(apiPayloadFormatConfigurerFor(imageConverter));
  }

  private Consumer<List<HttpMessageConverter<?>>> apiPayloadFormatConfigurerFor(
      HttpMessageConverter<?>... convertersToAdd) {

    return converters -> converters.addAll(Arrays.asList(convertersToAdd));
  }
}
