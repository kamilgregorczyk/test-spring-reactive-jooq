package com.example.jpademo.repository;

import com.example.jpademo.model.JooqModel;
import org.jooq.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class JooqRepository<MODEL extends JooqModel<ID>, RECORD extends UpdatableRecord<RECORD>, ID> {

    protected final DSLContext ctx;
    private final Table<RECORD> table;

    public JooqRepository(DSLContext ctx, Table<RECORD> table) {
        this.ctx = ctx;
        this.table = table;
    }

    public Mono<MODEL> save(MODEL model) {
        if (model.isPersisted()) {
            return Mono.from(ctx.update(table)
                    .set(toRecord(model))
                    .where(idField().eq(model.id()))
                    .returning())
                .map(this::fromRecord);

        } else {
            final var preparedRecord = toRecord(model);
            if (preparedRecord.get(idField()) == null) {
                preparedRecord.changed(idField(), false);
            }
            return Mono.from(ctx.insertInto(table).set(preparedRecord).returning())
                .map(this::fromRecord);
        }
    }

    public Mono<MODEL> findOneWhere(Condition condition) {
        return Mono.from(ctx.selectFrom(table).where(condition)).map(this::fromRecord);
    }

    public Mono<MODEL> findById(ID id) {
        return findOneWhere(idField().eq(id));
    }

    public Flux<MODEL> findAllWhere(Condition condition) {
        return Flux.from(ctx.selectFrom(table).where(condition)).map(this::fromRecord);
    }

    protected abstract MODEL fromRecord(RECORD record);

    protected abstract RECORD toRecord(MODEL model);

    private Field<ID> idField() {
        return (Field<ID>) table.field("id");
    }

}
