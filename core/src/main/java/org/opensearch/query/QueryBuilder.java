package org.opensearch.query;

import javaslang.Tuple2;
import org.opensearch.query.properties.constraint.Constraint;
import org.opensearch.query.quant.QuantType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Query builder creational interface
 */
public interface QueryBuilder {
    QueryBuilder withOnt(String ont);

    QueryBuilder withName(String name);

    QueryBuilder start();

    QueryBuilder eType(String type, String tag);

    QueryBuilder concrete(String id, String name, String type, String tag);

    QueryBuilder rel(String rType, Rel.Direction dir, String tag);

    QueryBuilder eProp(String pType);

    QueryBuilder eProp(String pType, Constraint constraint);

    QueryBuilder ePropGroup(List<Tuple2<String, Optional<Constraint>>> pTypes, QuantType type);

    QueryBuilder rProp(String pType);

    QueryBuilder projectField(EBase... name);

    QueryBuilder rProp(String pType, Constraint constraint);

    QueryBuilder quant(QuantType type);

    EBase pop();

    Optional<EBase> pop(int index);

    Optional<EBase> pop(Predicate<EBase> predicate);

    EBase current();

    EBase current(int index);

    int currentIndex(int newCurrent);

    int currentIndex();

    Query build();

}
