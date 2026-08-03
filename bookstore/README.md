# Pageturner — Online Bookstore Management System

A full-stack bookstore web app built with **Spring Boot 3**, **Spring Security**, **Spring Data JPA**,
**Thymeleaf**, and **Tailwind CSS**, integrated with the free **Google Books API** for its catalog.

## Features

- 🔎 **Google Books integration** — admins can search the live Google Books catalog and import
  titles (cover, author, description, ISBN, publisher...) into the store with one click.
- 🛒 **Shopping cart & checkout** — add to cart, update quantities, place orders, view order history.
- 🔐 **Authentication & roles** — registration/login via Spring Security, `USER` and `ADMIN` roles.
- 🛠️ **Admin dashboard** — sales stats, manage books (CRUD), manage orders (status updates),
  manage users (enable/disable).
- 💅 **Professional, responsive UI** — Tailwind CSS, no page-jank, mobile-friendly.
- 💾 **Zero-setup database** — uses an embedded, file-based H2 database. No install required, and
  data persists between restarts in the `./data` folder. Swap in MySQL/Postgres any time (see below).

## Tech Stack

| Layer      | Technology                                   |
|------------|-----------------------------------------------|
| Backend    | Java 17, Spring Boot 3.3, Spring MVC, Spring Data JPA, Spring Security |
| Frontend   | Thymeleaf (server-rendered), Tailwind CSS (CDN) |
| Database   | H2 (file-based, embedded) — free, no setup     |
| External API | Google Books API (free tier, no key required for basic use) |
| Build tool | Maven                                          |

Everything used is **free** — no paid services, no API keys required to get started.

## Prerequisites

- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`) — or use an IDE like IntelliJ IDEA / VS Code with the Java extension
  pack, which can run Maven projects without a separate install.
- Internet connection (to download Maven dependencies once, and to talk to the Google Books API)

## Running the app

```bash
cd bookstore
mvn spring-boot:run
```

The app starts on **http://localhost:8080**.

On first run, `DataInitializer` automatically:
1. Creates a default admin account: **admin@bookstore.com / admin123** (change this immediately in production).
2. Seeds a small starter catalog (~8 books) by querying the Google Books API live.

If you don't have internet access at startup, the seeding step is skipped gracefully — you can
still register a user and the admin can import books manually once the app can reach the internet.

## Using the app

- Visit `/` to browse the store, or `/books` to search/filter the full catalog.
- Register a normal account at `/register`, or log in as admin to reach the dashboard.
- **Admin → Import from Google** (`/admin/import`): search Google Books and click "Add to Catalog"
  to import any title, with editable starting price/stock.
- **Admin → Manage Books**: edit price/stock/description or delete titles.
- **Admin → Orders**: move orders through Pending → Confirmed → Shipped → Delivered (or Cancelled).
- **Admin → Users**: enable/disable customer accounts.
- H2 console (dev only): `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:file:./data/bookstore`, user `sa`, no password.

## Configuration

All settings live in `src/main/resources/application.properties`:

```properties
# Optional: get a free key at https://console.cloud.google.com/apis/library/books.googleapis.com
# for a higher Google Books API rate limit. Leave blank to use the public, unauthenticated tier.
google.books.api.key=
```

## Switching to MySQL/PostgreSQL (optional, for production)

Replace the H2 dependency usage in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore
spring.datasource.username=youruser
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

...and add the corresponding driver dependency to `pom.xml` (e.g. `mysql-connector-j`).

## Production notes

- **Tailwind CDN**: this project uses the Tailwind Play CDN (`cdn.tailwindcss.com`) for zero-build
  simplicity. It's great for development/demo. For a production deployment, install Tailwind via
  npm and run its CLI build step to generate a purged, minified stylesheet — this removes the
  runtime JIT compiler and shaves real load time.
- **Change the default admin password** immediately after first login.
- **Set a real `google.books.api.key`** if you expect meaningful traffic, to avoid rate limiting.
- Consider externalizing secrets (DB credentials, API keys) via environment variables instead of
  committing them to `application.properties`.

## Project structure

```
src/main/java/com/bookstore/app/
├── config/       # Security config, data seeding, global error handling
├── controller/   # MVC controllers (storefront + admin)
├── dto/          # Google Books API response mapping
├── model/        # JPA entities (User, Book, CartItem, Order, OrderItem)
├── repository/   # Spring Data JPA repositories
└── service/      # Business logic (cart, orders, books, Google Books client, auth)
src/main/resources/
├── templates/    # Thymeleaf views (storefront + admin/)
└── application.properties
```
