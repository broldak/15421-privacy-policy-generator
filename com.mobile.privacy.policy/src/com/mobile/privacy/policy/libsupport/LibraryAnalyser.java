package com.mobile.privacy.policy.libsupport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LibraryAnalyser {
    private Map<String,String> libToUtil;
    
    public LibraryAnalyser() {
        libToUtil = new HashMap<String,String>();
        libToUtil.put("android.", "ANDROID");
        libToUtil.put("java.", "ANDROID");
        libToUtil.put("javax.", "ANDROID");
    }
    
    public void loadMapping(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] libUse = line.split("-");
                if(libUse.length == 2) {
                    if(libUse[1].equalsIgnoreCase("Targeted Ads")) {
                        libToUtil.put(libUse[0], "ADVERTISEMENTS");
                    }
                    else if(libUse[1].equalsIgnoreCase("Social Networking Service")) {
                        libToUtil.put(libUse[0], "SOCIAL_NETWORKING");
                    }
                    else if(libUse[1].equalsIgnoreCase("Mobile Analytics")) {
                        libToUtil.put(libUse[0], "MOBILE_ANALYTICS");
                    }
                    else if(libUse[1].equalsIgnoreCase("Utility") || libUse[1].equalsIgnoreCase("Game Engine")
                       || libUse[1].equalsIgnoreCase("Ui component"))
                    {
                        libToUtil.put(libUse[0], "INTERNAL");
                    }
                    else if(libUse[1].equalsIgnoreCase("Mobile Analytics")) {
                        libToUtil.put(libUse[0], "MOBILE_ANALYTICS");
                    }
                    else {
                        //There are a couple more categories in the DB that we don't account for
                        libToUtil.put(libUse[0], "UNKNOWN");
                    }
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map.Entry<String, String> lookupPackgeName(String packageName) {
        packageName = packageName.toLowerCase();
        for (Map.Entry<String, String> e : libToUtil.entrySet()) {
            String library = e.getKey().toLowerCase();
            if(packageName.toLowerCase().contains(library)) {
                return e;
            }
        }
        
        return null;
    }
}
