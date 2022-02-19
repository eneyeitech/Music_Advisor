package advisor.presentation;

import java.util.Locale;
import java.util.Scanner;

public class MusicAdvisor {
    private final Scanner scanner;

    public MusicAdvisor() {
        scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            userPrompt();
        }
    }

    public void userPrompt() {
        String input = scanner.next();
        choices(input);

    }

    public void choices(String s) {
        switch (s.toLowerCase()) {
            case "featured":
                featured();
                break;
            case "new":
                newSelected();
                break;
            case "categories":
                categories();
                break;
            case "playlists":
                String category = scanner.next();
                playlist(category);
                break;
            case "exit":
                System.out.println("---GOODBYE!---");
                System.exit(0);
                break;
        }
    }

    public void featured() {
        System.out.println("---FEATURED---");
        System.out.println("Wake Up and Smell the Coffee");
    }
    public void newSelected() {
        System.out.println("---NEW RELEASES---");
        System.out.println("Mountains [Sia, Diplo, Labrinth]");
    }
    public void categories() {
        System.out.println("---CATEGORIES---");
        System.out.println("Mood");
        System.out.println("Pop");
    }
    public void playlist(String cat) {
        System.out.printf("---%s PLAYLISTS---\n", cat.toUpperCase(Locale.ROOT));
        System.out.println("Mood");
    }

}
