import Attempt;
import AttemptKind;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class Home {
    private final AudioClip mGong;
    private final AudioClip mApplause;

    @FXML
    private VBox container;

    @FXML
    private TextArea message;

    @FXML
    private Label title;
    private Attempt mCurrentAttempt;
    private StringProperty mTimerText;
    private Timeline mTimeline;

    public Home() {
        mTimerText = new SimpleStringProperty();
        setTimerText(0);
        mGong = new AudioClip(getClass().getResource("/sounds/chinese-gong-daniel_simon.mp3").toExternalForm());
        mApplause = new AudioClip(getClass().getResource("/sounds/applause7.mp3").toExternalForm());

    }

    public String getTimerText() {
        return mTimerText.get();
    }

    public StringProperty timerTextProperty() {
        return mTimerText;
    }

    public void setTimerText(String timerText) {
        this.mTimerText.set(timerText);
    }

    public void setTimerText(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        setTimerText(String.format("%02d:%02d", minutes, seconds));

    }

    private void prepareAttempt(AttemptKind kind) {
        resetTimer();
        mCurrentAttempt = new Attempt(kind, "");
        addAttemptStyle(kind);
        title.setText(kind.getDisplayName());
        setTimerText(mCurrentAttempt.getRemainingSeconds());
        // TODO: GB This is creating multiple timelines - needs fixing.
        mTimeline = new Timeline();
        mTimeline.setCycleCount(kind.getTotalSeconds());
        mTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
                 mCurrentAttempt.tick();
                 setTimerText(mCurrentAttempt.getRemainingSeconds());
        }));
        mTimeline.setOnFinished(e -> {
            saveCurrentAttempt();
            if(mCurrentAttempt.getKind() == AttemptKind.FOCUS){
                mApplause.play();
            } else {
                mGong.play();
            }
            prepareAttempt(mCurrentAttempt.getKind() == AttemptKind.FOCUS ?
                            AttemptKind.BREAK : AttemptKind.FOCUS);
            //TODO GB: make gong play for end of break and applause for end of focus
        });
    }

    private void saveCurrentAttempt() {
        mCurrentAttempt.setMessage(message.getText());
        mCurrentAttempt.save();
        // TODO: GB Create proper method to save
    }

    private void resetTimer() {
        clearAttemptStyles();
        if (mTimeline != null && mTimeline.getStatus() == Animation.Status.RUNNING) {
            mTimeline.stop();
        }
    }

    public void playTimer() {
        container.getStyleClass().add("playing");
        mTimeline.play();
    }

    public void pauseTimer() {
        container.getStyleClass().remove("playing");
        mTimeline.pause();
    }

    private void addAttemptStyle(AttemptKind kind) {
        container.getStyleClass().add(kind.toString().toLowerCase());
    }
    private void clearAttemptStyles() {
        container.getStyleClass().remove("playing");
    for (AttemptKind kind : AttemptKind.values()) {
        container.getStyleClass().remove(kind.toString().toLowerCase());
    }
    }



    public void handleRestart(ActionEvent actionEvent) {
        prepareAttempt(AttemptKind.FOCUS);
        playTimer();
    }

    public void handlePlay(ActionEvent actionEvent) {
        if(mCurrentAttempt == null) {
            handleRestart(actionEvent);
        }else {
            playTimer();
        }
    }

    public void handlePause(ActionEvent actionEvent) {
        pauseTimer();
    }
}
