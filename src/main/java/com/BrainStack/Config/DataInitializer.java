package com.BrainStack.Config;

import com.BrainStack.Entity.Appointment;
import com.BrainStack.Entity.User;
import com.BrainStack.Entity.Child;
import com.BrainStack.Enums.AppointmentStatus;
import com.BrainStack.Enums.AppointmentType;
import com.BrainStack.Repository.AppointmentRepository;
import com.BrainStack.Repository.UserRepository;
import com.BrainStack.Repository.ChildRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedAppointments(AppointmentRepository appointmentRepository,
                                             UserRepository userRepository,
                                             ChildRepository childRepository) {
        return args -> {
            long count = appointmentRepository.count();
            if (count > 0) {
                log.info("Skipping seed: {} appointments already present", count);
                return;
            }

            log.info("Seeding demo users (parents) and children...");

            // Parents
            User parentLeila = User.builder()
                    .firstName("Leila").lastName("Ben Ali")
                    .email("leila.parent@example.com")
                    .password("password")
                    .phoneNumber("+21620000001")
                    .address("Tunis")
                    .build();

            User parentAhmed = User.builder()
                    .firstName("Ahmed").lastName("Mansour")
                    .email("ahmed.parent@example.com")
                    .password("password")
                    .phoneNumber("+21620000002")
                    .address("Sfax")
                    .build();

            parentLeila = userRepository.save(parentLeila);
            parentAhmed = userRepository.save(parentAhmed);

            // Children
            Child childAmine = Child.builder()
                    .fullName("Amine Ben Ali")
                    .birthDate(java.time.LocalDate.now().minusYears(8))
                    .gender("M")
                    .diagnosis("Trouble du langage")
                    .parent(parentLeila)
                    .build();

            Child childSarah = Child.builder()
                    .fullName("Sarah Mansour")
                    .birthDate(java.time.LocalDate.now().minusYears(6))
                    .gender("F")
                    .diagnosis("Autisme")
                    .parent(parentAhmed)
                    .build();

            Child childYoussef = Child.builder()
                    .fullName("Youssef Mansour")
                    .birthDate(java.time.LocalDate.now().minusYears(10))
                    .gender("M")
                    .diagnosis("TDAH")
                    .parent(parentAhmed)
                    .build();

            childAmine = childRepository.save(childAmine);
            childSarah = childRepository.save(childSarah);
            childYoussef = childRepository.save(childYoussef);

            log.info("Seeded parents ids: {}, {} | children ids: {}, {}, {}",
                    parentLeila.getId(), parentAhmed.getId(), childAmine.getId(), childSarah.getId(), childYoussef.getId());

            log.info("Seeding demo appointments...");

            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

            Appointment a1 = Appointment.builder()
                    .title("Consultation Pédiatre")
                    .description("Contrôle de routine")
                    .startTime(now.plusDays(5).withHour(14).withMinute(0))
                    .endTime(now.plusDays(5).withHour(14).withMinute(30))
                    .type(AppointmentType.MEDICAL)
                    .status(AppointmentStatus.CONFIRMED)
                    .parentId((long) parentLeila.getId())
                    .professionalId(2L)
                    .childId((long) childAmine.getId())
                    .location("Cabinet Médical - 123 Rue de la Paix, Tunis")
                    .notes("Prévoir le carnet de santé")
                    .notificationSent(false)
                    .build();

            Appointment a2 = Appointment.builder()
                    .title("Séance Orthophoniste")
                    .description("Travail articulation")
                    .startTime(now.plusDays(7).withHour(10).withMinute(0))
                    .endTime(now.plusDays(7).withHour(10).withMinute(30))
                    .type(AppointmentType.THERAPEUTIC)
                    .status(AppointmentStatus.PENDING)
                    .parentId((long) parentLeila.getId())
                    .professionalId(4L)
                    .childId((long) childAmine.getId())
                    .location("Centre Spécialisé, Tunis")
                    .notes("Apporter exercices")
                    .notificationSent(false)
                    .build();

            Appointment a3 = Appointment.builder()
                    .title("Suivi Éducatif")
                    .description("Point avec l'éducateur spécialisé")
                    .startTime(now.minusDays(2).withHour(9).withMinute(0))
                    .endTime(now.minusDays(2).withHour(9).withMinute(45))
                    .type(AppointmentType.EDUCATIONAL)
                    .status(AppointmentStatus.COMPLETED)
                    .parentId((long) parentLeila.getId())
                    .professionalId(5L)
                    .childId((long) childAmine.getId())
                    .location("École Inclusive, La Marsa")
                    .notes("Envoyer le rapport à l'école")
                    .notificationSent(true)
                    .build();

            // === CAS 2 : Papa Ahmed gère 2 enfants ===
            // Sarah (6 ans, autisme) - ID: 4
            // Youssef (10 ans, TDAH) - ID: 5
            // Parent Ahmed - ID: 2

            // RDV pour Sarah (🔵) - Autisme
            Appointment sarah1 = Appointment.builder()
                    .title("Séance ABA - Sarah")
                    .description("Thérapie comportementale intensive")
                    .startTime(now.plusDays(3).withHour(9).withMinute(0))
                    .endTime(now.plusDays(3).withHour(10).withMinute(30))
                    .type(AppointmentType.THERAPEUTIC)
                    .status(AppointmentStatus.CONFIRMED)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(6L) // Psychologue ABA
                    .childId((long) childSarah.getId()) // Sarah
                    .location("Centre ABA Tunis, Avenue Habib Bourguiba")
                    .notes("Apporter le carnet de communication")
                    .notificationSent(false)
                    .build();

            Appointment sarah2 = Appointment.builder()
                    .title("Orthophonie - Sarah")
                    .description("Travail sur la communication non-verbale")
                    .startTime(now.plusDays(5).withHour(14).withMinute(0))
                    .endTime(now.plusDays(5).withHour(14).withMinute(45))
                    .type(AppointmentType.THERAPEUTIC)
                    .status(AppointmentStatus.PENDING)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(7L) // Orthophoniste spécialisée
                    .childId((long) childSarah.getId()) // Sarah
                    .location("Cabinet Orthophonie, Sfax")
                    .notes("Prévoir les pictogrammes")
                    .notificationSent(false)
                    .build();

            // RDV pour Youssef (🟢) - TDAH
            Appointment youssef1 = Appointment.builder()
                    .title("Consultation Neurologique - Youssef")
                    .description("Suivi médicamenteux TDAH")
                    .startTime(now.plusDays(4).withHour(10).withMinute(0))
                    .endTime(now.plusDays(4).withHour(10).withMinute(30))
                    .type(AppointmentType.MEDICAL)
                    .status(AppointmentStatus.CONFIRMED)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(8L) // Neurologue pédiatre
                    .childId((long) childYoussef.getId()) // Youssef
                    .location("Hôpital Charles Nicolle, Tunis")
                    .notes("Apporter les dernières analyses")
                    .notificationSent(false)
                    .build();

            Appointment youssef2 = Appointment.builder()
                    .title("Psychomotricité - Youssef")
                    .description("Travail sur la concentration et l'impulsivité")
                    .startTime(now.plusDays(6).withHour(15).withMinute(0))
                    .endTime(now.plusDays(6).withHour(15).withMinute(45))
                    .type(AppointmentType.THERAPEUTIC)
                    .status(AppointmentStatus.CONFIRMED)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(9L) // Psychomotricien
                    .childId((long) childYoussef.getId()) // Youssef
                    .location("Centre de Rééducation, Monastir")
                    .notes("Vêtements confortables requis")
                    .notificationSent(false)
                    .build();

            // === SCÉNARIO DE CONFLIT ===
            // Deux RDV simultanés pour tester la détection de conflit
            Appointment conflict1 = Appointment.builder()
                    .title("RDV Conflit 1 - Sarah")
                    .description("Test de détection de conflit")
                    .startTime(now.plusDays(8).withHour(14).withMinute(0))
                    .endTime(now.plusDays(8).withHour(14).withMinute(30))
                    .type(AppointmentType.MEDICAL)
                    .status(AppointmentStatus.PENDING)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(10L)
                    .childId((long) childSarah.getId()) // Sarah
                    .location("Cabinet Test 1")
                    .notes("⚠️ CONFLIT POTENTIEL")
                    .notificationSent(false)
                    .build();

            Appointment conflict2 = Appointment.builder()
                    .title("RDV Conflit 2 - Youssef")
                    .description("Test de détection de conflit simultané")
                    .startTime(now.plusDays(8).withHour(14).withMinute(0)) // Même heure !
                    .endTime(now.plusDays(8).withHour(14).withMinute(30))
                    .type(AppointmentType.THERAPEUTIC)
                    .status(AppointmentStatus.PENDING)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(11L)
                    .childId((long) childYoussef.getId()) // Youssef
                    .location("Cabinet Test 2")
                    .notes("⚠️ CONFLIT POTENTIEL - Même parent, même heure")
                    .notificationSent(false)
                    .build();

            // RDV passés pour Sarah
            Appointment sarahPast = Appointment.builder()
                    .title("Évaluation Autisme - Sarah")
                    .description("Bilan complet des compétences")
                    .startTime(now.minusDays(5).withHour(9).withMinute(0))
                    .endTime(now.minusDays(5).withHour(11).withMinute(0))
                    .type(AppointmentType.MEDICAL)
                    .status(AppointmentStatus.COMPLETED)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(12L)
                    .childId((long) childSarah.getId()) // Sarah
                    .location("Centre d'Évaluation, Tunis")
                    .notes("Rapport disponible")
                    .notificationSent(true)
                    .build();

            // RDV passés pour Youssef
            Appointment youssefPast = Appointment.builder()
                    .title("Suivi TDAH - Youssef")
                    .description("Ajustement du traitement")
                    .startTime(now.minusDays(3).withHour(16).withMinute(0))
                    .endTime(now.minusDays(3).withHour(16).withMinute(30))
                    .type(AppointmentType.MEDICAL)
                    .status(AppointmentStatus.COMPLETED)
                    .parentId((long) parentAhmed.getId()) // Papa Ahmed
                    .professionalId(13L)
                    .childId((long) childYoussef.getId()) // Youssef
                    .location("Cabinet Neurologie, Sousse")
                    .notes("Prescription renouvelée")
                    .notificationSent(true)
                    .build();

            appointmentRepository.saveAll(List.of(a1, a2, a3, sarah1, sarah2, youssef1, youssef2, 
                    conflict1, conflict2, sarahPast, youssefPast));
            log.info("Seeded {} demo appointments", appointmentRepository.count());
        };
    }
}


