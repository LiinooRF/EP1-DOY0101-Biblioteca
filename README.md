# Biblioteca Springboot - EP1 Ingeniería DevOps

Repositorio del encargo de la Evaluación Parcial 1 de Ingeniería DevOps (DOY0101).

**Integrantes:**
- Lino (LiinooRF)
- Arion (Kawyyz)

## De qué se trata

El microservicio que usamos de base es el sistema Biblioteca, hecho con Spring Boot.
Son varios servicios que trabajan juntos:

| Módulo | Qué hace | Puerto |
|---|---|---|
| `eureka` | Registro de servicios, ahí se anotan los demás | 8761 |
| `api-gateway` | Punto de entrada único | 9000 |
| `ms-usuarios` | Usuarios, login y token JWT | 9001 |
| `ms-catalogo` | Libros, autores y categorías | 9002 |
| `ms-recursos` | Recursos físicos y digitales | 9003 |
| `common` | Librería compartida | - |

Para levantarlo en el computador están las
[instrucciones de ejecución](INSTRUCCIONES%20EJECUCION.md).

## Modelo de ramificación

Antes de crear las ramas comparamos los tres modelos que vimos en clases:

| Modelo | Cómo funciona | Cuándo conviene | Contra |
|---|---|---|---|
| **GitFlow** | Ramas fijas `main` y `develop`, más ramas temporales de `feature`, `release` y `hotfix` | Proyectos que se entregan por versiones y equipos que necesitan orden | Es el más pesado, muchas ramas para un cambio chico |
| **GitHub Flow** | Solo `main` y ramas cortas, cada merge se despliega altiro | Aplicaciones web que despliegan varias veces al día | No tiene rama de integración, si algo falla llega directo a producción |
| **Trunk-based** | Casi todos trabajan sobre la misma rama, con ramas de horas | Equipos grandes con muchas pruebas automáticas | Sin buena cobertura de pruebas se rompe seguido |

**Elegimos GitFlow.** Las razones:

1. Somos dos trabajando en paralelo. Tener `develop` como rama de integración nos deja
   juntar el trabajo de los dos y probarlo antes de que llegue a `main`.
2. El proyecto se entrega por versiones, no se despliega 10 veces al día, así que no
   necesitamos la rapidez de GitHub Flow.
3. `main` queda siempre con lo que está funcionando. Si hay que arreglar algo urgente
   sale un `hotfix` desde `main`, sin arrastrar cosas a medio hacer que estén en
   `develop`. De hecho eso fue justo lo que nos pasó con la conexión a la base.
4. Trunk-based lo descartamos porque necesita muchas más pruebas automáticas de las que
   tiene hoy el proyecto.

### Ramas

| Rama | Sale de | Se mergea a | Para qué |
|---|---|---|---|
| `main` | - | - | Lo que está estable y funcionando |
| `develop` | `main` | `main` | Donde juntamos el trabajo de los dos |
| `feature/<nombre>` | `develop` | `develop` | Una funcionalidad nueva |
| `hotfix/<nombre>` | `main` | `main` y `develop` | Arreglo urgente de algo que ya estaba entregado |

## Convenciones que acordamos

### Nombres de ramas

Formato `tipo/descripcion-corta`, en minúsculas, sin tildes ni ñ, palabras separadas
con guion.

```
feature/contar-libros
feature/buscar-por-titulo
hotfix/conexion-bd
```

Lo que no hacemos: `feature/Cambios` (no se entiende qué hace),
`feature/cosas_de_lino` (nombre de la persona en vez del cambio),
`Feature/Contar-Libros` (mayúsculas, después se enreda en Linux).

### Mensajes de commit

Usamos Conventional Commits, o sea `tipo: qué hiciste`, en minúscula y sin punto final.

| Tipo | Cuándo |
|---|---|
| `feat` | Funcionalidad nueva |
| `fix` | Corrección de un error |
| `ci` | Cambios en el pipeline |
| `docs` | Documentación |
| `chore` | Configuración, `.gitignore`, cosas que no tocan el código |
| `test` | Pruebas |

Ejemplos de commits de este repo:

```
ci: agrego el workflow de actions para que compile y corra los tests
fix: comento el modulo ms-vehiculos que rompia la compilacion
feat: agrego el endpoint para contar los libros del catalogo
```

Reglas: un commit por cambio, nada de "varios arreglos" con 20 archivos adentro, y
nada de mensajes tipo `cambios`, `arreglos` o `asdasd`.

### Flujo de merge

Nadie hace push directo a `main` ni a `develop`, todo entra por Pull Request. El ciclo
que repetimos para cada cambio fue este:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nombre-del-cambio
# ... trabajar ...
git add .
git commit -m "feat: descripcion del cambio"
git push -u origin feature/nombre-del-cambio
# abrir el PR en GitHub hacia develop y esperar que el pipeline quede verde
```

Usamos merge commit y no squash, porque así en el historial queda cada commit y además
el merge que dice de qué rama vino. Para revisar el árbol:

```bash
git log --oneline --graph --all
```

### Revisión de código

Antes de aprobar un PR revisamos:

1. Que el pipeline esté verde. Si está rojo no se revisa nada más hasta que se arregle.
2. Que la rama tenga el nombre correcto.
3. Que no se hayan subido archivos que no van (`target/`, claves, configuración del IDE).
4. Que el PR explique qué hace.

Si algo está malo se comenta en el PR en vez de arreglarlo por fuera, así queda
registrado por qué se cambió.

### Estructura de carpetas

La estructura la manda Maven, no la cambiamos por gusto:

```
EP1-DOY0101-Biblioteca/
├── .github/workflows/ci.yml    <- el pipeline
├── common/                     <- librería compartida
├── eureka/                     <- registro de servicios
├── api-gateway/                <- puerta de entrada
├── ms-usuarios/                <- microservicio
├── ms-catalogo/                <- microservicio
├── ms-recursos/                <- microservicio
├── init-multi-db/              <- scripts SQL de las bases
├── postman/                    <- colecciones para probar la API
├── pom.xml                     <- pom padre
└── README.md
```

Y dentro de cada microservicio se respeta la estructura de Spring Boot:
`controller/`, `service/`, `repository/`, `model/`, `dto/`, `mapper/`.

Lo que no se sube al repositorio (está en el `.gitignore`): las carpetas `target/` con
los `.jar` compilados, los archivos del IDE (`.idea/`, `.vscode/`) y cualquier archivo
con claves.

### Control de versiones

Si tuviéramos que numerar las versiones usaríamos versionado semántico
(`MAYOR.MENOR.PARCHE`): el último número sube cuando es solo un arreglo, el del medio
cuando se agrega algo nuevo y el primero cuando se rompe la compatibilidad.

## Pipeline de GitHub Actions

Está en [`.github/workflows/ci.yml`](.github/workflows/ci.yml) y corre solo:

- con cada **push a `develop`**
- con cada **Pull Request hacia `main` o `develop`**

Lo que hace: baja el código a una máquina de GitHub, instala Java 21 (la versión que
pide el pom padre), compila todos los módulos y corre las pruebas unitarias.

**Para qué nos sirvió:** el pipeline compila en un servidor limpio, que no es el
computador de ninguno de los dos. Por eso se cae si a alguien se le olvidó subir un
archivo o si algo funcionaba solo en su máquina. Nos pasó de verdad la primera vez que
lo corrimos: el proyecto compilaba en el IDE, pero el pipeline se cayó altiro porque el
`pom.xml` padre declaraba el módulo `ms-vehiculos` y esa carpeta no venía en el
proyecto.

```
[ERROR] Child module .../ms-vehiculos of .../pom.xml does not exist
[ERROR] The build could not read 1 project
```

Sin el pipeline eso lo habríamos descubierto cuando el profe intentara compilarlo.

Las pruebas `*ApplicationTests` quedan fuera del pipeline a propósito, porque levantan
todo el contexto de Spring y necesitan MySQL y Eureka corriendo, cosa que no hay en la
máquina de GitHub. Quedan corriendo las pruebas unitarias con Mockito de `ms-catalogo`.

## Trabajo colaborativo

Simulamos el flujo con dos funcionalidades y un arreglo urgente, cada uno en su rama y
entrando por Pull Request:

| Rama | Tipo | Qué hicimos |
|---|---|---|
| `feature/contar-libros` | feature | Endpoint `GET /api/v1/libros/total` que devuelve cuántos libros hay, con su prueba unitaria |
| `feature/buscar-por-titulo` | feature | Endpoint `GET /api/v1/libros/buscar?titulo=` que busca libros por título, con su prueba unitaria |
| `hotfix/conexion-bd` | hotfix | Los tres `application.yml` tenían datos de conexión de ejemplo (`ip-host`, `user`, `pw`), así que ningún microservicio podía conectarse a MySQL |

El hotfix salió de `main` y no de `develop` porque el error estaba en lo que ya estaba
entregado. Después de mergearlo a `main` hay que bajarlo también a `develop`, si no el
arreglo se pierde la próxima vez que subamos cambios.

## Uso de Inteligencia Artificial

Como pide la pauta, dejamos declarado el uso de IA:

| Herramienta | En qué la usamos |
|---|---|
| ChatGPT | Entender el error que tiró el pipeline la primera vez, el del módulo `ms-vehiculos` que estaba declarado en el pom padre pero no existía la carpeta, y confirmar que comentarlo era la forma correcta de arreglarlo |
| ChatGPT | Revisar la redacción de este README y resolver dudas de sintaxis del archivo de GitHub Actions |

Lo que decidimos nosotros: qué modelo de ramificación usar y por qué, cómo nombrar las
ramas y los commits, qué revisamos en cada Pull Request y qué endpoints agregar al
catálogo. Las conclusiones y las reflexiones del final están escritas sin IA.

Todo lo que salió con ayuda de IA lo probamos antes de subirlo.

Referencia: https://bibliotecas.duoc.cl/ia

## Conclusiones

**Reflexión de _(integrante 1)_:**

_(escribir a mano, sin IA: qué aprendiste, qué te costó, en qué aportaste)_

**Reflexión de _(integrante 2)_:**

_(escribir a mano, sin IA: qué aprendiste, qué te costó, en qué aportaste)_
