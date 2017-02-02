package fr.inkarma.Inkarma;

/**
 * Created by paul.rodrigues on 24/02/16.
 */
public class Frame {

    int id;
    String text = "";
    String[] choix;
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
        choix= new String[2];
        choix[0] = null;
        choix[1] = null;
        choixText = new String[2];
        choixText[0] = "";
        choixText[1] = "";
    }

    public String getChoix(int n){
        return choix[n];
    }

    public boolean existChoix(int n){
        return choix[n] != null;
    }

    public int getNbChoix(){
        return existChoix(0) ? (existChoix(1) ? 2 : 1) : 0;
    }


}
