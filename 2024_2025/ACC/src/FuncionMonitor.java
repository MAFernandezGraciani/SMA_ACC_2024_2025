import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.*;
import java.sql.*;
import java.util.Random;

/**
 * class FuncionMonitor : Esta clase configura el hilo que ejecuta los procesos específicos del agente monitor
 * @author MAFG y Varios alumnos 2023-2024
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 * @version: 2024-2025-01
 * @observaciones:
 *      - Loas tareas que realizan son las siguientes :
 *          - Recibe los TODOS los mensajes de TODOS los agentes de del SMA y los imprime en la consola
 *          - PENDIENTE MAFG 2023-10-11, Cuando tenga tiempo y esten operativos habria que
 *              - Analizar los mensajes aferentes para identificar su contenido
 *              - Almacenar la informacion de mensajes y estado referente al SMA en BBDD
 *              - Generar una aplicacion web que acceda a la BBDD y monitorice el estado del SMA
 */
public class FuncionMonitor implements Runnable {

    protected int num_men_recibidos_monitor; // Para identificar los mensajes recibidos por este agente y poder identificarlos de forma unívoca
    protected Acc agente; // Para poder acceder a los datos generales de este agente

    protected int IdSesion; // Para identificar los mensajes recibidos por este agente y poder identificarlos de forma unívoca


    /**
     * public Acc : Contructor de la calse FuncionMonitor
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @param  este_agente : Este agente, para poder acceder a sus datos
     * @observaciones:
     *      - Inicializa datos
     *      - Arranca el hilo asociado a este objeto
     */
    FuncionMonitor(Acc este_agente) {
        num_men_recibidos_monitor = 0;
        this.agente = este_agente;

        // Damos de alta la sesion en la BBDD del monitor y obtenemos el "IdSesion" que le corresponde

        String sql_insert_sesion = "";
        try {
            // Crear la sentencia SQL
            sql_insert_sesion = "INSERT INTO sesiones (nombre_sesion, date_sesion) VALUES ('" + agente.nombre_sesion + "', " + agente.Tiempo_de_nacimiento + ")";

            // Crear un Statement
            Statement sta_insert_sesion = agente.conex_BBDD_monitor.createStatement();

            // Ejecutar la consulta
            int num_sesiones_insertadas = sta_insert_sesion.executeUpdate(sql_insert_sesion);

            // Obtenemos el valor de "IdSesion" de la sesion recien insertada
            ResultSet rs_insert_sesion = sta_insert_sesion.getGeneratedKeys();
                if (rs_insert_sesion.next())
                {
                    IdSesion = rs_insert_sesion.getInt(1); // Obtiene el primer valor de la clave generada
                }
            }
            catch (SQLException e_insert_sesion)
            {
                String texto_err_insert_sesion_log = "    **> ERROR 013. Desde FuncionMonitor => en FuncionMonitor() Al insertar la sesion en la BBDD."+
                        "\n - sql_insert_sesion : " + sql_insert_sesion +
                        "\n - e_insert_sesion.getMessage : " + e_insert_sesion.getMessage();
                agente.Gestor_de_logs.anota_en_log(texto_err_insert_sesion_log, 99); // 99 para error
            }

            // arrancamos el hilo
            this.agente.hilo_FuncionMonitor = new Thread(this, "hilo_FuncionMonitor");
            this.agente.hilo_FuncionMonitor.start();
    }

    /**
     * public void run() : Define el proceso que ejecuta este hilo
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - ESTE METODO ESTA ROTOTIPADO. Debera programarse para que realice las funciones especificas del agente monitor (MAFG 2023-10-04)
     *      - Por ahora, solo anota los mensajes recibidos por consola (ver descripción de esta clase)
     */
    @Override
    public void run() {
        String texto_run_hilo_fm_log = " - Desde FuncionMonitor => run() 01 \n El MONITOR : "+ this.agente.ID_propio+
                " - desde la ip : "+ this.agente.Ip_Propia+
                " Arranca el hilo  : FuncionMonitor";
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_run_hilo_fm_log, 50);}


        while(true){
            // //////////////////////////////////////
            // Obtenemos los mensajes recibidos
                // Miramos si hay algun mensaje recibido y si lo hay lo recogemos
            if(agente.num_elem_lita_recibidos() > 0) {
            recogeMensajeRecibido();
            }

            agente.num_ciclos_hilo_FuncionMonitor++; // Actualizamos el numero de ciclos de este hilo
        } // Fin de while(true){
    } // Fin de - public void run() {

    /**
     * void recogeMensajeRecibido() : Toma un mensaje de "contenedor_de_mensajes_recibidos" y lo notifica
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - ****************** LA FUNCION NO ESTA PROGRAMADA. SOLO ES UN PROTOTIPO *******************
     *      - Este método debe reprogramarse cuando se desarrolle el sistema de comunicaciones. Por ahora esta tan solo PROTOTIPADO (MAFG 2023-10-04)
     *      - Si el mensaje recibido lo es de Notificación de nacimiento, debe programarse para que el monitor envie :
     * 			    - Número de cromos totales del albun
     * 			    - Lista de cromos poseidos
     * 			    - Cantidad de rupias que posee
     * 			    - Objetivo a cumplir (por ahora sera solo uno, rellenar el album)
     *
     * 		- Se exponen en este metodo tres formas de acceder a datos del DOM de XML (Es suficiente con usar solo uno de estos. Exponemos tres, tan solo
     * 		    para que sirvan de ejemplo y que luego, segun las caracteristicas se use uno u otro). Los metodos expuestos aqui son:
     * 		            - OPCION 1 : Mediante acceso con traversing (Navegando por las ramas del arbol DOM)
     * 		            - OPCION 2 : Mediante acceso con Accesing (Buscando elementos con "getElementsByTagName". Ojo, si existen elementos con el
     * 		                            mismo nombre; puede generar problemas)
     * 		            - OPCION 3 : Mediante acceso con Xpath (Xpath permite definir busquedas especificas asociadas a las caracteristicas de los
     * 		                            elementos buscados, a partir de un nodo del arbol DOM)
     */
    void recogeMensajeRecibido() {
        String txt_men_rec_en_monitor_01_log = " - Desde FuncionMonitor => recogeMensajeRecibido() 01. Vamos  a recogeMensajeRecibido. Con : " + agente.num_elem_lita_recibidos() + " - mensajes en cola";
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_men_rec_en_monitor_01_log, 50);}

        // Obtenemos el mensaje
        Mensaje mensajeRecibido = agente.saca_de_lita_recibidos();
        num_men_recibidos_monitor++;
        String momento_actual_str = String.valueOf(System.currentTimeMillis());
        String puerto_origen_str = String.valueOf(mensajeRecibido.puerto_origen);

        String txt_men_rec_en_monitor_02_log = " - Desde FuncionMonitor => recogeMensajeRecibido() 02. \n  Con num rec : " + num_men_recibidos_monitor +
                                    "\n - en T : " +momento_actual_str +
                                    "\n - El agente : "+ mensajeRecibido.id_origen +
                                    "\n - desde la ip : "+ mensajeRecibido.IP_origen +
                                    "\n - puerto : "+ puerto_origen_str +
                                    "\n - protocolo : "+ mensajeRecibido.protocolo +
                                    "\n - envio el mensaje \n  * Mensaje recibido : "+ mensajeRecibido.cuerpo_del_mensaje;
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_men_rec_en_monitor_02_log, 50);}


        // Adsignamos valores por defecto
        String str_comunc_id = "comunc_id_por_defecto";
        String str_msg_id = "msg_id_por_defecto";
        String str_type_protocol = "type_protocol_por_defecto";
        String str_protocol_step = "protocol_step_por_defecto";
        String str_comunication_protocol = "comunication_protocol_por_defecto";

        try
        {
            Element elm_Message = mensajeRecibido.DOM_cuerpo_del_mensaje.getDocumentElement();

            // Vamos a por el contenido de "comunc_id"
            // Tomamos algunos datos del DOM del mensaje
            // Identificamos el elemento "Message"

                // ///////////////////////////////////
                // ///////////////////////////////////
                // OPCION 1 : Mediante acceso con traversing (Navegando por las ramas del arbol DOM)
            NodeList list_hijos_mensage = elm_Message.getChildNodes();
            for (int i = 0; i < list_hijos_mensage.getLength(); i++)
            {
                String str_elem = list_hijos_mensage.item(i).getNodeName();
                if (str_elem.equals("comunc_id")){str_comunc_id = list_hijos_mensage.item(i).getTextContent();}
                else if (str_elem.equals("msg_id")){str_msg_id = list_hijos_mensage.item(i).getTextContent();}
                else if (str_elem.equals("header"))
                {
                    NodeList list_hijos_header = list_hijos_mensage.item(i).getChildNodes();
                    for (int j = 0; j < list_hijos_header.getLength(); j++)
                    {
                        String str_elem_header = list_hijos_header.item(j).getNodeName();
                        if (str_elem_header.equals("type_protocol")){str_type_protocol = list_hijos_header.item(j).getTextContent();}
                        else if (str_elem_header.equals("protocol_step")){str_protocol_step = list_hijos_header.item(j).getTextContent();}
                        else if (str_elem_header.equals("comunication_protocol")){str_comunication_protocol = list_hijos_header.item(j).getTextContent();}
                        else {
                            String txt_err_men_rec_en_monitor_desconocido_log = "   ** ERROR. Desde FuncionMonitor => recogeMensajeRecibido() 02_01. ERROR por elemento no conocido list_hijos_mensage :" + j + " - nombre :" + str_elem_header;
                            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_err_men_rec_en_monitor_desconocido_log, 50);}
                        }
                    }
                }
            } // Fin de - for (int i = 0; i < list_hijos_mensage.getLength(); i++)
            String txt_rec_mon_traversing_log = " - Desde FuncionMonitor => recogeMensajeRecibido() 02_01. Daos del XML con con traversing :" +
                                "\n - comunc_id : " + str_comunc_id +
                                "\n - msg_id : " + str_msg_id +
                                "\n - En header : " +
                                "\n     - type_protocol : " + str_type_protocol +
                                "\n     - protocol_step : " + str_protocol_step +
                                "\n     - comunication_protocol : " + str_comunication_protocol;
            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_rec_mon_traversing_log, 50);}
                // Fin de - OPCION 1 : Mediante acceso con traversing
                // ///////////////////////////////////
                // ///////////////////////////////////

                // ///////////////////////////////////
                // ///////////////////////////////////
                // OPCION 2 : Mediante acceso con Accesing (Buscando elementos con "getElementsByTagName". Ojo, si existen elementos con el mismo nombre; puede generar problemas)
            NodeList lista_comunc_id =  elm_Message.getElementsByTagName("comunc_id");
            str_comunc_id =  lista_comunc_id.item(0).getTextContent();  // Ojo, solo debera existir un elemento con ese nombre, si no lo hara mal ya que tomara el primero que encuentre

            NodeList lista_msg_id =  elm_Message.getElementsByTagName("msg_id");
            str_msg_id =  lista_msg_id.item(0).getTextContent();  // Ojo, solo debera existir un elemento con ese nombre, si no lo hara mal ya que tomara el primero que encuentre

            NodeList lista_type_protocol =  elm_Message.getElementsByTagName("type_protocol");
            str_type_protocol =  lista_type_protocol.item(0).getTextContent();  // Ojo, solo debera existir un elemento con ese nombre, si no lo hara mal ya que tomara el primero que encuentre

            NodeList lista_protocol_step =  elm_Message.getElementsByTagName("protocol_step");
            str_protocol_step =  lista_protocol_step.item(0).getTextContent();  // Ojo, solo debera existir un elemento con ese nombre, si no lo hara mal ya que tomara el primero que encuentre

            NodeList lista_comunication_protocol =  elm_Message.getElementsByTagName("comunication_protocol");
            str_comunication_protocol =  lista_comunication_protocol.item(0).getTextContent();  // Ojo, solo debera existir un elemento con ese nombre, si no lo hara mal ya que tomara el primero que encuentre

            String txt_rec_mon_Accesing_log = " - Desde FuncionMonitor => recogeMensajeRecibido() 02_02. Daos del XML con Accesing :" +
                    "\n - comunc_id : " + str_comunc_id +
                    "\n - msg_id : " + str_msg_id +
                    "\n - En header : " +
                    "\n     - type_protocol : " + str_type_protocol +
                    "\n     - protocol_step : " + str_protocol_step +
                    "\n     - comunication_protocol : " + str_comunication_protocol;
            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_rec_mon_Accesing_log, 50);}
                // Fin de - OPCION 2 : Mediante acceso con Accesing (getElementsByTagName)
                // ///////////////////////////////////
                // ///////////////////////////////////


                // ///////////////////////////////////
                // ///////////////////////////////////
                // OPCION 3 : Mediante acceso con Xpath (Xpath permite definir busquedas especificas asociadas a las caracteristicas de los elementos buscados, a partir de un nodo del arbol DOM)
            // Generamos la instancia de xPath necesaria para compilar las expresiones
            XPath xpath = XPathFactory.newInstance().newXPath();

            String path_comunc_id = "//comunc_id";
//            String path_comunc_id = "child::comunc_id"; // Esto es otra opcion
            XPathExpression expr_comunc_id = xpath.compile(path_comunc_id); // Una vez compilada la expresión se evalua teniendo en cuenta que el resultado pueden ser múltiples nodos.
            NodeList list_comunc_id = (NodeList)expr_comunc_id.evaluate(elm_Message, XPathConstants.NODESET); // Si la expresión hace referéncia a un nodo aunque sepamos que sólo hay uno en el documento, el tipo de retorno será un NodeList por lo que se tiene que indicar XPathConstants.NODESET al invocar el evaluate
            str_comunc_id = list_comunc_id.item(0).getTextContent(); // Si esperamos que solo haya uno en la lista
            // Si hubiera mas de uno habria que tomar los valores con un for
            //        for(int i=0; i<list_comunc_id.getLength(); i++){
            //              Node parameterNode = list_comunc_id.item(i);
            //              String str_comunc_id = nodo_comunc_id.getTextContent();
            //        }

            String path_msg_id = "//msg_id";
            XPathExpression expr_msg_id = xpath.compile(path_msg_id); // Una vez compilada la expresión se evalua teniendo en cuenta que el resultado pueden ser múltiples nodos.
            NodeList list_msg_id = (NodeList)expr_msg_id.evaluate(elm_Message, XPathConstants.NODESET); // Si la expresión hace referéncia a un nodo aunque sepamos que sólo hay uno en el documento, el tipo de retorno será un NodeList por lo que se tiene que indicar XPathConstants.NODESET al invocar el evaluate
            str_msg_id = list_msg_id.item(0).getTextContent(); // Si esperamos que solo haya uno en la lista

            String path_type_protocol = "//header/type_protocol";
            XPathExpression expr_type_protocol = xpath.compile(path_type_protocol); // Una vez compilada la expresión se evalua teniendo en cuenta que el resultado pueden ser múltiples nodos.
            NodeList list_type_protocol = (NodeList)expr_type_protocol.evaluate(elm_Message, XPathConstants.NODESET); // Si la expresión hace referéncia a un nodo aunque sepamos que sólo hay uno en el documento, el tipo de retorno será un NodeList por lo que se tiene que indicar XPathConstants.NODESET al invocar el evaluate
            str_type_protocol = list_type_protocol.item(0).getTextContent(); // Si esperamos que solo haya uno en la lista

            String path_protocol_step = "//header/protocol_step";
            XPathExpression expr_protocol_step = xpath.compile(path_protocol_step); // Una vez compilada la expresión se evalua teniendo en cuenta que el resultado pueden ser múltiples nodos.
            NodeList list_protocol_step = (NodeList)expr_protocol_step.evaluate(elm_Message, XPathConstants.NODESET); // Si la expresión hace referéncia a un nodo aunque sepamos que sólo hay uno en el documento, el tipo de retorno será un NodeList por lo que se tiene que indicar XPathConstants.NODESET al invocar el evaluate
            str_protocol_step = list_protocol_step.item(0).getTextContent(); // Si esperamos que solo haya uno en la lista

            String path_comunication_protocol = "//header/comunication_protocol";
            XPathExpression expr_comunication_protocol = xpath.compile(path_comunication_protocol); // Una vez compilada la expresión se evalua teniendo en cuenta que el resultado pueden ser múltiples nodos.
            NodeList list_comunication_protocol = (NodeList)expr_comunication_protocol.evaluate(elm_Message, XPathConstants.NODESET); // Si la expresión hace referéncia a un nodo aunque sepamos que sólo hay uno en el documento, el tipo de retorno será un NodeList por lo que se tiene que indicar XPathConstants.NODESET al invocar el evaluate
            str_comunication_protocol = list_comunication_protocol.item(0).getTextContent(); // Si esperamos que solo haya uno en la lista

            String txt_rec_mon_Xpath_log = " - Desde FuncionMonitor => recogeMensajeRecibido() 02_03. Daos del XML con Xpath :" +
                    "\n - comunc_id : " + str_comunc_id +
                    "\n - msg_id : " + str_msg_id +
                    "\n - En header : " +
                    "\n     - type_protocol : " + str_type_protocol +
                    "\n     - protocol_step : " + str_protocol_step +
                    "\n     - comunication_protocol : " + str_comunication_protocol;
            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_rec_mon_Xpath_log, 50);}
                // Fin de - OPCION 3 : Mediante acceso con Xpath
                // ///////////////////////////////////
                // ///////////////////////////////////

            // ///////////////////////////////////
            // ///////////////////////////////////
            // CONTENIDO DEL CUERPO DEL MENSAJE : Llegados a este punto, deberiamos analizar el cuerpo del mensaje y obtener los datos que desearamos analizar
            //      en los procesos del monitor. Esta tarea, es dependiente de cada aplicacion especifica de los agenets en cada SMA
            //      - Los datos obtenidos deberian incluirse en los insert que se realizan referentes a los mensajes en la BBDD del monitor
            // ///////////////////////////////////
            // ///////////////////////////////////
        }
        catch (XPathExpressionException e_XPath)
        {
            String txt_err_rec_mon_Xpath_log = "** Error. Desde FuncionMonitor => recogeMensajeRecibido() 04. \n error (IOException) desde funcion del monitor recogeMensajeRecibido() : fallo al procesar un mensaje desde el monitor: " + e_XPath + "\n"+
                    "\n Desde : agente con id  : "+agente.ID_propio +
                    "\n - en la ip "+agente.Ip_Propia+
                    "\n - en Puerto_Propio : "+agente.Puerto_Propio+
                    "\n - total mensajes enviados : "+agente.dime_num_tot_men_env();
            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_err_rec_mon_Xpath_log, 50);}

        }

        // Grabamos los datos en la base de datos del monitor
        String sql_insert_mensaje = "";
        try
        {
            sql_insert_mensaje = "INSERT INTO Message(IdSesion,"+
                    " comunc_id,"+
                    " msg_id,"+
                    " type_protocol,"+
                    " protocol_step,"+
					" comunication_protocol,"+
					" origin_id,"+
					" origin_ip,"+
					" origin_port,"+
					" origin_time,"+
					" destination_id,"+
					" destination_ip,"+
					" destination_port,"+
					" destination_time,"+
					" body_info)"+
            " VALUES ("+IdSesion+","+
                    " '"+str_comunc_id+"',"+
                    " '"+str_msg_id+"',"+
                    " '"+str_type_protocol+"',"+
                    str_protocol_step+","+
                    " '"+str_comunication_protocol+"',"+
                    " '"+mensajeRecibido.id_origen+"',"+
                    " '"+mensajeRecibido.IP_origen+"',"+
                    mensajeRecibido.puerto_origen+","+
                    mensajeRecibido.tiempo_origen+","+
                    " '"+mensajeRecibido.id_destino+"',"+
                    " '"+mensajeRecibido.IP_destino+"',"+
                    " '"+mensajeRecibido.puerto_destino+"',"+
                    mensajeRecibido.tiempo_destino+","+
//                    " '"+mensajeRecibido.cuerpo_del_mensaje+"')";
                    " 'para pruebas')";

            Statement stmt_insert_mensaje = agente.conex_BBDD_monitor.createStatement();

            // Ejecutar la consulta
            int filasInsertadas = stmt_insert_mensaje.executeUpdate(sql_insert_mensaje);

            // Verificar el resultado
            if (filasInsertadas > 0) {
                System.out.println("¡Inserción exitosa!");
            }
        }
        catch (SQLException e_insert_mensaje)
            {
            System.err.println("Error al insertar datos:");
            e_insert_mensaje.printStackTrace();

            String texto_err_ins_bbdd_log = "    **> ERROR 011. Desde Acc => en recogeMensajeRecibido() Al insertar en la BBDD."+
                    "\n - sql_insert_mensaje : " + sql_insert_mensaje +
                    "\n - this.usr_BBDD_monitor : " + e_insert_mensaje.getMessage();
            agente.Gestor_de_logs.anota_en_log(texto_err_ins_bbdd_log, 99); // 99 para error

        }
   } // Fin de - void recogeMensajeRecibido() {
} // FIn de - public class FuncionDeAgente implements Runnable {
