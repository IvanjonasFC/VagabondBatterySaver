# Recuperación en caso de Brickeo

> **Atención:** Estas instrucciones son una guía general. Asumen que tienes experiencia básica en recuperación de Android, acceso a un ordenador y que tu dispositivo permite desbloqueo o reinstalación por Fastboot o Recovery. Cada modelo puede variar: si tienes dudas, consulta foros o documentación de tu dispositivo.

## 1. Identifica el tipo de brick

- **Bootloop/Soft-brick:** El móvil enciende, pero queda reiniciando o atascado en el logo. Suele permitir entrar a fastboot o recovery.

- **Hard-brick:** El dispositivo no responde, no enciende ni accede a fastboot/recovery. En este caso, busca soporte específico del fabricante o comunidades como XDA.

## 2. Intenta acceder a Recovery o Fastboot

- Apaga el dispositivo completamente.

- Mantén pulsadas las teclas específicas de tu modelo (normalmente Vol- + Power para Fastboot, Vol+ + Power para Recovery) hasta que aparezca el menú correspondiente.

- Si tienes TWRP u otro recovery avanzado, puedes restaurar un backup si tenías uno hecho previamente.

## 3. Elimina el módulo Magisk desde Recovery o con ADB

Si el problema fue causado por el módulo:

### Opción A: Desde Recovery (TWRP)

- Monta el sistema en modo lectura/escritura ("Mount System").

- Entra en "Advanced" > "File Manager" o usa la terminal.

- Navega a la ruta y elimina el módulo problemático:
```sh
rm -rf /data/adb/modules/com.batterysaver
```

- Si no tienes claro qué módulo es el problema, puedes eliminar toda la carpeta de módulos:
```sh
rm -rf /data/adb/modules/*
```

- Reinicia el dispositivo y verifica si arranca correctamente.

### Opción B: Desde PC usando ADB y Recovery

- Con el móvil en Recovery (y partición `/data` montada), conecta el móvil al PC.

- Ejecuta en tu PC:

```sh
- adb shell
su
rm -rf /data/adb/modules/com.batterysaver
exit
```

- Si el módulo tiene otro nombre, cambia `com.batterysaver` por el que corresponda.

## 4. Si sigue sin arrancar: reinstala el sistema

- Descarga la ROM oficial o una custom ROM compatible desde la web de tu fabricante o de la comunidad.

- En Fastboot (bootloader desbloqueado):

  - Conecta el móvil al PC.

  - Usa el comando `fastboot flash` para cada partición siguiendo las instrucciones de la ROM descargada.

- En Recovery (TWRP): Utiliza la opción "Install" para flashear el archivo ZIP de la ROM.

- Después, reinicia y sigue el proceso de inicio de Android.

## 5. Otros recursos y recomendaciones

- Consulta XDA-Developers o foros específicos de tu modelo para métodos de "unbrick" adaptados a tu dispositivo.

- Mantén siempre copias de seguridad antes de experimentar.

- Haz pruebas con precaución y documenta los cambios importantes que realices.



