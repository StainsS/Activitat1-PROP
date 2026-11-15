package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.heuristica.Heuristica;
import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;

public class CercaAStar extends Cerca {

    private Heuristica heur;

    private static final class Entrada {
        final Node node;
        final int h;
        final int f;
        final long seq;

        Entrada(Node node, int h, long seq) {
            this.node = node;
            this.h = h;
            this.f = node.g + h;
            this.seq = seq;
        }
    }

    public CercaAStar(boolean usarLNT, Heuristica heur) {
        super(usarLNT);
        this.heur = heur;
    }


    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        long tInici = System.currentTimeMillis();

        PriorityQueue<Entrada> LNO = new PriorityQueue<>(
            Comparator.<Entrada>comparingInt(e -> e.f)
            .thenComparingInt(e -> e.h)
            .thenComparingLong(e -> e.seq)
        );

        Set<Mapa> LNT = new HashSet<>();
        long seq = 0;

        int h0 = heur.h(inicial);
        rc.actualitzaHeuristica(h0);
        if (h0 >= Heuristica.INFINIT) return;

        LNO.add(new Entrada(new Node(inicial, null, null, 0, 0), h0, seq++));
        if (usarLNT) LNT.add(inicial);

        while (!LNO.isEmpty())
        {
            if (tempsExcedit(tInici))
            {
                rc.updateMemoria(LNO.size(), LNT.size());
                return;
            }

            Entrada e = LNO.poll();
            Node actual = e.node;

            rc.incNodesExplorats();
            rc.actualitzaProfunditat(actual.depth);
            rc.actualitzaHeuristica(e.h);

            if (actual.estat.esFinalPartida())
            {
                rc.setSolucio(reconstruir(actual));
                rc.updateMemoria(LNO.size(), LNT.size());
                return;
            }

            int gFill = actual.g + 1;
            for (Moviment mov : actual.estat.getAccionsPossibles())
            {
                Mapa fill = (Mapa) actual.estat.mou(mov);

                if (esRepetit(LNT, actual, fill))
                {
                    rc.incNodesTallats();
                    continue;
                }

                int h = heur.h(fill);
                if (h >= Heuristica.INFINIT) continue;

                LNO.add(new Entrada(new Node(fill, actual, mov, actual.depth + 1, gFill), h, seq++));
            }

            rc.updateMemoria(LNO.size(), LNT.size());            
        }
    }

    /**
     * Torna el nom de la cerca.
     * @return el nom de la cerca.
     */
    public String getNomCerca(){
        return "A*("+heur.getNom()+")";
    }
}
