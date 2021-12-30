package org.docktor_v;

import javafx.animation.FillTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.Arrays;
import java.util.List;

import static javafx.scene.layout.HBox.setMargin;


/**
 * JavaFX App
 */
public class App extends Application {
    private List<Note> notes = Arrays.asList(
            new Note("C", KeyCode.A, 60),
            new Note("C#", KeyCode.W, 61),
            new Note("D", KeyCode.S, 62),
            new Note("D#", KeyCode.E, 63),
            new Note("E", KeyCode.D, 64),

            new Note("F", KeyCode.F, 65),
            new Note("F#", KeyCode.T, 66),
            new Note("G", KeyCode.G, 67),
            new Note("G#", KeyCode.Y, 68),
            new Note("A", KeyCode.H, 69),
            new Note("A#", KeyCode.U, 70),
            new Note("B", KeyCode.J, 71),
            new Note("C", KeyCode.K, 72)

    );

    private HBox root = new HBox(15);

    private MidiChannel channel;

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent());
        scene.setOnKeyPressed(e -> onKeyPress(e.getCode()));
        stage.setScene(scene);
        stage.show();
    }

    private void onKeyPress(KeyCode key) {
        root.getChildren()
                .stream()
                .map(view -> (NoteView) view)
                .filter(view -> view.note.key.equals(key))
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

    private void playNoteCombination(KeyCode key1, KeyCode key2) {
        root.getChildren()
                .stream()
                .map(view -> (NoteView) view)
                .filter(view -> view.note.key.equals(key1))
                .forEach(view -> {
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
                });
    }

    private static class NoteView extends StackPane {
        private Note note;
        private Rectangle bg = new Rectangle(50, 200, Color.WHITE);
        private Text letter;
        NoteView(Note note) {
            this.note = note;
             this.letter = new Text(note.name);
             letter.setFill(Color.BLUE);
            if (note.name.contains("#")) {
                bg.setFill(Color.BLACK);
                bg.setHeight(180);
                bg.setWidth(30);
                letter.setFill(Color.WHITE);

            }
            bg.setStroke(Color.BLACK);
            getChildren().addAll(bg, letter);
            bg.setStrokeWidth(2.5);

        }
    }

    private Parent createContent() {
        loadChannel();
        root.setPrefSize(600, 150);

        root.setSpacing(0);

        notes.forEach(note -> {
            NoteView view = new NoteView(note);
            root.getChildren().add(view);
        });
        root.getChildren()
                .stream()
                .map(view -> (NoteView) view)
                .filter(view -> view.note.name.contains("#"))
                .forEach(sharp -> setMargin(sharp, new Insets(0, 0, 20, 0)));
//   root.setMargin(view, new Insets(10,20,30,40));

        return root;

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
    }

    public static void main(String[] args) {
        launch(args);
    }
}