package edu.epsevg.prop.ac1.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Representa l'estat d'una cerca multiagent sobre una graella rectangular.
 * <p>
 * Cada agent es representa al fitxer de mapa amb una lletra majúscula ('A', 'B', ...,
 * consecutives i sense salts) i ha d'arribar a la seva pròpia sortida, marcada amb la
 * mateixa lletra en minúscula ('a' és la sortida de l'agent 'A', etc.). El contorn del
 * mapa (fila 0, última fila, columna 0, última columna) ha de ser sempre paret ('#') o
 * una sortida; mai una casella buida ('·', punt volat) ni un agent.
 * <p>
 * En cada torn es mou un únic agent en una de les quatre direccions bàsiques
 * ({@link Direccio}). La casella que abandona es converteix sempre en paret (hi deixa
 * un rastre pel qual ja no es podrà tornar a passar). Si arriba a la seva pròpia
 * sortida, l'agent desapareix del tauler i la casella de sortida també queda segellada
 * com a paret. La cerca acaba quan tots els agents han sortit.
 * <p>
 * Aquesta classe implementa tot el mecanisme del problema (parseig del fitxer,
 * validacions, moviment, generació d'accions possibles i detecció de meta), però deixa
 * com a <b>abstractes</b> {@link #equals(Object)} i {@link #hashCode()}: com que ara la
 * graella canvia amb cada moviment (els rastres són permanents), decidir quan dos
 * estats són "el mateix" a efectes de detecció de cicles/repeticions (LNT) ja no és
 * trivial ni ve donat — és responsabilitat de la subclasse concreta que se n'encarregui,
 * ja que d'això depèn directament la correcció i l'eficàcia de la detecció de cicles.
 * Per la mateixa raó, la construcció de la còpia sobre la qual {@link #mou(Moviment)}
 * aplica el moviment es delega al mètode fàbrica abstracte {@link #clona()}.
 */
public abstract class MapaBase implements Comparable<MapaBase> {

    /** Valor de graella: paret (infranquejable). */
    public static final int PARET = -1;
    /** Valor de graella: casella buida i transitable. */
    public static final int ESPAI = 0;
    /** Extensió dels fitxers de mapa. */
    public static final String MAP_FILE_EXTENSION = ".map";

    private static final char PARET_CHAR = '#';
    private static final char ESPAI_CHAR = '·';

    protected final int n;
    protected final int m;
    protected final int[][] grid;

    /** Posició actual de cada agent (l'índex és l'id de l'agent: 0 = 'A', 1 = 'B'...); {@code null} si ja ha sortit. */
    protected final List<Posicio> agents;
    /** Posició de la sortida assignada a cada agent (mateix índex que {@link #agents}), fixa. */
    protected final List<Posicio> sortides;
    private final String name;

    /**
     * Parseja un mapa des d'un fitxer de text.
     * @param fitxer ruta del fitxer de mapa
     * @throws IOException si no es pot llegir el fitxer
     * @throws IllegalArgumentException si el mapa no és rectangular, si el contorn no
     *         és tot parets/sortides, si els agents no són consecutius des de 'A', o si
     *         el nombre de sortides no coincideix amb el nombre d'agents
     */
    protected MapaBase(Path fitxer) throws IOException {
        List<String> lines = Files.readAllLines(fitxer);
        if (lines.isEmpty()) throw new IllegalArgumentException("El mapa és buit");
        int nLoc = lines.size();
        int mLoc = lines.get(0).length();
        for (String linia : lines) {
            if (linia.length() != mLoc) throw new IllegalArgumentException("El mapa ha de ser rectangular");
        }

        int[][] gridLoc = new int[nLoc][mLoc];
        Posicio[] agentPos = new Posicio[26];
        Posicio[] sortidaPos = new Posicio[26];
        boolean[] agentTrobat = new boolean[26];
        boolean[] sortidaTrobada = new boolean[26];

        for (int i = 0; i < nLoc; i++) {
            String linia = lines.get(i);
            for (int j = 0; j < mLoc; j++) {
                char c = linia.charAt(j);
                boolean vora = (i == 0 || i == nLoc - 1 || j == 0 || j == mLoc - 1);
                if (c == PARET_CHAR) {
                    gridLoc[i][j] = PARET;
                } else if (c == ESPAI_CHAR) {
                    if (vora) throw new IllegalArgumentException("El contorn del mapa ha de ser tot parets o sortides: (" + i + "," + j + ")");
                    gridLoc[i][j] = ESPAI;
                } else if (Character.isUpperCase(c)) {
                    if (vora) throw new IllegalArgumentException("Un agent no pot ser al contorn del mapa: (" + i + "," + j + ")");
                    int idx = c - 'A';
                    if (agentTrobat[idx]) throw new IllegalArgumentException("Agent '" + c + "' repetit al mapa");
                    agentTrobat[idx] = true;
                    agentPos[idx] = new Posicio(i, j);
                    gridLoc[i][j] = ESPAI;
                } else if (Character.isLowerCase(c)) {
                    int idx = c - 'a';
                    if (sortidaTrobada[idx]) throw new IllegalArgumentException("Sortida '" + c + "' repetida al mapa");
                    sortidaTrobada[idx] = true;
                    sortidaPos[idx] = new Posicio(i, j);
                    gridLoc[i][j] = ESPAI;
                } else {
                    throw new IllegalArgumentException("Caràcter no reconegut al mapa: '" + c + "'");
                }
            }
        }

        int k = 0;
        while (k < 26 && agentTrobat[k]) k++;
        for (int idx = k; idx < 26; idx++) {
            if (agentTrobat[idx]) throw new IllegalArgumentException("Els agents han de ser consecutius a partir de 'A', sense salts");
        }
        if (k == 0) throw new IllegalArgumentException("Agents no definits.");
        for (int idx = 0; idx < k; idx++) {
            if (!sortidaTrobada[idx]) throw new IllegalArgumentException("Falta la sortida per a l'agent '" + (char) ('A' + idx) + "'");
        }
        for (int idx = k; idx < 26; idx++) {
            if (sortidaTrobada[idx]) throw new IllegalArgumentException("Sobra la sortida '" + (char) ('a' + idx) + "': no té agent assignat");
        }

        List<Posicio> agentsLoc = new ArrayList<>(k);
        List<Posicio> sortidesLoc = new ArrayList<>(k);
        for (int idx = 0; idx < k; idx++) {
            agentsLoc.add(agentPos[idx]);
            sortidesLoc.add(sortidaPos[idx]);
        }

        this.n = nLoc;
        this.m = mLoc;
        this.grid = gridLoc;
        this.agents = agentsLoc;
        this.sortides = Collections.unmodifiableList(sortidesLoc);
        this.name = extractName(fitxer);
    }

    /**
     * Constructor de còpia: fa una còpia profunda de la graella i de les posicions dels
     * agents. Les subclasses l'hauran de cridar des del seu propi constructor de còpia.
     */
    protected MapaBase(MapaBase other) {
        this.n = other.n;
        this.m = other.m;
        this.grid = new int[n][m];
        for (int i = 0; i < n; i++) System.arraycopy(other.grid[i], 0, this.grid[i], 0, m);
        this.agents = new ArrayList<>(other.agents);
        this.sortides = other.sortides;
        this.name = other.name;
    }

    private static String extractName(Path fitxer) {
        String fn = fitxer.getFileName().toString();
        if (fn.endsWith(MAP_FILE_EXTENSION)) fn = fn.substring(0, fn.length() - MAP_FILE_EXTENSION.length());
        return fn;
    }

    /**
     * Mètode fàbrica que la subclasse ha d'implementar retornant una còpia de si mateixa
     * (mateix tipus concret) amb idèntic estat, típicament {@code return new Mapa(this);}.
     * @return una còpia de l'estat actual, del tipus concret real
     */
    protected abstract MapaBase clona();

    /** @return el nombre de files del mapa */
    public int getN() { return n; }

    /** @return el nombre de columnes del mapa */
    public int getM() { return m; }

    /** @return el nom del mapa (nom del fitxer sense l'extensió {@link #MAP_FILE_EXTENSION}) */
    public String name() { return name; }

    /**
     * @return el valor de la cel·la a la posició donada ({@link #PARET} o {@link #ESPAI});
     *         qualsevol posició fora de la graella es considera {@link #PARET}
     */
    public int getCella(Posicio p) { return getCellaIntern(p); }

    private int getCellaIntern(Posicio p) {
        if (p.x < 0 || p.x >= n || p.y < 0 || p.y >= m) return PARET;
        return grid[p.x][p.y];
    }

    /**
     * @return la llista de posicions actuals dels agents, indexada pel seu id (índex 0 =
     *         agent 0 = 'A', índex 1 = agent 1 = 'B'...); un element és {@code null} si
     *         aquell agent ja ha arribat a la seva sortida
     */
    public List<Posicio> getAgents() { return Collections.unmodifiableList(agents); }

    /**
     * @param agentId identificador de l'agent (0..k-1, coincideix amb el seu índex a
     *                 {@link #getAgents()}/{@link #getSortides()})
     * @return true si l'agent encara no ha arribat a la seva sortida
     */
    public boolean esAgentActiu(int agentId) { return agents.get(agentId) != null; }

    /**
     * @param agentId identificador de l'agent (0..k-1, coincideix amb el seu índex a
     *                 {@link #getAgents()}/{@link #getSortides()})
     * @return la posició de la sortida assignada a aquest agent
     */
    public Posicio getSortida(int agentId) { return sortides.get(agentId); }

    /** @return la llista de sortides, una per agent, en el mateix ordre que {@link #getAgents()} */
    public List<Posicio> getSortides() { return sortides; }

    /**
     * Aplica el moviment sobre una còpia nova (no altera aquest mapa). La casella
     * d'origen sempre es converteix en paret (rastre). Si el destí és la sortida pròpia
     * de l'agent, aquest desapareix i la casella de destí també queda segellada com a
     * paret.
     * @return la nova instància, del tipus concret real, amb el moviment ja aplicat
     * @throws IllegalArgumentException si l'agent ja ha sortit, el destí és una paret,
     *         o el destí està ocupat per un altre agent
     */
    public final MapaBase mou(Moviment acc) {
        int aid = acc.getAgentId();
        if (aid < 0 || aid >= agents.size()) throw new IllegalArgumentException("Agent id invàlid");
        Posicio actual = agents.get(aid);
        if (actual == null) throw new IllegalArgumentException("L'agent " + aid + " ja ha sortit");
        Posicio dest = actual.translate(acc.getDireccio());
        if (getCellaIntern(dest) == PARET) throw new IllegalArgumentException("Moviment cap a una paret");
        for (int i = 0; i < agents.size(); i++) {
            if (i == aid) continue;
            if (dest.equals(agents.get(i))) throw new IllegalArgumentException("Col·lisió amb un altre agent");
        }

        MapaBase nou = clona();
        nou.grid[actual.x][actual.y] = PARET;
        if (dest.equals(sortides.get(aid))) {
            nou.agents.set(aid, null);
            nou.grid[dest.x][dest.y] = PARET;
        } else {
            nou.agents.set(aid, dest);
        }
        return nou;
    }

    /**
     * @return la llista de moviments possibles des de l'estat actual: per cada agent que
     *         encara no hagi sortit, cadascuna de les quatre direccions que no vagin cap
     *         a una paret ni xoquin amb un altre agent
     */
    public final List<Moviment> getAccionsPossibles() {
        List<Moviment> res = new ArrayList<>();
        for (int aid = 0; aid < agents.size(); aid++) {
            Posicio actual = agents.get(aid);
            if (actual == null) continue;
            for (Direccio d : Direccio.values()) {
                Posicio dest = actual.translate(d);
                if (getCellaIntern(dest) == PARET) continue;
                boolean colisio = false;
                for (int j = 0; j < agents.size(); j++) {
                    if (j == aid) continue;
                    if (dest.equals(agents.get(j))) { colisio = true; break; }
                }
                if (colisio) continue;
                res.add(new Moviment(aid, d));
            }
        }
        return res;
    }

    /** @return true si tots els agents han arribat a la seva sortida */
    public final boolean esFinalPartida() {
        for (Posicio p : agents) if (p != null) return false;
        return true;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public int compareTo(MapaBase other) { return this.name.compareTo(other.name); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mapa ").append(name).append(" - Agents:");
        for (int i = 0; i < agents.size(); i++) {
            Posicio p = agents.get(i);
            sb.append(' ').append(i).append(p == null ? "(sortit)" : p.toString());
        }

        sb.append('\n');
        for (int i = 0; i < this.getN(); i++) {
            for (int j = 0; j < this.getM(); j++) {
                int c = grid[i][j];
                if (c == PARET) {
                    sb.append('#');
                } else if (c == ESPAI) {
                    sb.append(' ');
                }                
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    //===================================================================
    // Utilitats per a les heurístiques
    //===================================================================
    public static final int INF = Integer.MAX_VALUE / 4;

    /**
     * Distàncies mínimes (en nombre de passos,  tenint en compte de no passar per les parets
     * actuals ni trepitjar els agents) des de {@code src} a cada casella de la graella, calculades amb BFS.
     * Útil per construir heurístiques informades..
     * @return matriu de distàncies indexat per {@code x, y}; Retorn {@code Integer.MAX_VALUE/4}
     *         si la casella és inabastable
     */
    public abstract int[][] distancesFrom(Posicio src);
}
