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
public class ScraperService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    private static final int MAX_LENGTH_NAME = 255;
    private static final int MAX_LENGTH_DESCRIPTION = 1000;
    private static final int MAX_LENGTH_PHOTO = 500;

    // Scraper les catégories depuis la page principale
    public void scrapeAndSaveCategories(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements categoryElements = doc.select("span.wc-block-product-categories-list-item__name");

        for (Element catElement : categoryElements) {
            String categoryName = safeText(catElement.text(), MAX_LENGTH_NAME);
            String categoryUrlPart = categoryName.toLowerCase().replace(" ", "-");

            Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
            Category category = existingCategory.orElseGet(() -> {
                Category c = Category.builder()
                        .name(categoryName)
                        .description(categoryName)
                        .build();
                return categoryRepository.save(c);
            });

            String categoryUrl = url + "/product-category/" + categoryUrlPart + "/";
            scrapeAndSaveProducts(categoryUrl, category);
        }
    }

    // Scraper les produits d'une catégorie spécifique
    private void scrapeAndSaveProducts(String url, Category category) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements productElements = doc.select("div.thunk-product-wrap");

        for (Element prodElement : productElements) {
            try {
                String name = safeText(getText(prodElement, "h2.woocommerce-loop-product__title"), MAX_LENGTH_NAME);

                // Vérification si le produit existe déjà
                if (productRepository.findByName(name).isPresent()) {
                    System.out.println("Produit déjà existant ignoré : " + name);
                    continue;
                }

                String link = getAttr(prodElement, "a.woocommerce-LoopProduct-link", "href");
                String description = safeText(getText(prodElement, "div.os-product-excerpt p"), MAX_LENGTH_DESCRIPTION);
                String photoUrl = safeText(getAttr(prodElement, "div.thunk-product-image img", "src"), MAX_LENGTH_PHOTO);

                Element priceEl = prodElement.selectFirst("span.price");
                double originalPrice = 0.0;
                double price = 0.0;
                if (priceEl != null) {
                    Element del = priceEl.selectFirst("del span.woocommerce-Price-amount bdi");
                    Element ins = priceEl.selectFirst("ins span.woocommerce-Price-amount bdi");
                    if (del != null) originalPrice = parsePrice(del.text());
                    if (ins != null) price = parsePrice(ins.text());
                    else price = originalPrice;
                }

                boolean promo = price < originalPrice;

                Product product = Product.builder()
                        .name(name)
                        .description(description)
                        .photoPath(photoUrl)
                        .category(category)
                        .originalPrice(originalPrice)
                        .price(price)
                        .promo(promo)
                        .stock(100)
                        .status("AVAILABLE")
                        .discount(promo ? (int) ((originalPrice - price) / originalPrice * 100) : 0)
                        .build();

                productRepository.save(product);

            } catch (Exception e) {
                System.out.println("Produit ignoré : " + e.getMessage());
            }
        }
    }


    // Méthode utilitaire pour parser le prix
    private double parsePrice(String text) {
        if (text == null) return 0.0;
        String clean = text.replaceAll("[^\\d,]", "").replace(",", ".");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Retourne le texte de l'élément ou une chaîne vide si null
    private String getText(Element element, String cssQuery) {
        Element el = element.selectFirst(cssQuery);
        return el != null ? el.text() : "";
    }

    // Retourne l'attribut de l'élément ou une chaîne vide si null
    private String getAttr(Element element, String cssQuery, String attr) {
        Element el = element.selectFirst(cssQuery);
        return el != null ? el.attr(attr) : "";
    }

    // Tronque une chaîne si elle dépasse la longueur max
    private String safeText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
