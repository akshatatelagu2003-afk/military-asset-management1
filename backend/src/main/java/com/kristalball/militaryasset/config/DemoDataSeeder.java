package com.kristalball.militaryasset.config;

import com.kristalball.militaryasset.entity.*;
import com.kristalball.militaryasset.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BaseRepository baseRepository;
    private final EquipmentRepository equipmentRepository;
    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, BaseRepository baseRepository,
                          EquipmentRepository equipmentRepository, PurchaseRepository purchaseRepository,
                          TransferRepository transferRepository, AssignmentRepository assignmentRepository,
                          ExpenditureRepository expenditureRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.baseRepository = baseRepository;
        this.equipmentRepository = equipmentRepository;
        this.purchaseRepository = purchaseRepository;
        this.transferRepository = transferRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenditureRepository = expenditureRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.beans.factory.annotation.Value("${demo.data.enabled}")
    private boolean demoEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoEnabled) {
            System.out.println("Demo data seeding is disabled.");
            return;
        }

        if (userRepository.findByEmail("admin@militaryasset.com").isPresent()) {
            System.out.println("Demo data already seeded.");
            return;
        }

        System.out.println("Seeding demo data...");

        // 1. Create Bases
        Base baseAlpha = createBase("Base Alpha", "Bengaluru");
        Base baseBravo = createBase("Base Bravo", "Mysuru");
        Base baseCharlie = createBase("Base Charlie", "Hubballi");

        // 2. Create Equipment
        Equipment armoredVehicle = createEquipment("Armored Vehicle", "VEHICLE", "Standard Armored Vehicle");
        Equipment rifle = createEquipment("Rifle", "WEAPON", "Assault Rifle");
        Equipment ammunition = createEquipment("Ammunition", "AMMUNITION", "Standard Ammunition");

        // 3. Create Users
        User admin = createUser("Admin User", "admin@militaryasset.com", "Admin@123", "ADMIN", null);
        User commander = createUser("Base Commander Alpha", "commander@militaryasset.com", "Commander@123", "BASE_COMMANDER", baseAlpha);
        User logisticsOfficer = createUser("Logistics Officer", "logistics@militaryasset.com", "Logistics@123", "LOGISTICS_OFFICER", baseAlpha);

        // 4. Create Transactions
        
        // Purchases
        createPurchase(baseAlpha, armoredVehicle, 50, LocalDate.now().minusDays(30), admin);
        createPurchase(baseAlpha, rifle, 500, LocalDate.now().minusDays(29), admin);
        createPurchase(baseAlpha, ammunition, 50000, LocalDate.now().minusDays(29), admin);
        
        createPurchase(baseBravo, armoredVehicle, 30, LocalDate.now().minusDays(25), admin);
        createPurchase(baseBravo, rifle, 300, LocalDate.now().minusDays(25), admin);
        
        // Transfers
        createTransfer(baseAlpha, baseCharlie, armoredVehicle, 5, LocalDate.now().minusDays(20), admin);
        createTransfer(baseAlpha, baseCharlie, rifle, 50, LocalDate.now().minusDays(20), admin);
        
        createTransfer(baseBravo, baseCharlie, armoredVehicle, 2, LocalDate.now().minusDays(15), admin);
        
        // Assignments
        createAssignment(baseAlpha, armoredVehicle, "Captain Smith", 2, LocalDate.now().minusDays(10), admin);
        createAssignment(baseAlpha, rifle, "Unit 1", 100, LocalDate.now().minusDays(9), admin);
        
        // Expenditures
        createExpenditure(baseAlpha, ammunition, 2000, "Training Exercise Alpha", LocalDate.now().minusDays(5), admin);
        createExpenditure(baseBravo, ammunition, 500, "Training Exercise Bravo", LocalDate.now().minusDays(4), admin);

        System.out.println("Demo data seeding completed.");
    }

    private Base createBase(String name, String location) {
        Optional<Base> existing = baseRepository.findByName(name);
        if (existing.isPresent()) return existing.get();
        Base base = new Base();
        base.setName(name);
        base.setLocation(location);
        return baseRepository.save(base);
    }

    private Equipment createEquipment(String name, String type, String desc) {
        Optional<Equipment> existing = equipmentRepository.findByName(name);
        if (existing.isPresent()) return existing.get();
        Equipment eq = new Equipment();
        eq.setName(name);
        eq.setType(type);
        eq.setDescription(desc);
        return equipmentRepository.save(eq);
    }

    private User createUser(String name, String email, String password, String role, Base base) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) return existing.get();
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBase(base);
        return userRepository.save(user);
    }

    private void createPurchase(Base base, Equipment equipment, int quantity, LocalDate date, User user) {
        Purchase p = new Purchase();
        p.setBase(base);
        p.setEquipment(equipment);
        p.setQuantity(quantity);
        p.setPurchaseDate(date);
        p.setCreatedBy(user);
        purchaseRepository.save(p);
    }

    private void createTransfer(Base from, Base to, Equipment equipment, int quantity, LocalDate date, User user) {
        Transfer t = new Transfer();
        t.setFromBase(from);
        t.setToBase(to);
        t.setEquipment(equipment);
        t.setQuantity(quantity);
        t.setTransferDate(date);
        t.setCreatedBy(user);
        transferRepository.save(t);
    }

    private void createAssignment(Base base, Equipment equipment, String person, int quantity, LocalDate date, User user) {
        Assignment a = new Assignment();
        a.setBase(base);
        a.setEquipment(equipment);
        a.setPersonnelName(person);
        a.setQuantity(quantity);
        a.setAssignedDate(date);
        a.setCreatedBy(user);
        assignmentRepository.save(a);
    }

    private void createExpenditure(Base base, Equipment equipment, int quantity, String reason, LocalDate date, User user) {
        Expenditure e = new Expenditure();
        e.setBase(base);
        e.setEquipment(equipment);
        e.setQuantity(quantity);
        e.setReason(reason);
        e.setExpendedDate(date);
        e.setCreatedBy(user);
        expenditureRepository.save(e);
    }
}
