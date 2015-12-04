/*
 * Project: asqare
 * Copyright (C) 2008-2012 rdrr.labs@gmail.com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.sjsu.cmpe202.gamelist;

import android.provider.BaseColumns;

//-----------------------------------------------

/**
 * Column names and URI constants for for the games table & provider
 */
public class Columns implements BaseColumns {

    /** The default sort order for this table, modified_ms DESC */
    public static final String DEFAULT_SORT_ORDER = "modified_ms DESC";

    /** The gameplay class name (FQCN). Type: TEXT */
    public static final String GAMEPLAY = "gameplay";

    /** The score of the saved game. Type: TEXT */
    public static final String SCORE = "score";

    /** The state of the saved game. Type: TEXT */
    public static final String STATE = "state";

    /**
     * The System.currentTimeMillis timestamp of when the game was created.
     * Type: INTEGER (long)
     */
    public static final String CREATED_MS = "created_ms";

    /**
     * The System.currentTimeMillis timestamp of when the game was last modified.
     * Type: INTEGER (long)
     */
    public static final String MODIFIED_MS = "modified_ms";

}
