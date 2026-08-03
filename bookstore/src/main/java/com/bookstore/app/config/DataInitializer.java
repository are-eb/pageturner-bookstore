package com.bookstore.app.config;

import com.bookstore.app.model.Book;
import com.bookstore.app.model.Role;
import com.bookstore.app.model.User;
import com.bookstore.app.repository.BookRepository;
import com.bookstore.app.repository.UserRepository;
import com.bookstore.app.service.GoogleBooksService;
import com.bookstore.app.dto.GoogleBookResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Seeds a default admin account and a starter catalog the first time the app
 * runs against an empty database. Safe to run repeatedly.
 *
 * The starter catalog is a hardcoded list of real, well-known titles with stable
 * cover images from the Open Library covers service (a free, keyless, effectively
 * unlimited image endpoint). This is deliberately NOT dependent on the live Google
 * Books API: that API has a modest unauthenticated daily quota that can be shared
 * across a whole network and exhausted by anyone, and a bookstore with an empty
 * catalog on first run is a much worse experience than one seeded from a small
 * fixed list. Admins can still pull additional live titles any time from
 * Admin -> Import from Google, which is unaffected by this change.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleBooksService googleBooksService;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedBooks();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail("admin@bookstore.com")) {
            return;
        }
        User admin = new User();
        admin.setName("Store Admin");
        admin.setEmail("admin@bookstore.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded default admin -> email: admin@bookstore.com / password: admin123 (please change this!)");
    }

    private record SeedBook(String isbn, String title, String author, String category,
                             double price, boolean discounted, String description) {}

    private static final List<SeedBook> STARTER_CATALOG = List.of(
            new SeedBook("9780132350884", "Clean Code", "Robert C. Martin", "Programming", 34.99, true,
                    "A handbook of agile software craftsmanship, focused on writing code that is easy to read, understand, and maintain."),
            new SeedBook("9780135957059", "The Pragmatic Programmer", "David Thomas, Andrew Hunt", "Programming", 39.99, false,
                    "Classic, practical guidance for becoming a more effective and adaptable software developer."),
            new SeedBook("9780439708180", "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", 14.99, true,
                    "An orphaned boy discovers he is a wizard on his eleventh birthday and enters a hidden magical world."),
            new SeedBook("9780618260300", "The Hobbit", "J.R.R. Tolkien", "Fantasy", 12.99, false,
                    "A reluctant hobbit is swept into an epic quest to reclaim a mountain kingdom from a dragon."),
            new SeedBook("9780345339706", "The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", 15.99, false,
                    "The first volume of the epic tale of the One Ring and the fellowship formed to destroy it."),
            new SeedBook("9780385333849", "A Game of Thrones", "George R.R. Martin", "Fantasy", 18.99, true,
                    "Noble houses vie for control of the Iron Throne in a kingdom on the brink of war and winter."),
            new SeedBook("9780735211292", "Atomic Habits", "James Clear", "Self-Help", 16.99, true,
                    "A practical, evidence-based guide to building good habits and breaking bad ones through small changes."),
            new SeedBook("9780062316097", "Sapiens", "Yuval Noah Harari", "History", 22.99, false,
                    "A sweeping look at how Homo sapiens came to dominate the planet, from the Cognitive Revolution onward."),
            new SeedBook("9780553380163", "A Brief History of Time", "Stephen Hawking", "Science", 18.00, false,
                    "A landmark, accessible tour of cosmology, black holes, and the nature of time itself."),
            new SeedBook("9780451524935", "1984", "George Orwell", "Fiction", 13.99, true,
                    "A dystopian vision of a totalitarian future under constant surveillance and thought control."),
            new SeedBook("9780743273565", "The Great Gatsby", "F. Scott Fitzgerald", "Fiction", 11.99, false,
                    "A tragic story of wealth, obsession, and the American Dream in Jazz Age New York."),
            new SeedBook("9780141439518", "Pride and Prejudice", "Jane Austen", "Classics", 10.99, true,
                    "A sharp, witty story of manners, marriage, and misunderstanding in Georgian England."),
            new SeedBook("9780679783268", "Crime and Punishment", "Fyodor Dostoevsky", "Classics", 14.99, false,
                    "A poor former student grapples with guilt and morality after committing a terrible crime."),
            new SeedBook("9780679720201", "One Hundred Years of Solitude", "Gabriel García Márquez", "Classics", 16.99, false,
                    "The multi-generational saga of the Buendía family in the mythical town of Macondo."),
            new SeedBook("9780441013593", "Dune", "Frank Herbert", "Science Fiction", 19.99, true,
                    "A young heir navigates politics, prophecy, and survival on the deadly desert planet Arrakis."),
            new SeedBook("9780345391803", "The Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Science Fiction", 12.99, false,
                    "An unwitting Earthman is swept into a chaotic, comic tour of the galaxy moments before Earth's demolition."),
            new SeedBook("9780060850524", "Brave New World", "Aldous Huxley", "Science Fiction", 13.99, false,
                    "A engineered, pleasure-controlled future society is threatened by an outsider's disruptive presence."),
            new SeedBook("9780307887894", "Gone Girl", "Gillian Flynn", "Thriller", 15.99, true,
                    "A marriage unravels into a chilling game of deception after a woman vanishes on her anniversary."),
            new SeedBook("9780307474278", "The Kite Runner", "Khaled Hosseini", "Fiction", 15.99, false,
                    "A story of friendship, guilt, and redemption spanning decades between Afghanistan and America."),
            new SeedBook("9780062315007", "The Alchemist", "Paulo Coelho", "Fiction", 14.99, true,
                    "A shepherd travels from Spain to Egypt in search of treasure, and discovers his own destiny."),
            new SeedBook("9780316769488", "The Catcher in the Rye", "J.D. Salinger", "Fiction", 12.99, false,
                    "A restless teenager wanders New York City over a few days, wrestling with alienation and loss."),
            new SeedBook("9780156012195", "Life of Pi", "Yann Martel", "Fiction", 14.99, false,
                    "A young man survives a shipwreck and 227 days adrift with a Bengal tiger for company."),
            new SeedBook("9780679745587", "Beloved", "Toni Morrison", "Fiction", 15.99, true,
                    "A formerly enslaved woman is haunted by the past in this Pulitzer Prize-winning novel."),
            new SeedBook("9780670813029", "It", "Stephen King", "Horror", 21.99, false,
                    "A group of childhood friends reunite to confront an ancient evil that preys on their town's children."),
            new SeedBook("9780064400558", "Charlotte's Web", "E.B. White", "Children", 8.99, true,
                    "A barnyard pig is saved from slaughter by the clever words spun by his spider friend."),
            new SeedBook("9780142437247", "Meditations", "Marcus Aurelius", "Philosophy", 11.99, false,
                    "The private reflections of a Roman emperor on virtue, duty, and the discipline of the mind."),
            new SeedBook("9780743477109", "Hamlet", "William Shakespeare", "Drama", 9.99, false,
                    "A Danish prince is consumed by grief and revenge after his father's murder by his own uncle."),
            new SeedBook("9780199535569", "Emma", "Jane Austen", "Classics", 10.99, false,
                    "A well-meaning but meddlesome young woman's matchmaking schemes go comically awry."),
            new SeedBook("9780544003415", "The Lord of the Rings", "J.R.R. Tolkien", "Fantasy", 29.99, true,
                    "The complete epic journey to destroy the One Ring, collected in a single volume."),
            new SeedBook("9781400032716", "The Curious Incident of the Dog in the Night-Time", "Mark Haddon", "Fiction", 13.99, false,
                    "A boy with an extraordinary mind investigates the death of a neighborhood dog."),
            new SeedBook("9780061120084", "To Kill a Mockingbird", "Harper Lee", "Classics", 14.99, true,
                    "A young girl in the Depression-era South witnesses her father defend a Black man falsely accused of rape."),
            new SeedBook("9780393354597", "Educated", "Tara Westover", "Memoir", 17.99, false,
                    "A woman raised off-grid in rural Idaho fights her way to a doctorate at Cambridge.")
    );

    private void seedBooks() {
        if (bookRepository.count() > 0) {
            return;
        }

        int discountCount = 0;
        for (SeedBook s : STARTER_CATALOG) {
            Book book = new Book();
            book.setTitle(s.title());
            book.setAuthor(s.author());
            book.setDescription(s.description());
            book.setIsbn(s.isbn());
            book.setCategory(s.category());
            book.setLanguage("en");
            // Open Library's cover service is free, keyless, and effectively unlimited -
            // it just serves a static image per ISBN, so it never rate-limits like a search API would.
            book.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/" + s.isbn() + "-L.jpg");
            book.setRating(Math.round((3.6 + Math.random() * 1.3) * 10.0) / 10.0);

            BigDecimal price = BigDecimal.valueOf(s.price());
            book.setPrice(price);
            if (s.discounted()) {
                book.setOriginalPrice(price.multiply(BigDecimal.valueOf(1.25)).setScale(2, RoundingMode.HALF_UP));
                discountCount++;
            }
            book.setStockQuantity(10 + (int) (Math.random() * 15));
            bookRepository.save(book);
        }
        log.info("Seeded starter catalog with {} books ({} on sale) - no external API calls required.",
                STARTER_CATALOG.size(), discountCount);

        // Best-effort bonus: also try pulling a couple of live titles from Google Books,
        // purely as a nice-to-have. Never blocks or reduces the guaranteed catalog above.
        try {
            List<GoogleBookResult> bonus = googleBooksService.search("new release fiction", 3);
            for (GoogleBookResult g : bonus) {
                if (g.getTitle() == null || bookRepository.findByGoogleBooksId(g.getGoogleBooksId()).isPresent()) {
                    continue;
                }
                Book book = new Book();
                book.setGoogleBooksId(g.getGoogleBooksId());
                book.setTitle(g.getTitle());
                book.setAuthor(g.getAuthor());
                book.setDescription(g.getDescription());
                book.setIsbn(g.getIsbn());
                book.setPublisher(g.getPublisher());
                book.setPublishedDate(g.getPublishedDate());
                book.setCategory(g.getCategory() != null ? g.getCategory() : "General");
                book.setLanguage(g.getLanguage());
                book.setCoverImageUrl(g.getCoverImageUrl());
                book.setRating(g.getRating());
                book.setPrice(g.getSuggestedPrice() != null ? g.getSuggestedPrice() : BigDecimal.valueOf(12.99));
                book.setStockQuantity(15);
                bookRepository.save(book);
            }
        } catch (Exception ex) {
            log.info("Skipped live Google Books bonus fetch (non-fatal): {}", ex.getMessage());
        }
    }
}
