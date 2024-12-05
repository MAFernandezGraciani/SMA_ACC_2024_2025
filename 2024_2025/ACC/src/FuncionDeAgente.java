import java.util.Random;

/**
 * class FuncionDeAgente : Esta clase configura el hilo que ejecuta los procesos específicos de la función del agente
 * @author MAFG y Varios alumnos 2022-2023
 * @author MAFG y Varios alumnos 2023-2024
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 * @version: 2024-2025-01
 * @observaciones:
 *      - Loas tareas que realizan son las siguientes :
 *      - ****************** LA CLASE NO ESTA PROGRAMADA. SOLO ES UN PROTOTIPO *******************
 */
public class FuncionDeAgente implements Runnable {

    // Para pruebas
    protected Random random; // Lo utilizaremos para diversos eventos aleatorios
    protected float Frecuencia_envio;  // este parametro nos permite definir la latencia de envio de mensajes
    // Fin de - Para pruebas

    protected int num_men_enviados_fa; // Para identificar los mensajes enviados por este agente y poder identificarlos de forma unívoca
    protected int num_men_recibidos_fa; // Para identificar los mensajes recibidos por este agente y poder identificarlos de forma unívoca


    protected Acc agente; // Para poder acceder a los datos generales de este agente

    /**
     * public Acc : Contructor de la calse FuncionDeAgente
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @param  este_agente : Este agente, para poder acceder a sus datos
     * @observaciones:
     *      - Inicializa datos
     *      - Arranca el hilo asociado a este objeto
     */
    FuncionDeAgente(Acc este_agente) {
        // Para pruebas
        random = new Random();
        Frecuencia_envio = 0f;  // Entre 0 y 1. Cuanto mas pequeña menor frecuencia de envios
        // Fin de - Para pruebas

        this.agente = este_agente;

        // Para llevar control del numero de mensages enviados y recibidos
        num_men_enviados_fa = 0;
        num_men_recibidos_fa = 0;

        // arrancamos el hilo
        this.agente.hilo_FuncionDeAgente = new Thread(this, "hilo_FuncionDeAgente");
        this.agente.hilo_FuncionDeAgente.start();

    }

    /**
     * public void run() : Define el proceso que ejecuta este hilo
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - ESTE METODO ESTA ROTOTIPADO. Debera programarse para que realice las funciones especificas del agente (MAFG 2023-10-04)
     *      - Por ahora, se ha programado
     *          - La recepción de mensajes (cuando lo recibe, tan solo lo loguea si procede)
     *          - El envío de mensajes aleatorios a otro agente (cuando lo envía, tan solo lo loguea si procede)
     */
    @Override
    public void run() {
        String texto_run_hilo_fa = " - El agente : "+ this.agente.ID_propio+" - desde la ip : "+ this.agente.Ip_Propia+" Arranca el hilo  : FuncionDeAgente";
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_run_hilo_fa, 50);}

        while(true){
            // //////////////////////////////////////
            // Obtenemos los mensajes recibidos
                // Miramos si hay algun mensaje recibido y si lo hay lo recogemos
            if(agente.num_elem_lita_recibidos() > 0) {
            recogeMensajeRecibido();
            }

            // //////////////////////////////////////
            // enviamos un mensaje de vez en cuando
            if (Frecuencia_envio > this.random.nextFloat())
            {
                // Miramos en el directorio y seleccionamos un agente para enviar
                // PARA PRUEBAS, tomamos el primer agente del directorio y le enviamos el mensaje
                if ((agente.num_elem_directorio_de_agentes() > 0) & (agente.Estado_Actual == Acc.Estado_del_ACC.VIVO )) {
                    // seleccionamos el agente
                    AccLocalizado otro_agente = agente.saca_de_directorio_de_agentes();

                    // Construimos el mensaje
                    num_men_enviados_fa = num_men_enviados_fa + 1;
                    String IP_or = agente.Ip_Propia;
                    int puerto_or = agente.Puerto_Propio;
                    String id_or = agente.ID_propio;
                    long tiempoUnixEnMilisegundos = System.currentTimeMillis();
                    String IP_dest = otro_agente.IP;
                    int puerto_dest = otro_agente.puerto;
                    String id_dest = otro_agente.ID;
                    String protocolo = "UDP";

                    String ID_mensaje = agente.dame_codigo_id_local_men();
                    String momento_actual = String.valueOf(System.currentTimeMillis());
                    String puerto_dest_str = String.valueOf(puerto_dest);
                    String cuerpo_mens = "Esto es el MENSAJE num = " + num_men_enviados_fa +
                            " - que el agente : " + agente.ID_propio +
                            " - con ip " + agente.Ip_Propia +
                            " - con ID_mensaje " + ID_mensaje +
                            " - envia al agente con id_dest = "+id_dest+
                            " - con IP_dest = "+IP_dest+
                            " - con puerto_dest = "+puerto_dest_str+
                            " :  - en T = " + momento_actual;

                    Mensaje nuevo_mensaje = new Mensaje(ID_mensaje,
                            IP_or,
                            puerto_or,
                            id_or,
                            tiempoUnixEnMilisegundos,
                            IP_dest,
                            puerto_dest,
                            id_dest,
                            0,
                            protocolo,
                            cuerpo_mens,
                            null);

                    // Enviamos el mensaje a la cola de envíos
                    enviaMensaje(nuevo_mensaje);
                }
                agente.num_ciclos_hilo_FuncionDeAgente++; // Actualizamos el numero de ciclos de este hilo
            }
        } // Fin de while(true){
    } // Fin de - public void run() {

    /**
     * void recogeMensajeRecibido() : Toma un mensaje de "contenedor_de_mensajes_recibidos" y lo notifica
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2023-2024-01
     * @observaciones:
     *      - Este método debe reprogramarse cuando se desarrolle el sistema de comunicaciones. Por ahora esta tan solo PROTOTIPADO (MAFG 2023-10-04)
     *      - ****************** LA FUNCION NO ESTA PROGRAMADA. SOLO ES UN PROTOTIPO *******************
     */
    void recogeMensajeRecibido() {
        // Obtenemos el mensaje
        Mensaje mensajeRecibido = agente.saca_de_lita_recibidos();
        num_men_recibidos_fa = num_men_recibidos_fa + 1;

        // Lo notificamos (para PRUEBAS)
        String momento_actual = String.valueOf(System.currentTimeMillis());
        String texto_mensaje_rec_log = " - Desde procesaMensajeRecibido. El agenteagente : "+agente.ID_propio+
                                    " - con ip "+agente.Ip_Propia+
                                    " - ordinal = "+num_men_recibidos_fa+
                                    " - en T = "+momento_actual+
                                    "\n - Mensaje recibido : "+ mensajeRecibido.cuerpo_del_mensaje;
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_mensaje_rec_log, 50);}
    } // Fin de - void recogeMensajeRecibido() {

    /**
     * void enviaMensaje() : Inserta un mensaje para su envio en "contenedor_de_mensajes_a_enviar" y lo notifica
     * @author MAFG y Varios alumnos 2022-2023
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion: 2023-10-04
     * @version: 2024-2025-01
     * @param  nuevo_mensaje : Objeto Mensaje a enviar
     * @observaciones:
     *      - Este método debe reprogramarse cuando se desarrolle el sistema de comunicaciones. Por ahora esta tan solo PROTOTIPADO (MAFG 2023-10-04)
     */
    void enviaMensaje(Mensaje nuevo_mensaje) {
        // Insertamos el mensaje
        agente.pon_en_lita_enviar(nuevo_mensaje);

        // Lo notificamos (para PRUEBAS)
        String momento_actual = String.valueOf(System.currentTimeMillis());
        String texto_mensaje_a_list_env_log = "Desde generaMensajeAEnviar. El agenteagente : "+agente.ID_propio+
                                    " - con ip "+agente.Ip_Propia+
                                    " - envia el mensaje  : "+ nuevo_mensaje.cuerpo_del_mensaje+
                                    " - ordinal = "+num_men_enviados_fa+
                                    " - en T = "+momento_actual;
        if(agente.nivel_log > 50){agente.Gestor_de_logs.anota_en_log(texto_mensaje_a_list_env_log, 50);}
    } // Fin de - void enviaMensaje(Mensaje nuevo_mensaje) {

} // FIn de - public class FuncionDeAgente implements Runnable {
