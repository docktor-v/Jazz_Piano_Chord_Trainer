package org.docktor_v;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import javafx.animation.FillTransition;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static javafx.scene.layout.HBox.setMargin;


/**
 * JavaFX App
 */
public class App extends Application {
    private List<Note> notes = new ArrayList<>();

    int third = 108;
    int seventh = 101;
    int tonic = 98;
    private HBox keyboard = new HBox(15);
    private VBox root = new VBox();
    private GridPane controllerPane = new GridPane();
    private Button fwdButton = new Button("Forward");
    private MidiChannel channel;

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
        scene.setOnKeyPressed(e -> {
            ArrayList<Note> playedNotes = notes.stream().filter(s -> s.key.equals(e.getCode())).collect(Collectors.toCollection(ArrayList::new));
            playedNotes.forEach(s -> playKey(s.number));
        });

        //.findFirst().ifPresent(App:test())));
        //orElse(null).number));


        fwdButton.setOnAction((event)
                -> {
            moveTrainerForward(third, seventh, tonic);
            third--;
            seventh--;
        });

        stage.setScene(scene);
        stage.show();
    }

    public void test() {

    }

    public void moveTrainerForward(int third, int seventh, int root) {
        playNoteCombination(third, seventh, root);
        playKey(98);

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
        controllerPane.setHgap(10);
        controllerPane.setMinSize(400, 200);
        controllerPane.setPadding(new Insets(20, 20, 20, 20));
        controllerPane.add(new Button("Reset"), 1, 0);
        controllerPane.add(fwdButton, 2, 0);
        controllerPane.setAlignment(Pos.CENTER);
        return controllerPane;

    }

    private Parent createKeyboardContent() {
        loadChannel();
        //   keyboard.setPrefSize(600, 150);
        HBox.setHgrow(keyboard, Priority.ALWAYS);

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