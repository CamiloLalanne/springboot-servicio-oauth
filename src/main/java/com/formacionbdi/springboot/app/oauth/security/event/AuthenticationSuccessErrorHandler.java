package com.formacionbdi.springboot.app.oauth.security.event;

 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.oauth.services.IUsuarioService;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

//esta clase es creada con el objetido de manejar los mensajes de error y exito del servicio oauth
//primero que nada anotamos como component e implementamos la clase authenticationEventPublisher
@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {

	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	//para implementar evento de tres intentos de login, se debe inyectar la interfaz IUsuarioService
	//de esta manera podemos llamar al metodo update para incrementar la cantidad de intentos fallidos
	@Autowired
	private IUsuarioService uService;
	
	//metodo cuando el usuario se logea correctamente
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		UserDetails user = (UserDetails) authentication.getPrincipal();
		Usuario usuarioLogeado= uService.findByUsername(user.getUsername());
		if(usuarioLogeado.getIntentos()>0) {
			usuarioLogeado.setIntentos(0);
 			uService.update(usuarioLogeado, usuarioLogeado.getId());
		}
		log.info("Success Login "+ user.getUsername());
		
	}

	//metodo cuando el usuario no se logea correctamente
	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		log.info("Error Login " + "Bad credentials");
		try {
			Usuario usuarioLogeado = uService.findByUsername(authentication.getPrincipal().toString());
			if(usuarioLogeado.getEnabled()==true) {
				usuarioLogeado.setIntentos(usuarioLogeado.getIntentos() + 1);
				log.info("Intento de logeo N°:"+ usuarioLogeado.getIntentos());

				if(usuarioLogeado.getIntentos()==3) {
					usuarioLogeado.setEnabled(false);
					log.info("Error User "+ usuarioLogeado.getUsername()+ " is disabled");
				}
				uService.update(usuarioLogeado, usuarioLogeado.getId());
			}
			
			
		}catch(Exception e) {
 			throw new BadCredentialsException("Bad credentials");
 		}

	}
	
	
}
