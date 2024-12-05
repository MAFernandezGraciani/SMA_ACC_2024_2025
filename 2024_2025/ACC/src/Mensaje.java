import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * class Mensaje : Clase que almacena la información asociada a un mensaje
 * @author MAFG y Varios alumnos 2022-2023
 * @fechaDeCreacion: 2022-xx-xx
 * @fechaDeUltimaModificacion: 2023-10-04
 *      2023-2024 (MAFG) Ajustes para el curso
 *      - 2023-11-08 (MAFG) Añado el proceso de XML (se habilita concertir el cuero XML en DOM y viceversa)
 * @version: 2023-2024-01
 * @observaciones:
 */
public class Mensaje {

    protected String ID_mensaje; // IP del agente que envía el mensaje
    protected String IP_origen; // IP del agente que envía el mensaje
    protected int puerto_origen; // Puerto del agente que envía el mensaje
    protected String id_origen; // Identificador único del agente que envía el mensaje
    protected long tiempo_origen; // Momento en el que se genera el mensaje en origen, cuando se manda a la cola de envio (segun el reloj del sistema origen)
                                    // Quemos ordinarlos temporalmente en origen, segun cuando son construidos, no segun cuando son enviados

    protected String IP_destino; // IP del agente destino de este mensaje
    protected int puerto_destino; // Puerto del agente destino de este mensaje.
                                    // OJO, el "puerto_destino" es el puerto asociado al agente destinatarios (esto es su "Puerto_Propio")
                                    // al realizar el envio, la calse "Enviar" se encarga de direccionarlo hacia "Puerto_Propio_TCP" o "Puerto_Propio_UDP"
                                    // segun proceda atendiendo al valor de la propiedad "protocolo" de este objeto
    protected String id_destino; // Identificador único del agente destino de este mensaje
    protected long tiempo_destino; // Momento en el que se recibe el mensaje en destino, cuando llega al soket (segun el reloj del sistema origen)
                                // Quemos ordinarlos temporalmente en destino, segun cuando son recibidos, no segun cuando son analizados
    protected String protocolo;  // Se admiten protocolos TCP y UDP
    protected String cuerpo_del_mensaje;  // Contenido del mensaje a enviar. Es el contenido de este campo, lo que viajara al destino
    protected Document DOM_cuerpo_del_mensaje;  // DOM del XML a enviar. Del XML que viajara al destino

    /**
     *  public Mensaje() : Constructor de la clase. Basicamente asigna las variables
     * @author Miguel Angel Fernandez Graciani
     * @fechaDeCreacion: 2023-10-04
     * @fechaDeUltimaModificacion:
     *      - 2023-11-08 : para incluir el tratamiento de XML
     * @version: 2023-11-08
     * @param : Acc agente; se le envia el agente, para poder acceder a sus recursos
     * @observaciones:
     *      - La gestion de mensajes esta planteada para que el resto de funciones procesen el cuerpo del mensaje
     *          como XML en estructura de objeto DOM. Laos metodos que envian pasan el DOM a string antes de enviarlo
     *          y los de recivir, convierten el string de XML en un objeto DOM al recibirlo.
     *          - Estos métodos tambein validan el XML recibido contra el esquema del Acc
     */
    public Mensaje(String ID_mensaje,
                    String IP_origen,
                    int puerto_origen,
                    String id_origen,
                    long tiempo_origen,
                    String IP_destino,
                    int puerto_destino,
                    String id_destino,
                    long tiempo_destino,
                    String protocolo,
                    String cuerpo_del_mensaje,
                    Document DOM_cuerpo_del_mensaje)
    {
        this.ID_mensaje = ID_mensaje;
        this.IP_origen = IP_origen;
        this.puerto_origen = puerto_origen;
        this.id_origen = id_origen;
        this.tiempo_origen = tiempo_origen;
        this.IP_destino = IP_destino;
        this.puerto_destino = puerto_destino;
        this.id_destino = id_destino;
        this.tiempo_destino = tiempo_destino;
        this.protocolo = protocolo;
        this.cuerpo_del_mensaje = cuerpo_del_mensaje;
        this.DOM_cuerpo_del_mensaje = DOM_cuerpo_del_mensaje;

    } // Fin de -  public Mensaje(String ID_mensaje,

    /**
     *  public void genera_Dom_base_mensaje() : Genera el objeto DOM, con los datos basicos del mensaje en la cabecera y lo almacena en "DOM_cuerpo_del_mensaje"
     * @author : Miguel Angel Fernandez Graciani
     * @fechaDeCreacion : 2023-11-08
     * @fechaDeUltimaModificacion :
     * @version : 2024-11-08
     * @param : Acc agente; se le envia el agente, para poder acceder a sus recursos
     * @observaciones :
     *      - OJOOO este metodo depende fuertemente del esquema asociado al protocolo de comunicaciones, ya que debe ajustarse a este
     *          SI SE CAMBIA EL ESQUEMA DEL SISTEMA DE COMUNICACIONES, ESTE METODO DEBE TAMBIEN MODIFICARSE
     *      - Esta version esta configurada para trabajar con documentos XML que se validen mediante el esquema "esquema_AG_basico_02.xsd" (SMA 2024-2025)
     *      - La estructura que se cumplimenta es :
     *
     *                  - Message
     *                      - comunc_id
     *                      - msg_id
     *                      - header
     *                          - type_protocol
     *                          - protocol_step
     *                          - comunication_protocol
     *                          - origin
     *                              - origin_id
     *                              - origin_ip
     *                              - origin_port_UDP
     *                              - origin_port_TCP
     *                              - origin_time
     *                          - destination
     *                              - destination_id
     *                              - destination_ip
     *                              - destination_port_UDP
     *                              - destination_port_TCP
     *                              - destination_time
     *                      - body (Deben cumplimentarse segun el mensaje concreto en el proceso que llama a este)
     *                      - common_content (Deben cumplimentarse segun el mensaje concreto en el proceso que llama a este)
     */
    public void genera_Dom_base_mensaje(Acc agente, String comunc_id, String type_protocol, int protocol_step) {
        Document obj_Dom_base = null;
        try {
            // Creamos un nuevo documento DOM para contener el XML
            obj_Dom_base = agente.construye_Dom();

            // Creamos el elemento raíz
            // Primero se crea el elemento dentro del documento
            Element rootElement = obj_Dom_base.createElement("Message");
            // despues se coloca en su sitio en el arbol, como raiz de este
            obj_Dom_base.appendChild(rootElement);

            // Creamos otros elementos hijos del elemento raiz
            Element e_comunc_id = obj_Dom_base.createElement("comunc_id"); // Creamos el elemento
            e_comunc_id.setTextContent(comunc_id); // Le asignamos su elemento texto con el contenido poertinente
            rootElement.appendChild(e_comunc_id); //Lo ahijamos a su progenitor

            Element e_msg_id = obj_Dom_base.createElement("msg_id");
            e_msg_id.setTextContent(this.ID_mensaje);
            rootElement.appendChild(e_msg_id);

            Element e_header = obj_Dom_base.createElement("header");
            rootElement.appendChild(e_header);
                // Los hijos de header
            Element e_type_protocol = obj_Dom_base.createElement("type_protocol");
            e_type_protocol.setTextContent(String.valueOf(type_protocol));
            e_header.appendChild(e_type_protocol);
            Element e_protocol_step = obj_Dom_base.createElement("protocol_step");
            e_protocol_step.setTextContent(String.valueOf(protocol_step));
            e_header.appendChild(e_protocol_step);
            Element e_comunication_protocol = obj_Dom_base.createElement("comunication_protocol");
            e_comunication_protocol.setTextContent(this.protocolo);
            e_header.appendChild(e_comunication_protocol);

            Element e_origin = obj_Dom_base.createElement("origin");
            e_header.appendChild(e_origin);
                    // Los hijos de origin
            Element e_origin_id = obj_Dom_base.createElement("origin_id");
            e_origin_id.setTextContent(this.id_origen);
            e_origin.appendChild(e_origin_id);
            Element e_origin_ip = obj_Dom_base.createElement("origin_ip");
            e_origin_ip.setTextContent(this.IP_origen);
            e_origin.appendChild(e_origin_ip);
            Element e_origin_port_UDP = obj_Dom_base.createElement("origin_port_UDP");
            int puerto_origen_UDP = this.puerto_origen + 1;
            e_origin_port_UDP.setTextContent(String.valueOf(puerto_origen_UDP));
            e_origin.appendChild(e_origin_port_UDP);
            Element e_origin_port_TCP = obj_Dom_base.createElement("origin_port_TCP");
            e_origin_port_TCP.setTextContent(String.valueOf(this.puerto_origen));
            e_origin.appendChild(e_origin_port_TCP);
            Element e_origin_time = obj_Dom_base.createElement("origin_time");
            String str_origin_time = String.valueOf(System.currentTimeMillis());
            e_origin_time.setTextContent(str_origin_time);
            e_origin.appendChild(e_origin_time);
                    // FIn de - Los hijos de origin

            Element e_destination = obj_Dom_base.createElement("destination");
            e_header.appendChild(e_destination);
                    // Los hijos de destination
            Element e_destination_id = obj_Dom_base.createElement("destination_id");
            e_destination_id.setTextContent(this.id_destino);
            e_destination.appendChild(e_destination_id);
            Element e_destination_ip = obj_Dom_base.createElement("destination_ip");
            e_destination_ip.setTextContent(this.IP_destino);
            e_destination.appendChild(e_destination_ip);
            Element e_destination_port_UDP = obj_Dom_base.createElement("destination_port_UDP");
            int puerto_destination_UDP = this.puerto_destino + 1;
            e_destination_port_UDP.setTextContent(String.valueOf(puerto_destination_UDP));
            e_destination.appendChild(e_destination_port_UDP);
            Element e_destination_port_TCP = obj_Dom_base.createElement("destination_port_TCP");
            e_destination_port_TCP.setTextContent(String.valueOf(this.puerto_destino));
            e_destination.appendChild(e_destination_port_TCP);
            Element e_destination_time = obj_Dom_base.createElement("destination_time");
            String str_destination_time = String.valueOf(System.currentTimeMillis());
            e_destination_time.setTextContent(str_destination_time);
            e_destination.appendChild(e_destination_time);
                    // FIn de - Los hijos de destination
                // FIn de - Los hijos de header

            Element e_body = obj_Dom_base.createElement("body");
            rootElement.appendChild(e_body);
                // Los hijos de body. Deben cumplimentarse segun el mensaje concreto en el proceso que llama a este

            Element e_common_content = obj_Dom_base.createElement("common_content");
            rootElement.appendChild(e_common_content);
                // Los hijos de common_content. Deben cumplimentarse segun el mensaje concreto en el proceso que llama a este

        } catch (Exception e) {
            String txt_err_DOM_001_log = "    **> ERROR 001. Desde Mensaje => genera_Dom_base_mensaje(). fallo al intentar generar el DOM del mensaje con this.ID_mensaje : "+ this.ID_mensaje +
                    " - comunc_id : "+ comunc_id +
                    " - type_protocol : "+ type_protocol+
                    " - protocol_step : "+ protocol_step +
                    "\n * Con e.getMessage : "+ e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_DOM_001_log, 99); // 99 para error

        }
        DOM_cuerpo_del_mensaje = obj_Dom_base; // Almacenamos el objeto DOM que hemos generado
    } // Fin de - public Document Xml_a_Dom(String xml_string)

    /**
     *  public Document Xml_a_Dom() : Toma el XML que esta almacenado en "cuerpo_del_mensaje" genera el correspondiente
     *              objeto DOM y lo almacena en "DOM_cuerpo_del_mensaje"
     * @author Miguel Angel Fernandez Graciani
     * @fechaDeCreacion: 2023-11-08
     * @fechaDeUltimaModificacion:
     * @version: 2023-11-08
     * @param : Acc agente; se le envia el agente, para poder acceder a sus recursos
     * @observaciones:
     */
    public void Xml_a_Dom(Acc agente) {
        Document Dom_del_xml = null;
        try {
            // Creamos un InputSource a partir del String XML
            InputSource inputSource = new InputSource(new StringReader(cuerpo_del_mensaje));
            // Parseamos el InputSource para obtener el Document DOM
            // Ojo, al construirlo lo validamos contra el esquema (ver generaConfiguracionInicial())
            DOM_cuerpo_del_mensaje =  agente.construye_Dom_desde_xml(inputSource); // este es el DOM asociado al XML
        } catch (Exception e) {
            String txt_err_DOM_002_log = "      ** ERROR. Desde Mensaje => Xml_a_Dom() - 01 \n con XML a convertir : " + cuerpo_del_mensaje +
                    "\n - Con  ID_mensaje : " + ID_mensaje +
                    "\n - Con  IP_origen : " + IP_origen +
                    "\n - Con  puerto_origen : " + puerto_origen +
                    "\n - Con  id_origen : " + id_origen +
                    "\n - Con  IP_destino : " + IP_destino +
                    "\n - Con  puerto_destino : " + puerto_destino +
                    "\n - Con  id_destino : " + id_destino +
                    "\n - Con  protocolo : " + protocolo +
                    "\n * Con e.getMessage : "+ e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_DOM_002_log, 99); // 99 para error
        }

    } // Fin de - public Document Xml_a_Dom(String xml_string)

    /**
     * public String Dom_a_Xml() : Toma el DOM que esta almacenado en "DOM_cuerpo_del_mensaje" genera el correspondiente string xml
     *                  y lo almacena en "cuerpo_del_mensaje"
     * @author Miguel Angel Fernandez Graciani
     * @fechaDeCreacion: 2023-11-08
     * @fechaDeUltimaModificacion:
     * @version: 2023-11-08
     * @param : Acc agente; se le envia el agente, para poder acceder a sus recursos
     * @observaciones:
     *      - El DOM se valida contra el esquema correspondiente antes de convertirlo a string XML
     */
    public void Dom_a_Xml(Acc agente) {
        try {
            // Creamos una fuente DOM a partir del objeto DOM
            DOMSource source = new DOMSource(DOM_cuerpo_del_mensaje);

            // Primero validamos el DOM contra el esquema correspondiente
            agente.valida_Dom(source);

            // Creamos un flujo (stream) de salida para el String XML
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);

            // Transformamos la fuente DOM en un String XML
            agente.transforma_Xml(source, result);

            // Obtener el String XML
            cuerpo_del_mensaje = stringWriter.toString();

        } catch (TransformerException e) {
            String txt_err_DOM_003_log = "\n ==> ERROR (TransformerException).  Desde Mensaje => Dom_a_Xml() - 01 \n  con XML a convertir : " + cuerpo_del_mensaje +
                    "\n\n - Con  ID_mensaje : " + ID_mensaje +
                    "\n - Con  IP_origen : " + IP_origen +
                    "\n - Con  puerto_origen : " + puerto_origen +
                    "\n - Con  id_origen : " + id_origen +
                    "\n - Con  IP_destino : " + IP_destino +
                    "\n - Con  puerto_destino : " + puerto_destino +
                    "\n - Con  id_destino : " + id_destino +
                    "\n - Con  protocolo : " + protocolo +
                    "\n * Con e.getMessage : "+ e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_DOM_003_log, 99); // 99 para error
        }  catch (IOException e) {
            String txt_err_DOM_004_log = "\n ==> ERROR validando (IOException).  Desde Mensaje => Dom_a_Xml() - 02 \n  con XML a convertir : " + cuerpo_del_mensaje +
                    "\n - Con  ID_mensaje : " + ID_mensaje +
                    "\n - Con  IP_origen : " + IP_origen +
                    "\n - Con  puerto_origen : " + puerto_origen +
                    "\n - Con  id_origen : " + id_origen +
                    "\n - Con  IP_destino : " + IP_destino +
                    "\n - Con  puerto_destino : " + puerto_destino +
                    "\n - Con  id_destino : " + id_destino +
                    "\n - Con  protocolo : " + protocolo +
                    "\n * Con e.getMessage : "+ e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_DOM_004_log, 99); // 99 para error
            throw new RuntimeException(e);
         } catch (SAXException e) {
            String txt_err_DOM_005_log = "\n ==> ERROR validando (SAXException).  Desde Mensaje => Dom_a_Xml() - 03 \n  con XML a convertir : " + cuerpo_del_mensaje +
                    "\n - Con  ID_mensaje : " + ID_mensaje +
                    "\n - Con  IP_origen : " + IP_origen +
                    "\n - Con  puerto_origen : " + puerto_origen +
                    "\n - Con  id_origen : " + id_origen +
                    "\n - Con  IP_destino : " + IP_destino +
                    "\n - Con  puerto_destino : " + puerto_destino +
                    "\n - Con  id_destino : " + id_destino +
                    "\n - Con  protocolo : " + protocolo +
                    "\n * Con e.getMessage : "+ e.getMessage();
            agente.Gestor_de_logs.anota_en_log(txt_err_DOM_005_log, 99); // 99 para error
            throw new RuntimeException(e);
        }
    } // Fin de - public static void Dom_a_Xml(Document xml_Dom) {

    //GETTERS
    public String getCuerpo_del_mensaje() {
        return cuerpo_del_mensaje;
    }
    public String getProtocolo() {
        return protocolo;
    }

} // Fin de - public class Mensaje {
