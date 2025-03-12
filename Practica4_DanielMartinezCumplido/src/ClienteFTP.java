import java.io.*;
import java.util.Scanner;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class ClienteFTP {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String servidorFTP = "localhost";
        String usuario;
        String contrasena;
        boolean logueado = false;

        FTPClient ftpClient = new FTPClient();

        try {
            // Establecer conexión con el servidor FTP
            ftpClient.connect(servidorFTP, 12345);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                System.out.println("No se pudo conectar al servidor FTP.");
            }

            // Preguntar cómo entrar
            System.out.print("""
                    ¿Cómo desea conectarse?
                    1. Con usuario y contraseña
                    2. Como usuario anónimo"""
            );

            int opcion = scanner.nextInt();

            // CON USUARIO Y CONTRASEÑA
            if (opcion == 1) {
                System.out.print("Introduce el nombre de usuario: ");
                usuario = scanner.nextLine();
                System.out.print("Introduce la contraseña: ");
                contrasena = scanner.nextLine();

                // Intentamos realizar el login con el usuario y la contraseña proporcionados
                if (!ftpClient.login(usuario, contrasena)) {
                    System.out.println("Error al iniciar sesión con las credenciales proporcionadas.");
                    ftpClient.disconnect();
                }
                logueado = true;
                System.out.println("Login exitoso con el usuario: " + usuario);

            } else {
                // COMO ANÓNIMO
                if (!ftpClient.login("anonymous", "")) {
                    System.out.println("Error al iniciar sesión anónimamente.");
                    ftpClient.disconnect();
                }
                System.out.println("Login exitoso como anónimo.");
            }

            // Menú principal para interactuar con el cliente FTP
            while (true) {
                System.out.print("""
                        1. Ver lista de archivos
                        2. Descargar archivo
                        3. Subir archivo
                        4. Salir"""
                );

                opcion = scanner.nextInt();

                switch (opcion) {
                    case 1:
                        // Ver lista de archivos en el servidor
                        String[] archivos = ftpClient.listNames();
                        if (archivos != null) {
                            System.out.println("Archivos disponibles:");
                            for (String archivo : archivos) {
                                System.out.println(archivo);
                            }
                        } else {
                            System.out.println("No se pudo obtener la lista de archivos.");
                        }
                        break;

                    case 2:
                        // Descargar archivo
                        System.out.print("Introduce el nombre del archivo a descargar: ");
                        String archivoDescargar = scanner.nextLine();
                        System.out.print("Introduce la ruta de destino para guardar el archivo: ");
                        String destinoDescarga = scanner.nextLine();
                        File archivoLocal = new File(destinoDescarga);
                        try (FileOutputStream fos = new FileOutputStream(archivoLocal)) {
                            if (ftpClient.retrieveFile(archivoDescargar, fos)) {
                                System.out.println("Archivo descargado exitosamente");
                            } else {
                                System.out.println("Error al descargar el archivo");
                            }
                        } catch (IOException e) {
                            System.out.println("Error al descargar el archivo");
                        }
                        break;

                    case 3:
                        // Subir archivo (no anónimo)
                        if (logueado) {
                            System.out.print("Introduce el nombre del archivo a subir: ");
                            String archivoSubir = scanner.nextLine();
                            System.out.print("Introduce la ruta del archivo que deseas subir: ");
                            String origenSubida = scanner.nextLine();
                            File archivoOrigen = new File(origenSubida);
                            try (FileInputStream fis = new FileInputStream(archivoOrigen)) {
                                if (ftpClient.storeFile(archivoSubir, fis)) {
                                    System.out.println("Archivo subido exitosamente");
                                } else {
                                    System.out.println("Error al subir el archivo");
                                }
                            } catch (IOException e) {
                                System.out.println("Error al subir el archivo");
                            }
                        } else {
                            System.out.println("No tienes permisos para subir archivos con acceso anónimo.");
                        }
                        break;

                    case 4:
                        // Salir y cerrar la conexión FTP
                        ftpClient.logout();
                        System.out.println("Usuario " + "deslogueado");
                        ftpClient.disconnect();
                        System.out.println("Usuario desconectado");
                        return;

                    default:
                        System.out.println("Opción no válida, inténtalo de nuevo.");
                }

                // Se desconecta y se cierra la sesión
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    System.out.println("Usuario " + "deslogueado");
                    ftpClient.disconnect();
                    System.out.println("Usuario desconectado");
                }
            }
        } catch (IOException e) {
            System.out.println("Problema con la conexión con el servidor");
        }
    }
}