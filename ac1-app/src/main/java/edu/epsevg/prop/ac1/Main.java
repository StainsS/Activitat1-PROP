package edu.epsevg.prop.ac1;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.epsevg.prop.ac1.cerca.model.Mapa;
import edu.epsevg.prop.ac1.model.MapaBase;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import edu.epsevg.prop.ac1.utils.CsvWriter;
import edu.epsevg.prop.ac1.cerca.*;
import edu.epsevg.prop.ac1.cerca.heuristica.HeuristicaAvancada;


public class Main {
    static final String RESULTS_FILE = "results.csv";

    /**
     * Directori on hi ha els fitxers {@code .map}. Per defecte és relatiu al directori
     * de treball actual (com quan s'executa {@code mvn exec:java} des d'{@code ac1-app}),
     * però es pot sobreescriure amb la propietat de sistema {@code mapes.dir} (p.ex.
     * amb una ruta absoluta) — l'eina d'avaluació del corrector ho necessita perquè
     * executa cada prova en un directori de treball propi.
     */
    public static final String DIR_MAPES = System.getProperty("mapes.dir", "./src/main/resources");

    /** Capçalera comuna de les files de resultats (reutilitzada per l'eina d'avaluació). */
    public static final String[] CSV_HEADER = {"Algoritme", "usaLNT", "Mapa", "Trobat", "Valida", "Longitud",
            "NodesExplorats", "NodesTallats", "MemoriaPicOberts", "MemoriaPicTancats",
            "MemoriaPicTotal", "TempsMs"};

    public static void main(String[] args) throws Exception {

        // Maps conté tots els mapes per ordre alfabètic
        final List<Mapa> maps = loadMaps(new File(DIR_MAPES));

        boolean usarLNT = true;

        List<String[]> csv = new ArrayList<>();
        csv.add(CSV_HEADER);

        Mapa m = maps.get(0); // Carrega el primer mapa

        executarIRecopilarResultats(new CercaBFS(false), m, csv);
        
        // Feu les crides que us semblin oportunes, p.ex:
        //===================================================================
        //executarIRecopilarResultats(  new CercaBFS(true), m, csv);
        //executarIRecopilarResultats(  new CercaDFS(false),m, csv);
        //  ...
        //executarIRecopilarResultats(  new CercaAStar(usarLNT, new HeuristicaAvancada()), m, csv);

        // Escrivim els resultats
        CsvWriter cw = new CsvWriter(RESULTS_FILE);
        cw.write(csv);
        System.out.println("CSV escrit a " + RESULTS_FILE);
    }

    /**
     * Retorna la llista de mapes carregats a partir de cada fitxer {@code .map} del
     * directori {@code directory}, ordenats pel seu nom.
     */
    public static List<Mapa> loadMaps(final File directory) throws IOException {
        System.out.println("Carpeta actual:"+directory.getAbsolutePath());
        List<Mapa> maps = new ArrayList<>();
        if(!directory.exists()) {
             throw new RuntimeException("Directori de mapes no trobat");
        }
        File[] fitxers = directory.listFiles();
        if (fitxers == null) return maps;

        for (final File entry : fitxers) {
            if (entry.isFile() && entry.getName().endsWith(MapaBase.MAP_FILE_EXTENSION)) {
                try {
                    maps.add(new Mapa(Path.of(entry.getCanonicalPath())));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Collections.sort(maps);

        return maps;
    }

    static final int LONG_BAR_LENGTH = 66;
    static final int SHORT_BAR_LENGTH = 2;
    static final String BAR_CHARACTER = "=";
    static final String LONG_BAR = BAR_CHARACTER.repeat(LONG_BAR_LENGTH);
    static final String SHORT_BAR = BAR_CHARACTER.repeat(SHORT_BAR_LENGTH);

    /**
     * Pretty-prints the provided `text` centered in a box.
     */
    static void printHeader(final String text) {
        final String padding = " ".repeat((LONG_BAR_LENGTH - text.length() - (SHORT_BAR.length() * 2)) / 2);
        final String extraRightPadding = " ".repeat(text.length() % 2);

        System.out.println(LONG_BAR);
        System.out.println(SHORT_BAR + padding + text + padding + extraRightPadding + SHORT_BAR);
        System.out.println(LONG_BAR);
    }

    public static void executarIRecopilarResultats(Cerca c, Mapa map, List<String[]> rows) {
        String className = c.getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);

        System.out.println("Executant " + className + " sobre " + map.name());

        ResultatCerca r = new ResultatCerca();
        String error = "";

        r.startTime(); // Engegar el temporitzador

        MonitorProgres monitor = new MonitorProgres(c, map, r);
        try {
            // El monitor s'ha d'aturar abans de qualsevol altra impressió (solució, error...):
            // si no, aquesta quedaria enganxada al final de la línia de progrés.
            try {
                c.ferCerca(map, r);
            } finally {
                monitor.atura();
            }
            System.out.println("> SOLUCIO: " + r.getSolucio());
        } catch (OutOfMemoryError ooem) {
            error = "OUT OF MEMORY ERROR";
            System.out.println(error);
        } catch (Exception ex) {
            ex.printStackTrace();
            error = ex.toString();
        }

        r.stopTime(); // Aturar el temporitzador

        // Comprova que el camí retornat sigui realment una solució (fora del temps mesurat).
        String valida = ValidadorSolucio.valida(map, r.getSolucio());
        if (r.getSolucio() != null && !valida.equals(ValidadorSolucio.VALIDA)) {
            System.out.println("> SOLUCIO NO VALIDA: " + valida);
        }

        rows.add(new String[] {
            c.getNomCerca(),
            c.usesLNT()?"YES":"NO",
            map.name(),
            (r.getSolucio() != null) ? "YES" : "NO" + (error.isEmpty() ? "" : "(" + perCsv(error) + ")"),
            perCsv(valida),
            (r.getSolucio() != null) ? String.valueOf(r.getSolucio().size()) : "-",
            String.valueOf(r.getNodesExplorats()),
            String.valueOf(r.getNodesTallats()),
            String.valueOf(r.getMemoriaPicOberts()),
            String.valueOf(r.getMemoriaPicTancats()),
            String.valueOf(r.getMemoriaPicTotal()),
            String.valueOf(r.getTempsMs())
        });

        System.out.println("\t" + className + " -> " + r + "\n");
    }

    /** Treu del text els separadors de camp i els salts de línia, que trencarien el CSV. */
    private static String perCsv(String text) {
        return text.replaceAll("[" + CsvWriter.CSV_SEPARATOR + "\\r\\n]+", " ");
    }

    /**
     * Fil (dimoni) que, cada segon, actualitza a la consola l'estat provisional d'un
     * {@link ResultatCerca} mentre la cerca encara està en marxa al fil principal — sempre
     * a la mateixa línia (amb un retorn de carro {@code \r}, sense saltar de línia), en
     * lloc d'anar-ne afegint una de nova cada vegada. Les dades es llegeixen directament
     * del resultat (els seus camps són {@code volatile}): no cal cap altra sincronització.
     * S'engega en construir-lo i s'atura amb {@link #atura()}.
     */
    private static final class MonitorProgres {
        private final ScheduledExecutorService executor;
        /** Longitud de l'última línia impresa (0 si encara no se n'ha imprès cap). */
        private final AtomicInteger longitudLinia = new AtomicInteger(0);

        MonitorProgres(Cerca c, Mapa map, ResultatCerca r) {
            ThreadFactory dimoni = runnable -> {
                Thread t = new Thread(runnable, "monitor-progres");
                t.setDaemon(true);
                return t;
            };
            executor = Executors.newSingleThreadScheduledExecutor(dimoni);
            executor.scheduleAtFixedRate(() -> imprimeix(c.getNomCerca(), map.name(), r),
                    1, 1, TimeUnit.SECONDS);
        }

        /**
         * Atura el fil, esperant que acabi l'actualització en curs si n'hi ha, i, si s'ha
         * arribat a imprimir alguna línia de progrés, salta de línia perquè la següent
         * impressió no hi quedi enganxada.
         */
        void atura() {
            executor.shutdown();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            if (longitudLinia.get() > 0) System.out.println();
        }

        private void imprimeix(String nomCerca, String nomMapa, ResultatCerca r) {
            StringBuilder sb = new StringBuilder();
            sb.append('[').append(nomCerca).append('/').append(nomMapa).append("] ")
              .append(r.getTempsTranscorregutMs() / 1000).append("s: explorats=").append(r.getNodesExplorats())
              .append(" tallats=").append(r.getNodesTallats())
              .append(" profunditatMax=").append(r.getProfunditatMaxima())
              .append(" memòria(oberts/tancats/total)=").append(r.getMemoriaPicOberts()).append('/')
              .append(r.getMemoriaPicTancats()).append('/').append(r.getMemoriaPicTotal());
            if (r.teHeuristicaDisponible()) {
                sb.append(" heurMin=").append(r.getHeuristicaMinima());
            }

            int longitudActual = sb.length();
            int espaiSobrant = longitudLinia.getAndSet(longitudActual) - longitudActual;
            if (espaiSobrant > 0) sb.append(" ".repeat(espaiSobrant)); // esborra la resta de la línia anterior, més llarga

            System.out.print("\r" + sb);
            System.out.flush();
        }
    }
}
