package com.example.jpademo.resource;

import com.example.jpademo.model.Category;
import com.example.jpademo.model.Note;
import com.example.jpademo.model.Recipe;
import com.example.jpademo.repository.CategoryRepository;
import com.example.jpademo.repository.NoteRepository;
import com.example.jpademo.repository.RecipeRepository;
import com.example.jpademo.repository.RecipeToCategoryRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.*;

@RestController
public class RecipeResource {

    private final RecipeRepository recipeRepository;
    private final NoteRepository noteRepository;
    private final RecipeToCategoryRepository recipeToCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public RecipeResource(RecipeRepository recipeRepository,
                          NoteRepository noteRepository,
                          RecipeToCategoryRepository recipeToCategoryRepository,
                          CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.noteRepository = noteRepository;
        this.recipeToCategoryRepository = recipeToCategoryRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/recipes")
    @Transactional
    public Mono<ResponseEntity<Map<String, Long>>> createRecipe(@RequestBody @Valid CreateRecipeRequest request) {
        return recipeRepository.save(Recipe.recipe()
                .title(request.title)
                .description(request.description)
                .build())
            .zipWhen(recipe -> createNotes(recipe, request.notes))
            .zipWhen(recipeAndNotes -> assignCategories(recipeAndNotes.getT1(), request.categories))
            .map(recipeAndNotesAndCategories -> status(CREATED).body(Map.of("id", recipeAndNotesAndCategories.getT1().getT1().id())));
    }

    @GetMapping("/recipes/{id}")
    @Transactional(readOnly = true)
    public Mono<ResponseEntity<RecipeResponse>> getRecipe(@PathVariable("id") Long id) {
        return recipeRepository.findById(id)
            .zipWhen(recipe -> noteRepository.findAllByRecipeId(recipe.id()).collectList())
            .zipWhen(recipeAndNotes -> recipeToCategoryRepository.findAllByRecipeId(recipeAndNotes.getT1().id()).collectList())
            .map(recipeAndNotesAndCategories -> {
                final var recipe = recipeAndNotesAndCategories.getT1().getT1();
                final var notes = recipeAndNotesAndCategories.getT1().getT2();
                final var categories = recipeAndNotesAndCategories.getT2();
                return ok().body(recipeToResponse(recipe, notes, categories));
            })
            .defaultIfEmpty(notFound().build());
    }

    private Mono<Integer> assignCategories(Recipe recipe, Set<CreateCategoryRequest> categoryRequests) {
        return Flux.concat(categoryRequests.stream().map(this::getOrCreateCategory).collect(toUnmodifiableList())).collectList()
            .flatMap(categories -> recipeToCategoryRepository.save(recipe, categories));
    }

    private Mono<List<Note>> createNotes(Recipe recipe, Set<CreateNoteRequest> notes) {
        return Flux.concat(notes.stream().map(noteRequest -> createNote(noteRequest, recipe))
            .collect(toUnmodifiableList())).collectList();
    }

    private Mono<Category> getOrCreateCategory(CreateCategoryRequest categoryRequest) {
        return categoryRepository.findByTitle(categoryRequest.getTitle())
            .switchIfEmpty(categoryRepository.save(Category.category()
                .title(categoryRequest.getTitle())
                .build()));
    }

    private Mono<Note> createNote(CreateNoteRequest noteRequest, Recipe recipe) {
        return noteRepository.save(Note.note()
            .title(noteRequest.title)
            .description(noteRequest.description)
            .recipeId(recipe.id())
            .build());
    }

    private static RecipeResponse recipeToResponse(Recipe recipe,
                                                   List<Note> notes,
                                                   List<Category> categories) {
        return RecipeResponse.recipeResponse()
            .title(recipe.title())
            .description(recipe.description())
            .notes(notes.stream()
                .map(RecipeResource::noteToResponse)
                .sorted(comparing(NoteResponse::getTitle))
                .collect(toUnmodifiableList()))
            .categories(categories.stream()
                .map(RecipeResource::categoryToResponse)
                .sorted(comparing(CategoryResponse::getTitle))
                .collect(toUnmodifiableList()))
            .build();
    }

    private static NoteResponse noteToResponse(Note note) {
        return NoteResponse.noteResponse().title(note.title()).description(note.description()).build();
    }

    public static CategoryResponse categoryToResponse(Category category) {
        return CategoryResponse.categoryResponse()
            .title(category.title())
            .build();
    }

    @Data
    static class CreateRecipeRequest {
        @NotBlank
        public String title;

        @NotBlank
        public String description;

        @NotEmpty
        public Set<CreateNoteRequest> notes;

        @NotEmpty
        public Set<CreateCategoryRequest> categories;
    }

    @Data
    static class CreateNoteRequest {
        @NotBlank
        public String title;

        @NotBlank
        public String description;
    }

    @Data
    static class CreateCategoryRequest {
        @NotBlank
        public String title;
    }

    @Data
    @Builder(builderMethodName = "recipeResponse")
    static class RecipeResponse {
        public String title;
        public String description;
        public List<NoteResponse> notes;
        public List<CategoryResponse> categories;
    }

    @Data
    @Builder(builderMethodName = "noteResponse")
    static class NoteResponse {
        public String title;
        public String description;
    }

    @Data
    @Builder(builderMethodName = "categoryResponse")
    static class CategoryResponse {
        public String title;
    }
}
