# Guía de ejecución y evidencias (PowerShell)

Instrucciones paso a paso para levantar el sistema y generar las evidencias de la
entrega. Ejecutar en **PowerShell**, en orden. Notas importantes:

- Usar **`curl.exe`** (no `curl`, que en PowerShell es un alias distinto).
- El login usa los campos **`usuario`** y **`clave`**.
- Si una cuenta ya no tiene saldo suficiente por pruebas anteriores, elegir otra
  (ver nota al final).

## 0. Ir a la carpeta del proyecto

```powershell
cd "ubicacion del archivo"
```

## 1. Levantar infraestructura Docker (MySQL + Kafka)

```powershell
docker start mysql-bank
docker compose up -d
docker ps
```

```powershell
# Crear tabla de idempotencia (solo la primera vez)
Get-Content .\scripts\sql\eventos_procesados.sql | docker exec -i mysql-bank mysql -uroot -proot bank_legacy
```

📸 Captura sugerida: `docker ps` mostrando `mysql-bank`, `kafka-bank`, `kafka-ui-bank`.

## 2. Compilar

```powershell
mvn clean package -DskipTests
```

## 3. Levantar los 5 servicios (una terminal/pestaña por servicio, en este orden)

```powershell
# Terminal 1
java -jar config-server/target/config-server-1.0.0.jar

# Terminal 2
java -jar eureka-server/target/eureka-server-1.0.0.jar

# Terminal 3
java -jar ms-cuentas/target/ms-cuentas-1.0.0.jar

# Terminal 4
java -jar ms-transacciones/target/ms-transacciones-1.0.0.jar

# Terminal 5
java -jar ms-operaciones/target/ms-operaciones-1.0.0.jar
```

📸 Capturas sugeridas: cada terminal arrancando, y http://localhost:8761 (Eureka
con los 3 microservicios registrados).

## 4. Evidencia del flujo de eventos (retiro → Kafka → ambos consumidores)

```powershell
# Login en ms-operaciones
$login = curl.exe -s -X POST http://localhost:8083/api/operaciones/auth/login `
  -H "X-API-KEY: operaciones-key-789" -H "Content-Type: application/json" `
  -d '{"usuario":"operador","clave":"op123"}' | ConvertFrom-Json
$token = $login.token

# Ejecutar retiro (publica el evento)
curl.exe -s -X POST http://localhost:8083/api/operaciones/retiro `
  -H "X-API-KEY: operaciones-key-789" -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" -d '{"cuentaId": 121, "monto": 5000}'
```

```powershell
# Verificar efecto en ms-operaciones (operacion anual registrada por el consumidor)
curl.exe -s "http://localhost:8083/api/operaciones/cuenta/121" `
  -H "X-API-KEY: operaciones-key-789" -H "Authorization: Bearer $token"

# Login en ms-transacciones
$loginT = curl.exe -s -X POST http://localhost:8082/api/transacciones/auth/login `
  -H "X-API-KEY: transacciones-key-456" -H "Content-Type: application/json" `
  -d '{"usuario":"analista","clave":"an123"}' | ConvertFrom-Json
$tokenT = $loginT.token

# Verificar efecto en ms-transacciones (el retiro aparece como transaccion)
curl.exe -s http://localhost:8082/api/transacciones/recientes `
  -H "X-API-KEY: transacciones-key-456" -H "Authorization: Bearer $tokenT"
```

```powershell
# Evidencia en la base de datos (evento procesado por ambos consumidores)
docker exec -i mysql-bank mysql -uroot -proot bank_legacy -e "SELECT * FROM operaciones_anuales ORDER BY registro_id DESC LIMIT 2; SELECT * FROM transacciones ORDER BY registro_id DESC LIMIT 2; SELECT * FROM eventos_procesados;"
```

📸 Capturas sugeridas: respuesta del retiro, ambos GET, la consulta SQL, los logs
`evento=retiro_publicado` / `operacion_registrada_desde_evento` /
`transaccion_registrada_desde_evento` en las terminales, y kafka-ui
(http://localhost:8090 → Topics → `retiros-realizados` → mensajes).

## 5. Evidencia de tolerancia a fallos (circuit breaker)

```powershell
# En la terminal de ms-cuentas: presionar Ctrl+C (simula servicio caido)

# Repetir el retiro varias veces: los primeros tardan ~4s (reintentos),
# luego el circuito ABRE y falla en milisegundos
1..5 | ForEach-Object {
  $r = curl.exe -s -w "`nHTTP %{http_code} - %{time_total}s`n" -X POST http://localhost:8083/api/operaciones/retiro `
    -H "X-API-KEY: operaciones-key-789" -H "Authorization: Bearer $token" `
    -H "Content-Type: application/json" -d '{"cuentaId": 121, "monto": 1000}'
  $r
}

# Estado de los circuit breakers (debe mostrar ms-cuentas en OPEN)
curl.exe -s http://localhost:8083/actuator/circuitbreakers
```

```powershell
# Recuperacion: volver a levantar ms-cuentas, esperar ~40s y reintentar el retiro
java -jar ms-cuentas/target/ms-cuentas-1.0.0.jar
# (en otra terminal, pasados ~40s)
curl.exe -s -X POST http://localhost:8083/api/operaciones/retiro `
  -H "X-API-KEY: operaciones-key-789" -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" -d '{"cuentaId": 121, "monto": 1000}'
```

📸 Capturas sugeridas: los 503 con tiempos (4s vs 0.006s), el JSON del actuator
con `"state":"OPEN"`, y el retiro exitoso tras la recuperación.

## Nota: elegir una cuenta con saldo

```powershell
docker exec -i mysql-bank mysql -uroot -proot bank_legacy -e "SELECT cuenta_id, saldo FROM cuentas ORDER BY registro_id DESC LIMIT 10;"
```

Usar una `cuentaId` con saldo suficiente en los comandos de retiro (y ajustar los
GET de verificación a esa misma cuenta).
