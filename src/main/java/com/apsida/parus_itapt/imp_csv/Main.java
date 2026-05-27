package com.apsida.parus_itapt.imp_csv;
import java.nio.file.*;
import java.io.IOException;
public class Main {
    static String iniFileName = "imp.ini";
    public static void main(String[] args) throws InterruptedException {
        //✅
        System.out.println("Import to Parus has started!");
        {
            Path iniPath = Paths.get(iniFileName);
            if (!Files.exists(iniPath)) {
                iniPath = iniPath.toAbsolutePath();
                System.err.println("❌ IniFile \""+iniPath.toString()+"\" not found.");
                return;
            }
        }
        Init ini = new Init("imp.ini");
        long pollingInterval = ini.getPollingInterval()*1000;
        long waitingTime = ini.getWaitingTime()*1000;
        String currFile = "";
        boolean next = true;
        OraConnect ora = new OraConnect(
                                ini.db.dbIP(),
                                ini.db.dbPort(),
                                ini.db.dbName(),
                                ini.db.dbUser(),
                                ini.db.dbPass()
                             );
        if (!ora.getConnected()) {
            return;
        }
        for (FileName file: ini.getFiles()){
            Path source = Paths.get(file.fullName());
            currFile = source.toAbsolutePath().toString();
            long bgnTime = System.currentTimeMillis();
            do {
                if ((Files.exists(source))) {
                    Path destination = Paths.get(ini.getTmpDir()+source.getFileName().toString());
                    try {
                        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("✔ File \""+file.shortName()+"\" moved successfully!");
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
                    System.out.println("✔ The \""+file.fullName()+"\" file moved to a temporary directory.");
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
            System.out.printf("\n✔ Connecting the \""+ini.db.dbName()+"\" database.\n");
        } else {
            System.err.printf("\n❌ File \""+currFile+"\" movement error to the temporary directory.\n");
        }
    }
}
