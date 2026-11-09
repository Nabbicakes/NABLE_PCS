
package ui;

import dao.UserDao;
import dao.ActivityLogDao;
import dao.AppointmentDao;
import dao.PetDao;
import dao.PaymentDao;
import dao.MedicalRecordDao;
import model.User;
import model.Appointment;
import model.MedicalRecord;

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
    private final PaymentDao paymentDao = new PaymentDao();
    private final MedicalRecordDao medicalRecordDao = new MedicalRecordDao();

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
            System.out.println("| 4. Manage Medical Records          |");
            System.out.println("| 5. Logout                          |");
            System.out.println("| 6. Exit                            |");
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
                    managePayments(user);
                    break;
                case "4":
                    manageMedicalRecords(user);
                    break;
                case "5":
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
                case "6":
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
                System.out.printf("| %-10s | %-18s |%n", owner[0], owner[1]);
            }

            System.out.println("+------------+----------------------+");

            System.out.print("Enter Pet Owner User ID (0 to cancel): ");
            int ownerUserId = readInt();

            if (ownerUserId <= 0) return;

            boolean ownerExists = false;

            for (String[] owner : owners) {
                if (Integer.parseInt(owner[0]) == ownerUserId) {
                    ownerExists = true;
                    break;
                }
            }

            if (!ownerExists) {
                System.out.println("Invalid Pet Owner ID.");
                return;
            }

            System.out.print("Enter Owner's Full Name: ");
            String ownerName = scanner.nextLine().trim();

            System.out.print("Enter Owner's Contact Number: ");
            String contact = scanner.nextLine().trim();

            System.out.print("Enter Owner's Address: ");
            String address = scanner.nextLine().trim();

            if (ownerName.isEmpty() || contact.isEmpty() || address.isEmpty()) {
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
                    ownerUserId, ownerName, contact, address,
                    petName, species, breed, age, gender, user.userId()
            );

        } catch (SQLException e) {
            System.out.println("Error registering pet: " + e.getMessage());
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
            System.out.println("Error viewing pet records: " + e.getMessage());
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

    private void scheduleAppointment(User user) {
        try {
            List<String[]> pets = appointmentDao.getAllPets();

            if (pets.isEmpty()) {
                System.out.println("No registered pets found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("|              SELECT A PET                  |");
            System.out.println("==============================================");
            System.out.printf("| %-8s | %-20s | %-15s |%n",
                    "PET ID", "PET NAME", "SPECIES");
            System.out.println("+----------+----------------------+-----------------+");

            for (String[] pet : pets) {
                System.out.printf("| %-8s | %-20s | %-15s |%n",
                        pet[0], pet[1], pet[2]);
            }

            System.out.println("+----------+----------------------+-----------------+");

            System.out.print("Enter Pet ID (0 to cancel): ");
            int petId = readInt();

            if (petId <= 0) return;

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

            if (veterinarianId <= 0) return;

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

            List<LocalDate> possibleDates =
                    appointmentDao.getAvailableDates(veterinarianId);

            List<LocalDate> availableDates = new ArrayList<>();

            for (LocalDate date : possibleDates) {
                List<String[]> slots =
                        appointmentDao.getAvailableTimeSlots(
                                veterinarianId, date.toString()
                        );

                for (String[] slot : slots) {
                    if ("Available".equalsIgnoreCase(slot[2])) {
                        availableDates.add(date);
                        break;
                    }
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
            System.out.printf("| %-8s | %-25s | %-12s |%n",
                    "OPTION", "DATE", "DAY");
            System.out.println("+----------+---------------------------+--------------+");

            for (int i = 0; i < availableDates.size(); i++) {
                LocalDate date = availableDates.get(i);
                System.out.printf("| %-8d | %-25s | %-12s |%n",
                        i + 1, date.format(DATE_FORMAT), date.getDayOfWeek());
            }

            System.out.println("+----------+---------------------------+--------------+");

            System.out.print("Choose a date (0 to cancel): ");
            int dateChoice = readInt();

            if (dateChoice <= 0 || dateChoice > availableDates.size()) {
                if (dateChoice != 0) System.out.println("Invalid date selection.");
                return;
            }

            LocalDate selectedDate = availableDates.get(dateChoice - 1);

            List<String[]> timeSlots =
                    appointmentDao.getAvailableTimeSlots(
                            veterinarianId, selectedDate.toString()
                    );

            if (timeSlots.isEmpty()) {
                System.out.println("No time slots found for this date.");
                return;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("|               TIME SLOTS                   |");
            System.out.println("==============================================");
            System.out.printf("| %-8s | %-25s | %-12s |%n",
                    "OPTION", "TIME", "STATUS");
            System.out.println("+----------+---------------------------+--------------+");

            for (int i = 0; i < timeSlots.size(); i++) {
                String[] slot = timeSlots.get(i);
                System.out.printf("| %-8d | %-25s | %-12s |%n",
                        i + 1, slot[1], slot[2]);
            }

            System.out.println("+----------+---------------------------+--------------+");

            String[] selectedSlot;

            while (true) {
                System.out.print("Choose a time slot (0 to cancel): ");
                int timeChoice = readInt();

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

            System.out.println();
            System.out.print("Enter Reason for Appointment: ");
            String reason = scanner.nextLine().trim();

            if (reason.isEmpty()) {
                System.out.println("Reason cannot be empty.");
                return;
            }

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
                    petId, veterinarianId, selectedDate.toString(),
                    selectedTime, reason, user.userId()
            );

        } catch (SQLException e) {
            System.out.println("Error scheduling appointment: " + e.getMessage());
        }
    }

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
                        appointment.appointmentId(), appointment.petId(),
                        appointment.veterinarianId(), appointment.appointmentDate(),
                        appointment.appointmentTime(), appointment.status()
                );
                System.out.println("| Reason: " + appointment.reason());
            }

            System.out.println("+-------+---------+---------+--------------+----------+--------------+");

        } catch (SQLException e) {
            System.out.println("Error viewing appointments: " + e.getMessage());
        }
    }

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

            if (appointmentId <= 0) return;

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
                case "1": newStatus = "Scheduled"; break;
                case "2": newStatus = "Completed"; break;
                case "3": newStatus = "Cancelled"; break;
                default:
                    System.out.println("Invalid status choice.");
                    return;
            }

            appointmentDao.updateAppointmentStatus(
                    appointmentId, newStatus, user.userId()
            );

        } catch (SQLException e) {
            System.out.println("Error updating appointment: " + e.getMessage());
        }
    }

    // ==================== PAYMENTS ====================

    private void managePayments(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|             PAYMENTS               |");
            System.out.println("======================================");
            System.out.println("| 1. Record Payment                  |");
            System.out.println("| 2. View All Payments               |");
            System.out.println("| 3. Update Payment Status           |");
            System.out.println("| 4. Back to Staff Menu              |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": recordPayment(user); break;
                case "2": viewAllPayments(); break;
                case "3": updatePaymentStatus(user); break;
                case "4": return;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void recordPayment(User user) {
        try {
            List<String[]> appointments =
                    paymentDao.getCompletedUnpaidAppointments();

            if (appointments.isEmpty()) {
                System.out.println(
                        "No completed appointments are available for payment."
                );
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("|          COMPLETED APPOINTMENTS WITHOUT PAYMENT            |");
            System.out.println("==============================================================");
            System.out.printf("| %-5s | %-18s | %-15s | %-10s |%n",
                    "ID", "PET NAME", "DATE", "TIME");
            System.out.println("+-------+--------------------+-----------------+------------+");

            for (String[] appointment : appointments) {
                System.out.printf("| %-5s | %-18s | %-15s | %-10s |%n",
                        appointment[0], appointment[1],
                        appointment[2], appointment[3]);
                System.out.println("| Reason: " + appointment[4]);
            }

            System.out.println("+-------+--------------------+-----------------+------------+");

            System.out.print("Enter Appointment ID (0 to cancel): ");
            int appointmentId = readInt();

            if (appointmentId <= 0) return;

            String[] selectedAppointment = null;

            for (String[] appointment : appointments) {
                if (Integer.parseInt(appointment[0]) == appointmentId) {
                    selectedAppointment = appointment;
                    break;
                }
            }

            if (selectedAppointment == null) {
                System.out.println("Invalid Appointment ID.");
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|           PAYMENT DETAILS          |");
            System.out.println("======================================");
            System.out.println("| Appointment ID: " + appointmentId);
            System.out.println("| Pet Name: " + selectedAppointment[1]);
            System.out.println("| Date: " + selectedAppointment[2]);
            System.out.println("| Time: " + selectedAppointment[3]);
            System.out.println("======================================");

            double amount;

            while (true) {
                System.out.print("Enter Payment Amount: ");

                try {
                    amount = Double.parseDouble(scanner.nextLine().trim());

                    if (amount <= 0 || !Double.isFinite(amount)) {
                        System.out.println(
                                "Amount must be a valid number greater than zero."
                        );
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount. Please enter a number.");
                }
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|          PAYMENT STATUS            |");
            System.out.println("======================================");
            System.out.println("| 1. Paid                             |");
            System.out.println("| 2. Unpaid                           |");
            System.out.println("======================================");
            System.out.print("Choose payment status: ");

            String statusChoice = scanner.nextLine().trim();
            String paymentStatus;

            switch (statusChoice) {
                case "1": paymentStatus = "Paid"; break;
                case "2": paymentStatus = "Unpaid"; break;
                default:
                    System.out.println("Invalid payment status.");
                    return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|            PAYMENT SUMMARY         |");
            System.out.println("======================================");
            System.out.println("| Appointment ID: " + appointmentId);
            System.out.printf("| Amount: %.2f%n", amount);
            System.out.println("| Payment Status: " + paymentStatus);
            System.out.println("| Payment Date: " + LocalDate.now());
            System.out.println("======================================");
            System.out.print("Confirm payment record? (Y/N): ");

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Payment recording cancelled.");
                return;
            }

            int paymentId = paymentDao.recordPayment(
                    appointmentId, amount, paymentStatus, user.userId()
            );

            if ("Paid".equalsIgnoreCase(paymentStatus)) {
                String[] receipt = paymentDao.getPaymentReceipt(paymentId);
                displayPaymentReceipt(receipt);
            } else {
                System.out.println(
                        "Payment saved as Unpaid. No paid receipt was generated."
                );
            }

        } catch (SQLException e) {
            System.out.println("Error recording payment: " + e.getMessage());
        }
    }

    // Display the payment receipt
    private void displayPaymentReceipt(String[] receipt) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("|          PET CLINIC RECEIPT          |");
        System.out.println("========================================");
        System.out.println("| Payment ID: " + receipt[0]);
        System.out.println("| Appointment ID: " + receipt[1]);
        System.out.println("| Pet Name: " + receipt[2]);
        System.out.println("| Appointment Date: " + receipt[3]);
        System.out.println("| Appointment Time: " + receipt[4]);
        System.out.println("----------------------------------------");
        System.out.println("| Payment Date: " + receipt[6]);
        System.out.printf("| Amount Paid: PHP %s%n", receipt[5]);
        System.out.println("| Payment Status: " + receipt[7]);
        System.out.println("========================================");
        System.out.println("|       THANK YOU FOR YOUR VISIT!      |");
        System.out.println("========================================");
    }

    private void viewAllPayments() {
        try {
            List<String[]> payments = paymentDao.getAllPayments();

            if (payments.isEmpty()) {
                System.out.println("No payment records found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("|                    ALL PAYMENT RECORDS                     |");
            System.out.println("==============================================================");
            System.out.printf(
                    "| %-5s | %-12s | %-12s | %-15s | %-12s |%n",
                    "ID", "APPOINTMENT", "AMOUNT", "PAYMENT DATE", "STATUS"
            );
            System.out.println("+-------+--------------+--------------+-----------------+--------------+");

            for (String[] payment : payments) {
                System.out.printf(
                        "| %-5s | %-12s | %-12s | %-15s | %-12s |%n",
                        payment[0], payment[1], payment[2], payment[3], payment[4]
                );
            }

            System.out.println("+-------+--------------+--------------+-----------------+--------------+");

        } catch (SQLException e) {
            System.out.println("Error viewing payment records: " + e.getMessage());
        }
    }

    private void updatePaymentStatus(User user) {
        try {
            List<String[]> payments = paymentDao.getAllPayments();

            if (payments.isEmpty()) {
                System.out.println("No payment records available to update.");
                return;
            }

            viewAllPayments();

            System.out.print("Enter Payment ID (0 to cancel): ");
            int paymentId = readInt();

            if (paymentId <= 0) return;

            String previousStatus = null;

            for (String[] payment : payments) {
                if (Integer.parseInt(payment[0]) == paymentId) {
                    previousStatus = payment[4];
                    break;
                }
            }

            if (previousStatus == null) {
                System.out.println("Invalid Payment ID.");
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|        UPDATE PAYMENT STATUS       |");
            System.out.println("======================================");
            System.out.println("| 1. Paid                             |");
            System.out.println("| 2. Unpaid                           |");
            System.out.println("======================================");
            System.out.print("Choose new status: ");

            String choice = scanner.nextLine().trim();
            String newStatus;

            switch (choice) {
                case "1": newStatus = "Paid"; break;
                case "2": newStatus = "Unpaid"; break;
                default:
                    System.out.println("Invalid payment status.");
                    return;
            }

            paymentDao.updatePaymentStatus(
                    paymentId, newStatus, user.userId()
            );

            // Show the receipt only when the status changes to Paid.
            if ("Paid".equalsIgnoreCase(newStatus)
                    && !"Paid".equalsIgnoreCase(previousStatus)) {

                String[] receipt = paymentDao.getPaymentReceipt(paymentId);

                if (receipt != null) {
                    displayPaymentReceipt(receipt);
                } else {
                    System.out.println(
                            "Payment was updated, but the receipt could not be retrieved."
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error updating payment status: " + e.getMessage());
        }
    }

    // ==================== MEDICAL RECORDS ====================

    private void manageMedicalRecords(User user) {
        while (true) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("|        MANAGE MEDICAL RECORDS      |");
            System.out.println("======================================");
            System.out.println("| 1. Add Medical Record              |");
            System.out.println("| 2. View All Medical Records        |");
            System.out.println("| 3. Back to Staff Menu              |");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addMedicalRecord(user);
                    break;
                case "2":
                    viewAllMedicalRecords();
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addMedicalRecord(User user) {
        try {
            List<String[]> pets = appointmentDao.getAllPets();

            if (pets.isEmpty()) {
                System.out.println("No registered pets found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("|              SELECT A PET                  |");
            System.out.println("==============================================");
            System.out.printf("| %-8s | %-20s | %-15s |%n",
                    "PET ID", "PET NAME", "SPECIES");
            System.out.println("+----------+----------------------+-----------------+");

            for (String[] pet : pets) {
                System.out.printf("| %-8s | %-20s | %-15s |%n",
                        pet[0], pet[1], pet[2]);
            }

            System.out.println("+----------+----------------------+-----------------+");

            System.out.print("Enter Pet ID (0 to cancel): ");
            int petId = readInt();

            if (petId <= 0) return;

            String petName = null;

            for (String[] pet : pets) {
                if (Integer.parseInt(pet[0]) == petId) {
                    petName = pet[1];
                    break;
                }
            }

            if (petName == null) {
                System.out.println("Invalid Pet ID.");
                return;
            }

            List<Appointment> appointments =
                    appointmentDao.getAllAppointments();

            List<Appointment> completedAppointments = new ArrayList<>();

            for (Appointment appointment : appointments) {
                if (appointment.petId() == petId
                        && "Completed".equalsIgnoreCase(appointment.status())) {
                    completedAppointments.add(appointment);
                }
            }

            if (completedAppointments.isEmpty()) {
                System.out.println(
                        "This pet has no completed appointments. "
                                + "A medical record can only be added to a completed appointment."
                );
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("|          COMPLETED APPOINTMENTS FOR THIS PET               |");
            System.out.println("==============================================================");
            System.out.printf(
                    "| %-5s | %-12s | %-12s | %-12s |%n",
                    "ID", "DATE", "TIME", "STATUS"
            );
            System.out.println("+-------+--------------+--------------+--------------+");

            for (Appointment appointment : completedAppointments) {
                System.out.printf(
                        "| %-5d | %-12s | %-12s | %-12s |%n",
                        appointment.appointmentId(),
                        appointment.appointmentDate(),
                        appointment.appointmentTime(),
                        appointment.status()
                );
            }

            System.out.println("+-------+--------------+--------------+--------------+");

            System.out.print("Enter Appointment ID (0 to cancel): ");
            int appointmentId = readInt();

            if (appointmentId <= 0) return;

            boolean appointmentExists = false;

            for (Appointment appointment : completedAppointments) {
                if (appointment.appointmentId() == appointmentId) {
                    appointmentExists = true;
                    break;
                }
            }

            if (!appointmentExists) {
                System.out.println(
                        "Invalid appointment ID. Choose a completed appointment for this pet."
                );
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|          MEDICAL INFORMATION       |");
            System.out.println("======================================");
            System.out.println("| Pet ID: " + petId);
            System.out.println("| Pet Name: " + petName);
            System.out.println("| Appointment ID: " + appointmentId);
            System.out.println("======================================");

            System.out.print("Enter Diagnosis: ");
            String diagnosis = scanner.nextLine().trim();

            System.out.print("Enter Treatment: ");
            String treatment = scanner.nextLine().trim();

            System.out.print("Enter Additional Notes: ");
            String notes = scanner.nextLine().trim();

            if (diagnosis.isEmpty() || treatment.isEmpty()) {
                System.out.println(
                        "Diagnosis and treatment cannot be empty."
                );
                return;
            }

            System.out.println();
            System.out.println("======================================");
            System.out.println("|          MEDICAL RECORD SUMMARY    |");
            System.out.println("======================================");
            System.out.println("| Pet Name: " + petName);
            System.out.println("| Pet ID: " + petId);
            System.out.println("| Appointment ID: " + appointmentId);
            System.out.println("| Diagnosis: " + diagnosis);
            System.out.println("| Treatment: " + treatment);
            System.out.println("| Notes: " + (notes.isEmpty() ? "None" : notes));
            System.out.println("======================================");
            System.out.print("Save this medical record? (Y/N): ");

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Medical record creation cancelled.");
                return;
            }

            medicalRecordDao.addMedicalRecord(
                    petId,
                    appointmentId,
                    diagnosis,
                    treatment,
                    notes,
                    user.userId()
            );

        } catch (SQLException e) {
            System.out.println(
                    "Error adding medical record: " + e.getMessage()
            );
        }
    }

    private void viewAllMedicalRecords() {
        try {
            List<MedicalRecord> records =
                    medicalRecordDao.getAllMedicalRecords();

            if (records.isEmpty()) {
                System.out.println("No medical records found.");
                return;
            }

            int[] widths = {5, 7, 13, 20, 25, 30};

            System.out.println();
            printMedicalRecordBorder(widths);
            System.out.println(
                    "| " + padRight("ID", widths[0])
                            + " | " + padRight("PET ID", widths[1])
                            + " | " + padRight("APPOINTMENT", widths[2])
                            + " | " + padRight("DIAGNOSIS", widths[3])
                            + " | " + padRight("TREATMENT", widths[4])
                            + " | " + padRight("NOTES", widths[5]) + " |"
            );
            printMedicalRecordBorder(widths);

            for (MedicalRecord record : records) {

                List<String> id = wrapText(
                        String.valueOf(record.recordId()), widths[0]);
                List<String> petId = wrapText(
                        String.valueOf(record.petId()), widths[1]);
                List<String> appointmentId = wrapText(
                        String.valueOf(record.appointmentId()), widths[2]);
                List<String> diagnosis = wrapText(
                        record.diagnosis(), widths[3]);
                List<String> treatment = wrapText(
                        record.treatment(), widths[4]);
                List<String> notes = wrapText(
                        record.notes() == null || record.notes().isBlank()
                                ? "None" : record.notes(),
                        widths[5]
                );

                int lines = Math.max(
                        Math.max(Math.max(id.size(), petId.size()),
                                Math.max(appointmentId.size(), diagnosis.size())),
                        Math.max(treatment.size(), notes.size())
                );

                for (int i = 0; i < lines; i++) {
                    System.out.println(
                            "| " + getWrappedLine(id, i, widths[0])
                                    + " | " + getWrappedLine(petId, i, widths[1])
                                    + " | " + getWrappedLine(appointmentId, i, widths[2])
                                    + " | " + getWrappedLine(diagnosis, i, widths[3])
                                    + " | " + getWrappedLine(treatment, i, widths[4])
                                    + " | " + getWrappedLine(notes, i, widths[5])
                                    + " |"
                    );
                }

                printMedicalRecordBorder(widths);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error viewing medical records: " + e.getMessage()
            );
        }
    }

    // Prints the table border using the same width as each column.
    private void printMedicalRecordBorder(int[] widths) {
        StringBuilder border = new StringBuilder("+");

        for (int width : widths) {
            border.append("-".repeat(width + 2)).append("+");
        }

        System.out.println(border);
    }

    // Wraps long text so it stays inside its assigned column.
    private List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            while (word.length() > width) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                lines.add(word.substring(0, width));
                word = word.substring(width);
            }

            if (currentLine.length() == 0) {
                currentLine.append(word);
            } else if (currentLine.length() + 1 + word.length() <= width) {
                currentLine.append(" ").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    // Gets a wrapped line and fills unused space to keep columns aligned.
    private String getWrappedLine(List<String> lines, int index, int width) {
        String value = index < lines.size() ? lines.get(index) : "";
        return padRight(value, width);
    }

    private String padRight(String text, int width) {
        if (text == null) {
            text = "";
        }

        if (text.length() > width) {
            text = text.substring(0, width);
        }

        return String.format("%-" + width + "s", text);
    }

    // ==================== INPUT ====================

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1;
        }
    }
}
