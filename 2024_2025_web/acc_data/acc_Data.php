<?php
/**
*
* description : Recurso para mostrar los datos del sistema ACC en la asignatura de SMA 2024-2025
* @author : MiguelAngel Fernandez Graciani
* @date  : 2024-10-01
*
* @resourcename : 
* @resourcetype PAGINA PHP
* @version : 001
* @historical
*
* Comentarios:
*		- Para mostrar los datos del SMA ACC
*
*/

require_once('conexion.php');

// Esto esta en conexion.php
//$Bdd_host="127.0.0.1"; // Ver "BBDD_ACC_SMA_v001.sql"
//$Bdd='acc_bbdd'; // Ver "BBDD_ACC_SMA_v001.sql"
//$Bdd_user='usrAcc'; // Ver "BBDD_ACC_SMA_v001.sql"
//$Bdd_Pass='hd83mmi8812a'; // Ver "BBDD_ACC_SMA_v001.sql"

// Conectamos con la BBDD
$conexion_BBDD = conect_Bdd($Bdd_host,$Bdd_user,$Bdd_Pass,$Bdd);

// Tomamos los datos del POST
// esta variable se puede utilizar para diferenciar los datos de varias sesiones de ejecucion que puedan 
// ejecutarse sobre diferentes configuraciones del codigo de ACC, para comparar sus resultados

// $sesion = $_GET["IdSesion"];	// es directamente dato
$sesion = 0;// Para pruebas
?>
<html>
<head>
	<title>SMA ACC</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
</head>

<body bgcolor="#ffffff" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<br/>
<center>
<img width=150 height=150 src="imagenes/cambiacromos_01.jpeg">
<?php 
// Buscamos el id de la competicion
//$IdCompetition= get_value("competitions","IdCompetition","competition_name",$Competition,$conexion_BBDD);
	
// Buscamos los games de esta competicion
$str_mensajes="SELECT IdMessage,
					  IdSesion, 
					  comunc_id,
					  msg_id,
					  type_protocol,
					  protocol_step,
					  comunication_protocol,
					  origin_id,
					  origin_ip,
					  origin_port,
					  origin_time,
					  destination_id,
					  destination_ip,
					  destination_port,
					  destination_time,
					  body_info
			 FROM Message ";
if($sesion != 0)
{
	$str_mensajes = $str_mensajes."WHERE IdSesion=".$sesion; 	
}
			 
			 
$rs_mensajes =mysqli_query($conexion_BBDD, $str_mensajes);
if (!$rs_mensajes) // Si la consulta falla
	{
	// ----- ERROR  -------
		echo " ERROR en la consulta de la BBDD con str_mensajes = ".$str_mensajes;
		echo " </center>
		</body>
		</html>";
		@mysqli_close($conexion_BBDD);
		
	}
else  // Si la consulta no falla buscamos entre los descendientes la instancia de ayuda a interfaz, asi como las re recursos de ayuda a interfaz
	{
	$num_mensajes = mysqli_num_rows($rs_mensajes);
?>
	<!-- Inicio Caja gobal -->
	<div class='container mt-3'> 
		<h2>Datos de MENSAJES</h2>
		<p><?php echo "numero de mensajes totales : ".$num_mensajes; ?></p>
		  
		<!-- Inicio caja de mensajes -->
		<div class='container mt-3'> 
		  	<h2>MENSAJES</h2>
		     	  <!-- Inicio lista de mensajes -->
				<div class="table-responsive">
				<table class="table table-bordered">
					<thead>
						<tr class="table-warning">
							<th>IdMessage</th> 
							<th>IdSesion</th> 
							<th>comunc_id</th>
							<th>msg_id</th>
							<th>type_protocol</th>
							<th>protocol_step</th>
							<th>comunication_protocol</th>
							<th>origin_id</th>
							<th>origin_ip</th>
							<th>origin_port</th>
							<th>origin_time</th>
							<th>destination_id</th>
							<th>destination_ip</th>
							<th>destination_port</th>
							<th>destination_time</th>
							<th>body_info</th>
						</tr>
					</thead>
					<tbody>
						<?php
						$num_mensajes = mysqli_num_rows($rs_mensajes);
						$ord_mensaje = 0;
						while ($este_mensaje = mysqli_fetch_assoc($rs_mensajes))
						{
							$ord_mensaje++;
							if(($ord_mensaje % 2) != 0){{echo "<tr class='table-primary'>";}}
							else {echo "<tr>";}
								echo "<td>".$este_mensaje["IdMessage"]."</td>";
								echo "<td>".$este_mensaje["IdSesion"]."</td>";
								echo "<td>".$este_mensaje["comunc_id"]."</td>";
								echo "<td>".$este_mensaje["msg_id"]."</td>";
								echo "<td>".$este_mensaje["type_protocol"]."</td>";
								echo "<td>".$este_mensaje["protocol_step"]."</td>";
								echo "<td>".$este_mensaje["comunication_protocol"]."</td>";
								echo "<td>".$este_mensaje["origin_id"]."</td>";
								echo "<td>".$este_mensaje["origin_ip"]."</td>";
								echo "<td>".$este_mensaje["origin_port"]."</td>";
								echo "<td>".$este_mensaje["origin_time"]."</td>";
								echo "<td>".$este_mensaje["destination_id"]."</td>";
								echo "<td>".$este_mensaje["destination_ip"]."</td>";
								echo "<td>".$este_mensaje["destination_port"]."</td>";
								echo "<td>".$este_mensaje["destination_time"]."</td>";
								echo "<td>".$este_mensaje["body_info"]."</td>";
							echo "</tr>";
						} // Fin de - for ($ord_competidor = 0; $ord_competidor < $num_competidores; $ord_competidor++)
						?>
					</tbody>
				</table>
		   </div> <!-- FIN lista de mensajes -->
		<div class='container mt-3'> <!-- FIN caja de mensajes -->
	</div> 	<!-- FIN Caja gobal -->
</center>
</body>
<?php
	@mysqli_close($conexion_BBDD);
	} // Fin de - else - de - if (!$rs_mensajes) // Si la consulta falla
?>
</html>

