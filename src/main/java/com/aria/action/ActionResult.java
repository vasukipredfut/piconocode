package com.aria.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Response envelope returned by every {@link ActionHandler}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionResult {
    private boolean ok;
    private String message;
    /** Identifier of any record the action created / updated. */
    private Object recordId;
    /** Frontend-honoured navigation/refresh directive. Schema mirrors
     *  Schema V2 §9: {navigate: "page.open"|"page.back", page: ..., params: {...}}
     *  or {clearForm: true} etc. */
    private Map<String, Object> nextPageState;
    /** Optional structured payload (e.g. workflow transition result). */
    private Map<String, Object> data;

    /** Convenience: build a successful empty result. */
    public static ActionResult ok() {
        return ActionResult.builder().ok(true).build();
    }
    /** Convenience: build a failed result with a message. */
    public static ActionResult error(String message) {
        return ActionResult.builder().ok(false).message(message).build();
    }
}
