<?php
/**
*
* description : Recurso para acceder a los datos de una competicion de walkers en la BBDD
* @author : MiguelAngel Fernandez Graciani
* @date  : 2023-04-16
*
* @resourcename : index.php
* @resourcetype PAGINA PHP
* @version : 001
* @historical
*
* Comentarios:
*		- Para mostrar los datos de una competicion de walkers
*
*/

// Esto esta en conexion.php
$Bdd_host="127.0.0.1"; // Ver "BBDD_ACC_SMA_v001.sql"
$Bdd='acc_bbdd'; // Ver "BBDD_ACC_SMA_v001.sql"
$Bdd_user='usrAcc'; // Ver "BBDD_ACC_SMA_v001.sql"
$Bdd_Pass='hd83mmi8812a'; // Ver "BBDD_ACC_SMA_v001.sql"

/*******************************************************
 Autor: Miguel Angel Fernandez Graciani
Fecha: 2023-04-16
Contenido: funcion que realiza la conexion
a la base de datos
Comentarios : se utiliza la que hace diego para DKSs
Entrada: Usuario, password, Base de datos, host
Salida: objeto de conexion a MySQL
*******************************************************/
function conect_Bdd($HOST,$USUARIO,$PASSWORD,$BASEDEDATOS)
{
	// $conBBDD=@mysql_connect($HOST,$USUARIO,$PASSWORD)or die("Error en la conexion");
	// @mysql_select_db($BASEDEDATOS,$conBBDD) or die("No se ha podido seleccionar la base de datos");

	$conBBDD=@mysqli_connect($HOST,$USUARIO,$PASSWORD)or die("Error en la conexion.");
	@mysqli_select_db($conBBDD, $BASEDEDATOS) or die("No se ha podido seleccionar la base de datos");
	return $conBBDD;
} // FIn de - function conect_Bdd($HOST,$USUARIO,$PASSWORD,$BASEDEDATOS)

/*********************************************************************************
 Autor: Diego J. Garcia
Fecha: 24-Marzo-2004
Contenido: funcion que muestra una pagina de enlace al inicio
Entrada: PathRelativo, variable que indica el path desde el que nos encontramos
*********************************************************************************/
function ErrorPassword($PathRelativo)
{
		?>
		<html>
		<head>
		<link rel="stylesheet" href="<?echo $PathRelativo?>estilos.css">
		</head>
		<body class="fondoGris">
			<table border="0" align="center" height="100%" cellpadding="0" cellspacing="0">
			  <tr> 
			    <td align="center"> <H2> Ha introducido un usuario o password erroneos </H2>
				<table align="center" id="btn_siguiente" class="botonxp" border="0" cellspacing="0" cellpadding="0" onClick="top.location.assign('<?echo $PathRelativo?>index.php')">
			        <tr> 
			          <td align="center">&nbsp;&nbsp;&nbsp;Atras&nbsp;&nbsp;&nbsp;</td>
			          <td width="1"> <img src="<?echo $PathRelativo?>imagenes/flecha_izq.gif"></td>
			        </tr>
			      </table><br><br><br><br><br><br><br>
				</td>
			  </tr>
			</table>
		</body></html>
	<?php
	die();
} // Fin de - function ErrorPassword($PathRelativo)

/*******************************************************
 Autor: Miguel Angel Fernandez Graciani
Fecha: 2023-04-16
Contenido: funcion que busca el id de un registro con el valor (string que se le envia) de un campo concreto
Entrada:
- $tabla : Tabla donde buscar
- $campo_de_id : Campo que buscamos en la tabla
- $campo_busqueda : Esel campo que nos sirve para buscar el registro (OJOO DEBE SER STRISNG, principalmente por las comas de SQL)
- $valor_busqueda : Es el valor del campo "$campo_busqueda" que utilizamos para seleccionar el registro del que buscamos su "$campo_de_id"
Salida:
- $id_resultado : Es el ide del registro que buscamos
Comentarios : s
- OJOOO "$campo_busqueda" DEBE SER STRISNG, principalmente por las comas de SQL

*******************************************************/
function get_value($tabla,$campo_de_id,$campo_busqueda,$valor_busqueda, $conexion_BBDD)
{
	$id_resultado = 0;

	$str="SELECT ".$campo_de_id." FROM ".$tabla." WHERE ".$campo_busqueda." = '".$valor_busqueda."';";
	$rs=mysqli_query($conexion_BBDD, $str);
	if (!$rs) // Si la consulta falla
	{
		echo " -- desde get_value. fallos en el acceso con mysql_error = ".mysqli_error($conexion_BBDD);
	}
	else
	{
		if (mysqli_num_rows($rs)==1)  // OK, solo hay un concepto con esa clave y localizacion
		{
			$result = mysqli_fetch_array($rs, MYSQLI_BOTH);
			$id_resultado = $result[$campo_de_id];
		}
		else
		{
			echo " -- desde get_value. fallos en mas de un row con mysql_num_rows = ".mysqli_num_rows($rs)." - Con valor_busqueda : ".$valor_busqueda." - Con str : ".$str;
		}
	}
	return $id_resultado;
} // FIn de - function conect_Bdd($HOST,$USUARIO,$PASSWORD,$BASEDEDATOS)

?>
