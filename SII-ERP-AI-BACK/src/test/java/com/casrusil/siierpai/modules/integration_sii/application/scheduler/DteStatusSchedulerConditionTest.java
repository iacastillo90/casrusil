package com.casrusil.siierpai.modules.integration_sii.application.scheduler;

import com.casrusil.siierpai.modules.integration_sii.application.service.DteStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DteStatusSchedulerConditionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(DteStatusService.class, () -> mock(DteStatusService.class))
            .withUserConfiguration(DteStatusScheduler.class);

    @Test
    void jobShouldBeEnabledByDefault() {
        runner.run(context -> assertThat(context).hasSingleBean(DteStatusScheduler.class));
    }

    @Test
    void jobShouldBeDisabledByProperty() {
        runner.withPropertyValues("sii.status.job.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DteStatusScheduler.class));
    }
}
