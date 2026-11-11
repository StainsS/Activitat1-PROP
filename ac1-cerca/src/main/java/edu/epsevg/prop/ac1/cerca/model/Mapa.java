package edu.epsevg.prop.ac1.cerca.model;

import edu.epsevg.prop.ac1.model.Direccio;
import edu.epsevg.prop.ac1.model.MapaBase;
import edu.epsevg.prop.ac1.model.Posicio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementació concreta de {@link MapaBase}. Aquí és on cal decidir quan dos estats
 * són "el mateix" a efectes de detecció de cicles/repeticions (LNT): com que cada
 * moviment converteix la casella d'origen en paret de manera permanent, dos estats amb
 * els mateixos agents però un rastre de parets diferent <b>no</b> són el mateix estat
 * (podrien tenir moviments futurs possibles diferents). Per tant {@link #equals(Object)}
 * i {@link #hashCode()} han de tenir en compte tant la posició de cada agent com el
 * contingut complet de la graella.
 */
public class Mapa extends MapaBase {


    public Mapa(Path fitxer) throws IOException { super(fitxer); }

    public Mapa(Mapa other) { super(other); }

    @Override
    protected MapaBase clona() { return new Mapa(this); }

    @Override
    public boolean equals(Object o) {
        boolean res = false;
        if(this == o){
            res = true;
        }
        else if(o != null || getClass() == o.getClass()){
            Mapa other = (Mapa) o;
            res = Arrays.deepEquals(grid, other.grid) && (n == other.n)
                    && (m == other.m)
                    && agents.equals(other.agents);
        }
        return res;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(n,m);
        result = 31 * result + Arrays.deepHashCode(grid);
        for (Posicio p : agents) {
            result = 31 * result + (p == null ? 0 : 31 * p.x + p.y);
        }
        return result;
    }

    /**
     * Distàncies mínimes (en nombre de passos,  tenint en compte de no passar per les parets
     * actuals ni trepitjar els agents) des de l'agent a la posició {@code src} a cada casella de la graella,
     * calculades amb BFS.
     * Útil per construir heurístiques informades..
     * @return matriu de distàncies indexat per {@code x, y}; Retorn {@code Integer.MAX_VALUE/4}
     *         si la casella és inabastable
     */
    @Override
    public int[][] distancesFrom(Posicio src) {
        //@TODO: implementar
        return null;
    }
    
    public boolean isSolution() { 
        boolean resultat = true;
        for(Posicio p : agents){
            if (p != null) resultat = false;
        }
        return resultat;
    }
}
