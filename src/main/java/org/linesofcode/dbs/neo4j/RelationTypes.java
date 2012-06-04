package org.linesofcode.dbs.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author Dominik Eckelmann
 */
public enum RelationTypes implements RelationshipType {
    KNOWS,
    PERSONS
}
