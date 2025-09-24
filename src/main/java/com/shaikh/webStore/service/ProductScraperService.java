package com.shaikh.webStore.service;

import com.shaikh.webStore.model.Category;
import com.shaikh.webStore.model.Product;
import com.shaikh.webStore.repository.CategoryRepository;
import com.shaikh.webStore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductScraperService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Scrape tous les produits d'une catégorie et les sauvegarde
     */
    public void scrapeProductsByCategory(String categoryName) throws IOException {
        // Récupérer la catégorie depuis la base
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + categoryName));

        // URL exacte de la catégorie stockée dans description
        String url = category.getDescription();

        System.out.println("→ Scraping produits de la catégorie : " + categoryName + " (" + url + ")");

        try {
            // Récupérer la page HTML
            Document doc = Jsoup.connect(url).get();

            // Sélectionner tous les <li> produits
            Elements productElements = doc.select("li.product.type-product");

            for (Element li : productElements) {
                // Nom du produit
                String name = li.select("h2.woocommerce-loop-product__title").text();

                // Description courte
                String description = li.select("div.os-product-excerpt p").text();

                // Photo
                String photoPath = li.select("img").attr("src");

                // Prix (à adapter selon ton HTML)
                Double price = null;
                Double originalPrice = null;

                // Stock, status, promo, discount
                Integer stock = null;
                String status = "INSTOCK";
                Boolean promo = false;
                Integer discount = 0;

                // Vérifier si le produit existe déjà
                Optional<Product> existingProduct = productRepository.findByName(name);
                if (existingProduct.isEmpty()) {
                    Product product = Product.builder()
                            .name(name)
                            .description(description)
                            .photoPath(photoPath)
                            .price(price)
                            .originalPrice(originalPrice)
                            .stock(stock)
                            .status(status)
                            .promo(promo)
                            .discount(discount)
                            .category(category)
                            .build();

                    productRepository.save(product);
                    System.out.println("✅ Produit inséré : " + name);
                } else {
                    System.out.println("⚡ Produit déjà existant : " + name);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du scraping de la catégorie " + categoryName + " : " + e.getMessage());
        }
    }
}
