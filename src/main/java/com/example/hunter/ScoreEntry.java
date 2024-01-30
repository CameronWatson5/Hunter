package com.example.hunter;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ScoreEntry implements Comparable<ScoreEntry> {
    private final String playerName;
    private final int score;

    public ScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(ScoreEntry other) {
        return Integer.compare(other.score, this.score); // Sort in descending order
    }

    // Save scores to Preferences
    public static void saveScores(List<ScoreEntry> scores) {
        Preferences prefs = Preferences.userNodeForPackage(ScoreEntry.class);
        String scoresString = scores.stream()
                .map(entry -> entry.getPlayerName() + ":" + entry.getScore())
                .collect(Collectors.joining(","));
        prefs.put("scores", scoresString);
    }

    // Load scores from Preferences
    public static List<ScoreEntry> loadScores() {
        Preferences prefs = Preferences.userNodeForPackage(ScoreEntry.class);
        String scoresString = prefs.get("scores", "");
        return Arrays.stream(scoresString.split(","))
                .filter(s -> !s.isEmpty())
                .map(s -> s.split(":"))
                .map(parts -> new ScoreEntry(parts[0], Integer.parseInt(parts[1])))
                .sorted()
                .collect(Collectors.toList());
    }
}


