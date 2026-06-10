# Ocorrências — Frontend Angular

Frontend SPA desenvolvido em **Angular 17** com **Angular Material**, consumindo a API REST `ocorrencias-api`.

## Stack

- Angular 17 (Standalone Components, Signals)
- Angular Material 17 (tema escuro customizado)
- TypeScript 5
- SCSS
- RxJS

## Estrutura de Pastas

```
src/app/
├── core/
│   ├── guards/           # authGuard (CanActivate)
│   ├── interceptors/     # authInterceptor, errorInterceptor
│   ├── models/           # interfaces TypeScript
│   └── services/         # AuthService, OcorrenciasService, ClientesService, EnderecosService
├── layout/
│   ├── shell/            # Layout raiz autenticado (sidebar + topbar + router-outlet)
│   ├── sidebar/          # Navegação lateral com RouterLinkActive
│   └── topbar/           # Barra superior com menu do usuário
├── pages/
│   ├── login/            # Página de login com Reactive Forms
│   ├── dashboard/        # Estatísticas e últimas ocorrências
│   ├── ocorrencias/
│   │   ├── lista/        # Tabela com filtros, paginação, sort
│   │   ├── nova/         # Stepper 3 etapas + upload de arquivos
│   │   └── detalhe/      # Detalhes + gerenciamento de fotos
│   └── clientes/
│       ├── lista/        # Tabela de clientes
│       └── form/         # Dialog de criação/edição
└── shared/
    ├── confirm-dialog/   # Dialog de confirmação reutilizável
    ├── upload-zone/      # Zona de drag-and-drop para arquivos
    └── status-badge/     # Badge de status ATIVA/FINALIZADA
```

## Funcionalidades

| Módulo | Funcionalidades |
|--------|----------------|
| **Autenticação** | Login com JWT, guard de rota, interceptor Bearer, logout automático na expiração |
| **Dashboard** | Cards de estatísticas, lista das últimas ocorrências |
| **Ocorrências — Lista** | Filtros por nome/CPF/cidade/data, paginação, ordenação, finalizar, deletar |
| **Ocorrências — Nova** | Stepper 3 etapas, validação de CPF (algoritmo), máscara de CPF/CEP, upload de arquivos |
| **Ocorrências — Detalhe** | Todos os dados, galeria de imagens, adicionar arquivos, finalizar, deletar |
| **Clientes** | CRUD completo via dialog, paginação |

## Pré-requisitos

- Node.js 18+
- npm 9+

## Como executar

### 1. Instalar dependências

```bash
npm install
```

### 2. Subir o backend

```bash
cd ../ocorrencias-api
docker compose --env-file .env.development up -d
```

### 3. Iniciar o servidor de desenvolvimento

```bash
npm start
# ou
ng serve
```

O Angular CLI está configurado com `proxy.conf.json` para redirecionar `/api/*` ao backend em `localhost:8080`, evitando erros de CORS.

Acesse: **http://localhost:4200**

### 4. Build de produção

```bash
npm run build
# Saída: dist/ocorrencias-frontend/browser/
```

## Autenticação e Rotas

Todas as rotas dentro do `ShellComponent` são protegidas pelo `authGuard`. Se o token não estiver presente ou expirar (interceptado pelo `errorInterceptor` no HTTP 401), o usuário é redirecionado para `/login`.

### Fluxo

```
Usuário acessa qualquer rota protegida
        ↓
authGuard verifica AuthService.isLoggedIn() (signal)
        ↓ (não autenticado)
Redireciona para /login
        ↓ (login bem-sucedido)
JWT salvo no localStorage
AuthService.isLoggedIn() = true
        ↓
Redireciona para /dashboard
```

### Credenciais padrão

```
Email: admin@admin.com.br
Senha: admin123
```

## Variáveis de Ambiente

Configure em `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'  // URL base da API
};
```

Para produção, edite `src/environments/environment.ts` (o build de produção usa este arquivo por padrão em Angular 17).
