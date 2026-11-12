package v1.mapaurbano.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;
import v1.mapaurbano.dto.request.LoginRequestDTO;
import v1.mapaurbano.dto.request.UsuarioRequestDTO;
import v1.mapaurbano.dto.response.TokenResponseDTO;
import v1.mapaurbano.dto.response.UsuarioResponseDTO;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastrar")
    public ResponseEntity<TokenResponseDTO> cadastrarUsuario(@RequestBody UsuarioRequestDTO usuarioRequestDTO){
        TokenResponseDTO usuario = usuarioService.cadastrar(usuarioRequestDTO);
        return ResponseEntity.status(201).body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO){
        TokenResponseDTO usuario = usuarioService.login(loginRequestDTO);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> buscarUsuario(@PathVariable String email){
        UsuarioResponseDTO usuario = usuarioService.buscarUsuario(email);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarUsuarios(){
        List<UsuarioResponseDTO> usuarios = usuarioService.buscarUsuarios();
       return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/atualizar")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@RequestBody UsuarioRequestDTO usuarioRequestDTO, @PathVariable Long id){
        UsuarioResponseDTO usuario = usuarioService.atualizar(id, usuarioRequestDTO);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping
    public void deletar(@PathVariable Long id){
        usuarioService.deletar(id);
        ResponseEntity.noContent().build();
    }
}
