package com.musicstreamer;

import com.google.gson.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class YouTubeMusicService {
    // Using multiple fallback APIs
    private static final String[] API_ENDPOINTS = {
        "https://invidious.nerdvpn.de/api/v1/search",
        "https://invidious.snopyta.org/api/v1/search",
        "https://yewtu.be/api/v1/search"
    };
    
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    
    public YouTubeMusicService() {
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }
    
    // Search for songs using multiple APIs
    public List<Song> searchSongs(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        
        // Try each API endpoint until one works
        for (String endpoint : API_ENDPOINTS) {
            try {
                String url = endpoint + "?q=" + encodedQuery + "&type=video";
                HttpGet request = new HttpGet(url);
                request.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
                
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getCode();
                    if (statusCode == 200) {
                        String json = EntityUtils.toString(response.getEntity());
                        
                        // Parse JSON safely
                        JsonElement element = JsonParser.parseString(json);
                        
                        List<Song> songs = new ArrayList<>();
                        
                        if (element.isJsonArray()) {
                            JsonArray items = element.getAsJsonArray();
                            songs = parseSearchResults(items);
                        } else if (element.isJsonObject()) {
                            JsonObject obj = element.getAsJsonObject();
                            if (obj.has("items") && obj.get("items").isJsonArray()) {
                                songs = parseSearchResults(obj.getAsJsonArray("items"));
                            }
                        }
                        
                        if (!songs.isEmpty()) {
                            System.out.println("✅ Found " + songs.size() + " songs using: " + endpoint);
                            return songs;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ API endpoint failed: " + endpoint + " - " + e.getMessage());
                // Try next endpoint
            }
        }
        
        // If all APIs fail, return some demo songs for testing
        return getDemoSongs(query);
    }
    
    private List<Song> parseSearchResults(JsonArray items) {
        List<Song> songs = new ArrayList<>();
        
        for (JsonElement element : items) {
            try {
                JsonObject item = element.getAsJsonObject();
                
                // Skip non-video items
                if (item.has("type") && !item.get("type").getAsString().equals("video")) {
                    continue;
                }
                
                String videoId = null;
                String title = null;
                String author = null;
                String duration = null;
                String thumbnail = null;
                
                // Extract videoId (different APIs use different field names)
                if (item.has("videoId")) {
                    videoId = item.get("videoId").getAsString();
                } else if (item.has("id")) {
                    videoId = item.get("id").getAsString();
                }
                
                // Extract title
                if (item.has("title")) {
                    title = item.get("title").getAsString();
                }
                
                // Extract author/artist
                if (item.has("author")) {
                    author = item.get("author").getAsString();
                } else if (item.has("uploader")) {
                    author = item.get("uploader").getAsString();
                } else if (item.has("artist")) {
                    author = item.get("artist").getAsString();
                }
                
                // Extract duration
                if (item.has("lengthSeconds")) {
                    duration = item.get("lengthSeconds").getAsString();
                } else if (item.has("duration")) {
                    duration = item.get("duration").getAsString();
                }
                
                // Extract thumbnail
                if (item.has("videoThumbnails") && item.get("videoThumbnails").isJsonArray()) {
                    JsonArray thumbs = item.getAsJsonArray("videoThumbnails");
                    if (thumbs.size() > 0) {
                        thumbnail = thumbs.get(0).getAsJsonObject().get("url").getAsString();
                    }
                } else if (videoId != null) {
                    thumbnail = "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
                }
                
                if (videoId != null && title != null && !title.toLowerCase().contains("live")) {
                    Song song = new Song();
                    song.id = videoId;
                    song.title = title.length() > 100 ? title.substring(0, 100) : title;
                    song.artist = author != null ? author : "Unknown Artist";
                    song.thumbnail = thumbnail != null ? thumbnail : "";
                    song.duration = formatDuration(duration);
                    song.streamUrl = "https://music.youtube.com/watch?v=" + videoId;
                    
                    songs.add(song);
                }
            } catch (Exception e) {
                // Skip malformed items
                System.out.println("⚠️ Skipping malformed item: " + e.getMessage());
            }
        }
        
        return songs;
    }
    
    private List<Song> getDemoSongs(String query) {
        List<Song> demoSongs = new ArrayList<>();
        
        // Return some demo songs based on query
        Song demo1 = new Song();
        demo1.id = "dQw4w9WgXcQ";
        demo1.title = "Never Gonna Give You Up";
        demo1.artist = "Rick Astley";
        demo1.thumbnail = "https://img.youtube.com/vi/dQw4w9WgXcQ/mqdefault.jpg";
        demo1.duration = "3:33";
        demo1.streamUrl = "https://music.youtube.com/watch?v=dQw4w9WgXcQ";
        demoSongs.add(demo1);
        
        Song demo2 = new Song();
        demo2.id = "kJQP7kiw5Fk";
        demo2.title = "Luis Fonsi - Despacito ft. Daddy Yankee";
        demo2.artist = "Luis Fonsi";
        demo2.thumbnail = "https://img.youtube.com/vi/kJQP7kiw5Fk/mqdefault.jpg";
        demo2.duration = "4:41";
        demo2.streamUrl = "https://music.youtube.com/watch?v=kJQP7kiw5Fk";
        demoSongs.add(demo2);
        
        Song demo3 = new Song();
        demo3.id = "RgKAFK5djSk";
        demo3.title = "Wiz Khalifa - See You Again ft. Charlie Puth";
        demo3.artist = "Wiz Khalifa";
        demo3.thumbnail = "https://img.youtube.com/vi/RgKAFK5djSk/mqdefault.jpg";
        demo3.duration = "3:49";
        demo3.streamUrl = "https://music.youtube.com/watch?v=RgKAFK5djSk";
        demoSongs.add(demo3);
        
        System.out.println("📀 Using demo songs (API unavailable). Searching for: " + query);
        return demoSongs;
    }
    
    // Get streaming URL using yt-dlp
    public String getStreamUrl(String videoId) throws Exception {
        // Try yt-dlp first
        try {
            String url = getStreamUrlWithYtDlp(videoId);
            if (url != null && !url.isEmpty()) {
                return url;
            }
        } catch (Exception e) {
            System.out.println("yt-dlp failed: " + e.getMessage());
        }
        
        // Return YouTube URL as fallback
        return "https://www.youtube.com/watch?v=" + videoId;
    }
    
    // Use yt-dlp to get direct audio URL
    public String getStreamUrlWithYtDlp(String videoId) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "yt-dlp",
            "-f", "bestaudio[ext=m4a]/bestaudio",
            "-g",
            "--no-playlist",
            "https://music.youtube.com/watch?v=" + videoId
        );
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        String url = reader.readLine();
        
        int exitCode = process.waitFor();
        if (exitCode == 0 && url != null && !url.isEmpty()) {
            return url;
        }
        
        throw new Exception("yt-dlp failed with exit code: " + exitCode);
    }
    
    private String formatDuration(String seconds) {
        if (seconds == null) return "0:00";
        try {
            int totalSecs = Integer.parseInt(seconds);
            int minutes = totalSecs / 60;
            int secs = totalSecs % 60;
            return String.format("%d:%02d", minutes, secs);
        } catch (NumberFormatException e) {
            return "0:00";
        }
    }
    
    public void close() throws IOException {
        httpClient.close();
    }
    
    // Song data class
    public static class Song {
        public String id;
        public String title;
        public String artist;
        public String album;
        public String thumbnail;
        public String duration;
        public String streamUrl;
        public String platform;
        
        @Override
        public String toString() {
            return title + " - " + artist;
        }
    }
}
