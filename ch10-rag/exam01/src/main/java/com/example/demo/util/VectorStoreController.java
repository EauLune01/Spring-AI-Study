package com.example.demo.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vectorstore")
public class VectorStoreController {
  private static final Logger LOGGER = LoggerFactory.getLogger(VectorStoreController.class);

  @Autowired
  private VectorStore vectorStore;

  @GetMapping("/method01")
  public void method01() {
    //Extract
    Resource resource = new ClassPathResource("static/documents/대한민국헌법(19880225).pdf");
    DocumentReader reader = new PagePdfDocumentReader(resource);
    List<Document> documents = reader.read();
    for(Document doc : documents) {
      Map<String, Object> metadata = doc.getMetadata();
      metadata.putAll(Map.of(
        "title", "대한민국헌법",
        "author", "법제처",
        "amendment", "9차"
      ));
    }

    //Transform
    DocumentTransformer transformer = new TokenTextSplitter(
      250, 200, 5, 10000, true);
    documents = transformer.apply(documents);
    LOGGER.info("documents: {}개", documents.size());
    
    //Load
    vectorStore.add(documents);
  }  

  @GetMapping("/method02")
  public void method02() {
    // Extract
    Resource resource = new ClassPathResource("static/documents/삼성화재자동차보험약관(20250101).pdf");
    DocumentReader reader = new PagePdfDocumentReader(resource);

    List<Document> documents = reader.read();
    for (Document doc : documents) {
      Map<String, Object> metadata = doc.getMetadata();
      metadata.putAll(Map.of(
          "title", "자동차보험",
          "author", "삼성화재",
          "date", "20250101"));
    }

    // Transform
    //DocumentTransformer transformer = new TokenTextSplitter(250, 200, 5, 10000, true);
    DocumentTransformer transformer = new TokenTextSplitter(100, 50, 5, 10000, true);
    documents = transformer.apply(documents);
    LOGGER.info("documents: {}개", documents.size());

    // Load
    vectorStore.add(documents);
  }

  @GetMapping("/method03")
  public void method03() {
    // Extract
    Resource resource = new ClassPathResource("static/documents/현대해상자동차보험약관(20250221).pdf");
    DocumentReader reader = new PagePdfDocumentReader(resource);

    List<Document> documents = reader.read();
    for (Document doc : documents) {
      Map<String, Object> metadata = doc.getMetadata();
      metadata.putAll(Map.of(
          "title", "자동차보험",
          "author", "현대해상",
          "date", "20250221"));
    }

    // Transform
    //DocumentTransformer transformer = new TokenTextSplitter(250, 200, 5, 10000, true);
    DocumentTransformer transformer = new TokenTextSplitter(100, 50, 5, 10000, true);
    documents = transformer.apply(documents);
    LOGGER.info("documents: {}개", documents.size());

    // Load
    vectorStore.add(documents);
  }  
}
