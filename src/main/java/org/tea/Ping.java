package org.tea;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Ping {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Path ORIGINAL_IMAGE = Paths.get("Ping.jpg");
    private static String lastKnownDate;
    private static String HOST_NAME;
    private static Path DIRECTORY;
    private static ScheduledExecutorService scheduler;

    public Ping() {
    }

    public static void start(String hostName) {
        HOST_NAME = hostName;
        System.out.println("Начал работу");
        try {
            DIRECTORY = Files.createDirectories(Paths.get(HOST_NAME));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку", e);
        }

        if (Files.notExists(ORIGINAL_IMAGE)) {
            throw new RuntimeException("Отсутствует изначальное изображение");
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();


        scheduler.scheduleAtFixedRate(() -> {
                    boolean reachable = pingIsReachable();
                    drawPing(reachable);
                }
                , 0, 1, TimeUnit.MINUTES);

    }

    private static boolean pingIsReachable() {
        boolean reachable = false;
        try {
            String currentTime = LocalTime.now().format(TIME_FORMATTER);
            long start = System.currentTimeMillis();
            reachable = InetAddress.getByName(HOST_NAME).isReachable(4000);
            long end = System.currentTimeMillis();
            Window.getInstance().addLog(currentTime + " | " + "Ответ от " + HOST_NAME + " " + reachable + " за " + (end - start) + "мс");
        } catch (IOException e) {
            System.err.println("Network error occurs: " + e.getMessage());
        }
        return reachable;
    }

    public static void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        System.out.println("Ping остановлен");
    }

    private static String getDate() {
        String currentDate = DATE_FORMAT.format(new Date());

        if (!currentDate.equals(lastKnownDate)) {
            lastKnownDate = currentDate;
        }
        return lastKnownDate;
    }

    private static void drawPing(boolean reachable) {
        Graphics g = null;
        BufferedImage originalImage = null;
        BufferedImage modifiedImage = null;
        try {
            String currentDate = getDate();

            Path targetPath = DIRECTORY.resolve(currentDate + ".jpg");
            Path copyFromPath = Files.exists(targetPath) ? targetPath : ORIGINAL_IMAGE;


            originalImage = ImageIO.read(new File(copyFromPath.toString()));

            modifiedImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    originalImage.getType()
            );
            LocalTime now = LocalTime.now();
            g = modifiedImage.getGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.setColor(reachable ? Color.green : Color.red);
            g.fillRect((now.getHour() * 60 + now.getMinute()) * 2, 0, 2, 557);
            Window.getInstance().drawImage(modifiedImage);
            saveModifiedImage(modifiedImage, targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Проблема с чтением изображения", e);
        } finally {
            if (g != null) {
                g.dispose();
            }
            if (originalImage != null) {
                originalImage.flush();
            }
            if (modifiedImage != null) {
                modifiedImage.flush();
            }
        }
    }

    private static void saveModifiedImage(BufferedImage modifiedImage, Path targetPath) {
        try (OutputStream out = Files.newOutputStream(targetPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ImageIO.write(modifiedImage, "JPG", out);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить изображение " + e.getMessage());
            Runtime.getRuntime().halt(1);
        } finally {
            modifiedImage.flush();
        }
    }
}