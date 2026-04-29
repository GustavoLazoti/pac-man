# Flap Bird — Documentação do Projeto

**Disciplina:** Jogos Digitais  
**Linguagem:** Java com libGDX  
**Recurso de Acessibilidade:** Suporte a pedal como dispositivo de entrada

---

## 1. Descrição do Projeto

O **Flap Bird** é um jogo 2D inspirado no clássico *Flappy Bird*, desenvolvido em Java utilizando o framework **libGDX** como projeto da disciplina de Jogos Digitais. O jogador controla um pássaro que deve desviar de obstáculos (canos) ao longo de um cenário de rolagem horizontal.

Um diferencial do projeto é o **suporte a pedal** como recurso de acessibilidade, permitindo que jogadores utilizem um pedal físico como alternativa ao teclado para acionar o salto do pássaro, tornando o jogo mais inclusivo.

---

## 2. Funcionalidades Principais

- Movimentação do pássaro com gravidade simulada
- Geração procedural de obstáculos (canos)
- Detecção de colisão
- Sistema de pontuação
- Controle por teclado (Espaço / seta para cima)
- **Controle por pedal** (acessibilidade)
- Níveis com dificuldade progressiva
- Telas de início, game over e placar

---

## 3. Tecnologias

| Tecnologia | Uso |
|---|---|
| Java (JDK 17+) | Linguagem principal |
| [libGDX](https://libgdx.com/) | Framework de desenvolvimento do jogo |
| Gradle | Gerenciamento de dependências e build |
| jSerialComm / JSSC | Leitura do pedal via porta serial (opcional) |

---

## 4. Planejamento de Entregas

### Entrega 1 — Protótipo Base
**Objetivo:** Jogo funcional com a mecânica principal implementada.

- Estrutura do projeto libGDX (core, desktop launcher)
- Game loop com libGDX (`ApplicationListener` / `Game`)
- Renderização do pássaro e dos canos com `SpriteBatch`
- Física básica (gravidade e salto)
- Detecção de colisão (`Rectangle` / `Intersector`)
- Pontuação simples exibida com `BitmapFont`
- Tela de game over

---

### Entrega 2 — Acessibilidade e Melhorias Visuais
**Objetivo:** Integrar o pedal e aprimorar a experiência visual.

- Mapeamento do pedal como dispositivo de entrada (via porta serial ou tecla dedicada)
- Camada de abstração de input (`InputMultiplexer`) para suportar teclado e pedal
- Instruções na tela sobre os controles disponíveis
- Sprites e animações do pássaro com `Animation` do libGDX
- Efeitos sonoros básicos com `Sound` / `Music` do libGDX
- Tela de início com seleção de controle

---

### Entrega 3 — Desenvolvimento de Níveis
**Objetivo:** Adicionar progressão de dificuldade e fases.

- Sistema de níveis com aumento gradual de velocidade
- Variação no espaçamento e posicionamento dos canos por nível
- Indicador visual do nível atual na HUD
- Transição entre níveis (tela intermediária ou efeito visual)
- Placar com high score salvo localmente (`Preferences` do libGDX)

---

### Entrega 4 — Polimento e Entrega Final
**Objetivo:** Refinamento geral, testes e apresentação.

- Testes de jogabilidade e correção de bugs
- Ajuste de balanceamento (velocidade, espaçamento, dificuldade)
- Tela de créditos
- Empacotamento do jogo (`.jar` executável via Gradle)
- Apresentação do projeto

---

## 5. Controles

| Ação | Teclado | Pedal |
|---|---|---|
| Pular | `Espaço` ou `↑` | Pressionar o pedal |
| Pausar | `P` | — |
| Reiniciar | `R` (após game over) | — |

---

## 6. Como Executar

**Pré-requisitos:** Java 17+ e Gradle instalados.

```bash
# Clonar o repositório
git clone https://github.com/GustavoLazoti/pac-man.git
cd pac-man

# Executar no desktop
./gradlew desktop:run
```

Para uso com pedal via porta serial, conecte o dispositivo antes de iniciar o jogo e selecione a opção **"Pedal"** na tela inicial.

---

## 7. Estrutura de Pastas (prevista)

```
flap-bird/
├── core/
│   └── src/
│       ├── FlapBirdGame.java        # Classe principal (Game)
│       ├── screens/
│       │   ├── MenuScreen.java
│       │   ├── GameScreen.java
│       │   └── GameOverScreen.java
│       ├── entities/
│       │   ├── Bird.java
│       │   └── Pipe.java
│       ├── input/
│       │   ├── KeyboardInput.java
│       │   └── PedalInput.java
│       ├── levels/
│       │   └── LevelManager.java
│       └── utils/
│           └── ScoreManager.java
├── desktop/
│   └── src/
│       └── DesktopLauncher.java
├── assets/
│   ├── sprites/
│   └── sounds/
├── build.gradle
└── README.md
```

---

## 8. Equipe

| Nome | Função |
|---|---|
| (preencher) | Desenvolvimento |
| (preencher) | Arte / Design |
| (preencher) | Testes / Documentação |
