package com.aria.m0.page.dao;

import com.aria.m0.page.model.PageWorkflowBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageWorkflowBindingRepository extends JpaRepository<PageWorkflowBinding, Long> {

    List<PageWorkflowBinding> findByTenantIdAndPageCode(Long tenantId, String pageCode);

    List<PageWorkflowBinding> findByTenantIdAndWorkflowCodeAndStepCode(
            Long tenantId, String workflowCode, String stepCode);
}
