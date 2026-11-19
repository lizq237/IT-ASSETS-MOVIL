# IT Assets Management — Android

**Aplicación móvil para consultar activos tecnológicos asignados y registrar incidencias de soporte técnico.**

> **Estado:** proyecto académico en desarrollo · **Facultad de Informática, UAS** · **Líder de proyecto:** Lizeth Quiroz Urias

La app forma parte de IT Assets Management, una plataforma multi-tenant con panel administrativo web y API REST. Su enfoque es que cada usuario autenticado pueda consultar sus propios equipos y reportar problemas desde Android.

**Repositorios relacionados:** [Web y guía general](https://github.com/lizq237/IT-ASSETS-WEB) · [API Laravel](https://github.com/lizq237/IT-ASSETS-API) · **Android (este repositorio)**

## Funcionalidades presentes en el código

- **Inicio de sesión** con correo y contraseña mediante la API Laravel y token Bearer.
- **Consulta de activos asignados** al usuario autenticado, con estados de carga, error y lista vacía.
- **Creación de tickets** seleccionando un equipo, descripción y prioridad (`baja`, `media`, `alta`).
- **Evidencia opcional:** selección de imagen desde el teléfono y envío multipart a la API (límite de 5 MiB).
- **Perfil básico y cierre de sesión** con datos de sesión almacenados en `SharedPreferences`.

**En desarrollo:** las vistas de historial/listado de tickets (`MyTicketsFragment` y `AllTicketsFragment`) aún son estructuras visuales básicas y **no realizan consultas de historial a la API**. No se anuncian como funcionalidades terminadas.

## Stack

| Capa | Tecnologías |
| --- | --- |
| Aplicación nativa | Java, XML, Android SDK |
| Entorno de desarrollo | Android Studio y Gradle Wrapper |
| Consumo de API | Retrofit 2, Gson |
| UI | AndroidX, Material Components, RecyclerView |
| Backend | Laravel 12 + Sanctum (repositorio independiente) |
| Persistencia remota | PostgreSQL para activos/usuarios y MongoDB para tickets |

El proyecto declara **`minSdk 24`**, compilación con **SDK 37**, **Java 11** y usa el Gradle Wrapper incluido en el repositorio. Android Studio necesitará tener instaladas las herramientas compatibles con la configuración actual.

## Antes de abrir la app

La aplicación **no es independiente**: necesita que la API Laravel esté ejecutándose y que existan datos de prueba. La infraestructura local se inicia desde el repositorio Web.

### 1. Preparar el backend

Sigue la [guía de instalación de IT-ASSETS-WEB](https://github.com/lizq237/IT-ASSETS-WEB) o la [guía técnica de la API](https://github.com/lizq237/IT-ASSETS-API).

En resumen:

```bash
git clone -b developer https://github.com/lizq237/IT-ASSETS-WEB.git
git clone -b developer https://github.com/lizq237/IT-ASSETS-API.git

cd IT-ASSETS-WEB
docker compose up -d
```

En otra terminal, dentro de `IT-ASSETS-API`, configura `.env` para PostgreSQL y MongoDB y ejecuta:

```bash
composer install
php artisan key:generate
php artisan migrate --seed
php artisan serve --host=127.0.0.1 --port=8000
```

**Importante:** los seeders iniciales crean organizaciones y administradores, pero **no crean una cuenta móvil con equipos asignados**. Para demostrar la app:

1. Entra al panel web como un administrador TI de una organización local.
2. Crea un usuario de prueba con rol `responsable` o `tecnico`.
3. Registra un equipo con estado `activo`, crea los catálogos requeridos y asígnale ese equipo.
4. Utiliza el correo y la contraseña de **ese usuario** para iniciar sesión en Android.

No intentes levantar tickets con el usuario `super_admin`: carece de organización asignada.

### 2. Clonar y ejecutar el proyecto Android

```bash
git clone -b main https://github.com/lizq237/IT-ASSETS-MOVIL.git
```

1. Abre la carpeta `IT-ASSETS-MOVIL` desde **Android Studio → Open**.
2. Permite la sincronización de Gradle y descarga las dependencias solicitadas.
3. Inicia un emulador Android compatible (API 24 o superior).
4. Confirma que Laravel sigue ejecutándose en el puerto `8000`.
5. Pulsa **Run** y abre la aplicación.

No se necesita configurar Firebase: el cliente consume la API REST de Laravel.

## Configurar la conexión API

En [`RetrofitClient.java`](https://github.com/lizq237/IT-ASSETS-MOVIL/blob/main/app/src/main/java/com/example/sysbase/RetrofitClient.java), el código actual contiene:

```java
private static final String BASE_URL = "http://10.0.2.2:8000/api/";
```

- **Emulador Android:** deja `10.0.2.2`, que apunta al equipo anfitrión. `localhost` dentro del emulador apunta al propio emulador, no a tu PC.
- **Teléfono físico:** sustituye esa dirección por la IP privada de tu computadora, por ejemplo `http://192.168.1.50:8000/api/`, y levanta Laravel con `php artisan serve --host=0.0.0.0 --port=8000`. El móvil y la PC deben estar en la misma red y el firewall debe permitir el acceso.
- **Producción:** se necesita un backend publicado con **HTTPS** y una configuración más segura antes de distribuir la aplicación.

El `AndroidManifest.xml` actual permite tráfico HTTP sin cifrar (`usesCleartextTraffic="true"`) para **pruebas locales**. No se debe interpretar como configuración segura de producción.

## Recorrido de prueba para reclutadores

1. **Login:** entra con el usuario de prueba que creaste en la API (no con el superadministrador).
2. **Mis equipos:** revisa la lista de activos asignados. Si no existen, verás el mensaje «No tienes equipos asignados».
3. **Reportar incidencia:** elige un equipo, escribe título y descripción y selecciona una prioridad.
4. **Adjuntar imagen (opcional):** selecciona una fotografía desde el dispositivo, revisa la vista previa y envía.
5. **Confirmar creación:** al recibir la respuesta `201` de Laravel, la aplicación muestra un folio de ticket. El registro queda guardado en MongoDB.
6. **Cerrar sesión:** utiliza el apartado de perfil.

**No incluyas datos reales ni imágenes sensibles durante la evaluación.** Se recomienda usar únicamente inventario y cuentas ficticias.

## Endpoints consumidos

La interfaz `ApiService.java` actualmente define estas peticiones:

| Método | Endpoint | Uso |
| --- | --- | --- |
| POST | `/api/login` | Autenticación y token |
| GET | `/api/employee/assets` | Equipos asignados al usuario |
| POST | `/api/employee/tickets` | Crear ticket, con imagen opcional |

El envío de tickets es `multipart/form-data`. La API valida que el equipo esté asignado al usuario y pertenezca a su organización. Si los datos son inválidos o el equipo no corresponde, puede devolver `422`.

## Problemas frecuentes

- **`Connection refused` en el emulador:** comprueba que Laravel esté encendido, usa `10.0.2.2:8000` y verifica la ruta `/api/login`.
- **`Connection timed out` en un teléfono físico:** revisa IP local, Wi-Fi, firewall y que Laravel use `--host=0.0.0.0` en una red de confianza.
- **Credenciales incorrectas:** comprueba que el usuario exista en PostgreSQL y utiliza las credenciales creadas para la demo.
- **No aparecen equipos:** el empleado no tiene activos asignados; regístralos y asígnalos desde la web.
- **Error `401`:** el token no es válido o la sesión expiró. Inicia sesión de nuevo.
- **Error `422` al crear ticket:** revisa los campos y que el equipo siga asignado a la cuenta.
- **Error de compilación o SDK:** sincroniza Gradle, revisa las versiones de SDK/AGP definidas en el repositorio y consulta la ventana Build de Android Studio.

## Organización del código

- `RetrofitClient.java`: URL base y creación del cliente HTTP.
- `ApiService.java`: contrato de endpoints consumidos.
- `LoginActivity.java`: autenticación y sesión local.
- `EquipmentFragment.java`: consulta y listado de activos.
- `AddTicketActivity.java`: formulario de ticket con evidencia.
- `ProfileFragment.java`: datos básicos de perfil y cierre de sesión.

## Estado y próximos pasos

La app continúa en desarrollo. Entre los trabajos pendientes están el historial real de tickets, una configuración de red para producción con HTTPS, pruebas automatizadas y capturas documentadas de las pantallas.

---

**Equipo:** proyecto académico realizado por seis integrantes, coordinado por Lizeth Quiroz Urias.
