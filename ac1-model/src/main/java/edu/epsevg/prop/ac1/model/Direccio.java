package edu.epsevg.prop.ac1.model;

/**
 * Enumeració d'utilitat per representar les direccions de
 * moviment dins d'una graella rectangular.
 */
public enum Direccio {
    AMUNT(-1, 0),
    AVALL(1, 0),
    ESQUERRA(0, -1),
    DRETA(0, 1);

    public final int dx;
    public final int dy;

    Direccio(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public static Direccio inverse(Direccio minDir) {
        switch(minDir){
            case DRETA: return ESQUERRA;
            case ESQUERRA: return DRETA;
            case AMUNT: return AVALL;
            case AVALL: return AMUNT;
            default: throw new RuntimeException("Unsupported direction");
        }
    }
}
