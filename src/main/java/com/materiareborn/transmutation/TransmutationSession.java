package com.materiareborn.transmutation;

import com.materiareborn.api.knowledge.KnowledgeSubject;
import com.materiareborn.api.transmutation.TransmutationQuery;
import com.materiareborn.api.transmutation.TransmutationViewState;

import java.util.Objects;

public final class TransmutationSession {
    private final KnowledgeSubject subject;
    private TransmutationQuery query;

    public TransmutationSession(KnowledgeSubject subject, TransmutationQuery query) {
        this.subject = Objects.requireNonNull(subject, "subject");
        this.query = Objects.requireNonNull(query, "query");
    }

    public KnowledgeSubject subject() {
        return subject;
    }

    public TransmutationQuery query() {
        return query;
    }

    public void updateQuery(TransmutationQuery query) {
        this.query = Objects.requireNonNull(query, "query");
    }

    public TransmutationViewState emptyView() {
        return TransmutationViewState.empty(query);
    }
}
