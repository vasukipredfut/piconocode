-- ═══════════════════════════════════════════════════════════════════
-- V1 — Aria baseline schema.
--
-- Generated from the live schema AFTER the aria: rename and the -1 re-scope,
-- so it carries no customer naming and no tenant data.
--
-- Replaces 134 incremental migrations. Those were not history in any useful
-- sense: Flyway was dormant (spring.flyway.enabled=false, no history table), so
-- nothing tracked what had been applied. A 1,169-line probe-based initialiser
-- guessed instead — "does page X exist? no -> re-run V-n" — which is why pages
-- accumulated duplicate PUBLISHED rows, why six gate-pass workflows silently
-- wrote no rows for months, and why a rename would resurrect itself on the next
-- boot. A ledger removes the need to guess.
--
-- Three portability pins were stripped from the dumped views, each of which
-- would have broken every fresh install and only surfaced by building one:
--   * DEFINER=`earms_app`@`localhost`  -> CURRENT_USER  (that user won't exist)
--   * `earms`.`table`                  -> `table`       (pins to a DB named earms)
--   * tenant_id = 1                    -> IN (-1, 1)    (reported on one customer forever)
-- ═══════════════════════════════════════════════════════════════════

SET FOREIGN_KEY_CHECKS = 0;
/*M!999999\- enable the sandbox mode */ 
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;
DROP TABLE IF EXISTS `Ho_Sales_Invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Ho_Sales_Invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `DistKm` text DEFAULT NULL,
  `advancePercentRemark` text DEFAULT NULL,
  `balancePercentRemark` text DEFAULT NULL,
  `billDate` datetime(6) DEFAULT NULL,
  `billNo` text DEFAULT NULL,
  `buyerDetail` text DEFAULT NULL,
  `buyerOrderDate` text DEFAULT NULL,
  `buyerOrderNo` text DEFAULT NULL,
  `cessRate` text DEFAULT NULL,
  `companyBranchId` bigint(20) DEFAULT NULL,
  `containerMark` text DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `createdDt` datetime(6) DEFAULT NULL,
  `cumPackingNotes` text DEFAULT NULL,
  `currency` text DEFAULT NULL,
  `currencyName` text DEFAULT NULL,
  `custName` text DEFAULT NULL,
  `displayKgs` text DEFAULT NULL,
  `empName` text DEFAULT NULL,
  `ewayBill` text DEFAULT NULL,
  `ewayBilldate` datetime(6) DEFAULT NULL,
  `exchangeRate` text DEFAULT NULL,
  `exportTo` text DEFAULT NULL,
  `exporterRef` text DEFAULT NULL,
  `finalDestincountry` text DEFAULT NULL,
  `flightNo` text DEFAULT NULL,
  `fobRemark` text DEFAULT NULL,
  `grossTotal` text DEFAULT NULL,
  `grossWeight` text DEFAULT NULL,
  `gstNo` text DEFAULT NULL,
  `hoId` bigint(20) DEFAULT NULL,
  `hoVechicleNo` text DEFAULT NULL,
  `hocustomerId` bigint(20) DEFAULT NULL,
  `hodeliverAt` bigint(20) DEFAULT NULL,
  `hoitemGroupId` bigint(20) DEFAULT NULL,
  `hubId` bigint(20) DEFAULT NULL,
  `invOrderDate` datetime(6) DEFAULT NULL,
  `invOrderNo` text DEFAULT NULL,
  `invType` bigint(20) DEFAULT NULL,
  `irnAckdate` datetime(6) DEFAULT NULL,
  `irnAckno` text DEFAULT NULL,
  `irnNo` text DEFAULT NULL,
  `locationName` text DEFAULT NULL,
  `netContent` text DEFAULT NULL,
  `netWeight` text DEFAULT NULL,
  `orgineCountry` text DEFAULT NULL,
  `otherReference` text DEFAULT NULL,
  `panNo` text DEFAULT NULL,
  `precarriageBy` text DEFAULT NULL,
  `qd` text DEFAULT NULL,
  `raw_ho_sales_invoice` text DEFAULT NULL,
  `receiptPlace` text DEFAULT NULL,
  `refType` text DEFAULT NULL,
  `roundOff` text DEFAULT NULL,
  `saleType` text DEFAULT NULL,
  `salesInvNo` text DEFAULT NULL,
  `scheme` text DEFAULT NULL,
  `schemePercent` text DEFAULT NULL,
  `siStatus` text DEFAULT NULL,
  `splDisc` text DEFAULT NULL,
  `stateCode` text DEFAULT NULL,
  `targetDiscount` text DEFAULT NULL,
  `tcsPercent` text DEFAULT NULL,
  `totalAmount` text DEFAULT NULL,
  `totalCess` text DEFAULT NULL,
  `totalCgst` text DEFAULT NULL,
  `totalDiscount` text DEFAULT NULL,
  `totalIgst` text DEFAULT NULL,
  `totalMcess` text DEFAULT NULL,
  `totalQtyBox` text DEFAULT NULL,
  `totalQtyPcs` text DEFAULT NULL,
  `totalSgst` text DEFAULT NULL,
  `totalTaxableValue` text DEFAULT NULL,
  `totalTcs` text DEFAULT NULL,
  `totalValue` text DEFAULT NULL,
  `totalVolume` text DEFAULT NULL,
  `transporterId` text DEFAULT NULL,
  `tripId` bigint(20) DEFAULT NULL,
  `tripSort` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `Ho_Sales_Invoice_Hsn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Ho_Sales_Invoice_Hsn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchId` bigint(20) DEFAULT NULL,
  `cessAmount` varchar(255) DEFAULT NULL,
  `cgst` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `createdDate` datetime(6) DEFAULT NULL,
  `discountValue` varchar(255) DEFAULT NULL,
  `hoInvoiceHsnInfoId` bigint(20) DEFAULT NULL,
  `hoinvoiceid` bigint(20) DEFAULT NULL,
  `hoitemGroupId` bigint(20) DEFAULT NULL,
  `hsnCode` varchar(255) DEFAULT NULL,
  `igst` varchar(255) DEFAULT NULL,
  `mcess` varchar(255) DEFAULT NULL,
  `netAmount` varchar(255) DEFAULT NULL,
  `proValue` varchar(255) DEFAULT NULL,
  `refType` varchar(255) DEFAULT NULL,
  `roundOff` varchar(255) DEFAULT NULL,
  `sgst` varchar(255) DEFAULT NULL,
  `soStatus` varchar(255) DEFAULT NULL,
  `taxableValue` varchar(255) DEFAULT NULL,
  `tcs` varchar(255) DEFAULT NULL,
  `totalCgst` varchar(255) DEFAULT NULL,
  `totalIgst` varchar(255) DEFAULT NULL,
  `totalMcess` varchar(255) DEFAULT NULL,
  `totalSgst` varchar(255) DEFAULT NULL,
  `totalTcs` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updatedDate` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `Ho_Sales_Invoice_Items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Ho_Sales_Invoice_Items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `calcdiscountAmount` varchar(255) DEFAULT NULL,
  `cessAmount` varchar(255) DEFAULT NULL,
  `cessRate` varchar(255) DEFAULT NULL,
  `cgst` bigint(20) DEFAULT NULL,
  `cgstPercent` varchar(255) DEFAULT NULL,
  `cgstValue` varchar(255) DEFAULT NULL,
  `containerNo` varchar(255) DEFAULT NULL,
  `earmItemId` bigint(20) DEFAULT NULL,
  `hoInvoiceItemId` bigint(20) DEFAULT NULL,
  `hoItemId` bigint(20) DEFAULT NULL,
  `hoitemGroupId` bigint(20) DEFAULT NULL,
  `hsnCode` varchar(255) DEFAULT NULL,
  `igst` bigint(20) DEFAULT NULL,
  `igstPercent` varchar(255) DEFAULT NULL,
  `igstValue` varchar(255) DEFAULT NULL,
  `invQtyPcs` varchar(255) DEFAULT NULL,
  `invUom` varchar(255) DEFAULT NULL,
  `invoiceId` bigint(20) DEFAULT NULL,
  `invoiceQty` varchar(255) DEFAULT NULL,
  `isFree` varchar(255) DEFAULT NULL,
  `isSchemeGift` varchar(255) DEFAULT NULL,
  `itemDescription` varchar(255) DEFAULT NULL,
  `netAmount` varchar(255) DEFAULT NULL,
  `otherStateDescription` varchar(255) DEFAULT NULL,
  `packingFactor` bigint(20) DEFAULT NULL,
  `sgst` bigint(20) DEFAULT NULL,
  `sgstPercent` varchar(255) DEFAULT NULL,
  `sgstValue` varchar(255) DEFAULT NULL,
  `sortOrder` bigint(20) DEFAULT NULL,
  `totalAmount` varchar(255) DEFAULT NULL,
  `totalVolume` varchar(255) DEFAULT NULL,
  `unitPrice` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `Ho_Sales_Order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Ho_Sales_Order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `DistKm` text DEFAULT NULL,
  `advancePercentRemark` text DEFAULT NULL,
  `balancePercentRemark` text DEFAULT NULL,
  `billDate` datetime(6) DEFAULT NULL,
  `billNo` text DEFAULT NULL,
  `buyerDetail` text DEFAULT NULL,
  `buyerOrderDate` text DEFAULT NULL,
  `buyerOrderNo` text DEFAULT NULL,
  `cessRate` text DEFAULT NULL,
  `companyBranchId` bigint(20) DEFAULT NULL,
  `containerMark` text DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `createdDt` datetime(6) DEFAULT NULL,
  `cumPackingNotes` text DEFAULT NULL,
  `currency` text DEFAULT NULL,
  `currencyName` text DEFAULT NULL,
  `custName` text DEFAULT NULL,
  `displayKgs` text DEFAULT NULL,
  `empName` text DEFAULT NULL,
  `ewayBill` text DEFAULT NULL,
  `ewayBilldate` datetime(6) DEFAULT NULL,
  `exchangeRate` text DEFAULT NULL,
  `exportTo` text DEFAULT NULL,
  `exporterRef` text DEFAULT NULL,
  `finalDestincountry` text DEFAULT NULL,
  `flightNo` text DEFAULT NULL,
  `fobRemark` text DEFAULT NULL,
  `grossTotal` text DEFAULT NULL,
  `grossWeight` text DEFAULT NULL,
  `gstNo` text DEFAULT NULL,
  `hoId` bigint(20) DEFAULT NULL,
  `hoVechicleNo` text DEFAULT NULL,
  `hocustomerId` bigint(20) DEFAULT NULL,
  `hodeliverAt` bigint(20) DEFAULT NULL,
  `hoitemGroupId` bigint(20) DEFAULT NULL,
  `hubId` bigint(20) DEFAULT NULL,
  `invOrderDate` datetime(6) DEFAULT NULL,
  `invOrderNo` text DEFAULT NULL,
  `invType` bigint(20) DEFAULT NULL,
  `irnAckdate` datetime(6) DEFAULT NULL,
  `irnAckno` text DEFAULT NULL,
  `irnNo` text DEFAULT NULL,
  `locationName` text DEFAULT NULL,
  `netContent` text DEFAULT NULL,
  `netWeight` text DEFAULT NULL,
  `orgineCountry` text DEFAULT NULL,
  `otherReference` text DEFAULT NULL,
  `panNo` text DEFAULT NULL,
  `precarriageBy` text DEFAULT NULL,
  `qd` text DEFAULT NULL,
  `raw_ho_sales_Order` text DEFAULT NULL,
  `receiptPlace` text DEFAULT NULL,
  `refType` text DEFAULT NULL,
  `roundOff` text DEFAULT NULL,
  `saleType` text DEFAULT NULL,
  `salesInvNo` text DEFAULT NULL,
  `scheme` text DEFAULT NULL,
  `schemePercent` text DEFAULT NULL,
  `siStatus` text DEFAULT NULL,
  `splDisc` text DEFAULT NULL,
  `stateCode` text DEFAULT NULL,
  `targetDiscount` text DEFAULT NULL,
  `tcsPercent` text DEFAULT NULL,
  `totalAmount` text DEFAULT NULL,
  `totalCess` text DEFAULT NULL,
  `totalCgst` text DEFAULT NULL,
  `totalDiscount` text DEFAULT NULL,
  `totalIgst` text DEFAULT NULL,
  `totalMcess` text DEFAULT NULL,
  `totalQtyBox` text DEFAULT NULL,
  `totalQtyPcs` text DEFAULT NULL,
  `totalSgst` text DEFAULT NULL,
  `totalTaxableValue` text DEFAULT NULL,
  `totalTcs` text DEFAULT NULL,
  `totalValue` text DEFAULT NULL,
  `totalVolume` text DEFAULT NULL,
  `transporterId` text DEFAULT NULL,
  `tripId` bigint(20) DEFAULT NULL,
  `tripSort` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `Ho_Sales_Order_Hsn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Ho_Sales_Order_Hsn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchId` bigint(20) DEFAULT NULL,
  `cessAmount` varchar(255) DEFAULT NULL,
  `cgst` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `createdDate` datetime(6) DEFAULT NULL,
  `discountValue` varchar(255) DEFAULT NULL,
  `hoInvoiceHsnInfoId` bigint(20) DEFAULT NULL,
  `hoitemGroupId` bigint(20) DEFAULT NULL,
  `hsnCode` varchar(255) DEFAULT NULL,
  `igst` varchar(255) DEFAULT NULL,
  `mcess` varchar(255) DEFAULT NULL,
  `netAmount` varchar(255) DEFAULT NULL,
  `orderid` bigint(20) DEFAULT NULL,
  `proValue` varchar(255) DEFAULT NULL,
  `refType` varchar(255) DEFAULT NULL,
  `roundOff` varchar(255) DEFAULT NULL,
  `sgst` varchar(255) DEFAULT NULL,
  `soStatus` varchar(255) DEFAULT NULL,
  `taxableValue` varchar(255) DEFAULT NULL,
  `tcs` varchar(255) DEFAULT NULL,
  `totalCgst` varchar(255) DEFAULT NULL,
  `totalIgst` varchar(255) DEFAULT NULL,
  `totalMcess` varchar(255) DEFAULT NULL,
  `totalSgst` varchar(255) DEFAULT NULL,
  `totalTcs` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updatedDate` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `Ho_Sales_Order_Items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `Ho_Sales_Order_Items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `calcdiscountAmount` varchar(255) DEFAULT NULL,
  `cessAmount` varchar(255) DEFAULT NULL,
  `cessRate` varchar(255) DEFAULT NULL,
  `cgst` bigint(20) DEFAULT NULL,
  `cgstPercent` varchar(255) DEFAULT NULL,
  `cgstValue` varchar(255) DEFAULT NULL,
  `containerNo` varchar(255) DEFAULT NULL,
  `earmItemId` bigint(20) DEFAULT NULL,
  `hoInvoiceItemId` bigint(20) DEFAULT NULL,
  `hoItemId` bigint(20) DEFAULT NULL,
  `hoitemGroupId` bigint(20) DEFAULT NULL,
  `hsnCode` varchar(255) DEFAULT NULL,
  `igst` bigint(20) DEFAULT NULL,
  `igstPercent` varchar(255) DEFAULT NULL,
  `igstValue` varchar(255) DEFAULT NULL,
  `invQtyPcs` varchar(255) DEFAULT NULL,
  `invUom` varchar(255) DEFAULT NULL,
  `invoiceQty` varchar(255) DEFAULT NULL,
  `isFree` varchar(255) DEFAULT NULL,
  `isSchemeGift` varchar(255) DEFAULT NULL,
  `itemDescription` varchar(255) DEFAULT NULL,
  `netAmount` varchar(255) DEFAULT NULL,
  `orderId` bigint(20) DEFAULT NULL,
  `otherStateDescription` varchar(255) DEFAULT NULL,
  `packingFactor` bigint(20) DEFAULT NULL,
  `sgst` bigint(20) DEFAULT NULL,
  `sgstPercent` varchar(255) DEFAULT NULL,
  `sgstValue` varchar(255) DEFAULT NULL,
  `sortOrder` bigint(20) DEFAULT NULL,
  `totalAmount` varchar(255) DEFAULT NULL,
  `totalVolume` varchar(255) DEFAULT NULL,
  `unitPrice` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ReceivedItems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ReceivedItems` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `containertype` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `itemname` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5r1yaqr2w1uuxhxqb9qf7rcou` (`org_id`),
  CONSTRAINT `FK5r1yaqr2w1uuxhxqb9qf7rcou` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `action_configurations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `action_configurations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `levelOfAccess` varchar(255) DEFAULT NULL,
  `successor` varchar(255) DEFAULT NULL,
  `successorType` varchar(255) DEFAULT NULL,
  `taskName` varchar(255) DEFAULT NULL,
  `taskType` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) NOT NULL,
  `department_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb0m8tejvid9ok9xetatv2k82f` (`branch_id`),
  KEY `FKh9kdxkrjmuc3cmshncrmmsy83` (`department_id`),
  CONSTRAINT `FKb0m8tejvid9ok9xetatv2k82f` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKh9kdxkrjmuc3cmshncrmmsy83` FOREIGN KEY (`department_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `action_registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `action_registry` (
  `code` varchar(64) NOT NULL,
  `display_name` varchar(256) NOT NULL,
  `category` varchar(32) NOT NULL,
  `params_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`params_schema_json`)),
  `implemented` tinyint(1) NOT NULL DEFAULT 0,
  `requires_role` varchar(64) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `actions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `custom_configuration` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`custom_configuration`)),
  `quantity` bigint(20) DEFAULT NULL,
  `ref_type` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `task_description` varchar(255) DEFAULT NULL,
  `unique_ref` varchar(255) DEFAULT NULL,
  `unique_ref_description` varchar(255) DEFAULT NULL,
  `unique_ref_id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `action_config_id` bigint(20) NOT NULL,
  `branch_id` bigint(20) NOT NULL,
  `department_id` bigint(20) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn2v07wct8jlw8prllsv3tskiu` (`action_config_id`),
  KEY `FKbjxkolglgynpoa6yvvtwno5d0` (`branch_id`),
  KEY `FK9u9r3oh0905lnov5qv4t4jrl` (`department_id`),
  KEY `FK5y0umuqdhxw4l8pwyb892l5wi` (`owner_id`),
  CONSTRAINT `FK5y0umuqdhxw4l8pwyb892l5wi` FOREIGN KEY (`owner_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK9u9r3oh0905lnov5qv4t4jrl` FOREIGN KEY (`department_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKbjxkolglgynpoa6yvvtwno5d0` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKn2v07wct8jlw8prllsv3tskiu` FOREIGN KEY (`action_config_id`) REFERENCES `action_configurations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `acwages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `acwages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ajournal` varchar(255) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cwagesid` bigint(20) DEFAULT NULL,
  `extin` varchar(255) DEFAULT NULL,
  `extout` varchar(255) DEFAULT NULL,
  `interin` varchar(255) DEFAULT NULL,
  `interout` varchar(255) DEFAULT NULL,
  `intra` varchar(255) DEFAULT NULL,
  `qcproce` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `stkin` varchar(255) DEFAULT NULL,
  `stkout` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `admin_inspection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_inspection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `inspection_details_value` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl4cdb698mnm7e4y5ywyng35ha` (`branch_id`),
  CONSTRAINT `FKl4cdb698mnm7e4y5ywyng35ha` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ai_agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `code` varchar(64) NOT NULL,
  `display_name` varchar(128) NOT NULL,
  `category` varchar(16) NOT NULL,
  `mode` varchar(16) NOT NULL DEFAULT 'SHADOW',
  `model` varchar(64) NOT NULL,
  `kind` varchar(16) NOT NULL DEFAULT 'LLM',
  `population` varchar(16) NOT NULL DEFAULT 'RUNTIME',
  `input_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`input_schema_json`)),
  `output_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`output_schema_json`)),
  `guardrails_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`guardrails_json`)),
  `prompt_version_id` bigint(20) DEFAULT NULL,
  `escalation_role` varchar(64) DEFAULT NULL,
  `confidence_threshold` decimal(3,2) NOT NULL DEFAULT 0.75,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ai_agent_tenant_code` (`tenant_id`,`code`),
  KEY `ix_ai_agent_tenant` (`tenant_id`),
  KEY `ix_ai_agent_category` (`category`),
  KEY `ix_ai_agent_mode` (`mode`),
  KEY `ix_ai_agent_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ai_agent_binding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_binding` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `agent_id` bigint(20) NOT NULL,
  `trigger_kind` varchar(32) NOT NULL,
  `trigger_ref` varchar(256) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `ix_ai_agent_binding_tenant` (`tenant_id`),
  KEY `ix_ai_agent_binding_agent` (`agent_id`),
  KEY `ix_ai_agent_binding_trigger` (`trigger_kind`,`trigger_ref`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ai_agent_promotion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_promotion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `agent_id` bigint(20) NOT NULL,
  `from_mode` varchar(16) NOT NULL,
  `to_mode` varchar(16) NOT NULL,
  `approved_by` bigint(20) NOT NULL,
  `approved_at` datetime NOT NULL DEFAULT current_timestamp(),
  `reason` text DEFAULT NULL,
  `metrics_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metrics_json`)),
  PRIMARY KEY (`id`),
  KEY `ix_ai_agent_promotion_agent` (`agent_id`),
  KEY `ix_ai_agent_promotion_when` (`approved_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ai_agent_prompt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_prompt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `agent_id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  `system_prompt` text NOT NULL,
  `user_template` text NOT NULL,
  `tools_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`tools_json`)),
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `published_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ai_agent_prompt_agent_version` (`agent_id`,`version`),
  KEY `ix_ai_agent_prompt_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ai_agent_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_run` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `agent_id` bigint(20) NOT NULL,
  `binding_id` bigint(20) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `triggered_by` varchar(64) NOT NULL,
  `started_at` datetime NOT NULL DEFAULT current_timestamp(),
  `finished_at` datetime DEFAULT NULL,
  `inputs_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`inputs_json`)),
  `outputs_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`outputs_json`)),
  `mode_at_run` varchar(16) NOT NULL,
  `action_taken` varchar(24) NOT NULL DEFAULT 'PROPOSED',
  `operator_id` bigint(20) DEFAULT NULL,
  `override_reason` text DEFAULT NULL,
  `error` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_ai_agent_run_recent` (`tenant_id`,`agent_id`,`started_at` DESC),
  KEY `ix_ai_agent_run_binding` (`binding_id`),
  KEY `ix_ai_agent_run_workflow` (`workflow_instance_id`),
  KEY `ix_ai_agent_run_action` (`action_taken`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ai_agent_tool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_agent_tool` (
  `code` varchar(64) NOT NULL,
  `handler_bean` varchar(128) NOT NULL,
  `schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`schema_json`)),
  `safety` varchar(16) NOT NULL DEFAULT 'READ',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `api_endpoint_registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_endpoint_registry` (
  `code` varchar(128) NOT NULL,
  `display_name` varchar(256) NOT NULL,
  `method` varchar(8) NOT NULL,
  `url_template` varchar(512) NOT NULL,
  `request_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`request_schema_json`)),
  `response_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`response_schema_json`)),
  `internal` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `arms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `arms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acat` bigint(20) DEFAULT NULL,
  `activity_Desc` varchar(255) DEFAULT NULL,
  `amand_Date` date DEFAULT NULL,
  `aremind` varchar(255) DEFAULT NULL,
  `assign_Branch` bigint(20) DEFAULT NULL,
  `assign_By` bigint(20) DEFAULT NULL,
  `assign_Dept` bigint(20) DEFAULT NULL,
  `assign_To` bigint(20) DEFAULT NULL,
  `bcost` double DEFAULT NULL,
  `begin_Date` date DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `dfname` varchar(255) DEFAULT NULL,
  `dispfont` varchar(255) DEFAULT NULL,
  `disporder` int(11) DEFAULT NULL,
  `dnotify` varchar(255) DEFAULT NULL,
  `duedays` int(11) DEFAULT NULL,
  `dwcost` double DEFAULT NULL,
  `dwtotal` double DEFAULT NULL,
  `end_Date` date DEFAULT NULL,
  `fid` bigint(20) DEFAULT NULL,
  `flow_Flag` int(11) DEFAULT NULL,
  `fmulevel` int(11) DEFAULT NULL,
  `fname` varchar(255) DEFAULT NULL,
  `gnote` varchar(255) DEFAULT NULL,
  `instruction` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `notifyupto` datetime(6) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `rdfname` varchar(255) DEFAULT NULL,
  `remaining_Date` date DEFAULT NULL,
  `rfid` bigint(20) DEFAULT NULL,
  `rfname` varchar(255) DEFAULT NULL,
  `root_id` bigint(20) DEFAULT NULL,
  `staffcost` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stotal` double DEFAULT NULL,
  `toulevel` int(11) DEFAULT NULL,
  `updated_on` varchar(255) DEFAULT NULL,
  `verified_on` varchar(255) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `view_Flag` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `asrs_journal_branch_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `asrs_journal_branch_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `branch_name` varchar(255) DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `asrs_pallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `asrs_pallet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allocatedQty` varchar(255) DEFAULT NULL,
  `asrsPalletNumber` varchar(255) DEFAULT NULL,
  `asrsProductionId` bigint(20) DEFAULT NULL,
  `deduction` varchar(255) DEFAULT NULL,
  `finalASRSQty` varchar(255) DEFAULT NULL,
  `palletNumber` varchar(255) DEFAULT NULL,
  `palletQty` varchar(255) DEFAULT NULL,
  `palletType` varchar(255) DEFAULT NULL,
  `partCategory` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `asrs_production_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `asrs_production_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amendmentCount` bigint(20) DEFAULT NULL,
  `amendment_createdby` bigint(20) DEFAULT NULL,
  `amendmentCreatedTime` datetime(6) DEFAULT NULL,
  `batchId` bigint(20) DEFAULT NULL,
  `batchNo` varchar(255) DEFAULT NULL,
  `dominoCount` bigint(20) DEFAULT NULL,
  `journalId` bigint(20) DEFAULT NULL,
  `journalName` varchar(255) DEFAULT NULL,
  `presentDominoCount` bigint(20) DEFAULT NULL,
  `productId` bigint(20) DEFAULT NULL,
  `productName` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `shiftDate` date DEFAULT NULL,
  `shiftId` bigint(20) DEFAULT NULL,
  `shiftName` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trnNo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `assign_duty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `assign_duty` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `allotment_id` bigint(20) DEFAULT NULL,
  `assign_duty` varchar(255) DEFAULT NULL,
  `assign_duty_id` bigint(20) DEFAULT NULL,
  `assigned_date` date DEFAULT NULL,
  `contract` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `current_duty` varchar(255) DEFAULT NULL,
  `emp_type` int(11) DEFAULT NULL,
  `staff_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `assign_branch_id` bigint(20) DEFAULT NULL,
  `assign_dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpv0y5y4nq6p927x4djq1fmdwg` (`assign_branch_id`),
  KEY `FK3mog7anlgv9b7bxtcwxdlwvap` (`assign_dept_id`),
  CONSTRAINT `FK3mog7anlgv9b7bxtcwxdlwvap` FOREIGN KEY (`assign_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKpv0y5y4nq6p927x4djq1fmdwg` FOREIGN KEY (`assign_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `atlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `atlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `logid` bigint(20) DEFAULT NULL,
  `logtype` varchar(255) DEFAULT NULL,
  `mcid` bigint(20) DEFAULT NULL,
  `resid` bigint(20) DEFAULT NULL,
  `resval` varchar(255) DEFAULT NULL,
  `schid` bigint(20) DEFAULT NULL,
  `taskid` bigint(20) DEFAULT NULL,
  `tcid` bigint(20) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `Break` varchar(255) DEFAULT NULL,
  `Breakin` datetime(6) DEFAULT NULL,
  `Breakout` datetime(6) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `atype` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `over_Due_To_Break_Time_Reason` varchar(255) DEFAULT NULL,
  `over_time` varchar(255) DEFAULT NULL,
  `over_Time_Reason` varchar(255) DEFAULT NULL,
  `production` varchar(255) DEFAULT NULL,
  `punchin` datetime(6) DEFAULT NULL,
  `punchout` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj3bgn2jwrcgvw8ods1ih2jiic` (`user_id`),
  CONSTRAINT `FKj3bgn2jwrcgvw8ods1ih2jiic` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attendance_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_timeline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attendanceDate` datetime(6) DEFAULT NULL,
  `attendanceId` bigint(20) DEFAULT NULL,
  `attendanceTime` datetime(6) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `endposition` varchar(255) DEFAULT NULL,
  `inTime` datetime(6) DEFAULT NULL,
  `instrulogId` bigint(20) DEFAULT NULL,
  `outTime` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `biomast_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7p8h5wbit6wnb1sf8a8dhjehu` (`biomast_id`),
  CONSTRAINT `FK7p8h5wbit6wnb1sf8a8dhjehu` FOREIGN KEY (`biomast_id`) REFERENCES `biomast` (`sno`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attribute_Type` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `direct_cummulative` varchar(255) DEFAULT NULL,
  `disp_Order` bigint(20) DEFAULT NULL,
  `formula` varchar(255) DEFAULT NULL,
  `input_Type` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `position_Number` bigint(20) DEFAULT NULL,
  `purpose_benefit` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `value_Type` varchar(255) DEFAULT NULL,
  `workinst_saftmeasure` varchar(255) DEFAULT NULL,
  `task_category_id` bigint(20) DEFAULT NULL,
  `task_name_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKif8b66s7whu0nsfm05p8n3i3r` (`task_category_id`),
  KEY `FK3s7tor90kmqljhc4qh29ap1st` (`task_name_id`),
  CONSTRAINT `FK3s7tor90kmqljhc4qh29ap1st` FOREIGN KEY (`task_name_id`) REFERENCES `task_name` (`id`),
  CONSTRAINT `FKif8b66s7whu0nsfm05p8n3i3r` FOREIGN KEY (`task_category_id`) REFERENCES `task_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attributes_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `attributes_assignments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `field_Name` varchar(255) DEFAULT NULL,
  `mode_Of_Entry` varchar(255) DEFAULT NULL,
  `process` varchar(255) DEFAULT NULL,
  `screen_Name` varchar(255) DEFAULT NULL,
  `task_Type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `kpi_name_id` bigint(20) DEFAULT NULL,
  `product_master_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrqol8c9l47rrnu6e12ki3npb5` (`dept_id`),
  KEY `FKn7014taia0fnwuqglwxwjhi2k` (`kpi_name_id`),
  KEY `FKerklsj3pb112pjjtkh1fsvrun` (`product_master_id`),
  CONSTRAINT `FKerklsj3pb112pjjtkh1fsvrun` FOREIGN KEY (`product_master_id`) REFERENCES `products_master` (`id`),
  CONSTRAINT `FKn7014taia0fnwuqglwxwjhi2k` FOREIGN KEY (`kpi_name_id`) REFERENCES `kpiname` (`id`),
  CONSTRAINT `FKrqol8c9l47rrnu6e12ki3npb5` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `award`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `award` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `achievements` varchar(255) DEFAULT NULL,
  `award_category` varchar(255) DEFAULT NULL,
  `award_date` datetime(6) DEFAULT NULL,
  `award_for` varchar(255) DEFAULT NULL,
  `award_status` varchar(255) DEFAULT NULL,
  `award_value` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `employee_category` varchar(255) DEFAULT NULL,
  `project` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7gwq1nkqsncy9oh13uxdslxen` (`branch_id`),
  KEY `FK2fu7wbryafou4hlyggj0ipujw` (`user_id`),
  CONSTRAINT `FK2fu7wbryafou4hlyggj0ipujw` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK7gwq1nkqsncy9oh13uxdslxen` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `award_maintainance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `award_maintainance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `award_category` varchar(255) DEFAULT NULL,
  `award_date` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `employee_category` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6ldvdko2svu7rbpccrpijg5i3` (`branch_id`),
  KEY `FKr84cm2iqlke084qiixvt9fup6` (`user_id`),
  CONSTRAINT `FK6ldvdko2svu7rbpccrpijg5i3` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKr84cm2iqlke084qiixvt9fup6` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bankdetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bankdetail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `branch` varchar(255) DEFAULT NULL,
  `centre` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `ifsc` varchar(255) DEFAULT NULL,
  `lastmodified` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bankinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bankinfo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accno` varchar(255) DEFAULT NULL,
  `bank` varchar(255) DEFAULT NULL,
  `bankid` bigint(20) DEFAULT NULL,
  `branch` varchar(255) DEFAULT NULL,
  `ifsc` varchar(255) DEFAULT NULL,
  `lastmodified` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `usertime` varchar(255) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  `vtable` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `batchmaster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `batchmaster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchno` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `btype` varchar(255) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `del` varchar(255) DEFAULT NULL,
  `delby` bigint(20) DEFAULT NULL,
  `deldt` datetime(6) DEFAULT NULL,
  `delydt` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `inwdt` datetime(6) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `lockstatus` varchar(255) DEFAULT NULL,
  `mcid` bigint(20) DEFAULT NULL,
  `pot` double DEFAULT NULL,
  `productid` bigint(20) DEFAULT NULL,
  `pshift` varchar(255) DEFAULT NULL,
  `score` varchar(255) DEFAULT NULL,
  `shiftdt` datetime(6) DEFAULT NULL,
  `shiftid` bigint(20) DEFAULT NULL,
  `si` varchar(255) DEFAULT NULL,
  `sj` varchar(255) DEFAULT NULL,
  `stkqty` double DEFAULT NULL,
  `tgt` double DEFAULT NULL,
  `track` varchar(255) DEFAULT NULL,
  `unlockby` bigint(20) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  `x` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_exchange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_exchange` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bill_amount` double DEFAULT NULL,
  `bill_date` datetime(6) DEFAULT NULL,
  `bill_inward_date` datetime(6) DEFAULT NULL,
  `bill_inward_id` bigint(20) DEFAULT NULL,
  `bill_number` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `firm_org_id` bigint(20) DEFAULT NULL,
  `hand_over_date` datetime(6) DEFAULT NULL,
  `hand_over_type` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `verification_date` datetime(6) DEFAULT NULL,
  `verification_status` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `exchange_user_id` bigint(20) DEFAULT NULL,
  `hand_over_to_dept_id` bigint(20) DEFAULT NULL,
  `hand_over_to_branch_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsl59u5lvf6psk9vyp1jutx86y` (`user_id`),
  KEY `FKngs2ghifsa143m8enlag1clq1` (`exchange_user_id`),
  KEY `FKaf8r91wt88h10jq5newt89tsu` (`hand_over_to_dept_id`),
  KEY `FKtqvyfekrw5qhxxjrqrt69g58i` (`hand_over_to_branch_id`),
  KEY `FKhmhg6j6omlv1081jvaef7kfkc` (`vendor_id`),
  CONSTRAINT `FKaf8r91wt88h10jq5newt89tsu` FOREIGN KEY (`hand_over_to_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKhmhg6j6omlv1081jvaef7kfkc` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKngs2ghifsa143m8enlag1clq1` FOREIGN KEY (`exchange_user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKsl59u5lvf6psk9vyp1jutx86y` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKtqvyfekrw5qhxxjrqrt69g58i` FOREIGN KEY (`hand_over_to_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_exchange_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_exchange_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `exchange_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_type` bigint(20) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `po_id` bigint(20) DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_forward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_forward` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bill_approved_by` bigint(20) DEFAULT NULL,
  `bill_date` datetime(6) DEFAULT NULL,
  `bill_inward_date` datetime(6) DEFAULT NULL,
  `bill_number` varchar(255) DEFAULT NULL,
  `bill_approved_date` datetime(6) DEFAULT NULL,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `bill_verified_by` bigint(20) DEFAULT NULL,
  `bill_verified_date` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `firm_org_id` bigint(20) DEFAULT NULL,
  `forward_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `from_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `hard_copy_user_id` bigint(20) DEFAULT NULL,
  `to_branch_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhsmc3l4sqr66o42ok4tc67pbi` (`from_branch_id`),
  KEY `FK1rh7x2viaaka8ehjcp8gv0wvt` (`user_id`),
  KEY `FKo4e823ccjdoluu3fucnkw8dym` (`hard_copy_user_id`),
  KEY `FK6x0a7rt6sdlmumrtwmo399uej` (`to_branch_id`),
  KEY `FK4r2d097jqtna1owu2ugdlsnf5` (`vendor_id`),
  CONSTRAINT `FK1rh7x2viaaka8ehjcp8gv0wvt` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK4r2d097jqtna1owu2ugdlsnf5` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FK6x0a7rt6sdlmumrtwmo399uej` FOREIGN KEY (`to_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKhsmc3l4sqr66o42ok4tc67pbi` FOREIGN KEY (`from_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKo4e823ccjdoluu3fucnkw8dym` FOREIGN KEY (`hard_copy_user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_forward_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_forward_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `forward_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_type` bigint(20) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `po_id` bigint(20) DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_inward`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_inward` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bill_amount` double DEFAULT NULL,
  `bill_copy` varchar(255) DEFAULT NULL,
  `bill_date` datetime(6) DEFAULT NULL,
  `bill_freight_amount` double DEFAULT NULL,
  `billno` varchar(255) DEFAULT NULL,
  `camp` varchar(255) DEFAULT NULL,
  `cess_tax` varchar(255) DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `cgstper` double DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dedLorryfreight` double DEFAULT NULL,
  `dedotheramount` double DEFAULT NULL,
  `eway_bill` varchar(255) DEFAULT NULL,
  `ho_bill_id` bigint(20) DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `igstper` double DEFAULT NULL,
  `insurance` double DEFAULT NULL,
  `lorry_date` datetime(6) DEFAULT NULL,
  `lorry_serial_no` varchar(255) DEFAULT NULL,
  `lorry_freight` bit(1) DEFAULT NULL,
  `lorry_with_Weight` varchar(255) DEFAULT NULL,
  `lorry_without_Weight` varchar(255) DEFAULT NULL,
  `lorryfreight` double DEFAULT NULL,
  `lorryno` varchar(255) DEFAULT NULL,
  `otheramount` double DEFAULT NULL,
  `per_bag_cost` varchar(255) DEFAULT NULL,
  `product_category` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `raw_ho_grn` varchar(255) DEFAULT NULL,
  `received_date` datetime(6) DEFAULT NULL,
  `round_of_type` varchar(255) DEFAULT NULL,
  `round_of_value` varchar(255) DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  `sgstper` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tcs` varchar(255) DEFAULT NULL,
  `tcsper` double DEFAULT NULL,
  `totBags` double DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `deliver_branch_id` bigint(20) DEFAULT NULL,
  `firm_org_id` bigint(20) DEFAULT NULL,
  `inward_branch_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcdm4c9o0liahbexp8g7j7wo24` (`deliver_branch_id`),
  KEY `FK109newyuypi275s4pda7iqx83` (`firm_org_id`),
  KEY `FK89gsdanud30o07odyr79hp614` (`inward_branch_id`),
  KEY `FK63g0cyjj92m71167hcsx79yci` (`vendor_id`),
  CONSTRAINT `FK109newyuypi275s4pda7iqx83` FOREIGN KEY (`firm_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK63g0cyjj92m71167hcsx79yci` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FK89gsdanud30o07odyr79hp614` FOREIGN KEY (`inward_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKcdm4c9o0liahbexp8g7j7wo24` FOREIGN KEY (`deliver_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_inward_product_item_received`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_inward_product_item_received` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `check_lorry_serial_date` date DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `conv_package_quantity` double DEFAULT NULL,
  `conv_package_quantity_value` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `final_order_price` double DEFAULT NULL,
  `freight_charges` double DEFAULT NULL,
  `ho_item_rec_id` bigint(20) DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `inspection_status` varchar(255) DEFAULT NULL,
  `inward_id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_type` bigint(20) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `package_quantity` varchar(255) DEFAULT NULL,
  `package_type` varchar(255) DEFAULT NULL,
  `packing_charges` double DEFAULT NULL,
  `pending_qty` double DEFAULT NULL,
  `po_item_id` bigint(20) DEFAULT NULL,
  `po_id` bigint(20) DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `received_quantity` double DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  `subtotalreceive` double DEFAULT NULL,
  `totalwtaxreceive` double DEFAULT NULL,
  `unit_cost` varchar(255) DEFAULT NULL,
  `unit_rate` varchar(255) DEFAULT NULL,
  `uom` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_lorry_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_lorry_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact_number` varchar(255) DEFAULT NULL,
  `inward_id` bigint(20) DEFAULT NULL,
  `lorry_no` varchar(255) DEFAULT NULL,
  `pan_no` varchar(255) DEFAULT NULL,
  `payable_freight_amount` varchar(255) DEFAULT NULL,
  `payee_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_number`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_number` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `billNo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_receive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_receive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `billDate` datetime(6) DEFAULT NULL,
  `billInwardDate` datetime(6) DEFAULT NULL,
  `billNumber` varchar(255) DEFAULT NULL,
  `billReceiveValue` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `exchangeDate` datetime(6) DEFAULT NULL,
  `firm_org_id` bigint(20) DEFAULT NULL,
  `forwardDate` datetime(6) DEFAULT NULL,
  `mdStatus` varchar(255) DEFAULT NULL,
  `mdapprovalDate` datetime(6) DEFAULT NULL,
  `receivedDate` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `from_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `received_branch_id` bigint(20) DEFAULT NULL,
  `received_user_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKko7nhckrw3gj92t8csvu89bja` (`from_branch_id`),
  KEY `FK1a122w1rtpep78gbt2y90yvnv` (`user_id`),
  KEY `FKgwfnl0x7jq6nxcpl3luv4wcb3` (`received_branch_id`),
  KEY `FKbjravmebyrynoe2p160eckbwm` (`received_user_id`),
  KEY `FK79i9nct6bn5o8rkyk7qyjpuex` (`vendor_id`),
  CONSTRAINT `FK1a122w1rtpep78gbt2y90yvnv` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK79i9nct6bn5o8rkyk7qyjpuex` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKbjravmebyrynoe2p160eckbwm` FOREIGN KEY (`received_user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKgwfnl0x7jq6nxcpl3luv4wcb3` FOREIGN KEY (`received_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKko7nhckrw3gj92t8csvu89bja` FOREIGN KEY (`from_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bill_receive_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_receive_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchno` varchar(255) DEFAULT NULL,
  `lorrySerialNo` varchar(255) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `receiveId` bigint(20) DEFAULT NULL,
  `product_master_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnjk8tsjnj6i1o0un7opuyu900` (`product_master_id`),
  CONSTRAINT `FKnjk8tsjnj6i1o0un7opuyu900` FOREIGN KEY (`product_master_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bin_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bin_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allotted_quantity` double DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bin_stock_id` bigint(20) DEFAULT NULL,
  `binid` bigint(20) DEFAULT NULL,
  `receive_bin_stock_id` bigint(20) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `binlock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `binlock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bin_no` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `inward_date` datetime(6) DEFAULT NULL,
  `lock_date` datetime(6) DEFAULT NULL,
  `lock_status` varchar(255) DEFAULT NULL,
  `lock_type` varchar(255) DEFAULT NULL,
  `lorry_serial_no` varchar(255) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `employee_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9qdb1rpir603w7evop59fq2vb` (`employee_id`),
  KEY `FKj1gywaqukhwhxjp7sjlsnjnbm` (`product_id`),
  CONSTRAINT `FK9qdb1rpir603w7evop59fq2vb` FOREIGN KEY (`employee_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKj1gywaqukhwhxjp7sjlsnjnbm` FOREIGN KEY (`product_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `binrange`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `binrange` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `docketno` bigint(20) DEFAULT NULL,
  `goalive` varchar(255) DEFAULT NULL,
  `hub` varchar(255) DEFAULT NULL,
  `hubtrnno` bigint(20) DEFAULT NULL,
  `opbatch` varchar(255) DEFAULT NULL,
  `pocketno` bigint(20) DEFAULT NULL,
  `slfm` bigint(20) DEFAULT NULL,
  `slto` bigint(20) DEFAULT NULL,
  `trnprefix` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhleukghy7ritscdconix1gcjv` (`godown_id`),
  KEY `FK3jmslmq3u93gleh8jwy1juiaq` (`org_id`),
  CONSTRAINT `FK3jmslmq3u93gleh8jwy1juiaq` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKhleukghy7ritscdconix1gcjv` FOREIGN KEY (`godown_id`) REFERENCES `godown` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `binstatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `binstatus` (
  `bin_name` bigint(20) NOT NULL,
  `locked` bit(1) DEFAULT NULL,
  `locktype` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`bin_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `binstock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `binstock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adate` date DEFAULT NULL,
  `avgcost` double DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `binid` varchar(255) DEFAULT NULL,
  `binname` bigint(20) DEFAULT NULL,
  `bintype` varchar(255) DEFAULT NULL,
  `blending_lock` bit(1) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `camp_name` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `delstatus` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `gid` bigint(20) DEFAULT NULL,
  `inwarddate` datetime(6) DEFAULT NULL,
  `ltrnno` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `lockstatus` varchar(255) DEFAULT NULL,
  `locktype` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `material_Transaction_Id` bigint(20) DEFAULT NULL,
  `mrnstatus` varchar(255) DEFAULT NULL,
  `oil_FFA` double DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `purchase_bill_id` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `sstkqty` double DEFAULT NULL,
  `seedmoisture` varchar(255) DEFAULT NULL,
  `seedcolor` varchar(255) DEFAULT NULL,
  `stkqty` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `binstock_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `binstock_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accouting_date` date DEFAULT NULL,
  `avgcost` double DEFAULT NULL,
  `balance` double DEFAULT NULL,
  `batch_id` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bill_id` bigint(20) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `entry_type` varchar(255) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `naration` varchar(255) DEFAULT NULL,
  `open_close_balance` double DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `trn_type` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `update_on` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `biomast`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `biomast` (
  `sno` bigint(20) NOT NULL AUTO_INCREMENT,
  `creason` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `cstatus` varchar(255) DEFAULT NULL,
  `gate` varchar(255) DEFAULT NULL,
  `instrutype` bigint(20) DEFAULT NULL,
  `interval` bigint(20) DEFAULT NULL,
  `istatus` varchar(255) DEFAULT NULL,
  `lastupdate` datetime(6) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `lupdt` datetime(6) DEFAULT NULL,
  `lupid` bigint(20) DEFAULT NULL,
  `machineip` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `branchid` bigint(20) NOT NULL,
  PRIMARY KEY (`sno`),
  KEY `FKauun78mw0d31oxlio1ueit6ic` (`branchid`),
  CONSTRAINT `FKauun78mw0d31oxlio1ueit6ic` FOREIGN KEY (`branchid`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_advice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_advice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blending_detail_type` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `from_date` date DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  `per_Kalasal_Qty` double DEFAULT NULL,
  `previous_cas` varchar(255) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_advice_bin_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_advice_bin_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `blending_advice_stock_id` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `mainlot` double DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_advice_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_advice_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blending_advice_id` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `cas_trn` varchar(255) DEFAULT NULL,
  `completed_no_of_kalasal` double DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_no_of_kalasal` double DEFAULT NULL,
  `total_no_of_kalasal_bag` double DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_advice_preprocess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_advice_preprocess` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blending_advice_details_id` bigint(20) DEFAULT NULL,
  `blending_advice_stock_id` bigint(20) DEFAULT NULL,
  `cas_group` varchar(255) DEFAULT NULL,
  `process` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_advice_preprocess_bin_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_advice_preprocess_bin_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `blending_advice_preprocess_id` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_advice_stock_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_advice_stock_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blending_advice_details_id` bigint(20) DEFAULT NULL,
  `cas_group` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `no_of_bags` double DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_preprocess_request_receive_bin_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_preprocess_request_receive_bin_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `blending_advice_stock_id` bigint(20) DEFAULT NULL,
  `blending_Detail_Id` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `casgroup` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_receive_bin_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_receive_bin_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `blending_Detail_Id` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `casgroup` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `type_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_request_preprocess_bin_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_request_preprocess_bin_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blending_Detail_Id` bigint(20) DEFAULT NULL,
  `casgroup` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `type_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `blending_scheduler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `blending_scheduler` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `a_date` date DEFAULT NULL,
  `adatetime` datetime(6) DEFAULT NULL,
  `blending_id` bigint(20) DEFAULT NULL,
  `blending_table` varchar(255) DEFAULT NULL,
  `blending_type` varchar(255) DEFAULT NULL,
  `from_date` date DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bloodgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bloodgroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `branch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acount` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `bcontrol` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `costreport` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dbname` varchar(255) DEFAULT NULL,
  `dgmid` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fax` varchar(255) DEFAULT NULL,
  `hostname` varchar(255) DEFAULT NULL,
  `hubid` varchar(255) DEFAULT NULL,
  `manager` varchar(255) DEFAULT NULL,
  `papprove` varchar(255) DEFAULT NULL,
  `pcount` varchar(255) DEFAULT NULL,
  `pdgm` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `pincode` varchar(255) DEFAULT NULL,
  `pmdesig` varchar(255) DEFAULT NULL,
  `podesig` varchar(255) DEFAULT NULL,
  `poname` varchar(255) DEFAULT NULL,
  `production` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `pv` varchar(255) DEFAULT NULL,
  `pverify` varchar(255) DEFAULT NULL,
  `registrationno` varchar(255) DEFAULT NULL,
  `regn_2` varchar(255) DEFAULT NULL,
  `rmcolid` varchar(255) DEFAULT NULL,
  `rmwh` varchar(255) DEFAULT NULL,
  `safetyinst` varchar(255) DEFAULT NULL,
  `search` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `stkcolid` varchar(255) DEFAULT NULL,
  `tinno` varchar(255) DEFAULT NULL,
  `trnprefix` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vattin` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn12qc1o6utk8ew7rd0bm0frc5` (`org_id`),
  CONSTRAINT `FKn12qc1o6utk8ew7rd0bm0frc5` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `branch_Org_Map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch_Org_Map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `brmproids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `brmproids` (
  `lno` bigint(20) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `day_End_Closing_Flag` bit(1) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `dispunit1` varchar(255) DEFAULT NULL,
  `dispunit2` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pcategory` varchar(255) DEFAULT NULL,
  `smanage` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stkgrp` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`lno`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `broker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `broker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `contact` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parentid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bslap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bslap` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `colid` varchar(255) DEFAULT NULL,
  `dispcolor` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pshift` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `camp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `camp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `camp` varchar(255) DEFAULT NULL,
  `place` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `state_id` bigint(20) DEFAULT NULL,
  `updated_on` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `candidate_qualification_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `candidate_qualification_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `candidate_id` bigint(20) DEFAULT NULL,
  `marks` varchar(255) DEFAULT NULL,
  `qualification_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `canteen_attendance_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_attendance_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attendance_menu_id` bigint(20) DEFAULT NULL,
  `in_time` datetime(6) DEFAULT NULL,
  `out_time` datetime(6) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3ldfhx1ts17nvkiojkjj2emob` (`user_id`),
  CONSTRAINT `FK3ldfhx1ts17nvkiojkjj2emob` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `canteen_attendance_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_attendance_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `shift1` varchar(255) DEFAULT NULL,
  `shift1no_Of_Snacks_Tokens_Used` varchar(255) DEFAULT NULL,
  `shift1no_Of_Tea_Tokens_Used` varchar(255) DEFAULT NULL,
  `shift2` varchar(255) DEFAULT NULL,
  `shift2no_Of_Snacks_Tokens_Used` varchar(255) DEFAULT NULL,
  `shift2no_Of_Tea_Tokens_Used` varchar(255) DEFAULT NULL,
  `shift3` varchar(255) DEFAULT NULL,
  `shift3no_Of_Snacks_Tokens_Used` varchar(255) DEFAULT NULL,
  `shift3no_Of_Tea_Tokens_Used` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `canteen_attendances`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_attendances` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `canteen_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_menu` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `from_date` date DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `canteen_menu_day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_menu_day` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `food_id` bigint(20) DEFAULT NULL,
  `day_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1dlo175c0emq55ojudts93nj3` (`day_id`),
  CONSTRAINT `FK1dlo175c0emq55ojudts93nj3` FOREIGN KEY (`day_id`) REFERENCES `day` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `canteen_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `canteen_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `groups` varchar(255) DEFAULT NULL,
  `no_Of_Token` bigint(20) DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `token_for` varchar(255) DEFAULT NULL,
  `token_no` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4gi2jmiml8jyn7nmiwjcu67kh` (`dept_id`),
  KEY `FKcemf78p4g323apxsw9twoyjpp` (`shift_id`),
  KEY `FKo4co4myar4krsnv5eoq2r9uky` (`user_id`),
  CONSTRAINT `FK4gi2jmiml8jyn7nmiwjcu67kh` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKcemf78p4g323apxsw9twoyjpp` FOREIGN KEY (`shift_id`) REFERENCES `shift_details` (`id`),
  CONSTRAINT `FKo4co4myar4krsnv5eoq2r9uky` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `category_constants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_constants` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `type` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cexpense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cexpense` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `aendt` varchar(255) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `branch1` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cdate` datetime(6) DEFAULT NULL,
  `cdesc` varchar(255) DEFAULT NULL,
  `cendt` varchar(255) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `conid` bigint(20) DEFAULT NULL,
  `contype` varchar(255) DEFAULT NULL,
  `ctype` varchar(255) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `logid` bigint(20) DEFAULT NULL,
  `logtable` varchar(255) DEFAULT NULL,
  `narration` varchar(255) DEFAULT NULL,
  `sconid` bigint(20) DEFAULT NULL,
  `scontype` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `vendt` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `city`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `city` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `countryid` bigint(20) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `districtid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pincode` int(11) DEFAULT NULL,
  `poname` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `stateid` bigint(20) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cprefix` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `oldname` varchar(255) DEFAULT NULL,
  `production` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `pv` varchar(255) DEFAULT NULL,
  `responsibility` varchar(255) DEFAULT NULL,
  `trnprefix` varchar(255) DEFAULT NULL,
  `utilisation` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh0h1j9y5amg0d9a05tdknujnv` (`branch_id`),
  KEY `FK79fgiam7pdugov79jb6x00pna` (`dept_id`),
  KEY `FKjtcikj5u0td9c24dil9cky215` (`org_id`),
  CONSTRAINT `FK79fgiam7pdugov79jb6x00pna` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKh0h1j9y5amg0d9a05tdknujnv` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKjtcikj5u0td9c24dil9cky215` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `color`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `color` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `common_report_menu_title`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `common_report_menu_title` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `compentency_matrix`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `compentency_matrix` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `desgination_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2xkg3rmvrdr06ckrquy33k9bc` (`dept_id`),
  KEY `FKcnkxxi5vaf6oph3m2a3i8e410` (`desgination_id`),
  CONSTRAINT `FK2xkg3rmvrdr06ckrquy33k9bc` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKcnkxxi5vaf6oph3m2a3i8e410` FOREIGN KEY (`desgination_id`) REFERENCES `designation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `consproducts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `consproducts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_On` datetime(6) DEFAULT NULL,
  `eid` bigint(20) DEFAULT NULL,
  `etable` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `process` bigint(20) DEFAULT NULL,
  `process_table` bigint(20) DEFAULT NULL,
  `product` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `consumption_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `consumption_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `consumed_by` bigint(20) DEFAULT NULL,
  `consumption_cost` double DEFAULT NULL,
  `consumption_datefrom` varchar(255) DEFAULT NULL,
  `consumption_dateto` varchar(255) DEFAULT NULL,
  `consumption_name` varchar(255) DEFAULT NULL,
  `consumption_qty` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `dp_entry_stored_product_id` bigint(20) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `conszone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `conszone` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `concate` varchar(255) DEFAULT NULL,
  `contype` varchar(255) DEFAULT NULL,
  `czone` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `mcid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cost_expense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cost_expense` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `cendt` varchar(255) DEFAULT NULL,
  `cluster` varchar(255) DEFAULT NULL,
  `conid` bigint(20) DEFAULT NULL,
  `contype` varchar(255) DEFAULT NULL,
  `cost_desc` varchar(255) DEFAULT NULL,
  `cost_type` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `logid` bigint(20) DEFAULT NULL,
  `logtable` varchar(255) DEFAULT NULL,
  `narration` varchar(255) DEFAULT NULL,
  `sconid` bigint(20) DEFAULT NULL,
  `scontype` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `country` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `currency` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `symbols` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `credit_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `approved_date` datetime(6) DEFAULT NULL,
  `authorized_date` datetime(6) DEFAULT NULL,
  `bill_approved_By` bigint(20) DEFAULT NULL,
  `bill_approved_Status` varchar(255) DEFAULT NULL,
  `bill_approved_Time` varchar(255) DEFAULT NULL,
  `bill_authorized_by` bigint(20) DEFAULT NULL,
  `bill_authorized_status` varchar(255) DEFAULT NULL,
  `bill_authorized_Time` varchar(255) DEFAULT NULL,
  `billcount` bigint(20) DEFAULT NULL,
  `bill_inward_by` bigint(20) DEFAULT NULL,
  `bill_inward_date` varchar(255) DEFAULT NULL,
  `bill_inward_quantity` double DEFAULT NULL,
  `bill_inward_status` varchar(255) DEFAULT NULL,
  `bill_inward_time` varchar(255) DEFAULT NULL,
  `bill_verified_by` bigint(20) DEFAULT NULL,
  `bill_verified_Status` varchar(255) DEFAULT NULL,
  `bill_Verified_Time` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `credit_amount` double DEFAULT NULL,
  `credit_bill_date` datetime(6) DEFAULT NULL,
  `credit_category` bigint(20) DEFAULT NULL,
  `credit_note_number` varchar(255) DEFAULT NULL,
  `credit_note_quantity` double DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `inward_bill_number` varchar(255) DEFAULT NULL,
  `inward_branch` bigint(20) DEFAULT NULL,
  `inward_company` bigint(20) DEFAULT NULL,
  `round_Off` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stock_inward_by` bigint(20) DEFAULT NULL,
  `stock_inward_status` varchar(255) DEFAULT NULL,
  `stock_inward_time` varchar(255) DEFAULT NULL,
  `tcs_amount` double DEFAULT NULL,
  `tcs_percentage` double DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `authorized_by` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1livplll89itqtek9e35bq4q4` (`approved_by`),
  KEY `FKid58siacsluf2gb517i9lordm` (`authorized_by`),
  KEY `FK3tr227f3uabouloaeiytg0s8v` (`created_by`),
  KEY `FK8vnjsxdcqh7ughom5lpsr1u7r` (`verified_by`),
  CONSTRAINT `FK1livplll89itqtek9e35bq4q4` FOREIGN KEY (`approved_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK3tr227f3uabouloaeiytg0s8v` FOREIGN KEY (`created_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK8vnjsxdcqh7ughom5lpsr1u7r` FOREIGN KEY (`verified_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKid58siacsluf2gb517i9lordm` FOREIGN KEY (`authorized_by`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `creditnote_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `creditnote_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `creditnote_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `creditnote_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `bill_number` varchar(255) DEFAULT NULL,
  `cgst_amount` double DEFAULT NULL,
  `cgst_percentage` double DEFAULT NULL,
  `credit_note_id` bigint(20) DEFAULT NULL,
  `credit_quantity` double DEFAULT NULL,
  `igst_amount` double DEFAULT NULL,
  `igst_percentage` double DEFAULT NULL,
  `line_total` double DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `party_id` bigint(20) DEFAULT NULL,
  `ref_id` bigint(20) DEFAULT NULL,
  `round_Off` varchar(255) DEFAULT NULL,
  `sgst_pmount` double DEFAULT NULL,
  `sgst_percentage` double DEFAULT NULL,
  `stock_inward_by` bigint(20) DEFAULT NULL,
  `unit_rate` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `customvou_deptmap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `customvou_deptmap` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `customVouNameId` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cwages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cwages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity` varchar(255) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `codeno` varchar(255) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `journal` varchar(255) DEFAULT NULL,
  `mvmt` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `salstruid` bigint(20) DEFAULT NULL,
  `slab` bigint(20) DEFAULT NULL,
  `sno` varchar(255) DEFAULT NULL,
  `tport` varchar(255) DEFAULT NULL,
  `trnyear` bigint(20) DEFAULT NULL,
  `type` bigint(20) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cwagesdetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cwagesdetail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amt` double DEFAULT NULL,
  `cwagesid` bigint(20) DEFAULT NULL,
  `desigid` bigint(20) DEFAULT NULL,
  `luzone` bigint(20) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `reftrnid` bigint(20) DEFAULT NULL,
  `reftrnno` varchar(255) DEFAULT NULL,
  `reftrntable` varchar(255) DEFAULT NULL,
  `trnid` bigint(20) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `urate` double DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cwagessum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cwagessum` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amt` double DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `con` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `expref` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `pshift` bigint(20) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `reftrnid` bigint(20) DEFAULT NULL,
  `reftrnno` varchar(255) DEFAULT NULL,
  `reftrntable` varchar(255) DEFAULT NULL,
  `shiftdate` date DEFAULT NULL,
  `trndate` date DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trnqty` double DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `von` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cwagestrn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cwagestrn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amt` double DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `logid` bigint(20) DEFAULT NULL,
  `pshift` bigint(20) DEFAULT NULL,
  `shiftdate` date DEFAULT NULL,
  `shiftid` bigint(20) DEFAULT NULL,
  `trnid` bigint(20) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dacategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dacategory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `colid` varchar(255) DEFAULT NULL,
  `dw` varchar(255) DEFAULT NULL,
  `mw` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `daily_emp_dependency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_emp_dependency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dateofbirth` varchar(255) DEFAULT NULL,
  `education` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `relationship` varchar(255) DEFAULT NULL,
  `userid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `daily_wages_attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_wages_attendance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `punchIn` datetime(6) DEFAULT NULL,
  `punchOut` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkhssc0ny7rke5uwsg1wydk8eo` (`user_id`),
  CONSTRAINT `FKkhssc0ny7rke5uwsg1wydk8eo` FOREIGN KEY (`user_id`) REFERENCES `emast_daily` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `daily_wages_attendance_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_wages_attendance_timeline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attendanceDate` datetime(6) DEFAULT NULL,
  `attendanceId` bigint(20) DEFAULT NULL,
  `attendanceTime` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dwlogId` bigint(20) DEFAULT NULL,
  `endposition` varchar(255) DEFAULT NULL,
  `inTime` datetime(6) DEFAULT NULL,
  `outTime` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `biomast_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg4s6ip069qko7pqnf6dve696o` (`biomast_id`),
  CONSTRAINT `FKg4s6ip069qko7pqnf6dve696o` FOREIGN KEY (`biomast_id`) REFERENCES `biomast` (`sno`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `day`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `day` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `day_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `day_end_closing_branch_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `day_end_closing_branch_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `branch_name` varchar(255) DEFAULT NULL,
  `child_branch_id` bigint(20) DEFAULT NULL,
  `child_branch_name` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `day_end_stock_closing_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `day_end_stock_closing_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adate` date DEFAULT NULL,
  `bin_stock` double DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `physical_stock` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `today_closing` double DEFAULT NULL,
  `today_production` double DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `yield_summary` double DEFAULT NULL,
  `yield_summary_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `day_salary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `day_salary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allowance` double DEFAULT NULL,
  `basic` double DEFAULT NULL,
  `check_in` datetime(6) DEFAULT NULL,
  `check_out` datetime(6) DEFAULT NULL,
  `contract` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `da` double DEFAULT NULL,
  `dacatid` bigint(20) DEFAULT NULL,
  `designation_id` bigint(20) DEFAULT NULL,
  `designation_name` varchar(255) DEFAULT NULL,
  `emp_id` bigint(20) DEFAULT NULL,
  `employee_name` varchar(255) DEFAULT NULL,
  `experience` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `joining_date` varchar(255) DEFAULT NULL,
  `luzone` bigint(20) DEFAULT NULL,
  `pshift` bigint(20) DEFAULT NULL,
  `per_day_total` double DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  `shift_type` varchar(255) DEFAULT NULL,
  `shift_date` date DEFAULT NULL,
  `total_working_hrs` varchar(255) DEFAULT NULL,
  `worked_branch` bigint(20) DEFAULT NULL,
  `worked_dept` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `delitestock_moverequest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `delitestock_moverequest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activated_by` bigint(20) DEFAULT NULL,
  `activated_on` date DEFAULT NULL,
  `activation_status` varchar(255) DEFAULT NULL,
  `completed_date` datetime(6) DEFAULT NULL,
  `destination_bin_no` bigint(20) DEFAULT NULL,
  `firm_Id` bigint(20) DEFAULT NULL,
  `from_branch_id` bigint(20) DEFAULT NULL,
  `from_branch_name` varchar(255) DEFAULT NULL,
  `from_dept_id` bigint(20) DEFAULT NULL,
  `from_dept_name` varchar(255) DEFAULT NULL,
  `gid` bigint(20) DEFAULT NULL,
  `kalasal_from_date` date DEFAULT NULL,
  `kalasal_to_date` date DEFAULT NULL,
  `material_request_id` bigint(20) DEFAULT NULL,
  `request_date` datetime(6) DEFAULT NULL,
  `to_branch_id` bigint(20) DEFAULT NULL,
  `to_branch_name` varchar(255) DEFAULT NULL,
  `to_dept_id` bigint(20) DEFAULT NULL,
  `to_dept_name` varchar(255) DEFAULT NULL,
  `to_gid` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `delitestock_moverequest_bin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `delitestock_moverequest_bin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `binstock_id` bigint(20) DEFAULT NULL,
  `cas_id` bigint(20) DEFAULT NULL,
  `delitestock_moverequest_id` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `ref_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `department_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `department_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` varchar(255) DEFAULT NULL,
  `contract` varchar(255) DEFAULT NULL,
  `costreport` varchar(255) DEFAULT NULL,
  `milluse` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `productivity` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `pv` varchar(255) DEFAULT NULL,
  `reminders` varchar(255) DEFAULT NULL,
  `resource` varchar(255) DEFAULT NULL,
  `trnprefix` varchar(255) DEFAULT NULL,
  `utilisation` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgn1ygso9bsnhrfpvm4u6ph5b8` (`org_id`),
  CONSTRAINT `FKgn1ygso9bsnhrfpvm4u6ph5b8` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `depaymaster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `depaymaster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allowance` double DEFAULT NULL,
  `bank` varchar(255) DEFAULT NULL,
  `bank_ifsc` varchar(255) DEFAULT NULL,
  `bankacno` varchar(255) DEFAULT NULL,
  `basic` double DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `bioid` bigint(20) DEFAULT NULL,
  `branch` varchar(255) DEFAULT NULL,
  `cby` int(11) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `contract` varchar(255) DEFAULT NULL,
  `cshiftid` int(11) DEFAULT NULL,
  `da` double DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `depfper` double DEFAULT NULL,
  `desigid` double DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `desiper` double DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `dispen` varchar(255) DEFAULT NULL,
  `edate` varchar(255) DEFAULT NULL,
  `eepf1per` double DEFAULT NULL,
  `eepf2per` double DEFAULT NULL,
  `eesiper` double DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `entrymode` varchar(255) DEFAULT NULL,
  `epfbase` double DEFAULT NULL,
  `epfexempted` varchar(255) DEFAULT NULL,
  `epfno` varchar(255) DEFAULT NULL,
  `eshiftid` int(11) DEFAULT NULL,
  `esiceil` double DEFAULT NULL,
  `esiexemp` varchar(255) DEFAULT NULL,
  `esino` varchar(255) DEFAULT NULL,
  `fathername` varchar(255) DEFAULT NULL,
  `firm` varchar(255) DEFAULT NULL,
  `fixshift` varchar(255) DEFAULT NULL,
  `fps` varchar(255) DEFAULT NULL,
  `jdate` datetime(6) DEFAULT NULL,
  `lefton` varchar(255) DEFAULT NULL,
  `lshiftid` int(11) DEFAULT NULL,
  `ltrnweek` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `natWork` varchar(255) DEFAULT NULL,
  `nat_wk1` varchar(255) DEFAULT NULL,
  `nshift` varchar(255) DEFAULT NULL,
  `nshiftid` int(11) DEFAULT NULL,
  `nwoff` varchar(255) DEFAULT NULL,
  `oallow` double DEFAULT NULL,
  `oda` double DEFAULT NULL,
  `otot` double DEFAULT NULL,
  `owoff` varchar(255) DEFAULT NULL,
  `scode` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tds` double DEFAULT NULL,
  `transferon` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `woff` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `depreciation_category_name_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `depreciation_category_name_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `depreciation_name` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `designation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `designation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `orderlevel` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `detailed_reason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `detailed_reason` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `offreason` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `direct_purchase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `direct_purchase` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `approved__date` datetime(6) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `authorised_by` bigint(20) DEFAULT NULL,
  `authorised_date` datetime(6) DEFAULT NULL,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `bill_amount` double DEFAULT NULL,
  `bill_date` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `credit_or_debit` varchar(255) DEFAULT NULL,
  `eTransfer` varchar(255) DEFAULT NULL,
  `excess_shortage` bit(1) DEFAULT NULL,
  `firm_org_id` bigint(20) DEFAULT NULL,
  `inward_date` datetime(6) DEFAULT NULL,
  `lorry_date` datetime(6) DEFAULT NULL,
  `lorry_freight_amount` double DEFAULT NULL,
  `product_category` varchar(255) DEFAULT NULL,
  `reference_no` varchar(255) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `rejected_date` datetime(6) DEFAULT NULL,
  `stock_status` varchar(255) DEFAULT NULL,
  `storage_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `camp_id` bigint(20) DEFAULT NULL,
  `hand_over_to_dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfxwsab4fhtrxpm2yap36ivp1h` (`branch_id`),
  KEY `FKdyxfru28b1cxq7sv9d4gi6h3s` (`camp_id`),
  KEY `FKio7ce7cm3ktlhal626pognf77` (`hand_over_to_dept_id`),
  CONSTRAINT `FKdyxfru28b1cxq7sv9d4gi6h3s` FOREIGN KEY (`camp_id`) REFERENCES `camp` (`id`),
  CONSTRAINT `FKfxwsab4fhtrxpm2yap36ivp1h` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKio7ce7cm3ktlhal626pognf77` FOREIGN KEY (`hand_over_to_dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `direct_purchase_stored_bin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `direct_purchase_stored_bin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allotted_quantity` double DEFAULT NULL,
  `bin_id` bigint(20) DEFAULT NULL,
  `bin_no` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `entry_product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `direct_purchase_stored_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `direct_purchase_stored_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) DEFAULT NULL,
  `batch_No` varchar(255) DEFAULT NULL,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `billamount` double DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `entry_id` bigint(20) DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_type` bigint(20) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `package_type` varchar(255) DEFAULT NULL,
  `per_bag_weight` varchar(255) DEFAULT NULL,
  `po_id` bigint(20) DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  `total_quantity` varchar(255) DEFAULT NULL,
  `unit_cost` varchar(255) DEFAULT NULL,
  `unit_rate` varchar(255) DEFAULT NULL,
  `warranty_Date` datetime(6) DEFAULT NULL,
  `warranty_certificate` varchar(255) DEFAULT NULL,
  `color_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5du5oi1da4uwx8d4u9trxpc5t` (`color_id`),
  CONSTRAINT `FK5du5oi1da4uwx8d4u9trxpc5t` FOREIGN KEY (`color_id`) REFERENCES `color` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_against_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_against_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bill_date` datetime(6) DEFAULT NULL,
  `billnumber` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dispatch_against_bill_value` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_name` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdu1wup5hfi125f49hb0a8xcud` (`branch_id`),
  KEY `FKhbig6tab1ncicukxshl8dhfoi` (`user_id`),
  CONSTRAINT `FKdu1wup5hfi125f49hb0a8xcud` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKhbig6tab1ncicukxshl8dhfoi` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_against_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_against_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dispatch_Against_Order_Value` varchar(255) DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `order_number` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_name` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdxdm6upr8kpppe819t9vpag4y` (`branch_id`),
  KEY `FK37gmk8dc8ek7rnjtdmtoyj5xi` (`user_id`),
  CONSTRAINT `FK37gmk8dc8ek7rnjtdmtoyj5xi` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKdxdm6upr8kpppe819t9vpag4y` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_driver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_driver` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `name` varchar(128) NOT NULL,
  `dl_number` varchar(32) NOT NULL,
  `dl_expiry` date DEFAULT NULL,
  `primary_mobile` varchar(16) NOT NULL,
  `alt_mobile` varchar(16) DEFAULT NULL,
  `preferred_language` varchar(4) NOT NULL DEFAULT 'ta',
  `aadhaar_last4` varchar(4) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `emergency_contact_name` varchar(255) DEFAULT NULL,
  `shift_pref` varchar(255) DEFAULT NULL,
  `aadhaar_full` varchar(20) DEFAULT NULL,
  `pan_no` varchar(16) DEFAULT NULL,
  `photo_url` varchar(512) DEFAULT NULL,
  `default_vehicle_id` bigint(20) DEFAULT NULL,
  `dl_class` varchar(16) DEFAULT NULL,
  `date_of_joining` date DEFAULT NULL,
  `salary_type` varchar(16) DEFAULT NULL,
  `salary_amount` decimal(10,2) DEFAULT NULL,
  `current_address` varchar(512) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `emergency_notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_drv_tenant_dl` (`tenant_id`,`dl_number`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_gate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_gate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(16) NOT NULL,
  `name` varchar(64) NOT NULL,
  `gate_type` varchar(16) NOT NULL,
  `scanner_workerless` tinyint(1) NOT NULL DEFAULT 1,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gate_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_lane`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_lane` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_code` varchar(16) NOT NULL,
  `code` varchar(16) NOT NULL,
  `direction` varchar(8) NOT NULL DEFAULT 'IN',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lane_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_order_withprice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_order_withprice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `broker_name` varchar(255) DEFAULT NULL,
  `cess` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dispatch_Order_With_Price_Value` varchar(255) DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `order_number` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_name` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhh7a5epusvd218c52edqekome` (`branch_id`),
  KEY `FK4kxftnotyu0js034hlnoq42eg` (`user_id`),
  CONSTRAINT `FK4kxftnotyu0js034hlnoq42eg` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKhh7a5epusvd218c52edqekome` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_parking_bay`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_parking_bay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_code` varchar(16) NOT NULL,
  `code` varchar(16) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 1,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bay_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_parking_bay_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_parking_bay_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_no` varchar(32) NOT NULL,
  `in_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `out_time` timestamp NULL DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `vehicle_no` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pbe_tenant_gp` (`tenant_id`,`gate_pass_no`),
  KEY `idx_pbe_in_time` (`in_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_tank_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_tank_location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(16) NOT NULL,
  `tank_capacity_kl` decimal(8,2) NOT NULL,
  `current_oil_grade` varchar(32) NOT NULL DEFAULT 'EMPTY',
  `min_headspace_kl` decimal(6,2) NOT NULL DEFAULT 5.00,
  `batch_density` decimal(6,4) DEFAULT NULL,
  `last_cleaned_on` date DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tank_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_vehicle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `registration_no` varchar(32) NOT NULL,
  `vehicle_type` varchar(32) NOT NULL DEFAULT 'TANKER',
  `tanker_compartments` int(11) DEFAULT NULL,
  `tare_weight_kg` decimal(10,2) DEFAULT NULL,
  `capacity_kg` decimal(10,2) DEFAULT NULL,
  `ownership` varchar(8) NOT NULL DEFAULT 'HIRED',
  `transporter_name` varchar(128) DEFAULT NULL,
  `rc_expiry` date DEFAULT NULL,
  `insurance_expiry` date DEFAULT NULL,
  `puc_expiry` date DEFAULT NULL,
  `tank_cleaning_cert_no` varchar(64) DEFAULT NULL,
  `tank_cleaning_cert_expiry` date DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `make` varchar(64) DEFAULT NULL,
  `model` varchar(64) DEFAULT NULL,
  `year_of_mfg` int(11) DEFAULT NULL,
  `fc_expiry` date DEFAULT NULL,
  `permit_no` varchar(64) DEFAULT NULL,
  `permit_expiry` date DEFAULT NULL,
  `insurance_policy_no` varchar(64) DEFAULT NULL,
  `insurance_sum_insured` decimal(12,2) DEFAULT NULL,
  `gps_device_id` varchar(64) DEFAULT NULL,
  `default_driver_id` bigint(20) DEFAULT NULL,
  `wheel_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_veh_tenant_reg` (`tenant_id`,`registration_no`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_vehicle_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_vehicle_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `workflow_instance_id` bigint(20) NOT NULL,
  `flow_variant_code` varchar(64) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  `ownership_type` varchar(16) DEFAULT NULL,
  `vehicle_no` varchar(32) DEFAULT NULL,
  `driver_name` varchar(128) DEFAULT NULL,
  `driver_phone` varchar(16) DEFAULT NULL,
  `broker_name` varchar(128) DEFAULT NULL,
  `invoice_no` varchar(64) DEFAULT NULL,
  `invoice_qty` decimal(12,3) DEFAULT NULL,
  `vendor_name` varchar(128) DEFAULT NULL,
  `oil_rm_item` varchar(128) DEFAULT NULL,
  `tanker_no` varchar(32) DEFAULT NULL,
  `po_verified` varchar(32) DEFAULT NULL,
  `po_override_reason` varchar(255) DEFAULT NULL,
  `dispatch_officer_name` varchar(128) DEFAULT NULL,
  `transport_name` varchar(128) DEFAULT NULL,
  `packing_fg_stock_details` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dvd_tenant_instance` (`tenant_id`,`workflow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dispatch_weighbridge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_weighbridge` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(16) NOT NULL,
  `capacity_kg` decimal(10,0) NOT NULL,
  `protocol` varchar(16) NOT NULL DEFAULT 'RS232',
  `frame` varchar(16) DEFAULT NULL,
  `device` varchar(64) DEFAULT NULL,
  `tolerance_kg` int(11) NOT NULL DEFAULT 20,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `name` varchar(128) DEFAULT NULL,
  `location` varchar(128) DEFAULT NULL,
  `make` varchar(64) DEFAULT NULL,
  `model` varchar(64) DEFAULT NULL,
  `serial_no` varchar(64) DEFAULT NULL,
  `tcp_ip` varchar(32) DEFAULT NULL,
  `tcp_port` int(11) DEFAULT NULL,
  `calibrated_on` date DEFAULT NULL,
  `calibration_cert_no` varchar(64) DEFAULT NULL,
  `next_calibration_due` date DEFAULT NULL,
  `default_operator_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wb_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `districts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `districts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `countryid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `stateid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dp_entry_proposal_param_inspection_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dp_entry_proposal_param_inspection_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `param_id` bigint(20) DEFAULT NULL,
  `param_score` int(11) DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dpatch_billorder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dpatch_billorder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `order_billdate` datetime(6) DEFAULT NULL,
  `order_billno` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dpatch_against_type_id` bigint(20) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `ordernumber` bigint(20) DEFAULT NULL,
  `order_status` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKko145h2xevgb4lu5jf6op79vr` (`created_by`),
  KEY `FKdvw6gt0eiqdwfgql38mp0t2o6` (`vendor_id`),
  CONSTRAINT `FKdvw6gt0eiqdwfgql38mp0t2o6` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKko145h2xevgb4lu5jf6op79vr` FOREIGN KEY (`created_by`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dpatch_billorder_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dpatch_billorder_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `boxquantity` double DEFAULT NULL,
  `lot_ref_id` bigint(20) DEFAULT NULL,
  `dpatch_bill_order_ref_id` bigint(20) DEFAULT NULL,
  `piecequantity` double DEFAULT NULL,
  `product_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKijeeygngr2qh3od3m0mlwicss` (`product_ref_id`),
  CONSTRAINT `FKijeeygngr2qh3od3m0mlwicss` FOREIGN KEY (`product_ref_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dpatch_contract_vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dpatch_contract_vehicle` (
  `dpatch_Contract_Vehicle_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cvehicle_Name` varchar(255) DEFAULT NULL,
  `cvehicle_Number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`dpatch_Contract_Vehicle_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dpatch_delivery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dpatch_delivery` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bigint(20) DEFAULT NULL,
  `created_Timestamp` datetime(6) DEFAULT NULL,
  `delivery_date` date DEFAULT NULL,
  `vehicle_Type_Id` bigint(20) DEFAULT NULL,
  `vehicle_no` varchar(255) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `despatch_id` bigint(20) DEFAULT NULL,
  `party_id` bigint(20) DEFAULT NULL,
  `vehicle_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlhfi6dpam6do6ollamw9lbbk6` (`created_By`),
  KEY `FKi855hdbd06h6b5usi0d86njrb` (`despatch_id`),
  KEY `FKkmcrjcy11tbwo0njlybx98dak` (`party_id`),
  KEY `FKp5h7h1ct9y8suc7bokb6awtdd` (`vehicle_id`),
  CONSTRAINT `FKi855hdbd06h6b5usi0d86njrb` FOREIGN KEY (`despatch_id`) REFERENCES `dpatch_despatch` (`id`),
  CONSTRAINT `FKkmcrjcy11tbwo0njlybx98dak` FOREIGN KEY (`party_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKlhfi6dpam6do6ollamw9lbbk6` FOREIGN KEY (`created_By`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKp5h7h1ct9y8suc7bokb6awtdd` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle_group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dpatch_despatch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dpatch_despatch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `trndate` datetime(6) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `order_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKef7otekb6rfy8uj3mtohnle4o` (`approved_by`),
  KEY `FK3y5gkyagt18ga7a1si5fk76yv` (`created_by`),
  KEY `FKglcno01lta1owkxyte4427gm1` (`order_ref_id`),
  CONSTRAINT `FK3y5gkyagt18ga7a1si5fk76yv` FOREIGN KEY (`created_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKef7otekb6rfy8uj3mtohnle4o` FOREIGN KEY (`approved_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKglcno01lta1owkxyte4427gm1` FOREIGN KEY (`order_ref_id`) REFERENCES `dpatch_billorder` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dpatch_items_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dpatch_items_batch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_By` bigint(20) DEFAULT NULL,
  `created_Timestamp` date DEFAULT NULL,
  `quanity` double DEFAULT NULL,
  `updated_Timestamp` date DEFAULT NULL,
  `batch_Ref_Id` bigint(20) DEFAULT NULL,
  `bin_Ref_Id` bigint(20) DEFAULT NULL,
  `binstock_Ref_Id` bigint(20) DEFAULT NULL,
  `dpatch_Despatch_Ref_Id` bigint(20) DEFAULT NULL,
  `dpatch_Items_Ref_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKedy2230twawp4d5v65jqldche` (`batch_Ref_Id`),
  KEY `FKipd69kntyblk009k4fu1u2qrl` (`bin_Ref_Id`),
  KEY `FKpygr8y8ylkcwstjvmr52jmhh3` (`binstock_Ref_Id`),
  KEY `FKqocvnkenah5qwil6n7rbahr59` (`dpatch_Despatch_Ref_Id`),
  KEY `FK3a1u60796u8hjrvb2cx4uqdi0` (`dpatch_Items_Ref_Id`),
  CONSTRAINT `FK3a1u60796u8hjrvb2cx4uqdi0` FOREIGN KEY (`dpatch_Items_Ref_Id`) REFERENCES `dpatch_billorder_items` (`id`),
  CONSTRAINT `FKedy2230twawp4d5v65jqldche` FOREIGN KEY (`batch_Ref_Id`) REFERENCES `batchmaster` (`id`),
  CONSTRAINT `FKipd69kntyblk009k4fu1u2qrl` FOREIGN KEY (`bin_Ref_Id`) REFERENCES `gogroup` (`binprimaryid`),
  CONSTRAINT `FKpygr8y8ylkcwstjvmr52jmhh3` FOREIGN KEY (`binstock_Ref_Id`) REFERENCES `binstock` (`id`),
  CONSTRAINT `FKqocvnkenah5qwil6n7rbahr59` FOREIGN KEY (`dpatch_Despatch_Ref_Id`) REFERENCES `dpatch_despatch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `duty_allotment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `duty_allotment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `shift` varchar(255) DEFAULT NULL,
  `shift_date` date DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  `transfer_id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8fm51et6b453u51ujelciqvh1` (`dept_id`),
  KEY `FKjhnyx2prr81sedb64adrkn62o` (`org_id`),
  CONSTRAINT `FK8fm51et6b453u51ujelciqvh1` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKjhnyx2prr81sedb64adrkn62o` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `duty_transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `duty_transfer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `shift` varchar(255) DEFAULT NULL,
  `shift_date` datetime(6) DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8b0mxglqvk8ixkbrivhac1i7u` (`dept_id`),
  KEY `FK6u8eays947mk5wvv7aettef5u` (`org_id`),
  CONSTRAINT `FK6u8eays947mk5wvv7aettef5u` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK8b0mxglqvk8ixkbrivhac1i7u` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `duty_transfer_assign_duty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `duty_transfer_assign_duty` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `emp_type` int(11) DEFAULT NULL,
  `transfer_id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `assign_branch_id` bigint(20) DEFAULT NULL,
  `assign_dept_id` bigint(20) DEFAULT NULL,
  `current_branch_id` bigint(20) DEFAULT NULL,
  `current_dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3cpcpxbyi5mqt7rbb2n562v8y` (`assign_branch_id`),
  KEY `FKfyl17rdtwfmrjkjt2yfsr3i2e` (`assign_dept_id`),
  KEY `FK7srndd9plhuv4d0h12o9ugvnf` (`current_branch_id`),
  KEY `FKsu9ml9guu7w8l7mj1u0al64tj` (`current_dept_id`),
  KEY `FK74wnsgl6cbjh3t0nas0he637x` (`org_id`),
  CONSTRAINT `FK3cpcpxbyi5mqt7rbb2n562v8y` FOREIGN KEY (`assign_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK74wnsgl6cbjh3t0nas0he637x` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK7srndd9plhuv4d0h12o9ugvnf` FOREIGN KEY (`current_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKfyl17rdtwfmrjkjt2yfsr3i2e` FOREIGN KEY (`assign_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKsu9ml9guu7w8l7mj1u0al64tj` FOREIGN KEY (`current_dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `duty_unplanned`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `duty_unplanned` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `duty_From` datetime(6) DEFAULT NULL,
  `dutyTo` datetime(6) DEFAULT NULL,
  `dutydescription` varchar(255) DEFAULT NULL,
  `dutyname` varchar(255) DEFAULT NULL,
  `employee_category` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhbe5res6ntmi1rx7b6kmo9ho9` (`dept_id`),
  KEY `FK8ml758jemjd2uhncc3p2tfn2h` (`user_id`),
  CONSTRAINT `FK8ml758jemjd2uhncc3p2tfn2h` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKhbe5res6ntmi1rx7b6kmo9ho9` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dwcluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dwcluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dwdaysum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dwdaysum` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allowance` bigint(20) DEFAULT NULL,
  `basic` bigint(20) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `ca` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `da` bigint(20) DEFAULT NULL,
  `desigid` bigint(20) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `expyears` bigint(20) DEFAULT NULL,
  `gwa` bigint(20) DEFAULT NULL,
  `hra` bigint(20) DEFAULT NULL,
  `intime` bigint(20) DEFAULT NULL,
  `jdate` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `oallow` bigint(20) DEFAULT NULL,
  `oda` bigint(20) DEFAULT NULL,
  `ototal` bigint(20) DEFAULT NULL,
  `outtime` bigint(20) DEFAULT NULL,
  `pshift` bigint(20) DEFAULT NULL,
  `pstatus` bigint(20) DEFAULT NULL,
  `salwkid` bigint(20) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `shiftdate` bigint(20) DEFAULT NULL,
  `shiftid` bigint(20) DEFAULT NULL,
  `total` bigint(20) DEFAULT NULL,
  `trnid` bigint(20) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trnweekid` bigint(20) DEFAULT NULL,
  `usecid` bigint(20) DEFAULT NULL,
  `wa` bigint(20) DEFAULT NULL,
  `amt` double DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `con` varchar(255) DEFAULT NULL,
  `shift` bigint(20) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `von` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dwlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dwlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `astatus` varchar(255) DEFAULT NULL,
  `atype` varchar(255) DEFAULT NULL,
  `attdatetime` datetime(6) DEFAULT NULL,
  `attstate` varchar(10) DEFAULT NULL,
  `bioid` bigint(20) NOT NULL,
  `date` date DEFAULT NULL,
  `slno` bigint(20) DEFAULT NULL,
  `sysip` varchar(20) DEFAULT NULL,
  `temp` double DEFAULT NULL,
  `ustatus` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `eb_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `eb_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `expense` bigint(20) DEFAULT NULL,
  `service_category` varchar(255) DEFAULT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `service_no` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4xfv4n9klmbj1pdb3qeeu6kjh` (`branch_id`),
  KEY `FKrejfbnovire3trnd6kecluaqb` (`created_by`),
  KEY `FKheti4116c78ujrf9r1o25xb43` (`firm_id`),
  KEY `FK7uceqc09vs41l2xy4qd166374` (`updated_by`),
  CONSTRAINT `FK4xfv4n9klmbj1pdb3qeeu6kjh` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK7uceqc09vs41l2xy4qd166374` FOREIGN KEY (`updated_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKheti4116c78ujrf9r1o25xb43` FOREIGN KEY (`firm_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKrejfbnovire3trnd6kecluaqb` FOREIGN KEY (`created_by`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `emast_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `emast_daily` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aadhaar_File` varchar(255) DEFAULT NULL,
  `aadhaar_Verified_By` bigint(20) DEFAULT NULL,
  `aadhaar_Verified_On` date DEFAULT NULL,
  `abry` int(11) DEFAULT NULL,
  `accountno` varchar(255) DEFAULT NULL,
  `act_category` varchar(255) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `adharno` varchar(255) DEFAULT NULL,
  `age` varchar(255) DEFAULT NULL,
  `allowance` varchar(255) DEFAULT NULL,
  `bankname` varchar(255) DEFAULT NULL,
  `basicsalary` varchar(255) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `bioid` bigint(20) DEFAULT NULL,
  `biostn` bigint(20) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `blood` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `bus` varchar(255) DEFAULT NULL,
  `caste` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `city` bigint(20) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `community` varchar(255) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `country` bigint(20) DEFAULT NULL,
  `cstatus` varchar(255) DEFAULT NULL,
  `dacatid` bigint(20) DEFAULT NULL,
  `deamessallowance` varchar(255) DEFAULT NULL,
  `describe` varchar(255) DEFAULT NULL,
  `desigid` bigint(20) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `district` bigint(20) DEFAULT NULL,
  `emergency_Contact_Person_Relationship` varchar(255) DEFAULT NULL,
  `emergencycontactno` varchar(255) DEFAULT NULL,
  `emergentcontactperson` varchar(255) DEFAULT NULL,
  `epfno` varchar(255) DEFAULT NULL,
  `esino` varchar(255) DEFAULT NULL,
  `fathername` varchar(255) DEFAULT NULL,
  `finals` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `goodworkallowance` varchar(255) DEFAULT NULL,
  `houserentallowance` varchar(255) DEFAULT NULL,
  `ifsccode` varchar(255) DEFAULT NULL,
  `joiningdate` date DEFAULT NULL,
  `lefton` date DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `maritalstatus` varchar(255) DEFAULT NULL,
  `member_address` varchar(255) DEFAULT NULL,
  `mobno` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nominee_dob` date DEFAULT NULL,
  `nominee_name` varchar(255) DEFAULT NULL,
  `nominee_relationship` varchar(255) DEFAULT NULL,
  `panno` varchar(255) DEFAULT NULL,
  `phoneno` varchar(255) DEFAULT NULL,
  `photo` varchar(255) DEFAULT NULL,
  `pictureheight` varchar(255) DEFAULT NULL,
  `picturename` varchar(255) DEFAULT NULL,
  `picturesize` varchar(255) DEFAULT NULL,
  `picturetype` varchar(255) DEFAULT NULL,
  `picturewidth` varchar(255) DEFAULT NULL,
  `quali` bigint(20) DEFAULT NULL,
  `qualification` varchar(255) DEFAULT NULL,
  `religion` varchar(255) DEFAULT NULL,
  `state` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `uanno` varchar(255) DEFAULT NULL,
  `upload_Spouse_Aadhar_Detail` varchar(255) DEFAULT NULL,
  `upload_Spouse_Bank_Detail` varchar(255) DEFAULT NULL,
  `upload_aadhar` varchar(255) DEFAULT NULL,
  `upload_bank_pass` varchar(255) DEFAULT NULL,
  `upload_pan` varchar(255) DEFAULT NULL,
  `upload_passportsize` varchar(255) DEFAULT NULL,
  `wages` bigint(20) DEFAULT NULL,
  `department` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8whosoog2rq3ppysclymo7h6d` (`department`),
  CONSTRAINT `FK8whosoog2rq3ppysclymo7h6d` FOREIGN KEY (`department`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `employee_dependency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_dependency` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aadharno` varchar(255) DEFAULT NULL,
  `dateofbirth` varchar(255) DEFAULT NULL,
  `education` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `relationship` varchar(255) DEFAULT NULL,
  `userid` bigint(20) DEFAULT NULL,
  `weddingdate` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `employee_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `abry` varchar(255) DEFAULT NULL,
  `aadhaarfile` varchar(255) DEFAULT NULL,
  `aadhaarverifiedby` bigint(20) DEFAULT NULL,
  `aadhaarverifiedon` date DEFAULT NULL,
  `aadharno` varchar(255) DEFAULT NULL,
  `accountno` varchar(255) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `allowance` varchar(255) DEFAULT NULL,
  `ar` varchar(255) DEFAULT NULL,
  `bankname` varchar(255) DEFAULT NULL,
  `basicsalary` varchar(255) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bioid` bigint(20) DEFAULT NULL,
  `biostn` varchar(255) DEFAULT NULL,
  `blood` bigint(20) DEFAULT NULL,
  `bloodgroupid` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `bus` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `cstatus` varchar(255) DEFAULT NULL,
  `dacatid` bigint(20) DEFAULT NULL,
  `dateofbirth` date DEFAULT NULL,
  `deamessallowance` varchar(255) DEFAULT NULL,
  `describe` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `emergency_contact_person_relationship` varchar(255) DEFAULT NULL,
  `emergencycontactno` varchar(255) DEFAULT NULL,
  `emergentcontactperson` varchar(255) DEFAULT NULL,
  `employeename` varchar(255) DEFAULT NULL,
  `employeetype` varchar(255) DEFAULT NULL,
  `epfno` varchar(255) DEFAULT NULL,
  `esino` varchar(255) DEFAULT NULL,
  `experience` varchar(255) DEFAULT NULL,
  `fathername` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `goodworkallowance` varchar(255) DEFAULT NULL,
  `houserentallowance` varchar(255) DEFAULT NULL,
  `ifsccode` varchar(255) DEFAULT NULL,
  `jobres` bigint(20) DEFAULT NULL,
  `joiningdate` date DEFAULT NULL,
  `lefton` date DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `locationpath` varchar(255) DEFAULT NULL,
  `maritalstatus` varchar(255) DEFAULT NULL,
  `memberaddress` varchar(255) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `panno` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `permenantstaffjoiningdate` date DEFAULT NULL,
  `phoneno` varchar(255) DEFAULT NULL,
  `photo` varchar(255) DEFAULT NULL,
  `pictureheight` varchar(255) DEFAULT NULL,
  `picturename` varchar(255) DEFAULT NULL,
  `picturesize` varchar(255) DEFAULT NULL,
  `picturetype` varchar(255) DEFAULT NULL,
  `picturewidth` varchar(255) DEFAULT NULL,
  `provisonalstaffjoiningdate` date DEFAULT NULL,
  `quali` bigint(20) DEFAULT NULL,
  `qualification` varchar(255) DEFAULT NULL,
  `roles_id` bigint(20) DEFAULT NULL,
  `room` varchar(255) DEFAULT NULL,
  `staffid` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `uanno` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `uploadaadhar` varchar(255) DEFAULT NULL,
  `uploadbankpass` varchar(255) DEFAULT NULL,
  `uploadpan` varchar(255) DEFAULT NULL,
  `uploadpassportsize` varchar(255) DEFAULT NULL,
  `uploadspouseaadhardetail` varchar(255) DEFAULT NULL,
  `uploadspousebankdetail` varchar(255) DEFAULT NULL,
  `userlevel` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `weddingday` date DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `desi_id` bigint(20) DEFAULT NULL,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `FKt1xsjdomintwqx3bv7m73auel` (`dept_id`),
  KEY `FKc8px2hrhtdb9x0oh7u0iy6xs8` (`desi_id`),
  KEY `idx_emp_tenant` (`tenant_id`),
  CONSTRAINT `FKc8px2hrhtdb9x0oh7u0iy6xs8` FOREIGN KEY (`desi_id`) REFERENCES `designation` (`id`),
  CONSTRAINT `FKt1xsjdomintwqx3bv7m73auel` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2018 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `employee_leave`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_leave` (
  `emp_id` bigint(20) NOT NULL,
  `approve_date` date DEFAULT NULL,
  `earn_leave_count` int(11) DEFAULT NULL,
  `remaining_leave_count` int(11) DEFAULT NULL,
  `updated_date` date DEFAULT NULL,
  PRIMARY KEY (`emp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `employee_qualification_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee_qualification_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `emp_id` bigint(20) DEFAULT NULL,
  `qualification_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `entity_field_registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `entity_field_registry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT -1,
  `entity_code` varchar(128) NOT NULL,
  `field_code` varchar(128) NOT NULL,
  `display_label` varchar(256) NOT NULL,
  `data_type` enum('STRING','INT','LONG','DECIMAL','BOOLEAN','DATE','DATETIME','TIME','TEXT','JSON','ENUM','RELATION','COMPUTED') NOT NULL,
  `filterable` tinyint(1) NOT NULL DEFAULT 1,
  `sortable` tinyint(1) NOT NULL DEFAULT 1,
  `searchable` tinyint(1) NOT NULL DEFAULT 0,
  `required` tinyint(1) NOT NULL DEFAULT 0,
  `default_value` text DEFAULT NULL,
  `options_source_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`options_source_json`)),
  `relation_entity` varchar(128) DEFAULT NULL,
  `widget_kind` varchar(32) DEFAULT NULL COMMENT 'text|number|date|datetime|textarea|select|multiselect|entity-ref|checkbox|currency|chip',
  `options_json` text DEFAULT NULL COMMENT 'For select: ["A","B"] or [{label,value}]. For entity-ref: {entity, valueField, labelField}',
  `visible_when` text DEFAULT NULL COMMENT 'JS-lite expr, e.g. #form.decision == ''REJECTED''',
  `required_when` text DEFAULT NULL COMMENT 'JS-lite expr; when true, field is required',
  `computed_from` text DEFAULT NULL COMMENT 'JS-lite expr, e.g. #form.gross_kg - #form.tare_kg',
  `hidden` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_field` (`tenant_id`,`entity_code`,`field_code`),
  CONSTRAINT `fk_efr_entity` FOREIGN KEY (`tenant_id`, `entity_code`) REFERENCES `entity_registry` (`tenant_id`, `code`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `entity_registry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `entity_registry` (
  `tenant_id` bigint(20) NOT NULL DEFAULT -1,
  `code` varchar(128) NOT NULL,
  `display_name` varchar(256) NOT NULL,
  `table_name` varchar(128) NOT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(64) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `read_role` varchar(64) DEFAULT NULL,
  `write_role` varchar(64) DEFAULT NULL,
  `approver_role` varchar(64) DEFAULT NULL,
  `value_column` varchar(100) DEFAULT NULL COMMENT 'Column used as the VALUE for dropdown options (e.g. id or code)',
  `display_column` varchar(100) DEFAULT NULL COMMENT 'Column used as the LABEL for dropdown options (e.g. name, title)',
  `lifecycle_kind` varchar(16) DEFAULT NULL,
  `workflow_code` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`tenant_id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `epaymaster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `epaymaster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aleave` int(11) DEFAULT NULL,
  `alsalary` int(11) DEFAULT NULL,
  `bank_ifsc` varchar(255) DEFAULT NULL,
  `bankacno` bigint(20) DEFAULT NULL,
  `bankcode` varchar(255) DEFAULT NULL,
  `basic` int(11) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `bioid` bigint(20) DEFAULT NULL,
  `blsalary` int(11) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `changedon` datetime(6) DEFAULT NULL,
  `chby` bigint(20) DEFAULT NULL,
  `chstatus` varchar(255) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `da` int(11) DEFAULT NULL,
  `dacatid` bigint(20) DEFAULT NULL,
  `depfper` int(11) DEFAULT NULL,
  `desigid` bigint(20) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `desiper` int(11) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `dispen` varchar(255) DEFAULT NULL,
  `edate` datetime(6) DEFAULT NULL,
  `eepf1per` int(11) DEFAULT NULL,
  `eepf2per` int(11) DEFAULT NULL,
  `eesi` int(11) DEFAULT NULL,
  `eesiper` int(11) DEFAULT NULL,
  `eexem` varchar(255) DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `epf` int(11) DEFAULT NULL,
  `epf1` int(11) DEFAULT NULL,
  `epf2` int(11) DEFAULT NULL,
  `epfbase` int(11) DEFAULT NULL,
  `epfexem` varchar(255) DEFAULT NULL,
  `epfno` varchar(255) DEFAULT NULL,
  `esi` int(11) DEFAULT NULL,
  `esiceil` bigint(20) DEFAULT NULL,
  `esiexem` varchar(255) DEFAULT NULL,
  `esino` bigint(20) DEFAULT NULL,
  `firm` varchar(255) DEFAULT NULL,
  `flamt` varchar(255) DEFAULT NULL,
  `fleave` int(11) DEFAULT NULL,
  `flsalary` int(11) DEFAULT NULL,
  `fpmode` varchar(255) DEFAULT NULL,
  `fps` varchar(255) DEFAULT NULL,
  `gacode` varchar(255) DEFAULT NULL,
  `gleave` int(11) DEFAULT NULL,
  `gperm` int(11) DEFAULT NULL,
  `hday` int(11) DEFAULT NULL,
  `hra` int(11) DEFAULT NULL,
  `jdate` datetime(6) DEFAULT NULL,
  `lincentive` int(11) DEFAULT NULL,
  `lnleave` int(11) DEFAULT NULL,
  `lpaid` datetime(6) DEFAULT NULL,
  `lsalary` int(11) DEFAULT NULL,
  `lsmf` int(11) DEFAULT NULL,
  `lstatus` varchar(255) DEFAULT NULL,
  `mldays` int(11) DEFAULT NULL,
  `mlpay` int(11) DEFAULT NULL,
  `mlsalary` int(11) DEFAULT NULL,
  `month` varchar(255) DEFAULT NULL,
  `msdays` int(11) DEFAULT NULL,
  `mwdays` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `natWork` varchar(255) DEFAULT NULL,
  `netsalary` int(11) DEFAULT NULL,
  `obasic` int(11) DEFAULT NULL,
  `oda` int(11) DEFAULT NULL,
  `oda1` int(11) DEFAULT NULL,
  `ohra1` int(11) DEFAULT NULL,
  `payon` varchar(255) DEFAULT NULL,
  `perdayamount` double DEFAULT NULL,
  `phleave` int(11) DEFAULT NULL,
  `pleave` int(11) DEFAULT NULL,
  `scode` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tabsent` int(11) DEFAULT NULL,
  `tdeduction` int(11) DEFAULT NULL,
  `tds` int(11) DEFAULT NULL,
  `texp` int(11) DEFAULT NULL,
  `tlate` int(11) DEFAULT NULL,
  `tleave` int(11) DEFAULT NULL,
  `tnolate` int(11) DEFAULT NULL,
  `tnoleave` int(11) DEFAULT NULL,
  `tnonil` int(11) DEFAULT NULL,
  `tnoperm` int(11) DEFAULT NULL,
  `tot` int(11) DEFAULT NULL,
  `totleave` int(11) DEFAULT NULL,
  `tperm` int(11) DEFAULT NULL,
  `tpresent` int(11) DEFAULT NULL,
  `trnyear` bigint(20) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `year` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `epaymasternew`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `epaymasternew` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` varchar(255) DEFAULT NULL,
  `allowance` double DEFAULT NULL,
  `bank_ifsc` varchar(255) DEFAULT NULL,
  `bankacno` bigint(20) DEFAULT NULL,
  `bankcode` varchar(255) DEFAULT NULL,
  `basic` double DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `bioid` bigint(20) DEFAULT NULL,
  `ca` double DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `connect_table` varchar(255) DEFAULT NULL,
  `contract` varchar(255) DEFAULT NULL,
  `da` double DEFAULT NULL,
  `dacatid` bigint(20) DEFAULT NULL,
  `depfper` double DEFAULT NULL,
  `desigid` bigint(20) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `desiper` double DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `dispen` varchar(255) DEFAULT NULL,
  `eepf1per` double DEFAULT NULL,
  `eepf2per` double DEFAULT NULL,
  `eesi` double DEFAULT NULL,
  `eesiper` double DEFAULT NULL,
  `eexem` varchar(255) DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `epf` double DEFAULT NULL,
  `epf1` double DEFAULT NULL,
  `epf2` double DEFAULT NULL,
  `epfexem` varchar(255) DEFAULT NULL,
  `epfno` varchar(255) DEFAULT NULL,
  `esi` double DEFAULT NULL,
  `esiexem` varchar(255) DEFAULT NULL,
  `esino` varchar(255) DEFAULT NULL,
  `firm` varchar(255) DEFAULT NULL,
  `fpmode` varchar(255) DEFAULT NULL,
  `fps` varchar(255) DEFAULT NULL,
  `gwa` double DEFAULT NULL,
  `hra` double DEFAULT NULL,
  `jdate` date DEFAULT NULL,
  `lincentive` double DEFAULT NULL,
  `lpaid` varchar(255) DEFAULT NULL,
  `lsalary` double DEFAULT NULL,
  `mldays` double DEFAULT NULL,
  `mlpay` double DEFAULT NULL,
  `mlsalary` double DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `mwdays` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nat_work` varchar(255) DEFAULT NULL,
  `netsalary` double DEFAULT NULL,
  `oc1` varchar(255) DEFAULT NULL,
  `oc2` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tdeduction` double DEFAULT NULL,
  `tds` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `trnyear` varchar(255) DEFAULT NULL,
  `wa` double DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `epf_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `epf_voucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `PMRPY_Benefit` varchar(255) DEFAULT NULL,
  `apby` bigint(20) DEFAULT NULL,
  `apon` datetime(6) DEFAULT NULL,
  `approve` varchar(255) DEFAULT NULL,
  `auby` bigint(20) DEFAULT NULL,
  `auon` datetime(6) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `dsal` varchar(255) DEFAULT NULL,
  `hoprocess` varchar(255) DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `oasal` varchar(255) DEFAULT NULL,
  `staffsal` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tstaffsal` varchar(255) DEFAULT NULL,
  `upby` bigint(20) DEFAULT NULL,
  `upon` datetime(6) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `von` datetime(6) DEFAULT NULL,
  `vouno` varchar(255) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `esi_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `esi_voucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `auby` bigint(20) DEFAULT NULL,
  `auon` datetime(6) DEFAULT NULL,
  `auth` varchar(255) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `dby` bigint(20) DEFAULT NULL,
  `desi` double DEFAULT NULL,
  `don` datetime(6) DEFAULT NULL,
  `dverify` varchar(255) DEFAULT NULL,
  `eesi` double DEFAULT NULL,
  `examount` double DEFAULT NULL,
  `exnoe` varchar(255) DEFAULT NULL,
  `exparticulars` varchar(255) DEFAULT NULL,
  `hoprocess` varchar(255) DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `nettotal` double DEFAULT NULL,
  `noe` varchar(255) DEFAULT NULL,
  `particulars` varchar(255) DEFAULT NULL,
  `pby` bigint(20) DEFAULT NULL,
  `pon` datetime(6) DEFAULT NULL,
  `pverify` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tesi` double DEFAULT NULL,
  `tnoe` bigint(20) DEFAULT NULL,
  `upby` bigint(20) DEFAULT NULL,
  `upon` datetime(6) DEFAULT NULL,
  `vouno` varchar(255) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `excess_shortage_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `excess_shortage_products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_No` varchar(255) DEFAULT NULL,
  `bin` varchar(255) DEFAULT NULL,
  `bin_Quantity` varchar(255) DEFAULT NULL,
  `excess_Or_Shortage` varchar(255) DEFAULT NULL,
  `excess_Storage_Id` bigint(20) DEFAULT NULL,
  `is_Select` char(1) DEFAULT 'N',
  `lot` varchar(255) DEFAULT NULL,
  `product_Code` varchar(255) DEFAULT NULL,
  `product_Type` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `trn_Type` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `product_master_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh3kuh1r6rwdpig1kgaapr1jgl` (`product_master_id`),
  CONSTRAINT `FKh3kuh1r6rwdpig1kgaapr1jgl` FOREIGN KEY (`product_master_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `excess_stored_bin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `excess_stored_bin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `current_stock` double DEFAULT NULL,
  `excess_or_shortage` double DEFAULT NULL,
  `excess_product_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `existing_page_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `existing_page_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `page_created_date` datetime(6) DEFAULT NULL,
  `page_name` varchar(255) DEFAULT NULL,
  `page_type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `page_created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc46y5rhhjg3419gd6niviwu9n` (`page_created_by`),
  CONSTRAINT `FKc46y5rhhjg3419gd6niviwu9n` FOREIGN KEY (`page_created_by`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `expense_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `expense_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` bit(1) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_hpruiq6k2y3j2km2hfta8pr9n` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cost` double DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `itype` varchar(255) DEFAULT NULL,
  `lot` int(11) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `rname` varchar(255) DEFAULT NULL,
  `rtype` varchar(255) DEFAULT NULL,
  `spec` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `expgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `expgroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expId` bigint(20) DEFAULT NULL,
  `secExpId` bigint(20) DEFAULT NULL,
  `secExpName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `expvoucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `expvoucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contype` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `expId` bigint(20) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `vouId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `external_godown_bindetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `external_godown_bindetail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_No` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `godown_external_id` bigint(20) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `qty` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fgbins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fgbins` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) DEFAULT NULL,
  `binname` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fgmdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fgmdata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apds` int(11) DEFAULT NULL,
  `bid` int(11) DEFAULT NULL,
  `created_by` bigint(20) NOT NULL,
  `itemorder` int(11) DEFAULT NULL,
  `ms` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `pds` int(11) DEFAULT NULL,
  `printOrder` int(11) NOT NULL,
  `refid` bigint(20) NOT NULL,
  `refid1` bigint(20) DEFAULT NULL,
  `ss` varchar(255) NOT NULL,
  `status` char(1) DEFAULT NULL,
  `storder` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fields_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fields_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `database_table` varchar(255) DEFAULT NULL,
  `field_data_type` varchar(255) DEFAULT NULL,
  `field_name` varchar(255) DEFAULT NULL,
  `field_Position` bigint(20) DEFAULT NULL,
  `field_size` varchar(255) DEFAULT NULL,
  `field_type` varchar(255) DEFAULT NULL,
  `new_Db_Table` varchar(255) DEFAULT NULL,
  `page_Id` bigint(20) DEFAULT NULL,
  `possible_data_type` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `filmproductvalidity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `filmproductvalidity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `locationtype` varchar(255) DEFAULT NULL,
  `bestbeforeinmonths` bigint(20) DEFAULT NULL,
  `productgroupid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finclose`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `finclose` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conversion` double DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `gParentId` bigint(20) DEFAULT NULL,
  `parentId` bigint(20) DEFAULT NULL,
  `refId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finclose1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `finclose1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdBy` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parentGroup` bigint(20) DEFAULT NULL,
  `parentId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finclose2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `finclose2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finger_print`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `finger_print` (
  `user_id` bigint(20) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `secure_key` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finished_goods_mapping_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `finished_goods_mapping_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apds` bigint(20) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `itemorder` bigint(20) DEFAULT NULL,
  `ms` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pds` bigint(20) DEFAULT NULL,
  `printOrder` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `refid1` bigint(20) DEFAULT NULL,
  `ss` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `storder` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `food_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `food_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `food_name` varchar(255) DEFAULT NULL,
  `measuring_type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fumigation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fumigation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `area` varchar(255) DEFAULT NULL,
  `bags` varchar(255) DEFAULT NULL,
  `bin_no` varchar(255) DEFAULT NULL,
  `breadth` varchar(255) DEFAULT NULL,
  `consumption` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `fumigation_type` varchar(255) DEFAULT NULL,
  `last_Date` date DEFAULT NULL,
  `length` varchar(255) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `measure` varchar(255) DEFAULT NULL,
  `next_Date` date DEFAULT NULL,
  `qty` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_status` bit(1) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcv70d61mwbehio1idgoffnyov` (`dept_id`),
  KEY `FKkt63p7pkn3ch4q8l8p11vfg7p` (`user_id`),
  KEY `FKek4upgx4hu3e0aexmsavovxp7` (`godown_id`),
  KEY `FKdlucylikjw7ui46uhjw5vwwdp` (`vendor_id`),
  CONSTRAINT `FKcv70d61mwbehio1idgoffnyov` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKdlucylikjw7ui46uhjw5vwwdp` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKek4upgx4hu3e0aexmsavovxp7` FOREIGN KEY (`godown_id`) REFERENCES `godown` (`id`),
  CONSTRAINT `FKkt63p7pkn3ch4q8l8p11vfg7p` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `fumigation_scheduler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fumigation_scheduler` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fumigation_type` varchar(255) DEFAULT NULL,
  `last_date` date DEFAULT NULL,
  `next_date` date DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8ha9ieodiautrp8r9sutp3jv` (`godown_id`),
  CONSTRAINT `FK8ha9ieodiautrp8r9sutp3jv` FOREIGN KEY (`godown_id`) REFERENCES `godown` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gate_pass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_pass` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gp_number` varchar(32) NOT NULL,
  `flow_variant_code` varchar(64) NOT NULL,
  `vehicle_no` varchar(32) NOT NULL,
  `driver_name` varchar(128) DEFAULT NULL,
  `driver_phone` varchar(16) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `closed_at` timestamp NULL DEFAULT NULL,
  `total_unloading_qty` decimal(12,3) DEFAULT NULL,
  `manager_overrides_count` int(11) NOT NULL DEFAULT 0,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  `w1_weight_kg` decimal(10,3) DEFAULT NULL,
  `w2_weight_kg` decimal(10,3) DEFAULT NULL,
  `w3_weight_kg` decimal(10,3) DEFAULT NULL,
  `source_party` varchar(255) DEFAULT NULL,
  `reference_no` varchar(64) DEFAULT NULL,
  `variance_reason` varchar(512) DEFAULT NULL,
  `ordered_qty` decimal(12,3) DEFAULT NULL,
  `vehicle_type` varchar(32) DEFAULT NULL,
  `source_party_type` varchar(16) DEFAULT NULL,
  `loading_type` varchar(32) DEFAULT NULL,
  `broker_name` varchar(128) DEFAULT NULL,
  `invoice_no` varchar(64) DEFAULT NULL,
  `invoice_qty` decimal(12,3) DEFAULT NULL,
  `oil_rm_item` varchar(128) DEFAULT NULL,
  `tanker_no` varchar(32) DEFAULT NULL,
  `vehicle_category` varchar(32) DEFAULT NULL,
  `transport_name` varchar(128) DEFAULT NULL,
  `loading_supervisor` varchar(128) DEFAULT NULL,
  `loading_tank_location` varchar(64) DEFAULT NULL,
  `supervisor_name` varchar(128) DEFAULT NULL,
  `checking_officer` varchar(128) DEFAULT NULL,
  `qc_status` varchar(16) DEFAULT NULL,
  `qc_reject_reason` varchar(255) DEFAULT NULL,
  `weight_officer` varchar(128) DEFAULT NULL,
  `weight_entry_at` timestamp NULL DEFAULT NULL,
  `route_id` varchar(64) DEFAULT NULL,
  `loading_bay` varchar(16) DEFAULT NULL,
  `processed_qty` decimal(12,3) DEFAULT NULL,
  `remaining_qty` decimal(12,3) DEFAULT NULL,
  `dispatch_qty` decimal(12,3) DEFAULT NULL,
  `parking_in_at` timestamp NULL DEFAULT NULL,
  `parking_out_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gp_tenant_number` (`tenant_id`,`gp_number`),
  KEY `idx_gp_status` (`tenant_id`,`status`,`current_step`),
  KEY `idx_gp_wf_inst` (`workflow_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=426 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gate_pass_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_pass_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_id` bigint(20) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp(),
  `kind` varchar(32) NOT NULL,
  `from_status` varchar(32) DEFAULT NULL,
  `to_status` varchar(32) DEFAULT NULL,
  `page_code` varchar(128) DEFAULT NULL,
  `actor_user_id` bigint(20) DEFAULT NULL,
  `payload_json` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_gpe_gp_ts` (`gate_pass_id`,`ts`),
  KEY `idx_gpe_tenant_ts` (`tenant_id`,`ts`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gate_pass_qc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_pass_qc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_id` bigint(20) NOT NULL,
  `sample_id` varchar(64) DEFAULT NULL,
  `verdict` varchar(16) DEFAULT NULL,
  `parameters_json` longtext DEFAULT NULL,
  `drawn_by` bigint(20) DEFAULT NULL,
  `drawn_at` timestamp NULL DEFAULT NULL,
  `decided_by` bigint(20) DEFAULT NULL,
  `decided_at` timestamp NULL DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_gpqc_gp` (`gate_pass_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gate_pass_unloading`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_pass_unloading` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_id` bigint(20) NOT NULL,
  `bay_code` varchar(16) DEFAULT NULL,
  `tank_code` varchar(16) DEFAULT NULL,
  `item_code` varchar(64) DEFAULT NULL,
  `batch_no` varchar(64) DEFAULT NULL,
  `qty_kg` decimal(12,3) DEFAULT NULL,
  `started_at` timestamp NULL DEFAULT NULL,
  `closed_at` timestamp NULL DEFAULT NULL,
  `supervisor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_gpu_gp` (`gate_pass_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gate_pass_weighment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gate_pass_weighment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_id` bigint(20) NOT NULL,
  `weighbridge_code` varchar(16) NOT NULL,
  `reading_no` int(11) NOT NULL,
  `weight_kg` decimal(12,3) NOT NULL,
  `captured_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `captured_by` bigint(20) DEFAULT NULL,
  `source` varchar(16) NOT NULL DEFAULT 'MANUAL',
  `weighment_date` date DEFAULT NULL,
  `entry_time` time DEFAULT NULL,
  `weight_officer` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gpw_gp_reading` (`gate_pass_id`,`reading_no`),
  KEY `idx_gpw_wb` (`tenant_id`,`weighbridge_code`,`captured_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `generator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `generator` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `machine_id` varchar(255) DEFAULT NULL,
  `mf` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa817c9hv3jadm3j04r2oj09ly` (`created_by`),
  KEY `FKt10w7jgjvoish5s68qhgqpvub` (`updated_by`),
  CONSTRAINT `FKa817c9hv3jadm3j04r2oj09ly` FOREIGN KEY (`created_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKt10w7jgjvoish5s68qhgqpvub` FOREIGN KEY (`updated_by`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `goDownExternal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `goDownExternal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gdc_no` varchar(255) DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `dispatch_on` datetime(6) DEFAULT NULL,
  `eway_bill` varchar(255) DEFAULT NULL,
  `firm_Id` bigint(20) DEFAULT NULL,
  `from_val` varchar(255) DEFAULT NULL,
  `from_id` bigint(20) DEFAULT NULL,
  `from_name` varchar(255) DEFAULT NULL,
  `issue_mode` varchar(255) DEFAULT NULL,
  `issue_reference` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `to_val` varchar(255) DEFAULT NULL,
  `to_id` bigint(20) DEFAULT NULL,
  `to_name` varchar(255) DEFAULT NULL,
  `transport_mode` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `vehicle_name` varchar(255) DEFAULT NULL,
  `vehicle_type` varchar(255) DEFAULT NULL,
  `delivered_By` bigint(20) DEFAULT NULL,
  `from_branch_id` bigint(20) DEFAULT NULL,
  `from_dept_id` bigint(20) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `issue_by` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6n0r4o5pqd2rtg3033coakey0` (`delivered_By`),
  KEY `FKd8ir39mnwf7u43efup4gs30sn` (`from_branch_id`),
  KEY `FKqp7j05cwo0i1tnjg4kodjwo74` (`from_dept_id`),
  KEY `FK84usahp3f3hcwyy6tujyy3cfa` (`issue_branch_id`),
  KEY `FKgp7f97bpgj9lreseflm1qxs6x` (`issue_by`),
  KEY `FK4i8c8b6t3064er7fjeagabc1v` (`issue_dept_id`),
  CONSTRAINT `FK4i8c8b6t3064er7fjeagabc1v` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FK6n0r4o5pqd2rtg3033coakey0` FOREIGN KEY (`delivered_By`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK84usahp3f3hcwyy6tujyy3cfa` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKd8ir39mnwf7u43efup4gs30sn` FOREIGN KEY (`from_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKgp7f97bpgj9lreseflm1qxs6x` FOREIGN KEY (`issue_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKqp7j05cwo0i1tnjg4kodjwo74` FOREIGN KEY (`from_dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `go_down_product_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `go_down_product_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) DEFAULT NULL,
  `dispname` bigint(20) DEFAULT NULL,
  `gd_name` varchar(255) DEFAULT NULL,
  `gid` bigint(20) DEFAULT NULL,
  `month` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `go_down_report_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `go_down_report_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `RMC` varchar(255) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `dp` varchar(255) DEFAULT NULL,
  `iname` varchar(255) DEFAULT NULL,
  `le` varchar(255) DEFAULT NULL,
  `lr` varchar(255) DEFAULT NULL,
  `ls` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pr` varchar(255) DEFAULT NULL,
  `rname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `go_down_report_product_menu_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `go_down_report_product_menu_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `menu_id` bigint(20) DEFAULT NULL,
  `menu_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `godown`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `godown` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ho_supplier_id` bigint(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `contract_Time_Peroid` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `document_Path` varchar(255) DEFAULT NULL,
  `ebservice` bigint(20) DEFAULT NULL,
  `external` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `from_date` date DEFAULT NULL,
  `genset` varchar(255) DEFAULT NULL,
  `gensetid` bigint(20) DEFAULT NULL,
  `godown_capacity` varchar(255) DEFAULT NULL,
  `godowntype` varchar(255) DEFAULT NULL,
  `ledger_id` bigint(20) DEFAULT NULL,
  `licenseno` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `plant` bigint(20) DEFAULT NULL,
  `print_order` bigint(20) DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7dqwdkmg8mmek77qs99kjy9cq` (`org_id`),
  CONSTRAINT `FK7dqwdkmg8mmek77qs99kjy9cq` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gogroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gogroup` (
  `binprimaryid` bigint(20) NOT NULL AUTO_INCREMENT,
  `bintype` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `display` varchar(255) DEFAULT NULL,
  `export` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `gcode` varchar(255) DEFAULT NULL,
  `gid` bigint(20) DEFAULT NULL,
  `goname` bigint(20) DEFAULT NULL,
  `journal_bye` int(11) DEFAULT NULL,
  `packages` varchar(255) DEFAULT NULL,
  `rbin` varchar(255) DEFAULT NULL,
  `silo` int(11) DEFAULT NULL,
  `under` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`binprimaryid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gogroup_palletnumber_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gogroup_palletnumber_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bin_id` bigint(20) DEFAULT NULL,
  `pallet_number` varchar(255) DEFAULT NULL,
  `pallet_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `goods_receipt_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_receipt_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `grn_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `qty_received` decimal(12,3) NOT NULL,
  `qty_invoiced` decimal(12,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `stored_in_tank` varchar(16) DEFAULT NULL,
  `qc_status` varchar(16) DEFAULT NULL,
  `qc_notes` text DEFAULT NULL,
  `qty_accepted` decimal(12,3) DEFAULT NULL,
  `qty_rejected` decimal(12,3) DEFAULT NULL,
  `qty_variance` decimal(12,3) GENERATED ALWAYS AS (coalesce(`qty_received`,0) - coalesce(`qty_invoiced`,0)) STORED,
  `rate` decimal(12,2) DEFAULT NULL,
  `hsn_code` varchar(16) DEFAULT NULL,
  `batch_no` varchar(64) DEFAULT NULL,
  `mfg_date` date DEFAULT NULL,
  `exp_date` date DEFAULT NULL,
  `shelf_life_days` int(11) DEFAULT NULL,
  `bin_assigned` varchar(32) DEFAULT NULL,
  `pallet_no` varchar(32) DEFAULT NULL,
  `temperature_at_receipt` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_grnl_grn` (`grn_id`),
  KEY `idx_grnl_item` (`item_code`),
  CONSTRAINT `fk_grnl_grn` FOREIGN KEY (`grn_id`) REFERENCES `goods_receipt_note` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `goods_receipt_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_receipt_note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `grn_number` varchar(32) NOT NULL,
  `source_type` varchar(16) NOT NULL,
  `source_ref` varchar(128) DEFAULT NULL,
  `gate_pass_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  `vendor_name` varchar(160) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `received_at` timestamp NULL DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `po_number` varchar(32) DEFAULT NULL,
  `invoice_number` varchar(32) DEFAULT NULL,
  `invoice_date` date DEFAULT NULL,
  `invoice_value` decimal(14,2) DEFAULT NULL,
  `challan_number` varchar(32) DEFAULT NULL,
  `ewb_no` varchar(32) DEFAULT NULL,
  `vehicle_no` varchar(16) DEFAULT NULL,
  `vehicle_type` varchar(16) DEFAULT NULL,
  `driver_name` varchar(96) DEFAULT NULL,
  `driver_phone` varchar(16) DEFAULT NULL,
  `driver_dl_no` varchar(24) DEFAULT NULL,
  `transporter_name` varchar(96) DEFAULT NULL,
  `gate_in_time` datetime DEFAULT NULL,
  `gate_out_time` datetime DEFAULT NULL,
  `weighbridge_in_kg` decimal(12,2) DEFAULT NULL,
  `weighbridge_out_kg` decimal(12,2) DEFAULT NULL,
  `weighbridge_net_kg` decimal(12,2) GENERATED ALWAYS AS (coalesce(`weighbridge_in_kg`,0) - coalesce(`weighbridge_out_kg`,0)) STORED,
  `weighbridge_slip_no` varchar(32) DEFAULT NULL,
  `weighbridge_operator` varchar(64) DEFAULT NULL,
  `seal_no` varchar(32) DEFAULT NULL,
  `seal_intact` tinyint(1) DEFAULT NULL,
  `container_no` varchar(24) DEFAULT NULL,
  `pkg_count` int(11) DEFAULT NULL,
  `pkg_condition` varchar(16) DEFAULT NULL,
  `doc_invoice_recd` tinyint(1) DEFAULT NULL,
  `doc_lr_recd` tinyint(1) DEFAULT NULL,
  `doc_ewb_recd` tinyint(1) DEFAULT NULL,
  `doc_test_cert_recd` tinyint(1) DEFAULT NULL,
  `doc_msds_recd` tinyint(1) DEFAULT NULL,
  `unloading_started_at` datetime DEFAULT NULL,
  `unloading_completed_at` datetime DEFAULT NULL,
  `unloading_supervisor_emp_id` bigint(20) DEFAULT NULL,
  `unloading_remarks` text DEFAULT NULL,
  `qc_required` tinyint(1) NOT NULL DEFAULT 1,
  `qc_lab_tech` varchar(96) DEFAULT NULL,
  `qc_sample_drawn_at` datetime DEFAULT NULL,
  `qc_completed_at` datetime DEFAULT NULL,
  `qc_verdict` varchar(16) DEFAULT NULL,
  `qc_rejection_reason` varchar(256) DEFAULT NULL,
  `qc_certificate_no` varchar(32) DEFAULT NULL,
  `manager_override_required` tinyint(1) NOT NULL DEFAULT 0,
  `manager_override_reason` varchar(256) DEFAULT NULL,
  `manager_override_by_emp_id` bigint(20) DEFAULT NULL,
  `manager_override_at` datetime DEFAULT NULL,
  `stored_in_warehouse` varchar(32) DEFAULT NULL,
  `storage_complete_at` datetime DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_grn_number` (`tenant_id`,`grn_number`),
  KEY `idx_grn_status` (`tenant_id`,`status`,`current_step`),
  KEY `idx_grn_gp` (`gate_pass_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `gst_tax_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `gst_tax_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `created_Time_Stamp` datetime(6) DEFAULT NULL,
  `emaster_id` bigint(20) DEFAULT NULL,
  `hsn_Code` varchar(255) DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `item_Category` varchar(255) DEFAULT NULL,
  `items_values` varchar(255) DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ho_config_rest_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ho_config_rest_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_type` varchar(255) DEFAULT NULL,
  `created_time` datetime(6) DEFAULT NULL,
  `from_office` varchar(255) DEFAULT NULL,
  `json_data` varchar(255) DEFAULT NULL,
  `response` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `type_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ho_purchase_brokerage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ho_purchase_brokerage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authorized_by` varchar(255) DEFAULT NULL,
  `authorized_on` varchar(255) DEFAULT NULL,
  `bank_acc_id` bigint(20) DEFAULT NULL,
  `broker_id` bigint(20) DEFAULT NULL,
  `brokerage_amt` varchar(255) DEFAULT NULL,
  `brokerage_id` bigint(20) DEFAULT NULL,
  `camp_name` varchar(255) DEFAULT NULL,
  `cgst_amt` double DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `comp_br_id` bigint(20) DEFAULT NULL,
  `comp_id` bigint(20) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_on` varchar(255) DEFAULT NULL,
  `from_date` datetime(6) DEFAULT NULL,
  `gst_amt` double DEFAULT NULL,
  `igst_amt` double DEFAULT NULL,
  `inv_amt` double DEFAULT NULL,
  `net_amt` double DEFAULT NULL,
  `ref_date` varchar(255) DEFAULT NULL,
  `ref_no` varchar(255) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `sgst_amt` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `supplier_name` varchar(255) DEFAULT NULL,
  `tcs_amt` double DEFAULT NULL,
  `tcs_per` double DEFAULT NULL,
  `tds_amt` double DEFAULT NULL,
  `to_date` datetime(6) DEFAULT NULL,
  `total_bags` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ho_purchase_brokerage_breakup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ho_purchase_brokerage_breakup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `brokerage_amt` varchar(255) DEFAULT NULL,
  `brokerage_id` bigint(20) DEFAULT NULL,
  `brokerage_rate` varchar(255) DEFAULT NULL,
  `calculated_bags` varchar(255) DEFAULT NULL,
  `camp_id` bigint(20) DEFAULT NULL,
  `camp_name` varchar(255) DEFAULT NULL,
  `conv_factor` varchar(255) DEFAULT NULL,
  `item_group_id` bigint(20) DEFAULT NULL,
  `round_off` varchar(255) DEFAULT NULL,
  `total_bags` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ho_purchase_claim_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ho_purchase_claim_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actual` varchar(255) DEFAULT NULL,
  `calculated_amount` varchar(255) DEFAULT NULL,
  `cp_id` varchar(255) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_on` varchar(255) DEFAULT NULL,
  `difference_type` varchar(255) DEFAULT NULL,
  `ea_difference` varchar(255) DEFAULT NULL,
  `entered_amount` varchar(255) DEFAULT NULL,
  `expected` varchar(255) DEFAULT NULL,
  `factor` varchar(255) DEFAULT NULL,
  `grn_id` varchar(255) DEFAULT NULL,
  `item_id` varchar(255) DEFAULT NULL,
  `rate` varchar(255) DEFAULT NULL,
  `received_qty` varchar(255) DEFAULT NULL,
  `shortage_per_bag` varchar(255) DEFAULT NULL,
  `spl_ffa` varchar(255) DEFAULT NULL,
  `uom` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ho_purchase_claim_premium`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ho_purchase_claim_premium` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `approved_by` varchar(255) DEFAULT NULL,
  `approved_on` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `cgst` varchar(255) DEFAULT NULL,
  `claim` varchar(255) DEFAULT NULL,
  `cp_id` bigint(20) DEFAULT NULL,
  `factor` varchar(255) DEFAULT NULL,
  `igst` varchar(255) DEFAULT NULL,
  `item_group_id` bigint(20) DEFAULT NULL,
  `lorry_date` datetime(6) DEFAULT NULL,
  `lorry_slno` varchar(255) DEFAULT NULL,
  `net_amount` varchar(255) DEFAULT NULL,
  `premium` varchar(255) DEFAULT NULL,
  `ref_date` datetime(6) DEFAULT NULL,
  `ref_no` varchar(255) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `rounding` varchar(255) DEFAULT NULL,
  `sgst` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tax_percent` varchar(255) DEFAULT NULL,
  `trf_by` varchar(255) DEFAULT NULL,
  `trf_on` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ho_purchase_claim_premium_breakup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ho_purchase_claim_premium_breakup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actual` varchar(255) DEFAULT NULL,
  `cpb_id` varchar(255) DEFAULT NULL,
  `cpd_id` varchar(255) DEFAULT NULL,
  `expected` varchar(255) DEFAULT NULL,
  `factor` varchar(255) DEFAULT NULL,
  `qlty_id` varchar(255) DEFAULT NULL,
  `qty` varchar(255) DEFAULT NULL,
  `rate` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `holiday`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `holiday` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `holiday_type` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `year` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `holiday_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `holiday_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `day` varchar(255) DEFAULT NULL,
  `holiday_id` bigint(20) DEFAULT NULL,
  `holiday_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `htacharges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `htacharges` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cost` double DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `servid` bigint(20) DEFAULT NULL,
  `tmode` varchar(4) DEFAULT NULL,
  `tper` double DEFAULT NULL,
  `trntype` varchar(1) DEFAULT NULL,
  `ttype` varchar(1) DEFAULT NULL,
  `vname` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `htatm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `htatm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `eord` int(11) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `htatresults`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `htatresults` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `atname` varchar(50) DEFAULT NULL,
  `atresults` varchar(25) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `con` varchar(50) DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `panelid` bigint(20) DEFAULT NULL,
  `servid` bigint(20) DEFAULT NULL,
  `shiftdate` date DEFAULT NULL,
  `tsid` bigint(20) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `htdaytrn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `htdaytrn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `atname` varchar(50) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `con` varchar(50) DEFAULT NULL,
  `cunits` double DEFAULT NULL,
  `entposition` varchar(1) DEFAULT NULL,
  `eord` bigint(20) DEFAULT NULL,
  `mclosing` double DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `mopening` double DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `panelid` bigint(20) DEFAULT NULL,
  `resbid` bigint(20) DEFAULT NULL,
  `servid` bigint(20) DEFAULT NULL,
  `shiftdate` date DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `strday` varchar(50) DEFAULT NULL,
  `trnno` varchar(50) DEFAULT NULL,
  `tsid` bigint(20) DEFAULT NULL,
  `upatstatus` varchar(1) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `usecid` varchar(50) DEFAULT NULL,
  `vouid` bigint(20) DEFAULT NULL,
  `voutrnno` varchar(100) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `htmasters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `htmasters` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bcycle` int(11) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `dmandcost` double DEFAULT NULL,
  `dq` double DEFAULT NULL,
  `eq` double DEFAULT NULL,
  `expenseid` bigint(20) DEFAULT NULL,
  `md` double DEFAULT NULL,
  `mf` bigint(20) DEFAULT NULL,
  `mrent` double DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `panelid` bigint(20) DEFAULT NULL,
  `pmd` double DEFAULT NULL,
  `resbid` bigint(20) DEFAULT NULL,
  `tfid` bigint(20) DEFAULT NULL,
  `tfname` varchar(10) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `htmsummary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `htmsummary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `atname` varchar(50) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `con` varchar(50) DEFAULT NULL,
  `cunits` double DEFAULT NULL,
  `dispname` varchar(15) DEFAULT NULL,
  `entposition` varchar(1) DEFAULT NULL,
  `ldate` date DEFAULT NULL,
  `mclosing` double DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `mopening` double DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `panelid` bigint(20) DEFAULT NULL,
  `resbid` bigint(20) DEFAULT NULL,
  `servid` bigint(20) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `strmonth` varchar(50) DEFAULT NULL,
  `trnno` varchar(50) DEFAULT NULL,
  `tsid` bigint(20) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `usecid` varchar(50) DEFAULT NULL,
  `vouid` bigint(20) DEFAULT NULL,
  `voutrnno` varchar(100) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `httfgrp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `httfgrp` (
  `primaryid` bigint(20) NOT NULL AUTO_INCREMENT,
  `aper` double DEFAULT NULL,
  `climit` double DEFAULT NULL,
  `dper` double DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `mceiling` varchar(1) DEFAULT NULL,
  `name` varchar(60) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `ttype` varchar(5) DEFAULT NULL,
  `urate` double DEFAULT NULL,
  PRIMARY KEY (`primaryid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `httslap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `httslap` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `eord` int(11) DEFAULT NULL,
  `name` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `hup_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `hup_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inprocess_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `inprocess_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `batch_type` varchar(255) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `entry_type` varchar(255) DEFAULT NULL,
  `inward_date` datetime(6) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inprocess_check`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `inprocess_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `entry` date DEFAULT NULL,
  `kpi` bigint(20) DEFAULT NULL,
  `skipflag` varchar(255) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  `tno` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `instrulog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `instrulog` (
  `slno` bigint(20) NOT NULL AUTO_INCREMENT,
  `astatus` varchar(255) DEFAULT NULL,
  `atype` varchar(255) DEFAULT NULL,
  `attstate` varchar(255) DEFAULT NULL,
  `attdatetime` datetime(6) DEFAULT NULL,
  `enrollnumber` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `sysip` varchar(255) DEFAULT NULL,
  `ustatus` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`slno`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_connector_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_connector_catalog` (
  `code` varchar(64) NOT NULL,
  `version` int(11) NOT NULL DEFAULT 1,
  `category` varchar(32) NOT NULL,
  `vendor` varchar(64) NOT NULL,
  `display_name` varchar(160) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `config_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`config_schema_json`)),
  `default_props_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`default_props_json`)),
  `supported_operations` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`supported_operations`)),
  `auth_methods` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`auth_methods`)),
  `dependencies` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`dependencies`)),
  `pii_paths_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`pii_paths_json`)),
  `residency_constraints` varchar(32) NOT NULL DEFAULT 'NONE',
  `docs_url` varchar(255) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`code`,`version`),
  KEY `idx_iccat_category` (`category`,`status`),
  KEY `idx_iccat_vendor` (`vendor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_event_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_event_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `instance_id` bigint(20) NOT NULL,
  `direction` varchar(16) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` varchar(16) NOT NULL,
  `request_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`request_json`)),
  `response_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`response_json`)),
  `error_code` varchar(64) DEFAULT NULL,
  `latency_ms` int(11) DEFAULT NULL,
  `redacted_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`redacted_json`)),
  PRIMARY KEY (`id`),
  KEY `idx_ielog_tenant_inst_ts` (`tenant_id`,`instance_id`,`ts`),
  KEY `idx_ielog_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_field_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_field_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_code` varchar(48) NOT NULL,
  `our_entity` varchar(64) NOT NULL,
  `our_field` varchar(64) NOT NULL,
  `their_field` varchar(128) NOT NULL,
  `transform_expr` varchar(255) DEFAULT NULL,
  `direction` varchar(8) NOT NULL DEFAULT 'OUT',
  PRIMARY KEY (`id`),
  KEY `idx_ifm_inst` (`instance_code`,`our_entity`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_inbound_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_inbound_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_id` bigint(20) NOT NULL,
  `token_hash` varchar(128) NOT NULL,
  `scope_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`scope_json`)),
  `valid_from` timestamp NULL DEFAULT NULL,
  `valid_to` timestamp NULL DEFAULT NULL,
  `last_used_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_iitok_hash` (`token_hash`),
  KEY `idx_iitok_instance` (`instance_id`),
  CONSTRAINT `fk_iitok_instance` FOREIGN KEY (`instance_id`) REFERENCES `integration_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(48) NOT NULL,
  `connector_code` varchar(64) NOT NULL,
  `connector_version` int(11) NOT NULL DEFAULT 1,
  `name` varchar(160) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `status_reason` varchar(255) DEFAULT NULL,
  `current_version` int(11) NOT NULL DEFAULT 1,
  `config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`config_json`)),
  `mapping_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`mapping_json`)),
  `mapped_workflow_codes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`mapped_workflow_codes`)),
  `data_residency_flag` varchar(32) NOT NULL DEFAULT 'ANY',
  `ip_allowlist_cidr` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`ip_allowlist_cidr`)),
  `rate_limit_rps` int(11) DEFAULT NULL,
  `last_health_at` timestamp NULL DEFAULT NULL,
  `last_event_at` timestamp NULL DEFAULT NULL,
  `error_count_24h` int(11) NOT NULL DEFAULT 0,
  `custom_fields` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`custom_fields`)),
  `optimistic_lock` int(11) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` bigint(20) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_instance_tenant_code` (`tenant_id`,`code`),
  KEY `idx_instance_status` (`tenant_id`,`status`,`last_health_at`),
  KEY `idx_instance_conn` (`connector_code`,`connector_version`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_instance_secret`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_instance_secret` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_id` bigint(20) NOT NULL,
  `kid` varchar(64) NOT NULL,
  `ciphertext` longtext NOT NULL,
  `encoded_key_ref` varchar(128) DEFAULT NULL,
  `rotates_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_isecret_instance` (`instance_id`,`kid`),
  CONSTRAINT `fk_isecret_instance` FOREIGN KEY (`instance_id`) REFERENCES `integration_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_instance_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_instance_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  `config_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`config_json`)),
  `mapping_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`mapping_json`)),
  `rationale` text DEFAULT NULL,
  `published_at` timestamp NULL DEFAULT NULL,
  `published_by` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_iiver_instance_version` (`instance_id`,`version`),
  CONSTRAINT `fk_iiver_instance` FOREIGN KEY (`instance_id`) REFERENCES `integration_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `integration_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `integration_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instance_id` bigint(20) NOT NULL,
  `cron` varchar(64) NOT NULL,
  `timezone` varchar(64) NOT NULL DEFAULT 'Asia/Kolkata',
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `last_fired_at` timestamp NULL DEFAULT NULL,
  `next_fire_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_ischd_instance` (`instance_id`),
  KEY `idx_ischd_next_fire` (`enabled`,`next_fire_at`),
  CONSTRAINT `fk_ischd_instance` FOREIGN KEY (`instance_id`) REFERENCES `integration_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `internal_material_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `internal_material_issue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `issueddate` date DEFAULT NULL,
  `issuedepartment` varchar(255) DEFAULT NULL,
  `issueperson` varchar(255) DEFAULT NULL,
  `requestreferance` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `interview`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aadhar_Number` varchar(255) DEFAULT NULL,
  `batch_No` varchar(255) DEFAULT NULL,
  `candidate_name` varchar(255) DEFAULT NULL,
  `caste` varchar(255) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date_Of_Birth` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `employee_Type` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `home_Town` varchar(255) DEFAULT NULL,
  `interview_Date` datetime(6) DEFAULT NULL,
  `interview_Position` varchar(255) DEFAULT NULL,
  `interview_Time` varchar(255) DEFAULT NULL,
  `job_Type` varchar(255) DEFAULT NULL,
  `marks_Percentage` varchar(255) DEFAULT NULL,
  `phone_Number` varchar(255) DEFAULT NULL,
  `qualification` varchar(255) DEFAULT NULL,
  `referance` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk6nf52kyarvetkuaacclgrjxw` (`branch_id`),
  KEY `FKorq859a4xasw92a4ycmxhs1hn` (`dept_id`),
  CONSTRAINT `FKk6nf52kyarvetkuaacclgrjxw` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKorq859a4xasw92a4ycmxhs1hn` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `interview_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_qualification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `designation` varchar(255) DEFAULT NULL,
  `experience_from` datetime(6) DEFAULT NULL,
  `experience_to` datetime(6) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  `previous_company` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `interview_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `average_Score` bigint(20) DEFAULT NULL,
  `communication` bigint(20) DEFAULT NULL,
  `score_Id` bigint(20) DEFAULT NULL,
  `technical_Knowledge` bigint(20) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `interviewer_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgvmo4do9ld2f4cskapbfo1luq` (`interview_id`),
  KEY `FKdeqbl3ncw603136l8sm35uk1b` (`branch_id`),
  KEY `FKk36n786tkme7fis9ultmhi19y` (`interviewer_id`),
  CONSTRAINT `FKdeqbl3ncw603136l8sm35uk1b` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKgvmo4do9ld2f4cskapbfo1luq` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`),
  CONSTRAINT `FKk36n786tkme7fis9ultmhi19y` FOREIGN KEY (`interviewer_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `interviewer_assign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `interviewer_assign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `examiner_id` bigint(20) DEFAULT NULL,
  `examiner_designation_id` bigint(20) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  `interviewer_id` bigint(20) DEFAULT NULL,
  `interviewer_designation_id` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8qoidi8ylkkr7se87ep0s0r5` (`examiner_id`),
  KEY `FKp8nxncpuwspko59mdttdw00ue` (`examiner_designation_id`),
  KEY `FK71636jcqdpgq9tpyn0wsqrcks` (`interview_id`),
  KEY `FKcidur6fcohw6576h58obu3q6` (`interviewer_id`),
  KEY `FKkb1qfkna928k2i6k3q88nkt03` (`interviewer_designation_id`),
  KEY `FK33mr57mrd8nwevbce1i9acm2` (`branch_id`),
  CONSTRAINT `FK33mr57mrd8nwevbce1i9acm2` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK71636jcqdpgq9tpyn0wsqrcks` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`),
  CONSTRAINT `FK8qoidi8ylkkr7se87ep0s0r5` FOREIGN KEY (`examiner_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKcidur6fcohw6576h58obu3q6` FOREIGN KEY (`interviewer_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKkb1qfkna928k2i6k3q88nkt03` FOREIGN KEY (`interviewer_designation_id`) REFERENCES `designation` (`id`),
  CONSTRAINT `FKp8nxncpuwspko59mdttdw00ue` FOREIGN KEY (`examiner_designation_id`) REFERENCES `designation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `interviewer_assign_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `interviewer_assign_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interviewer_id` bigint(20) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  `designation_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `desgn_id` bigint(20) NOT NULL,
  `users_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgv6etthuu3qy68lcjytstrxgp` (`interview_id`),
  KEY `FK1lqefdpm7vor4bnc5vq2n2rq2` (`designation_id`),
  KEY `FKcjucb8qk18x8cc33u4ru5vjj2` (`user_id`),
  KEY `FKcvid08f1djph5163p9xa6oti8` (`desgn_id`),
  KEY `FK8r61h02i82j233wdu346cupt2` (`users_id`),
  CONSTRAINT `FK1lqefdpm7vor4bnc5vq2n2rq2` FOREIGN KEY (`designation_id`) REFERENCES `designation` (`id`),
  CONSTRAINT `FK8r61h02i82j233wdu346cupt2` FOREIGN KEY (`users_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKcjucb8qk18x8cc33u4ru5vjj2` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKcvid08f1djph5163p9xa6oti8` FOREIGN KEY (`desgn_id`) REFERENCES `designation` (`id`),
  CONSTRAINT `FKgv6etthuu3qy68lcjytstrxgp` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `intra_branch_rec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `intra_branch_rec` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `delivery_user_id` bigint(20) DEFAULT NULL,
  `dispatch_by` bigint(20) DEFAULT NULL,
  `dispatch_on` date DEFAULT NULL,
  `docket_No` varchar(255) DEFAULT NULL,
  `hup_Id` varchar(255) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnhxkbctdsyfemg101v9ki3u96` (`issue_branch_id`),
  CONSTRAINT `FKnhxkbctdsyfemg101v9ki3u96` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `intra_branch_receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `intra_branch_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `docket_No` varchar(255) DEFAULT NULL,
  `entry_Date` date DEFAULT NULL,
  `group_by` varchar(255) DEFAULT NULL,
  `hup_id` varchar(255) DEFAULT NULL,
  `intra_Branch_Receipt_Value` varchar(255) DEFAULT NULL,
  `issue_Date` date DEFAULT NULL,
  `issue_id` bigint(20) DEFAULT NULL,
  `received_Date` date DEFAULT NULL,
  `received_vehicle_no` varchar(255) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_No` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `issue_org_id` bigint(20) DEFAULT NULL,
  `received_user_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  `request_org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpt9lu1hcbosf47nk6hpd25npk` (`issue_branch_id`),
  KEY `FKjb4ouamkvynqvlynsj5f6bw6` (`user_id`),
  KEY `FKnlw485e5v81ukp9nvgvenx79r` (`issue_dept_id`),
  KEY `FKh9svbp7gnunf8fwiol7egijt5` (`issue_org_id`),
  KEY `FKr22202rhhyx6tx0y6ajtn9l1` (`received_user_id`),
  KEY `FKpghdlhiqs6wtqy4n5u7ea8clf` (`request_branch_id`),
  KEY `FKpiyduxsgtjq8ohng0srvxj49i` (`request_dept_id`),
  KEY `FKto0pc0gm1gcy9ubmwqauo9625` (`request_org_id`),
  CONSTRAINT `FKh9svbp7gnunf8fwiol7egijt5` FOREIGN KEY (`issue_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKjb4ouamkvynqvlynsj5f6bw6` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKnlw485e5v81ukp9nvgvenx79r` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKpghdlhiqs6wtqy4n5u7ea8clf` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKpiyduxsgtjq8ohng0srvxj49i` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKpt9lu1hcbosf47nk6hpd25npk` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKr22202rhhyx6tx0y6ajtn9l1` FOREIGN KEY (`received_user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKto0pc0gm1gcy9ubmwqauo9625` FOREIGN KEY (`request_org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `intra_branch_receipt_mappings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `intra_branch_receipt_mappings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `intra_branch_receipt_id` bigint(20) DEFAULT NULL,
  `other_branch_issue_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inward_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `inward_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `batch_type` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `entry_type` varchar(255) DEFAULT NULL,
  `inward_date` datetime(6) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `roqcomment` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inward_check`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `inward_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bag` varchar(255) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `completedstep` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `inward_date` datetime(6) DEFAULT NULL,
  `migrated` varchar(255) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `recheck_log` varchar(255) DEFAULT NULL,
  `recheckdesc` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inward_skip_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `inward_skip_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `entry` date DEFAULT NULL,
  `kpi` bigint(20) DEFAULT NULL,
  `skipflag` varchar(255) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  `tno` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `iso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `iso` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` bit(1) DEFAULT NULL,
  `iso_no` varchar(255) DEFAULT NULL,
  `module_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `issue_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `issue_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `allocated_Quantity` varchar(255) DEFAULT NULL,
  `bin_stock_id` bigint(20) DEFAULT NULL,
  `branch_Id` bigint(20) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept_Id` bigint(20) DEFAULT NULL,
  `issue` varchar(255) DEFAULT NULL,
  `issue_Item_Id` bigint(20) DEFAULT NULL,
  `issued_From_Batch` varchar(255) DEFAULT NULL,
  `issued_From_Bin` varchar(255) DEFAULT NULL,
  `item_Id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `pending_Quantity` varchar(255) DEFAULT NULL,
  `pre_allocated_Quantity` varchar(255) DEFAULT NULL,
  `receive_bin_stock_id` bigint(20) DEFAULT NULL,
  `requested_Quantity` varchar(255) DEFAULT NULL,
  `select_Lot` varchar(255) DEFAULT NULL,
  `stock_Quantity` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `jaggattrvalues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `jaggattrvalues` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` bigint(20) DEFAULT NULL,
  `comment` bigint(20) DEFAULT NULL,
  `gunny` bigint(20) DEFAULT NULL,
  `quality` bigint(20) DEFAULT NULL,
  `thul` bigint(20) DEFAULT NULL,
  `thulfrom` bigint(20) DEFAULT NULL,
  `thulto` bigint(20) DEFAULT NULL,
  `uniqno` varchar(255) DEFAULT NULL,
  `vabqty` double DEFAULT NULL,
  `vabus` bigint(20) DEFAULT NULL,
  `vabus_Value` varchar(255) DEFAULT NULL,
  `weight_Value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `jaggeryinsp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `jaggeryinsp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `adate` datetime(6) DEFAULT NULL,
  `approve` varchar(255) DEFAULT NULL,
  `auby` bigint(20) DEFAULT NULL,
  `audate` datetime(6) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `billdt` date DEFAULT NULL,
  `billno` varchar(255) DEFAULT NULL,
  `billtrnno` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `inwardat` bigint(20) DEFAULT NULL,
  `jdate` date DEFAULT NULL,
  `party` bigint(20) DEFAULT NULL,
  `place` varchar(255) DEFAULT NULL,
  `prepareby` bigint(20) DEFAULT NULL,
  `preparedate` datetime(6) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `rejectdate` date DEFAULT NULL,
  `rejected_By` bigint(20) DEFAULT NULL,
  `rejected_Description` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `uniqno` varchar(255) DEFAULT NULL,
  `uniqueses` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `urate` double DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `vdate` datetime(6) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `jagginspscore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `jagginspscore` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acid_insoluble_ash` varchar(255) DEFAULT NULL,
  `black` double DEFAULT NULL,
  `blacks` bigint(20) DEFAULT NULL,
  `brown` double DEFAULT NULL,
  `chocol` double DEFAULT NULL,
  `insol1` double DEFAULT NULL,
  `insol2` double DEFAULT NULL,
  `insol3` double DEFAULT NULL,
  `insolavg` double DEFAULT NULL,
  `insolmark` double DEFAULT NULL,
  `insoluble` bigint(20) DEFAULT NULL,
  `leakage` bigint(20) DEFAULT NULL,
  `lime` bigint(20) DEFAULT NULL,
  `ltblack` double DEFAULT NULL,
  `moist1` double DEFAULT NULL,
  `moist2` double DEFAULT NULL,
  `moist3` double DEFAULT NULL,
  `moistavg` double DEFAULT NULL,
  `moistmark` double DEFAULT NULL,
  `moistures` bigint(20) DEFAULT NULL,
  `score` bigint(20) DEFAULT NULL,
  `sucrose` varchar(255) DEFAULT NULL,
  `thuls` bigint(20) DEFAULT NULL,
  `total` bigint(20) DEFAULT NULL,
  `total_ash` varchar(255) DEFAULT NULL,
  `total_sugar` varchar(255) DEFAULT NULL,
  `uniqno` varchar(255) DEFAULT NULL,
  `vision` bigint(20) DEFAULT NULL,
  `white` double DEFAULT NULL,
  `wtless` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `jaggoptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `jaggoptions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attribute_Id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `range` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `jattributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `jattributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `jobresponse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `jobresponse` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchid` varchar(255) DEFAULT NULL,
  `clusterid` bigint(20) DEFAULT NULL,
  `deptid` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `userlevel` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_Status` varchar(255) DEFAULT NULL,
  `activity` varchar(255) DEFAULT NULL,
  `asrs` bit(1) DEFAULT NULL,
  `batchtype` varchar(255) DEFAULT NULL,
  `batchproxid` bigint(20) DEFAULT NULL,
  `blending` bit(1) DEFAULT NULL,
  `calculationtype` varchar(255) DEFAULT NULL,
  `company_Id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dctype` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `pridunit` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `cate` varchar(255) DEFAULT NULL,
  `pvitytype` varchar(255) DEFAULT NULL,
  `stock_type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal_attributes_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_attributes_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_cal_status` bit(1) DEFAULT NULL,
  `attributes_id` bigint(20) DEFAULT NULL,
  `attributes_name` varchar(255) DEFAULT NULL,
  `awith` varchar(255) DEFAULT NULL,
  `awithid` bigint(20) DEFAULT NULL,
  `awithtable` varchar(255) DEFAULT NULL,
  `bom_intake_type` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `connect_table` varchar(255) DEFAULT NULL,
  `consider_as` varchar(255) DEFAULT NULL,
  `datatype` varchar(255) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `disporder` int(11) DEFAULT NULL,
  `dispunit` bigint(20) DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  `maximum_value` varchar(255) DEFAULT NULL,
  `minimum_value` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `type_of_map` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal_attributes_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_attributes_option` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attributes_id` bigint(20) DEFAULT NULL,
  `attributes_map_id` bigint(20) DEFAULT NULL,
  `optionvalue` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal_branch_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_branch_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `branch_name` varchar(255) DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `accounting_date` date DEFAULT NULL,
  `adate` datetime(6) DEFAULT NULL,
  `aon` varchar(255) DEFAULT NULL,
  `astatus` varchar(255) DEFAULT NULL,
  `atrndate` datetime(6) DEFAULT NULL,
  `atrnid` bigint(20) DEFAULT NULL,
  `atrnno` varchar(255) DEFAULT NULL,
  `avgcost` double DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bdt` datetime(6) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `billid` bigint(20) DEFAULT NULL,
  `binid` varchar(255) DEFAULT NULL,
  `binname` bigint(20) DEFAULT NULL,
  `bom_Intake_Type` varchar(255) DEFAULT NULL,
  `bptype` varchar(255) DEFAULT NULL,
  `casgrp` varchar(255) DEFAULT NULL,
  `casweekid` bigint(20) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `cper` double DEFAULT NULL,
  `ctype` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `eby` bigint(20) DEFAULT NULL,
  `exid` varchar(255) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `godown` bigint(20) DEFAULT NULL,
  `instype` bigint(20) DEFAULT NULL,
  `lineid` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `poid` bigint(20) DEFAULT NULL,
  `priid` bigint(20) DEFAULT NULL,
  `prilot` double DEFAULT NULL,
  `priqty` double DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  `pshiftid` bigint(20) DEFAULT NULL,
  `reduce_batchno` varchar(255) DEFAULT NULL,
  `rowid` bigint(20) DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `sflag` varchar(255) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `srce` varchar(255) DEFAULT NULL,
  `sstkqty` double DEFAULT NULL,
  `stkqty` double DEFAULT NULL,
  `tastk` double DEFAULT NULL,
  `tasum` varchar(255) DEFAULT NULL,
  `total_input_qty` double DEFAULT NULL,
  `total_output_qty` double DEFAULT NULL,
  `trndate` datetime(6) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trntype` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  `wcid` bigint(20) DEFAULT NULL,
  `wctable` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal_machine_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_machine_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `connect_table` varchar(255) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  `machine_name` varchar(255) DEFAULT NULL,
  `primary_work_center` varchar(255) DEFAULT NULL,
  `primary_machine_id` bigint(20) DEFAULT NULL,
  `primary_machine_name` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `journal_product_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal_product_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity` varchar(255) DEFAULT NULL,
  `applicable_For_Jcal` bit(1) DEFAULT NULL,
  `bptype` varchar(255) DEFAULT NULL,
  `bom_take_type` varchar(255) DEFAULT NULL,
  `bottleweight` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `consider_as` varchar(255) DEFAULT NULL,
  `cost` varchar(255) DEFAULT NULL,
  `cost_amount` double DEFAULT NULL,
  `cost_per` double DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `fqty` double DEFAULT NULL,
  `journal_id` bigint(20) DEFAULT NULL,
  `pridunit` varchar(255) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `trackno` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `kpi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `kpi` (
  `priid` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` varchar(255) DEFAULT NULL,
  `dispord` bigint(20) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `skip` varchar(255) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`priid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `kpiname`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `kpiname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attype` varchar(255) DEFAULT NULL,
  `ffieldid` bigint(20) DEFAULT NULL,
  `formno` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pendingreportflag` int(11) DEFAULT NULL,
  `process_type` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs7bhatymqpxkrj8bdfw989snu` (`proid`),
  CONSTRAINT `FKs7bhatymqpxkrj8bdfw989snu` FOREIGN KEY (`proid`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelLocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelLocation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ceilinglimit` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `locationid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8m3qbxy3jm4earc4ie8qjac9a` (`createdby`),
  CONSTRAINT `FK8m3qbxy3jm4earc4ie8qjac9a` FOREIGN KEY (`createdby`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelPrices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelPrices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entereddate` datetime(6) DEFAULT NULL,
  `fromdate` datetime(6) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `labelLocationId` bigint(20) DEFAULT NULL,
  `per_ml_price` double DEFAULT NULL,
  `price` double DEFAULT NULL,
  `labelproductgroupid` bigint(20) DEFAULT NULL,
  `labelproductid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp1vsqq2ioq40kuk4t2jjfo1j` (`updatedby`),
  CONSTRAINT `FKp1vsqq2ioq40kuk4t2jjfo1j` FOREIGN KEY (`updatedby`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelceilingstocklimit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelceilingstocklimit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` varchar(255) DEFAULT NULL,
  `ceilinglimit` bigint(20) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `productgroup` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `branchid` bigint(20) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKoe3nvol6bl2thptxi0nb2odfm` (`branchid`),
  KEY `FKdamepig36jx9ntp5lqu5b7kk9` (`updatedby`),
  CONSTRAINT `FKdamepig36jx9ntp5lqu5b7kk9` FOREIGN KEY (`updatedby`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKoe3nvol6bl2thptxi0nb2odfm` FOREIGN KEY (`branchid`) REFERENCES `labelproductionbranches` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelgroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelproduct`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelproduct` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchid` bigint(20) DEFAULT NULL,
  `capacity` bigint(20) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `dislabelname` varchar(255) DEFAULT NULL,
  `displocation` varchar(255) DEFAULT NULL,
  `displocationorder` bigint(20) DEFAULT NULL,
  `productnameid` bigint(20) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `labelweightinkg1` double DEFAULT NULL,
  `labelweightinkg` double DEFAULT NULL,
  `locationid` bigint(20) DEFAULT NULL,
  `productname` varchar(255) DEFAULT NULL,
  `lableprice` varchar(255) DEFAULT NULL,
  `productgroupid` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3cgkcs7j0a5gbf0hqy7edr83f` (`updatedby`),
  CONSTRAINT `FK3cgkcs7j0a5gbf0hqy7edr83f` FOREIGN KEY (`updatedby`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelproductionbranches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelproductionbranches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelproductname`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelproductname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` varchar(255) DEFAULT NULL,
  `displayorder` bigint(20) DEFAULT NULL,
  `flag` bigint(20) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `salesproductgroup` bigint(20) DEFAULT NULL,
  `productname` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8f8bh13dlslmdo0u3def2gb5x` (`createdby`),
  CONSTRAINT `FK8f8bh13dlslmdo0u3def2gb5x` FOREIGN KEY (`createdby`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `labelproductvalidity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `labelproductvalidity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `locationid` bigint(20) DEFAULT NULL,
  `locationtype` varchar(255) DEFAULT NULL,
  `bestbeforeinmonths` bigint(20) DEFAULT NULL,
  `productgroupid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `leave_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `approved__date` datetime(6) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `employee_name` varchar(255) DEFAULT NULL,
  `from_date_session1` int(11) DEFAULT NULL,
  `from_date_session2` int(11) DEFAULT NULL,
  `leave_from` datetime(6) DEFAULT NULL,
  `leave_reason` varchar(255) DEFAULT NULL,
  `leave_to` datetime(6) DEFAULT NULL,
  `leave_type` varchar(255) DEFAULT NULL,
  `no_of_days` int(11) DEFAULT NULL,
  `reason_for_rejection` varchar(255) DEFAULT NULL,
  `rejected__date` datetime(6) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `remaining_leave` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `to_date_session1` int(11) DEFAULT NULL,
  `to_date_session2` int(11) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `week_Off_Days` varchar(255) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk9an723c2gd69vmv362sxx9av` (`org_id`),
  CONSTRAINT `FKk9an723c2gd69vmv362sxx9av` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `leave_salary_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_salary_voucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adate` date DEFAULT NULL,
  `aendt` varchar(255) DEFAULT NULL,
  `amt` double DEFAULT NULL,
  `apdate` date DEFAULT NULL,
  `apendt` varchar(255) DEFAULT NULL,
  `approval` bigint(20) DEFAULT NULL,
  `apstaff` bigint(20) DEFAULT NULL,
  `astaff` bigint(20) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `cst` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `pay` varchar(255) DEFAULT NULL,
  `paydesc` varchar(255) DEFAULT NULL,
  `paytype` varchar(255) DEFAULT NULL,
  `trndate` date DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `vdate` date DEFAULT NULL,
  `vendt` varchar(255) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `vstaff` bigint(20) DEFAULT NULL,
  `vtype` varchar(255) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `leave_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `C1` bigint(20) DEFAULT NULL,
  `C10` bigint(20) DEFAULT NULL,
  `C11` bigint(20) DEFAULT NULL,
  `C12` bigint(20) DEFAULT NULL,
  `C13` bigint(20) DEFAULT NULL,
  `C14` bigint(20) DEFAULT NULL,
  `C15` bigint(20) DEFAULT NULL,
  `C16` bigint(20) DEFAULT NULL,
  `C17` bigint(20) DEFAULT NULL,
  `C18` bigint(20) DEFAULT NULL,
  `C19` bigint(20) DEFAULT NULL,
  `C2` bigint(20) DEFAULT NULL,
  `C20` bigint(20) DEFAULT NULL,
  `C21` bigint(20) DEFAULT NULL,
  `C22` bigint(20) DEFAULT NULL,
  `C23` bigint(20) DEFAULT NULL,
  `C23trnno` varchar(255) DEFAULT NULL,
  `C24` bigint(20) DEFAULT NULL,
  `C25` bigint(20) DEFAULT NULL,
  `C26` bigint(20) DEFAULT NULL,
  `C27` bigint(20) DEFAULT NULL,
  `C28` bigint(20) DEFAULT NULL,
  `C29` bigint(20) DEFAULT NULL,
  `C3` bigint(20) DEFAULT NULL,
  `C30` bigint(20) DEFAULT NULL,
  `C31` bigint(20) DEFAULT NULL,
  `C32` bigint(20) DEFAULT NULL,
  `C33` bigint(20) DEFAULT NULL,
  `C34` bigint(20) DEFAULT NULL,
  `C35` bigint(20) DEFAULT NULL,
  `C4` bigint(20) DEFAULT NULL,
  `C5` bigint(20) DEFAULT NULL,
  `C6` bigint(20) DEFAULT NULL,
  `C7` double DEFAULT NULL,
  `C8` bigint(20) DEFAULT NULL,
  `C9` bigint(20) DEFAULT NULL,
  `CON` double DEFAULT NULL,
  `GWA` double DEFAULT NULL,
  `HRA` double DEFAULT NULL,
  `LWOP` double DEFAULT NULL,
  `Location` varchar(255) DEFAULT NULL,
  `Machineip` varchar(255) DEFAULT NULL,
  `S1` bigint(20) DEFAULT NULL,
  `S10` bigint(20) DEFAULT NULL,
  `S11` bigint(20) DEFAULT NULL,
  `S12` bigint(20) DEFAULT NULL,
  `S13` bigint(20) DEFAULT NULL,
  `S14` bigint(20) DEFAULT NULL,
  `S15` bigint(20) DEFAULT NULL,
  `S16` bigint(20) DEFAULT NULL,
  `S17` bigint(20) DEFAULT NULL,
  `S18` bigint(20) DEFAULT NULL,
  `S19` bigint(20) DEFAULT NULL,
  `S2` bigint(20) DEFAULT NULL,
  `S20` bigint(20) DEFAULT NULL,
  `S21` bigint(20) DEFAULT NULL,
  `S22` bigint(20) DEFAULT NULL,
  `S23` bigint(20) DEFAULT NULL,
  `S24` bigint(20) DEFAULT NULL,
  `S25` bigint(20) DEFAULT NULL,
  `S26` bigint(20) DEFAULT NULL,
  `S27` bigint(20) DEFAULT NULL,
  `S28` bigint(20) DEFAULT NULL,
  `S29` bigint(20) DEFAULT NULL,
  `S3` bigint(20) DEFAULT NULL,
  `S30` bigint(20) DEFAULT NULL,
  `S4` bigint(20) DEFAULT NULL,
  `S5` bigint(20) DEFAULT NULL,
  `S6` bigint(20) DEFAULT NULL,
  `S7` bigint(20) DEFAULT NULL,
  `S8` bigint(20) DEFAULT NULL,
  `S9` bigint(20) DEFAULT NULL,
  `WA` double DEFAULT NULL,
  `absent` varchar(255) DEFAULT NULL,
  `absents` double DEFAULT NULL,
  `ayear` varchar(255) DEFAULT NULL,
  `bcontrol` varchar(255) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `bleave` double DEFAULT NULL,
  `bleaves` double DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `changedon` datetime(6) DEFAULT NULL,
  `chstatus` varchar(255) DEFAULT NULL,
  `cstatus` varchar(255) DEFAULT NULL,
  `cumulative_Deducted_Leave` double DEFAULT NULL,
  `cumulative_Leave_taken` double DEFAULT NULL,
  `deducted_Leave` double DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `dleave` double DEFAULT NULL,
  `dutyAllocatedDays` double DEFAULT NULL,
  `dutyAllocatedSalary` double DEFAULT NULL,
  `earnedLeaveBasic` double DEFAULT NULL,
  `earnedLeaveDA` double DEFAULT NULL,
  `earnedLeaveDays` double DEFAULT NULL,
  `earned_Cumulative_Leave` double DEFAULT NULL,
  `earned_Leave` double DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `expyears` double DEFAULT NULL,
  `jdate` date DEFAULT NULL,
  `late` varchar(255) DEFAULT NULL,
  `leave` varchar(255) DEFAULT NULL,
  `leaveHolidayAttendedBasic` double DEFAULT NULL,
  `leaveHolidayAttendedDA` double DEFAULT NULL,
  `leaveHolidayAttendedDays` bigint(20) DEFAULT NULL,
  `leaveHolidayBasic` double DEFAULT NULL,
  `leaveHolidayDA` double DEFAULT NULL,
  `leaveHolidaySalaryDays` double DEFAULT NULL,
  `leave_Taken` double DEFAULT NULL,
  `leaves` double DEFAULT NULL,
  `lossOfPayDays` double DEFAULT NULL,
  `lupdate` datetime(6) DEFAULT NULL,
  `missign` bigint(20) DEFAULT NULL,
  `misupdate` double DEFAULT NULL,
  `month` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `nodays` double DEFAULT NULL,
  `nolate` varchar(255) DEFAULT NULL,
  `noleave` varchar(255) DEFAULT NULL,
  `nonil` varchar(255) DEFAULT NULL,
  `noperm` varchar(255) DEFAULT NULL,
  `noshifts` double DEFAULT NULL,
  `odapprove` bigint(20) DEFAULT NULL,
  `onduty` double DEFAULT NULL,
  `overTimeWages` double DEFAULT NULL,
  `perm` varchar(255) DEFAULT NULL,
  `pleaves` double DEFAULT NULL,
  `present` varchar(255) DEFAULT NULL,
  `presents` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `strmy` bigint(20) DEFAULT NULL,
  `tpresents` bigint(20) DEFAULT NULL,
  `tsaldays` double DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `upstatus` varchar(255) DEFAULT NULL,
  `workedDays` double DEFAULT NULL,
  `workedDaysBasic` double DEFAULT NULL,
  `workedDaysDA` double DEFAULT NULL,
  `year` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ledger` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activeflag` int(11) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `camp` varchar(255) DEFAULT NULL,
  `chess` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `cityid` bigint(20) DEFAULT NULL,
  `companylink` int(11) DEFAULT NULL,
  `contact` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `countryid` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `godownflag` int(11) DEFAULT NULL,
  `gst` varchar(255) DEFAULT NULL,
  `headplant` int(11) DEFAULT NULL,
  `ho_status` varchar(255) DEFAULT NULL,
  `lfinal` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `packing_raw_material` int(11) DEFAULT NULL,
  `partygroupid` bigint(20) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `pincode` varchar(255) DEFAULT NULL,
  `send_ft_to_ho_object` varchar(255) DEFAULT NULL,
  `sister_Concern_Group_Id` bigint(20) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `stateid` bigint(20) DEFAULT NULL,
  `taxtype` varchar(255) DEFAULT NULL,
  `tinno` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `zone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `linksj`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `linksj` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity` varchar(255) DEFAULT NULL,
  `batchproid` bigint(20) DEFAULT NULL,
  `bottle_Weight_From` double DEFAULT NULL,
  `bottle_Weight_To` double DEFAULT NULL,
  `bproid` varchar(255) DEFAULT NULL,
  `bptype` varchar(255) DEFAULT NULL,
  `cas` varchar(255) DEFAULT NULL,
  `cate` varchar(255) DEFAULT NULL,
  `cbox` varchar(255) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `cper` double DEFAULT NULL,
  `ctype` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `dry` varchar(255) DEFAULT NULL,
  `exid` varchar(255) DEFAULT NULL,
  `exid1` varchar(255) DEFAULT NULL,
  `exop` varchar(255) DEFAULT NULL,
  `flm` varchar(255) DEFAULT NULL,
  `fqty` double DEFAULT NULL,
  `ldbag` varchar(255) DEFAULT NULL,
  `lmf` double DEFAULT NULL,
  `pridunit` varchar(255) DEFAULT NULL,
  `priid` bigint(20) DEFAULT NULL,
  `prm` varchar(255) DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  `proname` varchar(255) DEFAULT NULL,
  `qtype` varchar(255) DEFAULT NULL,
  `report_Activity` varchar(255) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `sjname` varchar(255) DEFAULT NULL,
  `tasum` varchar(255) DEFAULT NULL,
  `trackno` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `log_exception`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `log_exception` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exception_message` varchar(255) DEFAULT NULL,
  `http_status` int(11) DEFAULT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3636 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `lot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `lot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dispname` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `lot_str` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `lot_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `lot_value` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dispname` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `luzone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `luzone` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `cost_group_id` bigint(20) DEFAULT NULL,
  `cost_report` varchar(255) DEFAULT NULL,
  `dacat_id` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `female` bigint(20) DEFAULT NULL,
  `male` bigint(20) DEFAULT NULL,
  `modified_time_stamp` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `t` varchar(255) DEFAULT NULL,
  `wagestype` varchar(255) DEFAULT NULL,
  `wcid` bigint(20) DEFAULT NULL,
  `wctable` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `ts` timestamp NOT NULL DEFAULT current_timestamp(),
  `actor_id` bigint(20) DEFAULT NULL,
  `action_kind` varchar(32) NOT NULL,
  `entity_code` varchar(64) DEFAULT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `workflow_code` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `from_state` varchar(64) DEFAULT NULL,
  `to_state` varchar(64) DEFAULT NULL,
  `message` varchar(512) DEFAULT NULL,
  `payload_json` text DEFAULT NULL,
  `event_at` datetime DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `entity_type` varchar(255) DEFAULT NULL,
  `details_json` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_audit_ts` (`ts`),
  KEY `idx_audit_actor` (`actor_id`,`ts`),
  KEY `idx_audit_entity` (`entity_code`,`entity_id`),
  KEY `idx_audit_wf` (`workflow_code`,`workflow_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6391 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `doc_code` varchar(32) NOT NULL,
  `doc_kind` varchar(32) NOT NULL,
  `title` varchar(256) NOT NULL,
  `file_name` varchar(256) DEFAULT NULL,
  `mime_type` varchar(64) DEFAULT NULL,
  `size_bytes` bigint(20) DEFAULT NULL,
  `storage_path` varchar(512) DEFAULT NULL,
  `linked_entity_code` varchar(64) DEFAULT NULL,
  `linked_entity_id` bigint(20) DEFAULT NULL,
  `uploaded_by` bigint(20) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `retention_until` date DEFAULT NULL,
  `esign_status` varchar(16) DEFAULT NULL,
  `esign_provider` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_doc_code` (`tenant_id`,`doc_code`),
  KEY `idx_doc_link` (`linked_entity_code`,`linked_entity_id`),
  KEY `idx_doc_kind` (`doc_kind`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_document_vault`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_document_vault` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `doc_no` varchar(48) NOT NULL,
  `doc_category` varchar(32) NOT NULL,
  `doc_type` varchar(48) NOT NULL,
  `title` varchar(160) NOT NULL,
  `related_to_entity` varchar(64) DEFAULT NULL,
  `related_to_id` bigint(20) DEFAULT NULL,
  `fy` varchar(8) DEFAULT NULL,
  `period` varchar(16) DEFAULT NULL,
  `document_url` varchar(255) NOT NULL,
  `document_size_kb` int(11) DEFAULT NULL,
  `document_hash` varchar(64) DEFAULT NULL,
  `issued_by` varchar(96) DEFAULT NULL,
  `issued_at` date DEFAULT NULL,
  `valid_to` date DEFAULT NULL,
  `uploaded_by_emp_id` bigint(20) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `tags` text DEFAULT NULL,
  `access_level` varchar(16) NOT NULL DEFAULT 'INTERNAL',
  `archived` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_doc` (`tenant_id`,`doc_no`),
  KEY `idx_doc_cat` (`doc_category`,`doc_type`),
  KEY `idx_doc_fy` (`fy`,`period`),
  KEY `idx_doc_related` (`related_to_entity`,`related_to_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_pivot_view`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_pivot_view` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `pivot_code` varchar(64) NOT NULL,
  `pivot_name` varchar(256) NOT NULL,
  `base_entity` varchar(64) NOT NULL,
  `row_fields_json` text NOT NULL,
  `col_fields_json` text DEFAULT NULL,
  `measure_field` varchar(64) NOT NULL,
  `agg_kind` varchar(16) NOT NULL DEFAULT 'SUM',
  `filter_json` text DEFAULT NULL,
  `shared` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pivot` (`tenant_id`,`pivot_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_saved_view`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_saved_view` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `owner_id` bigint(20) DEFAULT NULL,
  `view_code` varchar(64) NOT NULL,
  `view_name` varchar(256) NOT NULL,
  `base_page_code` varchar(128) NOT NULL,
  `filter_json` text DEFAULT NULL,
  `sort_json` text DEFAULT NULL,
  `columns_json` text DEFAULT NULL,
  `shared` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_saved_view` (`tenant_id`,`view_code`),
  KEY `idx_sv_page` (`base_page_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_tenant_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_tenant_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `setting_key` varchar(128) NOT NULL,
  `setting_value` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tenant_setting` (`tenant_id`,`setting_key`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_vendor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_vendor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `name` varchar(256) NOT NULL,
  `gstin` varchar(16) DEFAULT NULL,
  `city_id` bigint(20) DEFAULT NULL,
  `state_id` bigint(20) DEFAULT NULL,
  `is_msme` tinyint(1) NOT NULL DEFAULT 0,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `vendor_code` varchar(32) DEFAULT NULL,
  `vendor_name` varchar(255) DEFAULT NULL,
  `pan` varchar(16) DEFAULT NULL,
  `bank_account` varchar(32) DEFAULT NULL,
  `bank_ifsc` varchar(16) DEFAULT NULL,
  `payment_terms_days` int(11) NOT NULL DEFAULT 30,
  `credit_limit` decimal(12,2) DEFAULT NULL,
  `contact_person` varchar(128) DEFAULT NULL,
  `contact_phone` varchar(16) DEFAULT NULL,
  `contact_email` varchar(128) DEFAULT NULL,
  `address` varchar(512) DEFAULT NULL,
  `msme_regn` varchar(64) DEFAULT NULL,
  `default_item_category` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_m0_vendor_tenant` (`tenant_id`,`status`),
  KEY `idx_m0_vendor_city` (`city_id`),
  KEY `idx_m0_vendor_state` (`state_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m0_workflow_projection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m0_workflow_projection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `workflow_code` varchar(64) NOT NULL,
  `target_table` varchar(64) NOT NULL,
  `key_column` varchar(64) NOT NULL DEFAULT 'workflow_instance_id',
  `state_columns` varchar(255) NOT NULL DEFAULT 'status',
  `variant_column` varchar(64) DEFAULT NULL,
  `number_column` varchar(64) DEFAULT NULL,
  `number_pattern` varchar(128) DEFAULT NULL,
  `terminal_states` varchar(255) DEFAULT NULL,
  `terminal_ts_column` varchar(64) DEFAULT NULL,
  `field_map_json` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wf_projection` (`tenant_id`,`workflow_code`,`target_table`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m10_diesel_filling_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m10_diesel_filling_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_id` bigint(20) DEFAULT NULL,
  `vehicle_no` varchar(32) DEFAULT NULL,
  `diesel_qty_litres` decimal(8,2) DEFAULT NULL,
  `filled_at` timestamp NULL DEFAULT NULL,
  `filled_by` bigint(20) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'COMPLETED',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `ix_diesel_gp` (`gate_pass_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m10_eway_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m10_eway_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `ewb_number` varchar(32) NOT NULL,
  `source_doc_type` varchar(16) NOT NULL,
  `source_doc_no` varchar(64) DEFAULT NULL,
  `source_doc_date` date DEFAULT NULL,
  `supply_type` varchar(8) NOT NULL,
  `sub_supply_type` varchar(16) NOT NULL,
  `doc_value` decimal(14,2) NOT NULL,
  `from_gstin` varchar(16) DEFAULT NULL,
  `from_address` varchar(255) DEFAULT NULL,
  `from_pincode` varchar(10) DEFAULT NULL,
  `from_state_code` varchar(8) DEFAULT NULL,
  `to_gstin` varchar(16) DEFAULT NULL,
  `to_address` varchar(255) DEFAULT NULL,
  `to_pincode` varchar(10) DEFAULT NULL,
  `to_state_code` varchar(8) DEFAULT NULL,
  `transport_mode` varchar(8) DEFAULT NULL,
  `vehicle_no` varchar(16) DEFAULT NULL,
  `transporter_id` varchar(20) DEFAULT NULL,
  `transporter_name` varchar(64) DEFAULT NULL,
  `distance_km` int(11) DEFAULT NULL,
  `irn` varchar(64) DEFAULT NULL,
  `ewb_status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `generated_at` timestamp NULL DEFAULT NULL,
  `valid_upto` timestamp NULL DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `cancel_reason` varchar(128) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ewb_number` (`tenant_id`,`ewb_number`),
  KEY `idx_ewb_status` (`tenant_id`,`ewb_status`,`generated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m10_seal_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m10_seal_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `gate_pass_id` bigint(20) DEFAULT NULL,
  `seal_number` varchar(64) NOT NULL,
  `seal_kind` varchar(16) NOT NULL DEFAULT 'AUTO',
  `status` varchar(24) NOT NULL DEFAULT 'ISSUED',
  `issued_at` timestamp NULL DEFAULT NULL,
  `applied_at` timestamp NULL DEFAULT NULL,
  `verified_at` timestamp NULL DEFAULT NULL,
  `broken_at` timestamp NULL DEFAULT NULL,
  `issued_by` bigint(20) DEFAULT NULL,
  `applied_by` bigint(20) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_seal_num` (`tenant_id`,`seal_number`),
  KEY `ix_seal_gp` (`gate_pass_id`),
  KEY `ix_seal_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m11_agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m11_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `agent_id` varchar(64) NOT NULL,
  `agent_kind` varchar(32) NOT NULL DEFAULT 'conveyor',
  `line_id` varchar(64) NOT NULL,
  `display_name` varchar(128) DEFAULT NULL,
  `location` varchar(128) DEFAULT NULL,
  `plc_host` varchar(64) DEFAULT NULL,
  `plc_port` int(11) NOT NULL DEFAULT 502,
  `plc_unit_id` int(11) NOT NULL DEFAULT 1,
  `plc_timeout_ms` int(11) NOT NULL DEFAULT 2000,
  `devices_json` longtext DEFAULT NULL,
  `line_workflow_json` longtext DEFAULT NULL,
  `config_version` int(11) NOT NULL DEFAULT 1,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `notes` varchar(512) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_agent_id` (`tenant_id`,`agent_id`),
  KEY `ix_agent_line` (`line_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m11_agent_acceptance_scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m11_agent_acceptance_scope` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `agent_id` varchar(64) NOT NULL,
  `scope_kind` varchar(32) NOT NULL,
  `scope_ref_id` bigint(20) DEFAULT NULL,
  `expected_sku` varchar(64) DEFAULT NULL,
  `expected_offer` varchar(32) DEFAULT NULL,
  `expected_barcode` varchar(64) DEFAULT NULL,
  `max_pieces` int(11) DEFAULT NULL,
  `current_pieces` int(11) NOT NULL DEFAULT 0,
  `weight_min_kg` decimal(8,3) DEFAULT NULL,
  `weight_max_kg` decimal(8,3) DEFAULT NULL,
  `flow_steps_json` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `ttl_s` int(11) NOT NULL DEFAULT 60,
  `notes` varchar(512) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `ix_scope_agent_active` (`agent_id`,`active`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m11_agent_heartbeat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m11_agent_heartbeat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `agent_kind` varchar(32) NOT NULL,
  `line_id` varchar(64) NOT NULL,
  `queue_depth` int(11) NOT NULL DEFAULT 0,
  `last_ts` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_agent_line` (`tenant_id`,`agent_kind`,`line_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m11_conveyor_command`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m11_conveyor_command` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `line_id` varchar(64) NOT NULL,
  `kind` varchar(32) NOT NULL,
  `rpm` int(11) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `issued_by` bigint(20) DEFAULT NULL,
  `issued_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `ack_at` timestamp NULL DEFAULT NULL,
  `ack_result` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_ccmd_line_pending` (`line_id`,`ack_at`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_accident_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_accident_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `accident_no` varchar(32) NOT NULL,
  `occurred_at` datetime NOT NULL,
  `reported_at` datetime DEFAULT NULL,
  `location` varchar(96) DEFAULT NULL,
  `injured_emp_id` bigint(20) DEFAULT NULL,
  `injured_name` varchar(128) DEFAULT NULL,
  `injured_designation` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `gender` varchar(8) DEFAULT NULL,
  `body_part_affected` varchar(96) DEFAULT NULL,
  `injury_type` varchar(32) DEFAULT NULL,
  `severity` varchar(16) NOT NULL,
  `cause_category` varchar(32) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `first_aid_given_by` varchar(96) DEFAULT NULL,
  `hospital_referred` varchar(96) DEFAULT NULL,
  `days_lost` int(11) DEFAULT NULL,
  `reported_to_inspector` tinyint(1) NOT NULL DEFAULT 0,
  `inspector_ref` varchar(64) DEFAULT NULL,
  `reported_to_esic` tinyint(1) NOT NULL DEFAULT 0,
  `esic_ref` varchar(64) DEFAULT NULL,
  `investigated_by_emp_id` bigint(20) DEFAULT NULL,
  `root_cause` text DEFAULT NULL,
  `corrective_action` text DEFAULT NULL,
  `capa_closed_at` datetime DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_acc` (`tenant_id`,`accident_no`),
  KEY `idx_acc_date` (`occurred_at`,`severity`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_amc_contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_amc_contract` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `amc_number` varchar(32) NOT NULL,
  `asset_id` bigint(20) DEFAULT NULL,
  `vendor_name` varchar(256) NOT NULL,
  `cover_kind` varchar(16) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `annual_value` decimal(14,2) NOT NULL,
  `pm_freq_months` int(11) NOT NULL DEFAULT 3,
  `last_pm_on` date DEFAULT NULL,
  `next_pm_on` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_amc_number` (`tenant_id`,`amc_number`),
  KEY `idx_amc_status` (`status`,`next_pm_on`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_asset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_asset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `asset_code` varchar(32) NOT NULL,
  `asset_name` varchar(256) NOT NULL,
  `category` varchar(32) DEFAULT NULL,
  `location` varchar(64) DEFAULT NULL,
  `installed_on` date DEFAULT NULL,
  `serial_no` varchar(64) DEFAULT NULL,
  `manufacturer` varchar(128) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_asset` (`tenant_id`,`asset_code`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_asset_depreciation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_asset_depreciation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `asset_id` bigint(20) NOT NULL,
  `asset_code` varchar(32) NOT NULL,
  `method` varchar(16) NOT NULL,
  `acquisition_cost` decimal(14,2) NOT NULL,
  `salvage_value` decimal(14,2) NOT NULL DEFAULT 0.00,
  `useful_life_months` int(11) NOT NULL,
  `start_date` date NOT NULL,
  `posted_for_period` varchar(8) NOT NULL,
  `depreciation_amount` decimal(14,2) NOT NULL,
  `accumulated_dep` decimal(14,2) NOT NULL,
  `net_book_value` decimal(14,2) NOT NULL,
  `posted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dep_period` (`tenant_id`,`asset_id`,`posted_for_period`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_boiler_daily_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_boiler_daily_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `boiler_code` varchar(16) NOT NULL,
  `log_date` date NOT NULL,
  `shift` varchar(8) NOT NULL,
  `operator_emp_id` bigint(20) DEFAULT NULL,
  `operator_ibr_cert_no` varchar(32) DEFAULT NULL,
  `startup_time` datetime DEFAULT NULL,
  `shutdown_time` datetime DEFAULT NULL,
  `pressure_max_bar` decimal(6,2) DEFAULT NULL,
  `pressure_avg_bar` decimal(6,2) DEFAULT NULL,
  `temperature_max_c` decimal(6,2) DEFAULT NULL,
  `water_consumed_kl` decimal(8,2) DEFAULT NULL,
  `fuel_type` varchar(16) DEFAULT NULL,
  `fuel_consumed_kg` decimal(10,2) DEFAULT NULL,
  `steam_generated_ton` decimal(8,2) DEFAULT NULL,
  `safety_valve_test_done` tinyint(1) NOT NULL DEFAULT 0,
  `pressure_gauge_calib_ok` tinyint(1) NOT NULL DEFAULT 1,
  `water_level_alarm_ok` tinyint(1) NOT NULL DEFAULT 1,
  `blowdown_count` int(11) DEFAULT NULL,
  `abnormal_observations` text DEFAULT NULL,
  `corrective_action` text DEFAULT NULL,
  `supervisor_signoff_emp_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'CLOSED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_boiler_log` (`tenant_id`,`boiler_code`,`log_date`,`shift`),
  KEY `idx_bl_date` (`log_date`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_bom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_bom` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `bom_code` varchar(64) NOT NULL,
  `product_code` varchar(64) NOT NULL,
  `product_name` varchar(256) DEFAULT NULL,
  `yield_uom` varchar(16) NOT NULL,
  `yield_qty` decimal(14,3) NOT NULL,
  `version` int(11) NOT NULL DEFAULT 1,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `effective_from` date DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bom` (`tenant_id`,`bom_code`,`version`),
  KEY `idx_bom_product` (`product_code`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_bom_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_bom_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bom_id` bigint(20) NOT NULL,
  `input_item_code` varchar(64) NOT NULL,
  `line_type` varchar(16) NOT NULL DEFAULT 'INPUT',
  `input_item_name` varchar(256) DEFAULT NULL,
  `qty_per_yield` decimal(14,4) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `loss_pct` decimal(5,2) DEFAULT NULL,
  `yield_pct` decimal(5,2) DEFAULT NULL,
  `recovery_value` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_boml_bom` (`bom_id`),
  KEY `idx_boml_input` (`input_item_code`),
  CONSTRAINT `fk_boml_bom` FOREIGN KEY (`bom_id`) REFERENCES `m5_bom` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_consumable_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_consumable_issue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `issue_no` varchar(32) NOT NULL,
  `issue_date` date NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(160) DEFAULT NULL,
  `qty` decimal(14,3) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `issued_to_dept` varchar(32) DEFAULT NULL,
  `issued_to_emp_id` bigint(20) DEFAULT NULL,
  `purpose` varchar(96) DEFAULT NULL,
  `reference_jc` varchar(32) DEFAULT NULL,
  `reference_batch` varchar(32) DEFAULT NULL,
  `unit_cost` decimal(12,2) DEFAULT NULL,
  `total_cost` decimal(14,2) GENERATED ALWAYS AS (`qty` * coalesce(`unit_cost`,0)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ci` (`tenant_id`,`issue_no`),
  KEY `idx_ci_date` (`issue_date`,`item_code`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_contractor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_contractor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `contractor_code` varchar(32) NOT NULL,
  `contractor_name` varchar(160) NOT NULL,
  `service_category` varchar(48) DEFAULT NULL,
  `agreement_no` varchar(48) DEFAULT NULL,
  `agreement_from` date DEFAULT NULL,
  `agreement_to` date DEFAULT NULL,
  `contract_value` decimal(14,2) DEFAULT NULL,
  `clra_licence_no` varchar(48) DEFAULT NULL,
  `clra_licence_to` date DEFAULT NULL,
  `gstin` varchar(16) DEFAULT NULL,
  `pan` varchar(16) DEFAULT NULL,
  `pf_registration` varchar(32) DEFAULT NULL,
  `esi_registration` varchar(32) DEFAULT NULL,
  `contact_person` varchar(96) DEFAULT NULL,
  `contact_phone` varchar(16) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_contractor` (`tenant_id`,`contractor_code`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_contractor_labour`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_contractor_labour` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `contractor_id` bigint(20) NOT NULL,
  `emp_full_name` varchar(128) NOT NULL,
  `father_name` varchar(128) DEFAULT NULL,
  `designation` varchar(64) DEFAULT NULL,
  `aadhaar_last4` varchar(4) DEFAULT NULL,
  `pan` varchar(16) DEFAULT NULL,
  `uan` varchar(16) DEFAULT NULL,
  `esic_ip` varchar(24) DEFAULT NULL,
  `joining_date` date DEFAULT NULL,
  `separation_date` date DEFAULT NULL,
  `gender` varchar(8) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `daily_wage` decimal(10,2) DEFAULT NULL,
  `esic_wage_pct` decimal(5,2) DEFAULT NULL,
  `pf_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `esi_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `badge_no` varchar(16) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_cl_active` (`contractor_id`,`active`),
  CONSTRAINT `fk_cl_contractor` FOREIGN KEY (`contractor_id`) REFERENCES `m5_contractor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_cycle_count`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_cycle_count` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `count_no` varchar(32) NOT NULL,
  `warehouse` varchar(32) DEFAULT NULL,
  `bin_code` varchar(32) DEFAULT NULL,
  `item_code` varchar(64) DEFAULT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `count_date` date NOT NULL,
  `qty_book` decimal(14,3) NOT NULL,
  `qty_physical` decimal(14,3) DEFAULT NULL,
  `qty_variance` decimal(14,3) GENERATED ALWAYS AS (coalesce(`qty_physical`,0) - `qty_book`) STORED,
  `variance_value` decimal(14,2) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PLANNED',
  `counted_by_emp_id` bigint(20) DEFAULT NULL,
  `recount_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `reason` varchar(64) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_count` (`tenant_id`,`count_no`),
  KEY `idx_cc_bin` (`warehouse`,`bin_code`),
  KEY `idx_cc_status` (`status`,`count_date`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_daily_journal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_daily_journal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `journal_date` date NOT NULL,
  `journal_no` varchar(32) NOT NULL,
  `raw_received_kg` decimal(14,3) DEFAULT NULL,
  `raw_consumed_kg` decimal(14,3) DEFAULT NULL,
  `oil_produced_kg` decimal(14,3) DEFAULT NULL,
  `cake_produced_kg` decimal(14,3) DEFAULT NULL,
  `other_byproduct_kg` decimal(14,3) DEFAULT NULL,
  `overall_yield_pct` decimal(5,2) GENERATED ALWAYS AS (case when `raw_consumed_kg` > 0 then coalesce(`oil_produced_kg`,0) / `raw_consumed_kg` * 100 else NULL end) STORED,
  `packs_produced` int(11) DEFAULT NULL,
  `spoil_packs` int(11) DEFAULT NULL,
  `trucks_dispatched` int(11) DEFAULT NULL,
  `qty_dispatched_kg` decimal(14,3) DEFAULT NULL,
  `invoice_value` decimal(14,2) DEFAULT NULL,
  `power_kwh` decimal(12,2) DEFAULT NULL,
  `steam_tons` decimal(12,3) DEFAULT NULL,
  `water_kl` decimal(12,2) DEFAULT NULL,
  `fuel_l` decimal(12,2) DEFAULT NULL,
  `manpower_present` int(11) DEFAULT NULL,
  `manpower_absent` int(11) DEFAULT NULL,
  `ot_hours` decimal(8,2) DEFAULT NULL,
  `near_miss_count` int(11) DEFAULT 0,
  `first_aid_count` int(11) DEFAULT 0,
  `lti_count` int(11) DEFAULT 0,
  `power_failures_min` int(11) DEFAULT 0,
  `major_breakdowns` int(11) DEFAULT 0,
  `qc_rejections` int(11) DEFAULT 0,
  `highlights` text DEFAULT NULL,
  `issues` text DEFAULT NULL,
  `next_day_plan` text DEFAULT NULL,
  `prepared_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_by_emp_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dj` (`tenant_id`,`journal_date`),
  KEY `idx_dj_status` (`status`,`journal_date`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_emergency_drill_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_emergency_drill_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `drill_no` varchar(32) NOT NULL,
  `drill_date` date NOT NULL,
  `drill_type` varchar(16) NOT NULL,
  `area` varchar(64) DEFAULT NULL,
  `conducted_by_emp_id` bigint(20) DEFAULT NULL,
  `participants_count` int(11) DEFAULT NULL,
  `evacuation_time_sec` int(11) DEFAULT NULL,
  `target_time_sec` int(11) DEFAULT NULL,
  `observations` text DEFAULT NULL,
  `gaps_identified` text DEFAULT NULL,
  `corrective_action` text DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'CONDUCTED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_drill` (`tenant_id`,`drill_no`),
  KEY `idx_drill_date` (`drill_date`,`drill_type`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_energy_reading`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_energy_reading` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `meter_code` varchar(32) NOT NULL,
  `meter_kind` varchar(16) NOT NULL,
  `shift_code` varchar(8) DEFAULT NULL,
  `recorded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `start_reading` decimal(14,3) DEFAULT NULL,
  `end_reading` decimal(14,3) DEFAULT NULL,
  `consumption` decimal(14,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `reading_kwh` decimal(14,2) DEFAULT NULL,
  `demand_kw` decimal(8,2) DEFAULT NULL,
  `power_factor` decimal(4,3) DEFAULT NULL,
  `tariff_inr_per_kwh` decimal(8,3) DEFAULT NULL,
  `cost_inr` decimal(12,2) DEFAULT NULL,
  `reading_type` varchar(16) DEFAULT NULL,
  `area` varchar(32) DEFAULT NULL,
  `recorded_by_emp_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_em_meter_date` (`meter_code`,`recorded_at`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_fg_dispatch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_fg_dispatch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `dispatch_number` varchar(32) NOT NULL,
  `linked_so_id` bigint(20) DEFAULT NULL,
  `customer_name` varchar(256) DEFAULT NULL,
  `source_bin` varchar(64) DEFAULT NULL,
  `loading_bay` varchar(32) DEFAULT NULL,
  `packed_lot_no` varchar(64) DEFAULT NULL,
  `qty_to_dispatch` decimal(14,3) DEFAULT NULL,
  `qty_dispatched` decimal(14,3) DEFAULT NULL,
  `uom` varchar(16) DEFAULT NULL,
  `feed_mode` varchar(16) DEFAULT 'MANUAL',
  `conveyor_id` varchar(32) DEFAULT NULL,
  `vehicle_no` varchar(32) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'PLANNED',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `gate_pass_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `customer_id` bigint(20) DEFAULT NULL,
  `dispatch_date` date DEFAULT NULL,
  `shipping_address` text DEFAULT NULL,
  `shipping_pincode` varchar(10) DEFAULT NULL,
  `shipping_state_code` varchar(8) DEFAULT NULL,
  `pkg_count` int(11) DEFAULT NULL,
  `pkg_type` varchar(16) DEFAULT NULL,
  `gross_weight_kg` decimal(14,3) DEFAULT NULL,
  `net_weight_kg` decimal(14,3) DEFAULT NULL,
  `vehicle_type` varchar(16) DEFAULT NULL,
  `driver_name` varchar(96) DEFAULT NULL,
  `driver_phone` varchar(16) DEFAULT NULL,
  `driver_dl_no` varchar(24) DEFAULT NULL,
  `transporter_name` varchar(96) DEFAULT NULL,
  `gate_out_time` datetime DEFAULT NULL,
  `expected_delivery_date` datetime DEFAULT NULL,
  `weighbridge_in_kg` decimal(12,2) DEFAULT NULL,
  `weighbridge_out_kg` decimal(12,2) DEFAULT NULL,
  `weighbridge_slip_no` varchar(32) DEFAULT NULL,
  `seal_no` varchar(32) DEFAULT NULL,
  `seal_applied_by` varchar(64) DEFAULT NULL,
  `invoice_no` varchar(32) DEFAULT NULL,
  `invoice_value` decimal(14,2) DEFAULT NULL,
  `irn` varchar(64) DEFAULT NULL,
  `ewb_no` varchar(32) DEFAULT NULL,
  `ewb_valid_upto` datetime DEFAULT NULL,
  `qc_check_at_load` tinyint(1) DEFAULT NULL,
  `qc_remarks_at_load` text DEFAULT NULL,
  `pod_received` tinyint(1) NOT NULL DEFAULT 0,
  `pod_received_at` datetime DEFAULT NULL,
  `pod_url` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dispatch_number` (`tenant_id`,`dispatch_number`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_hsn_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_hsn_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hsn_code` varchar(16) NOT NULL,
  `description` varchar(256) NOT NULL,
  `chapter` varchar(4) DEFAULT NULL,
  `cgst_pct` decimal(5,2) NOT NULL,
  `sgst_pct` decimal(5,2) NOT NULL,
  `igst_pct` decimal(5,2) NOT NULL,
  `cess_pct` decimal(5,2) DEFAULT NULL,
  `effective_from` date NOT NULL,
  `effective_to` date DEFAULT NULL,
  `notes` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_hsn` (`hsn_code`,`effective_from`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_inward_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_inward_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `grn_id` bigint(20) NOT NULL,
  `audit_type` varchar(24) NOT NULL,
  `check_item` varchar(96) NOT NULL,
  `result` varchar(8) NOT NULL,
  `observation` varchar(256) DEFAULT NULL,
  `corrective_action` varchar(256) DEFAULT NULL,
  `audited_by_emp_id` bigint(20) DEFAULT NULL,
  `audited_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_aud_grn` (`grn_id`,`audit_type`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_item_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_item_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) NOT NULL,
  `short_name` varchar(64) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `item_type` varchar(16) NOT NULL DEFAULT 'FINISHED',
  `category` varchar(64) DEFAULT NULL,
  `sub_category` varchar(64) DEFAULT NULL,
  `brand` varchar(64) DEFAULT NULL,
  `uom` varchar(16) NOT NULL DEFAULT 'NOS',
  `alt_uom` varchar(16) DEFAULT NULL,
  `uom_conversion_factor` decimal(12,4) DEFAULT NULL,
  `hsn_code` varchar(16) DEFAULT NULL,
  `sac_code` varchar(16) DEFAULT NULL,
  `gst_rate_pct` decimal(5,2) DEFAULT NULL,
  `cess_pct` decimal(5,2) DEFAULT NULL,
  `is_batch_tracked` tinyint(1) NOT NULL DEFAULT 0,
  `is_serial_tracked` tinyint(1) NOT NULL DEFAULT 0,
  `shelf_life_days` int(11) DEFAULT NULL,
  `storage_method` varchar(16) DEFAULT NULL,
  `abc_class` varchar(1) DEFAULT NULL,
  `reorder_min` decimal(12,3) DEFAULT NULL,
  `reorder_max` decimal(12,3) DEFAULT NULL,
  `safety_stock` decimal(12,3) DEFAULT NULL,
  `eoq` decimal(12,3) DEFAULT NULL,
  `lead_time_days` int(11) DEFAULT NULL,
  `std_cost` decimal(12,2) DEFAULT NULL,
  `list_price` decimal(12,2) DEFAULT NULL,
  `vendor_default_id` bigint(20) DEFAULT NULL,
  `rack` varchar(16) DEFAULT NULL,
  `bin_default` varchar(16) DEFAULT NULL,
  `weight_per_unit` decimal(12,3) DEFAULT NULL,
  `weight_uom` varchar(8) DEFAULT NULL,
  `density_at_15c` decimal(8,4) DEFAULT NULL,
  `qc_required` tinyint(1) NOT NULL DEFAULT 0,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_item_code` (`tenant_id`,`item_code`),
  KEY `idx_item_hsn` (`hsn_code`),
  KEY `idx_item_cat` (`category`,`sub_category`),
  KEY `idx_item_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_job_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_job_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `jc_number` varchar(32) NOT NULL,
  `production_order_id` bigint(20) DEFAULT NULL,
  `operator_id` bigint(20) DEFAULT NULL,
  `shift_code` varchar(16) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'OPEN',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `output_qty` decimal(14,3) DEFAULT NULL,
  `output_lot_no` varchar(64) DEFAULT NULL,
  `output_bin` varchar(64) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `closed_at` timestamp NULL DEFAULT NULL,
  `shift_start` datetime DEFAULT NULL,
  `shift_end` datetime DEFAULT NULL,
  `planned_qty` decimal(14,3) DEFAULT NULL,
  `scrap_qty` decimal(14,3) DEFAULT NULL,
  `rework_qty` decimal(14,3) DEFAULT NULL,
  `downtime_min` int(11) DEFAULT NULL,
  `downtime_reason` varchar(64) DEFAULT NULL,
  `energy_kwh` decimal(12,2) DEFAULT NULL,
  `steam_consumed_kg` decimal(12,2) DEFAULT NULL,
  `water_consumed_l` decimal(12,2) DEFAULT NULL,
  `qc_sample_drawn` tinyint(1) NOT NULL DEFAULT 0,
  `qc_result` varchar(16) DEFAULT NULL,
  `supervisor_signoff_emp_id` bigint(20) DEFAULT NULL,
  `supervisor_signoff_at` datetime DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_jc_number` (`tenant_id`,`jc_number`),
  KEY `idx_jc_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_job_card_consumption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_job_card_consumption` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `jc_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `planned_qty` decimal(14,3) DEFAULT NULL,
  `actual_qty` decimal(14,3) NOT NULL,
  `variance_qty` decimal(14,3) GENERATED ALWAYS AS (coalesce(`actual_qty`,0) - coalesce(`planned_qty`,0)) STORED,
  `variance_pct` decimal(6,2) GENERATED ALWAYS AS (case when `planned_qty` > 0 then (`actual_qty` - `planned_qty`) / `planned_qty` * 100 else NULL end) STORED,
  `uom` varchar(16) NOT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `unit_cost` decimal(12,2) DEFAULT NULL,
  `total_cost` decimal(14,2) GENERATED ALWAYS AS (coalesce(`actual_qty`,0) * coalesce(`unit_cost`,0)) STORED,
  PRIMARY KEY (`id`),
  KEY `idx_jcc_jc` (`jc_id`),
  KEY `idx_jcc_item` (`item_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_job_work_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_job_work_issue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `jw_no` varchar(32) NOT NULL,
  `issue_date` date NOT NULL,
  `vendor_code` varchar(48) NOT NULL,
  `vendor_name` varchar(160) DEFAULT NULL,
  `process` varchar(64) NOT NULL,
  `expected_return_date` date DEFAULT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(160) DEFAULT NULL,
  `qty_issued` decimal(14,3) NOT NULL,
  `qty_returned` decimal(14,3) DEFAULT NULL,
  `shrinkage_qty` decimal(14,3) GENERATED ALWAYS AS (`qty_issued` - coalesce(`qty_returned`,0)) STORED,
  `uom` varchar(16) NOT NULL,
  `lot_no_out` varchar(64) DEFAULT NULL,
  `lot_no_in` varchar(64) DEFAULT NULL,
  `rate_per_unit` decimal(12,2) DEFAULT NULL,
  `charge_amount` decimal(14,2) DEFAULT NULL,
  `delivery_challan_no` varchar(48) DEFAULT NULL,
  `ewb_no` varchar(32) DEFAULT NULL,
  `gst_pct` decimal(5,2) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ISSUED',
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_jw` (`tenant_id`,`jw_no`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_kyc_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_kyc_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `party_kind` varchar(16) NOT NULL,
  `party_id` bigint(20) DEFAULT NULL,
  `party_name` varchar(256) NOT NULL,
  `gstin` varchar(16) DEFAULT NULL,
  `pan` varchar(16) DEFAULT NULL,
  `fssai_no` varchar(20) DEFAULT NULL,
  `bank_account_masked` varchar(32) DEFAULT NULL,
  `ifsc` varchar(16) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'PENDING',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `verified_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_kyc_party` (`party_kind`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_license_renewal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_license_renewal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `licence_code` varchar(32) NOT NULL,
  `licence_type` varchar(32) NOT NULL,
  `licence_no` varchar(64) NOT NULL,
  `issuing_authority` varchar(96) NOT NULL,
  `issue_date` date NOT NULL,
  `valid_from` date DEFAULT NULL,
  `valid_to` date NOT NULL,
  `renewal_fee` decimal(12,2) DEFAULT NULL,
  `renewal_cycle_months` int(11) DEFAULT NULL,
  `document_url` varchar(255) DEFAULT NULL,
  `responsible_emp_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `notes` text DEFAULT NULL,
  `days_to_expiry` int(11) GENERATED ALWAYS AS (to_days(`valid_to`) - to_days(curdate())) VIRTUAL,
  `expiry_bucket` varchar(16) GENERATED ALWAYS AS (case when to_days(`valid_to`) - to_days(curdate()) < 0 then 'EXPIRED' when to_days(`valid_to`) - to_days(curdate()) <= 30 then 'CRITICAL' when to_days(`valid_to`) - to_days(curdate()) <= 90 then 'WARNING' else 'OK' end) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lic` (`tenant_id`,`licence_code`),
  KEY `idx_lic_type` (`licence_type`,`valid_to`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_maintenance_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_maintenance_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `mo_number` varchar(32) NOT NULL,
  `asset_id` bigint(20) DEFAULT NULL,
  `kind` varchar(16) NOT NULL,
  `reported_by` bigint(20) DEFAULT NULL,
  `reported_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `description` text DEFAULT NULL,
  `priority` varchar(8) NOT NULL DEFAULT 'P3',
  `status` varchar(32) NOT NULL DEFAULT 'REPORTED',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `closed_at` timestamp NULL DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_mo_number` (`tenant_id`,`mo_number`),
  KEY `idx_mo_status` (`status`,`priority`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_maintenance_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_maintenance_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `asset_code` varchar(32) NOT NULL,
  `asset_name` varchar(96) DEFAULT NULL,
  `maintenance_type` varchar(16) NOT NULL,
  `frequency` varchar(16) NOT NULL,
  `frequency_value` int(11) DEFAULT NULL,
  `task_description` varchar(256) NOT NULL,
  `responsible_emp_id` bigint(20) DEFAULT NULL,
  `next_due_date` date DEFAULT NULL,
  `last_done_date` date DEFAULT NULL,
  `spare_parts_required` text DEFAULT NULL,
  `estimated_duration_min` int(11) DEFAULT NULL,
  `shutdown_required` tinyint(1) NOT NULL DEFAULT 0,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_msched` (`tenant_id`,`asset_code`,`task_description`(64)),
  KEY `idx_msched_due` (`next_due_date`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_orderapp_dump`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_orderapp_dump` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `order_ref` varchar(64) NOT NULL,
  `customer_name` varchar(128) NOT NULL,
  `customer_route` varchar(64) DEFAULT NULL,
  `sku_code` varchar(64) NOT NULL,
  `ordered_qty_cases` int(11) NOT NULL,
  `ordered_weight_kg` decimal(10,3) DEFAULT NULL,
  `expected_dispatch_date` date DEFAULT NULL,
  `status` varchar(24) NOT NULL DEFAULT 'NEW',
  `loaded_qty_cases` int(11) DEFAULT NULL,
  `loaded_weight_kg` decimal(10,3) DEFAULT NULL,
  `variance_pct` decimal(6,2) DEFAULT NULL,
  `variance_reason` text DEFAULT NULL,
  `manager_approval_id` bigint(20) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `uploaded_by` bigint(20) DEFAULT NULL,
  `reconciled_at` timestamp NULL DEFAULT NULL,
  `route_stops` int(11) DEFAULT NULL,
  `eta_at` datetime DEFAULT NULL,
  `dispatch_gp_id` bigint(20) DEFAULT NULL,
  `priority` varchar(8) NOT NULL DEFAULT 'NORMAL',
  `credit_status` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_orderapp_status` (`status`),
  KEY `ix_orderapp_route` (`customer_route`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_packing_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_packing_run` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `pr_number` varchar(32) NOT NULL,
  `from_bulk_lot` varchar(64) DEFAULT NULL,
  `from_bin` varchar(64) DEFAULT NULL,
  `packaging_sku` varchar(64) DEFAULT NULL,
  `packed_lot_no` varchar(64) DEFAULT NULL,
  `to_bin` varchar(64) DEFAULT NULL,
  `planned_packs` decimal(14,3) DEFAULT NULL,
  `actual_packs` decimal(14,3) DEFAULT NULL,
  `spoil_packs` decimal(14,3) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'OPEN',
  `planned_total_pieces` int(11) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `packing_line` varchar(32) DEFAULT NULL,
  `machine_code` varchar(32) DEFAULT NULL,
  `operator_emp_id` bigint(20) DEFAULT NULL,
  `shift_code` varchar(16) DEFAULT NULL,
  `shift_start` datetime DEFAULT NULL,
  `shift_end` datetime DEFAULT NULL,
  `pack_size` decimal(8,3) DEFAULT NULL,
  `pack_uom` varchar(8) DEFAULT NULL,
  `bulk_qty_consumed` decimal(14,3) DEFAULT NULL,
  `bulk_qty_uom` varchar(8) DEFAULT NULL,
  `caps_consumed` int(11) DEFAULT NULL,
  `bottles_consumed` int(11) DEFAULT NULL,
  `labels_consumed` int(11) DEFAULT NULL,
  `cartons_consumed` int(11) DEFAULT NULL,
  `spoil_reason` varchar(64) DEFAULT NULL,
  `yield_pct` decimal(5,2) GENERATED ALWAYS AS (case when `planned_packs` > 0 then coalesce(`actual_packs`,0) / `planned_packs` * 100 else NULL end) STORED,
  `spoil_pct` decimal(5,2) GENERATED ALWAYS AS (case when `actual_packs` > 0 then coalesce(`spoil_packs`,0) / `actual_packs` * 100 else NULL end) STORED,
  `downtime_min` int(11) DEFAULT NULL,
  `downtime_reason` varchar(64) DEFAULT NULL,
  `qc_status` varchar(16) DEFAULT NULL,
  `avg_pack_weight_g` decimal(8,2) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pr_number` (`tenant_id`,`pr_number`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_packing_scan_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_packing_scan_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `packing_run_id` bigint(20) DEFAULT NULL,
  `bay_code` varchar(32) DEFAULT NULL,
  `scanned_barcode` varchar(64) NOT NULL,
  `sku_code` varchar(64) DEFAULT NULL,
  `offer_variant` varchar(64) DEFAULT NULL,
  `bundle_count` int(11) DEFAULT NULL,
  `pieces_total` int(11) DEFAULT NULL,
  `outcome` varchar(16) NOT NULL,
  `reject_reason` varchar(64) DEFAULT NULL,
  `operator_id` bigint(20) DEFAULT NULL,
  `scanned_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `ix_pck_scan_run` (`packing_run_id`),
  KEY `ix_pck_scan_outcome` (`outcome`),
  KEY `ix_pck_scan_sku` (`sku_code`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_plant_transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_plant_transfer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `transfer_no` varchar(32) NOT NULL,
  `from_plant` varchar(32) NOT NULL,
  `to_plant` varchar(32) NOT NULL,
  `reason` varchar(64) DEFAULT NULL,
  `required_by_date` date DEFAULT NULL,
  `initiated_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `loaded_at` datetime DEFAULT NULL,
  `load_supervisor_emp_id` bigint(20) DEFAULT NULL,
  `vehicle_no` varchar(16) DEFAULT NULL,
  `driver_name` varchar(96) DEFAULT NULL,
  `driver_phone` varchar(16) DEFAULT NULL,
  `transporter` varchar(96) DEFAULT NULL,
  `weighbridge_out_loaded_kg` decimal(12,2) DEFAULT NULL,
  `weighbridge_out_slip` varchar(32) DEFAULT NULL,
  `out_gate_pass_no` varchar(32) DEFAULT NULL,
  `out_gate_pass_at` datetime DEFAULT NULL,
  `seal_no` varchar(32) DEFAULT NULL,
  `dispatched_at` datetime DEFAULT NULL,
  `expected_arrival` datetime DEFAULT NULL,
  `arrived_at` datetime DEFAULT NULL,
  `weighbridge_in_loaded_kg` decimal(12,2) DEFAULT NULL,
  `weighbridge_in_slip` varchar(32) DEFAULT NULL,
  `weight_variance_kg` decimal(12,2) GENERATED ALWAYS AS (coalesce(`weighbridge_out_loaded_kg`,0) - coalesce(`weighbridge_in_loaded_kg`,0)) STORED,
  `in_gate_pass_no` varchar(32) DEFAULT NULL,
  `in_gate_pass_at` datetime DEFAULT NULL,
  `seal_intact_on_arrival` tinyint(1) DEFAULT NULL,
  `unloaded_at` datetime DEFAULT NULL,
  `unload_supervisor_emp_id` bigint(20) DEFAULT NULL,
  `stocked_at` datetime DEFAULT NULL,
  `stored_in_warehouse` varchar(32) DEFAULT NULL,
  `status` varchar(24) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_xfer` (`tenant_id`,`transfer_no`),
  KEY `idx_xfer_status` (`status`,`required_by_date`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_plant_transfer_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_plant_transfer_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `transfer_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(160) DEFAULT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `qty_requested` decimal(14,3) NOT NULL,
  `qty_loaded` decimal(14,3) DEFAULT NULL,
  `qty_received` decimal(14,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_xfer_item` (`transfer_id`,`item_code`),
  CONSTRAINT `fk_xfer_line` FOREIGN KEY (`transfer_id`) REFERENCES `m5_plant_transfer` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_process_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_process_batch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `batch_no` varchar(48) NOT NULL,
  `process_code` varchar(32) NOT NULL,
  `product_code` varchar(64) NOT NULL,
  `product_name` varchar(160) DEFAULT NULL,
  `bom_code` varchar(64) DEFAULT NULL,
  `source_grn_id` bigint(20) DEFAULT NULL,
  `source_lot_no` varchar(64) DEFAULT NULL,
  `planned_input_qty` decimal(14,3) NOT NULL,
  `planned_output_qty` decimal(14,3) DEFAULT NULL,
  `actual_input_qty` decimal(14,3) DEFAULT NULL,
  `actual_output_qty` decimal(14,3) DEFAULT NULL,
  `total_byproduct_qty` decimal(14,3) DEFAULT NULL,
  `total_loss_qty` decimal(14,3) DEFAULT NULL,
  `overall_yield_pct` decimal(5,2) GENERATED ALWAYS AS (case when `actual_input_qty` > 0 then coalesce(`actual_output_qty`,0) / `actual_input_qty` * 100 else NULL end) STORED,
  `started_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `current_stage` varchar(32) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PLANNED',
  `supervisor_emp_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_batch` (`tenant_id`,`batch_no`),
  KEY `idx_pb_proc` (`process_code`,`status`),
  KEY `idx_pb_source` (`source_lot_no`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_process_stage_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_process_stage_catalog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `process_code` varchar(32) NOT NULL,
  `stage_code` varchar(32) NOT NULL,
  `stage_label` varchar(96) NOT NULL,
  `stage_order` int(11) NOT NULL,
  `stage_type` varchar(16) NOT NULL,
  `expected_duration_min` int(11) DEFAULT NULL,
  `expected_yield_pct` decimal(5,2) DEFAULT NULL,
  `byproduct_pct` decimal(5,2) DEFAULT NULL,
  `loss_pct` decimal(5,2) DEFAULT NULL,
  `byproduct_item_code` varchar(64) DEFAULT NULL,
  `primary_equipment` varchar(64) DEFAULT NULL,
  `qc_required` tinyint(1) NOT NULL DEFAULT 0,
  `notes` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_stage` (`tenant_id`,`process_code`,`stage_code`,`stage_order`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_process_stage_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_process_stage_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) NOT NULL,
  `stage_code` varchar(32) NOT NULL,
  `stage_order` int(11) NOT NULL,
  `started_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `duration_min` int(11) GENERATED ALWAYS AS (case when `started_at` is not null and `completed_at` is not null then timestampdiff(MINUTE,`started_at`,`completed_at`) else NULL end) VIRTUAL,
  `input_qty` decimal(14,3) NOT NULL,
  `output_qty` decimal(14,3) DEFAULT NULL,
  `byproduct_qty` decimal(14,3) DEFAULT NULL,
  `byproduct_item_code` varchar(64) DEFAULT NULL,
  `byproduct_lot_no` varchar(64) DEFAULT NULL,
  `loss_qty` decimal(14,3) DEFAULT NULL,
  `stage_yield_pct` decimal(5,2) GENERATED ALWAYS AS (case when `input_qty` > 0 then coalesce(`output_qty`,0) / `input_qty` * 100 else NULL end) STORED,
  `expected_yield_pct` decimal(5,2) DEFAULT NULL,
  `yield_variance_pct` decimal(5,2) GENERATED ALWAYS AS (case when `input_qty` > 0 then coalesce(`output_qty`,0) / `input_qty` * 100 - coalesce(`expected_yield_pct`,0) else NULL end) STORED,
  `operator_emp_id` bigint(20) DEFAULT NULL,
  `shift_code` varchar(8) DEFAULT NULL,
  `equipment_code` varchar(32) DEFAULT NULL,
  `temperature_c` decimal(6,2) DEFAULT NULL,
  `pressure_bar` decimal(6,2) DEFAULT NULL,
  `rpm` int(11) DEFAULT NULL,
  `energy_kwh` decimal(10,2) DEFAULT NULL,
  `steam_kg` decimal(10,2) DEFAULT NULL,
  `water_l` decimal(10,2) DEFAULT NULL,
  `chemicals_used` text DEFAULT NULL,
  `qc_sample_drawn` tinyint(1) NOT NULL DEFAULT 0,
  `qc_result` varchar(16) DEFAULT NULL,
  `qc_remarks` text DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_psl_batch` (`batch_id`,`stage_order`),
  CONSTRAINT `fk_psl_batch` FOREIGN KEY (`batch_id`) REFERENCES `m5_process_batch` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_production_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_production_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `po_number` varchar(32) NOT NULL,
  `product_code` varchar(64) NOT NULL,
  `product_name` varchar(256) DEFAULT NULL,
  `planned_qty` decimal(14,3) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `production_line` varchar(32) DEFAULT NULL,
  `required_by_date` date DEFAULT NULL,
  `linked_so_id` bigint(20) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'PLANNED',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `bom_code` varchar(64) DEFAULT NULL,
  `planned_start_date` datetime DEFAULT NULL,
  `planned_end_date` datetime DEFAULT NULL,
  `actual_start_date` datetime DEFAULT NULL,
  `actual_end_date` datetime DEFAULT NULL,
  `actual_qty_good` decimal(14,3) DEFAULT NULL,
  `actual_qty_rework` decimal(14,3) DEFAULT NULL,
  `actual_qty_scrap` decimal(14,3) DEFAULT NULL,
  `yield_pct` decimal(5,2) GENERATED ALWAYS AS (case when `planned_qty` > 0 then coalesce(`actual_qty_good`,0) / `planned_qty` * 100 else NULL end) STORED,
  `scrap_pct` decimal(5,2) GENERATED ALWAYS AS (case when `planned_qty` > 0 then coalesce(`actual_qty_scrap`,0) / `planned_qty` * 100 else NULL end) STORED,
  `planned_runtime_min` int(11) DEFAULT NULL,
  `actual_runtime_min` int(11) DEFAULT NULL,
  `planned_downtime_min` int(11) DEFAULT NULL,
  `actual_downtime_min` int(11) DEFAULT NULL,
  `oee_availability_pct` decimal(5,2) DEFAULT NULL,
  `oee_performance_pct` decimal(5,2) DEFAULT NULL,
  `oee_quality_pct` decimal(5,2) DEFAULT NULL,
  `oee_overall_pct` decimal(5,2) GENERATED ALWAYS AS (coalesce(`oee_availability_pct`,0) * coalesce(`oee_performance_pct`,0) * coalesce(`oee_quality_pct`,0) / 10000) STORED,
  `work_center` varchar(32) DEFAULT NULL,
  `supervisor_emp_id` bigint(20) DEFAULT NULL,
  `qc_passed` tinyint(1) DEFAULT NULL,
  `qc_remarks` text DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_m5prod_number` (`tenant_id`,`po_number`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `po_number` varchar(32) NOT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  `vendor_name` varchar(256) DEFAULT NULL,
  `total_value` decimal(14,2) DEFAULT NULL,
  `currency` varchar(4) NOT NULL DEFAULT 'INR',
  `required_by_date` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_m5po_number` (`tenant_id`,`po_number`),
  KEY `idx_m5po_status` (`tenant_id`,`status`,`current_step`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_purchase_order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_purchase_order_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `po_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `qty_ordered` decimal(12,3) NOT NULL,
  `qty_received` decimal(12,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `rate` decimal(12,4) DEFAULT NULL,
  `line_value` decimal(14,2) DEFAULT NULL,
  `tax_pct` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_m5pol_po` (`po_id`),
  KEY `idx_m5pol_item` (`item_code`),
  CONSTRAINT `fk_m5pol_po` FOREIGN KEY (`po_id`) REFERENCES `m5_purchase_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_qc_parameter_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_qc_parameter_catalog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `item_code` varchar(64) NOT NULL,
  `parameter` varchar(64) NOT NULL,
  `method` varchar(32) DEFAULT NULL,
  `unit` varchar(16) DEFAULT NULL,
  `spec_min` decimal(12,4) DEFAULT NULL,
  `spec_max` decimal(12,4) DEFAULT NULL,
  `is_mandatory` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_qc_cat` (`tenant_id`,`item_code`,`parameter`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_qc_test_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_qc_test_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `source_type` varchar(16) NOT NULL,
  `source_ref_id` bigint(20) NOT NULL,
  `item_code` varchar(64) DEFAULT NULL,
  `parameter` varchar(64) NOT NULL,
  `method` varchar(32) DEFAULT NULL,
  `unit` varchar(16) DEFAULT NULL,
  `spec_min` decimal(12,4) DEFAULT NULL,
  `spec_max` decimal(12,4) DEFAULT NULL,
  `measured_value` decimal(12,4) DEFAULT NULL,
  `pass_fail` varchar(8) DEFAULT NULL,
  `tested_at` datetime DEFAULT NULL,
  `tested_by_emp_id` bigint(20) DEFAULT NULL,
  `remarks` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_qc_src` (`source_type`,`source_ref_id`),
  KEY `idx_qc_item` (`item_code`,`parameter`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_quality_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_quality_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `plan_code` varchar(64) NOT NULL,
  `applies_to_item` varchar(64) DEFAULT NULL,
  `applies_to_stage` varchar(32) DEFAULT NULL,
  `sample_size` int(11) NOT NULL DEFAULT 1,
  `frequency` varchar(32) NOT NULL,
  `parameters_json` text DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_qplan` (`tenant_id`,`plan_code`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_quality_test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_quality_test` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `test_number` varchar(32) NOT NULL,
  `plan_id` bigint(20) DEFAULT NULL,
  `taken_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `item_code` varchar(64) DEFAULT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `bin_code` varchar(64) DEFAULT NULL,
  `result` varchar(16) NOT NULL DEFAULT 'PASS',
  `parameters_json` text DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'CLOSED',
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_qtest` (`tenant_id`,`test_number`),
  KEY `idx_qtest_item` (`item_code`,`taken_at`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_return_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_return_note` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `rn_number` varchar(32) NOT NULL,
  `return_kind` varchar(16) NOT NULL,
  `source_ref` varchar(128) DEFAULT NULL,
  `customer_or_dept` varchar(256) DEFAULT NULL,
  `reason` varchar(256) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'REQUESTED',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `qty_returned` decimal(14,3) DEFAULT NULL,
  `destination` varchar(16) DEFAULT NULL,
  `item_code` varchar(64) DEFAULT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `to_bin` varchar(64) DEFAULT NULL,
  `uom` varchar(16) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rn_number` (`tenant_id`,`rn_number`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_rfq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_rfq` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `rfq_no` varchar(32) NOT NULL,
  `rfq_date` date NOT NULL,
  `source_mr_id` bigint(20) DEFAULT NULL,
  `buyer_emp_id` bigint(20) DEFAULT NULL,
  `closing_date` date DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_rfq` (`tenant_id`,`rfq_no`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_rfq_quote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_rfq_quote` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rfq_vendor_id` bigint(20) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(160) DEFAULT NULL,
  `qty` decimal(14,3) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `unit_price` decimal(14,4) DEFAULT NULL,
  `delivery_days` int(11) DEFAULT NULL,
  `credit_days` int(11) DEFAULT NULL,
  `freight_terms` varchar(32) DEFAULT NULL,
  `payment_terms` varchar(64) DEFAULT NULL,
  `taxes_pct` decimal(5,2) DEFAULT NULL,
  `landed_unit_cost` decimal(14,4) DEFAULT NULL,
  `is_selected` tinyint(1) NOT NULL DEFAULT 0,
  `remarks` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_quote_rfqv` (`rfq_vendor_id`),
  KEY `idx_quote_item` (`item_code`),
  CONSTRAINT `fk_quote_rfqv` FOREIGN KEY (`rfq_vendor_id`) REFERENCES `m5_rfq_vendor` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_rfq_vendor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_rfq_vendor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rfq_id` bigint(20) NOT NULL,
  `vendor_code` varchar(48) NOT NULL,
  `vendor_name` varchar(160) DEFAULT NULL,
  `sent_at` datetime DEFAULT NULL,
  `response_received_at` datetime DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'INVITED',
  PRIMARY KEY (`id`),
  KEY `fk_rfqv_rfq` (`rfq_id`),
  CONSTRAINT `fk_rfqv_rfq` FOREIGN KEY (`rfq_id`) REFERENCES `m5_rfq` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_safety_committee_meeting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_safety_committee_meeting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `meeting_no` varchar(32) NOT NULL,
  `meeting_date` date NOT NULL,
  `meeting_type` varchar(16) NOT NULL,
  `chair_emp_id` bigint(20) DEFAULT NULL,
  `attendees` text DEFAULT NULL,
  `attendees_count` int(11) DEFAULT NULL,
  `agenda` text DEFAULT NULL,
  `key_decisions` text DEFAULT NULL,
  `action_items` text DEFAULT NULL,
  `minutes_url` varchar(255) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'CLOSED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_safety_meet` (`tenant_id`,`meeting_no`),
  KEY `idx_meet_date` (`meeting_date`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_sales_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_sales_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `so_number` varchar(32) NOT NULL,
  `customer_name` varchar(256) NOT NULL,
  `customer_id` bigint(20) DEFAULT NULL,
  `customer_gstin` varchar(16) DEFAULT NULL,
  `total_value` decimal(14,2) DEFAULT NULL,
  `currency` varchar(4) NOT NULL DEFAULT 'INR',
  `required_by_date` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `gate_pass_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `customer_phone` varchar(16) DEFAULT NULL,
  `customer_email` varchar(96) DEFAULT NULL,
  `billing_address` text DEFAULT NULL,
  `shipping_address` text DEFAULT NULL,
  `shipping_state_code` varchar(8) DEFAULT NULL,
  `so_date` date DEFAULT NULL,
  `quote_ref` varchar(32) DEFAULT NULL,
  `po_ref_no` varchar(32) DEFAULT NULL,
  `po_ref_date` date DEFAULT NULL,
  `payment_terms` varchar(32) DEFAULT NULL,
  `payment_received_pct` decimal(5,2) DEFAULT NULL,
  `delivery_terms` varchar(32) DEFAULT NULL,
  `sales_person` varchar(96) DEFAULT NULL,
  `discount_pct` decimal(5,2) DEFAULT NULL,
  `subtotal` decimal(14,2) DEFAULT NULL,
  `tax_amount` decimal(14,2) DEFAULT NULL,
  `freight_amount` decimal(14,2) DEFAULT NULL,
  `credit_check_status` varchar(16) DEFAULT NULL,
  `credit_limit` decimal(14,2) DEFAULT NULL,
  `credit_used` decimal(14,2) DEFAULT NULL,
  `lost_reason` varchar(64) DEFAULT NULL,
  `expected_dispatch_date` date DEFAULT NULL,
  `actual_dispatch_date` date DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_m5so_number` (`tenant_id`,`so_number`),
  KEY `idx_m5so_status` (`tenant_id`,`status`,`current_step`),
  KEY `idx_m5so_gp` (`gate_pass_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_sales_order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_sales_order_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `so_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `qty_ordered` decimal(12,3) NOT NULL,
  `qty_dispatched` decimal(12,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `rate` decimal(12,4) DEFAULT NULL,
  `line_value` decimal(14,2) DEFAULT NULL,
  `tax_pct` decimal(5,2) DEFAULT NULL,
  `hsn_code` varchar(16) DEFAULT NULL,
  `discount_pct` decimal(5,2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `taxable_amount` decimal(14,2) DEFAULT NULL,
  `cgst_pct` decimal(5,2) DEFAULT NULL,
  `sgst_pct` decimal(5,2) DEFAULT NULL,
  `igst_pct` decimal(5,2) DEFAULT NULL,
  `tax_amount` decimal(14,2) DEFAULT NULL,
  `line_total` decimal(14,2) DEFAULT NULL,
  `qty_reserved` decimal(12,3) NOT NULL DEFAULT 0.000,
  `line_status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `route_id` varchar(64) DEFAULT NULL,
  `sku_barcode` varchar(64) DEFAULT NULL,
  `offer` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_m5sol_so` (`so_id`),
  KEY `idx_m5sol_item` (`item_code`),
  CONSTRAINT `fk_m5sol_so` FOREIGN KEY (`so_id`) REFERENCES `m5_sales_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_shift_handover`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_shift_handover` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `handed_off_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `outgoing_shift` varchar(8) NOT NULL,
  `incoming_shift` varchar(8) NOT NULL,
  `outgoing_supervisor_id` bigint(20) DEFAULT NULL,
  `incoming_supervisor_id` bigint(20) DEFAULT NULL,
  `open_jc_count` int(11) DEFAULT 0,
  `open_dispatch_count` int(11) DEFAULT 0,
  `notes` text DEFAULT NULL,
  `shift_date` date DEFAULT NULL,
  `oil_produced_kg` decimal(14,3) DEFAULT NULL,
  `cake_produced_kg` decimal(14,3) DEFAULT NULL,
  `packs_produced` int(11) DEFAULT NULL,
  `power_kwh` decimal(12,2) DEFAULT NULL,
  `open_qc_holds` int(11) DEFAULT NULL,
  `breakdowns_min` int(11) DEFAULT NULL,
  `safety_incidents` int(11) DEFAULT 0,
  `pending_actions` text DEFAULT NULL,
  `visitor_log` text DEFAULT NULL,
  `environment_remarks` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_handover_ts` (`handed_off_at`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_shift_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_shift_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `shift_date` date NOT NULL,
  `shift_code` varchar(8) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `supervisor_id` bigint(20) DEFAULT NULL,
  `notes` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_shift` (`tenant_id`,`shift_date`,`shift_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_sku_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_sku_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `sku_code` varchar(64) NOT NULL,
  `brand` varchar(64) NOT NULL,
  `product_family` varchar(64) NOT NULL,
  `pack_size_ml` int(11) DEFAULT NULL,
  `pack_pieces_per_case` int(11) NOT NULL DEFAULT 12,
  `carton_weight_kg` decimal(8,3) NOT NULL,
  `barcode` varchar(64) DEFAULT NULL,
  `offer_variant` varchar(64) DEFAULT NULL,
  `bundle_pieces` int(11) NOT NULL DEFAULT 15,
  `mrp_inr` decimal(10,2) DEFAULT NULL,
  `hsn_code` varchar(16) DEFAULT NULL,
  `gst_rate_pct` decimal(4,2) NOT NULL DEFAULT 5.00,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `uom_primary` varchar(16) NOT NULL DEFAULT 'KG',
  `uom_secondary` varchar(16) DEFAULT NULL,
  `uom_conversion` decimal(10,4) DEFAULT NULL COMMENT 'e.g. 1 case = 12 pcs',
  `shelf_life_days` int(11) DEFAULT NULL,
  `batch_tracked` tinyint(1) NOT NULL DEFAULT 1,
  `fefo_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'First-Expiry-First-Out picking',
  `reorder_point` int(11) DEFAULT NULL,
  `safety_stock` int(11) DEFAULT NULL,
  `abc_class` varchar(1) DEFAULT NULL COMMENT 'A/B/C by volume',
  `cess_pct` decimal(4,2) NOT NULL DEFAULT 0.00,
  `purchase_price` decimal(10,2) DEFAULT NULL,
  `sale_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sku_code` (`tenant_id`,`sku_code`),
  KEY `ix_sku_barcode` (`barcode`),
  KEY `ix_sku_brand_family` (`brand`,`product_family`)
) ENGINE=InnoDB AUTO_INCREMENT=951 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_stock_balance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_stock_balance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `bin_code` varchar(64) NOT NULL,
  `warehouse` varchar(32) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `qty_on_hand` decimal(14,3) NOT NULL DEFAULT 0.000,
  `qty_reserved` decimal(14,3) NOT NULL DEFAULT 0.000,
  `last_movement_at` timestamp NULL DEFAULT NULL,
  `rack` varchar(16) DEFAULT NULL,
  `qty_available` decimal(14,3) GENERATED ALWAYS AS (`qty_on_hand` - `qty_reserved`) STORED,
  `qty_in_qc` decimal(14,3) NOT NULL DEFAULT 0.000,
  `qty_on_hold` decimal(14,3) NOT NULL DEFAULT 0.000,
  `qty_in_transit` decimal(14,3) NOT NULL DEFAULT 0.000,
  `receipt_date` date DEFAULT NULL,
  `mfg_date` date DEFAULT NULL,
  `exp_date` date DEFAULT NULL,
  `days_in_stock` int(11) GENERATED ALWAYS AS (to_days(curdate()) - to_days(`receipt_date`)) VIRTUAL,
  `days_to_expiry` int(11) GENERATED ALWAYS AS (to_days(`exp_date`) - to_days(curdate())) VIRTUAL,
  `aging_bucket` varchar(8) GENERATED ALWAYS AS (case when to_days(curdate()) - to_days(`receipt_date`) <= 30 then '0-30' when to_days(curdate()) - to_days(`receipt_date`) <= 60 then '31-60' when to_days(curdate()) - to_days(`receipt_date`) <= 90 then '61-90' when to_days(curdate()) - to_days(`receipt_date`) <= 180 then '91-180' else '180+' end) VIRTUAL,
  `unit_cost` decimal(14,2) DEFAULT NULL,
  `valuation` decimal(14,2) GENERATED ALWAYS AS (`qty_on_hand` * coalesce(`unit_cost`,0)) STORED,
  `hold_reason` varchar(64) DEFAULT NULL,
  `qc_status` varchar(16) DEFAULT NULL,
  `last_count_at` datetime DEFAULT NULL,
  `abc_class` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_balance` (`tenant_id`,`item_code`,`lot_no`,`bin_code`),
  KEY `idx_balance_item` (`item_code`),
  KEY `idx_balance_bin` (`bin_code`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_stock_movement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_stock_movement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `ts` timestamp NOT NULL DEFAULT current_timestamp(),
  `kind` varchar(16) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `lot_no` varchar(64) DEFAULT NULL,
  `from_bin` varchar(64) DEFAULT NULL,
  `to_bin` varchar(64) DEFAULT NULL,
  `qty` decimal(14,3) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `ref_type` varchar(32) DEFAULT NULL,
  `ref_id` bigint(20) DEFAULT NULL,
  `ref_number` varchar(64) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `warehouse` varchar(32) DEFAULT NULL,
  `unit_cost` decimal(14,2) DEFAULT NULL,
  `value_amount` decimal(14,2) GENERATED ALWAYS AS (`qty` * coalesce(`unit_cost`,0)) STORED,
  `reason` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mvt_item` (`item_code`,`ts`),
  KEY `idx_mvt_kind` (`kind`,`ts`),
  KEY `idx_mvt_ref` (`ref_type`,`ref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_subscription` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `sub_number` varchar(32) NOT NULL,
  `customer_name` varchar(256) NOT NULL,
  `product_code` varchar(64) DEFAULT NULL,
  `plan_name` varchar(128) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `frequency` varchar(16) NOT NULL,
  `recurring_amount` decimal(14,2) NOT NULL,
  `next_invoice_date` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sub_number` (`tenant_id`,`sub_number`),
  KEY `idx_sub_next` (`next_invoice_date`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_visitor_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_visitor_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `visit_no` varchar(32) NOT NULL,
  `visit_date` date NOT NULL,
  `visitor_name` varchar(96) NOT NULL,
  `visitor_company` varchar(96) DEFAULT NULL,
  `visitor_mobile` varchar(16) DEFAULT NULL,
  `visitor_id_proof` varchar(32) DEFAULT NULL,
  `visitor_id_number` varchar(32) DEFAULT NULL,
  `purpose` varchar(96) DEFAULT NULL,
  `whom_to_meet_emp_id` bigint(20) DEFAULT NULL,
  `whom_to_meet_name` varchar(96) DEFAULT NULL,
  `area_access` text DEFAULT NULL,
  `badge_no` varchar(16) DEFAULT NULL,
  `in_time` datetime NOT NULL,
  `out_time` datetime DEFAULT NULL,
  `duration_min` int(11) GENERATED ALWAYS AS (case when `out_time` is not null then timestampdiff(MINUTE,`in_time`,`out_time`) else NULL end) VIRTUAL,
  `vehicle_no` varchar(16) DEFAULT NULL,
  `ppe_issued` tinyint(1) NOT NULL DEFAULT 0,
  `hygiene_brief_given` tinyint(1) NOT NULL DEFAULT 0,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_visit` (`tenant_id`,`visit_no`),
  KEY `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m5_wastage_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m5_wastage_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `log_no` varchar(32) NOT NULL,
  `log_date` date NOT NULL,
  `source_type` varchar(16) NOT NULL,
  `source_ref` varchar(64) DEFAULT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(160) DEFAULT NULL,
  `qty` decimal(14,3) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `unit_cost` decimal(12,2) DEFAULT NULL,
  `value_loss` decimal(14,2) GENERATED ALWAYS AS (`qty` * coalesce(`unit_cost`,0)) STORED,
  `reason_category` varchar(24) NOT NULL,
  `reason_detail` varchar(256) DEFAULT NULL,
  `recovery_action` varchar(96) DEFAULT NULL,
  `reported_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'REPORTED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_waste` (`tenant_id`,`log_no`),
  KEY `idx_waste_date` (`log_date`,`reason_category`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_ar_open_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_ar_open_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `party_type` varchar(8) NOT NULL,
  `party_code` varchar(48) NOT NULL,
  `party_name` varchar(160) NOT NULL,
  `doc_type` varchar(16) NOT NULL,
  `doc_no` varchar(48) NOT NULL,
  `doc_date` date NOT NULL,
  `due_date` date NOT NULL,
  `original_amount` decimal(14,2) NOT NULL,
  `paid_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `outstanding` decimal(14,2) GENERATED ALWAYS AS (`original_amount` - coalesce(`paid_amount`,0)) STORED,
  `days_overdue` int(11) GENERATED ALWAYS AS (case when `due_date` < curdate() then to_days(curdate()) - to_days(`due_date`) else 0 end) VIRTUAL,
  `aging_bucket` varchar(16) GENERATED ALWAYS AS (case when `due_date` >= curdate() then 'NOT_DUE' when to_days(curdate()) - to_days(`due_date`) <= 30 then '0-30' when to_days(curdate()) - to_days(`due_date`) <= 60 then '31-60' when to_days(curdate()) - to_days(`due_date`) <= 90 then '61-90' when to_days(curdate()) - to_days(`due_date`) <= 180 then '91-180' else '180+' end) VIRTUAL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `msme_43bh_due_date` date DEFAULT NULL,
  `msme_43bh_breached` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_ar_party` (`party_type`,`party_code`,`status`),
  KEY `idx_ar_due` (`due_date`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_bank_recon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_bank_recon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `bank_account_code` varchar(16) NOT NULL,
  `statement_date` date NOT NULL,
  `statement_balance` decimal(14,2) NOT NULL,
  `book_balance` decimal(14,2) NOT NULL,
  `unmatched_credits` decimal(14,2) NOT NULL DEFAULT 0.00,
  `unmatched_debits` decimal(14,2) NOT NULL DEFAULT 0.00,
  `diff` decimal(14,2) GENERATED ALWAYS AS (`statement_balance` - `book_balance`) STORED,
  `reconciled_by_emp_id` bigint(20) DEFAULT NULL,
  `reconciled_at` datetime DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_recon_acct` (`bank_account_code`,`statement_date`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_day_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_day_book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `entry_no` varchar(32) NOT NULL,
  `entry_date` date NOT NULL,
  `voucher_type` varchar(16) NOT NULL,
  `ledger_account` varchar(128) NOT NULL,
  `contra_account` varchar(128) DEFAULT NULL,
  `narration` text DEFAULT NULL,
  `debit` decimal(14,2) DEFAULT NULL,
  `credit` decimal(14,2) DEFAULT NULL,
  `reference_doc` varchar(64) DEFAULT NULL,
  `cost_center` varchar(32) DEFAULT NULL,
  `approved_by_emp_id` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `posted` tinyint(1) NOT NULL DEFAULT 0,
  `reversal_of_entry_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_db` (`tenant_id`,`entry_no`),
  KEY `idx_db_date` (`entry_date`,`voucher_type`),
  KEY `idx_db_ledger` (`ledger_account`,`entry_date`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_director_register`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_director_register` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `din` varchar(16) NOT NULL,
  `full_name` varchar(128) NOT NULL,
  `designation` varchar(48) NOT NULL,
  `pan` varchar(16) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `appointed_at` date NOT NULL,
  `cessation_at` date DEFAULT NULL,
  `shareholding_pct` decimal(5,2) DEFAULT NULL,
  `resident_status` varchar(16) DEFAULT NULL,
  `pan_url` varchar(255) DEFAULT NULL,
  `dir_kyc_done` tinyint(1) DEFAULT NULL,
  `dir_kyc_year` varchar(8) DEFAULT NULL,
  `email` varchar(96) DEFAULT NULL,
  `mobile` varchar(16) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_din` (`tenant_id`,`din`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_gl_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_gl_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `account_code` varchar(16) NOT NULL,
  `account_name` varchar(160) NOT NULL,
  `account_class` varchar(16) NOT NULL,
  `account_group` varchar(64) DEFAULT NULL,
  `parent_code` varchar(16) DEFAULT NULL,
  `is_control_account` tinyint(1) NOT NULL DEFAULT 0,
  `is_bank_account` tinyint(1) NOT NULL DEFAULT 0,
  `bank_acct_no` varchar(32) DEFAULT NULL,
  `bank_ifsc` varchar(16) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `opening_balance` decimal(14,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_acct` (`tenant_id`,`account_code`),
  KEY `idx_acct_class` (`account_class`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_gl_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_gl_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `je_no` varchar(32) NOT NULL,
  `je_date` date NOT NULL,
  `fy` varchar(8) NOT NULL,
  `period` varchar(8) NOT NULL,
  `source_module` varchar(16) NOT NULL,
  `source_doc` varchar(64) DEFAULT NULL,
  `account_code` varchar(16) NOT NULL,
  `cost_center` varchar(32) DEFAULT NULL,
  `narration` text DEFAULT NULL,
  `debit` decimal(14,2) NOT NULL DEFAULT 0.00,
  `credit` decimal(14,2) NOT NULL DEFAULT 0.00,
  `party_code` varchar(48) DEFAULT NULL,
  `party_type` varchar(16) DEFAULT NULL,
  `posted` tinyint(1) NOT NULL DEFAULT 1,
  `reversal_of_je_no` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_gl_period` (`period`,`account_code`),
  KEY `idx_gl_acct` (`account_code`,`je_date`),
  KEY `idx_gl_party` (`party_type`,`party_code`),
  KEY `idx_gl_je` (`tenant_id`,`je_no`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_gstr1_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_gstr1_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `period` varchar(8) NOT NULL,
  `b2b_invoice_count` int(11) NOT NULL DEFAULT 0,
  `b2b_taxable` decimal(16,2) NOT NULL DEFAULT 0.00,
  `b2b_igst` decimal(14,2) NOT NULL DEFAULT 0.00,
  `b2b_cgst` decimal(14,2) NOT NULL DEFAULT 0.00,
  `b2b_sgst` decimal(14,2) NOT NULL DEFAULT 0.00,
  `b2c_invoice_count` int(11) NOT NULL DEFAULT 0,
  `b2c_taxable` decimal(16,2) NOT NULL DEFAULT 0.00,
  `b2c_igst` decimal(14,2) NOT NULL DEFAULT 0.00,
  `b2c_cgst` decimal(14,2) NOT NULL DEFAULT 0.00,
  `b2c_sgst` decimal(14,2) NOT NULL DEFAULT 0.00,
  `export_invoice_count` int(11) NOT NULL DEFAULT 0,
  `export_taxable` decimal(16,2) NOT NULL DEFAULT 0.00,
  `cdnr_credit_notes_count` int(11) NOT NULL DEFAULT 0,
  `cdnr_amount` decimal(16,2) NOT NULL DEFAULT 0.00,
  `json_status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `json_path` varchar(255) DEFAULT NULL,
  `filed_at` datetime DEFAULT NULL,
  `ack_no` varchar(48) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gstr1` (`tenant_id`,`period`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `payment_no` varchar(32) NOT NULL,
  `payment_date` date NOT NULL,
  `direction` varchar(8) NOT NULL,
  `counterparty` varchar(256) NOT NULL,
  `amount` decimal(14,2) NOT NULL,
  `mode` varchar(16) NOT NULL,
  `reference_no` varchar(64) DEFAULT NULL,
  `applied_invoice_no` varchar(32) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'POSTED',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pay_no` (`tenant_id`,`payment_no`),
  KEY `idx_pay_direction` (`tenant_id`,`direction`,`payment_date`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_purchase_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_purchase_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `vendor_invoice_no` varchar(32) NOT NULL,
  `invoice_date` date NOT NULL,
  `vendor_name` varchar(256) NOT NULL,
  `vendor_gstin` varchar(16) DEFAULT NULL,
  `linked_po_id` bigint(20) DEFAULT NULL,
  `linked_grn_id` bigint(20) DEFAULT NULL,
  `subtotal` decimal(14,2) NOT NULL DEFAULT 0.00,
  `tax_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `total_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `paid_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `balance` decimal(14,2) NOT NULL DEFAULT 0.00,
  `due_date` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pi_no` (`tenant_id`,`vendor_invoice_no`),
  KEY `idx_pi_status` (`tenant_id`,`status`,`due_date`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_sales_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_sales_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `invoice_no` varchar(32) NOT NULL,
  `invoice_date` date NOT NULL,
  `customer_name` varchar(256) NOT NULL,
  `gstin` varchar(16) DEFAULT NULL,
  `place_of_supply_state` varchar(8) DEFAULT NULL,
  `supplier_gstin` varchar(16) DEFAULT NULL,
  `linked_so_id` bigint(20) DEFAULT NULL,
  `linked_dispatch_id` bigint(20) DEFAULT NULL,
  `subtotal` decimal(14,2) NOT NULL DEFAULT 0.00,
  `tax_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `total_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `paid_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `balance` decimal(14,2) NOT NULL DEFAULT 0.00,
  `due_date` date DEFAULT NULL,
  `irn` varchar(64) DEFAULT NULL,
  `irn_ack_no` varchar(32) DEFAULT NULL,
  `irn_ack_date` datetime DEFAULT NULL,
  `irn_signed_qr` longtext DEFAULT NULL,
  `irn_qr` text DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `payment_terms` varchar(32) DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_si_no` (`tenant_id`,`invoice_no`),
  KEY `idx_si_status` (`tenant_id`,`status`,`due_date`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_sales_invoice_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_sales_invoice_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `invoice_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) DEFAULT NULL,
  `item_name` varchar(256) NOT NULL,
  `hsn_code` varchar(16) DEFAULT NULL,
  `qty` decimal(14,3) NOT NULL,
  `uom` varchar(16) NOT NULL,
  `rate` decimal(14,4) NOT NULL,
  `amount` decimal(14,2) NOT NULL,
  `discount_pct` decimal(5,2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `taxable_amount` decimal(14,2) NOT NULL,
  `cgst_pct` decimal(5,2) NOT NULL DEFAULT 0.00,
  `cgst_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `sgst_pct` decimal(5,2) NOT NULL DEFAULT 0.00,
  `sgst_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `igst_pct` decimal(5,2) NOT NULL DEFAULT 0.00,
  `igst_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `cess_amount` decimal(14,2) NOT NULL DEFAULT 0.00,
  `total_amount` decimal(14,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sil_inv` (`invoice_id`),
  KEY `idx_sil_item` (`item_code`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_shipment_checkpoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_shipment_checkpoint` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tracking_id` bigint(20) NOT NULL,
  `checkpoint_at` datetime NOT NULL,
  `location` varchar(128) DEFAULT NULL,
  `geo` varchar(48) DEFAULT NULL,
  `status` varchar(32) NOT NULL,
  `remarks` varchar(256) DEFAULT NULL,
  `recorded_by` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ckpt_track` (`tracking_id`,`checkpoint_at`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_shipment_tracking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_shipment_tracking` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `tracking_no` varchar(48) NOT NULL,
  `shipment_type` varchar(16) NOT NULL,
  `reference_no` varchar(48) NOT NULL,
  `origin_city` varchar(64) DEFAULT NULL,
  `destination_city` varchar(64) DEFAULT NULL,
  `expected_eta` datetime DEFAULT NULL,
  `actual_eta` datetime DEFAULT NULL,
  `carrier_code` varchar(32) DEFAULT NULL,
  `carrier_name` varchar(96) DEFAULT NULL,
  `vehicle_no` varchar(16) DEFAULT NULL,
  `driver_phone` varchar(16) DEFAULT NULL,
  `current_status` varchar(32) NOT NULL DEFAULT 'CREATED',
  `current_location` varchar(128) DEFAULT NULL,
  `current_geo` varchar(48) DEFAULT NULL,
  `total_weight_kg` decimal(12,3) DEFAULT NULL,
  `total_pkg_count` int(11) DEFAULT NULL,
  `ewb_no` varchar(32) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `delivered_at` timestamp NULL DEFAULT NULL,
  `pod_url` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_track` (`tenant_id`,`tracking_no`),
  KEY `idx_track_ref` (`reference_no`),
  KEY `idx_track_status` (`current_status`,`expected_eta`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_tds_challan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_tds_challan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `challan_no` varchar(48) NOT NULL,
  `paid_date` date NOT NULL,
  `fy` varchar(8) NOT NULL,
  `period` varchar(16) NOT NULL,
  `section` varchar(16) NOT NULL,
  `basic_tax` decimal(14,2) NOT NULL,
  `surcharge` decimal(14,2) DEFAULT NULL,
  `cess` decimal(14,2) DEFAULT NULL,
  `interest` decimal(14,2) DEFAULT NULL,
  `fee_u_s_234e` decimal(14,2) DEFAULT NULL,
  `penalty` decimal(14,2) DEFAULT NULL,
  `total_paid` decimal(14,2) NOT NULL,
  `bsr_code` varchar(8) NOT NULL,
  `bank_name` varchar(96) DEFAULT NULL,
  `bank_branch` varchar(96) DEFAULT NULL,
  `challan_url` varchar(255) DEFAULT NULL,
  `return_filed_in` varchar(16) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PAID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_challan` (`tenant_id`,`challan_no`),
  KEY `idx_chal_section` (`section`,`paid_date`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m6_vendor_kyc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m6_vendor_kyc` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `vendor_code` varchar(48) NOT NULL,
  `vendor_name` varchar(160) NOT NULL,
  `pan` varchar(16) DEFAULT NULL,
  `gstin` varchar(16) DEFAULT NULL,
  `msme_status` varchar(16) DEFAULT NULL,
  `udyam_no` varchar(48) DEFAULT NULL,
  `iec_code` varchar(16) DEFAULT NULL,
  `bank_name` varchar(96) DEFAULT NULL,
  `bank_acct` varchar(32) DEFAULT NULL,
  `bank_ifsc` varchar(16) DEFAULT NULL,
  `cancelled_cheque_url` varchar(255) DEFAULT NULL,
  `agreement_url` varchar(255) DEFAULT NULL,
  `agreement_from` date DEFAULT NULL,
  `agreement_to` date DEFAULT NULL,
  `credit_days` int(11) DEFAULT NULL,
  `credit_limit` decimal(14,2) DEFAULT NULL,
  `tds_section` varchar(8) DEFAULT NULL,
  `tds_rate_pct` decimal(5,2) DEFAULT NULL,
  `msme_form1_compliant` tinyint(1) DEFAULT NULL,
  `last_kyc_review_date` date DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_vendor_kyc` (`tenant_id`,`vendor_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_appraisal_cycle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_appraisal_cycle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `cycle_code` varchar(32) NOT NULL,
  `cycle_name` varchar(96) NOT NULL,
  `fy` varchar(8) NOT NULL,
  `period_from` date NOT NULL,
  `period_to` date NOT NULL,
  `self_window_from` date DEFAULT NULL,
  `self_window_to` date DEFAULT NULL,
  `mgr_window_from` date DEFAULT NULL,
  `mgr_window_to` date DEFAULT NULL,
  `hr_window_from` date DEFAULT NULL,
  `hr_window_to` date DEFAULT NULL,
  `rating_scale_max` int(11) NOT NULL DEFAULT 5,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cycle` (`tenant_id`,`cycle_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_appraisal_form`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_appraisal_form` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `cycle_id` bigint(20) NOT NULL,
  `emp_id` bigint(20) NOT NULL,
  `manager_emp_id` bigint(20) DEFAULT NULL,
  `self_submitted_at` timestamp NULL DEFAULT NULL,
  `mgr_submitted_at` timestamp NULL DEFAULT NULL,
  `hr_finalised_at` timestamp NULL DEFAULT NULL,
  `rating_productivity` decimal(3,1) DEFAULT NULL,
  `rating_quality` decimal(3,1) DEFAULT NULL,
  `rating_attendance` decimal(3,1) DEFAULT NULL,
  `rating_initiative` decimal(3,1) DEFAULT NULL,
  `rating_teamwork` decimal(3,1) DEFAULT NULL,
  `rating_technical` decimal(3,1) DEFAULT NULL,
  `rating_safety` decimal(3,1) DEFAULT NULL,
  `rating_overall` decimal(3,1) DEFAULT NULL,
  `self_achievements` text DEFAULT NULL,
  `self_improvement` text DEFAULT NULL,
  `mgr_strengths` text DEFAULT NULL,
  `mgr_dev_areas` text DEFAULT NULL,
  `mgr_overall_remark` text DEFAULT NULL,
  `hr_remarks` text DEFAULT NULL,
  `promotion_recommended` tinyint(1) NOT NULL DEFAULT 0,
  `increment_pct_recommended` decimal(5,2) DEFAULT NULL,
  `pip_required` tinyint(1) NOT NULL DEFAULT 0,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING_SELF',
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_apr_cycle` (`cycle_id`),
  KEY `idx_apr_emp_cycle` (`emp_id`,`cycle_id`),
  CONSTRAINT `fk_apr_cycle` FOREIGN KEY (`cycle_id`) REFERENCES `m7_appraisal_cycle` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_apprentice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_apprentice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `apprentice_code` varchar(32) NOT NULL,
  `emp_full_name` varchar(128) NOT NULL,
  `father_name` varchar(128) DEFAULT NULL,
  `trade` varchar(64) NOT NULL,
  `trade_code` varchar(8) DEFAULT NULL,
  `qualification` varchar(64) DEFAULT NULL,
  `institute` varchar(160) DEFAULT NULL,
  `registration_no` varchar(48) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `stipend_monthly` decimal(10,2) DEFAULT NULL,
  `supervisor_emp_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `completion_cert_no` varchar(48) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_apprentice` (`tenant_id`,`apprentice_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_attendance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `employee_id` bigint(20) NOT NULL,
  `shift_code` varchar(8) DEFAULT NULL,
  `date_of` date NOT NULL,
  `in_time` time DEFAULT NULL,
  `out_time` time DEFAULT NULL,
  `worked_hours` decimal(5,2) DEFAULT NULL,
  `ot_hours` decimal(5,2) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PRESENT',
  `source` varchar(16) NOT NULL DEFAULT 'BIOMETRIC',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_att` (`tenant_id`,`employee_id`,`date_of`),
  KEY `idx_att_date` (`date_of`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_attendance_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_attendance_summary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) NOT NULL,
  `period` varchar(8) NOT NULL,
  `total_days` int(11) NOT NULL,
  `present_days` decimal(5,2) NOT NULL,
  `weekly_off_days` int(11) NOT NULL DEFAULT 0,
  `holiday_days` int(11) NOT NULL DEFAULT 0,
  `paid_leave_days` decimal(5,2) NOT NULL DEFAULT 0.00,
  `lop_days` decimal(5,2) NOT NULL DEFAULT 0.00,
  `ot_hours` decimal(6,2) NOT NULL DEFAULT 0.00,
  `late_count` int(11) NOT NULL DEFAULT 0,
  `early_out_count` int(11) NOT NULL DEFAULT 0,
  `missing_punch_count` int(11) NOT NULL DEFAULT 0,
  `payable_days` decimal(5,2) GENERATED ALWAYS AS (`present_days` + `paid_leave_days` + `holiday_days` + `weekly_off_days`) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_att_emp_period` (`emp_id`,`period`),
  KEY `idx_att_period` (`period`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_candidate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_candidate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `candidate_no` varchar(32) NOT NULL,
  `full_name` varchar(256) NOT NULL,
  `contact_phone` varchar(32) DEFAULT NULL,
  `contact_email` varchar(128) DEFAULT NULL,
  `applied_designation_code` varchar(32) DEFAULT NULL,
  `applied_dept_id` bigint(20) DEFAULT NULL,
  `source` varchar(32) DEFAULT NULL,
  `experience_years` decimal(4,1) DEFAULT NULL,
  `current_salary` decimal(12,2) DEFAULT NULL,
  `expected_salary` decimal(12,2) DEFAULT NULL,
  `resume_doc_id` bigint(20) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'NEW',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `hired_employee_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cand_no` (`tenant_id`,`candidate_no`),
  KEY `idx_cand_status` (`tenant_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_designation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_designation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(32) NOT NULL,
  `title` varchar(128) NOT NULL,
  `band` varchar(8) DEFAULT NULL,
  `reports_to_code` varchar(32) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_desig` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_code` varchar(32) NOT NULL,
  `full_name` varchar(128) NOT NULL,
  `father_or_spouse_name` varchar(128) DEFAULT NULL,
  `gender` varchar(8) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `marital_status` varchar(16) DEFAULT NULL,
  `blood_group` varchar(4) DEFAULT NULL,
  `religion` varchar(32) DEFAULT NULL,
  `category` varchar(16) DEFAULT NULL,
  `nationality` varchar(32) DEFAULT 'Indian',
  `language_known` varchar(128) DEFAULT NULL,
  `photo_url` varchar(256) DEFAULT NULL,
  `mobile_primary` varchar(16) DEFAULT NULL,
  `mobile_alt` varchar(16) DEFAULT NULL,
  `email_personal` varchar(96) DEFAULT NULL,
  `email_official` varchar(96) DEFAULT NULL,
  `emergency_contact_name` varchar(96) DEFAULT NULL,
  `emergency_contact_phone` varchar(16) DEFAULT NULL,
  `emergency_contact_relation` varchar(32) DEFAULT NULL,
  `current_address` text DEFAULT NULL,
  `permanent_address` text DEFAULT NULL,
  `city` varchar(64) DEFAULT NULL,
  `state` varchar(64) DEFAULT NULL,
  `pincode` varchar(10) DEFAULT NULL,
  `country` varchar(64) DEFAULT 'India',
  `designation` varchar(64) DEFAULT NULL,
  `department` varchar(64) DEFAULT NULL,
  `grade` varchar(16) DEFAULT NULL,
  `employment_type` varchar(16) DEFAULT NULL,
  `work_location` varchar(64) DEFAULT NULL,
  `reports_to_emp_id` bigint(20) DEFAULT NULL,
  `date_of_joining` date DEFAULT NULL,
  `probation_end_date` date DEFAULT NULL,
  `confirmation_date` date DEFAULT NULL,
  `separation_date` date DEFAULT NULL,
  `separation_reason` varchar(64) DEFAULT NULL,
  `notice_period_days` int(11) DEFAULT 30,
  `shift_code` varchar(16) DEFAULT NULL,
  `aadhaar_no` varchar(20) DEFAULT NULL,
  `pan_no` varchar(16) DEFAULT NULL,
  `voter_id` varchar(20) DEFAULT NULL,
  `driving_licence` varchar(24) DEFAULT NULL,
  `passport_no` varchar(20) DEFAULT NULL,
  `uan_no` varchar(16) DEFAULT NULL,
  `esic_ip_no` varchar(24) DEFAULT NULL,
  `bank_account_no` varchar(32) DEFAULT NULL,
  `bank_ifsc` varchar(16) DEFAULT NULL,
  `bank_name` varchar(64) DEFAULT NULL,
  `bank_branch` varchar(64) DEFAULT NULL,
  `bank_account_holder` varchar(96) DEFAULT NULL,
  `pf_no` varchar(32) DEFAULT NULL,
  `esi_no` varchar(24) DEFAULT NULL,
  `pt_state` varchar(8) DEFAULT NULL,
  `lwf_state` varchar(8) DEFAULT NULL,
  `pf_nominee_name` varchar(96) DEFAULT NULL,
  `pf_nominee_relation` varchar(32) DEFAULT NULL,
  `pf_nominee_share_pct` decimal(5,2) DEFAULT 100.00,
  `gratuity_nominee_name` varchar(96) DEFAULT NULL,
  `gratuity_nominee_relation` varchar(32) DEFAULT NULL,
  `pf_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `esi_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `pt_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `tds_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `lwf_applicable` tinyint(1) NOT NULL DEFAULT 1,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_emp_code` (`tenant_id`,`emp_code`),
  KEY `idx_emp_status` (`tenant_id`,`status`),
  KEY `idx_emp_dept` (`department`),
  KEY `idx_emp_designation` (`designation`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_employee_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_employee_experience` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `emp_id` bigint(20) NOT NULL,
  `company` varchar(128) NOT NULL,
  `designation` varchar(64) DEFAULT NULL,
  `from_date` date DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  `ctc_lpa` decimal(10,2) DEFAULT NULL,
  `reason_for_leaving` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_emp_exp` (`emp_id`),
  CONSTRAINT `fk_emp_exp` FOREIGN KEY (`emp_id`) REFERENCES `m7_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_employee_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_employee_family` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `emp_id` bigint(20) NOT NULL,
  `member_name` varchar(96) NOT NULL,
  `relation` varchar(32) NOT NULL,
  `dob` date DEFAULT NULL,
  `occupation` varchar(64) DEFAULT NULL,
  `is_esic_dependent` tinyint(1) NOT NULL DEFAULT 0,
  `is_mediclaim_dependent` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_emp_fam` (`emp_id`),
  CONSTRAINT `fk_emp_fam` FOREIGN KEY (`emp_id`) REFERENCES `m7_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_employee_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_employee_qualification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `emp_id` bigint(20) NOT NULL,
  `qualification` varchar(64) NOT NULL,
  `specialization` varchar(96) DEFAULT NULL,
  `institute` varchar(128) DEFAULT NULL,
  `university` varchar(128) DEFAULT NULL,
  `year_of_passing` int(11) DEFAULT NULL,
  `percentage` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_emp_qual` (`emp_id`),
  CONSTRAINT `fk_emp_qual` FOREIGN KEY (`emp_id`) REFERENCES `m7_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_employee_salary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_employee_salary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) NOT NULL,
  `effective_from` date NOT NULL,
  `effective_to` date DEFAULT NULL,
  `ctc_annual` decimal(12,2) NOT NULL,
  `basic_monthly` decimal(12,2) NOT NULL,
  `da_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `hra_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `conveyance_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `medical_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `special_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `lta_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `cea_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `hostel_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `cca_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `shift_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `bonus_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `incentive_monthly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `medical_reimb_yearly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `fuel_reimb_yearly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `telephone_reimb_yearly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `variable_pay_yearly` decimal(12,2) NOT NULL DEFAULT 0.00,
  `pf_employer_basic_capped` tinyint(1) NOT NULL DEFAULT 1,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_emp_sal_eff` (`emp_id`,`effective_from`),
  CONSTRAINT `fk_emp_sal` FOREIGN KEY (`emp_id`) REFERENCES `m7_employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_employee_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_employee_skill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) NOT NULL,
  `skill_code` varchar(32) NOT NULL,
  `skill_name` varchar(96) NOT NULL,
  `category` varchar(24) DEFAULT NULL,
  `proficiency` varchar(16) NOT NULL,
  `certified` tinyint(1) NOT NULL DEFAULT 0,
  `cert_authority` varchar(64) DEFAULT NULL,
  `cert_no` varchar(48) DEFAULT NULL,
  `cert_expiry` date DEFAULT NULL,
  `last_assessed` date DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_emp_skill` (`emp_id`,`skill_code`),
  KEY `idx_skill_cat` (`category`,`proficiency`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_esi_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_esi_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `effective_from` date NOT NULL,
  `emp_rate_pct` decimal(5,3) NOT NULL DEFAULT 0.750,
  `employer_rate_pct` decimal(5,3) NOT NULL DEFAULT 3.250,
  `gross_wage_ceiling` decimal(10,2) NOT NULL DEFAULT 21000.00,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_health_check`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) DEFAULT NULL,
  `contract_labour_id` bigint(20) DEFAULT NULL,
  `emp_name` varchar(128) NOT NULL,
  `check_type` varchar(24) NOT NULL,
  `check_date` date NOT NULL,
  `doctor_name` varchar(96) DEFAULT NULL,
  `clinic` varchar(96) DEFAULT NULL,
  `bp_systolic` int(11) DEFAULT NULL,
  `bp_diastolic` int(11) DEFAULT NULL,
  `heart_rate` int(11) DEFAULT NULL,
  `vision` varchar(8) DEFAULT NULL,
  `audiometry` varchar(16) DEFAULT NULL,
  `lung_function_pct` decimal(5,2) DEFAULT NULL,
  `bmi` decimal(5,2) DEFAULT NULL,
  `ecg_normal` tinyint(1) DEFAULT NULL,
  `blood_sugar_random` decimal(6,2) DEFAULT NULL,
  `haemoglobin` decimal(4,2) DEFAULT NULL,
  `liver_function_normal` tinyint(1) DEFAULT NULL,
  `fitness_verdict` varchar(24) NOT NULL,
  `restrictions` text DEFAULT NULL,
  `next_check_due` date DEFAULT NULL,
  `certificate_url` varchar(255) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hc_emp` (`emp_id`,`check_date`),
  KEY `idx_hc_next` (`next_check_due`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_increment_letter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_increment_letter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) NOT NULL,
  `letter_no` varchar(32) NOT NULL,
  `cycle_period` varchar(16) NOT NULL,
  `effective_from` date NOT NULL,
  `prev_ctc_annual` decimal(12,2) NOT NULL,
  `new_ctc_annual` decimal(12,2) NOT NULL,
  `increment_amount` decimal(12,2) GENERATED ALWAYS AS (`new_ctc_annual` - `prev_ctc_annual`) STORED,
  `increment_pct` decimal(6,2) GENERATED ALWAYS AS ((`new_ctc_annual` - `prev_ctc_annual`) / `prev_ctc_annual` * 100) STORED,
  `reason` varchar(64) DEFAULT NULL,
  `prev_designation` varchar(64) DEFAULT NULL,
  `new_designation` varchar(64) DEFAULT NULL,
  `prev_grade` varchar(16) DEFAULT NULL,
  `new_grade` varchar(16) DEFAULT NULL,
  `manager_recommendation` text DEFAULT NULL,
  `hr_remarks` text DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `released_at` timestamp NULL DEFAULT NULL,
  `accepted_at` timestamp NULL DEFAULT NULL,
  `letter_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inc_letter` (`tenant_id`,`letter_no`),
  KEY `idx_inc_emp` (`emp_id`,`effective_from`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_interview_round`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_interview_round` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL,
  `round_no` int(11) NOT NULL,
  `round_type` varchar(24) NOT NULL,
  `scheduled_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `panelist_emp_id` bigint(20) DEFAULT NULL,
  `mode` varchar(16) DEFAULT NULL,
  `meeting_link` varchar(255) DEFAULT NULL,
  `rating_technical` decimal(3,1) DEFAULT NULL,
  `rating_communication` decimal(3,1) DEFAULT NULL,
  `rating_attitude` decimal(3,1) DEFAULT NULL,
  `rating_overall` decimal(3,1) DEFAULT NULL,
  `strengths` text DEFAULT NULL,
  `concerns` text DEFAULT NULL,
  `verdict` varchar(16) DEFAULT NULL,
  `feedback` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_intv` (`application_id`,`round_no`),
  CONSTRAINT `fk_intv_app` FOREIGN KEY (`application_id`) REFERENCES `m7_job_application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_job_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_job_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `applicant_code` varchar(32) NOT NULL,
  `opening_id` bigint(20) NOT NULL,
  `full_name` varchar(128) NOT NULL,
  `mobile` varchar(16) DEFAULT NULL,
  `email` varchar(96) DEFAULT NULL,
  `current_company` varchar(96) DEFAULT NULL,
  `current_designation` varchar(64) DEFAULT NULL,
  `current_ctc_lpa` decimal(8,2) DEFAULT NULL,
  `expected_ctc_lpa` decimal(8,2) DEFAULT NULL,
  `notice_period_days` int(11) DEFAULT NULL,
  `experience_years` decimal(4,1) DEFAULT NULL,
  `source` varchar(24) DEFAULT NULL,
  `source_detail` varchar(128) DEFAULT NULL,
  `resume_url` varchar(255) DEFAULT NULL,
  `status` varchar(24) NOT NULL DEFAULT 'CV_SCREENING',
  `current_step` varchar(32) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `recommended_grade` varchar(16) DEFAULT NULL,
  `offered_ctc_lpa` decimal(8,2) DEFAULT NULL,
  `joining_date` date DEFAULT NULL,
  `decline_reason` varchar(96) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_app` (`tenant_id`,`applicant_code`),
  KEY `idx_app_open` (`opening_id`,`status`),
  CONSTRAINT `fk_app_open` FOREIGN KEY (`opening_id`) REFERENCES `m7_job_opening` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_job_opening`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_job_opening` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `opening_code` varchar(32) NOT NULL,
  `title` varchar(96) NOT NULL,
  `department` varchar(64) DEFAULT NULL,
  `grade` varchar(16) DEFAULT NULL,
  `required_count` int(11) NOT NULL DEFAULT 1,
  `filled_count` int(11) NOT NULL DEFAULT 0,
  `employment_type` varchar(16) DEFAULT NULL,
  `experience_min_years` int(11) DEFAULT NULL,
  `experience_max_years` int(11) DEFAULT NULL,
  `ctc_min_lpa` decimal(8,2) DEFAULT NULL,
  `ctc_max_lpa` decimal(8,2) DEFAULT NULL,
  `job_description` text DEFAULT NULL,
  `required_skills` text DEFAULT NULL,
  `hiring_manager_emp_id` bigint(20) DEFAULT NULL,
  `hr_owner_emp_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `opened_at` date DEFAULT NULL,
  `filled_at` date DEFAULT NULL,
  `closed_at` date DEFAULT NULL,
  `job_posting_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_opening` (`tenant_id`,`opening_code`),
  KEY `idx_open_status` (`status`,`opened_at`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_leave_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_leave_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `lr_number` varchar(32) NOT NULL,
  `employee_id` bigint(20) NOT NULL,
  `leave_kind` varchar(16) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `days` decimal(4,1) NOT NULL,
  `reason` varchar(256) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'PENDING',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lr_number` (`tenant_id`,`lr_number`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_lwf_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_lwf_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `state_code` varchar(8) NOT NULL,
  `cadence` varchar(16) NOT NULL,
  `emp_amount` decimal(8,2) NOT NULL,
  `employer_amount` decimal(8,2) NOT NULL,
  `effective_from` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lwf_state` (`state_code`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_onboarding_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_onboarding_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) DEFAULT NULL,
  `application_id` bigint(20) DEFAULT NULL,
  `task_category` varchar(24) NOT NULL,
  `task_code` varchar(32) NOT NULL,
  `task_label` varchar(160) NOT NULL,
  `due_offset_days` int(11) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `completed_by_emp_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `remarks` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_onb_emp` (`emp_id`,`status`),
  KEY `idx_onb_app` (`application_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_payroll_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_payroll_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `run_id` bigint(20) NOT NULL,
  `emp_id` bigint(20) NOT NULL,
  `component_code` varchar(32) NOT NULL,
  `component_type` varchar(16) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `formula_used` varchar(256) DEFAULT NULL,
  `base_amount` decimal(12,2) DEFAULT NULL,
  `base_pct` decimal(6,3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pl_run` (`run_id`),
  KEY `idx_pl_emp` (`emp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_payroll_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_payroll_run` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `payroll_period` varchar(8) NOT NULL,
  `employee_id` bigint(20) NOT NULL,
  `worked_days` decimal(4,1) DEFAULT NULL,
  `basic` decimal(12,2) NOT NULL,
  `hra` decimal(12,2) NOT NULL DEFAULT 0.00,
  `conveyance` decimal(12,2) NOT NULL DEFAULT 0.00,
  `ot_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `gross` decimal(12,2) NOT NULL,
  `pf_employee` decimal(12,2) NOT NULL DEFAULT 0.00,
  `esi_employee` decimal(12,2) NOT NULL DEFAULT 0.00,
  `tds` decimal(12,2) NOT NULL DEFAULT 0.00,
  `other_deduction` decimal(12,2) NOT NULL DEFAULT 0.00,
  `net_pay` decimal(12,2) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'COMPUTED',
  `payslip_path` varchar(256) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_payroll` (`tenant_id`,`payroll_period`,`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_payslip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_payslip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `emp_id` bigint(20) NOT NULL,
  `payroll_period` varchar(8) NOT NULL,
  `generated_at` timestamp NULL DEFAULT NULL,
  `generated_by` varchar(64) DEFAULT NULL,
  `pdf_url` varchar(255) DEFAULT NULL,
  `html_snapshot` longtext DEFAULT NULL,
  `email_sent_at` timestamp NULL DEFAULT NULL,
  `email_to` varchar(128) DEFAULT NULL,
  `whatsapp_sent_at` timestamp NULL DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `gross` decimal(12,2) DEFAULT NULL,
  `total_deduct` decimal(12,2) DEFAULT NULL,
  `net_pay` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_payslip` (`tenant_id`,`emp_id`,`payroll_period`),
  KEY `idx_payslip_period` (`payroll_period`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_pf_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_pf_setting` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `effective_from` date NOT NULL,
  `emp_rate_pct` decimal(5,3) NOT NULL DEFAULT 12.000,
  `employer_rate_pct` decimal(5,3) NOT NULL DEFAULT 12.000,
  `eps_share_pct` decimal(5,3) NOT NULL DEFAULT 8.330,
  `epf_share_pct` decimal(5,3) NOT NULL DEFAULT 3.670,
  `edli_pct` decimal(5,3) NOT NULL DEFAULT 0.500,
  `admin_pct` decimal(5,3) NOT NULL DEFAULT 0.500,
  `wage_ceiling` decimal(10,2) NOT NULL DEFAULT 15000.00,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_posh_committee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_posh_committee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `member_role` varchar(24) NOT NULL,
  `emp_id` bigint(20) DEFAULT NULL,
  `external_name` varchar(96) DEFAULT NULL,
  `external_org` varchar(96) DEFAULT NULL,
  `external_email` varchar(96) DEFAULT NULL,
  `external_phone` varchar(16) DEFAULT NULL,
  `gender` varchar(8) NOT NULL,
  `effective_from` date NOT NULL,
  `effective_to` date DEFAULT NULL,
  `appointment_letter_url` varchar(255) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_posh_incident`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_posh_incident` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `case_no` varchar(32) NOT NULL,
  `filed_at` date NOT NULL,
  `complainant_emp_id` bigint(20) DEFAULT NULL,
  `respondent_emp_id` bigint(20) DEFAULT NULL,
  `nature_of_complaint` text DEFAULT NULL,
  `inquiry_started_at` date DEFAULT NULL,
  `inquiry_closed_at` date DEFAULT NULL,
  `findings` text DEFAULT NULL,
  `action_taken` text DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `reported_in_annual_return` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_posh` (`tenant_id`,`case_no`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_pt_slab`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_pt_slab` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `state_code` varchar(8) NOT NULL,
  `cadence` varchar(16) NOT NULL DEFAULT 'MONTHLY',
  `gender` varchar(8) DEFAULT NULL,
  `effective_from` date NOT NULL,
  `gross_from` decimal(10,2) NOT NULL,
  `gross_to` decimal(10,2) DEFAULT NULL,
  `pt_amount` decimal(8,2) NOT NULL,
  `feb_surcharge` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pt_state` (`state_code`,`effective_from`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_salary_component`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_salary_component` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(32) NOT NULL,
  `name` varchar(64) NOT NULL,
  `component_type` varchar(16) NOT NULL,
  `calc_method` varchar(16) NOT NULL,
  `percent_of` varchar(16) DEFAULT NULL,
  `default_percent` decimal(6,3) DEFAULT NULL,
  `taxable` tinyint(1) NOT NULL DEFAULT 1,
  `show_on_payslip` tinyint(1) NOT NULL DEFAULT 1,
  `pf_eligible` tinyint(1) NOT NULL DEFAULT 0,
  `esi_eligible` tinyint(1) NOT NULL DEFAULT 0,
  `formula_text` text DEFAULT NULL,
  `sort_order` int(11) NOT NULL DEFAULT 100,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_comp` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_statutory_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_statutory_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `return_kind` varchar(16) NOT NULL,
  `period` varchar(8) NOT NULL,
  `due_date` date NOT NULL,
  `filed_on` date DEFAULT NULL,
  `acknowledgement_no` varchar(64) DEFAULT NULL,
  `amount` decimal(14,2) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DUE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_stat_return` (`tenant_id`,`return_kind`,`period`),
  KEY `idx_stat_due` (`due_date`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_statutory_return_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_statutory_return_catalog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `return_code` varchar(32) NOT NULL,
  `return_name` varchar(128) NOT NULL,
  `authority` varchar(32) NOT NULL,
  `cadence` varchar(16) NOT NULL,
  `due_day_of_period` int(11) DEFAULT NULL,
  `applicable` tinyint(1) NOT NULL DEFAULT 1,
  `next_due_date` date DEFAULT NULL,
  `last_filed_period` varchar(16) DEFAULT NULL,
  `last_filed_date` date DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_statutory_return_filing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_statutory_return_filing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `return_code` varchar(32) NOT NULL,
  `period` varchar(8) NOT NULL,
  `due_date` date NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `filed_at` timestamp NULL DEFAULT NULL,
  `filed_by` varchar(64) DEFAULT NULL,
  `ack_no` varchar(64) DEFAULT NULL,
  `amount` decimal(14,2) DEFAULT NULL,
  `challan_no` varchar(64) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_filing` (`tenant_id`,`return_code`,`period`),
  KEY `idx_filing_due` (`due_date`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_tds_slab`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_tds_slab` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `regime` varchar(8) NOT NULL,
  `fy` varchar(8) NOT NULL,
  `annual_from` decimal(12,2) NOT NULL,
  `annual_to` decimal(12,2) DEFAULT NULL,
  `rate_pct` decimal(5,2) NOT NULL,
  `rebate_87a` decimal(10,2) DEFAULT NULL,
  `std_deduction` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tds_regime` (`regime`,`fy`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_training_attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_training_attendance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `program_id` bigint(20) NOT NULL,
  `emp_id` bigint(20) NOT NULL,
  `scheduled_date` date DEFAULT NULL,
  `attended_date` date DEFAULT NULL,
  `duration_hours` decimal(5,1) DEFAULT NULL,
  `attendance_status` varchar(16) NOT NULL DEFAULT 'SCHEDULED',
  `pre_score` decimal(5,2) DEFAULT NULL,
  `post_score` decimal(5,2) DEFAULT NULL,
  `pass_threshold` decimal(5,2) DEFAULT NULL,
  `certified` tinyint(1) DEFAULT NULL,
  `certificate_no` varchar(48) DEFAULT NULL,
  `next_refresh_due` date DEFAULT NULL,
  `feedback` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ta_emp` (`emp_id`,`scheduled_date`),
  KEY `idx_ta_prog` (`program_id`,`attendance_status`),
  CONSTRAINT `fk_ta_prog` FOREIGN KEY (`program_id`) REFERENCES `m7_training_program` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m7_training_program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m7_training_program` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `program_code` varchar(32) NOT NULL,
  `title` varchar(160) NOT NULL,
  `category` varchar(24) NOT NULL,
  `description` text DEFAULT NULL,
  `duration_hours` decimal(5,1) DEFAULT NULL,
  `delivery_mode` varchar(16) DEFAULT NULL,
  `trainer` varchar(96) DEFAULT NULL,
  `is_mandatory` tinyint(1) NOT NULL DEFAULT 0,
  `refresh_months` int(11) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tp` (`tenant_id`,`program_code`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m8_lead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m8_lead` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `lead_number` varchar(32) NOT NULL,
  `lead_name` varchar(256) NOT NULL,
  `contact_name` varchar(128) DEFAULT NULL,
  `contact_phone` varchar(32) DEFAULT NULL,
  `contact_email` varchar(128) DEFAULT NULL,
  `source` varchar(32) DEFAULT NULL,
  `estimated_value` decimal(14,2) DEFAULT NULL,
  `stage` varchar(16) NOT NULL DEFAULT 'NEW',
  `owner_id` bigint(20) DEFAULT NULL,
  `next_action_on` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'OPEN',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lead_number` (`tenant_id`,`lead_number`),
  KEY `idx_lead_stage` (`stage`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m8_quote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m8_quote` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `quote_number` varchar(32) NOT NULL,
  `lead_id` bigint(20) DEFAULT NULL,
  `customer_name` varchar(256) NOT NULL,
  `total_value` decimal(14,2) NOT NULL,
  `valid_until` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_quote_number` (`tenant_id`,`quote_number`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `m9_service_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `m9_service_ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `ticket_no` varchar(32) NOT NULL,
  `customer_name` varchar(256) NOT NULL,
  `site_address` varchar(256) DEFAULT NULL,
  `issue_kind` varchar(32) DEFAULT NULL,
  `priority` varchar(8) NOT NULL DEFAULT 'P3',
  `reported_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `technician_id` bigint(20) DEFAULT NULL,
  `scheduled_for` timestamp NULL DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'OPEN',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `resolution` text DEFAULT NULL,
  `closed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ticket_no` (`tenant_id`,`ticket_no`),
  KEY `idx_st_status` (`status`,`priority`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `about_machine` varchar(255) DEFAULT NULL,
  `acc_group` varchar(255) DEFAULT NULL,
  `branch_id` varchar(255) DEFAULT NULL,
  `cstatus` varchar(255) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `classify` varchar(255) DEFAULT NULL,
  `cluster` int(11) DEFAULT NULL,
  `company` int(11) DEFAULT NULL,
  `consumables` varchar(255) DEFAULT NULL,
  `cost` bigint(20) DEFAULT NULL,
  `costgroupid` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `doi` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `dispname` varchar(255) DEFAULT NULL,
  `ebmf` float DEFAULT NULL,
  `emergency` varchar(255) DEFAULT NULL,
  `genset` varchar(255) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `hsn_ref_id` varchar(255) DEFAULT NULL,
  `ice` varchar(255) DEFAULT NULL,
  `main` varchar(255) DEFAULT NULL,
  `iso_no` varchar(255) DEFAULT NULL,
  `locationpath` varchar(255) DEFAULT NULL,
  `make` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `panel_presence` varchar(255) DEFAULT NULL,
  `panelent` varchar(255) DEFAULT NULL,
  `pictureheight` bigint(20) DEFAULT NULL,
  `picturename` varchar(255) DEFAULT NULL,
  `picturesize` int(11) DEFAULT NULL,
  `picturetype` varchar(255) DEFAULT NULL,
  `picturewidth` bigint(20) DEFAULT NULL,
  `place` varchar(255) DEFAULT NULL,
  `production` varchar(255) DEFAULT NULL,
  `purchaseac` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `responsibility` bigint(20) DEFAULT NULL,
  `safetyinst` varchar(255) DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `slno` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tcid` int(11) DEFAULT NULL,
  `test` varchar(255) DEFAULT NULL,
  `tools` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `utilisation` varchar(255) DEFAULT NULL,
  `vendor` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `classifyid` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `depreciation_id` bigint(20) DEFAULT NULL,
  `ebservice` bigint(20) DEFAULT NULL,
  `genesetid` bigint(20) DEFAULT NULL,
  `purchase_from` bigint(20) DEFAULT NULL,
  `purchase_on_acc` bigint(20) DEFAULT NULL,
  `resp_1` bigint(20) DEFAULT NULL,
  `resp_2` bigint(20) DEFAULT NULL,
  `resp_3` bigint(20) DEFAULT NULL,
  `task_category_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4ngw9ostwybtbgqkhef36072c` (`branch`),
  KEY `FKoseema3sq3g7f7mools3nc1yt` (`classifyid`),
  KEY `FK1j0icy0u15m8u4ari0ssq0cqv` (`dept_id`),
  KEY `FKmm5o3yris13t6u6ryu399hcpl` (`depreciation_id`),
  KEY `FKp32k1uqdf71ies55r4p7q50fs` (`ebservice`),
  KEY `FKbwv0py072qaq5hdjxprm59nj8` (`genesetid`),
  KEY `FKmip1q0r1mylskbkphlfqjhwum` (`purchase_from`),
  KEY `FKr1cttxnd2bgtoers08anu0n0t` (`purchase_on_acc`),
  KEY `FKslelwuy725305nja1nr5rmxdn` (`resp_1`),
  KEY `FKp046m3ya29n2bm4tjcrofeq0n` (`resp_2`),
  KEY `FK9930yu96rqf6sxbo33b53e8ak` (`resp_3`),
  KEY `FKlmj270yk80mefj72l4l5p1m89` (`task_category_id`),
  CONSTRAINT `FK1j0icy0u15m8u4ari0ssq0cqv` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FK4ngw9ostwybtbgqkhef36072c` FOREIGN KEY (`branch`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK9930yu96rqf6sxbo33b53e8ak` FOREIGN KEY (`resp_3`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKbwv0py072qaq5hdjxprm59nj8` FOREIGN KEY (`genesetid`) REFERENCES `generator` (`id`),
  CONSTRAINT `FKlmj270yk80mefj72l4l5p1m89` FOREIGN KEY (`task_category_id`) REFERENCES `task_category` (`id`),
  CONSTRAINT `FKmip1q0r1mylskbkphlfqjhwum` FOREIGN KEY (`purchase_from`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKmm5o3yris13t6u6ryu399hcpl` FOREIGN KEY (`depreciation_id`) REFERENCES `depreciation_category_name_master` (`id`),
  CONSTRAINT `FKoseema3sq3g7f7mools3nc1yt` FOREIGN KEY (`classifyid`) REFERENCES `machine_classification` (`id`),
  CONSTRAINT `FKp046m3ya29n2bm4tjcrofeq0n` FOREIGN KEY (`resp_2`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKp32k1uqdf71ies55r4p7q50fs` FOREIGN KEY (`ebservice`) REFERENCES `eb_master` (`id`),
  CONSTRAINT `FKr1cttxnd2bgtoers08anu0n0t` FOREIGN KEY (`purchase_on_acc`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKslelwuy725305nja1nr5rmxdn` FOREIGN KEY (`resp_1`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_classification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_classification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_cluster`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_cluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpsogi18lbas5both3w58m8lg0` (`machine_id`),
  CONSTRAINT `FKpsogi18lbas5both3w58m8lg0` FOREIGN KEY (`machine_id`) REFERENCES `machine` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_cluster_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_cluster_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cluster_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlse2j6b24hado06jmjt0c9rnv` (`machine_id`),
  CONSTRAINT `FKlse2j6b24hado06jmjt0c9rnv` FOREIGN KEY (`machine_id`) REFERENCES `machine` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_wf_breakdown_reason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_wf_breakdown_reason` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `completed_date` datetime(6) DEFAULT NULL,
  `completed_time` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `machine_work_flow_id` bigint(20) DEFAULT NULL,
  `problem` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_wf_breakdown_reason_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_wf_breakdown_reason_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_name` varchar(255) DEFAULT NULL,
  `breakdown_reason_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `machine_work_flow_id` bigint(20) DEFAULT NULL,
  `quantity` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `zone` varchar(255) DEFAULT NULL,
  `consumption_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeyq3krbd00el8t9aw01i98fcw` (`consumption_id`),
  CONSTRAINT `FKeyq3krbd00el8t9aw01i98fcw` FOREIGN KEY (`consumption_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_wf_maintenance_reason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_wf_maintenance_reason` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin_name` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `machine_work_flow_id` bigint(20) DEFAULT NULL,
  `quantity` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `value_type` varchar(255) DEFAULT NULL,
  `zone` varchar(255) DEFAULT NULL,
  `assign_attribute_id` bigint(20) DEFAULT NULL,
  `consumption_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhak2gqi1cy9uq5i0whr0mfbb2` (`assign_attribute_id`),
  KEY `FK90wnyc4ooqpcxqdcfc6u0ifa8` (`consumption_id`),
  CONSTRAINT `FK90wnyc4ooqpcxqdcfc6u0ifa8` FOREIGN KEY (`consumption_id`) REFERENCES `products_master` (`id`),
  CONSTRAINT `FKhak2gqi1cy9uq5i0whr0mfbb2` FOREIGN KEY (`assign_attribute_id`) REFERENCES `attributes_assignments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_workflow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `breakdown_time` varchar(255) DEFAULT NULL,
  `breakdown_date` datetime(6) DEFAULT NULL,
  `c_status` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `detailed_reason` varchar(255) DEFAULT NULL,
  `detailed_reason_id` bigint(20) DEFAULT NULL,
  `log_id` int(11) DEFAULT NULL,
  `meter_closing_reading` varchar(255) DEFAULT NULL,
  `operated_by` bigint(20) DEFAULT NULL,
  `power_off_date` datetime(6) DEFAULT NULL,
  `power_off_reason` varchar(255) DEFAULT NULL,
  `reason_id` bigint(20) DEFAULT NULL,
  `power_off_time` varchar(255) DEFAULT NULL,
  `power_on_date` datetime(6) DEFAULT NULL,
  `power_on_time` varchar(255) DEFAULT NULL,
  `power_source` varchar(255) DEFAULT NULL,
  `production_shift` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `time_slab` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4v4j3xlpql4qgpyx42va6xmvi` (`machine_id`),
  CONSTRAINT `FK4v4j3xlpql4qgpyx42va6xmvi` FOREIGN KEY (`machine_id`) REFERENCES `machine` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `machine_workflow_panel_reading`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_workflow_panel_reading` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `machine_id` bigint(20) DEFAULT NULL,
  `meter_closing_reading` varchar(255) DEFAULT NULL,
  `meter_open_reading` varchar(255) DEFAULT NULL,
  `panel_id` bigint(20) DEFAULT NULL,
  `panel_name` varchar(255) DEFAULT NULL,
  `work_flow_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `mailsend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `mailsend` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mailid` varchar(255) DEFAULT NULL,
  `msg` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `maintainance_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `maintainance_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `empId` bigint(20) DEFAULT NULL,
  `machineAlloted` varchar(255) DEFAULT NULL,
  `maintainanceBreakdown` varchar(255) DEFAULT NULL,
  `maintainanceDone` varchar(255) DEFAULT NULL,
  `maintainanceReq` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhuj794ojsubtwum1u84cafpt9` (`user_id`),
  CONSTRAINT `FKhuj794ojsubtwum1u84cafpt9` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `maintenance_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `maintenance_options` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attributes_id` bigint(20) DEFAULT NULL,
  `options` varchar(255) DEFAULT NULL,
  `score` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `master_component`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `master_component` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `componentName` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `userLevelAccess` varchar(255) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqw7i85duyyvr8r9dq23vcj3pp` (`dept_id`),
  KEY `FKdeppyh32e2si50qwr0npqja86` (`org_id`),
  CONSTRAINT `FKdeppyh32e2si50qwr0npqja86` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKqw7i85duyyvr8r9dq23vcj3pp` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `master_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `master_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dataSelectionType` varchar(255) DEFAULT NULL,
  `fieldName` varchar(255) DEFAULT NULL,
  `fieldType` varchar(255) DEFAULT NULL,
  `masterComponentId` bigint(20) DEFAULT NULL,
  `maxMumLength` varchar(255) DEFAULT NULL,
  `possibleValues` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_flow_gdc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_flow_gdc` (
  `material_Flow_Gdc_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_Flag` int(11) DEFAULT NULL,
  `created_By_Branch_Id` bigint(20) DEFAULT NULL,
  `created_By_Department_Id` bigint(20) DEFAULT NULL,
  `created_By_Id` bigint(20) DEFAULT NULL,
  `created_Time_Stamp` datetime(6) DEFAULT NULL,
  `docket_Number` varchar(255) DEFAULT NULL,
  `eWayBill_QRCode` varchar(255) DEFAULT NULL,
  `eWayBill_cancelledBy` bigint(20) DEFAULT NULL,
  `eWayBill_cancelledOn` datetime(6) DEFAULT NULL,
  `eWayBill_flag` int(11) DEFAULT NULL,
  `eWayBill_no` varchar(255) DEFAULT NULL,
  `gdcVerifiedBy` bigint(20) DEFAULT NULL,
  `gdc_Company_Id` bigint(20) DEFAULT NULL,
  `gdc_Date` datetime(6) DEFAULT NULL,
  `gdc_Number` varchar(255) DEFAULT NULL,
  `gdc_Transaction_Type` varchar(255) DEFAULT NULL,
  `gdc_Transaction_Type_Detail_Id` bigint(20) DEFAULT NULL,
  `ho_goods_trans_id` bigint(20) DEFAULT NULL,
  `material_Return` varchar(255) DEFAULT NULL,
  `material_Return_Status` varchar(255) DEFAULT NULL,
  `sender_By_Branch_Id` bigint(20) DEFAULT NULL,
  `sender_By_Department` bigint(20) DEFAULT NULL,
  `sender_By_Id` bigint(20) DEFAULT NULL,
  `truck_Number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`material_Flow_Gdc_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_flow_gdc_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_flow_gdc_item` (
  `material_Flow_Gdc_Item_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `HSN_CODE` varchar(255) DEFAULT NULL,
  `gdc_Item_Avgcost` double DEFAULT NULL,
  `gdc_Item_Id` bigint(20) DEFAULT NULL,
  `gdc_Item_Name` varchar(255) DEFAULT NULL,
  `gdc_Item_Quandity` double DEFAULT NULL,
  `gdc_Item_Totalcost` double DEFAULT NULL,
  `gdc_Item_UOM` varchar(255) DEFAULT NULL,
  `gdc_Item_lot` double DEFAULT NULL,
  `material_Flow_Gdc_Number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`material_Flow_Gdc_Item_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_flow_send_external`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_flow_send_external` (
  `material_Flow_Send_External_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gdc_no` varchar(255) DEFAULT NULL,
  `active_Flag` bigint(20) DEFAULT NULL,
  `created_Branch_Id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_Department_Id` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `eway_bill` varchar(255) DEFAULT NULL,
  `firm_Id` bigint(20) DEFAULT NULL,
  `gdc_date` datetime(6) DEFAULT NULL,
  `issue_By_Id` bigint(20) DEFAULT NULL,
  `issue_Mode` varchar(255) DEFAULT NULL,
  `material_Return` varchar(255) DEFAULT NULL,
  `reference_Date` datetime(6) DEFAULT NULL,
  `status` bigint(20) DEFAULT NULL,
  `to_Branch_Id` bigint(20) DEFAULT NULL,
  `to_Department_Id` bigint(20) DEFAULT NULL,
  `to_Reach` varchar(255) DEFAULT NULL,
  `to_Vendor_Ref_Id` bigint(20) DEFAULT NULL,
  `transaction_Number` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vehicle` varchar(255) DEFAULT NULL,
  `vehicle_Number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`material_Flow_Send_External_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_flow_send_external_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_flow_send_external_items` (
  `material_flow_send_external_items_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material_return` varchar(255) DEFAULT NULL,
  `packing_Quantity` bigint(20) DEFAULT NULL,
  `packing_Type` varchar(255) DEFAULT NULL,
  `send_External_Item_Cost` double DEFAULT NULL,
  `send_External_Item_Hsncode` varchar(255) DEFAULT NULL,
  `send_External_Item_Id` bigint(20) DEFAULT NULL,
  `send_External_Item_Lot` double DEFAULT NULL,
  `send_External_Item_Name` varchar(255) DEFAULT NULL,
  `send_External_Item_Quantity` double DEFAULT NULL,
  `send_External_Item_Unit` varchar(255) DEFAULT NULL,
  `send_External_Ref_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`material_flow_send_external_items_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_issue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accounting_date` date DEFAULT NULL,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `request_date` date DEFAULT NULL,
  `request_description` varchar(255) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `request_name` varchar(255) DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKntew2erypkusw8dgiavna9pti` (`issue_branch_id`),
  KEY `FK8iv835gk5pammriw968xjts10` (`user_id`),
  KEY `FKf1kcc5q9o2eyqrnhngeu4t95e` (`issue_dept_id`),
  KEY `FK625tr1yl1hbqhyykywatiit7a` (`org_id`),
  KEY `FK7nl3yn4c7r11m9vi544953gku` (`request_branch_id`),
  KEY `FKjywv9d1d5wf8u9xj6h84lk10n` (`request_dept_id`),
  CONSTRAINT `FK625tr1yl1hbqhyykywatiit7a` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK7nl3yn4c7r11m9vi544953gku` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK8iv835gk5pammriw968xjts10` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKf1kcc5q9o2eyqrnhngeu4t95e` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKjywv9d1d5wf8u9xj6h84lk10n` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKntew2erypkusw8dgiavna9pti` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `request_Date` date DEFAULT NULL,
  `request_Description` varchar(255) DEFAULT NULL,
  `request_name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2txhfgpehumav5wdaddyhta9f` (`branch_id`),
  KEY `FKoaci4yxenaacnvwg8w6b52bvq` (`dept_id`),
  KEY `FK7p4uy2q545m0ubvnht5ies9tj` (`org_id`),
  KEY `FK5c1oxbnraw4ewb9kl504y5g5q` (`user_id`),
  CONSTRAINT `FK2txhfgpehumav5wdaddyhta9f` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK5c1oxbnraw4ewb9kl504y5g5q` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK7p4uy2q545m0ubvnht5ies9tj` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKoaci4yxenaacnvwg8w6b52bvq` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_requisition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_requisition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `mr_number` varchar(32) NOT NULL,
  `requested_by` bigint(20) NOT NULL,
  `requested_for_dept_id` bigint(20) DEFAULT NULL,
  `required_by_date` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `closed_at` timestamp NULL DEFAULT NULL,
  `assignee_id` bigint(20) DEFAULT NULL,
  `assigned_at` timestamp NULL DEFAULT NULL,
  `assigned_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_mr_number` (`tenant_id`,`mr_number`),
  KEY `idx_mr_status` (`tenant_id`,`status`,`current_step`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_requisition_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_requisition_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mr_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `qty_requested` decimal(12,3) NOT NULL,
  `qty_approved` decimal(12,3) DEFAULT NULL,
  `qty_issued` decimal(12,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `purpose` varchar(256) DEFAULT NULL,
  `line_status` varchar(32) NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `fk_mrl_mr` (`mr_id`),
  KEY `idx_mrl_status` (`line_status`),
  CONSTRAINT `fk_mrl_mr` FOREIGN KEY (`mr_id`) REFERENCES `material_requisition` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dispatch_date` date DEFAULT NULL,
  `external_godown_id` bigint(20) DEFAULT NULL,
  `from_dept_id` bigint(20) DEFAULT NULL,
  `from_dept_name` varchar(255) DEFAULT NULL,
  `from_id` bigint(20) DEFAULT NULL,
  `from_name` varchar(255) DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `issue_id` bigint(20) DEFAULT NULL,
  `request_date` date DEFAULT NULL,
  `request_description` varchar(255) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `request_name` varchar(255) DEFAULT NULL,
  `sent_external_id` bigint(20) DEFAULT NULL,
  `to_dept_id` bigint(20) DEFAULT NULL,
  `to_dept_name` varchar(255) DEFAULT NULL,
  `to_id` bigint(20) DEFAULT NULL,
  `to_name` varchar(255) DEFAULT NULL,
  `to_reach` varchar(255) DEFAULT NULL,
  `to_reach_id` bigint(20) DEFAULT NULL,
  `to_reach_name` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `current_step` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt12wln2vc3e3guyych8r5ne88` (`user_id`),
  KEY `FKadn0ggt6gug9gte5jlrs6tv75` (`org_id`),
  CONSTRAINT `FKadn0ggt6gug9gte5jlrs6tv75` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKt12wln2vc3e3guyych8r5ne88` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `material_within_branch_receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `material_within_branch_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `entry_date` date DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `issue_id` bigint(20) DEFAULT NULL,
  `received_date` date DEFAULT NULL,
  `request_date` date DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `issue_org_id` bigint(20) DEFAULT NULL,
  `received_user_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  `request_org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1pk4mtkrhp1eihmmy0q8s3jfb` (`issue_branch_id`),
  KEY `FKt49ctj9jryk9rsamo3bjm01r` (`user_id`),
  KEY `FKn2nm6cn9pa144utulweya1p4u` (`issue_dept_id`),
  KEY `FKca8883lhd1tueqp72ked3wmcl` (`issue_org_id`),
  KEY `FKo5s7sfda3338aex65j7na1rbk` (`received_user_id`),
  KEY `FKlprr0w9hjy9e4tbicoa41yfqa` (`request_branch_id`),
  KEY `FK3uo5ijjx0jwpaq3oy6cd62m8x` (`request_dept_id`),
  KEY `FK2ybuvqdvivoyk907i6ndrcqds` (`request_org_id`),
  CONSTRAINT `FK1pk4mtkrhp1eihmmy0q8s3jfb` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK2ybuvqdvivoyk907i6ndrcqds` FOREIGN KEY (`request_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK3uo5ijjx0jwpaq3oy6cd62m8x` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKca8883lhd1tueqp72ked3wmcl` FOREIGN KEY (`issue_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKlprr0w9hjy9e4tbicoa41yfqa` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKn2nm6cn9pa144utulweya1p4u` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKo5s7sfda3338aex65j7na1rbk` FOREIGN KEY (`received_user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKt49ctj9jryk9rsamo3bjm01r` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `medical_checkup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_checkup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `checkup_date` date DEFAULT NULL,
  `checkup_type` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `medical_check_up_value` varchar(255) DEFAULT NULL,
  `proof_report` varchar(255) DEFAULT NULL,
  `staff_category` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vaccination_name` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3vqg0n4ql15u15gi079ea0dx` (`branch_id`),
  KEY `FK7mq15jpvh51ugyyur0phqhtoe` (`dept_id`),
  KEY `FK43r4j4toxave78e2x5qp7jbm0` (`org_id`),
  KEY `FKogq0hwqu1adxqp8k3qnqm56qd` (`user_id`),
  CONSTRAINT `FK3vqg0n4ql15u15gi079ea0dx` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK43r4j4toxave78e2x5qp7jbm0` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK7mq15jpvh51ugyyur0phqhtoe` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKogq0hwqu1adxqp8k3qnqm56qd` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `menu_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `food_id` bigint(20) DEFAULT NULL,
  `menutype` varchar(255) DEFAULT NULL,
  `food_Item_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfxph9q79kdi7sr93eaetjs5ue` (`food_Item_id`),
  CONSTRAINT `FKfxph9q79kdi7sr93eaetjs5ue` FOREIGN KEY (`food_Item_id`) REFERENCES `food_item` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `mess_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `mess_voucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activeflag` bigint(20) DEFAULT NULL,
  `approveby` int(11) DEFAULT NULL,
  `approveon` datetime(6) DEFAULT NULL,
  `approvestatus` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `createby` int(11) DEFAULT NULL,
  `createon` datetime(6) DEFAULT NULL,
  `createstatus` varchar(255) DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `verifyby` int(11) DEFAULT NULL,
  `verifyon` datetime(6) DEFAULT NULL,
  `verifystatus` varchar(255) DEFAULT NULL,
  `voucherdate` date DEFAULT NULL,
  `voucherno` bigint(20) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `module_privilege_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `module_privilege_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `privilege_activate` varchar(255) DEFAULT NULL,
  `privilege_activate_across_branch` varchar(255) DEFAULT NULL,
  `privilege_amendment` varchar(255) DEFAULT NULL,
  `privilege_amendment_across_branch` varchar(255) DEFAULT NULL,
  `privilege_approve` varchar(255) DEFAULT NULL,
  `privilege_approve_across_branch` varchar(255) DEFAULT NULL,
  `privilege_authorize` varchar(255) DEFAULT NULL,
  `privilege_authorize_across_branch` varchar(255) DEFAULT NULL,
  `privilege_create` varchar(255) DEFAULT NULL,
  `privilege_create_across_branch` varchar(255) DEFAULT NULL,
  `privilege_delete` varchar(255) DEFAULT NULL,
  `privilege_delete_across_branch` varchar(255) DEFAULT NULL,
  `privilege_print` varchar(255) DEFAULT NULL,
  `privilege_print_across_branch` varchar(255) DEFAULT NULL,
  `privilege_reject` varchar(255) DEFAULT NULL,
  `privilege_reject_across_branch` varchar(255) DEFAULT NULL,
  `privilege_update` varchar(255) DEFAULT NULL,
  `privilege_update_across_branch` varchar(255) DEFAULT NULL,
  `privilege_validate` varchar(255) DEFAULT NULL,
  `privilege_validate_across_branch` varchar(255) DEFAULT NULL,
  `privilege_verify` varchar(255) DEFAULT NULL,
  `privilege_verify_across_branch` varchar(255) DEFAULT NULL,
  `privilege_view` varchar(255) DEFAULT NULL,
  `privilege_view_across_branch` varchar(255) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `module_id` bigint(20) DEFAULT NULL,
  `role_id` bigint(20) DEFAULT NULL,
  `submodule_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhm9mkruacg2fy7iamhk6fk336` (`dept_id`),
  KEY `FKm6wytvv0ltnxb1dkca5d3jbag` (`module_id`),
  KEY `FKqt42njm7auy4qvn2gup0efr4i` (`role_id`),
  KEY `FK3rfhfjb81uaa6cy2rn2tcgst4` (`submodule_id`),
  CONSTRAINT `FK3rfhfjb81uaa6cy2rn2tcgst4` FOREIGN KEY (`submodule_id`) REFERENCES `sub_modules` (`id`),
  CONSTRAINT `FKhm9mkruacg2fy7iamhk6fk336` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKm6wytvv0ltnxb1dkca5d3jbag` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`),
  CONSTRAINT `FKqt42njm7auy4qvn2gup0efr4i` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `modules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `multiple_choice_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `multiple_choice_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `optionA` varchar(255) DEFAULT NULL,
  `optionB` varchar(255) DEFAULT NULL,
  `optionC` varchar(255) DEFAULT NULL,
  `optionD` varchar(255) DEFAULT NULL,
  `question` varchar(255) DEFAULT NULL,
  `right_answer` varchar(255) DEFAULT NULL,
  `schedule_training_id` bigint(20) DEFAULT NULL,
  `score` float DEFAULT NULL,
  `serial_no` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `my_test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `my_test` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `no2_bill_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `no2_bill_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `bill_id` bigint(20) DEFAULT NULL,
  `cgst_amt` double DEFAULT NULL,
  `igst_amt` double DEFAULT NULL,
  `kattu` bigint(20) NOT NULL DEFAULT 0,
  `product_id` bigint(20) DEFAULT NULL,
  `quantity` bigint(20) DEFAULT NULL,
  `rate` double DEFAULT NULL,
  `sgst_amt` double DEFAULT NULL,
  `transaction_by` bigint(20) DEFAULT NULL,
  `transaction_date` datetime(6) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `no2_bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `no2_bills` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adate` datetime(6) DEFAULT NULL,
  `aodate` datetime(6) DEFAULT NULL,
  `approve_status` varchar(255) DEFAULT NULL,
  `authorize_status` varchar(255) DEFAULT NULL,
  `bill_prepare_by` bigint(20) DEFAULT NULL,
  `bill_status` varchar(255) DEFAULT NULL,
  `gstamt` double DEFAULT NULL,
  `kattu` bigint(20) NOT NULL DEFAULT 0,
  `net_amount` double DEFAULT NULL,
  `party_id` bigint(20) DEFAULT NULL,
  `perbag_amount` double DEFAULT NULL,
  `product_type` varchar(255) DEFAULT NULL,
  `quantity` bigint(20) DEFAULT NULL,
  `roundoff` double DEFAULT NULL,
  `tcs_amount` double DEFAULT NULL,
  `tcs_percent` double DEFAULT NULL,
  `total_cost` double DEFAULT NULL,
  `transaction_date` datetime(6) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `vdate` datetime(6) DEFAULT NULL,
  `verify_status` varchar(255) DEFAULT NULL,
  `approve_by` bigint(20) DEFAULT NULL,
  `authorize_by` bigint(20) DEFAULT NULL,
  `transaction_by` bigint(20) DEFAULT NULL,
  `verify_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq3w3vpretwv57n0bww988duws` (`approve_by`),
  KEY `FK5euxfr8ys1awmmx96cuw0rm7n` (`authorize_by`),
  KEY `FKf99pjjyt86wpk4m8v0v3cxdbo` (`transaction_by`),
  KEY `FKkcbnukg7aajfdra49ysbq8pnm` (`verify_by`),
  CONSTRAINT `FK5euxfr8ys1awmmx96cuw0rm7n` FOREIGN KEY (`authorize_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKf99pjjyt86wpk4m8v0v3cxdbo` FOREIGN KEY (`transaction_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKkcbnukg7aajfdra49ysbq8pnm` FOREIGN KEY (`verify_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKq3w3vpretwv57n0bww988duws` FOREIGN KEY (`approve_by`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `no2productpartymapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `no2productpartymapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `brokerid` bigint(20) DEFAULT NULL,
  `brokername` varchar(255) DEFAULT NULL,
  `lot` bigint(20) DEFAULT NULL,
  `originalamount` double DEFAULT NULL,
  `partyid` bigint(20) DEFAULT NULL,
  `productid` bigint(20) DEFAULT NULL,
  `product_type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `no2products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `no2products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bigint(20) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lot` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `percentage` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `producttype` varchar(255) DEFAULT NULL,
  `units` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `no2productsparty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `no2productsparty` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bigint(20) DEFAULT NULL,
  `broker_id` bigint(20) DEFAULT NULL,
  `broker_name` varchar(255) DEFAULT NULL,
  `partyid` bigint(20) DEFAULT NULL,
  `party_name` varchar(255) DEFAULT NULL,
  `percentage` double DEFAULT NULL,
  `productType` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` date DEFAULT NULL,
  `ref_party` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrltgytae0eprt99i7vv4tbedj` (`ref_party`),
  CONSTRAINT `FKrltgytae0eprt99i7vv4tbedj` FOREIGN KEY (`ref_party`) REFERENCES `ledger` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `no2ratehistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `no2ratehistory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `increasedecrease` bigint(20) DEFAULT NULL,
  `lot` bigint(20) DEFAULT NULL,
  `originalamount` double DEFAULT NULL,
  `partyid` bigint(20) DEFAULT NULL,
  `percentage` double DEFAULT NULL,
  `productid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `notification_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(64) NOT NULL,
  `channel` varchar(16) NOT NULL,
  `language` varchar(8) NOT NULL DEFAULT 'en-IN',
  `subject` varchar(256) DEFAULT NULL,
  `body` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nt_tenant_code_lang` (`tenant_id`,`code`,`language`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `offreason`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `offreason` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `open_ended_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `open_ended_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question` varchar(255) DEFAULT NULL,
  `right_answer` varchar(255) DEFAULT NULL,
  `schedule_training_id` bigint(20) DEFAULT NULL,
  `score` float DEFAULT NULL,
  `serial_no` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `options` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attributes_id` bigint(20) DEFAULT NULL,
  `options` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `organisation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `organisation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `GST` varchar(255) DEFAULT NULL,
  `activeflag` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fax` varchar(255) DEFAULT NULL,
  `lfname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `regn_1` varchar(255) DEFAULT NULL,
  `regn_2` varchar(255) DEFAULT NULL,
  `safetyinst` varchar(255) DEFAULT NULL,
  `search` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `stock` varchar(255) DEFAULT NULL,
  `trnprefix` varchar(255) DEFAULT NULL,
  `vattin` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `other_branch_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `other_branch_issue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gdc_no` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `delivery_user_id` bigint(20) DEFAULT NULL,
  `dispatch_on` date DEFAULT NULL,
  `generateeway_bill` varchar(255) DEFAULT NULL,
  `issue_id` bigint(20) DEFAULT NULL,
  `issue_mode` varchar(255) DEFAULT NULL,
  `issue_reference` varchar(255) DEFAULT NULL,
  `material_with_in_branch_id` bigint(20) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `transport_mode` varchar(255) DEFAULT NULL,
  `transport_vehicle_no` varchar(255) DEFAULT NULL,
  `transport_vehicle_type` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr987gj7ur0xqcsowlua66mp01` (`issue_branch_id`),
  KEY `FK6opfwki91vl36o0u39tje3i3m` (`user_id`),
  KEY `FK7ow1df4opjio6v3srtdf2chn2` (`issue_dept_id`),
  KEY `FK5cqhers26app87i7ufn1lia79` (`org_id`),
  KEY `FKa66x97x3k5ncwxktqjrciop32` (`request_branch_id`),
  KEY `FK5iqao2figy7k326vi3r4jxfy9` (`request_dept_id`),
  CONSTRAINT `FK5cqhers26app87i7ufn1lia79` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK5iqao2figy7k326vi3r4jxfy9` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FK6opfwki91vl36o0u39tje3i3m` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK7ow1df4opjio6v3srtdf2chn2` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKa66x97x3k5ncwxktqjrciop32` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKr987gj7ur0xqcsowlua66mp01` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `other_branch_issue_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `other_branch_issue_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `allocated_Quantity` varchar(255) DEFAULT NULL,
  `bin_stock_id` bigint(20) DEFAULT NULL,
  `branch_Id` bigint(20) DEFAULT NULL,
  `created_By` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept_Id` bigint(20) DEFAULT NULL,
  `issue` varchar(255) DEFAULT NULL,
  `issue_Item_Id` bigint(20) DEFAULT NULL,
  `issued_From_Batch` varchar(255) DEFAULT NULL,
  `issued_From_Bin` varchar(255) DEFAULT NULL,
  `item_Id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `material_return` varchar(255) DEFAULT NULL,
  `pending_Quantity` varchar(255) DEFAULT NULL,
  `pre_allocated_Quantity` varchar(255) DEFAULT NULL,
  `requested_Quantity` varchar(255) DEFAULT NULL,
  `select_Lot` varchar(255) DEFAULT NULL,
  `updated_By` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `other_branch_receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `other_branch_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `docket_no` varchar(255) DEFAULT NULL,
  `entry_date` datetime(6) DEFAULT NULL,
  `hup_id` varchar(255) DEFAULT NULL,
  `issue_date` datetime(6) DEFAULT NULL,
  `issue_id` bigint(20) DEFAULT NULL,
  `other_Branch_Receipt_Value` varchar(255) DEFAULT NULL,
  `received_date` datetime(6) DEFAULT NULL,
  `request_date` datetime(6) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_No` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `issue_org_id` bigint(20) DEFAULT NULL,
  `received_user_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  `request_org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9mlp6sj5h6gq62kla14u62ixu` (`issue_branch_id`),
  KEY `FKg2rhie5139anfk6n5rl0nlge3` (`user_id`),
  KEY `FKcx0x79x32fub82o34ag32todt` (`issue_dept_id`),
  KEY `FK2w1pq8utulslbae1dekb3kp5h` (`issue_org_id`),
  KEY `FKqgnsp2vw9rr4nr7ympx00nhni` (`received_user_id`),
  KEY `FKm991ihd6o1r4x6irjmxqa227c` (`request_branch_id`),
  KEY `FKnkxnvnwg03ueq9bde5ufwpltm` (`request_dept_id`),
  KEY `FK5nmxpujv9vj0nkgntuknne180` (`request_org_id`),
  CONSTRAINT `FK2w1pq8utulslbae1dekb3kp5h` FOREIGN KEY (`issue_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK5nmxpujv9vj0nkgntuknne180` FOREIGN KEY (`request_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK9mlp6sj5h6gq62kla14u62ixu` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKcx0x79x32fub82o34ag32todt` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKg2rhie5139anfk6n5rl0nlge3` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKm991ihd6o1r4x6irjmxqa227c` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKnkxnvnwg03ueq9bde5ufwpltm` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKqgnsp2vw9rr4nr7ympx00nhni` FOREIGN KEY (`received_user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `otherdeduction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `otherdeduction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `conid` bigint(20) DEFAULT NULL,
  `contable` varchar(255) DEFAULT NULL,
  `created_time` date DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `generatebill` bit(1) DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `our_company_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `our_company_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `pincode` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `outputProducts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `outputProducts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `binno` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `godown` varchar(255) DEFAULT NULL,
  `inventorytype` varchar(255) DEFAULT NULL,
  `outputlot` varchar(255) DEFAULT NULL,
  `outputquantity` varchar(255) DEFAULT NULL,
  `productname` varchar(255) DEFAULT NULL,
  `totalinputquantity` varchar(255) DEFAULT NULL,
  `totaloutputquantity` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_def_workflow_binding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_def_workflow_binding` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `page_version_id` bigint(20) NOT NULL,
  `workflow_code` varchar(128) NOT NULL,
  `workflow_version` int(11) NOT NULL,
  `binding_role` enum('SUBMIT_OPENS','SUBMIT_ADVANCES','SUBMIT_CLOSES','READ_ONLY') NOT NULL,
  `field_map_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`field_map_json`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_pdwb_pv` (`page_version_id`),
  KEY `idx_pdwb_workflow` (`workflow_code`,`workflow_version`),
  CONSTRAINT `fk_pdwb_pv` FOREIGN KEY (`page_version_id`) REFERENCES `page_version` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `code` varchar(128) NOT NULL,
  `title` varchar(256) NOT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(64) DEFAULT NULL,
  `current_version_id` bigint(20) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` bigint(20) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_page_tenant_code` (`tenant_id`,`code`),
  KEY `idx_page_tenant_status` (`tenant_id`,`status`,`category`)
) ENGINE=InnoDB AUTO_INCREMENT=416 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `pageName` varchar(255) DEFAULT NULL,
  `pageType` varchar(255) DEFAULT NULL,
  `parentMenu` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `userLevelAccess` varchar(255) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbbd5xrcq4tuamk6wh8wr6txfm` (`dept_id`),
  KEY `FKq48qhx675ni5oou9qoqi07y1e` (`org_id`),
  CONSTRAINT `FKbbd5xrcq4tuamk6wh8wr6txfm` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKq48qhx675ni5oou9qoqi07y1e` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_element_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_element_catalog` (
  `code` varchar(64) NOT NULL,
  `category` varchar(32) NOT NULL,
  `config_schema_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`config_schema_json`)),
  `default_props_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`default_props_json`)),
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_field` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `componentField` varchar(255) DEFAULT NULL,
  `componentName` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `fieldOrder` varchar(255) DEFAULT NULL,
  `pageId` bigint(20) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `page_id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  `status` varchar(16) NOT NULL,
  `body_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`body_json`)),
  `rationale` text DEFAULT NULL,
  `published_at` timestamp NULL DEFAULT NULL,
  `published_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_page_version` (`page_id`,`version`),
  CONSTRAINT `fk_pv_page` FOREIGN KEY (`page_id`) REFERENCES `page_definition` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=803 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `page_workflow_binding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `page_workflow_binding` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `page_code` varchar(128) NOT NULL,
  `workflow_code` varchar(128) NOT NULL,
  `step_code` varchar(128) NOT NULL,
  `role` enum('INPUT','OUTPUT','BOTH') NOT NULL DEFAULT 'INPUT',
  `field_map_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`field_map_json`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_pwb` (`tenant_id`,`page_code`,`workflow_code`,`step_code`,`role`),
  KEY `idx_pwb_workflow` (`tenant_id`,`workflow_code`,`step_code`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `panel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `panel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `benefit` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cluster` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `ebservice` varchar(255) DEFAULT NULL,
  `isono` varchar(255) DEFAULT NULL,
  `machine_on_off` bit(1) DEFAULT NULL,
  `max_units` double DEFAULT NULL,
  `mf` varchar(255) DEFAULT NULL,
  `panel_id` bigint(20) DEFAULT NULL,
  `panel_name` varchar(255) DEFAULT NULL,
  `pfinal` varchar(255) DEFAULT NULL,
  `pgodown` varchar(255) DEFAULT NULL,
  `pmachine` varchar(255) DEFAULT NULL,
  `purpose_benefit` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `work_Safety_Instructions` varchar(255) DEFAULT NULL,
  `eb_service_id` bigint(20) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  `unit_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlyv8k84khvqypvpuh2v7rvuxg` (`eb_service_id`),
  KEY `FK8vosibhlfw62jlrxg74i38dul` (`godown_id`),
  KEY `FK700ldc90lrr0865d20naklrm0` (`machine_id`),
  KEY `FK7ss3yrvcofj74ydt0inuie8q6` (`unit_id`),
  CONSTRAINT `FK700ldc90lrr0865d20naklrm0` FOREIGN KEY (`machine_id`) REFERENCES `machine` (`id`),
  CONSTRAINT `FK7ss3yrvcofj74ydt0inuie8q6` FOREIGN KEY (`unit_id`) REFERENCES `units` (`id`),
  CONSTRAINT `FK8vosibhlfw62jlrxg74i38dul` FOREIGN KEY (`godown_id`) REFERENCES `godown` (`id`),
  CONSTRAINT `FKlyv8k84khvqypvpuh2v7rvuxg` FOREIGN KEY (`eb_service_id`) REFERENCES `eb_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `panel_name_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `panel_name_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `paoptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `paoptions` (
  `primaryid` bigint(20) NOT NULL,
  `id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`primaryid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `patlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `patlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dcolor` varchar(255) DEFAULT NULL,
  `dunit` varchar(255) DEFAULT NULL,
  `entry` date DEFAULT NULL,
  `journalflag` varchar(255) DEFAULT NULL,
  `kpi` bigint(20) DEFAULT NULL,
  `logid` bigint(20) DEFAULT NULL,
  `logtype` varchar(255) DEFAULT NULL,
  `mcauxid` bigint(20) DEFAULT NULL,
  `mcauxtable` varchar(255) DEFAULT NULL,
  `mcid` bigint(20) DEFAULT NULL,
  `mctable` varchar(255) DEFAULT NULL,
  `mmcid` bigint(20) DEFAULT NULL,
  `mmctable` varchar(255) DEFAULT NULL,
  `no_of_bag` varchar(255) DEFAULT NULL,
  `proreportdbid` bigint(20) DEFAULT NULL,
  `resid` bigint(20) DEFAULT NULL,
  `resscore` int(11) DEFAULT NULL,
  `restype` varchar(255) DEFAULT NULL,
  `resval` varchar(255) DEFAULT NULL,
  `schid` bigint(20) DEFAULT NULL,
  `scoreto` varchar(255) DEFAULT NULL,
  `scoretoid` bigint(20) DEFAULT NULL,
  `shiftdate` datetime(6) DEFAULT NULL,
  `shiftid` bigint(20) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  `tno` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `pattributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pattributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `datatype` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `pattributes_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pattributes_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attributes_id` bigint(20) DEFAULT NULL,
  `attributes_name` varchar(255) DEFAULT NULL,
  `avg` varchar(255) DEFAULT NULL,
  `awith` varchar(255) DEFAULT NULL,
  `awithid` bigint(20) DEFAULT NULL,
  `awithtable` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `datatype` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `disporder` int(11) DEFAULT NULL,
  `dispunit` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `linkwith` varchar(255) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `total` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_items_id` int(11) DEFAULT NULL,
  `action_items_name` varchar(255) DEFAULT NULL,
  `module_id` int(11) DEFAULT NULL,
  `module_name` varchar(255) DEFAULT NULL,
  `role_id` int(11) DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  `sub_module_id` int(11) DEFAULT NULL,
  `sub_module_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `physical_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `physical_stock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `adate` date DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `binid` varchar(255) DEFAULT NULL,
  `binname` bigint(20) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `gid` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `pmschedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pmschedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` varchar(255) DEFAULT NULL,
  `approver` bigint(20) DEFAULT NULL,
  `audit` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `begintime` varchar(255) DEFAULT NULL,
  `clusres` bigint(20) DEFAULT NULL,
  `clusval` bigint(20) DEFAULT NULL,
  `countdt` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `delaymf` bigint(20) DEFAULT NULL,
  `describe` varchar(255) DEFAULT NULL,
  `ext` varchar(255) DEFAULT NULL,
  `frequency` bigint(20) DEFAULT NULL,
  `last_completed_date` datetime(6) DEFAULT NULL,
  `mailid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `ontime` bigint(20) DEFAULT NULL,
  `option` varchar(255) DEFAULT NULL,
  `pfinal` varchar(255) DEFAULT NULL,
  `ppublic` varchar(255) DEFAULT NULL,
  `remain` bigint(20) DEFAULT NULL,
  `remaind_me` varchar(255) DEFAULT NULL,
  `remind` bigint(20) DEFAULT NULL,
  `responsibility` bigint(20) DEFAULT NULL,
  `responsibility1` bigint(20) DEFAULT NULL,
  `responsibility2` bigint(20) DEFAULT NULL,
  `schedule_date` date DEFAULT NULL,
  `scheduleid` bigint(20) DEFAULT NULL,
  `esc1` bigint(20) DEFAULT NULL,
  `esc2` bigint(20) DEFAULT NULL,
  `esc3` bigint(20) DEFAULT NULL,
  `esc4` bigint(20) DEFAULT NULL,
  `unlockres` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `validator` bigint(20) DEFAULT NULL,
  `machineid` bigint(20) DEFAULT NULL,
  `panelid` bigint(20) DEFAULT NULL,
  `taskcategory` bigint(20) DEFAULT NULL,
  `task` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt80jk76j3amy72399oarjw1xx` (`machineid`),
  KEY `FK7ha8qcpjergdirw82h45st4xh` (`panelid`),
  KEY `FKewspw14pdi712nafhwx0w0by5` (`taskcategory`),
  KEY `FKk215aa38rycf5cm4w5c2jefto` (`task`),
  CONSTRAINT `FK7ha8qcpjergdirw82h45st4xh` FOREIGN KEY (`panelid`) REFERENCES `panel` (`id`),
  CONSTRAINT `FKewspw14pdi712nafhwx0w0by5` FOREIGN KEY (`taskcategory`) REFERENCES `task_category` (`id`),
  CONSTRAINT `FKk215aa38rycf5cm4w5c2jefto` FOREIGN KEY (`task`) REFERENCES `task_group_master` (`id`),
  CONSTRAINT `FKt80jk76j3amy72399oarjw1xx` FOREIGN KEY (`machineid`) REFERENCES `machine` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `pmscheduledetails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pmscheduledetails` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` varchar(255) DEFAULT NULL,
  `active_flag` int(11) DEFAULT NULL,
  `approver` bigint(20) DEFAULT NULL,
  `audit` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `begintime` varchar(255) DEFAULT NULL,
  `clusres` bigint(20) DEFAULT NULL,
  `clusval` bigint(20) DEFAULT NULL,
  `countdt` datetime(6) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `delaymf` bigint(20) DEFAULT NULL,
  `describe` varchar(255) DEFAULT NULL,
  `ext` varchar(255) DEFAULT NULL,
  `frequency` bigint(20) DEFAULT NULL,
  `last_completed_date` datetime(6) DEFAULT NULL,
  `machineid` bigint(20) DEFAULT NULL,
  `mailid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `ontime` bigint(20) DEFAULT NULL,
  `option` varchar(255) DEFAULT NULL,
  `panelid` bigint(20) DEFAULT NULL,
  `pfinal` varchar(255) DEFAULT NULL,
  `pm_schedule_id` bigint(20) DEFAULT NULL,
  `ppublic` varchar(255) DEFAULT NULL,
  `remain` bigint(20) DEFAULT NULL,
  `remaind_me` varchar(255) DEFAULT NULL,
  `remind` bigint(20) DEFAULT NULL,
  `responsibility` bigint(20) DEFAULT NULL,
  `responsibility1` bigint(20) DEFAULT NULL,
  `responsibility2` bigint(20) DEFAULT NULL,
  `schedule_date` date DEFAULT NULL,
  `scheduleid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `taskcategory` bigint(20) DEFAULT NULL,
  `task` bigint(20) DEFAULT NULL,
  `esc1` bigint(20) DEFAULT NULL,
  `esc2` bigint(20) DEFAULT NULL,
  `esc3` bigint(20) DEFAULT NULL,
  `esc4` bigint(20) DEFAULT NULL,
  `unlockres` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `validator` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `preprocess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `preprocess` (
  `priid` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`priid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `prgrppro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `prgrppro` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdby` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `lotname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `prgrpid` bigint(20) DEFAULT NULL,
  `prid` bigint(20) DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  `prprogrp` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `prgrpsjid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `prgrpsjid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdby` bigint(20) DEFAULT NULL,
  `prgrpid` bigint(20) DEFAULT NULL,
  `prgrptitle` bigint(20) DEFAULT NULL,
  `prid` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `primary_expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `primary_expenses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `print_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `print_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `code` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `output_kind` varchar(16) NOT NULL DEFAULT 'html',
  `target_table` varchar(64) NOT NULL,
  `child_table` varchar(64) DEFAULT NULL,
  `child_fk` varchar(64) DEFAULT NULL,
  `spec_json` text NOT NULL,
  `label_zpl` text DEFAULT NULL,
  `copies` int(11) NOT NULL DEFAULT 1,
  `printer_integration_code` varchar(64) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_print_template` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `privilege_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `privilege_details` (
  `privilegeId` bigint(20) NOT NULL AUTO_INCREMENT,
  `privilege_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`privilegeId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `process_report_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `process_report_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `prid` bigint(20) DEFAULT NULL,
  `total` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `process_report_group_title`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `process_report_group_title` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdby` bigint(20) DEFAULT NULL,
  `disp_name` varchar(255) DEFAULT NULL,
  `order` bigint(20) DEFAULT NULL,
  `prgrpid` bigint(20) DEFAULT NULL,
  `prgrptitle` varchar(255) DEFAULT NULL,
  `prid` bigint(20) DEFAULT NULL,
  `prprgrp` bigint(20) DEFAULT NULL,
  `trntype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `process_report_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `process_report_name` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `process_report_product_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `process_report_product_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activeFlag` bigint(20) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `prid` bigint(20) DEFAULT NULL,
  `prigrpid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `productName` varchar(255) DEFAULT NULL,
  `totalQuantity` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_category` (
  `code` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_excess_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_excess_storage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bill_Date` date DEFAULT NULL,
  `bill_Inward_Date` date DEFAULT NULL,
  `bill_no` varchar(255) DEFAULT NULL,
  `bill_Transactionno` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lorry_Serial_No` varchar(255) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `rejected_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3bw1klhm6oe0r2gqrc2iui5ca` (`firm_id`),
  KEY `FKc2l4oq0pm5h808ppv1htmceub` (`vendor_id`),
  CONSTRAINT `FK3bw1klhm6oe0r2gqrc2iui5ca` FOREIGN KEY (`firm_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKc2l4oq0pm5h808ppv1htmceub` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_map_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_map_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_mapping` (
  `product_mapping_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_category_ref_id` bigint(20) DEFAULT NULL,
  `product_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`product_mapping_id`),
  KEY `FK9x6fvoyilqf35lm835ye62mvv` (`product_ref_id`),
  CONSTRAINT `FK9x6fvoyilqf35lm835ye62mvv` FOREIGN KEY (`product_ref_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_pieces_count`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_pieces_count` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `no_of_pieces` bigint(20) DEFAULT NULL,
  `lot_pieces` bigint(20) DEFAULT NULL,
  `product_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `approver` bigint(20) DEFAULT NULL,
  `audit` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `begintime` time DEFAULT NULL,
  `clusres` bigint(20) DEFAULT NULL,
  `clusval` bigint(20) DEFAULT NULL,
  `count_Date` date DEFAULT NULL,
  `created_On` datetime(6) DEFAULT NULL,
  `delaymf` int(11) DEFAULT NULL,
  `describe` varchar(255) DEFAULT NULL,
  `esc1` bigint(20) DEFAULT NULL,
  `esc2` bigint(20) DEFAULT NULL,
  `esc3` bigint(20) DEFAULT NULL,
  `esc4` bigint(20) DEFAULT NULL,
  `ext` varchar(255) DEFAULT NULL,
  `finals` varchar(255) DEFAULT NULL,
  `fpublic` varchar(255) DEFAULT NULL,
  `frequency` int(11) DEFAULT NULL,
  `machine_Id` bigint(20) DEFAULT NULL,
  `mail_Id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `ontime` int(11) DEFAULT NULL,
  `option` varchar(255) DEFAULT NULL,
  `remain` int(11) DEFAULT NULL,
  `remind` int(11) DEFAULT NULL,
  `responsibility` bigint(20) DEFAULT NULL,
  `responsibility1` bigint(20) DEFAULT NULL,
  `responsibility2` bigint(20) DEFAULT NULL,
  `task_Category_Id` bigint(20) DEFAULT NULL,
  `task_Id` bigint(20) DEFAULT NULL,
  `unlockres` bigint(20) DEFAULT NULL,
  `validator` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `production_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `CAVG` double DEFAULT NULL,
  `CTOTAL` double DEFAULT NULL,
  `accounting_date` date DEFAULT NULL,
  `avg` varchar(255) DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bom_Intake_Type` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `c1` varchar(255) DEFAULT NULL,
  `c10` varchar(255) DEFAULT NULL,
  `c11` varchar(255) DEFAULT NULL,
  `c12` varchar(255) DEFAULT NULL,
  `c13` varchar(255) DEFAULT NULL,
  `c2` varchar(255) DEFAULT NULL,
  `c3` varchar(255) DEFAULT NULL,
  `c4` varchar(255) DEFAULT NULL,
  `c5` varchar(255) DEFAULT NULL,
  `c6` varchar(255) DEFAULT NULL,
  `c7` varchar(255) DEFAULT NULL,
  `c8` varchar(255) DEFAULT NULL,
  `c9` varchar(255) DEFAULT NULL,
  `colcate` varchar(255) DEFAULT NULL,
  `colname` varchar(255) DEFAULT NULL,
  `coltable` varchar(255) DEFAULT NULL,
  `coltype` varchar(255) DEFAULT NULL,
  `col_lot` varchar(255) DEFAULT NULL,
  `colid` bigint(20) DEFAULT NULL,
  `consider_as` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `data_res_values` varchar(255) DEFAULT NULL,
  `datatype` varchar(255) DEFAULT NULL,
  `dctype` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `dispname` varchar(255) DEFAULT NULL,
  `dispunit` bigint(20) DEFAULT NULL,
  `disporder` int(11) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `godown` varchar(255) DEFAULT NULL,
  `godownid` bigint(20) DEFAULT NULL,
  `humidity` varchar(255) DEFAULT NULL,
  `inward_date` date DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `mcauxid` bigint(20) DEFAULT NULL,
  `mcauxtable` varchar(255) DEFAULT NULL,
  `mmcid` bigint(20) DEFAULT NULL,
  `mmctable` varchar(255) DEFAULT NULL,
  `order_number` varchar(255) DEFAULT NULL,
  `ptype` varchar(255) DEFAULT NULL,
  `pshift` bigint(20) DEFAULT NULL,
  `shiftdate` date DEFAULT NULL,
  `shiftid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `temperature` varchar(255) DEFAULT NULL,
  `total` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `tsid` bigint(20) DEFAULT NULL,
  `tsname` varchar(255) DEFAULT NULL,
  `tstable` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `upstatus` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `bin` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `lot` bigint(20) DEFAULT NULL,
  `product_code` varchar(255) DEFAULT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `product_type` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `trn_type` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `products_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_weight` double DEFAULT NULL,
  `allowadd` varchar(255) DEFAULT NULL,
  `altunit` varchar(255) DEFAULT NULL,
  `asrs_product_type` varchar(255) DEFAULT NULL,
  `avgqty1` varchar(255) DEFAULT NULL,
  `avgqty2` varchar(255) DEFAULT NULL,
  `avgqty3` varchar(255) DEFAULT NULL,
  `bom` varchar(255) DEFAULT NULL,
  `blending` bit(1) DEFAULT NULL,
  `bye` varchar(255) DEFAULT NULL,
  `cluster` varchar(255) DEFAULT NULL,
  `contractpurchase` varchar(255) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `cst` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `gweight` varchar(255) DEFAULT NULL,
  `ho_status` varchar(255) DEFAULT NULL,
  `hsn_Ref_Id` varchar(255) DEFAULT NULL,
  `inward` varchar(255) DEFAULT NULL,
  `library_flag` varchar(255) DEFAULT NULL,
  `maintain` varchar(255) DEFAULT NULL,
  `minoq` varchar(255) DEFAULT NULL,
  `packingtype` varchar(255) DEFAULT NULL,
  `pallet_quantity` bigint(20) DEFAULT NULL,
  `pcategory` varchar(255) DEFAULT NULL,
  `picture` varchar(255) DEFAULT NULL,
  `pictureheight` varchar(255) DEFAULT NULL,
  `picturename` varchar(255) DEFAULT NULL,
  `picturesize` int(11) DEFAULT NULL,
  `picturetype` varchar(255) DEFAULT NULL,
  `picturewidth` varchar(255) DEFAULT NULL,
  `piece_quantity` int(11) DEFAULT NULL,
  `pofreq` varchar(255) DEFAULT NULL,
  `productcategory` varchar(255) DEFAULT NULL,
  `productcode` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `product_category_type` varchar(255) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `rawmaterial` varchar(255) DEFAULT NULL,
  `recycle` varchar(255) DEFAULT NULL,
  `search` varchar(255) DEFAULT NULL,
  `send_ft_to_ho_object` varchar(255) DEFAULT NULL,
  `split_config` bit(1) DEFAULT NULL,
  `split_config_based` varchar(255) DEFAULT NULL,
  `standing_authorise` varchar(255) DEFAULT NULL,
  `standing_order` varchar(255) DEFAULT NULL,
  `stock_ledger` bit(1) DEFAULT NULL,
  `taskcategory` varchar(255) DEFAULT NULL,
  `tcid` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `unit1` varchar(255) DEFAULT NULL,
  `unit2` varchar(255) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vat` int(11) DEFAULT NULL,
  `where1` varchar(255) DEFAULT NULL,
  `where2` varchar(255) DEFAULT NULL,
  `workinst` varchar(255) DEFAULT NULL,
  `dept_id` bigint(20) NOT NULL,
  `purchase_responsibility_plant` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsqw22i06d38lnhb55ahs01i4s` (`purchase_responsibility_plant`),
  CONSTRAINT `FKsqw22i06d38lnhb55ahs01i4s` FOREIGN KEY (`purchase_responsibility_plant`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `professional_tax_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `professional_tax_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `ptAmount` double DEFAULT NULL,
  `salaryRange_From` double DEFAULT NULL,
  `salaryRange_To` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proposal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activated_date` datetime(6) DEFAULT NULL,
  `active` bigint(20) DEFAULT NULL,
  `approved_date` datetime(6) DEFAULT NULL,
  `authorized_date` datetime(6) DEFAULT NULL,
  `authorizer_role` bigint(20) DEFAULT NULL,
  `category` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `po_status` varchar(255) DEFAULT NULL,
  `proposal_status` varchar(255) DEFAULT NULL,
  `rejected_date` datetime(6) DEFAULT NULL,
  `singlevendor` bit(1) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `valid_date` datetime(6) DEFAULT NULL,
  `validated1_date` datetime(6) DEFAULT NULL,
  `validated2_date` datetime(6) DEFAULT NULL,
  `verifier1_date` datetime(6) DEFAULT NULL,
  `verifier2_date` datetime(6) DEFAULT NULL,
  `verifier3_date` datetime(6) DEFAULT NULL,
  `activated_by` bigint(20) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `authorized_by` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `verified_by4` bigint(20) DEFAULT NULL,
  `verified_by5` bigint(20) DEFAULT NULL,
  `verified_by1` bigint(20) DEFAULT NULL,
  `verified_by2` bigint(20) DEFAULT NULL,
  `verified_by3` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK83gcp975094ytxi5h2t7el57v` (`activated_by`),
  KEY `FK8nqeb267t0m7k5mhjo5d11h7p` (`approved_by`),
  KEY `FKq31ch527vp5n5hr5kqiihu73n` (`authorized_by`),
  KEY `FK6vdq421k7v5rxn5t2esysgaoa` (`branch_id`),
  KEY `FKlum1i8d86ydyfqxfpp9ol88yi` (`dept_id`),
  KEY `FK70nkip5kc5vasxy91p4oy48ui` (`rejected_by`),
  KEY `FKhidq6wih0utwivwq64jt12reg` (`verified_by4`),
  KEY `FK1v0jued6w3d9u2thaw32364ir` (`verified_by5`),
  KEY `FKndht1rof8khdf3jqf25ni8vmt` (`verified_by1`),
  KEY `FKshysl81xeqv14ovsxtcejhfad` (`verified_by2`),
  KEY `FK1bf09fbrbgl7ehirh79fvptn9` (`verified_by3`),
  CONSTRAINT `FK1bf09fbrbgl7ehirh79fvptn9` FOREIGN KEY (`verified_by3`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK1v0jued6w3d9u2thaw32364ir` FOREIGN KEY (`verified_by5`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK6vdq421k7v5rxn5t2esysgaoa` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK70nkip5kc5vasxy91p4oy48ui` FOREIGN KEY (`rejected_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK83gcp975094ytxi5h2t7el57v` FOREIGN KEY (`activated_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK8nqeb267t0m7k5mhjo5d11h7p` FOREIGN KEY (`approved_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKhidq6wih0utwivwq64jt12reg` FOREIGN KEY (`verified_by4`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKlum1i8d86ydyfqxfpp9ol88yi` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKndht1rof8khdf3jqf25ni8vmt` FOREIGN KEY (`verified_by1`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKq31ch527vp5n5hr5kqiihu73n` FOREIGN KEY (`authorized_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKshysl81xeqv14ovsxtcejhfad` FOREIGN KEY (`verified_by2`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proposal_quotation_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_quotation_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) DEFAULT NULL,
  `proposal_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proposal_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_spec` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `active_flag` int(11) DEFAULT NULL,
  `approved_price` double DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `packing_product` bit(1) DEFAULT NULL,
  `proposal_ref_id` bigint(20) DEFAULT NULL,
  `specid` bigint(20) DEFAULT NULL,
  `spectype` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proposal_vendor_spec`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_vendor_spec` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `active_flag` int(11) DEFAULT NULL,
  `basic_price` double DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `delivery_in_days` bigint(20) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `net_approved_price` double DEFAULT NULL,
  `net_approved_qty` double DEFAULT NULL,
  `packing_forward` double DEFAULT NULL,
  `packing_frieght` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `percentage_split` double DEFAULT NULL,
  `po_validity_request` varchar(255) DEFAULT NULL,
  `proposal_ref_id` bigint(20) DEFAULT NULL,
  `proposal_spec_id` bigint(20) DEFAULT NULL,
  `proposal_tax` double DEFAULT NULL,
  `quotation_date` datetime(6) DEFAULT NULL,
  `quotation_no` varchar(255) DEFAULT NULL,
  `recommended_vendor` bit(1) DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `proreportdb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `proreportdb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `atname` varchar(255) DEFAULT NULL,
  `attribute_max_value` varchar(255) DEFAULT NULL,
  `attribute_min_value` varchar(255) DEFAULT NULL,
  `attribute_range_value` varchar(255) DEFAULT NULL,
  `attype` varchar(255) DEFAULT NULL,
  `bgid` varchar(255) DEFAULT NULL,
  `cas` varchar(255) DEFAULT NULL,
  `cascolid` varchar(255) DEFAULT NULL,
  `colid` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `cv` varchar(255) DEFAULT NULL,
  `cvamt` float DEFAULT NULL,
  `cvtype` varchar(255) DEFAULT NULL,
  `formula` varchar(255) DEFAULT NULL,
  `input_type` varchar(255) DEFAULT NULL,
  `iv` varchar(255) DEFAULT NULL,
  `ivamt` float DEFAULT NULL,
  `ivtype` varchar(255) DEFAULT NULL,
  `kpi` bigint(20) DEFAULT NULL,
  `lastupid` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `rstatus` varchar(255) DEFAULT NULL,
  `sambanthi_formula` varchar(255) DEFAULT NULL,
  `scre` varchar(255) DEFAULT NULL,
  `stk` varchar(255) DEFAULT NULL,
  `stkprn` varchar(255) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  `units` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `pshifts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pshifts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hours` double DEFAULT NULL,
  `intime` time DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `outtime` time DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_advance_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_advance_payment` (
  `purchase_advance_payment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acount_number` varchar(255) DEFAULT NULL,
  `activate_date` datetime(6) DEFAULT NULL,
  `active_flag` int(11) DEFAULT NULL,
  `advance_amount` double DEFAULT NULL,
  `advance_payment_company_id` bigint(20) DEFAULT NULL,
  `advance_payment_date` datetime(6) DEFAULT NULL,
  `approved_date` datetime(6) DEFAULT NULL,
  `authorized_date` datetime(6) DEFAULT NULL,
  `created_time_stamp` datetime(6) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `ddfavourof` varchar(255) DEFAULT NULL,
  `ifsc` varchar(255) DEFAULT NULL,
  `number_of_pay` int(11) DEFAULT NULL,
  `payment_category_id` bigint(20) DEFAULT NULL,
  `payment_category_name` varchar(255) DEFAULT NULL,
  `payment_mode` varchar(255) DEFAULT NULL,
  `payment_type` varchar(255) DEFAULT NULL,
  `pending_amount` double DEFAULT NULL,
  `percentage` double DEFAULT NULL,
  `pinfo` int(11) DEFAULT NULL,
  `po_amount` double DEFAULT NULL,
  `purchase_order_ref_id` bigint(20) DEFAULT NULL,
  `purchase_payment_id` bigint(20) DEFAULT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `quotation_date` datetime(6) DEFAULT NULL,
  `quotation_number` varchar(255) DEFAULT NULL,
  `rejected_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tds` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `vendor_address` varchar(255) DEFAULT NULL,
  `vendor_ref_id` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `activate_by_id` bigint(20) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `authorized_by` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `create_by_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`purchase_advance_payment_id`),
  KEY `FKmqumco60ip11gy6g36t474hbx` (`activate_by_id`),
  KEY `FKmova9c8q68ifc68exjquwul5c` (`approved_by`),
  KEY `FKjbpv1ggdcqsdkels5dchf3vob` (`authorized_by`),
  KEY `FKh9uaxydwyvrwo9itv1aau8l48` (`branch_id`),
  KEY `FK2bhkf1csn76n7ipa876nl3umk` (`create_by_id`),
  KEY `FKg82r5bf44r4oua52nyyvalwc0` (`dept_id`),
  KEY `FK3lo8gbcwd1pt7s8ap7j26psqj` (`firm_id`),
  KEY `FKlhc8xdeick8tba3nk9glngpnh` (`rejected_by`),
  KEY `FK1vd37dbv4jje2fb2wggnlf34i` (`verified_by`),
  CONSTRAINT `FK1vd37dbv4jje2fb2wggnlf34i` FOREIGN KEY (`verified_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK2bhkf1csn76n7ipa876nl3umk` FOREIGN KEY (`create_by_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK3lo8gbcwd1pt7s8ap7j26psqj` FOREIGN KEY (`firm_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKg82r5bf44r4oua52nyyvalwc0` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKh9uaxydwyvrwo9itv1aau8l48` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKjbpv1ggdcqsdkels5dchf3vob` FOREIGN KEY (`authorized_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKlhc8xdeick8tba3nk9glngpnh` FOREIGN KEY (`rejected_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKmova9c8q68ifc68exjquwul5c` FOREIGN KEY (`approved_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKmqumco60ip11gy6g36t474hbx` FOREIGN KEY (`activate_by_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_amc_expense`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_amc_expense` (
  `amc_Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amc_Category_Id` int(11) DEFAULT NULL,
  `amc_Expense_Name` varchar(255) DEFAULT NULL,
  `amc_Expense_Variable_type` int(11) DEFAULT NULL,
  `amc_Expense_cost` double DEFAULT NULL,
  `amc_Expense_lot` int(11) DEFAULT NULL,
  `amc_Expense_unit` int(11) DEFAULT NULL,
  `amc_Flag` int(11) DEFAULT NULL,
  `amc_Machine_Ref_Id` bigint(20) DEFAULT NULL,
  `branch` int(11) DEFAULT NULL,
  `company` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_Timestamp` datetime(6) DEFAULT NULL,
  `department` int(11) DEFAULT NULL,
  `hsn_Ref_Id` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_Timestamp` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`amc_Id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_bill_approve`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_bill_approve` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_Flag` bigint(20) DEFAULT NULL,
  `bill_Amount` varchar(255) DEFAULT NULL,
  `bill_Date` datetime(6) DEFAULT NULL,
  `bill_Description` varchar(255) DEFAULT NULL,
  `bill_Number` varchar(255) DEFAULT NULL,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `bill_Status` bigint(20) DEFAULT NULL,
  `bill_Type` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `created_Time` datetime(6) DEFAULT NULL,
  `currency_Flag` int(11) DEFAULT NULL,
  `exchange_Branch_Id` bigint(20) DEFAULT NULL,
  `exchange_By_Id` bigint(20) DEFAULT NULL,
  `exchange_Department` bigint(20) DEFAULT NULL,
  `lorry_Date` datetime(6) DEFAULT NULL,
  `lorry_Serial_Number` varchar(255) DEFAULT NULL,
  `mail_Status` int(11) DEFAULT NULL,
  `packing_Type` bigint(20) DEFAULT NULL,
  `packing_Type_Qty` varchar(255) DEFAULT NULL,
  `payment_type` int(11) DEFAULT NULL,
  `purchaseReturn_Flag` int(11) DEFAULT NULL,
  `qc_Failure_Flag` int(11) DEFAULT NULL,
  `received_By_Id` bigint(20) DEFAULT NULL,
  `received_Department` bigint(20) DEFAULT NULL,
  `tcs_flag` varchar(255) DEFAULT NULL,
  `tcs_tax_amount` varchar(255) DEFAULT NULL,
  `tcs_tax_percentage` varchar(255) DEFAULT NULL,
  `vendor_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address_id` bigint(20) DEFAULT NULL,
  `advance_type` bit(1) DEFAULT NULL,
  `amendment_date` datetime(6) DEFAULT NULL,
  `approval_status` varchar(255) DEFAULT NULL,
  `approved_on` datetime(6) DEFAULT NULL,
  `authorized_on` datetime(6) DEFAULT NULL,
  `broker_id` bigint(20) DEFAULT NULL,
  `broker_name` varchar(255) DEFAULT NULL,
  `camp` varchar(255) DEFAULT NULL,
  `camp_id` bigint(20) DEFAULT NULL,
  `cess_amount` double DEFAULT NULL,
  `cess_date` datetime(6) DEFAULT NULL,
  `cess_no` varchar(255) DEFAULT NULL,
  `cess_type` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `city_id` bigint(20) DEFAULT NULL,
  `company_branch_id` bigint(20) DEFAULT NULL,
  `created_on` datetime(6) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `deliveryduedate` datetime(6) DEFAULT NULL,
  `deliveryplant` bigint(20) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `gst_no` varchar(255) DEFAULT NULL,
  `ho_po_id` bigint(20) DEFAULT NULL,
  `item_group` varchar(255) DEFAULT NULL,
  `item_group_id` bigint(20) DEFAULT NULL,
  `partnercontribution` bigint(20) DEFAULT NULL,
  `po_date` datetime(6) DEFAULT NULL,
  `po_mode` varchar(255) DEFAULT NULL,
  `po_no_original` varchar(255) DEFAULT NULL,
  `po_notes` varchar(255) DEFAULT NULL,
  `po_no` varchar(255) DEFAULT NULL,
  `po_total_amount` double DEFAULT NULL,
  `po_transfered_by` varchar(255) DEFAULT NULL,
  `po_Transfered_on` datetime(6) DEFAULT NULL,
  `po_status` varchar(255) DEFAULT NULL,
  `proposal_id` bigint(20) DEFAULT NULL,
  `raw_ho_Po` varchar(255) DEFAULT NULL,
  `received_by` varchar(255) DEFAULT NULL,
  `received_oN` datetime(6) DEFAULT NULL,
  `receiver_designation` varchar(255) DEFAULT NULL,
  `reference_no` varchar(255) DEFAULT NULL,
  `rejected_on` datetime(6) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `roundOffType` varchar(255) DEFAULT NULL,
  `roundOff_amount` double DEFAULT NULL,
  `state_id` bigint(20) DEFAULT NULL,
  `state_name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `sub_broker_id` bigint(20) DEFAULT NULL,
  `sub_broker_name` varchar(255) DEFAULT NULL,
  `supplier_id` bigint(20) DEFAULT NULL,
  `supplier_name` varchar(255) DEFAULT NULL,
  `total_final_price` double DEFAULT NULL,
  `total_initial_price` double DEFAULT NULL,
  `type_flag` varchar(255) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `verified_on` datetime(6) DEFAULT NULL,
  `amendment_by` bigint(20) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `authorized_by` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) NOT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgootqms6660t60s1idi6mb96o` (`amendment_by`),
  KEY `FKpri22sba8sc3xoj527hw8501f` (`approved_by`),
  KEY `FKgraircu8sbrjvlt45npxr0nrw` (`authorized_by`),
  KEY `FKspd62muyawu5kuk9qaxsdk2u0` (`branch_id`),
  KEY `FKdafyu3djn8la2y1tjevniu04o` (`created_by`),
  KEY `FKtlk9s05tbn8tj2vcq03giltjc` (`dept_id`),
  KEY `FKt10sex4dbj5c2la456gecsxse` (`rejected_by`),
  KEY `FKjsl3jjr4lglxby3e0x6fojh29` (`updated_by`),
  KEY `FKm73pcb8x2pmilknnvf18ceg2j` (`vendor_id`),
  KEY `FKr72j9e430hnjivgptvxkkop44` (`verified_by`),
  CONSTRAINT `FKdafyu3djn8la2y1tjevniu04o` FOREIGN KEY (`created_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKgootqms6660t60s1idi6mb96o` FOREIGN KEY (`amendment_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKgraircu8sbrjvlt45npxr0nrw` FOREIGN KEY (`authorized_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKjsl3jjr4lglxby3e0x6fojh29` FOREIGN KEY (`updated_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKm73pcb8x2pmilknnvf18ceg2j` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`),
  CONSTRAINT `FKpri22sba8sc3xoj527hw8501f` FOREIGN KEY (`approved_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKr72j9e430hnjivgptvxkkop44` FOREIGN KEY (`verified_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKspd62muyawu5kuk9qaxsdk2u0` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKt10sex4dbj5c2la456gecsxse` FOREIGN KEY (`rejected_by`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKtlk9s05tbn8tj2vcq03giltjc` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `APPROVED_BY` varchar(255) DEFAULT NULL,
  `APPROVED_ON` datetime(6) DEFAULT NULL,
  `BOX_QTY` double DEFAULT NULL,
  `BROKERAGE_ID` bigint(20) DEFAULT NULL,
  `CAMP_ID` bigint(20) DEFAULT NULL,
  `CLAIM_RATE` double DEFAULT NULL,
  `CONV_FACTOR` double DEFAULT NULL,
  `CREATED_BY` varchar(255) DEFAULT NULL,
  `CREATED_ON` datetime(6) DEFAULT NULL,
  `DESTIN_UOM` varchar(255) DEFAULT NULL,
  `FFA` varchar(255) DEFAULT NULL,
  `FINAL_ORDER_PRICE` double DEFAULT NULL,
  `FINAL_ORDER_QTY` double DEFAULT NULL,
  `FINAL_PRICE` double DEFAULT NULL,
  `FINAL_QTY` double DEFAULT NULL,
  `INCLUDE_GST` varchar(255) DEFAULT NULL,
  `INITIAL_QTY` double DEFAULT NULL,
  `ITEM_GROUP_ID` bigint(20) DEFAULT NULL,
  `ITEM_ID` bigint(20) DEFAULT NULL,
  `ITEM_NAME` varchar(255) DEFAULT NULL,
  `ITEM_QTY` double DEFAULT NULL,
  `MOISTURE` varchar(255) DEFAULT NULL,
  `PO_NOTES` varchar(255) DEFAULT NULL,
  `PO_PURPOSE` varchar(255) DEFAULT NULL,
  `PO_STATUS` varchar(255) DEFAULT NULL,
  `PREMIUM_RATE` double DEFAULT NULL,
  `REFFERENCE_NO` varchar(255) DEFAULT NULL,
  `REMARKS` varchar(255) DEFAULT NULL,
  `TOTAL_AMOUNT` double DEFAULT NULL,
  `UOM` varchar(255) DEFAULT NULL,
  `UPDATED_BY` varchar(255) DEFAULT NULL,
  `UPDATED_ON` datetime(6) DEFAULT NULL,
  `YIELD` varchar(255) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `cgst_percentage` double DEFAULT NULL,
  `freight_charges` double DEFAULT NULL,
  `freight_charges_type` varchar(255) DEFAULT NULL,
  `igst_percentage` double DEFAULT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `net_approved_price` double DEFAULT NULL,
  `net_approved_qty` double DEFAULT NULL,
  `packing_forwarding_charges` double DEFAULT NULL,
  `po_validity_request` varchar(255) DEFAULT NULL,
  `po_inward_status` varchar(255) DEFAULT NULL,
  `purchase_Order_Id` bigint(20) DEFAULT NULL,
  `sgst_percentage` double DEFAULT NULL,
  `spec_id` bigint(20) DEFAULT NULL,
  `spec_name` varchar(255) DEFAULT NULL,
  `spec_type` bigint(20) DEFAULT NULL,
  `warranty` varchar(255) DEFAULT NULL,
  `warranty_Date` datetime(6) DEFAULT NULL,
  `machine_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `expense_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6i6isqq5bfcpegs31clcd4g9d` (`machine_id`),
  KEY `FK1a84vf5pubskan3ksnw33lqbf` (`product_id`),
  KEY `FK2cewcxriwv89vxh4smeqnwfnv` (`expense_id`),
  CONSTRAINT `FK1a84vf5pubskan3ksnw33lqbf` FOREIGN KEY (`product_id`) REFERENCES `products_master` (`id`),
  CONSTRAINT `FK2cewcxriwv89vxh4smeqnwfnv` FOREIGN KEY (`expense_id`) REFERENCES `purchase_amc_expense` (`amc_Id`),
  CONSTRAINT `FK6i6isqq5bfcpegs31clcd4g9d` FOREIGN KEY (`machine_id`) REFERENCES `machine` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `po_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `qty_ordered` decimal(12,3) NOT NULL,
  `qty_received` decimal(12,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `rate` decimal(12,4) DEFAULT NULL,
  `line_value` decimal(14,2) DEFAULT NULL,
  `tax_pct` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pol_po` (`po_id`),
  KEY `idx_pol_item` (`item_code`),
  CONSTRAINT `fk_pol_po` FOREIGN KEY (`po_id`) REFERENCES `purchase_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `payment_Category_Id` bigint(20) DEFAULT NULL,
  `payment_Category_Name` varchar(255) DEFAULT NULL,
  `payment_by` bigint(20) DEFAULT NULL,
  `payment_date` datetime(6) DEFAULT NULL,
  `payment_status` int(11) DEFAULT NULL,
  `percentage` double DEFAULT NULL,
  `purchase_Order_Id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKidog4lytng3yqju1qgu3rab3o` (`branch_id`),
  KEY `FKakxdymutaepkgbpokxou4l6hd` (`dept_id`),
  CONSTRAINT `FKakxdymutaepkgbpokxou4l6hd` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKidog4lytng3yqju1qgu3rab3o` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_quality_inspection_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_quality_inspection_score` (
  `purchase_quality_inspection_score_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `approve_by_id` bigint(20) DEFAULT NULL,
  `bill_amount` int(11) DEFAULT NULL,
  `bill_date` datetime(6) DEFAULT NULL,
  `bill_number` varchar(255) DEFAULT NULL,
  `bill_ref_id` bigint(20) DEFAULT NULL,
  `box_compression` varchar(255) DEFAULT NULL,
  `box_compression_path_value` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `created_by_id` bigint(20) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `delivery_delay` varchar(255) DEFAULT NULL,
  `delivery_score` double DEFAULT NULL,
  `department` int(11) DEFAULT NULL,
  `due_date` datetime(6) DEFAULT NULL,
  `email_id` varchar(255) DEFAULT NULL,
  `email_status_flag` varchar(255) DEFAULT NULL,
  `inward_by` bigint(20) DEFAULT NULL,
  `inward_date` datetime(6) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `item_type` bigint(20) DEFAULT NULL,
  `loyality_score` double DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `number_off_items` double DEFAULT NULL,
  `pending_qty` double DEFAULT NULL,
  `po_id` bigint(20) DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `po_qty` double DEFAULT NULL,
  `qc_score` double DEFAULT NULL,
  `supplied_qty` double DEFAULT NULL,
  `total_qc_score` double DEFAULT NULL,
  `total_score` double DEFAULT NULL,
  `trn_number` varchar(255) DEFAULT NULL,
  `vendor_ref_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`purchase_quality_inspection_score_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_quality_inspection_spec_description_detailspur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_quality_inspection_spec_description_detailspur` (
  `purchase_quality_inspection_score_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `bill_item_ref_id` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `item_ref_id` bigint(20) DEFAULT NULL,
  `parameter_score` int(11) DEFAULT NULL,
  `spec_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`purchase_quality_inspection_score_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gdc_no` varchar(255) DEFAULT NULL,
  `bill_Date` datetime(6) DEFAULT NULL,
  `bill_Inward_Date` datetime(6) DEFAULT NULL,
  `bill_No` varchar(255) DEFAULT NULL,
  `bill_Transaction_No` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `gdc_date` datetime(6) DEFAULT NULL,
  `lorry_Serial_No` varchar(255) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `rejected_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vehicle_mode` varchar(255) DEFAULT NULL,
  `vehicle_no` varchar(255) DEFAULT NULL,
  `vehicle_type` varchar(255) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfxulenxwd9fa4s16pu1ty0ugo` (`firm_id`),
  KEY `FKn25ex5iraqpsbvdug4wqipiur` (`vendor_id`),
  CONSTRAINT `FKfxulenxwd9fa4s16pu1ty0ugo` FOREIGN KEY (`firm_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKn25ex5iraqpsbvdug4wqipiur` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_return_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_return_products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_No` varchar(255) DEFAULT NULL,
  `bin` varchar(255) DEFAULT NULL,
  `current_Stock` varchar(255) DEFAULT NULL,
  `is_select` char(1) DEFAULT 'N',
  `lot` varchar(255) DEFAULT NULL,
  `product_Type` varchar(255) DEFAULT NULL,
  `purchase_Return_Id` bigint(20) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `return_Quantity` varchar(255) DEFAULT NULL,
  `product_master_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfkny61eyo5wwnhfsguiyogqfd` (`product_master_id`),
  CONSTRAINT `FKfkny61eyo5wwnhfsguiyogqfd` FOREIGN KEY (`product_master_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_return_stored_bin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_return_stored_bin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_No` varchar(255) DEFAULT NULL,
  `bin_No` bigint(20) DEFAULT NULL,
  `current_Stock` double DEFAULT NULL,
  `excess_Or_Shortage` double DEFAULT NULL,
  `item_Id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `return_Product_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qc_consumption_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qc_consumption_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conProduct` bigint(20) DEFAULT NULL,
  `configId` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `required_qty` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qc_consumption_setup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qc_consumption_setup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `createdDate` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `kpiId` bigint(20) DEFAULT NULL,
  `productId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qc_inprocess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qc_inprocess` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `atid` bigint(20) DEFAULT NULL,
  `batchBinNo` bigint(20) DEFAULT NULL,
  `batchProduct` bigint(20) DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `binid` varchar(255) DEFAULT NULL,
  `binname` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dcolor` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `dunit` varchar(255) DEFAULT NULL,
  `entry` date DEFAULT NULL,
  `journalflag` varchar(255) DEFAULT NULL,
  `kpi` bigint(20) DEFAULT NULL,
  `logid` bigint(20) DEFAULT NULL,
  `logtype` varchar(255) DEFAULT NULL,
  `mcauxid` bigint(20) DEFAULT NULL,
  `mcauxtable` varchar(255) DEFAULT NULL,
  `mcid` bigint(20) DEFAULT NULL,
  `mctable` varchar(255) DEFAULT NULL,
  `mmcid` bigint(20) DEFAULT NULL,
  `mmctable` varchar(255) DEFAULT NULL,
  `packingBatchNo` varchar(255) DEFAULT NULL,
  `proreportdbid` bigint(20) DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `refId` bigint(20) DEFAULT NULL,
  `resid` bigint(20) DEFAULT NULL,
  `resscore` int(11) DEFAULT NULL,
  `restype` varchar(255) DEFAULT NULL,
  `resval` varchar(255) DEFAULT NULL,
  `schid` bigint(20) DEFAULT NULL,
  `scoreto` varchar(255) DEFAULT NULL,
  `scoretoid` bigint(20) DEFAULT NULL,
  `shiftdate` datetime(6) DEFAULT NULL,
  `shiftid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tgid` bigint(20) DEFAULT NULL,
  `tno` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qtymain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qtymain` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qtyrefid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qtyrefid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdBy` bigint(20) DEFAULT NULL,
  `gggparent` bigint(20) DEFAULT NULL,
  `ggparent` bigint(20) DEFAULT NULL,
  `gparent` bigint(20) DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `pf` varchar(255) DEFAULT NULL,
  `refId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qtysjid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qtysjid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdBy` bigint(20) DEFAULT NULL,
  `ggparent` bigint(20) DEFAULT NULL,
  `gparent` bigint(20) DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qtysub1`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qtysub1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qtysub2`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qtysub2` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gparent` bigint(20) DEFAULT NULL,
  `lineId` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `trn` varchar(255) DEFAULT NULL,
  `trnType` varchar(255) DEFAULT NULL,
  `ttable` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `qualification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NOT NULL,
  `token` varchar(255) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r4k4edos30bx9neoq81mdvwph` (`token`),
  KEY `FK63isxnp3dr8thsecumpquvwvy` (`user_id`),
  CONSTRAINT `FK63isxnp3dr8thsecumpquvwvy` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1731 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `reptgrp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `reptgrp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cfr` double DEFAULT NULL,
  `display_order` bigint(20) DEFAULT NULL,
  `dlot` double DEFAULT NULL,
  `dunit` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `rid` bigint(20) DEFAULT NULL,
  `rsid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `reptname`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `reptname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activeFlag` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `request_Item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_Item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `select_Lot` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stock_Quantity` varchar(255) DEFAULT NULL,
  `stock_Required` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5bdhbhmb27df0lb8wx92em92i` (`branch_id`),
  KEY `FKst9xhw4owd5gjypyel8dyi9vs` (`dept_id`),
  CONSTRAINT `FK5bdhbhmb27df0lb8wx92em92i` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKst9xhw4owd5gjypyel8dyi9vs` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `request_items_godown`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_items_godown` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allocated_Quantity` varchar(255) DEFAULT NULL,
  `godown_external_id` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `material_return` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `select_Lot` varchar(255) DEFAULT NULL,
  `stock_Quantity` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnevmr5g6k3vue1yh6t8wjpoy6` (`branch_id`),
  KEY `FKleeg39bk072mjkv0psodeiod5` (`dept_id`),
  CONSTRAINT `FKleeg39bk072mjkv0psodeiod5` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKnevmr5g6k3vue1yh6t8wjpoy6` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `required_item_food`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `required_item_food` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `food_Item_Id` bigint(20) DEFAULT NULL,
  `measuring_type` varchar(255) DEFAULT NULL,
  `product_master_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfvuwbipi3759309x3x574ddus` (`product_master_id`),
  CONSTRAINT `FKfvuwbipi3759309x3x574ddus` FOREIGN KEY (`product_master_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `return_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_Id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `return_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `rmprolots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rmprolots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdby` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `rodent_monitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rodent_monitor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `boxNo` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `rodentDate` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkel70c25unjuvpalh4k93mjv6` (`godown_id`),
  CONSTRAINT `FKkel70c25unjuvpalh4k93mjv6` FOREIGN KEY (`godown_id`) REFERENCES `godown` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `landing_route` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1053 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `rsubhead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rsubhead` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `rid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salary_master_head`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary_master_head` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dw` varchar(255) DEFAULT NULL,
  `epf_eligible` varchar(255) DEFAULT NULL,
  `esi_eligible` varchar(255) DEFAULT NULL,
  `factory_structure_name` varchar(255) DEFAULT NULL,
  `is_accounting` varchar(255) DEFAULT NULL,
  `is_deduction` varchar(255) DEFAULT NULL,
  `is_factory` varchar(255) DEFAULT NULL,
  `max_amount` double DEFAULT NULL,
  `min_amount` double DEFAULT NULL,
  `mw` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `payroll_type` varchar(255) DEFAULT NULL,
  `percentage_of_salary` varchar(255) DEFAULT NULL,
  `sorting` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `structure_name` varchar(255) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `voucher_notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salary_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary_rules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contribution_by_employee` varchar(255) DEFAULT NULL,
  `contribution_by_employer` varchar(255) DEFAULT NULL,
  `head_id` varchar(255) DEFAULT NULL,
  `maximum_amount` varchar(255) DEFAULT NULL,
  `minimum_amount` varchar(255) DEFAULT NULL,
  `percentage` double DEFAULT NULL,
  `sal_head_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salary_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary_structure` (
  `aid` bigint(20) NOT NULL AUTO_INCREMENT,
  `allowance` double DEFAULT NULL,
  `applyto` varchar(255) DEFAULT NULL,
  `bper` double DEFAULT NULL,
  `convper` double DEFAULT NULL,
  `da` double DEFAULT NULL,
  `dacategory` bigint(20) DEFAULT NULL,
  `depfper` double DEFAULT NULL,
  `desiper` double DEFAULT NULL,
  `eepf1per` double DEFAULT NULL,
  `eepf2per` double DEFAULT NULL,
  `eesiper` double DEFAULT NULL,
  `epfbase` double DEFAULT NULL,
  `esiceil` double DEFAULT NULL,
  `fixed` varchar(255) DEFAULT NULL,
  `gwper` double DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `maxconv` double DEFAULT NULL,
  `minbasic` double DEFAULT NULL,
  `minconv` double DEFAULT NULL,
  `minhra` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trnyear` bigint(20) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `wtype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`aid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salary_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary_transaction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `C10` double DEFAULT NULL,
  `C7` double DEFAULT NULL,
  `C8` double DEFAULT NULL,
  `C9` double DEFAULT NULL,
  `CL_atCredit` double DEFAULT NULL,
  `CL_availed` double DEFAULT NULL,
  `EL_atCredit` double DEFAULT NULL,
  `EL_availed` double DEFAULT NULL,
  `M_atCredit` double DEFAULT NULL,
  `M_availed` double DEFAULT NULL,
  `SL_atCredit` double DEFAULT NULL,
  `SL_availed` double DEFAULT NULL,
  `aby` bigint(20) DEFAULT NULL,
  `add_sal` double DEFAULT NULL,
  `advance` double DEFAULT NULL,
  `allowance` double DEFAULT NULL,
  `approved_on` datetime(6) DEFAULT NULL,
  `ast` varchar(255) DEFAULT NULL,
  `auby` bigint(20) DEFAULT NULL,
  `aust` varchar(255) DEFAULT NULL,
  `authorized_on` datetime(6) DEFAULT NULL,
  `balance` double DEFAULT NULL,
  `bank` varchar(255) DEFAULT NULL,
  `bankacno` varchar(255) DEFAULT NULL,
  `basic` double DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `conid` bigint(20) DEFAULT NULL,
  `contable` varchar(255) DEFAULT NULL,
  `conveyance` double DEFAULT NULL,
  `cst` varchar(255) DEFAULT NULL,
  `da` double DEFAULT NULL,
  `dacatid` bigint(20) DEFAULT NULL,
  `depf` double DEFAULT NULL,
  `desi` double DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `diffdepf` double DEFAULT NULL,
  `diffdesi` double DEFAULT NULL,
  `diffeepf1` double DEFAULT NULL,
  `diffeepf2` double DEFAULT NULL,
  `diffeepftotal` double DEFAULT NULL,
  `diffeesi` double DEFAULT NULL,
  `eepf1` double DEFAULT NULL,
  `eepf2` double DEFAULT NULL,
  `eepftotal` double DEFAULT NULL,
  `eesi` double DEFAULT NULL,
  `epfno` varchar(255) DEFAULT NULL,
  `esino` varchar(255) DEFAULT NULL,
  `fmperiod` date DEFAULT NULL,
  `gwa` double DEFAULT NULL,
  `hra` double DEFAULT NULL,
  `jdate` date DEFAULT NULL,
  `leaveHolidayBasic` double DEFAULT NULL,
  `leaveHolidayDA` double DEFAULT NULL,
  `mess_bill` double DEFAULT NULL,
  `mnodays` varchar(255) DEFAULT NULL,
  `netsalary` double DEFAULT NULL,
  `nowday1` double DEFAULT NULL,
  `nowdays` double DEFAULT NULL,
  `odepf` double DEFAULT NULL,
  `odesi` double DEFAULT NULL,
  `oeepf1` double DEFAULT NULL,
  `oeepf2` double DEFAULT NULL,
  `oeepftotal` double DEFAULT NULL,
  `oeesi` double DEFAULT NULL,
  `otherDeduction` double DEFAULT NULL,
  `ptAmount` double DEFAULT NULL,
  `refNoWithDate` varchar(255) DEFAULT NULL,
  `salarytype` varchar(255) DEFAULT NULL,
  `scode` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tdeduction` double DEFAULT NULL,
  `tds` double DEFAULT NULL,
  `toperiod` date DEFAULT NULL,
  `total` double DEFAULT NULL,
  `trndate` date DEFAULT NULL,
  `trnid` bigint(20) DEFAULT NULL,
  `trnmonth` int(11) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trnweekid` bigint(20) DEFAULT NULL,
  `trnyear` varchar(255) DEFAULT NULL,
  `tsalary` double DEFAULT NULL,
  `uniformReimbursement` double DEFAULT NULL,
  `uniformReimbursment` double DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `verified_on` datetime(6) DEFAULT NULL,
  `vst` varchar(255) DEFAULT NULL,
  `vtype` varchar(255) DEFAULT NULL,
  `wa` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salary_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salary_voucher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adate` date DEFAULT NULL,
  `aendt` varchar(255) DEFAULT NULL,
  `amt` double DEFAULT NULL,
  `apdate` date DEFAULT NULL,
  `apendt` varchar(255) DEFAULT NULL,
  `approval` varchar(255) DEFAULT NULL,
  `apstaff` bigint(20) DEFAULT NULL,
  `astaff` bigint(20) DEFAULT NULL,
  `authenty` bigint(20) DEFAULT NULL,
  `bendt` varchar(255) DEFAULT NULL,
  `bentdt` datetime(6) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cform` varchar(255) DEFAULT NULL,
  `cformdt` date DEFAULT NULL,
  `cfstaff` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `department` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `descx` varchar(255) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `entry` datetime(6) DEFAULT NULL,
  `f17dt` date DEFAULT NULL,
  `f17staff` bigint(20) DEFAULT NULL,
  `fmperiod` date DEFAULT NULL,
  `form17` varchar(255) DEFAULT NULL,
  `formR` int(11) DEFAULT NULL,
  `formT` int(11) DEFAULT NULL,
  `formid` bigint(20) DEFAULT NULL,
  `hdate` date DEFAULT NULL,
  `hoprocess` varchar(255) DEFAULT NULL,
  `hstaff` bigint(20) DEFAULT NULL,
  `idate` date DEFAULT NULL,
  `iendt` varchar(255) DEFAULT NULL,
  `incid` bigint(20) DEFAULT NULL,
  `inv` varchar(255) DEFAULT NULL,
  `istaff` bigint(20) DEFAULT NULL,
  `items` bigint(20) DEFAULT NULL,
  `itemtype` bigint(20) DEFAULT NULL,
  `payment` varchar(255) DEFAULT NULL,
  `pdate` date DEFAULT NULL,
  `pstaff` bigint(20) DEFAULT NULL,
  `toperiod` date DEFAULT NULL,
  `trnmonth` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trntype` varchar(255) DEFAULT NULL,
  `trnweekid` bigint(20) DEFAULT NULL,
  `trnyear` bigint(20) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `vdate` date DEFAULT NULL,
  `vendt` varchar(255) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `vgrp` varchar(255) DEFAULT NULL,
  `voudt` date DEFAULT NULL,
  `vouno` varchar(255) DEFAULT NULL,
  `voushift` bigint(20) DEFAULT NULL,
  `vstaff` bigint(20) DEFAULT NULL,
  `vtype` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `so_number` varchar(32) NOT NULL,
  `customer_name` varchar(256) NOT NULL,
  `customer_gstin` varchar(16) DEFAULT NULL,
  `total_value` decimal(14,2) DEFAULT NULL,
  `currency` varchar(4) NOT NULL DEFAULT 'INR',
  `required_by_date` date DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `current_step` varchar(64) DEFAULT NULL,
  `workflow_instance_id` bigint(20) DEFAULT NULL,
  `gate_pass_id` bigint(20) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_so_number` (`tenant_id`,`so_number`),
  KEY `idx_so_status` (`tenant_id`,`status`,`current_step`),
  KEY `idx_so_gp` (`gate_pass_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_order_line` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `so_id` bigint(20) NOT NULL,
  `line_no` int(11) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(256) DEFAULT NULL,
  `qty_ordered` decimal(12,3) NOT NULL,
  `qty_dispatched` decimal(12,3) DEFAULT NULL,
  `uom` varchar(16) NOT NULL,
  `rate` decimal(12,4) DEFAULT NULL,
  `line_value` decimal(14,2) DEFAULT NULL,
  `tax_pct` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_sol_so` (`so_id`),
  KEY `idx_sol_item` (`item_code`),
  CONSTRAINT `fk_sol_so` FOREIGN KEY (`so_id`) REFERENCES `sales_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `assignedQuantity` bigint(20) DEFAULT NULL,
  `batch` varchar(255) DEFAULT NULL,
  `bin` varchar(255) DEFAULT NULL,
  `comparePercentage` varchar(255) DEFAULT NULL,
  `createdBy` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `newPrice` bigint(20) DEFAULT NULL,
  `oldPrice` bigint(20) DEFAULT NULL,
  `productId` bigint(20) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4twg7123h7m1su3i39h2p73pw` (`vendor_id`),
  CONSTRAINT `FK4twg7123h7m1su3i39h2p73pw` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_return` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `bill_date` date DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `con` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `refno` varchar(255) DEFAULT NULL,
  `rejected_by` bigint(20) DEFAULT NULL,
  `rejected_date` datetime(6) DEFAULT NULL,
  `return_reason` varchar(255) DEFAULT NULL,
  `return_type` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trndate` date DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `updatedBy` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_date` datetime(6) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  `von` varchar(255) DEFAULT NULL,
  `firm_id` bigint(20) DEFAULT NULL,
  `vendor_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5sicbgxud3olmb84ho08yv1c6` (`firm_id`),
  KEY `FK8tss30w13ogxojor96hoi59fk` (`vendor_id`),
  CONSTRAINT `FK5sicbgxud3olmb84ho08yv1c6` FOREIGN KEY (`firm_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK8tss30w13ogxojor96hoi59fk` FOREIGN KEY (`vendor_id`) REFERENCES `ledger` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_return_bin_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_return_bin_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accounting_type` varchar(255) DEFAULT NULL,
  `allocated_qty` varchar(255) DEFAULT NULL,
  `bin_no` bigint(20) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `return_product_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_return_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_return_products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batch_Id` bigint(20) DEFAULT NULL,
  `batch_No` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `product_code` varchar(255) DEFAULT NULL,
  `product_type` varchar(255) DEFAULT NULL,
  `return_id` bigint(20) DEFAULT NULL,
  `return_quantity` double DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkshio0empdc560ve8ol2olwlc` (`product_id`),
  CONSTRAINT `FKkshio0empdc560ve8ol2olwlc` FOREIGN KEY (`product_id`) REFERENCES `products_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesceilingstocklimit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesceilingstocklimit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchid` bigint(20) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `ceilinglimit` bigint(20) DEFAULT NULL,
  `sdate` date DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `productgroup` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKssd06qtidbmd4qxtg04l17iey` (`updatedby`),
  CONSTRAINT `FKssd06qtidbmd4qxtg04l17iey` FOREIGN KEY (`updatedby`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `saleslocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `saleslocation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ceilinglimit` varchar(255) DEFAULT NULL,
  `codeword` varchar(255) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesprices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesprices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entereddate` datetime(6) DEFAULT NULL,
  `fromdate` datetime(6) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `per_ml_price` double DEFAULT NULL,
  `price` double DEFAULT NULL,
  `saleslocationid` bigint(20) DEFAULT NULL,
  `salesproductgroupid` bigint(20) DEFAULT NULL,
  `salesproductid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductcapacity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductcapacity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductfilm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductfilm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` bigint(20) DEFAULT NULL,
  `created_date` date DEFAULT NULL,
  `filmweightinkg` double DEFAULT NULL,
  `productnameid` bigint(20) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `productgroupid` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `qty` varchar(255) DEFAULT NULL,
  `branchid` bigint(20) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKojc47y8y0jnqi8s18mor1fax1` (`branchid`),
  KEY `FKgefugxpdvtsvo0e5w9kfeq72y` (`updatedby`),
  CONSTRAINT `FKgefugxpdvtsvo0e5w9kfeq72y` FOREIGN KEY (`updatedby`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKojc47y8y0jnqi8s18mor1fax1` FOREIGN KEY (`branchid`) REFERENCES `salesproductionbranches` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductfilmorder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductfilmorder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `balance` double DEFAULT NULL,
  `bestbefore` date DEFAULT NULL,
  `cancelledby` bigint(20) DEFAULT NULL,
  `completeddate` datetime(6) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `locationid` varchar(255) DEFAULT NULL,
  `lot` bigint(20) DEFAULT NULL,
  `matterstoprint` varchar(255) DEFAULT NULL,
  `material_request_id` bigint(20) DEFAULT NULL,
  `mrnstatus` varchar(255) DEFAULT NULL,
  `mrp` double DEFAULT NULL,
  `orderbranch` bigint(20) DEFAULT NULL,
  `orderdate` datetime(6) DEFAULT NULL,
  `orderqty` double DEFAULT NULL,
  `orderqtykg` double DEFAULT NULL,
  `orderedby` bigint(20) DEFAULT NULL,
  `packedon` varchar(255) DEFAULT NULL,
  `per_ml_price` double DEFAULT NULL,
  `printedby` varchar(255) DEFAULT NULL,
  `printedqty` double DEFAULT NULL,
  `specialinstructions` varchar(255) DEFAULT NULL,
  `stockqty` bigint(20) DEFAULT NULL,
  `sublocationid` bigint(20) DEFAULT NULL,
  `filmid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6qq01la9yb3x7yyus7jymogg4` (`filmid`),
  CONSTRAINT `FK6qq01la9yb3x7yyus7jymogg4` FOREIGN KEY (`filmid`) REFERENCES `salesproductfilm` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductgroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `codeword` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductionbranches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductionbranches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branchid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductlabelorder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductlabelorder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `balance` double DEFAULT NULL,
  `bestbefore` date DEFAULT NULL,
  `cancelledby` bigint(20) DEFAULT NULL,
  `completeddate` datetime(6) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `locationid` varchar(255) DEFAULT NULL,
  `lot` bigint(20) DEFAULT NULL,
  `matterstoprint` varchar(255) DEFAULT NULL,
  `material_request_id` bigint(20) DEFAULT NULL,
  `mrnstatus` varchar(255) DEFAULT NULL,
  `mrp` double DEFAULT NULL,
  `orderbranch` bigint(20) DEFAULT NULL,
  `orderdate` datetime(6) DEFAULT NULL,
  `orderqty` double DEFAULT NULL,
  `orderedby` bigint(20) DEFAULT NULL,
  `packedon` varchar(255) DEFAULT NULL,
  `per_ml_price` double DEFAULT NULL,
  `printedby` varchar(255) DEFAULT NULL,
  `printedqty` double DEFAULT NULL,
  `specialinstructions` varchar(255) DEFAULT NULL,
  `stockqty` bigint(20) DEFAULT NULL,
  `sublocationid` bigint(20) DEFAULT NULL,
  `labelid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKd284rttpsop3bvrm7qryboy74` (`labelid`),
  CONSTRAINT `FKd284rttpsop3bvrm7qryboy74` FOREIGN KEY (`labelid`) REFERENCES `labelproduct` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesproductname`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesproductname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` bigint(20) DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `productname` varchar(255) DEFAULT NULL,
  `salesproductgroup` bigint(20) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesvra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesvra` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amt` double DEFAULT NULL,
  `approval` varchar(255) DEFAULT NULL,
  `apdate` datetime(6) DEFAULT NULL,
  `apstaff` bigint(20) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `adate` datetime(6) DEFAULT NULL,
  `astaff` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `billdt` date DEFAULT NULL,
  `billitemsid` varchar(255) DEFAULT NULL,
  `billno` varchar(255) DEFAULT NULL,
  `bstaff` bigint(20) DEFAULT NULL,
  `bin` varchar(255) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `istaff` bigint(20) DEFAULT NULL,
  `idate` datetime(6) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount` double DEFAULT NULL,
  `ebill_receive` varchar(255) DEFAULT NULL,
  `ebill_send` varchar(255) DEFAULT NULL,
  `entry` datetime(6) DEFAULT NULL,
  `handlingCharges` double DEFAULT NULL,
  `hoprocess` varchar(255) DEFAULT NULL,
  `mess_Voucher` bigint(20) DEFAULT NULL,
  `mess_vou_date` date DEFAULT NULL,
  `bill_month` bigint(20) DEFAULT NULL,
  `party` varchar(255) DEFAULT NULL,
  `payment_type` varchar(255) DEFAULT NULL,
  `place` varchar(255) DEFAULT NULL,
  `printed_flag` bigint(20) DEFAULT NULL,
  `refnumber` varchar(255) DEFAULT NULL,
  `roundoff` double DEFAULT NULL,
  `scheme_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tcs_amount` double DEFAULT NULL,
  `tcs_percentage` double DEFAULT NULL,
  `totable` varchar(255) DEFAULT NULL,
  `transportMode` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `vehicleNo` varchar(255) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `vdate` datetime(6) DEFAULT NULL,
  `vstaff` bigint(20) DEFAULT NULL,
  `bill_year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `salesvraitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesvraitems` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchno` varchar(255) DEFAULT NULL,
  `bdt` date DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `billid` bigint(20) DEFAULT NULL,
  `bno` varchar(255) DEFAULT NULL,
  `cgst` double DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `discountamt` double DEFAULT NULL,
  `enteredby` bigint(20) DEFAULT NULL,
  `enteredon` datetime(6) DEFAULT NULL,
  `igst` double DEFAULT NULL,
  `ltotal` double DEFAULT NULL,
  `ltotal_withoutgst` double DEFAULT NULL,
  `qty` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `reftype` varchar(255) DEFAULT NULL,
  `sgst` double DEFAULT NULL,
  `taxamt` double DEFAULT NULL,
  `taxper` double DEFAULT NULL,
  `tcs` double DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `urate` double DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `schedule_name_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_name_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `schedule_training`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `schedule_training` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `last_Registration_Date` datetime(6) DEFAULT NULL,
  `remaining_Seats` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_Registered_Employees` int(11) DEFAULT NULL,
  `total_Seats` int(11) DEFAULT NULL,
  `training_Date` datetime(6) DEFAULT NULL,
  `training_Topic` varchar(255) DEFAULT NULL,
  `training_Type` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `designation_id` bigint(20) DEFAULT NULL,
  `skill_id` bigint(20) DEFAULT NULL,
  `training_branch_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtkqxcn90cu296hoin4eay65db` (`department_id`),
  KEY `FKpiyiiyc5s2nly3dn2uft3yhfu` (`designation_id`),
  KEY `FK7j8xctngghm3tqa07kp4pm3en` (`skill_id`),
  KEY `FKcf95w2y5s2d4910bvj3maavr` (`training_branch_id`),
  CONSTRAINT `FK7j8xctngghm3tqa07kp4pm3en` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `FKcf95w2y5s2d4910bvj3maavr` FOREIGN KEY (`training_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKpiyiiyc5s2nly3dn2uft3yhfu` FOREIGN KEY (`designation_id`) REFERENCES `designation` (`id`),
  CONSTRAINT `FKtkqxcn90cu296hoin4eay65db` FOREIGN KEY (`department_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `score_assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `score_assessment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `batchno` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `food_saftey_score` int(11) DEFAULT NULL,
  `industrial_score` int(11) DEFAULT NULL,
  `interview_communication_skill` varchar(255) DEFAULT NULL,
  `interview_score` int(11) DEFAULT NULL,
  `interview_technical_skill` varchar(255) DEFAULT NULL,
  `overall_score` int(11) DEFAULT NULL,
  `phone_Number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn9u85jncwqopktm67ut5qg82s` (`interview_id`),
  KEY `FKcu68ufcplc8qc4cf7c49c82w9` (`user_id`),
  CONSTRAINT `FKcu68ufcplc8qc4cf7c49c82w9` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKn9u85jncwqopktm67ut5qg82s` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `scrap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `scrap` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `from_Process` varchar(255) DEFAULT NULL,
  `scrap_Item` varchar(255) DEFAULT NULL,
  `scrap_Type` varchar(255) DEFAULT NULL,
  `select_Storage_Bin` varchar(255) DEFAULT NULL,
  `stored_On` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5j1uc6c0mu0n89js3swyapgoo` (`dept_id`),
  CONSTRAINT `FK5j1uc6c0mu0n89js3swyapgoo` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_allotment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_allotment` (
  `id` varchar(255) NOT NULL,
  `branch_Id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `dept_Id` bigint(20) DEFAULT NULL,
  `emp_id` bigint(20) DEFAULT NULL,
  `emp_type` int(11) DEFAULT NULL,
  `org_Id` bigint(20) DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  `staffemployee` bit(1) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_allotment_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_allotment_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shift_id` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgdefkpa3u7j1aeh165w8mu7af` (`shift_id`),
  KEY `FK3docoxtxphb8jkj6x7toqya0t` (`user_id`),
  CONSTRAINT `FK3docoxtxphb8jkj6x7toqya0t` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKgdefkpa3u7j1aeh165w8mu7af` FOREIGN KEY (`shift_id`) REFERENCES `shift_allotment` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_assign`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_assign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `designation_id` bigint(20) DEFAULT NULL,
  `emp_id` bigint(20) DEFAULT NULL,
  `emp_type` int(11) DEFAULT NULL,
  `from_date` date DEFAULT NULL,
  `rotation_id` bigint(20) DEFAULT NULL,
  `shift_date` date DEFAULT NULL,
  `shift_value` varchar(255) DEFAULT NULL,
  `to_date` date DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `week_day_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKal49llxfbpdcr7njns13p857g` (`org_id`),
  KEY `FK1fm0e0amkgui7obtf8edhvcky` (`shift_id`),
  CONSTRAINT `FK1fm0e0amkgui7obtf8edhvcky` FOREIGN KEY (`shift_id`) REFERENCES `shifts` (`id`),
  CONSTRAINT `FKal49llxfbpdcr7njns13p857g` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shift_name` varchar(255) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKauw2wknnjx5elysws5lmyk781` (`org_id`),
  CONSTRAINT `FKauw2wknnjx5elysws5lmyk781` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_override`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_override` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdby` bigint(20) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `emp_id` bigint(20) DEFAULT NULL,
  `shift_date` datetime(6) DEFAULT NULL,
  `shift_id` bigint(20) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_rotation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_rotation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `from_Date` datetime(6) DEFAULT NULL,
  `no_Of_Employees` varchar(255) DEFAULT NULL,
  `shift1_id` bigint(20) DEFAULT NULL,
  `shift2_id` bigint(20) DEFAULT NULL,
  `shift3_id` bigint(20) DEFAULT NULL,
  `shift_name` varchar(255) DEFAULT NULL,
  `to_Date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr9cw9st631o9j511d4irevf5o` (`branch_id`),
  KEY `FKna5814exdftrgff3ekx8mmli4` (`dept_id`),
  KEY `FK687wyg87mg8092g1badcu3mqo` (`org_id`),
  CONSTRAINT `FK687wyg87mg8092g1badcu3mqo` FOREIGN KEY (`org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKna5814exdftrgff3ekx8mmli4` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKr9cw9st631o9j511d4irevf5o` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shift_rotation_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_rotation_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `current_Week_Off` varchar(255) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `emp_Id` bigint(20) DEFAULT NULL,
  `emp_type` int(11) DEFAULT NULL,
  `employee_Name` varchar(255) DEFAULT NULL,
  `shift` varchar(255) DEFAULT NULL,
  `shift_Rotation_Id` bigint(20) DEFAULT NULL,
  `week_Off` bigint(20) DEFAULT NULL,
  `week_Off_Name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shifts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `shifts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `delay_Duration` varchar(255) DEFAULT NULL,
  `delaytime` varchar(255) DEFAULT NULL,
  `fri` varchar(255) DEFAULT NULL,
  `hours` varchar(255) DEFAULT NULL,
  `intime` varchar(255) DEFAULT NULL,
  `lunchtime` varchar(255) DEFAULT NULL,
  `mon` varchar(255) DEFAULT NULL,
  `outtime` varchar(255) DEFAULT NULL,
  `pshift_id` bigint(20) DEFAULT NULL,
  `sat` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `shiftnumber` int(11) DEFAULT NULL,
  `sun` varchar(255) DEFAULT NULL,
  `thu` varchar(255) DEFAULT NULL,
  `tue` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `wed` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8b52lcodtho7ycx5pl44ksvgv` (`branch_id`),
  CONSTRAINT `FK8b52lcodtho7ycx5pl44ksvgv` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sjtqty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sjtqty` (
  `rid` bigint(20) NOT NULL AUTO_INCREMENT,
  `conversion` double DEFAULT NULL,
  `conversion_old` double DEFAULT NULL,
  `ina` bigint(20) DEFAULT NULL,
  `inb` bigint(20) DEFAULT NULL,
  `inc` bigint(20) DEFAULT NULL,
  `ind` bigint(20) DEFAULT NULL,
  `ine` bigint(20) DEFAULT NULL,
  `inf` bigint(20) DEFAULT NULL,
  `ing` bigint(20) DEFAULT NULL,
  `inh` bigint(20) DEFAULT NULL,
  `ini` bigint(20) DEFAULT NULL,
  `inj` bigint(20) DEFAULT NULL,
  `ink` bigint(20) DEFAULT NULL,
  `outa` bigint(20) DEFAULT NULL,
  `outb` bigint(20) DEFAULT NULL,
  `outc` bigint(20) DEFAULT NULL,
  `outd` bigint(20) DEFAULT NULL,
  `oute` bigint(20) DEFAULT NULL,
  `outf` bigint(20) DEFAULT NULL,
  `outg` bigint(20) DEFAULT NULL,
  `outh` bigint(20) DEFAULT NULL,
  `outi` bigint(20) DEFAULT NULL,
  `outj` bigint(20) DEFAULT NULL,
  `outk` bigint(20) DEFAULT NULL,
  `outl` bigint(20) DEFAULT NULL,
  `outm` bigint(20) DEFAULT NULL,
  `outn` bigint(20) DEFAULT NULL,
  `outo` bigint(20) DEFAULT NULL,
  `outp` bigint(20) DEFAULT NULL,
  `outq` bigint(20) DEFAULT NULL,
  `outr` bigint(20) DEFAULT NULL,
  `outs` bigint(20) DEFAULT NULL,
  `outt` bigint(20) DEFAULT NULL,
  `outu` bigint(20) DEFAULT NULL,
  `outv` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `sjname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `skill_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `competency_id` bigint(20) DEFAULT NULL,
  `s_no` bigint(20) DEFAULT NULL,
  `skill_id` bigint(20) DEFAULT NULL,
  `skills` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `skill_grade_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill_grade_level` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `level_Number` int(11) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `skill_matrix`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill_matrix` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `department_id` bigint(20) NOT NULL,
  `designation_id` bigint(20) NOT NULL,
  `trainee_exam_score_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbka17w58mb0ypxft6muxohln9` (`department_id`),
  KEY `FK6bu4bbs0m7plj4f0o6ws9j7pa` (`designation_id`),
  KEY `FKpq606ki7mxhxp504awbtwxif4` (`trainee_exam_score_id`),
  KEY `FKdt0ejwyjayeb7t0989qr66vrr` (`user_id`),
  CONSTRAINT `FK6bu4bbs0m7plj4f0o6ws9j7pa` FOREIGN KEY (`designation_id`) REFERENCES `designation` (`id`),
  CONSTRAINT `FKbka17w58mb0ypxft6muxohln9` FOREIGN KEY (`department_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKdt0ejwyjayeb7t0989qr66vrr` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKpq606ki7mxhxp504awbtwxif4` FOREIGN KEY (`trainee_exam_score_id`) REFERENCES `trainee_exam_score` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `skill_matrix_grade_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `skill_matrix_grade_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `skill_Matrix_Id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `skill_grade_level_id` bigint(20) NOT NULL,
  `skill_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKff1je43mssqeiyef3im3q0mdc` (`skill_grade_level_id`),
  KEY `FKj5lu93jqiw8i15a9014imrnt4` (`skill_id`),
  CONSTRAINT `FKff1je43mssqeiyef3im3q0mdc` FOREIGN KEY (`skill_grade_level_id`) REFERENCES `skill_grade_level` (`id`),
  CONSTRAINT `FKj5lu93jqiw8i15a9014imrnt4` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `skills` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `specification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `specification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `department` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `organisation` bigint(20) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `specification_type` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `specification_parameter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `specification_parameter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `department` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `organisation` bigint(20) DEFAULT NULL,
  `specification_type` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `specification_parameter_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `specification_parameter_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `parameter_option_type` bigint(20) DEFAULT NULL,
  `parameter_type` bigint(20) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `parameter_id` bigint(20) DEFAULT NULL,
  `specification_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn7nbj6hymmq3ldhpwni933uy4` (`parameter_id`),
  KEY `FKabto7ox79lsfkahyw8qvip4mk` (`specification_id`),
  CONSTRAINT `FKabto7ox79lsfkahyw8qvip4mk` FOREIGN KEY (`specification_id`) REFERENCES `specification` (`id`),
  CONSTRAINT `FKn7nbj6hymmq3ldhpwni933uy4` FOREIGN KEY (`parameter_id`) REFERENCES `specification_parameter` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `split_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `split_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lot` double DEFAULT NULL,
  `pcode` bigint(20) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `slot` double DEFAULT NULL,
  `spcode` bigint(20) DEFAULT NULL,
  `splitid` bigint(20) DEFAULT NULL,
  `sqty` double DEFAULT NULL,
  `sqty_old` double DEFAULT NULL,
  `stype` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `split_product_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `split_product_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` bigint(20) DEFAULT NULL,
  `balance_quantity` double DEFAULT NULL,
  `batch_id` bigint(20) DEFAULT NULL,
  `batch_no` varchar(255) DEFAULT NULL,
  `binno` bigint(20) DEFAULT NULL,
  `binstock_id` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `bsk_batch_id` bigint(20) DEFAULT NULL,
  `bsk_batch_no` varchar(255) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `department` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `godown_id` bigint(20) DEFAULT NULL,
  `hub` bigint(20) DEFAULT NULL,
  `inventory_date` date DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `order_quantity` double DEFAULT NULL,
  `packing_factor` varchar(255) DEFAULT NULL,
  `palletNumber` varchar(255) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `production_date` date DEFAULT NULL,
  `quantity` double DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `selected_qty` double DEFAULT NULL,
  `split_trn_id` bigint(20) DEFAULT NULL,
  `split_type` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `split_trn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `split_trn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) DEFAULT NULL,
  `binname` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `con` varchar(255) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `splitid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trndate` datetime(6) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `splitname`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `splitname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `state` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `countryid` bigint(20) DEFAULT NULL,
  `gst_state_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stbin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `stbin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `accounting_date` date DEFAULT NULL,
  `adate` datetime(6) DEFAULT NULL,
  `aref` bigint(20) DEFAULT NULL,
  `authenty` bigint(20) DEFAULT NULL,
  `avgcost` double DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bdt` datetime(6) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `billid` bigint(20) DEFAULT NULL,
  `bintype` varchar(255) DEFAULT NULL,
  `bno` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `conid` bigint(20) DEFAULT NULL,
  `contype` varchar(255) DEFAULT NULL,
  `conzone` varchar(255) DEFAULT NULL,
  `conzoneid` bigint(20) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `ddate` datetime(6) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `dktno` varchar(255) DEFAULT NULL,
  `entry_type` varchar(255) DEFAULT NULL,
  `fgcode` varchar(255) DEFAULT NULL,
  `fgname` varchar(255) DEFAULT NULL,
  `fmid` bigint(20) DEFAULT NULL,
  `fmtable` varchar(255) DEFAULT NULL,
  `fre` double DEFAULT NULL,
  `gcode` varchar(255) DEFAULT NULL,
  `gid` bigint(20) DEFAULT NULL,
  `gname` bigint(20) DEFAULT NULL,
  `idate` datetime(6) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `ltotal` double DEFAULT NULL,
  `naration` varchar(255) DEFAULT NULL,
  `oth` double DEFAULT NULL,
  `poud` bigint(20) DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `reftype` varchar(255) DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `sflag` varchar(255) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `stkpno` varchar(255) DEFAULT NULL,
  `stkrno` varchar(255) DEFAULT NULL,
  `stkstatus` varchar(255) DEFAULT NULL,
  `toid` bigint(20) DEFAULT NULL,
  `totable` varchar(255) DEFAULT NULL,
  `trn_date` date DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trnstatus` varchar(255) DEFAULT NULL,
  `trntype` varchar(255) DEFAULT NULL,
  `ttable` varchar(255) DEFAULT NULL,
  `updatedon` datetime(6) DEFAULT NULL,
  `urate` double DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stkoillist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `stkoillist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conversion` double DEFAULT NULL,
  `conversion_old` double DEFAULT NULL,
  `createdby` double DEFAULT NULL,
  `cword` varchar(255) DEFAULT NULL,
  `displayOrder1` double DEFAULT NULL,
  `displayOrder2` double DEFAULT NULL,
  `group` varchar(255) DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `oil_Consumption` int(11) DEFAULT NULL,
  `parentid` bigint(20) DEFAULT NULL,
  `pconversion` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `subGroup1` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stktable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `stktable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cost` double DEFAULT NULL,
  `lot` double DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sub_modules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sub_modules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `module_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4krwlyse1v14r4ptgyat4dsxa` (`module_id`),
  CONSTRAINT `FK4krwlyse1v14r4ptgyat4dsxa` FOREIGN KEY (`module_id`) REFERENCES `modules` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supeval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `supeval` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `approval` varchar(255) DEFAULT NULL,
  `approval_staff` bigint(20) DEFAULT NULL,
  `approved_date` datetime(6) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `authorise_staff` bigint(20) DEFAULT NULL,
  `authorised_date` datetime(6) DEFAULT NULL,
  `blocklist` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `created` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `entry` datetime(6) DEFAULT NULL,
  `fromdate` date DEFAULT NULL,
  `mailsent` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `todate` date DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `verify_date` datetime(6) DEFAULT NULL,
  `verify_staff` bigint(20) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplierevaluationattributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplierevaluationattributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `colors` int(11) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `lastupdate` datetime(6) DEFAULT NULL,
  `maximum` double DEFAULT NULL,
  `scorefrom` double DEFAULT NULL,
  `scoreto` double DEFAULT NULL,
  `scoretype` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updatedby` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplierevaluationreport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplierevaluationreport` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `billid` varchar(255) DEFAULT NULL,
  `billno` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `deliveryduedate` datetime(6) DEFAULT NULL,
  `deliveryscore` int(11) DEFAULT NULL,
  `inwarddate` datetime(6) DEFAULT NULL,
  `ledgerid` bigint(20) DEFAULT NULL,
  `loyaltyscore` int(11) DEFAULT NULL,
  `period` varchar(255) DEFAULT NULL,
  `poid` bigint(20) DEFAULT NULL,
  `qualityscore` int(11) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `suppliergroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliergroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `task_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inp_ins_flag` varchar(255) DEFAULT NULL,
  `inw_ins_flag` varchar(255) DEFAULT NULL,
  `task_Category_Name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `task_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cluster` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `plant` bigint(20) DEFAULT NULL,
  `task_Group_Name` varchar(255) DEFAULT NULL,
  `taskgroup` bigint(20) DEFAULT NULL,
  `taskcategory` bigint(20) DEFAULT NULL,
  `task` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6tp82arpi7mj03xrplclrslhl` (`taskcategory`),
  KEY `FK8ibth2w44mxae3dy5ojityygh` (`task`),
  CONSTRAINT `FK6tp82arpi7mj03xrplclrslhl` FOREIGN KEY (`taskcategory`) REFERENCES `task_category` (`id`),
  CONSTRAINT `FK8ibth2w44mxae3dy5ojityygh` FOREIGN KEY (`task`) REFERENCES `task_name` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `task_group_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_group_master` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `task_group_name` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `task_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_name` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(255) DEFAULT NULL,
  `createdon` datetime(6) DEFAULT NULL,
  `final` varchar(255) DEFAULT NULL,
  `taskcategory` varchar(255) DEFAULT NULL,
  `task_id` bigint(20) DEFAULT NULL,
  `task_name` varchar(255) DEFAULT NULL,
  `task_category_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8pw6vibwdp81oewaaa8h4ob53` (`task_category_id`),
  CONSTRAINT `FK8pw6vibwdp81oewaaa8h4ob53` FOREIGN KEY (`task_category_id`) REFERENCES `task_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `display_name` varchar(256) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tenant_code` (`code`),
  KEY `idx_tenant_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tenant_ai_credential`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_ai_credential` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `provider` varchar(32) NOT NULL,
  `model` varchar(128) NOT NULL,
  `api_key_ciphertext` text NOT NULL,
  `endpoint` varchar(256) DEFAULT NULL,
  `max_tokens_per_day` int(11) NOT NULL DEFAULT 500000,
  `cost_cap_paise_per_day` int(11) NOT NULL DEFAULT 100000,
  `kill_switch` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tenant_ai_credential_tenant_provider` (`tenant_id`,`provider`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tenant_ai_query_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_ai_query_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `credential_id` bigint(20) NOT NULL,
  `agent_run_id` bigint(20) DEFAULT NULL,
  `operator_id` bigint(20) DEFAULT NULL,
  `called_at` timestamp NOT NULL,
  `kind` varchar(16) NOT NULL,
  `tokens_in` int(11) NOT NULL,
  `tokens_out` int(11) NOT NULL,
  `cost_paise` int(11) NOT NULL,
  `prompt_hash` char(64) DEFAULT NULL,
  `response_hash` char(64) DEFAULT NULL,
  `status` varchar(16) NOT NULL,
  `error` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tenant_ai_query_log_credential` (`credential_id`),
  KEY `idx_tenant_ai_query_log_tenant_called` (`tenant_id`,`called_at` DESC),
  KEY `idx_tenant_ai_query_log_tenant_credential_called` (`tenant_id`,`credential_id`,`called_at`),
  CONSTRAINT `fk_tenant_ai_query_log_credential` FOREIGN KEY (`credential_id`) REFERENCES `tenant_ai_credential` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tenant_form_layout`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_form_layout` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `entity_code` varchar(128) NOT NULL,
  `code` varchar(128) NOT NULL,
  `label` varchar(256) DEFAULT NULL,
  `device` varchar(64) DEFAULT NULL,
  `body_json` longtext NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_fl_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tenant_list_view`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_list_view` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `page_slug` varchar(128) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text DEFAULT NULL,
  `icon` varchar(64) DEFAULT NULL,
  `scope` enum('TENANT','ROLE','USER') NOT NULL,
  `scope_value` varchar(128) DEFAULT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT 0,
  `body_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`body_json`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_listview_tenant_page` (`tenant_id`,`page_slug`,`scope`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tenant_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenant_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `code` varchar(64) NOT NULL,
  `label` varchar(128) NOT NULL,
  `permissions_json` longtext DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_trole_tenant_code` (`tenant_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `test_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `score_id` bigint(20) DEFAULT NULL,
  `test_Score` bigint(20) DEFAULT NULL,
  `interviewer_id` bigint(20) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6aa3mrtclgf5vf1ggupk85nes` (`interviewer_id`),
  KEY `FK9eadbabq8cp99yckgin27pxxy` (`interview_id`),
  KEY `FKghym6oa9fqbxwfd8iqnt7v9ye` (`branch_id`),
  CONSTRAINT `FK6aa3mrtclgf5vf1ggupk85nes` FOREIGN KEY (`interviewer_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK9eadbabq8cp99yckgin27pxxy` FOREIGN KEY (`interview_id`) REFERENCES `interview` (`id`),
  CONSTRAINT `FKghym6oa9fqbxwfd8iqnt7v9ye` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `trainee_answer_multiple_choice_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainee_answer_multiple_choice_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `evaluation_Date` datetime(6) DEFAULT NULL,
  `submitted_Date` datetime(6) DEFAULT NULL,
  `test_Exam_Score_Id` bigint(20) DEFAULT NULL,
  `trainee_Answer` varchar(255) DEFAULT NULL,
  `trainee_Score` float DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `multiple_choice_question_id` bigint(20) NOT NULL,
  `trainee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt9ax6lera5h7k4oiej0cqo8xt` (`multiple_choice_question_id`),
  KEY `FK60kyryulg6yajaryf6wf28xmw` (`trainee_id`),
  CONSTRAINT `FK60kyryulg6yajaryf6wf28xmw` FOREIGN KEY (`trainee_id`) REFERENCES `training_user_registration_mapping` (`id`),
  CONSTRAINT `FKt9ax6lera5h7k4oiej0cqo8xt` FOREIGN KEY (`multiple_choice_question_id`) REFERENCES `multiple_choice_question` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `trainee_answer_open_ended_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainee_answer_open_ended_question` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `evaluation_Date` datetime(6) DEFAULT NULL,
  `submitted_Date` datetime(6) DEFAULT NULL,
  `test_Exam_Score_Id` bigint(20) DEFAULT NULL,
  `trainee_Answer` varchar(255) DEFAULT NULL,
  `trainee_Score` float DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `open_ended_question_id` bigint(20) NOT NULL,
  `trainee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKomt3sf02m9bj4qhfncn7qs2nt` (`open_ended_question_id`),
  KEY `FKsryc2x65bs3hx31pjqp0uc03f` (`trainee_id`),
  CONSTRAINT `FKomt3sf02m9bj4qhfncn7qs2nt` FOREIGN KEY (`open_ended_question_id`) REFERENCES `open_ended_question` (`id`),
  CONSTRAINT `FKsryc2x65bs3hx31pjqp0uc03f` FOREIGN KEY (`trainee_id`) REFERENCES `training_user_registration_mapping` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `trainee_exam_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainee_exam_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `answer_Sheet_File_Name` varchar(255) DEFAULT NULL,
  `answer_Sheet_File_Type` varchar(255) DEFAULT NULL,
  `answer_Sheet_Upload_Path` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `evaluation_Date` datetime(6) DEFAULT NULL,
  `evaluation_Status` varchar(255) DEFAULT NULL,
  `interaction_Score` float DEFAULT NULL,
  `is_Answer_Sheet_Attached` bit(1) DEFAULT NULL,
  `ontime_Score` float DEFAULT NULL,
  `schedule_Training_Id` bigint(20) DEFAULT NULL,
  `test_Score` float DEFAULT NULL,
  `total_Score` float DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `trainer_detail_id` bigint(20) DEFAULT NULL,
  `trainee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjffo63yxvm47sduip3n3u0tu4` (`trainer_detail_id`),
  KEY `FKeotunppstv67s2vfpfkv0prw7` (`trainee_id`),
  CONSTRAINT `FKeotunppstv67s2vfpfkv0prw7` FOREIGN KEY (`trainee_id`) REFERENCES `training_user_registration_mapping` (`id`),
  CONSTRAINT `FKjffo63yxvm47sduip3n3u0tu4` FOREIGN KEY (`trainer_detail_id`) REFERENCES `trainer_detail` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `trainer_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `average_Score` float DEFAULT NULL,
  `external_Employee_Name` varchar(255) DEFAULT NULL,
  `external_Organisation_Name` varchar(255) DEFAULT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `schedule_training_id` bigint(20) NOT NULL,
  `trainer_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf1833vt69f42toma8h0fihqcl` (`department_id`),
  KEY `FK91lu976ru69ya9lc6l4p3jc6p` (`schedule_training_id`),
  KEY `FK8steggqxgd7gl61q6cmx9tjrv` (`trainer_id`),
  CONSTRAINT `FK8steggqxgd7gl61q6cmx9tjrv` FOREIGN KEY (`trainer_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK91lu976ru69ya9lc6l4p3jc6p` FOREIGN KEY (`schedule_training_id`) REFERENCES `schedule_training` (`id`),
  CONSTRAINT `FKf1833vt69f42toma8h0fihqcl` FOREIGN KEY (`department_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `trainer_feedback_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trainer_feedback_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `average_Score` float DEFAULT NULL,
  `communication_Score` float DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `interaction_Score` float DEFAULT NULL,
  `schedule_Training_Id` bigint(20) DEFAULT NULL,
  `submitted_Date` datetime(6) DEFAULT NULL,
  `technical_Knowledge_Score` float DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `user_Id_Trainee` bigint(20) DEFAULT NULL,
  `user_Id_Trainer` bigint(20) DEFAULT NULL,
  `trainer_id` bigint(20) NOT NULL,
  `trainee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf3f2s1p1fso5qbmep34xwxg4e` (`trainer_id`),
  KEY `FKkkrt7lwq9nj07mdcms19soda4` (`trainee_id`),
  CONSTRAINT `FKf3f2s1p1fso5qbmep34xwxg4e` FOREIGN KEY (`trainer_id`) REFERENCES `trainer_detail` (`id`),
  CONSTRAINT `FKkkrt7lwq9nj07mdcms19soda4` FOREIGN KEY (`trainee_id`) REFERENCES `training_user_registration_mapping` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `training_employee_category_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `training_employee_category_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_Category` varchar(255) DEFAULT NULL,
  `schedule_Training_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `training_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `training_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `training_Type_Name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `training_user_level_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `training_user_level_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `schedule_Training_Id` bigint(20) DEFAULT NULL,
  `user_Level` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `training_user_registration_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `training_user_registration_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attendance_Status` varchar(255) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `registration_Date` datetime(6) DEFAULT NULL,
  `registration_Status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `schedule_training_id` bigint(20) NOT NULL,
  `trainee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKabtk4i0g5vitn9ja3xqk6tf9t` (`schedule_training_id`),
  KEY `FK5p7j4ohssq848r0ufoao9lspa` (`trainee_id`),
  CONSTRAINT `FK5p7j4ohssq848r0ufoao9lspa` FOREIGN KEY (`trainee_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKabtk4i0g5vitn9ja3xqk6tf9t` FOREIGN KEY (`schedule_training_id`) REFERENCES `schedule_training` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tripexp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tripexp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adriver` varchar(255) DEFAULT NULL,
  `disporder` bigint(20) DEFAULT NULL,
  `slab` varchar(255) DEFAULT NULL,
  `slabid` bigint(20) DEFAULT NULL,
  `swfare` varchar(255) DEFAULT NULL,
  `expid` bigint(20) NOT NULL,
  `tripid` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5x1m029r2ydce76qwm1oqs2sj` (`expid`),
  KEY `FKkkehmkjpr1uiauuvgh44bbob9` (`tripid`),
  CONSTRAINT `FK5x1m029r2ydce76qwm1oqs2sj` FOREIGN KEY (`expid`) REFERENCES `expenses` (`id`),
  CONSTRAINT `FKkkehmkjpr1uiauuvgh44bbob9` FOREIGN KEY (`tripid`) REFERENCES `vtrips` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `trnindex`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `trnindex` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `advno` bigint(20) DEFAULT NULL,
  `batchno` bigint(20) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `compcode` bigint(20) DEFAULT NULL,
  `formid` bigint(20) DEFAULT NULL,
  `forwardno` bigint(20) DEFAULT NULL,
  `lastno` bigint(20) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  `receivedno` bigint(20) DEFAULT NULL,
  `vouno` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tslap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tslap` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aday` int(11) DEFAULT NULL,
  `additional_Time` varchar(255) DEFAULT NULL,
  `bday` int(11) DEFAULT NULL,
  `btime` time DEFAULT NULL,
  `colid` varchar(255) DEFAULT NULL,
  `dispcolor` varchar(255) DEFAULT NULL,
  `edtime` datetime(6) DEFAULT NULL,
  `edtvar` varchar(255) DEFAULT NULL,
  `etime` time DEFAULT NULL,
  `fri` varchar(255) DEFAULT NULL,
  `hol` varchar(255) DEFAULT NULL,
  `mon` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pshift` int(11) DEFAULT NULL,
  `pspos` varchar(255) DEFAULT NULL,
  `sat` varchar(255) DEFAULT NULL,
  `shour` int(11) DEFAULT NULL,
  `sun` varchar(255) DEFAULT NULL,
  `thu` varchar(255) DEFAULT NULL,
  `tue` varchar(255) DEFAULT NULL,
  `wed` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `uniform_deduction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `uniform_deduction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` bigint(20) DEFAULT NULL,
  `billing_month` bigint(20) DEFAULT NULL,
  `contable` bigint(20) DEFAULT NULL,
  `created_time` date DEFAULT NULL,
  `createdby` bigint(20) DEFAULT NULL,
  `empcode` bigint(20) DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `unitreportproductlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `unitreportproductlist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `input` varchar(255) DEFAULT NULL,
  `output` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `reportOrder` bigint(20) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `yieldGroup` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `units`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `units` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `display_name` varchar(255) DEFAULT NULL,
  `eway_bill_Flag` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `urepfin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `urepfin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ureptplate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ureptplate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bprodlot` double DEFAULT NULL,
  `delitCleaningProcess` bigint(20) DEFAULT NULL,
  `delitProcess` bigint(20) DEFAULT NULL,
  `exop` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `packsjid` bigint(20) DEFAULT NULL,
  `pbyeid` bigint(20) DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  `proid2` bigint(20) DEFAULT NULL,
  `sbyeid` bigint(20) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `sprocess` bigint(20) DEFAULT NULL,
  `syield` varchar(255) DEFAULT NULL,
  `ygroup` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activity_time` datetime(6) DEFAULT NULL,
  `activity_type` varchar(255) DEFAULT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `token_expiry_time` datetime(6) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7520 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `v_master_table_inventory`;
/*!50001 DROP VIEW IF EXISTS `v_master_table_inventory`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `v_master_table_inventory` AS SELECT
 1 AS `table_name`,
  1 AS `entity_code`,
  1 AS `category`,
  1 AS `display_name`,
  1 AS `page_count`,
  1 AS `workflow_count`,
  1 AS `last_write_at`,
  1 AS `writes_7d`,
  1 AS `row_count` */;
SET character_set_client = @saved_cs_client;
DROP TABLE IF EXISTS `v_reorder_alert`;
/*!50001 DROP VIEW IF EXISTS `v_reorder_alert`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `v_reorder_alert` AS SELECT
 1 AS `tenant_id`,
  1 AS `item_code`,
  1 AS `item_name`,
  1 AS `uom`,
  1 AS `reorder_min`,
  1 AS `reorder_max`,
  1 AS `safety_stock`,
  1 AS `eoq`,
  1 AS `lead_time_days`,
  1 AS `qty_on_hand`,
  1 AS `qty_reserved`,
  1 AS `qty_available`,
  1 AS `alert_level` */;
SET character_set_client = @saved_cs_client;
DROP TABLE IF EXISTS `values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `values` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attributes_id` bigint(20) DEFAULT NULL,
  `max_value` varchar(255) DEFAULT NULL,
  `min_value` varchar(255) DEFAULT NULL,
  `score` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_break_down_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_break_down_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `aendt` varchar(255) DEFAULT NULL,
  `apby` bigint(20) DEFAULT NULL,
  `apendt` varchar(255) DEFAULT NULL,
  `astrvarr` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cendt` varchar(255) DEFAULT NULL,
  `cluster` bigint(20) DEFAULT NULL,
  `datepro` date DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `difftime` double DEFAULT NULL,
  `endate` date DEFAULT NULL,
  `entime` time DEFAULT NULL,
  `entposition` varchar(255) DEFAULT NULL,
  `inkm` bigint(20) DEFAULT NULL,
  `iono` varchar(255) DEFAULT NULL,
  `iopageno` varchar(255) DEFAULT NULL,
  `jcarddt` datetime(6) DEFAULT NULL,
  `jcardno` varchar(255) DEFAULT NULL,
  `mcid` bigint(20) DEFAULT NULL,
  `mciso` varchar(255) DEFAULT NULL,
  `mcname` varchar(255) DEFAULT NULL,
  `modified` datetime(6) DEFAULT NULL,
  `outkm` bigint(20) DEFAULT NULL,
  `paction` varchar(255) DEFAULT NULL,
  `problem` varchar(255) DEFAULT NULL,
  `rcause` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `stdate` date DEFAULT NULL,
  `stren` varchar(255) DEFAULT NULL,
  `strst` varchar(255) DEFAULT NULL,
  `sttime` time DEFAULT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `trialby` bigint(20) DEFAULT NULL,
  `varrivedon` datetime(6) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `vendt` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_driver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_driver` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `driver_id` bigint(20) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `vehicle_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2b0f2n6qnhfurf16q8brypqxd` (`vehicle_id`),
  CONSTRAINT `FK2b0f2n6qnhfurf16q8brypqxd` FOREIGN KEY (`vehicle_id`) REFERENCES `machine` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint(20) DEFAULT NULL,
  `vehicle_no` varchar(255) DEFAULT NULL,
  `group_head` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKheuwaqxv1sb411ov8k7isrdfp` (`group_head`),
  CONSTRAINT `FKheuwaqxv1sb411ov8k7isrdfp` FOREIGN KEY (`group_head`) REFERENCES `vehicle_grouping_head` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_grouping_head`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_grouping_head` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active_flag` int(11) DEFAULT NULL,
  `cost_target` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `target` int(11) DEFAULT NULL,
  `type` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` varchar(255) DEFAULT NULL,
  `CREATED_DATE` datetime(6) DEFAULT NULL,
  `no_of_vehicle` bigint(20) DEFAULT NULL,
  `req_department` bigint(20) DEFAULT NULL,
  `req_plant` bigint(20) DEFAULT NULL,
  `requested_by` bigint(20) DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `travel_date` datetime(6) DEFAULT NULL,
  `travel_distance` varchar(255) DEFAULT NULL,
  `trip_id` bigint(20) DEFAULT NULL,
  `type_of_vehicle` varchar(255) DEFAULT NULL,
  `UPDATED_DATE` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_request_allot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_request_allot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actual_arrival_on` datetime(6) DEFAULT NULL,
  `actual_departure_on` datetime(6) DEFAULT NULL,
  `approx_date` datetime(6) DEFAULT NULL,
  `arrival_by` bigint(20) DEFAULT NULL,
  `arrival_on` datetime(6) DEFAULT NULL,
  `arrival_plant` varchar(255) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `CREATED_BY` bigint(20) DEFAULT NULL,
  `CREATED_DATE` datetime(6) DEFAULT NULL,
  `departure_by` bigint(20) DEFAULT NULL,
  `departure_on` datetime(6) DEFAULT NULL,
  `distance` bigint(20) DEFAULT NULL,
  `req_id` bigint(20) DEFAULT NULL,
  `shift` varchar(255) DEFAULT NULL,
  `shift_slab` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `UPDATED_DATE` datetime(6) DEFAULT NULL,
  `vehicle_info_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_request_driver_additional`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_request_driver_additional` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `vehicle_req_allot_id` bigint(20) DEFAULT NULL,
  `CREATED_BY` bigint(20) DEFAULT NULL,
  `CREATED_DATE` datetime(6) DEFAULT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `driver_status` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `UPDATED_DATE` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_request_driver_shuffle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_request_driver_shuffle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `vehicle_req_allot_id` bigint(20) DEFAULT NULL,
  `CREATED_BY` bigint(20) DEFAULT NULL,
  `CREATED_DATE` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `UPDATED_DATE` datetime(6) DEFAULT NULL,
  `vehicle_info_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_request_ref_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_request_ref_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action` varchar(255) DEFAULT NULL,
  `vehicle_req_allot_id` bigint(20) DEFAULT NULL,
  `CREATED_BY` bigint(20) DEFAULT NULL,
  `CREATED_DATE` datetime(6) DEFAULT NULL,
  `ref_type` varchar(255) DEFAULT NULL,
  `ref_type_value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vehicle_request_vanboy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_request_vanboy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `vehicle_req_allot_id` bigint(20) DEFAULT NULL,
  `CREATED_BY` bigint(20) DEFAULT NULL,
  `CREATED_DATE` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vendor_sister_concern_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendor_sister_concern_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) DEFAULT NULL,
  `vendors` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vexgen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vexgen` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `authenty` bigint(20) DEFAULT NULL,
  `avgcost` double DEFAULT NULL,
  `bankacno` varchar(255) DEFAULT NULL,
  `bankcode` varchar(255) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bdt` date DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `billid` bigint(20) DEFAULT NULL,
  `bno` varchar(255) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `conbranch` bigint(20) DEFAULT NULL,
  `condept` bigint(20) DEFAULT NULL,
  `conid` bigint(20) DEFAULT NULL,
  `conname` varchar(255) DEFAULT NULL,
  `contype` varchar(255) DEFAULT NULL,
  `conzone` varchar(255) DEFAULT NULL,
  `conzoneid` bigint(20) DEFAULT NULL,
  `cost` double DEFAULT NULL,
  `extype` varchar(255) DEFAULT NULL,
  `fre` double DEFAULT NULL,
  `ltotal` double DEFAULT NULL,
  `oth` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `reftype` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `urate` double DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vexpdes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vexpdes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bId` bigint(20) DEFAULT NULL,
  `batchNo` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `refId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `view_accounts_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `view_accounts_ledger` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `approved_By` bigint(20) DEFAULT NULL,
  `authorize_By` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `party_id` bigint(20) DEFAULT NULL,
  `prepared_By` bigint(20) DEFAULT NULL,
  `ref_date` date DEFAULT NULL,
  `trn_amount` double DEFAULT NULL,
  `trn_date` date DEFAULT NULL,
  `trn_Type` bigint(20) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `verified_By` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vinfo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `battery` varchar(255) DEFAULT NULL,
  `capacity` varchar(255) DEFAULT NULL,
  `cate` varchar(255) DEFAULT NULL,
  `chassis` varchar(255) DEFAULT NULL,
  `engine` varchar(255) DEFAULT NULL,
  `ftype` varchar(255) DEFAULT NULL,
  `mmx` int(11) DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `target` int(11) DEFAULT NULL,
  `tcapacity` varchar(255) DEFAULT NULL,
  `tyre1` varchar(255) DEFAULT NULL,
  `tyre10` varchar(255) DEFAULT NULL,
  `tyre2` varchar(255) DEFAULT NULL,
  `tyre3` varchar(255) DEFAULT NULL,
  `tyre4` varchar(255) DEFAULT NULL,
  `tyre5` varchar(255) DEFAULT NULL,
  `tyre6` varchar(255) DEFAULT NULL,
  `tyre7` varchar(255) DEFAULT NULL,
  `tyre8` varchar(255) DEFAULT NULL,
  `tyre9` varchar(255) DEFAULT NULL,
  `upby` bigint(20) DEFAULT NULL,
  `upon` datetime(6) DEFAULT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `vid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtqel60f7jvs8poitbyyivs2ul` (`driver_id`),
  KEY `FKcn0ja52mdxreylg0n9pwe3rcn` (`vid`),
  CONSTRAINT `FKcn0ja52mdxreylg0n9pwe3rcn` FOREIGN KEY (`vid`) REFERENCES `machine` (`id`),
  CONSTRAINT `FKtqel60f7jvs8poitbyyivs2ul` FOREIGN KEY (`driver_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `adate` datetime(6) DEFAULT NULL,
  `aendt` varchar(255) DEFAULT NULL,
  `amt` double DEFAULT NULL,
  `apdate` datetime(6) DEFAULT NULL,
  `apendt` varchar(255) DEFAULT NULL,
  `approval` varchar(255) DEFAULT NULL,
  `apstaff` bigint(20) DEFAULT NULL,
  `astaff` bigint(20) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `bankacno` varchar(255) DEFAULT NULL,
  `bankcode` varchar(255) DEFAULT NULL,
  `bendt` varchar(255) DEFAULT NULL,
  `bentdt` date DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cform` varchar(255) DEFAULT NULL,
  `cformdt` date DEFAULT NULL,
  `cfstaff` bigint(20) DEFAULT NULL,
  `company` bigint(20) DEFAULT NULL,
  `department` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `destinbranch` bigint(20) DEFAULT NULL,
  `entry` datetime(6) DEFAULT NULL,
  `f17dt` date DEFAULT NULL,
  `f17staff` bigint(20) DEFAULT NULL,
  `form17` varchar(255) DEFAULT NULL,
  `formid` bigint(20) DEFAULT NULL,
  `grpNo` varchar(255) DEFAULT NULL,
  `hdate` date DEFAULT NULL,
  `hofwd` varchar(255) DEFAULT NULL,
  `hofwddate` date DEFAULT NULL,
  `hofwdenddt` varchar(255) DEFAULT NULL,
  `hofwdstaff` bigint(20) DEFAULT NULL,
  `hofwdtable` varchar(255) DEFAULT NULL,
  `hoprocess` varchar(255) DEFAULT NULL,
  `horecddate` date DEFAULT NULL,
  `horeceived` varchar(255) DEFAULT NULL,
  `horeceivedby` bigint(20) DEFAULT NULL,
  `hstaff` bigint(20) DEFAULT NULL,
  `idate` datetime(6) DEFAULT NULL,
  `iendt` varchar(255) DEFAULT NULL,
  `ifscCode` varchar(255) DEFAULT NULL,
  `inv` varchar(255) DEFAULT NULL,
  `istaff` bigint(20) DEFAULT NULL,
  `items` int(11) DEFAULT NULL,
  `itemtype` varchar(255) DEFAULT NULL,
  `payableToName` varchar(255) DEFAULT NULL,
  `payment` varchar(255) DEFAULT NULL,
  `paytable` varchar(255) DEFAULT NULL,
  `payto` bigint(20) DEFAULT NULL,
  `pdate` date DEFAULT NULL,
  `pmode` varchar(255) DEFAULT NULL,
  `pstaff` bigint(20) DEFAULT NULL,
  `rBy` bigint(20) DEFAULT NULL,
  `rDate` datetime(6) DEFAULT NULL,
  `reject` varchar(255) DEFAULT NULL,
  `sourcebranch` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `submenddt` varchar(255) DEFAULT NULL,
  `submittrnno` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `vdate` datetime(6) DEFAULT NULL,
  `vendt` varchar(255) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `vouName` varchar(255) DEFAULT NULL,
  `vouType` varchar(255) DEFAULT NULL,
  `voudt` date DEFAULT NULL,
  `vouno` varchar(255) DEFAULT NULL,
  `vourecd` varchar(255) DEFAULT NULL,
  `vourecdby` bigint(20) DEFAULT NULL,
  `vourecddate` date DEFAULT NULL,
  `vourecdtrnno` varchar(255) DEFAULT NULL,
  `voushift` bigint(20) DEFAULT NULL,
  `vousubmdate` date DEFAULT NULL,
  `vousubmit` varchar(255) DEFAULT NULL,
  `vousubmstaff` bigint(20) DEFAULT NULL,
  `vousubmtable` varchar(255) DEFAULT NULL,
  `vstaff` bigint(20) DEFAULT NULL,
  `vtype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vouformat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouformat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `isono` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vouitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouitems` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `asscount` bigint(20) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `contype` varchar(255) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `expid` bigint(20) DEFAULT NULL,
  `formid` bigint(20) DEFAULT NULL,
  `ltotal` double DEFAULT NULL,
  `qty` double DEFAULT NULL,
  `rcost` double DEFAULT NULL,
  `refid` bigint(20) DEFAULT NULL,
  `reftype` varchar(255) DEFAULT NULL,
  `rtype` varchar(255) DEFAULT NULL,
  `secExpName` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `vouid` bigint(20) DEFAULT NULL,
  `vouno` varchar(255) DEFAULT NULL,
  `vrefid` bigint(20) DEFAULT NULL,
  `vtype` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vouname`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouname` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dept` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  `recordStatus` varchar(255) DEFAULT NULL,
  `vouFirmId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `voupayments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `voupayments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `aby` bigint(20) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `aon` varchar(255) DEFAULT NULL,
  `aonNew` datetime(6) DEFAULT NULL,
  `apby` bigint(20) DEFAULT NULL,
  `apon` varchar(255) DEFAULT NULL,
  `aponNew` datetime(6) DEFAULT NULL,
  `approve` varchar(255) DEFAULT NULL,
  `authenty` varchar(255) DEFAULT NULL,
  `bid` bigint(20) DEFAULT NULL,
  `cby` bigint(20) DEFAULT NULL,
  `cid` bigint(20) DEFAULT NULL,
  `con` varchar(255) DEFAULT NULL,
  `conNew` datetime(6) DEFAULT NULL,
  `did` bigint(20) DEFAULT NULL,
  `fmdate` date DEFAULT NULL,
  `rBy` bigint(20) DEFAULT NULL,
  `rDate` datetime(6) DEFAULT NULL,
  `reject` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `todate` date DEFAULT NULL,
  `trndate` date DEFAULT NULL,
  `trnid` bigint(20) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `trnweekid` bigint(20) DEFAULT NULL,
  `usecid` varchar(255) DEFAULT NULL,
  `vby` bigint(20) DEFAULT NULL,
  `verify` varchar(255) DEFAULT NULL,
  `von` varchar(255) DEFAULT NULL,
  `vonNew` datetime(6) DEFAULT NULL,
  `vouid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `vtrips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vtrips` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bhrs` varchar(255) DEFAULT NULL,
  `cate` varchar(255) DEFAULT NULL,
  `coverage` varchar(255) DEFAULT NULL,
  `distance` varchar(255) DEFAULT NULL,
  `freq` varchar(255) DEFAULT NULL,
  `jhrs` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `shortname` varchar(255) DEFAULT NULL,
  `starttime` time DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wcshift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `wcshift` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mrp` double DEFAULT NULL,
  `astatus` varchar(255) DEFAULT NULL,
  `acby` bigint(20) DEFAULT NULL,
  `acon` varchar(255) DEFAULT NULL,
  `bstatus` varchar(255) DEFAULT NULL,
  `batchid` bigint(20) DEFAULT NULL,
  `batchno` varchar(255) DEFAULT NULL,
  `bby` bigint(20) DEFAULT NULL,
  `begin` varchar(255) DEFAULT NULL,
  `bon` varchar(255) DEFAULT NULL,
  `branch` bigint(20) DEFAULT NULL,
  `dept` bigint(20) DEFAULT NULL,
  `dominoCount` bigint(20) DEFAULT NULL,
  `e_end` varchar(255) DEFAULT NULL,
  `estatus` varchar(255) DEFAULT NULL,
  `eby` bigint(20) DEFAULT NULL,
  `eon` varchar(255) DEFAULT NULL,
  `imagelocation` varchar(255) DEFAULT NULL,
  `jentry` varchar(255) DEFAULT NULL,
  `jentryby` bigint(20) DEFAULT NULL,
  `jentryon` varchar(255) DEFAULT NULL,
  `sjid` bigint(20) DEFAULT NULL,
  `pshift` varchar(255) DEFAULT NULL,
  `pshiftid` bigint(20) DEFAULT NULL,
  `proid` bigint(20) DEFAULT NULL,
  `reversedby` varchar(255) DEFAULT NULL,
  `reversedon` datetime(6) DEFAULT NULL,
  `salesorderid` bigint(20) DEFAULT NULL,
  `shiftdate` date DEFAULT NULL,
  `shrs` varchar(255) DEFAULT NULL,
  `strbegin` varchar(255) DEFAULT NULL,
  `strend` varchar(255) DEFAULT NULL,
  `trnno` varchar(255) DEFAULT NULL,
  `wcid` bigint(20) DEFAULT NULL,
  `wctable` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `weekday`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekday` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `weekday` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `weekoff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekoff` (
  `emp_id` bigint(20) NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `emp_type` int(11) DEFAULT NULL,
  `shiftassigndate` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `weekoffday` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`emp_id`),
  KEY `FK7i2qg9hsjix63rnk5sibmco2s` (`weekoffday`),
  CONSTRAINT `FK7i2qg9hsjix63rnk5sibmco2s` FOREIGN KEY (`weekoffday`) REFERENCES `weekday` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `welfare_amount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `welfare_amount` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `from_Date` datetime(6) DEFAULT NULL,
  `to_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `welfare_category` varchar(255) DEFAULT NULL,
  `welfare_name` varchar(255) DEFAULT NULL,
  `productionentryid` bigint(20) DEFAULT NULL,
  `welfareId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7tqboub0ocq8dpdj47mvaeyq4` (`productionentryid`),
  KEY `FKasj3eo52juuc3358yh63jwaos` (`welfareId`),
  CONSTRAINT `FK7tqboub0ocq8dpdj47mvaeyq4` FOREIGN KEY (`productionentryid`) REFERENCES `production_entry` (`id`),
  CONSTRAINT `FKasj3eo52juuc3358yh63jwaos` FOREIGN KEY (`welfareId`) REFERENCES `welfare_contribution` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `welfare_contribution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `welfare_contribution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `food_Percentage` varchar(255) DEFAULT NULL,
  `from_Date` datetime(6) DEFAULT NULL,
  `to_Date` datetime(6) DEFAULT NULL,
  `transportation_Percentage` varchar(255) DEFAULT NULL,
  `uniform_percentage` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `welfare_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `welfare_employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cost` bigint(20) DEFAULT NULL,
  `emp_id` bigint(20) DEFAULT NULL,
  `item` varchar(255) DEFAULT NULL,
  `registrationon` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK55j9lx2fqg9fs98kmkt6b5jy0` (`user_id`),
  CONSTRAINT `FK55j9lx2fqg9fs98kmkt6b5jy0` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `welfare_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `welfare_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `welfare_category` varchar(255) DEFAULT NULL,
  `welfare_Items_Value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `welfare_registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `welfare_registration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `employee_Type` varchar(255) DEFAULT NULL,
  `from_Date` datetime(6) DEFAULT NULL,
  `select_Employee` varchar(255) DEFAULT NULL,
  `to_Date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `welfare_Name` varchar(255) DEFAULT NULL,
  `branch_id` bigint(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKchcnd078lcvudqdt7ohf72b7b` (`branch_id`),
  KEY `FKblunqst7u27i9bjsw8wsl8yak` (`dept_id`),
  CONSTRAINT `FKblunqst7u27i9bjsw8wsl8yak` FOREIGN KEY (`dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKchcnd078lcvudqdt7ohf72b7b` FOREIGN KEY (`branch_id`) REFERENCES `branch` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `windmill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windmill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `capacity` double DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  `mf` double DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `otherchargespayablehtserviceid` int(11) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `td_loss` double DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `windmill_othercharges_trn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windmill_othercharges_trn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `trndate` date DEFAULT NULL,
  `udpatedby` bigint(20) DEFAULT NULL,
  `windmillid` bigint(20) DEFAULT NULL,
  `windmillotherchargesid` bigint(20) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `windmillattributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windmillattributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `formula` varchar(100) DEFAULT NULL,
  `formulaflag` int(11) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `orderid` bigint(20) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `windmillid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `windmillothercharges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windmillothercharges` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `displayorder` int(11) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `windmillid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `windmilltimming`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windmilltimming` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(300) DEFAULT NULL,
  `name` varchar(300) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `windmilltrn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windmilltrn` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attributeid` bigint(20) DEFAULT NULL,
  `closingreading` double DEFAULT NULL,
  `createddate` date DEFAULT NULL,
  `difference` double DEFAULT NULL,
  `entposition` varchar(1) DEFAULT NULL,
  `fromdate` date DEFAULT NULL,
  `month` bigint(20) DEFAULT NULL,
  `openingreading` double DEFAULT NULL,
  `timmingid` bigint(20) DEFAULT NULL,
  `todate` date DEFAULT NULL,
  `updatedby` bigint(20) DEFAULT NULL,
  `windmillid` bigint(20) DEFAULT NULL,
  `workingdays` bigint(20) DEFAULT NULL,
  `year` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `with_in_branch_distribution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `with_in_branch_distribution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `docket_No` varchar(255) DEFAULT NULL,
  `hup_id` varchar(255) DEFAULT NULL,
  `issue_date` datetime(6) DEFAULT NULL,
  `issue_id` bigint(20) DEFAULT NULL,
  `received_date` datetime(6) DEFAULT NULL,
  `request_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `within_branch_Distribution_Value` varchar(255) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `issue_org_id` bigint(20) DEFAULT NULL,
  `received_user_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  `request_org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK77enuln6kh5b6bggiv17anslq` (`issue_branch_id`),
  KEY `FK2av7bvtl725k1va75mv7mra9o` (`user_id`),
  KEY `FKgc2dg6yffbds0j6f30oxor15y` (`issue_dept_id`),
  KEY `FK2395lsf1ixgjsu4jl8jsd97ms` (`issue_org_id`),
  KEY `FK5vp6ns4hgf2hbv7yycyhy6hgj` (`received_user_id`),
  KEY `FKfdji4j8w2vx2qfs91pff725a3` (`request_branch_id`),
  KEY `FKps1g2vt554lejhbh61mm0nyun` (`request_dept_id`),
  KEY `FKflabc3k5b7y2dib4jsv27v52p` (`request_org_id`),
  CONSTRAINT `FK2395lsf1ixgjsu4jl8jsd97ms` FOREIGN KEY (`issue_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FK2av7bvtl725k1va75mv7mra9o` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK5vp6ns4hgf2hbv7yycyhy6hgj` FOREIGN KEY (`received_user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK77enuln6kh5b6bggiv17anslq` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKfdji4j8w2vx2qfs91pff725a3` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKflabc3k5b7y2dib4jsv27v52p` FOREIGN KEY (`request_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKgc2dg6yffbds0j6f30oxor15y` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKps1g2vt554lejhbh61mm0nyun` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `with_in_branch_receive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `with_in_branch_receive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `entry_date` date DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `issue_Id` bigint(20) DEFAULT NULL,
  `received_date` date DEFAULT NULL,
  `request_date` date DEFAULT NULL,
  `request_Id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `trn_no` varchar(255) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `issue_branch_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `issue_dept_id` bigint(20) DEFAULT NULL,
  `issue_org_id` bigint(20) DEFAULT NULL,
  `received_user_id` bigint(20) DEFAULT NULL,
  `request_branch_id` bigint(20) DEFAULT NULL,
  `request_dept_id` bigint(20) DEFAULT NULL,
  `request_org_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4sunibnh1m2ice4hvyg1bjfog` (`issue_branch_id`),
  KEY `FK19f8raktvbx7h155dwuva0emd` (`user_id`),
  KEY `FKrtxwkup8hy6w5gm7j7kysk7yo` (`issue_dept_id`),
  KEY `FKj189f975blb51peqrs3l2w1dd` (`issue_org_id`),
  KEY `FKcqj5uddnsjdagh2tf698w9cmk` (`received_user_id`),
  KEY `FK76yiu7dethnwvkqiu2bbnaerg` (`request_branch_id`),
  KEY `FKf1jqmeew4i2wy0tyn2pdvaswt` (`request_dept_id`),
  KEY `FKgnfy4i4oeggrfyif6le4fuh0d` (`request_org_id`),
  CONSTRAINT `FK19f8raktvbx7h155dwuva0emd` FOREIGN KEY (`user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FK4sunibnh1m2ice4hvyg1bjfog` FOREIGN KEY (`issue_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FK76yiu7dethnwvkqiu2bbnaerg` FOREIGN KEY (`request_branch_id`) REFERENCES `branch` (`id`),
  CONSTRAINT `FKcqj5uddnsjdagh2tf698w9cmk` FOREIGN KEY (`received_user_id`) REFERENCES `employee_details` (`id`),
  CONSTRAINT `FKf1jqmeew4i2wy0tyn2pdvaswt` FOREIGN KEY (`request_dept_id`) REFERENCES `department_details` (`id`),
  CONSTRAINT `FKgnfy4i4oeggrfyif6le4fuh0d` FOREIGN KEY (`request_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKj189f975blb51peqrs3l2w1dd` FOREIGN KEY (`issue_org_id`) REFERENCES `organisation` (`id`),
  CONSTRAINT `FKrtxwkup8hy6w5gm7j7kysk7yo` FOREIGN KEY (`issue_dept_id`) REFERENCES `department_details` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `within_branch_receipt_bin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `within_branch_receipt_bin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `allotted_quantity` double DEFAULT NULL,
  `bin_id` bigint(20) DEFAULT NULL,
  `bin_no` varchar(255) DEFAULT NULL,
  `item_id` bigint(20) DEFAULT NULL,
  `within_item_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `within_branch_receipt_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `within_branch_receipt_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(20) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `package_quantity` varchar(255) DEFAULT NULL,
  `package_type` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `within_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `within_branch_receive_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `within_branch_receive_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bin_stock_id` bigint(20) DEFAULT NULL,
  `issue_Branch_Id` bigint(20) DEFAULT NULL,
  `issue_Dept_Id` bigint(20) DEFAULT NULL,
  `issued_From_Batch` varchar(255) DEFAULT NULL,
  `issued_From_Bin` varchar(255) DEFAULT NULL,
  `item_Id` bigint(20) DEFAULT NULL,
  `item_Name` varchar(255) DEFAULT NULL,
  `package_Quantity` varchar(255) DEFAULT NULL,
  `package_Type` varchar(255) DEFAULT NULL,
  `quantity` varchar(255) DEFAULT NULL,
  `receive_Branch_Id` bigint(20) DEFAULT NULL,
  `receive_Dept_Id` bigint(20) DEFAULT NULL,
  `sal_status` varchar(255) DEFAULT NULL,
  `select_Lot` varchar(255) DEFAULT NULL,
  `within_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `work_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `work_flow_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `work_flow_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_flow_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `child_flow_id` bigint(20) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `flow_name` varchar(255) DEFAULT NULL,
  `main` int(11) DEFAULT NULL,
  `parent_flow_id` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `flow_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl73ypt4wqccgrq4oxuuh8xul5` (`flow_id`),
  CONSTRAINT `FKl73ypt4wqccgrq4oxuuh8xul5` FOREIGN KEY (`flow_id`) REFERENCES `work_flow` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `work_flow_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_flow_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `flow_Id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `step_Name` varchar(255) DEFAULT NULL,
  `step_Number` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `work_flow_steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_flow_steps` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `step_Name` varchar(255) DEFAULT NULL,
  `step_Number` int(11) DEFAULT NULL,
  `workflow_Id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `workflow_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `code` varchar(128) NOT NULL,
  `version` int(11) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PUBLISHED',
  `body_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`body_json`)),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_wfd` (`tenant_id`,`code`,`version`),
  KEY `idx_wfd_code` (`tenant_id`,`code`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `workflow_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `instance_id` bigint(20) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT current_timestamp(),
  `kind` varchar(32) NOT NULL,
  `from_state` varchar(64) DEFAULT NULL,
  `to_state` varchar(64) DEFAULT NULL,
  `page_code` varchar(128) DEFAULT NULL,
  `actor_user_id` bigint(20) DEFAULT NULL,
  `payload_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`payload_json`)),
  PRIMARY KEY (`id`),
  KEY `idx_we_instance_ts` (`instance_id`,`ts`),
  CONSTRAINT `fk_we_instance` FOREIGN KEY (`instance_id`) REFERENCES `workflow_instance` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11968 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `workflow_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `workflow_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL,
  `workflow_code` varchar(128) NOT NULL,
  `workflow_version` int(11) NOT NULL,
  `workflow_owner_tenant_id` bigint(20) DEFAULT NULL,
  `current_state` varchar(64) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'OPEN',
  `business_ref` varchar(128) DEFAULT NULL,
  `submitted_data_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`submitted_data_json`)),
  `assigned_role` varchar(64) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `audit_field_rules_probe` varchar(255) DEFAULT NULL,
  `reject_reason` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wi_queue` (`tenant_id`,`assigned_role`,`status`,`current_state`),
  KEY `idx_wi_ref` (`tenant_id`,`business_ref`)
) ENGINE=InnoDB AUTO_INCREMENT=1456 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50001 DROP VIEW IF EXISTS `v_master_table_inventory`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_unicode_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=CURRENT_USER SQL SECURITY DEFINER */
/*!50001 VIEW `v_master_table_inventory` AS select `er`.`table_name` AS `table_name`,`er`.`code` AS `entity_code`,`er`.`category` AS `category`,`er`.`display_name` AS `display_name`,(select count(distinct `pd`.`id`) from `page_definition` `pd` where `pd`.`tenant_id` IN (-1, 1) and (json_search((select `pv`.`body_json` from `page_version` `pv` where `pv`.`id` = `pd`.`current_version_id`),'one',`er`.`code`,NULL,'$**.entity') is not null or json_search((select `pv`.`body_json` from `page_version` `pv` where `pv`.`id` = `pd`.`current_version_id`),'one',`er`.`table_name`,NULL,'$**.table') is not null)) AS `page_count`,(select count(distinct `wd`.`id`) from `workflow_definition` `wd` where `wd`.`tenant_id` IN (-1, 1) and `wd`.`status` = 'PUBLISHED' and exists(select 1 from `page_definition` `pd2` where `pd2`.`tenant_id` IN (-1, 1) and json_search(`wd`.`body_json`,'one',`pd2`.`code`,NULL,'$**.pageCode') is not null and (json_search((select `pv2`.`body_json` from `page_version` `pv2` where `pv2`.`id` = `pd2`.`current_version_id`),'one',`er`.`code`,NULL,'$**.entity') is not null or json_search((select `pv2`.`body_json` from `page_version` `pv2` where `pv2`.`id` = `pd2`.`current_version_id`),'one',`er`.`table_name`,NULL,'$**.table') is not null) limit 1)) AS `workflow_count`,(select max(`al`.`ts`) from `m0_audit_log` `al` where `al`.`entity_code` = `er`.`code` and `al`.`ts` >= current_timestamp() - interval 7 day and `al`.`action_kind` in ('CREATE','UPDATE','DELETE','WRITE','INSERT')) AS `last_write_at`,(select count(0) from `m0_audit_log` `al2` where `al2`.`entity_code` = `er`.`code` and `al2`.`ts` >= current_timestamp() - interval 7 day and `al2`.`action_kind` in ('CREATE','UPDATE','DELETE','WRITE','INSERT')) AS `writes_7d`,coalesce(`t`.`TABLE_ROWS`,0) AS `row_count` from (`entity_registry` `er` left join `information_schema`.`tables` `t` on(`t`.`TABLE_SCHEMA` = database() and `t`.`TABLE_NAME` = `er`.`table_name`)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!50001 DROP VIEW IF EXISTS `v_reorder_alert`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_unicode_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=CURRENT_USER SQL SECURITY DEFINER */
/*!50001 VIEW `v_reorder_alert` AS select `i`.`tenant_id` AS `tenant_id`,`i`.`item_code` AS `item_code`,`i`.`item_name` AS `item_name`,`i`.`uom` AS `uom`,`i`.`reorder_min` AS `reorder_min`,`i`.`reorder_max` AS `reorder_max`,`i`.`safety_stock` AS `safety_stock`,`i`.`eoq` AS `eoq`,`i`.`lead_time_days` AS `lead_time_days`,coalesce(sum(`sb`.`qty_on_hand`),0) AS `qty_on_hand`,coalesce(sum(`sb`.`qty_reserved`),0) AS `qty_reserved`,coalesce(sum(`sb`.`qty_on_hand` - `sb`.`qty_reserved`),0) AS `qty_available`,case when coalesce(sum(`sb`.`qty_on_hand`),0) <= coalesce(`i`.`safety_stock`,0) then 'CRITICAL' when coalesce(sum(`sb`.`qty_on_hand`),0) <= coalesce(`i`.`reorder_min`,0) then 'REORDER' when coalesce(sum(`sb`.`qty_on_hand`),0) >= coalesce(`i`.`reorder_max`,0) then 'OVERSTOCK' else 'OK' end AS `alert_level` from (`m5_item_master` `i` left join `m5_stock_balance` `sb` on(`sb`.`item_code` = `i`.`item_code` and `sb`.`tenant_id` = `i`.`tenant_id`)) group by `i`.`tenant_id`,`i`.`item_code`,`i`.`item_name`,`i`.`uom`,`i`.`reorder_min`,`i`.`reorder_max`,`i`.`safety_stock`,`i`.`eoq`,`i`.`lead_time_days` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;


SET FOREIGN_KEY_CHECKS = 1;
