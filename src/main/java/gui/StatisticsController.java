package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import services.EvenementService;
import services.ParticipationEvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class StatisticsController {

    @FXML
    private PieChart typeChart;
    @FXML
    private BarChart<String, Integer> participationChart;

    private EvenementService es = new EvenementService();
    private ParticipationEvenementService ps = new ParticipationEvenementService();

    @FXML
    public void initialize() {
        loadTypeStats();
        loadParticipationStats();
    }

    private void loadTypeStats() {
        try {
            Map<String, Integer> stats = es.getEventCountByType();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            stats.forEach((type, count) -> pieChartData.add(new PieChart.Data(type + " (" + count + ")", count)));
            typeChart.setData(pieChartData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadParticipationStats() {
        try {
            Map<String, Integer> stats = ps.getParticipationCountPerEvent();
            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            series.setName("Participants");
            // Data retrieval for series
            stats.forEach((titre, count) -> series.getData().add(new XYChart.Data<>(titre, count)));
            
            participationChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleRetour(ActionEvent event) {
        ((Stage) typeChart.getScene().getWindow()).close();
    }
}
