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
import java.util.logging.Level;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import org.apache.log4j.Logger;

/**
 *
 * @author jdegiova
 */
public class Slipsheet extends Application {

    private Stage stage;
    private File historicalSetDirectory, currentSetDirectory, newDocumentSetDirectory, stampPDFLocation, lastChosenDirectory;
    private Text resultText;
    private Button slipsheetButton;
    private final SlipsheetConfig config = SlipsheetConfig.getInstance();
    private final Logger logger = Logger.getRootLogger();
    private BooleanProperty waitingToStart;

    @Override
    public void start(Stage primaryStage) {
        logger.info("Application Started");
        this.waitingToStart = new SimpleBooleanProperty(false);
        waitingToStart.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                logger.debug("changed " + oldValue + "->" + newValue);
                startSlipsheeter();
            }
        });

        this.stage = primaryStage;
        this.historicalSetDirectory = new File(config.getDefaultHistoricalSet());
        this.currentSetDirectory = new File(config.getDefaultCurrentSet());
        this.newDocumentSetDirectory = new File(config.getDefaultNewDocSet());
        this.stampPDFLocation = new File(config.getDefaultPDFStamp());

        Scene scene = buildScene();

        primaryStage.setTitle("Bluebeam Auto Slipsheeting");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane initGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        return grid;
    }

    private Scene buildScene() {
        GridPane grid = initGrid();
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.LEFT);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.RIGHT);
        column2.setMinWidth(150);

        Text scenetitle = new Text("Bluebeam Auto Slipsheeting");
        scenetitle.setId("welcome-text");
        grid.add(scenetitle, 0, 0, 3, 1);

        Text instructions = new Text("Please select the locations for the historical set, current set, and new document set...");
        grid.add(instructions, 0, 1, 3, 1);

        // historical set
        Text historicalSetText = new Text(config.getDefaultHistoricalSet());
        historicalSetText.setWrappingWidth(600);
        grid.add(historicalSetText, 2, 2);
        Button historicalSetButton = generateChooserButton("Select historical set...", historicalSetText);
        grid.add(historicalSetButton, 1, 2);

        // current set
        Text currentSetText = new Text(config.getDefaultCurrentSet());
        currentSetText.setWrappingWidth(600);
        grid.add(currentSetText, 2, 3);
        Button currentSetButton = generateChooserButton("Select current set...", currentSetText);
        grid.add(currentSetButton, 1, 3);

        // new document set
        Text newDocumentSetText = new Text(config.getDefaultNewDocSet());
        newDocumentSetText.setWrappingWidth(600);
        grid.add(newDocumentSetText, 2, 4);
        Button newDocSetButton = generateChooserButton("Select new document set...", newDocumentSetText);
        grid.add(newDocSetButton, 1, 4);

        // PDF Stamp location
        Text pdfStampText = new Text(config.getDefaultPDFStamp());
        grid.add(pdfStampText, 2, 5);
        Button pdfStampButton = generateStampChooserButton(pdfStampText);
        grid.add(pdfStampButton, 1, 5);

        // slipsheet button
        slipsheetButton = new Button("Start Slipsheet process...");
        slipsheetButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        waitingToStart.setValue(true);
                    }
                });
        grid.add(slipsheetButton, 2, 6);

        resultText = new Text("");
        grid.add(resultText, 0, 7, 3, 1);

        grid.getColumnConstraints().add(0, column1);
        grid.getColumnConstraints().add(1, column2);

        Scene scene = new Scene(grid, 900, 350);

        addStyleSheetToScene(scene, "Login.css");

        return scene;
    }

    private Task<Slipsheeter> createTask() {
        Task<Slipsheeter> task = new Task<Slipsheeter>() {

            @Override
            protected Slipsheeter call() throws Exception {
                Slipsheeter s;
                updateMessage("Running Task...");
                try {
                    s = new Slipsheeter(historicalSetDirectory, currentSetDirectory, newDocumentSetDirectory, stampPDFLocation);
                } catch (InstantiationException ie) {
                    updateMessage("Error: " + ie.getMessage());
                    return null;
                } catch (Exception e) {
                    updateMessage("Error: " + e.getMessage());
                    return null;
                }

                s.start();

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                updateMessage("Successfully completed slipsheeting at " + dateFormat.format(cal.getTime()));
                return s;
            }

        };
        return task;

    }

    private void startSlipsheeter() {
        if (waitingToStart.getValue() == true) {
            try {
                waitingToStart.setValue(false);

                Task<Slipsheeter> task = createTask();
                Thread th = new Thread(task);
                th.setDaemon(true);
                th.start();

                //resultText.textProperty().bind(task.messageProperty());
                resultText.textProperty().bind(task.messageProperty());
                slipsheetButton.disableProperty().bind(task.runningProperty());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Slipsheet.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void configureFileChooser(final DirectoryChooser dirChooser, String title) {
        dirChooser.setTitle(title);
        File initialDirectory;
        if (lastChosenDirectory == null) {
            initialDirectory = new File(System.getProperty("user.home"));
        } else {
            initialDirectory = lastChosenDirectory;
        }
        dirChooser.setInitialDirectory(initialDirectory);
    }

    private Button generateStampChooserButton(final Text txtElement) {
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
                            // update configuration file
                            config.setDefaultPDFStamp(file.getAbsolutePath());
                            txtElement.setText(config.getDefaultPDFStamp());
                            stampPDFLocation = file;
                        }
                    }
                });
        return button;
    }

    private Button generateChooserButton(final String btnText, final Text txtElement) {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        Button button = new Button(btnText);
        button.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        configureFileChooser(dirChooser, btnText);
                        File file = dirChooser.showDialog(stage);
                        if (file != null) {
                            lastChosenDirectory = file;
                            runPDFCount(file,txtElement);
                            String title = ((Button) e.getSource()).getText();
                            if (title.contains("historical")) {
                                config.setDefaultHistoricalSet(file.getAbsolutePath());
                                historicalSetDirectory = file;
                            } else if (title.contains("current")) {
                                config.setDefaultCurrentSet(file.getAbsolutePath());
                                currentSetDirectory = file;
                            } else if (title.contains("new")) {
                                config.setDefaultNewDocSet(file.getAbsolutePath());
                                newDocumentSetDirectory = file;
                            } else {
                                System.out.println("invalid button");
                            }
                        }
                    }
                });
        return button;
    }

    private Task<Integer> createPDFCountTask(final File directory) {
        Task<Integer> task = new Task<Integer>() {

            @Override
            protected Integer call() throws Exception {
                int i = 0;
                File[] files = directory.listFiles(new PDFFileFilter());

                if (files != null) {
                    i = files.length;
                    updateMessage("searching... "+directory.getAbsolutePath() + " - searching... found " + i + " PDFs");
                }

                File[] subDirs = directory.listFiles(new DirectoryFileFilter());
                if (subDirs != null) {
                    for (File sub : subDirs) {
                        i += getPDFCount(sub);
                    updateMessage(directory.getAbsolutePath() + " - searching... found " + i + " PDFs");
                    }
                }

                updateMessage(directory.getAbsolutePath() + " - found " + i + " PDFs");
                return i;
            }

        };
        return task;
    }

    private void runPDFCount(File directory, Text countElement) {
        Task<Integer> t = createPDFCountTask(directory);
        Thread th = new Thread(t);
        th.setDaemon(true);
        th.start();
        countElement.textProperty().bind(t.messageProperty());
    }

    private int getPDFCount(File directory) {
        int i = 0;
        File[] files = directory.listFiles(new PDFFileFilter());

        if (files != null) {
            i = files.length;
        }

        File[] subDirs = directory.listFiles(new DirectoryFileFilter());
        if (subDirs != null) {
            for (File sub : subDirs) {
                i += getPDFCount(sub);
            }
        }

        return i;
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
