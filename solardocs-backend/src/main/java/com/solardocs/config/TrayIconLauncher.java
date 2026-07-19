package com.solardocs.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

@Component
public class TrayIconLauncher {

    @PostConstruct
    public void setupTray() {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/tray-icon.png"));
            PopupMenu popup = new PopupMenu();

            MenuItem open = new MenuItem("Open SolarDocs");
            open.addActionListener(e -> openBrowser());
            popup.add(open);

            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(e -> System.exit(0));
            popup.add(exit);

            TrayIcon trayIcon = new TrayIcon(image, "SolarDocs", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> openBrowser());
            tray.add(trayIcon);

            openBrowser();
        } catch (Exception e) {
            // tray not critical to app function — log and continue
        }
    }

    private void openBrowser() {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI("http://localhost:8080"));
        } catch (Exception ignored) {}
    }
}
