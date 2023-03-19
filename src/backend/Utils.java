package backend;

import backend.types.ListFunction;

import java.util.List;

public class Utils {

    public static String joinWithComma(List<String> list) {
        return String.join(", ", list);
    }

    public static String joinWithCommaT(List<String> list, ListFunction listFn) {
        return String.join(", ", list.stream().map(listFn::transform).toList());
    }

    public static String formatOrder(List<String> list) {
        return String.join(", ", list.stream().map(Utils::formatOrderItem).toList());
    }

    public static String formatOrderItem(String item) {
        if (item.length() > 1) {
            return item.substring(0, 1).toUpperCase() + item.substring(1).toLowerCase();
        } else {
            return item.toUpperCase();
        }
    }
}
