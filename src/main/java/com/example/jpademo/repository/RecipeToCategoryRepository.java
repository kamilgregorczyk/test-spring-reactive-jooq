package com.example.jpademo.repository;

import com.example.jpademo.model.Category;
import com.example.jpademo.model.Recipe;
import com.example.jpademo.model.RecipeToCategory;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableOnConditionStep;
import org.jooq.generated.default_schema.tables.records.RecipeToCategoryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.jooq.generated.default_schema.Tables.CATEGORY;
import static org.jooq.generated.default_schema.Tables.RECIPE_TO_CATEGORY;

@Repository
public class RecipeToCategoryRepository extends JooqRepository<RecipeToCategory, RecipeToCategoryRecord, Long> {

    private final TableOnConditionStep<Record> recipeToCategoryWithCategory;

    @Autowired
    public RecipeToCategoryRepository(DSLContext context) {
        super(context, RECIPE_TO_CATEGORY);
        this.recipeToCategoryWithCategory = RECIPE_TO_CATEGORY.leftJoin(CATEGORY).on(CATEGORY.ID.eq(RECIPE_TO_CATEGORY.CATEGORY_ID));
    }


    @Override
    protected RecipeToCategory fromRecord(RecipeToCategoryRecord record) {
        return RecipeToCategory.recipeToCategory()
            .id(Optional.of(record.getId()))
            .isPersisted(true)
            .recipeId(record.getRecipeId())
            .categoryId(record.getCategoryId())
            .build();
    }

    @Override
    protected RecipeToCategoryRecord toRecord(RecipeToCategory model) {
        return new RecipeToCategoryRecord(
            model.hasId() ? model.id() : null,
            model.recipeId(),
            model.categoryId()
        );
    }

    public Flux<Category> findAllByRecipeId(Long id) {
        return Flux.from(ctx.selectFrom(recipeToCategoryWithCategory).where(RECIPE_TO_CATEGORY.RECIPE_ID.eq(id)))
            .map(r -> r.into(CATEGORY))
            .map(r -> Category.category()
                .title(r.getTitle())
                .build());
    }

    public Mono<Integer> save(Recipe recipe, List<Category> categories) {
        return Mono.from(ctx.batch(
            categories.stream()
                .map(c -> {
                        final var record = toRecord(RecipeToCategory.recipeToCategory()
                            .recipeId(recipe.id())
                            .categoryId(c.id())
                            .build());
                        record.changed(RECIPE_TO_CATEGORY.ID, false);
                        return ctx.insertInto(RECIPE_TO_CATEGORY)
                            .set(record)
                            .onDuplicateKeyIgnore();
                    }
                )
                .collect(toUnmodifiableList())
        ));
    }
}
