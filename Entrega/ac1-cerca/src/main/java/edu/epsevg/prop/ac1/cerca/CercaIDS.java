package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

public class CercaIDS extends CercaDFS {


    public CercaIDS(boolean usarLNT) {
        super(usarLNT);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        long tempsInici = System.currentTimeMillis();
        for (int limit = 0; ; ++limit)
        {
            Resultat r = dfsLimitat(inicial, rc, limit, tempsInici);
            if (r != Resultat.TALLAT_PER_LIMIT)
            {
                return;
            }
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
