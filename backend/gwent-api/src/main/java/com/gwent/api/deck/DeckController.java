package com.gwent.api.deck;

import com.gwent.api.deck.dto.DeckDto;
import com.gwent.api.deck.dto.SaveDeckRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public ResponseEntity<List<DeckDto>> getUserDecks(Principal principal) {
        return ResponseEntity.ok(deckService.getUserDecks(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckDto> getDeck(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(deckService.getDeck(id, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<DeckDto> createDeck(@RequestBody SaveDeckRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deckService.createDeck(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckDto> updateDeck(@PathVariable UUID id, @RequestBody SaveDeckRequest request, Principal principal) {
        return ResponseEntity.ok(deckService.updateDeck(id, principal.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable UUID id, Principal principal) {
        deckService.deleteDeck(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
