package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

import java.util.List;

/**
 * Com {@link HeuristicaBasica} suma per cada agent actiu  la distància fins a la seva
 * pròpia sortida, però usant la distància real sobre la graella actual calculada amb BFS
 * tenint en compte les parets/rastres ja existents en lloc de la distància Manhattan.
 */
public class HeuristicaAvancada implements Heuristica {
    
  
    public HeuristicaAvancada() {
        //@TODO: implementar       
    }

    
    @Override
    public int h(Mapa estat) {
        //@TODO: implementar
        return 0;
    }
    
    @Override
    public String getNom() {
        return "H.av";
    }

        // Podeu implementar aquí d'altres métodes que considereu necessaris
    // ...
}
