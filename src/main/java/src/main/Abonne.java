package src.main;


public class Abonne {

    String nom;
    String prenom;
    int numeroAbonne;

    public Abonne(String nom, String prenom, int numeroAbonne) {
        this.nom = nom;
        this.prenom = prenom;
        this.numeroAbonne = numeroAbonne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonne abonne = (Abonne) o;
        return numeroAbonne == abonne.numeroAbonne;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(numeroAbonne);
    }
}
