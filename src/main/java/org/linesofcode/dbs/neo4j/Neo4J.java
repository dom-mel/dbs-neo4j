package org.linesofcode.dbs.neo4j;

import com.google.common.collect.Lists;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.impl.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Dominik Eckelmann
 */
public class Neo4J {

    private final String dbPath;
    private GraphDatabaseService db;
    private Index<Node> nodeIndex;

    public Neo4J(final String dbPath) {
        this.dbPath = dbPath;
    }

    public void clear() {
        try {
            FileUtils.deleteRecursively(new File(dbPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void open() {
        db = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        nodeIndex = db.index().forNodes("nodes");
    }

    public void close() {
        db.shutdown();
    }

    public Node createPerson(final String name, final long age) {
        final Transaction transaction = db.beginTx();
        try {
            final Node node = db.createNode();
            node.setProperty("name", name);
            nodeIndex.add(node, "name", name);
            node.setProperty("age", age);

            db.getReferenceNode().createRelationshipTo(node, RelationTypes.PERSONS);

            transaction.success();
            return node;
        } finally {
            transaction.finish();
        }
    }

    public List<Node> getPersons() {
        List<Relationship> relations = Lists.newArrayList(db.getReferenceNode().getRelationships(RelationTypes.PERSONS, Direction.OUTGOING));
        List<Node> result = Lists.newArrayListWithCapacity(relations.size());

        for (final Relationship relationship : relations) {
            result.add(relationship.getEndNode());
        }
        return result;
    }

    public void addKnows(final Node from , final Node to) {
        final Transaction transaction = db.beginTx();
        try {
            from.createRelationshipTo(to, RelationTypes.KNOWS);
            transaction.success();
        } finally {
            transaction.finish();
        }
    }

    public void delete(final Node person) {
        final Transaction transaction = db.beginTx();
        try {
            for (final Relationship relationship : person.getRelationships()){
                relationship.delete();
            }
            person.delete();
            transaction.success();
        } finally {
            transaction.finish();
        }
    }
}
