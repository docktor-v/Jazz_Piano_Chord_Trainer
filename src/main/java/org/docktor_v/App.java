package org.docktor_v;

import javafx.animation.FillTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static javafx.scene.layout.HBox.setMargin;


/**
 * JavaFX App
 */
public class App extends Application {
    private List<Note> notes = new ArrayList<>();

    private int third = 108;
    private int seventh = 101;
    private int tonic = 98;
    private HBox keyboard = new HBox(15);
    private VBox root = new VBox();
    private GridPane controllerPane = new GridPane();
    private Button fwdButton = new Button("Forward");
    private Button rstButton = new Button("Reset");
    private MidiChannel channel;
    private int buttonPressCounter = 1;
    TextArea monitor1 = new TextArea();
    TextArea monitor2 = new TextArea();
    TextArea monitor3 = new TextArea();
    Image image = new Image("File:arrow.jpg");
    ImageView pic = new ImageView(image);
    Label chordType = new Label();

    @Override
    public void start(Stage stage) {
        File file = new File("noteFile.csv");
        try (Scanner scanner = new Scanner(Paths.get("noteFile.csv"))) {

            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                String[] rowParts = row.split(",");
                if (rowParts.length == 3) {
                    notes.add(new Note(rowParts[0].trim(), KeyCode.getKeyCode(rowParts[1].trim()), Integer.valueOf(rowParts[2].trim())));
                } else {
                    notes.add(new Note(rowParts[0].trim(), KeyCode.PRINTSCREEN, Integer.valueOf(rowParts[1].trim())));
                }

            }
        } catch (Exception e) {
            System.out.println("Faled to read file");
        }

        root.setPrefSize(600, 500);
        root.getChildren().addAll(createKeyboardContent(), createControlContent());

        Scene scene = new Scene(root);
        List<Integer> noteList = new ArrayList<>();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                ArrayList<Note> playedNotes = notes.stream().filter(s -> s.key.equals(keyEvent.getCode())).collect(Collectors.toCollection(ArrayList::new));
                playedNotes.forEach(s -> playKey(s.number));
            }
        });
        rstButton.setOnAction(((event) -> {
            buttonPressCounter = 1;
            third = 101; //D
            seventh = 108; //C
            tonic = 98; //F
        }));

        fwdButton.setOnAction((event)
                -> {
            if (buttonPressCounter == 1) {
                monitor1.appendText("Play minor seventh, then move finger #five down one semitone");
                chordType.setText("Minor Seventh");
                playNoteCombination(third, seventh, tonic);
                seventh--;
                tonic = tonic - 7;
                buttonPressCounter++;
            } else if (buttonPressCounter == 2) {
                monitor2.appendText("Dominant seventh");
                playNoteCombination(third, seventh, tonic);
                third--;
                tonic = tonic + 5;
                buttonPressCounter++;
            } else if (buttonPressCounter == 3) {
                monitor3.appendText("Major seventh");
                playNoteCombination(third, seventh, tonic);
                buttonPressCounter++;

            } else if (buttonPressCounter % 4 == 0) {
                // monitor3.appendText("Major seventh");
                playNoteCombination(third, seventh, tonic);
                buttonPressCounter = 1;
                //do nothing.
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    public void playKey(int key) {
        keyboard.getChildren()
                .stream()
                .map(view -> (NoteView) view)
                .filter(view -> view.note.number == key)
                .forEach(view -> {
                    if (!view.note.name.contains("#")) {
                        FillTransition ft = new FillTransition(
                                Duration.seconds(.15),
                                view.bg,
                                Color.WHITE,
                                Color.BLACK
                        );
                        ft.setCycleCount(2);
                        ft.setAutoReverse(true);
                        ft.play();

                        channel.noteOn(view.note.number, 90);
                        view.bg.setFill(Color.BLACK);
                    } else {
                        FillTransition ft = new FillTransition(
                                Duration.seconds(.15),
                                view.bg,
                                Color.BLACK,
                                Color.WHITE
                        );
                        ft.setCycleCount(2);
                        ft.setAutoReverse(true);
                        ft.play();
                        channel.noteOn(view.note.number, 90);
                    }
                });
    }

    private void playNoteCombination(int keyNumber1, int keyNumber2, int keyNumber3) {
        keyboard.getChildren()
                .stream()
                .map(view -> (NoteView) view)
                .filter(view -> view.note.number == keyNumber1 || view.note.number == keyNumber2 || view.note.number == keyNumber3)
                .forEach(view -> {
                    if (!view.note.name.contains("#")) {
                        FillTransition ft = new FillTransition(
                                Duration.seconds(.75),
                                view.bg,
                                Color.WHITE,
                                Color.BLACK
                        );
                        ft.setCycleCount(2);
                        ft.setAutoReverse(true);
                        ft.play();

                        channel.noteOn(view.note.number, 90);
                        view.bg.setFill(Color.BLACK);
                    } else {
                        FillTransition ft = new FillTransition(
                                Duration.seconds(.75),
                                view.bg,
                                Color.BLACK,
                                Color.WHITE
                        );
                        ft.setCycleCount(2);
                        ft.setAutoReverse(true);
                        ft.play();
                        channel.noteOn(view.note.number, 90);
                    }
                });
    }

    private static class NoteView extends StackPane {
        private Note note;
        private Rectangle bg = new Rectangle(35, 200, Color.WHITE);
        private Text letter;

        NoteView(Note note) {
            this.note = note;
            this.letter = new Text(note.name);

            if (note.name.contains("#")) {
                bg.setFill(Color.BLACK);
                bg.setHeight(180);
                bg.setWidth(25);
                letter.setFill(Color.WHITE);

            }
            bg.setStroke(Color.BLACK);
            getChildren().addAll(bg, letter);
            bg.setStrokeWidth(2.5);

        }
    }

    private Parent createControlContent() {

        controllerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(5))));
        for (int i = 0; i < 9; i++) {
            ColumnConstraints column = new ColumnConstraints(100);
            controllerPane.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 8; i++) {
            RowConstraints row = new RowConstraints(35);
            controllerPane.getRowConstraints().add(row);
        }
        controllerPane.setHgap(5);
        controllerPane.setMinSize(400, 350);
        controllerPane.setVgap(5);
        controllerPane.setHgap(5);
        monitor1.setWrapText(true);
        monitor2.setWrapText(true);
        monitor3.setWrapText(true);
        GridPane.setRowSpan(monitor1, 2);
        GridPane.setColumnSpan(monitor1, 3);
        GridPane.setRowSpan(monitor2, 2);
        GridPane.setColumnSpan(monitor2, 3);
        GridPane.setRowSpan(monitor3, 2);
        GridPane.setColumnSpan(monitor3, 3);
        monitor1.setEditable(false);

        chordType.setText("Chord types will show here");

        controllerPane.setGridLinesVisible(true);

        //i is x axis i1 is Y axis  page.add(Node, colIndex, rowIndex, colSpan, rowSpan):
        controllerPane.add(rstButton, 0, 0);
        controllerPane.add(fwdButton, 1, 0);
        controllerPane.add(chordType, 2, 0);

        controllerPane.add(monitor1, 6, 0);
        controllerPane.add(monitor2, 6, 3);
        controllerPane.add(monitor3, 6, 6);

        pic.setFitHeight(30);
        pic.setFitWidth(50);
        controllerPane.add(pic, 5, 0);
        GridPane.setHalignment(pic, HPos.RIGHT);
        pic.setVisible(false);
        controllerPane.setAlignment(Pos.CENTER);
        return controllerPane;
    }

    private Parent createKeyboardContent() {
        loadChannel();

        keyboard.setSpacing(0);
        notes.forEach(note -> {
            NoteView view = new NoteView(note);
            view.bg.setOnMouseClicked((event) -> {
                playKey(view.note.number);
            });
            keyboard.getChildren().add(view);
        });


        keyboard.getChildren()
                .stream()
                .map(view -> (NoteView) view)
                .filter(view -> view.note.name.contains("#"))
                .forEach(sharp -> setMargin(sharp, new Insets(0, 0, 20, 0)));


        return keyboard;

    }

    public void loadChannel() {
        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            synth.loadInstrument(synth.getDefaultSoundbank().getInstruments()[0]);
            channel = synth.getChannels()[4];
        } catch (MidiUnavailableException e) {
            System.out.println("Cannot get synth");
            e.printStackTrace();
        }
    }

    private static class Note {
        private String name;
        private KeyCode key;
        private int number;

        Note(String name, KeyCode key, int number) {
            this.name = name;
            this.key = key;
            this.number = number;
        }

        Note(String name, int number) {
            this.name = name;
            this.key = KeyCode.PRINTSCREEN;
            this.number = number;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}