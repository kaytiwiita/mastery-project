package ui;

import domain.GuestService;
import domain.HostService;
import domain.ReservationService;
import domain.Result;
import models.Guest;
import models.Host;
import models.MainMenu;
import models.Reservation;
import repository.DataAccessException;

import java.time.LocalDate;
import java.util.List;

public class Controller {

    private final View view;
    private final ReservationService reservationService;
    private final HostService hostService;
    private final GuestService guestService;

    public Controller(View view, ReservationService reservationService, HostService hostService, GuestService guestService) {
        this.view = view;
        this.reservationService = reservationService;
        this.hostService = hostService;
        this.guestService = guestService;
    }

    public void run() {
        view.displayHeader("Welcome");
        try {
            runAppLoop();
        } catch (DataAccessException ex) {
            view.displayException(ex);
        }
        view.displayHeader("Goodbye!");
    }

    public void runAppLoop() throws DataAccessException {
        MainMenu mainMenu;
        do {
            mainMenu = view.selectMainMenuOption();

            switch (mainMenu) {
                case VIEW_RESERVATIONS:
                    viewReservations();
                    break;

                case MAKE_RESERVATION:
                    makeReservation();
                    break;

                case EDIT_RESERVATION:
                    editReservation();
                    break;

                case DELETE_RESERVATION:
                    deleteReservation();
                    break;
            }
        } while (mainMenu != MainMenu.EXIT);
    }

    private void viewReservations() throws DataAccessException {
        view.displayHeader(MainMenu.VIEW_RESERVATIONS.getTitle());
        String hostEmail = view.chooseHost();
        Host host = hostService.findByEmail(hostEmail);
        List<Reservation> reservations = reservationService.findById(host.getId());
        view.displayReservations(host, reservations);

    }

    private void makeReservation() throws DataAccessException {
        view.displayHeader(MainMenu.MAKE_RESERVATION.getTitle());

        String hostEmail = view.chooseHost();
        Host host = hostService.findByEmail(hostEmail);

        String guestEmail = view.chooseGuest();
        Guest guest = guestService.findByEmail(guestEmail);

        List<Reservation> reservations = reservationService.findById(host.getId());
        view.displayReservations(host, reservations);

        LocalDate startDate = view.chooseStartDate();
        LocalDate endDate = view.chooseEndDate(startDate);

        Reservation reservation = new Reservation(host, guest, startDate, endDate);
        Result<Reservation> result = reservationService.isReservationAvailable(reservation);

        if (!result.isSuccess()) {
            view.displayStatus(false, result.getMessages());
            return;
        }

        boolean isGoingToBook = view.displaySummary(result.getPayload());
        if (!isGoingToBook) {
            return;
        }

        result = reservationService.addReservation(reservation);
        if (result.isSuccess()) {
            view.displayStatus(true, "Reservation was created.");
        } else {
            view.displayStatus(false, result.getMessages());
        }
    }

    private void editReservation() throws DataAccessException {
        view.displayHeader(MainMenu.EDIT_RESERVATION.getTitle());
        Reservation reservation = getReservation();

        reservation = view.editReservation(reservation);
        Result<Reservation> result = reservationService.isReservationAvailable(reservation);

        if (!result.isSuccess()) {
            view.displayStatus(false, result.getMessages());
            return;
        }

        boolean isGoingToBook = view.displaySummary(result.getPayload());
        if (!isGoingToBook) {
            return;
        }

        result = reservationService.updateReservation(reservation);
        if (result.isSuccess()) {
            view.displayStatus(true, "Reservation was updated.");
        } else {
            view.displayStatus(false, result.getMessages());
        }
    }

    private void deleteReservation() throws DataAccessException {
        view.displayHeader(MainMenu.DELETE_RESERVATION.getTitle());
        Reservation reservation = getReservation();
        boolean isGoingToDelete = view.chooseToDelete(reservation);

        if (!isGoingToDelete) {
            return;
        }

        Result<Reservation> result = reservationService.deleteReservationById(reservation);

    }

    //support methods
    private Reservation getReservation() throws DataAccessException {
        String hostEmail = view.chooseHost();
        Host host = hostService.findByEmail(hostEmail);

        String guestEmail = view.chooseGuest();
        Guest guest = guestService.findByEmail(guestEmail);

        List<Reservation> reservations = reservationService.findById(host.getId(), guest.getId());
        view.displayReservations(host, reservations);

        Reservation reservation = view.chooseReservation(reservations);
        return reservationService.findByReservationId(host.getId(), reservation.getReservationId());
    }
}
