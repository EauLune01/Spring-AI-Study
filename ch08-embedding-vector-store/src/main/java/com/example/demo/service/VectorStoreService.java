package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final VectorStore vectorStore;

    public void addDocument() {
        // Document 목록 생성
        // 메타데이터를 Map으로 전달
        List<Document> documents = List.of(
                new Document("대통령 선거는 5년마다 있습니다.", Map.of("source", "헌법", "year", 1987)),
                new Document("대통령 임기는 4년입니다.", Map.of("source", "헌법", "year", 1980)),
                new Document("국회의원은 법률안을 심의·의결합니다.", Map.of("source", "헌법", "year", 1987)),
                new Document("자동차를 사용하려면 등록을 해야합니다.", Map.of("source", "자동차관리법")),
                new Document("대통령은 행정부의 수반입니다.", Map.of("source", "헌법", "year", 1987)),
                new Document("국회의원은 4년마다 투표로 뽑습니다.", Map.of("source", "헌법", "year", 1987)),
                new Document("승용차는 정규적인 점검이 필요합니다.", Map.of("source", "자동차관리법"))
        );

        // 벡터 저장소에 저장
        vectorStore.add(documents);
    }

    public List<Document> search(String query, int topK, double threshold) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query) //텍스트
                        .topK(topK) //상위 K개 지정
                        .similarityThreshold(threshold) //threshold보다 높은 유사도 문서들만 검색됨
                        .build()
        );
    }

    public List<Document> searchWithFilter(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(1)
                        .similarityThreshold(0.4)
                        .filterExpression("source == '헌법' && year >= 1987") //SQL의 where절과 유사
                        .build()
        );
    }

    public void deleteByFilter(String filter) {
        vectorStore.delete(filter);
        log.info("삭제 완료 - filter: {}", filter);
    }
}
