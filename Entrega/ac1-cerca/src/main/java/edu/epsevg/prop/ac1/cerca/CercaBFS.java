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
        long tempsInici = System.currentTimeMillis();

        Set<Mapa> LNT = new HashSet<>();
        Queue<Node> LNO = new ArrayDeque<>();

        LNO.add(new Node(inicial, null, null, 0, 0));
        if (usarLNT) {
            LNT.add(inicial);
        }

        while (!LNO.isEmpty()) {
            if (tempsExcedit(tempsInici)) {
                rc.updateMemoria(LNO.size(), LNT.size());
                return;
            }

            Node actual = LNO.poll();
            rc.incNodesExplorats();
            rc.actualitzaProfunditat(actual.depth);

            if (actual.estat.esFinalPartida()) {
                rc.setSolucio(reconstruir(actual));
                rc.updateMemoria(LNO.size(), LNT.size());
                return;
            }

            for (Moviment mov : actual.estat.getAccionsPossibles()) {
                Mapa fill = (Mapa) actual.estat.mou(mov);

                if (esRepetit(LNT, actual, fill)) {
                    rc.incNodesTallats();
                } else {
                    LNO.add(new Node(fill, actual, mov, actual.depth + 1, actual.g + 1));
                }
            }
            rc.updateMemoria(LNO.size(), LNT.size());
        }
    }

    /**
     * Torna el nom de la cerca.
     *
     * @return el nom de la cerca.
     */
    public String getNomCerca() {
        return "BFS";
    }
}
