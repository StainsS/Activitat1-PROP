package edu.epsevg.prop.ac1;

import edu.epsevg.prop.ac1.model.MapaBase;
import edu.epsevg.prop.ac1.model.Moviment;
import edu.epsevg.prop.ac1.model.Posicio;

import java.util.List;

/**
 * Comprova que una seqüència de moviments retornada per una cerca sigui realment una
 * solució: reprodueix els moviments un a un sobre l'estat inicial i verifica que cadascun
 * sigui legal i que, en acabar, tots els agents hagin sortit.
 * <p>
 * La reproducció es fa sobre una còpia pròpia del mapa ({@link MapaReferencia}), que
 * només fa servir la lògica de {@link MapaBase}: el resultat no depèn de cap codi
 * d'{@code ac1-cerca} (ni del {@code clona()} ni de l'{@code equals()}/{@code hashCode()}
 * de {@code Mapa}).
 */
public final class ValidadorSolucio {
    private ValidadorSolucio() {}

    /** Valor retornat quan la solució és vàlida. */
    public static final String VALIDA = "YES";

    /**
     * @param inicial l'estat inicial sobre el qual s'ha fet la cerca
     * @param solucio la seqüència de moviments trobada (pot ser {@code null})
     * @return {@code "-"} si no hi ha solució, {@link #VALIDA} si és vàlida, o
     *         {@code "NO(motiu)"} indicant el primer moviment il·legal o els agents que
     *         no han arribat a la sortida
     */
    public static String valida(MapaBase inicial, List<Moviment> solucio) {
        if (solucio == null) return "-";
        MapaBase estat = new MapaReferencia(inicial);
        for (int i = 0; i < solucio.size(); i++) {
            Moviment mov = solucio.get(i);
            try {
                estat = estat.mou(mov);
            } catch (RuntimeException e) {
                return "NO(moviment " + (i + 1) + " [" + mov + "]: " + e.getMessage() + ")";
            }
        }
        if (!estat.esFinalPartida()) {
            StringBuilder pendents = new StringBuilder();
            List<?> agents = estat.getAgents();
            for (int id = 0; id < agents.size(); id++) {
                if (agents.get(id) != null) {
                    if (pendents.length() > 0) pendents.append(' ');
                    pendents.append((char) ('A' + id));
                }
            }
            return "NO(no han sortit: " + pendents + ")";
        }
        return VALIDA;
    }

    /** Còpia de l'estat que només usa el codi donat de {@link MapaBase}. */
    private static final class MapaReferencia extends MapaBase {
        MapaReferencia(MapaBase other) { super(other); }

        @Override
        protected MapaBase clona() { return new MapaReferencia(this); }

        @Override
        public boolean equals(Object o) { return this == o; }

        @Override
        public int hashCode() { return System.identityHashCode(this); }

        @Override
        public int[][] distancesFrom(Posicio src) {
            throw new UnsupportedOperationException("No cal per validar una solució");
        }
    }
}
