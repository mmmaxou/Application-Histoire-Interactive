package fr.inkarma.Inkarma.Inkarma;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul.rodrigues on 24/02/16.
 */
public class Data {

    Map<Integer,Frame> frame;


    public Data(Context context) throws XmlPullParserException, IOException {

        //instanciations
        frame = new HashMap() ;
        Frame currentFrame = null;
        int cpt = 0;

        // Récupérer le fichier xml
        XmlResourceParser xpp = context.getResources().getXml(R.xml.data);

        //début de l'analyse du xml
        xpp.next();
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {// tant que pas fini...
            if (eventType == XmlPullParser.START_TAG) {
                if ("frame".equals(xpp.getName())) {// début d'une nouvelle catégorie
                    currentFrame = new Frame();
                    currentFrame.id = xpp.getAttributeIntValue(null, "id", -1);
                    frame.put(currentFrame.id, currentFrame);
                    cpt = 0;
                } else if ("choix".equals(xpp.getName())) {// début d'une nouvelle catégorie
                    currentFrame.choix[cpt] = xpp.getAttributeIntValue(null, "consequence", -1);
                    cpt++;
                } else if ("img".equals(xpp.getName())) {// début d'une nouvelle image
                    String src = xpp.getAttributeValue(null, "src");
                    if (src != null) {
                        currentFrame.img = context.getResources().getIdentifier(src, "drawable", context.getPackageName());
                    }
                } else if ("expression".equals(xpp.getName())) {
                    String src = xpp.getAttributeValue(null, "src");
                    if (src != null) {
                        currentFrame.expression = context.getResources().getIdentifier(src, "drawable", context.getPackageName());
                    }
                } else if ("locuteur".equals(xpp.getName())) {
                    currentFrame.locuteur = xpp.getAttributeValue(null, "personnage");

                    String src = currentFrame.locuteur;
                    if ( src != null ) {
                        currentFrame.locuteurImg = context.getResources().getIdentifier(src, "drawable", context.getPackageName());
                    }
                }

            } else if (eventType == XmlPullParser.END_TAG) { // fin d'un tag
            } else if (eventType == XmlPullParser.TEXT) {
                if (currentFrame != null)
                    currentFrame.text = xpp.getText(); // On met le text de coté pour la fin de la balise.
            }
            eventType = xpp.next(); // au suivant !
        }

    }

    public Frame get(int i) {
        return frame.get(i);
    }
}
