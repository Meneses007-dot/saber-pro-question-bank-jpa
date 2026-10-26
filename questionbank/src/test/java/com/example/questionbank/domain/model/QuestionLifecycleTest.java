package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests of the question lifecycle and of the content rules.
 *
 * <p>No Spring, JPA or database is involved.</p>
 */
class QuestionLifecycleTest {

    private MultipleChoiceQuestion multipleChoice(Set<Long> competencyIds) {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null,
                "Which strategy uses a base table plus one table per subclass?",
                Difficulty.MEDIUM, QuestionStatus.DRAFT, 10L, 20L, competencyIds, null, null);
        for (int index = 1; index <= 5; index++) {
            question.addOption("Option " + index, index == 1);
        }
        return question;
    }

    private TrueFalseQuestion trueFalse(Boolean expectedAnswer) {
        return new TrueFalseQuestion(null, "JOINED stores common columns in a base table",
                Difficulty.EASY, QuestionStatus.DRAFT, 10L, 20L, Set.of(30L), null, expectedAnswer);
    }

    @Test
    void newQuestionsStartAsDraft() {
        assertThat(multipleChoice(Set.of(30L)).getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void submittingAValidQuestionMovesItToPendingReview() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));

        question.submitForReview();

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void theHappyPathReachesPublished() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));

        question.submitForReview();
        question.startReview();
        question.approve();
        question.publish();

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PUBLISHED);
        assertThat(question.isPublished()).isTrue();
    }

    @Test
    void rejectingFromInReviewReturnsToDraft() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));
        question.submitForReview();
        question.startReview();

        question.reject();

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void rejectingFromPendingReviewReturnsToDraft() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));
        question.submitForReview();

        question.reject();

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void anIllegalTransitionIsRejected() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));

        assertThatThrownBy(question::approve)
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("APPROVED");
    }

    @Test
    void aPublishedQuestionCannotBePublishedTwice() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));
        question.submitForReview();
        question.startReview();
        question.approve();
        question.publish();

        assertThatThrownBy(question::publish).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void anArchivedQuestionCannotChangeState() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));
        question.submitForReview();
        question.startReview();
        question.approve();
        question.publish();
        question.archive();

        assertThatThrownBy(question::publish).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void aQuestionWithoutCompetenciesCannotBeSubmitted() {
        MultipleChoiceQuestion question = multipleChoice(Set.of());

        assertThatThrownBy(question::submitForReview)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("competency");
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void aMultipleChoiceQuestionNeedsExactlyFiveOptions() {
        MultipleChoiceQuestion withFour = multipleChoice(Set.of(30L));
        MultipleChoiceQuestion fourOptions = new MultipleChoiceQuestion(null, "Statement",
                Difficulty.EASY, QuestionStatus.DRAFT, 10L, 20L, Set.of(30L), null,
                withFour.getOptions().subList(0, 4));
        MultipleChoiceQuestion sixOptions = multipleChoice(Set.of(30L));
        sixOptions.addOption("Option 6", false);

        assertThatThrownBy(fourOptions::submitForReview)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("exactly 5 options");
        assertThatThrownBy(sixOptions::submitForReview)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("exactly 5 options");
    }

    @Test
    void aMultipleChoiceQuestionNeedsExactlyOneCorrectOption() {
        MultipleChoiceQuestion twoCorrect = new MultipleChoiceQuestion(null, "Statement",
                Difficulty.EASY, QuestionStatus.DRAFT, 10L, 20L, Set.of(30L), null,
                List.of(new AnswerOption("Option 1", true), new AnswerOption("Option 2", true),
                        new AnswerOption("Option 3", false), new AnswerOption("Option 4", false),
                        new AnswerOption("Option 5", false)));
        MultipleChoiceQuestion base = multipleChoice(Set.of(30L));
        MultipleChoiceQuestion noCorrect = new MultipleChoiceQuestion(null, "Statement",
                Difficulty.EASY, QuestionStatus.DRAFT, 10L, 20L, Set.of(30L), null,
                base.getOptions().stream()
                        .map(option -> new AnswerOption(option.getText(), false))
                        .toList());

        assertThatThrownBy(twoCorrect::submitForReview)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("exactly one correct");
        assertThatThrownBy(noCorrect::submitForReview)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("exactly one correct");
    }

    @Test
    void aTrueFalseQuestionNeedsAnExpectedAnswer() {
        assertThatThrownBy(() -> trueFalse(null).submitForReview())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("expected answer");
    }

    @Test
    void aCompleteTrueFalseQuestionCanBeSubmitted() {
        TrueFalseQuestion question = trueFalse(Boolean.TRUE);

        assertThatCode(question::submitForReview).doesNotThrowAnyException();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void classifyingAQuestionReplacesTopicAndCompetencies() {
        MultipleChoiceQuestion question = multipleChoice(Set.of(30L));

        question.classify(99L, Set.of(40L, 41L));

        assertThat(question.getTopicId()).isEqualTo(99L);
        assertThat(question.getCompetencyIds()).containsExactlyInAnyOrder(40L, 41L);
    }

    @Test
    void aBlankStatementIsRejected() {
        assertThatThrownBy(() -> new TrueFalseQuestion(null, "  ", Difficulty.EASY,
                QuestionStatus.DRAFT, 10L, 20L, Set.of(30L), null, true))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("statement");
    }

    @Test
    void theStatusMachineExposesOnlyLegalTransitions() {
        assertThat(QuestionStatus.DRAFT.allowedTransitions())
                .containsExactly(QuestionStatus.PENDING_REVIEW);
        assertThat(QuestionStatus.PUBLISHED.allowedTransitions())
                .containsExactly(QuestionStatus.ARCHIVED);
        assertThat(QuestionStatus.ARCHIVED.allowedTransitions()).isEmpty();
    }
}
