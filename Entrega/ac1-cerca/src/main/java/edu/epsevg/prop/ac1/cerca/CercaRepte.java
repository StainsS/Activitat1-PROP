package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.heuristica.Heuristica;
import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.Direccio;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.model.Posicio;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;

import java.util.*;


public class CercaRepte extends Cerca {

    private List<Direccio[]> prioritats;
    private Set<Mapa> LNT;
    private List<Moviment> millor;
    private long tInici;

    public CercaRepte() {
        super(true);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        tInici = System.currentTimeMillis();
        prioritats = permutacionsDireccions();
        LNT = new HashSet<>();
        millor = null;

        LNT.add(inicial);
        explora(inicial, new ArrayList<>(), rc);

        rc.updateMemoria(0, LNT.size());
        if (millor != null) {
            rc.setSolucio(millor);
        }
    }

    /** Escull el següent agent a moure i el camí que seguirà, i continua recursivament. */
    private void explora(Mapa estat, List<Moviment> cami, ResultatCerca rc) {
        rc.incNodesExplorats();
        rc.actualitzaProfunditat(cami.size());

        if (estat.esFinalPartida()) 
        {
            if (millor == null || cami.size() < millor.size()) 
            {
                millor = new ArrayList<>(cami);
            }
            return;
        }

        List<Posicio> agents = estat.getAgents();
        for (int agent = 0; agent < agents.size(); agent++) 
        {
            if (agents.get(agent) == null) continue;      // ja ha sortit

            Posicio origen = agents.get(agent);
            Posicio sortida = estat.getSortides().get(agent);
            int[][] dist = estat.distancesFrom(origen);   // un únic BFS per agent i estat
            if (dist[sortida.x][sortida.y] >= Heuristica.INFINIT) return;   // sortida tancada: estat mort

            Set<List<Posicio>> camins = new HashSet<>();  // evita executar dos cops el mateix camí
            for (Direccio[] prio : prioritats) 
            {
                if (tempsExcedit(tInici)) return;

                List<Posicio> cella = reconstrueixCami(estat, dist, origen, sortida, prio);
                if (!camins.add(cella)) continue;

                // Cota: el cost d'una solució només creix, així que si ja no pot millorar, es talla
                if (millor != null && cami.size() + cella.size() >= millor.size()) continue;

                List<Moviment> tram = new ArrayList<>();
                Mapa fill = executaCami(estat, agent, cella, tram);
                if (fill == null) continue;

                if (!LNT.add(fill)) {
                    rc.incNodesTallats();
                    continue;
                }

                cami.addAll(tram);
                explora(fill, cami, rc);
                cami.subList(cami.size() - tram.size(), cami.size()).clear();
            }
        }
    }

    /** Camí mínim (sense l'origen, acabant a la sortida) segons la prioritat de direccions. */
    private List<Posicio> reconstrueixCami(Mapa estat, int[][] dist, Posicio origen, Posicio sortida, Direccio[] prio) {
        LinkedList<Posicio> cella = new LinkedList<>();
        Posicio p = sortida;
        while (!p.equals(origen)) 
        {
            cella.addFirst(p);
            Posicio anterior = null;
            for (Direccio d : prio) 
            {
                Posicio q = p.translate(d);
                if (q.x < 0 || q.y < 0 || q.x >= dist.length || q.y >= dist[0].length) continue;
                // les sortides no s'expandeixen al BFS: no poden ser un pas intermedi
                if (dist[q.x][q.y] == dist[p.x][p.y] - 1 && !estat.getSortides().contains(q)) {
                    anterior = q;
                    break;
                }
            }
            p = anterior;
        }
        return cella;
    }

    /** Aplica el camí a l'agent, afegint els moviments a {@code tram}. Retorna l'estat final. */
    private Mapa executaCami(Mapa estat, int agent, List<Posicio> cella, List<Moviment> tram) {
        Posicio sortida = estat.getSortides().get(agent);
        for (Posicio seguent : cella) 
        {
            Mapa nou = null;
            for (Moviment mov : estat.getAccionsPossibles()) 
            {
                Mapa fill = (Mapa) estat.mou(mov);
                Posicio nova = fill.getAgents().get(agent);
                if (seguent.equals(sortida) ? nova == null : seguent.equals(nova)) {
                    tram.add(mov);
                    nou = fill;
                    break;
                }
            }
            if (nou == null) return null;
            estat = nou;
        }
        return estat;
    }

    private static List<Direccio[]> permutacionsDireccions() {
        List<Direccio[]> res = new ArrayList<>();
        permutaDir(Direccio.values(), 0, res);
        return res;
    }

    private static void permutaDir(Direccio[] a, int k, List<Direccio[]> res) {
        if (k == a.length) { res.add(a.clone()); return; }
        for (int i = k; i < a.length; i++) 
        {
            Direccio t = a[k]; a[k] = a[i]; a[i] = t;
            permutaDir(a, k + 1, res);
            t = a[k]; a[k] = a[i]; a[i] = t;
        }
    }

    @Override
    public String getNomCerca() {
        return "Repte";
    }
}