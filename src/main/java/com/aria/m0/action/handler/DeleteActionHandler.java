package com.aria.m0.action.handler;

import com.aria.m0.action.ActionHandler;
import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** {@code delete} — soft-delete (sets {@code status='DELETED'}) when the
 *  entity has a status column; falls back to hard delete otherwise. */
@Component
public class DeleteActionHandler implements ActionHandler {

    @Autowired private EntityWriter writer;

    @Override public String code() { return "delete"; }

    @Override public ActionResult handle(ActionInvocation inv) {
        if (inv.getEntity() == null) throw new IllegalArgumentException("entity required");
        if (inv.getRecordId() == null) throw new IllegalArgumentException("recordId required");
        writer.softDelete(inv.getEntity(), inv.getRecordId());
        return ActionResult.builder().ok(true).recordId(inv.getRecordId()).message("deleted").build();
    }
}
