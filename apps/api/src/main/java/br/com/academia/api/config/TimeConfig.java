package br.com.academia.api.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

	@Bean
	Clock clock(AppProperties props) {
		return Clock.system(props.timeZone());
	}
}
