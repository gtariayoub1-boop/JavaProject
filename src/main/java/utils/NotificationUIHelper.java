package utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.util.Duration;
import services.NotificationService;
import java.util.List;

public class NotificationUIHelper {
    public static void setupNotificationMenu(MenuButton notificationMenuButton) {
        if (notificationMenuButton == null) return;
        NotificationService notificationService = new NotificationService();
        
        Runnable updateNotifications = new Runnable() {
            @Override
            public void run() {
                try {
                    entities.Utilisateur user = UserSession.getCurrentUser();
                    if (user != null) {
                        int count = notificationService.countUnreadNotifications(user.getId());
                        notificationMenuButton.setText("🔔 (" + count + ")");
                        notificationMenuButton.getItems().clear();
                        
                        List<entities.Notification> notifs = notificationService.getNotificationsForUser(user.getId());
                        if (notifs.isEmpty()) {
                            notificationMenuButton.getItems().add(new MenuItem("Aucune notification"));
                        } else {
                            for (entities.Notification n : notifs) {
                                MenuItem item = new MenuItem(n.getTitre() + " - " + n.getContenu());
                                if (!n.isEstLu()) {
                                    item.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8;");
                                }
                                item.setOnAction(e -> {
                                    try {
                                        notificationService.markAsRead(n.getId());
                                        this.run(); // trigger refresh
                                    } catch (Exception ex) {}
                                });
                                notificationMenuButton.getItems().add(item);
                            }
                            MenuItem readAll = new MenuItem("Tout marquer comme lu");
                            readAll.setOnAction(e -> {
                                try {
                                    notificationService.markAllAsReadForUser(user.getId());
                                    this.run(); // trigger refresh
                                } catch (Exception ex) {}
                            });
                            notificationMenuButton.getItems().add(new SeparatorMenuItem());
                            notificationMenuButton.getItems().add(readAll);
                        }
                    }
                } catch (Exception e) {}
            }
        };

        updateNotifications.run();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> updateNotifications.run()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
