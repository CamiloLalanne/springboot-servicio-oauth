package com.formacionbdi.springboot.app.oauth.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.formacionbdi.springboot.app.oauth.clients.UsuarioFeignClient;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Role;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

import brave.Tracer;
import feign.FeignException;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService{
	
	private Logger log = LoggerFactory.getLogger(UsuarioService.class);
	
	@Autowired
	private UsuarioFeignClient client;
	
	//para agregar nuevos tag a zipkin, se necesita primero inyectar el componente Tracer
	@Autowired
	private Tracer tracer;
	
	Usuario usuario = null;
	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			
		//consultar servicio Usuarios obtener usuarios en distintos endpoint
		//creados en servicio usuarios
		//usuario = client.findByUsername(username);
		  usuario = client.obtenerUsername(username);				
			
		//con esto obtenemos los roles del usuario para luego retornarlos en el metodo
		//los roles de spring security son del tipo grandAuthority
		List<GrantedAuthority> authorities = usuario.getRoles()
				.stream()
				.map(role -> new SimpleGrantedAuthority(role.getNombre()))
				.peek(authority -> log.info("Role: " + authority.getAuthority()))
				.collect(Collectors.toList());
		
		log.info("Usuario autenticado: " + username);
		
		return new User(usuario.getUsername(), usuario.getPassword(), usuario.getEnabled(), true, 
				true, true, authorities);
		}catch (FeignException e) {
			String error = "Error en el login, no existe el usuario '"+username+"' en el sistema";
			log.error(error);
			//luego con el metodo currentSpan llamamos al metodo tag en el cual se pasa el id del tag y
			// le pasamos el mensaje que mostrara ese tag
			tracer.currentSpan().tag("error.mensaje", error+ " "+ e.getMessage());
			throw new UsernameNotFoundException("Error en el login, no existe el usuario '"+username+"' en el sistema");
		}
	}

	@Override
	public Usuario findByUsername(String username) {
		return client.findByUsername(username);
	}

	@Override
	public Usuario update(Usuario usuario, Long id) {
		return client.update(usuario, id);
	}

}
