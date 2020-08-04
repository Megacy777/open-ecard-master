/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.richclient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a fix that sets the JNA runtime directory to an executable directory, because the default value
 * /tmp may be mounted as 'noexec' on some systems, which will prevent the startup of the app.
 *
 * @author Sebastian Schuberth
 */
public class JnaRuntimeDirectoryFix {

    private static final Logger LOG = LoggerFactory.getLogger(JnaRuntimeDirectoryFix.class);
    private static final String NOEXEC_FLAG = "noexec";
    private static final String JNA_TMP_DIR = "jna.tmpdir";

    /**
     * Sets the runtime directory for JNA to an executable directory
     * <p>
     * The directory will be set to one of the following, in descending order of priority:
     * <ul>
     * <li>the directory pointed to by the System property <em>jna.tmpdir</em>, if this value is set</li>
     * <li>the <em>/tmp</em> directory, if it is not mounted as 'noexec'. This is also the default for JNA.</li>
     * <li>the directory pointed to by the enviroment variable <em>XDG_RUNTIME_DIR</em>, if this value is set and the
     * referenced directory is not mounted as 'noexec'.</li>
     * <li><em>~/.openecard/run</em> as a fallback if none of the above work
     * </ul>
     *
     * @throws IOException if there is an error reading the "/proc/mounts" file, which contains information about
     * whether a path is mounted as 'noexec' or not
     */
    public static void setJnaRuntimeDirectory() throws IOException {
	// read value of jna.tmpdir property
	Properties properties = new Properties(System.getProperties());
	String propJnaTmpDir = properties.getProperty(JNA_TMP_DIR);

	// if the property has been set externally don't change it
	if (propJnaTmpDir != null) {
	    LOG.info("Use {} from jna.tmpdir as tmp directory for JNA");
	    return;
	}

	//check if we are on Linux
	String osName = properties.getProperty("os.name");
	if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
	    // parse "/proc/mounts"
	    List<MountInfo> mountInfos = MountInfo.getMounts();

	    // convert mountInfos to mapping from mount path to MountInfo to speed up lookup of mount paths
	    Map<String, MountInfo> map = new HashMap<>();
	    for (MountInfo mountInfo : mountInfos) {
		map.put(mountInfo.getMountPath(), mountInfo);
	    }

	    // get tmp directory
	    String tmpDirPath = properties.getProperty("java.io.tmpdir");
	    // get canonical file for tmp directory, as it might be a symlink
	    File tmpDirectory = new File(tmpDirPath).getCanonicalFile();
	    // check if noexec flag is set for tmp directory
	    boolean noexecSetForTmp = checkIfNoexecFlagIsSetForLongestMatchingMountPath(map, tmpDirectory);

	    if (!noexecSetForTmp) {
		// we can use tmp directly and as JNA uses it as default anyway, nothing more to do here
		LOG.info("Use default {} as tmp directory for JNA", tmpDirPath);
		return;
	    }

	    // we cannot use tmp directly, check the user run dir next
	    // first read "XDG_RUNTIME_DIR" to see if a user run dir is set
	    String userRuntimeDirPath = System.getenv("XDG_RUNTIME_DIR");

	    // if it is set, then check for "noexec" flag like with tmp before
	    boolean userRunDirSet = (userRuntimeDirPath != null);
	    if (userRunDirSet) {
		// get canonical file for user run time directory, as it might be a symlink
		File userRuntimeDirectory = new File(userRuntimeDirPath).getCanonicalFile();
		boolean noexecSetForUserRuntimeDir = checkIfNoexecFlagIsSetForLongestMatchingMountPath(map, userRuntimeDirectory);

		if (!noexecSetForUserRuntimeDir) {
		    // the user run dir is set and executable, set jna.tempdir to XDG_RUNTIME_DIR
		    LOG.info("Setting jna.tmpdir to user run dir at {}", userRuntimeDirPath);
		    System.getProperties().put(JNA_TMP_DIR, userRuntimeDirPath);
		    return;
		}
	    }
	    // neither tmp nor user run dir are usable,  use '~/.openecard/run' as last ressort
	    LOG.info("Setting jna.tmpdir to be '~/.openecard/run' as last ressort");
	    String homeDir = properties.getProperty("user.home");
	    String openEcardRunDirectory = homeDir + "/.openecard/run";
	    System.getProperties().put(JNA_TMP_DIR, openEcardRunDirectory);
	}
    }

    /**
     * Checks if for the provided File the longest matching mount path in the provided Map has the "noexec" flag set
     *
     * @param map a mapping from mount paths to their corresponding {@link MountInfo}
     * @param file the {@link File} to check
     * @return true, if the "noexec" flag for the provided File is set; false, if the "noexec" flag is not set or if the
     * file does not have a matching mount path in the Mapping
     * @throws IOException if an I/O error occurs during the construction of the canonical pathname
     */
    private static boolean checkIfNoexecFlagIsSetForLongestMatchingMountPath(Map<String, MountInfo> map, File file)
	    throws IOException {

	// Default case: if the provided dir is not in /proc/mounts, then the "noexec" flag is not set
	boolean noexecFlagSet = false;

	// find the closest parent to the directory and 
	// see if it is in /proc/mounts and if so, if it has the "noexec" flag
	while (file != null) {
	    String path = file.getCanonicalPath();
	    if (map.containsKey(path)) {
		// we found the longest matching mount path
		MountInfo mountInfo = map.get(path);
		// check if "noexec" flag is set in the mountOptions
		noexecFlagSet = mountInfo.checkIfFlagIsSet(NOEXEC_FLAG);
		break;
	    }
	    // continue with parent of current file
	    file = file.getParentFile();
	}
	return noexecFlagSet;
    }

}
