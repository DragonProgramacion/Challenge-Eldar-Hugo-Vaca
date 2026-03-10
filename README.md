# API de Procesamiento de Transacciones

Servicio backend desarrollado con **Java 17** y **Spring Boot** que permite procesar archivos CSV con transacciones financieras.
El sistema procesa los archivos de forma **asíncrona** y expone una **API REST** para consultar el estado del procesamiento y la información resultante.

---

# Tecnologías utilizadas

* Java 17
* Spring Boot
* Spring Data JPA
* H2 Database (por defecto)
* MySQL (opcional)
* OpenCSV
* MapStruct
* SpringDoc OpenAPI (Swagger)

---

# Cómo ejecutar el proyecto

## 1. Clonar el repositorio

```bash
git clone https://github.com/DragonProgramacion/Challenge-Eldar-Hugo-Vaca.git
```

## 2. Ejecutar la aplicación

Con Maven:

```bash
./mvnw spring-boot:run
```

o

```bash
mvn spring-boot:run
```

La aplicación iniciará en:

```
http://localhost:8080
```

---

# Documentación de la API

La documentación interactiva de la API está disponible en **Swagger**:

```
http://localhost:8080/swagger
```

Desde allí es posible probar todos los endpoints directamente.

---

# Endpoints principales

## Iniciar procesamiento de archivo

Permite subir un archivo CSV y comenzar su procesamiento en segundo plano.

```
POST /process
```

Request (form-data):

```
file: transactions.csv
```

Respuesta:

```json
{
  "processingId": "uuid"
}
```

---

## Consultar estado del procesamiento

```
GET /process/{processingId}
```

Respuesta:

```json
{
  "status": "PROCESSING",
  "totalRecords": 1000,
  "processedRecords": 850,
  "errorRecords": 10
}
```

Estados posibles:

```
PENDING
PROCESSING
COMPLETED
FAILED
```

---

## Obtener balance de una cuenta

Devuelve el balance actual de una cuenta.

```
GET /accounts/{accountId}/balance
```

Respuesta:

```json
1200.50
```

El balance se calcula como:

```
créditos - débitos
```

---

## Top 10 cuentas con más transacciones

Devuelve las 10 cuentas con mayor cantidad de transacciones procesadas.

```
GET /accounts/top-accounts
```

Respuesta:

```json
[
  {
    "accountId": "acc5",
    "transactionCount": 120
  }
]
```

---

## Verificar si una transacción ya fue procesada

Permite verificar si una transacción ya fue registrada en el sistema.

```
GET /transactions/{transactionId}/status
```

Respuesta:

```json
{
  "transactionId": "t10",
  "wasProcessed": true
}
```

---

# Formato esperado del CSV

El archivo CSV debe tener el siguiente formato:

```
transactionId,accountId,amount,type,timestamp
t1,acc1,100.0,CREDIT,2024-01-01 10:00:00
t2,acc2,50.0,DEBIT,2024-01-01 10:05:00
```

Campos:

| Campo         | Descripción                           |
| ------------- | ------------------------------------- |
| transactionId | Identificador único de la transacción |
| accountId     | Identificador de la cuenta            |
| amount        | Monto de la transacción               |
| type          | Tipo de transacción (CREDIT o DEBIT)  |
| timestamp     | Fecha y hora de la transacción        |

---

# Decisiones de diseño

## Procesamiento asíncrono

El procesamiento de archivos se ejecuta de forma asíncrona utilizando:

```
@Async + ThreadPoolTaskExecutor
```

Esto permite:

* no bloquear la request HTTP
* procesar múltiples archivos en paralelo
* mejorar la escalabilidad del sistema

---

## Persistencia del progreso

Durante el procesamiento del archivo se actualiza periódicamente el estado del procesamiento.

Esto permite consultar el progreso mientras el archivo aún se está procesando.

---

## Detección de duplicados

Las transacciones se identifican de forma única por `transactionId`.

Se utiliza una restricción de unicidad en base de datos para evitar duplicados.

Cuando se detecta un duplicado:

* la transacción se ignora
* se registra un log de advertencia
* el procesamiento continúa

---

## Manejo de líneas inválidas

Las líneas corruptas o inválidas del CSV se registran como errores pero **no detienen el procesamiento completo del archivo**.

Esto permite que el sistema sea resiliente frente a datos incorrectos.

---

## Optimización de consultas

Se agregaron índices en la base de datos para optimizar consultas frecuentes:

* `transactionId` → detección de duplicados
* `accountId` → cálculo de balance y top cuentas

---

# Consideraciones de escalabilidad

El diseño permite escalar fácilmente el sistema en el futuro mediante:

* uso de colas de mensajería (Kafka / RabbitMQ)
* workers distribuidos
* almacenamiento externo de archivos (S3)
* frameworks de batch processing

---
