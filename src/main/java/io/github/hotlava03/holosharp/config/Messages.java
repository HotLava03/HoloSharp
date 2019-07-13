/*
 * Copyright (c) 2019 HotLava03.
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.hotlava03.holosharp.config;

import io.github.hotlava03.holosharp.HoloSharp;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {
    private static FileConfiguration messages = HoloSharp.messages;
    public static final String PREFIX = getString("prefix");
    public static final String ERROR_PREFIX = getString("errorPrefix");
    public static final String CMD_USAGE = getString("cmdUsage");
    public static final String NOT_FOUND = getString("notFound");
    public static final String PLAYER_NOT_FOUND = getString("playerNotFound");
    public static final String DELETED_SUCCESS = getString("deletedSuccess");
    public static final String NO_PERMS = getString("noPerms");
    public static final String RELOAD_SUCCESS = getString("reloadSuccess");
    public static final String CMD_USAGE_ADMIN = getString("cmdUsageAdmin");
    public static final String BUY_HELP = getString("buyHelp")+HoloSharp.getPlugin(HoloSharp.class).getConfig().getDouble("costPerHologram");
    public static final String REMOVE_HELP = getString("removeHelp");
    public static final String ADDLINE_HELP = getString("addLineHelp");
    public static final String ABOUT_HELP = getString("aboutHelp");
    public static final String HOLOLIST_HELP = getString("holoListHelp");
    public static final String HOLOLISTOTHER_HELP = getString("holoListOtherHelp");
    public static final String REMOVE_OTHER_HELP = getString("removeOtherHelp");
    public static final String MOVE_HERE_HELP = getString("moveHereHelp");
    public static final String FATAL = getString("fatal");
    public static final String ADDLINE_SUCCESS = getString("addLineSuccess");
    public static final String LIST_HEADER_1 = getString("listHeader1");
    public static final String LIST_HEADER_2 = getString("listHeader2");
    public static final String LIST_PREFIX = getString("listPrefix");
    public static final String LIST_END = getString("listEnd");
    public static final String LIST_ELEMENT_SEP = getString("listElementSep");
    public static final String LIST_ELEMENT_SEP2 = getString("listElementSep2");
    public static final String BUY_SUCCESS = getString("boughtSuccess");
    public static final String MOVE_SUCCESS = getString("moveSuccess");
    public static final String NO_FACTION = getString("noFaction");
    public static final String STAFF_PREFIX = getString("staffPrefix");
    public static final String NO_ADD_HOLOGRAM_HERE = getString("noAddHologramHere");
    public static final String BAD_BAL = getString("badBal");
    public static final String EXISTS_ALREADY = getString("existsAlready");
    public static final String BYPASSED_CHUNKS = getString("bypassedChunks");
    public static final String BYPASSED_BAL = getString("bypassedBal");

    private static String getString(String element){
        try {
            return messages.get(element).toString().replace("&", "\u00a7");
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        return "(message not found for "+element+") ";
    }
}
