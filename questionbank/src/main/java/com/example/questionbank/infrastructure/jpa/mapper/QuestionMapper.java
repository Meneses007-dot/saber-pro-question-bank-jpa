package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.infrastructure.jpa.record.AnswerOptionRecord;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.MultipleChoiceQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.TrueFalseQuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Translates the question aggregate between the domain and the JPA model.
 *
 * <p>This is the only class that knows how domain questions and JPA records
 * correspond, which keeps the domain free of persistence decisions.</p>
 */
public final class QuestionMapper {

    private QuestionMapper() {
    }

    /**
     * Converts a JPA record to its domain representation.
     *
     * @param record JPA record
     * @return domain question
     * @throws DomainException when the record type is unknown
     */
    public static Question toDomain(QuestionRecord record) {
        Set<Long> competencyIds = record.getCompetencies().stream()
                .map(CompetencyRecord::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        QuestionMetadata metadata = toDomain(record.getMetadata());
        if (record instanceof MultipleChoiceQuestionRecord multipleChoice) {
            List<AnswerOption> options = multipleChoice.getOptions().stream()
                    .map(QuestionMapper::toDomain)
                    .toList();
            return new MultipleChoiceQuestion(multipleChoice.getId(), multipleChoice.getStatement(),
                    multipleChoice.getDifficulty(), multipleChoice.getStatus(),
                    multipleChoice.getAuthor().getId(), multipleChoice.getTopic().getId(),
                    competencyIds, metadata, options);
        }
        if (record instanceof TrueFalseQuestionRecord trueFalse) {
            return new TrueFalseQuestion(trueFalse.getId(), trueFalse.getStatement(),
                    trueFalse.getDifficulty(), trueFalse.getStatus(),
                    trueFalse.getAuthor().getId(), trueFalse.getTopic().getId(),
                    competencyIds, metadata, trueFalse.getExpectedAnswer());
        }
        throw new DomainException("Unsupported question record type: " + record.getClass().getName());
    }

    /**
     * Builds a new JPA record from a domain question.
     *
     * @param question domain question
     * @param author author record
     * @param topic topic record
     * @param competencies competency records
     * @return new JPA record
     */
    public static QuestionRecord toNewRecord(Question question, UserRecord author, TopicRecord topic,
                                             Set<CompetencyRecord> competencies) {
        QuestionRecord record;
        if (question instanceof MultipleChoiceQuestion multipleChoice) {
            MultipleChoiceQuestionRecord multipleChoiceRecord = new MultipleChoiceQuestionRecord(
                    question.getStatement(), question.getDifficulty(), question.getStatus(), author, topic);
            for (AnswerOption option : multipleChoice.getOptions()) {
                multipleChoiceRecord.addOption(new AnswerOptionRecord(option.getText(), option.isCorrect()));
            }
            record = multipleChoiceRecord;
        } else if (question instanceof TrueFalseQuestion trueFalse) {
            record = new TrueFalseQuestionRecord(question.getStatement(), question.getDifficulty(),
                    question.getStatus(), author, topic, trueFalse.getExpectedAnswer());
        } else {
            throw new DomainException("Unsupported question type: " + question.getClass().getName());
        }
        record.getCompetencies().addAll(competencies);
        syncMetadata(record, question.getMetadata());
        return record;
    }

    /**
     * Copies the state of a domain question into an existing managed record.
     *
     * <p>Updating instead of creating a new record is what allows a question to
     * change state without producing a duplicate row.</p>
     *
     * @param record managed record
     * @param question domain question
     * @param author author record
     * @param topic topic record
     * @param competencies competency records
     * @throws DomainException when the concrete type would change
     */
    public static void updateRecord(QuestionRecord record, Question question, UserRecord author,
                                    TopicRecord topic, Set<CompetencyRecord> competencies) {
        boolean sameType = (record instanceof MultipleChoiceQuestionRecord
                && question instanceof MultipleChoiceQuestion)
                || (record instanceof TrueFalseQuestionRecord && question instanceof TrueFalseQuestion);
        if (!sameType) {
            throw new DomainException("The type of question " + question.getId() + " cannot change");
        }
        record.update(question.getStatement(), question.getDifficulty(), question.getStatus(), author, topic);
        record.getCompetencies().clear();
        record.getCompetencies().addAll(competencies);
        syncMetadata(record, question.getMetadata());
        if (record instanceof MultipleChoiceQuestionRecord multipleChoiceRecord) {
            syncOptions(multipleChoiceRecord, (MultipleChoiceQuestion) question);
        } else {
            ((TrueFalseQuestionRecord) record)
                    .updateExpectedAnswer(((TrueFalseQuestion) question).getExpectedAnswer());
        }
    }

    private static void syncOptions(MultipleChoiceQuestionRecord record, MultipleChoiceQuestion question) {
        Set<Long> domainOptionIds = question.getOptions().stream()
                .map(AnswerOption::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        record.getOptions().removeIf(option -> option.getId() != null
                && !domainOptionIds.contains(option.getId()));
        Map<Long, AnswerOptionRecord> existing = record.getOptions().stream()
                .filter(option -> option.getId() != null)
                .collect(Collectors.toMap(AnswerOptionRecord::getId, Function.identity()));
        for (AnswerOption option : question.getOptions()) {
            AnswerOptionRecord optionRecord = option.getId() == null ? null : existing.get(option.getId());
            if (optionRecord == null) {
                record.addOption(new AnswerOptionRecord(option.getText(), option.isCorrect()));
            } else {
                optionRecord.update(option.getText(), option.isCorrect());
            }
        }
    }

    private static void syncMetadata(QuestionRecord record, QuestionMetadata metadata) {
        QuestionMetadataRecord current = record.getMetadata();
        if (metadata == null) {
            if (current != null) {
                current.setQuestion(null);
                record.setMetadata(null);
            }
            return;
        }
        if (current == null) {
            record.setMetadata(new QuestionMetadataRecord(record, metadata.getLastReviewedAt(),
                    metadata.getCognitiveLevel(), metadata.getEstimatedMinutes(),
                    metadata.getEditorialNotes()));
        } else {
            current.update(metadata.getLastReviewedAt(), metadata.getCognitiveLevel(),
                    metadata.getEstimatedMinutes(), metadata.getEditorialNotes());
        }
    }

    private static AnswerOption toDomain(AnswerOptionRecord record) {
        return new AnswerOption(record.getId(), record.getText(), record.isCorrect());
    }

    private static QuestionMetadata toDomain(QuestionMetadataRecord record) {
        if (record == null) {
            return null;
        }
        return new QuestionMetadata(record.getLastReviewedAt(), record.getCognitiveLevel(),
                record.getEstimatedMinutes(), record.getEditorialNotes());
    }
}
