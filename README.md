<p align="center">
  <img src="https://raw.githubusercontent.com/DrakesCraft-Labs/DrakesCrates/master/banner.svg" width="100%" alt="DRAKES CRATES animated banner" />
</p>

# DrakesCrates

> ### 🏰 ¡Únete a la Comunidad Oficial de DrakesCraft!
> 
> * 🎮 **IP del Servidor**: `play.drakescraft.cl` *(Java 1.21.11 & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/drakescraft](https://discord.gg/rR7FbfCt9Y)
> * 🌐 **Web & Guía**: [web.drakescraft.cl](https://web.drakescraft.cl) — 🛒 **Tienda**: [web.drakescraft.cl/store](https://web.drakescraft.cl/store.html)
> 
> *¡Juega con este addon y más de 80 expansiones optimizadas en vivo en nuestra network de supervivencia técnica!*

---

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

## Architecture heredada del Core
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


## ⚖️ Upstream Attribution & License / Licencia y Créditos

- **Original Project / Upstream**: Slimefun4 Community Addon.
- **Port & Maintenance**: DrakesCraft Labs team (Compatibility for Paper / Purpur 1.21.11).
- **License**: GPL-3.0 / MIT.
- **Source Code**: [GitHub Repository](https://github.com/DrakesCraft-Labs/DrakesCrates)
- **Support & Issues**: [GitHub Issues](https://github.com/DrakesCraft-Labs/DrakesCrates/issues) | [Discord](https://discord.gg/rR7FbfCt9Y)

*This project is an open-source derivative work maintained by DrakesCraft Labs under the terms of its original license. All original assets and concepts belong to their respective creators.*
