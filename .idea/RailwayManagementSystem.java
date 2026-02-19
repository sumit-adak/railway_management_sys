// Railway Management System (Console Based)
// Final Year Java Project
// Uses: OOP + File Handling + ArrayList


import java.io.*;
import java.util.*;

// ======================= MODELS =======================

class User {
    String username;
    String password;
    String role; // ADMIN / PASSENGER

    public User(String u, String p, String r) {
        username = u;
        password = p;
        role = r;
    }
}

class Train {
    int trainNo;
    String name;
    String source;
    String destination;
    int totalSeats;
    double farePerKm;

    public Train(int no, String n, String s, String d, int seats, double fare) {
        trainNo = no;
        name = n;
        source = s;
        destination = d;
        totalSeats = seats;
        farePerKm = fare;
    }
}

class Ticket {
    String pnr;
    String passengerName;
    int age;
    int trainNo;
    int seatNo;
    String status; // CONFIRMED / WAITING / CANCELLED
    double fare;

    public Ticket(String pnr, String name, int age, int trainNo,
                  int seatNo, String status, double fare) {
        this.pnr = pnr;
        this.passengerName = name;
        this.age = age;
        this.trainNo = trainNo;
        this.seatNo = seatNo;
        this.status = status;
        this.fare = fare;
    }
}

// ======================= UTILS =======================

class FileUtil {

    public static void writeLine(String file, String data) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(data + "\n");
        } catch (IOException e) {
            System.out.println("File Write Error");
        }
    }

    public static List<String> readAll(String file) {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null)
                list.add(line);
        } catch (IOException e) {
        }
        return list;
    }

    public static void overwrite(String file, List<String> data) {
        try (FileWriter fw = new FileWriter(file)) {
            for (String s : data)
                fw.write(s + "\n");
        } catch (IOException e) {
        }
    }
}

class PNRGenerator {
    public static String generatePNR() {
        return "PNR" + System.currentTimeMillis() % 1000000;
    }
}

// ======================= SERVICES =======================

class AuthService {

    static final String FILE = "users.txt";

    public static User login(String u, String p) {
        for (String line : FileUtil.readAll(FILE)) {
            String[] data = line.split(",");
            if (data[0].equals(u) && data[1].equals(p))
                return new User(data[0], data[1], data[2]);
        }
        return null;
    }

    public static void register(String u, String p, String role) {
        FileUtil.writeLine(FILE, u + "," + p + "," + role);
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

    public static void deleteTrain(int trainNo) {
        List<String> data = FileUtil.readAll(FILE);
        data.removeIf(line -> line.startsWith(trainNo + ","));
        FileUtil.overwrite(FILE, data);
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
                System.out.println(t.trainNo + " " + t.name);
            }
        }
    }
}

class TicketService {

    static final String FILE = "tickets.txt";

    public static int getBookedSeats(int trainNo) {
        int count = 0;
        for (String line : FileUtil.readAll(FILE)) {
            String[] d = line.split(",");
            if (Integer.parseInt(d[3]) == trainNo &&
                    d[5].equals("CONFIRMED"))
                count++;
        }
        return count;
    }

    public static void bookTicket(String name, int age,
                                  int trainNo, boolean tatkal) {

        Train t = TrainService.findTrain(trainNo);
        if (t == null) {
            System.out.println("Train not found");
            return;
        }

        int booked = getBookedSeats(trainNo);
        int seatNo = booked + 1;

        String status;
        if (booked < t.totalSeats)
            status = "CONFIRMED";
        else
            status = "WAITING";

        double fare = t.farePerKm * 100; // base distance
        if (tatkal) fare *= 1.5;

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

// ======================= MAIN UI =======================

public class RailwayManagementSystem {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n1. Login  2. Register  3. Exit");
            int ch = sc.nextInt();

            if (ch == 1) login();
            else if (ch == 2) register();
            else break;
        }
    }

    static void login() {
        System.out.print("Username: ");
        String u = sc.next();
        System.out.print("Password: ");
        String p = sc.next();

        User user = AuthService.login(u, p);

        if (user == null) {
            System.out.println("Invalid Login");
            return;
        }

        if (user.role.equals("ADMIN")) adminMenu();
        else passengerMenu();
    }

    static void register() {
        System.out.print("Username: ");
        String u = sc.next();
        System.out.print("Password: ");
        String p = sc.next();
        System.out.print("Role (ADMIN/PASSENGER): ");
        String r = sc.next();

        AuthService.register(u, p, r);
    }

    // ================= ADMIN =================

    static void adminMenu() {
        while (true) {
            System.out.println(
                    "\n1.Add Train 2.View Trains 3.Delete Train 4.Revenue 5.Logout");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> addTrain();
                case 2 -> viewTrains();
                case 3 -> deleteTrain();
                case 4 -> TicketService.revenueReport();
                default -> {return;}
            }
        }
    }

    static void addTrain() {
        System.out.print("No: ");
        int no = sc.nextInt();
        System.out.print("Name: ");
        String name = sc.next();
        System.out.print("Source: ");
        String src = sc.next();
        System.out.print("Destination: ");
        String dest = sc.next();
        System.out.print("Seats: ");
        int seats = sc.nextInt();
        System.out.print("Fare/Km: ");
        double fare = sc.nextDouble();

        TrainService.addTrain(
                new Train(no, name, src, dest, seats, fare));
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

    // ================= PASSENGER =================

    static void passengerMenu() {
        while (true) {
            System.out.println(
                    "\n1.Search Train 2.Book Ticket 3.Cancel Ticket 4.Logout");
            int ch = sc.nextInt();

            switch (ch) {
                case 1 -> searchTrain();
                case 2 -> bookTicket();
                case 3 -> cancelTicket();
                default -> {return;}
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
        System.out.print("Name: ");
        String name = sc.next();
        System.out.print("Age: ");
        int age = sc.nextInt();
        System.out.print("Train No: ");
        int no = sc.nextInt();
        System.out.print("Tatkal? (true/false): ");
        boolean tatkal = sc.nextBoolean();

        TicketService.bookTicket(name, age, no, tatkal);
    }

    static void cancelTicket() {
        System.out.print("PNR: ");
        String pnr = sc.next();
        TicketService.cancelTicket(pnr);
    }
}
