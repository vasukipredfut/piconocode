package com.aria.m0.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Request envelope for {@code POST /api/m0/actions/{code}/invoke}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionInvocation {
    /** Canonical entity code, when the action operates on an entity. */
    private String entity;
    /** Record id, when the action targets a specific row. */
    private Long recordId;
    /** Form payload (for save / save.and.close / save.and.new). */
    private Map<String, Object> formData;
    /** Row ids selected in a list view (for bulk + export actions). */
    private List<Long> listSelection;
    /** Free-form context — page slug, workflow instance, etc. */
    private Map<String, Object> context;
    /** For workflow.transition: target workflow instance id. */
    private Long instanceId;
    /** For workflow.transition: the page submitting the form. */
    private String pageCode;
    /** For page.open: destination page slug. */
    private String page;
    /** For page.open: nav params. */
    private Map<String, Object> params;
    /** For export.csv: the DataSourceSpec to export. */
    private com.aria.m0.datasource.DataSourceSpec spec;
    /** Generic action args (Schema V2 row-action / header-action `args` block). */
    private Map<String, Object> args;
}
