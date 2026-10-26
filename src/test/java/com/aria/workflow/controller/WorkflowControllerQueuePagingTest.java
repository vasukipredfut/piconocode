package com.aria.workflow.controller;

import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.engine.WorkflowEngineService.QueuePage;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** queue-for-page keeps its plain-list body and reports the size of the whole queue in X-Total-Count. */
class WorkflowControllerQueuePagingTest {

    private final WorkflowEngineService engine = mock(WorkflowEngineService.class);
    private final WorkflowController controller = new WorkflowController();

    WorkflowControllerQueuePagingTest() {
        ReflectionTestUtils.setField(controller, "engine", engine);
    }

    @Test
    void thePageIsTheBody_theWholeQueueSizeIsInTheHeader() {
        when(engine.queuePageForPage("aria:billing-entry", 2, 100)).thenReturn(new QueuePage(List.of(), 250L));

        ResponseEntity<?> r = controller.queueForPage("aria:billing-entry", 2, 100);

        assertThat(r.getStatusCode().value()).isEqualTo(200);
        assertThat(r.getHeaders().getFirst("X-Total-Count")).isEqualTo("250");
        assertThat(r.getHeaders().getFirst("X-Page")).isEqualTo("2");
        assertThat(r.getHeaders().getFirst("X-Page-Size")).isEqualTo("100");
        assertThat(r.getHeaders().getFirst("Access-Control-Expose-Headers")).contains("X-Total-Count");
        assertThat((List<?>) r.getBody()).isEmpty();
    }

    @Test
    void aBigSize_isNotCapped_andIsReportedAsAsked() {
        when(engine.queuePageForPage("p", 0, 200)).thenReturn(new QueuePage(List.of(), 5L));

        ResponseEntity<?> r = controller.queueForPage("p", 0, 200);

        assertThat(r.getHeaders().getFirst("X-Page-Size")).isEqualTo("200");
    }

    @Test
    void aPageThatIsNotAWorkflowStep_isStill404() {
        when(engine.queuePageForPage("aria:inbox", 0, 25)).thenReturn(null);

        assertThat(controller.queueForPage("aria:inbox", 0, 25).getStatusCode().value()).isEqualTo(404);
    }
}
