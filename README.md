# DrakesCrates

Plugin de crates extraido desde el modulo `drakescrates` del antiguo `DrakesCore`.

## Objetivo
Gestionar cajas de premios con llaves fisicas, ruleta visual y edicion de probabilidades sin tocar YAML manualmente.

## Que hace hoy
- Comando admin: `/drakescrates givekey|editor|reload`.
- Apertura de crates por bloque registrado en `crates.yml`.
- Animacion tipo ruleta antes de entregar premio.
- Editor GUI para ajustar `chance` por reward.
- Preview pasivo al click izquierdo en el bloque de crate.
- PlaceholderAPI: `%drakescrates_keys_physical%`.
- PlaceholderAPI por llave: `%drakescrates_keys_<key_id>%` (ej: `%drakescrates_keys_basic_key%`).
- Recarga runtime de `crates.yml` y `crates-settings.yml` con `/drakescrates reload`.

## Arquitectura heredada del Core
- `application/`: casos de uso y repositorio.
- `domain/`: `Crate`, `Reward`, `Key`, `OpenResult`.
- `infrastructure/`: parser YAML y settings.
- `presentation/`: comandos, listeners, editor, animacion.

## Configuracion
- `src/main/resources/crates.yml`
- `src/main/resources/crates-settings.yml`

## Dependencias
- Paper 1.20.6
- Java 21
- PlaceholderAPI (opcional)

## Pendiente real
- Reportes/export de configuracion para auditoria de economia.
