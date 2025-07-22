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
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Ping {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Path ORIGINAL_IMAGE = Paths.get("Ping.jpg");
    private static int x = 0;
    private static String lastKnownDate;
    private static String HOST_NAME;
    private static Path DIRECTORY;

    public static void main(String[] args) throws IOException {
        HOST_NAME = args[0];
        DIRECTORY = Files.createDirectories(Paths.get(HOST_NAME));
        if (Files.notExists(ORIGINAL_IMAGE)) {
            throw new RuntimeException("Отсутствует изначальное изображение");
        }



        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("программа завершается");
                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                })
        );
        scheduler.scheduleAtFixedRate(() -> {
                    boolean reachable = false;
                    try {
                        reachable = InetAddress.getByName(HOST_NAME).isReachable(4000);
                    } catch (IOException e) {
                        System.err.println("Network error occurs: " + e.getMessage());
                    }
                    drawPing(reachable, x);
                    x += 2;
                }
                , 0, 1, TimeUnit.MINUTES);
    }

    private static String getDate() {
        String currentDate = DATE_FORMAT.format(new Date());

        if (!currentDate.equals(lastKnownDate)) {
            x = 0;
            lastKnownDate = currentDate;
        }
        return lastKnownDate;
    }

    private static void drawPing(boolean reachable, int x) {
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
            g = modifiedImage.getGraphics();
            g.drawImage(originalImage, 0, 0, null);
            g.setColor(reachable ? Color.green : Color.red);
            g.fillRect(x, 0, 2, 557);

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