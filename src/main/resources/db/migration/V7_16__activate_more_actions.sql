-- =====================================================================
-- V7_16 — Mark additional Schema V2 actions implemented.
-- Round 1H seeded 30 action codes with implemented=false. Round 1G impl'd
-- 7 in code (save/delete/etc). This flips 14 more to true: navigation,
-- recall/delegate/escalate-style transitions, notify.*, expr.*, print.*.
-- Each is covered by an existing endpoint or client-side handler.
-- =====================================================================

UPDATE action_registry SET implemented = TRUE WHERE code IN (
  'discard',
  'duplicate',
  'notify.toast',
  'notify.email',
  'notify.sms',
  'page.refresh',
  'restore',
  'workflow.assign',
  'workflow.cancel',
  'workflow.start',
  'expr.compute',
  'expr.evaluate',
  'print',
  'print.template'
);
