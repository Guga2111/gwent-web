package com.gwent.api.catalog;

import com.gwent.engine.domain.Faction;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Order(2)
public class CardCatalogCache implements ApplicationRunner {

    private final CardCatalogRepository cardCatalogRepository;
    private Map<String, CardEntity> byId;
    private Map<Faction, List<CardEntity>> byFaction;

    public CardCatalogCache(CardCatalogRepository cardCatalogRepository) {
        this.cardCatalogRepository = cardCatalogRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<CardEntity> all = cardCatalogRepository.findAll();
        byId = all.stream().collect(Collectors.toMap(CardEntity::getId, c -> c));
        byFaction = all.stream().collect(Collectors.groupingBy(CardEntity::getFaction));
    }

    public CardEntity getById(String id) {
        CardEntity card = byId.get(id);
        if (card == null) {
            throw new IllegalArgumentException("Card not found: " + id);
        }
        return card;
    }

    public Map<String, CardEntity> getAllById(Collection<String> ids) {
        Map<String, CardEntity> result = new HashMap<>();
        for (String id : ids) {
            CardEntity card = byId.get(id);
            if (card == null) {
                throw new IllegalArgumentException("Card not found: " + id);
            }
            result.put(id, card);
        }
        return result;
    }

    public List<CardEntity> getByFactionIn(List<Faction> factions) {
        return factions.stream()
                .flatMap(f -> byFaction.getOrDefault(f, List.of()).stream())
                .toList();
    }
}
