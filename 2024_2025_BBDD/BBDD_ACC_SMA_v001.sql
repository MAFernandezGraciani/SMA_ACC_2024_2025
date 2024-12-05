/*
SQLyog Community Edition- MySQL GUI v8.05 

Autor : Migel A. Fcez. Graciani

Fecha de creacion : 2024-11-05

Ultima modificacion :

Generacion de BBDD para el ejercicio de prácticas ACC de SMA

INSTRUCCIONES DE USO
1) Crear la BBDD en MySql
2) Hacer correr el script
5) Gestinoar los usuarios de acceso a la BBDD

Observaciones:
	DEBE ESTAR TODO EN UTF-8
	
usr : usrAcc
pswd : hd83mmi8812a
	
 *******  Fin de pendientes de hacer  */

/*!40101 SET NAMES utf8 */;
/*!40101 SET SQL_MODE=''*/;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

USE `acc_bbdd`;  /* Aqui se identifica la base de datos  */

SET FOREIGN_KEY_CHECKS=0; /* Anulamos los enlaces de FOREIGN_KEY para poder borrar las tablas */

/*  ******* Definimos algunas variables para utilizarlas a lo largo del script **************************  */

SET @claveUsuario = "administrador_ACC";  /* Indica el usuario por defecto */

/*  Permisos de acceso */

/* 1. ****** tabla de sesiones ACC 
		Cada sesion conlleva un conjunto de datos asociados a una configuracion de ejecucion
		********************* */

DROP TABLE IF EXISTS `sesiones`;

CREATE TABLE `sesiones` (
  `IdSesion` int unsigned NOT NULL auto_increment,  /* Identificador de sesion en esta tabla */
  `Nombre_sesion` varchar(255) NOT NULL,  /* nombre de la sision se define desde el monitor en el codigo de ACC */
  `date_sesion` bigint(64) unsigned NOT NULL, /* indica el momento en el que se arranca la sesion en el monitor en formato unix  */
  /* common_content */
  PRIMARY KEY  (`IdSesion`)
); 

/*  Datos de sesion para pruebas ***********  */
insert  into `sesiones`(IdSesion, Nombre_sesion, date_sesion)
	values	(0, "Todas", 0);
		  
/* 2. ****** tabla basica de mensajes de ACC  ********************* */

DROP TABLE IF EXISTS `Message`;

CREATE TABLE `Message` (
  `IdMessage` bigint(64) unsigned NOT NULL auto_increment,  /* Identificador de mensaje en esta tabla */
  `IdSesion` int unsigned NOT NULL,  /* Identificador de sesion en esta tabla "sesiones" */
  `comunc_id` varchar(255) NOT NULL,  /* identificador de comunicacion (conversacion) */
  `msg_id` varchar(255) NOT NULL,  /* identificador del mensage en ACC */
  /* header */
  `type_protocol` varchar(255) NOT NULL,  /* identificador del protocolo en ACC */
  `protocol_step` int(3) unsigned NOT NULL,  /* Indica el ordinal del paso dentro del protocolo ACC */
  `comunication_protocol` varchar(4) NOT NULL,  /* TCP o UDP */
	  /* origin */
  `origin_id` varchar(255) NOT NULL,  /* identificador del agente origen*/
  `origin_ip` varchar(255) NOT NULL,  /* IP del agente origen*/
  `origin_port` int(10) unsigned NOT NULL,  /* Puerto del agente origen*/
  `origin_time` bigint(64) unsigned NOT NULL,  /*momento en el que se genera el mensaje en el reloj del agente origen*/
	  /* destination */
  `destination_id` varchar(255) NOT NULL,  /* identificador del agente destino */
  `destination_ip` varchar(255) NOT NULL,  /* IP del agente destino */
  `destination_port` int(10) unsigned NOT NULL,  /* Puerto del agente destino */
  `destination_time` bigint(64) unsigned NOT NULL,  /*momento en el que se recibe el el mensaje en la cola de mensajes recibidos, segun el reloj del agente destino */
  /* body */
  `body_info` varchar(255) NOT NULL, /* Cuerpo del mensage */
  /* common_content */
  FOREIGN KEY(`IdSesion`) REFERENCES sesiones(`IdSesion`),
  PRIMARY KEY  (`IdMessage`)
); 

/*  Conceptos basicos para este DKS especifico ***********  */
insert  into `Message`(`IdMessage`,
					  `IdSesion`, 
					  `comunc_id`,
					  `msg_id`,
					  `type_protocol`,
					  `protocol_step`,
					  `comunication_protocol`,
					  `origin_id`,
					  `origin_ip`,
					  `origin_port`,
					  `origin_time`,
					  `destination_id`,
					  `destination_ip`,
					  `destination_port`,
					  `destination_time`,
					  `body_info`)
	values	(0,
		  0,
		  "comunc_id_de_0",
		  "msg_id_de_0",
		  "type_protocol_de_0",
		  0,
		  "XDP0_de_0",
		  "origin_id_de_0",
		  "origin_ip_de_0",
		  0,
		  0,
		  "destination_id_de_0",
		  "destination_ip_de_0",
		  0,
		  0,
		  "body_info_de_0");

SET FOREIGN_KEY_CHECKS=1; /* Activamos los enlaces de FOREIGN_KEY para que trabajen */

