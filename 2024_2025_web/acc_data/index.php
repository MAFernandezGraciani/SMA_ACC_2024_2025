<?php
/**
*
* description : Recurso para acceder a los datos de monitorizacion del SMA ACC
* @author : MiguelAngel Fernandez Graciani
* @date  : 2024-11-01
*
* @resourcename : index.php
* @resourcetype PAGINA PHP
* @version : 001
* @historical
*
* Comentarios:
*		- Para mostrar los datos de monitorizacion del SMA ACC
*
*/

require_once('conexion.php');

// Conectamos con la BBDD
$conexion_BBDD = conect_Bdd($Bdd_host,$Bdd_user,$Bdd_Pass,$Bdd);

// Declaramos las variables que vamos a utilizar


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
<!--
	Access page to the viewer of competition results of the videogame walkers<br/>
	- The video game walkers is a video game generated for teaching in the subject of
	of Artificial Intelligence in videogames of the "Department of Computer Systems" of the UCLM<br/><br/>
	
	Last update 2023-04
-->
<br/>
<center>
<img width=200 height=200 src="imagenes/cambiando_cromos_01.jpeg">
<br/>
	<h5>SMA ACC</h5>
	</br>
	
	<div class="container mt-3">
	  <h2>Selecciona la sesion</h2>
	  <form action="acc_Data.php">
	    <label for="sel1" class="form-label">Nombre de la sesion:</label>
	    <select class="form-select" id="IdSesion" name="sesion">
	    <?php 
		$str_sesiones="SELECT IdSesion,Nombre_sesion,date_sesion FROM sesiones"; 
//		$rs_competition=mysql_query($str_competition);
		$rs_sesiones = mysqli_query($conexion_BBDD, $str_sesiones);
		if (!$rs_sesiones) // Si la consulta falla
			{
			// ----- ERROR  -------
			die ("Error en index al buscar las sesiones");
			}
		else  // Si la consulta no falla obtenemos las sesiones
			{
//			$num_competition = mysql_num_rows($rs_competition);
			$num_sesiones = mysqli_num_rows($rs_sesiones);
				
			for ($indice_sesion=0; $indice_sesion < $num_sesiones; $indice_sesion++)
				{
//				$result[$indice_competition] = mysql_fetch_array($rs_competition);
//				$IdCompetition = $result[$indice_competition]["IdCompetition"];
//				$competition_name = $result[$indice_competition]["competition_name"];

				$result = mysqli_fetch_array($rs_sesiones, MYSQLI_ASSOC);
						
//				$IdCompetition = $result[$indice_competition]["IdCompetition"];
//				$competition_name = $result[$indice_competition]["competition_name"];

				$IdSesion = $result["IdSesion"];
				$Nombre_sesion = $result["Nombre_sesion"];
				$date_sesion = $result["date_sesion"];
				
				echo "<option value=".$IdSesion.">  - SESION : ".$Nombre_sesion." - Hora : ".$date_sesion."</option>";
				}   // FIn de - for for ($indice_sesion=0; $indice_sesion < $num_sesiones; $indice_sesion++)
			} // FIn del else
				
	?>
	    </select>
	    <br>
	    <button type="submit" class="btn btn-primary mt-3">Submit</button>
	  </form>
	</div>
	
	</br>
	</br>
	<p>
	Selecciona la competición de la que solicitas información<br/>
	
	- ACC es un sistema multiagente diseñado en clase de SMA 2024-2025<br/><br/>
	
	
	Last update 2024-11
	</p>
</center>
<?php
@mysqli_close($conexion_BBDD);
//phpinfo() 
?>
</body>
</html>

