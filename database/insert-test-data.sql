-- Insert Test Data for Repair, Scrap, Inventory
USE `it_asset_manage`;

-- Insert Repair Records
INSERT INTO `asset_repair_record` (`asset_id`, `apply_user_id`, `apply_user_name`, `apply_department`, `repair_reason`, `repair_status`, `remark`) VALUES
(1, 1, 'Admin', 'IT Dept', 'Server fan failure', 2, 'Fan replaced'),
(2, 1, 'Admin', 'Finance Dept', 'Desktop cannot boot', 1, 'Checking power supply'),
(5, 1, 'Admin', 'Admin Dept', 'Printer paper jam', 0, 'Pending repair');

-- Insert Scrap Records
INSERT INTO `asset_scrap_record` (`asset_id`, `apply_user_id`, `apply_user_name`, `apply_department`, `scrap_reason`, `scrap_type`, `original_price`, `residual_value`, `approve_status`, `remark`) VALUES
(4, 1, 'Admin', 'IT Dept', 'Equipment aging', 0, 3200.00, 100.00, 1, 'Approved'),
(3, 1, 'Admin', 'IT Dept', 'Screen damaged', 1, 14999.00, 500.00, 0, 'Pending approval');

-- Insert Inventory Tasks
INSERT INTO `asset_inventory` (`inventory_no`, `inventory_name`, `inventory_range`, `inventory_date`, `operator_id`, `operator_name`, `status`, `remark`) VALUES
('PD202603210001', 'Q1 2026 Inventory', 0, '2026-03-21 10:00:00', 1, 'Admin', 2, 'Completed'),
('PD202603210002', 'IT Dept Special Inventory', 1, '2026-03-21 14:00:00', 1, 'Admin', 0, 'Pending');

-- Insert Inventory Details
INSERT INTO `asset_inventory_detail` (`inventory_id`, `asset_id`, `asset_code`, `asset_name`, `department`, `user_name`, `status`, `result_remark`) VALUES
(1, 1, 'ZC20240001', 'Dell PowerEdge R750', 'IT Dept', 'Zhao', 1, 'Normal'),
(1, 2, 'ZC20240002', 'Lenovo ThinkCentre', 'Finance Dept', 'Wang', 1, 'Normal'),
(2, 1, 'ZC20240001', 'Dell PowerEdge R750', 'IT Dept', 'Zhao', 0, 'Pending check');
