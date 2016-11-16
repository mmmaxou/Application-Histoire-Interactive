package com.example.maximilienpluchard.inkarma;

/**
 * Created by paul.rodrigues on 24/02/16.
 */
public class Frame {

    int id;
    String text;
    int[] choix;
    int img = -1;
    int expression = -1;
    int locuteurImg;
    String locuteur = "";

    public Frame() {
        choix= new int[2];
        choix[0] = -1;
        choix[1] = -1;

    }
}
