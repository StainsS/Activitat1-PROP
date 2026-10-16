package edu.epsevg.prop.ac1.model;

/**
 * Representa el moviment d'un únic agent en una de les quatre direccions bàsiques.
 * Cada torn de la cerca aplica un únic {@code Moviment} sobre un {@link MapaBase}.
 */
public class Moviment {
    private final int agentId;
    private final Direccio direccio;

    /**
     * Constructor principal
     * @param agentId el identificador de l'agent (0,1,....)
     * @param direccio és la direcció del moviment
     */
    public Moviment(int agentId, Direccio direccio) {
        this.agentId = agentId;
        this.direccio = direccio;
    }

    /**
     * @return l'Id (0,1,..) de l'agent que ha mogut
     */
    public int getAgentId() { return agentId; }
    /**
     * @return la direcció de moviment
     */
    public Direccio getDireccio() { return direccio; }

    @Override
    public String toString() {
        return "Agent " + agentId + " -> " + direccio;
    }
}
