package com.example.scrumio.cache;

import com.example.scrumio.web.dto.TicketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicketCacheIndex {

    private static final Logger LOG = LoggerFactory.getLogger(TicketCacheIndex.class);

    private final Map<TicketCacheKey, Page<TicketResponse>> index = new ConcurrentHashMap<>();

    public Optional<Page<TicketResponse>> get(TicketCacheKey key) {
        return Optional.ofNullable(index.get(key));
    }

    public void put(TicketCacheKey key, Page<TicketResponse> page) {
        index.put(key, page);
    }

    public void invalidateByProjectId(UUID projectId) {
        int removed = 0;
        var it = index.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().projectId().equals(projectId)) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            LOG.info("[CACHE INVALIDATE] project={} removed {} entr{}", projectId, removed, removed == 1 ? "y" : "ies");
        }
    }
}
