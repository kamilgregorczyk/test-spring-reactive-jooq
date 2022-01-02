package com.example.jpademo.repository;

import com.example.jpademo.model.Note;
import org.jooq.DSLContext;
import org.jooq.generated.default_schema.tables.records.NoteRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static org.jooq.generated.default_schema.Tables.NOTE;

@Repository
public class NoteRepository extends JooqRepository<Note, NoteRecord, Long> {

    @Autowired
    public NoteRepository(DSLContext context) {
        super(context, NOTE);
    }

    @Override
    protected Note fromRecord(NoteRecord record) {
        return Note.note()
            .id(Optional.of(record.getId()))
            .isPersisted(true)
            .title(record.getTitle())
            .description(record.getDescription())
            .build();
    }

    @Override
    protected NoteRecord toRecord(Note model) {
        return new NoteRecord(
            model.hasId() ? model.id() : null,
            model.description(),
            model.title(),
            model.recipeId()
        );
    }

    public Flux<Note> findAllByRecipeId(Long recipeId) {
        return findAllWhere(NOTE.RECIPE_ID.eq(recipeId));
    }
}
