***

# Guía de Solución de Problemas: LockScreen Battery Saver

Esta guía describe los problemas más comunes que pueden surgir al desplegar **LockScreen Battery Saver** y cómo solucionarlos.  
Se basa en la experiencia real de instalación y configuración del módulo Magisk y la app asociada.

***

## ⚠️ Error 1: La app no aparece tras instalar el módulo

**Síntomas:**  
El módulo se instala correctamente en Magisk, pero la app no aparece en el lanzador o no ejecuta su función.

### Soluciones

- Verifica que el APK esté exactamente en:

```text
magisk_module/system/priv-app/BatterySaverToggle/BatterySaverToggle.apk
```

- Asegúrate de que el archivo XML de permisos esté presente y correctamente configurado:

```text
magisk_module/system/etc/permissions/privapp-permissions-batterysaver.xml
```

- Reinstala el módulo tras limpiar caché Dalvik y reiniciar el dispositivo.
- Si usas una ROM personalizada, revisa que admite apps privilegiadas (**priv-app**).

***

## ⚙️ Error 2: Fallo al compilar el módulo por rutas/caracteres

**Síntomas:**  
Al usar comandos con 7-Zip o al copiar archivos, aparecen errores por rutas inválidas o caracteres especiales.

### Soluciones

- Utiliza PowerShell en vez de CMD tradicional. El manejo de rutas y caracteres especiales es mucho más robusto en PowerShell.
- Evita espacios y tildes en los nombres de carpetas o archivos.
- Usa el comando recomendado para empaquetar tu módulo:

```powershell
7z a -tzip ../LockScreenBatterySaver-magisk.zip *
```

- Si tienes rutas largas, acorta los nombres de carpetas y archivos.

***

## 🔋 Error 3: El módulo no activa el Battery Saver automáticamente

**Síntomas:**  
El módulo se instala y el script parece funcionar, pero no se activa el modo Battery Saver.

### Soluciones

- Revisa los logs del script:

```bash
adb shell tail -f /data/adb/service.d/govbattery.log
```

- Asegúrate de que el script `govbattery.sh` tiene permisos ejecutables y su contenido está correcto.
- Comprueba que la ROM no está restringiendo el acceso a ciertas APIs de ahorro de batería.
- Verifica que el servicio esté funcionando en segundo plano tras reiniciar.

***

## 🔐 Error 4: Permisos insuficientes para cambiar governors o modos de batería

**Síntomas:**  
El log indica `Permission denied` al cambiar governor o activar Battery Saver.

### Soluciones

- Asegúrate de que el dispositivo está rooteado y tienes la última versión de Magisk.
- Verifica que el módulo tiene permisos de sistema y que la app fue instalada como **priv-app**.
- Si el archivo XML de permisos está incompleto, revisa su sintaxis y los permisos declarados, por ejemplo:

```xml
<permission name="android.permission.DEVICE_POWER" />
<permission name="android.permission.CHANGE_CONFIGURATION" />
```

- Si el script accede a archivos protegidos, asegúrate de que Magisk parcheó correctamente el boot image.

***

## 🧩 Error 5: El módulo no aparece en la lista de Magisk

### Soluciones

- Verifica que el archivo zip se empaquetó correctamente: revisa la estructura interna del zip (debe tener las siguientes carpetas y archivos mínimos):

```text
META-INF/
system/
service.d/
module.prop
```

- No añadas subcarpetas adicionales dentro del zip.
- Usa la opción **"Instalar desde almacenamiento"** en Magisk Manager, no la opción **"Descargar"**.

***

## 💡 Consejos generales

- Reinicia siempre el dispositivo tras instalar o actualizar el módulo.
- Comprueba el log generado en:

```text
/data/adb/service.d/govbattery.log
```

para obtener detalles de su ejecución.

- Si actualizas la app o el script, **recompila y empaqueta de nuevo** el módulo antes de reinstalar.

***

## 📄 Ejemplo rápido de empaquetado

Para referencia, este es el comando completo en PowerShell para generar el ZIP del módulo:

```powershell
cd "C:\Users\TU_USUARIO\Desktop\LockScreenBatterySaver\magisk_module"
7z a -tzip ../LockScreenBatterySaver-magisk.zip *
```

Esto creará el archivo `LockScreenBatterySaver-magisk.zip` listo para su instalación desde Magisk Manager.

***

