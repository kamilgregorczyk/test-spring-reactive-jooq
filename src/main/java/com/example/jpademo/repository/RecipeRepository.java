package com.example.jpademo.repository;

import com.example.jpademo.model.Recipe;
import org.jooq.DSLContext;
import org.jooq.generated.default_schema.tables.records.RecipeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.jooq.generated.default_schema.Tables.RECIPE;

@Repository
public class RecipeRepository extends JooqRepository<Recipe, RecipeRecord, Long> {

    @Autowired
    public RecipeRepository(DSLContext context) {
        super(context, RECIPE);
    }

    public Mono<Recipe> findByTitle(String title) {
        return findOneWhere(RECIPE.TITLE.eq(title));
    }

    @Override
    protected Recipe fromRecord(RecipeRecord record) {
        return Recipe.recipe()
            .id(Optional.of(record.getId()))
            .isPersisted(true)
            .title(record.getTitle())
            .description(record.getDescription())
            .build();
    }

    @Override
    protected RecipeRecord toRecord(Recipe recipe) {
        return new RecipeRecord(
            recipe.hasId() ? recipe.id() : null,
            recipe.description(),
            recipe.title()
        );
    }
}
