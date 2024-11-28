import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * La clase `Estadio` gestiona las operaciones relacionadas con la reservación de asientos,
 * listas de espera, cancelaciones y la funcionalidad de deshacer.
 * 
 * Estructuras de datos utilizadas:
 * 1. **Set** (`HashSet`): Para almacenar los asientos disponibles. Un `HashSet` asegura que no haya duplicados y ofrece búsquedas rápidas.
 * 2. **LinkedList**: Para registrar el historial de transacciones (reservas y cancelaciones) en orden de ocurrencia.
 * 3. **HashMap**: Para parear clientes con los asientos que han reservado, permitiendo búsquedas rápidas.
 * 4. **Stack**: Para implementar la funcionalidad de deshacer, ya que sigue una estructura LIFO (Last In, First Out).
 * 5. **Queue** (`LinkedList` como implementación): Para manejar las listas de espera, asegurando un acceso FIFO (First In, First Out).
 */
public class Estadio {
    private Set<Asiento> availableSeats; // Set para almacenar los asientos disponibles.
    private LinkedList<String> reservationHistory; // LinkedList para almacenar el historial de transacciones.
    private HashMap<Cliente, Asiento> reservations; // HashMap para parear clientes con asientos reservados.
    private Stack<String> undoStack; // Stack para implementar la funcionalidad de deshacer.
    private Map<String, Queue<Cliente>> waitlistBySection; // Map para manejar listas de espera por sección.

    /**
     * Constructor que inicializa todas las estructuras de datos y los asientos del estadio.
     */
    public Estadio() {
        availableSeats = new HashSet<>();
        reservationHistory = new LinkedList<>();
        reservations = new HashMap<>();
        undoStack = new Stack<>();
        waitlistBySection = new HashMap<>();

        // Inicializar listas de espera para cada sección.
        waitlistBySection.put("Field Level", new LinkedList<>());
        waitlistBySection.put("Main Level", new LinkedList<>());
        waitlistBySection.put("Grandstand Level", new LinkedList<>());

        // Llenar el estadio con asientos disponibles.
        initializeSeats();
    }

    /**
     * Inicializa los asientos disponibles en cada sección del estadio.
     * Se utiliza un `HashSet` porque:
     * - Permite almacenar asientos únicos sin duplicados.
     * - Las operaciones de búsqueda y eliminación son rápidas (O(1)).
     */
    private void initializeSeats() {
        for (int i = 1; i <= 1; i++) {
            availableSeats.add(new Asiento("Field Level", 1, i));
        }
        for (int i = 1; i <= 1000; i++) {
            availableSeats.add(new Asiento("Main Level", 2, i));
        }
        for (int i = 1; i <= 2000; i++) {
            availableSeats.add(new Asiento("Grandstand Level", 3, i));
        }
    }

    /**
     * Muestra las secciones disponibles y la cantidad de asientos libres en cada una.
     * Utiliza un `Stream` para contar los asientos disponibles por sección.
     */
    public void showAvailableSections() {
        System.out.println("Field Level ($300) - Available Seats: " + getAvailableSeatsInSection("Field Level"));
        System.out.println("Main Level ($120) - Available Seats: " + getAvailableSeatsInSection("Main Level"));
        System.out.println("Grandstand Level ($45) - Available Seats: " + getAvailableSeatsInSection("Grandstand Level"));
    }

    /**
     * Obtiene la cantidad de asientos disponibles en una sección específica.
     *
     * @param section Nombre de la sección.
     * @return Número de asientos disponibles en la sección.
     */
    private int getAvailableSeatsInSection(String section) {
        return (int) availableSeats.stream().filter(seat -> seat.getSection().equals(section)).count();
    }

    /**
     * Realiza la reservación de un asiento para un cliente en una sección específica.
     * Si no hay asientos disponibles, ofrece opciones para unirse a la lista de espera
     * o cambiar de sección.
     *
     * @param client Cliente que realiza la reserva.
     * @param section Nombre de la sección deseada.
     * @param scanner Scanner para leer la entrada del usuario.
     */
    public void reserveSeat(Cliente client, String section, Scanner scanner) {
        String normalizedSection = getNormalizedSection(section);

        if (normalizedSection == null) {
            System.out.println("Sección no válida. Intente nuevamente.");
            return;
        }

        Optional<Asiento> seat = availableSeats.stream().filter(s -> s.getSection().equals(normalizedSection)).findFirst();
        if (seat.isPresent()) {
            Asiento reservedSeat = seat.get();
            availableSeats.remove(reservedSeat); // Actualiza el Set de asientos disponibles.
            reservations.put(client, reservedSeat); // Añade al HashMap de reservas.
            reservationHistory.add(client + " reserved " + reservedSeat); // Registro en LinkedList.
            undoStack.push("reserve:" + client); // Guarda la acción en el Stack.
            System.out.println("Reservation successful for " + client + ". Total cost: $" + getSectionCost(normalizedSection));
        } else {
            System.out.println("Section full. What would you like to do?");
            System.out.println("1. Choose another section");
            System.out.println("2. Join the waitlist for this section");

            int option = -1;
            while (true) {
                try {
                    System.out.print("Ingrese su opción: ");
                    option = Integer.parseInt(scanner.nextLine());
                    if (option == 1 || option == 2) {
                        break;
                    } else {
                        System.out.println("Por favor, seleccione 1 o 2.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Por favor, ingrese 1 o 2.");
                }
            }

            if (option == 1) {
                suggestAlternativeSections(client, scanner);
            } else if (option == 2) {
                waitlistBySection.get(normalizedSection).add(client); // Añade a la Queue de la sección.
                System.out.println(client + " ha sido agregado a la lista de espera de la sección.");
            }
        }
    }
    

    /**
     * Sugiere al cliente secciones alternativas con asientos disponibles para reservar.
     * Si hay asientos disponibles en otras secciones, se le pide al cliente que elija una sección
     * diferente para realizar la reserva. Si no hay opciones disponibles, informa al cliente.
     *
     * @param client Cliente que solicita la reserva de un asiento.
     * @param scanner Objeto Scanner para leer la entrada del usuario.
     */
    private void suggestAlternativeSections(Cliente client, Scanner scanner) {
        System.out.println("Available sections:");
        boolean hasOptions = false; // Variable para verificar si existen opciones disponibles.

        // Recorre todas las secciones y verifica si tienen asientos disponibles.
        for (String section : waitlistBySection.keySet()) {
            // El método getAvailableSeatsInSection verifica cuántos asientos hay disponibles.
            if (getAvailableSeatsInSection(section) > 0) {
                hasOptions = true; // Si encuentra al menos una sección con asientos, activa la bandera.
                System.out.println("- " + section); // Muestra la sección disponible.
            }
        }

        // Si existen secciones con asientos disponibles.
        if (hasOptions) {
            System.out.print("Enter the section name to reserve: ");
            String newSection = scanner.nextLine(); // Lee el nombre de la nueva sección que el cliente desea.
            reserveSeat(client, newSection, scanner); // Llama al método reserveSeat para intentar reservar en la nueva sección.
        } else {
            // Si no hay secciones disponibles, informa al cliente.
            System.out.println("No alternative sections are available.");
        }
    }


    /**
     * Obtiene el costo de un asiento según la sección seleccionada.
     * 
     * Este método devuelve un valor en dólares que corresponde al precio de un asiento
     * en una de las tres secciones disponibles del estadio.
     *
     * @param section Nombre de la sección seleccionada.
     * @return Costo del asiento en la sección. Si la sección no es válida, devuelve 0.
     */
    private int getSectionCost(String section) {
        switch (section) {
            case "Field Level":
                return 300; // Costo de los asientos en "Field Level".
            case "Main Level":
                return 120; // Costo de los asientos en "Main Level".
            case "Grandstand Level":
                return 45;  // Costo de los asientos en "Grandstand Level".
            default:
                return 0;   // Devuelve 0 si la sección no es válida.
        }
    }


        /**
     * Muestra la lista de espera para una sección seleccionada por el usuario.
     * 
     * Este método permite al usuario seleccionar una de las secciones disponibles y,
     * a continuación, visualiza la lista de clientes en espera para esa sección.
     *
     * @param scanner Objeto Scanner para leer la entrada del usuario.
     */
    public void showWaitlist(Scanner scanner) {
        // Crear una lista de las secciones disponibles a partir de las claves del mapa de listas de espera.
        List<String> sections = new ArrayList<>(waitlistBySection.keySet());
        int choice = -1; // Variable para almacenar la elección del usuario.

        // Bucle para manejar errores de entrada.
        while (true) {
            try {
                // Mostrar las secciones disponibles y pedir al usuario que elija una.
                System.out.println("Seleccione la sección para ver la lista de espera:");
                for (int i = 0; i < sections.size(); i++) {
                    System.out.println((i + 1) + ". " + sections.get(i));
                }
                System.out.print("Ingrese el número de la sección: ");

                // Leer la entrada del usuario como String y convertirla a un número entero.
                choice = Integer.parseInt(scanner.nextLine());

                // Validar que el número ingresado esté dentro del rango de secciones disponibles.
                if (choice >= 1 && choice <= sections.size()) {
                    break; // Salir del bucle si la entrada es válida.
                } else {
                    System.out.println("Por favor, seleccione un número dentro del rango disponible.");
                }
            } catch (NumberFormatException e) {
                // Manejar el caso en que el usuario ingrese algo que no sea un número.
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
            }
        }

        // Recuperar el nombre de la sección seleccionada usando la lista.
        String selectedSection = sections.get(choice - 1);

        // Obtener la lista de espera de la sección seleccionada.
        Queue<Cliente> waitlist = waitlistBySection.get(selectedSection);

        // Mostrar la lista de espera o informar si está vacía.
        if (waitlist.isEmpty()) {
            System.out.println("La lista de espera para " + selectedSection + " está vacía.");
        } else {
            System.out.println("Lista de espera para " + selectedSection + ":");
            for (Cliente client : waitlist) {
                System.out.println("- " + client);
            }
        }
    }

    

    /**
     * Cancela una reserva existente y actualiza los asientos disponibles y las listas de espera.
     * 
     * Este método busca una reserva existente para un cliente específico. Si se encuentra,
     * elimina la reserva, libera el asiento, lo agrega a los disponibles y, si hay clientes
     * en la lista de espera, asigna el asiento al próximo cliente en la cola.
     *
     * @param client Cliente que solicita la cancelación de su reserva.
     */
    public void cancelReservation(Cliente client) {
        // Buscar al cliente en el conjunto de claves del mapa de reservas.
        Cliente foundClient = reservations.keySet().stream()
                .filter(c -> c.getName().equals(client.getName()) && c.getEmail().equals(client.getEmail()))
                .findFirst()
                .orElse(null); // Si no se encuentra el cliente, devuelve null.

        if (foundClient != null) {
            // Si se encuentra el cliente, recuperar y eliminar su asiento reservado.
            Asiento seat = reservations.remove(foundClient); // Elimina la entrada del mapa de reservas.
            availableSeats.add(seat); // Agregar el asiento de nuevo al conjunto de disponibles.
            reservationHistory.add(foundClient + " canceled reservation for " + seat); // Registrar la cancelación.

            // Informar que la cancelación fue exitosa.
            System.out.println("Reservation canceled for " + foundClient + ".");

            // Ofrecer el asiento liberado al próximo cliente en la lista de espera.
            manageWaitlist(seat);
        } else {
            // Informar que no se encontró una reserva para el cliente.
            System.out.println("No reservation found for " + client + ".");
        }
    }


    /**
     * Gestiona la lista de espera para una sección específica cuando un asiento queda disponible.
     * 
     * Si hay clientes en la lista de espera para la sección del asiento liberado, este método
     * asigna el asiento al primer cliente en la cola (orden FIFO) y actualiza las estructuras
     * de datos correspondientes.
     *
     * @param seat Asiento que ha quedado disponible tras una cancelación.
     */
    private void manageWaitlist(Asiento seat) {
        // Obtener el nombre de la sección del asiento liberado.
        String section = seat.getSection();

        // Recuperar la lista de espera asociada a la sección.
        Queue<Cliente> waitlistForSection = waitlistBySection.get(section);

        // Si la lista de espera no está vacía, asignar el asiento al próximo cliente en la cola.
        if (!waitlistForSection.isEmpty()) {
            Cliente nextClient = waitlistForSection.poll(); // Quitar al cliente del frente de la cola (FIFO).
            reservations.put(nextClient, seat); // Asignar el asiento al cliente en el mapa de reservas.
            availableSeats.remove(seat); // Remover el asiento del conjunto de disponibles.
            reservationHistory.add(nextClient + " reserved from waitlist for " + seat); // Registrar la acción.

            // Informar que la reserva desde la lista de espera fue exitosa.
            System.out.println("Waitlist reservation successful for " + nextClient + " in " + section + ".");
        }
    }


    /**
     * Normaliza el nombre de una sección ingresada por el usuario para que sea insensible
     * a mayúsculas y minúsculas.
     * 
     * Este método garantiza que las entradas del usuario se correspondan con los nombres
     * estándar de las secciones, incluso si se ingresan con un formato diferente.
     *
     * @param section Nombre de la sección ingresado por el usuario.
     * @return Nombre normalizado de la sección, o null si la entrada no es válida.
     */
    private String getNormalizedSection(String section) {
        // Compara el nombre ingresado con los nombres estándar de las secciones, ignorando mayúsculas y minúsculas.
        if (section.equalsIgnoreCase("Field Level")) return "Field Level"; // Sección "Field Level".
        if (section.equalsIgnoreCase("Main Level")) return "Main Level";   // Sección "Main Level".
        if (section.equalsIgnoreCase("Grandstand Level")) return "Grandstand Level"; // Sección "Grandstand Level".
        return null; // Devuelve null si la entrada no coincide con ninguna sección válida.
    }

}
