import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.LinkedList;
import java.util.Random;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Esta clase genera el agente, inicializa sus valores y arranca los procesos necesarios para su funcionamiento
 * @author MAFG y Varios alumnos 2022-2023
 * @author MAFG y Varios alumnos 2023-2024
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 * @fechaDeUltimaModificacion: 2023-11-07 (inclusion de synchronized en los metodos al caso. Lo habia quitado para pruebas)
 * @fechaDeUltimaModificacion: 2023-11-18 (inclusion de funciones basicas de proceso de XML)
 * @version: 2023-2024-01-03  (anteriores : 2023-2024-01, 2023-2024-02)
 * @observaciones:
 *      - Contiene la información básica asociada al agente
 *          - Datos del agente
 *          - Datos del entorno de ejecución
 *          - Datos para ingeniería social
 *          - Datos de la función del agente
 *          - Herramientas para generar y manejar objetos DOM
 *          - Datos del sistema de comunicaciones
 *
 *      - En los metodos de "finalizaAgente()" y "notificaNacimiento()" puede verse como se envia un mensaje en formato XML cumpliendo con el esquema "esquema_AG_basico_01"
 */

public class Acc {

    // //////////////////////////////////////
    // //////////////////////////////////////
    // DATOS GLOBALES

    // //////////////////////////////////////
    // Datos del agente
    protected String ID_propio; // Identificador unico de este agente
    protected String Ip_Propia;  // Ip donde reside este agente
    protected int Puerto_Propio;  // Es el puerto asociado al agente (coincide con el puerto de servidor TCP del agente)
    protected int Puerto_Propio_TCP;  // Es el puerto de servidor TCP del agente (coincide con el puerto asociado al agente)
    protected int Puerto_Propio_UDP;  // Es el puerto de servidor UDP del agente (es el siguiente a "Puerto_Propio" osea - Puerto_Propio_UDP = Puerto_Propio+1)
    protected long Tiempo_de_nacimiento;  // La hora del sistema de esta maquina en la que se genera el agente
    protected tipos_de_agentes tipo_agente;  // Para indicar si es cambiacromos o monitor
    protected enum tipos_de_agentes
    {
        CAMBIACROMOS, MONITOR
    }
    protected long pid = obtenerPID();


    // //////////////////////////////////////
    // Datos del entorno de ejecución
    protected String nombre_sesion;  // Es el nombre de la sesion de ejecucion. Podemos ejecutar el SMA ACC varias veces, con la misma o distinta configuracion de codigo o
                                    // parametros de ejecucion. Este nombre de sesion debe definirse cada vez que se ejecuta el SAM_ACC, para que en el monitor al almacenar
                                    // los datos de ejecucion, puedan diferenciarse y analizarse los datos de las distintas ejecuciones del SMA
    protected String Ip_Monitor;  // Es la IP donde reside el monitor (es la misma para todos los agentes del SMA)
    protected int Puerto_Monitor;  // Es el puerto donde reside el monitor (es la misma para todos los agentes del SMA)
    protected int Puerto_Monitor_TCP;  // Es el puerto de servidor TCP del agente monitor (es el mismo que "Puerto_Monitor" y es la misma para todos los agentes del SMA)
    protected int Puerto_Monitor_UDP;  // Es el puerto de servidor UDP del agente monitor (es el mismo que "Puerto_Monitor+1" y es la misma para todos los agentes del SMA)
    protected String Inicio_rango_IPs; // Para indicar el inicio del rango de IPs donde este agente podrá buscar otros agentes
    protected int Rango_IPs; // Se suma a "Inicio_rango_IPs" para definir la ultima IP del rango donde este agente podrá buscar otros agentes
    protected int Puerto_Inicio; // Para indicar el inicio del rango de puertos donde este agente podrá buscar otros agentes, o buscar donde anidar
    protected int Rango_Puertos; // Se suma a "Puerto_Inicio" para definir el ultimo puerto del rango donde este agente podrá buscar otros agentes, o buscar donde anidar
    protected String localizacion_codigo;
    protected long tiempo_espera_fin_env; // Es el tiempo maximo que esperaremos para enviar los mensajen pendientes en la cola de envios, antes de finalizar el agente
                                        // si transcurrido esta cantidad de milisegundos sigue habiendo mensajes por enviar en la cola de envios, el agente se cierra y estos
                                        // quedaran sin enviar
    protected FuncionMonitor funcionMonitor;  // Sera un hilo de ejecución

        // Hilos de ejecucion
    protected Thread hilo_ComportamientoBase;
    protected Thread hilo_FuncionMonitor;
    protected Thread hilo_FuncionDeAgente;
    protected Thread hilo_RecibeTcp;
    protected Thread hilo_RecibeUdp;
    protected Thread hilo_Enviar;

        // Para la conexion a la BBDD que usa el monitor
    protected String localizacion_BBDD_monitor;  // Localizacion de la BBDD para el caso del monitor
    protected String nombre_BBDD_monitor;  // Nombre de la BBDD para el caso del monitor
    protected String usr_BBDD_monitor;  // Usuario para el acceso de la BBDD para el caso del monitor
    protected String pswd_BBDD_monitor;  // Pasword de usuario para el acceso de la BBDD para el caso del monitor
    protected Connection conex_BBDD_monitor; // La conexion a BBDD para el agente monitor

        // Para gestion de logs
    protected GestionLogs Gestor_de_logs; // Este objeto sera el que gistione la traza del proceso, anotando en concola, en el fichero de log o en ambos, segun este indicaco aqui
    protected boolean sustituir_fich_log; // Para gestionar si el fichero de log se inicializa antes de una nueva ejecucion del proceso, o se escriben los logs
                                // continuando a partir de los anteriores
                                //  sustituir_fich_log = true : eliminar el fichero anterior antes de generar uno nuevo al iniciar el proceso del agente
                                //  sustituir_fich_log = false : anotar los log continuando los anotados en ejecuciones anteriores
    protected boolean anotar_en_consola_log; // Para gestionar si se envian o no los logs a consola. (true: anota en consola - false : no anota en consola)
    protected boolean anotar_en_fich_log; // Para gestionar si se anota o no los logs en el fichero de losg. (true: anota en fichero - false : no anota en fichero)
    protected int nivel_log; // Para gestionar si se envian o no los logs a consola. Condicionando los logs a este valor, podemos activarlos o desactivarlos con facilidad
                                            // ver la clase "GestionLogs"
                                            // se define una escala. (normalmente 100, por debajo de este nivel se loguea, por encima no)
                                            //  50 para datos que estamos logueando en desarrollo
                                            //  99 para errores
                                            //  mayores que 100  para anotaciones de desarrollo que no queremos loguear
    protected int ordinal_log; // Para llevar una cuenta de los logs que va realizando el agente

    // Definimos las variables "num_ciclos_hilo_...", tan solo para monitorizar el funcionamiento de los hilos.
    protected int num_ciclos_hilo_recibeTcp;
    protected int num_ciclos_hilo_recibeUdp;
    protected int num_ciclos_hilo_enviar;
    protected int num_ciclos_hilo_ComportamientoBase;
    protected int num_ciclos_hilo_FuncionDeAgente;
    protected int num_ciclos_hilo_FuncionMonitor;

    // //////////////////////////////////////
    // Datos para ingeniería social
    protected ComportamientoBase comportamientoBase ;  // Sera un hlo de ejecución
    protected enum Estado_del_ACC
    {
        VIVO, MUERTO, FINALIZADO
    }
    protected Estado_del_ACC Estado_Actual;
    protected long Tiempo_de_vida; // Definimos aqui en milisegundos el tiempo que el proceso del agente estara activo antes de terminarse
    protected int Num_generacion; // Un agente que se arranca en una maquina genera procesos hijos y estos generan procesos nietos, este numero
                                    // indica a que generación corresponde este agente como descendiente del agente inicial
    protected int Num_max_de_generaciones; // Los agentes de este nivel de generaciones, no generaran agente hijos
    protected int Num_hijos_generados; // Define el numero de descendientes que este agente ha generado (en primera generación)
    protected int Num_max_hijos_generados; // Define el numero maximo de descendientes de primera generación. que este agente puede generar
    protected double Frecuencia_partos;  // Para manejar la velocidad en la que el agente se reproduce
    protected double Frecuencia_rastreo_puertos; // Para manejar la velocidad en la que el agente busca otros agentes

    // //////////////////////////////////////
    // //////////////////////////////////////
    // Datos de la función del agente
    protected FuncionDeAgente funcionAgente;  // Sera un hlo de ejecución

    // //////////////////////////////////////
    // //////////////////////////////////////
    // Datos del sistema de comunicaciones
    protected Enviar enviar;  // Sera un hlo de ejecución
    protected RecibeTcp recibeTcp;  // Sera un hlo de ejecución
    protected RecibeUdp recibeUdp;  // Sera un hlo de ejecución

    protected ServerSocket servidor_TCP;  // Puerto para el servicio por TCP
    protected DatagramSocket servidor_UDP;  // Puerto para el servicio por UDP

    private LinkedList<AccLocalizado> directorio_de_agentes = new LinkedList<>(); // Contenedor para almacenar los agentes que este agente ha localizado
    private  int num_tot_acc_loc;  // Numero total de agentes localizados
    private LinkedList<Mensaje> contenedor_de_mensajes_a_enviar = new LinkedList<>(); // Contenedor para almacenar cada uno de los mensajes para enviar por el agente
    private  int num_tot_men_env;  // Numero total de mensajes enviados por el agente
    private LinkedList<Mensaje> contenedor_de_mensajes_recibidos = new LinkedList<>(); // Contenedor para almacenar cada uno de los mensajes recibidos por el agente
    private  int num_tot_men_rec;  // Numero total de mensajes recibidos por el agente
    private  int num_id_local_men;  // Este numero, junto con el identificador del agente, generan un codigo unico de mensaje


    // //////////////////////////////////////////////////
    // Definimos primero algunos objetos que seran necesarios para trabajar con documentos XML, estos pueden generarse una vez
    // y despues ser usados cada vez que hagan falta

    // Herramientas para generar y manejar objetos DOM
    private DocumentBuilderFactory dom_factory = null;
    protected DocumentBuilder dom_builder = null;
    // Heramientas para validar documentos XML
    private SchemaFactory factory_esquema = null;
    private Schema este_schema = null;
    protected Validator validador_xml = null;
    // Herramientas para pasar de DOM a string
    private TransformerFactory xml_transformerFactory = null;
    protected Transformer xml_transformer = null;

    // Definimos ahora una serie de Strings genericas, para manejo de XML
//    String  esquema_AG_basico_01 = "D:\\Datos\\miki\\Docencia\\PracticasSMA\\Codigo\\cambiaCromos\\2023_2024\\out\\production\\2023_2024\\esquema_AG_basico_01.xsd"; // Localizacion del fichero de esquema XML con el que vamos a trabajar
//    String  esquema_AG_basico_01 = "D:\\Datos\\miki\\Docencia\\PracticasSMA\\Codigo\\cambiaCromos\\2024_2025\\out\\production\\2024_2025\\esquema_AG_basico_01.xsd"; // Localizacion del fichero de esquema XML con el que vamos a trabajar
    String  esquema_AG_basico_01 = "esquema_AG_basico_02.xsd"; // Localizacion del fichero de esquema XML con el que vamos a trabajar
    String cabecera = "<?xml version='1.0' encoding='UTF-8'?>";
    String raiz_sin_esquema = "<Message>";
    String raiz_con_esquema = "<Message xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='"+esquema_AG_basico_01+"'>";
    String cola = "</Message>";
    String elm_Message = "Message"; // Elemento raiz del XML de mensaje
    String elm_comunc_id = "comunc_id"; // elemento identificador de la comunicaion
    String elm_protocol_step = "protocol_step"; // elemento identificador del ordinal del paso dentro de un protocolo de comunicacion concreto
    String elm_comunication_protocol = "comunication_protocol"; // Elemento que define el protocolo asociado al mensaje
    String elm_origin = "origin"; // Datos del agente que envia el mensaje
    String elm_destination = "destination"; // Datos del agente que envia el mensaje
    String elm_body = "body";  // Elemento que contiene el cuerpo del mensaje
    String elm_common_content = "common_content";

    /**
     * public Acc : Contructor de la calse Acc
     * @author : MAFG y Varios alumnos 2022-2023
     * @author : MAFG y Varios alumnos 2023-2024
     * @author : MAFG y Varios alumnos 2024-2025
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     *      - 2024-10-01
     * @version: 2024-2025-01
     * @param : ID_propio : sera el identificador único "ID_propio" del agente
     * @param : este_Num_generacion_str : indica la profundidad de la generación en la que se genera el agente "Num_generacion"
     * @observaciones:
     *      - Inicializa los datos del agente
     *      - Busca un puerto libre en esta maquina donde alojarse "buscaNido()"
     *      - Configura el estado inicial "generaConfiguracionInicial()"
     *      - Genera los hilos donde correran los procesos que se ejecutan paralelamente en el agente
     *          Hilos de comunicaciones
     *          - Enviar
     *          - RecibeTcp
     *          - RecibeUdp
     *          - ComportamientoBase : procesos básicos del agente
     *              Dependiendo de si el agente es monitor o cambiaCromos :
     *              - En el caso del CAMBIACROMOS :
     *                  - FuncionDeAgente : Procesos especificos del agente
     *                  - Notifica su existencia al monitor "notificaNacimiento"
     *              - En el caso del MONITOR :
     *                  - FuncionMonitor : Procesos especificos del agente monitor
     */
    public Acc (String ID_propio, String este_Num_generacion_str, String este_tipo_agente, String este_Ip_Monitor, String este_Puerto_Monitor) {

        nombre_sesion = "nombre_de_sesion_por_defecto";
        this.ID_propio = ID_propio;  // identificamos el agente
        this.pid = obtenerPID(); // Obtiene el PID del proceso actual para poder pararlo

        // Para la conexion a la BBDD que usa el monitor
        this.localizacion_BBDD_monitor = "jdbc:mysql://localhost:3306/";  // jdbc:mysql:// => para indicar quee trabajamos con MySql
                                                                    // localhost => para indicar que la BBDD esta en la maquina local
                                                                    // :3306/ => para indicar por el puerto que atiende
                                                                    // Debe ser sustituido por el adecuado en cada implantacion
        this.nombre_BBDD_monitor = "acc_bbdd";  // Debe ser sustituido por el adecuado en cada implantacion
        this.usr_BBDD_monitor = "usrAcc";  // Debe ser sustituido por el adecuado en cada implantacion
        this.pswd_BBDD_monitor = "hd83mmi8812a";  // Debe ser sustituido por el adecuado en cada implantacion

        // Para gestion de logs
        this.sustituir_fich_log = true; // True : sustituir archivo - false : escribir a continuacion de lo anterior
        this.anotar_en_consola_log = true; // Para gestionar si se envian o no los logs a consola. (true: anota en consola - false : no anota en consola)
        this.anotar_en_fich_log = true; // Para gestionar si se anota o no los logs en el fichero de losg. (true: anota en fichero - false : no anota en fichero)
        this.nivel_log = 100; // Queremos loguear errores y desarrollo activado
        this.ordinal_log = 0;
        this.Gestor_de_logs = new GestionLogs(this);

        String texto_inicio_log = "\n    >>>>>>>>> INICIO LOG AGENTE " +
                " - ID_propio : "+ ID_propio+
                " - Num generacion : "+ este_Num_generacion_str+
                " - Tipo agente : "+ este_tipo_agente+
                " - Ip Monitor : "+ este_Ip_Monitor +
                " - Puerto Monitor : "+ este_Puerto_Monitor+
                " - PID proceso : "+pid+" -  Para matar el proceso : taskkill /PID "+pid+" /F  ";

        this.Gestor_de_logs.anota_en_log(texto_inicio_log, 50); // 50 porque queremos que lo loguee

           // Definimos los datos y parametros operativos del agente. Con ellos podremos manejar sus caracteísticas
        generaConfiguracionInicial(ID_propio, este_Num_generacion_str, este_tipo_agente, este_Ip_Monitor, este_Puerto_Monitor);

        String txt_fin_conf_log = "   >> el agente ha finalizado su configuracion : ";
        if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(txt_fin_conf_log, 50); }

        // Buscamos el puerto donde alojarlo.
        if(this.tipo_agente == tipos_de_agentes.CAMBIACROMOS)
        {
            // Para el agente CAMBIACROMOS tenemos que buscar los puertos donde albergar el agente
            buscaNido();

            String texto_anidado_log = ">> el agente ha anidado en Puerto_Propio : "+ this.Puerto_Propio +
                            " - Puerto_Propio_TCP : " + this.Puerto_Propio_TCP +
                            " - Puerto_Propio_UDP : " + this.Puerto_Propio_UDP;
            if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_log, 50); }
        }
        else if (this.tipo_agente == tipos_de_agentes.MONITOR) {
            // Para el agente MONITOR los puertos vienen fijados al generar el agente
            this.Puerto_Propio = this.Puerto_Monitor;
            this.Puerto_Propio_TCP = this.Puerto_Propio;
            this.Puerto_Propio_UDP = this.Puerto_Propio + 1;

            // Generamos los sockets de TCP y UDP en el monitor, ya que este sabe cuales son sus puertos y no usa "buscaNido()" para localizarse
            try {
                servidor_TCP = new ServerSocket(Puerto_Propio_TCP);
                servidor_UDP = new DatagramSocket(Puerto_Propio_UDP);
            } catch (Exception e) {
                String texto_err_001_log = "    **>> ERROR 001.  \n al abrir los puertos de comunicaciones con Puerto_Propio : " + Puerto_Propio +
                        " - con Puerto_Propio_TCP : " + Puerto_Propio_TCP +
                        " - con Puerto_Propio_UDP : " + Puerto_Propio_UDP;
                this.Gestor_de_logs.anota_en_log(texto_err_001_log, 99); // 99 para error
                finalizaAgente(); // Si no ha funcionado, finalizamos
            }
        } // Fin de - else if (this.tipo_agente == tipos_de_agentes.MONITOR) {
        else
        {
            String texto_err_002_log = "    **> ERROR 002. Al procesar el tipo de agente al buscar nido";
            this.Gestor_de_logs.anota_en_log(texto_err_002_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
        }

        // Generamos los hilos de ejecucion que utiliza ente agente
            // Hilos de comunicaiones
        this.recibeTcp = new RecibeTcp(this);
        this.recibeUdp = new RecibeUdp(this);
        this.enviar = new Enviar(this);
            // Hilos de comportamiento
        this.comportamientoBase = new ComportamientoBase(this);
            // Dependiendo del tipo de agente
        if(this.tipo_agente == tipos_de_agentes.CAMBIACROMOS)
        {
                // Para el agente CAMBIACROMOS arrancamos su funcion del agente y notificamos al monitor su nacimiento
            this.funcionAgente = new FuncionDeAgente(this);
            notificaNacimiento();
        }
        else if (this.tipo_agente == tipos_de_agentes.MONITOR)
        {
            // Para el agente MONITOR tan solo arrancamos su funcion del agente monitor
            this.funcionMonitor = new FuncionMonitor(this);
            // EL monitor no se notifica su propio nacimiento
        }
        else
        {
            String texto_err_003_log = "    **> ERROR 003. Al procesar el tipo de agente al ir a notificar el nacimiento";
            this.Gestor_de_logs.anota_en_log(texto_err_003_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
        }

        this.Estado_Actual = Estado_del_ACC.VIVO;
    }

    /**
     *  generaConfiguracionInicial() : realiza la configuracion inicial del agente
     * @author MAFG y Varios alumnos 2022-2023
     * @author MAFG y Varios alumnos 2023-2024
     * @author MAFG y Varios alumnos 2024-2025
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     *      - 2023-11-08 (Incluye proceso de XML)
     *      - 2024-10-01
     * @version: 2024-2025-01
     * @observaciones:
     *
     */
    protected void generaConfiguracionInicial(String ID_propio, String este_Num_generacion_str, String este_tipo_agente, String este_Ip_Monitor, String este_Puerto_Monitor) {

        // //////////////////////////////////////
        // Definiendo datos del agente
        try {

            // Para la ip de la maquina donde se ejecuta el agente, proponemos dos opciones
            // 1. de forma automatica (en ocasiones si estamos en una red privada, nos devuelve la IP externa del router que nos da salida a internet)
            this.Ip_Propia = InetAddress.getLocalHost().getHostAddress();
            // 2. manualmente, en este caso habra que mirar desde una consola de windos la ip que tenemos asignada (si es una red privada sera 192.168...)
            //      Si optamos por la opcion 1. simplemente hay que comentar esta
            this.Ip_Propia = "192.168.1.133";
        } catch (UnknownHostException e) {
            String texto_err_004_log = "    **> ERROR 004. Desde Acc => en geraConfiguracionInicial() Al adquirir la Ip_Propia, da un valor : " + Ip_Propia;
            this.Gestor_de_logs.anota_en_log(texto_err_004_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
            throw new RuntimeException(e);
        }

        if (este_tipo_agente.equals("MONITOR")) {
            this.tipo_agente = tipos_de_agentes.MONITOR;
        } else if (este_tipo_agente.equals("CAMBIACROMOS")) {
            this.tipo_agente = tipos_de_agentes.CAMBIACROMOS;
        } else {
            String texto_err_005_log = "    **> ERROR 005. Desde Acc => en geraConfiguracionInicial() Al error definiendo tipo_agente : " + este_tipo_agente;
            this.Gestor_de_logs.anota_en_log(texto_err_005_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
        }

        // //////////////////////////////////////
        // Definiendo Datos del entorno de ejecución
        if (este_tipo_agente.equals("MONITOR")) {
            this.Ip_Monitor = this.Ip_Propia; // Si el agente es el monitor su ip y la del monitor son la misma evidentemente

            // Preparamos la conexion a la BBDD del monitor
            String acceso_BBDD = localizacion_BBDD_monitor + nombre_BBDD_monitor;
//            try (Connection esta_conexion = DriverManager.getConnection(acceso_BBDD, this.usr_BBDD_monitor, this.pswd_BBDD_monitor))
            try
            {
                conex_BBDD_monitor = DriverManager.getConnection(acceso_BBDD, this.usr_BBDD_monitor, this.pswd_BBDD_monitor);

                String texto_conex_BBDD_ok_log = "Desde Acc => en geraConfiguracionInicial() Conesion a BBDD OK";
                if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_conex_BBDD_ok_log, 50); }
            } catch (SQLException e_conexion) {
                String texto_err_009_log = "    **> ERROR 009. Desde Acc => en geraConfiguracionInicial() Al crear la BBDD con acceso_BBDD : " + acceso_BBDD +
                                        " - this.usr_BBDD_monitor : " + this.usr_BBDD_monitor +
                                        " - this.usr_BBDD_monitor : " + e_conexion.getMessage();
                this.Gestor_de_logs.anota_en_log(texto_err_009_log, 99); // 99 para error
            }

        } else if (este_tipo_agente.equals("CAMBIACROMOS")) {
            this.Ip_Monitor = este_Ip_Monitor;
        } else {
            String texto_err_006_log = "    **> ERROR 006. Desde Acc => en geraConfiguracionInicial(). Error Definiendo DATOS DEL ENTORNO : " + este_tipo_agente;
            this.Gestor_de_logs.anota_en_log(texto_err_006_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
        }
        this.Puerto_Monitor = Integer.parseInt(este_Puerto_Monitor);  // El puerto de monitor se define como parametro de llamada al proceso
        this.Puerto_Monitor_TCP = this.Puerto_Monitor;  // Es el puerto de servidor TCP del agente monitor (es el mismo que "Puerto_Monitor" y es la misma para todos los agentes del SMA)
        this.Puerto_Monitor_UDP = this.Puerto_Monitor + 1;  // Es el puerto de servidor UDP del agente monitor (es el mismo que "Puerto_Monitor+1" y es la misma para todos los agentes del SMA)

        this.Inicio_rango_IPs = Ip_Propia;  // Solo para pruebas
        this.Rango_IPs = 0;
        this.Puerto_Inicio = 50000;
        this.Rango_Puertos = 50;
        this.localizacion_codigo = "D:/Datos/miki/Docencia/PracticasSMA/Codigo/cambiaCromos/2024_2025/out/production/2024_2025/";
        this.tiempo_espera_fin_env = 1000 * 1; // Es el tiempo (milisegundos) que esperaremos para enviar los mensajen pendientes en la cola de envios, antes de finalizar el agente

            // Inicializamos el numero de ciclos ejecutado por cada hilo a cero
        this.num_ciclos_hilo_recibeTcp = 0;
        this.num_ciclos_hilo_recibeUdp = 0;
        this.num_ciclos_hilo_enviar = 0;
        this.num_ciclos_hilo_ComportamientoBase = 0;
        this.num_ciclos_hilo_FuncionDeAgente = 0;
        this.num_ciclos_hilo_FuncionMonitor = 0;

        // //////////////////////////////////////
        // Definiendo  Datos para ingeniería social
        this.Estado_Actual = Estado_del_ACC.VIVO;
        this.Tiempo_de_nacimiento = System.currentTimeMillis();
        if (este_tipo_agente.equals("MONITOR")) {
            this.Tiempo_de_vida = 1000 * 100;  // Lo es en milisegundos
        } else if (este_tipo_agente.equals("CAMBIACROMOS")) {
            this.Tiempo_de_vida = 1000 * 50;  // Lo es en milisegundos
        } else {
            String texto_err_007_log = "    **> ERROR 007. Desde Acc => en geraConfiguracionInicial(). Error definiendo Tiempo_de_vida : " + este_tipo_agente;
            this.Gestor_de_logs.anota_en_log(texto_err_007_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
        }
        int este_Num_generacion = Integer.parseInt(este_Num_generacion_str);
        this.Num_generacion = este_Num_generacion;
        this.Num_max_de_generaciones = 1;
        this.Num_hijos_generados = 0; // Por ahora el agente no ha generado ningún descendiente
        this.Num_max_hijos_generados = 2; // el agente no debe superar este numero de descendientes en primera generacion (en principio arbitrario
        this.Frecuencia_partos = 0.01;
        this.Frecuencia_rastreo_puertos = 0.00001f;

        // //////////////////////////////////////
        // Definimos datos del sistema de comunicaciones
        this.num_tot_acc_loc = 0;  // Numero total de agentes localizados
        this.num_tot_men_env = 0;  // Numero total de mensajes enviados por el agente
        this.num_tot_men_rec = 0;  // Numero total de mensajes recibidos por el agente
        this.num_id_local_men = 0;  // Cada vez que se solicita, se incrementa en uno, para generar un codigo local unico para los mensajes (ver dame_codigo_id_local_men())

        // ////////////////////////////////////////////////////////////////////////////
        // ////////////////////////////////////////////////////////////////////////////
        // Definimos y generamos los objetos generales Para manejo de XML
        try {
            // ///////////////////////////////
            // Generamos las herramientas para VALIDAR con el esquema
            factory_esquema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            este_schema = factory_esquema.newSchema(new StreamSource(new File(esquema_AG_basico_01)));
            validador_xml = este_schema.newValidator();

            // Para validar un objeto DON contra el esquema habria que hacer :
            //  - Para validar un documento XML almacenado en un objeto DOM
            //      validador_xml.validate(new DOMSource(obj_document)); // donde obj_document es un objeto DOM de la clase Document
            //  - Para validar un documento XML almacenado en un archivo del sistema
            //      validador_xml.validate(new StreamSource(new File((String)archivoXML))); // donde obj_document es un objeto DOM de la clase Document
            //  - Para validar un documento XML almacenado en un string
            //      validador_xml.validate(new StreamSource(new StringReader(xmlString))); // donde obj_document es un objeto DOM de la clase Document

            // OJOOOO, como en las siguientes sentencias, al construir el "dom_factory", ya le asignamos  el esquema, cada vez que se ejecuta el
            // "dom_builder", se valida el documento.
            //  - Como el "dom_builder" se ejecuta al recibir y antes de mandar el XML (que es cuando estan construidos definitivamente), no
            //      es necesario validarlos en otros estados de proceso

            // ///////////////////////////////
            // Generamos las herramientas para construir los objetos DOM
            dom_factory = DocumentBuilderFactory.newInstance();
            dom_factory.setNamespaceAware(true); // Habilitamos los espacios de nombres. Son necesarios entre otras cosas para que reconozca la referencia al esquema
            dom_factory.setSchema(este_schema); // Validamos CONTRA EL ESQUEMA
            dom_builder = dom_factory.newDocumentBuilder();

            // ///////////////////////////////
            // Generamos las herramientas para pasar un xml de DOM a string
            xml_transformerFactory = TransformerFactory.newInstance();
            xml_transformer = xml_transformerFactory.newTransformer();
            // Para obtener el string de un XML que esta en un objeto DOM
            //      Ver (public String Dom_a_Xml(Document xml_Dom)
        }
        catch (Exception e)
        {
  //          e.printStackTrace();
            String texto_err_008_log = "    **> ERROR 008. Desde Acc => en geraConfiguracionInicial(). Error  El DOM XML no es válido según el esquema XSD.\n e.getMessage() : " + e.getMessage();
            this.Gestor_de_logs.anota_en_log(texto_err_008_log, 99); // 99 para error
            finalizaAgente(); // Si no ha funcionado, finalizamos
        }

    } // Fin de - protected void generaConfiguracionInicial)

    /**
     * buscaNido() : Busca en la maquina dosde se esta ejecutando un puerto libre para alojar al agente
     * @author MAFG y Varios alumnos 2022-2023
     * @author MAFG y Varios alumnos 2023-2024
     * @author MAFG y Varios alumnos 2024-2025
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     *      - 2024-10-01
     * @version: 2024-2025-01
     * @observaciones:
     *      - Recordamos que la ip del agente "Puerto_Propio" es la misma que la ip de su puerto de escucha TCP "Puerto_Propio_TCP", asi mismo
     *          el puerto se escucah UDP "Puerto_Propio_UDP" de este agente se coloca en el puerto siguiente a este.
     *      - Cada agente ocupa pues un par de puertos consegutivos, puerto par - TCP y puerto impar - UDP
     *      - Detro del rango de IPs donde debemos buscar los puertos donde anide el agente, buscamos una localizacion
     *          aleatoriamente y luego vamos buscando por puertos consecutivos hasta recorrer todo el rango de Ips
     *
     */
    protected void buscaNido() {

        String texto_anidado_in_log = ">> Desde Acc => buscaNido() 01. entrando";
        if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_in_log, 50);}

        Random rand = new Random();
        int puerto_ini_busqueda = Puerto_Inicio + rand.nextInt(Rango_Puertos + 1);
        puerto_ini_busqueda = (puerto_ini_busqueda / 2) * 2; // Nos aseguramos que el numero sea par
        int puerto_busqueda = puerto_ini_busqueda;
        boolean sigue_buscando = true;
            // Para evitar que el proceso se eternice
        int num_intentos = 0; // Para llevar una cuenta del numero de puertos en los que hemos intentado anidar
        int max_num_intentos = 5000; // Para llevar una cuenta del numero de puertos en los que hemos intentado anidar
        long T_ini_busqueda =  System.currentTimeMillis();  // La hora del sistema de esta maquina en la que se inicia la busqueda de nido
        long T_max_busqueda = 1000 * 10;  // El periodo maximo de tiempo que permitimos que el agente este buscando su nido (en milisegundos)
        long T_limite_busqueda = T_ini_busqueda + T_max_busqueda;  // El momento en el que el agente debe parar de buscar su nido (en milisegundos)

        int num_ciclo = 0;
        while (sigue_buscando) {
            boolean TCP_ok = false;

            num_ciclo = num_ciclo+1;
            String texto_anidado_en_while_log = ">> Desde Acc => buscaNido() 01. dentro del while, en ciclo : " + num_ciclo;
            if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_en_while_log, 50);}

            try {
                servidor_TCP = new ServerSocket(puerto_busqueda);
                TCP_ok = true;
                String texto_anidado_enTCP_log = ">> Desde Acc => buscaNido() 01. En TCP puerto : "+ puerto_busqueda +" - dentro del while, en ciclo : " + num_ciclo;
                if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_enTCP_log, 50);}

                servidor_UDP = new DatagramSocket(puerto_busqueda + 1);
                String texto_anidado_enUDP_log = ">> Desde Acc => buscaNido() 01. En UDP puerto : "+ puerto_busqueda +" - dentro del while, en ciclo : " + num_ciclo;
                if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_enUDP_log, 50);}

                // Si hemos podido ocupar los dos puertos, ya son nuestros y por tanto anotamos nuestra localizacion
                this.Puerto_Propio = puerto_busqueda;
                this.Puerto_Propio_TCP = Puerto_Propio;
                this.Puerto_Propio_UDP = Puerto_Propio +1;

                // Si los dos puertos han funcionado, ya tenemos nido y podemos para de buscar
                sigue_buscando = false;

                long T_actual = System.currentTimeMillis();
                long T_buscando = System.currentTimeMillis() - T_ini_busqueda;
                String texto_anidado_log = ">> Desde Acc => buscaNido() 01. \n   ANIDADO CORRECTAMENTE con num_intentos : "+num_intentos+
                        " - con max_num_intentos : "+ max_num_intentos +
                        " - con T_ini_busqueda : "+ T_ini_busqueda +
                        " - con T_actual : "+ T_actual +
                        " - con T_limite_busqueda : "+ T_limite_busqueda +
                        " - tiempo invertido (milisegundos) : "+ T_buscando+
                        "\n - anidado en Puerto_Propio : "+ this.Puerto_Propio +
                        " - Puerto_Propio_TCP : " + this.Puerto_Propio_TCP +
                        " - Puerto_Propio_UDP : " + this.Puerto_Propio_UDP;
                if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_log, 50);}

            }
            catch (java.net.BindException puerto_en_uso)
            {
                String texto_anidado_puerto_Ocupado_log = ">> Desde Acc => buscaNido() 01. PPUERTO COUPADO : "+ puerto_busqueda +" - dentro del while, en ciclo : " + num_ciclo;
                if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_anidado_puerto_Ocupado_log, 50);}
                // Si NO hemos podido ocupar los dos puertos, debemos seguir buscando mas adelante
                puerto_busqueda++;
                num_intentos++;
                if (puerto_busqueda > (Puerto_Inicio + Rango_Puertos)) {
                    puerto_busqueda = Puerto_Inicio;
                } // SI nos salimos del rango, volvemos al principio

                if (TCP_ok) {
                    // SI hemos llegado aqui es que hemos podido abrir "servidor_TCP", pero no "servidor_UDP", por lo que cerramos "servidor_TCP"
                    // para que quede todo como estaba
                    try
                    {
                        servidor_TCP.close();
                    }
                    catch (IOException IO_e )
                    {
                        long T_actual = System.currentTimeMillis();
                        long T_buscando = System.currentTimeMillis() - T_ini_busqueda;
                        String texto_err_009_log = "    **>> ERROR. ==> Desde Acc => buscaNido() 02. error  al intentar derrar el socket TCP. \n -- con puerto_busqueda : "+ puerto_busqueda+
                                " - con max_num_intentos : "+ max_num_intentos +
                                " - con T_ini_busqueda : "+ T_ini_busqueda +
                                " - con T_actual : "+ T_actual +
                                " - con T_limite_busqueda : "+ T_limite_busqueda +
                                " - tiempo invertido (milisegundos) : "+ T_buscando+
                                " - con IO_e : "+IO_e;
                        this.Gestor_de_logs.anota_en_log(texto_err_009_log, 99); // // 99 para error
                        finalizaAgente(); // Si no ha funcionado, finalizamos
                    }
                } // Fin de - if (TCP_ok) {

                // COntrolamos si debemos detener la busqueda de nido
                long T_actual = System.currentTimeMillis();
                if((num_intentos > max_num_intentos) || (T_actual > T_limite_busqueda))
                {
                    sigue_buscando = false;
                    long T_buscando = System.currentTimeMillis() - T_ini_busqueda;
                    String texto_deten_buscanido_log = ">> Desde Acc => buscaNido() 03. \n  .Detenemos la busqueda por exceso de intentos o tiempo con num_intentos : "+num_intentos+
                            " - con max_num_intentos : "+ max_num_intentos +
                            " - con T_ini_busqueda : "+ T_ini_busqueda +
                            " - con T_actual : "+ T_actual +
                            " - con T_limite_busqueda : "+ T_limite_busqueda +
                            " - tiempo invertido (milisegundos) : "+ T_buscando;
                    if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_deten_buscanido_log, 50);}
                    finalizaAgente(); // Si no ha funcionado, finalizamos
                }
            } // Fin de - try catch (
            catch (IOException e_IO)
            {
                String texto_err_e_IO_001_log = "    **>> ERROR. ==> Desde Acc => buscaNido() 02.  error IOException al buscar nido con  \n -- con puerto_busqueda : "+ puerto_busqueda+
                        " - num_intentos : "+num_intentos+
                        " - con max_num_intentos : "+ max_num_intentos +
                        " - con T_ini_busqueda : "+ T_ini_busqueda +
                        " - con T_limite_busqueda : "+ T_limite_busqueda +
                        " - con e_IO.getMessage : "+e_IO.getMessage();
                this.Gestor_de_logs.anota_en_log(texto_err_e_IO_001_log, 99); // 99 para error
                finalizaAgente(); // Si no ha funcionado, finalizamos
            } // Fin de - try catch (
            catch (Exception e)
            {
                String texto_err_e_IO_001_log = "    **>> ERROR. ==> Desde Acc => buscaNido() 02.  error Exception al buscar nido con  \n -- con puerto_busqueda : "+ puerto_busqueda+
                        " - num_intentos : "+num_intentos+
                        " - con max_num_intentos : "+ max_num_intentos +
                        " - con T_ini_busqueda : "+ T_ini_busqueda +
                        " - con T_limite_busqueda : "+ T_limite_busqueda +
                        " - con e.getMessage : "+e.getMessage();
                this.Gestor_de_logs.anota_en_log(texto_err_e_IO_001_log, 99); // 99 para error
                finalizaAgente(); // Si no ha funcionado, finalizamos
            } // Fin de - try catch (
        }  // FIn de - while (sigue_buscando)
    } // FIn de - protected void buscaNido()

    /**
     *  notificaNacimiento() : Notifica al monitor que este agente ha sido generado
     * @author MAFG y Varios alumnos 2022-2023
     * @author MAFG y Varios alumnos 2023-2024
     * @author MAFG y Varios alumnos 2024-2025
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     *      - 2023-11-09 : Se incluye el formato XML en los mensages
     *      - "024-10-01
     * @version: 2024-2025-01
     * @observaciones:
     *      - Este mensaje se envia por (TCP)
     *      - (PENDIENTE MAFG 2023-10-04) Falta poner el mensaje con formato XML (creo que ya esta, no se si falta algo MAFG 2024-10-21)
     */
    protected void notificaNacimiento() {
        // Por ahora solo es una función prototipo
        // Construimos el mensaje

        String ID_mensaje = dame_codigo_id_local_men();
        String momento_actual = String.valueOf(System.currentTimeMillis());
        String Puerto_Propio_str = String.valueOf(Puerto_Propio);
        String Puerto_Monitor_UDP_str = String.valueOf(Puerto_Monitor_UDP);

        long tiempoUnixEnMilisegundos = System.currentTimeMillis();

        String cuerpo_mens = ">> Desde Acc => notificaNacimiento() 01. \n  Esto es el MENSAJE HE NACIDO  \n- que el agente con ID_propio : " + ID_propio +
                "\n - con ip : " + Ip_Propia +
                "\n - con Puerto_Propio : " + Puerto_Propio_str +
                "\n - con ID_mensaje : " + ID_mensaje +
                "\n - envia al monitor con Ip_Monitor : "+Ip_Monitor+
                "\n - con Puerto_Monitor : "+Puerto_Monitor_UDP_str+
                "\n :  - en T : " + momento_actual;

        Mensaje mensaje_he_nacido = new Mensaje(ID_mensaje,
                Ip_Propia,
                Puerto_Propio,
                ID_propio,
                tiempoUnixEnMilisegundos,
                Ip_Monitor,
                Puerto_Monitor_UDP,
                "monitor",
                0,
                "UDP",
                cuerpo_mens,
                null);

        // //////////////////////////////////////////
        // Generamos ahora el DOM del XML que sera el mensaje que enviaremos
            // generamos la cabecera, este DOM quedara localizado en "mensaje_he_nacido.DOM_cuerpo_del_mensaje"
        String este_comunc_id = "nacimiento_de_agente_"+ID_propio;
        String este_type_protocol = "heNacido";  // es el protocolo de nacimiento
        int este_protocol_step = 0;  // este protocolo solo tiene el paso 0
        mensaje_he_nacido.genera_Dom_base_mensaje(this, este_comunc_id, este_type_protocol, este_protocol_step);
            // cumplimentamos el contenido del cuerpo del mensaje
                // Preparamos el elemento "body_info"
        Element e_body_info = mensaje_he_nacido.DOM_cuerpo_del_mensaje.createElement("body_info");
        String e_body_info_str ="Este es el mensaje de HE NACIDO con XML del agente : " + this.ID_propio;
        e_body_info.setTextContent(e_body_info_str);
                // Localizamos el elemento "body" y hacemos "body_info" su descendiente
        mensaje_he_nacido.DOM_cuerpo_del_mensaje.getElementsByTagName("body").item(0).appendChild(e_body_info);

        // ///////////////////////////////////////////
        // Insertamos el mensaje
        pon_en_lita_enviar(mensaje_he_nacido);

        String Num_generacion_str = String.valueOf(this.Num_generacion);
        String Tiempo_de_nacimiento_str = String.valueOf(this.Tiempo_de_nacimiento);
        String texto_he_nacido_log = ">> Desde Acc => notificaNacimiento() 02. Ha nacido un agente en la IP = "+Ip_Propia+
                                " - con ID_propio :" + this.ID_propio +
                                " - en el puerto :" + this.Puerto_Propio +
                                " - Su generación es :" + Num_generacion_str +
                                " - t de generación :" + Tiempo_de_nacimiento_str;
        if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_he_nacido_log, 50);}
    } // Fin de - protected void notificaNacimiento() {

    /**
     *  notificaFin() : Notifica al monitor que este agente ha finalizado e informa del resultado de la realizacion del proceso
     * @author MAFG y Varios alumnos 2022-2023
     * @author MAFG y Varios alumnos 2023-2024
     * @author MAFG y Varios alumnos 2024-2025
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - Este mensaje se envia por TCP
     *      - (PENDIENTE MAFG 2023-10-04) Falta poner el mensaje con formato XML (creo que ya esta, no se si falta algo MAFG 2024-10-21)
     */
    protected void finalizaAgente() {
        Estado_Actual = Estado_del_ACC.FINALIZADO;

        // ///////////////////////////////////////////////////////
        // Notificamos al monitor que este agente ha finalizadO

        String ID_mensaje = dame_codigo_id_local_men();
        long momento_actual = System.currentTimeMillis();
        String momento_actual_str = String.valueOf(System.currentTimeMillis());
        String tiempo_vivido =  String.valueOf(System.currentTimeMillis() - Tiempo_de_nacimiento);
        String Puerto_Propio_str = String.valueOf(Puerto_Propio);
        long tiempoUnixEnMilisegundos = System.currentTimeMillis();
        String Puerto_Monitor_TCP_str = String.valueOf(Puerto_Monitor_TCP);
        String cuerpo_mens_fin_agente = "\n==> Desde Acc => finalizaAgente() 01. \n Esto es el MENSAJE FIN DE AGENTE  - que el agente con ID_propio : " + ID_propio +
                " - con ip : " + Ip_Propia +
                " - con Puerto_Propio : " + Puerto_Propio_str +
                " - con ID_mensaje : " + ID_mensaje +
                " - envia al monitor con Ip_Monitor : "+Ip_Monitor+
                " - con Puerto_Monitor : "+Puerto_Monitor_TCP_str+
                " - en T : " + momento_actual_str+
                " - con T de vida : " + Tiempo_de_vida +
                " - con T vivido : " + tiempo_vivido;

        Mensaje mensaje_fin_agente = new Mensaje(ID_mensaje,
                Ip_Propia,
                Puerto_Propio,
                ID_propio,
                tiempoUnixEnMilisegundos,
                Ip_Monitor,
                Puerto_Monitor_TCP,
                "monitor",
                0,
                "TCP",
                cuerpo_mens_fin_agente,
                null);

        // //////////////////////////////////////////
        // Generamos ahora el DOM del XML que sera el mensaje que enviaremos
        // generamos la cabecera, este DOM quedara localizado en "mensaje_fin_agente.DOM_cuerpo_del_mensaje"
        String este_comunc_id_fin = "fin_de_agente_"+ID_propio;
        String este_type_protocol_fin = "meMuero";  // es el protocolo de informe de finalizacion de agente
        int este_protocol_step_fin = 0;  // este protocolo solo tiene el paso 0
        mensaje_fin_agente.genera_Dom_base_mensaje(this, este_comunc_id_fin, este_type_protocol_fin, este_protocol_step_fin);
        // cumplimentamos el contenido del cuerpo del mensaje
        // Preparamos el elemento "body_info"
        Element e_body_info =mensaje_fin_agente.DOM_cuerpo_del_mensaje.createElement("body_info");
        String e_body_info_str_fin ="Este es el mensaje de FIN DE AGENTE con XNL del agente : " + this.ID_propio;
        e_body_info.setTextContent(e_body_info_str_fin);
        // Localizamos el elemento "body" y hacemos "body_info" su descendiente
        mensaje_fin_agente.DOM_cuerpo_del_mensaje.getElementsByTagName("body").item(0).appendChild(e_body_info);

        // /////////////////////////
        // Insertamos el mensaje
        pon_en_lita_enviar(mensaje_fin_agente);

        String texto_ag_finalizado_log = ">> Desde Acc => finalizaAgente() 02. NOTIFICACION LOCAL de FIN DE AGENTE - \n "+ cuerpo_mens_fin_agente;
        if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_ag_finalizado_log, 50);}

        // ///////////////////////////////////////////////////////
        // Nos vamos
        //  - Antes de cerrar los sockets, esperamos a que todos los mensajes esten enviados. Los
        //       recibidos, como estamos muertos no nos importan

        boolean espera_fin_envios = true;
        while (espera_fin_envios)
        {
            if ((num_elem_lita_enviar() <= 0) || ((momento_actual + tiempo_espera_fin_env) < System.currentTimeMillis())){espera_fin_envios = false;}
        }

        // Dejamos la casa como estaba
        cerrarSockets();

        String texto_ag_info_fin_log = ">>>>>>>>>> Finalizamos el agente con : \n - men pendientes de envio : "+ num_elem_lita_enviar() +
                            "\n - Total enviados : " + num_tot_men_env +
                            "\n - men en cola recibidos : "+ num_elem_lita_recibidos() +
                            "\n - Total recibidos : " + num_tot_men_rec +
                            "\n - num_ciclos_hilo_recibeTcp : " + num_ciclos_hilo_recibeTcp +
                            "\n - num_ciclos_hilo_recibeUdp : " + num_ciclos_hilo_recibeUdp +
                            "\n - num_ciclos_hilo_enviar : " + num_ciclos_hilo_enviar +
                            "\n - num_ciclos_hilo_ComportamientoBase : " + num_ciclos_hilo_ComportamientoBase +
                            "\n - num_ciclos_hilo_FuncionDeAgente : " + num_ciclos_hilo_FuncionDeAgente +
                            "\n - num_ciclos_hilo_FuncionMonitor : " + num_ciclos_hilo_FuncionMonitor +
                            "\n  >>>>>>   FIN DE AGENTE  ID_propio : "+ ID_propio+ "  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
        if(this.nivel_log > 50){this.Gestor_de_logs.anota_en_log(texto_ag_info_fin_log, 50);}

        // Cerramos la conexion a la BBDD

        try
        {
            if(conex_BBDD_monitor != null)
            {
                conex_BBDD_monitor.close();
            }
        } catch (SQLException e_conexion) {
            String texto_err_011_log = "    **> ERROR 011. Desde Acc => en finalizaAgente() Al cerrar la BBDD."+
                    "\n - this.usr_BBDD_monitor : " + e_conexion.getMessage();
            this.Gestor_de_logs.anota_en_log(texto_err_011_log, 99); // 99 para error
        }

        // detenemos los hilos de proceso asociados a este agente
        this.hilo_ComportamientoBase.interrupt();
        this.hilo_RecibeTcp.interrupt();
        this.hilo_RecibeUdp.interrupt();
        this.hilo_Enviar.interrupt();
        if(this.tipo_agente.equals("MONITOR"))
        {
            this.hilo_FuncionMonitor.interrupt();
        }
        if (this.tipo_agente.equals("CAMBIACROMOS"))
        {
            this.hilo_FuncionDeAgente.interrupt();
        }

        // cerramos el fichero de log
        this.Gestor_de_logs.cierra_fich_log();

        // Terminamos el proceso
        System.exit(0);     // Parar el agente

    } // Fin de - protected void notificaNacimiento() {

    /**
     * void cerrarSockets() : Cierra los sockets del agente para liberarlos
     * @author MAFG y Varios alumnos 2022-2023
     * @author MAFG y Varios alumnos 2023-2024
     * @author MAFG y Varios alumnos 2024-2025
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - Debe llamarse al terminar el agente
     */
    void cerrarSockets() {

        //
        // Cerramos el socket TCP
        try {
            recibeTcp.servidor_TCP.close();
        } catch (Exception e) {
            String texto_err_010_log = "    **> ERROR 010. Desde Acc => cerrarSockets() 01. Problemas al cerrar el socket TCP";
            this.Gestor_de_logs.anota_en_log(texto_err_010_log, 99); // 99 para error
        }
        // Cerramos el socket UDP
        try {
            recibeUdp.servidor_UDP.close();
        } catch (Exception e) {
            String texto_err_011_log = "    **> ERROR. Desde Acc => cerrarSockets() 02. Problemas al cerrar el socket UDP";
            this.Gestor_de_logs.anota_en_log(texto_err_011_log, 99); // 99 para error
        }
    } // Fin de - void cerrarSockets() {

    /**
     * public static long obtenerPID(); Para obtener el id del proceso en el que se ejecuta la clase
     * @author Miguel Angel Fernandez Graciani
     * @fechaDeCreacion: 2023-10-16
     * @fechaDeUltimaModificacion:
     * @version: V_01
     * @observaciones:
     *  - Me lo dice chatGPT (2023-10-18)
     */
    public static long obtenerPID() {
        String nombreGestion = ManagementFactory.getRuntimeMXBean().getName();
        // El nombre de gestión tiene el formato "pid@hostname"
        String[] partes = nombreGestion.split("@");

        if (partes.length > 0) {
            try {
                return Long.parseLong(partes[0]);
            } catch (NumberFormatException e) {
                // Manejar la excepción si no se puede convertir a un número
                e.printStackTrace();
            }
        }

        // Si no se pudo obtener el PID, devolver un valor predeterminado
        return -1;
    }

    /**
     *  pon o saca : Funciones para el manejo de datos compartidos. Introduce o saca mensajes o agentes localizados de la lista correspondiente
     * @author MAFG y Varios alumnos 2023-2024
     * @fechaDeCreacion: 2022-xx-xx
     * @fechaDeUltimaModificacion:
     *      - 2023-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *  - Se hace mediante "synchronized" para evitar problemas de concurrencia
     */
    // synchronized

    // Sincronizamos los accesos a las listas de mensagen recibidos y enviados, ya que estas son accesibles desde varios hilos
    // y el acceso concurrente de estos, puede (y dan) problemas
    // A este respecto, son recursos generales compartidos los siguientes :
    //      - contenedor_de_mensajes_a_enviar
    //      - contenedor_de_mensajes_recibidos
    //      - directorio_de_agentes
    //      - num_tot_men_env
    //      - num_tot_men_rec
    //      - num_tot_acc_loc
    //      - num_id_local_men
    protected synchronized void pon_en_lita_enviar(Mensaje este_mensaje) {contenedor_de_mensajes_a_enviar.add(este_mensaje); num_tot_men_env++; }
    protected synchronized void pon_en_lita_recibidos(Mensaje este_mensaje) {contenedor_de_mensajes_recibidos.add(este_mensaje); num_tot_men_rec++; }
    protected synchronized void pon_en_directorio_de_agentes(AccLocalizado este_accLocalizado) {directorio_de_agentes.add(este_accLocalizado); num_tot_acc_loc++; }

    protected synchronized int num_elem_lita_enviar() {int num_elem = contenedor_de_mensajes_a_enviar.size(); return num_elem;}
    protected synchronized int num_elem_lita_recibidos() {int num_elem = contenedor_de_mensajes_recibidos.size(); return num_elem;}
    protected synchronized int num_elem_directorio_de_agentes() {int num_elem = directorio_de_agentes.size(); return num_elem;}

    protected synchronized int dime_num_tot_men_env() {return num_tot_men_env;}
    protected synchronized int dime_num_tot_men_rec() {return num_tot_men_rec;}
    protected synchronized int dime_num_tot_acc_loc() {return num_tot_acc_loc;}

    protected synchronized String dame_codigo_id_local_men(){
            String codigo_id_local_men = ID_propio + "_men_" + num_id_local_men;
            num_id_local_men++;
            return codigo_id_local_men;
        }

    protected synchronized Mensaje saca_de_lita_enviar() {Mensaje este_mensaje = contenedor_de_mensajes_a_enviar.pop(); return este_mensaje; }
    protected synchronized Mensaje saca_de_lita_recibidos() {Mensaje este_mensaje = contenedor_de_mensajes_recibidos.pop(); return este_mensaje; }
    protected synchronized AccLocalizado saca_de_directorio_de_agentes() {AccLocalizado este_accLocalizado = directorio_de_agentes.pop(); return este_accLocalizado; }

    // Sincronizamos tambien los recursos de generacion y gestion de documentos XML, ya que al generarlos como elementos globales a todos los hilos
    // estos pueden (y dan) problemas de concurrencia
    // Son elementos generales a todos los hilos  :
    //          - dom_builder
    //          - validador_xml
    //          - xml_transformer
    protected synchronized Document construye_Dom() {Document Dom_generado = dom_builder.newDocument(); return Dom_generado; }
    protected synchronized Document construye_Dom_desde_xml(InputSource xml_in) throws IOException, SAXException {Document Dom_generado = dom_builder.parse(xml_in); return Dom_generado; }
    protected synchronized void valida_Xml(StreamSource xml_in) throws IOException, SAXException {validador_xml.validate(xml_in);}
    protected synchronized void valida_Dom(DOMSource dom_in) throws IOException, SAXException {validador_xml.validate(dom_in);}
    protected synchronized void transforma_Xml(DOMSource source, StreamResult result) throws TransformerException {xml_transformer.transform(source, result);}

} // Fin de - public class Acc {