package com.example.spring_server.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.spring_server.entity.ExperimentLog;
import com.example.spring_server.repository.ExperimentLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExperimentUpdateIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ExperimentLogRepository repository;

    private ExperimentLog seedActive() {
        return repository.save(ExperimentLog.builder()
                .algorithm("MLP")
                .optimizer("adam")
                .epochs(50)
                .batchSize(16)
                .learningRate(0.01)
                .accuracy(0.95)
                .loss(0.1)
                .memo("초기메모")
                .tag("v1")
                .deleted(false)
                .build());
    }

    @Test
    void editForm_loads_with_existingValues() throws Exception {
        ExperimentLog seed = seedActive();

        mvc.perform(get("/experiments/" + seed.getId() + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MLP")))
                .andExpect(content().string(containsString("초기메모")))
                .andExpect(content().string(containsString("v1")));
    }

    @Test
    void update_changes_memoAndTagOnly_and_redirectsToSingleView() throws Exception {
        ExperimentLog seed = seedActive();
        Long id = seed.getId();

        mvc.perform(post("/experiments/" + id)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("memo", "수정된메모")
                        .param("tag", "v2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/experiments/" + id));

        ExperimentLog after = repository.findById(id).orElseThrow();
        // memo/tag 만 바뀌고
        assertEquals("수정된메모", after.getMemo());
        assertEquals("v2", after.getTag());
        // ML/하이퍼파라미터/메타는 그대로
        assertEquals("MLP", after.getAlgorithm());
        assertEquals("adam", after.getOptimizer());
        assertEquals(50, after.getEpochs());
        assertEquals(16, after.getBatchSize());
        assertEquals(0.01, after.getLearningRate());
        assertEquals(0.95, after.getAccuracy());
        assertEquals(0.1, after.getLoss());
        assertEquals(false, after.isDeleted());
        assertNotNull(after.getCreatedAt());
    }

    @Test
    void editForm_softDeleted_returns404() throws Exception {
        ExperimentLog seed = repository.save(ExperimentLog.builder()
                .algorithm("MLP")
                .deleted(true)
                .build());

        mvc.perform(get("/experiments/" + seed.getId() + "/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_softDeleted_returns404() throws Exception {
        ExperimentLog seed = repository.save(ExperimentLog.builder()
                .algorithm("MLP")
                .memo("원본")
                .deleted(true)
                .build());

        mvc.perform(post("/experiments/" + seed.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("memo", "해킹")
                        .param("tag", "x"))
                .andExpect(status().isNotFound());

        ExperimentLog after = repository.findById(seed.getId()).orElseThrow();
        assertEquals("원본", after.getMemo());
    }

    @Test
    void editForm_nonexistent_returns404() throws Exception {
        mvc.perform(get("/experiments/999999/edit"))
                .andExpect(status().isNotFound());
    }
}
