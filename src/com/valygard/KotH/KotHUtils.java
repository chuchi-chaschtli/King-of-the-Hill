/**
 * KotHUtils.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Anand
 *
 */
public class KotHUtils {
	
	
    public static <E> String formatList(Collection<E> list, boolean none, KotH plugin) {
        if (list == null || list.isEmpty()) {
            return (none ? "You cannot join any arenas." : "");
        }
        
        StringBuffer buffy = new StringBuffer();
        int trimLength = 0;

        E type = list.iterator().next();
        if (type instanceof Player) {
            for (E e : list) {
                buffy.append(((Player) e).getName());
                buffy.append(" ");
            }
        }
        else {
        	for (E e : list) {
        		buffy.append(e.toString());
        		buffy.append(" ");
        	}
        }
            
        return buffy.toString().substring(0, buffy.length() - trimLength);
    }
    
    public static <E> String formatList(Collection<E> list, JavaPlugin plugin) { 
    	return formatList(list, true, (KotH) plugin); 
    }
    
	public static List<String> formatList(String list) {
        List<String> result = new LinkedList<String>();
        if (list == null) return result;
        
        String[] parts = list.trim().split(",");
        
        for (String part : parts)
            result.add(part.trim());
        
        return result;
    }

}
