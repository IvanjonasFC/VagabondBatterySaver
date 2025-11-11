Descripción General
LockScreen Battery Saver utiliza una arquitectura híbrida para sortear las nuevas limitaciones de Android 14 y superior, automatizando el modo Battery Saver y la gestión de governors únicamente con permisos de root y sin desbloqueo activo. El sistema se divide en tres componentes principales:

1. Módulo Magisk
Ubicación: /magisk_module/

Responsabilidad:

Posiciona la app como aplicación del sistema (priv-app).

Añade el archivo de permisos privilegiados (privapp-permissions-batterysaver.xml).

Instala scripts automáticos en /data/adb/service.d/ para ejecutarse al arrancar.

Rol: Es la "capa de integración" que permite que app y scripts tengan acceso de sistema sin limitaciones de usuario.

2. App Privilegiada
Ubicación: /magisk_module/system/priv-app/BatterySaverToggle/BatterySaverToggle.apk

Responsabilidad:

Interfaz básica para monitorizar el estado, logs y personalizar parámetros del ahorro de batería y governor.

Usa PowerManager (acceso privilegiado) para activar/desactivar el modo Battery Saver desde lockscreen.

Expone logs y configuración accesibles para el usuario root/intermedio.

3. Scripts de Automatización
Ubicación: /magisk_module/service.d/govbattery.sh

Responsabilidad:

Supervisa el estado de la pantalla (on/off).

Cambia governors a powersave/interactive y activa/desactiva Battery Saver según el bloqueo/desbloqueo de pantalla.

Genera logs para depuración y monitoreo.

Esquema del Flujo de Trabajo
text
flowchart TD
    A[Boot/Inicio sistema]
    A --> B[Service.d se ejecuta]
    B --> C{¿Pantalla bloqueada?}
    C -- Sí --> D[Script: activa Battery Saver y powersave governor]
    C -- No --> E[Script: desactiva Battery Saver y governor normal]
    B --> F[App privileged inicia]
    F --> G[Usuario revisa logs/cambia parámetros]
Ventajas:

Altísima integración, funcionamiento 100% automático y sin intervención manual tras la instalación.

Supera restricciones estándar de Battery Saver gracias a privilegios de sistema.

Modular: puedes adaptar scripts o lógica de la app según necesidades futuras.

Nota:
El uso privilegiado solo es posible gracias a la combinación de Magisk y los permisos correctamente aplicados en la estructura del módulo.