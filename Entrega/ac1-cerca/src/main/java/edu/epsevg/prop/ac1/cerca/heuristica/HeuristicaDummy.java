package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.cerca.model.Mapa;

/**
 * Heurística d'exemple...MOLT MOLT MOLT dolenta però admissible ;-)
 */
public class HeuristicaDummy implements Heuristica {

    /**
     * Retorna el valor heurístic que li correspon al mapa
     * @param estat és el mapa que volem avaluar
     */
    @Override
    public int h(Mapa estat) {

        if (estat.esFinalPartida())
            return 0;
        else
            return 1;
    }
    
    @Override
    public String getNom() {
        return "Heurística dummy";
    }
}
