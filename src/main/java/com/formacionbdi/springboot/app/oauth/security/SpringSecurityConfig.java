package com.formacionbdi.springboot.app.oauth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private UserDetailsService usuarioService;
	
	@Autowired
	private AuthenticationEventPublisher eventPublisher;
	
	//creando el objeto bcrypter para que se registre en el contenedor de spring
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	//para registrar el usuario usuarioService en el authenticationManager hay que sobreescribir
	//el metodo configure y anotarlo con autowired para que se pueda inyectar mediante el metodo
	@Override
	@Autowired
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		//con el objeto auth se registra el usuarioService, ademas se utiliza bcrypt para dar mayor
		//seguridad a la contraseña del usuario, para esto utilizamos el metodo passwordEncoder
		//se necesita crear el beans passwordEncoder mediante un metodo
		//para poder encryptar la contraseña del usuario
		auth.userDetailsService(this.usuarioService).passwordEncoder(passwordEncoder())
		.and().authenticationEventPublisher(eventPublisher);
	}

	//ademas se necesita registrar el authenticationManager, para que despues lo podamos utilizar
	// en la configuracion del servidor de autorizacion de oauth2, sobreescribimos el metodo 
	// y agregamos la anotacion beans para que se registre como un objeto en el contenedor de spring
	@Override
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	
	

}
