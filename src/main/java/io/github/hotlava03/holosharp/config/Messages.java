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

public class Messages {
    public static String PREFIX = getString("prefix");
    public static String ERROR_PREFIX = getString("errorPrefix");
    public static String CMD_USAGE = getString("cmdUsage");
    public static String NOT_FOUND = getString("notFound");
    public static String PLAYER_NOT_FOUND = getString("playerNotFound");
    public static String DELETED_SUCCESS = getString("deletedSuccess");
    public static String NO_PERMS = getString("noPerms");
    public static String RELOAD_SUCCESS = getString("reloadSuccess");
    public static String CMD_USAGE_ADMIN = getString("cmdUsageAdmin");
    public static String BUY_HELP = getString("buyHelp")+HoloSharp.getPlugin(HoloSharp.class).getConfig().getDouble("costPerHologram");
    public static String REMOVE_HELP = getString("removeHelp");
    public static String ADDLINE_HELP = getString("addLineHelp");
    public static String ABOUT_HELP = getString("aboutHelp");
    public static String HOLOLIST_HELP = getString("holoListHelp");
    public static String HOLOLISTOTHER_HELP = getString("holoListOtherHelp");
    public static String REMOVE_OTHER_HELP = getString("removeOtherHelp");
    public static String MOVE_HERE_HELP = getString("moveHereHelp");
    public static String FATAL = getString("fatal");
    public static String ADDLINE_SUCCESS = getString("addLineSuccess");
    public static String LIST_HEADER_1 = getString("listHeader1");
    public static String LIST_HEADER_2 = getString("listHeader2");
    public static String LIST_PREFIX = getString("listPrefix");
    public static String LIST_END = getString("listEnd");
    public static String LIST_ELEMENT_SEP = getString("listElementSep");
    public static String LIST_ELEMENT_SEP2 = getString("listElementSep2");
    public static String BUY_SUCCESS = getString("boughtSuccess");
    public static String MOVE_SUCCESS = getString("moveSuccess");
    public static String NO_FACTION = getString("noFaction");
    public static String STAFF_PREFIX = getString("staffPrefix");
    public static String NO_ADD_HOLOGRAM_HERE = getString("noAddHologramHere");
    public static String BAD_BAL = getString("badBal");
    public static String EXISTS_ALREADY = getString("existsAlready");
    public static String BYPASSED_CHUNKS = getString("bypassedChunks");
    public static String BYPASSED_BAL = getString("bypassedBal");
    public static String BYPASSED_LIMIT = getString("bypassedLimit");
    public static String TRANSFER_HELP = getString("transferHelp");
    public static String TRANSFER_ALL_HELP = getString("transferAllHelp");
    public static String TRANSFER_SUCCESS = getString("transferSuccess");
    public static String TRANSFER_ALL_SUCCESS = getString("transferAllSuccess");
    public static String TOO_MANY_HOLOGRAMS = getString("tooManyHolograms");
    public static String DELETE_LINE_HELP = getString("deleteLineHelp");
    public static String DELETE_LINE_OTHER_HELP = getString("deleteLineOtherHelp");
    public static String INVALID_NUM = getString("invalidNum");
    public static String DELETE_LINE_SUCCESS = getString("deleteLineSuccess");
    public static String INVALID_LINE = getString("invalidLine");
    public static String LAST_LINE = getString("lastLine");
    public static String CREATE_HELP = getString("createHelp");
    public static String CREATE_SUCCESS = getString("createSuccess");

    private static String getString(String element){
        try {
            return HoloSharp.messages.get(element).toString().replace("&", "\u00a7");
        }catch(NullPointerException e){
            HoloSharp.getPlugin(HoloSharp.class).getLogger().warning("Element "+element+" not found in messages.yml. Please add this element in messages.yml or delete messages.yml and reload the plugin.");
        }
        return "(message not found for "+element+") ";
    }

    public static void reload(){
        PREFIX = getString("prefix");
        ERROR_PREFIX = getString("errorPrefix");
        CMD_USAGE = getString("cmdUsage");
        NOT_FOUND = getString("notFound");
        PLAYER_NOT_FOUND = getString("playerNotFound");
        DELETED_SUCCESS = getString("deletedSuccess");
        NO_PERMS = getString("noPerms");
        RELOAD_SUCCESS = getString("reloadSuccess");
        CMD_USAGE_ADMIN = getString("cmdUsageAdmin");
        BUY_HELP = getString("buyHelp")+HoloSharp.getPlugin(HoloSharp.class).getConfig().getDouble("costPerHologram");
        REMOVE_HELP = getString("removeHelp");
        ADDLINE_HELP = getString("addLineHelp");
        ABOUT_HELP = getString("aboutHelp");
        HOLOLIST_HELP = getString("holoListHelp");
        HOLOLISTOTHER_HELP = getString("holoListOtherHelp");
        REMOVE_OTHER_HELP = getString("removeOtherHelp");
        MOVE_HERE_HELP = getString("moveHereHelp");
        FATAL = getString("fatal");
        ADDLINE_SUCCESS = getString("addLineSuccess");
        LIST_HEADER_1 = getString("listHeader1");
        LIST_HEADER_2 = getString("listHeader2");
        LIST_PREFIX = getString("listPrefix");
        LIST_END = getString("listEnd");
        LIST_ELEMENT_SEP = getString("listElementSep");
        LIST_ELEMENT_SEP2 = getString("listElementSep2");
        BUY_SUCCESS = getString("boughtSuccess");
        MOVE_SUCCESS = getString("moveSuccess");
        NO_FACTION = getString("noFaction");
        STAFF_PREFIX = getString("staffPrefix");
        NO_ADD_HOLOGRAM_HERE = getString("noAddHologramHere");
        BAD_BAL = getString("badBal");
        EXISTS_ALREADY = getString("existsAlready");
        BYPASSED_CHUNKS = getString("bypassedChunks");
        BYPASSED_BAL = getString("bypassedBal");
        BYPASSED_LIMIT = getString("bypassedLimit");
        TRANSFER_HELP = getString("transferHelp");
        TRANSFER_ALL_HELP = getString("transferAllHelp");
        TRANSFER_SUCCESS = getString("transferSuccess");
        TRANSFER_ALL_SUCCESS = getString("transferAllSuccess");
        TOO_MANY_HOLOGRAMS = getString("tooManyHolograms");
        DELETE_LINE_HELP = getString("deleteLineHelp");
        DELETE_LINE_OTHER_HELP = getString("deleteLineOtherHelp");
        INVALID_NUM = getString("invalidNum");
        DELETE_LINE_SUCCESS = getString("deleteLineSuccess");
        INVALID_LINE = getString("invalidLine");
        LAST_LINE = getString("lastLine");
        CREATE_HELP = getString("createHelp");
        CREATE_SUCCESS = getString("createSuccess");
    }
}
