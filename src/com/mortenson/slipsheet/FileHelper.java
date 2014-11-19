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
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author jdegiova
 */
public class FileHelper {
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
    
    private FileHelper(){
        // nothing
    }
    
    public static boolean isFileWritable(File file) {
        if (file.canWrite()) {
            // file.canWrite does not reliably indicate if the file can be written
            // to on a Windows machine, so further checking needs to be done.
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                logger.info("file " + file.getName() + " is writable");
                return true;
            } catch (IOException e) {
                logger.error("Unable to write to file. Check to make sure it is not open elsewhere: " + e);
                return false;
            }
        } else{
            logger.error("Unable to write to file. Check to make permissions are correct.");
            return false;
        }
    }
    
    public static void main(String[] args){
        System.out.println(FileHelper.isFileWritable(new File("C:\\Users\\jdegiova\\Slipsheeter\\New Document Set\\2.pdf")));
    }
}
