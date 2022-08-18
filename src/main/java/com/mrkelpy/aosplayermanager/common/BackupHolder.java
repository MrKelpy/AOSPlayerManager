package com.mrkelpy.aosplayermanager.common;

import java.util.Date;

/**
 * This class is used to store and handle a backup for the playerdata of a player.
 * It stores both the playerdata and any useful information about the backup.
 */
public class BackupHolder {

    private final PlayerDataHolder playerdata;
    private final Date saveDate;

    /**
     * Main constructor for the BackupHolder.
     * @param playerdata The playerdata to be stored.
     * @param saveDate When playerdata was saved.
     */
    public BackupHolder(PlayerDataHolder playerdata, Date saveDate) {
        this.playerdata = playerdata;
        this.saveDate = saveDate;
    }

    /**
     * Gets the playerdata stored in this BackupHolder.
     * @return The playerdata.
     */
    public PlayerDataHolder getPlayerdata() {
        return playerdata;
    }

    /**
     * Gets the date when the playerdata was saved.
     * @return The date.
     */
    public Date getSaveDate() {
        return saveDate;
    }


}

