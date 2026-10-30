package com.example.yuaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 *恋爱大师应用文档加载器
 * */
@Component
@Slf4j
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
//        加载多篇md文档
        try {

            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
//            循环加载多个md
            for (Resource resource : resources) {
//                取文件名
                String fileName = resource.getFilename();
//                只从“xxx - 状态篇.md”格式的文件名中提取状态
                String status = null;
                int separatorIndex = fileName.lastIndexOf(" - ");
                if (separatorIndex >= 0 && fileName.endsWith("篇.md")) {
                    int statusStartIndex = separatorIndex + " - ".length();
                    int statusEndIndex = fileName.length() - "篇.md".length();
                    if (statusStartIndex < statusEndIndex) {
                        status = fileName.substring(statusStartIndex, statusEndIndex);
                    }
                }
//                如何读取文件
//                遇到 Markdown 水平分隔线时，可以拆分成不同的 Document；
//                不读取代码块；
//                不读取引用块；
//                给文档加上原始文件名元数据。
                var configBuilder = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
//                        添加元信息“文件名”
                        .withAdditionalMetadata("filename", fileName);

//                只有成功提取到状态时，才添加 status 元数据
                if (status != null) {
                    configBuilder.withAdditionalMetadata("status", status);
                }

                MarkdownDocumentReaderConfig config = configBuilder.build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }
}
