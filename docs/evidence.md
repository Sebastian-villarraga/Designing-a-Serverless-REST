# Evidencia

- Predicci?n DELETE: commit cfd44a5, anterior a su implementaci?n.
- `mvn test`: 11 pruebas, 0 fallos, 0 errores.
- `mvn package`: BUILD SUCCESS; generado target/orders-api.jar.
- Entorno local: JDK 23 con `--release 17` (no se ejecut? un runtime JDK 17 local).
- JAR inspeccionado: handlers y dependencias presentes, clases del proyecto con bytecode 61 (Java 17), JUnit ausente.
- Plantilla JSON: estructura y cuatro rutas por arquitectura verificadas; no equivale a validaci?n SAM/VTL.
- Script smoke: sintaxis PowerShell comprobada.

Las verificaciones registradas son locales; no constituyen evidencia de despliegue ni ejecucion en AWS.
