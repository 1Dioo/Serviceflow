package com.serviceflow.controller; 
import com.serviceflow.model.Appointment; 
 
import javafx.fxml.FXML; 
import javafx.fxml.FXMLLoader; 
import javafx.scene.Parent; 
import javafx.scene.Scene; 
import javafx.scene.control.Label; 
import javafx.scene.layout.HBox; 
import javafx.scene.layout.Region; 
import javafx.scene.layout.VBox; 
import javafx.stage.Modality; 
import javafx.stage.Stage; 
 
import java.io.IOException; 
import java.time.LocalDate; 
import java.time.LocalTime; 
import java.time.format.DateTimeFormatter; 
import java.time.format.TextStyle; 
import java.util.ArrayList; 
import java.util.List; 
import java.util.Locale; 
 
public class AgendaController { 
    @FXML  
    private Label dateLabel; 
 
    @FXML  
    private Label dayLabel; 
 
    @FXML  
    private VBox scheduleContainer; 
 
    private LocalDate selectedDate = LocalDate.now(); 
 
    private final List<Appointment> appointments = new ArrayList<>(); 
 
    private final DateTimeFormatter dateFormatter = 
        DateTimeFormatter.ofPattern( 
            "dd 'de' MMMM 'de' yyyy", 
            new Locale("pt", "BR") 
        ); 
     
    @FXML  
    private void initialize() { 
        addExampleAppointments(); 
        updateDate(); 
        renderSchedule(); 
    } 
 
    @FXML  
    private void handlePreviousDay() { 
        selectedDate = selectedDate.minusDays(1); 
        updateDate(); 
        renderSchedule(); 
    } 
 
    @FXML  
    private void handleNextDay() { 
        selectedDate = selectedDate.plusDays(1); 
        updateDate(); 
        renderSchedule(); 
    } 
 
    @FXML  
    private void handleToday() { 
        selectedDate = LocalDate.now(); 
        updateDate(); 
        renderSchedule(); 
    } 
 
    @FXML  
    private void handleNewAppointment() { 
        try { 
            FXMLLoader loader = new FXMLLoader( 
                getClass().getResource( 
                    "/com/serviceflow/view/NewAppointmentView.fxml" 
                ) 
            ); 
 
            Parent root = loader.load(); 
            NewAppointmentController controller = loader.getController(); 
            controller.setSelectedDate(selectedDate); 
 
            controller.setOnAppointmentSaved( 
                this::addAppointment 
            ); 
 
            Stage stage = new Stage(); 
            stage.setTitle("Novo agendamento"); 
            stage.setScene( 
                new Scene(root, 420, 570) 
            ); 
 
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.setResizable(false); 
            stage.showAndWait(); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    } 
 
    private void addAppointment(Appointment appointment) { 
        appointments.add(appointment); 
        renderSchedule(); 
    } 
 
    private void updateDate() { 
        dateLabel.setText( 
            selectedDate.format(dateFormatter) 
        ); 
 
        String day = selectedDate 
            .getDayOfWeek() 
            .getDisplayName(TextStyle.FULL, 
                new Locale("pt", "BR") 
            ); 
         
        day = Character.toUpperCase(day.charAt(0)) + day.substring(1); 
        dayLabel.setText(day); 
    } 
 
    private void renderSchedule() { 
        scheduleContainer.getChildren().clear(); 
 
        for (int hour = 8; hour <= 18; hour++) { 
            addTimeRow(hour, 0); 
 
            if (hour < 18) { 
                addTimeRow(hour, 30); 
            } 
        } 
    } 
 
    private void addTimeRow(int hour, int minute) { 
        HBox row = new HBox(); 
 
        row.getStyleClass().add("time-row"); 
        row.setMinHeight(65); 
        Label timeLabel = new Label( 
            String.format("%02d:%02d", hour, minute) 
        ); 
 
        timeLabel.getStyleClass().add("time-label"); 
        row.getChildren().add(timeLabel); 
 
        Region separator = new Region(); 
        separator.getStyleClass().add("time-separator"); 
        row.getChildren().add(separator); 
 
        Appointment appointment = findAppointment(hour, minute); 
        if (appointment != null) { 
            VBox appointmentBlock = createAppointmentBlock(appointment); 
 
            row.getChildren().add(appointmentBlock); 
        } else { 
            Region emptySpace = new Region(); 
 
            HBox.setHgrow( 
                emptySpace, 
                javafx.scene.layout.Priority.ALWAYS 
            ); 
 
            row.getChildren().add(emptySpace); 
        } 
        scheduleContainer.getChildren().add(row); 
    } 
 
    private Appointment findAppointment(int hour, int minute) { 
        LocalTime time = LocalTime.of(hour, minute); 
 
        for (Appointment appointment : appointments) { 
            if (appointment.getDate().equals(selectedDate) 
                    && appointment.getTime().equals(time)) { 
                return appointment; 
            } 
        } 
        return null; 
    } 
 
    private VBox createAppointmentBlock(Appointment appointment) { 
        VBox block = new VBox(); 
 
        block.getStyleClass().add("appointment-block"); 
        block.setSpacing(3); 
 
        Label client = new Label(appointment.getCliente()); 
        client.getStyleClass().add("agenda-client"); 
 
        Label service = new Label(appointment.getService()); 
        service.getStyleClass().add("agenda-service"); 
 
        Label status = new Label(appointment.getStatus()); 
         
        if ("Confirmado".equals(appointment.getStatus())) { 
            status.getStyleClass().add("status-confirmed"); 
        } else { 
            status.getStyleClass().add("status-pending"); 
        } 
 
        block.getChildren().addAll( 
            client, 
            service, 
            status 
        ); 
 
        HBox.setHgrow( 
            block, 
            javafx.scene.layout.Priority.ALWAYS 
        ); 
 
        return block; 
    } 
 
    private void addExampleAppointments() { 
        appointments.add( 
            new Appointment( 
                LocalDate.now(), 
                LocalTime.of(9, 0), 
                "Ana Silva", 
                "Corte de cabelo", 
                "Confirmado" 
            ) 
        ); 
 
        appointments.add( 
            new Appointment( 
                LocalDate.now(), 
                LocalTime.of(11, 0), 
                "Bruno Oliveira", 
                "Manutenção", 
                "Pendente" 
            ) 
        ); 
 
        appointments.add( 
            new Appointment( 
                LocalDate.now(), 
                LocalTime.of(14, 0), 
                "Carlos Eduardo", 
                "Corte Masculino", 
                "Confirmado" 
            ) 
        ); 
    } 
} 