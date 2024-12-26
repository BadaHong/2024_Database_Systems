-- MySQL dump 10.13  Distrib 8.0.40, for macos15.1 (arm64)
--
-- Host: localhost    Database: music_service
-- ------------------------------------------------------
-- Server version	8.0.40

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

-- music_service 데이터베이스 구조 내보내기
DROP DATABASE IF EXISTS `music_service`;
CREATE DATABASE IF NOT EXISTS `music_service` /*!40100 DEFAULT CHARACTER SET utf8mb3 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `music_service`;

--
-- Table structure for table `addMusic`
--

DROP TABLE IF EXISTS `addMusic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addMusic` (
  `playlistTitle` varchar(50) NOT NULL,
  `playlistOwner` int NOT NULL,
  `musicId` int NOT NULL,
  PRIMARY KEY (`playlistTitle`,`playlistOwner`,`musicId`),
  KEY `addmusic_ibfk_1` (`musicId`),
  CONSTRAINT `addmusic_ibfk_1` FOREIGN KEY (`musicId`) REFERENCES `music` (`musicId`) ON DELETE CASCADE,
  CONSTRAINT `playlist_fk` FOREIGN KEY (`playlistTitle`, `playlistOwner`) REFERENCES `playlists` (`title`, `ownedBy`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addMusic`
--

LOCK TABLES `addMusic` WRITE;
/*!40000 ALTER TABLE `addMusic` DISABLE KEYS */;
INSERT INTO `addMusic` VALUES ('Happy',22,1),('k-pop',13,1),('Boy Group',21,2),('Girl Group',24,4),('Happy',22,4),('k-pop',13,4),('Girl Group',24,7),('Girl Group',24,8),('Boy Group',21,9),('k-pop',13,9),('Boy Group',21,11),('Happy',22,11),('Boy Group',21,13),('Happy',22,13),('Boy Group',21,14);
/*!40000 ALTER TABLE `addMusic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `albums`
--

DROP TABLE IF EXISTS `albums`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `albums` (
  `albumId` int NOT NULL AUTO_INCREMENT,
  `title` varchar(50) NOT NULL,
  `releaseDate` date DEFAULT NULL,
  PRIMARY KEY (`albumId`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `albums`
--

LOCK TABLES `albums` WRITE;
/*!40000 ALTER TABLE `albums` DISABLE KEYS */;
INSERT INTO `albums` VALUES (1,'Good Luck','2014-06-15'),(2,'Love Yourself: Tear','2018-05-18'),(6,'Butter','2022-10-10'),(8,'Super Lady','2024-02-02');
/*!40000 ALTER TABLE `albums` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artists`
--

DROP TABLE IF EXISTS `artists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artists` (
  `musicId` int NOT NULL,
  `artistName` varchar(30) NOT NULL,
  PRIMARY KEY (`musicId`,`artistName`),
  CONSTRAINT `artists_ibfk_1` FOREIGN KEY (`musicId`) REFERENCES `music` (`musicId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artists`
--

LOCK TABLES `artists` WRITE;
/*!40000 ALTER TABLE `artists` DISABLE KEYS */;
INSERT INTO `artists` VALUES (1,'BEAST'),(2,'BTS'),(4,'Bruno Mars'),(4,'Rose'),(7,'NewJeans'),(8,'(G)I-DLE'),(9,'BTS'),(11,'BTS'),(12,'BEAST'),(13,'HIGHLIGHT(BEAST)'),(14,'BTS'),(16,'NFlying');
/*!40000 ALTER TABLE `artists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `likes`
--

DROP TABLE IF EXISTS `likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `likes` (
  `userId` int NOT NULL,
  `musicId` int NOT NULL,
  PRIMARY KEY (`userId`,`musicId`),
  KEY `musicId` (`musicId`),
  CONSTRAINT `musicId` FOREIGN KEY (`musicId`) REFERENCES `music` (`musicId`) ON DELETE CASCADE,
  CONSTRAINT `userId` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `likes`
--

LOCK TABLES `likes` WRITE;
/*!40000 ALTER TABLE `likes` DISABLE KEYS */;
INSERT INTO `likes` VALUES (13,1),(21,1),(22,1),(23,1),(26,1),(13,2),(21,2),(23,2),(13,4),(24,4),(26,4),(22,7),(23,7),(21,8),(13,9),(21,9),(23,9),(26,9),(13,11),(21,11),(22,11),(23,11),(24,11),(25,11),(26,11),(21,12),(24,12),(13,13),(22,13),(24,13),(25,13),(26,13),(13,14),(21,14),(22,14),(26,14);
/*!40000 ALTER TABLE `likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `managers`
--

DROP TABLE IF EXISTS `managers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `managers` (
  `managerId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`managerId`),
  UNIQUE KEY `name_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `managers`
--

LOCK TABLES `managers` WRITE;
/*!40000 ALTER TABLE `managers` DISABLE KEYS */;
INSERT INTO `managers` VALUES (10,'hong','9c2364ba04c712fb921ed288c94cb6b14b85f9d342377d4f007ee4da089779da'),(11,'bada','5b39c3df32b27ab42ba8fadee41beb427a45c73fbb5a35b16a4d66098bb1d39f'),(12,'admin','8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918'),(13,'admin2','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),(15,'haneul','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4');
/*!40000 ALTER TABLE `managers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `music`
--

DROP TABLE IF EXISTS `music`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `music` (
  `musicId` int NOT NULL AUTO_INCREMENT,
  `managedBy` int NOT NULL,
  `albumId` int DEFAULT NULL,
  `title` varchar(100) NOT NULL,
  `lyricist` varchar(30) DEFAULT NULL,
  `composer` varchar(30) DEFAULT NULL,
  `genre` varchar(30) DEFAULT NULL,
  `lyrics` text,
  `releaseDate` date DEFAULT NULL,
  PRIMARY KEY (`musicId`),
  KEY `managedBy` (`managedBy`),
  KEY `albumId` (`albumId`),
  CONSTRAINT `albumId` FOREIGN KEY (`albumId`) REFERENCES `albums` (`albumId`),
  CONSTRAINT `music_ibfk_1` FOREIGN KEY (`managedBy`) REFERENCES `managers` (`managerId`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `music`
--

LOCK TABLES `music` WRITE;
/*!40000 ALTER TABLE `music` DISABLE KEYS */;
INSERT INTO `music` VALUES (1,10,1,'How To Love','Yong Jun-hyung','Yong Jun-hyung','ballad','How to love 우린 얼마나 특별할까\nHow to love 난 아직도 모르겠어 사랑하는 법','2013-07-19'),(2,10,2,'Fake Love','RM','RM','K-pop','널 위해서라면 난 슬퍼도 기쁜 척 할 수가 있었어\n널 위해서라면 난 아파도 강한 척 할 수가 있었어\n사랑이 사랑만으로 완벽하길 내 모든 약점들은 다 숨겨지길\n이뤄지지 않는 꿈속에서 피울 수 없는 꽃을 키웠어\nI\'m so sick of this fake love\nFake love, fake love\nI\'m so sorry but it\'s fake love\nFake love, fake love\nI wanna be a good man just for you\n세상을 줬네 just for you\n전부 바꿨어 just for you\nNow I don\'t know me, who are you?','2018-05-18'),(4,12,NULL,'Apt.','Rose','Rose','pop','채영이가 좋아하는 랜덤 게임, 랜덤 게임\nGame start\n아파트, 아파트, 아파트, 아파트\n아파트, 아파트, uh, uh-huh, uh-huh\n아파트, 아파트, 아파트, 아파트\n아파트, 아파트, uh, uh-huh, uh-huh\nKissy face, kissy face sent to your phone, but\nI\'m trying to kiss your lips for real (uh-huh, uh-huh)\nRed hearts, red hearts, that\'s what I\'m on, yeah\nCome give me somethin\' I can feel, oh-oh-oh\nDon\'t you want me like I want you, baby?\nDon\'t you need me like I need you now?\nSleep tomorrow, but tonight go crazy\nAll you gotta do is just meet me at the\n아파트, 아파트, 아파트, 아파트\n아파트, 아파트, uh, uh-huh, uh-huh\n아파트, 아파트, 아파트, 아파트\n아파트, 아파트, uh, uh-huh, uh-huh','2024-10-18'),(7,12,NULL,'Super Shy','Sim-Ya Kim','FRNK','K-pop','I\'m super shy, super shy\nBut wait a minute while I make you mine, make you mine\n떨리는 지금도 you\'re on my mind all the time\nI wanna tell you, but I\'m super shy, super shy\nI\'m super shy, super shy\nBut wait a minute while I make you mine, make you mine\n떨리는 지금도 you\'re on my mind all the time\nI wanna tell you, but I\'m super shy, super shy\nAnd I wanna go out with you, where you wanna go? (Huh?)\nFind a lil\' spot, just sit and talk\nLooking pretty, follow me, 우리 둘이 나란히\n보이지? 내 눈이 갑자기 빛나지 when you say I\'m your dream\nYou don\'t even know my name, do you?\nYou don\'t even know my name, do you?\n누구보다도','2023-07-07'),(8,12,NULL,'Queencard','So-Yeon Jeon','So-Yeon Jeon','K-pop','Hey, you 뭘 보니?\n내가 좀 sexy, sexy 반했니?\nYeah, you 뭐 하니?\n너도 내 kiss, kiss 원하니?\n월, 화, 수, 목, 금, 토, 일, 미모가 쉬지를 않네\n머리부터 발끝까지 눈부셔 빛이 나네\nOh 저기 언니야들 내 fashion을 따라 하네\n아름다운 여자의 하루는 다 아름답네\n이 party에 준비된 birthday cake\n태어나서 감사해 every day\nI don\'t need them\n그래 내가 봐도 난 (three, two)\n퀸카 I\'m hot\nMy boob and booty is hot\nSpotlight 날 봐\nI\'m a star, star, star\n퀸카 I\'m top\nI\'m twerkin\' on the runway\nI am a 퀸카\nYou wanna be the 퀸카?','2023-05-15'),(9,15,6,'Butter','RM','RM','pop','Smooth like butter, like a criminal undercover\nGon\' pop like trouble breaking into your heart like that, ooh\nCool shade, stunner, yeah, I owe it all to my mother, uh\nHot like summer, yeah, I\'m making you sweat like that (break it down)\nOoh, when I look in the mirror\nI\'ll melt your heart into two\nI got that superstar glow, so\nOoh (do the boogie, like)','2022-10-10'),(11,15,6,'I NEED U','RM','RM','k-pop','I need you, girl\n왜 혼자 사랑하고? 혼자서만 이별해\nI need you, girl\n왜 다칠 걸 알면서? 자꾸 네가 필요해','2022-10-10'),(12,15,NULL,'Good Luck','Yong Jun-hyung','Yong Jun-hyung','k-pop','Good luck baby good luck to you 꼭 행복해야 해\n너만 보는 날 너 하나밖에 없던 날 두고 떠나갔다면\nGood luck baby good luck to you 누구를 만나도\n네가 내게 남긴 상처만큼 넌 더 행복해야 해 eyo','2014-07-15'),(13,15,NULL,'Plz Don\'t Be Sad','Yong Jun-hyung','Yong Jun-hyung','k-pop','얼굴 찌푸리지 말아요\nBaby 넌 웃는 게 더 예뻐\n그렇게 슬픈 표정하지 말아요\n널 보면 내 맘이 너무 아파 Oh Oh','2017-05-22'),(14,15,NULL,'Boy in Luv','RM','RM','hip-pop','되고파 너의 오빠\n너의 사랑이 난 너무 고파\n되고파 너의 오빠\n널 갖고 말 거야 두고 봐 (fire!)\n왜 내 맘을 흔드는 건데?\n왜 내 맘을 흔드는 건데?\n왜 내 맘을 흔드는 건데?\n흔드는 건데, 흔드는 건데?','2022-10-10'),(16,10,NULL,'Blue Moon','J.DON','J.DON','rock','푸르른 달에 맘을 도둑맞듯\n세상에 너와 나 단둘만 같았어\n이 순간이 잠시라면 영원히 멈춰 버리자\n지겨워져도 괜찮아 들어 줘 나의 blue moon\nOh, my blue moon\n희미한 말들로 너에게 닿을 수만 있다면\n시시한 일상 밖에 날 밀어둔 채로\n신비한 꿈처럼 너만을 바라보아서\n하루 종일 너에게 전하고 싶은 맘\n괜시리 기대하죠 이 문 뒤에선 왠지\n숨죽이며 날 기다리겠죠\n푸르른 날의 눈부신 밤 향기에 빠져서\n아리따운 너의 맘과 영원을 보고 싶어\n이 풍경 속에 홀리듯 내 몸이 잠겨 버리게\n푸르른 달아 텅 빈 그대 맘을 가득 채워 줘 (hey)','2023-03-03');
/*!40000 ALTER TABLE `music` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `playlists`
--

DROP TABLE IF EXISTS `playlists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `playlists` (
  `title` varchar(50) NOT NULL,
  `ownedBy` int NOT NULL,
  `isShared` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`title`,`ownedBy`),
  KEY `ownedBy` (`ownedBy`),
  CONSTRAINT `playlists_ibfk_1` FOREIGN KEY (`ownedBy`) REFERENCES `users` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `playlists`
--

LOCK TABLES `playlists` WRITE;
/*!40000 ALTER TABLE `playlists` DISABLE KEYS */;
INSERT INTO `playlists` VALUES ('Boy Group',21,1),('FANCY',13,1),('Girl Group',24,1),('Happy',22,1),('k-pop',13,1),('MINE',13,0);
/*!40000 ALTER TABLE `playlists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plays`
--

DROP TABLE IF EXISTS `plays`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plays` (
  `userId` int NOT NULL,
  `musicId` int NOT NULL,
  `numOfPlays` int DEFAULT '0',
  PRIMARY KEY (`userId`,`musicId`),
  KEY `plays_ibfk_2` (`musicId`),
  CONSTRAINT `plays_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`userId`) ON DELETE CASCADE,
  CONSTRAINT `plays_ibfk_2` FOREIGN KEY (`musicId`) REFERENCES `music` (`musicId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plays`
--

LOCK TABLES `plays` WRITE;
/*!40000 ALTER TABLE `plays` DISABLE KEYS */;
INSERT INTO `plays` VALUES (13,1,8),(13,2,2),(13,4,17),(13,8,1),(13,9,4),(13,11,2),(13,13,7),(21,1,4),(21,2,2),(21,8,6),(21,9,1),(21,11,6),(21,12,11),(21,14,1),(22,1,7),(22,4,1),(22,7,9),(22,11,5),(22,13,2),(22,14,5),(23,1,4),(23,7,2),(23,9,2),(23,11,1),(24,4,13),(24,11,1),(24,12,1),(24,13,3),(25,8,1),(25,9,3),(25,13,1),(26,1,4),(26,4,6),(26,9,3),(26,11,1),(26,13,4),(26,14,3);
/*!40000 ALTER TABLE `plays` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `userId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL,
  `password` varchar(255) NOT NULL,
  `managedBy` int DEFAULT NULL,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  KEY `managedBy` (`managedBy`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`managedBy`) REFERENCES `managers` (`managerId`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (13,'bada','5b39c3df32b27ab42ba8fadee41beb427a45c73fbb5a35b16a4d66098bb1d39f',10),(14,'user2','38083c7ee9121e17401883566a148aa5c2e2d55dc53bc4a94a026517dbff3c6b',13),(17,'user4','252f10c83610ebca1a059c0bae8255eba2f95be4d1d7bcfa89d7248a82d9f111',15),(21,'user3','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb',10),(22,'user5','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb',11),(23,'user6','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb',10),(24,'user7','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb',12),(25,'user8','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb',13),(26,'user9','04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb',10);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-12-11 19:01:20
