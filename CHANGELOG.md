Registro de versiones y cambios realizados en el proyecto LockScreen Battery Saver.

[v2.0.0] - 2025-11-11
Estado: Funcional (no final)

Añadido
Implementación completa y verificada del módulo Magisk con integración de la app como privilegio del sistema.

Automatización de activación/desactivación de Battery Saver y cambio de governor desde lockscreen.

Documentación profesional actualizada: README, INSTALLATION, ARCHITECTURE y TROUBLESHOOTING.

Sistema de logs integrados y consultables desde la app.

Instalación segura y empaquetado del módulo con 7-Zip y PowerShell.

Gestión de errores y troubleshooting basado en experiencia de despliegue.

Scripts de testeo funcional en la carpeta /tests/.

Mejorado
Flujo de instalación detallado y robusto para evitar errores típicos.

Permisos XML revisados y encaminados para ROMs compatibles.

Pendiente
Mejoras en la interfaz gráfica para configuración avanzada.

Testing automatizado y dashboard de métricas de ahorro de batería.

[v1.0.0] - 2025-11-09
Estado: Generación inicial de la app y la interfaz

Añadido
Primera versión de la app Android: interfaz principal y visualización de logs básicos.

Desarrollo del método de activación de Battery Saver mediante PowerManager.

Documentación básica de la arquitectura y esquema de pruebas iniciales.

Observaciones
App funcional pero sin integración de módulo Magisk.

No incluye privilegios completos ni cambios automatizados de governor; funcionalidades avanzadas desarrolladas posteriormente.