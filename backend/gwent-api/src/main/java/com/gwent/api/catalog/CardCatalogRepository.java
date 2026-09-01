package com.gwent.api.catalog;

import com.gwent.engine.domain.Faction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardCatalogRepository extends JpaRepository<CardEntity, String> {
    List<CardEntity> findByFaction(Faction faction);
    List<CardEntity> findByFactionIn(List<Faction> factions);
}
