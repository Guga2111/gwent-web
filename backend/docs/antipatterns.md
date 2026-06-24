# Gwent Engine — Anti-Patterns

## 1. Colocar lógica de jogo no state

State classes são containers. Nunca tomam decisões sobre regras.

```java
// ERRADO
public class PlayerState {
    public void playCard(Card card, RowType row) {
        if (!hand.contains(card)) throw ...  // validação é do core
        hand.remove(card);
        // qual linha? com que efeito? isso é regra — pertence ao core
    }
}
```

---

## 2. Usar IllegalStateException genérico

Toda exceção lançada pela engine deve ser uma subclasse de `GwentException` com semântica clara.
O caller precisa saber se foi erro do jogador (`InvalidCommandException`) ou bug da engine (`InvalidStateTransitionException`).

```java
// ERRADO
throw new IllegalStateException("Leader already used");

// CORRETO
throw new LeaderAlreadyUsedException();
```

---

## 3. Commands carregando contexto de transporte

Commands não sabem nada de sessões, conexões WebSocket ou HTTP. São dados puros da intenção do jogador.
O contexto de quem enviou o command é resolvido na camada de transporte, não na engine.

```java
// ERRADO
public record PlayCardCommand(String sessionId, Card card, RowType row) {}

// CORRETO
public record PlayCardCommand(Card card, RowType targetRow) {}
```

---

## 4. Commands para ações iniciadas pela engine

Coin flip, draw automático de cartas, reset de round — são transições da engine, não ações do jogador.
Não criar commands para isso; implementar como métodos no core.

```java
// ERRADO
new CoinFlipCommand(Turn.PLAYER_1)

// CORRETO
gameEngine.resolveCoinFlip(state) // método interno do core
```

---

## 5. Importar Spring/framework na gwent-engine

O módulo `gwent-engine` tem zero dependências de framework. Qualquer anotação Spring (`@Service`, `@Component`, `@Autowired`) ou dependência HTTP pertence ao `gwent-api`.

---

## 6. Adicionar eventos/callbacks de WebSocket na engine

A engine não emite eventos para clientes. Ela muta o estado e para por aí.
Notificações de rede são responsabilidade do `gwent-api`.

```java
// ERRADO — engine não sabe de WebSocket
public void execute(GameState state, GameCommand command) {
    ...
    webSocketSession.sendMessage(...); // NUNCA
}
```

---

## 7. Booleans para estados com semântica de domínio

Quando um valor representa um conceito do jogo, use enum. Booleans escondem a intenção.

```java
// ERRADO
boolean isPlayer1Turn;

// CORRETO
Turn currentTurn; // PLAYER_1 ou PLAYER_2 — auto-documentado
```

---

## 8. Over-engineering antecipado

Não implementar funcionalidades para requisitos hipotéticos futuros.
Exemplos do que NÃO fazer antes de ter requisito concreto:
- Retornar `GameState` imutável de `execute()` para suportar undo
- Emitir `List<GameEvent>` do core para suportar replay
- Criar abstrações genéricas para um único caso de uso
