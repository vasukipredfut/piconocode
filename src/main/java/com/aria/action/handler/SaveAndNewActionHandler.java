package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/** {@code save.and.new} — save and signal the form to reset for a new entry. */
@Component
public class SaveAndNewActionHandler implements ActionHandler {

    @Autowired private EntityWriter writer;

    @Override public String code() { return "save.and.new"; }

    @Override public ActionResult handle(ActionInvocation inv) {
        if (inv.getEntity() == null) throw new IllegalArgumentException("entity required");
        Long id = writer.upsert(inv.getEntity(), inv.getRecordId(), inv.getFormData());
        return ActionResult.builder()
                .ok(true).recordId(id).message("saved")
                .nextPageState(Map.of("clearForm", true))
                .build();
    }
}
