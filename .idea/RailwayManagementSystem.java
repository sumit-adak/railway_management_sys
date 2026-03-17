import java.io.*;
import java.util.*;

// ================= MODELS =================

class User {
    String username, password, role;

    User(String u, String p, String r) {
        username = u;
        password = p;
        role = r;
    }
}

class Train {
    int trainNo, totalSeats;
    String name, source, destination;
    double farePerKm;

    Train(int no, String n, String s, String d, int seats, double fare) {
        trainNo = no;
        name = n;
        source = s;
        destination = d;
        totalSeats = seats;
        farePerKm = fare;
    }
}

// ================= UTIL =================

class FileUtil {

    public static void writeLine(String file, String data) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(data + "\n");
        } catch (Exception e) {}
    }

    public static List<String> readAll(String file) {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null)
                list.add(line);
        } catch (Exception e) {}
        return list;
    }

    public static void overwrite(String file, List<String> data) {
        try (FileWriter fw = new FileWriter(file)) {
            for (String s : data) fw.write(s + "\n");
        } catch (Exception e) {}
    }
}

class PNRGenerator {
    public static String generatePNR() {
        return "PNR" + (int)(Math.random() * 100000);
    }
}

// ================= SERVICES =================

class AuthService {
    static final String FILE = "users.txt";

    public static User login(String u, String p) {
        for (String line : FileUtil.readAll(FILE)) {
            String[] d = line.split(",");
            if (d[0].equals(u) && d[1].equals(p))
                return new User(d[0], d[1], d[2]);
        }
        return null;
    }

    public static void register(String u, String p, String r) {
        FileUtil.writeLine(FILE, u + "," + p + "," + r);
    }
}

class TrainService {
    static final String FILE = "trains.txt";

    public static void addTrain(Train t) {
        FileUtil.writeLine(FILE,
                t.trainNo + "," + t.name + "," + t.source + "," +
                        t.destination + "," + t.totalSeats + "," + t.farePerKm);
    }

    public static List<Train> getAllTrains() {
        List<Train> list = new ArrayList<>();
        for (String line : FileUtil.readAll(FILE)) {
            String[] d = line.split(",");
            list.add(new Train(
                    Integer.parseInt(d[0]), d[1], d[2], d[3],
                    Integer.parseInt(d[4]), Double.parseDouble(d[5])
            ));
        }
        return list;
    }

    public static Train findTrain(int trainNo) {
        for (Train t : getAllTrains())
            if (t.trainNo == trainNo)
                return t;
        return null;
    }

    public static void search(String src, String dest) {
        for (Train t : getAllTrains()) {
            if (t.source.equalsIgnoreCase(src) &&
                t.destination.equalsIgnoreCase(dest)) {

                double price = t.farePerKm * 100;

                System.out.println("Train No: " + t.trainNo +
                        " | Name: " + t.name +
                        " | Price: " + price);
            }
        }
    }

    public static void deleteTrain(int no) {
        List<String> data = FileUtil.readAll(FILE);
        data.removeIf(line -> line.startsWith(no + ","));
        FileUtil.overwrite(FILE, data);
    }
}

class TicketService {

    static final String FILE = "tickets.txt";

    public static int getBookedSeats(int trainNo) {
        int count = 0;
        for (String line : FileUtil.readAll(FILE)) {
            String[] d = line.split(",");
            if (Integer.parseInt(d[3]) == trainNo && d[5].equals("CONFIRMED"))
                count++;
        }
        return count;
    }

    public static void bookTicket(String name, int age, int trainNo, boolean tatkal) {

        Train t = TrainService.findTrain(trainNo);
        if (t == null) {
            System.out.println("Train not found ❌");
            return;
        }

        int booked = getBookedSeats(trainNo);
        int seatNo = booked + 1;

        String status = (booked < t.totalSeats) ? "CONFIRMED" : "WAITING";

        double fare = t.farePerKm * 100;
        if (tatkal) fare *= 1.5;

        System.out.println("Ticket Price: " + fare);

        String pnr = PNRGenerator.generatePNR();

        FileUtil.writeLine(FILE,
                pnr + "," + name + "," + age + "," + trainNo + "," +
                        seatNo + "," + status + "," + fare);

        System.out.println("PNR: " + pnr);
        System.out.println("Status: " + status);
    }

    public static void cancelTicket(String pnr) {
        List<String> data = FileUtil.readAll(FILE);
        List<String> newData = new ArrayList<>();

        for (String line : data) {
            if (line.startsWith(pnr + ",")) {
                String[] d = line.split(",");
                double refund = Double.parseDouble(d[6]) * 0.8;
                System.out.println("Refund: " + refund);
                newData.add(line.replace(d[5], "CANCELLED"));
            } else newData.add(line);
        }

        FileUtil.overwrite(FILE, newData);
    }

    public static void viewMyTickets(String username) {

        boolean found = false;

        for (String line : FileUtil.readAll(FILE)) {
            String[] d = line.split(",");

            if (d[1].equalsIgnoreCase(username)) {

                Train t = TrainService.findTrain(Integer.parseInt(d[3]));

                System.out.println("\nPNR: " + d[0]);
                System.out.println("Name: " + d[1]);
                System.out.println("Train: " + (t != null ? t.name : d[3]));
                System.out.println("Route: " + (t != null ? t.source + " -> " + t.destination : ""));
                System.out.println("Seat No: " + d[4]);
                System.out.println("Status: " + d[5]);
                System.out.println("Fare: " + d[6]);
                System.out.println("----------------------");

                found = true;
            }
        }

        if (!found) System.out.println("No Tickets Found ❌");
    }

    public static void revenueReport() {
        double total = 0;
        for (String line : FileUtil.readAll(FILE)) {
            String[] d = line.split(",");
            if (d[5].equals("CONFIRMED"))
                total += Double.parseDouble(d[6]);
        }
        System.out.println("Total Revenue: " + total);
    }
}

// ================= MAIN =================

public class RailwayManagementSystem {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n1.Login 2.Register 3.Exit");
            int ch = sc.nextInt();

            if (ch == 1) login();
            else if (ch == 2) register();
            else break;
        }
    }
static void login() {

    sc.nextLine(); // ✅ buffer clear

    System.out.print("Username: ");
    String u = sc.nextLine();

    System.out.print("Password: ");
    String p = sc.nextLine();

    User user = AuthService.login(u, p);

    if (user == null) {
        System.out.println("Invalid Login ❌");
        return;
    }

    if (user.role.equalsIgnoreCase("ADMIN")) adminMenu();
    else passengerMenu(user.username);
}

   static void register() {

    sc.nextLine(); // ✅ buffer clear

    System.out.print("Username: ");
    String u = sc.nextLine();

    System.out.print("Password: ");
    String p = sc.nextLine();

    System.out.print("Role (ADMIN/PASSENGER): ");
    String r = sc.nextLine();

    AuthService.register(u, p, r);
}

    // ADMIN

    static void adminMenu() {
        while (true) {
            System.out.println("\n1.Add Train 2.View Trains 3.Delete Train 4.Revenue 5.Logout");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> addTrain();
                case 2 -> viewTrains();
                case 3 -> deleteTrain();
                case 4 -> TicketService.revenueReport();
                default -> { return; }
            }
        }
    }

    static void addTrain() {
        sc.nextLine(); // FIX BUFFER

        System.out.print("No: ");
        int no = sc.nextInt();
        sc.nextLine();

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Source: ");
        String src = sc.nextLine();

        System.out.print("Destination: ");
        String dest = sc.nextLine();

        System.out.print("Seats: ");
        int seats = sc.nextInt();

        System.out.print("Fare/Km: ");
        double fare = sc.nextDouble();

        TrainService.addTrain(new Train(no, name, src, dest, seats, fare));
    }

    static void viewTrains() {
        for (Train t : TrainService.getAllTrains()) {
            System.out.println(t.trainNo + " " + t.name + " " +
                    t.source + " -> " + t.destination);
        }
    }

    static void deleteTrain() {
        System.out.print("Train No: ");
        int no = sc.nextInt();
        TrainService.deleteTrain(no);
    }

    // PASSENGER

    static void passengerMenu(String user) {
        while (true) {
            System.out.println("\n1.Search 2.Book 3.Cancel 4.My Tickets 5.Logout");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> searchTrain();
                case 2 -> bookTicket();
                case 3 -> cancelTicket();
                case 4 -> TicketService.viewMyTickets(user);
                default -> { return; }
            }
        }
    }

    static void searchTrain() {
        System.out.print("Source: ");
        String s = sc.next();
        System.out.print("Destination: ");
        String d = sc.next();
        TrainService.search(s, d);
    }

    static void bookTicket() {
        sc.nextLine(); // FIX

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Age: ");
        int age = sc.nextInt();

        System.out.print("Train No: ");
        int no = sc.nextInt();

        System.out.print("Tatkal (true/false): ");
        boolean tatkal = sc.nextBoolean();

        TicketService.bookTicket(name, age, no, tatkal);
    }

    static void cancelTicket() {
        System.out.print("PNR: ");
        String pnr = sc.next();
        TicketService.cancelTicket(pnr);
    }
}