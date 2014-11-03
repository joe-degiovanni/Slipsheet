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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author jdegiova
 */
public class Slipsheet extends Application {
    
    private Stage stage;
    private File historicalSetDirectory, currentSetDirectory, newDocumentSetDirectory, stampPDFLocation, lastChosenDirectory;
    private Text resultText;
    
    @Override
    public void start(Stage primaryStage) {        
        this.stage = primaryStage;
        
        Scene scene = buildScene();
        
        primaryStage.setTitle("Slipsheet 3000");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private GridPane initGrid(){
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        return grid;
    }
    
    private Scene buildScene(){
        GridPane grid = initGrid();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.LEFT);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.RIGHT);
         
        Text scenetitle = new Text("Slippy 3000");
        scenetitle.setId("welcome-text");
        grid.add(scenetitle, 0, 0, 3, 1);
        
        Text instructions = new Text("Please select the locations for the historical set, current set, and new document set...");
        grid.add(instructions, 0, 1, 3, 1);
        
        // historical set
        Text historicalSetText = new Text("Please select the historical set...");
        grid.add(historicalSetText, 2, 2);
        Button historicalSetButton = generateChooserButton("Select historical set...",historicalSetText);
        grid.add(historicalSetButton, 1, 2);
        
        // current set
        Text currentSetText = new Text("Please select the current set...");
        grid.add(currentSetText, 2, 3);
        Button currentSetButton = generateChooserButton("Select current set...",currentSetText);
        grid.add(currentSetButton, 1, 3);
        
        // new document set
        Text newDocumentSetText = new Text("Please select the new document set...");
        grid.add(newDocumentSetText, 2, 4);
        Button newDocSetButton = generateChooserButton("Select new document set...",newDocumentSetText);
        grid.add(newDocSetButton, 1, 4);
        
        // PDF Stamp location
        Text pdfStampText = new Text("Please select the PDF stamp...");
        grid.add(pdfStampText, 2, 5);
        Button pdfStampButton = generateStampChooserButton(pdfStampText);
        grid.add(pdfStampButton, 1, 5);
        
        // slipsheet button
        Button slipsheetButton = new Button("Start Slipsheet process...");
        slipsheetButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    System.out.println("Start slipsheeting process");
                    Button btn = (Button)e.getSource();
                    btn.setDisable(true);
                    btn.setText("In progress...");
                    resultText.setText("Running slipsheet process...");
                    Slipsheeter s;
                    try {
                        s = new Slipsheeter(historicalSetDirectory, currentSetDirectory, newDocumentSetDirectory, stampPDFLocation);
                    } catch (InstantiationException ie){
                        resultText.setText("Error: "+ie.getMessage());
                        btn.setText("Failed");
                        return;
                    }
                    
                    s.start();
                    btn.setDisable(false);
                    btn.setText("Rerun Slipsheet Process");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    resultText.setText("Successfully completed slipsheeting at "+dateFormat.format(cal.getTime()));
                }
            });
        grid.add(slipsheetButton, 2, 6);
        
        resultText = new Text("");
        grid.add(resultText,0,7, 3, 1);
        
        grid.getColumnConstraints().add(0, column1);
        grid.getColumnConstraints().add(1, column2);
        
        Scene scene = new Scene(grid, 700, 350);
        
        addStyleSheetToScene(scene,"Login.css");
        
        return scene;
    }
    
    private void configureFileChooser(final DirectoryChooser dirChooser,String title){                           
        dirChooser.setTitle(title);
        File initialDirectory;
        if(lastChosenDirectory==null){
            initialDirectory = new File(System.getProperty("user.home"));
        } else {
            initialDirectory = lastChosenDirectory;
        }
        dirChooser.setInitialDirectory(initialDirectory); 
    }
    
    
    private Button generateStampChooserButton(final Text txtElement){
        final FileChooser fileChooser = new FileChooser();
        Button button = new Button("Select stamp...");
        button.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    fileChooser.setTitle("Select stamp PDF...");
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        txtElement.setText(file.getAbsolutePath());
                        String title = ((Button)e.getSource()).getText();
                        stampPDFLocation = file;
                    }
                }
            });
        return button;
    }
    
    private Button generateChooserButton(final String btnText, final Text txtElement){
        final DirectoryChooser dirChooser = new DirectoryChooser();
        Button button = new Button(btnText);
        button.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    configureFileChooser(dirChooser,btnText);
                    File file = dirChooser.showDialog(stage);
                    if (file != null) {
                        lastChosenDirectory = file;
                        int count = getPDFCount(file);
                        txtElement.setText(file.getAbsolutePath() + " - found "+count+" PDF documents.");
                        String title = ((Button)e.getSource()).getText();
                        if(title.contains("historical")){
                            historicalSetDirectory = file;
                        } else if (title.contains("current")) {
                            currentSetDirectory = file;
                        } else if (title.contains("new")) {
                            newDocumentSetDirectory = file;
                        } else {
                            System.out.println("invalid button");
                        }
                    }
                }
            });
        return button;
    }
    
    final private int getPDFCount(File directory){
        
        File[] files = directory.listFiles(new PDFFileFilter());
        int count = files.length;
        
        File[] subDirs = directory.listFiles(new DirectoryFileFilter());
        for(File sub:subDirs){
            count+=getPDFCount(sub);
        }
      
        return count;
    }
 

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void addStyleSheetToScene(Scene scene, String cssFileName) {
        scene.getStylesheets().add(Slipsheet.class.getResource(cssFileName).toExternalForm());
    }
    
}
