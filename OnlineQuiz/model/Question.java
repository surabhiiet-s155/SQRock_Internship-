package com.quiz.model;

public class Question {
    private int id, categoryId;
    private String categoryName, text;
    private String[] options = new String[4];
    private char correct;   // 'A'..'D'

    public Question() {}
    public Question(int id, int categoryId, String text, String a, String b, String c, String d, char correct) {
        this.id = id; this.categoryId = categoryId; this.text = text;
        options = new String[]{a, b, c, d}; this.correct = correct;
    }
    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String n) { categoryName = n; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public char getCorrect() { return correct; }
    /** Index 0..3 of the correct option. */
    public int getCorrectIndex() { return correct - 'A'; }
}
