# Mine Show Me (Fabric)

Language: [English](#english-en) | [Português (BR)](#português-pt-br)

## English (EN)

Lightweight utility HUD to display quick in‑game information (coordinates, biome, time, performance and more) in a configurable way.

## Features
Implemented:
- Coordinates (XYZ + facing)
- Light level (block & sky)
- Current biome
- World day (counter)
- FPS
- (Base HUD structure)

## Requirements
- Minecraft: 1.21.x (define)
- Fabric Loader: 0.17.x or newer
- Fabric API: 0.133.x
- Java: 17

## Installation (End User)
1. Download this mod .jar (Release or Builds).
2. Put it in: .minecraft/mods
3. Ensure Fabric Loader + Fabric API are present.
4. Launch the game.

## Build (Dev)
Prerequisites: Java 17, Gradle (or included wrapper).

```bash
# first time
./gradlew build
# resulting artifact
build/libs/<show-me>-<version>.jar
```

For continuous development:
```bash
./gradlew runClient
```

## Configuration
(Planned) File: config/mine-show-me.json  
Future example:
```json
{
  "show_coordinates": true,
  "show_light": true,
  "show_biome": true,
  "show_day": true,
  "theme": "dark",
  "position": "TOP_LEFT"
}
```

## Suggested Structure (Code)
- package hud.widgets (each component)
- WidgetRegistry to register & order
- Central render loop only calling visible widgets

## Contributing
1. Fork
2. Branch: feature/name
3. Small, descriptive commits
4. Pull Request with summary + screenshots

Commit pattern (suggestion):
feat:, fix:, perf:, docs:, refactor:, build:, chore:, test:

## Testing
- Check overlap with other HUD mods
- Verify debug mode + F3
- Test on remote server (ping)

## License
To define (e.g.: MIT / LGPL / ARR).  
(Add LICENSE file later)

## FAQ
Q: Works on Forge?  
A: No. Fabric only.

Q: Heavy on FPS?  
A: Lightweight. Minimal text and no heavy loops.

Q: Can I use it in a modpack?  
A: Yes (respect chosen license).

---
Update this README as features are completed.

## Português (PT-BR)

HUD utilitário para exibir informações rápidas do jogo (coordenadas, bioma, horário, performance e mais) de forma leve e configurável.

## Funcionalidades
Implementadas:
- Coordenadas (XYZ + direção)
- Brilho/Light level (bloco e céu)
- Bioma atual
- Dia do mundo (contador)
- FPS
- (Estrutura base do HUD)

## Requisitos
- Minecraft: 1.21.x (definir)
- Fabric Loader: 0.17.x ou superior
- Fabric API: 0.133.x
- Java: 17

## Instalação (Usuário Final)
1. Baixe o .jar deste mod (Release ou Builds).
2. Coloque em: .minecraft/mods
3. Certifique-se de ter Fabric Loader + Fabric API.
4. Inicie o jogo.

## Compilação (Dev)
Pré-requisitos: Java 17, Gradle (ou wrapper incluso).

```bash
# primeira vez
./gradlew build
# artefato resultante
build/libs/<show-me>-<versão>.jar
```

Para desenvolvimento contínuo:
```bash
./gradlew runClient
```

## Configuração
(Planejado) Arquivo: config/mine-show-me.json  
Exemplo (futuro):
```json
{
  "show_coordinates": true,
  "show_light": true,
  "show_biome": true,
  "show_day": true,
  "theme": "dark",
  "position": "TOP_LEFT"
}
```

## Estrutura Sugerida (Código)
- package hud.widgets (cada componente)
- WidgetRegistry para registrar e ordenar
- Render loop central chamando somente visíveis

## Contribuição
1. Fork
2. Branch feature/nome
3. Commit pequeno e descritivo
4. Pull Request com resumo + screenshots

Padrão de commit (sugestão):
feat:, fix:, perf:, docs:, refactor:, build:, chore:, test:

## Testes
- Verificar sobreposição com outros mods de HUD
- Checar modo debug + F3
- Testar em servidor remoto (ping)

## Licença
Definir (ex: MIT / LGPL / ARR).  
(Adicionar arquivo LICENSE posteriormente)

## FAQ
P: Funciona em Forge?  
R: Não. Apenas Fabric.

P: Consome muito FPS?  
R: Leve. Textos mínimos e sem loops pesados.

P: Pode usar em modpack?  
R: Sim (respeite a licença escolhida).

---
Atualize este README conforme as features forem concluídas.
