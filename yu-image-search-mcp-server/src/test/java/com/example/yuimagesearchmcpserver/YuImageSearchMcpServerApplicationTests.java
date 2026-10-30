package com.example.yuimagesearchmcpserver;

import com.example.yuimagesearchmcpserver.tools.ImageSearchTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class YuImageSearchMcpServerApplicationTests {
	@Resource
	private ImageSearchTool imageSearchTool;

	@Test
	void searchImage() {
		String result = imageSearchTool.searchImage("猫咪");
		Assertions.assertNotNull(result);
	}

}
