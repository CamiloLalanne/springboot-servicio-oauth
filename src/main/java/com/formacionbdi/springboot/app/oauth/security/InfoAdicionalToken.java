package com.formacionbdi.springboot.app.oauth.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.oauth.services.IUsuarioService;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

@Component
//para agregar mas informacion al token se necesita implementar la interfaz TokenEnhancer
//y sobreescribir el metodo enhance que retorna el objeto OAuth2AccessToken
public class InfoAdicionalToken implements TokenEnhancer{

	//inyectamos el usuario actual que esta intentando obtener permisos
	//para utilizar nuestros servicios protegidos
	@Autowired
	private IUsuarioService usuarioService;
	
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> info = new HashMap<String, Object>();
		//buscamos el usuario actual por nombre en nuestra bd y luego en un map
		//vamos guardando toda la informacion del usuario que encontremos util para utilizar
		Usuario usuario = usuarioService.findByUsername(authentication.getName());
		info.put("nombre", usuario.getNombre());
		info.put("apellido", usuario.getApellido());
		info.put("correo", usuario.getEmail());
		info.put("edad", usuario.getEdad());
		//en el metodo setAdditionalInformation le pasamos nuestro map con los datos que queremos
		//mostrar en nuestro token
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
		
		return accessToken;
	}

}
