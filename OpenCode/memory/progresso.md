# Progresso do Projeto Mapa Urbano

## 1. O que já está implementado e funcional

### 1.1. Infraestrutura e Configuração
- Estrutura base do projeto com Spring Boot 4.1.1, Java 21 e Gradle Wrapper 9.8.1.
- Migrações do banco com Flyway (`V1` a `V5`) cobrindo as tabelas `usuario`, `ocorrencia`, `HistoricoStatus` e roles.
- Dockerfile com multi-stage build e usuário não-root.
- Arquivo `docker-compose.yaml` com serviços do PostgreSQL e aplicação, incluindo healthcheck do banco.
- Configuração de propriedades no `application.yaml` para PostgreSQL, JPA e JWT.

### 1.2. Segurança e Autenticação
- Implementação de `UserDetails` na entidade `Usuario` e serviço `UserDetailsServiceImpl`.
- Geração e validação de tokens JWT simétricos via `TokenProvider`.
- Filtro de requisições `JwtAuthFilter` com validação de cabeçalho `Authorization: Bearer <token>`.
- `SecurityConfig` configurando autenticação stateless, `BCryptPasswordEncoder` e liberação de rotas públicas.

### 1.3. Módulo de Usuário
- Entidade `Usuario` com atributos de login, role e credenciais criptografadas.
- `UsuarioRepository` com busca customizada `findByEmail`.
- `UsuarioService` com métodos para:
  - Cadastro de usuário com codificação de senha e emissão de token JWT (`cadastrar`).
  - Autenticação e geração de token via `AuthenticationManager` (`login`).
  - Busca de usuário por email (`buscarUsuario`).
  - Listagem de todos os usuários (`buscarUsuarios`).
  - Atualização básica (`atualizar`).
  - Exclusão (`deletar`).
- `UsuarioController` expondo as rotas sob o prefixo `auth`.

### 1.4. Módulo de Ocorrência
- Entidade `Ocorrencia` com campos de endereço, coordenadas, categoria, status e relação com `Usuario`.
- `OcorrenciaRepository` com consultas para listar por usuário e contagem agrupada por bairro.
- `OcorrenciaService` com métodos para:
  - Criação de ocorrência com valores default de bairro/cidade e status inicial `PENDENTE` (`criar`).
  - Busca das ocorrências do usuário autenticado (`buscar`).
  - Atualização de dados da ocorrência (`atualizar`).
  - Exclusão de ocorrência (`deletar`).
- `OcorrenciaController` com endpoints sob o prefixo `/ocorrencias`.

---

## 2. O que está pendente ou precisa de correção

### 2.1. Inconsistências de Rotas e Métodos nos Controllers
- **`UsuarioController`**:
  - `buscarUsuario`: A anotação `@GetMapping` não possui o path variable (ex: `@GetMapping("/{email}")`), embora receba `@PathVariable String email`.
  - `atualizar`: `@PutMapping("/atualizar")` não possui o parâmetro `{id}` no path (ex: `@PutMapping("/atualizar/{id}")`), gerando erro de rota.
  - `deletar`: `@DeleteMapping` não possui `{id}` no path e o método retorna `void` (a linha `ResponseEntity.noContent().build()` é descartada sem retorno).
  - Import não utilizado `org.w3c.dom.stylesheets.LinkStyle` presente na classe.
- **`OcorrenciaController`**:
  - `listarOcorrenciasPorId`: Possui rota `@GetMapping("/{id}")`, porém o método não recebe `@PathVariable Long id` e apenas busca todas as ocorrências do usuário autenticado via `ocorrenciaService.buscar(usuario)`.
  - `atualizar`: `@PutMapping("/atualizar")` não possui `{id}` no path, mas o método declara `@PathVariable Long id`.
  - `deletar`: `@DeleteMapping` não possui `{id}` no path e retorna `void`.

### 2.2. Inconsistências na Lógica de Negócios nos Serviços
- **`UsuarioService.atualizar`**: Instancia um novo `Usuario` com Builder sem carregar a entidade existente e sem atribuir o `id`, `role` ou `senha`. Ao salvar com `id` nulo ou sem campos essenciais, provoca erro ou inserção duplicada em vez de atualização.
- **`OcorrenciaService.atualizar`**: Instancia uma nova `Ocorrencia` sem carregar o registro existente do banco, redefinindo `dataCriacao` e `status` para `PENDENTE`, em vez de atualizar apenas os campos modificáveis da ocorrência existente.
- **Transição de Status**: Não há endpoint ou serviço acionado para mudar o status de uma ocorrência (usando `AtualizarStatusDTO`) e registrar no `HistoricoStatus`.

### 2.3. Módulos e Classes Pendentes / Não Implementados
- **`GeoapiService` e `GeoapiResponseDTO`**: Classes criadas vazias. Falta implementar o consumo de serviço de geocodificação para preencher automaticamente `latitude` e `longitude` ao registrar uma ocorrência.
- **`WebClientConfig`**: Classe de configuração de cliente REST vazia (necessária para instanciar o `RestClient` ou `RestTemplate` da GeoAPI).
- **`HistoricoStatusService`**: Classe vazia. Necessário implementar a persistência de alterações de status e consulta do histórico de uma ocorrência.
- **`EstatisticaService` e `EstatisticaController`**: Métodos vazios sem implementação da lógica de agregação por categoria, bairro e métricas temporais.
- **`OpenAPIConfig`**: Classe vazia; documentação OpenAPI/Swagger ainda não configurada.
- **`GlobalExceptionHandler`**: Declarada como subclasse de `RuntimeException` em vez de um `@RestControllerAdvice` estruturado com `@ExceptionHandler` para formatar respostas HTTP JSON de erro com status 400, 401, 403, 404, 500.
- **Filtro de Segurança**: `JwtAuthFilter.shouldNotFilter` ignora qualquer URI que comece com `/auth`. Como `UsuarioController` está mapeado em `auth`, endpoints protegidos sob `auth` (como `GET /auth/all`) nunca recebem a autenticação do token JWT. Recomenda-se separar `/auth` (login/registro público) de `/usuarios` (gestão autenticada de usuários).

### 2.4. Testes Automatizados
- **`UsuarioServiceTest`**: Teste unitário falha com `NullPointerException` porque `tokenProvider` não foi mockado em `criarUsuario`.
- **`MapaurbanoApplicationTests`**: Falha ao carregar o contexto (`contextLoads`) porque tenta conectar a um PostgreSQL real indisponível durante o build.
- **Cobertura Geral**: Ausência de testes para `OcorrenciaService`, `EstatisticaService`, controllers e filtros.

---

## 3. Próximos Passos Sugeridos

1. **Correção de Testes e Setup de Perfil de Teste**:
   - Ajustar o mock de `TokenProvider` em `UsuarioServiceTest`.
   - Configurar perfil de teste (`application-test.yaml`) com banco em memória H2 ou desabilitar conexão do Flyway/Postgres nos testes unitários/de contexto.
2. **Refatoração do Tratamento de Exceções**:
   - Transformar `GlobalExceptionHandler` em `@RestControllerAdvice` padronizando respostas de erro com timestamp, status HTTP e mensagem.
3. **Correção de Rotas e Métodos nos Controllers**:
   - Ajustar `@PathVariable` e paths nos mapeamentos de `UsuarioController` e `OcorrenciaController`.
   - Garantir retorno de `ResponseEntity<Void>` com `204 No Content` nos métodos de exclusão.
4. **Revisão da Lógica de Atualização**:
   - Ajustar `UsuarioService.atualizar` e `OcorrenciaService.atualizar` para buscar o registro existente no banco, aplicar alterações e persistir.
5. **Implementação do Histórico de Status**:
   - Implementar método para atualização de status de ocorrência registrando automaticamente na entidade `HistoricoStatus`.
6. **Implementação do Módulo de Geocodificação (`GeoapiService`)**:
   - Configurar `WebClientConfig` e implementar chamada à API externa de geolocalização para preencher coordenadas a partir do endereço fornecido.
7. **Implementação do Módulo de Estatísticas**:
   - Implementar regras de agregação em `EstatisticaService` utilizando a consulta existente `contarOcorrenciaPorBairro` e criar novas consultas para categorias e status.
8. **Configuração de Documentação OpenAPI/Swagger**:
   - Configurar `OpenAPIConfig` para expor a documentação interativa dos endpoints.
