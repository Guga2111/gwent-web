package com.gwent.api.catalog;

import com.gwent.api.catalog.dto.CatalogCardDto;
import com.gwent.engine.domain.Faction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CardCatalogRepository cardCatalogRepository;

    public CatalogController(CardCatalogRepository cardCatalogRepository) {
        this.cardCatalogRepository = cardCatalogRepository;
    }

    @GetMapping
    public ResponseEntity<List<CatalogCardDto>> getCards(
            @RequestParam Faction faction,
            @RequestParam(defaultValue = "true") boolean includeNeutral
    ) {
        List<Faction> factions = new ArrayList<>();
        factions.add(faction);
        if (includeNeutral) {
            factions.add(Faction.NEUTRAL);
        }

        List<CatalogCardDto> cards = cardCatalogRepository.findByFactionIn(factions).stream()
                .map(e -> new CatalogCardDto(
                        e.getId(), e.getName(), e.getFaction(), e.getCardType(),
                        e.getAbility(), e.getLeaderAbility(), e.getRowType(),
                        e.getBasePower(), e.getDeckCopies()))
                .toList();

        return ResponseEntity.ok(cards);
    }
}
