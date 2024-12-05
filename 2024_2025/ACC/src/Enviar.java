import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Esta clase recoge los mensages que hay en "contenedor_de_mensajes_a_enviar" y los envia a su destinatario
 * @author MAFG y Varios alumnos 2022-2023
 * @author MAFG y Varios alumnos 2023-2024
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 * @version: 2024-2025-01
 * @observaciones:
 *      - los mensajes se pueden enviar por TCP o por UDP
 */
public class Enviar extends Thread {

    protected Acc agente;
    protected int latenciaDeAtencionDeEnvio;  // Es el tiempo en milisegundos que queremos que transcurra desde que
                                            // no encontramos mensajes a enviar hasta que volvemos a buscar de nuevo en la lista
    /**
     * Constructor de la clase
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2023-2024-01
     * @param este_agente : Es el objeto agente, donde este objeto puede encontrar toda la informacion y recursos que necesita
     * @observaciones:
     */
    Enviar(Acc este_agente){
        super();
        this.agente = este_agente; // Para acceder al agente que nos ocupa
        this.latenciaDeAtencionDeEnvio = 10;
        this.agente.hilo_Enviar = new Thread(this, "hilo_Enviar");
        this.agente.hilo_Enviar.start();
    }

    /**
     * public void run() : Define el proceso que ejecuta este hilo
     * @author MAFG
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - Basicamente consulta la lista "contenedor_de_mensajes_a_enviar" y si hay mensajes, los envia
     *      - Los mensajes estan como objetos de la clase "Mensaje"
     */
    @Override
    public void run()
    {
        String texto_Enviar_001_log = "    - Desde Enviar => run().  El agente : "+ this.agente.ID_propio+ " - desde la ip : "+ this.agente.Ip_Propia+" Arranca el hilo  : Enviar";
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_Enviar_001_log, 50);}

        while (true) {
            // Si el agente no tiene mensajes para enviar, se para 1s antes de mirar otra vez
            try {
                // Si el agente no tiene mensajes para enviar, se para 1s antes de mirar otra vez
                if (agente.num_elem_lita_enviar() >0) {
                    Mensaje mensajeAEnviar = agente.saca_de_lita_enviar();
                    String protocolo_mensaje = mensajeAEnviar.getProtocolo();
                    // Dependiendo del protocolo, enviamos el mensaje de una forma u otra
                    if (protocolo_mensaje.equals("TCP")) {
                        EnviaTcp(mensajeAEnviar);
                    }
                    else if(protocolo_mensaje.equals("UDP")){
                        EnviaUdp(mensajeAEnviar);
                    }
                    else {
                        String texto_Enviar_err_001_log = "    **> ERROR 001. DesdeEnviar => run().  Protocolo desconocido : "+ protocolo_mensaje;
                        agente.Gestor_de_logs.anota_en_log(texto_Enviar_err_001_log, 99); // 99 para error
                    }
                }
                else{
                    sleep(latenciaDeAtencionDeEnvio); // Para controlar la velocidad de envio
                }

                agente.num_ciclos_hilo_enviar++; // Actualizamos el numero de ciclos de este hilo
            } catch (Exception e){
                String texto_Enviar_err_002_log = "    **> ERROR 002. DesdeEnviar => run().  No se ha podido enviar el mensaje.\n * Con e.getMessage : "+ e.getMessage();
                agente.Gestor_de_logs.anota_en_log(texto_Enviar_err_002_log, 99); // 99 para error
            }
        } // Fin de - while (true) {
    } // Fin de - public void run()


    /**
     * void EnviaTcp() : Método para enviar mensajes por TCP
     * @author : MAFG
     * @fechaDeCreacion : 2022-xx-xx
     * @fechaDeUltimaModificacion : 2023-10-04
     * @version : 2024-2025-01
     * @param : mensajeAEnviar Mensaje a enviar
     * @observaciones:
     *      - Este metodo envia por TCP el contenido del objeto de clase "Mensaje" que se le envia. Este objeto contiene dos propiedades :
     *              - cuerpo_del_mensaje; que es un string XML con el cuerpo del mensaje
     *              - DOM_cuerpo_del_mensaje; que es un objeto dom que contiene el cuerpo del mensaje en XML
     *        SI "DOM_cuerpo_del_mensaje" SI NO ES NULO, se enviara el contenido de "cuerpo_del_mensaje"
     *        Si "DOM_cuerpo_del_mensaje" ES NULO, se enviara el XML que este contiene independientemente de lo que contenga "DOM_cuerpo_del_mensaje"
     *
     *      - En cualquiera de los dos casos. el XML se valida contra el esquema correspondiente antes de ser enviado
     */
    public void EnviaTcp(Mensaje mensajeAEnviar) throws ParserConfigurationException, IOException, SAXException {

        int puerto_destino_TCP = mensajeAEnviar.puerto_destino; // EL TCP es el puerto destino y el UDP es el mismo incrementado en uno

        try {
            // Primero obtenemos el string XML asociado al DOM (si este no es nulo)
            if(mensajeAEnviar.DOM_cuerpo_del_mensaje != null)
            {
                mensajeAEnviar.Dom_a_Xml(agente); // OJO, esta operacion ya valida el XML contra el esquema correspondiente
            }
            else{
                // Si el DOM es nulo es que nos envian el mensaje directamente en string XML
                // Y antes de enviarlo tendremos que validarlo
                StreamSource xml_StreamSource = new StreamSource(new StringReader((mensajeAEnviar.cuerpo_del_mensaje)));
                agente.valida_Xml(xml_StreamSource);
            }

            // Creación socket para comunicarse con el servidor con el host y puerto asociados al servidor
            Socket skCliente = new Socket(mensajeAEnviar.IP_destino, puerto_destino_TCP);
            // Creación flujo de salida
            DataOutputStream obj = new DataOutputStream(skCliente.getOutputStream());
            // Envía objeto al servidor
            obj.writeUTF(mensajeAEnviar.cuerpo_del_mensaje);
            // Cierra flujo de salida
            obj.close();
            // Cierra socket
            skCliente.close();
            // Ok

            String num_men_por_enviar_str = String.valueOf(agente.num_elem_lita_enviar());
            String texto_EnviaTcp_002_log = "    - Desde Enviar => EnviaTcp().  Mensaje TCP enviado : "+
                    " - en Puerto_Propio : "+agente.Puerto_Propio+
                    " - mensaje en cola de envio : "+num_men_por_enviar_str+
                    " - total mensajes enviados : "+agente.dime_num_tot_men_env()+
                    "\n -- Destinatario id_destino : "+mensajeAEnviar.id_destino+
                    " - en la ip : "+mensajeAEnviar.IP_destino+
                    " - puerto destino : "+mensajeAEnviar.puerto_destino+
                    " - protocolo : "+mensajeAEnviar.protocolo+
                    "\n -- mensaje : "+mensajeAEnviar.cuerpo_del_mensaje;
            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_EnviaTcp_002_log, 50);}

            // ////////////////////////////////////////////////////////
            // Ahora, si el mensaje no va destinado al Monitor, enviamos al monitor una copia de este mensaje
            // (hemos decidido que monitorizaremos el SMA enviando copia de todos los mensajes al monitor)
            if ((!mensajeAEnviar.IP_destino.equals(agente.Ip_Monitor)) & (puerto_destino_TCP != agente.Puerto_Monitor_TCP))
            {
                // Creación socket para comunicarse con el servidor con el host y puerto asociados al servidor
                Socket skCliente_Monitor = new Socket(agente.Ip_Monitor, agente.Puerto_Monitor_TCP);
                // Creación flujo de salida
                DataOutputStream obj_Monitor = new DataOutputStream(skCliente_Monitor.getOutputStream());
                // Envía objeto al servidor
                obj_Monitor.writeUTF(mensajeAEnviar.cuerpo_del_mensaje);
                // Cierra flujo de salida
                obj_Monitor.close();
                // Cierra socket
                skCliente_Monitor.close();
            }

       }
        catch (Exception e) {
            // Failure
            String num_men_por_enviar_str = String.valueOf(agente.num_elem_lita_enviar());
            String texto_EnviaTcp_err_003_log = "    **> ERROR 002. Desde Enviar => EnviaTcp(). fallo al enviar mensaje  TCP. mensaje en cola de envio : "+num_men_por_enviar_str+
                    " - total mensajes enviados : "+agente.dime_num_tot_men_env()+
                    "\n -- Destinatario id_destino : "+mensajeAEnviar.id_destino+
                    " - en la ip : "+mensajeAEnviar.IP_destino+
                    " - puerto destino : "+mensajeAEnviar.puerto_destino+
                    " - protocolo : "+mensajeAEnviar.protocolo+
                    "\n -- mensaje : "+mensajeAEnviar.cuerpo_del_mensaje+
                    "\n * Con e.getMessage : "+ e.getMessage();
            agente.Gestor_de_logs.anota_en_log(texto_EnviaTcp_err_003_log, 99); // 99 para error
        }
    } // Fin de - public void EnviaTcp(Mensaje mensajeAEnviar) throws ParserConfigurationException, IOException, SAXException {

    /**
     * void EnviaUdp() : Método para enviar mensajes por UDP
     * @author : MAFG
     * @fechaDeCreacion : 2022-xx-xx
     * @fechaDeUltimaModificacion : 2023-10-04
     * @version : 2024-2025-01
     * @param : mensajeAEnviar Mensaje a enviar
     * @observaciones:
     *      - Este metodo envia por UDP el contenido del objeto de clase "Mensaje" que se le envia. Este objeto contiene dos propiedades :
     *              - cuerpo_del_mensaje; que es un string XML con el cuerpo del mensaje
     *              - DOM_cuerpo_del_mensaje; que es un objeto dom que contiene el cuerpo del mensaje en XML
     *        SI "DOM_cuerpo_del_mensaje" SI NO ES NULO, se enviara el contenido de "cuerpo_del_mensaje"
     *        Si "DOM_cuerpo_del_mensaje" ES NULO, se enviara el XML que este contiene independientemente de lo que contenga "DOM_cuerpo_del_mensaje"
     *
     *      - En cualquiera de los dos casos. el XML se valida contra el esquema correspondiente antes de ser enviado
     */
    public void EnviaUdp(Mensaje mensajeAEnviar)
    {
        int puerto_destino_UDP = mensajeAEnviar.puerto_destino; // EL TCP es el puerto destino y el UDP es el mismo incrementado en uno

        try {
            // Primero obtenemos el string XML asociado al DOM (si este no es nulo)
            if(mensajeAEnviar.DOM_cuerpo_del_mensaje != null)
            {
                mensajeAEnviar.Dom_a_Xml(agente); // OJO, esta operacion ya valida el XML contra el esquema correspondiente
            }
            else{
                // Si el DOM es nulo es que nos envian el mensaje directamente en string XML
                // Y antes de enviarlo tendremos que validarlo
                StreamSource xml_StreamSource = new StreamSource(new StringReader((mensajeAEnviar.cuerpo_del_mensaje)));
                agente.valida_Xml(xml_StreamSource);
            }

            //Creamos el socket de UDP
            DatagramSocket socketUDP = new DatagramSocket();
            //Convertimos el mensaje a bytes
            byte[] mensaje_UDP = mensajeAEnviar.cuerpo_del_mensaje.getBytes();
            //Creamos un datagrama
            DatagramPacket paquete_UDP = new DatagramPacket(mensaje_UDP, mensaje_UDP.length, InetAddress.getByName(mensajeAEnviar.IP_destino), puerto_destino_UDP);
            //Lo enviamos con send
            socketUDP.send(paquete_UDP);
            //Cerramos el socket
            socketUDP.close();

            String num_men_por_enviar_str = String.valueOf(agente.num_elem_lita_enviar());
            String texto_EnviaUdp_003_log = "    - Desde Enviar => EnviaUdp().  Mensaje UDP enviado : "+
                    " - en Puerto_Propio : "+agente.Puerto_Propio+
                    " - mensaje en cola de envio : "+num_men_por_enviar_str+
                    " - total mensajes enviados : "+agente.dime_num_tot_men_env()+
                    "\n -- Destinatario id_destino : "+mensajeAEnviar.id_destino+
                    " - en la ip : "+mensajeAEnviar.IP_destino+
                    " - puerto destino : "+mensajeAEnviar.puerto_destino+
                    " - protocolo : "+mensajeAEnviar.protocolo+
                    "\n -- mensaje : "+mensajeAEnviar.cuerpo_del_mensaje;
            if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_EnviaUdp_003_log, 50);}

            // ////////////////////////////////////////////////////////
            // Ahora, si el mensaje no va destinado al Monitor, enviamos al monitor una copia de este mensaje
            // (hemos decidido que monitorizaremos el SMA enviando copia de todos los mensajes al monitor)
            if ((!mensajeAEnviar.IP_destino.equals(agente.Ip_Monitor)) & (puerto_destino_UDP != agente.Puerto_Monitor_UDP))
            {
                //Creamos el socket de UDP
                DatagramSocket socketUDP_Monitor = new DatagramSocket();
                //Convertimos el mensaje a bytes
                byte[] mensaje_UDP_Monitor = mensajeAEnviar.cuerpo_del_mensaje.getBytes();
                //Creamos un datagrama
                DatagramPacket paquete_UDP_Monitor = new DatagramPacket(mensaje_UDP_Monitor, mensaje_UDP_Monitor.length, InetAddress.getByName(agente.Ip_Monitor), agente.Puerto_Monitor_UDP);
                //Lo enviamos con send
                socketUDP.send(paquete_UDP_Monitor);
                //Cerramos el socket
                socketUDP_Monitor.close();
            }
        }
        catch (IOException ex)
        {
            String num_men_por_enviar_str = String.valueOf(agente.num_elem_lita_enviar());
            String texto_EnviaUdp_err_004_log = "    **> ERROR 002. Desde Enviar => EnviaUdp(). fallo al enviar mensaje UDP. mensaje en cola de envio : "+num_men_por_enviar_str+
                    " - total mensajes enviados : "+agente.dime_num_tot_men_env()+
                    "\n -- Destinatario id_destino : "+mensajeAEnviar.id_destino+
                    " - en la ip : "+mensajeAEnviar.IP_destino+
                    " - puerto destino : "+mensajeAEnviar.puerto_destino+
                    " - protocolo : "+mensajeAEnviar.protocolo+
                    "\n -- mensaje : "+mensajeAEnviar.cuerpo_del_mensaje+
                    "\n * Con ex.getMessage : "+ ex.getMessage();
            agente.Gestor_de_logs.anota_en_log(texto_EnviaUdp_err_004_log, 99); // 99 para error
        }
        catch (SAXException eSax)
        {
            String num_men_por_enviar_str = String.valueOf(agente.num_elem_lita_enviar());
            String texto_EnviaUdp_err_005_log = "    **> ERROR 002. Desde Enviar => EnviaUdp(). fallo al enviar mensaje UDP. mensaje en cola de envio : "+num_men_por_enviar_str+
                    " - total mensajes enviados : "+agente.dime_num_tot_men_env()+
                    "\n -- Destinatario id_destino : "+mensajeAEnviar.id_destino+
                    " - en la ip : "+mensajeAEnviar.IP_destino+
                    " - puerto destino : "+mensajeAEnviar.puerto_destino+
                    " - protocolo : "+mensajeAEnviar.protocolo+
                    "\n -- mensaje : "+mensajeAEnviar.cuerpo_del_mensaje+
                    "\n * Con eSax.getMessage : "+ eSax.getMessage();
            agente.Gestor_de_logs.anota_en_log(texto_EnviaUdp_err_005_log, 99); // 99 para error
            throw new RuntimeException(eSax);
        }
    } // FIn de - public void EnviaUdp(Mensaje mensajeAEnviar)
} // Fin de - public class Enviar extends Thread {
