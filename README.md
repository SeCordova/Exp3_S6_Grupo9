# Banco XYZ — Microservicios con tolerancia a fallos y arquitectura de eventos

Sistema bancario distribuido (Experiencia 3, Semana 7 — Desarrollo Backend III, PBY2203).
Continuidad del proyecto de la semana 6, al que se agrega **mensajería asíncrona con
Apache Kafka** aplicando el **patrón Saga con coreografía**, y se refuerza la
**tolerancia a fallos con Resilience4j** (Circuit Breaker + Retry).

## Objetivo

Demostrar una arquitectura de microservicios resiliente y orientada a eventos:
el retiro de dinero se ejecuta como una transacción distribuida coordinada por
eventos, donde ningún fallo transitorio (servicio caído, base de datos no
disponible, broker inaccesible) deja el sistema en un estado inconsistente.

## Arquitectura de eventos elegida

**Kafka + patrón Saga con coreografía + entrega at-least-once con consumidores idempotentes.**

¿Por qué?

- El retiro es una **transacción distribuida** (ms-operaciones coordina, ms-cuentas
  descuenta el saldo, y dos historiales deben registrarse). El patrón Saga la divide
  en pasos locales encadenados por eventos.
- Se eligió **coreografía** (sin orquestador central): cada servicio es autónomo y
  reacciona a los eventos del tópico. El material del curso recomienda Kafka para
  coreografías por su escalabilidad y persistencia de eventos.
- Se eligió **Kafka sobre JMS** porque el log persistente del tópico retiene los
  eventos aunque un consumidor esté caído (y permite reprocesarlos), escala por
  particiones y admite múltiples grupos de consumidores independientes sobre el
  mismo evento. JMS elimina el mensaje al consumirlo y está pensado para
  comunicación punto a punto transaccional.
- La entrega **at-least-once** puede duplicar mensajes; por eso cada evento lleva
  un `eventoId` (UUID) y cada consumidor lo marca en la tabla `eventos_procesados`
  dentro de la misma transacción que el efecto de negocio (**consumidor idempotente**).

Además, la mensajería resuelve una limitación real del proyecto de la semana 6:
antes, si fallaba el registro local después de ejecutado el cargo, **el retiro
quedaba sin historial**. Ahora el evento persiste en Kafka y el consumidor lo
reintenta hasta registrarlo.

## Flujo de la saga (retiro)

```
POST /api/operaciones/retiro (ms-operaciones)
  → 1. consultarSaldo  (Feign → ms-cuentas)   [Circuit Breaker + Retry]
  → 2. ejecutarCargo   (Feign → ms-cuentas)   [Circuit Breaker + Retry]
  → 3. publica RetiroRealizadoEvent en tópico "retiros-realizados" (key = cuentaId)
       [Circuit Breaker publicacion-eventos;
        fallback: registro síncrono local (degrada al comportamiento de la semana 6)]
  → responde 200 con el saldo restante

Tópico retiros-realizados — dos grupos de consumidores independientes:
  ├─ grupo ms-operaciones   → registra la operación anual (tabla operaciones_anuales)
  └─ grupo ms-transacciones → registra la transacción  (tabla transacciones,
                              visible en GET /api/transacciones/recientes)
```

El evento `RetiroRealizadoEvent` contiene: `eventoId` (UUID), `cuentaId`, `monto`,
`saldoRestante`, `fecha` y `requestId` (trazabilidad con la solicitud HTTP original).

El diagrama completo de la solución está en
[docs/diagrama-arquitectura.md](docs/diagrama-arquitectura.md).

## Estructura del código

```
Exp3_S6_Grupo9-main/
├── pom.xml                        # POM padre (Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.5)
├── docker-compose.yml             # Kafka (KRaft) + kafka-ui
├── scripts/sql/
│   └── eventos_procesados.sql     # tabla de idempotencia de consumidores
├── docs/
│   └── diagrama-arquitectura.md   # diagrama de la arquitectura de eventos
├── bank-common/                   # contrato compartido
│   └── .../common/
│       ├── event/RetiroRealizadoEvent.java   # evento de dominio del retiro
│       ├── dto/ (CargoRequest, SaldoCuentaDTO)
│       ├── exception/ (ErrorResponse, CodigoError, excepciones de negocio)
│       └── filter/RequestIdFilter.java       # trazabilidad X-Request-ID (MDC)
├── config-server/                 # configuración centralizada (puerto 8888)
│   └── src/main/resources/config-repo/       # yml compartido + uno por servicio
├── eureka-server/                 # service discovery (puerto 8761)
├── ms-cuentas/                    # dueño del saldo (puerto 8081)
├── ms-transacciones/              # historial (puerto 8082)
│   └── .../transacciones/
│       ├── event/RetiroEventConsumer.java    # consume retiros → registra transacción
│       ├── model/EventoProcesado.java        # idempotencia
│       └── config/KafkaConsumerConfig.java   # reintentos del consumidor
└── ms-operaciones/                # orquestador del retiro (puerto 8083)
    └── .../operaciones/
        ├── event/RetiroEventProducer.java    # publica RetiroRealizadoEvent
        ├── event/RetiroEventConsumer.java    # consume retiros → registra operación anual
        ├── service/OperacionesService.java   # saga: saldo → cargo → evento
        └── config/KafkaConsumerConfig.java   # reintentos del consumidor
```

## Tolerancia a fallos (Resilience4j)

| Mecanismo | Dónde | Comportamiento |
|---|---|---|
| Circuit Breaker `ms-cuentas` | ms-operaciones → Feign | Si ms-cuentas falla, responde 503; el circuito se abre al 50 % de fallos |
| **Retry `ms-cuentas`** (nuevo) | ms-operaciones → Feign | 3 intentos con 500 ms de espera ante fallos transitorios, antes de abrir el circuito |
| Circuit Breaker `publicacion-eventos` (nuevo) | ms-operaciones → Kafka | Si el broker no está disponible, degrada al registro síncrono local |
| Circuit Breaker `persistencia-*` | los 3 ms → MySQL | Degradación controlada ante fallos de la base de datos |
| Reintentos del consumidor (nuevo) | listeners Kafka | 3 reintentos cada 2 s; el evento permanece en el tópico hasta ser procesado |
| Productor idempotente + `acks=all` | ms-operaciones → Kafka | Sin duplicados ni pérdida en la publicación |
| Consumidores idempotentes | listeners Kafka | `eventos_procesados` evita efectos duplicados (at-least-once) |

Estado de los circuit breakers en vivo: `GET /actuator/circuitbreakers` de cada servicio.

## Prerrequisitos

- Java 21 y Maven 3.9+
- Docker (contenedor MySQL `mysql-bank` con la base `bank_legacy` de la semana 6)

## Instrucciones de ejecución

1. **Base de datos**: contenedor MySQL de la semana 6 en marcha, y crear la tabla
   de idempotencia (una sola vez):
   ```bash
   docker start mysql-bank
   docker exec -i mysql-bank mysql -uroot -proot bank_legacy < scripts/sql/eventos_procesados.sql
   ```

2. **Kafka**:
   ```bash
   docker compose up -d
   ```
   kafka-ui queda disponible en http://localhost:8090 (tópicos, mensajes y grupos).

3. **Compilar** (desde la raíz del proyecto):
   ```bash
   mvn clean package -DskipTests
   ```

4. **Levantar los servicios** (cada uno en su terminal, en este orden):
   ```bash
   mvn -pl config-server spring-boot:run     # puerto 8888
   mvn -pl eureka-server spring-boot:run     # puerto 8761
   mvn -pl ms-cuentas spring-boot:run        # puerto 8081
   mvn -pl ms-transacciones spring-boot:run  # puerto 8082
   mvn -pl ms-operaciones spring-boot:run    # puerto 8083
   ```

## Cómo probar el flujo de eventos

```bash
# 1) Login en ms-operaciones (obtener JWT)
TOKEN=$(curl -s -X POST http://localhost:8083/api/operaciones/auth/login \
  -H "X-API-KEY: operaciones-key-789" -H "Content-Type: application/json" \
  -d '{"username":"operador","password":"op123"}' | jq -r .token)

# 2) Ejecutar un retiro (publica el evento en Kafka)
curl -s -X POST http://localhost:8083/api/operaciones/retiro \
  -H "X-API-KEY: operaciones-key-789" -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cuentaId": 1001, "monto": 5000}'

# 3) Verificar los efectos asíncronos del evento:
curl -s "http://localhost:8083/api/operaciones/cuenta/1001" \
  -H "X-API-KEY: operaciones-key-789" -H "Authorization: Bearer $TOKEN"

TOKENT=$(curl -s -X POST http://localhost:8082/api/transacciones/auth/login \
  -H "X-API-KEY: transacciones-key-456" -H "Content-Type: application/json" \
  -d '{"username":"analista","password":"an123"}' | jq -r .token)

curl -s http://localhost:8082/api/transacciones/recientes \
  -H "X-API-KEY: transacciones-key-456" -H "Authorization: Bearer $TOKENT"
```

En los logs de ms-operaciones se ve `evento=retiro_publicado` y
`evento=operacion_registrada_desde_evento`; en ms-transacciones,
`evento=transaccion_registrada_desde_evento`. El mensaje también es visible en
kafka-ui (http://localhost:8090).

## Cómo demostrar la tolerancia a fallos

- **ms-cuentas caído**: detener ms-cuentas y repetir el retiro → responde 503
  (`ServicioNoDisponibleException` vía fallback); en los logs se ven los
  reintentos de `@Retry` y la apertura del circuito en
  `GET http://localhost:8083/actuator/circuitbreakers`.
- **MySQL caído durante el consumo**: el consumidor reintenta 3 veces cada 2 s
  (log) y el evento no se pierde; al volver la base, se registra.
- **Kafka caído**: el retiro se confirma igual; el fallback del Circuit Breaker
  `publicacion-eventos` registra la operación de forma síncrona
  (`evento=publicacion_fallida` en el log).
