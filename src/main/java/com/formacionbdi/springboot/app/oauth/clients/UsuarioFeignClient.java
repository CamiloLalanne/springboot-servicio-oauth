package com.formacionbdi.springboot.app.oauth.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

//en feig client name se pasa el nombre del servicio que esta definido en el archivo properties
@FeignClient(name="servicio-usuarios")
public interface UsuarioFeignClient {

	//y con el getMapping podemos usar el endpoint para buscar el username que se encuentra en 
	//el servicio usuario
	@GetMapping("/usuarios/search/findUsernameQuery")
	public Usuario findByUsername(@RequestParam String username);
	
	@PutMapping("/usuarios/{id}")
	public Usuario update(@RequestBody Usuario usuario, @PathVariable Long id);
}
