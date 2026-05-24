package com.Ishwarjit.Wolf_OVRN_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "banner_texts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannerText {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * The text content to display in the banner strip.
     * Example: "▌ DROP 002 — HOWL SEASON // LOADING"
     */
    @Column(name = "text", nullable = false)
    private String text;

    /**
     * When true, the frontend should render this item with the accent/highlight style
     * (e.g., className="util text-accent").
     */
    @Column(name = "is_highlight", nullable = false)
    private boolean isHighlight = false;

    /**
     * Controls display order. Lower numbers appear first.
     */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    /**
     * Soft toggle — inactive items are excluded from the public endpoint.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
