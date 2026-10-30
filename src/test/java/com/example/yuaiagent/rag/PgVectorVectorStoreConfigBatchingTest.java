package com.example.yuaiagent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PgVectorVectorStoreConfigBatchingTest {

    @Test
    void addsDocumentsInBatchesOfAtMostTenWithoutLosingOrder() {
        RecordingVectorStore vectorStore = new RecordingVectorStore();
        List<Document> documents = IntStream.range(0, 23)
                .mapToObj(index -> new Document("document-" + index))
                .toList();

        PgVectorVectorStoreConfig.addDocumentsInBatches(vectorStore, documents);

        assertEquals(List.of(10, 10, 3), vectorStore.batches.stream().map(List::size).toList());
        assertEquals(documents, vectorStore.batches.stream().flatMap(List::stream).toList());
    }

    private static final class RecordingVectorStore implements VectorStore {
        private final List<List<Document>> batches = new ArrayList<>();

        @Override
        public void add(List<Document> documents) {
            batches.add(List.copyOf(documents));
        }

        @Override
        public void delete(List<String> idList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            throw new UnsupportedOperationException();
        }
    }
}
