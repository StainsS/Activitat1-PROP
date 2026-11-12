package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;

public class CercaBFS extends Cerca {

    
    public CercaBFS(boolean usarLNT) {
        super(usarLNT);
    }
    
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {   
        
        Set<Mapa> LNT = new HashSet<>();
        Queue<Node> LNO = new ArrayDeque<>();

        LNO.add(new Node(inicial, null, null, 0, 0));
        if (usarLNT) LNT.add(inicial);

        int profActual = 0;

        while (!LNO.isEmpty()) {
            Node actual = LNO.poll();                 
            rc.incNodesExplorats();

            if (actual.depth > profActual) {
                profActual = actual.depth;
                rc.actualitzaProfunditat(profActual);
            }

            if (actual.estat.esFinalPartida()) {
                rc.setSolucio(reconstruir(actual));
                rc.updateMemoria(LNO.size(), LNT.size());
                return;
            }

            for (Moviment mov : actual.estat.getAccionsPossibles()) {
                Mapa fill = (Mapa) actual.estat.mou(mov);

                boolean repetit = usarLNT
                        ? !LNT.add(fill)                  
                        : repetitEnBranca(actual, fill);

                if (repetit) {
                    rc.incNodesTallats();
                } else {
                    LNO.add(new Node(fill, actual, mov, actual.depth + 1,actual.g+1));
                }
            }
            rc.updateMemoria(LNO.size(), LNT.size());
        }
    }
    
    /**
     * Torna el nom de la cerca.
     * @return el nom de la cerca.
     */
    public String getNomCerca(){
        return "BFS";
    }    
}
