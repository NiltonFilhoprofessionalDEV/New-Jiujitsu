package br.com.academia.api.config;

import br.com.academia.api.access.SuspendedAcademyGuard;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final SuspendedAcademyGuard suspendedAcademyGuard;

	public WebConfig(SuspendedAcademyGuard suspendedAcademyGuard) {
		this.suspendedAcademyGuard = suspendedAcademyGuard;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(suspendedAcademyGuard);
	}
}
