Esta guía describe los problemas más comunes que pueden surgir al desplegar LockScreen Battery Saver y cómo solucionarlos. Se basa en la experiencia real de instalación y configuración del módulo Magisk y la app asociada.

1. Error: La app no aparece tras instalar el módulo
Síntomas: El módulo se instala correctamente en Magisk, pero la app no aparece en el lanzador o no ejecuta su función.

Soluciones:

Verifica que el APK esté exactamente en:

magisk_module/system/priv-app/BatterySaverToggle/BatterySaverToggle.apk

Asegúrate de que el archivo XML de permisos esté presente y correctamente configurado:

magisk_module/system/etc/permissions/privapp-permissions-batterysaver.xml

Reinstala el módulo tras limpiar caché Dalvik y reiniciar el dispositivo.

Si usas una ROM personaliza, revisa que admita apps privilegiadas.

2. Error: Fallo al compilar el módulo por rutas/caracteres
Síntomas: Al usar comandos con 7-Zip o al copiar archivos, aparecen errores por rutas inválidas o caracteres especiales.

Soluciones:

Utiliza PowerShell en vez de CMD tradicional. El manejo de rutas y caracteres especiales es mucho más robusto en PowerShell.

Evita espacios y tildes en los nombres de carpetas.

Usa el comando recomendado:

powershell
7z a -tzip ../LockScreenBatterySaver-magisk.zip *
Si tienes rutas largas, acorta los nombres de carpetas y archivos.

3. Error: El módulo no activa el Battery Saver automáticamente
Síntomas: El módulo se instala y el script parece funcionar, pero no se activa el modo Battery Saver.

Soluciones:

Revisa los logs del script:

text
adb shell tail -f /data/adb/service.d/govbattery.log
Asegúrate de que el script govbattery.sh tiene permisos ejecutables y su contenido está correcto.

Comprueba que la ROM no esté restringiendo el acceso a ciertas APIs de ahorro de batería.

Verifica que el servicio esté funcionando en segundo plano tras reiniciar.

4. Error: Permisos insuficientes para cambiar governors o modos de batería
Síntomas: El log indica "Permission denied" al cambiar governor o activar Battery Saver.

Soluciones:

Asegúrate de que el dispositivo está rooteado y tienes la última versión de Magisk.

Verifica que el módulo tiene permisos de sistema y que la app fue instalada como priv-app.

Si el archivo XML de permisos está incompleto, revisa su sintaxis y los permisos declarados (android.permission.DEVICE_POWER, etc.).

Si el script accede a archivos protegidos, asegúrate de que Magisk parcheó correctamente el boot image.

5. Error: El módulo no aparece en la lista de Magisk
Soluciones:

Verifica que el archivo zip se empaquetó correctamente: revisa la estructura interna del zip (debe tener META-INF y carpetas system, service.d, etc.).

No añadas subcarpetas adicionales dentro del zip.

Usa la opción "Instalar desde almacenamiento" en Magisk Manager, no la opción "Descargar".

Consejos generales
Siempre reinicia tras instalar el módulo.

Comprueba el log generado en /data/adb/service.d/govbattery.log para detalles de funcionamiento.

Si actualizas la app o el script, compila y empaqueta de nuevo el módulo antes de reinstalar.