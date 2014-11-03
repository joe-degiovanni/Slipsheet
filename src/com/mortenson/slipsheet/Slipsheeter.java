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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jdegiova
 */
public class Slipsheeter {
    
    File historicalSet, currentSet, newDocumentSet, stampPDF, bbScriptEngine;
    
    public Slipsheeter(File historicalSet, File currentSet, File newDocumentSet, File stampPDF) throws InstantiationException{
        this.historicalSet = historicalSet;
        this.currentSet = currentSet;
        this.newDocumentSet = newDocumentSet;
        this.stampPDF = stampPDF;
        this.bbScriptEngine = locateBlueBeamScriptEngine();
        if (bbScriptEngine==null) throw new InstantiationException("unable to find Bluebeam Revu installation");
    }
    
    private File locateBlueBeamScriptEngine(){
        String[] defaultLocations = {
            "C:\\Program Files\\Bluebeam Software\\Bluebeam Revu\\Script\\ScriptEngine.exe",
            "C:\\Program Files (x86)\\Bluebeam Software\\Bluebeam Revu\\Script\\ScriptEngine.exe"
        };
        for(String path:defaultLocations){
            File file = new File(path);
            if (file.exists()) return file;
        }
        return null;
    }
    
    public void start(){
        System.out.println("Starting process...");
               
        slipsheetDirectory(newDocumentSet,historicalSet,currentSet,true);
        
    }
    
    private void slipsheetDirectory(File newDocDir, File historicalDocDir, File currentDocDir, boolean recursive){
        PDFFileFilter pff = new PDFFileFilter();
        
        File[] newDocuments = newDocDir.listFiles(pff);
        File[] historicalDocuments = historicalDocDir.listFiles(pff);
        
        for(File newFile:newDocuments){
            System.out.println("Processing new file "+ newFile.getName() + "... ");
            
            if(containsFileName(historicalDocuments,newFile)){
                System.out.println("Found a match in historical documents... kick off bluebeam slipsheet script");
                File histTarget = new File(historicalDocDir.getAbsolutePath()+"\\"+newFile.getName());
                File currTarget = new File(currentDocDir.getAbsolutePath()+"\\"+newFile.getName());
                slipsheetSingleFile(newFile,histTarget,currTarget);
            } else {
                System.out.println("No matches found. Adding new document to historical set.");
                copyFile(newFile,historicalDocDir);
                copyFile(newFile,currentDocDir);
            }
            
        }
        
        if(recursive==true){
            slipsheetSubDirectories(newDocDir,historicalDocDir,currentDocDir);
        }
    }
    
    private void slipsheetSingleFile(File latest, File historical, File current){
        File temp = new File(historical.getParentFile().getAbsolutePath() + "temp.pdf");
        try (PrintWriter writer = new PrintWriter("slipsheeter3000script.bci", "UTF-8")) {
            writer.println("Open(\""+historical.getAbsolutePath()+"\")");
            writer.println("Unflatten()");
            writer.println("PageExtract(\"1\",\""+temp.getAbsolutePath()+"\")");
            writer.println("InsertPages(\"0\",\""+temp.getAbsolutePath()+"\")");
            writer.println("ReplacePages (\""+latest.getAbsolutePath()+"\",\"1\",\"1\",\"true\")");
            writer.println("Stamp (\""+stampPDF.getAbsolutePath()+"\",\"upperleft\",\"1\",\"1\",\"0\",\"1\",\"1\",\"normal\",\"2\",\"true\")");
            writer.println("DeleteFile(\""+temp.getAbsolutePath()+"\")");
            writer.println("Flatten()");
            writer.println("DeleteFile(\""+current.getAbsolutePath()+"\")");
            writer.println("PageExtract(\"1\",\""+current.getAbsolutePath()+"\") ");
            writer.println("Save()");
            writer.println("Close()");
            writer.close();
            
            // execute bluebeam script
            executeBlueBeamScript("slipsheeter3000script.bci");
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Slipsheeter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void executeBlueBeamScript(String scriptName){
        String cmd = this.bbScriptEngine.getAbsolutePath()+ " Script(\""+scriptName+"\")";
        try {
            // Run "netsh" Windows command
            Process process = Runtime.getRuntime().exec(cmd);

            // Get input streams
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read command standard output
            String s;
            System.out.println("Standard output: ");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // Read command errors
            System.out.println("Standard error: ");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    private void copyFile(File file, File destDir){
        File dest = new File(destDir.getAbsolutePath()+'\\'+file.getName());
        try{
            //dest.createNewFile();
            Files.copy(file.toPath(), dest.toPath(),StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
    
    private boolean containsFileName(File[] directory, File file){
        for(File f:directory){
            if(f.getName().equals(file.getName())) return true;
        }
        return false;
    }

    private void slipsheetSubDirectories(File newDocDir, File historicalDocDir, File currentDocDir) {
        DirectoryFileFilter dff = new DirectoryFileFilter();
        File[] newSubDirectories = newDocDir.listFiles(dff);
        for(File newSubDir:newSubDirectories){
            
            File histSubDir = new File(historicalDocDir.getAbsolutePath()+"\\"+newSubDir.getName());
            File currSubDir = new File(currentDocDir.getAbsolutePath()+"\\"+newSubDir.getName());
            
            if(!histSubDir.exists()) histSubDir.mkdir();
            if(!currSubDir.exists()) currSubDir.mkdir();
            
            slipsheetDirectory(newSubDir,histSubDir,currSubDir,true);
        }
    }
    
}
