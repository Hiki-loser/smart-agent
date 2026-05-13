# SmartAgent Frontend

A Vue 3-based anime-style AI chat and blog frontend for the SmartAgent platform. Features real-time SSE streaming, JWT authentication, markdown rendering, and a fully configurable internationalization system.

## Features

- **Real-time AI Chat** with SSE (Server-Sent Events) streaming
- **JWT Authentication** with automatic token refresh
- **Markdown Rendering** with syntax highlighting for code blocks
- **Anime/2D Design** with soft pastel colors, smooth animations, and light/dark themes
- **Full i18n Support** — Chinese (zh-CN) and English (en), all text configurable via JSON locale files
- **API Key Management** — create, view, and revoke API keys for programmatic access
- **Responsive** — desktop sidebar layout, tablet collapsed sidebar, mobile drawer overlay

## Tech Stack

| Category | Library | Version |
|----------|---------|---------|
| Framework | Vue 3 (Composition API) | 3.5+ |
| Language | TypeScript | 5.x |
| Build | Vite | 6.x |
| Router | Vue Router | 4.x |
| State | Pinia | 2.x |
| HTTP | Axios | 1.x |
| UI Library | Element Plus | 2.x |
| i18n | vue-i18n | 10.x |
| Markdown | markdown-it + highlight.js | latest |
| SSE | @microsoft/fetch-event-source | latest |
| Utils | dayjs, @vueuse/core, uuid | latest |

## Prerequisites

- **Node.js** >= 18
- **npm** >= 9
- Backend services running (see [Backend Integration](#backend-integration))

## Quick Start

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Copy environment config
cp .env.example .env.development

# Start development server
npm run dev
```

The dev server starts at `http://localhost:5173` with hot module replacement.

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server with HMR |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint |
| `npm run format` | Run Prettier |

## Project Structure

```
frontend/
├── public/                    # Static assets
├── src/
│   ├── api/                   # API layer
│   │   ├── index.ts           # Axios instance + request/response interceptors
│   │   ├── auth.ts            # Auth endpoints (login, register, refresh)
│   │   ├── user.ts            # User endpoints (profile, api keys)
│   │   └── chat.ts            # Chat endpoints (sessions, messages)
│   ├── assets/styles/         # Global styles
│   │   ├── variables.scss     # CSS custom properties (design tokens)
│   │   ├── reset.scss         # CSS reset
│   │   ├── global.scss        # Global styles & utilities
│   │   ├── animations.scss    # Keyframe animations
│   │   ├── element-plus-overrides.scss  # Element Plus theming
│   │   └── markdown.scss      # Markdown content styles
│   ├── components/            # Reusable components
│   │   ├── common/            # Shared: AppLogo, UserAvatar, MarkdownRenderer, etc.
│   │   ├── chat/              # Chat: ChatBubble, ChatInput, StreamingBubble, etc.
│   │   └── layout/            # Layout: AppLayout, AppSidebar, AppNavbar, etc.
│   ├── composables/           # Vue composables (useAuth, useChat, useSSE, etc.)
│   ├── config/                # Configuration files
│   │   ├── api.config.ts      # API endpoints & settings
│   │   ├── theme.config.ts    # Design tokens & theme application
│   │   └── app.config.ts      # App constants
│   ├── locales/               # i18n translation files
│   │   ├── index.ts           # vue-i18n setup & locale detection
│   │   ├── zh-CN.json         # Chinese translations
│   │   └── en.json            # English translations
│   ├── router/                # Vue Router
│   │   ├── index.ts           # Router instance
│   │   ├── routes.ts          # Route definitions (lazy-loaded)
│   │   └── guards.ts          # Auth navigation guards
│   ├── stores/                # Pinia stores
│   │   ├── auth.store.ts      # Authentication state
│   │   ├── chat.store.ts      # Chat state & SSE streaming
│   │   └── app.store.ts       # App-wide state (theme, locale, sidebar)
│   ├── types/                 # TypeScript type definitions
│   ├── utils/                 # Utilities (token, format, validators, markdown, dom)
│   ├── views/                 # Page components
│   │   ├── auth/              # Login, Register
│   │   ├── chat/              # ChatView
│   │   ├── user/              # Profile, API Keys
│   │   └── error/             # 404, 403
│   ├── App.vue                # Root component
│   └── main.ts                # Entry point
├── .env / .env.development / .env.production  # Environment variables
├── vite.config.ts             # Vite configuration
├── tsconfig.json              # TypeScript configuration
└── package.json               # Dependencies & scripts
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080` |
| `VITE_API_TIMEOUT` | API request timeout (ms) | `30000` |
| `VITE_SSE_TIMEOUT` | SSE stream timeout (ms) | `1800000` (30 min) |
| `VITE_APP_NAME` | Application display name | `SmartAgent` |
| `VITE_APP_VERSION` | Application version | `1.0.0` |
| `VITE_ENABLE_REGISTER` | Enable user registration | `true` |

### Internationalization (i18n)

All user-facing text is stored in JSON locale files under `src/locales/`. To add a new language:

1. Create a new file `src/locales/<locale>.json` with the same structure as `zh-CN.json`
2. Add the locale to `SUPPORTED_LOCALES` in `src/config/app.config.ts`
3. Import and register the locale in `src/locales/index.ts`

### Theme Customization

Design tokens (colors, radii, shadows) are defined in `src/config/theme.config.ts`. The theme supports light and dark modes, applied as CSS custom properties at runtime. To customize:

1. Edit color values in `COLORS.light` and `COLORS.dark` in `src/config/theme.config.ts`
2. Customize radii and shadows in the `RADII` and `SHADOWS` objects
3. Element Plus components are themed via CSS custom properties in `src/assets/styles/element-plus-overrides.scss`

### API Configuration

All API endpoint paths are centralized in `src/config/api.config.ts`. The Axios instance in `src/api/index.ts` automatically:
- Injects the `Authorization: Bearer <token>` header
- Adds device identification headers
- Unwraps the backend's `ApiResponse<T>` envelope
- Handles 401 responses with automatic token refresh and request queuing

## Architecture

### Data Flow

```
User Action → Vue Component → Pinia Store → API Layer (Axios) → Backend API
                    ↑                                              |
                    └──── Response (unwrapped) ←── Interceptor ←───┘
```

### Auth Flow

1. User logs in → receives JWT access token (1h) + refresh token (7d)
2. Tokens stored in localStorage, attached to all requests via Axios interceptor
3. Access token auto-refreshed 5 minutes before expiry
4. On 401 response: queued request refresh with retry
5. Refresh failure → redirect to login

### SSE Streaming

The chat message endpoint uses POST-based SSE. The `@microsoft/fetch-event-source` library is used (instead of native `EventSource`) because:
- The backend endpoint is POST (native `EventSource` only supports GET)
- Custom headers (Authorization) are required
- Configurable retry with exponential backoff is needed

## Routes

| Path | View | Auth Required |
|------|------|---------------|
| `/login` | LoginView | No |
| `/register` | RegisterView | No |
| `/chat` | ChatView | Yes |
| `/chat/:sessionId` | ChatView (specific session) | Yes |
| `/profile` | ProfileView | Yes |
| `/api-keys` | ApiKeysView | Yes |
| `/403` | ForbiddenView | No |
| `/*` | NotFoundView (404) | No |

## Backend Integration

This frontend connects to the SmartAgent backend microservices:

- **Gateway** (port 8080): Routes requests, validates JWT, rate limiting
- **User Service** (port 8081): Authentication, user management
- **Chat Service** (port 8082): AI chat sessions, SSE streaming

The Vite dev server proxies `/api/*` to `http://localhost:8080` for local development.

## Browser Support

Modern browsers with ES module support:
- Chrome >= 90
- Firefox >= 90
- Safari >= 15
- Edge >= 90

## License

MIT
