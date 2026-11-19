# Contexto Técnico Consolidado — Projeto Mapa Urbano

> Documento único de contexto para handoff. Consolida as informações dos arquivos de memória (`OpenCode/memory/arquitetura.md`, `progresso.md`, `decisoes.md`, `erros-resolvidos.md`) e do código-fonte real do repositório.
>
> **Data de consolidação:** 2026-11-17
> **Repositório:** `https://github.com/danieldasneves1/MapaUrbano`
> **Pacote raiz:** `v1.mapaurbano`
> **Branch de referência:** estado atual do working tree (commits `635b5e7` e `ee95eed`)

---

## 1. Visão Geral e Objetivo do Projeto

### 1.1. O que o sistema resolve
O **Mapa Urbano** é uma **API REST** cujo objetivo é **registrar, acompanhar e mapear problemas urbanos** relatados por cidadãos. Os tipos de problema previstos incluem:

- Buracos em vias;
- Falhas na iluminação pública;
- Acúmulo de lixo;
- Vazamento de água;
- Falta de saneamento;
- Sinalização danificada;
- Outros.

O sistema permite:
- Cadastro/autenticação de usuários (cidadãos e administradores);
- Registro georreferenciado de ocorrências (endereço + latitude/longitude);
- Ciclo de vida da ocorrência com status (`PENDENTE` → `EM_ANDAMENTO` → `RESOLVIDO`/`CANCELADO`);
- Auditoria temporal de mudanças de status em histórico;
- Consolidação de estatísticas por status, categoria e bairro.

> **Importante:** a API está em **estágio inicial/incompleto**. Vários módulos existem apenas como esqueletos (geocodificação, estatísticas, histórico de status, OpenAPI, tratamento global de exceções). Consulte a Seção 7 para o status real.

### 1.2. Principais perfis de usuário
| Perfil | Role | Descrição |
|---|---|---|
| Cidadão | `ROLE_USER` | Atribuído automaticamente no cadastro. Registra e acompanha as próprias ocorrências. |
| Administrador | `ROLE_ADMIN` | Previsto para visualizar todos os usuários e extrair métricas consolidadas. Atualmente **não há fluxo de criação de admin** nem autorização por role implementada nos endpoints. |

### 1.3. Fluxo principal (previsto)
1. O cidadão se cadastra (`POST /auth/cadastrar`) e recebe um token JWT.
2. Faz login (`POST /auth/login`) e passa a enviar o token no header `Authorization: Bearer <token>`.
3. Registra uma ocorrência (`POST /ocorrencias/registrar`) informando descrição, categoria e endereço.
4. O sistema **deveria** geocodificar o endereço para obter `latitude`/`longitude` (módulo planejado, não implementado).
5. A ocorrência nasce com status `PENDENTE` e é vinculada ao usuário autenticado.
6. O cidadão consulta suas ocorrências (`GET /ocorrencias/{id}` — comportamento atual inconsistente; ver Seção 6).
7. Gestores/administradores atualizariam o status e consultariam estatísticas (parcialmente planejado).
8. Cada mudança de status deve gerar um registro em `HistoricoStatus` (planejado, não implementado).

---

## 2. Stack Tecnológica e Infraestrutura

### 2.1. Linguagem e framework
| Item | Versão / Detalhe |
|---|---|
| Linguagem | **Java 21** (toolchain Eclipse Temurin) |
| Framework principal | **Spring Boot 4.1.1** |
| Build | **Gradle 9.8.1** via Gradle Wrapper (`./gradlew`) |
| Grupo / versão do artefato | `v1` / `0.0.1-SNAPSHOT` |
| Gerenciador de dependências | Plugin `io.spring.dependency-management` 1.1.7 |

### 2.2. Dependências principais (`build.gradle`)
- `spring-boot-starter-webmvc` — APIs REST (Servlet/MVC).
- `spring-boot-starter-data-jpa` — persistência ORM via Hibernate.
- `spring-boot-starter-security` — autenticação e controle de acesso.
- `spring-boot-starter-validation` — Bean Validation (Jakarta).
- `spring-boot-starter-flyway` — versionamento de schema.
- `org.flywaydb:flyway-database-postgresql` — suporte do Flyway ao PostgreSQL.
- `org.postgresql:postgresql` (runtime) — driver JDBC.
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` **0.12.5** — emissão/validação de JWT.
- `org.projectlombok:lombok` (`compileOnly` + `annotationProcessor`) — boilerplate.
- Dependências de teste: starters `*-test` do Spring Boot, JUnit Platform Launcher, Lombok de teste.

### 2.3. Containerização e orquestração

**`Dockerfile` — multi-stage build:**
1. **Estágio builder:** `eclipse-temurin:21-jdk-alpine`.
   - Copia `gradlew`, `build.gradle`, `settings.gradle`, `gradle/` e baixa dependências.
   - Copia `src`, executa `./gradlew bootJar --no-daemon`.
2. **Estágio runtime:** `eclipse-temurin:21-jre-alpine`.
   - Cria usuário/grupo sem privilégios `spring:spring` e executa como ele.
   - Copia o JAR para `app.jar`, expõe a porta `8080` e executa `java -jar app.jar`.

**`docker-compose.yaml`:**
- Serviço `postgres`:
  - Imagem `postgres:15-alpine`, container `banco`, porta `5432:5432`.
  - `healthcheck` com `pg_isready -U postgres` (intervalo 5s, timeout 5s, 5 tentativas).
- Serviço `app`:
  - Build local (`build: .`), container `mapa-urbano`, porta `8080:8080`.
  - Depende do `postgres` com `condition: service_healthy`.
  - Recebe `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` via ambiente.

### 2.4. Variáveis de ambiente e propriedades

> **Nenhum valor secreto real é reproduzido neste documento.** Abaixo apenas os nomes/chaves.

**Variáveis consumidas pelo `docker-compose.yaml`:**
| Variável | Uso |
|---|---|
| `POSTGRES_USER` | Usuário do banco no container PostgreSQL. |
| `POSTGRES_PASSWORD` | Senha do banco no container PostgreSQL. **Atualmente hardcoded no compose (risco).** |
| `POSTGRES_DB` | Nome do banco criado (`mapaurbano`). |
| `SPRING_DATASOURCE_URL` | URL JDBC da aplicação. |
| `SPRING_DATASOURCE_USERNAME` | Usuário JDBC da aplicação. |
| `SPRING_DATASOURCE_PASSWORD` | Senha JDBC da aplicação. |

**Propriedades em `src/main/resources/application.yaml`:**
| Propriedade | Finalidade |
|---|---|
| `spring.application.name` | Nome da aplicação (`mapaurbano`). |
| `spring.datasource.url` | URL JDBC do PostgreSQL. |
| `spring.datasource.username` | Usuário do banco. |
| `spring.datasource.password` | Senha do banco. **Atualmente hardcoded (risco).** |
| `spring.datasource.driver-class-name` | Driver (`org.postgresql.Driver`). |
| `spring.jpa.hibernate.ddl-auto` | Atualmente `update`. |
| `spring.jpa.show-sql` | Log de SQL habilitado. |
| `spring.jpa.properties.hibernate.format_sql` | Formatação do SQL logado. |
| `spring.jpa.properties.hibernate.dialect` | Dialeto (`PostgreSQLDialect`). |
| `spring.logging.level.org.springframework.security` | Nível de log `DEBUG` para segurança. |
| `jwt.expiration` | Tempo de expiração do token em ms (24h). |
| `jwt.key` | Chave simétrica HMAC-SHA (base64). **Atualmente hardcoded (risco).** |

> **Recomendação de segurança:** externalizar `jwt.key`, `spring.datasource.password` e as credenciais do compose para variáveis de ambiente/secret manager. O histórico do Git já conteve `JWT_KEY` como variável de ambiente, mas a versão atual usa valor literal.

**Observação sobre perfis:** não existe `application-test.yaml` nem profile de teste configurado.

---

## 3. Arquitetura e Estrutura de Pastas

### 3.1. Padrão arquitetural
**Arquitetura em Camadas (Layered Architecture):**
`Controller` → `Service` → `Repository` → `Entity`/Banco, com encapsulamento via **DTOs imutáveis (Java Records)**.
A conversão entidade→DTO é feita por métodos estáticos de fábrica `fromEntity(...)` dentro dos próprios records de resposta.
As entidades usam **Lombok** (`@Getter`, `@Setter`, `@Builder`, etc.).

### 3.2. Árvore de diretórios comentada
```
mapaurbano/
├── build.gradle                  # Dependências, plugins Spring Boot/Gradle, toolchain Java 21
├── settings.gradle               # Nome do projeto Gradle: mapaurbano
├── gradlew / gradlew.bat         # Gradle Wrapper (9.8.1)
├── gradle/wrapper/               # Configuração do wrapper
├── Dockerfile                    # Multi-stage build (JDK 21 -> JRE 21 Alpine, usuário non-root)
├── docker-compose.yaml           # PostgreSQL 15 + aplicação, com healthcheck
├── HELP.md                       # Documentação padrão gerada pelo Spring Initializr
├── AGENTS.md                     # Instruções para agentes/IA (memória persistente em OpenCode/)
├── OpenCode/
│   ├── memory/                   # Memória persistente do projeto
│   │   ├── arquitetura.md        # Estrutura, stack, modelo de dados
│   │   ├── progresso.md          # O que está pronto / pendente / próximos passos
│   │   ├── decisoes.md           # Regras de negócio e decisões técnicas
│   │   └── erros-resolvidos.md   # Bugs e soluções (ERR-001 a ERR-004)
│   └── CONTEXTO_CLAUDE.md        # Este documento consolidado
└── src/
    ├── main/
    │   ├── java/v1/mapaurbano/
    │   │   ├── MapaurbanoApplication.java    # Classe main / bootstrap Spring Boot
    │   │   ├── config/                       # Infraestrutura e segurança
    │   │   │   ├── SecurityConfig.java       # Cadeia de filtros, stateless, CSRF off, rotas públicas
    │   │   │   ├── JwtAuthFilter.java        # OncePerRequestFilter: extrai/valida Bearer token
    │   │   │   ├── TokenProvider.java        # Geração/validação de JWT HMAC-SHA
    │   │   │   ├── UserDetailsServiceImpl.java # Carrega usuário por email (UserDetailsService)
    │   │   │   ├── OpenAPIConfig.java        # ESQUELETO (vazio) — Swagger/OpenAPI não configurado
    │   │   │   └── RestClientConfig.java     # ESQUELETO (vazio) — cliente HTTP externo não configurado
    │   │   ├── controller/                   # Camada HTTP REST
    │   │   │   ├── UsuarioController.java    # Rotas /auth (cadastro, login, listagem, update, delete)
    │   │   │   ├── OcorrenciaController.java # Rotas /ocorrencias
    │   │   │   └── EstatisticaController.java# ESQUELETO — métodos vazios, sem @RestController
    │   │   ├── service/                      # Regras de negócio
    │   │   │   ├── UsuarioService.java       # Cadastro/login/busca/atualização/exclusão
    │   │   │   ├── OcorrenciaService.java    # Criação/busca/atualização/exclusão de ocorrências
    │   │   │   ├── HistoricoStatusService.java # ESQUELETO (vazio)
    │   │   │   ├── EstatisticaService.java   # ESQUELETO (vazio)
    │   │   │   └── GeoapiService.java        # ESQUELETO (vazio) — geocodificação planejada
    │   │   ├── repository/                   # Spring Data JPA
    │   │   │   ├── UsuarioRepository.java    # findByEmail
    │   │   │   ├── OcorrenciaRepository.java # findByStatus, agregações por categoria/bairro, findByUsuarioId
    │   │   │   └── HistoricoStatusRepository.java # CRUD básico
    │   │   ├── dto/
    │   │   │   ├── request/                  # Records de entrada (validados)
    │   │   │   │   ├── LoginRequestDTO.java
    │   │   │   │   ├── UsuarioRequestDTO.java
    │   │   │   │   ├── OcorrenciaRequestDTO.java
    │   │   │   │   └── AtualizarStatusDTO.java
    │   │   │   └── response/                 # Records de saída
    │   │   │       ├── TokenResponseDTO.java
    │   │   │       ├── UsuarioResponseDTO.java
    │   │   │       ├── OcorrenciaResponseDTO.java
    │   │   │       ├── EstatisticaBairroDTO.java
    │   │   │       └── GeoapiResponseDTO.java # ESQUELETO (classe vazia)
    │   │   ├── entity/                       # Entidades JPA
    │   │   │   ├── Usuario.java              # Implementa UserDetails
    │   │   │   ├── Ocorrencia.java
    │   │   │   ├── HistoricoStatus.java
    │   │   │   └── enums/
    │   │   │       ├── Role.java             # ROLE_ADMIN, ROLE_USER
    │   │   │       ├── Categoria.java        # categorias + parsing resiliente
    │   │   │       └── StatusOcorrencia.java # PENDENTE, EM_ANDAMENTO, RESOLVIDO, CANCELADO
    │   │   └── exception/
    │   │       ├── ResourceNotFoundException.java  # RuntimeException
    │   │       ├── GeocodificacaoException.java    # RuntimeException (integração externa)
    │   │       └── GlobalExceptionHandler.java     # RuntimeException (NÃO é @RestControllerAdvice)
    │   └── resources/
    │       ├── application.yaml              # Datasource, JPA, JWT, logging
    │       └── db/migration/                 # Scripts Flyway V1..V5
    └── test/java/v1/mapaurbano/
        ├── MapaurbanoApplicationTests.java   # contextLoads (falha sem PostgreSQL real)
        └── service/UsuarioServiceTest.java   # Teste unitário (falha por mock ausente)
```

---

## 4. Banco de Dados e Migrações

### 4.1. Banco e ferramentas
- **Banco:** PostgreSQL **15** (imagem `postgres:15-alpine` no compose).
- **Migrações:** **Flyway**, scripts em `src/main/resources/db/migration/`.
- **ORM:** Hibernate/JPA.
- **Estratégia de ID:** `GenerationType.IDENTITY` no JPA, alinhada a `BIGINT GENERATED BY DEFAULT AS IDENTITY` no PostgreSQL.
- **Atenção:** `spring.jpa.hibernate.ddl-auto` está como `update`, o que pode mascarar divergências em relação ao Flyway. O recomendado seria `validate` em produção.

### 4.2. Histórico de migrações
| Versão | Arquivo | O que faz |
|---|---|---|
| V1 | `V1__criar_tabela_usuario.sql` | Cria `usuario` (`id BIGINT PK`, `nome VARCHAR(100) NOT NULL`, `email VARCHAR(100) UNIQUE`, `senha VARCHAR(100) NOT NULL`). |
| V2 | `V2__criar_tabela_ocorrencia.sql` | Cria `ocorrencia` com dados de endereço, coordenadas `DECIMAL(10,8)`, status, data de criação e FK `usuario_id` → `usuario(id)` com `ON DELETE SET NULL`. |
| V3 | `V3__criar_tabela_historicostatus.sql` | Cria `HistoricoStatus` com status anterior/novo, data e FK `ocorrencia_id` → `ocorrencia(id)` com `ON DELETE CASCADE`. |
| V4 | `V4__criar_coluna_role_usuario.sql` | Adiciona coluna `role VARCHAR(15)` em `usuario`. |
| V5 | `V5__alterar_coluna_id_usuario.sql` | Altera `usuario.id` para `GENERATED BY DEFAULT AS IDENTITY` (corrige erro de persistência com IDENTITY). |

### 4.3. Tabelas, campos e relacionamentos

#### `usuario`
| Campo | Tipo | Restrições / Observações |
|---|---|---|
| `id` | `BIGINT` | PK, identity (após V5). |
| `nome` | `VARCHAR(100)` | `NOT NULL`. |
| `email` | `VARCHAR(100)` | `UNIQUE`; usado como *username*. |
| `senha` | `VARCHAR(100)` | `NOT NULL`; armazenada como hash BCrypt. |
| `role` | `VARCHAR(15)` | Enum `Role` (`ROLE_ADMIN`/`ROLE_USER`), adicionada na V4. |

#### `ocorrencia`
| Campo | Tipo | Restrições / Observações |
|---|---|---|
| `id` | `BIGINT` | PK, identity. |
| `descricao` | `VARCHAR(100)` | `NOT NULL` (entidade `nullable = false`). |
| `categoria` | `VARCHAR(20)` | Enum `Categoria` (`EnumType.STRING`). |
| `logradouro` | `VARCHAR(100)` | — |
| `numero` | `VARCHAR(20)` | — |
| `bairro` | `VARCHAR(20)` | Default de negócio `"Marco"` quando nulo (no `@PrePersist`). |
| `cidade` | `VARCHAR(20)` | Default de negócio `"Belem"` quando nulo (no `@PrePersist`). |
| `longitude` | `DECIMAL(10,8)` | Preenchida pela geocodificação (planejado). |
| `latitude` | `DECIMAL(10,8)` | Preenchida pela geocodificação (planejado). |
| `status` | `VARCHAR(20)` | Enum `StatusOcorrencia` (`EnumType.STRING`). |
| `data_criacao` | `TIMESTAMP` | Definida no `@PrePersist`. |
| `usuario_id` | `BIGINT` | FK → `usuario(id)`, `ON DELETE SET NULL`. |

#### `HistoricoStatus`
| Campo | Tipo | Restrições / Observações |
|---|---|---|
| `id` | `BIGINT` | PK, identity. |
| `status_anterior` | `VARCHAR(20)` | Enum `StatusOcorrencia`. |
| `status_novo` | `VARCHAR(20)` | Enum `StatusOcorrencia`. |
| `data_alteracao` | `TIMESTAMP` | Definida no `@PrePersist`. |
| `ocorrencia_id` | `BIGINT` | FK → `ocorrencia(id)`, `ON DELETE CASCADE`. |

### 4.4. Modelo Entidade-Relacionamento
```
+------------------+         1:N          +--------------------+
|     USUARIO      | -------------------> |     OCORRENCIA     |
|------------------|                      |--------------------|
| id (PK, Identity)|                      | id (PK, Identity)  |
| nome             |                      | descricao          |
| email (Unique)   |                      | categoria (Enum)   |
| senha (Hash)     |                      | logradouro, numero |
| role (Enum)      |                      | bairro, cidade     |
+------------------+                      | latitude, longitude|
                                          | status (Enum)      |
                                          | data_criacao       |
                                          | usuario_id (FK)    |
                                          +--------------------+
                                                     | 1
                                                     |
                                                     | N
                                          +--------------------+
                                          |  HISTORICO_STATUS  |
                                          |--------------------|
                                          | id (PK, Identity)  |
                                          | status_anterior    |
                                          | status_novo        |
                                          | data_alteracao     |
                                          | ocorrencia_id (FK) |
                                          +--------------------+
```
- **Usuário 1:N Ocorrência** — exclusão do usuário preserva a ocorrência, anulando o autor (`ON DELETE SET NULL`).
- **Ocorrência 1:N HistóricoStatus** — exclusão da ocorrência remove o histórico em cascata (`ON DELETE CASCADE`).

---

## 5. Integrações Externas — API de Geocodificação

> **STATUS REAL: NÃO IMPLEMENTADO.** Não existe, no código atual, nenhuma chamada HTTP a um serviço externo de geocodificação, nem propriedades de configuração da API no `application.yaml`, nem URL/endpoint definido. O que existe são apenas esqueletos e uma exceção dedicada. Esta seção documenta **o que existe hoje** e **o contrato previsto**, para orientar a implementação.

### 5.1. O que existe hoje no repositório
| Artefato | Caminho | Estado |
|---|---|---|
| `GeoapiService` | `src/main/java/v1/mapaurbano/service/GeoapiService.java` | **Classe vazia**, sem anotações `@Service`, sem dependências, sem métodos. |
| `GeoapiResponseDTO` | `src/main/java/v1/mapaurbano/dto/response/GeoapiResponseDTO.java` | **Classe vazia**, sem campos. |
| `WebClientConfig` | `src/main/java/v1/mapaurbano/config/RestClientConfig.java` | **Classe vazia**, sem `@Configuration` nem beans de `RestClient`/`RestTemplate`. |
| `GeocodificacaoException` | `src/main/java/v1/mapaurbano/exception/GeocodificacaoException.java` | `RuntimeException` com construtor de mensagem — prevista para falhas de geocodificação. |
| Campos de coordenadas | `Ocorrencia.longitude` / `Ocorrencia.latitude` | `BigDecimal`, mapeados para `DECIMAL(10,8)`. |
| DTO de saída | `OcorrenciaResponseDTO` | Já expõe `longitude` e `latitude`. |

### 5.2. Cliente HTTP
- **Cliente atualmente utilizado:** **nenhum**.
- **Planejado:** instanciar um cliente HTTP do ecossistema Spring (recomendado `RestClient`, nativo do Spring Framework 6+/Spring Boot 3+, compatível com o Boot 4.x; alternativa `RestTemplate`) por meio de um bean em `WebClientConfig` (`@Configuration` + `@Bean`).
- **Observação técnica:** para desserialização JSON, o projeto já possui Jackson via `jjwt-jackson`/Spring Web MVC, portanto não é necessária dependência adicional.

### 5.3. Propriedades no `application.yaml`
- **Atualmente:** **não há** nenhuma propriedade de geocodificação (nem URL base, nem chave de API, nem timeout).
- **Planejado (sugestão de nomes, ainda não implementados):**
  ```yaml
  geoapi:
    base-url: <URL_DO_SERVICO>          # nome sugerido
    api-key: <CHAVE>                    # nome sugerido (se o provedor exigir)
    timeout-ms: <TIMEOUT>               # nome sugerido
  ```
  > Não há provedor definido no projeto (não há referência a Nominatim, OpenStreetMap, ViaCEP, Google Maps etc.). Qualquer escolha de provedor deve ser confirmada com o time antes da implementação.

### 5.4. Entradas previstas
A geocodificação deve ocorrer no fluxo de **criação de ocorrência**, consumindo os campos de endereço já recebidos em `OcorrenciaRequestDTO`:
| Campo | Tipo | Obrigatório |
|---|---|---|
| `logradouro` | `String` | Sim (`@NotBlank`) |
| `numero` | `String` | Sim (`@NotBlank`) |
| `bairro` | `String` | Sim (`@NotBlank`) |
| `cidade` | `String` | Sim (`@NotBlank`) |

Formato de consulta previsto: composição do endereço completo (ex.: `"<logradouro>, <numero>, <bairro>, <cidade>"`) como parâmetro de busca do provedor.

### 5.5. Saídas previstas
`GeoapiService` deveria retornar um `GeoapiResponseDTO` contendo, no mínimo:
| Campo | Tipo | Destino |
|---|---|---|
| `latitude` | `BigDecimal` | `Ocorrencia.latitude` |
| `longitude` | `BigDecimal` | `Ocorrencia.longitude` |
| (opcional) endereço normalizado / precisão | `String` | Log/auditoria |

Esses valores devem ser atribuídos à entidade **antes** de `ocorrenciaRepository.save(...)`, garantindo persistência das coordenadas no registro.

### 5.6. Tratamento de erros previsto
- Falhas de comunicação, timeout, resposta vazia/ambígua ou endereço não encontrado devem lançar **`GeocodificacaoException`** (já existente), com mensagem descritiva.
- **Comportamento a definir com o time:** (a) abortar a criação da ocorrência com erro HTTP apropriado, ou (b) criar a ocorrência sem coordenadas (campos nulos) e permitir geocodificação posterior. O estado atual do código não define essa política.
- O tratamento HTTP padronizado depende da refatoração do `GlobalExceptionHandler` para `@RestControllerAdvice` (ver Seção 7), incluindo mapeamento de `GeocodificacaoException` para, por exemplo, `502 Bad Gateway` ou `422 Unprocessable Entity`.

---

## 6. Contratos de API (Endpoints) e Regras de Negócio

### 6.1. Segurança e autenticação
- **Stateless:** `SessionCreationPolicy.STATELESS`, CSRF desabilitado.
- **JWT HMAC-SHA:** chave simétrica em `jwt.key`, expiração em `jwt.expiration` (24h). O token é gerado por `TokenProvider` com *subject* = e-mail do usuário.
- **Filtro:** `JwtAuthFilter` (anotado `@Component`) lê `Authorization: Bearer <token>`, valida assinatura/expiração e popula o `SecurityContextHolder`.
- **Autorização atual:** apenas `POST /auth/**` é público; **todas** as demais rotas exigem autenticação (`anyRequest().authenticated()`).
- **HttpStatus de borda:** entrada não autenticada → `401`; acesso negado → `403`.
- **Senhas:** hash **BCrypt** (`BCryptPasswordEncoder`).
- **⚠️ Problemas conhecidos de segurança/autorização:**
  - `JwtAuthFilter.shouldNotFilter` ignora **qualquer** URI que comece com `/auth`, então `GET /auth`, `GET /auth/all`, `PUT /auth/atualizar` e `DELETE /auth` **não passam pelo filtro JWT** → não há autenticação efetiva nessas rotas.
  - **Não há uso de `@PreAuthorize`/`hasRole(...)`** em nenhum endpoint. A distinção `ROLE_ADMIN` × `ROLE_USER` existe no modelo, mas **não é aplicada**.
  - `SecurityConfig` libera `POST /auth/**`, o que inclui `/auth/cadastrar` e `/auth/login` (correto), mas também qualquer futuro POST sob `/auth`.

### 6.2. Endpoints — estado real do código

#### `UsuarioController` — prefixo `auth` (sem barra inicial no `@RequestMapping`)
| Método | Rota | Método Java | Corpo / Parâmetros | Retorno | Observações |
|---|---|---|---|---|---|
| `POST` | `/auth/cadastrar` | `cadastrarUsuario` | `UsuarioRequestDTO` | `201` + `TokenResponseDTO` | Público. Cria usuário com `ROLE_USER`, hash BCrypt e emite JWT. |
| `POST` | `/auth/login` | `login` | `LoginRequestDTO` | `200` + `TokenResponseDTO` | Público. Usa `AuthenticationManager`. Normaliza e-mail (`trim().toLowerCase()`). |
| `GET` | `/auth` | `buscarUsuario` | `@PathVariable String email` **sem `{email}` no path** | `200` + `UsuarioResponseDTO` | **Rota quebrada** — falta `@GetMapping("/{email}")`. |
| `GET` | `/auth/all` | `buscarUsuarios` | — | `200` + `List<UsuarioResponseDTO>` | Não exige token por causa do `shouldNotFilter`. |
| `PUT` | `/auth/atualizar` | `atualizar` | `UsuarioRequestDTO` + `@PathVariable Long id` **sem `{id}` no path** | `200` + `UsuarioResponseDTO` | **Rota quebrada** e lógica de update incorreta. |
| `DELETE` | `/auth` | `deletar` | `@PathVariable Long id` **sem `{id}` no path** | `void` | **Rota quebrada**; `ResponseEntity.noContent()` é descartado; deveria retornar `204`. |

#### `OcorrenciaController` — prefixo `/ocorrencias`
| Método | Rota | Método Java | Corpo / Parâmetros | Retorno | Observações |
|---|---|---|---|---|---|
| `POST` | `/ocorrencias/registrar` | `registrarOcorrencia` | `OcorrenciaRequestDTO` + usuário autenticado | `200` + `OcorrenciaResponseDTO` | Cria com status `PENDENTE`, `dataCriacao = now()`, vincula usuário. Deveria retornar `201`. |
| `GET` | `/ocorrencias/{id}` | `listarOcorrenciasPorId` | — | `200` + `List<OcorrenciaResponseDTO>` | **Inconsistente:** declara `/{id}` mas **não recebe `@PathVariable`**; na prática lista todas as ocorrências do usuário autenticado. |
| `PUT` | `/ocorrencias/atualizar` | `atualizar` | `@PathVariable Long id` **sem `{id}` no path** + `OcorrenciaRequestDTO` | `200` + `OcorrenciaResponseDTO` | **Rota quebrada**; lógica recria a ocorrência em vez de atualizar. |
| `DELETE` | `/ocorrencias` | `deletar` | `@PathVariable Long id` **sem `{id}` no path** | `void` | **Rota quebrada**; não retorna `204`. |

#### `EstatisticaController`
- **Sem `@RestController`/`@RequestMapping`.**
- Métodos vazios (`totalOcorrencias`, `ocorrenciaPorStatus`, `ocorrenciaPorCategoria`, `metricasTemporais`), **sem rotas expostas**.

#### Rotas planejadas mas ainda inexistentes
- `PATCH/PUT /ocorrencias/{id}/status` — mudança de status usando `AtualizarStatusDTO` e registro em `HistoricoStatus`.
- `GET /ocorrencias/{id}/historico` — consulta do histórico de status.
- Endpoints de estatísticas agregadas (total, por status, por categoria, por bairro, métricas temporais).

### 6.3. DTOs de entrada e validações
| DTO | Campos | Validações |
|---|---|---|
| `UsuarioRequestDTO` | `nome`, `email`, `senha` | `nome` `@NotBlank`; `email` `@NotBlank`; `senha` `@NotBlank` + `@Size(min = 6)`. |
| `LoginRequestDTO` | `email`, `senha` | `email` `@NotBlank`; `senha` `@NotBlank` + `@Size(min = 6)`. |
| `OcorrenciaRequestDTO` | `descricao`, `categoria`, `logradouro`, `numero`, `bairro`, `cidade` | Todos `@NotBlank`. **Não há `@Valid` nos controllers**, então as validações declarativas não são disparadas automaticamente. |
| `AtualizarStatusDTO` | `novoStatus` | `@NotBlank`. Ainda não utilizado por nenhum endpoint. |

### 6.4. DTOs de saída
- `TokenResponseDTO`: `token`, `tipo`, `expiresAt`, `nome`, `role`.
- `UsuarioResponseDTO`: `id`, `nome`, `email`, `role` (+ `fromEntity`). **Não expõe senha** (correto).
- `OcorrenciaResponseDTO`: `id`, `descricao`, `categoria`, `logradouro`, `numero`, `bairro`, `cidade`, `longitude`, `latitude`, `status`, `dataCricao` (typo no campo), `usuario` (expõe a **entidade `Usuario` completa**, incluindo hash de senha — **risco de vazamento**).
- `EstatisticaBairroDTO`: `totalOcorrencias`, `totalPendentes`, `totalEmAndamento`, `totalResolvidas`, `totalCanceladas`, `ocorrenciaPorCategoria` (`Map<String, Long>`), `ocorrenciaPorBairro` (`Map<String, Long>`), `percentualResolvidos` (`Double`).
- `GeoapiResponseDTO`: **vazio** (sem campos).

### 6.5. Regras de negócio implementadas e previstas

**Usuários:**
- E-mail único: cadastro com e-mail já existente é recusado (`GlobalExceptionHandler("email do usuario ja cadastrado")`).
- Senha mínima de 6 caracteres; persistida com hash BCrypt.
- Novos usuários recebem `ROLE_USER` automaticamente.
- Login normaliza o e-mail para minúsculas antes da autenticação.

**Ocorrências:**
- Toda ocorrência é criada com status `PENDENTE`.
- `dataCriacao` definida automaticamente (`@PrePersist` e também no service).
- Defaults de localização quando nulos: `cidade = "Belem"`, `bairro = "Marco"` (`@PrePersist`).
- A ocorrência é vinculada ao usuário autenticado.
- Consulta de ocorrências filtra por `usuario.id` (`findByUsuarioId`).
- **Parsing resiliente de categoria (`Categoria.fromString`):** compara com `name()` e `descricao`, normaliza via NFD (remove acentos), converte para maiúsculas, substitui espaços/hífens por `_`, aceita prefixos parciais e, em último caso, retorna `OUTROS`. Contudo, `OcorrenciaService` usa `Categoria.valueOf(...)` diretamente (não usa `fromString`), o que **quebra** com entradas acentuadas/minúsculas.

**Categorias suportadas (`Categoria`):**
`BURACO_NA_RUA`, `iLUMINAÇAO_PUBLICA`, `LIXO_ACUMULADO`, `VAZAMENTO_DE_AGUA`, `FALTA_SANEAMENTO`, `SINALIZAÇAO_DANIFICADA`, `OUTROS`.
> Os identificadores contêm erro de digitação/acentuação (`iLUMINAÇAO_PUBLICA`, `SINALIZAÇAO_DANIFICADA`), o que impacta o uso de `valueOf`.

**Ciclo de status (`StatusOcorrencia`):**
`PENDENTE` → `EM_ANDAMENTO` → `RESOLVIDO` / `CANCELADO`.
Mudanças devem ser auditadas em `HistoricoStatus` (não implementado).

### 6.6. Consultas de repositório
| Repositório | Método | Descrição |
|---|---|---|
| `UsuarioRepository` | `findByEmail(String)` | `Optional<Usuario>` por e-mail (usado no login e UserDetails). |
| `OcorrenciaRepository` | `findByStatus(StatusOcorrencia)` | Lista por status. |
| `OcorrenciaRepository` | `findByUsuarioId(Long)` | Lista ocorrências do usuário. |
| `OcorrenciaRepository` | `contarOcorrenciaPorCategoria()` | JPQL `GROUP BY categoria` → `List<Object[]>` (ainda não consumido). |
| `OcorrenciaRepository` | `contarOcorrenciaPorBairro()` | JPQL `GROUP BY bairro` → `List<Object[]>` (ainda não consumido). |
| `HistoricoStatusRepository` | CRUD herdado | `JpaRepository<HistoricoStatus, Long>`. |

---

## 7. Status Atual e Próximos Passos

### 7.1. O que já está implementado e funcional
- **Infraestrutura:** projeto Spring Boot 4.1.1 + Java 21 + Gradle Wrapper 9.8.1; Flyway V1–V5; Dockerfile multi-stage non-root; `docker-compose.yaml` com PostgreSQL 15 e healthcheck; `application.yaml` com datasource, JPA e JWT.
- **Segurança:** `Usuario implements UserDetails`; `UserDetailsServiceImpl`; geração/validação de JWT em `TokenProvider`; `JwtAuthFilter`; `SecurityConfig` stateless com BCrypt e rotas públicas `POST /auth/**`.
- **Módulo de usuário:** entidade, repositório (`findByEmail`), service (cadastro, login, busca, listagem, atualização, exclusão) e controller sob `auth`.
- **Módulo de ocorrência:** entidade, repositório (por usuário, por status, agregações), service (criação com status `PENDENTE`, busca por usuário, atualização, exclusão) e controller sob `/ocorrencias`.
- **Enums:** `Role`, `Categoria` (com parsing resiliente) e `StatusOcorrencia`.
- **DTOs:** requests e responses baseados em records, com `fromEntity` nos responses.

### 7.2. Bugs conhecidos e correções necessárias
**Rotas e controllers:**
- `UsuarioController.buscarUsuario`: adicionar `@GetMapping("/{email}")` (ou `@RequestParam`).
- `UsuarioController.atualizar`: `@PutMapping("/atualizar/{id}")`.
- `UsuarioController.deletar`: `@DeleteMapping("/{id}")` e retornar `ResponseEntity<Void>` com `204`.
- `OcorrenciaController.listarOcorrenciasPorId`: adicionar `@PathVariable Long id` e buscar ocorrência específica (ou remover `/{id}` e renomear para listagem).
- `OcorrenciaController.atualizar`: `@PutMapping("/atualizar/{id}")`.
- `OcorrenciaController.deletar`: `@DeleteMapping("/{id}")` e retornar `204`.
- Remover import não utilizado `org.w3c.dom.stylesheets.LinkStyle` de `UsuarioController`.
- Adicionar `@Valid` nos corpos dos controllers para acionar Bean Validation.

**Lógica de negócio:**
- `UsuarioService.atualizar`: hoje cria um novo `Usuario` sem `id`, `role` nem `senha`, podendo inserir duplicado ou falhar. Deve buscar a entidade existente, atualizar apenas campos permitidos e salvar.
- `OcorrenciaService.atualizar`: hoje recria a ocorrência com `dataCriacao = now()` e status `PENDENTE`. Deve carregar o registro existente e atualizar somente os campos editáveis.
- `OcorrenciaService.criar`: usar `Categoria.fromString(...)` em vez de `Categoria.valueOf(...)`; integrar geocodificação para preencher latitude/longitude; retornar `201`.
- `OcorrenciaResponseDTO`: não expor a entidade `Usuario` completa (risco de vazar `senha`); expor apenas dados públicos (nome/e-mail/id) ou o `UsuarioResponseDTO`.
- `TokenProvider.getExpireAt()` retorna o **tempo de expiração configurado**, não um timestamp absoluto; além disso, `cadastrar` usa `expiresAt` em ms e `login` divide por 1000, gerando inconsistência de contrato.

**Segurança:**
- `JwtAuthFilter.shouldNotFilter` ignora todo `/auth`; separar rotas públicas (`/auth/login`, `/auth/cadastrar`) de rotas protegidas (`/usuarios/...`) ou ajustar o filtro.
- Não há autorização por role; implementar `@PreAuthorize("hasRole('ADMIN')")` nos endpoints de listagem/estatísticas.
- Externalizar segredos (`jwt.key`, senha do banco, credenciais do compose) para variáveis de ambiente.

**Exceções:**
- `GlobalExceptionHandler` é uma `RuntimeException`, **não** um `@RestControllerAdvice`. Deve ser refatorado para tratar `ResourceNotFoundException` (404), `GeocodificacaoException` (502/422), `BadCredentialsException`/`UsernameNotFoundException` (401), validação (400) e erros genéricos (500), com JSON padronizado (timestamp, status, mensagem).

**Testes:**
- `UsuarioServiceTest`: falha com `NullPointerException` porque `TokenProvider` (e possivelmente `AuthenticationManager`) não é mockado em `cadastrar`.
- `MapaurbanoApplicationTests.contextLoads`: falha por tentar conectar a um PostgreSQL real.
- Não há testes para `OcorrenciaService`, `EstatisticaService`, controllers e filtros.

### 7.3. Módulos pendentes (esqueletos vazios)
| Módulo | Arquivos | O que falta |
|---|---|---|
| Geocodificação | `GeoapiService`, `GeoapiResponseDTO`, `WebClientConfig` | Implementar cliente HTTP, propriedades, DTO de resposta, chamada no fluxo de criação e tratamento via `GeocodificacaoException`. |
| Histórico de status | `HistoricoStatusService` | Implementar transição de status e persistência em `HistoricoStatus`; endpoint de consulta do histórico. |
| Estatísticas | `EstatisticaService`, `EstatisticaController` | Anotar controller, implementar agregações por status, categoria e bairro (usar `contarOcorrenciaPorCategoria`/`contarOcorrenciaPorBairro`), métricas temporais e `percentualResolvidos`. |
| Documentação | `OpenAPIConfig` | Configurar OpenAPI/Swagger (recomendado `springdoc-openapi`, dependência ainda ausente no `build.gradle`). |

### 7.4. Próximos passos sugeridos (ordem recomendada)
1. **Corrigir build/testes:** mockar `TokenProvider` em `UsuarioServiceTest`; criar `application-test.yaml` com H2 ou desabilitar Flyway/Postgres nos testes de contexto.
2. **Padronizar exceções:** transformar `GlobalExceptionHandler` em `@RestControllerAdvice`.
3. **Corrigir rotas/controllers:** ajustar `@PathVariable`/paths e retornar `204` nas exclusões; adicionar `@Valid`.
4. **Corrigir lógica de atualização:** buscar entidade existente em `UsuarioService.atualizar` e `OcorrenciaService.atualizar`.
5. **Implementar histórico de status:** endpoint + service + registro automático em `HistoricoStatus`.
6. **Implementar geocodificação:** `WebClientConfig` + `GeoapiService` + `GeoapiResponseDTO` + integração na criação de ocorrência.
7. **Implementar estatísticas:** `EstatisticaService` + exposição no `EstatisticaController`.
8. **Configurar OpenAPI/Swagger** via `OpenAPIConfig` + `springdoc-openapi`.
9. **Endurecer segurança:** separar rotas públicas/privadas, aplicar roles e externalizar segredos.
10. **Ampliar cobertura de testes** (services, controllers, filtros e segurança).

### 7.5. Erros já resolvidos (histórico — não reintroduzir)
| ID | Problema | Solução aplicada |
|---|---|---|
| ERR-001 | `HistoricoStatusRepository` estendia `JpaRepository<HistoricoStatusRepository, Long>` (auto-referência). | Corrigido para `JpaRepository<HistoricoStatus, Long>`. |
| ERR-002 | `JwtAuthFilter` não era bean e `SecurityConfig` não conseguia injetá-lo. | Adicionado `@Component`. |
| ERR-003 | `usuario.id` sem identity, incompatível com `GenerationType.IDENTITY`. | Migração `V5` com `GENERATED BY DEFAULT AS IDENTITY`. |
| ERR-004 | URL JDBC com banco errado, porta `8080:80` e app subindo antes do Postgres. | Corrigido banco na URL, porta `8080:8080` e `healthcheck` + `condition: service_healthy`. |

---

## 8. Referência Rápida de Comandos

```bash
# Build (gera o JAR)
./gradlew build

# Apenas compilar
./gradlew compileJava

# Executar testes
./gradlew test

# Subir a stack completa (PostgreSQL + app)
docker compose up --build

# Subir apenas o banco
docker compose up postgres

# Rodar a aplicação localmente (requer PostgreSQL acessível na URL configurada)
./gradlew bootRun
```

> **Pré-requisitos locais:** Java 21 e Docker/Docker Compose. A aplicação espera o PostgreSQL acessível conforme `spring.datasource.url` (por padrão, host `postgres`, porta `5432`, banco `mapaurbano`).

---

## 9. Pontos de Atenção para Quem For Assumir o Projeto

1. **O código atual tem inconsistências estruturais** (rotas quebradas, services recriando entidades em updates, módulos vazios). Não trate a documentação de memória como “tudo pronto”: ela própria lista pendências.
2. **Segurança não está madura:** não há autorização por role, rotas `/auth` não passam pelo filtro JWT e há segredos hardcoded.
3. **A geocodificação é apenas planejada:** não invente que existe integração configurada. Qualquer provedor/URL/chave precisa ser definido e implementado.
4. **`ddl-auto: update` + Flyway** é uma combinação arriscada; prefira `validate` após estabilizar o schema.
5. **A exposição da entidade `Usuario` em `OcorrenciaResponseDTO` pode vazar o hash de senha** — corrija antes de qualquer uso público.
6. **Testes não passam no estado atual** (contexto sem banco e mock ausente) — trate como prioridade para garantir uma base confiável.
