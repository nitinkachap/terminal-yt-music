package com.musicstreamer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.Executors;

public class MusicApp extends Application {
    private YouTubeMusicService musicService;
    private VBox searchResultsContainer;
    private Label nowPlayingLabel;
    private MediaPlayer mediaPlayer;
    private Button playPauseButton;
    private Slider progressSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private YouTubeMusicService.Song currentSong;
    
    @Override
    public void start(Stage primaryStage) {
        musicService = new YouTubeMusicService();
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e 0%, #16213e 100%);");
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Center content (search + results)
        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);
        
        // Bottom player controls
        VBox playerControls = createPlayerControls();
        root.setBottom(playerControls);
        
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("🎵 Java Music Streamer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load default songs
        searchSongs("bohemian rhapsody");
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        
        Label title = new Label("🎵 Java Music Streamer");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Stream music from YouTube Music");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.rgb(200, 200, 200));
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }
    
    private VBox createCenterContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setMaxWidth(600);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search for any song...");
        searchField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.5);" +
            "-fx-background-radius: 25;" +
            "-fx-padding: 12 20;" +
            "-fx-font-size: 14px;"
        );
        searchField.setPrefWidth(400);
        
        Button searchButton = new Button("🔍 Search");
        searchButton.setStyle(
            "-fx-background-color: #ff4757;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 25;" +
            "-fx-padding: 12 25;" +
            "-fx-font-weight: bold;"
        );
        searchButton.setOnAction(e -> searchSongs(searchField.getText()));
        
        searchField.setOnAction(e -> searchSongs(searchField.getText()));
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Results area with scroll
        searchResultsContainer = new VBox(8);
        searchResultsContainer.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(searchResultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        content.getChildren().addAll(searchBox, scrollPane);
        return content;
    }
    
    private VBox createPlayerControls() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(15, 20, 20, 20));
        controls.setStyle(
            "-fx-background-color: rgba(0,0,0,0.8);" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        // Progress bar
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER);
        
        currentTimeLabel = new Label("0:00");
        currentTimeLabel.setTextFill(Color.WHITE);
        currentTimeLabel.setFont(Font.font("System", 12));
        
        progressSlider = new Slider();
        progressSlider.setPrefWidth(400);
        progressSlider.setStyle("-fx-control-inner-background: #ff4757;");
        
        totalTimeLabel = new Label("0:00");
        totalTimeLabel.setTextFill(Color.WHITE);
        totalTimeLabel.setFont(Font.font("System", 12));
        
        progressBox.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);
        
        // Now playing info
        nowPlayingLabel = new Label("No song playing");
        nowPlayingLabel.setTextFill(Color.rgb(200, 200, 200));
        nowPlayingLabel.setFont(Font.font("System", 14));
        
        // Control buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button prevButton = createControlButton("⏮", "#4a4a4a");
        playPauseButton = createControlButton("▶", "#ff4757");
        Button nextButton = createControlButton("⏭", "#4a4a4a");
        Button stopButton = createControlButton("⏹", "#ff4757");
        
        prevButton.setOnAction(e -> playPrevious());
        playPauseButton.setOnAction(e -> togglePlayPause());
        nextButton.setOnAction(e -> playNext());
        stopButton.setOnAction(e -> stopPlayback());
        
        buttonBox.getChildren().addAll(prevButton, playPauseButton, stopButton, nextButton);
        
        controls.getChildren().addAll(progressBox, nowPlayingLabel, buttonBox);
        return controls;
    }
    
    private Button createControlButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 20;" +
            "-fx-min-width: 60;" +
            "-fx-min-height: 60;" +
            "-fx-background-radius: 30;" +
            "-fx-cursor: hand;"
        );
        return button;
    }
    
    private void searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) return;
        
        // Show loading indicator
        searchResultsContainer.getChildren().clear();
        Label loadingLabel = new Label("🎵 Loading songs...");
        loadingLabel.setTextFill(Color.WHITE);
        loadingLabel.setFont(Font.font("System", 14));
        searchResultsContainer.getChildren().add(loadingLabel);
        
        // Search in background thread
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<YouTubeMusicService.Song> songs = musicService.searchSongs(query);
                
                Platform.runLater(() -> {
                    searchResultsContainer.getChildren().clear();
                    
                    if (songs.isEmpty()) {
                        Label noResults = new Label("No songs found. Try another search.");
                        noResults.setTextFill(Color.WHITE);
                        searchResultsContainer.getChildren().add(noResults);
                        return;
                    }
                    
                    for (YouTubeMusicService.Song song : songs) {
                        HBox songItem = createSongItem(song);
                        searchResultsContainer.getChildren().add(songItem);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    searchResultsContainer.getChildren().clear();
                    Label errorLabel = new Label("❌ Error: " + e.getMessage());
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setWrapText(true);
                    searchResultsContainer.getChildren().add(errorLabel);
                });
                e.printStackTrace();
            }
        });
    }
    
    private HBox createSongItem(YouTubeMusicService.Song song) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;"
        );
        
        // Thumbnail
        ImageView thumbnail = new ImageView();
        thumbnail.setFitWidth(50);
        thumbnail.setFitHeight(50);
        thumbnail.setStyle("-fx-background-color: #333; -fx-background-radius: 5;");
        
        // Load thumbnail if available
        if (song.thumbnail != null && !song.thumbnail.isEmpty()) {
            try {
                Image img = new Image(song.thumbnail, true);
                thumbnail.setImage(img);
            } catch (Exception e) {
                // Use default
                thumbnail.setStyle("-fx-background-color: #ff4757; -fx-background-radius: 5;");
            }
        }
        
        // Song info
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);  // FIXED: Changed from setHgrow to HBox.setHgrow
        
        Label titleLabel = new Label(song.title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label artistLabel = new Label(song.artist != null ? song.artist : "Unknown Artist");
        artistLabel.setTextFill(Color.rgb(180, 180, 180));
        artistLabel.setFont(Font.font("System", 12));
        
        infoBox.getChildren().addAll(titleLabel, artistLabel);
        
        // Duration
        Label durationLabel = new Label(song.duration != null ? song.duration : "0:00");
        durationLabel.setTextFill(Color.rgb(150, 150, 150));
        durationLabel.setFont(Font.font("System", 12));
        
        // Play button
        Button playButton = new Button("▶");
        playButton.setStyle(
            "-fx-background-color: #ff4757;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20;" +
            "-fx-min-width: 36;" +
            "-fx-min-height: 36;"
        );
        playButton.setOnAction(e -> playSong(song));
        
        item.getChildren().addAll(thumbnail, infoBox, durationLabel, playButton);
        
        // Hover effect
        item.setOnMouseEntered(e -> 
            item.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10;")
        );
        item.setOnMouseExited(e -> 
            item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;")
        );
        
        return item;
    }
    
    private void playSong(YouTubeMusicService.Song song) {
        currentSong = song;
        nowPlayingLabel.setText("Now Playing: " + song.title + " - " + song.artist);
        
        // Stop current playback
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        
        // Play in background
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                // Get streaming URL
                String streamUrl = musicService.getStreamUrl(song.id);
                
                if (streamUrl != null && !streamUrl.isEmpty()) {
                    final String finalUrl = streamUrl;
                    Platform.runLater(() -> {
                        try {
                            Media media = new Media(finalUrl);
                            mediaPlayer = new MediaPlayer(media);
                            mediaPlayer.setOnReady(() -> {
                                if (media.getDuration() != null) {
                                    totalTimeLabel.setText(formatDuration(media.getDuration()));
                                    progressSlider.setMax(media.getDuration().toSeconds());
                                }
                            });
                            mediaPlayer.currentTimeProperty().addListener((obs, old, newTime) -> {
                                if (newTime != null) {
                                    progressSlider.setValue(newTime.toSeconds());
                                    currentTimeLabel.setText(formatDuration(newTime));
                                }
                            });
                            mediaPlayer.setOnEndOfMedia(() -> playNext());
                            mediaPlayer.play();
                            playPauseButton.setText("⏸");
                        } catch (Exception e) {
                            nowPlayingLabel.setText("Error playing: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        nowPlayingLabel.setText("Could not get stream URL for: " + song.title);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    nowPlayingLabel.setText("Error: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }
    
    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("▶");
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
                playPauseButton.setText("⏸");
            }
        }
    }
    
    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            playPauseButton.setText("▶");
            nowPlayingLabel.setText("Playback stopped");
            progressSlider.setValue(0);
            currentTimeLabel.setText("0:00");
        }
    }
    
    private void playPrevious() {
        // TODO: Implement playlist history
        nowPlayingLabel.setText("Previous song feature coming soon");
    }
    
    private void playNext() {
        // TODO: Implement playlist queue
        nowPlayingLabel.setText("Next song feature coming soon");
    }
    
    private String formatDuration(Duration duration) {
        if (duration == null) return "0:00";
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        try {
            musicService.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
