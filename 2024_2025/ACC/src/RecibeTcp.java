import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Esta clase recibe los envíos que llegan mediante el protocolo TCP y los almacena como objetos de la clase mensaje en "contenedor_de_mensajes_a_enviar"
 * @author MAFG y Varios alumnos 2022-2023
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 * @version: 2024-2025-01
 * @observaciones:
 *      - Es necesario convertir el envío recibido en un objeto de clase "Mensaje"
 */
public class RecibeTcp extends Thread {
    //private static GestorMensajes gestor; // Referencia del gestor de mensajes
    protected Acc agente;  // Para tener acceso a los datos y recursos de este agente
    ServerSocket servidor_TCP;

    /**
     * Constructor de la clase
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2023-2024-01
     * @param este_agente : Es el objeto agente, donde este objeto puede encontrar toda la informacion y recursos que necesita
     * @observaciones:
     *      - Inicializa datos
     *      - Arranca el hilo encargado de recibir mediante TCP
     */
    RecibeTcp(Acc este_agente){
        // Inicializamos
        super();
        this.agente = este_agente;
        servidor_TCP = agente.servidor_TCP;

        // arrancamos el hilo
        this.agente.hilo_RecibeTcp = new Thread(this, "hilo_RecibeTcp");
        this.agente.hilo_RecibeTcp.start();
    }

    /**
     * public void run() : Define el proceso que ejecuta este hilo
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - Abre el ServerSocket "servidor" y lo deja a la espera de recibir
     *      - Cuando recibe un envío, lo transforma en objeto de la clase "Mensaje" y lo guarda en "contenedor_de_mensajes_recibidos"
     *      - Cuando el agente se destruye, cierra los sockets abiertos
     */
    @Override
    public void run()
    {
        String txt_RecibeTcp_01_log = " - Desde RecibeTcp => run() - El agente : "+ this.agente.ID_propio+" - desde la ip : "+ this.agente.Ip_Propia+" Arranca el hilo  : RecibeTcp";
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_RecibeTcp_01_log, 50);}

        try
        {
            // El socket de servicio TCP, ya se genero al buscar el nido "Acc => buscaNido()"
            while(true) {
                String txt_RecibeTcp_02_log = " - Desde RecibeTcp => run() - 02.  ESPERANDO paquete TCP en el agente con id  : "+agente.ID_propio +
                        " - con ip : "+agente.Ip_Propia+
                        " - y Puerto_Propio_TCP : "+agente.Puerto_Propio_TCP +
                        " - agente.num_ciclos_hilo_recibeTcp : " + agente.num_ciclos_hilo_recibeTcp;
                if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_RecibeTcp_02_log, 50);}

                // El servidor espera a que el cliente se conecte y devuelve un socket nuevo
                Socket cliente = servidor_TCP.accept();

                // Obtiene el flujo de entrada y lee el objeto del stream
                DataInputStream obj = new DataInputStream(cliente.getInputStream());

                // Lee el objeto del stream
                String texto_recibido_TCP = obj.readUTF();
                String texto_recibido_TCP_limpio = texto_recibido_TCP.trim();

                // Construimos el objeto de la clase "Mensaje"
                String IP_or = String.valueOf(cliente.getInetAddress());
                int puerto_or = cliente.getPort();
                String id_or = "id_or por determinar";
                String IP_dest = agente.Ip_Propia;
                int puerto_dest = agente.Puerto_Propio_TCP;
                String id_dest = agente.ID_propio;
                long tiempoUnixEnMilisegundos = System.currentTimeMillis();
                String protocolo = "TCP";
                String cuerpo_mens = texto_recibido_TCP_limpio;

                Mensaje mensaje_recibido_TCP = new Mensaje("El ID_mensaje viene en el cuerpo del mensaje",
                        IP_or,
                        puerto_or,
                        id_or,
                        0, // ponemos 0, a la espera de lleerlo del xml
                        IP_dest,
                        puerto_dest,
                        id_dest,
                        tiempoUnixEnMilisegundos,
                        protocolo,
                        cuerpo_mens,
                        null);
                // Ahora construimos el DOM para que lo tenga listo quien lo reciba
                mensaje_recibido_TCP.Xml_a_Dom(agente);

                // Llevamos el mensaje al contenedor de recibidos
                    agente.pon_en_lita_recibidos(mensaje_recibido_TCP);
                    String txt_RecibeTcp_03_log = " - Desde RecibeTcp => run() - 03. El agente : "+agente.Ip_Propia+" - IP : "+agente.ID_propio+
                            " - en contenedor recibidos tenemos : " + String.valueOf(agente.num_elem_lita_recibidos()) +
                            " - total recibidos : " + agente.num_elem_lita_recibidos() +
                            "\n - HA RECIBIDO EL MENSAJE : " + mensaje_recibido_TCP.cuerpo_del_mensaje;
                    if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_RecibeTcp_03_log, 50);}

                    // Cerramos los sockets
                    obj.close();
                    // Cerramos el cliente
                    cliente.close();

                agente.num_ciclos_hilo_recibeTcp++; // Actualizamos el numero de ciclos de este hilo
            } // Fin de -  while(true) {
        }
        catch (Exception e)
        {
            String txt_err_RecibeTcp_001_log = "    ** ERROR:  Desde RecibeTcp => run() - 04 \n . Exception : " + e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_RecibeTcp_001_log, 99); // 99 para error
        }
    } // FIn de -  public void run()
} // Fin de - public class RecibeTcp extends Thread {