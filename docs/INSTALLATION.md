Bienvenido a la guía profesional de instalación de LockScreen Battery Saver para Android. Sigue estos pasos para compilar, empacar e instalar correctamente el módulo y la app en tu dispositivo.

Requisitos Previos
Dispositivo Android con root y Magisk instalado (v28 o superior)

Android 14 o superior

7-Zip instalado y agregado a la variable de entorno (para compilar el módulo)

PowerShell (Windows 10/11, o usa Terminal integrada en VS Code)

Cable USB y drivers ADB instalados

1. Compilar la Aplicación Android
Abre tu proyecto Android en Android Studio.

Compila el APK en modo release:

Ve a Build > Build Bundle(s) / APK(s) > Build APK(s)

El APK generado estará en:

text
android_app/app/build/outputs/apk/release/app-release.apk
2. Insertar APK dentro del Módulo Magisk
Copia el app-release.apk compilado dentro de la ruta:

text
magisk_module/system/priv-app/BatterySaverToggle/BatterySaverToggle.apk
Verifica que el archivo XML de permisos esté presente:

text
magisk_module/system/etc/permissions/privapp-permissions-batterysaver.xml
Comprueba que los scripts y archivos de configuración están correctamente ubicados:

Script principal: magisk_module/service.d/govbattery.sh

Archivo module.prop configurado

3. Empaquetar el Módulo Magisk (de forma segura)
Desde PowerShell (recomendado para evitar errores de rutas/caracteres):

Navega a la carpeta raíz del módulo:

powershell
cd "C:\Users\TU_USUARIO\Desktop\LockScreenBatterySaver\magisk_module"
Empaqueta todo el módulo en un solo archivo .zip usando 7-Zip:

powershell
7z a -tzip ../LockScreenBatterySaver-magisk.zip *
Esto crea el archivo seguro LockScreenBatterySaver-magisk.zip en la carpeta superior.

4. Transferir el Módulo a tu Dispositivo
Conecta tu dispositivo Android por USB (con depuración activada).

Usa ADB para transferir el archivo ZIP al almacenamiento del dispositivo:

text
adb push ../LockScreenBatterySaver-magisk.zip /sdcard/
5. Instalar Módulo vía Magisk Manager
Abre Magisk Manager en tu dispositivo Android.

Pulsa en “Instalar desde almacenamiento” o “Install from storage”.

Selecciona el archivo LockScreenBatterySaver-magisk.zip que transferiste.

Espera a que finalice la instalación y reinicia el dispositivo cuando lo solicite.

6. Verifica el Funcionamiento
El módulo se aplicará automáticamente al arrancar.

Los logs y configuraciones se pueden visualizar y modificar desde la aplicación Android instalada como app de sistema.

Para ver logs directamente:

text
adb shell tail -f /data/adb/service.d/govbattery.log

Notas y Sugerencias

No instales la APK por separado: debe instalarse automáticamente como app de sistema por el módulo.
Para desinstalar, elimina el módulo desde Magisk y reinicia.


¿Tienes problemas? Consulta docs/TROUBLESHOOTING.md para soluciones y soporte adicional.