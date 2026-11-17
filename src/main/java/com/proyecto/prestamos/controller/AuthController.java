package com.proyecto.prestamos.controller;
import javax.naming.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.proyecto.prestamos.dto.LoginRequest;
import com.proyecto.prestamos.dto.RegistroRequest;
import com.proyecto.prestamos.dto.SesionDTO;
import com.proyecto.prestamos.dto.SolicitanteDTO;
import com.proyecto.prestamos.dto.TokenResponse;
import com.proyecto.prestamos.model.User;
import com.proyecto.prestamos.repository.UserRepository;
import com.proyecto.prestamos.security.JwtUtil;
import com.proyecto.prestamos.service.SolicitanteService;
import com.proyecto.prestamos.service.TokenRevocationService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y administración de la sesión JWT.")
public class AuthController {

    private final AuthenticationManager manager;
    private final UserRepository users;
    private final SolicitanteService solicitantes;
    private final JwtUtil jwtUtil;
    private final TokenRevocationService revocacion;

    public AuthController(AuthenticationManager manager, UserRepository users,
                          SolicitanteService solicitantes, JwtUtil jwtUtil,
                          TokenRevocationService revocacion) {
        this.manager = manager;
        this.users = users;
        this.solicitantes = solicitantes;
        this.jwtUtil = jwtUtil;
        this.revocacion = revocacion;
    }

    // el registro de una cuenta va a ser de perfil Alumno por defecto
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar una cuenta", description = "Crea una cuenta de usuario con perfil de alumno.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada correctamente."),
            @ApiResponse(responseCode = "400", description = "Los datos enviados no son válidos."),
            @ApiResponse(responseCode = "409", description = "El usuario o el correo ya están registrados.")
    })
    public SolicitanteDTO registrarse(@RequestBody RegistroRequest datos) {
        return solicitantes.registrar(datos);
    }

    // el inicio de sesion va a dar un token JWT que se pone en autorizacion
    @PostMapping("/signin")
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y devuelve un token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas; se devuelve el token."),
            @ApiResponse(responseCode = "401", description = "Usuario o contraseña incorrectos.")
    })
    public TokenResponse ingresar(@RequestBody LoginRequest datos) {
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(datos.username(), datos.password()));
        User user = users.findByUsername(auth.getName());
        return new TokenResponse(jwtUtil.generateToken(auth.getName()), user.getUsername(), user.getRole());
    }

    //al poner el token va a devolver el usuario y su rol para saber si es un admin, encargado o usuario
    @GetMapping("/me")
    @Operation(summary = "Consultar la sesión actual", description = "Devuelve el usuario y los roles asociados al token.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión autenticada."),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido.")
    })
    public SesionDTO miSesion(Authentication auth) {
        return new SesionDTO(auth.getName(),
                auth.getAuthorities().stream().map(a -> a.getAuthority()).toList(), "Autenticado");
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el token JWT enviado en la cabecera Authorization.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token revocado correctamente."),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido.")
    })
    public String salir(@RequestHeader("Authorization") String authorization) {
        //llega aca cuando el filtro JWT ya se verifico que el token era valido 
        // se revoca la sesion para q no se use mas y se guarda en la bd para q no se use mas
        revocacion.revoke(authorization.substring(7));
        return "JWT revocado correctamente.";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String errorCredenciales() { return "Usuario o contraseña incorrectos."; }
}
