package com.shaikh.webStore;

import com.shaikh.webStore.auth.User;
import com.shaikh.webStore.auth.UserRepository;
import com.shaikh.webStore.model.Category;
import com.shaikh.webStore.repository.CategoryRepository;
import com.shaikh.webStore.service.ProductScraperService;
import com.shaikh.webStore.service.ScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
public class WebStoreApplication implements CommandLineRunner {

	private final CategoryRepository categoryRepository;
	private final ScraperService scraperService; // pour scraper les catégories
	private final ProductScraperService productScraperService; // pour scraper les produits
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	public static void main(String[] args) {
		SpringApplication.run(WebStoreApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {


		if (userRepository.count() == 0) {
			User admin = User.builder()
					.firstname("Default")
					.lastname("Admin")
					.email("admin@ardastore.com")
					.password(passwordEncoder.encode("GoNewArda@2026!")) // mot de passe par défaut
					.build();

			userRepository.save(admin);
			System.out.println("✅ Admin par défaut créé : email=admin@example.com / password=admin123");
		} else {
			System.out.println("ℹ️ Des utilisateurs existent déjà, aucun admin par défaut créé.");
		}
		try {
			String baseUrl = "https://elsheikh-store.com";
			System.out.println("🔄 Scraping des catégories et produits depuis " + baseUrl + " ...");

			// Scraper les catégories et les produits
			scraperService.scrapeAndSaveCategories(baseUrl);

			System.out.println("✅ Scraping terminé avec succès !");
		} catch (Exception e) {
			System.err.println("❌ Erreur lors du scraping : " + e.getMessage());
			e.printStackTrace();
		}
	}




}
