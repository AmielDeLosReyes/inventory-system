-- MySQL dump 10.13  Distrib 8.0.34, for macos13 (arm64)
--
-- Host: 127.0.0.1    Database: inventory_management
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `status_code` varchar(255) DEFAULT NULL,
  `added_by` varchar(255) DEFAULT 'SYSTEM',
  `added_date` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT 'SYSTEM',
  `modified_date` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Marta',50.00,'0','admin','2024-09-05 14:17:02','admin','2024-09-05 14:17:02'),(2,'marta',45.00,'0','admin','2024-09-05 14:17:39','admin','2024-09-05 14:17:39'),(3,'olivia',55.00,'0','admin','2024-09-05 14:17:58','admin','2024-09-05 14:17:58'),(4,'georgina',35.00,'1','admin','2024-09-05 14:18:25','admin','2024-09-05 14:18:25'),(5,'mia',40.00,'1','admin','2024-09-05 14:18:40','admin','2024-09-05 14:18:40'),(6,'matilda',40.00,'1','admin','2024-09-05 14:18:51','admin','2024-09-05 14:18:51'),(7,'lily',40.00,'1','admin','2024-09-05 14:18:59','admin','2024-09-05 14:18:59'),(8,'oda',40.00,'1','admin','2024-09-05 14:19:06','admin','2024-09-05 14:19:06'),(9,'crista',70.00,'1','admin','2024-09-05 14:19:18','admin','2024-09-05 14:19:18'),(10,'ava',70.00,'1','admin','2024-09-05 14:19:41','admin','2024-09-05 14:19:41'),(11,'serenity',40.00,'1','admin','2024-09-05 14:20:01','admin','2024-09-05 14:20:01'),(12,'sophia',40.00,'1','admin','2024-09-05 14:20:11','admin','2024-09-05 14:20:11'),(13,'amara',16.00,'1','admin','2024-09-05 14:20:20','admin','2024-09-05 14:20:20'),(14,'lana',40.00,'1','admin','2024-09-05 14:20:34','admin','2024-09-05 14:20:34'),(15,'aria',50.00,'1','admin','2024-09-05 14:20:42','admin','2024-09-05 14:20:42'),(16,'athena',30.00,'1','admin','2024-09-05 14:20:53','admin','2024-09-05 14:20:53'),(17,'cristina',40.00,'1','admin','2024-09-05 14:21:08','admin','2024-09-05 14:21:08'),(18,'casey',40.00,'1','admin','2024-09-05 14:21:57','admin','2024-09-05 14:21:57'),(19,'rosie',40.00,'1','admin','2024-09-05 14:22:06','admin','2024-09-05 14:22:06'),(20,'stella',85.00,'1','admin','2024-09-05 14:22:15','admin','2024-09-05 14:22:15'),(21,'josie',40.00,'1','admin','2024-09-05 14:22:31','admin','2024-09-05 14:22:31'),(22,'kara',80.00,'1','admin','2024-09-05 14:22:44','admin','2024-09-05 14:22:44'),(23,'olivia',50.00,'1','admin','2024-09-05 14:23:01','admin','2024-09-05 14:23:01'),(24,'blythe',75.00,'1','admin','2024-09-05 14:23:11','admin','2024-09-05 14:23:11'),(25,'sheena',85.00,'1','admin','2024-09-05 14:23:20','admin','2024-09-05 14:23:20'),(26,'colet',75.00,'1','admin','2024-09-05 14:23:32','admin','2024-09-05 14:23:32'),(27,'bini',75.00,'1','admin','2024-09-05 14:23:41','admin','2024-09-05 14:23:41'),(28,'marta',40.00,'1','admin','2024-09-05 14:23:55','admin','2024-09-05 14:23:55');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-09-13 16:52:25
