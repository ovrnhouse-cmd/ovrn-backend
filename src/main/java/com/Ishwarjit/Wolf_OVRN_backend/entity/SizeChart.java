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

/**
 * A global size chart image that can be reused across many products.
 * Products reference a size chart via a nullable FK (size_chart_id).
 */
@Entity
@Table(name = "size_charts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SizeChart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Human-readable display name shown in admin dropdowns (e.g. "Heavy Tee Size Guide"). */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Original filename of the uploaded image (e.g. "heavy-tee-size-guide.png"). */
    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    /** URL-friendly slug derived from image_name via SlugUtils (e.g. "heavy-tee-size-guide"). */
    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    /** Cloudinary secure URL (wolf-ovrn/size-charts folder). */
    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
