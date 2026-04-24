package com.luxury.concierge.service;

import com.luxury.concierge.model.ConciergeAlert;
import com.luxury.concierge.repository.ConciergeAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Proactive gifting alert service that scans for upcoming significant dates.
 *
 * <p>This service implements the headless Spring Batch-style daily job
 * described in the spec. It scans HyperProfiles for significant dates
 * occurring within the next 21 days and generates concierge alerts
 * with AI-suggested vault items.</p>
 *
 * <h3>Execution Flow (per spec §2.2):</h3>
 * <ol>
 *   <li>Query hyper_profiles for significant dates +21 days</li>
 *   <li>For each profile, fetch taste_vector</li>
 *   <li>Call Semantic Search Service (curated_vault) for top 3 matches</li>
 *   <li>Generate a concierge_alert record</li>
 * </ol>
 *
 * <p><strong>Note:</strong> In local development, the semantic search and
 * taste_vector lookups are stubbed. Full LangChain4j integration requires
 * HyperProfile and CuratedVault entities from WP-HUB-01 and WP-HUB-02.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProactiveGiftingJobService {

    private final ConciergeAlertRepository alertRepository;

    /**
     * Daily scheduled job that scans for upcoming significant dates.
     *
     * <p>Runs at 06:00 UTC daily. In production, this would:
     * <ul>
     *   <li>Query significant_dates table for dates within T+21 days</li>
     *   <li>Fetch taste_vector from hyper_profiles</li>
     *   <li>Perform semantic search against curated_vault using pgvector</li>
     *   <li>Generate personalized alert with top 3 item suggestions</li>
     * </ul>
     *
     * Currently uses stub data for local development.</p>
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void scanAndGenerateAlerts() {
        log.info("Proactive Gifting Job — Starting daily scan for significant dates (T+21 days)");

        // ──────────────────────────────────────────────────────────────────────
        // STUB: In production, this would execute:
        //   SELECT hp.id, hp.taste_vector, sd.date_type, sd.date_value
        //   FROM hyper_profiles hp
        //   JOIN significant_dates sd ON sd.profile_id = hp.id
        //   WHERE sd.date_value BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '21 days'
        //
        // For each result:
        //   SELECT id FROM curated_vault
        //   ORDER BY aesthetic_embedding <-> :taste_vector LIMIT 3
        // ──────────────────────────────────────────────────────────────────────

        log.info("Proactive Gifting Job — Scan complete. Stub mode active; no alerts generated.");
    }

    /**
     * Generates a concierge alert for a specific profile and associate.
     *
     * <p>This method is called by the batch job for each profile with an
     * upcoming significant date. It creates the alert record with
     * AI-suggested vault items.</p>
     *
     * @param profileId     the client's HyperProfile UUID
     * @param associateId   the assigned sales associate UUID
     * @param alertType     type of alert (e.g., "Upcoming_Anniversary")
     * @param message       personalized alert message
     * @param suggestedItems list of vault item UUIDs from semantic search
     * @return the created ConciergeAlert
     */
    @Transactional
    public ConciergeAlert generateAlert(UUID profileId, UUID associateId,
                                        String alertType, String message,
                                        List<String> suggestedItems) {
        ConciergeAlert alert = ConciergeAlert.builder()
                .profileId(profileId)
                .associateId(associateId)
                .alertType(alertType)
                .message(message)
                .suggestedItems(suggestedItems)
                .isRead(false)
                .build();

        ConciergeAlert saved = alertRepository.save(alert);
        log.info("Generated concierge alert '{}' — type='{}' for profile '{}', associate '{}'",
                saved.getId(), alertType, profileId, associateId);

        return saved;
    }
}
