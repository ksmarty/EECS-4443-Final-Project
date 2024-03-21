package ca.yorku.eecs4443_finalproject_golf;
import android.content.Context;
import android.content.SharedPreferences;
public class SharedPref {
    private static final String PREF_NAME = "CSVData";
    private static final String KEY_CSV_DATA = "csvData";

    /**
     * Overwrite and save new data.
     * @param context
     * @param csvData
     */
    public static void saveCSVData(Context context, String csvData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CSV_DATA, csvData.trim());
        editor.apply();
    }

    /**
     * Return saved csv data from Shared Preferences.
     * @param context
     * @return csvData
     */
    public static String getCSVData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_CSV_DATA, "").trim();
    }


    /**
     * Append on top of current saved data.
     * @param context
     * @param csvData
     */
    public static void appendAndSaveCSVData(Context context, String csvData) {
        String previousData = getCSVData(context);
        String combinedBuilder = previousData.trim() + "\n" + csvData.trim();
        saveCSVData(context, combinedBuilder);
    }


    /**
     * Clear current saved data.
     * @param context
     */
    public static void clearCSVData(Context context) {
        saveCSVData(context, "");
    }

    public static boolean hasSavedData(Context context) {
        return getCSVData(context).length() > 0;
    }
}
