package edu.epsevg.prop.ac1.resultat;

import edu.epsevg.prop.ac1.model.Moviment;
import java.util.Date;
import java.util.List;


/**
 * Classe per emmagatzemar els resultats d'una cerca.
 * La cerca ha de cridar els mètodes per actualitzar-ne les dades:
 * <ul>
 *     <li>setCami(...): Useu-lo per informar de la solució (quan es troba).</li>
 *     <li>incNodesExplorats(): Useu-lo per anar comptant nodes explorats.</li>
 *     <li>incNodesTallats(): Useu-lo per anar comptant nodes tallats.</li>
 *     <li>updateMemoria(int, int): Useu-lo per informar de la memòria consumida,
 *     desglossada entre la llista d'oberts i la de tancats/visitats (LNT).</li>
 *     </ul>
 * Els mètodes startTime() i stopTime() ja es criden des del mètode executarIRecollir().
 * <p>
 * Els camps són {@code volatile}: la cerca els actualitza des del seu propi fil mentre
 * un altre fil (p.ex. un monitor de progrés que imprimeix l'estat cada segon) els pot
 * llegir en qualsevol moment de manera segura, sense necessitat de sincronització
 * completa (no cal cap operació compost llegir-modificar-escriure entre fils).
 */
public class ResultatCerca {
    private volatile List<Moviment> solucio;
    private volatile int nodesExplorats;
    private volatile int nodesTallats;
    private volatile int memoriaPicOberts;
    private volatile int memoriaPicTancats;
    private volatile int memoriaPicTotal;
    private volatile int profunditatMaxima;
    private volatile int heuristicaMinima;
    private volatile boolean heuristicaDisponible;

    private volatile long inici;
    private volatile long fi;

    /**
     * Constructor buit, dades de cerca a 0, i seteja el temps d'inici.
     */
    public ResultatCerca() {
        this.solucio = null;
        this.nodesExplorats = 0;
        this.nodesTallats = 0;
        this.memoriaPicOberts = 0;
        this.memoriaPicTancats = 0;
        this.memoriaPicTotal = 0;
        this.profunditatMaxima = 0;
        this.heuristicaMinima = Integer.MAX_VALUE;
        this.heuristicaDisponible = false;
        inici = (new Date()).getTime();
    }


    /**
     * IMPORTANT! Informa de la solució trobada
     * @param cami : la llista de passos per aconseguir arribar a la sortida
     */
    public void setSolucio(List<Moviment> cami) { this.solucio = cami; }

    /**
     * Incrementa en 1 els nodes explorats
     */
    public void incNodesExplorats(){
        nodesExplorats++;
    }

    /**
     * Incrementa en 1 els nodes tallats (prunning)
     */
    public void incNodesTallats(){
        this.nodesTallats++;
    }

    /**
     * Permet informar de l'ús de memòria actual, desglossat entre la mida de la llista
     * d'oberts (nodes pendents d'explorar) i la de tancats/visitats (LNT, nodes ja
     * explorats que es conserven per detectar cicles/repeticions quan {@code usarLNT}
     * és cert). Es registren tres pics independents: el de {@code nOberts}, el de
     * {@code nTancats} (sempre 0 si no s'usa LNT) i el de la suma total en un mateix
     * instant. Si algun no supera el màxim històric, s'ignora.
     */
    public void updateMemoria(int nOberts, int nTancats){
        if (nOberts > this.memoriaPicOberts) this.memoriaPicOberts = nOberts;
        if (nTancats > this.memoriaPicTancats) this.memoriaPicTancats = nTancats;
        int total = nOberts + nTancats;
        if (total > this.memoriaPicTotal) this.memoriaPicTotal = total;
    }

    /**
     * Informa de la profunditat d'un node explorat; si supera el màxim històric,
     * l'actualitza. Cal cridar-lo per cada node que es compti amb
     * {@link #incNodesExplorats()}.
     */
    public void actualitzaProfunditat(int profunditat) {
        if (profunditat > this.profunditatMaxima) this.profunditatMaxima = profunditat;
    }

    /**
     * Informa del valor heurístic d'un node explorat (només té sentit en cerques
     * informades, com A*); si és menor que el mínim històric, l'actualitza. La primera
     * crida marca l'heurística com a disponible (veure {@link #teHeuristicaDisponible()}).
     */
    public void actualitzaHeuristica(int h) {
        this.heuristicaDisponible = true;
        if (h < this.heuristicaMinima) this.heuristicaMinima = h;
    }

    /**
     * Engega el cronòmetre per temporitzar la cerca.
     */
    public void startTime(){
        inici = (new Date()).getTime();
    }

    /**
     * Atura el cronòmetre de la cerca.
     */
    public void stopTime(){
        fi = (new Date()).getTime();
    }

    // ==================================================
    // Getters
    // ==================================================

    public List<Moviment> getSolucio() { return solucio; }
    public int getNodesExplorats() { return nodesExplorats; }
    public int getNodesTallats() { return nodesTallats; }
    public int getMemoriaPicOberts() { return memoriaPicOberts; }
    public int getMemoriaPicTancats() { return memoriaPicTancats; }
    public int getMemoriaPicTotal() { return memoriaPicTotal; }
    public int getProfunditatMaxima() { return profunditatMaxima; }
    /** @return true si alguna cerca ha informat d'un valor heurístic amb {@link #actualitzaHeuristica(int)} */
    public boolean teHeuristicaDisponible() { return heuristicaDisponible; }
    /** @return el valor heurístic mínim vist fins ara; indefinit si {@link #teHeuristicaDisponible()} és fals */
    public int getHeuristicaMinima() { return heuristicaMinima; }
    public long getTempsMs() { return fi-inici; }

    /**
     * Temps transcorregut fins ara, en mil·lisegons. A diferència de {@link #getTempsMs()}
     * (pensat per quan la cerca ja s'ha aturat amb {@link #stopTime()}), aquest és segur
     * de cridar <b>mentre la cerca encara està en marxa</b>: si encara no s'ha aturat,
     * calcula el temps des de l'inici fins a l'instant actual.
     */
    public long getTempsTranscorregutMs() {
        long ara = (fi == 0) ? System.currentTimeMillis() : fi;
        return ara - inici;
    }

    @Override
    public String toString() {
        return "Resultat: " + (solucio != null ? solucio.size()+" moviments" : "sense solució") +
               ", explorats=" + nodesExplorats +
               ", tallats=" + nodesTallats +
               ", profunditatMax=" + profunditatMaxima +
               ", memòria(oberts/tancats/total)=" + memoriaPicOberts + "/" + memoriaPicTancats + "/" + memoriaPicTotal +
               (heuristicaDisponible ? ", heurMin=" + heuristicaMinima : "") +
               ", temps=" + getTempsMs() + "ms";
    }
}
