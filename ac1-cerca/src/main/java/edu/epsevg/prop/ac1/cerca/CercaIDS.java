package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import edu.epsevg.prop.ac1.model.Moviment;
import java.util.*;

public class CercaIDS extends Cerca {


    public CercaIDS(boolean usarLNT) {
        super(usarLNT);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        int limit = 0;
        boolean quedenNodesPerExplorar = true;
        while (quedenNodesPerExplorar)
        {
            Set<Mapa> LNT = new HashSet<>();
            Deque<Node> LNO = new ArrayDeque<>();

            LNO.push(new Node (inicial, null, null, 0, 0));
            if (usarLNT) LNT.add(inicial);

            quedenNodesPerExplorar = false;
            while (!LNO.isEmpty())
            {
                Node actual = LNO.pop();
                rc.incNodesExplorats();
                rc.actualitzaProfunditat(actual.depth);

                if (actual.estat.esFinalPartida())
                {
                    rc.setSolucio(reconstruir(actual));
                    rc.updateMemoria(LNO.size(), LNT.size());
                    return;
                }

                if (actual.depth < limit)
                {
                    for (Moviment mov : actual.estat.getAccionsPossibles())
                    {
                        Mapa fill = (Mapa) actual.estat.mou(mov);

                        boolean repetit;
                        if (usarLNT) {
                            repetit = !LNT.add(fill);
                        } else  {
                            repetit = repetitEnBranca(actual, fill);
                        }

                        if (repetit) {
                            rc.incNodesTallats();
                        } else {
                            LNO.push(new Node (fill, actual, mov, actual.depth + 1, actual.g + 1));
                        }
                    }
                } else if (!actual.estat.getAccionsPossibles().isEmpty())
                {
                    quedenNodesPerExplorar = true;
                }

                rc.updateMemoria(LNO.size(), LNT.size());
            }

            ++limit;
        }
    }
    
    /**
     * Torna el nom de la cerca.
     * @return el nom de la cerca.
     */
    public String getNomCerca(){
        return "IDS";
    }
}
