package com.pixelpro.data;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(3)
@RequiredArgsConstructor
public class CatalogDataInit implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        System.out.println("📚 Inicializando categorías y productos de catálogo...");

    }
}
