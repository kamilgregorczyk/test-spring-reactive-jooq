package com.example.jpademo.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
@SuperBuilder
public abstract class JooqModel<ID> {
    private Optional<ID> id = Optional.empty();
    private boolean isPersisted;

    public ID id() {
        return id.orElseThrow();
    }

    public boolean hasId() {
        if(id == null){
            return false;
        } else {
            return id.isPresent();
        }
    }
}
