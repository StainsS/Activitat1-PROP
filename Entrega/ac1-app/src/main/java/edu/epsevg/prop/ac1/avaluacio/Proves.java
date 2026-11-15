package edu.epsevg.prop.ac1.avaluacio;

import edu.epsevg.prop.ac1.Main;
import edu.epsevg.prop.ac1.cerca.Cerca;
import edu.epsevg.prop.ac1.cerca.CercaAStar;
import edu.epsevg.prop.ac1.cerca.CercaBFS;
import edu.epsevg.prop.ac1.cerca.CercaDFS;
import edu.epsevg.prop.ac1.cerca.CercaIDS;
import edu.epsevg.prop.ac1.cerca.heuristica.HeuristicaAvancada;
import edu.epsevg.prop.ac1.cerca.heuristica.HeuristicaBasica;
import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.MapaBase;
import edu.epsevg.prop.ac1.utils.CsvWriter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Mètodes estàtics sense arguments que {@code corrector/corrector.py} descobreix
 * (via {@link ExecutaProva#main}, mode {@code --list}) i executa un per un, cadascun
 * en un procés Java aïllat, contra l'{@code ac1-cerca} d'un grup concret. Cada mètode és
 * deliberadament granular (un algorisme sobre un mapa, o un petit grup coherent) perquè
 * un penjat o un OutOfMemoryError en un experiment no s'emporti per davant els altres
 * del mateix grup. Escriu sempre el resultat a {@value #FITXER_SORTIDA}, al directori de
 * treball actual (el corrector n'usa un de propi per cada (grup, experiment)).
 * <p>
 * Aquests són només exemples de partida: amplia aquesta classe amb el mateix patró quan
 * defineixis el joc de proves real (més mapes, límits de temps diferents, etc.); no cal
 * tocar {@code corrector.py}.
 */
public final class Proves {
    private Proves() {}

    private static final String FITXER_SORTIDA = "resultat.csv";

    public static void bfs_mapA() throws Exception { executaUnaCerca(new CercaBFS(true), "mapA"); }
    public static void bfs_mapA_senseLNT() throws Exception { executaUnaCerca(new CercaBFS(false), "mapA"); }

    public static void dfs_mapA() throws Exception { executaUnaCerca(new CercaDFS(true), "mapA"); }
    public static void dfs_mapA_senseLNT() throws Exception { executaUnaCerca(new CercaDFS(false), "mapA"); }

    public static void ids_mapA() throws Exception { executaUnaCerca(new CercaIDS(true), "mapA"); }
    public static void ids_mapA_senseLNT() throws Exception { executaUnaCerca(new CercaIDS(false), "mapA"); }

    public static void astarBasica_mapA() throws Exception {
        executaUnaCerca(new CercaAStar(true, new HeuristicaBasica()), "mapA");
    }
    public static void astarBasica_mapA_senseLNT() throws Exception {
        executaUnaCerca(new CercaAStar(false, new HeuristicaBasica()), "mapA");
    }

    public static void astarAvancada_mapA() throws Exception {
        executaUnaCerca(new CercaAStar(true, new HeuristicaAvancada()), "mapA");
    }
    public static void astarAvancada_mapA_senseLNT() throws Exception {
        executaUnaCerca(new CercaAStar(false, new HeuristicaAvancada()), "mapA");
    }

    /**
     * Demostra l'efecte (o els límits) de la LNT amb un mapa fet expressament per fer
     * petar la memòria: {@code mapF} té 6 agents que comencen molt junts dins una sala
     * enorme i oberta, així que la frontera de BFS (oberts) creix igual de descontrolada
     * amb LNT o sense — la diferència es veu en {@code tallats}/{@code tancats} (sense
     * LNT no es detecta cap repetit: {@code tallats=0}). Amb el heap per defecte de la
     * JVM pot trigar molt a fallar; per veure l'{@code OutOfMemoryError} en un temps
     * raonable, limita'l explícitament, p.ex. {@code java -Xmx512m ...}.
     */
    public static void bfs_mapF_ambLNT() throws Exception { executaUnaCerca(new CercaBFS(true), "mapF"); }
    public static void bfs_mapF_senseLNT() throws Exception { executaUnaCerca(new CercaBFS(false), "mapF"); }

    /** Exemple de prova de concurs: A* amb heurística avançada sobre tots els mapes. */
    public static void astarAvancada_totsElsMapes() throws Exception {
        List<String[]> files = new ArrayList<>();
        files.add(Main.CSV_HEADER);
        for (Mapa mapa : Main.loadMaps(new File(Main.DIR_MAPES))) {
            Main.executarIRecopilarResultats(new CercaAStar(true, new HeuristicaAvancada()), mapa, files);
        }
        new CsvWriter(FITXER_SORTIDA).write(files);
    }

    private static void executaUnaCerca(Cerca cerca, String nomMapa) throws Exception {
        Mapa mapa = new Mapa(Path.of(Main.DIR_MAPES, nomMapa + MapaBase.MAP_FILE_EXTENSION));
        List<String[]> files = new ArrayList<>();
        files.add(Main.CSV_HEADER);
        Main.executarIRecopilarResultats(cerca, mapa, files);
        new CsvWriter(FITXER_SORTIDA).write(files);
    }
}
