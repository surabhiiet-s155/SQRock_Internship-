package com.quiz.model;

import java.util.List;

/** Holds the evaluated outcome of one quiz attempt. */
public class QuizResult {
    public static final double MARK_CORRECT = 1.0;
    public static final double MARK_WRONG   = -0.25;   // negative marking
    public static final double PASS_PERCENT = 40.0;

    public final int total, correct, wrong, unanswered;
    public final double score, percentage;
    public final boolean passed;

    private QuizResult(int total, int correct, int wrong, int unanswered) {
        this.total = total; this.correct = correct; this.wrong = wrong; this.unanswered = unanswered;
        this.score = correct * MARK_CORRECT + wrong * MARK_WRONG;
        this.percentage = total == 0 ? 0 : Math.max(0, score / (total * MARK_CORRECT) * 100.0);
        this.passed = percentage >= PASS_PERCENT;
    }

    /** answers[i] = selected option index (0..3) or -1 if skipped. */
    public static QuizResult evaluate(List<Question> qs, int[] answers) {
        int c = 0, w = 0, u = 0;
        for (int i = 0; i < qs.size(); i++) {
            if (answers[i] < 0) u++;
            else if (answers[i] == qs.get(i).getCorrectIndex()) c++;
            else w++;
        }
        return new QuizResult(qs.size(), c, w, u);
    }

    public String performance() {
        if (percentage >= 90) return "Outstanding! You have mastered this topic.";
        if (percentage >= 75) return "Excellent work! Just a little more revision needed.";
        if (percentage >= 60) return "Good job! Keep practising to improve further.";
        if (percentage >= PASS_PERCENT) return "You passed, but there is room to improve. Review the wrong answers.";
        return "Needs improvement. Revise the topic and try again.";
    }
}
