/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author lavz24
 */
public class ValueComparator  {

   public static class MapStringIntegerComparator implements Comparator {

        Map<String,Integer>  base;

        public MapStringIntegerComparator(Map<String,Integer> base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
        if ((Integer) base.get(a) == (Integer) base.get(b)) {
            return 0;
        } else if((Integer) base.get(a) > (Integer) base.get(b)) {
            return -1;
        }else{
            return 1;
        }
    }
        
        
    }
}
