package com.example.yuimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度图片搜索工具。
 *
 * <p>通过接口盒子的图片搜索 API 按关键词查询图片，并将结果暴露为 Spring AI 工具。</p>
 */
@Service
public class ImageSearchTool {

    /** 接口盒子用户中心的数字 ID，请替换为你自己的值。 */
    private static final String API_USER_ID = "10022193";

    /** 接口盒子用户中心的通讯密钥，请替换为你自己的值。 */
    private static final String API_KEY = "e983f00a6864e5a54d371a93948c6a9b";

    /** 图片搜索接口地址。 */
    private static final String API_URL = "https://cn.apihz.cn/api/img/apihzimgbaidu.php";

    /** 默认查询第一页。 */
    private static final int DEFAULT_PAGE = 1;

    /** 每次默认返回 5 张图片。 */
    private static final int DEFAULT_LIMIT = 5;

    /** type=1 表示返回较稳定的百度预览图。 */
    private static final int PREVIEW_IMAGE_TYPE = 1;

    /** HTTP 请求超时时间：10 秒。 */
    private static final int REQUEST_TIMEOUT_MILLIS = 10_000;

    private final String apiUrl;
    private final String apiUserId;
    private final String apiKey;

    /**
     * Spring 创建 Bean 时使用的默认构造器，加载上方写死的接口配置。
     */
    public ImageSearchTool() {
        this(API_URL, API_USER_ID, API_KEY);
    }

    /**
     * 允许测试时传入本地模拟接口，避免测试直接访问真实服务。
     */
    ImageSearchTool(String apiUrl, String apiUserId, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiUserId = apiUserId;
        this.apiKey = apiKey;
    }

    /**
     * 提供给大模型调用的 MCP 工具入口。
     *
     * @param query 图片搜索关键词，最多 10 个字符
     * @return 以英文逗号分隔的图片地址；调用失败时返回错误信息
     */
    @Tool(description = "Search images from Baidu by keyword")
    public String searchImage(@ToolParam(description = "Search keyword, no more than 10 characters") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error searching images: " + e.getMessage();
        }
    }

    /**
     * 调用图片搜索接口并返回图片地址列表。
     *
     * @param query 图片搜索关键词
     * @return 有效的图片地址列表；接口未返回 res 时返回空列表
     */
    public List<String> searchMediumImages(String query) {
        // 在真正发送请求之前检查凭证和关键词，尽早给出清晰的错误信息。
        validateConfiguration();
        if (StrUtil.isBlank(query)) {
            throw new IllegalArgumentException("Search query must not be blank");
        }
        String keyword = query.trim();
        if (keyword.codePointCount(0, keyword.length()) > 10) {
            throw new IllegalArgumentException("Search query must not exceed 10 characters");
        }

        // 按接口文档组装 GET 查询参数。
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("id", apiUserId);
        params.put("key", apiKey);
        params.put("words", keyword);
        params.put("page", DEFAULT_PAGE);
        params.put("limit", DEFAULT_LIMIT);
        params.put("type", PREVIEW_IMAGE_TYPE);

        // HttpResponse 实现了 AutoCloseable，使用 try-with-resources 自动释放连接资源。
        try (HttpResponse httpResponse = HttpUtil.createGet(apiUrl)
                .form(params)
                .timeout(REQUEST_TIMEOUT_MILLIS)
                .execute()) {
            // 先检查 HTTP 状态，再解析业务 JSON，避免把错误页面当成 JSON 处理。
            if (!httpResponse.isOk()) {
                throw new IllegalStateException(
                        "Image search request failed with HTTP status " + httpResponse.getStatus()
                );
            }

            // code=200 表示接口业务调用成功，msg 保存失败原因。
            JSONObject response = JSONUtil.parseObj(httpResponse.body());
            if (response.getInt("code", -1) != 200) {
                throw new IllegalStateException(
                        "Image search API error: " + StrUtil.blankToDefault(response.getStr("msg"), "Unknown error")
                );
            }

            // res 是接口返回的图片 URL 数组，同时过滤空地址。
            JSONArray imageUrls = response.getJSONArray("res");
            if (imageUrls == null) {
                return List.of();
            }

            return imageUrls.stream()
                    .map(String::valueOf)
                    .filter(StrUtil::isNotBlank)
                    .toList();
        }
    }

    /**
     * 防止忘记替换示例凭证时仍然向远程接口发送无效请求。
     */
    private void validateConfiguration() {
        if ("你的用户ID".equals(apiUserId) || "你的通讯密钥".equals(apiKey)) {
            throw new IllegalStateException("Please replace API_USER_ID and API_KEY with your API credentials");
        }
    }
}
