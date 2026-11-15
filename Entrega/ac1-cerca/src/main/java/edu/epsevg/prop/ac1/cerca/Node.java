package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;

/**
 * Node de l'arbre de cerca: guarda un estat ({@link Mapa}), el node pare, l'acció
 * (moviment) que l'ha generat, la profunditat respecte l'arrel i el cost acumulat
 * {@code g} (nombre de moviments fets fins arribar-hi).
 */
public class Node {
    public final Mapa estat;
    public final Node pare;
    public final Moviment accio;
    public final int depth;
    public final int g; // cost

    public Node(Mapa estat, Node pare, Moviment accio, int depth, int g) {
        this.estat = estat;
        this.pare = pare;
        this.accio = accio;
        this.depth = depth;
        this.g = g;
    }
}
