package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.cerca.model.Mapa;

/**
 * Interfície per totes les heurístiques
 */
public interface Heuristica {
    /**
     * Retorna el valor heurístic que li correspon al mapa
     * @param estat és el mapa que volem avaluar
     * @return el valor heurístic que li correspon al mapa
     */
    int h(Mapa estat);
    
    /**
     * Nom que li donem a l'heurística
     * @return el nom de l'heurística
     */
    String getNom();
    
}
