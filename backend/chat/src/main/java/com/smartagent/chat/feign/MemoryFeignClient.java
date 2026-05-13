package com.smartagent.chat.feign;

import com.smartagent.common.model.ApiResponse;
import com.smartagent.common.model.MemoryContextRequest;
import com.smartagent.common.model.MemorySummaryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "smart-agent-memory", url = "${chat.memory.service-url:http://localhost:8086}")
public interface MemoryFeignClient {

    @PostMapping("/api/memory/context/build")
    ApiResponse<String> buildContext(@RequestBody MemoryContextRequest request);

    @PostMapping("/api/memory/summary")
    ApiResponse<Void> processSummary(@RequestBody MemorySummaryRequest request);
}
