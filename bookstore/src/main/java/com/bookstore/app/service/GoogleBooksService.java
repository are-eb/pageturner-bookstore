package com.bookstore.app.service;

import com.bookstore.app.dto.GoogleBookResult;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around the free Google Books API (https://developers.google.com/books).
 * No API key is required for basic search; an optional key can be set in
 * application.properties (google.books.api.key) for a higher request quota.
 */
@Service
@Slf4j
public class GoogleBooksService {

    private final RestTemplate restTemplate;

    @Value("${google.books.api.base-url}")
    private String baseUrl;

    @Value("${google.books.api.key:}")
    private String apiKey;

    public GoogleBooksService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** Thrown (with a human-readable message) whenever the Google Books API call itself fails. */
    public static class GoogleBooksApiException extends RuntimeException {
        public GoogleBooksApiException(String message) {
            super(message);
        }
    }

    public List<GoogleBookResult> search(String query, int maxResults) {
        List<GoogleBookResult> results = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return results;
        }
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .queryParam("q", query)
                    .queryParam("maxResults", Math.min(Math.max(maxResults, 1), 40));
            if (apiKey != null && !apiKey.isBlank()) {
                builder.queryParam("key", apiKey);
            }

            JsonNode root = restTemplate.getForObject(builder.toUriString(), JsonNode.class);
            if (root == null || !root.has("items")) {
                return results;
            }
            for (JsonNode item : root.get("items")) {
                results.add(mapToResult(item));
            }
            return results;
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("Google Books API rate limit hit for query '{}'", query);
            throw new GoogleBooksApiException(
                    "Google Books' free API quota has been used up for now (shared across everyone on this network). " +
                    "Try again later, or add your own free API key in application.properties (google.books.api.key) for a private quota.");
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.warn("Google Books API error for query '{}': {}", query, ex.getMessage());
            throw new GoogleBooksApiException("Google Books API returned an error: " + ex.getStatusCode() + ". Please try again.");
        } catch (ResourceAccessException ex) {
            log.warn("Could not reach Google Books API for query '{}': {}", query, ex.getMessage());
            throw new GoogleBooksApiException("Could not reach the Google Books API. Check your internet connection and try again.");
        } catch (Exception ex) {
            log.warn("Unexpected error calling Google Books API for query '{}': {}", query, ex.getMessage());
            throw new GoogleBooksApiException("Something went wrong talking to Google Books. Please try again.");
        }
    }

    public GoogleBookResult findByVolumeId(String volumeId) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/" + volumeId);
            if (apiKey != null && !apiKey.isBlank()) {
                builder.queryParam("key", apiKey);
            }
            JsonNode item = restTemplate.getForObject(builder.toUriString(), JsonNode.class);
            if (item == null) return null;
            return mapToResult(item);
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("Google Books API rate limit hit fetching volume '{}'", volumeId);
            throw new GoogleBooksApiException(
                    "Google Books' free API quota has been used up for now. Try again later, or add your own API key.");
        } catch (Exception ex) {
            log.warn("Google Books API lookup failed for id '{}': {}", volumeId, ex.getMessage());
            return null;
        }
    }

    private GoogleBookResult mapToResult(JsonNode item) {
        GoogleBookResult r = new GoogleBookResult();
        r.setGoogleBooksId(text(item, "id"));

        JsonNode info = item.get("volumeInfo");
        if (info != null) {
            r.setTitle(text(info, "title"));

            if (info.has("authors") && info.get("authors").isArray()) {
                List<String> authors = new ArrayList<>();
                info.get("authors").forEach(a -> authors.add(a.asText()));
                r.setAuthor(String.join(", ", authors));
            }

            r.setDescription(text(info, "description"));
            r.setPublisher(text(info, "publisher"));
            r.setPublishedDate(text(info, "publishedDate"));
            r.setLanguage(text(info, "language"));

            if (info.has("categories") && info.get("categories").isArray() && info.get("categories").size() > 0) {
                r.setCategory(info.get("categories").get(0).asText());
            }

            if (info.has("averageRating")) {
                r.setRating(info.get("averageRating").asDouble());
            }

            if (info.has("industryIdentifiers")) {
                for (JsonNode idNode : info.get("industryIdentifiers")) {
                    String type = text(idNode, "type");
                    if ("ISBN_13".equals(type) || (r.getIsbn() == null && "ISBN_10".equals(type))) {
                        r.setIsbn(text(idNode, "identifier"));
                    }
                }
            }

            JsonNode imageLinks = info.get("imageLinks");
            if (imageLinks != null) {
                String cover = text(imageLinks, "thumbnail");
                if (cover == null) cover = text(imageLinks, "smallThumbnail");
                if (cover != null) {
                    cover = cover.replace("http://", "https://");
                }
                r.setCoverImageUrl(cover);
            }
        }

        // Google's API rarely exposes real retail pricing for free-tier search,
        // so we derive a sensible default that admins can adjust before publishing.
        BigDecimal suggested = BigDecimal.valueOf(9.99);
        JsonNode saleInfo = item.get("saleInfo");
        if (saleInfo != null && saleInfo.has("retailPrice") && saleInfo.get("retailPrice").has("amount")) {
            suggested = BigDecimal.valueOf(saleInfo.get("retailPrice").get("amount").asDouble());
        }
        r.setSuggestedPrice(suggested);

        return r;
    }

    private String text(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
}
