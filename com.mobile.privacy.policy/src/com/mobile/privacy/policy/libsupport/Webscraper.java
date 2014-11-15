package com.mobile.privacy.policy.libsupport;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Webscraper {

    static Map<String, String> libraryPurposes;
    public static void main(String[] args) {
        libraryPurposes = new HashMap<String,String>();
        try {
            PrintWriter writer = new PrintWriter("3rdPartyLibraries.txt", "UTF-8");
            for(int i = 0; i < 9; i++) {
                URL privacyGradeWeb = new URL("http://privacygrade.org/third_party_libraries?page=" + (i+1));
                URLConnection connection = privacyGradeWeb.openConnection();
                Scanner webScanner = new Scanner(connection.getInputStream());
                webScanner.useDelimiter("<tr ");
                String content = webScanner.next();
                while(webScanner.hasNext()) {
                    content = webScanner.next();
                    int idx = content.indexOf("<td>") + 4;
                    idx = content.indexOf(">",idx) + 1;
                    int endIdx = content.indexOf("<",idx);
                    String libraryName = content.substring(idx,endIdx).trim();
                    idx = content.indexOf("<td>",idx) + 4;
                    endIdx = content.indexOf("<",idx);
                    String libraryType = content.substring(idx,endIdx).trim();
                    writer.println(libraryName + "-" + libraryType);
                }
            }
            writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
    }

}
