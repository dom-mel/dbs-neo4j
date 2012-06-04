package org.linesofcode.dbs.neo4j;

import com.google.common.collect.Lists;
import com.sun.deploy.util.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

import java.util.List;
import java.util.Random;

/**
 * @author Dominik Eckelmann
 */
public class Main {

    public static final String[] names = {
            "Mrs. Cake",
            "Otto Chriek",
            "Fred Colon",
            "Mrs. Marietta Cosmopilite",
            "Adora Belle Dearheart",
            "Detritus",
            "Cut-Me-Own-Throat Dibbler",
            "Dorfl",
            "Rufus Drumknott",
            "Gaspode the Wonder Dog",
            "Carrot Ironfoundersson",
            "Leonard of Quirm",
            "Moist von Lipwig",
            "Cheery Littlebottom",
            "Nobby Nobbs",
            "Lady Sybil Ramkin, Duchess of Ankh",
            "Foul Ole Ron",
            "Reg Shoe",
            "Rodney",
            "Mr. Slant",
            "Angua von Überwald",
            "Lord Havelock Vetinari",
            "Samuel Vimes",
            "Visit-the-Infidel-with-Explanatory-Pamphlets",
            "Willikins",
            "William de Worde"
    };

    public static void main(String[] args) {

        final Neo4J db = new Neo4J("target/db");
        final Random random = new Random(System.currentTimeMillis());

        out("Remove old db...");
        db.clear();

        out("Creating db ...");
        db.open();

        try {

            out("adding %d persons...", names.length);
            for (final String name : names) {
                db.createPerson(name, 30+random.nextInt(30));
            }

            out("");
            out("persons are:");
            final List<Node> persons = db.getPersons();
            for (final Node person : persons) {
                out((String) person.getProperty("name"));
            }
            out("");
            out("Adding some relations...");
            for (final Node person : persons) {
                final int numberOfKnows = random.nextInt(5);
                final List<Node> unknownPeople = Lists.newArrayList(persons);
                for (int i = 0; i < numberOfKnows; i++) {
                    db.addKnows(person, unknownPeople.get(random.nextInt(unknownPeople.size()-1)));
                }
            }
            out("done");
            out("");
            out("Looking for friends");
            for (final Node person : persons) {
                List<String> friends = Lists.newArrayListWithExpectedSize(5);
                final Traverser traverser = person.traverse(Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
                        ReturnableEvaluator.ALL_BUT_START_NODE,
                        RelationTypes.KNOWS, Direction.OUTGOING);


                for (final Node friend : traverser) {
                    friends.add((String) friend.getProperty("name"));
                }

                out("%s friends: %s", person.getProperty("name"), StringUtils.join(friends, ", "));
            }

            out("");
            out("delete %s!", persons.get(0).getProperty("name"));
            db.delete(persons.get(0));


        } finally {
            out("closing db");
            db.close();
        }
    }

    public static void out(String string, Object... args) {
        System.out.println(String.format(string, args));
    }

}
