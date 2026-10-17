package com.thesis.demomicroservice.service;

import com.thesis.demomicroservice.entity.Item;
import com.thesis.demomicroservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Seeds demo inventory data in the dev profile only. */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        itemRepository.save(Item.builder()
            .sku("SKU-001").name("Widget").quantity(25).build());
        itemRepository.save(Item.builder()
            .sku("SKU-002").name("Gadget").quantity(3).build());
        itemRepository.save(Item.builder()
            .sku("SKU-003").name("Gizmo").quantity(0).build());
    }
}
