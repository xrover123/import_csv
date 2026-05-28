package com.apsida.parus_itapt.imp_csv;
import org.ini4j.Ini;
import java.io.File;
import java.io.IOException;
record Rdb(String dbIP, String dbPort, String dbName, String dbUser, String dbPass){}

public class Init {
    public Rdb db;

    private String tmpDir;
    private long waitingTime;
    private long pollingInterval;
    private FileName[] files;

    public String getTmpDir() {return tmpDir;}
    public long getWaitingTime() {return waitingTime;}
    public long getPollingInterval() {return pollingInterval;}
    //public Rdb getDb() {return db;}
    public FileName[] getFiles() {return files;}

    Init(String cfgFileName){
        String dbIP;
        String dbPort;
        String dbName;
        String dbUser;
        String dbPass;
        String[] shortNames;
        try {
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
            Ini ini = new Ini(new File(cfgFileName));
            shortNames = ini.get("FILES").keySet().toArray(new String[0]);
            files = new FileName[shortNames.length];
            int i = 0;
            for (String name: shortNames) {
                String s = ini.get("FILES", name);
                if (s != null) {
                    String ss[] = s.split(";",2);
                    if (ss.length == 2) {
                        files[i++] = new FileName(name, ss[0],ss[1]);
                    }
                }
            }
            dbIP   = ini.get("DB","DB_IP");
            dbPort = ini.get("DB","DB_PORT");
            dbName = ini.get("DB","DB_NAME");
            dbUser = ini.get("DB","DB_USER");
            dbPass = ini.get("DB","DB_PASS");

            tmpDir = ini.get("FILE","IMP_TMP");
            try {
                waitingTime = Long.parseLong(ini.get("FILE","IMP_WAIT"));
            } catch(NumberFormatException | NullPointerException e) {
                waitingTime = 60;
            }
            try {
                pollingInterval = Long.parseLong(ini.get("FILE","IMP_INT"));
            } catch(NumberFormatException | NullPointerException e) {
                pollingInterval = 1;
            }

            db = new Rdb(dbIP,dbPort,dbName, dbUser, dbPass);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
