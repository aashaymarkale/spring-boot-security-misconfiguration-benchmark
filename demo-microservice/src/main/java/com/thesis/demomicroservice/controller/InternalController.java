package com.thesis.demomicroservice.controller;

import com.thesis.demomicroservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Internal statistics endpoint, intended for in-cluster use only.
 * See AUTH-004 in SecurityConfig: reachability is assumed to be
 * restricted at the network level, which cannot be verified from
 * the code in this repository.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final ItemService itemService;

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
            "totalItems", itemService.count(),
            "lowStockItems", itemService.countLowStock());
    }
}
