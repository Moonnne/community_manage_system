/*
 Navicat Premium Data Transfer

 Source Server         : ECS MySQL
 Source Server Type    : MySQL
 Source Server Version : 80408
 Source Host           : 123.57.220.167:3636
 Source Schema         : cms

 Target Server Type    : MySQL
 Target Server Version : 80408
 File Encoding         : 65001

 Date: 02/06/2026 10:46:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for asset
-- ----------------------------
DROP TABLE IF EXISTS `asset`;
CREATE TABLE `asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资产标识ID',
  `asset_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资产业务编码',
  `asset_owner_id` bigint NULL DEFAULT NULL COMMENT '资产所有人ID',
  `asset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资产名称',
  `asset_type` int NOT NULL COMMENT '资产类型',
  `asset_reg_time` datetime NULL DEFAULT NULL COMMENT '资产登记时间',
  `asset_due_time` datetime NULL DEFAULT NULL COMMENT '资产过期时间',
  `asset_status` int NULL DEFAULT NULL COMMENT '资产状态\r\n0 - 正常\r\n1 - 过期\r\n2 - 未使用\r\n3 - 冻结',
  `asset_area` decimal(10, 2) NULL DEFAULT NULL COMMENT '资产面积',
  `asset_building` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资产所在建筑编号',
  `asset_floor` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资产所在楼层',
  `asset_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资产相对定位',
  `asset_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资产备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ASSET_TYPE_KF`(`asset_type` ASC) USING BTREE,
  INDEX `ASSET_OWNER_ID_FK`(`asset_owner_id` ASC) USING BTREE,
  CONSTRAINT `ASSET_OWNER_ID_FK` FOREIGN KEY (`asset_owner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ASSET_TYPE_KF` FOREIGN KEY (`asset_type`) REFERENCES `asset_type` (`asset_type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asset
-- ----------------------------
INSERT INTO `asset` VALUES (1, 'B0001', 1, '物业办公室', 1, '2026-05-31 13:10:54', '2099-12-31 13:10:56', 1, NULL, '1', '1', NULL, NULL);
INSERT INTO `asset` VALUES (2, 'P0001', 1, '物业办公楼前车位-1', 2, '2026-05-31 13:15:12', '2026-05-31 13:16:40', 0, NULL, '1', '1', '1-1', NULL);
INSERT INTO `asset` VALUES (3, 'P0002', 1, '物业办公楼前车位-2', 2, '2026-05-31 13:16:21', '2026-05-31 13:16:44', 0, NULL, '1', '1', '1-2', NULL);

-- ----------------------------
-- Table structure for asset_type
-- ----------------------------
DROP TABLE IF EXISTS `asset_type`;
CREATE TABLE `asset_type`  (
  `asset_type_id` int NOT NULL COMMENT '资产类型ID',
  `asset_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资产类型名称',
  PRIMARY KEY (`asset_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of asset_type
-- ----------------------------
INSERT INTO `asset_type` VALUES (1, '房产');
INSERT INTO `asset_type` VALUES (2, '停车位');

-- ----------------------------
-- Table structure for bill
-- ----------------------------
DROP TABLE IF EXISTS `bill`;
CREATE TABLE `bill`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账单标识ID',
  `bill_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单业务编码',
  `owner_id` bigint NOT NULL COMMENT '账单所有人ID',
  `creater_id` bigint NOT NULL COMMENT '账单创建人ID',
  `asset_id` bigint NOT NULL COMMENT '账单归属资产ID',
  `bill_create_time` datetime NULL DEFAULT NULL COMMENT '账单创建时间',
  `bill_start_time` datetime NULL DEFAULT NULL COMMENT '账单起始时间',
  `bill_end_time` datetime NULL DEFAULT NULL COMMENT '账单截止时间',
  `bill_finish_time` datetime NULL DEFAULT NULL COMMENT '账单完成时间',
  `bill_status` int NULL DEFAULT NULL COMMENT '账单状态\r\n0 - 已完成\r\n1 - 未完成\r\n2 - 部分完成\r\n3 - 已逾期\r\n4 - 已补缴\r\n5 - 已取消',
  `bill_total` decimal(10, 2) NOT NULL COMMENT '账单应收总额',
  `bill_paid` decimal(10, 2) NOT NULL COMMENT '账单实收总额',
  `bill_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `OWNER_ID_FK`(`owner_id` ASC) USING BTREE,
  INDEX `CREATER_ID_FK`(`creater_id` ASC) USING BTREE,
  INDEX `ASSET_ID_FK`(`asset_id` ASC) USING BTREE,
  CONSTRAINT `ASSET_ID_FK` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `CREATER_ID_FK` FOREIGN KEY (`creater_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `OWNER_ID_FK` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bill
-- ----------------------------
INSERT INTO `bill` VALUES (1, 'B00000001', 2, 1, 1, '2026-05-31 18:18:39', '2026-05-31 18:18:41', '2026-05-31 18:18:46', '2026-05-31 19:15:16', 1, 0.00, 0.00, NULL);

-- ----------------------------
-- Table structure for bill_detail
-- ----------------------------
DROP TABLE IF EXISTS `bill_detail`;
CREATE TABLE `bill_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账目标识ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID',
  `bill_detail_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账目名',
  `bill_detail_type` int NULL DEFAULT NULL COMMENT '账目类型',
  `bill_detail_quantity` int NOT NULL COMMENT '账目数量',
  `bill_detail_unit_price` decimal(10, 2) NOT NULL COMMENT '账目单价',
  `bill_detail_total` decimal(10, 2) NOT NULL COMMENT '账目金额',
  `bill_detail_status` int NOT NULL DEFAULT 1 COMMENT '账目状态 0-已结清 1-未结清 2-部分结清',
  `bill_detail_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账目备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `BILL_DETAIL_TYPE_FK`(`bill_detail_type` ASC) USING BTREE,
  INDEX `BILL_ID_FK`(`bill_id` ASC) USING BTREE,
  CONSTRAINT `BILL_DETAIL_TYPE_FK` FOREIGN KEY (`bill_detail_type`) REFERENCES `bill_type` (`bill_type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `BILL_ID_FK` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bill_detail
-- ----------------------------

-- ----------------------------
-- Table structure for bill_type
-- ----------------------------
DROP TABLE IF EXISTS `bill_type`;
CREATE TABLE `bill_type`  (
  `bill_type_id` int NOT NULL COMMENT '账目类型ID',
  `bill_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账目类型名称',
  PRIMARY KEY (`bill_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bill_type
-- ----------------------------
INSERT INTO `bill_type` VALUES (1, '物业费');
INSERT INTO `bill_type` VALUES (2, '采暖费');
INSERT INTO `bill_type` VALUES (3, '水费');
INSERT INTO `bill_type` VALUES (4, '电费');
INSERT INTO `bill_type` VALUES (5, '燃气费');
INSERT INTO `bill_type` VALUES (6, '公共设施维修');
INSERT INTO `bill_type` VALUES (7, '社区事务');
INSERT INTO `bill_type` VALUES (8, '车位租金');

-- ----------------------------
-- Table structure for car
-- ----------------------------
DROP TABLE IF EXISTS `car`;
CREATE TABLE `car`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆标识ID',
  `car_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车辆业务代码',
  `owner_id` bigint NULL DEFAULT NULL COMMENT '车辆所有人ID',
  `asset_id` bigint NULL DEFAULT NULL COMMENT '车辆所属车位ID',
  `visitor_id` bigint NULL DEFAULT NULL COMMENT '访客ID',
  `car_number` int NOT NULL COMMENT '车辆牌照号',
  `car_type` int NOT NULL COMMENT '车辆类型',
  `car_status` int NOT NULL COMMENT '车辆状态\r\n0 - 已入场\r\n1 - 已离场',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `CAR_OWNER_ID_FK`(`owner_id` ASC) USING BTREE,
  INDEX `CAR_ASSET_ID_FK`(`asset_id` ASC) USING BTREE,
  INDEX `CAR_VISITOR_FK`(`visitor_id` ASC) USING BTREE,
  INDEX `CAR_TYPE`(`car_type` ASC) USING BTREE,
  CONSTRAINT `CAR_ASSET_ID_FK` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `CAR_OWNER_ID_FK` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `CAR_TYPE` FOREIGN KEY (`car_type`) REFERENCES `car_type` (`car_type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `CAR_VISITOR_FK` FOREIGN KEY (`visitor_id`) REFERENCES `visitor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of car
-- ----------------------------

-- ----------------------------
-- Table structure for car_history
-- ----------------------------
DROP TABLE IF EXISTS `car_history`;
CREATE TABLE `car_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆出入历史标识ID',
  `car_history_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车辆出入历史业务代码',
  `car_history_operator_id` bigint NULL DEFAULT NULL COMMENT '车辆出入经办人ID',
  `car_id` bigint NOT NULL COMMENT '车辆ID',
  `gate_id` int NULL DEFAULT NULL COMMENT '大门ID',
  `car_history_type` int NOT NULL COMMENT '车辆出入类型\r\n0 - 入场\r\n1 - 离场\r\n2 - 访客入场\r\n3 - 访客离场',
  `car_history_time` datetime NULL DEFAULT NULL COMMENT '车辆出入时间',
  `car_history_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车辆出入备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `CAR_HISTORY_OPERATOR_FK`(`car_history_operator_id` ASC) USING BTREE,
  INDEX `CAR_HISTORY_CAR_ID_FK`(`car_id` ASC) USING BTREE,
  INDEX `GATE_ID_FK`(`gate_id` ASC) USING BTREE,
  CONSTRAINT `CAR_HISTORY_CAR_ID_FK` FOREIGN KEY (`car_id`) REFERENCES `car` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `CAR_HISTORY_OPERATOR_FK` FOREIGN KEY (`car_history_operator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `GATE_ID_FK` FOREIGN KEY (`gate_id`) REFERENCES `gate` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of car_history
-- ----------------------------

-- ----------------------------
-- Table structure for car_type
-- ----------------------------
DROP TABLE IF EXISTS `car_type`;
CREATE TABLE `car_type`  (
  `car_type_id` int NOT NULL COMMENT '车辆类型ID',
  `car_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车辆类型名',
  `car_type_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车辆类型备注',
  PRIMARY KEY (`car_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of car_type
-- ----------------------------
INSERT INTO `car_type` VALUES (1, '业主车辆', '小区内业主的已注册车辆');
INSERT INTO `car_type` VALUES (2, '访客车辆', '访客进入小区的临时停放车辆');
INSERT INTO `car_type` VALUES (3, '服务车辆', '快递运输/搬家公司等服务型车辆');
INSERT INTO `car_type` VALUES (4, '特种车辆', '公共服务车辆');

-- ----------------------------
-- Table structure for facility
-- ----------------------------
DROP TABLE IF EXISTS `facility`;
CREATE TABLE `facility`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设施标识ID',
  `facility_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设施业务代码',
  `facility_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施名称',
  `facility_chief_id` bigint NULL DEFAULT NULL COMMENT '设施负责人ID',
  `facility_type` int NOT NULL COMMENT '设施类型',
  `facility_status` int NOT NULL COMMENT '设备状态\r\n0 - 正常\r\n1 - 故障\r\n2 - 损坏\r\n3 - 缺失',
  `facility_check_time` datetime NULL DEFAULT NULL COMMENT '设备检修时间',
  `facility_expiration_date` int NULL DEFAULT NULL COMMENT '设备保质期 [月]',
  `facility_building` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备所在建筑编号',
  `facility_floor` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备所在楼层',
  `facility_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施相对定位',
  `facility_owner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施所有主体',
  `facility_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FACILITY_CHIEF_ID_FK`(`facility_chief_id` ASC) USING BTREE,
  INDEX `FACILITY_TYPE_FK`(`facility_type` ASC) USING BTREE,
  CONSTRAINT `FACILITY_CHIEF_ID_FK` FOREIGN KEY (`facility_chief_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FACILITY_TYPE_FK` FOREIGN KEY (`facility_type`) REFERENCES `facility_type` (`facility_type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of facility
-- ----------------------------

-- ----------------------------
-- Table structure for facility_type
-- ----------------------------
DROP TABLE IF EXISTS `facility_type`;
CREATE TABLE `facility_type`  (
  `facility_type_id` int NOT NULL COMMENT '设施类型ID',
  `facility_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施类型名',
  `facility_type_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施类型备注',
  PRIMARY KEY (`facility_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of facility_type
-- ----------------------------
INSERT INTO `facility_type` VALUES (1, '变电器', '电力设施');
INSERT INTO `facility_type` VALUES (2, '水泵', '供水设施');
INSERT INTO `facility_type` VALUES (3, '供热泵', '供热设施');
INSERT INTO `facility_type` VALUES (4, '弱电机房', '通讯设施');
INSERT INTO `facility_type` VALUES (5, '通讯基站', '通讯设施');
INSERT INTO `facility_type` VALUES (6, '电梯', '公共设施');
INSERT INTO `facility_type` VALUES (7, '摄像头', '安防设施');
INSERT INTO `facility_type` VALUES (8, '电子门禁', '安防设施');

-- ----------------------------
-- Table structure for gate
-- ----------------------------
DROP TABLE IF EXISTS `gate`;
CREATE TABLE `gate`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '大门标识ID',
  `gate_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '大门业务代码',
  `gate_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '大门名称',
  `gate_chief_id` bigint NULL DEFAULT NULL COMMENT '大门负责人ID',
  `gate_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '大门备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `GATE_CHIEF_ID_FK`(`gate_chief_id` ASC) USING BTREE,
  CONSTRAINT `GATE_CHIEF_ID_FK` FOREIGN KEY (`gate_chief_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gate
-- ----------------------------
INSERT INTO `gate` VALUES (1, 'N1', '北一门', 4, '北一门通用');
INSERT INTO `gate` VALUES (2, 'N2', '北二门', NULL, '北二门仅允许行人');
INSERT INTO `gate` VALUES (3, 'E1', '东门', NULL, '东门通用');

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单标识ID',
  `task_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单业务代码',
  `task_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单标题',
  `task_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工单信息',
  `owner_id` bigint NOT NULL COMMENT '工单所有人',
  `task_operator_id` bigint NULL DEFAULT NULL COMMENT '工单经办人',
  `task_status` int NOT NULL COMMENT '工单状态\r\n0 - 已完成\r\n1 - 待处理\r\n2 - 受理中\r\n3 - 已驳回\r\n4 - 已取消',
  `task_type` int NOT NULL COMMENT '工单类型',
  `task_create_time` datetime NULL DEFAULT NULL COMMENT '工单创建时间',
  `task_update_time` datetime NULL DEFAULT NULL COMMENT '工单更新时间',
  `task_finish_time` datetime NULL DEFAULT NULL COMMENT '工单完成时间',
  `task_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工单备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `TASK_OWNER_ID_FK`(`owner_id` ASC) USING BTREE,
  INDEX `TASK_PERATOR_ID_FK`(`task_operator_id` ASC) USING BTREE,
  INDEX `TASK_TYPE_FK`(`task_type` ASC) USING BTREE,
  CONSTRAINT `TASK_OWNER_ID_FK` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `TASK_PERATOR_ID_FK` FOREIGN KEY (`task_operator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `TASK_TYPE_FK` FOREIGN KEY (`task_type`) REFERENCES `task_type` (`task_type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task
-- ----------------------------

-- ----------------------------
-- Table structure for task_type
-- ----------------------------
DROP TABLE IF EXISTS `task_type`;
CREATE TABLE `task_type`  (
  `task_type_id` int NOT NULL COMMENT '工单类型ID',
  `task_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工单类型名称',
  `task_type_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工单类型备注',
  PRIMARY KEY (`task_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of task_type
-- ----------------------------
INSERT INTO `task_type` VALUES (1, '投诉/申诉', '物业管理人员违规或失职，以及物业处理不当');
INSERT INTO `task_type` VALUES (2, '报修', '公共设施损坏等维修申请');
INSERT INTO `task_type` VALUES (3, '意见建议', '对物业提出建设性意见');
INSERT INTO `task_type` VALUES (4, '退款', '缴费错误退款');
INSERT INTO `task_type` VALUES (5, '账户', '系统账户操作');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户标识ID',
  `uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户UID',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `user_birthday` date NULL DEFAULT NULL COMMENT '用户生日',
  `user_gender` int NULL DEFAULT NULL COMMENT '用户性别\r\n0 - 男\r\n1 - 女\r\n2 - 不愿透露',
  `user_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户电话',
  `user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户电子邮箱',
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码',
  `user_role` int NOT NULL COMMENT '用户角色',
  `user_status` int NOT NULL COMMENT '用户状态\r\n0 - 正常\r\n1 - 冻结\r\n2 - 禁用',
  `user_create_time` datetime NULL DEFAULT NULL COMMENT '用户创建时间',
  `user_avata` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
  `user_initialized` tinyint NOT NULL DEFAULT 0 COMMENT '是否完成账户初始化 0-否 1-是',
  `uid_locked` tinyint NOT NULL DEFAULT 0 COMMENT '账户是否锁定 0-可修改 1-不可修改',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `USER_ROLE_FK`(`user_role` ASC) USING BTREE,
  CONSTRAINT `USER_ROLE_FK` FOREIGN KEY (`user_role`) REFERENCES `user_role` (`role_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'TestAdmin', '2026-05-04', 1, '13811112222', '', '$2a$10$BHQSZwduJfrAapgQWiW9AOS9pIJ.lGZSdroe5wTU7lxPQTOOTh78m', 0, 0, '2026-05-04 17:52:08', 'https://oss.moonne.top/cms/avatas/a5a5e769-23d8-4c54-99cb-d87df8b31f34.png', 1, 1);
INSERT INTO `user` VALUES (2, 'user', 'TestUser', '2026-05-01', 1, '', '', '$2a$10$BHQSZwduJfrAapgQWiW9AOS9pIJ.lGZSdroe5wTU7lxPQTOOTh78m', 4, 0, '2026-05-05 09:52:27', 'https://oss.moonne.top/cms/avatas/bdb1aecb-0d4b-414e-ac3b-1cbe7c41efaa.png', 1, 1);
INSERT INTO `user` VALUES (3, 'manager', 'TestManger', '2026-03-05', NULL, NULL, NULL, '$2a$10$BHQSZwduJfrAapgQWiW9AOS9pIJ.lGZSdroe5wTU7lxPQTOOTh78m', 1, 0, '2026-05-05 09:54:11', NULL, 1, 1);
INSERT INTO `user` VALUES (4, 'employee', 'TestEmployee', NULL, 1, '', '', '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 2, 0, '2026-05-06 15:11:19', NULL, 1, 1);
INSERT INTO `user` VALUES (5, 't1', '测试1', '2026-05-06', NULL, NULL, NULL, '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 0, '2026-05-06 17:30:20', NULL, 1, 1);
INSERT INTO `user` VALUES (6, 't2', '测试2', '2026-05-06', NULL, NULL, NULL, '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 0, '2026-05-06 17:31:14', NULL, 1, 1);
INSERT INTO `user` VALUES (7, 't3', '测试3', '2026-05-06', NULL, NULL, NULL, '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 0, '2026-05-06 17:31:41', NULL, 1, 1);
INSERT INTO `user` VALUES (8, 't4', '测试4', '2026-05-06', NULL, NULL, NULL, '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 0, '2026-05-06 17:32:01', NULL, 1, 1);
INSERT INTO `user` VALUES (9, 't5', '测试5', '2026-05-06', NULL, NULL, NULL, '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 0, '2026-05-06 17:32:22', NULL, 1, 1);
INSERT INTO `user` VALUES (10, 't6', '测试6', '2026-05-06', NULL, NULL, NULL, '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 0, '2026-05-06 17:32:39', NULL, 1, 1);
INSERT INTO `user` VALUES (11, 't7', '测试7', '1990-05-30', 1, '123456', 'testuser@163.com', '$2a$10$7ZxQ2qYDxMftxJO/byk8feLe4oZ1LUezhCeVlLTPuU27e3xwCtKwG', 4, 1, '2026-05-06 17:33:03', NULL, 1, 1);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `role_id` int NOT NULL COMMENT '用户角色ID',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户角色名',
  `role_promission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色权限等级\r\n0 - 无权限\r\n1 - 操作权限\r\n2 - 管理权限\r\n3 - 系统权限',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (0, '系统管理员', '3');
INSERT INTO `user_role` VALUES (1, '物业经理', '2');
INSERT INTO `user_role` VALUES (2, '物业员工', '1');
INSERT INTO `user_role` VALUES (3, '安保人员', '1');
INSERT INTO `user_role` VALUES (4, '业主', '0');
INSERT INTO `user_role` VALUES (5, '租户', '0');

-- ----------------------------
-- Table structure for visitor
-- ----------------------------
DROP TABLE IF EXISTS `visitor`;
CREATE TABLE `visitor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访客标识ID',
  `visitor_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访客业务编码',
  `asset_id` bigint NULL DEFAULT NULL COMMENT '资产ID',
  `owner_id` bigint NULL DEFAULT NULL COMMENT '业主ID',
  `visitor_creater` bigint NOT NULL COMMENT '访问创建人ID',
  `visitor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访客电话',
  `visitor_type` int NOT NULL COMMENT '访客类型',
  `visitor_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访客状态\r\n0 - 已结束\r\n1 - 待批准\r\n2 - 访问中\r\n3 - 已驳回',
  `visitor_create_time` datetime NULL DEFAULT NULL COMMENT '访问起始时间',
  `visitor_finish_time` datetime NULL DEFAULT NULL COMMENT '访问结束时间',
  `visitor_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访客备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `VISITOR_ASSET_ID_FK`(`asset_id` ASC) USING BTREE,
  INDEX `VISITOR_OWNER_ID_FK`(`owner_id` ASC) USING BTREE,
  INDEX `VISITOR_CREATER_FK`(`visitor_creater` ASC) USING BTREE,
  INDEX `VISITOR_TYPE_FK`(`visitor_type` ASC) USING BTREE,
  CONSTRAINT `VISITOR_ASSET_ID_FK` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `VISITOR_CREATER_FK` FOREIGN KEY (`visitor_creater`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `VISITOR_OWNER_ID_FK` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `VISITOR_TYPE_FK` FOREIGN KEY (`visitor_type`) REFERENCES `visitor_type` (`visitor_type_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of visitor
-- ----------------------------

-- ----------------------------
-- Table structure for visitor_type
-- ----------------------------
DROP TABLE IF EXISTS `visitor_type`;
CREATE TABLE `visitor_type`  (
  `visitor_type_id` int NOT NULL COMMENT '访客类型ID',
  `visitor_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访客类型名',
  `visitor_type_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访客类型说明',
  PRIMARY KEY (`visitor_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of visitor_type
-- ----------------------------
INSERT INTO `visitor_type` VALUES (1, '亲友拜访', '业主亲属或朋友访问');
INSERT INTO `visitor_type` VALUES (2, '外卖配送', '外卖员');
INSERT INTO `visitor_type` VALUES (3, '快递物流', '快递员/物流公司');
INSERT INTO `visitor_type` VALUES (4, '家政维修', '装修公司/家政公司/维修公司');
INSERT INTO `visitor_type` VALUES (5, '公务人员', '社区工作人员/警务人员');

SET FOREIGN_KEY_CHECKS = 1;
