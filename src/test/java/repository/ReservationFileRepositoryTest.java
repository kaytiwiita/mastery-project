package repository;

import models.Host;
import models.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.core.Local;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationFileRepositoryTest {

    private static final String SEED_FILE = "./data/reservations-seed-file.csv";
    private static final String TEST_FILE = "./data/reservations-test-folder/2e72f86c-b8fe-4265-b4f1-304dea8762db.csv";
    private static final String TEST_DIRECTORY_PATH = "./data/reservations-test-folder";

    private final String testHostId = "2e72f86c-b8fe-4265-b4f1-304dea8762db";

    ReservationFileRepository repository = new ReservationFileRepository(TEST_DIRECTORY_PATH);

    @BeforeEach
    void setUp() throws IOException {
        Path seedPath = Paths.get(SEED_FILE);
        Path testPath = Paths.get(TEST_FILE);

        Files.copy(seedPath, testPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void shouldReturnAllReservationIfValidId() throws DataAccessException {
        List<Reservation> actual = repository.findById(testHostId);
        assertEquals(3, actual.size());
    }

    @Test
    void shouldNotReturnAnyReservationsIfInvalid() throws DataAccessException {
        List<Reservation> actual = repository.findById("Test Id");
        assertEquals(0, actual.size());

    }

    @Test
    void shouldNotReturnAnyReservationIfIdIsNullOrBlank() throws DataAccessException {
        List<Reservation> actual = repository.findById(null);
        assertEquals(0, actual.size());

        actual = repository.findById(" ");
        assertEquals(0, actual.size());

        actual = repository.findById("");
        assertEquals(0, actual.size());
    }

    @Test
    void shouldAddValidReservation() throws DataAccessException {
        List<Reservation> all = repository.findById(testHostId);
        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.of(2022, 10, 4));
        reservation.setEndDate(LocalDate.of(2022, 10, 6));
        Host host = new Host();
        host.setId(testHostId);
        reservation.setHost(host);
        reservation.setGuest(GuestRepositoryDouble.GUEST);
        reservation.setTotal(new BigDecimal("400.00"));
        reservation = repository.add(reservation);

        List<Reservation> actual = repository.findById(testHostId);

        assertNotNull(reservation);
        assertEquals(all.size() + 1, actual.size());
    }

    @Test
    void shouldNotMakeReservationIfNull() throws DataAccessException {
        List<Reservation> all = repository.findById(testHostId);
        Reservation reservation = repository.add(null);
        List<Reservation> actual = repository.findById(testHostId);

        assertNull(reservation);
        assertEquals(all.size(), actual.size());
    }

    @Test
    void shouldReturnMakeNewFileIfNoPreviousReservations() throws DataAccessException {
        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.of(2022, 10, 4));
        reservation.setEndDate(LocalDate.of(2022, 10, 6));
        Host host = new Host();
        host.setId("Test Id");
        reservation.setHost(host);
        reservation.setGuest(GuestRepositoryDouble.GUEST);
        reservation.setTotal(new BigDecimal("400.00"));
        reservation = repository.add(reservation);

        assertNotNull(reservation);
    }

    @Test
    void shouldNotMakeReservationIfNullFieldsInReservation() throws DataAccessException {
        Reservation reservation = new Reservation();
        assertNull(repository.add(reservation));
    }

}