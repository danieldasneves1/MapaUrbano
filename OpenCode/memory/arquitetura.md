# Arquitetura do Projeto Mapa Urbano

## 1. Visão Geral
O **Mapa Urbano** é uma API REST desenvolvida em Spring Boot / Java com o objetivo de registrar, acompanhar e mapear problemas urbanos (como buracos em vias, falhas na iluminação pública, acúmulo de lixo, vazamento de água e saneamento) relatados por cidadãos, permitindo geolocalização, histórico de alterações de status e consolidação de dados estatísticos por região.

---

## 2. Stack Tecnológica
- **Linguagem**: Java 21 (toolchain Eclipse Temurin)
- **Framework Principal**: Spring Boot 4.1.1
- **Módulos Spring**:
  - `spring-boot-starter-webmvc`: Exposição de APIs RESTful baseadas em Servlets.
  - `spring-boot-starter-data-jpa`: Camada de persistência ORM via Hibernate.
  - `spring-boot-starter-security`: Segurança, controle de acesso e autenticação.
  - `spring-boot-starter-validation`: Validação declarativa via Jakarta Bean Validation.
  - `spring-boot-starter-flyway`: Controle de versionamento e migração de banco de dados.
- **Banco de Dados**: PostgreSQL 15 (`org.postgresql:postgresql`, `org.flywaydb:flyway-database-postgresql`).
- **Autenticação & JWT**: `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (versão 0.12.5).
- **Utilitários e Boilerplate**: Project Lombok (`compileOnly` e `annotationProcessor`).
- **Gerenciador de Build**: Gradle 9.8.1 (Gradle Wrapper).
- **Containerização & DevOps**:
  - `Dockerfile`: Multi-stage build com `eclipse-temurin:21-jdk-alpine` (builder) e `eclipse-temurin:21-jre-alpine` (execução com usuário sem privilégios `spring:spring`).
  - `docker-compose.yaml`: Orquestração de containers do serviço PostgreSQL 15 e da aplicação backend com healthcheck de banco.

---

## 3. Padrão Arquitetural e Estrutura de Diretórios
A aplicação adota uma **Arquitetura em Camadas (Layered Architecture)**, dividida em Controller, Service, Repository e Entity, com forte encapsulamento através de DTOs baseados em Java Records.

Pacote raiz: `v1.mapaurbano`

### Estrutura de Pacotes:
- **`config`**: Configurações de infraestrutura do sistema.
  - `SecurityConfig`: Configuração da cadeia de filtros do Spring Security (sessão stateless, CSRF desabilitado, rotas públicas/privadas).
  - `JwtAuthFilter`: Filtro por requisição (`OncePerRequestFilter`) responsável pela extração e validação do token JWT no header `Authorization`.
  - `TokenProvider`: Emissor e validador de tokens JWT com chave simétrica HMAC-SHA.
  - `UserDetailsServiceImpl`: Implementação de `UserDetailsService` para carregar detalhes do usuário via repositório.
  - `OpenAPIConfig`: Ponto de extensão para configuração de documentação Swagger/OpenAPI.
  - `WebClientConfig`: Ponto de extensão para cliente HTTP REST para serviços externos.
- **`controller`**: Controladores REST responsáveis por receber requisições HTTP, delegar a execução para serviços e retornar respostas HTTP com DTOs.
  - `UsuarioController`: Endpoints de autenticação, login, gestão e listagem de usuários.
  - `OcorrenciaController`: Endpoints de cadastro, consulta, atualização e remoção de ocorrências.
  - `EstatisticaController`: Endpoints de métricas agregadas por status, categoria e bairro.
- **`service`**: Regras de negócio, validações lógicas e orquestração de operações entre repositórios e serviços auxiliares.
  - `UsuarioService`: Criação de conta, login, codificação de senha e busca de perfis.
  - `OcorrenciaService`: Ciclo de vida da ocorrência (criação com status inicial, listagem por usuário, atualização e deleção).
  - `HistoricoStatusService`: Rastreamento de transições de status da ocorrência.
  - `EstatisticaService`: Consolidação de métricas e contagens.
  - `GeoapiService`: Integração planejada para busca de geolocalização e coordenadas a partir de logradouro/bairro.
- **`repository`**: Interfaces que herdam de `JpaRepository` com consultas derivadas e JPQL personalizadas.
  - `UsuarioRepository`: Consultas de usuário por email.
  - `OcorrenciaRepository`: Consultas por usuário logado e agregações analíticas (`contarOcorrenciaPorBairro`).
  - `HistoricoStatusRepository`: Operações na tabela de histórico de status.
- **`dto`**: Objetos imutáveis para transporte de dados (Records), isolando entidades do contrato de API:
  - `dto.request`: `LoginRequestDTO`, `UsuarioRequestDTO`, `OcorrenciaRequestDTO`, `AtualizarStatusDTO`.
  - `dto.response`: `TokenResponseDTO`, `UsuarioResponseDTO`, `OcorrenciaResponseDTO`, `EstatisticaBairroDTO`, `GeoapiResponseDTO`.
- **`entity`**: Classes mapeadas pelo JPA representando as tabelas do banco de dados:
  - `Usuario`: Usuário do sistema implementando `UserDetails`.
  - `Ocorrencia`: Registro principal do problema urbano reportado.
  - `HistoricoStatus`: Registro de auditoria temporal de mudanças de status.
  - `entity.enums`: `Role` (permissões de usuário), `Categoria` (tipos de ocorrências) e `StatusOcorrencia` (estados do fluxo).
- **`exception`**: Classes de exceções customizadas para erros de negócio e infraestrutura (`ResourceNotFoundException`, `GeocodificacaoException`, `GlobalExceptionHandler`).

---

## 4. Banco de Dados e Migrações (Flyway)
O banco de dados relacional é o **PostgreSQL**. O controle de esquema é feito de maneira incremental e versionada pelo **Flyway** em `src/main/resources/db/migration/`.

### Histórico de Migrações:
1. `V1__criar_tabela_usuario.sql`:
   - Cria tabela `usuario` (`id BIGINT PRIMARY KEY`, `nome VARCHAR(100)`, `email VARCHAR(100) UNIQUE`, `senha VARCHAR(100)`).
2. `V2__criar_tabela_ocorrencia.sql`:
   - Cria tabela `ocorrencia` (`id BIGINT PRIMARY KEY`, `descricao`, `categoria`, `logradouro`, `numero`, `bairro`, `cidade`, `longitude DECIMAL(10,8)`, `latitude DECIMAL(10,8)`, `status`, `data_criacao`, `usuario_id BIGINT`).
   - Foreign Key: `fk_usuario` referenciando `usuario(id)` com `ON DELETE SET NULL`.
3. `V3__criar_tabela_historicostatus.sql`:
   - Cria tabela `HistoricoStatus` (`id BIGINT PRIMARY KEY`, `status_anterior`, `status_novo`, `data_alteracao`, `ocorrencia_id BIGINT`).
   - Foreign Key: `fk_ocorrencia` referenciando `ocorrencia(id)` com `ON DELETE CASCADE`.
4. `V4__criar_coluna_role_usuario.sql`:
   - Adiciona coluna `role VARCHAR(15)` à tabela `usuario`.
5. `V5__alterar_coluna_id_usuario.sql`:
   - Altera coluna `id` da tabela `usuario` para `GENERATED BY DEFAULT AS IDENTITY`, compatibilizando com `GenerationType.IDENTITY` do JPA.

### Modelo Entidade-Relacionamento:
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
- Um `Usuario` pode registrar várias `Ocorrencia`s (relação 1:N). Se o usuário for deletado, `usuario_id` na ocorrência torna-se nulo (`ON DELETE SET NULL`).
- Uma `Ocorrencia` possui múltiplos registros em `HistoricoStatus` (relação 1:N). Ao excluir uma ocorrência, seu histórico é removido em cascata (`ON DELETE CASCADE`).
