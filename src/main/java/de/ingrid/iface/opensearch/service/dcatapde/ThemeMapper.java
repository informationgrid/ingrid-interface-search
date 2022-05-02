package de.ingrid.iface.opensearch.service.dcatapde;

import de.ingrid.iface.opensearch.model.dcatapde.general.Theme;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThemeMapper {
    public static Set<Theme> mapThemes(List<String> categories, List<String> keywords) {
        Set<Theme> themes = new HashSet<>();
        for(String category: categories){
            switch (category){
                case "farming":
                    themes.add(Theme.AGRI);
                    themes.add(Theme.ENVI);
                    break;
                case "biota":
                    themes.add(Theme.ENVI);
                    break;
                case "boundaries":
                    themes.add(Theme.REGI);
                    themes.add(Theme.GOVE);
                    break;
                case "climatologyMeteorology Atmosphere":
                    themes.add(Theme.ENVI);
                    themes.add(Theme.TECH);
                    break;
                case "economy":
                    themes.add(Theme.ECON);
                    if (keywords.contains("Energiequellen")) {
                        themes.add(Theme.ENER);
                        themes.add(Theme.ENVI);
                        themes.add(Theme.TECH);
                    }
                    if (keywords.contains("Mineralische Bodenschätze")) {
                        themes.add(Theme.ENVI);
                        themes.add(Theme.TECH);
                    }
                    break;
                case "elevation":
                    themes.add(Theme.ENVI);
                    themes.add(Theme.GOVE);
                    themes.add(Theme.TECH);
                    break;
                case "environment":
                    themes.add(Theme.ENVI);
                    break;
                case "geoscientificInformation":
                    themes.add(Theme.REGI);
                    themes.add(Theme.ENVI);
                    themes.add(Theme.TECH);
                    break;
                case "health":
                    themes.add(Theme.HEAL);
                    break;
                case "imageryBaseMapsEarthCover ":
                    themes.add(Theme.ENVI);
                    themes.add(Theme.GOVE);
                    themes.add(Theme.TECH);
                    themes.add(Theme.REGI);
                    themes.add(Theme.AGRI);
                    break;
                case "intelligenceMilitary":
                    themes.add(Theme.JUST);
                    break;
                case "inlandWaters":
                    themes.add(Theme.ENVI);
                    themes.add(Theme.TRAN);
                    themes.add(Theme.AGRI);
                    break;
                case "location":
                    themes.add(Theme.REGI);
                    themes.add(Theme.GOVE);
                    break;
                case "oceans":
                    themes.add(Theme.ENVI);
                    themes.add(Theme.TRAN);
                    themes.add(Theme.AGRI);
                    break;
                case "planningCadastre":
                    themes.add(Theme.REGI);
                    themes.add(Theme.GOVE);
                    if (keywords.contains("Flurstücke/Grundstücke")) {
                        themes.add(Theme.JUST);
                    }
                    break;
                case "society":
                    themes.add(Theme.SOCI);
                    themes.add(Theme.EDUC);
                    break;
                case "structure":
                    themes.add(Theme.REGI);
                    themes.add(Theme.TRAN);
                    if (keywords.contains("Produktions- und Industrieanlagen")) {
                        themes.add(Theme.ECON);
                    }
                    if (keywords.contains("Umweltüberwachung")) {
                        themes.add(Theme.ENVI);
                    }
                    break;
                case "transportation":
                    themes.add(Theme.TRAN);
                    break;
                case "utilitiesCommunication":
                    themes.add(Theme.ENER);
                    themes.add(Theme.ENVI);
                    themes.add(Theme.GOVE);
                    break;
            }
        }
        for(Theme theme: Theme.values()){
            if(keywords.contains(theme.toString())){
                themes.add(theme);
            }
        }
        return themes;
    }
}
