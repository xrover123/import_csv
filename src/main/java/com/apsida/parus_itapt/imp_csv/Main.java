package com.apsida.parus_itapt.imp_csv;
import java.nio.file.*;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Import to Parus has started!");
        Init ini = new Init("imp.ini");
        long pollingInterval = ini.getPollingInterval()*1000;
        long waitingTime = ini.getWaitingTime()*1000;
        String currFile = "";
        boolean next = true;
        for (FileName file: ini.getFiles()){
            currFile = file.fullName();
            Path source = Paths.get(currFile);
            long bgnTime = System.currentTimeMillis();
            do {
                if ((Files.exists(source))) {
                    Path destination = Paths.get(ini.getTmpDir()+source.getFileName().toString());
                    try {
                        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("file \""+file.shortName()+"\" moved successfully!");
                        break;
                    } catch (IOException e) {
                        if (System.currentTimeMillis()-bgnTime >= waitingTime) {
                            System.out.println("@");
                            next = false;
                        } else {
                            System.out.printf("-");
                            Thread.sleep(pollingInterval);
                        }
                    }
                    System.out.println("The \""+file.fullName()+"\" file moved to a temporary directory.");
                } else {
                    if (System.currentTimeMillis()-bgnTime >= waitingTime) {
                        System.out.println("#");
                        next = false;
                    } else {
                        System.out.printf("+");
                        Thread.sleep(pollingInterval);
                    }
                }
            } while (next);
            if (!next) {
                break;
            }
        }
        if (next) {
            System.out.printf("\nConnecting the \""+ini.getDb().dbName()+"\" database.\n");
        } else {
            System.out.printf("\nFile \""+currFile+"\" movement error to the temporary directory.\n");
        }
    }
}
