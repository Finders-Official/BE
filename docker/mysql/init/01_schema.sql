-- ============================================
-- Finders Database Schema
-- ============================================
--
-- GCP Cloud SQL에서 추출한 테이블 스키마입니다.
-- 로컬 Docker 환경에서 테이블 생성용으로 사용됩니다.
--
-- [주의]
-- - 이 파일은 docker-entrypoint-initdb.d에서 자동 실행됩니다.
-- - 02_mock_data.sql보다 먼저 실행되어야 합니다 (알파벳 순서)
-- - GCP 배포 환경에서는 JPA가 테이블을 생성하므로 이 파일이 필요 없습니다.
--
-- 생성일: 2026-01-17
-- ============================================

-- MySQL dump 10.13  Distrib 8.0.41, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: finders
-- ------------------------------------------------------
-- Server version	8.0.41-google

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `finders`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `finders` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `finders`;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `content` varchar(1000) NOT NULL,
  `status` enum('ACTIVE','DELETED','HIDDEN') NOT NULL,
  `post_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs1slvnkuemjsq2kj4h3vhx7i1` (`post_id`),
  CONSTRAINT `FKs1slvnkuemjsq2kj4h3vhx7i1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--


--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `content` varchar(1000) NOT NULL,
  `status` enum('ACTIVE','DELETED','HIDDEN') NOT NULL,
  `member_user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnwq2h2smgtdsd684xdh21ibqu` (`member_user_id`),
  KEY `FKbqnvawwwv4gtlctsi3o7vs131` (`post_id`),
  CONSTRAINT `FKbqnvawwwv4gtlctsi3o7vs131` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `FKnwq2h2smgtdsd684xdh21ibqu` FOREIGN KEY (`member_user_id`) REFERENCES `member_user` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--


--
-- Table structure for table `development_order`
--

DROP TABLE IF EXISTS `development_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `development_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `order_code` varchar(20) NOT NULL,
  `status` enum('COMPLETED','DEVELOPING','RECEIVED','SCANNING') NOT NULL,
  `total_photos` int NOT NULL,
  `total_price` int NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  `reservation_id` bigint DEFAULT NULL,
  `is_develop` bit(1) NOT NULL,
  `is_print` bit(1) NOT NULL,
  `is_scan` bit(1) NOT NULL,
  `roll_count` int NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dev_order_code` (`order_code`),
  UNIQUE KEY `UKro63982v5r7qwph4plqbd3521` (`reservation_id`),
  KEY `idx_dev_order_lab` (`photo_lab_id`,`status`),
  KEY `idx_dev_order_member` (`member_id`,`status`),
  CONSTRAINT `FKdeljnoaojapq2kqvfru3cvrrk` FOREIGN KEY (`member_id`) REFERENCES `member_user` (`member_id`),
  CONSTRAINT `FKjpt205tpkl1max5i77k1oreak` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`),
  CONSTRAINT `FKsw8v81rwdu5829kwukwb0tnap` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `development_order`
--


--
-- Table structure for table `favorite_photo_lab`
--

DROP TABLE IF EXISTS `favorite_photo_lab`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_photo_lab` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `member_id` bigint NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_member_lab` (`member_id`,`photo_lab_id`),
  KEY `idx_favorite_member` (`member_id`),
  KEY `idx_favorite_lab` (`photo_lab_id`),
  CONSTRAINT `FK6eym4esyu80jf6c56bbw34p75` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`),
  CONSTRAINT `FKpbj1itfbdo0l2hdd30hwbl1dy` FOREIGN KEY (`member_id`) REFERENCES `member_user` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite_photo_lab`
--


--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `role` varchar(20) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `name` varchar(20) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `refresh_token_hash` varchar(500) DEFAULT NULL,
  `status` enum('ACTIVE','SUSPENDED','WITHDRAWN') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_member_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=300 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--


--
-- Table structure for table `member_address`
--

DROP TABLE IF EXISTS `member_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `address` varchar(200) NOT NULL,
  `address_detail` varchar(100) DEFAULT NULL,
  `address_name` varchar(50) NOT NULL,
  `is_default` bit(1) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `recipient_name` varchar(50) DEFAULT NULL,
  `zipcode` varchar(10) NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_address_member` (`member_id`,`is_default`),
  CONSTRAINT `FKeslc8586cwl3ej73mv7gr83x2` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_address`
--


--
-- Table structure for table `member_admin`
--

DROP TABLE IF EXISTS `member_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_admin` (
  `password_hash` varchar(255) NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`member_id`),
  CONSTRAINT `FKao3w17do4v0ytsbu2ns34cg3v` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_admin`
--


--
-- Table structure for table `member_agreement`
--

DROP TABLE IF EXISTS `member_agreement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_agreement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `agreed_at` datetime(6) NOT NULL,
  `is_agreed` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `terms_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_agreement_member` (`member_id`),
  KEY `idx_agreement_terms` (`terms_id`),
  CONSTRAINT `FKbecrju0ypoymb076j3p7ivrx8` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `FKdc0j5ybioli1y6i1rjsbsv7vp` FOREIGN KEY (`terms_id`) REFERENCES `terms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_agreement`
--


--
-- Table structure for table `member_owner`
--

DROP TABLE IF EXISTS `member_owner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_owner` (
  `bank_account_holder` varchar(50) DEFAULT NULL,
  `bank_account_number` varchar(50) DEFAULT NULL,
  `bank_name` varchar(50) DEFAULT NULL,
  `business_number` varchar(20) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`member_id`),
  CONSTRAINT `FK3idgpkg3nc1na53i6n5yi25tt` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_owner`
--


--
-- Table structure for table `member_user`
--

DROP TABLE IF EXISTS `member_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_user` (
  `last_token_refresh_at` datetime(6) DEFAULT NULL,
  `nickname` varchar(20) NOT NULL,
  `profile_image` varchar(500) DEFAULT NULL,
  `token_balance` int NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_member_user_nickname` (`nickname`),
  CONSTRAINT `FKt6m4nili9m3pq5jeoc5h3xj2s` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_user`
--


--
-- Table structure for table `photo_lab`
--

DROP TABLE IF EXISTS `photo_lab`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_lab` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `address` varchar(200) NOT NULL,
  `address_detail` varchar(100) DEFAULT NULL,
  `avg_work_time` int DEFAULT NULL,
  `description` text,
  `is_delivery_available` bit(1) NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `max_reservations_per_hour` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `post_count` int NOT NULL,
  `qr_code_url` varchar(500) DEFAULT NULL,
  `reservation_count` int NOT NULL,
  `status` enum('ACTIVE','CLOSED','PENDING','SUSPENDED') NOT NULL,
  `work_count` int NOT NULL,
  `zipcode` varchar(10) DEFAULT NULL,
  `owner_id` bigint NOT NULL,
  `region_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lab_status` (`status`),
  KEY `idx_lab_location` (`latitude`,`longitude`),
  KEY `FKnn388colks6mj2l7ttvmpv8el` (`owner_id`),
  KEY `FKpa7kfuhikaa4uy2qa7eovbcnp` (`region_id`),
  CONSTRAINT `FKnn388colks6mj2l7ttvmpv8el` FOREIGN KEY (`owner_id`) REFERENCES `member_owner` (`member_id`),
  CONSTRAINT `FKpa7kfuhikaa4uy2qa7eovbcnp` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_lab`
--


--
-- Table structure for table `photo_lab_business_hour`
--

DROP TABLE IF EXISTS `photo_lab_business_hour`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_lab_business_hour` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `close_time` time(6) DEFAULT NULL,
  `day_of_week` enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
  `is_closed` bit(1) NOT NULL,
  `open_time` time(6) DEFAULT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_hour` (`photo_lab_id`,`day_of_week`),
  CONSTRAINT `FK59b7c4rwic0yopd3nilskf6y1` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_lab_business_hour`
--


--
-- Table structure for table `photo_lab_document`
--

DROP TABLE IF EXISTS `photo_lab_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_lab_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `document_type` enum('BUSINESS_LICENSE','BUSINESS_PERMIT') NOT NULL,
  `file_name` varchar(200) DEFAULT NULL,
  `file_url` varchar(500) NOT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lab_doc` (`photo_lab_id`,`document_type`),
  CONSTRAINT `FKlb205xn7kh1djg4cun5qh4tgu` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_lab_document`
--


--
-- Table structure for table `photo_lab_image`
--

DROP TABLE IF EXISTS `photo_lab_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_lab_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `display_order` int NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `is_main` bit(1) NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lab_image` (`photo_lab_id`,`is_main`),
  CONSTRAINT `FKsx8067nb921h5qryc2sv7ukht` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_lab_image`
--


--
-- Table structure for table `photo_lab_keyword`
--

DROP TABLE IF EXISTS `photo_lab_keyword`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_lab_keyword` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `keyword` varchar(50) NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lab_keyword` (`photo_lab_id`,`keyword`),
  CONSTRAINT `FK5i0dnam6ds66cdgjnyirysy59` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_lab_keyword`
--


--
-- Table structure for table `photo_lab_notice`
--

DROP TABLE IF EXISTS `photo_lab_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_lab_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `content` text NOT NULL,
  `end_date` date DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `notice_type` enum('EVENT','GENERAL','POLICY') NOT NULL,
  `start_date` date DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lab_notice` (`photo_lab_id`),
  CONSTRAINT `FK2bcysxfxowlf7pjq0pdts8s59` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_lab_notice`
--


--
-- Table structure for table `photo_restoration`
--

DROP TABLE IF EXISTS `photo_restoration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_restoration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  `feedback_comment` varchar(500) DEFAULT NULL,
  `feedback_rating` enum('BAD','GOOD') DEFAULT NULL,
  `mask_url` varchar(500) NOT NULL,
  `member_id` bigint NOT NULL,
  `original_url` varchar(500) NOT NULL,
  `replicate_prediction_id` varchar(100) DEFAULT NULL,
  `restored_url` varchar(500) DEFAULT NULL,
  `status` enum('COMPLETED','FAILED','PENDING','PROCESSING') NOT NULL,
  `token_used` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_restoration_member` (`member_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_restoration`
--


--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `comment_count` int NOT NULL,
  `content` text NOT NULL,
  `is_self_developed` bit(1) NOT NULL,
  `lab_review` varchar(300) DEFAULT NULL,
  `like_count` int NOT NULL,
  `status` enum('ACTIVE','DELETED','HIDDEN') NOT NULL,
  `title` varchar(30) NOT NULL,
  `member_user_id` bigint NOT NULL,
  `photo_lab_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK580lhdrbg7p73ukg8j584j3h4` (`member_user_id`),
  KEY `FK4q17aihpws2be5i4oyrrsio32` (`photo_lab_id`),
  CONSTRAINT `FK4q17aihpws2be5i4oyrrsio32` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`),
  CONSTRAINT `FK580lhdrbg7p73ukg8j584j3h4` FOREIGN KEY (`member_user_id`) REFERENCES `member_user` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--


--
-- Table structure for table `post_image`
--

DROP TABLE IF EXISTS `post_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `display_order` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `image_url` varchar(500) NOT NULL,
  `width` int DEFAULT NULL,
  `post_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsip7qv57jw2fw50g97t16nrjr` (`post_id`),
  CONSTRAINT `FKsip7qv57jw2fw50g97t16nrjr` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_image`
--


--
-- Table structure for table `post_like`
--

DROP TABLE IF EXISTS `post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `member_user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj7iy0k7n3d0vkh8o7ibjna884` (`post_id`),
  KEY `FKhvqmiy40yfpwa8hvwliv358qc` (`member_user_id`),
  CONSTRAINT `FKhvqmiy40yfpwa8hvwliv358qc` FOREIGN KEY (`member_user_id`) REFERENCES `member_user` (`member_id`),
  CONSTRAINT `FKj7iy0k7n3d0vkh8o7ibjna884` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_like`
--


--
-- Table structure for table `print_order`
--

DROP TABLE IF EXISTS `print_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `print_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `estimated_at` datetime(6) DEFAULT NULL,
  `order_code` varchar(20) NOT NULL,
  `receipt_method` enum('DELIVERY','PICKUP') NOT NULL,
  `status` enum('COMPLETED','CONFIRMED','PENDING','PRINTING','READY','SHIPPED') NOT NULL,
  `total_price` int NOT NULL,
  `dev_order_id` bigint DEFAULT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_print_order_code` (`order_code`),
  KEY `idx_print_order_lab` (`photo_lab_id`,`status`),
  KEY `FKij1jl0qdw52wwjuc8b8146awg` (`dev_order_id`),
  CONSTRAINT `FKeefhb99lsto5biocu1afnje20` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`),
  CONSTRAINT `FKij1jl0qdw52wwjuc8b8146awg` FOREIGN KEY (`dev_order_id`) REFERENCES `development_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `print_order`
--


--
-- Table structure for table `region`
--

DROP TABLE IF EXISTS `region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `sigungu` varchar(50) NOT NULL,
  `sido` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_sigungu_sido` (`sigungu`,`sido`),
  KEY `idx_region_sido` (`sido`),
  CONSTRAINT `FKaa87jbhbcc6pkkmsmtf0amome` FOREIGN KEY (`sido`) REFERENCES `region` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `region`
--


--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_develop` bit(1) NOT NULL,
  `is_print` bit(1) NOT NULL,
  `is_scan` bit(1) NOT NULL,
  `request_message` varchar(500) DEFAULT NULL,
  `roll_count` int NOT NULL,
  `status` enum('CANCELED','COMPLETED','RESERVED') NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  `slot_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_lab_status` (`photo_lab_id`,`status`),
  KEY `FK10txn5okljgqcfojl7g3qf0bh` (`slot_id`),
  KEY `FKakdstaxbbt91v021v0sfg093l` (`member_id`),
  CONSTRAINT `FK10txn5okljgqcfojl7g3qf0bh` FOREIGN KEY (`slot_id`) REFERENCES `reservation_slot` (`id`),
  CONSTRAINT `FKakdstaxbbt91v021v0sfg093l` FOREIGN KEY (`member_id`) REFERENCES `member_user` (`member_id`),
  CONSTRAINT `FKe1orjthnkdedhmkt1th0ffo6g` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--


--
-- Table structure for table `reservation_slot`
--

DROP TABLE IF EXISTS `reservation_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `max_capacity` int NOT NULL,
  `reservation_date` date NOT NULL,
  `reservation_time` time(6) NOT NULL,
  `reserved_count` int NOT NULL,
  `photo_lab_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slot_lab_date_time` (`photo_lab_id`,`reservation_date`,`reservation_time`),
  CONSTRAINT `FKc2b3d494kehhq3nep26se7rrm` FOREIGN KEY (`photo_lab_id`) REFERENCES `photo_lab` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation_slot`
--


--
-- Table structure for table `scanned_photo`
--

DROP TABLE IF EXISTS `scanned_photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scanned_photo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `display_order` int NOT NULL,
  `file_name` varchar(200) DEFAULT NULL,
  `image_key` varchar(500) NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_scanned_order` (`order_id`),
  CONSTRAINT `FKq18ukbe3bmh181y27jh7o37by` FOREIGN KEY (`order_id`) REFERENCES `development_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scanned_photo`
--


--
-- Table structure for table `social_account`
--

DROP TABLE IF EXISTS `social_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `social_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `provider` enum('APPLE','KAKAO') NOT NULL,
  `provider_id` varchar(100) NOT NULL,
  `social_email` varchar(100) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_social_provider` (`provider`,`provider_id`),
  KEY `idx_social_member` (`member_id`),
  CONSTRAINT `FKomk939bj6nrsy3e2t2mgs93nx` FOREIGN KEY (`member_id`) REFERENCES `member_user` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `social_account`
--


--
-- Table structure for table `terms`
--

DROP TABLE IF EXISTS `terms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `terms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `content` tinytext NOT NULL,
  `effective_date` date NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_required` bit(1) NOT NULL,
  `title` varchar(200) NOT NULL,
  `type` enum('LOCATION','NOTIFICATION','PRIVACY','SERVICE') NOT NULL,
  `version` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_terms_version` (`type`,`version`),
  KEY `idx_terms_active` (`type`,`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `terms`
--


--
-- Table structure for table `token_history`
--

DROP TABLE IF EXISTS `token_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `amount` int NOT NULL,
  `balance_after` int NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `related_type` enum('PAYMENT','PHOTO_RESTORATION') DEFAULT NULL,
  `type` enum('PURCHASE','REFRESH','REFUND','SIGNUP_BONUS','USE') NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_token_history_member` (`member_id`,`created_at` DESC),
  KEY `idx_token_history_type` (`type`),
  CONSTRAINT `FK7cn8e2g269xixjd0fihmj3002` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `token_history`
--

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-16 15:34:47
