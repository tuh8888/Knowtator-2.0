package edu.ucdenver.ccp.knowtator.owl;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

class OntologyTranslator {
    public static final Logger log = Logger.getLogger(KnowtatorManager.class);

    private static final String SEP = ":";

    private static final String PREFIX_GO = "GO";

    private static final String LOCATION_GO = "http://purl.obolibrary.org/obo/go.owl";
//    public static final String VERSION_GO = "http://purl.obolibrary.org/obo/go/releases/2017-10-11/go.owl";
    private static final String LOCATION_GO_BASIC = "http://purl.obolibrary.org/obo/go/go-basic.obo";


    private static final Map<String, String> PREFIX_LOCATION_MAP = new HashMap<String, String>() {
        {
            put(PREFIX_GO, LOCATION_GO);
        }
    };

    private static final Map<String, String> LOCATION_VERSION_MAP = new HashMap<String, String>() {
        {
            put(LOCATION_GO, LOCATION_GO_BASIC);
        }
    };

    public static String translate(String toTranslate) {
        String prefix = toTranslate.split(SEP)[0];

        return(PREFIX_LOCATION_MAP.get(prefix));
    }

    public static String whichOntologyToUse(String ontologyLocation) {
        return (LOCATION_VERSION_MAP.get(ontologyLocation));
    }
}
