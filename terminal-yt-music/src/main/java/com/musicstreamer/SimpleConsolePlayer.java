import java.io.*;
import java.util.Scanner;

public class SimpleYTPlayer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n🎵 YouTube Music Player");
        System.out.println("=====================");
        System.out.print("\n🎤 Search for a song: ");
        String query = scanner.nextLine();
        
        if (query.trim().isEmpty()) {
            System.out.println("Please enter a search term.");
            return;
        }
        
        try {
            // Search using yt-dlp
            System.out.println("\n🔍 Searching for: " + query);
            
            ProcessBuilder searchPb = new ProcessBuilder(
                "yt-dlp",
                "--get-title",
                "--get-id",
                "--playlist-end", "5",
                "ytsearch5:" + query
            );
            
            Process searchProcess = searchPb.start();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(searchProcess.getInputStream())
            );
            
            String line;
            int count = 0;
            String[] titles = new String[5];
            String[] ids = new String[5];
            
            while ((line = reader.readLine()) != null && count < 10) {
                if (count % 2 == 0) {
                    titles[count/2] = line;
                } else {
                    ids[count/2] = line;
                }
                count++;
            }
            
            searchProcess.waitFor();
            
            if (count == 0) {
                System.out.println("No results found.");
                return;
            }
            
            // Display results
            System.out.println("\n📀 Results:");
            for (int i = 0; i < count/2; i++) {
                System.out.println((i+1) + ". " + titles[i]);
            }
            
            System.out.print("\n🎯 Select number (1-" + (count/2) + "): ");
            int choice = scanner.nextInt() - 1;
            
            if (choice >= 0 && choice < count/2) {
                System.out.println("\n🎵 Now playing: " + titles[choice]);
                System.out.println("Press Ctrl+C to stop playback\n");
                
                // Play the selected song
                ProcessBuilder playPb = new ProcessBuilder(
                    "mpv", "--no-video",
                    "https://music.youtube.com/watch?v=" + ids[choice]
                );
                playPb.inheritIO();
                Process playProcess = playPb.start();
                playProcess.waitFor();
            } else {
                System.out.println("Invalid selection.");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("\nMake sure you have installed:");
            System.out.println("  sudo pacman -S yt-dlp mpv");
        }
        
        scanner.close();
    }
}
