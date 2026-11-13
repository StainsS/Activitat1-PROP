package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaDFS extends Cerca {

    protected enum Resultat {TROBAT, TALLAT_PER_LIMIT, ESGOTAT}

    private static final int LIMIT = 50;

    public CercaDFS(boolean usarLNT) { super(usarLNT); }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        dfsLimitat(inicial, rc, LIMIT);
    }

    protected Resultat dfsLimitat(Mapa inicial, ResultatCerca rc, int limit)
    {
        Set<Mapa> LNT = new HashSet<>();
        Deque<Node> LNO = new ArrayDeque<>();

        LNO.push(new Node (inicial, null, null, 0, 0));
        if (usarLNT) LNT.add(inicial);

        boolean limitTallat = false;

        while (!LNO.isEmpty())
        {
            Node actual = LNO.pop();
            rc.incNodesExplorats();
            rc.actualitzaProfunditat(actual.depth);

            if (actual.estat.esFinalPartida())
            {
                rc.setSolucio(reconstruir(actual));
                rc.updateMemoria(LNO.size(), LNT.size());
                return Resultat.TROBAT;
            }
            
            if (actual.depth < limit)
            {
                for (Moviment mov : actual.estat.getAccionsPossibles())
                {
                    Mapa fill = (Mapa) actual.estat.mou(mov);

                    boolean repetit;
                    if (usarLNT) {
                        repetit = !LNT.add(fill);
                    } else {
                        repetit = repetitEnBranca(actual, fill);
                    }

                    if (repetit) {
                        rc.incNodesTallats();
                    } else {
                        LNO.push(new Node (fill, actual, mov, actual.depth + 1, actual.g + 1));
                    }
                }
            } else if (!actual.estat.getAccionsPossibles().isEmpty()) {
                limitTallat = true;
            }

            rc.updateMemoria(LNO.size(), LNT.size());
        }

        if (limitTallat) {
            return Resultat.TALLAT_PER_LIMIT;
        } else {
            return Resultat.ESGOTAT;
        }
    }

    /**
     * Torna el nom de la cerca.
     * @return el nom de la cerca.
     */
    public String getNomCerca(){
        return "DFS";
    }    
}
