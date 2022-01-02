package com.example.jpademo

import com.example.jpademo.repository.CategoryRepository
import com.example.jpademo.repository.RecipeRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.BodyInserters

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals
import static org.springframework.http.MediaType.APPLICATION_JSON

class RecipeResourceSpec extends Spec {

    @Autowired
    private RecipeRepository recipeRepository
    @Autowired
    private CategoryRepository categoryRepository

    def "should create recipe"() {
        given:
        def mapper = new ObjectMapper()
        def request = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString([
                title      : "Title1",
                description: "Description",
                notes      : [
                    [
                        title      : "a",
                        description: "a"
                    ],
                    [
                        title      : "b",
                        description: "b"
                    ]
                ],
                categories : [
                    [
                        title: "cat3"
                    ]
                ]
            ])

        when:
        def response = client.post().uri("/recipes")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()

        then:
        response.expectStatus().isCreated()
        assertEquals(response.returnResult(String.class).getResponseBody().blockLast(), new JSONObject(["id": 1]), true)
        with(recipeRepository.findByTitle("Title1").block(), {
            it.title() == "Title1"
            it.description() == "Description"
        })

    }

    def "should create recipe and not duplicate categories"() {
        given:
        def mapper = new ObjectMapper()
        def request = { title ->
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                [
                    title      : title,
                    description: "Description",
                    notes      : [
                        [
                            title      : "a",
                            description: "a"
                        ],
                        [
                            title      : "b",
                            description: "b"
                        ]
                    ],
                    categories : [
                        [
                            title: "cat1"
                        ]
                    ]
                ]
            )
        }

        when:
        2.times {
            client.post().uri("/recipes")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(request(it.toString())))
                .exchange()
                .expectStatus().isCreated()
        }

        then:
        recipeRepository.findByTitle("0").block()
        recipeRepository.findByTitle("1").block()
    }

    def "should get recipe"() {
        given:
        def mapper = new ObjectMapper()
        def request = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
            [
                title      : "Title2",
                description: "Description",
                notes      : [
                    [
                        title      : "a",
                        description: "a"
                    ],
                    [
                        title      : "b",
                        description: "b"
                    ]
                ],
                categories : [
                    [
                        title: "cat1"
                    ],
                    [
                        title: "cat2"
                    ]
                ]
            ]
        )
        and: "create recipe"
        def createdRecipe = client.post()
            .uri("/recipes")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .exchange()
            .expectStatus()
            .isCreated()
            .returnResult(String.class)
            .getResponseBody()
            .blockLast()
        def createdRecipeId = mapper.readTree(createdRecipe).get("id").asLong()

        when:
        def response = client.get()
            .uri("/recipes/${createdRecipeId}")
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class)
            .getResponseBody()
            .blockLast()

        then:
        assertEquals(response, new JSONObject(
            [
                title      : "Title2",
                description: "Description",
                notes      : [
                    [
                        title      : "a",
                        description: "a"
                    ],
                    [
                        title      : "b",
                        description: "b"
                    ]
                ],
                categories : [
                    [
                        title: "cat1"
                    ],
                    [
                        title: "cat2"
                    ]
                ]
            ]), true)

    }
}
