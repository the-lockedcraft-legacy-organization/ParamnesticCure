/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.mcsmp;

import java.util.logging.Logger;
import static org.bukkit.Bukkit.getLogger;
import org.bukkit.Location;

/**
 * @author InteriorCamping
 * 
 * @deprecated This class is not used by the plugin anymore. See classes involving databases.
 */
public class CoreProtectData{
    //Establishes plugin
    private ParamnesticCure plugin;
    //Establishes logger
    private Logger log = getLogger();
    //stores RollbackManager if initialized.
    private static CoreProtectData instance;

    /**
     * Fragment of an outdated method once used to manage logger data.
     *
     * @deprecated no longer used by paramnestic (see classes related to databases)
     * @param cpAction coreprotect action id.
     * @param cpWhere Location where action occured.
     **/
    public CoreProtectData(boolean cpAction, Location cpWhere){
    }
}
