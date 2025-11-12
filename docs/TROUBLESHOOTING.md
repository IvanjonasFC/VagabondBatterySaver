# Guía de Solución de Problemas: LockScreen Battery Saver

Esta guía describe los problemas más comunes que pueden surgir al desplegar **LockScreen Battery Saver** y cómo solucionarlos.  
Se basa en la experiencia real de instalación y configuración del módulo Magisk y la aplicación asociada.

---

## ⚠️ 1. Error: La app no aparece tras instalar el módulo

**Síntomas:**  
El módulo se instala correctamente en Magisk, pero la app no aparece en el lanzador o no ejecuta su función.

### Soluciones

1. Verifica que el archivo APK esté exactamente en:

magisk_module/system/priv-app/BatterySaverToggle/BatterySaverToggle.apk

text

2. Asegúrate de que el archivo XML de permisos esté presente y correctamente configurado:

magisk_module/system/etc/permissions/privapp-permissions-batterysaver.xml

text

3. Reinstala el módulo tras limpiar la caché Dalvik y reiniciar el dispositivo.  
4. Si usas una ROM personalizada, revisa que admita **aplicaciones privilegiadas (priv-app)**.

---

## ⚙️ 2. Error: Fallo al compilar el módulo por rutas o caracteres

**Síntomas:**  
Al usar 7-Zip o copiar archivos, aparecen errores por rutas inválidas o caracteres especiales en nombres de carpetas o archivos.

### Soluciones

1. Utiliza **PowerShell** en lugar del CMD tradicional.  
   PowerShell maneja mejor caracteres especiales y espacios en rutas.  

2. Evita tildes, espacios o símbolos especiales en los nombres de carpetas y archivos.  
3. Usa este comando recomendado para empaquetar:

7z a -tzip ../LockScreenBatterySaver-magisk.zip *

text

4. Si las rutas son demasiado largas, acorta los nombres de directorios antes de empaquetar.

---

## 🔋 3. Error: El módulo no activa el Battery Saver automáticamente

**Síntomas:**  
El módulo se instala correctamente y el script parece ejecutarse, pero el modo Battery Saver no se activa.

### Soluciones

1. Revisa los logs del script:

adb shell tail -f /data/adb/service.d/govbattery.log

text

2. Comprueba que el archivo `govbattery.sh` tiene permisos ejecutables y su contenido sea correcto.  
3. Verifica que tu ROM no restrinja el acceso a las APIs de ahorro de batería.  
4. Tras reiniciar, confirma que el servicio sigue ejecutándose en segundo plano.

---

## 🔐 4. Error: Permisos insuficientes para cambiar governors o modos de batería

**Síntomas:**  
El log muestra el mensaje `Permission denied` al intentar cambiar el modo Battery Saver o governor de CPU.

### Soluciones

1. Asegúrate de que tu dispositivo esté **rooteado** y ejecutes la última versión de **Magisk**.  
2. Verifica que el módulo tenga permisos de sistema y la app esté instalada como **priv-app**.  
3. Si el archivo XML de permisos está incompleto, revisa su sintaxis y agrega permisos como:

<permission name="android.permission.DEVICE_POWER" /> <permission name="android.permission.CHANGE_CONFIGURATION" /> ```
Si el script accede a archivos protegidos, asegúrate de que Magisk haya parcheado correctamente el boot image.

🧩 5. Error: El módulo no aparece en la lista de Magisk
Soluciones
Verifica que el archivo ZIP se haya empaquetado correctamente.
Debe contener, al menos, la siguiente estructura:

text
META-INF/
system/
service.d/
module.prop
No agregues subcarpetas adicionales dentro del ZIP.

Asegúrate de instalarlo mediante la opción “Instalar desde almacenamiento” en Magisk Manager
(no uses “Descargar” si no estás publicando el módulo).

💡 Consejos Generales
Reinicia siempre el dispositivo tras instalar o actualizar el módulo.

Comprueba el log generado en:

text
/data/adb/service.d/govbattery.log
para obtener detalles de su ejecución.

Si actualizas la app o el script, recompila y empaqueta de nuevo el módulo antes de reinstalar.

📄 Ejemplo rápido de empaquetado
Para referencia, este es el comando completo en PowerShell para generar el ZIP del módulo:

text
cd "C:\Users\TU_USUARIO\Desktop\LockScreenBatterySaver\magisk_module"
7z a -tzip ../LockScreenBatterySaver-magisk.zip *
Esto creará el archivo LockScreenBatterySaver-magisk.zip listo para su instalación desde Magisk Manager.
