# PlaylistScript 🎵

Lenguaje de scripting especializado para generar playlists reales en Spotify.
Proyecto final de la materia **Traductores y Compiladores** — UABC FCQI, 2026.

## ¿Qué hace?

Interpreta un script declarativo con reglas (artistas, géneros, álbumes, filtros de año)
y crea automáticamente la playlist en tu cuenta de Spotify via la Web API.

## Fases del intérprete

1. Análisis Léxico (`PlaylistLexer`)
2. Análisis Sintáctico (`PlaylistParser`)
3. Análisis Semántico (`PlaylistSemantico`)
4. Autenticación OAuth 2.0 con Spotify
5. Interpretación y creación (`PlaylistInterprete`)

## Ejemplo de script

```plaintext
PLAYLIST "Mis Mejores Canciones"
DESCRIPTION "Canciones para andar de rogon"
GENRE "indie"
GENRE "pop"
ARTIST "Mon Laferte"
TRACK "Supermercado"
ALBUM "SEIS"
YEAR 2010-2024
MIN 10
LIMIT 30
NOREPEAT true
CREATE
```

## Cómo ejecutar

```bash
# Compilar
javac src/*.java -d out/

# Ejecutar
java -cp out Main
```

## Requisitos

- Java 11 o superior
- Cuenta de Spotify con app registrada en el Developer Dashboard
- Redirect URI configurada: `http://127.0.0.1:8888/callback`

## Autor

Marco Antonio Pérez Hernández — UABC, Ingeniería en Software y Tecnologías Emergentes