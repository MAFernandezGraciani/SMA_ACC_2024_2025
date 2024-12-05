import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * class GestionLogs : Esta clase contiene los recursos necesarios para gestionar los logs del proceso
 * @author MAFG 2024-2025
 * @fechaDeCreacion: 2024-10-10
 * @fechaDeUltimaModificacion: 2024-10-10
 * @version: 2024-2025-01
 * @param :
 *      - Acc este_agente : El objeto agente cuyo proceso estamos trazando
 * @observaciones:
 *      - Cuando arrancamos los agentes desde consola, podemos ver en esta las anotaciones que se realizan
 *      desde "System.out.println()".pero cuando los agentes los arranca el código es mas dificil trazar la ejecución
 *      Esta clase permite tanto imprimir mensages en la consola mediante "System.out.println()", como realizar anotaciones
 *      en un fichero de log que tendrá en el nonbre Log_"nombre_del_agente".txt
 *
 */
public class GestionLogs  {

    // Para pruebas
    protected File archivo_log; // Es el archivo de log de este agente (el archivo quedara localizado en
                                // el directorio donde se arranco el proceso del agente)
    protected String nombreArchivo_log; // El nombre del archivo de log de este agente
    protected FileWriter escritor_fich_log;  // Para excribir en el fichero

    protected Acc este_agente; // Para poder acceder a los datos generales de este agente

    /**
     * public Acc : Contructor de la calse GestionLogs
     * @author MAFG 2024-2025
     * @fechaDeCreacion: 2024-10-10
     * @fechaDeUltimaModificacion:
     * @version: 2024-2025-01
     * @param  "Acc" agente : Este agente, para poder acceder a sus datos
     * @observaciones:
     *      - Si el fichero de log no existe, lo crea
     *      - si el fichero de log existe, dependiendo del valor de sustituir, lo borra y vuelve a escribir, o
     *      continua escribiendo a partir del contenido existente
     *      PASOS DE EJECUCION :
     *          - 1.) Si hay que sustituir el fichero anterior, miramos si existe, y si es asi lo borrmos
     */
    GestionLogs(Acc agente)
    {
        this.este_agente = agente;
        String texto_inicio = " - iniciamos el log del agente : " + este_agente.ID_propio;

        try {

            nombreArchivo_log = "Log_" + este_agente.ID_propio + ".txt";

            // Crear objeto File para representar el archivo
            archivo_log = new File(nombreArchivo_log);

            // Verificar si el archivo existe
            if (archivo_log.exists()) {
                if(este_agente.sustituir_fich_log)
                {
                    // Intentar borrar el archivo
                    if (archivo_log.delete())
                    {
                        System.out.println("El archivo de log :" + nombreArchivo_log + " - ha sido borrado con éxito.");
                        texto_inicio = texto_inicio + "\n - El archivo de log :" + nombreArchivo_log + " - ha sido borrado con éxito.";
                        // una vez borrado, generamos otro archivo con el mismo nombre
                        if (archivo_log.createNewFile())
                        {
                            System.out.println("Archivo de log creado sustituyendo: " + archivo_log.getName());
                            texto_inicio = texto_inicio + "\n - Archivo de log creado sustituyendo: " + archivo_log.getName();
                        }
                        else
                        {
                            System.out.println("Error al intentar generar el archivo de log : " + nombreArchivo_log);
                            texto_inicio = texto_inicio + "\n - Error al intentar generar el archivo de log : " + nombreArchivo_log;
                        }
                    } // Fin de - if (archivo_log.delete())
                    else
                    {
                        System.out.println("Error al intentar borrar el archivo de log : " + nombreArchivo_log);
                        texto_inicio = texto_inicio + "\n - Error al intentar borrar el archivo de log : " + nombreArchivo_log;
                    }
                } // Fin de - if(este_agente.sustituir_fich_log)
                else
                {
                    // Si el archivo ya existe, deberemos escribir en el, a continuación de la última anotacion realizada
                    System.out.println("El archivo de log : " + nombreArchivo_log + " - va a seguir siendo completado con nuevas anotaciones \n");
                    texto_inicio = texto_inicio + "\n - El archivo de log : " + nombreArchivo_log + " - va a seguir siendo completado con nuevas anotaciones";
                }

            }
            else
            {
                // Si el archivo no existia lo generamos
                if (archivo_log.createNewFile()) {
                    System.out.println("Generamos un nuevo fichero de log que no existia: " + archivo_log.getName());
                    texto_inicio = texto_inicio + "\n - Generamos un nuevo fichero de log que no existia: " + archivo_log.getName();
                } else {
                    System.out.println("Error al intentar generar el archivo de log que no existia : " + nombreArchivo_log);
                    texto_inicio = texto_inicio + "\n -Error al intentar generar el archivo de log que no existia : " + nombreArchivo_log;
                }
            }

            // Ya tenemos listo el fichero de log. Crear FileWriter para escribir en el archivo
            this.escritor_fich_log = new FileWriter(archivo_log);

            // Escribir contenido en el archivo
//            this.escritor_fich_log.write("Este es el mensage de inicio del fichero de log : "+ nombreArchivo_log +"\n");
            texto_inicio = texto_inicio + " - Este es el mensage de inicio del fichero de log : "+ nombreArchivo_log;

        } catch (IOException e) {
            System.out.println("Ocurrió un error al crear o escribir en el archivo.");
            e.printStackTrace();

            texto_inicio = texto_inicio + "\n - Ocurrió un error al crear o escribir en el archivo.";
            texto_inicio = texto_inicio + "\n - Con e.getMessage() : " + e.getMessage() +
                                            "\n - Con e.getLocalizedMessage() : " + e.getLocalizedMessage() ;
        }
        if(este_agente.nivel_log > 50){anota_en_log(texto_inicio, 50);}
    } // Fin de - GestionLogs(Acc este_agente) {

    /**
     * void anota_en_log(String texto_a_log, int este_nivel_de_log) : anota en log, segun proceda
     * @author MAFG 2024-2025
     * @fechaDeCreacion: 2024-10-10
     * @fechaDeUltimaModificacion: 2024-10-04
     * @version: 2024-2025-01
     * @param  "String" texto_a_log : Es el texto que se debe loguear
     * @param  "int" este_nivel_de_log : define el nivel de interes para ser logueado
     *                      - Si this.este_agente.nivel_log < este_nivel_de_log => NO LOGUEAMOS
     *                      - En otro caso SI LOGUEAMOS
     * @observaciones:
     *      - Este método debe llamarse justo antes de finalizar el proceso del agente
     */
    public void anota_en_log(String texto_recibido, int este_nivel_de_log)
    {
        String texto_a_log = "";
        this.este_agente.ordinal_log = this.este_agente.ordinal_log + 1;

        // si el nivel de log que nos envian es mayor que el general, NO LOGUEAMOS
        if(this.este_agente.nivel_log < este_nivel_de_log)
        {
            return;
        }
        // si el nivel de log que nos envian es menor o igual que el general, SI LOGUEAMOS
        long unixTimeMillis = Instant.now().toEpochMilli();
        long unixTime = Instant.now().getEpochSecond();
        // Convertir a LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault());
        // Formatear la fecha y hora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDate = dateTime.format(formatter);

        texto_a_log = "\n ===> agente que anota : " + this.este_agente.ID_propio +
                " - anotacion : "+ this.este_agente.ordinal_log +
                " - en la ip : "+ this.este_agente.Ip_Propia +
                " - en el puerto : "+ this.este_agente.Puerto_Propio +
                " - hora : " + formattedDate + " - Hora Unix en milisegundos: " + unixTimeMillis;
        texto_a_log = texto_a_log + " \n " + texto_recibido;
        texto_a_log = texto_a_log + "- \n<<====  fim de anotacion : " + this.este_agente.ordinal_log + " - de agente  : " + this.este_agente.ID_propio;

        // Si el agente esta configurado para loguear en consola, LOGUEAMOS POR CONSOLA
        if(this.este_agente.anotar_en_consola_log)
        {
            System.out.println(texto_a_log);
        }

        // Si el agente esta configurado para loguear en fichero, LOGUEAMOS EN FICHERO
        if(this.este_agente.anotar_en_fich_log)
        {
           try
           {
                this.escritor_fich_log.write(texto_a_log + " - \n.");
           }
           catch (IOException e) {
                System.out.println("Ocurrió un error al intentar hacer una anotacion en el fichero de log. EL stream del fichero ya se ha cerrado");
           }
        }

    } // Fin de - void anota_en_log(String texto_a_log, int este_nivel_de_log)

    /**
     * void cierra_fich_log() : Cierra el fichero de log asociado a este agente
     * @author MAFG 2024-2025
     * @fechaDeCreacion: 2024-10-10
     * @fechaDeUltimaModificacion: 2024-10-04
     * @version: 2024-2025-01
     * @observaciones:
     *      - Este método debe llamarse justo antes de finalizar el proceso del agente
     */
    void cierra_fich_log()
    {
        // Cerrar el escritor para que se guarden los cambios
        try
        {
            escritor_fich_log.write("Este es el mensage de FINALIZACION del fichero de log : "+ nombreArchivo_log +"\n");
            this.escritor_fich_log.close();
        }
        catch (IOException e)
        {
            System.out.println("Ocurrió un error al crear o escribir en el fichero : " + nombreArchivo_log + " - \n.");
            e.printStackTrace();
        }
        System.out.println("Se ha cerrado, con éxito, el fichero : "+ nombreArchivo_log + " - \n");

    } // Fin de -  void cierra_fich_log() {

} // FIn de - public class FuncionDeAgente implements Runnable {
