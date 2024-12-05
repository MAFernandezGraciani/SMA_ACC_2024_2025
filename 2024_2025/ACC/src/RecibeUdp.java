import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

/**
 * Esta clase se ha creado con el motivo de separar la lógica de recepción de mensajes UDP
 */
/**
 * Esta clase recibe los envíos que llegan mediante el protocolo DPP y los almacena como objetos de la clase mensaje en "contenedor_de_mensajes_a_enviar"
 * @author MAFG y Varios alumnos 2022-2023
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 * @version: 2023-2024-01
 * @observaciones:
 *      - Es necesario convertir el envío recibido en un objeto de clase "Mensaje"
 */
public class RecibeUdp extends Thread {

    protected Acc agente;  // Para tener acceso a los datos de este agente
    DatagramSocket servidor_UDP;

    /**
     * Constructor de la clase
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2023-2024-01
     * @param este_agente : Es el objeto agente, donde este objeto puede encontrar toda la informacion y recursos que necesita
     * @observaciones:
     *      - Inicializa datos
     *      - Arranca el hilo encargado de recibir mediante UDP
     */
    RecibeUdp(Acc este_agente) {
        // Inicializamos
        super();
        this.agente = este_agente;
        servidor_UDP = agente.servidor_UDP;

        // Arrancamos el hilo
        this.agente.hilo_RecibeUdp = new Thread(this, "hilo_RecibeUdp");
        this.agente.hilo_RecibeUdp.start();
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
    public void run() {
        String txt_RecibeUDp_01_log = " - desde RecibeUdp => run() - 01 \n El agente : "+ this.agente.ID_propio+" - desde la ip : "+ this.agente.Ip_Propia+" Arranca el hilo  : RecibeUdp";
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_RecibeUDp_01_log, 50);}

        try {
            // El socket de servicio UDP, ya se genero al buscar el nido "Acc => buscaNido()"
            byte[] bufer = new byte[65500];  // Le pongo poco menos de 64 k (65536) para ajustarlo al tamaño del paquete udp

            while(true)
            {
                // El servidor espera a que el cliente se conecte y devuelve un socket nuevo
                // Obtiene el flujo de entrada y lee el objeto del stream
//                DatagramPacket datos_recibido_UDP = new DatagramPacket(new byte[1024], 1024);
                DatagramPacket paquete_recibido_UDP = new DatagramPacket(bufer, bufer.length);

                String txt_RecibeUDP_02_log = " - Desde RecibeUdp => run() - 02. ESPERANDO paquete UDP en el agente con id  : "+agente.ID_propio +
                        " - con ip : "+agente.Ip_Propia+
                        " - y Puerto_Propio_UDP : "+agente.Puerto_Propio_UDP+
                        " - agente.num_ciclos_hilo_recibeUdp : " + agente.num_ciclos_hilo_recibeUdp;
                if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_RecibeUDP_02_log, 50);}

                // Recibimos el DatagramPacket
                servidor_UDP.receive(paquete_recibido_UDP);

                String paquete_recibido = new String(paquete_recibido_UDP.getData());
                String paquete_recibido_limpio = paquete_recibido.trim();

                   // Convertimos el envío recibido en objeto de la clase "Mensaje"
                String IP_or = String.valueOf(paquete_recibido_UDP.getAddress());
                int puerto_or = paquete_recibido_UDP.getPort();
                String id_or = "id_or por determinar";
                String IP_dest = agente.Ip_Propia;
                int puerto_dest =  agente.Puerto_Propio_UDP;
                String id_dest = agente.ID_propio;
                long tiempoUnixEnMilisegundos = System.currentTimeMillis();
                String protocolo = "UDP";
                String cuerpo_mens = paquete_recibido_limpio;

                Mensaje mensaje_recibido_UDP = new Mensaje("El ID_mensaje viene en el cuerpo del mensaje",
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
                mensaje_recibido_UDP.Xml_a_Dom(agente);

                String num_men_por_recibidos_str = String.valueOf(agente.num_elem_lita_recibidos());
                String txt_RecibeUDP_03_log = " -  Desde RecibeUdp => run() - 03. Mensaje UDP RECIBIDO desde el agente con id  : "+agente.ID_propio +
                        " - en la ip "+agente.Ip_Propia+
                        " - en la ip : "+agente.Ip_Propia+
                        " - en Puerto_Propio : "+agente.Puerto_Propio+
                        " - mensaje en cola de envio : "+num_men_por_recibidos_str+
                        " - total mensajes enviados : "+agente.num_elem_lita_enviar()+
                        "\n Destinatario id_destino : "+mensaje_recibido_UDP.id_destino+
                        " - en la ip : "+mensaje_recibido_UDP.IP_destino+
                        " - puerto destino : "+mensaje_recibido_UDP.puerto_destino+
                        " - protocolo : "+mensaje_recibido_UDP.protocolo+
                        "\n - mensaje : "+mensaje_recibido_UDP.cuerpo_del_mensaje;
                if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(txt_RecibeUDP_03_log, 50);}


                // Llevamos el mensaje al contenedor de recibidos
                agente.pon_en_lita_recibidos(mensaje_recibido_UDP);
                    System.out.println("\n ==> desde RecibeUdp => run() - 04\n  hemos recibido el mensage : " + mensaje_recibido_UDP.cuerpo_del_mensaje+
                            " - en contenedor tenemos : "+String.valueOf(agente.num_elem_lita_recibidos())+
                            " - total recibidos : "+agente.num_elem_lita_recibidos());

                agente.num_ciclos_hilo_recibeUdp++; // Actualizamos el numero de ciclos de este hilo
            } // FIn de - while (true) {
        }
        catch (Exception e) {
                //Si llegamos a un error, imprimimos la exception correspondiente
            String txt_err_RecibeUDP_001_log = "    ** ERROR: desde RecibeUdp => run() - 05. Con Exception : " + e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_RecibeUDP_001_log, 99); // 99 para error
        }
    } // Fin de - public void run() {
} // FIn de - public class RecibeUdp extends Thread {

