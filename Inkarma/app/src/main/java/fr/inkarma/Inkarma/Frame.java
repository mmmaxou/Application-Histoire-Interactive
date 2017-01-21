package fr.inkarma.Inkarma;

/**
 * Created by paul.rodrigues on 24/02/16.
 */
public class Frame {

    int id;
    String text;
    int[] choix;
    int img = -1;
    String imgTag = "";
    int expression = -1;
    int locuteurImg;
    String[] choixText;
    String locuteur = "";
    int karma = 0;
    boolean karmaEvaluated = false;

    int music = -1;

    Frame precedent,suivant;

    public Frame() {
        choix= new int[2];
        choix[0] = -1;
        choix[1] = -1;
        choixText = new String[2];
        choixText[0] = "";
        choixText[1] = "";
    }
}
