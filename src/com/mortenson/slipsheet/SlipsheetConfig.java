/*
 * Copyright (C) 2014 jdegiova
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mortenson.slipsheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author jdegiova
 */
public final class SlipsheetConfig {
    
    private static SlipsheetConfig instance = null;
    private Properties config;
    private File configFile;
    public static final String HIST_KEY = "DefaultHistoricalSet";
    public static final String CURR_KEY = "DefaultCurrentSet";
    public static final String NEW_DOC_KEY = "DefaultNewDocumentSet";
    public static final String STAMP_KEY = "DefaultPDFStamp";
    private static final String userHome = System.getProperty("user.home");
    private final Logger logger = Logger.getRootLogger();
    
    private SlipsheetConfig(){
        configFile = new File("slipsheet.cfg");
        config = new Properties();
        
        try(FileReader fr = new FileReader(configFile)){
            config.load(fr);
            fr.close();
        } catch (FileNotFoundException ex) {
            logger.info(ex.getMessage()+"\nCould not find config file. Attempting to create one...");
            saveConfigFile();
        } catch (IOException ex) {
            logger.error("Unable to load config file\n" + ex.getMessage());
        }
    }
    
    public void saveConfigFile(){
        try(FileWriter fw = new FileWriter(configFile)){
            config.store(fw, "Configuration Properties for Slippy 3000");
            fw.close();
        } catch (IOException ex) {
            logger.error("Unable to save config file");
            logger.error(ex.getMessage());
        }
    }
    
    public String getDefaultHistoricalSet(){
        return getKeyOrUserHome(HIST_KEY);
    }
    
    public String getDefaultCurrentSet(){
        return getKeyOrUserHome(CURR_KEY);
    }
    
    public String getDefaultNewDocSet(){
        return getKeyOrUserHome(NEW_DOC_KEY);
    }
    
    public String getDefaultPDFStamp(){
        return getKeyOrUserHome(STAMP_KEY);
    }
    
    private String getKeyOrUserHome(String key){
        if(!config.containsKey(key) || config.getProperty(key).equals("")){
            logger.info("Unable to find property "+key+". Defaulting to "+userHome);
            config.put(key, userHome);
            saveConfigFile();
        }
        return config.getProperty(key);
    }
    
    public String setDefaultHistoricalSet(String value){
        return setKeyOrUserHome(HIST_KEY,value);
    }
    
    public String setDefaultCurrentSet(String value){
        return setKeyOrUserHome(CURR_KEY,value);
    }
    
    public String setDefaultNewDocSet(String value){
        return setKeyOrUserHome(NEW_DOC_KEY,value);
    }
    
    public String setDefaultPDFStamp(String value){
        return setKeyOrUserHome(STAMP_KEY,value);
    }
    
    private String setKeyOrUserHome(String key,String value){
        logger.debug("Setting "+key+" to "+value);
        config.put(key, value);
        saveConfigFile();
        return config.getProperty(key);
    }
    
    public static SlipsheetConfig getInstance(){
        if (instance == null){
            instance = new SlipsheetConfig();
        }
        return instance;
    }
}
