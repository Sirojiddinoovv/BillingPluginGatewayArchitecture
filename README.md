# Billing: Pluggable Payments Architecture

> Kotlin/Spring Boot plugin-based payment gateway with dynamic adapter loading and a unified adapter registry.

## Overview

This project implements a **plugin architecture** for payment processors (Uzcard, Humo, Visa, MasterCard, ABS, etc.). Adapters are distributed as JAR plugins and discovered/loaded at runtime. The core app exposes HTTP endpoints to interact with the registry and perform operations.

### Key Components

- `plugin-architect/billing/Billing/src/main/kotlin/uz/nodir/billing/service/plugin/PluginLoader.kt` — package `uz.nodir.billing.service.plugin`; declares: PluginLoader.
- `plugin-architect/billing/Billing/src/main/kotlin/uz/nodir/billing/service/registry/PaymentPluginRegistry.kt` — package `uz.nodir.billing.service.registry`; declares: PaymentPluginRegistry.
- `plugin-architect/pay-common/PayCommon/src/main/kotlin/uz/nodir/paycommon/adapter/PaymentAdapter.kt` — package `uz.nodir.paycommon.adapter`; declares: PaymentAdapter.

## Configuration

Configuration lives in `plugin-architect/billing/Billing/build/resources/main/application.yml`. Example:

```yaml
plugins:
    dir: D:\testDir
    watch: true       #watcher is enabled?
    adapters:
     enabled: [ ]        # if empty, enabled all adapter
     disabled: [ ]       # otherwise, all are active except those listed
```

### How Plugins are Loaded


1. On startup, `PluginLoader` scans the configured `plugins.dir` for `*.jar` files.
2. Each JAR is opened with a `URLClassLoader`. The loader uses `ServiceLoader<PaymentAdapter>` to discover adapters declared in `META-INF/services/uz.nodir.paycommon.adapter.PaymentAdapter` inside the plugin.
3. Discovered adapters are registered into `PaymentPluginRegistry` keyed by `Processor` enum (`UZCARD`, `HUMO`, `VISA`, `MASTER`, `ABS`, etc.). Concurrent updates use `ConcurrentHashMap` and `putIfAbsent` to avoid duplicates.
4. If `watch: true`, a file-watcher monitors the plugins directory and triggers reload when JARs are added/updated.

### SPI Contract


Each plugin implements the `PaymentAdapter` interface and provides its `id: Processor` plus operations like `debit`, `reverse`, and `check`. Example (simplified):

```kotlin
interface PaymentAdapter {
    val id: Processor
    fun debit(request: DebitRequest): DebitResponse
    fun reverse(requestId: String): ReverseResponse
    fun check(requestId: String): CheckResponse
}
```

### Registry


`PaymentPluginRegistry` keeps a map `Processor → PaymentAdapter`. It registers built-in beans from Spring context on startup and can accept dynamically loaded plugins:

```kotlin
@Component
class PaymentPluginRegistry(
  private val paymentAdapters: List<PaymentAdapter>
) {
  private val adapters = ConcurrentHashMap<Processor, PaymentAdapter>()
  init { paymentAdapters.forEach { adapters.putIfAbsent(it.id, it) } }
  fun register(adapter: PaymentAdapter) { adapters[adapter.id] = adapter }
  fun get(processor: Processor) = adapters[processor]
}
```

## Postman Examples

**Collection:** `plugin-architect/plugin management.postman_collection.json` — *plugin management*

Endpoints:

- `GET http://localhost:8883/api/v1/pay/adapters` — get adapters
  <details><summary>Sample response</summary>

```json
[
    "UZCARD",
    "HUMO"
]
```
</details>
- `PATCH http://localhost:8883/api/v1/pay/reload` — reload
- `POST http://localhost:8883/api/v1/pay/UZCARD/debit` — uzcard debit
  <details><summary>Request body</summary>

```json
{
  "operationId": "operationId_ac8cb9418f53",
  "amount": 10,
  "currency": "currency_e64339d573dd",
  "from": "from_0074a5403876",
  "to": "to_4f98d230d734",
  "metadata": {}
}
```
</details>
  <details><summary>Sample response</summary>

```json
{
    "success": true,
    "code": "0",
    "message": "SUCCESS",
    "payload": {
        "rrn": "015352"
    }
}
```
</details>
- `POST http://localhost:8883/api/v1/pay/HUMO/debit` — humo debit
  <details><summary>Request body</summary>

```json
{
  "operationId": "operationId_ac8cb9418f53",
  "amount": 10,
  "currency": "currency_e64339d573dd",
  "from": "from_0074a5403876",
  "to": "to_4f98d230d734",
  "metadata": {}
}
```
</details>
  <details><summary>Sample response</summary>

```json
{
    "success": true,
    "code": "OK",
    "message": "Debited via Humo",
    "payload": {
        "provider": "HUMO",
        "operationId": "operationId_ac8cb9418f53"
    }
}
```
</details>

## Build & Run

Using **Gradle**:

```bash
./gradlew clean build
java -jar build/libs/*.jar
```

### Plugins Directory Layout


Place adapter JARs under the configured `plugins.dir`:
```
plugins/
 ├─ uzcard-adapter-1.0.jar
 ├─ humo-adapter-1.0.jar
 └─ visa-adapter-1.0.jar
```
Each adapter JAR must include a `META-INF/services/uz.nodir.paycommon.adapter.PaymentAdapter` file listing the adapter implementation class.

## HTTP API (generic)


Common endpoints you will likely find in the collection:
- `POST /api/payments/debit` — debit via specific `processor`.
- `POST /api/payments/reverse` — reverse by `requestId`.
- `GET /api/payments/check/{requestId}` — check status.

All endpoints route internally to the `PaymentPluginRegistry` which delegates to the adapter matching the provided `processor`.

## Troubleshooting


- **Spring can't see plugin beans**: plugins are not Spring-managed; they are loaded via `ServiceLoader`. Expose them through `PaymentPluginRegistry`.
- **ClassNotFoundException / NoClassDefFoundError**: ensure plugin JAR includes all *compile-time* deps, or shade them to avoid conflicts.
- **Adapter not found**: verify `Processor` id matches the enum and that `enabled/disabled` config doesn't filter it out.
- **Windows path issues**: prefer forward slashes or absolute paths for `plugins.dir`.


## License

Proprietary / internal. Update as appropriate.
