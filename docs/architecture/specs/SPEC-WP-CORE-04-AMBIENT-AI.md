# SPEC-WP-CORE-04 — Ambient AI & Multi-Modal Execution

> Technical specification for LangChain4j integration, Asynchronous Eventing, and Multi-Modal processing (WP-CORE-04 & WP-CORE-06).

---

## 1. Ambient Architecture

AI operations (LLM generation, vector embedding, vision processing) are notoriously slow and subject to rate limits. They MUST NEVER block an associate's HTTP thread. We use Spring Events to enforce asynchronous processing.

### 1.1 The Event Dispatcher

When an associate submits notes, the service publishes an event and immediately returns `HTTP 202 Accepted` or `HTTP 201 Created`.

```java
@Service
public class ClientEngagementService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public EngagementResponse createEngagement(UUID profileId, EngagementRequest request) {
        ClientEngagement engagement = // ... save to DB ...
        
        // Fire and Forget
        eventPublisher.publishEvent(new EngagementCreatedEvent(engagement.getId()));
        
        return toResponse(engagement);
    }
}
```

### 1.2 The Ambient Listener (LangChain4j)

```java
@Component
public class AmbientEngagementProcessor {

    @Autowired
    private ChatLanguageModel chatModel; // LangChain4j Interface
    
    @Autowired
    private EmbeddingModel embeddingModel; // LangChain4j Interface

    @Async("aiTaskExecutor") // Specific thread pool for long-running AI tasks
    @EventListener
    public void handleEngagement(EngagementCreatedEvent event) {
        // 1. Fetch Engagement
        // 2. Call chatModel to extract 'Taste', 'Sentiment', 'Urgency'
        // 3. Call embeddingModel to convert 'Taste' into a vector
        // 4. Update HyperProfile
    }
}
```

---

## 2. Multi-Modal Embeddings (Vision & Text)

The `curated_vault` is queried via `aesthetic_embedding`. This embedding space must seamlessly combine text and images.

### 2.1 The Universal Vector Space
We utilize a model capable of multi-modal embeddings (e.g., OpenAI's CLIP model or a compatible architecture via Azure). Both `Text` and `Images` map to the exact same 1536-dimensional space.

### 2.2 LangChain4j Interfaces

**Text Vectorization:**
```java
Response<Embedding> embedding = embeddingModel.embed("A dark, moody, architectural alligator skin bag.");
// Returns float[1536]
```

**Image Vectorization (Vision API Integration):**
Since standard LangChain4j `EmbeddingModel` is heavily text-biased, we define a custom wrapper:

```java
public interface MultiModalEmbeddingModel {
    float[] embedText(String text);
    float[] embedImage(byte[] imageBytes);
}
```

### 2.3 Semantic Search Execution (pgvector)

Once we have the `float[]` (whether from text or image), we query PostgreSQL using the `pgvector` nearest neighbor `<->` operator.

```java
@Repository
public interface CuratedVaultRepository extends JpaRepository<VaultItem, UUID> {

    @Query(value = "SELECT * FROM curated_vault " +
                   "WHERE is_deleted = false " +
                   "ORDER BY aesthetic_embedding <-> cast(:vector as vector) LIMIT :limit", 
           nativeQuery = true)
    List<VaultItem> findNearestNeighbors(@Param("vector") String vectorString, @Param("limit") int limit);
}
```
*(Note: `float[]` is serialized to a string like `"[0.123, -0.456, ...]"` before passing to the native query).*
