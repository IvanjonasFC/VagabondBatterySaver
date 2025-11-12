#!/system/bin/sh

# Script Post FS Data para Lock Screen Battery Saver
# Funciones principales:
# 1. Crear la estructura de carpetas necesarias para el script
# 2. Copiar el script principal de monitorización
# 3. Conceder permisos especiales a la app si está instalada

#Directorio Actual
MODDIR=${0%/*}  

# Esperamos 30 segundos para asegurar que el sistema está completamente iniciado
sleep 30



# Crea la carpeta para scripts si no existe  y suprimimos los errores si ya existe
mkdir -p /data/adb/service.d 2>/dev/null 

# Copiamos el scipt del modulo en la carpeta de script 
if [ -f "$MODDIR/service.d/govbattery.sh" ]; then
    cp -f "$MODDIR/service.d/govbattery.sh" /data/adb/service.d/
    # Le damos permisos de ejecucion
    chmod 755 /data/adb/service.d/govbattery.sh  
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ✅ Script copiado a /data/adb/service.d/" >> /data/adb/batterysaver_install.log
else
    # Guardamos el error 
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ✗ ERROR: govbattery.sh no encontrado en el módulo" >> /data/adb/batterysaver_install.log
fi


# Buscamos la app
if pm list packages | grep -q "com.batterysaver"; then
    # Le damos permisos a la aplicacion para obtener mayores ajustes
    pm grant com.batterysaver android.permission.WRITE_SECURE_SETTINGS 2>/dev/null
    
    # Guardamos si pudo o no darle permisos
    if [ $? -eq 0 ]; then
        echo "$(date '+%Y-%m-%d %H:%M:%S'): ✅ Permisos concedidos a com.batterysaver" >> /data/adb/batterysaver_perms.log
    else
    
        echo "$(date '+%Y-%m-%d %H:%M:%S'): ⚠️  ADVERTENCIA: Fallo al conceder permisos (puede ser normal si la app no está instalada)" >> /data/adb/batterysaver_perms.log
    fi
else
    echo "$(date '+%Y-%m-%d %H:%M:%S'): ℹ️  App com.batterysaver no instalada (solo script funcionará)" >> /data/adb/batterysaver_perms.log
fi

exit 0 
