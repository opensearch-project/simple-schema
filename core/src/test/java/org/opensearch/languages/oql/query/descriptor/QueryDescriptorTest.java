package org.opensearch.languages.oql.query.descriptor;

import javaslang.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.languages.oql.query.Query;
import org.opensearch.languages.oql.query.Rel;
import org.opensearch.languages.oql.query.entity.EConcrete;
import org.opensearch.languages.oql.query.entity.ETyped;
import org.opensearch.languages.oql.query.properties.EProp;
import org.opensearch.languages.oql.query.properties.EPropGroup;
import org.opensearch.languages.oql.query.properties.RelProp;
import org.opensearch.languages.oql.query.properties.RelPropGroup;
import org.opensearch.languages.oql.query.properties.constraint.Constraint;
import org.opensearch.languages.oql.query.properties.constraint.ConstraintOp;
import org.opensearch.languages.oql.query.properties.projection.IdentityProjection;
import org.opensearch.languages.oql.query.quant.Quant1;
import org.opensearch.languages.oql.query.quant.QuantType;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.of;


class QueryDescriptorTest {
    static Query query;

    @BeforeAll
    static void setup() {
        query = Query.Builder.instance()
                .start()
                .withOnt("Knowledge")
                .withName("test")
                .eType("Entity", "P1")
                .quant(QuantType.some)
                .ePropGroup(
                        Arrays.asList(new Tuple2<>("category", Optional.empty()), new Tuple2<>("context", of(new Constraint(ConstraintOp.notEmpty)))), QuantType.all)
                .rel("hasOutRelation", Rel.Direction.R, "k")
                .eType("Entity", "P2")
                .quant(QuantType.all)
                .ePropGroup(Arrays.asList(new Tuple2<>("deleteTime", of(new Constraint(ConstraintOp.empty)))), QuantType.all)
                .build();
    }

    /**
     * verify constraint descriptions
     */
    @Test
    void testPrintConstraint() {
        String describe = QueryDescriptor.printConstraint(null);
        Assertions.assertEquals("-", describe);

        describe = QueryDescriptor.printConstraint(new Constraint());
        Assertions.assertEquals(",", describe);

        describe = QueryDescriptor.printConstraint(new Constraint(ConstraintOp.ne));
        Assertions.assertEquals("ne,", describe);

        describe = QueryDescriptor.printConstraint(new Constraint(ConstraintOp.eq,"10"));
        Assertions.assertEquals("eq,10", describe);
    }

    /**
     * verify concrete descriptions
     */
    @Test
    void testDescribeEntityConcrete() {
        String describe = QueryDescriptor.describe(new EConcrete(1, "1", "label", "id123", "name", 1));
        Assertions.assertEquals("EConcrete[label:1:ID[id123]]", describe);
    }

    /**
     * verify typed descriptions
     */
    @Test
    void testDescribeEntityType() {
        String describe = QueryDescriptor.describe(new ETyped(1, "1", "label", 1));
        Assertions.assertEquals("ETyped[label:1]", describe);
    }

    /**
     * verify typed descriptions with props
     */
    @Test
    void testDescribeEntityTypeWithProps() {
        String describe = QueryDescriptor.describe(new ETyped(1, "1", "label", 1),
                new EPropGroup(2,
                        new EProp(20,"name",new Constraint(ConstraintOp.eq,"March")),
                        new EProp(20,"property",new Constraint(ConstraintOp.ne)),
                        new EProp(21,"age",new Constraint(ConstraintOp.ge,20))));
        Assertions.assertEquals("ETyped[label:1]::[name<eq,March>, property<ne,>, age<ge,20>]", describe);
    }

    /**
     * verify quant descriptions
     */
    @Test
    void testDescribeQuantBase() {
        Quant1 quant = new Quant1(1, QuantType.all);
        quant.addNext(1);
        quant.addNext(2);
        quant.addNext(3);
        String describe = QueryDescriptor.describe(quant);
        Assertions.assertEquals("Quant1[1]:{1|2|3}", describe);
    }

    /**
     * verify query entire description
     */
    @Test
    void testDescribeQuery() {
        String describe = new QueryDescriptor().describe(query);
        Assertions.assertEquals("Start[0]:ETyped[Entity:1]:Quant1[2]:{3|4}:EPropGroup[3]:Rel[hasOutRelation:4]:ETyped[Entity:5]:Quant1[6]:{7}:EPropGroup[7]", describe);
    }

    /**
     * describe a relationship
     */
    @Test
    void testDescribeRel() {
        String describe = QueryDescriptor.describe(new Rel(1,"knows", Rel.Direction.L,"wrap",2));
        Assertions.assertEquals("Rel[knows:1]", describe);
    }

    /**
     * describe a relationship with properties
     */
    @Test
    void testDescribeRelWithProp() {
        String describe = QueryDescriptor.describe(new Rel(1,"knows", Rel.Direction.L,"wrap",2),
                new RelPropGroup(1,
                        new RelProp(10,"date",new Constraint(ConstraintOp.ge,"01/01/2000")),
                        new RelProp(10,"degree",new Constraint(ConstraintOp.notEmpty)))
        );
        Assertions.assertEquals("Rel[knows:1]::[date<ge,01/01/2000>, degree<notEmpty,>]", describe);
    }

    @Test
    void printRelProps() {
        String props = QueryDescriptor.printProps(new RelPropGroup(1,
                new RelProp(10, "date", new Constraint(ConstraintOp.ge, "01/01/2000")),
                new RelProp(10, "degree", new Constraint(ConstraintOp.notEmpty))));

        Assertions.assertEquals(":[date<ge,01/01/2000>, degree<notEmpty,>]", props);
    }

    @Test
    void testEProps() {
        String props = QueryDescriptor.printProps(new EPropGroup(2,
                new EProp(20,"name",new Constraint(ConstraintOp.eq,"March")),
                new EProp(20,"property",new Constraint(ConstraintOp.ne)),
                new EProp(21,"age",new Constraint(ConstraintOp.ge,20))));

        Assertions.assertEquals(":[name<eq,March>, property<ne,>, age<ge,20>]", props);
    }

    @Test
    void printDetailedProp() {
        String props = QueryDescriptor.printDetailedProp(new EProp(20,"name",new Constraint(ConstraintOp.eq,"March")));
        Assertions.assertEquals("Typ:[name] [eq,March]", props);

        props = QueryDescriptor.printDetailedProp(new EProp(20,"property",new Constraint(ConstraintOp.ne)));
        Assertions.assertEquals("Typ:[property] [ne,]", props);

        props = QueryDescriptor.printDetailedProp(new EProp(21,"age",new Constraint(ConstraintOp.ge,20)));
        Assertions.assertEquals("Typ:[age] [ge,20]", props);

        props = QueryDescriptor.printDetailedProp(new EProp(21,"label",new IdentityProjection()));
        Assertions.assertEquals("Typ:[label] [IdentityProjection]", props);

        props = QueryDescriptor.printDetailedProp(new RelProp(21,"size",new Constraint(ConstraintOp.ne,20)));
        Assertions.assertEquals("Typ:[size] [ne,20]", props);

        props = QueryDescriptor.printDetailedProp(new RelProp(21,"length",new Constraint(ConstraintOp.notEmpty)));
        Assertions.assertEquals("Typ:[length] [notEmpty,]", props);

    }

    @Test
    void printProp() {
        String props = QueryDescriptor.printProp(new EProp(20,"name",new Constraint(ConstraintOp.eq,"March")));
        Assertions.assertEquals("name<eq,March>", props);

        props = QueryDescriptor.printProp(new EProp(20,"property",new Constraint(ConstraintOp.ne)));
        Assertions.assertEquals("property<ne,>", props);

        props = QueryDescriptor.printProp(new EProp(21,"age",new Constraint(ConstraintOp.ge,20)));
        Assertions.assertEquals("age<ge,20>", props);

        props = QueryDescriptor.printProp(new EProp(21,"label",new IdentityProjection()));
        Assertions.assertEquals("label<IdentityProjection>", props);

    }

    @Test
    void testToString() {
        testDescribeQuery();
    }

    @Test
    void testPrint() {
        Assertions.assertEquals("[└── Start, \n" +
                "    ──Typ[Entity:$]──Q[$]:{3|4}, \n" +
                "                           └─?[$]:[category<IdentityProjection>, context<notEmpty,>], \n" +
                "                           └-> Rel(hasOutRelation:$)──Typ[Entity:$]──Q[$]:{7}, \n" +
                "                                                                         └─?[$]:[deleteTime<empty,>]]",
                QueryDescriptor.print(query,false));
        Assertions.assertEquals("[└── Start, \n" +
                "    ──Typ[Entity:1]──Q[2]:{3|4}, \n" +
                "                           └─?[3]:[category<IdentityProjection>, context<notEmpty,>], \n" +
                "                           └-> Rel(hasOutRelation:4)──Typ[Entity:5]──Q[6]:{7}, \n" +
                "                                                                         └─?[7]:[deleteTime<empty,>]]"
                ,QueryDescriptor.print(query,true));
    }

    /**
     * test the graphviz dot product describing this query
     * https://dreampuf.github.io/GraphvizOnline/
     */
    @Test
    void testPrintGraph() {
        Assertions.assertEquals("digraph G { \n" +
                "\t rankdir=LR; \n" +
                "\t node [shape=Mrecord]; \n" +
                "\t start [shape=Mdiamond, color=blue, style=\"rounded\"]; \n" +
                " \n" +
                " subgraph cluster_Q_2 { \n" +
                " \t color=blue; \n" +
                " \t node [style=filled]; \n" +
                " \t color=blue; \n" +
                " \t 2 [color=lightblue, shape=folder, label=\"some\"]; \n" +
                " \t label = \" Quant1[2];\"; \n" +
                "\n" +
                "  \n" +
                " subgraph cluster_Props_3 { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t 3 [fillcolor=green, shape=folder, label=\"all\"]; \n" +
                " \t label = \" Props[3]\"; \n" +
                "\n" +
                " 300 [fillcolor=khaki3, label=\"category\" ,shape = component]; \n" +
                "301 [fillcolor=khaki3, label=\"Typ:[context] [notEmpty,]\" ,shape = component]; \n" +
                "\n" +
                " 3->300\n" +
                " 3->301\n" +
                " } \n" +
                "\n" +
                " 2->3 \n" +
                " subgraph cluster_Q_6 { \n" +
                " \t color=blue; \n" +
                " \t node [style=filled]; \n" +
                " \t color=blue; \n" +
                " \t 6 [color=lightblue, shape=folder, label=\"all\"]; \n" +
                " \t label = \" Quant1[6];\"; \n" +
                "\n" +
                "  \n" +
                " subgraph cluster_Props_7 { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t 7 [fillcolor=green, shape=folder, label=\"all\"]; \n" +
                " \t label = \" Props[7]\"; \n" +
                "\n" +
                " 700 [fillcolor=khaki3, label=\"Typ:[deleteTime] [empty,]\" ,shape = component]; \n" +
                "\n" +
                " 7->700\n" +
                " } \n" +
                "\n" +
                " 6->7\n" +
                " } \n" +
                "\n" +
                " 4 [ fillcolor=yellow, label=\"Rel(hasOutRelation:4)\", shape = rarrow]; \n" +
                "\n" +
                " 2->4->5->6\n" +
                " } \n" +
                "start->1->2\n" +
                "\t }", QueryDescriptor.printGraph(query));
    }





}