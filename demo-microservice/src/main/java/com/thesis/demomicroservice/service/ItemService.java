package com.thesis.demomicroservice.service;

import com.thesis.demomicroservice.entity.Item;
import com.thesis.demomicroservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item create(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes an item.
     *
     * <p>VULNERABILITY-TP [AUTHZ-004]: @PreAuthorize without method
     * security enabled.
     * <br>Intent: only holders of the ADMIN key (ROLE_ADMIN) may delete
     *   items; ordinary service-key holders (ROLE_SERVICE) must not.
     * <br>Risk: the annotation below is silently IGNORED, because
     *   SecurityConfig does not declare @EnableMethodSecurity. Any
     *   caller with the ordinary service key can therefore delete items.
     * <br>Why this is a TRUE POSITIVE: the restriction is expressed in
     *   code but never enforced, and a ROLE_ADMIN principal does exist
     *   (see ApiKeyAuthFilter), so the intent is realisable. The
     *   evidence is spread across two files (this annotation plus the
     *   missing enabler in SecurityConfig), which makes it hard for
     *   pattern-based tools to detect.
     * <br>Expected SonarQube detection: UNVERIFIED (likely not
     *   flagged -- confirm after scan; record the real outcome in
     *   findings.csv).
     * <br>Expected LLM classification: TRUE_POSITIVE
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    public long count() {
        return itemRepository.count();
    }

    public long countLowStock() {
        return itemRepository.countByQuantityLessThan(5);
    }
}
