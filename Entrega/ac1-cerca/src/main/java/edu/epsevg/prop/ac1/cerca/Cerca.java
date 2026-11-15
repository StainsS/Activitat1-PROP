package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Contracte comú de tots els algorismes de cerca. {@code usarLNT} indica si l'algorisme
 * ha de mantenir una llista de nodes tancats/visitats (LNT) per detectar i tallar
 * estats repetits, o bé si només ha d'evitar tornar directament a un ancestre del propi
 * camí (detecció de cicles més feble i barata en memòria).
 */
public abstract class Cerca {
    protected final boolean usarLNT;
    protected static final long TEMPS_MAXIM_MS = 120_000L;

    public Cerca(boolean usarLNT) {
        this.usarLNT = usarLNT;
    }

    /**
     * Indica si la cerca fa servir LNT
     * @return si la cerca fa servir LNT
     */
    public boolean usesLNT(){
        return this.usarLNT;
    }
    
    protected boolean tempsExcedit(long tempsInici) 
    {
        return (System.currentTimeMillis() - tempsInici > TEMPS_MAXIM_MS)
                || Thread.currentThread().isInterrupted();
    }

    /**
     * Cerca un camí des de {@code inicial} fins a un estat meta i n'informa a
     * {@code rc} (camí trobat, nodes explorats/tallats, pic de memòria...).
     */
    public abstract void ferCerca(Mapa inicial, ResultatCerca rc);
    
    /**
     * Torna el nom de la cerca.
     * @return el nom de la cerca.
     */
    public abstract String getNomCerca();
    
    
    public List<Moviment> reconstruir(Node fi) {       
        List<Moviment> cami = new ArrayList<>();
        Node aux = fi;
        while(aux.pare != null){
            cami.add(aux.accio);
            aux = aux.pare;
        }
        Collections.reverse(cami);
        return cami;
    }
    public boolean repetitEnBranca(Node pare, Mapa estat) {
        for (Node n = pare; n != null; n = n.pare) {
            if (n.estat.equals(estat)) return true;
        }
        return false;
    }

    /**
     * Retorna true si l'estat fill és repetit. Amb LNT, a més, el registra
     * (Set.add retorna false si ja hi era); sense LNT, el busca a la branca actual.
     */
    protected boolean esRepetit(Set<Mapa> LNT, Node pare, Mapa fill) 
    {
        if (usarLNT) {
            return !LNT.add(fill);
        } else {
            return repetitEnBranca(pare, fill);
        }
    }
}
