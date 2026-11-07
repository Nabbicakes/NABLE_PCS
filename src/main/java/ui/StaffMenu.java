
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import dao.AppointmentDao;
import dao.PetDao;
import model.User;
import model.Appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StaffMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final AppointmentDao appointmentDao = new AppointmentDao();
    private final PetDao petDao = new PetDao();

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy");

    public StaffMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|          PET CLINIC SYSTEM         |");
            System.out.println("|               STAFF                |");
            System.out.println("======================================");
            System.out.println("| 1. Manage Pet Records              |");
            System.out.println("| 2. Manage Appointments             |");
            System.out.println("| 3. View Payments                   |");
            System.out.println("| 4. Logout                          |");
            System.out.println("| 5. Exit                            |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    managePetRecords(user);
                    break;

                case "2":
                    manageAppointments(user);
                    break;

                case "3":
                    System.out.println("View Payments is not implemented yet.");
                    break;

                case "4":
                    try {
                        activityLogDao.logAction(
                                user.userId(),
                                "USER_LOGOUT",
                                "Staff " + user.username()
                                        + " (ID: " + user.userId()
                                        + ") logged out."
                        );

                        System.out.println("Logged out successfully!");

                    } catch (SQLException e) {
                        System.out.println(
                                "Warning: Logout activity could not be recorded: "
                                        + e.getMessage()
                        );
                        System.out.println("Logged out successfully!");
                    }

                    return;

                case "5":
                    System.out.println("Thank you for using Pet Clinic System!");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ==================== PET RECORDS ====================

    private void managePetRecords(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|           MANAGE PET RECORDS       |");
            System.out.println("======================================");
            System.out.println("| 1. Register Pet                    |");
            System.out.println("| 2. View All Registered Pets        |");
            System.out.println("| 3. Back to Staff Menu              |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    registerPet(user);
                    break;

                case "2":
                    viewAllPets();
                    break;

                case "3":
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void registerPet(User user) {
        try {
            List<String[]> owners = petDao.getApprovedPetOwners();

            if (owners.isEmpty()) {
                System.out.println("No approved Pet Owner accounts found.");
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|         APPROVED PET OWNERS        |");
            System.out.println("======================================");
            System.out.printf("| %-10s | %-18s |%n", "USER ID", "USERNAME");
            System.out.println("+------------+----------------------+");

            for (String[] owner : owners) {
                System.out.printf(
                        "| %-10s | %-18s |%n",
                        owner[0], owner[1]
                );
            }

            System.out.println("+------------+----------------------+");

            System.out.print("Enter Pet Owner User ID (0 to cancel): ");
            int ownerUserId = readInt();

            if (ownerUserId <= 0) {
                return;
            }

            String selectedUsername = null;

            for (String[] owner : owners) {
                if (Integer.parseInt(owner[0]) == ownerUserId) {
                    selectedUsername = owner[1];
                    break;
                }
            }

            if (selectedUsername == null) {
                System.out.println("Invalid Pet Owner ID.");
                return;
            }

            System.out.print("Enter Owner's Full Name: ");
            String ownerName = scanner.nextLine().trim();

            System.out.print("Enter Owner's Contact Number: ");
            String contact = scanner.nextLine().trim();

            System.out.print("Enter Owner's Address: ");
            String address = scanner.nextLine().trim();

            if (ownerName.isEmpty() || contact.isEmpty()
                    || address.isEmpty()) {
                System.out.println("Owner information cannot be empty.");
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|           PET INFORMATION          |");
            System.out.println("======================================");

            System.out.print("Enter Pet Name: ");
            String petName = scanner.nextLine().trim();

            System.out.print("Enter Species: ");
            String species = scanner.nextLine().trim();

            System.out.print("Enter Breed: ");
            String breed = scanner.nextLine().trim();

            System.out.print("Enter Age: ");
            int age = readInt();

            if (age < 0) {
                System.out.println("Invalid pet age.");
                return;
            }

            System.out.print("Enter Gender (Male/Female): ");
            String gender = scanner.nextLine().trim();

            if (petName.isEmpty() || species.isEmpty()
                    || breed.isEmpty() || gender.isEmpty()) {
                System.out.println("Pet information cannot be empty.");
                return;
            }

            if (!gender.equalsIgnoreCase("Male")
                    && !gender.equalsIgnoreCase("Female")) {
                System.out.println("Gender must be Male or Female.");
                return;
            }

            petDao.registerPet(
                    ownerUserId,
                    ownerName,
                    contact,
                    address,
                    petName,
                    species,
                    breed,
                    age,
                    gender,
                    user.userId()
            );

        } catch (SQLException e) {
            System.out.println(
                    "Error registering pet: " + e.getMessage()
            );
        }
    }

    private void viewAllPets() {
        try {
            List<String[]> pets = petDao.getAllPets();

            if (pets.isEmpty()) {
                System.out.println("No registered pets found yet.");
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("|                    ALL REGISTERED PETS                     |");
            System.out.println("==============================================================");
            System.out.printf(
                    "| %-5s | %-15s | %-12s | %-12s | %-5s | %-8s | %-10s |%n",
                    "ID", "PET NAME", "SPECIES", "BREED",
                    "AGE", "GENDER", "STATUS"
            );
            System.out.println("+-------+-----------------+--------------+--------------+-------+----------+------------+");

            for (String[] pet : pets) {
                System.out.printf(
                        "| %-5s | %-15s | %-12s | %-12s | %-5s | %-8s | %-10s |%n",
                        pet[0], pet[1], pet[2], pet[3],
                        pet[4], pet[5], pet[6]
                );

                System.out.println("| Owner: " + pet[7]);
            }

            System.out.println("+-------+-----------------+--------------+--------------+-------+----------+------------+");

        } catch (SQLException e) {
            System.out.println(
                    "Error viewing pet records: " + e.getMessage()
            );
        }
    }

    // ==================== APPOINTMENTS ====================

    private void manageAppointments(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|          MANAGE APPOINTMENTS       |");
            System.out.println("======================================");
            System.out.println("| 1. Schedule Appointment            |");
            System.out.println("| 2. View All Appointments           |");
            System.out.println("| 3. Update Appointment Status       |");
            System.out.println("| 4. Back to Staff Menu              |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    scheduleAppointment(user);
                    break;

                case "2":
                    viewAllAppointments();
                    break;

                case "3":
                    updateAppointmentStatus(user);
                    break;

                case "4":
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Schedule using available dates and time slots
    private void scheduleAppointment(User user) {
        try {
            // Select pet
            List<String[]> pets = appointmentDao.getAllPets();

            if (pets.isEmpty()) {
                System.out.println("No registered pets found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("|              SELECT A PET                  |");
            System.out.println("==============================================");
            System.out.printf(
                    "| %-8s | %-20s | %-15s |%n",
                    "PET ID", "PET NAME", "SPECIES"
            );
            System.out.println("+----------+----------------------+-----------------+");

            for (String[] pet : pets) {
                System.out.printf(
                        "| %-8s | %-20s | %-15s |%n",
                        pet[0], pet[1], pet[2]
                );
            }

            System.out.println("+----------+----------------------+-----------------+");

            System.out.print("Enter Pet ID (0 to cancel): ");
            int petId = readInt();

            if (petId <= 0) {
                return;
            }

            boolean petExists = false;

            for (String[] pet : pets) {
                if (Integer.parseInt(pet[0]) == petId) {
                    petExists = true;
                    break;
                }
            }

            if (!petExists) {
                System.out.println("Invalid Pet ID.");
                return;
            }

            // Select veterinarian
            List<String[]> veterinarians =
                    appointmentDao.getAllVeterinarians();

            if (veterinarians.isEmpty()) {
                System.out.println("No veterinarians found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("|                    SELECT A VETERINARIAN                   |");
            System.out.println("==============================================================");
            System.out.printf(
                    "| %-5s | %-18s | %-20s | %-13s | %-10s |%n",
                    "ID", "NAME", "SPECIALIZATION", "AVAILABILITY", "STATUS"
            );
            System.out.println("+-------+--------------------+----------------------+---------------+------------+");

            for (String[] vet : veterinarians) {
                System.out.printf(
                        "| %-5s | %-18s | %-20s | %-13s | %-10s |%n",
                        vet[0], vet[1], vet[2], vet[3], vet[4]
                );
                System.out.println("| Working Days: " + vet[5]);
            }

            System.out.println("+-------+--------------------+----------------------+---------------+------------+");

            System.out.print("Enter Veterinarian ID (0 to cancel): ");
            int veterinarianId = readInt();

            if (veterinarianId <= 0) {
                return;
            }

            boolean veterinarianExists = false;

            for (String[] vet : veterinarians) {
                if (Integer.parseInt(vet[0]) == veterinarianId) {
                    veterinarianExists = true;

                    if (!"Available".equalsIgnoreCase(vet[3])
                            || !"Active".equalsIgnoreCase(vet[4])) {
                        System.out.println(
                                "This veterinarian is not currently available."
                        );
                        return;
                    }

                    break;
                }
            }

            if (!veterinarianExists) {
                System.out.println("Invalid Veterinarian ID.");
                return;
            }

            // Select available date
            List<LocalDate> possibleDates =
                    appointmentDao.getAvailableDates(veterinarianId);

            List<LocalDate> availableDates = new ArrayList<>();

            for (LocalDate date : possibleDates) {
                List<String[]> slots =
                        appointmentDao.getAvailableTimeSlots(
                                veterinarianId,
                                date.toString()
                        );

                boolean hasAvailableSlot = false;

                for (String[] slot : slots) {
                    if ("Available".equalsIgnoreCase(slot[2])) {
                        hasAvailableSlot = true;
                        break;
                    }
                }

                if (hasAvailableSlot) {
                    availableDates.add(date);
                }
            }

            if (availableDates.isEmpty()) {
                System.out.println(
                        "No available appointment dates for this veterinarian."
                );
                return;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("|              AVAILABLE DATES               |");
            System.out.println("==============================================");
            System.out.printf(
                    "| %-8s | %-25s | %-12s |%n",
                    "OPTION", "DATE", "DAY"
            );
            System.out.println("+----------+---------------------------+--------------+");

            for (int i = 0; i < availableDates.size(); i++) {
                LocalDate date = availableDates.get(i);

                System.out.printf(
                        "| %-8d | %-25s | %-12s |%n",
                        i + 1,
                        date.format(DATE_FORMAT),
                        date.getDayOfWeek()
                );
            }

            System.out.println("+----------+---------------------------+--------------+");

            System.out.print("Choose a date (0 to cancel): ");
            int dateChoice = readInt();

            if (dateChoice <= 0 || dateChoice > availableDates.size()) {
                if (dateChoice != 0) {
                    System.out.println("Invalid date selection.");
                }
                return;
            }

            LocalDate selectedDate = availableDates.get(dateChoice - 1);

            // Display ALL time slots, including booked slots
            List<String[]> timeSlots =
                    appointmentDao.getAvailableTimeSlots(
                            veterinarianId,
                            selectedDate.toString()
                    );

            if (timeSlots.isEmpty()) {
                System.out.println("No time slots found for this date.");
                return;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("|               TIME SLOTS                   |");
            System.out.println("==============================================");
            System.out.printf(
                    "| %-8s | %-25s | %-12s |%n",
                    "OPTION", "TIME", "STATUS"
            );
            System.out.println("+----------+---------------------------+--------------+");

            for (int i = 0; i < timeSlots.size(); i++) {
                String[] slot = timeSlots.get(i);

                System.out.printf(
                        "| %-8d | %-25s | %-12s |%n",
                        i + 1,
                        slot[1],
                        slot[2]
                );
            }

            System.out.println("+----------+---------------------------+--------------+");

            // Keep asking until an available slot is selected or cancelled
            int timeChoice;
            String[] selectedSlot;

            while (true) {
                System.out.print("Choose a time slot (0 to cancel): ");
                timeChoice = readInt();

                if (timeChoice == 0) {
                    System.out.println("Appointment scheduling cancelled.");
                    return;
                }

                if (timeChoice < 1 || timeChoice > timeSlots.size()) {
                    System.out.println("Invalid time selection. Try again.");
                    continue;
                }

                selectedSlot = timeSlots.get(timeChoice - 1);

                if (!"Available".equalsIgnoreCase(selectedSlot[2])) {
                    System.out.println(
                            "This time slot is already booked or unavailable. Please choose another."
                    );
                    continue;
                }

                break;
            }

            String selectedTime = selectedSlot[0];

            // Appointment reason
            System.out.println();
            System.out.print("Enter Reason for Appointment: ");
            String reason = scanner.nextLine().trim();

            if (reason.isEmpty()) {
                System.out.println("Reason cannot be empty.");
                return;
            }

            // Confirm appointment
            System.out.println();
            System.out.println("==============================================");
            System.out.println("|             APPOINTMENT SUMMARY            |");
            System.out.println("==============================================");
            System.out.println("| Pet ID: " + petId);
            System.out.println("| Veterinarian ID: " + veterinarianId);
            System.out.println("| Date: " + selectedDate.format(DATE_FORMAT));
            System.out.println("| Time: " + selectedSlot[1]);
            System.out.println("| Reason: " + reason);
            System.out.println("==============================================");
            System.out.print("Confirm appointment? (Y/N): ");

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Appointment scheduling cancelled.");
                return;
            }

            appointmentDao.createAppointment(
                    petId,
                    veterinarianId,
                    selectedDate.toString(),
                    selectedTime,
                    reason,
                    user.userId()
            );

        } catch (SQLException e) {
            System.out.println(
                    "Error scheduling appointment: " + e.getMessage()
            );
        }
    }

    // View all appointments
    private void viewAllAppointments() {
        try {
            List<Appointment> appointments =
                    appointmentDao.getAllAppointments();

            if (appointments.isEmpty()) {
                System.out.println("No appointments found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("|                    ALL APPOINTMENTS                        |");
            System.out.println("==============================================================");
            System.out.printf(
                    "| %-5s | %-7s | %-7s | %-12s | %-8s | %-12s |%n",
                    "ID", "PET ID", "VET ID", "DATE", "TIME", "STATUS"
            );
            System.out.println("+-------+---------+---------+--------------+----------+--------------+");

            for (Appointment appointment : appointments) {
                System.out.printf(
                        "| %-5d | %-7d | %-7d | %-12s | %-8s | %-12s |%n",
                        appointment.appointmentId(),
                        appointment.petId(),
                        appointment.veterinarianId(),
                        appointment.appointmentDate(),
                        appointment.appointmentTime(),
                        appointment.status()
                );

                System.out.println("| Reason: " + appointment.reason());
            }

            System.out.println("+-------+---------+---------+--------------+----------+--------------+");

        } catch (SQLException e) {
            System.out.println(
                    "Error viewing appointments: " + e.getMessage()
            );
        }
    }

    // Update appointment status
    private void updateAppointmentStatus(User user) {
        try {
            List<Appointment> appointments =
                    appointmentDao.getAllAppointments();

            if (appointments.isEmpty()) {
                System.out.println("No appointments available to update.");
                return;
            }

            viewAllAppointments();

            System.out.print("Enter Appointment ID (0 to cancel): ");
            int appointmentId = readInt();

            if (appointmentId <= 0) {
                return;
            }

            boolean appointmentExists = false;

            for (Appointment appointment : appointments) {
                if (appointment.appointmentId() == appointmentId) {
                    appointmentExists = true;
                    break;
                }
            }

            if (!appointmentExists) {
                System.out.println("Invalid Appointment ID.");
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|       UPDATE APPOINTMENT STATUS    |");
            System.out.println("======================================");
            System.out.println("| 1. Scheduled                       |");
            System.out.println("| 2. Completed                       |");
            System.out.println("| 3. Cancelled                       |");
            System.out.println("======================================");
            System.out.print("Choose new status: ");

            String choice = scanner.nextLine();

            String newStatus;

            switch (choice) {
                case "1":
                    newStatus = "Scheduled";
                    break;

                case "2":
                    newStatus = "Completed";
                    break;

                case "3":
                    newStatus = "Cancelled";
                    break;

                default:
                    System.out.println("Invalid status choice.");
                    return;
            }

            appointmentDao.updateAppointmentStatus(
                    appointmentId,
                    newStatus,
                    user.userId()
            );

        } catch (SQLException e) {
            System.out.println(
                    "Error updating appointment: " + e.getMessage()
            );
        }
    }

    // Read integer input
    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }
}
