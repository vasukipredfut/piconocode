-- FlowVariant master: lookup for the M10.* flow_variant_code strings used
-- free-text today on GatePass / DispatchVehicleDetail (visible_when/required_when
-- rules reference them, e.g. #form.flow_variant_code == 'M10.TankerOutbound').

CREATE TABLE `m10_flow_variant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `display_name` varchar(128) NOT NULL,
  `module` varchar(16) NOT NULL DEFAULT 'M10',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_flow_variant_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

INSERT INTO `m10_flow_variant` (`code`, `display_name`) VALUES
('M10.TankerInbound', 'Tanker Inbound'),
('M10.TankerOutbound', 'Tanker Outbound'),
('M10.PackLoading', 'Pack Loading'),
('M10.PackUnload', 'Pack Unload'),
('M10.FGLoading', 'FG Loading'),
('M10.ReturnStockIn', 'Return Stock In'),
('M10.IntTransferIn', 'Internal Transfer In'),
('M10.IntTransferOut', 'Internal Transfer Out'),
('M10.ThirdPartyIn', 'Third Party In'),
('M10.ThirdPartyOut', 'Third Party Out'),
('M10.EwayBill', 'E-Way Bill');

INSERT INTO `entity_registry`
  (`tenant_id`, `code`, `display_name`, `table_name`, `description`, `category`,
   `read_role`, `write_role`, `value_column`, `display_column`, `lifecycle_kind`)
VALUES
  (-1, 'FlowVariant', 'Flow Variant', 'm10_flow_variant',
   'Lookup for M10 dispatch/gate-pass flow_variant_code values.', 'M10',
   '*', 'TenantAdmin', 'code', 'display_name', 'DEFINED');

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
VALUES
  (-1, 'FlowVariant', 'id', 'ID', 'LONG', 0, 1, 0, 1, 'number'),
  (-1, 'FlowVariant', 'code', 'Code', 'STRING', 1, 1, 1, 1, 'text'),
  (-1, 'FlowVariant', 'display_name', 'Display Name', 'STRING', 1, 1, 1, 1, 'text'),
  (-1, 'FlowVariant', 'module', 'Module', 'STRING', 1, 1, 0, 1, 'text'),
  (-1, 'FlowVariant', 'active', 'Active', 'BOOLEAN', 1, 1, 0, 0, 'checkbox');
