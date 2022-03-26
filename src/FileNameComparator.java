import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Pattern;

// Custom comparator class that makes sure files are in alpha-numerical order
public class FileNameComparator implements Comparator<String> {
    public final Pattern NUMBERS =
            Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    @Override public int compare(String o1, String o2) {
        if (o1 == null || o2 == null)
            return o1 == null ? o2 == null ? 0 : -1 : 1;

        String[] split1 = NUMBERS.split(o1);
        String[] split2 = NUMBERS.split(o2);
        for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
            char c1 = split1[i].charAt(0);
            char c2 = split2[i].charAt(0);
            int cmp = 0;

            if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
                cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));
            }
            if (cmp == 0) {
                cmp = split1[i].compareTo(split2[i]);
            }

            if (cmp != 0) {
                return cmp;
            }
        }
        return split1.length - split2.length;
    }
}