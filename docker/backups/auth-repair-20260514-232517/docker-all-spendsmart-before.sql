-- MySQL dump 10.13  Distrib 8.4.9, for Linux (x86_64)
--
-- Host: localhost    Database: spendsmart_auth_db
-- ------------------------------------------------------
-- Server version	8.4.9

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
-- Current Database: `spendsmart_auth_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_auth_db`;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `otp` varchar(255) NOT NULL,
  `used` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `monthly_budget` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `currency` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `timezone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (NULL,'2026-05-14 08:32:12.382908',1,'INR','anni@gmail.com','Anni Yadav','$2a$10$dzG7Xxx31Mfl6MymEyNScunZKuPeIipYV5nP9J1NijvZJOvdH5.KC','active','Asia/Kolkata'),(NULL,'2026-05-14 08:49:30.523133',3,'INR','virat@gmail.com','Virat Kohli','$2a$10$4iT.QaSHEE1R4Rp8OGn18utSniPPX7Dal07O4b7Easq1CL3XNlsGu','active','Asia/Kolkata'),(6000,'2026-05-14 09:46:15.876503',4,'INR','mishrayush0503@gmail.com','Ayush Mishra','$2a$10$/Ymw3U75pxQk745aalXw6e/3w81MLAZQuJsCe7Nqa4Fujl0BkgQfO','active','Asia/Kolkata'),(1300000,'2026-05-14 16:40:40.297753',5,'INR','anuragkushwahaji10k@gmail.com','Anurag ','$2a$10$ScjDu5M797Aisyx7nq2e5.zCThJlzNNt9GG6jMge.scBCpP1HdLy2','active','Asia/Kolkata'),(NULL,'2026-05-14 17:19:44.152876',6,'INR','rahul@gmail.com','Rahul','$2a$10$odiPOkpzg7ycq./aAeuJK.snAPxGyXosyJtQ7e/ifpV4tTpVG3KKm','active','Asia/Kolkata');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_budget_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_budget_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_budget_db`;

--
-- Table structure for table `budgets`
--

DROP TABLE IF EXISTS `budgets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `budgets` (
  `budget_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `month` int DEFAULT NULL,
  `monthly_limit` double DEFAULT NULL,
  `spent_amount` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `year` int DEFAULT NULL,
  `alert_threshold` int NOT NULL,
  `category_id` int NOT NULL,
  `limit_amount` decimal(12,2) NOT NULL,
  `name` varchar(255) NOT NULL,
  `period` enum('CUSTOM','MONTHLY','WEEKLY') NOT NULL,
  `limit_alert_sent` bit(1) DEFAULT NULL,
  `threshold_alert_sent` bit(1) DEFAULT NULL,
  PRIMARY KEY (`budget_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `budgets`
--

LOCK TABLES `budgets` WRITE;
/*!40000 ALTER TABLE `budgets` DISABLE KEYS */;
INSERT INTO `budgets` VALUES (1,'2026-04-24 10:36:12.518179','INR',_binary '',4,20000,0,NULL,1,2026,50,0,0.00,'','CUSTOM',NULL,NULL),(2,'2026-04-26 15:17:54.378991','INR',_binary '',4,10000,0,NULL,3,2026,50,0,0.00,'','CUSTOM',NULL,NULL),(3,'2026-04-28 15:49:16.393003','INR',_binary '',4,15000,0,NULL,16,2026,50,0,15000.00,'Monthly Budget','MONTHLY',NULL,NULL),(4,'2026-04-28 15:49:39.794858','INR',_binary '',4,10000,0,NULL,16,2026,50,0,10000.00,'Monthly Budget','MONTHLY',NULL,NULL),(5,'2026-04-28 16:31:50.637675','INR',_binary '',4,15000,5000,NULL,19,2026,50,0,15000.00,'Monthly Budget','MONTHLY',NULL,NULL),(6,'2026-04-28 16:37:33.036684','INR',_binary '',4,250000,70200,'2026-05-02 12:39:01.794524',6,2026,50,0,250000.00,'Monthly Budget','MONTHLY',NULL,NULL),(7,'2026-04-29 11:16:44.532315','INR',_binary '',4,10000,1000,NULL,20,2026,50,0,10000.00,'Monthly Budget','MONTHLY',NULL,NULL),(8,'2026-04-29 11:56:46.996645','INR',_binary '',4,10000,11000,'2026-04-29 11:59:10.512293',21,2026,50,0,10000.00,'Monthly Budget','MONTHLY',NULL,NULL),(9,'2026-04-29 15:07:11.354344','INR',_binary '',4,30000,20000,NULL,23,2026,50,0,30000.00,'Monthly Budget','MONTHLY',NULL,NULL),(10,'2026-04-29 16:34:09.351417','INR',_binary '',4,6000,3500,'2026-04-29 16:39:25.228668',25,2026,50,0,6000.00,'Monthly Budget','MONTHLY',NULL,NULL),(11,'2026-04-29 16:38:36.316760','INR',_binary '',4,6000,3500,NULL,25,2026,50,0,6000.00,'Monthly Budget','MONTHLY',NULL,NULL),(12,'2026-04-29 21:31:34.446347','INR',_binary '',4,15000,15799,'2026-04-29 21:44:16.751258',26,2026,50,0,15000.00,'Monthly Budget','MONTHLY',NULL,NULL),(13,'2026-04-30 16:53:25.016155','INR',_binary '',4,5000,5499,'2026-04-30 16:53:51.677845',29,2026,50,0,5000.00,'Monthly Budget','MONTHLY',NULL,NULL),(14,'2026-05-01 18:46:39.241944','INR',_binary '',5,35000,300,NULL,26,2026,50,0,35000.00,'Monthly Budget','MONTHLY',NULL,NULL),(15,'2026-05-01 22:49:26.855797','INR',_binary '',5,25000,20000,'2026-05-01 23:03:57.493891',30,2026,50,0,25000.00,'Monthly Budget','MONTHLY',NULL,NULL),(16,'2026-05-01 23:08:01.550313','INR',_binary '',5,500,700,'2026-05-01 23:09:53.140446',31,2026,50,0,500.00,'Monthly Budget','MONTHLY',NULL,NULL),(17,'2026-05-02 10:49:52.535352','USD',_binary '',5,50000,51000,'2026-05-02 11:23:23.465744',32,2026,50,0,50000.00,'Monthly Budget','MONTHLY',NULL,NULL),(18,'2026-05-02 11:10:38.062038','INR',_binary '',5,1300000,645000,'2026-05-14 16:47:47.378123',5,2026,50,0,1300000.00,'Monthly Budget','MONTHLY',_binary '\0',_binary '\0'),(19,'2026-05-03 13:05:42.023178','USD',_binary '',5,99999,83499,'2026-05-03 13:35:29.773775',33,2026,50,0,99999.00,'Monthly Budget','MONTHLY',_binary '\0',_binary ''),(20,'2026-05-04 16:29:44.208827','INR',_binary '',5,15000,7999,'2026-05-04 16:33:39.038928',34,2026,50,0,15000.00,'Monthly Budget','MONTHLY',_binary '\0',_binary '\0'),(21,'2026-05-08 11:40:22.078228','INR',_binary '',5,10000,11499,'2026-05-08 12:13:50.208288',36,2026,50,0,10000.00,'Monthly Budget','MONTHLY',_binary '',_binary '\0'),(22,'2026-05-11 14:59:51.921986','INR',_binary '',5,15000,6000,NULL,38,2026,50,0,15000.00,'Monthly Budget','MONTHLY',_binary '\0',_binary '\0'),(23,'2026-05-13 19:28:58.478782','INR',_binary '',5,6000,2500,'2026-05-13 19:28:59.939604',39,2026,50,0,6000.00,'Monthly Budget','MONTHLY',_binary '\0',_binary '\0'),(24,'2026-05-14 09:50:23.957091','INR',_binary '',5,6000,8049,'2026-05-14 09:57:15.437846',4,2026,50,0,6000.00,'Monthly Budget','MONTHLY',_binary '',_binary '\0');
/*!40000 ALTER TABLE `budgets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_category_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_category_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_category_db`;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `is_default` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `type` enum('EXPENSE','INCOME') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `budget_limit` decimal(12,2) DEFAULT NULL,
  `color_code` varchar(7) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'#FF5733','2026-04-24 00:00:00.000000','??',_binary '',_binary '\0','Food','EXPENSE',NULL,1,NULL,NULL),(2,'#1f7a5c','2026-04-26 00:00:00.000000','?',_binary '',_binary '\0','Travelling','EXPENSE',NULL,3,NULL,NULL),(3,'#1f7a5c','2026-04-26 00:00:00.000000','5000',_binary '',_binary '\0','rapido','INCOME',NULL,3,NULL,NULL),(4,'#2dbe8e','2026-04-28 15:15:25.430110','.',_binary '',_binary '\0','Travelling','EXPENSE',NULL,16,NULL,NULL),(5,'#df5911','2026-04-28 15:15:57.059802','?',_binary '',_binary '\0','3000','INCOME',NULL,16,NULL,NULL),(6,'#8d9a96','2026-04-28 15:19:44.139809','?',_binary '',_binary '\0','Party','EXPENSE',NULL,16,NULL,NULL),(7,'#76d5b6','2026-04-29 11:16:06.485462','?',_binary '',_binary '\0','Travelling','EXPENSE',NULL,20,NULL,NULL),(8,'#ed0c23','2026-04-29 11:16:27.980538','?',_binary '',_binary '\0','Rapido','INCOME',NULL,20,NULL,NULL),(9,'#f51414','2026-04-29 11:58:17.672282','?',_binary '',_binary '\0','Food','EXPENSE',NULL,21,NULL,NULL),(10,'#9e9e9e','2026-04-29 11:58:43.279335','?',_binary '',_binary '\0','Social Media','INCOME',NULL,21,NULL,NULL),(11,'#1f7a5c','2026-04-29 12:10:07.699957','*',_binary '',_binary '\0','Breakfast','EXPENSE',NULL,21,NULL,NULL),(12,'#3df5b8','2026-04-29 12:38:10.179033','??',_binary '',_binary '\0','Accessories','EXPENSE',NULL,6,NULL,NULL),(13,'#f4ed1f','2026-04-29 12:40:37.870984','#',_binary '',_binary '\0','Insta','INCOME',NULL,6,NULL,NULL),(14,'#0af0a3','2026-04-29 15:06:37.171730','?',_binary '',_binary '\0','Cricket','EXPENSE',NULL,23,NULL,NULL),(15,'#06130f','2026-04-29 15:06:51.886230','?',_binary '',_binary '\0','Football','INCOME',NULL,23,NULL,NULL),(16,'#33e31c','2026-04-29 16:32:44.737717','?',_binary '',_binary '\0','Sleeper','EXPENSE','2026-04-29 16:33:00.922276',25,NULL,NULL),(17,'#d71431','2026-04-29 16:33:35.488127','?',_binary '',_binary '\0','Gold','INCOME','2026-04-29 16:33:54.085106',25,NULL,NULL),(18,'#2618dc','2026-04-29 16:35:39.304283','?',_binary '',_binary '\0','Groceries','EXPENSE',NULL,25,NULL,NULL),(19,'#f50aca','2026-04-29 16:37:46.331340','?',_binary '',_binary '\0','Fastfood','EXPENSE',NULL,25,NULL,NULL),(20,'#e6a519','2026-04-29 16:38:22.080537','?',_binary '',_binary '\0','ShareApp','INCOME',NULL,25,NULL,NULL),(21,'#6319d2','2026-04-29 21:33:48.160194','?',_binary '',_binary '\0','Rajshree','EXPENSE',NULL,26,NULL,NULL),(22,'#2ddf20','2026-04-29 21:34:18.367497','?',_binary '',_binary '\0','Farming','INCOME',NULL,26,NULL,NULL),(23,'#e60a7f','2026-05-01 18:46:07.813109','$',_binary '',_binary '\0','Shirt','EXPENSE',NULL,26,NULL,NULL),(24,'#289571','2026-05-01 22:49:08.361572','?',_binary '',_binary '\0','Bat','EXPENSE',NULL,30,NULL,NULL),(25,'#1f7a5c','2026-05-01 22:49:16.282805','?',_binary '',_binary '\0','Ball','INCOME',NULL,30,NULL,NULL),(26,'#1be428','2026-05-02 10:49:10.074673','?',_binary '',_binary '\0','Bat','INCOME',NULL,32,NULL,NULL),(27,'#fb0e0e','2026-05-02 10:49:22.790386','?',_binary '',_binary '\0','Ball','EXPENSE',NULL,32,NULL,NULL),(28,'#f43210','2026-05-03 13:04:55.755800','?',_binary '',_binary '\0','Shirt','EXPENSE',NULL,33,NULL,NULL),(29,'#22ce24','2026-05-03 13:05:21.620648','?',_binary '',_binary '\0','Post','INCOME',NULL,33,NULL,NULL),(30,'#1f7a5c','2026-05-04 16:33:21.599083','?',_binary '',_binary '\0','Travelling','EXPENSE',NULL,34,NULL,NULL),(31,'#1f7a5c','2026-05-08 12:12:06.978349','?',_binary '',_binary '\0','Cloth','EXPENSE',NULL,36,NULL,NULL),(32,'#1f7a5c','2026-05-08 12:12:20.337355','?',_binary '',_binary '\0','Social Media','INCOME',NULL,36,NULL,NULL),(33,'#ff0000','2026-05-08 14:19:37.512634','?',_binary '',_binary '\0','Resturant','EXPENSE',NULL,6,NULL,NULL),(34,'#45bf96','2026-05-11 14:58:59.564748','?',_binary '',_binary '\0','Groceries','EXPENSE',NULL,38,NULL,NULL),(35,'#eb5514','2026-05-11 14:59:36.576132','?',_binary '',_binary '\0','Mobile','INCOME',NULL,38,NULL,NULL),(36,'#f2bc26','2026-05-13 19:25:41.806625','🥼👟',_binary '',_binary '\0','Outfit','EXPENSE',NULL,39,NULL,NULL),(40,'#0ac2f0','2026-05-13 19:28:48.210713','•',_binary '',_binary '\0','Blogging','INCOME',NULL,39,NULL,NULL),(41,'#f12009','2026-05-14 09:49:35.805217','•',_binary '',_binary '\0','FastFood','EXPENSE',NULL,4,NULL,NULL),(42,'#10f449','2026-05-14 09:50:12.817019','•',_binary '',_binary '\0','Social Media','INCOME',NULL,4,NULL,NULL),(43,'#065b3f','2026-05-14 16:46:32.899622','•',_binary '',_binary '\0','Fun','EXPENSE',NULL,5,NULL,NULL),(44,'#c98e0d','2026-05-14 16:47:04.657704','•',_binary '',_binary '\0','Business','INCOME',NULL,5,NULL,NULL);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_expense_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_expense_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_expense_db`;

--
-- Table structure for table `expenses`
--

DROP TABLE IF EXISTS `expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenses` (
  `expense_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `is_recurring` bit(1) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `payment_method` enum('BANK','CARD','CASH','UPI','WALLET') DEFAULT NULL,
  `receipt_url` varchar(2048) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` enum('EXPENSE','SPLIT') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`expense_id`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expenses`
--

LOCK TABLES `expenses` WRITE;
/*!40000 ALTER TABLE `expenses` DISABLE KEYS */;
INSERT INTO `expenses` VALUES (1,200,1,'2026-04-24 01:18:46.199630','INR','2026-04-24 10:00:00.000000',_binary '\0','Lunch','UPI',NULL,'Food','EXPENSE',NULL,1),(2,200,1,'2026-04-24 01:22:05.508266','INR','2026-04-24 10:00:00.000000',_binary '\0','Lunch','UPI',NULL,'Food','EXPENSE',NULL,1),(3,499,1,'2026-04-24 11:57:09.255426',NULL,NULL,NULL,NULL,NULL,NULL,'Netflix Subscription',NULL,NULL,1),(4,2000,NULL,'2026-04-28 13:03:04.757974','INR','2026-04-28 12:00:00.000000',_binary '\0','','UPI','','Travelling','EXPENSE',NULL,16),(5,500,NULL,'2026-04-28 15:18:58.044795','INR','2026-04-28 12:00:00.000000',_binary '\0','','CASH','','Club','EXPENSE',NULL,16),(6,5000,NULL,'2026-04-28 16:26:14.846711','INR','2026-04-28 16:26:14.558980',_binary '','Auto-generated recurring','BANK',NULL,'Room Rent','EXPENSE',NULL,16),(7,5000,NULL,'2026-04-28 16:29:28.144653','INR','2026-04-28 12:00:00.000000',_binary '\0','','CASH','','Tution','EXPENSE',NULL,19),(8,3000,NULL,'2026-04-28 16:33:14.494047','INR','2026-04-28 16:33:14.413791',_binary '','Auto-generated recurring','BANK',NULL,'Rent','EXPENSE',NULL,19),(9,25000,NULL,'2026-04-28 16:36:39.070295','INR','2026-04-28 12:00:00.000000',_binary '\0','','CASH','','Criket Kit','EXPENSE',NULL,6),(10,25000,NULL,'2026-04-29 10:45:58.067303','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Family','EXPENSE',NULL,6),(11,10000,NULL,'2026-04-29 10:47:20.315604','INR','2026-04-29 10:47:19.676994',_binary '','Auto-generated recurring','BANK',NULL,'Match','EXPENSE',NULL,6),(12,1000,NULL,'2026-04-29 11:15:11.759733','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Study','EXPENSE',NULL,20),(13,3000,NULL,'2026-04-29 11:18:01.378412','INR','2026-04-29 11:18:01.007937',_binary '','Auto-generated recurring','BANK',NULL,'Fees','EXPENSE',NULL,20),(15,5000,NULL,'2026-04-29 11:57:03.334752','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Study','EXPENSE',NULL,21),(16,3000,NULL,'2026-04-29 11:57:51.309218','INR','2026-04-29 11:57:51.020737',_binary '','Auto-generated recurring','BANK',NULL,'Fees','EXPENSE',NULL,21),(17,3000,NULL,'2026-04-29 11:59:10.478126','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Party','EXPENSE',NULL,21),(18,10000,NULL,'2026-04-29 12:41:59.715334','INR','2026-04-29 12:41:57.364694',_binary '','Auto-generated recurring','BANK',NULL,'Gym','EXPENSE',NULL,6),(19,100,1,'2026-04-29 12:51:33.781983','INR','2026-04-29 12:00:00.000000',_binary '\0','test','UPI','https://example.com/receipt.jpg','Test receipt','EXPENSE',NULL,6),(20,100,1,'2026-04-29 12:51:35.737916','INR','2026-04-29 12:00:00.000000',_binary '\0','test','UPI','https://example.com/receipt.jpg','Gateway receipt','EXPENSE',NULL,6),(21,500,NULL,'2026-04-29 14:04:08.435473','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Petrol','EXPENSE',NULL,22),(22,20000,NULL,'2026-04-29 15:05:59.444480','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Mobile','EXPENSE',NULL,23),(23,1000,NULL,'2026-04-29 16:32:03.387432','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Tea','EXPENSE',NULL,25),(24,2000,NULL,'2026-04-29 16:35:53.829429','INR','2026-04-29 16:35:53.789467',_binary '','Auto-generated recurring','BANK',NULL,'Fees','EXPENSE',NULL,25),(25,500,NULL,'2026-04-29 16:36:54.350883','INR','2026-04-29 12:00:00.000000',_binary '\0','','CASH','','Vegetables','EXPENSE',NULL,25),(26,350,NULL,'2026-04-29 16:39:53.792253','INR','2026-04-29 16:39:53.786807',_binary '','Auto-generated recurring','BANK',NULL,'Recharge','EXPENSE',NULL,25),(27,800,NULL,'2026-04-29 21:32:58.096063','INR','2026-04-29 12:00:00.000000',_binary '\0','','UPI','','Wine','EXPENSE',NULL,26),(28,3000,NULL,'2026-04-29 21:36:16.730804','INR','2026-04-29 21:36:16.690972',_binary '','Auto-generated recurring','BANK',NULL,'Room Rent','EXPENSE',NULL,26),(29,9000,NULL,'2026-04-29 21:36:30.931398','INR','2026-04-29 12:00:00.000000',_binary '\0','','CASH','','Travel','EXPENSE',NULL,26),(30,2000,NULL,'2026-04-29 21:43:16.701927','INR','2026-04-29 21:43:16.690966',_binary '','Auto-generated recurring','BANK',NULL,'Fees','EXPENSE',NULL,26),(31,999,NULL,'2026-04-29 21:44:16.694326','INR','2026-04-29 21:44:16.681713',_binary '','Auto-generated recurring','BANK',NULL,'Recharge','EXPENSE',NULL,26),(32,999,NULL,'2026-04-30 16:50:50.484501','INR','2026-04-30 16:50:48.621462',_binary '','Auto-generated recurring','BANK',NULL,'Recharge','EXPENSE',NULL,29),(33,4500,NULL,'2026-04-30 16:53:51.502591','INR','2026-04-30 12:00:00.000000',_binary '\0','','UPI','','Shoes','EXPENSE',NULL,29),(34,300,NULL,'2026-05-01 18:45:02.158386','INR','2026-05-01 12:00:00.000000',_binary '\0','','UPI','','Tea','EXPENSE',NULL,26),(35,20000,NULL,'2026-05-01 22:48:37.209566','INR','2026-05-01 12:00:00.000000',_binary '\0','','UPI','','Cricket Kit','EXPENSE',NULL,30),(37,600,NULL,'2026-05-01 23:08:29.134231','INR','2026-05-01 12:00:00.000000',_binary '\0','','UPI','','dfg','EXPENSE',NULL,31),(38,100,NULL,'2026-05-01 23:09:53.113720','INR','2026-05-01 12:00:00.000000',_binary '\0','','UPI','','100','EXPENSE',NULL,31),(40,19000,NULL,'2026-05-02 10:47:59.203615','USD','2026-05-02 12:00:00.000000',_binary '','','UPI','','Body Building','EXPENSE',NULL,32),(41,500000,NULL,'2026-05-02 11:10:10.616377','INR','2026-05-02 12:00:00.000000',_binary '\0','','UPI','','World Tour','EXPENSE',NULL,5),(42,100000,NULL,'2026-05-02 11:11:21.843035','INR','2026-05-02 12:00:00.000000',_binary '\0','','UPI','','Ipl','EXPENSE',NULL,5),(43,15000,NULL,'2026-05-02 11:18:32.256649','INR','2026-05-02 12:00:00.000000',_binary '\0','','UPI','','Shoes','EXPENSE',NULL,5),(44,32000,NULL,'2026-05-02 11:23:23.330461','USD','2026-05-02 12:00:00.000000',_binary '\0','','UPI','','Family Trip','EXPENSE',NULL,32),(45,15000,NULL,'2026-05-02 12:37:28.288621','INR','2026-05-02 12:00:00.000000',_binary '','','UPI','','Fitness','EXPENSE',NULL,6),(46,15000,NULL,'2026-05-02 12:37:46.897794','INR','2026-05-02 12:37:46.842874',_binary '','Auto-generated recurring','BANK',NULL,'Fitness','EXPENSE',NULL,6),(47,25000,NULL,'2026-05-02 12:54:38.639654','INR','2026-05-02 12:00:00.000000',_binary '','','UPI','','Tution Fees','EXPENSE',NULL,15),(48,25000,NULL,'2026-05-03 13:02:40.979710','USD','2026-05-03 12:00:00.000000',_binary '\0','','UPI','','Sports Shoes','SPLIT',NULL,33),(49,20000,NULL,'2026-05-03 13:03:19.287116','USD','2026-05-03 12:00:00.000000',_binary '\0','','CASH','','Casual Shoes','SPLIT',NULL,33),(50,999,NULL,'2026-05-03 13:09:32.844313','INR','2026-05-03 13:09:32.703878',_binary '','Auto-generated recurring','BANK',NULL,'Recharge','EXPENSE',NULL,33),(51,10000,NULL,'2026-05-03 13:18:12.096326','USD','2026-05-03 12:00:00.000000',_binary '','MRF Bat','UPI','','Bat','EXPENSE',NULL,33),(52,10000,NULL,'2026-05-03 13:18:32.738800','INR','2026-05-03 13:18:32.717067',_binary '','Auto-generated recurring','BANK',NULL,'Bat','EXPENSE',NULL,33),(53,500,NULL,'2026-05-03 13:20:30.597493','USD','2026-05-03 12:00:00.000000',_binary '\0','','WALLET','','Ball','EXPENSE',NULL,33),(54,15000,NULL,'2026-05-03 13:21:29.918085','USD','2026-05-03 12:00:00.000000',_binary '\0','Criket Kit','UPI','','Kit','EXPENSE',NULL,33),(55,2000,NULL,'2026-05-03 13:34:13.563578','USD','2026-05-03 12:00:00.000000',_binary '\0','','UPI','https://www.google.com/search?q=bhopal+famous+restaurant&oq=bhopal+famous+re&gs_lcrp=EgZjaHJvbWUqBwgBEAAYgAQyBggAEEUYOTIHCAEQABiABDIHCAIQABiABDIICAMQABgWGB4yCAgEEAAYFhgeMggIBRAAGBYYHjIICAYQABgWGB4yCAgHEAAYFhgeMggICBAAGBYYHjIICAkQABgWGB7SAQkxMjgwMGowajeoAgCwAgA&sourceid=chrome&ie=UTF-8#sv=CAESzQEKuQEStgEKd0FNbjMteVE3VDdCcTJlOUZEQUdwTnJGN1ZHNGt5Yk9NMXR2bmM5QTUweXRmbVFVdVA2ejg1a1VmUzdaNnZkUFJKX0lsSk4zY0Y3QmMxVTE1V3JmSUxCRFRwVm1kZi15QzBnWTFiTDhWUS1UaURHMjhEd1FFVnJZEhdiZ0gzYWZQc0JlRHZzZU1QN2FfVDRRYxoiQUpLTEZtTDRDNWtvWTl5WlRIQU9XbC1tYUlNRnVtQlM1URIEODA3NhoBMyoAMAA4AUAAGAAgza2Vsw1KAhAC','Food','EXPENSE',NULL,33),(56,5000,NULL,'2026-05-04 16:30:29.577146','INR','2026-05-04 12:00:00.000000',_binary '\0','','UPI',NULL,'College Fees','EXPENSE',NULL,34),(57,999,NULL,'2026-05-04 16:32:57.966915','INR','2026-05-04 16:32:57.929689',_binary '','Auto-generated recurring','BANK',NULL,'Mobile Recharge','EXPENSE',NULL,34),(58,2000,30,'2026-05-04 16:33:39.013460','INR','2026-05-04 12:00:00.000000',_binary '\0','','UPI',NULL,'Train','EXPENSE',NULL,34),(59,5000,NULL,'2026-05-08 11:50:46.843105','USD','2026-05-08 12:00:00.000000',_binary '\0','','UPI',NULL,'Travel','EXPENSE',NULL,36),(60,1000,NULL,'2026-05-08 11:53:11.603615','INR','2026-05-08 12:00:00.000000',_binary '\0','','UPI',NULL,'Fast Food','EXPENSE',NULL,36),(61,2000,NULL,'2026-05-08 11:53:59.334291','INR','2026-05-08 12:00:00.000000',_binary '\0','','UPI',NULL,'Tip','EXPENSE',NULL,36),(62,2500,NULL,'2026-05-08 12:09:22.530799','INR','2026-05-08 12:00:00.000000',_binary '\0','','UPI',NULL,'Kit','EXPENSE',NULL,36),(63,999,NULL,'2026-05-08 12:13:49.935479','INR','2026-05-08 12:13:49.805689',_binary '','Auto-generated recurring','BANK',NULL,'Recharge','EXPENSE',NULL,36),(64,100000,33,'2026-05-08 14:20:22.490915','INR','2026-05-08 12:00:00.000000',_binary '\0','','UPI',NULL,'Crazy Food','EXPENSE',NULL,6),(65,6000,NULL,'2026-05-11 14:57:30.341872','INR','2026-05-11 12:00:00.000000',_binary '\0','','CASH',NULL,'Coaching','EXPENSE',NULL,38),(66,2500,NULL,'2026-05-13 19:23:26.810520','INR','2026-05-13 12:00:00.000000',_binary '\0','','CASH',NULL,'Shoes','EXPENSE',NULL,39),(67,3050,NULL,'2026-05-14 09:48:43.864342','INR','2026-05-14 12:00:00.000000',_binary '\0','','CASH',NULL,'Sports','EXPENSE','2026-05-14 09:48:59.481719',4),(69,2000,NULL,'2026-05-14 09:52:47.378704','INR','2026-05-14 12:00:00.000000',_binary '\0','','UPI',NULL,'Groceries','EXPENSE',NULL,4),(70,999,NULL,'2026-05-14 09:52:53.939905','INR','2026-05-14 09:52:53.812772',_binary '','Auto-generated recurring','BANK',NULL,'Mobile Recharge','EXPENSE',NULL,4),(71,1000,NULL,'2026-05-14 09:54:48.503630','INR','2026-05-14 12:00:00.000000',_binary '\0','','UPI',NULL,'Study','EXPENSE',NULL,4),(72,1000,NULL,'2026-05-14 09:57:15.238228','INR','2026-05-14 12:00:00.000000',_binary '\0','','UPI',NULL,'Fun','EXPENSE',NULL,4),(73,15000,NULL,'2026-05-14 16:47:44.720996','INR','2026-05-14 12:00:00.000000',_binary '\0','','UPI',NULL,'Cloth','EXPENSE',NULL,5),(74,15000,NULL,'2026-05-14 16:47:44.684731','INR','2026-05-14 12:00:00.000000',_binary '\0','','UPI',NULL,'Cloth','EXPENSE',NULL,5);
/*!40000 ALTER TABLE `expenses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_income_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_income_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_income_db`;

--
-- Table structure for table `income`
--

DROP TABLE IF EXISTS `income`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `income` (
  `income_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `is_recurring` bit(1) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`income_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `income`
--

LOCK TABLES `income` WRITE;
/*!40000 ALTER TABLE `income` DISABLE KEYS */;
INSERT INTO `income` VALUES (1,50000,'2026-04-24 02:01:57.390908','INR','2026-04-24 10:00:00.000000',_binary '','Monthly salary','Salary',NULL,1),(2,1000,'2026-04-26 15:20:28.176832','INR','2026-04-26 12:00:00.000000',_binary '\0','','Gift',NULL,3),(5,1000000,'2026-04-27 11:43:53.411777','INR','2026-04-27 12:00:00.000000',_binary '\0','','Salary',NULL,6),(6,25000,'2026-04-27 23:14:18.093231','INR','2026-04-27 12:00:00.000000',_binary '\0','','Salary',NULL,15),(7,32000,'2026-04-28 13:02:23.101164','INR','2026-04-28 12:00:00.000000',_binary '\0','','Salary',NULL,16),(8,25000,'2026-04-28 15:19:16.839432','INR','2026-04-28 12:00:00.000000',_binary '\0','','Salary',NULL,16),(9,11000,'2026-04-28 16:31:36.488355','INR','2026-04-28 12:00:00.000000',_binary '\0','','Business',NULL,19),(10,100000,'2026-04-29 10:46:16.877434','INR','2026-04-29 12:00:00.000000',_binary '\0','','Gift',NULL,6),(11,15000,'2026-04-29 11:15:37.330990','INR','2026-04-29 12:00:00.000000',_binary '\0','','Salary',NULL,20),(12,15000,'2026-04-29 11:57:12.343896','INR','2026-04-29 12:00:00.000000',_binary '\0','','Salary',NULL,21),(13,50000,'2026-04-29 15:06:17.096064','INR','2026-04-29 12:00:00.000000',_binary '\0','','Salary',NULL,23),(14,5000,'2026-04-29 16:32:21.401106','INR','2026-04-29 12:00:00.000000',_binary '\0','','Other',NULL,25),(15,1500,'2026-04-29 16:37:19.604639','INR','2026-04-29 12:00:00.000000',_binary '\0','','Business',NULL,25),(16,10000,'2026-04-29 21:33:20.540919','INR','2026-04-29 12:00:00.000000',_binary '\0','','Salary',NULL,26),(17,20000,'2026-04-29 21:40:01.306634','INR','2026-04-29 12:00:00.000000',_binary '\0','','Business',NULL,26),(18,5000,'2026-04-30 17:00:07.583848','INR','2026-04-30 12:00:00.000000',_binary '\0','','Salary',NULL,29),(19,1500,'2026-05-01 18:45:26.979794','INR','2026-05-01 12:00:00.000000',_binary '\0','','Business',NULL,26),(20,100000,'2026-05-01 22:48:53.731284','INR','2026-05-01 12:00:00.000000',_binary '\0','','Salary',NULL,30),(21,1000,'2026-05-01 23:07:55.154267','INR','2026-05-01 12:00:00.000000',_binary '\0','','Salary',NULL,31),(23,160000,'2026-05-02 10:47:20.229528','USD','2026-05-02 12:00:00.000000',_binary '','','Salary',NULL,32),(24,900000,'2026-05-02 11:10:25.247845','INR','2026-05-02 12:00:00.000000',_binary '\0','','Salary',NULL,5),(25,55000,'2026-05-02 12:38:11.979349','INR','2026-05-02 12:00:00.000000',_binary '','','Business',NULL,6),(26,55000,'2026-05-02 12:38:46.894572','INR','2026-05-02 12:38:46.831331',_binary '','Auto-generated recurring',NULL,NULL,6),(28,100000,'2026-05-03 13:04:16.218309','USD','2026-05-03 12:00:00.000000',_binary '','','Salary',NULL,33),(29,25000,'2026-05-04 16:30:41.118689','INR','2026-05-04 12:00:00.000000',_binary '\0','','Salary',NULL,34),(30,26000,'2026-05-08 11:50:31.891459','USD','2026-05-08 12:00:00.000000',_binary '\0','','Salary',NULL,36),(31,1200,'2026-05-08 12:11:40.045501','INR','2026-05-08 12:00:00.000000',_binary '\0','','Gift',NULL,36),(32,22000,'2026-05-11 14:58:24.142251','INR','2026-05-11 12:00:00.000000',_binary '\0','','Salary',NULL,38),(33,18000,'2026-05-13 19:23:45.765759','INR','2026-05-13 12:00:00.000000',_binary '\0','','Salary',NULL,39),(34,26000,'2026-05-14 09:49:15.407266','INR','2026-05-14 12:00:00.000000',_binary '\0','','Salary',NULL,4);
/*!40000 ALTER TABLE `income` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_notification_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_notification_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_notification_db`;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) DEFAULT NULL,
  `message` varchar(2000) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(40) NOT NULL,
  `user_id` bigint NOT NULL,
  `acknowledged_at` datetime(6) DEFAULT NULL,
  `is_acknowledged` bit(1) NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `recipient_email` varchar(255) DEFAULT NULL,
  `related_id` bigint DEFAULT NULL,
  `related_type` varchar(255) DEFAULT NULL,
  `severity` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=160 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,'2026-04-24 14:10:23.276660',_binary '\0','This is a test','Test Notification','SYSTEM',1,NULL,_binary '\0',NULL,NULL,NULL,NULL,'CRITICAL'),(2,'2026-04-29 11:13:00.195107',_binary '\0','Hi Harsh Choudhary, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',20,NULL,_binary '\0',NULL,'btechboss420@gmail.com',20,'USER','INFO'),(3,'2026-04-29 11:54:34.929072',_binary '\0','Hi Harsh Vardhan , your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',21,NULL,_binary '\0',NULL,'youte208@gmail.com',21,'USER','INFO'),(4,'2026-04-29 11:59:11.066442',_binary '\0','Monthly Budget budget is over the limit. Spent INR 11000.00 out of INR 10000.00 (110.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',21,NULL,_binary '\0',NULL,'youte208@gmail.com',8,'BUDGET','CRITICAL'),(5,'2026-04-29 12:41:10.047099',_binary '\0','Monthly Budget budget is over the limit. Spent INR 60000.00 out of INR 25000.00 (240.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',6,NULL,_binary '\0',NULL,'virat@gmail.com',6,'BUDGET','CRITICAL'),(6,'2026-04-29 12:42:01.587247',_binary '\0','Monthly Budget budget is over the limit. Spent INR 70000.00 out of INR 25000.00 (280.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',6,NULL,_binary '\0',NULL,'virat@gmail.com',6,'BUDGET','CRITICAL'),(7,'2026-04-29 12:43:50.424682',_binary '\0','Monthly Budget budget is over the limit. Spent INR 70000.00 out of INR 25000.00 (280.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',6,NULL,_binary '\0',NULL,'virat@gmail.com',6,'BUDGET','CRITICAL'),(8,'2026-04-29 12:51:34.480288',_binary '\0','Monthly Budget budget is over the limit. Spent INR 70100.00 out of INR 25000.00 (280.40% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',6,NULL,_binary '\0',NULL,'virat@gmail.com',6,'BUDGET','CRITICAL'),(9,'2026-04-29 12:51:35.910403',_binary '\0','Monthly Budget budget is over the limit. Spent INR 70200.00 out of INR 25000.00 (280.80% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',6,NULL,_binary '\0',NULL,'virat@gmail.com',6,'BUDGET','CRITICAL'),(10,'2026-04-29 14:01:18.878335',_binary '\0','Hi Abhijeet Kumar, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',22,NULL,_binary '\0',NULL,'abhijeetsingh1623@gmail.com',22,'USER','INFO'),(11,'2026-04-29 14:46:56.629738',_binary '\0','Monthly Budget budget is over the limit. Spent INR 70200.00 out of INR 25000.00 (280.80% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',6,NULL,_binary '\0',NULL,'virat@gmail.com',6,'BUDGET','CRITICAL'),(12,'2026-04-29 15:04:01.226611',_binary '\0','Hi Ayush Mishra, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',23,NULL,_binary '\0',NULL,'mishrayush0503@gmail.com',23,'USER','INFO'),(13,'2026-04-29 15:55:26.553133',_binary '\0','Hi Dev Mishra, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',24,NULL,_binary '\0',NULL,'devarshim963@gmail.com',24,'USER','INFO'),(14,'2026-04-29 16:30:48.293745',_binary '\0','Hi Abhi, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',25,NULL,_binary '\0',NULL,'kushwahaabhishek100k@gmail.com',25,'USER','INFO'),(15,'2026-04-29 21:30:29.033508',_binary '\0','Hi  Ajay Kushwaha, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',26,'USER','INFO'),(16,'2026-04-29 21:36:17.608583',_binary '\0','Monthly Budget budget is over the limit. Spent INR 3800.00 out of INR 2000.00 (190.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',12,'BUDGET','CRITICAL'),(17,'2026-04-29 21:36:31.035582',_binary '\0','Monthly Budget budget is over the limit. Spent INR 12800.00 out of INR 2000.00 (640.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',12,'BUDGET','CRITICAL'),(18,'2026-04-29 21:44:16.796491',_binary '\0','Monthly Budget budget is over the limit. Spent INR 15799.00 out of INR 15000.00 (105.33% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',12,'BUDGET','CRITICAL'),(19,'2026-04-29 22:15:59.476398',_binary '\0','Hi Ramu, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',27,NULL,_binary '\0',NULL,'ramu@gmail.com',27,'USER','INFO'),(20,'2026-04-30 10:06:53.339519',_binary '\0','Hi Ayush , your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',28,NULL,_binary '\0',NULL,'ayushmagarde03@gmail.com',28,'USER','INFO'),(21,'2026-04-30 12:23:45.266140',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',1,NULL,_binary '\0',NULL,'abhi@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(22,'2026-04-30 12:23:49.792009',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',2,NULL,_binary '\0',NULL,'anurag@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(23,'2026-04-30 12:23:53.539984',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',3,NULL,_binary '\0',NULL,'kushabhi2003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(24,'2026-04-30 12:23:57.278645',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',4,NULL,_binary '\0',NULL,'joeroot@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(25,'2026-04-30 12:24:00.978425',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',5,NULL,_binary '\0',NULL,'philipsalt@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(26,'2026-04-30 12:24:04.851503',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',6,NULL,_binary '\0',NULL,'virat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(27,'2026-04-30 12:24:08.505443',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',7,NULL,_binary '\0',NULL,'rajat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(28,'2026-04-30 12:24:12.213603',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',9,NULL,_binary '\0',NULL,'abhishekkushwaha17032003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(29,'2026-04-30 12:24:15.923609',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',10,NULL,_binary '\0',NULL,'abhishekkushwaha2003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(30,'2026-04-30 12:24:19.814243',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',11,NULL,_binary '\0',NULL,'kunalpandya@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(31,'2026-04-30 12:24:23.544745',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',12,NULL,_binary '\0',NULL,'pandit.adarsh.mishra145@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(32,'2026-04-30 12:24:27.256431',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',13,NULL,_binary '\0',NULL,'admin@spendsmart.com',NULL,'ADMIN_BROADCAST','INFO'),(33,'2026-04-30 12:24:31.166275',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',14,NULL,_binary '\0',NULL,'rajrai@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(34,'2026-04-30 12:24:34.903594',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',15,NULL,_binary '\0',NULL,'raajrai@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(35,'2026-04-30 12:24:38.654051',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',16,NULL,_binary '\0',NULL,'harshal@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(36,'2026-04-30 12:24:42.344921',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',18,NULL,_binary '\0',NULL,'hemant@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(37,'2026-04-30 12:24:46.110739',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',19,NULL,_binary '\0',NULL,'chandu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(38,'2026-04-30 12:24:49.811427',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',20,NULL,_binary '\0',NULL,'btechboss420@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(39,'2026-04-30 12:24:53.483378',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',21,NULL,_binary '\0',NULL,'youte208@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(40,'2026-04-30 12:24:57.136029',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',22,NULL,_binary '\0',NULL,'abhijeetsingh1623@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(41,'2026-04-30 12:25:00.928301',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',23,NULL,_binary '\0',NULL,'mishrayush0503@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(42,'2026-04-30 12:25:04.702058',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',24,NULL,_binary '\0',NULL,'devarshim963@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(43,'2026-04-30 12:25:08.409212',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',25,NULL,_binary '\0',NULL,'kushwahaabhishek100k@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(44,'2026-04-30 12:25:12.201649',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(45,'2026-04-30 12:25:15.964824',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',27,NULL,_binary '\0',NULL,'ramu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(46,'2026-04-30 12:25:19.685563',_binary '\0','Celebrating One Month Of SpendSmart.','Hurray!','SYSTEM',28,NULL,_binary '\0',NULL,'ayushmagarde03@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(47,'2026-04-30 12:25:54.152886',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',1,NULL,_binary '\0',NULL,'abhi@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(48,'2026-04-30 12:25:57.952946',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',2,NULL,_binary '\0',NULL,'anurag@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(49,'2026-04-30 12:26:01.781423',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',3,NULL,_binary '\0',NULL,'kushabhi2003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(50,'2026-04-30 12:26:05.494759',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',4,NULL,_binary '\0',NULL,'joeroot@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(51,'2026-04-30 12:26:09.345464',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',5,NULL,_binary '\0',NULL,'philipsalt@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(52,'2026-04-30 12:26:13.189594',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',6,NULL,_binary '\0',NULL,'virat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(53,'2026-04-30 12:26:16.943683',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',7,NULL,_binary '\0',NULL,'rajat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(54,'2026-04-30 12:26:20.662621',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',9,NULL,_binary '\0',NULL,'abhishekkushwaha17032003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(55,'2026-04-30 12:26:24.367537',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',10,NULL,_binary '\0',NULL,'abhishekkushwaha2003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(56,'2026-04-30 12:26:28.158642',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',11,NULL,_binary '\0',NULL,'kunalpandya@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(57,'2026-04-30 12:26:31.867256',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',12,NULL,_binary '\0',NULL,'pandit.adarsh.mishra145@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(58,'2026-04-30 12:26:35.859280',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',13,NULL,_binary '\0',NULL,'admin@spendsmart.com',NULL,'ADMIN_BROADCAST','INFO'),(59,'2026-04-30 12:26:39.610497',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',14,NULL,_binary '\0',NULL,'rajrai@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(60,'2026-04-30 12:26:43.462041',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',15,NULL,_binary '\0',NULL,'raajrai@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(61,'2026-04-30 12:26:47.170720',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',16,NULL,_binary '\0',NULL,'harshal@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(62,'2026-04-30 12:26:51.229174',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',18,NULL,_binary '\0',NULL,'hemant@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(63,'2026-04-30 12:26:54.954106',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',19,NULL,_binary '\0',NULL,'chandu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(64,'2026-04-30 12:26:58.926369',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',20,NULL,_binary '\0',NULL,'btechboss420@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(65,'2026-04-30 12:27:02.648528',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',21,NULL,_binary '\0',NULL,'youte208@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(66,'2026-04-30 12:27:06.527370',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',22,NULL,_binary '\0',NULL,'abhijeetsingh1623@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(67,'2026-04-30 12:27:10.373258',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',23,NULL,_binary '\0',NULL,'mishrayush0503@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(68,'2026-04-30 12:27:14.410026',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',24,NULL,_binary '\0',NULL,'devarshim963@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(69,'2026-04-30 12:27:18.100353',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',25,NULL,_binary '\0',NULL,'kushwahaabhishek100k@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(70,'2026-04-30 12:27:21.855310',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(71,'2026-04-30 12:27:25.590169',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',27,NULL,_binary '\0',NULL,'ramu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(72,'2026-04-30 12:27:29.381410',_binary '\0','Celebrating Day 5 Of SpendSmart - Expense Tracker Platform.','Hurray!','SYSTEM',28,NULL,_binary '\0',NULL,'ayushmagarde03@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(73,'2026-04-30 16:49:09.451802',_binary '\0','Hi Anurag, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',29,NULL,_binary '\0',NULL,'VEER.IGT@gmail.com',29,'USER','INFO'),(74,'2026-04-30 16:53:52.835672',_binary '\0','Monthly Budget budget is over the limit. Spent INR 5499.00 out of INR 5000.00 (109.98% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',29,NULL,_binary '\0',NULL,'VEER.IGT@gmail.com',13,'BUDGET','CRITICAL'),(75,'2026-05-01 22:47:16.628236',_binary '\0','Hi Jacob Bethell, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',30,NULL,_binary '\0',NULL,'jacob@gmail.com',30,'USER','INFO'),(76,'2026-05-01 22:49:27.394599',_binary '\0','Monthly Budget budget is over the limit. Spent INR 20000.00 out of INR 15000.00 (133.33% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',30,NULL,_binary '\0',NULL,'jacob@gmail.com',15,'BUDGET','CRITICAL'),(77,'2026-05-01 22:49:32.531081',_binary '\0','Monthly Budget budget is over the limit. Spent INR 20000.00 out of INR 15000.00 (133.33% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',30,NULL,_binary '\0',NULL,'jacob@gmail.com',15,'BUDGET','CRITICAL'),(78,'2026-05-01 22:49:33.696817',_binary '\0','Monthly Budget budget is over the limit. Spent INR 20000.00 out of INR 15000.00 (133.33% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',30,NULL,_binary '\0',NULL,'jacob@gmail.com',15,'BUDGET','CRITICAL'),(79,'2026-05-01 22:49:34.429961',_binary '\0','Monthly Budget budget is over the limit. Spent INR 20000.00 out of INR 15000.00 (133.33% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',30,NULL,_binary '\0',NULL,'jacob@gmail.com',15,'BUDGET','CRITICAL'),(80,'2026-05-01 23:07:18.131045',_binary '\0','Hi sdf dfg, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',31,NULL,_binary '\0',NULL,'asdfg@hjkl',31,'USER','INFO'),(81,'2026-05-01 23:08:23.265357',_binary '\0','Monthly Budget budget is over the limit. Spent INR 600.00 out of INR 500.00 (120.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',31,NULL,_binary '\0',NULL,'asdfg@hjkl',16,'BUDGET','CRITICAL'),(82,'2026-05-01 23:08:29.191383',_binary '\0','Monthly Budget budget is over the limit. Spent INR 1200.00 out of INR 500.00 (240.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',31,NULL,_binary '\0',NULL,'asdfg@hjkl',16,'BUDGET','CRITICAL'),(83,'2026-05-01 23:09:01.939092',_binary '\0','Monthly Budget budget is over the limit. Spent INR 600.00 out of INR 500.00 (120.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',31,NULL,_binary '\0',NULL,'asdfg@hjkl',16,'BUDGET','CRITICAL'),(84,'2026-05-01 23:09:53.178598',_binary '\0','Monthly Budget budget is over the limit. Spent INR 700.00 out of INR 500.00 (140.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',31,NULL,_binary '\0',NULL,'asdfg@hjkl',16,'BUDGET','CRITICAL'),(85,'2026-05-02 10:34:29.283384',_binary '\0','Hi Romario Shepherd, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',32,NULL,_binary '\0',NULL,'romario@gmail.com',32,'USER','INFO'),(86,'2026-05-02 11:11:26.172951',_binary '\0','Monthly Budget budget is over the limit. Spent INR 600000.00 out of INR 600000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',5,NULL,_binary '\0',NULL,'philipsalt@gmail.com',18,'BUDGET','CRITICAL'),(87,'2026-05-02 11:18:33.445608',_binary '\0','Monthly Budget budget is over the limit. Spent INR 615000.00 out of INR 600000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',5,NULL,_binary '\0',NULL,'philipsalt@gmail.com',18,'BUDGET','CRITICAL'),(88,'2026-05-02 11:23:23.588594',_binary '\0','Monthly Budget budget is over the limit. Spent USD 51000.00 out of USD 50000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',32,NULL,_binary '\0',NULL,'romario@gmail.com',17,'BUDGET','CRITICAL'),(89,'2026-05-03 12:56:04.000082',_binary '\0','Hi AB de Villiers, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',33,NULL,_binary '\0',NULL,'abde@gmail.com',33,'USER','INFO'),(90,'2026-05-03 13:05:43.113852',_binary '\0','Monthly Budget is over the limit. Spent USD 45000.00 out of USD 30000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','CRITICAL'),(91,'2026-05-03 13:05:43.856363',_binary '\0','Monthly Budget is over the limit. Spent USD 45000.00 out of USD 30000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','CRITICAL'),(92,'2026-05-03 13:05:45.061122',_binary '\0','Monthly Budget is over the limit. Spent USD 45000.00 out of USD 30000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','CRITICAL'),(93,'2026-05-03 13:05:46.047750',_binary '\0','Monthly Budget is over the limit. Spent USD 45000.00 out of USD 30000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','CRITICAL'),(94,'2026-05-03 13:05:48.428209',_binary '\0','Monthly Budget is over the limit. Spent USD 45000.00 out of USD 30000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','CRITICAL'),(95,'2026-05-03 13:06:48.647089',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 45000.00 out of USD 50000.00 (90.00% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(96,'2026-05-03 13:06:51.680604',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 45000.00 out of USD 50000.00 (90.00% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(97,'2026-05-03 13:18:12.293235',_binary '\0','Monthly Budget is over the limit. Spent USD 55999.00 out of USD 51000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','CRITICAL'),(98,'2026-05-03 13:35:18.387299',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 83499.00 out of USD 99999.00 (83.50% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(99,'2026-05-03 13:35:21.589201',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 83499.00 out of USD 99999.00 (83.50% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(100,'2026-05-03 13:35:22.763126',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 83499.00 out of USD 99999.00 (83.50% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(101,'2026-05-03 13:35:23.711801',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 83499.00 out of USD 99999.00 (83.50% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(102,'2026-05-03 13:35:23.832949',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 83499.00 out of USD 99999.00 (83.50% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(103,'2026-05-03 13:35:23.995722',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent USD 83499.00 out of USD 99999.00 (83.50% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',33,NULL,_binary '\0',NULL,'abde@gmail.com',19,'BUDGET','WARNING'),(104,'2026-05-04 16:27:15.159240',_binary '\0','Hi Ayush Mishre, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',34,NULL,_binary '\0',NULL,'mishrayush0503@gmail.com',34,'USER','INFO'),(105,'2026-05-05 00:09:23.061267',_binary '\0','Hi Bhuvi Kumar, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',35,NULL,_binary '\0',NULL,'Bhuvi@gmail.com',35,'USER','INFO'),(106,'2026-05-05 14:34:36.734259',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',3,NULL,_binary '\0',NULL,'kushabhi2003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(107,'2026-05-05 14:34:40.818899',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',5,NULL,_binary '\0',NULL,'philipsalt@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(108,'2026-05-05 14:34:44.380752',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',12,NULL,_binary '\0',NULL,'pandit.adarsh.mishra145@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(109,'2026-05-05 14:34:48.011114',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',13,NULL,_binary '\0',NULL,'admin@spendsmart.com',NULL,'ADMIN_BROADCAST','INFO'),(110,'2026-05-05 14:34:51.662063',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',15,NULL,_binary '\0',NULL,'raajrai@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(111,'2026-05-05 14:34:55.195223',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',16,NULL,_binary '\0',NULL,'harshal@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(112,'2026-05-05 14:34:58.710073',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',19,NULL,_binary '\0',NULL,'chandu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(113,'2026-05-05 14:35:02.312761',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',20,NULL,_binary '\0',NULL,'btechboss420@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(114,'2026-05-05 14:35:06.337173',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',21,NULL,_binary '\0',NULL,'youte208@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(115,'2026-05-05 14:35:13.635361',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',22,NULL,_binary '\0',NULL,'abhijeetsingh1623@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(116,'2026-05-05 14:35:17.274665',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',24,NULL,_binary '\0',NULL,'devarshim963@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(117,'2026-05-05 14:35:20.999331',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(118,'2026-05-05 14:35:24.616047',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',29,NULL,_binary '\0',NULL,'VEER.IGT@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(119,'2026-05-05 14:35:28.267435',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',30,NULL,_binary '\0',NULL,'jacob@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(120,'2026-05-05 14:35:31.863781',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',32,NULL,_binary '\0',NULL,'romario@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(121,'2026-05-05 14:35:35.488873',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',33,NULL,_binary '\0',NULL,'abde@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(122,'2026-05-05 14:35:39.171269',_binary '\0','Don?t forget to log your daily expenses to keep your budget accurate. Small updates every day can make a big difference in your financial insights!','Stay on Track with Your Expenses','SYSTEM',35,NULL,_binary '\0',NULL,'Bhuvi@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(123,'2026-05-08 10:12:56.118233',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',3,NULL,_binary '\0',NULL,'kushabhi2003@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(124,'2026-05-08 10:13:00.253593',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',5,NULL,_binary '\0',NULL,'philipsalt@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(125,'2026-05-08 10:13:03.949684',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',6,NULL,_binary '\0',NULL,'virat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(126,'2026-05-08 10:13:07.741778',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',12,NULL,_binary '\0',NULL,'pandit.adarsh.mishra145@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(127,'2026-05-08 10:13:12.267850',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',13,NULL,_binary '\0',NULL,'admin@spendsmart.com',NULL,'ADMIN_BROADCAST','INFO'),(128,'2026-05-08 10:13:15.937639',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',16,NULL,_binary '\0',NULL,'harshal@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(129,'2026-05-08 10:13:19.586918',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',19,NULL,_binary '\0',NULL,'chandu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(130,'2026-05-08 10:13:23.300762',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',20,NULL,_binary '\0',NULL,'btechboss420@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(131,'2026-05-08 10:13:27.040071',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',21,NULL,_binary '\0',NULL,'youte208@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(132,'2026-05-08 10:13:30.760780',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',22,NULL,_binary '\0',NULL,'abhijeetsingh1623@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(133,'2026-05-08 10:13:34.364939',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',24,NULL,_binary '\0',NULL,'devarshim963@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(134,'2026-05-08 10:13:38.183688',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',26,NULL,_binary '\0',NULL,'ajaykushwaha9770@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(135,'2026-05-08 10:13:41.995139',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',30,NULL,_binary '\0',NULL,'jacob@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(136,'2026-05-08 10:13:45.759425',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',32,NULL,_binary '\0',NULL,'romario@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(137,'2026-05-08 10:13:49.425753',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',33,NULL,_binary '\0',NULL,'abde@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(138,'2026-05-08 10:13:53.099948',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',34,NULL,_binary '\0',NULL,'mishrayush0503@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(139,'2026-05-08 10:13:56.636256',_binary '\0','Hello Users ??\nTrack your daily expenses regularly to stay in control of your budget ??\n\n? Add every transaction\n? Monitor spending categories\n? Set monthly limits\n? Avoid unnecessary expenses\n? Build better saving habits\n\nSmall savings today can create a better tomorrow ??','?? Smart Expense Reminder','SYSTEM',35,NULL,_binary '\0',NULL,'Bhuvi@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(140,'2026-05-08 11:38:17.659969',_binary '\0','Hi Krunal Pandya, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',36,NULL,_binary '\0',NULL,'abhishekkushwaha17032003@gmail.com',36,'USER','INFO'),(141,'2026-05-08 11:54:00.426599',_binary '\0','Monthly Budget has reached the 75.00% alert threshold. Spent INR 8000.00 out of INR 10000.00 (80.00% used).','Budget threshold reached for Monthly Budget','BUDGET_ALERT',36,NULL,_binary '\0',NULL,'abhishekkushwaha17032003@gmail.com',21,'BUDGET','WARNING'),(142,'2026-05-08 12:09:26.141594',_binary '\0','Monthly Budget is over the limit. Spent INR 10500.00 out of INR 10000.00 (100.00% used).','Budget exceeded for Monthly Budget','BUDGET_EXCEEDED',36,NULL,_binary '\0',NULL,'abhishekkushwaha17032003@gmail.com',21,'BUDGET','CRITICAL'),(143,'2026-05-08 12:13:51.054029',_binary '\0','Recurring executed:\nTitle: Recharge\nAmount: Rs 999.0\nDate: 2026-05-08T12:13:50.475497','Recurring Transaction Alert','SYSTEM',36,NULL,_binary '\0',NULL,'abhishekkushwaha17032003@gmail.com',22,'RECURRING','INFO'),(144,'2026-05-14 05:31:17.556411',_binary '\0','Hi Sonu, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',41,NULL,_binary '\0',NULL,'sonu@gmail.com',41,'USER','INFO'),(145,'2026-05-14 06:50:08.524930',_binary '\0','Hi Glenn Maxwell, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',42,NULL,_binary '\0',NULL,'glenn@gmail.com',42,'USER','INFO'),(146,'2026-05-14 07:09:12.053088',_binary '\0','Hi Ram , your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',43,NULL,_binary '\0',NULL,'ram@gmail.com',43,'USER','INFO'),(147,'2026-05-14 08:32:15.894611',_binary '\0','Hi Anni Yadav, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',1,NULL,_binary '\0',NULL,'anni@gmail.com',1,'USER','INFO'),(148,'2026-05-14 08:39:12.207836',_binary '\0','Hi Bittu, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',2,NULL,_binary '\0',NULL,'bittu@gmail.com',2,'USER','INFO'),(149,'2026-05-14 08:49:33.753887',_binary '\0','Hi Virat Kohli, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',3,NULL,_binary '\0',NULL,'virat@gmail.com',3,'USER','INFO'),(150,'2026-05-14 08:52:31.301406',_binary '\0','Enjoy SpendSmart Service and Balance your life.','Hello SpendSmart User🖐️','SYSTEM',1,NULL,_binary '\0',NULL,'anni@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(151,'2026-05-14 08:52:35.068344',_binary '\0','Enjoy SpendSmart Service and Balance your life.','Hello SpendSmart User🖐️','SYSTEM',2,NULL,_binary '\0',NULL,'bittu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(152,'2026-05-14 08:52:38.874028',_binary '\0','Enjoy SpendSmart Service and Balance your life.','Hello SpendSmart User🖐️','SYSTEM',3,NULL,_binary '\0',NULL,'virat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(153,'2026-05-14 09:41:16.191779',_binary '\0','Welcome to SpendSmart! 🚀\n\nWe’re excited to help you manage your expenses, track budgets, and improve your financial health.\n\nNew features are now live:\n• Smart budget tracking\n• Expense analytics dashboard\n• Monthly financial summaries\n• Recurring transaction reminders\n\nStart tracking smarter and save better 💰','Hey Users🖐️','SYSTEM',1,NULL,_binary '\0',NULL,'anni@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(154,'2026-05-14 09:41:23.909656',_binary '\0','Welcome to SpendSmart! 🚀\n\nWe’re excited to help you manage your expenses, track budgets, and improve your financial health.\n\nNew features are now live:\n• Smart budget tracking\n• Expense analytics dashboard\n• Monthly financial summaries\n• Recurring transaction reminders\n\nStart tracking smarter and save better 💰','Hey Users🖐️','SYSTEM',2,NULL,_binary '\0',NULL,'bittu@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(155,'2026-05-14 09:41:28.812627',_binary '\0','Welcome to SpendSmart! 🚀\n\nWe’re excited to help you manage your expenses, track budgets, and improve your financial health.\n\nNew features are now live:\n• Smart budget tracking\n• Expense analytics dashboard\n• Monthly financial summaries\n• Recurring transaction reminders\n\nStart tracking smarter and save better 💰','Hey Users🖐️','SYSTEM',3,NULL,_binary '\0',NULL,'virat@gmail.com',NULL,'ADMIN_BROADCAST','INFO'),(156,'2026-05-14 09:46:16.997021',_binary '\0','Hi Ayush Mishra, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',4,NULL,_binary '\0',NULL,'mishrayush0503@gmail.com',4,'USER','INFO'),(158,'2026-05-14 16:40:45.068474',_binary '\0','Hi Anurag , your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',5,NULL,_binary '\0',NULL,'anuragkushwahaji10k@gmail.com',5,'USER','INFO'),(159,'2026-05-14 17:19:45.206382',_binary '\0','Hi Rahul, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.','Welcome to Spend Smart','SYSTEM',6,NULL,_binary '\0',NULL,'rahul@gmail.com',6,'USER','INFO');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `acknowledged_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_acknowledged` bit(1) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` varchar(1000) NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `recipient_email` varchar(255) DEFAULT NULL,
  `recipient_id` int NOT NULL,
  `related_id` int DEFAULT NULL,
  `related_type` varchar(255) DEFAULT NULL,
  `severity` varchar(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `type` varchar(50) NOT NULL,
  PRIMARY KEY (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_payment_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_payment_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_payment_db`;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `currency` varchar(255) NOT NULL,
  `razorpay_order_id` varchar(255) DEFAULT NULL,
  `razorpay_payment_id` varchar(255) DEFAULT NULL,
  `razorpay_signature` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `expense_id` bigint DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_method` enum('CASH','CARD','UPI','BANK','WALLET','RAZORPAY') NOT NULL,
  `title` varchar(255) NOT NULL,
  `transaction_reference` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `access_valid_until` datetime(6) DEFAULT NULL,
  `feature_code` varchar(255) DEFAULT NULL,
  `payer_email` varchar(255) DEFAULT NULL,
  `provider_name` varchar(255) DEFAULT NULL,
  `provider_order_id` varchar(255) DEFAULT NULL,
  `provider_payment_id` varchar(255) DEFAULT NULL,
  `provider_signature` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `UKc3w49re3w3eiexjdnm9khcsd8` (`razorpay_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (5,199,'2026-04-29 15:02:05.650882','INR',NULL,NULL,NULL,'COMPLETED',6,NULL,'Recurring transactions access payment','2026-04-29 15:02:56.025335','RAZORPAY','Recurring Access Plan','recurring_6_1777455125346','2026-04-29 15:02:56.028336','2026-05-29 15:02:56.025335','RECURRING_ACCESS','virat@gmail.com','RAZORPAY','order_SjGBaiFt9sbwpb','pay_SjGCDlji935BSv','91f870ffdf3ed5e4e470ce91d2f08f1c5d347ad4f4b8b0706dbcc59853cbd049'),(6,199,'2026-04-29 15:07:32.038326','INR',NULL,NULL,NULL,'COMPLETED',23,NULL,'Recurring transactions access payment','2026-04-29 15:08:24.896676','RAZORPAY','Recurring Access Plan','recurring_23_1777455451774','2026-04-29 15:08:24.898676','2026-05-29 15:08:24.896676','RECURRING_ACCESS','mishrayush0503@gmail.com','RAZORPAY','order_SjGHKzTsuyflKC','pay_SjGI0wGeVtAklG','a693c49643f981b6e8a9149581403059a9b499a38d2acc7d942e1409b5191f62'),(7,199,'2026-04-29 15:12:18.242801','INR',NULL,NULL,NULL,'COMPLETED',16,NULL,'Recurring transactions access payment','2026-04-29 15:13:46.567966','RAZORPAY','Recurring Access Plan','recurring_16_1777455738153','2026-04-29 15:13:46.576997','2026-05-29 15:13:46.567966','RECURRING_ACCESS','harshal@gmail.com','RAZORPAY','order_SjGMNPWhcrUiue','pay_SjGNg043odKbDz','4c73471bd139e1f35f3a16384d6de16324dc94e6a76f5199d0c6d3fcb7db5f53'),(8,199,'2026-04-29 16:34:14.055645','INR',NULL,NULL,NULL,'COMPLETED',25,NULL,'Recurring transactions access payment','2026-04-29 16:34:38.968555','RAZORPAY','Recurring Access Plan','recurring_25_1777460653715','2026-04-29 16:34:38.970574','2026-05-29 16:34:38.968555','RECURRING_ACCESS','kushwahaabhishek100k@gmail.com','RAZORPAY','order_SjHkv8DXIEQQIu','pay_SjHl5zXr4H3wfs','a37c25771be9bde870062c882e2b48f600962876cd3159899a0195178a9effbf'),(9,199,'2026-04-29 21:34:49.894721','INR',NULL,NULL,NULL,'COMPLETED',26,NULL,'Recurring transactions access payment','2026-04-29 21:35:15.386743','RAZORPAY','Recurring Access Plan','recurring_26_1777478689068','2026-04-29 21:35:15.395022','2026-05-29 21:35:15.386743','RECURRING_ACCESS','ajaykushwaha9770@gmail.com','RAZORPAY','order_SjMsKkN7YOzh7o','pay_SjMsVsY5N7t7ZQ','009adf3c7459b64096c58d627b1e7d40c51cc2c234dab7e007f58b7f00c388c3'),(10,199,'2026-04-30 10:08:33.509336','INR',NULL,NULL,NULL,'COMPLETED',28,NULL,'Recurring transactions access payment','2026-04-30 10:08:59.581757','RAZORPAY','Recurring Access Plan','recurring_28_1777523913134','2026-04-30 10:08:59.585169','2026-05-30 10:08:59.583170','RECURRING_ACCESS','ayushmagarde03@gmail.com','RAZORPAY','order_SjZiZlc8qxxBO3','pay_SjZiifdIuRFeBo','015a2ef79b35613d680df6ccd64b59380ffe269d5dcb36f33dd2a9a5d9aa3a74'),(11,199,'2026-04-30 16:49:31.579069','INR',NULL,NULL,NULL,'COMPLETED',29,NULL,'Recurring transactions access payment','2026-04-30 16:49:59.573230','RAZORPAY','Recurring Access Plan','recurring_29_1777547970962','2026-04-30 16:49:59.576737','2026-05-30 16:49:59.573230','RECURRING_ACCESS','VEER.IGT@gmail.com','RAZORPAY','order_SjgY7rYV77MYZp','pay_SjgYJ8wRLxXcH8','e7c1f4316e5ba46e44e0f70611fdbd56a49a0fcda94b4d8bc060811269761af9'),(12,199,'2026-05-02 10:50:30.308639','INR',NULL,NULL,NULL,'COMPLETED',32,NULL,'Recurring transactions access payment','2026-05-02 10:51:00.439854','RAZORPAY','Recurring Access Plan','recurring_32_1777699229556','2026-05-02 10:51:00.451869','2026-06-01 10:51:00.439854','RECURRING_ACCESS','romario@gmail.com','RAZORPAY','order_SkNV7HDIu4SBnb','pay_SkNVK0MzEdr27R','ddc113755f9a54eed69df4c602bb595cb81108b7b033e5af2b6c6c7644d61657'),(13,199,'2026-05-03 13:07:44.231598','INR',NULL,NULL,NULL,'COMPLETED',33,NULL,'Recurring transactions access payment','2026-05-03 13:08:11.893811','RAZORPAY','Recurring Access Plan','recurring_33_1777793863007','2026-05-03 13:08:11.903224','2026-06-02 13:08:11.894811','RECURRING_ACCESS','abde@gmail.com','RAZORPAY','order_SkoNCF1jczZeoj','pay_SkoNPhr6fG4pBz','a99a4c6999384de4aee936c3c3872d0a22a51444badbcbd924a810e13555dac6'),(14,199,'2026-05-04 16:31:00.529327','INR',NULL,NULL,NULL,'COMPLETED',34,NULL,'Recurring transactions access payment','2026-05-04 16:31:35.861853','RAZORPAY','Recurring Access Plan','recurring_34_1777892460084','2026-05-04 16:31:35.865853','2026-06-03 16:31:35.861853','RECURRING_ACCESS','mishrayush0503@gmail.com','RAZORPAY','order_SlGN3wcEku8P2v','pay_SlGNPq98q90rMt','4c7f077b4e7dd173c5fb96db6b9799e10a3341497bfe6e9ebf27c806f1e31e0d'),(15,199,'2026-05-08 12:12:29.695559','INR',NULL,NULL,NULL,'COMPLETED',36,NULL,'Recurring transactions access payment','2026-05-08 12:12:56.354840','RAZORPAY','Recurring Access Plan','recurring_36_1778222548885','2026-05-08 12:12:56.365605','2026-06-07 12:12:56.354840','RECURRING_ACCESS','abhishekkushwaha17032003@gmail.com','RAZORPAY','order_Smm6Sd9UV2wjHe','pay_Smm6fj5BB2iZIa','fd629b831e986d400b8fc67801ffeaba9ab8d5fde42e24bddd7283e95ad7fe94'),(16,199,'2026-05-13 19:29:29.158133','INR',NULL,NULL,NULL,'COMPLETED',39,NULL,'Recurring transactions access payment','2026-05-13 19:29:55.451402','RAZORPAY','Recurring Access Plan','recurring_39_1778700567383','2026-05-13 19:29:55.467111','2026-06-12 19:29:55.451514','RECURRING_ACCESS','jatin@gmail.com','RAZORPAY','order_SoxqIdBvlkwprs','pay_SoxqUNpYguYHKs','9f3bb6251e44cd726b70d72ffd238f5848b68c7a1f091dd63388b2e792f0f34c'),(17,199,'2026-05-14 09:51:04.371014','INR',NULL,NULL,NULL,'COMPLETED',4,NULL,'Recurring transactions access payment','2026-05-14 09:51:34.876628','RAZORPAY','Recurring Access Plan','recurring_4_1778752262458','2026-05-14 09:51:34.896210','2026-06-13 09:51:34.876710','RECURRING_ACCESS','mishrayush0503@gmail.com','RAZORPAY','order_SpCWRhxppcuoyz','pay_SpCWgSHECW11PZ','129a25da91efeb8708c1221591864c2128061a7413864c8dbb2ce040cee5979b');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `spendsmart_recurring_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `spendsmart_recurring_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `spendsmart_recurring_db`;

--
-- Table structure for table `recurring`
--

DROP TABLE IF EXISTS `recurring`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recurring` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `amount` double DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `frequency` varchar(255) DEFAULT NULL,
  `next_execution_date` date DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` enum('EXPENSE','INCOME') DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `last_reminder_sent_at` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recurring`
--

LOCK TABLES `recurring` WRITE;
/*!40000 ALTER TABLE `recurring` DISABLE KEYS */;
INSERT INTO `recurring` VALUES (1,_binary '',499,1,'MONTHLY','2026-04-25','2026-04-24','Netflix Subscription','EXPENSE',1,NULL),(2,_binary '',5000,NULL,'MONTHLY','2026-04-26','2026-04-26','Room Rent','EXPENSE',3,NULL),(4,_binary '',5000,NULL,'MONTHLY','2026-05-28','2026-04-28','Room Rent','EXPENSE',16,NULL),(5,_binary '',3000,NULL,'MONTHLY','2026-05-28','2026-04-28','Rent','EXPENSE',19,NULL),(6,_binary '',10000,NULL,'MONTHLY','2026-05-29','2026-04-29','Match','EXPENSE',6,NULL),(7,_binary '',3000,NULL,'MONTHLY','2026-05-29','2026-04-29','Fees','EXPENSE',20,NULL),(8,_binary '',3000,NULL,'MONTHLY','2026-05-29','2026-04-29','Fees','EXPENSE',21,NULL),(10,_binary '',10000,NULL,'MONTHLY','2026-05-29','2026-04-29','Gym','EXPENSE',6,NULL),(11,_binary '',2000,NULL,'MONTHLY','2026-05-29','2026-04-29','Fees','EXPENSE',25,NULL),(12,_binary '',350,NULL,'MONTHLY','2026-05-29','2026-04-29','Recharge','EXPENSE',25,NULL),(13,_binary '',3000,NULL,'MONTHLY','2026-05-29','2026-04-29','Room Rent','EXPENSE',26,NULL),(14,_binary '',2000,NULL,'MONTHLY','2026-05-29','2026-04-29','Fees','EXPENSE',26,NULL),(15,_binary '',999,NULL,'MONTHLY','2026-05-29','2026-04-29','Recharge','EXPENSE',26,NULL),(16,_binary '',999,NULL,'MONTHLY','2026-05-30','2026-04-30','Recharge','EXPENSE',29,NULL),(17,_binary '',15000,NULL,'MONTHLY','2026-06-02','2026-05-02','Fitness','EXPENSE',6,NULL),(18,_binary '',55000,NULL,'MONTHLY','2026-06-02','2026-05-02','Business','INCOME',6,NULL),(19,_binary '',999,NULL,'MONTHLY','2026-06-03','2026-05-03','Recharge','EXPENSE',33,NULL),(20,_binary '',10000,NULL,'MONTHLY','2026-06-03','2026-05-03','Bat','EXPENSE',33,NULL),(21,_binary '',999,NULL,'MONTHLY','2026-06-04','2026-05-04','Mobile Recharge','EXPENSE',34,NULL),(22,_binary '',999,NULL,'MONTHLY','2026-06-08','2026-05-08','Recharge','EXPENSE',36,NULL),(23,_binary '',999,NULL,'MONTHLY','2026-06-14','2026-05-14','Mobile Recharge','EXPENSE',4,NULL);
/*!40000 ALTER TABLE `recurring` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-14 17:55:22
