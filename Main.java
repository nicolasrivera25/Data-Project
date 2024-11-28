import java.util.Scanner;

/**
 * Clase principal que maneja la interacción con el usuario y controla
 * el flujo del programa utilizando el objeto Estadio.
 */
public class Main {
    public static void main(String[] args) {
        // Crear una instancia de la clase Estadio.
        Estadio estadio = new Estadio();

        // Llamar al menú principal para interactuar con el usuario.
        mainMenu(estadio);
    }

    /**
     * Menú principal del programa, que permite al usuario interactuar
     * con el sistema de reservas mediante opciones claras.
     *
     * @param estadio Objeto de la clase Estadio que gestiona las reservas.
     */
    public static void mainMenu(Estadio estadio) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false; // Controla la salida del programa.

        while (!exit) {
            System.out.println("\n--- Menú de Reservación de Estadio ---");
            System.out.println("1. Reservar asiento");
            System.out.println("2. Cancelar reserva");
            System.out.println("3. Ver secciones y asientos disponibles");
            System.out.println("4. Ver lista de espera");
            System.out.println("5. Salir");

            int option = -1;

            // Manejo de errores para validar que la opción ingresada sea un número válido.
            while (true) {
                try {
                    System.out.print("Seleccione una opción: ");
                    option = Integer.parseInt(scanner.nextLine());
                    if (option >= 1 && option <= 5) {
                        break; // Salir del bucle si la opción es válida.
                    } else {
                        System.out.println("Por favor, seleccione un número entre 1 y 5.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Por favor, ingrese un número.");
                }
            }

            // Procesar la opción seleccionada por el usuario.
            switch (option) {
                case 1:
                    handleReservation(estadio, scanner);
                    break;
                case 2:
                    handleCancellation(estadio, scanner);
                    break;
                case 3:
                    estadio.showAvailableSections();
                    break;
                case 4:
                    estadio.showWaitlist(scanner);
                    break;
                case 5:
                    exit = true;
                    System.out.println("Saliendo del sistema. ¡Gracias por usar nuestro servicio!");
                    break;
                default:
                    System.out.println("Opción no válida. Intente nuevamente.");
            }
        }

        scanner.close(); // Cerrar el Scanner al salir del programa.
    }

    /**
     * Método que maneja la lógica para reservar un asiento.
     * Solicita los datos del cliente y la sección deseada.
     *
     * @param estadio Objeto de la clase Estadio que gestiona las reservas.
     * @param scanner Scanner para leer la entrada del usuario.
     */
    private static void handleReservation(Estadio estadio, Scanner scanner) {
        // Solicitar los datos del cliente.
        System.out.print("Ingrese el nombre del cliente: ");
        String name = scanner.nextLine();
        System.out.print("Ingrese el email del cliente: ");
        String email = scanner.nextLine();
        System.out.print("Ingrese el número de teléfono del cliente: ");
        String phone = scanner.nextLine();

        // Mostrar las secciones disponibles para reservar.
        System.out.println("Seleccione la sección para reservar:");
        System.out.println("1. Field Level ($300)");
        System.out.println("2. Main Level ($120)");
        System.out.println("3. Grandstand Level ($45)");

        int sectionChoice = -1;

        // Validar la entrada del usuario para la selección de la sección.
        while (true) {
            try {
                System.out.print("Ingrese el número de la sección: ");
                sectionChoice = Integer.parseInt(scanner.nextLine());
                if (sectionChoice >= 1 && sectionChoice <= 3) {
                    break; // Entrada válida
                } else {
                    System.out.println("Por favor, seleccione un número entre 1 y 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
            }
        }

        // Mapear la elección del usuario al nombre de la sección.
        String section = switch (sectionChoice) {
            case 1 -> "Field Level";
            case 2 -> "Main Level";
            case 3 -> "Grandstand Level";
            default -> null; // Esto no debería ocurrir debido a la validación previa.
        };

        // Crear un objeto `Cliente` con los datos ingresados.
        Cliente client = new Cliente(name, email, phone);

        // Intentar reservar un asiento para el cliente en la sección seleccionada.
        estadio.reserveSeat(client, section, scanner);
    }

    /**
     * Método que maneja la lógica para cancelar una reserva existente.
     * Solicita los datos del cliente para identificar la reserva.
     *
     * @param estadio Objeto de la clase Estadio que gestiona las reservas.
     * @param scanner Scanner para leer la entrada del usuario.
     */
    private static void handleCancellation(Estadio estadio, Scanner scanner) {
        // Solicitar los datos del cliente para buscar la reserva a cancelar.
        System.out.print("Ingrese el nombre del cliente para cancelar su reserva: ");
        String name = scanner.nextLine();
        System.out.print("Ingrese el email del cliente: ");
        String email = scanner.nextLine();
        System.out.print("Ingrese el número de teléfono del cliente: ");
        String phone = scanner.nextLine();

        // Crear un objeto `Cliente` con los datos ingresados.
        Cliente client = new Cliente(name, email, phone);

        // Intentar cancelar la reserva del cliente.
        estadio.cancelReservation(client);
    }
}

