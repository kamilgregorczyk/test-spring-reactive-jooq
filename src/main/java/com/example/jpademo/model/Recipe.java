package com.example.jpademo.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter()
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(builderMethodName = "recipe")
public class Recipe extends JooqModel<Long> {
    private final String title;
    private final String description;
}
