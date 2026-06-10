# Ocorrências — Sistema de Gestão de Registros

> Teste Técnico · Desenvolvedor Full Stack Java & Angular — Attus  
> Candidato: **Humberto Freitas da Silva Filho**
---

## O que é este sistema

O **Sistema de Gestão de Ocorrências** tem como funcionalidade central registrar, acompanhar e encerrar ocorrências vinculadas a clientes e localizações geográficas. Cada ocorrência agrega dados do cliente (CPF, nome, data de nascimento), o endereço onde o fato aconteceu, arquivos de evidência (fotos, PDFs, documentos) e passa por um ciclo de vida controlado: **ATIVA → FINALIZADA**.

---

## Por que esta funcionalidade se encaixa na Attus

A Attus fornece uma **procuradoria digital** para gestão de execução fiscal, cobrança extrajudicial e contencioso judicial em órgãos públicos. Nesse contexto, o módulo de ocorrências resolve um problema real que aparece em múltiplos fluxos do produto:

### Execução Fiscal e Cobrança Extrajudicial

Durante o acompanhamento de um processo de cobrança, surgem eventos que precisam ser documentados com rastreabilidade completa: notificações não entregues, contestações do devedor, vistorias de bens, acordos frustrados. Cada um desses eventos é, na prática, uma **ocorrência** — vinculada a um CPF, com localização, data e evidências documentais.

O ciclo `ATIVA → FINALIZADA` mapeia diretamente o encerramento de um caso: quando a dívida é quitada ou o processo arquivado, o registro se torna imutável. Isso é um requisito jurídico — **nenhuma evidência de um processo encerrado pode ser alterada retroativamente**.

### Contencioso Judicial

Em autos eletrônicos, cada movimentação processual é um registro que não pode ser adulterado após protocolo. O padrão de imutabilidade implementado aqui (bloqueio de edição e remoção após finalização, hash SHA-256 de cada arquivo) reflete exatamente esse requisito. Um arquivo enviado ao processo tem seu hash gravado no banco; qualquer substituição posterior seria detectável.

### Atendimento e Suporte às Procuradorias

Procuradores que utilizam o sistema precisam registrar ocorrências operacionais: indisponibilidade de sistemas integrados, falhas em integrações SOAP com tribunais, respostas incorretas de APIs externas. Ter um módulo de ocorrências com filtros por data, cidade e status permite que gestores identifiquem padrões, priorizem resoluções e gerem relatórios de SLA — exatamente o que o endpoint `GET /api/v1/ocorrencias` com seus quatro filtros combinados suporta.

### Observabilidade e Diagnóstico de Incidentes

O sistema atua como microsserviço e pode ser integrado com outros ecosistemas da arquitetura (REST/SOAP, Kafka, Camunda).
Quando um incidente ocorre em produção, o sistema oferece logs que auxiliam na identificação de problemas de forma isolada.

---

## Decisões de Arquitetura

### 1. Estrutura em Camadas

O projeto segue a separação clássica **Controller → Service → Repository**, com DTOs dedicados para request e response. Essa escolha visa:

- **Controle de exposição:** o `OcorrenciaResponse` não expõe a entidade diretamente, evitando vazamento de campos internos (e.g. `dscHash` da foto) e laços de serialização do Jackson.
- **Testabilidade:** cada camada é testada de forma isolada (unitário na service via Mockito, integração na controller via `@WebMvcTest`).
- **Evolução independente:** alterar a estrutura da entidade não força mudança na API.

### 2. Armazenamento de Arquivos — Local Storage

A ideia do projeto é utilizar **MinIO** (object storage S3-compatível). A versão entregue substituiu por **LocalStorageService** com volume Docker persistente (`uploads_data:/app/uploads`).

**Por quê:**
- Elimina dependência de runtime extra sem reduzir a funcionalidade entregável.
- O `ArquivoController` expõe os arquivos via `GET /api/v1/arquivos/**`, reproduzindo o comportamento de URL pública do MinIO.
- Para produção real, a interface `StorageService` poderia ser implementada com S3/MinIO sem alterar nenhum service ou controller.

**Trade-off:** URLs de download expiram de forma implícita se o volume for recriado. Em produção, URLs pré-assinadas do MinIO/S3 são mais seguras e escaláveis.

### 3 Autenticação com JWT Stateless

- Token JWT com expiração de **30 minutos** via `io.jsonwebtoken` (JJWT 0.12.5).
- `JwtAuthFilter` intercepta todas as requisições exceto `/api/v1/auth/**`, `/swagger-ui/**`, e o endpoint de download de arquivos.
- O endpoint de arquivo é público intencionalmente: qualquer pessoa com a URL pode baixar, o que é adequado para um sistema de ocorrências que precisa compartilhar evidências.

**Trade-off:** para maior segurança, URLs de arquivos deveriam ser assinadas temporariamente. Não foi implementado para simplificar.

### 4 Regra de Imutabilidade das Ocorrências FINALIZADAS

A lógica de negócio central é que uma ocorrência `FINALIZADA`:
- Não pode receber novos arquivos
- Não pode ser deletada
- Não pode ser editada (status é terminal)

Isso é verificado na camada de `Service` e não no controller, o que garante que a regra vale para qualquer ponto de entrada futuro (e.g. um job agendado, mensageria).

### 5 Reutilização de Cliente por CPF

No cadastro de ocorrência (`POST /api/v1/ocorrencias`), o sistema busca um cliente existente pelo CPF antes de criar um novo. Isso permite que o mesmo cliente apareça em múltiplas ocorrências sem duplicação de dados.

```java
// ClienteService.java
public Cliente buscarOuCriarPorCpf(ClienteRequest request) {
    String cpfLimpo = limparCpf(request.getNroCpf());
    return clienteRepository.findByNroCpf(cpfLimpo)
        .orElseGet(() -> { /* cria novo cliente */ });
}
```

### 6 Paginação e Filtros com JPQL

A listagem de ocorrências usa uma `@Query` JPQL com `JOIN FETCH` para evitar o problema N+1 (carregar cliente e endereço em uma única query).

Os filtros são dinâmicos usando `IS NULL OR` em vez de `Specification`, o que é suficiente para o volume esperado e mais legível.

### 7 Migrations com Flyway

Toda a estrutura do banco é gerenciada via Flyway (`V1__create_tables.sql`, `V2__create_usuario.sql`). Isso garante:
- Reproducibilidade do ambiente
- Rastreabilidade de mudanças no esquema
- O usuário admin é inserido na migration V2 com senha já hasheada em BCrypt


## Parte 2 — Análise de Incidente

### 1. Cenário

O time de suporte reporta que, desde a última implantação, o endpoint `POST /api/v1/ocorrencias` falha de forma intermitente com HTTP 500. O erro acontece em ~15% das requisições, especialmente durante pico de uso (09h–11h). Os logs estão abaixo.

---

### 2. Logs do Incidente

```
2026-04-09 09:14:32.441 ERROR [ocorrencias-api,req-7f3a] c.h.a.o.exception.GlobalExceptionHandler - Erro inesperado
java.lang.RuntimeException: Falha ao armazenar arquivo: /app/uploads/ocorrencias/47 (Too many open files)
    at c.h.a.o.service.LocalStorageService.upload(LocalStorageService.java:38)
    at c.h.a.o.service.FotoOcorrenciaService.adicionarFotos(FotoOcorrenciaService.java:35)
    at c.h.a.o.service.OcorrenciaService.cadastrar(OcorrenciaService.java:32)
    ...

2026-04-09 09:17:18.009 ERROR [ocorrencias-api,req-2b9c] c.h.a.o.exception.GlobalExceptionHandler - Erro inesperado
java.lang.RuntimeException: Falha ao armazenar arquivo: /app/uploads/ocorrencias/51 (Too many open files)
    at c.h.a.o.service.LocalStorageService.upload(LocalStorageService.java:38)
    ...

2026-04-09 09:31:54.772 WARN  [ocorrencias-api,req-8e1d] c.h.a.o.service.LocalStorageService - Erro ao salvar arquivo
java.io.IOException: /app/uploads/ocorrencias/63/a4f2...uuid.jpg: No space left on device
    at java.base/sun.nio.ch.FileChannelImpl.write(FileChannelImpl.java:281)
    ...

2026-04-09 09:45:01.113 ERROR [ocorrencias-api,req-9a4f] org.hibernate.exception.JDBCConnectionException
com.zaxxer.hikari.pool.HikariPool$PoolInitializationException: Failed to initialize pool: 
    FATAL: remaining connection slots are reserved for non-replication superuser connections
    at com.zaxxer.hikari.pool.HikariPool.checkFailFast(HikariPool.java:576)
    ...

2026-04-09 09:47:22.331 ERROR [ocorrencias-api,req-c01b] c.h.a.o.exception.GlobalExceptionHandler - Erro inesperado
org.springframework.dao.DataIntegrityViolationException: 
    ERROR: duplicate key value violates unique constraint "cliente_nro_cpf_key"
    Detail: Key (nro_cpf)=(98765432100) already exists.
    at c.h.a.o.service.ClienteService.buscarOuCriarPorCpf(ClienteService.java:58)
    ...
```

---

### 3. Análise por Tipo de Erro

#### Erro 1 — `Too many open files` (FileDescriptor Leak)

**Causa raiz:** O método `LocalStorageService.upload()` chama `arquivo.transferTo(destPath)` e depois `arquivo.getBytes()` para calcular o hash. Em condições de alta concorrência, o Spring pode não fechar os file descriptors intermediários a tempo, acumulando até ultrapassar o limite do SO (`ulimit -n`).

**Evidência:** o erro aparece sistematicamente no horário de pico (09h–11h), indicando relação direta com volume de requisições.

**Correção:**

```java
// ANTES (problemático)
public UploadResult upload(MultipartFile arquivo, String prefixo) {
    arquivo.transferTo(destPath);                  // abre FD
    String hash = calcularHash(arquivo.getBytes()); // lê bytes novamente
    return new UploadResult(relativePath, hash);
}

// DEPOIS (corrigido)
public UploadResult upload(MultipartFile arquivo, String prefixo) throws IOException {
    byte[] bytes = arquivo.getBytes(); // lê uma única vez em memória
    try (InputStream is = new ByteArrayInputStream(bytes)) {
        Files.copy(is, destPath, StandardCopyOption.REPLACE_EXISTING);
    }
    String hash = calcularHash(bytes);
    return new UploadResult(relativePath, hash);
}
```

**Prevenção:** configurar `ulimit -n 65535` no Dockerfile/container e adicionar health check de file descriptors.

---

#### Erro 2 — `No space left on device`

**Causa raiz:** o volume `/app/uploads` está cheio. Uploads de arquivos grandes sem limpeza ou quotas esgotam o espaço em disco.

**Correção imediata:** ampliar o volume Docker ou limpar arquivos antigos.

**Correção definitiva:**
1. Validar tamanho máximo no controller antes de processar (`@RequestPart` com validação custom ou verificar `arquivo.getSize()`).
2. Configurar política de retenção: após ocorrência `FINALIZADA`, mover fotos para cold storage (S3 Glacier).
3. Monitorar uso de disco com alerta em 80% de capacidade.

```java
// Adicionar no FotoOcorrenciaService
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

for (MultipartFile arquivo : arquivos) {
    if (arquivo.getSize() > MAX_FILE_SIZE) {
        throw new BusinessException("Arquivo " + arquivo.getOriginalFilename() + 
            " excede o tamanho máximo de 10MB");
    }
}
```

---

#### Erro 3 — `FATAL: remaining connection slots are reserved` (HikariCP)

**Causa raiz:** pool de conexões HikariCP esgotado. O PostgreSQL tem um limite de `max_connections` (padrão: 100), e a aplicação está abrindo mais conexões do que o banco permite — provavelmente porque conexões não estão sendo devolvidas ao pool corretamente (e.g. transação não fechada por exceção não tratada).

**Correção:**

```yaml
# application.yml — configurar pool adequadamente
spring:
  datasource:
    hikari:
      maximum-pool-size: 10        # não exceder max_connections do PostgreSQL
      minimum-idle: 2
      connection-timeout: 30000    # 30s — falha rápido em vez de travar
      idle-timeout: 600000         # 10min
      max-lifetime: 1800000        # 30min
      leak-detection-threshold: 15000  # detectar conexões não devolvidas
```

```sql
-- No PostgreSQL, reservar mais slots para a aplicação
ALTER SYSTEM SET max_connections = 200;
```

**Prevenção:** nunca abrir conexão fora de um `@Transactional`. Verificar que toda exceção em método transacional faz rollback e devolve a conexão ao pool.

---

#### Erro 4 — `duplicate key value violates unique constraint "cliente_nro_cpf_key"` (Race Condition)

**Causa raiz:** race condition na lógica de `buscarOuCriarPorCpf`. Quando duas requisições simultâneas chegam com o mesmo CPF, ambas executam o `findByNroCpf` antes de qualquer uma chamar o `save`, e ambas tentam inserir — uma falha com violação de constraint.

```
Thread A: findByNroCpf("98765432100") → NOT FOUND
Thread B: findByNroCpf("98765432100") → NOT FOUND (antes de A salvar)
Thread A: save(novoCLiente)           → OK
Thread B: save(novoCliente)           → ERRO: duplicate key
```

**Correção:** usar `INSERT ... ON CONFLICT DO NOTHING` via JPQL ou tratar a exceção e fazer re-fetch:

```java
// Opção 1: Tratar DataIntegrityViolationException
public Cliente buscarOuCriarPorCpf(ClienteRequest request) {
    String cpfLimpo = limparCpf(request.getNroCpf());
    try {
        return clienteRepository.findByNroCpf(cpfLimpo)
            .orElseGet(() -> {
                Cliente novo = new Cliente();
                novo.setNmeCliente(request.getNmeCliente());
                novo.setDtaNascimento(request.getDtaNascimento());
                novo.setNroCpf(cpfLimpo);
                return clienteRepository.save(novo);
            });
    } catch (DataIntegrityViolationException e) {
        // Race condition: outro thread criou o cliente simultaneamente
        return clienteRepository.findByNroCpf(cpfLimpo)
            .orElseThrow(() -> new BusinessException("Erro ao criar cliente"));
    }
}
```

```java
// Opção 2: Lock pessimista na busca (mais seguro para alta concorrência)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Cliente c WHERE c.nroCpf = :cpf")
Optional<Cliente> findByNroCpfLocked(@Param("cpf") String cpf);
```

---

### 4. Medidas de Prevenção Gerais

| # | Medida | Impacto |
|---|--------|---------|
| 1 | **Logs estruturados com `requestId`** — adicionar MDC no filter para correlacionar logs de uma mesma requisição | Diagnóstico muito mais rápido |
| 2 | **Health checks** — endpoint `/actuator/health` com verificação de disco, conexão DB e memória | Detectar problemas antes de impactar usuário |
| 3 | **Circuit breaker** — Resilience4j para isolar falha de storage e impedir cascata | Resiliência |
| 4 | **Testes de carga** — rodar k6/JMeter antes de cada deploy para identificar gargalos | Prevenção |
| 5 | **Alertas de infra** — Prometheus + Grafana monitorando uso de disco, pool size, latência P99 | Observabilidade |
| 6 | **Validação de arquivo na entrada** — checar MIME type real, não apenas extensão | Segurança + estabilidade |

---

### 5 Implementação de Log Mínimo para Diagnóstico

O projeto já possui `log.error("Erro inesperado", ex)` e `log.info("Arquivo salvo: {}", destPath)`. Para diagnóstico mais eficiente, recomenda-se adicionar um **MDC filter**:

```java
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        MDC.put("path", ((HttpServletRequest) request).getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

Com `%X{requestId}` no padrão de log, todos os logs de uma mesma requisição ficam correlacionados — exatamente como aparece nos logs do incidente acima (`[ocorrencias-api,req-7f3a]`).

---
# APÊNDICE
## INFORMAÇÕES TÉCNICAS DO BACKEND E FRONDEND

## Funcionalidades Implementadas

### Para o Usuário Final (Front-end Angular)

| Funcionalidade | Como acessa |
|---|---|
| Login com JWT e sessão persistente | `/login` |
| Dashboard com estatísticas e últimas ocorrências | `/dashboard` |
| Listar ocorrências com 4 filtros simultâneos + paginação + ordenação | `/ocorrencias` |
| Cadastrar ocorrência em 3 etapas (stepper) com upload de arquivos | `/ocorrencias/nova` |
| Ver detalhes completos de uma ocorrência com galeria de arquivos | `/ocorrencias/:id` |
| Finalizar ocorrência (irreversível, com confirmação) | Botão na lista ou detalhe |
| Adicionar arquivos a uma ocorrência ativa | Página de detalhe |
| CRUD completo de clientes via dialog | `/clientes` |
| Logout automático com redirecionamento quando o JWT expira | Automático via interceptor |

### Para a API (Back-end Spring Boot)

| Módulo | O que entrega |
|---|---|
| Autenticação JWT | Token de 30 min, filtro stateless, rotas públicas configuradas |
| Gestão de Clientes | CRUD com validação de CPF (algoritmo), unicidade por CPF |
| Gestão de Endereços | CRUD com validação de CEP (8 dígitos) |
| Gestão de Ocorrências | Cadastro unificado, filtros combinados, paginação, ciclo de vida |
| Upload de Arquivos | Armazenamento local com volume Docker, hash SHA-256, URL de download pública |
| Diagnóstico | Logs estruturados, Problem Details, preparado para MDC/Kibana |


## Repositório

```
/
├── ocorrencias-api/        ← Back-end (Java 17 + Spring Boot 3.5)
│   ├── src/main/           ← Código de produção
│   ├── src/test/           ← Testes unitários e de controller
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── .env.development
│   └── docs/
│       └── diagrama_banco_dados.svg
├── ocorrencias-frontend/   ← Front-end (Angular 17 + Angular Material)
│   ├── src/app/
│   │   ├── core/           ← Guards, interceptors, services, models
│   │   ├── layout/         ← Shell, Sidebar, Topbar
│   │   ├── pages/          ← Login, Dashboard, Ocorrências, Clientes
│   │   └── shared/         ← Componentes reutilizáveis
│   └── proxy.conf.json
└── README.md               ← README GERAL 
```

---

## Stack Tecnológica

### Back-end
| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.5 | Framework de aplicação |
| Spring Security | 6 | Autenticação JWT stateless |
| Spring Data JPA | 3.5 | Persistência / queries JPQL |
| PostgreSQL | 16 | Banco de dados relacional |
| Flyway | 10 | Migrações versionadas do schema |
| JJWT | 0.12.5 | Geração e validação de tokens JWT |
| SpringDoc OpenAPI | 2.8 | Documentação Swagger automática |
| Docker Compose | — | Orquestração local de serviços |
| JUnit 5 + Mockito | — | Testes unitários e de controller |

### Front-end
| Tecnologia | Versão | Papel |
|---|---|---|
| Angular | 17 | Framework SPA (Standalone Components) |
| Angular Material | 17 | Design system + componentes UI |
| RxJS | 7 | Programação reativa / HTTP |
| Angular Signals | 17 | Estado reativo nos components |
| TypeScript | 5 | Tipagem estática |
| SCSS | — | Estilização com tema escuro customizado |

---

## Pré-requisitos

- **Docker** e **Docker Compose** (para o back-end)
- **Node.js 18+** e **npm 9+** (para o front-end)

---


## Como Executar

### ✅ Opção 1 — Tudo com Docker (recomendado)

Sobe PostgreSQL + API + Frontend com um único comando, a partir da **raiz do repositório**:

```bash
docker compose --env-file .env.development up -d --build
```

Aguarde ~60–90s na primeira execução (Maven e npm baixam dependências). Nas execuções seguintes o cache do Docker reduz para ~15s.

> **Atenção:** use o `docker-compose.yml` da **raiz**, não o de dentro de `ocorrencias-api/`.

A ordem de inicialização é garantida por healthchecks:

```
postgres (healthy) → api (healthy) → frontend
postgres (healthy) → pgadmin
```

Serviços disponíveis após subir:

| Serviço | URL | Credenciais |
|---|---|---|
| **Frontend Angular** | http://localhost:4200 | admin@admin.com.br / admin123 |
| **API REST** | http://localhost:8080 | — |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | — |
| **pgAdmin** | http://localhost:5050 | admin@admin.com / admin |
| **PostgreSQL** | localhost:5432 | postgres / postgres |

---

### 🔧 Opção 2 — Desenvolvimento local (hot-reload no frontend)

Sobe apenas banco e API no Docker, e o frontend com `ng serve` para ter hot-reload:

```bash
# Terminal 1 — banco + API
docker compose --env-file .env.development up postgres api -d --build

# Terminal 2 — frontend com hot-reload
cd ocorrencias-frontend
npm install
npm start
```

Acesse **http://localhost:4200**

O `proxy.conf.json` redireciona `/api/*` para `localhost:8080`, eliminando CORS em desenvolvimento.

---

### 🐳 Opção 3 — Só a API (sem frontend)

Dentro da pasta `ocorrencias-api/`:

```bash
cd ocorrencias-api
docker compose --env-file .env.development up -d --build
```

---

### Parar os containers

```bash
# Na raiz — para o compose completo
docker compose down          # mantém dados nos volumes
docker compose down -v       # remove volumes (apaga banco e uploads)

# Rebuild forçado (sem cache — use após mudanças em arquivos Java ou SCSS)
docker compose --env-file .env.development up -d --build --no-cache
```

---

### Variáveis de Ambiente

O arquivo `.env.development` na raiz contém todas as variáveis necessárias:

```env
# PostgreSQL
POSTGRES_DB=ocorrencias_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# pgAdmin
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=admin

# API
DB_HOST=postgres
DB_PORT=5432
DB_NAME=ocorrencias_db
DB_USER=postgres
DB_PASS=postgres
JWT_SECRET=3f8a7b2c9d4e1f6a0b5c8d3e7f2a9b4c1d6e0f3a8b7c2d5e9f4a1b6c0d3e7f
SERVER_PORT=8080
APP_BASE_URL=http://localhost:8080

# CORS — origens permitidas (separar por vírgula)
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:80,http://localhost
```

> Para produção, crie um `.env` com os mesmos campos e valores seguros — especialmente `JWT_SECRET`, senhas do banco e `CORS_ALLOWED_ORIGINS` com o domínio real.

---

## Credenciais Padrão

```
Email: admin@admin.com.br
Senha: admin123
```

O usuário administrador é criado automaticamente pela migration `V2__create_usuario.sql` com senha hasheada em BCrypt.

---


## Diagrama do Banco de Dados

![Diagrama ER](./docs/diagrama_banco_dados.svg)

```
usuario          → autenticação (email + senha BCrypt)
cliente          → CPF único, dados pessoais
endereco         → localização da ocorrência
ocorrencia       → vincula cliente + endereço, ciclo de vida ATIVA → FINALIZADA
foto_ocorrencia  → arquivos vinculados a uma ocorrência, com hash SHA-256
```

Índices criados: `cod_cliente`, `cod_endereco`, `sta_ocorrencia`, `dta_ocorrencia`, `cod_ocorrencia` (em foto).

---

## API — Referência de Endpoints

> Documentação interativa completa: **http://localhost:8080/swagger-ui.html**  
> Todos os endpoints (exceto `/api/v1/auth/login` e `/api/v1/arquivos/**`) exigem o header:  
> `Authorization: Bearer <token>`

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Autentica e retorna token JWT (expira em 30min) |

```json
// Request
{ "email": "admin@admin.com.br", "senha": "admin123" }

// Response 200
{ "token": "eyJhbGci...", "tipo": "Bearer", "expiracaoMs": 1800000 }
```

### Clientes

| Método | Endpoint | Descrição | Status de sucesso |
|---|---|---|---|
| `GET` | `/api/v1/clientes` | Listar (paginado, sort por `nmeCliente`) | 200 |
| `GET` | `/api/v1/clientes/{id}` | Buscar por ID | 200 |
| `POST` | `/api/v1/clientes` | Criar (CPF validado pelo algoritmo) | 201 + Location |
| `PUT` | `/api/v1/clientes/{id}` | Atualizar | 200 |
| `DELETE` | `/api/v1/clientes/{id}` | Remover | 204 |

### Endereços

| Método | Endpoint | Descrição | Status de sucesso |
|---|---|---|---|
| `GET` | `/api/v1/enderecos` | Listar (paginado) | 200 |
| `GET` | `/api/v1/enderecos/{id}` | Buscar por ID | 200 |
| `POST` | `/api/v1/enderecos` | Criar (CEP deve ter 8 dígitos) | 201 + Location |
| `PUT` | `/api/v1/enderecos/{id}` | Atualizar | 200 |
| `DELETE` | `/api/v1/enderecos/{id}` | Remover | 204 |

### Ocorrências

| Método | Endpoint | Descrição | Status de sucesso |
|---|---|---|---|
| `POST` | `/api/v1/ocorrencias` | Cadastrar — `multipart/form-data` | 201 + Location |
| `GET` | `/api/v1/ocorrencias` | Listar com filtros, paginação e sort | 200 |
| `GET` | `/api/v1/ocorrencias/{id}` | Buscar por ID | 200 |
| `PATCH` | `/api/v1/ocorrencias/{id}/finalizar` | Finalizar (irreversível) | 200 |
| `POST` | `/api/v1/ocorrencias/{id}/arquivos` | Upload de arquivos — `multipart/form-data` | 200 |
| `DELETE` | `/api/v1/ocorrencias/{id}` | Remover (bloqueado se FINALIZADA) | 204 |

**Filtros disponíveis no `GET /api/v1/ocorrencias`:**

| Parâmetro | Tipo | Exemplo |
|---|---|---|
| `nmeCliente` | String parcial | `?nmeCliente=Maria` |
| `nroCpf` | String | `?nroCpf=987.654.321-00` |
| `dtaOcorrencia` | `yyyy-MM-dd` | `?dtaOcorrencia=2026-04-09` |
| `nmeCidade` | String parcial | `?nmeCidade=Curitiba` |
| `sort` | `campo,direção` | `?sort=dtaOcorrencia,desc` |

Campos ordenáveis: `dtaOcorrencia`, `endereco.nmeCidade`

**Regras de negócio da ocorrência:**
- Uma ocorrência **FINALIZADA** não pode ser editada, não aceita novos arquivos e não pode ser removida.
- O campo `staOcorrencia` é terminal — a transição `ATIVA → FINALIZADA` é irreversível.
- Tentativas de violação retornam HTTP `422 Unprocessable Entity` com mensagem clara.

### Arquivos

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/arquivos/**` | Download de arquivo por caminho relativo (público) |

---

## Testes

### Executar

```bash
cd ocorrencias-api
./mvnw test
```

### Cobertura

Os testes cobrem dois níveis:

**Unitários (Mockito)** — isolam cada service do banco e do storage:
- `OcorrenciaServiceTest` — cadastro, busca, finalização, deleção, regras de imutabilidade
- `ClienteServiceTest` — CRUD, validação de CPF duplicado
- `EnderecoServiceTest` — CRUD
- `FotoOcorrenciaServiceTest` — upload, bloqueio em ocorrência finalizada
- `LocalStorageServiceTest` — persistência de arquivo, cálculo de hash
- `AuthServiceTest` — autenticação, geração de token

**Controller (WebMvcTest)** — validam contratos HTTP sem subir servidor:
- `OcorrenciaControllerTest` — 201, 400, 404, 422, 401
- `ClienteControllerTest` — 201, 200, 404, 400, 401
- `EnderecoControllerTest` — CRUD completo
- `AuthControllerTest` — 200 e 401

### Cenários de erro testados explicitamente

| Cenário | HTTP Esperado |
|---|---|
| Requisição sem token | 401 |
| CPF com dígitos verificadores inválidos | 400 |
| CEP com menos de 8 dígitos | 400 |
| Campo obrigatório ausente | 400 |
| Recurso não encontrado | 404 |
| Finalizar ocorrência já finalizada | 422 |
| Upload em ocorrência finalizada | 422 |
| Deletar ocorrência finalizada | 422 |

---

## Arquitetura do Front-end

### Estrutura de Pastas

```
src/app/
├── core/
│   ├── guards/          → authGuard (CanActivateFn)
│   ├── interceptors/    → authInterceptor (Bearer), errorInterceptor (401 → logout)
│   ├── models/          → interfaces TypeScript completas
│   └── services/        → AuthService, OcorrenciasService, ClientesService, EnderecosService
├── layout/
│   ├── shell/           → Layout raiz autenticado
│   ├── sidebar/         → Navegação com RouterLinkActive
│   └── topbar/          → Menu do usuário + logout
├── pages/
│   ├── login/           → Reactive Forms + validação de email
│   ├── dashboard/       → forkJoin para carregamento paralelo + skeleton loading
│   ├── ocorrencias/
│   │   ├── lista/       → mat-table + filtros com debounceTime(400)
│   │   ├── nova/        → mat-stepper linear 3 etapas + validação CPF
│   │   └── detalhe/     → @Input binding via router + galeria de arquivos
│   └── clientes/
│       ├── lista/       → Tabela paginada
│       └── form/        → MatDialog reutilizável para criar/editar
└── shared/
    ├── confirm-dialog/  → Dialog genérico com variante isDanger
    ├── upload-zone/     → Drag-and-drop + MatChipRow
    └── status-badge/    → Badge ATIVA / FINALIZADA
```

### Fluxo de Autenticação

```
Acesso a rota protegida
        ↓
authGuard → AuthService.isLoggedIn() [Angular Signal]
        ↓ false
redirect /login
        ↓ login bem-sucedido
JWT salvo em localStorage
AuthService._token.set(token)
        ↓
authInterceptor injeta Bearer em toda requisição
        ↓ HTTP 401 em qualquer chamada
errorInterceptor → auth.logout() → redirect /login?expired=true
```

---

## Logs e Diagnóstico

A API produz logs estruturados via SLF4J/Logback em três níveis principais:

```
INFO  LocalStorageService  - Arquivo salvo: /app/uploads/ocorrencias/47/uuid.jpg
ERROR GlobalExceptionHandler - Erro inesperado [stack trace completo]
WARN  LocalStorageService  - Erro ao salvar arquivo
```
