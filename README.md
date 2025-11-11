LockScreen Battery Saver
Sistema avanzado para dispositivos Android rooteados que activa automáticamente el modo Battery Saver al bloquear la pantalla y gestiona los governors de la CPU, optimizando al máximo el consumo en standby.

Tabla de Contenidos
Descripción General

Características Principales

Arquitectura del Sistema

Estructura del Proyecto

Requisitos

Instalación

Uso

Solución de Problemas

Contribuciones

Licencia

Contacto

Descripción General
LockScreen Battery Saver supera las limitaciones de Android 14+ permitiendo activar el modo Battery Saver y cambiar el perfil del CPU governor desde la pantalla de bloqueo, todo de forma automática y sin intervención manual. Está diseñado para ROMs personalizadas y sistemas con root y Magisk.

Características Principales
Activación automática de Battery Saver al bloquear la pantalla.

Cambio dinámico de CPU governor (powersave/interactive).

Integración mediante módulo Magisk.

App instalada como privilegio del sistema para control y logs.

Compatible con Android 14 y superior.

Instalación segura mediante 7-Zip y PowerShell.

Sistema de logs consultables desde la app.

Arquitectura del Sistema
El proyecto consta de tres capas integradas:

Módulo Magisk: Instala la app como priv-app, aplica permisos privilegiados y ejecuta scripts automáticos.

App Privilegiada: Permite revisar logs, elegir parámetros y controla directamente el modo Battery Saver mediante llamadas a PowerManager.

Scripts de Automatización: Detectan el estado de la pantalla y cambian el governor y el modo Battery Saver según corresponda, generando logs para supervisión.

text
LockScreenBatterySaver/
 ├── magisk_module/
 │    ├── system/priv-app/BatterySaverToggle/BatterySaverToggle.apk
 │    ├── system/etc/permissions/privapp-permissions-batterysaver.xml
 │    └── service.d/govbattery.sh
 ├── android_app/
 │    └── (tu proyecto Android Studio completo)
 └── docs/
      ├── INSTALLATION.md
      ├── ARCHITECTURE.md
      └── TROUBLESHOOTING.md
Estructura del Proyecto
Consulta la sección anterior para ver la organización de carpetas y archivos. Así podrás ubicar rápidamente scripts, documentación y archivos clave.

Requisitos
Android 14 o superior

Dispositivo con root y Magisk v28+

Android Studio para compilar la app

7-Zip instalado

PowerShell y drivers ADB

Instalación (Resumen)
Compila tu app con Android Studio y copia el APK a:
magisk_module/system/priv-app/BatterySaverToggle/BatterySaverToggle.apk

Verifica los archivos de permisos y scripts están en el módulo.

Empaqueta el módulo utilizando PowerShell y 7-Zip:

powershell
7z a -tzip ../LockScreenBatterySaver-magisk.zip *
Transfiere el ZIP al móvil con ADB:

text
adb push ../LockScreenBatterySaver-magisk.zip /sdcard/
Instala desde Magisk Manager y reinicia.

La app aparecerá como privilegiada y el sistema se activará automáticamente.

Para instalación detallada, consulta docs/INSTALLATION.md.

Uso
Tras instalar y reiniciar, el sistema activará el Battery Saver y el governor powersave automáticamente al bloquear la pantalla, y revertirá al interactive al desbloquear.

Accede a la app instalada para revisar logs y configurar parámetros.

Monitorea logs con:

text
adb shell tail -f /data/adb/service.d/govbattery.log
Solución de Problemas
Revisa docs/TROUBLESHOOTING.md para problemas como:

La app no aparece

Permisos insuficientes para governor/battery saver

Error al empaquetar o instalar el módulo

Problemas con ROMs personalizadas o rutas

Contribuciones
Aporta mejoras siguiendo la guía:

Haz un fork del repositorio

Crea tu rama de trabajo

Documenta tus cambios y pruebas

Haz PRs claros y concisos

Guía completa de contribución

Portfolio: portfolio.pesoz.i234.me
LinkedIn: Ivan Jonas

Licencia
MIT License. Consúltala en el archivo LICENSE.

Contacto
¿Preguntas, dudas o feedback?

Crea un issue en GitHub.

Contacta por LinkedIn: Ivan Jonas

Portafolio: portfolio.pesoz.i234.me