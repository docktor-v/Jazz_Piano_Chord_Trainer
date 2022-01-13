package org.docktor_v;

import javafx.animation.FillTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
import javafx.scene.text.Font;
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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static javafx.scene.layout.HBox.setMargin;


/**
 * JavaFX App
 */
public class App extends Application {
    private List<Note> notes = new ArrayList<>();
    private HashMap<Integer, Note> noteMap = new HashMap<>();
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
    private boolean option = true;
    TextArea monitor1 = new TextArea();
    TextArea monitor2 = new TextArea();
    TextArea monitor3 = new TextArea();
    Image arrowImage = new Image("File:arrow.jpg");
    ImageView arrowImageView1 = new ImageView(arrowImage);
    ImageView arrowImageView2 = new ImageView(arrowImage);
    ImageView arrowImageView3 = new ImageView(arrowImage);
    Image handsImage = new Image("File:Hands.jpg");
    ImageView handsImageView = new ImageView(handsImage);
    Image notesImage = new Image("File:notes.jpg");
    ImageView notesImageView = new ImageView(notesImage);

    Label chord1 = new Label();
    Label chord2 = new Label();
    Label chord3 = new Label();

    Label introduction = new Label();
    Label introductionHeader = new Label();
    Label fingeringNote = new Label();
    Label getFingeringNoteHeader = new Label();
    Label instructions = new Label();

    @Override
    public void start(Stage stage) {
        File file = new File("noteFile.csv");
        try (Scanner scanner = new Scanner(Paths.get("noteFile.csv"))) {

            while (scanner.hasNextLine()) {
                String row = scanner.nextLine();
                String[] rowParts = row.split(",");
                if (rowParts.length == 3) {
                    notes.add(new Note(rowParts[0].trim(), KeyCode.getKeyCode(rowParts[1].trim()), Integer.valueOf(rowParts[2].trim())));
                    noteMap.put(Integer.valueOf(rowParts[2].trim()), notes.get(notes.size() - 1));
                } else {
                    notes.add(new Note(rowParts[0].trim(), KeyCode.PRINTSCREEN, Integer.valueOf(rowParts[1].trim())));
                    noteMap.put(Integer.valueOf(rowParts[1].trim()), notes.get(notes.size() - 1));
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
            monitor1.clear();
            monitor2.clear();
            monitor3.clear();
            option=true;
            arrowImageView1.setVisible(false);
            arrowImageView2.setVisible(false);
            arrowImageView3.setVisible(false);
        }));

        arrowImageView1.setVisible(false);
        arrowImageView2.setVisible(false);
        arrowImageView3.setVisible(false);
        //option == true means you are on the spread out version
        fwdButton.setOnAction((event)
                -> {
            if (buttonPressCounter == 1) {
                arrowImageView1.setVisible(true);
                arrowImageView2.setVisible(false);
                arrowImageView3.setVisible(false);
                playNoteCombination(third, seventh, tonic);
                if (option) {
                    monitor1.setText("The Minor Seventh was just played. Next, move finger# 5 down one semitone. Pressing forward will play the next chord (Dominant Seventh.)");
                    third--;
                    tonic = tonic - 7;
                } else {
                    monitor1.setText("false The Minor Seventh was just played. Next, move finger# 1 down one semitone. Pressing forward will play the next chord (Dominant Seventh.)");
                    seventh--;
                    tonic = tonic+5;
                }
                chord1.setText(noteMap.get(tonic).name + " Minor Seventh (ii)");



                buttonPressCounter++;
            } else if (buttonPressCounter == 2) {
                arrowImageView2.setVisible(true);
                playNoteCombination(third, seventh, tonic);
                if (option) {
                    monitor2.setText("The Dominant Seventh was just played. Now, move finger #1 down one semitone. Press forward to play the next chord.");
                    seventh--;
                    tonic=tonic+5;
                } else {
                    monitor2.setText("false The Dominant Seventh was just played. Now, move finger #5 down one semitone. Press forward to play the next chord.");
                    third--;
                    tonic=tonic-7;

                }

                chord2.setText(noteMap.get(tonic).name + " Dominant Seventh (V)");
                buttonPressCounter++;
            } else if (buttonPressCounter == 3) {
                arrowImageView3.setVisible(true);
                playNoteCombination(third, seventh, tonic);
                    monitor3.setText("This is the major Seventh, which is the tonic, or root. By convention, we play this chord twice before move to the next chord. " +
                            "Pressing forward will play the chord again.");
                chord3.setText(noteMap.get(tonic).name + " Major Seventh (I)");
                buttonPressCounter++;
            } else if (buttonPressCounter % 4 == 0) {
                // monitor3.appendText("Major seventh");
                playNoteCombination(third, seventh, tonic);
                if (option) {
                    monitor3.setText("This is the major Seventh, which is the tonic, or root. By convention, we play this chord twice before move to the next chord. " +
                            "To move to the next chord from this position, move fingers 1 and 5 away from each other by one semitone. That will restart our progression" +
                            "on the minor seventh of the next key on the circle of fourths.");
                    third--;
                    seventh++;
                    tonic=tonic-5;
                    option=false;
                } else {
                    monitor3.setText("false This is the major Seventh, which is the tonic, or root. By convention, we play this chord twice before move to the next chord. " +
                            "To move to the next chord from this position, move fingers 1 and 5 toward each other by one semitone. That will restart our progression" +
                            "on the minor seventh of the next key on the circle of fourths.");
                    third++;
                    seventh--;
                    tonic=tonic+7;
                    option = true;
                }

                buttonPressCounter = 1;
                //do nothing.
            }
        });
        stage.setTitle("Jazz Piano Chord Trainer");
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
        controllerPane.setStyle("-fx-background-color: WHITE;");

        controllerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(5))));
        for (int i = 0; i < 16; i++) {
            ColumnConstraints column = new ColumnConstraints(100);
            controllerPane.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 12; i++) {
            RowConstraints row = new RowConstraints(18);
            controllerPane.getRowConstraints().add(row);
        }

        introduction.setText("This application will help you learn to play the standard jazz chord \nprogression, ii V I, in every key, from memory, by teaching the pattern \nbehind it. " +
                "We play across the circle of fourths, starting at C Major.\nThe first three chords are listed above in the three bars of sheet music. \nThe next root chord will be F, so the " +
                "next progression will be\nGm7, C7, Fmaj7. When stepping through the trainer, it will play\nthe current root twice before moving to the next root.");
        introductionHeader.setText("Intoduction:");
        fingeringNote.setText("For simplicities sake, use fingering\n1 and 5 in the right hand.");
        fingeringNote.setFont(Font.font("Arial"));
        introductionHeader.setFont(Font.font("Arial", 30));
        introduction.setFont(Font.font("Arial"));

        controllerPane.setMinSize(400, 350);
        controllerPane.setVgap(7);
        controllerPane.setHgap(5);
        monitor1.setWrapText(true);
        monitor2.setWrapText(true);
        monitor3.setWrapText(true);
        GridPane.setRowSpan(monitor1, 4);
        GridPane.setColumnSpan(monitor1, 5);
        GridPane.setRowSpan(monitor2, 4);
        GridPane.setColumnSpan(monitor2, 5);
        GridPane.setRowSpan(monitor3, 4 );
        GridPane.setColumnSpan(monitor3, 5);
        monitor1.setEditable(false);
        monitor2.setEditable(false);
        monitor3.setEditable(false);

        rstButton.setPrefWidth(80);
        fwdButton.setPrefWidth(80);

        //    controllerPane.setGridLinesVisible(true);

        //i is x axis i1 is Y axis  page.add(Node, colIndex, rowIndex, colSpan, rowSpan):

        controllerPane.add(rstButton, 3, 2);
        controllerPane.add(fwdButton, 3, 0);
        controllerPane.add(new Label("Chord Type:"), 4, 0, 2, 1);
        controllerPane.add(chord1, 4, 1, 2, 1);
        controllerPane.add(chord2, 4, 2, 2, 1);
        controllerPane.add(chord3, 4, 3, 2, 1);

        controllerPane.add(monitor1, 6, 0);
        controllerPane.add(monitor2, 6, 3);
        controllerPane.add(monitor3, 6, 6);

        arrowImageView1.setFitHeight(20);
        arrowImageView1.setFitWidth(30);
        arrowImageView2.setFitHeight(20);
        arrowImageView2.setFitWidth(30);
        arrowImageView3.setFitHeight(20);
        arrowImageView3.setFitWidth(30);
        notesImageView.setFitWidth(250);
        notesImageView.setFitHeight(100);
        controllerPane.add(arrowImageView1, 5, 0);
        controllerPane.add(arrowImageView2, 5, 3);
        controllerPane.add(arrowImageView3, 5, 6);
        controllerPane.add(handsImageView, 1, 4);
        controllerPane.add(notesImageView, 12, 6);
        controllerPane.add(introduction, 12, 1, 4, 6);
        controllerPane.add(introductionHeader, 12, 0, 2, 1);
        controllerPane.add(fingeringNote, 1, 9, 2, 2);
        GridPane.setHalignment(arrowImageView1, HPos.RIGHT);
        GridPane.setHalignment(arrowImageView2, HPos.RIGHT);
        GridPane.setHalignment(arrowImageView3, HPos.RIGHT);
        GridPane.setHalignment(rstButton, HPos.CENTER);
        GridPane.setHalignment(fwdButton, HPos.CENTER);
        GridPane.setValignment(notesImageView, VPos.TOP);
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

        public String getName() {
            return name;

        }
    }

    public static void main(String[] args) {
launch(args);
    }
}