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

package io.github.hotlava03.holosharp.util;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import io.github.hotlava03.holosharp.HoloSharp;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;


public class HologramIdentification{

    private static final FileConfiguration HOLOGRAMS = HoloSharp.holograms;

    public static boolean saveHologram(Player player, Hologram hologram, String name, List<String> lineList) throws IOException {
        if(!(HOLOGRAMS.get("holograms."+player.getName()+"."+name+".coordinates") == null)) return false;
        HOLOGRAMS.set("holograms."+player.getName()+"."+name+"."+"coordinates",hologram.getLocation());
        HOLOGRAMS.set("holograms."+player.getName()+"."+name+"."+"lines",lineList);
        HOLOGRAMS.options().header("Please do not edit this file. To edit players' holograms use the in game provided admin commands. If something breaks contact the developer.");
        HOLOGRAMS.save(HoloSharp.file);
        return true;
    }

    public static Location deleteHologram(String player, String name) throws IOException {
        Location deletedHoloLocation = (Location) HOLOGRAMS.get("holograms."+player+"."+name+".coordinates");
        if(HOLOGRAMS.get("holograms."+player+"."+name+".coordinates") == null) return null;
        HOLOGRAMS.set("holograms."+player+"."+name,null);
        HOLOGRAMS.options().header("Please do not edit this file. To edit players' holograms use the in game provided admin commands. If something breaks contact the developer.");
        HOLOGRAMS.save(HoloSharp.file);
        return deletedHoloLocation;
    }

    public static void addLine(Player player, String name, List<String> lineList){
        HOLOGRAMS.set("holograms."+player.getName()+"."+name+"."+"lines",lineList);
    }

    public static Location getLocation(String player, String name){
        return (Location) HOLOGRAMS.get("holograms."+player+"."+name+".coordinates");
    }

    public static boolean transfer(String name, String oldPlayer, String newPlayer){
        Location location = (Location) HOLOGRAMS.get("holograms."+oldPlayer+"."+name+".coordinates");
        List<String> lines = HOLOGRAMS.getStringList("holograms."+oldPlayer+"."+name+".lines");
        if(HOLOGRAMS.get("holograms."+oldPlayer+"."+name+".coordinates") == null) return false;
        HOLOGRAMS.set("holograms."+oldPlayer+"."+name,null);
        HOLOGRAMS.set("holograms."+newPlayer+"."+name+".coordinates",location);
        HOLOGRAMS.set("holograms."+newPlayer+"."+name+".lines",lines);
        return true;
    }
}
