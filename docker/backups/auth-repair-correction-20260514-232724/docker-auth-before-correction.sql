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
-- Table structure for table `old_users_restore`
--

DROP TABLE IF EXISTS `old_users_restore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `old_users_restore` (
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
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `old_users_restore`
--

LOCK TABLES `old_users_restore` WRITE;
/*!40000 ALTER TABLE `old_users_restore` DISABLE KEYS */;
INSERT INTO `old_users_restore` VALUES (NULL,'2026-05-14 08:32:12.382908',44,'INR','anni@gmail.com','Anni Yadav','$2a$10$dzG7Xxx31Mfl6MymEyNScunZKuPeIipYV5nP9J1NijvZJOvdH5.KC','active','Asia/Kolkata'),(NULL,'2026-05-14 08:49:30.523133',45,'INR','virat@gmail.com','Virat Kohli','$2a$10$4iT.QaSHEE1R4Rp8OGn18utSniPPX7Dal07O4b7Easq1CL3XNlsGu','active','Asia/Kolkata'),(6000,'2026-05-14 09:46:15.876503',46,'INR','mishrayush0503@gmail.com','Ayush Mishra','$2a$10$/Ymw3U75pxQk745aalXw6e/3w81MLAZQuJsCe7Nqa4Fujl0BkgQfO','active','Asia/Kolkata'),(1300000,'2026-05-14 16:40:40.297753',47,'INR','anuragkushwahaji10k@gmail.com','Anurag ','$2a$10$ScjDu5M797Aisyx7nq2e5.zCThJlzNNt9GG6jMge.scBCpP1HdLy2','active','Asia/Kolkata'),(NULL,'2026-05-14 17:19:44.152876',48,'INR','rahul@gmail.com','Rahul','$2a$10$odiPOkpzg7ycq./aAeuJK.snAPxGyXosyJtQ7e/ifpV4tTpVG3KKm','active','Asia/Kolkata');
/*!40000 ALTER TABLE `old_users_restore` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Table structure for table `user_id_repair_map`
--

DROP TABLE IF EXISTS `user_id_repair_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_id_repair_map` (
  `old_docker_user_id` bigint NOT NULL DEFAULT '0',
  `repaired_user_id` bigint DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_id_repair_map`
--

LOCK TABLES `user_id_repair_map` WRITE;
/*!40000 ALTER TABLE `user_id_repair_map` DISABLE KEYS */;
INSERT INTO `user_id_repair_map` VALUES (1,44,'anni@gmail.com','2026-05-14 08:32:12.382908'),(3,45,'virat@gmail.com','2026-05-14 08:49:30.523133'),(4,46,'mishrayush0503@gmail.com','2026-05-14 09:46:15.876503'),(5,47,'anuragkushwahaji10k@gmail.com','2026-05-14 16:40:40.297753'),(6,48,'rahul@gmail.com','2026-05-14 17:19:44.152876');
/*!40000 ALTER TABLE `user_id_repair_map` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (NULL,'2026-05-14 08:32:12.382908',44,'INR','anni@gmail.com','Anni Yadav','$2a$10$dzG7Xxx31Mfl6MymEyNScunZKuPeIipYV5nP9J1NijvZJOvdH5.KC','active','Asia/Kolkata'),(NULL,'2026-05-14 08:49:30.523133',45,'INR','virat@gmail.com','Virat Kohli','$2a$10$4iT.QaSHEE1R4Rp8OGn18utSniPPX7Dal07O4b7Easq1CL3XNlsGu','active','Asia/Kolkata'),(6000,'2026-05-14 09:46:15.876503',46,'INR','mishrayush0503@gmail.com','Ayush Mishra','$2a$10$/Ymw3U75pxQk745aalXw6e/3w81MLAZQuJsCe7Nqa4Fujl0BkgQfO','active','Asia/Kolkata'),(1300000,'2026-05-14 16:40:40.297753',47,'INR','anuragkushwahaji10k@gmail.com','Anurag ','$2a$10$ScjDu5M797Aisyx7nq2e5.zCThJlzNNt9GG6jMge.scBCpP1HdLy2','active','Asia/Kolkata'),(NULL,'2026-05-14 17:19:44.152876',48,'INR','rahul@gmail.com','Rahul','$2a$10$odiPOkpzg7ycq./aAeuJK.snAPxGyXosyJtQ7e/ifpV4tTpVG3KKm','active','Asia/Kolkata');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_docker_before_repair`
--

DROP TABLE IF EXISTS `users_docker_before_repair`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_docker_before_repair` (
  `monthly_budget` double DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL DEFAULT '0',
  `currency` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `timezone` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_docker_before_repair`
--

LOCK TABLES `users_docker_before_repair` WRITE;
/*!40000 ALTER TABLE `users_docker_before_repair` DISABLE KEYS */;
INSERT INTO `users_docker_before_repair` VALUES (NULL,'2026-05-14 08:32:12.382908',1,'INR','anni@gmail.com','Anni Yadav','$2a$10$dzG7Xxx31Mfl6MymEyNScunZKuPeIipYV5nP9J1NijvZJOvdH5.KC','active','Asia/Kolkata'),(NULL,'2026-05-14 08:49:30.523133',3,'INR','virat@gmail.com','Virat Kohli','$2a$10$4iT.QaSHEE1R4Rp8OGn18utSniPPX7Dal07O4b7Easq1CL3XNlsGu','active','Asia/Kolkata'),(6000,'2026-05-14 09:46:15.876503',4,'INR','mishrayush0503@gmail.com','Ayush Mishra','$2a$10$/Ymw3U75pxQk745aalXw6e/3w81MLAZQuJsCe7Nqa4Fujl0BkgQfO','active','Asia/Kolkata'),(1300000,'2026-05-14 16:40:40.297753',5,'INR','anuragkushwahaji10k@gmail.com','Anurag ','$2a$10$ScjDu5M797Aisyx7nq2e5.zCThJlzNNt9GG6jMge.scBCpP1HdLy2','active','Asia/Kolkata'),(NULL,'2026-05-14 17:19:44.152876',6,'INR','rahul@gmail.com','Rahul','$2a$10$odiPOkpzg7ycq./aAeuJK.snAPxGyXosyJtQ7e/ifpV4tTpVG3KKm','active','Asia/Kolkata');
/*!40000 ALTER TABLE `users_docker_before_repair` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-14 17:57:24
