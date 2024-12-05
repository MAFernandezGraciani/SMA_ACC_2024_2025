<?php
/**
*
* description : Recurso para grabar los datos de una partida (game) de walkers en la BBDD
* @author : MiguelAngel Fernandez Graciani
* @date  : 2023-04-16
*
* @resourcename : saveGame.php
* @resourcetype PAGINA PHP
* @version : 001
* @historical
*
* Comentarios:
*		- Normalmente este recurso se llama desde la aplicacoin walkers programada en Unity para la asignatura de Inteligencia Artificial en videojuegos
*
*/

session_start();
require_once('conexion.php');

$respuesta = "";

// Esto esta en conexion.php
//$Bdd_host="127.0.0.1"; // Ver "BBDD_walkers_v001.sql"
//$Bdd='BBDD_walkers'; // Ver "BBDD_walkers_v001.sql"
//$Bdd_user='usr_walkers'; // Ver "BBDD_walkers_v001.sql"
//$Bdd_Pass='ut73md98bb7h'; // Ver "BBDD_walkers_v001.sql"


// Obtenemos las variables que vienen en el POST

$Competition = $_POST["Competition"];	// es directamente dato
$timeLeftString = $_POST["timeLeftString"];	// es directamente dato
$Game_winner = $_POST["Game_winner"];	// es directamente dato

$Game_creation_time_clk_clinet = $_POST["Game_creation_time_clk_clinet"];	// es directamente dato

$Team_near = $_POST["Team_near"];	// Hay que buscar id
$Player_mode_near = $_POST["Player_mode_near"];	// Hay que buscar id
$profitsPlayer_Near = $_POST["profitsPlayer_Near"];	//  es directamente dato
$livesPlayer_Near = $_POST["livesPlayer_Near"];	//  es directamente dato

$Player_ai_algorithm_near = $_POST["Player_ai_algorithm_near"];	// Hay que buscar id
$Minion_ai_algorithm_near = $_POST["Minion_ai_algorithm_near"];	//  Hay que buscar id

$Team_far = $_POST["Team_far"];	// Cadena de concepto
$Player_mode_far = $_POST["Player_mode_far"];	// Cadena de concepto
$profitsPlayer_far = $_POST["profitsPlayer_far"];	// Cadena de concepto
$livesPlayer_far = $_POST["livesPlayer_far"];	// Cadena de concepto

$Player_ai_algorithm_far = $_POST["Player_ai_algorithm_far"];	// Hay que buscar id
$Minion_ai_algorithm_far = $_POST["Minion_ai_algorithm_far"];	//  Hay que buscar id

// Definimos variables que se generan aqui

$Game_creation_time_clk_host = time();

$time_string = date('Y/m/d H:i:s', $Game_creation_time_clk_host);
$Game_name = "Game_".$time_string;

// //////////////////////////////////////////
// //////////////////////////////////////////
// Grabamos en la BBDD
$conexion_BBDD = conect_Bdd($Bdd_host,$Bdd_user,$Bdd_Pass,$Bdd);

// Information from client
$Game_HTTP_USER_AGENT = $_SERVER['HTTP_USER_AGENT'];
$Game_REMOTE_ADDR = $_SERVER["REMOTE_ADDR"];
$Game_REMOTE_PORT = $_SERVER["REMOTE_PORT"];

// Game information
$IdGame_winner = get_value("programmers","IdProgrammer","Programmer_name",$Game_winner,$conexion_BBDD); // We need the name (not the id)
$int_timeLeftString =  intval($timeLeftString); 
$IdCompetition = get_value("competitions","IdCompetition","competition_name",$Competition,$conexion_BBDD);

$IdTeam_near = get_value("programmers","IdProgrammer","Programmer_name",$Team_near,$conexion_BBDD);
$IdPlayer_mode_near = get_value("player_modes","IdPlayer_mode","Player_mode_name",$Player_mode_near,$conexion_BBDD);
$IdPlayer_ai_algorithm_near = 1; //Player_ai_algorithm_near;
$IdMinion_ai_algorithm_near = 1; //Minion_ai_algorithm_near;

$IdTeam_far = get_value("programmers","IdProgrammer","Programmer_name",$Team_far,$conexion_BBDD);
$IdPlayer_mode_far = get_value("player_modes","IdPlayer_mode","Player_mode_name",$Player_mode_far,$conexion_BBDD); 
$IdPlayer_ai_algorithm_far = 1; //Player_ai_algorithm_far;
$IdMinion_ai_algorithm_far = 1; //Minion_ai_algorithm_far;

// Insertamos el registro en la BBDD
$str="insert into games(Game_name, 
						Game_creation_time_clk_clinet, 
						Game_creation_time_clk_host,
						Game_HTTP_USER_AGENT,
						Game_REMOTE_ADDR,
						Game_REMOTE_PORT,
						IdGame_winner, 
						timeLeftString, 
						IdCompetition, 
						IdTeam_near, 
						IdPlayer_mode_near, 
						profitsPlayer_Near, 
						livesPlayer_Near, 
						IdPlayer_ai_algorithm_near, 
						IdMinion_ai_algorithm_near, 
						IdTeam_far, 
						IdPlayer_mode_far, 
						profitsPlayer_far, 
						livesPlayer_far, 
						IdPlayer_ai_algorithm_far, 
						IdMinion_ai_algorithm_far)
values ('".$Game_name."', 
		".$Game_creation_time_clk_clinet.", 
		".$Game_creation_time_clk_host.",
		'".$Game_HTTP_USER_AGENT."',
		'".$Game_REMOTE_ADDR."',
		'".$Game_REMOTE_PORT."',
		".$IdGame_winner.", 
		".$int_timeLeftString.", 
		".$IdCompetition.",
		".$IdTeam_near.",
		".$IdPlayer_mode_near.",
		".$profitsPlayer_Near.",
		".$livesPlayer_Near.",
		".$IdPlayer_ai_algorithm_near.",
		".$IdMinion_ai_algorithm_near.",
		".$IdTeam_far.",
		".$IdPlayer_mode_far.",
		".$profitsPlayer_far.",
		".$livesPlayer_far.",
		".$IdPlayer_ai_algorithm_far.",
		".$IdMinion_ai_algorithm_far.");";

$respuesta = $respuesta." -- Consulta SQL  = ".$str;

$rs=mysqli_query($conexion_BBDD, $str);

if ($rs) 
{
	$IdInsertado = mysqli_insert_id($conexion_BBDD);
	$respuesta = $respuesta." -- Resultado Consulta SQL OK. To mu bien al insertar el registro con IdInsertado = ".$IdInsertado;
} 
else 
{
	$respuesta = $respuesta." -- Resultado Consulta SQL KO. Error: este mismico";
}



// //////////////////////////////////////////
// //////////////////////////////////////////
// Construimos respuesta de prueba
$respuesta = $respuesta." -- Competition = ".$Competition;
$respuesta = $respuesta." -- timeLeftString = ".$timeLeftString;
$respuesta = $respuesta." -- Game_winner = ".$Game_winner;

$respuesta = $respuesta." -- Game_creation_time_clk_clinet = ".$Game_creation_time_clk_clinet;

$respuesta = $respuesta." -- Team_near = ".$Team_near;
$respuesta = $respuesta." -- Player_mode_near = ".$Player_mode_near;
$respuesta = $respuesta." -- profitsPlayer_Near = ".$profitsPlayer_Near;
$respuesta = $respuesta." -- livesPlayer_Near = ".$livesPlayer_Near;

$respuesta = $respuesta." -- Player_ai_algorithm_near = ".$Player_ai_algorithm_near;
$respuesta = $respuesta." -- Minion_ai_algorithm_near = ".$Minion_ai_algorithm_near;

$respuesta = $respuesta." -- Team_far = ".$Team_far;
$respuesta = $respuesta." -- Player_mode_far = ".$Player_mode_far;
$respuesta = $respuesta." -- profitsPlayer_far = ".$profitsPlayer_far;
$respuesta = $respuesta." -- livesPlayer_far = ".$livesPlayer_far;

$respuesta = $respuesta." -- Player_ai_algorithm_far = ".$Player_ai_algorithm_far;
$respuesta = $respuesta." -- Minion_ai_algorithm_far = ".$Minion_ai_algorithm_far;

@mysqli_close($conexion_BBDD);
echo $respuesta;



?>