/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 1997-2007 by wemove GmbH
 */
package de.ingrid.iface.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.ingrid.utils.PlugDescription;


/**
 * Helper class dealing with all aspects of iPlugs (PlugDescription).
 *
 * @author joachim@wemove.com
 */
public class IPlugHelper {

	public final static String HIT_KEY_OBJ_ID = "t01_object.obj_id";
	public static final String HIT_KEY_ADDRESS_ADDRID = "t02_address.adr_id";
	
	/**
     * Return true if the given iPlug has a specific data type.
     * 
     * @param iPlug The PlugDescription to work on.
     * @param dataType The data type to search for
     * @return True if the iPlug has the data type, false if not.
     */
    public static boolean hasDataType(PlugDescription iPlug, String dataType) {
        String[] dataTypes = iPlug.getDataTypes();
        for (int i=0; i<dataTypes.length; i++) {
            if (dataTypes[i].equalsIgnoreCase(dataType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the given iPlug has a specific partner.
     * 
     * @param iPlug The PlugDescription to work on.
     * @param partnerId The partner to search for (pass id)
     * @return True if the iPlug has the partner, false if not.
     */
    public static boolean hasPartner(PlugDescription iPlug, String partnerId) {
        String[] partners = iPlug.getPartners();
        for (int i=0; i<partners.length; i++) {
            if (partners[i].equals(partnerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns only iPlugs of given type from given iPlugs  
     * 
     * @param iPlugs iPlugs of different types which will be filtered
     * @param plugTypes The DataTypes to look for
     * @return Array containing filtered iPlugs or empty
     */
    public static PlugDescription[] filterIPlugsByType(PlugDescription[] iPlugs, String[] plugTypes) {
        ArrayList result = new ArrayList();
        for (int i = 0; i < iPlugs.length; i++) {
            PlugDescription plug = iPlugs[i];
            for (int j = 0; j < plugTypes.length; j++) {
            	if (hasDataType(plug, plugTypes[j])) {
            		result.add(plug);
            		break;
                }
            }
        }

        return (PlugDescription[]) result.toArray(new PlugDescription[result.size()]);
    }

    /**
     * Returns only iPlugs of given Partner from given iPlugs  
     * 
     * @param iPlugs iPlugs of different partners which will be filtered
     * @param partners The partners to look for
     * @return Array containing filtered iPlugs or empty
     */
    public static PlugDescription[] filterIPlugsByPartner(PlugDescription[] iPlugs, ArrayList partners) {
        ArrayList result = new ArrayList();
        for (int i = 0; i < iPlugs.length; i++) {
            PlugDescription plug = iPlugs[i];
            for (int j = 0; j < partners.size(); j++) {
            	if (hasPartner(plug, (String)partners.get(j))) {
            		result.add(plug);
            		break;
                }
            }
        }

        return (PlugDescription[]) result.toArray(new PlugDescription[result.size()]);
    }

    public static PlugDescription[] sortPlugs(PlugDescription[] iPlugs, Comparator plugComparator) {
        List plugList = Arrays.asList(iPlugs);
        Collections.sort(plugList, plugComparator);

        return (PlugDescription[]) plugList.toArray(new PlugDescription[plugList.size()]);
    }
}
