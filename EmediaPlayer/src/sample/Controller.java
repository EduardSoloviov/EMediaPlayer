package sample;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.scene.media.Media;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private Timeline slideIn = new Timeline();

    private Timeline slideOut = new Timeline();

    @FXML
    private BorderPane root;

    @FXML
     private VBox vBox;

    private MediaPlayer player;

    @FXML
    private Slider timeSlider;


    @FXML
    private Slider volumeSlider = new Slider(0.0,0.0,50);

    @FXML
    private MediaView view;

    @FXML
    private Button resizeScreen;



    @FXML
    private void handleButtonAction(){
        if(view.getMediaPlayer()==null) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Select a file extension (*.mp4)",
                    "*.mp4");
            fileChooser.getExtensionFilters().add(filter);
            File file = fileChooser.showOpenDialog(null);
            String filepath = file.toURI().toString();
            if (filepath != null) {
                Media media = new Media(filepath);
                player = new MediaPlayer(media);
                player.setVolume(volumeSlider.getValue()/100);
                view.setMediaPlayer(player);
                DoubleProperty width = view.fitWidthProperty();
                DoubleProperty height = view.fitHeightProperty();
                width.bind(Bindings.selectDouble(view.sceneProperty(), "width"));
                height.bind(Bindings.selectDouble(view.sceneProperty(), "height"));
                player.setOnEndOfMedia(()->{
                    player.seek(Duration.seconds(0.0));
                    player.stop();
                });

                volumeSlider.valueProperty().addListener((observable -> player.setVolume(volumeSlider.getValue()/100)));
                player.setOnReady(() -> {
                    timeSlider.setValue(0.0);
                    timeSlider.setMin(0.0);
                    timeSlider.setMax(player.getTotalDuration().toSeconds());

                    player.currentTimeProperty().addListener((observable, oldValue, newValue) -> timeSlider.setValue(newValue.toSeconds()));
                    timeSlider.setOnMouseDragged(event1 -> player.seek(Duration.seconds(timeSlider.getValue())));
                    timeSlider.setOnMousePressed(event1 -> player.seek(Duration.seconds(timeSlider.getValue())));
                });
                root.setOnMouseEntered(e -> slideIn.play());
                root.setOnMouseExited(e -> slideOut.play());
                PauseTransition idle = new PauseTransition(Duration.seconds(5));
                idle.setOnFinished(e -> {
                    slideOut.play();
                    root.setCursor(Cursor.NONE);
                });
                root.addEventHandler(Event.ANY, e -> {
                    idle.playFromStart();
                    if(Cursor.NONE ==root.getCursor())
                        slideIn.play();
                    root.setCursor(Cursor.DEFAULT);
                });

                slideIn.getKeyFrames().addAll(new KeyFrame(new Duration(0),
                        new KeyValue(vBox.translateYProperty(), player.getMedia().getHeight()),
                        new KeyValue(vBox.opacityProperty(),0.0)
                ),new KeyFrame(new Duration(1000),
                        new KeyValue(vBox.translateYProperty(),player.getMedia().getHeight()-25),
                        new KeyValue(vBox.opacityProperty(),1.0)));

                slideOut.getKeyFrames().addAll(new KeyFrame(new Duration(0),
                                new KeyValue(vBox.translateYProperty(),player.getMedia().getHeight()-25),
                                new KeyValue(vBox.opacityProperty(),0.9))

                        ,new KeyFrame(new Duration(1000),
                                new KeyValue(vBox.translateYProperty(),player.getMedia().getHeight()),
                                new KeyValue(vBox.opacityProperty(),0.0)));



                player.play();

            }
        }else{
            new Alert(Alert.AlertType.WARNING,"Please close video and after open new!", ButtonType.OK).showAndWait();
        }




    }


    @FXML
    private void snapShot(){
        if(player.getMedia()!=null){

            try {
                SnapshotParameters parameters = new SnapshotParameters();
                WritableImage wi = new WritableImage((int)root.getWidth(), (int)root.getHeight());
                WritableImage snapshot = view.snapshot(parameters, wi);


                File output = new File(System.getProperty("user.home")+"/Desktop/" + "snapshot" + new Date().getTime() + ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
            }catch(IOException ex){
                ex.getMessage();
            }
        }else{
            new Alert(Alert.AlertType.WARNING,"Please choose and open video!",ButtonType.OK).showAndWait();
        }
    }

    @FXML
    private void play(){
        player.setRate(1);
        player.play();

    }

    @FXML
    private void pause(){
        player.pause();

    }

    @FXML
    private void stop(){
        player.stop();

    }

    @FXML
    private void slowest(){
        player.setRate(0.5);

    }

    @FXML
    private void slower(){
        player.setRate(0.75);
    }

    @FXML
    private void faster(){
        player.setRate(1.25);

    }

    @FXML
    private void fastest(){
        player.setRate(1.5);
    }

    @FXML
    private void closeVideo(){
        player.stop();
        view.setMediaPlayer(null);
    }

    private Background makeBackgroundIcon(String filepath,double backgroundWidth,double backgroundHeight){
        Image icon = new Image(getClass().getResourceAsStream(filepath));
        BackgroundSize backgroundSize = new BackgroundSize(backgroundWidth,backgroundHeight,true,
                true,true,false);
        BackgroundImage backgroundImage = new BackgroundImage(icon,BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,BackgroundPosition.CENTER,backgroundSize);
        return new Background(backgroundImage);
    }

    @FXML
    private void resize_screen(){
        Stage stage = (Stage)resizeScreen.getScene().getWindow();

            if(!stage.isFullScreen() ){
                stage.setFullScreen(true);
                resizeScreen.setBackground(makeBackgroundIcon("\\icons\\minimize.png",20.0,20.0));

            }else {
                stage.setFullScreen(false);
                resizeScreen.setBackground(makeBackgroundIcon("\\icons\\fullscreen.png",22.0,22.0));

            }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resizeScreen.setBackground(makeBackgroundIcon("\\icons\\fullscreen.png",20.0,20.0));
    }
}
