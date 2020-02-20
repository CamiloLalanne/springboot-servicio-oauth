package com.formacionbdi.springboot.app.oauth.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

//con refreshScope la clase se actualiza sin tener que reiniciar el projecto (se actualiza en tiempo de ejecucion)
@RefreshScope
@Configuration
//esta clase se debe habilitar como un servidor de autorizacion, para eso utiliozamos la anotacion
//enableAuthorizationServer
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{
	
	//para obtener elementos de nuestros archivos properties
	//se puede hacer mediante la inyeccion de el objeto Environment o 
	//mediante la anotacion @value
	//se recomienda usar environment cuando es mas de una propiedad,pero es indiferente
	@Autowired
	private Environment env;
	
	@Value("${config.security.oauth.client.id}")
	private String clientId;
	
	//se necesita inyectar los dos beans que hemos creado en la clase springSecurityConfig
	//para esto utilizamos autowired ya que ya estan creados en la clase anterior.
	//beans a inyectar BCryptPasswordEncoder y authenticationManager
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private InfoAdicionalToken infoAdicionalToken;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		//con este metodo le damos acceso a todos los usuarios para que accesan a crear el token
		security.tokenKeyAccess("permitAll()")
		//el metodo checkTokenAccess sirve para validar que el cliente este autenticado
		.checkTokenAccess("isAuthenticated()");
	}

	//con este metodo configuramos a los clientes que tendran acceso para acceder a los recursos
	// de nuestros servicios
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		//client id y secret es el id del usuario al que registraremos para que pueda tener acceso
		//a nuestras aplicaciones y el secret vendria siendo el password
		clients.inMemory().withClient(clientId)
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		//scope es el alcance que tendra el usuario, en este caso podra leer y escribir
		.scopes("read", "write")
		//el metodo authorizedGrantTypes, se refiere a como nosotros vamos a obtener el token
		//en este caso si colocamos con password quiere decir que sera con credenciales
		//refresh token se utiliza para que se refresque el token antes de caduque
		.authorizedGrantTypes("password", "refresh_token")
		//tiempo en el que sera valido el token en segundos
		.accessTokenValiditySeconds(3600)
		//tiempo en el que se refrescara el el token en segundos
		.refreshTokenValiditySeconds(3600)
		.and()
		.withClient(env.getProperty("config.security.oauth.client.idk"))
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret")))
		.scopes("read", "write")
		.authorizedGrantTypes("password", "refresh_token")
		.accessTokenValiditySeconds(3600)
		.refreshTokenValiditySeconds(3600);
	}

	//este metodo esta relacionado con el endpoint de oauth2 del servidor de autenticacion
	//que se encarga de generar el token
	//el path para crear el token es oauth/token y es un metodo http del tipo post
	//el metodo recibira el username, password, grant type (tipo de otorgamiento del token que 
	//va a ser del tipo password via credenciales) y ademas el client id con el secret que 
	//corresponde a las credenciales de la apk cliente  y si todo sale correcto se genera el token
 	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(infoAdicionalToken, accessTokenConverter()));
		
		endpoints.authenticationManager(authenticationManager)
		//este es el componente que se encarga de generar el token
		.tokenStore(tokenStore())
		.accessTokenConverter(accessTokenConverter())
		.tokenEnhancer(tokenEnhancerChain);
	}

	//se necesita crear este metodo tokenStore, y que retorne un JwtTokenStore
	//anotarlo como componente tambien
	@Bean
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	//hay que cambiar el modificador de acceso por public, ademas el objeto 
	//que retorna tiene que ser un JwtAccessTokenConverter
	//y registrar como componente de spring
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		//creamos la instancia de JwtAccessTokenConverter
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		//ahora toca crear la firma del servicio, con esto podemos validar que el token sea real
		//y asi dar acceso a los usuarios a ver los recursos protegidos de nuestro servicio
		tokenConverter.setSigningKey(env.getProperty("config.security.oauth.jwt.key"));
		return tokenConverter;
	}
	
	
	
	
}
