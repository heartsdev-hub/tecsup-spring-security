# Examen de Autenticación JWT

## Objetivo

Agregar autenticación JWT a este proyecto Spring Boot:

- crear usuarios, registrarlos y autenticarlos
- generar y validar tokens JWT
- proteger los endpoints existentes con autenticación stateless

Solo autenticación. **No** implementes roles ni permisos: una request está autenticada o no lo está.

## Contexto

El proyecto ya incluye:

- API REST en Spring Boot
- CRUD de productos
- validación y manejo de excepciones
- `SecurityConfig` placeholder (permite todo)

Debes extenderlo sin romper el módulo de productos.

## Endpoints requeridos

Respeta las URLs exactamente:

| Método | URL | Acceso | Propósito |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Público | Crear usuario |
| `POST` | `/api/v1/auth/login` | Público | Autenticar y devolver JWT |

## Reglas de seguridad

- `/api/v1/auth/**` → público
- `/api/v1/products/**` → requiere JWT válido
- Aplicación **stateless**
- JWT en header `Authorization: Bearer <token>`
- Token faltante, inválido o expirado → `401 Unauthorized`

## Entidad `User`

Campos mínimos:

- `id` (`UUID`)
- `email` (`String`, único)
- `password` (`String`, hasheado — nunca en texto plano)
- `createdAt`, `updatedAt` (pueden reutilizar el auditing existente)

## Comportamiento esperado

### Registro

- valida la entrada
- rechaza emails duplicados
- hashea la password antes de guardar
- persiste el usuario

### Login

- verifica que el usuario existe
- verifica la password contra el hash
- devuelve un JWT en caso de éxito
- credenciales inválidas → `401`

### JWT

- firma los tokens
- valida los tokens entrantes
- rechaza tokens malformados, inválidos o expirados

## Restricciones técnicas

- usa Spring Security
- usa JWT
- hashea las passwords
- mantén la app stateless
- no cambies las URLs de auth
- no agregues roles

Puedes agregar: DTOs, entidades, repositorios, servicios, controladores, filtros, utilidades, properties, migraciones y tests.

## Ejemplos de request/response

**Register / Login request**

```json
{ "email": "student@example.com", "password": "Secret123!" }
```

**Login response**

```json
{ "token": "jwt-token-value" }
```

Los nombres de campos/DTOs son libres, pero el JWT debe aparecer claramente en el body.

## Validación mínima

- email presente y con formato válido
- password presente

Reglas de password más estrictas son opcionales (documéntalas si las añades).

## Persistencia

Debe incluir mapeo de la entidad, repositorio y migración o esquema para la tabla de usuarios.

## Rúbrica (100 pts)

| Sección | Pts |
|---|---|
| A. Endpoints de auth (`register` + `login`) | 20 |
| B. Persistencia y hash de password | 20 |
| C. JWT (generación, validación, rechazo de expirados) | 25 |
| D. Spring Security (stateless + reglas públicas/protegidas) | 20 |
| E. Protección de `/api/v1/products/**` | 10 |
| F. Calidad del código | 5 |

## Reprobación automática

- URLs de auth distintas a las requeridas
- passwords en texto plano
- productos accesibles sin autenticar
- restricciones por roles en lugar de simple autenticación
- no usar JWT

## Entregables

Código fuente, migraciones y tests añadidos. El proyecto debe compilar y ejecutarse.

## Notas

- estructura de paquetes libre
- nombres de DTOs libres
- foco solo en autenticación
- prioriza la simplicidad
